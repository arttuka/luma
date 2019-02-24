(ns luma.db
  (:require [mount.core :refer [defstate]]
            [clojure.set :refer [union difference]]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            clj-time.jdbc
            [config.core :refer [env]]
            [hikari-cp.core :as hikari]
            [luma.util :refer [group-by-kv map-by]]))

(def datasource-options {:adapter       "postgresql"
                         :username      (env :db-user)
                         :password      (env :db-password)
                         :database-name (env :db-name)
                         :server-name   (env :db-host)
                         :port-number   (env :db-port)})

(defstate db
  :start (hikari/make-datasource datasource-options)
  :stop (hikari/close-datasource @db))

(def ^:dynamic *tx* nil)

(defmacro with-transaction [& body]
  `(jdbc/with-db-transaction [tx# {:datasource @db}]
     (binding [*tx* tx#]
       ~@body)))

(defn ^:private get-ids [table key ids]
  (into #{}
        (comp (map key) (filter ids))
        (jdbc/query *tx* [(str "SELECT " (name key) " FROM " (name table))])))

(defn get-albums [ids]
  (get-ids :album :id ids))

(defn get-artists [ids]
  (get-ids :artist :id ids))

(defn get-existing-tags [tags]
  (get-ids :tag :tag tags))

(defn get-new-tags [tags]
  (difference tags (get-existing-tags tags)))

(defn save-tags! [artists albums]
  (let [tags (into #{} (mapcat :tags) (concat artists albums))
        new-tags (get-new-tags tags)]
    (jdbc/insert-multi! *tx* :artist (for [{id :id} artists] {:id id}))
    (jdbc/insert-multi! *tx* :album (for [{id :id} albums] {:id id}))
    (jdbc/insert-multi! *tx* :album_artist (for [{:keys [id artists]} albums
                                                 artist artists]
                                             {:album id, :artist artist}))
    (jdbc/insert-multi! *tx* :tag (for [tag new-tags] {:tag tag}))
    (jdbc/insert-multi! *tx* :album_tag (for [{:keys [id tags]} albums
                                              tag tags]
                                          {:album id, :tag tag}))
    (jdbc/insert-multi! *tx* :artist_tag (for [{:keys [id tags]} artists
                                               tag tags]
                                           {:artist id, :tag tag}))))

(defn get-tags [albums]
  (let [qs (str/join "," (repeat (count albums) \?))
        query (format "SELECT album, tag
                         FROM album_tag
                         WHERE album IN (%s)
                         UNION
                         SELECT album, tag
                         FROM artist_tag at
                         JOIN album_artist aa ON at.artist = aa.artist
                         WHERE album IN (%s)
                         ORDER BY album, tag"
                      qs qs)
        args (concat albums albums)]
    (group-by-kv :album :tag (jdbc/query *tx* (into [query] args)))))

(defn get-playcounts [username]
  (let [query "SELECT album, playcount, updated
                 FROM album_playcount
                 WHERE username = ?"
        results (jdbc/query *tx* [query username])]
    (map-by :album #(dissoc % :album) results)))

(defn save-playcounts! [username playcounts]
  (jdbc/execute! *tx* ["INSERT INTO lastfm_user(username) VALUES (?) ON CONFLICT DO NOTHING" username])
  (doseq [{:keys [album playcount]} playcounts]
    (jdbc/execute! *tx* ["INSERT INTO album_playcount (username, album, playcount)
                        VALUES (?, ?, ?)
                        ON CONFLICT (username, album) DO UPDATE
                        SET playcount = ?, updated = now()"
                         username album playcount playcount])))

(defn erase-lastfm-data! [username]
  (jdbc/delete! *tx* :album_playcount ["username = ?" username])
  (jdbc/delete! *tx* :lastfm_user ["username = ?" username]))
