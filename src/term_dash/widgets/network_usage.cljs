(ns term-dash.widgets.network-usage
  (:require
    [clojure.string :as string]
    [re-frame.core :as rf]
    [term-dash.shell :as shell]
    [term-dash.utils :as utils]))

(def si (js/require "systeminformation"))
(def context (js/require "drawille-canvas-blessed-contrib"))

(defn scale [data width height]
  "Helper function for logarithmically scaling data to handle spikes"
  (->> data
       (take-last (* width 2))
       (mapv #(max 0 (/ (js/Math.log %) (js/Math.log 1.5))))
       (mapv #(/ % 35))
       (mapv #(* % height))))

(defn data->canvas
  "Transform data into a canvas and return it in the form of a string.
   Transferred as green on top. Received as red on the bottom."
  [{:keys [rx_sec tx_sec]} width height]
  (let [height'    (* height 4)
        width'     (* width 2)
        mid-pointt (* 4 (quot height 2))
        mid-pointr (dec mid-pointt)
        rData      (scale rx_sec width (/ height' 2))
        tData      (scale tx_sec width (/ height' 2))
        ctx        (new context width' height')]

    ;; Received
    (set! (.-strokeStyle ctx) "green")
    (.beginPath ctx)
    (doseq [[x y] (map-indexed vector rData)]
      (.moveTo ctx x mid-pointr)
      (if (>= y mid-pointr)
        (.lineTo ctx x 0)
        (.lineTo ctx x (utils/abs (- mid-pointr y)))))
    (.stroke ctx)

    ;; Transferred
    (set! (.-strokeStyle ctx) "red")
    (.beginPath ctx)
    (doseq [[x y] (map-indexed vector tData)]
      (.moveTo ctx x mid-pointt)
      (if (>= y height')
        (.lineTo ctx x height')
        (.lineTo ctx x (+ mid-pointt y))))
    (.stroke ctx)

    ;; Draw
    (.frame (.-canvas ctx))))

(defn format-data
  "Format data to be displayed in graph format"
  [data]
  (let [data (-> (js->clj data :keywordize-keys true)
                 (string/split #"\n")
                 last
                 (string/split #" "))]
    (->> (remove string/blank? data)
         (mapv js/parseFloat)
         (zipmap [:rx_sec :tx_sec]))))

(defn get-net-usage
  "Fetch network-usage data"
  []
  (.inetLatency si #(rf/dispatch [:set-data [:network-usage :latency] %]))
  (shell/run-shell-command {:event     [:set-network-speed]
                            :format-fn format-data
                            :cmd       "ifstat"
                            :args      ["-i" "en0" "0.5" "1"]}))

(defn network-usage
  "Displays network usage as a double sparkline"
  [{:keys [height width] {:keys [speed latency]} :data}]
  (let [{:keys [rx_sec tx_sec]} speed
        cur-throughput (fn [data color]
                         (-> (str (last data))
                             (utils/space-before 11)
                             (utils/color color)))]
    [:<>
     [:text {:top     0
             :left    0
             :height  1
             :content (str "Latency: " latency)}]
     [:text {:top     0
             :right   1
             :height  2
             :align   :right
             :content (str "    KB/s in   KB/s out\n"
                           (cur-throughput rx_sec :green)
                           (cur-throughput tx_sec :red))}]
     [:text {:top     2
             :height  (- height 2)
             :content (data->canvas speed (dec width) (dec height))}]]))

;; .networkStats .inetLatency .networkInterfaces