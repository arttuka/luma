(ns luma.integration.oauth2
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]))

(defn http-get
  ([url access-token]
   (http-get url access-token {}))
  ([url access-token options]
   (let [response @(http/get url (-> options
                                   (assoc :as :text)
                                   (update :headers assoc "Authorization" (str "Bearer " access-token))))]
     (if (#{200 201 204} (:status response))
       (json/parse-string (:body response) true)
       (throw (ex-info "HTTP error" response))))))
