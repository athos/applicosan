(ns applicosan.models.worktime
  (:require [applicosan.db :as db]
            [applicosan.image :as image]
            [applicosan.models.worktime.chart :as chart]
            [applicosan.time :as time]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [drains.core :as d]
            [drains.utils :as dutils]
            [monger.collection :as mc]
            [monger.operators :as mo]
            [monger.query :as mq])
  (:import [java.util Date]
           [java.time LocalDateTime]))

(s/def ::year int?)
(s/def ::month int?)
(s/def ::day int?)
(s/def ::in inst?)
(s/def ::out inst?)

(s/def ::worktime
  (s/keys :req-un [::year ::month ::day]
          :opt-un [::in ::out]))

(defn- record-time! [db type dt]
  {:pre (#{:in :out} type)}
  (mc/update db db/COLL_WORKTIME (time/date-map dt)
             {mo/$set {type (time/->date dt)}}
             {:upsert true}))

(defn clock-in!
  ([db] (clock-in! db (time/now)))
  ([db dt]
   (record-time! db :in dt)))

(defn clock-out!
  ([db] (clock-out! db (time/now)))
  ([db dt]
   (record-time! db :out dt)))

(defn latest-worktimes
  ([db] (latest-worktimes db 30))
  ([db n]
   (->> (mq/with-collection db db/COLL_WORKTIME
          (mq/find {})
          (mq/sort {:year -1 :month -1 :day -1})
          (mq/limit n))
        reverse)))

(defn aggregate-overtime [worktimes year month]
  (let [drain (d/with (comp (filter #(and (= (:year %) year) (= (:month %) month)))
                            (map (fn [{:keys [^Date in, ^Date out]}]
                              (if out
                                (- (/ (- (.getTime out) (.getTime in)) 1000.0 60) (* 9 60))
                                0))))
                      (d/drains {:total (dutils/sum)
                                 :average (dutils/mean)
                                 :last (d/drain (completing (fn [_ t] t)) 0)}))]
    (d/reduce drain worktimes)))

(defn generate-chart [worktimes]
  (let [width 300, height 150]
    (image/generate-image width height
      (fn [g]
        (chart/render-chart g width height worktimes)))))

(defn- ^long ->int [^String s]
  (Long/parseLong s))

(defn parse-tsv [lines]
  (for [line lines
        :let [[year month day in out] (str/split line #"\t")
              [year month day] (map ->int [year month day])
              _ (prn :in in :out out)
              [_ in-h in-m in-s] (some->> in (re-find #"^[^ ]+? (\d\d):(\d\d):(\d\d)"))
              [_ out-h out-m out-s] (some->> out (re-find #"^[^ ]+? (\d\d):(\d\d):(\d\d)"))]]
    {:year year
     :month month
     :day day
     :in (when (and in-h in-m in-s)
           (time/->date (LocalDateTime/of year month day (->int in-h) (->int in-m) (->int in-s))))
     :out (when (and out-h out-m out-s)
            (time/->date (LocalDateTime/of year month day (->int out-h) (->int out-m) (->int out-s))))}))
