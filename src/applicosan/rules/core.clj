(ns applicosan.rules.core)

(defmacro defrule [rule-name condition [event opts] & body]
  `(def ~(with-meta rule-name {:condition condition})
     (with-meta
       (fn [~'&match ~event ~opts]
         ~@body)
       {:condition ~condition})))

(defn ->rule-set [opts rules]
  {:rules rules
   :opts opts})
