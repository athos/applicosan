(ns applicosan.rules
  (:require [applicosan.rules.core :as rules]
            [applicosan.rules.worktime]
            [duct.logger :as logger]
            [integrant.core :as ig]))

(defn apply-rule [{:keys [rules logger]} message event]
  (or (reduce (fn [_ {:keys [pattern action opts]}]
                (when-let [match (re-find pattern message)]
                  (action match event opts)
                  (reduced true)))
              nil
              rules)
      (logger/log logger :warn ::no-rules-applied {:message message})))

(defmethod ig/init-key :applicosan/rules [_ {:keys [rule-sets logger]}]
  (->> (for [{:keys [rules opts]} rule-sets
             rule rules]
         {:pattern (get (meta rule) :pattern)
          :action rule
          :opts opts})
       (into [])
       (array-map :logger logger :rules)))
