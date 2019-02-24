(ns luma.integration.oauth2
  (:require [aleph.http :as http]
            [byte-streams :as bs]
            [cheshire.core :as json]))

(defn http-get
  ([url access-token]
   (http-get url access-token {}))
  ([url access-token options]
   (let [response @(http/get url (update options :headers assoc "Authorization" (str "Bearer " access-token)))]
     (if (#{200 201 204} (:status response))
       (-> (:body response)
           bs/to-string
           (json/parse-string true))
       (throw (ex-info "HTTP error" response))))))
