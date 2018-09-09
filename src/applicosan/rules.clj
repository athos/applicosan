(ns applicosan.rules
  (:require [applicosan.rules.core :as rules]
            [applicosan.rules.worktime]
            [duct.logger :as logger]
            [integrant.core :as ig]))

(defn apply-rule [{:keys [rules opts]} message event]
  (or (reduce (fn [_ {:keys [pattern action] :as rule}]
                (when-let [match (re-find pattern message)]
                  (action match event opts)
                  (reduced true)))
              nil
              rules)
      (logger/log (:logger opts) :warn ::no-rules-applied {:message message})))

(defmethod ig/init-key :applicosan/rules [_ opts]
  {:rules @rules/defined-rules
   :opts (update opts :db :db)})
