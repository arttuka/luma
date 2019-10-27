(ns luma.transit
  (:refer-clojure :exclude [read])
  (:require [cognitect.transit :as transit]
            [taoensso.sente.packers.transit :as sente-transit]
            #?(:cljs [goog.string :as gs])
            #?(:cljs goog.date.UtcDateTime))
  (:import #?(:clj  (org.joda.time DateTime DateTimeZone)
              :cljs (goog.date UtcDateTime))
           #?(:clj (java.io ByteArrayOutputStream ByteArrayInputStream))))

(defn read-date-time
  "Read RFC3339 string to DateTime."
  [s]
  #?(:clj  (DateTime/parse s)
     :cljs (UtcDateTime.fromIsoString s)))

(defn write-date-time
  "Represent DateTime in RFC3339 format string."
  [d]
  #?(:clj  (.toString (.withZone ^DateTime d (DateTimeZone/forID "UTC")))
     :cljs (str (.getUTCFullYear d)
                "-" (gs/padNumber (inc (.getUTCMonth d)) 2)
                "-" (gs/padNumber (.getUTCDate d) 2)
                "T" (gs/padNumber (.getUTCHours d) 2)
                ":" (gs/padNumber (.getUTCMinutes d) 2)
                ":" (gs/padNumber (.getUTCSeconds d) 2)
                "." (gs/padNumber (.getUTCMilliseconds d) 3)
                "Z")))

(def writers {#?(:clj DateTime, :cljs UtcDateTime)
              (transit/write-handler (constantly "datetime")
                                     write-date-time
                                     write-date-time)})

(def readers {"datetime" read-date-time})

(def packer (sente-transit/->TransitPacker :json
                                           {:handlers writers}
                                           {:handlers readers}))

(defn write [x]
  #?(:clj  (let [out (ByteArrayOutputStream.)
                 writer (transit/writer out :json {:handlers writers})]
             (transit/write writer x)
             (.toString out "UTF-8"))
     :cljs (let [writer (transit/writer :json {:handlers writers})]
             (transit/write writer x))))

(defn read [^String s]
  #?(:clj  (let [in (ByteArrayInputStream. (.getBytes s "UTF-8"))
                 reader (transit/reader in :json {:handlers readers})]
             (transit/read reader))
     :cljs (let [reader (transit/reader :json {:handlers readers})]
             (transit/read reader s))))
