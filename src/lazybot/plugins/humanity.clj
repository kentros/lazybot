(ns lazybot.plugins.humanity
  (:use [lazybot registry info]
        [somnium.congomongo :only [fetch fetch-one insert! destroy!]]))

(def blanks (re-pattern "_{2,}"))

(defn fill-in-blanks [s words]
  ;(println (:noun (words 0))))
  (loop [end s x (dec (count (re-seq blanks s)))]
    (if (< x 0)
      end
      (recur (clojure.string/replace-first end blanks (:noun (words x))) (dec x)))))

(defplugin
  (:cmd
   "Gives you a fun sentence -- Cards Against Humanity style."
   #{"hum"}
   (fn [com-m]
     (let [statements (fetch :statements)
           nouns (fetch :nouns)]
       (if (zero? (count statements))
         (send-message com-m "I have no statements! Please feed me some!")
         (send-message com-m (fill-in-blanks (str (:statement (rand-nth statements))) (shuffle nouns)))))))

  (:cmd
   "Adds a noun or gerund to the humanity game database."
   #{"addnoun" "addn"}
   (fn [{:keys [args] :as com-m}]
     (if (seq args)
       (do
         (insert! :nouns {:noun (->> args (interpose " ") (apply str))})
         (send-message com-m "Thanks!  I like your style."))
       (send-message com-m "Hey!  What's your problem?  I need a noun!"))))

  (:cmd
   "Adds a statement with blanks to the humanity game database."
   #{"addstatement" "adds"}
   (fn [{:keys [args] :as com-m}]
     (if (seq args)
       (do
         (insert! :statements {:statement (->> args (interpose " ") (apply str))})
         (send-message com-m "Thanks!  You rule."))
       (send-message com-m "Hey!  What's your problem?  I need a statement!")))))