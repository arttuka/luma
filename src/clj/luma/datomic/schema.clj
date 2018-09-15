(ns luma.datomic.schema
  (:require [datomic-schema.schema :as s]))

(def parts
  [(s/part "luma")])

(def schema
  [(s/schema genre
     (s/fields
       [dbo_uri :uri :indexed :unique-identity]
       [title :string :indexed]
       [subgenre :ref :many]
       [fusiongenre :ref :many]
       [derivative :ref :many]))])

(defn generate-schema []
  (concat
    (s/generate-parts parts)
    (s/generate-schema schema)))
