(ns applicosan.rules
  (:require [applicosan.rules.core :as rules]
            [applicosan.rules.worktime]
            [integrant.core :as ig]))

(defn apply-rule [rules message event]
  (reduce (fn [_ {:keys [pattern action opts]}]
            (when-let [match (re-find pattern message)]
              (action match event opts)
              (reduced true)))
          nil
          rules))

(defmethod ig/init-key :applicosan/rules [_ rule-sets]
  (->> (for [{:keys [rules opts]} rule-sets
             rule rules]
         {:pattern (get (meta rule) :pattern)
          :action rule
          :opts opts})
       (into [])))
