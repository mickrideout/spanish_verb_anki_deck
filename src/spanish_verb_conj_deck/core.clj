(ns spanish-verb-conj-deck.core
  (:require [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clojure.string :as string]
            [semantic-csv.core :as sc :refer :all])
  (:gen-class))


(def article_set (atom (hash-set)))

; [form_key english_prefix convert_was_to_were]
(def forms [{:form :form_1s, :prefix "I ", :was_conv false, :have_conv false},
            {:form :form_2s, :prefix "You ", :was_conv true, :have_conv false},
            {:form :form_3s, :prefix "He/She ", :was_conv false, :have_conv true},
            {:form :form_1p, :prefix "We ", :was_conv true, :have_conv false},
            {:form :form_2p, :prefix "You all ", :was_conv true, :have_conv false},
            {:form :form_3p, :prefix "They ",  :was_conv true, :have_conv false}])

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

(defn print-entry [form_map mapentry startindex]
  (let [form_eng_prefix (:prefix form_map)
        form (:form form_map)
        convert_was (:was_conv form_map)
        convert_have (and (= "Present Perfect" (:tense_english mapentry)) (:have_conv form_map))
        outstr (str form_eng_prefix (subs (:verb_english mapentry) startindex) "<br>(" (:tense_english mapentry) " - " (:mood_english mapentry) "):"
           (form mapentry) " <br>(" (:tense mapentry) " - " (:mood mapentry) ")")]
    (println
      (cond
        convert_have (string/replace outstr #"\shave\s" " has ")
        convert_was (string/replace outstr #"\swas\s" " were ")
      :else outstr))))

(defn print-form [form_map mapentry]
    (cond
      (= "" ((:form form_map) mapentry)) nil
      (= "Imperative Affirmative" (:mood_english mapentry)) (print-entry form_map mapentry 0)
      (= "Imperative Negative" (:mood_english mapentry)) (print-entry form_map mapentry 0)
      :else (print-entry form_map mapentry 2)))

(defn iterate-csvmap [csvmaplist]
  (doseq [csvmap csvmaplist]
    (print-article :infinitive_english " (infinitive)" :infinitive " (infinitivo)" csvmap)
    (print-article :gerund_english " (gerund)" :gerund " (gerundio)" csvmap)
    (print-article :pastparticiple_english " (past participle)" :pastparticiple " (preterite participio)" csvmap)
    (doseq [form_map forms]
      (print-form form_map csvmap))))

(defn -main
  "Print out spanish anki deck cards of spanish verb conjugations"
  [& args]
  (iterate-csvmap (parsecsv "resources/jehle_verb_database.csv")))
