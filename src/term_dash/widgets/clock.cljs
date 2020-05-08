(ns term-dash.widgets.clock
  (:require
    [clojure.string :as string]
    [reagent.core :as r]
    [term-dash.utils :as utils]))

(def ascii (js/require "asciify"))
(def time (r/atom nil))

(defn set-time []
  (-> (utils/date-now)
      (string/split #" ")
      second
      (string/replace #":" " ")
      (ascii #js{:font "banner"} (fn [err res] (reset! time res)))))

(defn clock
  "Displays digital clock"
  [{:keys [width height]}]
  (let [lines (string/split @time #"\n")
        [w h] (vector (count (first lines)) (count lines))
        x-offset (-> (- width w) (quot 2) (max 1))
        y-offset (-> (- height h) (quot 2) (max 1) inc)]
    [:text
     {:top     y-offset
      :left    x-offset
      :content @time}]))
