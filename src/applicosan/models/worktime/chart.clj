(ns applicosan.models.worktime.chart
  (:require [applicosan.time :as time])
  (:import [java.awt BasicStroke Color Graphics2D]))

(set! *warn-on-reflection* true)

(defn make-renderer [g width height]
  (let [margin 10
        interval 2]
    {:g g
     :width width
     :height height
     :margin margin
     :interval interval
     :origin-x (+ margin interval)
     :origin-y (+ margin interval)
     :area-width (- width (* 2 margin) (* 2 interval))
     :area-height (- height (* 2 margin) (* 2 interval))
     :bar-width (long (- (/ (- width interval) 20.0) interval))}))

(defn time->height [{:keys [area-height]} t]
  (* area-height (/ t 720.0)))

(defn time-value [t]
  (let [{:keys [hour minute]} (time/time-map t)]
    (+ (* 60 hour) minute)))

(defn render-worktimes [renderer worktimes]
  (let [{:keys [^Graphics2D g width height interval origin-x origin-y bar-width]} renderer]
    (doseq [[i {:keys [in out]}] (map-indexed vector worktimes)
            :when (and in out)
            :let [[in out] (map time-value [in out])
                  worktime (- out in)
                  color (condp <= (- worktime 540)
                          45 Color/RED
                          15 Color/ORANGE
                          Color/GREEN)]]
      (.setColor g color)
      (.fillRect g
                 (+ origin-x (* i (+ bar-width interval)))
                 (+ origin-y (time->height renderer (- in 540)))
                 bar-width
                 (time->height renderer worktime)))))

(defn mask-previous-month [{:keys [^Graphics2D g] :as renderer} [w :as worktimes]]
  (let [interval (:interval renderer)
        n (->> worktimes
               (take-while #(and (= (:year w) (:year %))
                                 (= (:month w) (:month %))))
               count)]
    (when (not= n (count worktimes))
      (.setColor g (Color. 150 150 150 128))
      (.fillRect g (:margin renderer) (:margin renderer)
                 (+ (/ interval 2.0) (* n (+ (:bar-width renderer) interval)))
                 (+ (:area-height renderer) (* 2 interval))))))

(defn render-scale [renderer]
  (let [half-len 3
        {:keys [^Graphics2D g origin-x origin-y area-width interval]} renderer]
    (.setColor g Color/GRAY)
    (doseq [i (range 1 12)
            :let [y (+ origin-y (* i (time->height renderer 60)))]]
      (.drawLine g
                 (- origin-x half-len) y
                 (+ origin-x half-len) y)
      (.drawLine g
                 (- (+ origin-x area-width interval) half-len) y
                 (+ (+ origin-x area-width interval) half-len) y))))

(defn render-dashes [{:keys [^Graphics2D g origin-x origin-y area-width] :as renderer}]
  (let [stroke (.getStroke g)
        dashed (BasicStroke. 1 BasicStroke/CAP_BUTT BasicStroke/JOIN_BEVEL 0 (float-array [5]) 0)]
    (.setStroke g dashed)
    (.setColor g Color/GRAY)
    (doseq [t [10 19]
            :let [y (+ origin-y (time->height renderer (* (- t 9) 60)))]]
      (.drawLine g origin-x y (+ origin-x area-width) y))
    (.setStroke g stroke)))

(defn render-chart
  ([g width height worktimes]
   (render-chart (make-renderer g width height) worktimes))
  ([{:keys [^Graphics2D g width height margin] :as renderer} worktimes]
   (.setColor g Color/WHITE)
   (.fillRect g 0 0 width height)
   (.setColor g Color/BLACK)
   (.drawRect g margin margin (- width (* 2 margin)) (- height (* 2 margin)))
   (render-worktimes renderer worktimes)
   (mask-previous-month renderer worktimes)
   (render-scale renderer)
   (render-dashes renderer)))
