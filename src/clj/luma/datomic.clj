(ns luma.datomic
  (:require [mount.core :refer [defstate]]
            [datomic.api :as d]
            [taoensso.timbre :as log]
            [luma.datomic.schema :as schema]
            [luma.integration.dbpedia :as dbpedia :refer [dbo]]
            [luma.util :refer [map-by map-values]]
            [clojure.string :as str]))

(def db-uri "datomic:dev://localhost:4334/luma")

(defstate connection :start (d/connect db-uri))

(defn ^:private alt-labels [genre redirects]
  (filter some? (into [(:name genre)] (mapcat (juxt :label :name)) redirects)))

(defn process-genres [genres]
  (let [{real-genres "genre"
         redirects   "redirect"} (group-by :type genres)
        id->redirects (group-by :id redirects)]
    (for [genre real-genres]
      (assoc genre :alt-labels (alt-labels genre (id->redirects (:id genre)))))))

(defn setup-db! []
  (let [id (atom 0)
        next-id #(swap! id dec)
        genres (for [genre (process-genres (dbpedia/get-genres))]
                 {:db/id           (d/tempid :db.part/user (next-id))
                  :genre/dbo_uri   (:id genre)
                  :genre/title     (:label genre)
                  :genre/alt_title (:alt-labels genre)})
        genre->id (map-by :genre/dbo_uri :db/id genres)
        links (for [[s ls] (group-by :s (dbpedia/get-links))
                    :when (contains? genre->id s)
                    :let [outgoing (apply merge-with concat (for [{:keys [p o]} ls]
                                                              {p [o]}))]]
                {:db/id             (genre->id s)
                 :genre/subgenre    (map genre->id (get outgoing (dbo :musicSubgenre)))
                 :genre/fusiongenre (map genre->id (get outgoing (dbo :musicFusionGenre)))
                 :genre/derivative  (map genre->id (get outgoing (dbo :derivative)))})]
    (d/transact @connection (schema/generate-schema))
    (d/transact @connection (concat genres links))))

(defn get-db []
  (d/db @connection))

(defn has-tags? [db album-id]
  (some->
    (d/q '[:find (count ?tag) .
           :in $ ?albumId
           :where
           [?album :album/id ?albumId]
           [?album :album/tag ?tag]]
         db album-id)
    pos?))

(defn get-new-tags-ids [db tx-data]
  (d/q '[:find [?e ...]
         :in $ [[?e ?a ?v _ ?added]]
         :where
         [?e ?a ?v _ ?added]
         [?a :db/ident :tag/name]
         [(= ?added true)]]
       db
       tx-data))

(defn connect-tags-and-genres [db tags]
  (d/q '[:find ?genre ?tag
         :in $ [?tag ...]
         :where
         [?tag :tag/name ?tagName]
         (or [?genre :genre/title ?title]
             [?genre :genre/alt_title ?title])
         [(.toLowerCase ^String ?title) ?lowercaseTitle]
         [(= ?lowercaseTitle ?tagName)]]
       db
       tags))

(defn check-unconnected-tags! [new-tags connections]
  (let [connected (into #{} (map second) connections)
        unconnected (remove connected new-tags)]
    (when (seq unconnected)
      (log/warnf "Some tags weren't connected: %s" (str/join ", " unconnected)))))

(defn save-albums! [artists albums]
  (let [id (atom 0)
        next-id #(swap! id dec)
        tag-entities (for [tag (distinct (concat (mapcat val artists)
                                                 (mapcat (comp :tags val) albums)))]
                       {:db/id    (d/tempid :db.part/user (next-id))
                        :tag/name tag})
        tag->id (map-by :tag/name :db/id tag-entities)
        artist-entities (for [[artist-id tags] artists]
                          {:db/id      (d/tempid :db.part/user (next-id))
                           :artist/id  artist-id
                           :artist/tag (map tag->id tags)})
        artist->id (map-by :artist/id :db/id artist-entities)
        album-entities (for [[album-id {:keys [artists tags]}] albums]
                         {:album/id     album-id
                          :album/artist (map artist->id artists)
                          :album/tag    (map tag->id tags)})
        tx @(d/transact @connection (concat
                                      tag-entities
                                      artist-entities
                                      album-entities))
        new-tags (get-new-tags-ids (:db-after tx) (:tx-data tx))
        connections (connect-tags-and-genres (:db-after tx) new-tags)]
    (check-unconnected-tags! new-tags connections)
    (d/transact @connection (for [[genre tag] connections]
                              [:db/add genre :genre/tag tag]))))

(defn get-genres [db album-ids]
  (let [genre->album (into {} (d/q '[:find ?genre (distinct ?albumId)
                                     :in $ [?albumId ...]
                                     :where
                                     [?album :album/id ?albumId]
                                     (or-join [?tag ?album]
                                              [?album :album/tag ?tag]
                                              (and [?album :album/artist ?artist]
                                                   [?artist :artist/tag ?tag]))
                                     [?genre :genre/tag ?tag]]
                                   db album-ids))
        genres (d/q '[:find [(pull ?genre [:db/id :genre/title :genre/dbo_uri]) ...]
                      :in $ [?genre ...]]
                    db (keys genre->album))]
    (for [{:keys [db/id genre/title]} genres]
      {:id     id
       :title  title
       :albums (genre->album id)})))
