(ns luma.datomic
  (:require [mount.core :refer [defstate]]
            [datomic.api :as d]
            [luma.datomic.schema :as schema]
            [luma.integration.dbpedia :as dbpedia :refer [dbo]]))

(def db-uri "datomic:dev://localhost:4334/luma")

(defstate connection :start (d/connect db-uri))

(defn ->dbid [uri]
  [:genre/dbo_uri uri])

(defn setup-db! []
  (let [genres (for [genre (dbpedia/get-genres)]
                 #:genre{:dbo_uri (:id genre)
                         :title   (:label genre)})
        genre-exists? (into #{} (map :genre/dbo_uri) genres)
        links (for [[s ls] (group-by :s (dbpedia/get-links))
                    :when (genre-exists? s)
                    :let [outgoing (apply merge-with concat (for [{:keys [p o]} ls]
                                                              {p [o]}))]]
                {:db/id             [:genre/dbo_uri s]
                 :genre/subgenre    (map ->dbid (get outgoing (dbo :musicSubgenre)))
                 :genre/fusiongenre (map ->dbid (get outgoing (dbo :musicFusionGenre)))
                 :genre/derivative  (map ->dbid (get outgoing (dbo :derivative)))})]
    (d/transact @connection (concat (schema/generate-schema) genres links))))
