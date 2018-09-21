(ns luma.db
  (:require [mount.core :refer [defstate]]
            [clojure.set :refer [union difference]]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            clj-time.jdbc
            [config.core :refer [env]]
            [hikari-cp.core :as hikari]))

(def datasource-options {:adapter       "postgresql"
                         :username      (env :postgresql-user)
                         :password      (env :postgresql-password)
                         :database-name (env :postgresql-database)
                         :server-name   (env :postgresql-server)
                         :port-number   (env :postgresql-port)})

(defstate db
  :start (hikari/make-datasource datasource-options)
  :stop (hikari/close-datasource @db))

(def ^:dynamic *tx* nil)

(defmacro with-transaction [& body]
  `(jdbc/with-db-transaction [tx# {:datasource @db}]
     (binding [*tx* tx#]
       ~@body)))

(defn rollback! [tx]
  (jdbc/db-set-rollback-only! tx))

(defn ^:private get-ids [tx table key ids]
  (into #{}
        (comp (map key) (filter ids))
        (jdbc/query tx [(str "SELECT " (name key) " FROM " (name table))])))

(defn get-albums [tx ids]
  (get-ids tx :album :id ids))

(defn get-artists [tx ids]
  (get-ids tx :artist :id ids))

(defn get-existing-tags [tx tags]
  (get-ids tx :tag :tag tags))

(defn get-new-tags [tx tags]
  (difference tags (get-existing-tags tx tags)))

(defn save-tags! [tx artists albums]
  (let [tags (into #{} (mapcat :tags) (concat artists albums))
        new-tags (get-new-tags tx tags)]
    (jdbc/insert-multi! tx :artist (for [{id :id} artists] {:id id}))
    (jdbc/insert-multi! tx :album (for [{id :id} albums] {:id id}))
    (jdbc/insert-multi! tx :album_artist (for [{:keys [id artists]} albums
                                               artist artists]
                                           {:album id, :artist artist}))
    (jdbc/insert-multi! tx :tag (for [tag new-tags] {:tag tag}))
    (jdbc/insert-multi! tx :album_tag (for [{:keys [id tags]} albums
                                            tag tags]
                                        {:album id, :tag tag}))
    (jdbc/insert-multi! tx :artist_tag (for [{:keys [id tags]} artists
                                             tag tags]
                                         {:artist id, :tag tag}))))

(def tags-xf (comp (map (juxt :album :tag))
                   (partition-by first)
                   (map (fn [tags] [(ffirst tags) (map second tags)]))))

(defn get-tags [tx albums]
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
    (into {} tags-xf (jdbc/query tx (into [query] args)))))
