(ns term-dash.dashboard
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [term-dash.config :refer [config]]
    [term-dash.utils :as utils]))

(defn widget [w-key]
  (let [props    (assoc (get config w-key) :w-key w-key)
        data     (rf/subscribe [:data w-key])
        interval (atom nil)]
    (r/create-class
      {:component-will-mount
       (fn []
         (let [{:keys [interval-fn interval-delay]} props]
           (interval-fn)
           (reset! interval (js/setInterval interval-fn interval-delay))))

       :component-will-unmount
       (fn []
         (js/clearInterval @interval))

       :reagent-render
       (fn [_]
         (let [{:keys [top left width height label render]} props
               size (rf/subscribe [:size])]
           [:box {:top    top
                  :left   left
                  :width  width
                  :height height
                  :style  {:border {:fg :blue}}
                  :border {:type :line}
                  :label  (str " " label " ")}
            (if (seq @data)
              (let [{:keys [width height] :as inner-size} (utils/get-inner-size @size width height)]
                (when (and (pos-int? width) (pos-int? height))
                  [render (merge props inner-size {:data @data})]))
              [:text {:content "Loading..."}])]))})))


;                       Main Dashboard
; +---------------------------+---------------------------+
; | Stock Ticker |  Clock     |  Processes  |             |
; +---------------------------+-------------+-------------+
; |                           |  CPU Usage  |  Net Usage  |
; +                           +-------------+-------------+
; |       Stock Charts        |       Docker Stats        |
; +                           +---------------------------+
; |                           |          Weather          |
; +---------------------------+---------------------------+

(defn dashboard []
  [:box#base {:left   0
              :right  0
              :width  "100%"
              :height "100%"}
   (for [w-key (keys config)]
     [widget w-key])])