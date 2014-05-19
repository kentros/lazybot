(ns lazybot.plugins.smh
  (:use lazybot.registry))

(def comebacks '("What?  Too soon?" "I know, that was really stupid.  Please forgive me.  Jesus would."
                 "You just don't understand, old man!" "Yeah, I wish I could take that back.  /facepalm"
                 "I'm sorry you had to see that." "Sorry!  I wasn't thinking.  Blame it on my programming."
                 "Yeah, you're right.  That's lame.  Why don't you show me how it's done?"))

(defplugin
  (:cmd
   "Respond to a shaking of head!"
   #{"smh"}
   (fn [com-m] (send-message com-m (rand-nth comebacks)))))