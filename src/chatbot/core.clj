(ns chatbot.core
  (:use [clojure.data.json :only (read-json)]
        [clojure.pprint])
  (:require [clojure.string])
  (:gen-class))

(def common-words
  #{"is" "a" "the" "are" "an" "and" "what" "why" "he" "she" "it" "i" "in" "me" "you" "they" "him" "can" "should" "will" "have" "for" "I'm" "I"})

(defn entity? [word]
  (.startsWith word "&"))

(defn url? [poss-url]
  (.startsWith poss-url "http:"))

(defn hashtag? [word]
  (.startsWith word "#"))

(defn without [pred tweet]
  (let [words (seq (.split tweet " "))]
    (clojure.string/join " " (remove pred words))))

(defn twitter-search [terms]
  (let [concat-terms (clojure.string/join
                      "+" (remove common-words terms))
        uri    (str "http://search.twitter.com/search.json?q="
                 (java.net.URLEncoder/encode concat-terms "UTF-8"))
        result (read-json (slurp (java.net.URI. uri)))
        tweet  (or (first (filter
                           (complement (partial some #{\@}))
                           (map :text (:results result))))
                   "sorry, I don't understand")]
    ; originally:
    ; (without entity? (without hashtag? (without url? tweet)))
    (reduce #(without %2 %1) tweet [entity? hashtag? url?])))

(defn -main [& args]
  (loop []
    (print "> ")
    (flush)
    (println (twitter-search (.split (read-line) "\\s+")))
    (recur)))


