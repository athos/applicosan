(ns applicosan.rules.core)

(def defined-rules (atom []))

(defn add-rule! [rules name pattern action]
  (let [rule {:name name :pattern pattern :action action}]
    (if-let [[i _] (->> (filter #(= (:name (second %)) name)
                                (map-indexed vector @rules))
                        first)]
      (swap! rules assoc i rule)
      (swap! rules conj rule))
    name))

(defmacro defrule [rule-name pattern [event & args] & body]
  `(do
     (defn ~rule-name [~'&match ~event ~@args] ~@body)
     (add-rule! defined-rules
                (symbol (name (ns-name *ns*)) (name '~rule-name))
                ~pattern
                #'~rule-name)))

(defn apply-rule [message event & args]
  (reduce (fn [_ {:keys [pattern action] :as rule}]
            (when-let [match (re-find pattern message)]
              (reduced (apply action match event args))))
          nil
          @defined-rules))
