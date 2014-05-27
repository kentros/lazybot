(ns lazybot.plugins.dumbot
  (:use lazybot.registry)
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :as string])
  (:import (java.util Date)
           (java.net URLEncoder)))

(use '[clojure.java.shell :only [sh]])

(defn app-id [bot] (-> bot :config :dumbot :parse-app-id))
(defn api-key [bot] (-> bot :config :dumbot :parse-api-key))
(defn yelp-id [bot] (-> bot :config :dumbot :yelp-id))

(defn today [] (.format (java.text.SimpleDateFormat. "MM/dd") (Date.)))

(defn query-finance [x] (str (str (second (re-find #"<title>(.*?)<\/title>" (slurp x)))) ": " x))

(defn parse-business [j] (let [b (get-in (json/parse-string j) ["businesses" 0])] 
  (str (get-in b ["name"]) " [" (get-in b ["avg_rating"]) " stars] - URL: " (get-in b ["url"]) " | Review: " 
       (get-in b ["reviews" 0 "text_excerpt"]))))

(defn suburb-define [bot w] (get-in (json/parse-string (:body (http/post "https://api.parse.com/1/functions/define"
  {:body (str "{\"word\": \"" w "\"}")
   :headers {"X-Parse-Application-Id" (app-id @bot)
             "X-Parse-REST-API-Key" (api-key @bot)}
   :content-type :json
   :accept :json}))) ["result"]))

(defn chat [s] (last (re-find #"<that>(.*?)</that>"
  (slurp (str "http://www.pandorabots.com/pandora/talk-xml?botid=e677ae285e345411&custid=dab7dadbaf3ed558&input=" (URLEncoder/encode s "UTF-8"))))))

(defplugin
  (:cmd
   "Hello, you can tell me anything, and I will talk to you."
   #{"dumbot"}
   (fn [{:keys [args] :as com-m}]
     (send-message com-m
                   (chat (string/join " " args)))))

  (:cmd
   "Displays the closest highest rated business matching your query from Yelp, along with URL and a random review."
   #{"yelp" "bbb" "extort"}
   (fn [{:keys [bot args] :as com-m}]
     (send-message com-m
                   (parse-business (slurp (str "http://api.yelp.com/business_review_search?term="
                                               (string/join "%20" args)
                                               "&limit=3&location=64137&ywsid="
                                               (yelp-id @bot)))))))

  (:cmd
   "Displays a G-rated version of an urbandictionary term, which should be even creepier than the original."
   #{"suburban" "suburb" "burb" "burbs"}
   (fn [{:keys [bot args] :as com-m}]
     (send-message com-m
                   (suburb-define bot (string/join "%20" args)))))

  (:cmd
   "Say a random yomomma joke."
   #{"yomomma"}
   (fn [{:keys [args] :as com-m}]
     (send-message com-m
                   (str (string/join " " args) " " (second (string/split (:body (http/get "http://api.yomomma.info/")) #"joke\":\"|\"}</body>"))))))

  (:cmd
   "Gives a sales pitch."
   #{"pitch"}
   (fn [{:keys [args] :as com-m}]
     (send-message com-m
                   (str (:body (http/get "http://itsthisforthat.com/api.php?text")) " With a $10,000 investment, you can be in on the ground floor."))))

  (:cmd
   "Display a random cat picture."
   #{"cat"}
   (fn [{:keys [args] :as com-m}]
     (send-message com-m
                   (get (:headers (http/get "http://thecatapi.com/api/images/get" {:follow-redirects false})) "location"))))

  (:cmd
   "Say a random Chuck Norris joke."
   #{"chuck"}
   (fn [{:keys [args] :as com-m}]
     (send-message com-m
                   (get-in (json/parse-string (:body (http/get "http://api.icndb.com/jokes/random"))) ["value" "joke"]))))
  
  (:cmd
   "Display random fact about today."
   #{"today"}
   (fn [{:keys [args] :as com-m}]
     (send-message com-m
                   (slurp (str "http://numbersapi.com/" (today))))))

  (:cmd
   "Link to Google Finance stock information for the given company."
   #{"stock"}
   (fn [{:keys [args] :as com-m}]
     (send-message com-m
                   (query-finance (str "https://www.google.com/finance?q=" (string/join "%20" args))))))

  (:cmd
   "Test."
   #{"test"}
   (fn [{:keys [com nick hmask args irc] :as com-m}]
     (send-message com-m
                   (.toLowerCase (str hmask))))))
