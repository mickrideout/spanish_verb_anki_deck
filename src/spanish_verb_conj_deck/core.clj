(ns spanish-verb-conj-deck.core
  (:require [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clojure.string :as string]
            [semantic-csv.core :as sc :refer :all])
  (:gen-class))


(def article_set (atom (hash-set)))

; [form_key english_prefix convert_was_to_were]
(def forms [[:form_1s "I " false],
            [:form_2s "You " true],
            [:form_3s "He/She " false],
            [:form_1p "We " true],
            [:form_2p "You all " true],
            [:form_3p "They " true]])

(defn parsecsv [csvfile]
  (with-open [in-file (io/reader csvfile)]
     (->>
       (csv/parse-csv in-file)
       remove-comments
       mappify
       doall)))

(defn print-article [eng_key eng_append esp_key esp_append csvmap]
  (let [output_str (str (eng_key csvmap) "<br>" eng_append ":" (str (esp_key csvmap)) "<br>" esp_append)]
    (if (contains? @article_set output_str)
      nil
      (do
        (println output_str)
        (swap! article_set conj output_str)))))

(defn print-entry [form form_eng_prefix convert_was mapentry startindex]
  (let [outstr (str form_eng_prefix (subs (:verb_english mapentry) startindex) "<br>(" (:tense_english mapentry) " - " (:mood_english mapentry) "):"
           (form mapentry) " <br>(" (:tense mapentry) " - " (:mood mapentry)" )")]
    (if convert_was
      (println (string/replace outstr #"\swas\s" " were "))
      (println outstr))))

(defn print-form [form form_eng_prefix convert_was mapentry]
    (cond
      (= "" (form mapentry)) nil
      (= "Imperative Affirmative" (:mood_english mapentry)) (print-entry form form_eng_prefix convert_was mapentry 0)
      (= "Imperative Negative" (:mood_english mapentry)) (print-entry form form_eng_prefix convert_was mapentry 0)
      :else (print-entry form form_eng_prefix convert_was mapentry 2)))

(defn iterate-csvmap [csvmaplist]
  (doseq [csvmap csvmaplist]
    (print-article :infinitive_english " (infinitive)" :infinitive " (infinitivo)" csvmap)
    (print-article :gerund_english " (gerund)" :gerund " (gerundio)" csvmap)
    (print-article :pastparticiple_english " (past participle)" :pastparticiple " (preterite participio)" csvmap)
    (doseq [[form eng_prefix convert_was] forms]
      (print-form form eng_prefix convert_was csvmap))))

(defn -main
  "Print out spanish anki deck cards of spanish verb conjugations"
  [& args]
  (iterate-csvmap (parsecsv "resources/jehle_verb_database.csv")))
