(ns luma.datomic.schema
  (:require [datomic-schema.schema :as s]))

(def parts
  [(s/part "luma")])

(def schema
  [(s/schema genre
     (s/fields
       [dbo_uri :uri :indexed :unique-identity]
       [title :string :indexed]
       [alt_title :string :indexed :many]
       [subgenre :ref :many]
       [fusiongenre :ref :many]
       [derivative :ref :many]
       [tag :ref :many]))

   (s/schema artist
     (s/fields
       [id :string :indexed :unique-identity]
       [tag :ref :many]))

   (s/schema album
     (s/fields
       [id :string :indexed :unique-identity]
       [artist :ref :many]
       [tag :ref :many]))

   (s/schema tag
     (s/fields
       [name :string :indexed :unique-identity]))])

(defn generate-schema []
  (concat
    (s/generate-parts parts)
    (s/generate-schema schema)))
