(ns applicosan.rules.core)

(defmacro defrule [rule-name pattern [event opts] & body]
  `(def ~(with-meta rule-name {:pattern pattern})
     (with-meta
       (fn [~'&match ~event ~opts]
         ~@body)
       {:pattern ~pattern})))

(defn ->rule-set [opts rules]
  {:rules rules
   :opts opts})
