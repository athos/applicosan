(ns applicosan.attachments.core)

(defrecord Attachment [id fallback actions])

(defn make-attachment [id {:keys [fallback] :as opts} actions]
  (let [fallback (or fallback
                     "Your UI doesn't seem to support interactive message.")]
    (merge (->Attachment id fallback (vec actions))
           (dissoc opts :fallback))))

(defmacro defattachment [id arg]
  (let [callback-id (str (ns-name *ns*) "/" (name id))
        opts (meta id)]
    `(def ~(with-meta id {})
       ~(if (symbol? arg)
          `(-> ~arg
               (assoc :id ~callback-id)
               ~@(when opts [`(merge ~opts)]))
          `(make-attachment ~callback-id
                            ~opts
                            ~(vec arg))))))

(defn ->map [{:keys [id] :as attachment}]
  (into {:callback_id id} (dissoc attachment :id)))

(defn value-of [attachment action-name]
  (->> (:actions attachment)
       (filter #(= (:name %) action-name))
       first
       :value))

(defn action-of [attachment value]
  (first (filter #(= (:value %) value) (:actions attachment))))

(defn action-name-of [attachment value]
  (:name (action-of attachment value)))

(defattachment press-me
  [{:name :press
    :text "Press me!"
    :type :button
    :value "pressed"}])
