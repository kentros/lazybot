(ns lazybot.plugins.dumbot
  (:use lazybot.registry)
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.string :as string])
  (:import (java.util Date)))

(use '[clojure.java.shell :only [sh]])

(defn today [] (.format (java.text.SimpleDateFormat. "MM/dd") (Date.)))

(defplugin
  (:cmd 
   "Attempts to make an intelligent reply from arbitrary input." 
   #{"dumbot"} 
   (fn [{:keys [args] :as com-m}]
     (send-message com-m 
                   (:out (sh "python" "chat.py" (string/join " " args))))))

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
   "Test."
   #{"test"}
   (fn [{:keys [com nick hmask args irc] :as com-m}]
     (send-message com-m
                   (.toLowerCase (str hmask))))))
