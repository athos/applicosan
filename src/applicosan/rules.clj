(ns applicosan.rules
  (:require [applicosan.condition :as condition]
            [integrant.core :as ig]))

(defn apply-rule [rules event]
  (reduce (fn [_ {:keys [condition action opts]}]
            (when-let [match (condition/match condition event)]
              (action match event opts)
              (reduced true)))
          nil
          rules))

(defmethod ig/init-key :applicosan/rules [_ rule-sets]
  (->> (for [{:keys [rules opts]} rule-sets
             rule rules]
         {:condition (get (meta rule) :condition)
          :action rule
          :opts opts})
       (into [])))
