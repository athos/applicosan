(ns applicosan.rules
  (:require [applicosan.rules.core :as rules]
            [applicosan.rules.worktime]
            [integrant.core :as ig]))

(defn apply-rule [{:keys [rules opts]} message event]
  (reduce (fn [_ {:keys [pattern action] :as rule}]
            (when-let [match (re-find pattern message)]
              (reduced (apply action match event opts))))
          nil
          rules))

(defmethod ig/init-key :applicosan/rules [_ opts]
  {:rules @rules/defined-rules
   :opts (update opts :db :db)})
