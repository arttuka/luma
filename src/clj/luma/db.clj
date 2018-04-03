(ns luma.db
  (:require [mount.core :refer [defstate]]
            [clojure.set :refer [union]]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            clj-time.jdbc
            [config.core :refer [env]]
            [hikari-cp.core :as hikari]
            [luma.util :refer [grouping]]))

(def datasource-options {:adapter       "postgresql"
                         :username      (env :postgresql-user)
                         :password      (env :postgresql-password)
                         :database-name (env :postgresql-database)
                         :server-name   (env :postgresql-server)
                         :port-number   (env :postgresql-port)})

(defstate db
  :start (hikari/make-datasource datasource-options)
  :stop (hikari/close-datasource @db))

(def ^:dynamic *db* nil)

(defmacro with-transaction [& body]
  `(jdbc/with-db-transaction [conn# {:datasource @db}]
     (binding [*db* conn#]
       ~@body)))

(defn save-album-tags [artist title tags]
  (with-transaction
    (let [tags (map str/lower-case tags)]
      (jdbc/delete! *db* :album_tag ["title = ?" title])
      (if (seq tags)
        (jdbc/insert-multi! *db* :album_tag (for [tag tags] {:tag tag, :title title, :artist artist}))
        (jdbc/insert! *db* :album_tag {:tag "", :title title, :artist artist})))))

(defn save-artist-tags [artist tags]
  (with-transaction
    (let [tags (map str/lower-case tags)]
      (jdbc/delete! *db* :artist_tag ["artist = ?" artist])
      (jdbc/insert-multi! *db* :artist_tag (for [tag tags] {:tag tag, :artist artist})))))

(defn has-tags? [artists title]
  (with-transaction
    (let [qs (str/join "," (repeat (count artists) \?))
          query (format "SELECT COUNT(*) > 0 AS cnt
                         FROM album_tag
                         WHERE artist IN (%s) AND title = ?"
                        qs)
          args (concat artists [title])]
      (-> (jdbc/query *db* (into [query] args))
        first
        :cnt))))

(defn get-tags [artists title]
  (with-transaction
    (let [qs (str/join "," (repeat (count artists) \?))
          query (format "SELECT tag
                         FROM album_tag
                         WHERE title = ? AND artist IN (%s) AND tag != ''
                         UNION
                         SELECT tag
                         FROM artist_tag
                         WHERE artist IN (%s)"
                        qs qs)
          args (concat [title] artists artists)]
      (map :tag (jdbc/query *db* (into [query] args))))))
