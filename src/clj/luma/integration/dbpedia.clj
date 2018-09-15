(ns luma.integration.dbpedia
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [luma.util :refer [map-values]])
  (:import (java.net URI)))

(def dbo {:musicSubgenre     (URI. "http://dbpedia.org/ontology/musicSubgenre")
          :musicFusionGenre  (URI. "http://dbpedia.org/ontology/musicFusionGenre")
          :derivative        (URI. "http://dbpedia.org/ontology/derivative")
          :wikiPageRedirects (URI. "http://dbpedia.org/ontology/wikiPageRedirects")})

(defn ^:private format-sparql-value [{:keys [type datatype value]}]
  (case type
    "uri" (URI. value)
    "literal" value
    "bnode" value
    "typed-literal" (case datatype
                      "http://www.w3.org/2001/XMLSchema#integer" (Integer/parseInt value))))

(defn ^:private format-sparql-results [results]
  (for [binding (get-in results [:results :bindings])]
    (map-values binding format-sparql-value)))

(defn run-query [query]
  (let [response @(http/post "http://dbpedia.org/sparql"
                             {:form-params {:query query}
                              :headers     {"Content-Type" "application/x-www-form-urlencoded"
                                            "Accept"       "application/sparql-results+json"}})]
    (if (= 200 (:status response))
      (format-sparql-results (json/parse-string (:body response) true))
      (throw (ex-info "Error with SPARQL query" response)))))

(def genre-query "
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX foaf: <http://xmlns.com/foaf/0.1/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT * {
  {
    SELECT ?id ?label ?name (\"genre\" AS ?type) WHERE {
      ?id a dbo:Genre, dbo:MusicGenre ;
          rdfs:label ?label .
      FILTER(langMatches(lang(?label), \"EN\"))
      OPTIONAL {
        ?id foaf:name ?name .
        FILTER(langMatches(lang(?name), \"EN\"))
      }
    }
  } UNION {
    SELECT ?id ?label ?name (\"redirect\" AS ?type) WHERE {
      ?id a dbo:Genre, dbo:MusicGenre .
      ?redirect dbo:wikiPageRedirects ?id ;
                rdfs:label ?label .
      FILTER(langMatches(lang(?label), \"EN\"))
      OPTIONAL {
        ?redirect foaf:name ?name .
        FILTER(langMatches(lang(?name), \"EN\"))
      }
    }
  }
}")

(defn get-genres []
  (run-query genre-query))

(def links-query "
PREFIX dbo: <http://dbpedia.org/ontology/>

SELECT * {
  {
    SELECT ?s ?p ?o WHERE {
      ?o a dbo:MusicGenre, dbo:Genre .
      VALUES ?p { dbo:musicSubgenre dbo:musicFusionGenre dbo:derivative } .
      ?s ?p ?o ;
         a dbo:MusicGenre, dbo:Genre .
      FILTER(?s != ?o)
    }
  } UNION {
    SELECT ?s (dbo:wikiPageRedirects AS ?p) ?o WHERE {
      ?s a dbo:MusicGenre, dbo:Genre .
      ?o dbo:wikiPageRedirects ?s .
      FILTER(?s != ?o)
    }
  }
} ORDER BY ?s ?p ?o")

(defn get-links []
  (run-query links-query))
