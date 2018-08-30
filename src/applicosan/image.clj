(ns applicosan.image
  (:import [java.awt.image BufferedImage]
           [java.io ByteArrayOutputStream]
           [javax.imageio ImageIO]))

(set! *warn-on-reflection* true)

(defn ->bytes [^BufferedImage image]
  (let [baos (ByteArrayOutputStream.)]
    (ImageIO/write image "png" baos)
    (.toByteArray baos)))
