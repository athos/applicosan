(ns applicosan.attachments.common
  (:require [applicosan.attachments.core :as attach :refer [defattachment]]))

(def ok-button
  (attach/button :ok "OK" :ok :style :primary))

(def cancel-button
  (attach/button :cancel "Cancel" :canceled))

(defattachment ^{:color "#3AA3E3"} confirm-buttons
  [ok-button
   cancel-button])

(defattachment ^{:color "#3AA3E3"} empty-menu
  [cancel-button])
