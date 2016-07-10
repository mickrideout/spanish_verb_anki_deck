(ns spanish-verb-conj-deck.core
  (:require [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [semantic-csv.core :as sc :refer :all])
  (:gen-class))


(def article_set (atom (hash-set)))
(def forms [[:form_1s "I"],
            [:form_2s "You"],
            [:form_3s "He/She"],
            [:form_1p "We"],
            [:form_2p "You all"],
            [:form_3p "They"]])

(defn parsecsv [csvfile]
  (with-open [in-file (io/reader csvfile)]
     (->>
       (csv/parse-csv in-file)
       remove-comments
       mappify
       doall)))

(defn print-article [eng_key eng_append esp_key esp_append csvmap]
  (let [output_str (str (eng_key csvmap) eng_append ":" (str (esp_key csvmap)) esp_append)]
    (if (contains? @article_set output_str)
      nil
      (do
        (println output_str)
        (swap! article_set conj output_str)))))

(defn print-form [form form_eng_prefix mapentry]
  (if (not (= "" (form mapentry)))
    (println form_eng_prefix (subs (:verb_english mapentry) 2) "(" (:tense_english mapentry) " - " (:mood_english mapentry) "):"
           (form mapentry) " (" (:tense mapentry) " - " (:mood mapentry)" )")))

(defn iterate-csvmap [csvmaplist]
  (doseq [csvmap csvmaplist]
    (print-article :infinitive_english " (infinitive)" :infinitive " (infinitivo)" csvmap)
    (print-article :gerund_english " (gerund)" :gerund " (gerundio)" csvmap)
    (print-article :pastparticiple_english " (past participle)" :pastparticiple " (preterite participio)" csvmap)
    (doseq [[form eng_prefix] forms]
      (print-form form eng_prefix csvmap))))

(defn -main
  "Print out spanish anki deck cards of spanish verb conjugations"
  [& args]
  (iterate-csvmap (parsecsv "resources/jehle_verb_database.csv")))
