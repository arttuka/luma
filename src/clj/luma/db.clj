(ns luma.db
  (:require [mount.core :refer [defstate]]
            [clojure.set :refer [union]]
            [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [clj-time.core :as time]
            clj-time.jdbc
            [hikari-cp.core :as hikari]
            [luma.util :refer [grouping]]))

(def datasource-options {:adapter       "postgresql"
                         :username      "luma"
                         :password      "luma"
                         :database-name "luma"
                         :server-name   "localhost"
                         :port-number   5432})

(defstate db
  :start (hikari/make-datasource datasource-options)
  :stop (hikari/close-datasource @db))

(def ^:dynamic *db* nil)

(defmacro with-transaction [& body]
  `(jdbc/with-db-transaction [conn# {:datasource @db}]
     (binding [*db* conn#]
       ~@body)))

(defn get-account [id]
  (with-transaction
    (let [result (jdbc/query *db* ["SELECT * FROM account WHERE id = ?" id])]
      (first result))))

(defn update-account [id data]
  (let [save-data (select-keys data [:access_token :refresh_token :expiration])]
    (with-transaction
      (jdbc/update! *db* :account save-data ["id = ?" id]))))

(defn save-account [id data]
  (let [save-data (select-keys data [:access_token :refresh_token :expiration])]
    (with-transaction
      (let [existing (get-account id)]
        (if existing
          (update-account id save-data)
          (jdbc/insert! *db* :account (assoc save-data :id id)))))))

(defn save-albums [user-id albums]
  (with-transaction
    (let [conn (jdbc/get-connection *db*)
          select-album (jdbc/prepare-statement conn "SELECT * FROM album WHERE id = ?")
          select-artist (jdbc/prepare-statement conn "SELECT id, name FROM artist WHERE id = ?")
          insert-artist (jdbc/prepare-statement conn "INSERT INTO artist (name, id) VALUES (?, ?)")
          update-artist (jdbc/prepare-statement conn "UPDATE artist SET name = ? WHERE id = ?")
          insert-album (jdbc/prepare-statement conn "INSERT INTO album (title, uri, image, id) VALUES (?, ?, ?, ?)")
          update-album (jdbc/prepare-statement conn "UPDATE album SET title = ?, uri = ?, image = ? WHERE id = ?")]
      (letfn [(save-artists [album-id artists]
                (doseq [artist artists
                        :let [saved-artist (first (jdbc/query *db* [select-artist (:id artist)]))]]
                  (jdbc/execute! *db* [(if saved-artist update-artist insert-artist) (:name artist) (:id artist)]))
                (jdbc/delete! *db* :album_artist ["album = ?" album-id])
                (jdbc/insert-multi! *db* :album_artist (for [artist artists] {:album album-id, :artist (:id artist)})))
              (save-album [album]
                (let [saved (first (jdbc/query *db* [select-album (:id album)]))]
                  (jdbc/execute! *db* [(if saved update-album insert-album) (:title album) (:uri album) (:image album) (:id album)])
                  (save-artists (:id album) (:artists album))))
              (save-user-albums [album-ids]
                (jdbc/delete! *db* :account_album ["account = ?" user-id])
                (jdbc/insert-multi! *db* :account_album (for [album-id album-ids] {:account user-id, :album album-id})))]
        (run! save-album albums)
        (save-user-albums (map :id albums))
        (jdbc/update! *db* :account {:last_loaded (time/now)} ["id = ?" user-id])))))

(defn get-albums [user-id]
  (with-transaction
    (let [data (jdbc/query *db* ["WITH album_tags AS (
                                    SELECT album, tag
                                    FROM album_tag
                                    UNION
                                    SELECT album, tag
                                    FROM album_artist
                                    JOIN artist_tag ON album_artist.artist = artist_tag.artist
                                  )
                                  SELECT album.id, album.title, album.uri, album.image, artist.id artist_id, artist.name, album_tags.tag
                                  FROM album
                                  JOIN album_artist ON album.id = album_artist.album
                                  JOIN artist ON artist.id = album_artist.artist
                                  JOIN account_album ON album.id = account_album.album
                                  JOIN album_tags ON album.id = album_tags.album
                                  WHERE account_album.account = ?"
                                 user-id])]
      (->> data
           (grouping [:title :uri :image :id :artist_id :name] :tags)
           (map #(update % :tags (partial map :tag)))
           (grouping [:title :uri :image :id :tags] :artists)))))

(defn save-album-tags [album tags]
  (with-transaction
    (let [tags (map str/lower-case tags)
          existing-tags (set (map :tag (jdbc/query *db* "SELECT tag FROM tag")))
          new-tags (remove existing-tags tags)]
      (jdbc/insert-multi! *db* :tag (vec (for [tag new-tags] {:tag tag})))
      (jdbc/delete! *db* :album_tag ["album = ?" album])
      (jdbc/insert-multi! *db* :album_tag (for [tag tags] {:tag tag, :album album})))))

(defn save-artist-tags [artist tags]
  (with-transaction
    (let [tags (map str/lower-case tags)
          existing-tags (set (map :tag (jdbc/query *db* "SELECT tag FROM tag")))
          new-tags (remove existing-tags tags)]
      (jdbc/insert-multi! *db* :tag (for [tag new-tags] {:tag tag}))
      (jdbc/delete! *db* :artist_tag ["artist = ?" artist])
      (jdbc/insert-multi! *db* :artist_tag (for [tag tags] {:tag tag, :artist artist})))))

(defn get-tags [artist album]
  (with-transaction
    (jdbc/query *db* ["SELECT tag
                       FROM album_tag
                       JOIN album ON album.id = album_tag.album
                       JOIN album_artist ON album_artist.album = album.id
                       JOIN artist ON artist.id = album_artist.artist
                       WHERE album.title = ? OR artist.name = ?
                       UNION ALL
                       SELECT tag
                       FROM artist_tag
                       JOIN artist ON artist.id = artist_tag.artist
                       WHERE artist.name = ?"
                      album artist artist])))
