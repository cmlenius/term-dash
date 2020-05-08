(ns term-dash.widgets.cpu-usage
  (:require
    [re-frame.core :as rf]
    [term-dash.utils :as utils]))

(def si (js/require "systeminformation"))
(def context (js/require "drawille-canvas-blessed-contrib"))

(defn data->canvas [data width height]
  "Transform data into a canvas and return it in the form of a string"
  (let [height' (* height 4)
        width'  (* width 2)
        data    (mapv #(/ (* % height') 100) data)
        ctx     (new context width' height')]

    (set! (.-strokeStyle ctx) "magenta")
    (.beginPath ctx)
    (doseq [[x y] (map-indexed vector data)]
      (.moveTo ctx x height')
      (if (>= y height')
        (.lineTo ctx x 0)
        (.lineTo ctx x (utils/abs (- height' y)))))
    (.stroke ctx)
    (.frame (.-canvas ctx))))

(defn get-cpu-usage
  "Fetch cpu-usage data"
  []
  (.currentLoad si #(rf/dispatch [:set-cpu-usage (:currentload (js->clj % :keywordize-keys true))])))

(defn cpu-usage
  "Displays cpu-usage as a sparkline"
  [{:keys [data width height]}]
  (let [current (-> data last int (str "%"))]
    [:<>
     [:text {:top     0
             :right   1
             :height  1
             :align   :right
             :content current}]
     [:text {:top     1
             :height  (dec height)
             :content (data->canvas (take-last (* (dec width) 2) data)
                                    (dec width)
                                    (dec height))}]]))