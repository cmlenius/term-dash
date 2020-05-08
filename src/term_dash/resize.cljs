(ns term-dash.resize
  (:require
   [re-frame.core :as rf]))

(defn size
  "Get hash-map with the rows and cols of the screen size."
  [screen]
  {:rows (.-rows screen)
   :cols (.-cols screen)})

(defn setup [screen]
  (.on screen "resize" #(rf/dispatch [:update-size (size screen)])))
