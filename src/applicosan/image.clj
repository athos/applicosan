(ns applicosan.image
  (:import [java.awt Graphics2D RenderingHints]
           [java.awt.image BufferedImage]
           [java.io ByteArrayOutputStream]
           [javax.imageio ImageIO]))

(set! *warn-on-reflection* true)

(defn- enable-antialiasing [^Graphics2D g]
  (doto g
    (.setRenderingHint RenderingHints/KEY_ANTIALIASING
                       RenderingHints/VALUE_ANTIALIAS_ON)
    (.setRenderingHint RenderingHints/KEY_TEXT_ANTIALIASING
                       RenderingHints/VALUE_TEXT_ANTIALIAS_ON)))

(defn ^BufferedImage generate-image [width height render-fn]
  (let [image (BufferedImage. width height BufferedImage/TYPE_INT_ARGB)
        g (.getGraphics image)]
    (enable-antialiasing g)
    (render-fn g)
    (.dispose g)
    image))

(defn ->bytes [^BufferedImage image]
  (let [baos (ByteArrayOutputStream.)]
    (ImageIO/write image "png" baos)
    (.toByteArray baos)))
