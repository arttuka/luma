(ns luma.db
  (:require [mount.core :refer [defstate]]
            [clojure.java.jdbc :as jdbc]
            clj-time.jdbc
            [hikari-cp.core :as hikari]))

(def datasource-options {:adapter       "postgresql"
                         :username      "luma"
                         :password      "luma"
                         :database-name "luma"
                         :server-name   "localhost"
                         :port-number   5432})

(defstate db
  :start (hikari/make-datasource datasource-options)
  :stop (hikari/close-datasource db))

(def ^:dynamic *db* nil)

(defmacro with-transaction [& body]
  `(jdbc/with-db-transaction [conn# {:datasource db}]
     (binding [*db* conn#]
       ~@body)))

(defn get-account [id]
  (with-transaction
    (let [result (jdbc/query *db* ["SELECT * FROM account WHERE id = ?" id])]
      (first result))))

(defn save-account [id data]
  (let [save-data (select-keys data [:access_token :refresh_token :expiration])]
    (with-transaction
      (let [existing (get-account id)]
        (if existing
          (jdbc/update! *db* :account save-data ["id = ?" id])
          (jdbc/insert! *db* :account (assoc save-data :id id)))))))
