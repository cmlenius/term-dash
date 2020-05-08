(ns term-dash.widgets.stock-chart
  (:require
    [clojure.string :as string]
    [term-dash.shell :as shell]
    [term-dash.utils :as utils]))

(def context (js/require "drawille-canvas-blessed-contrib"))
(def symbol "VFV.TO")

(def config-map
  {:stock-intraday
   {:freq      "TIME_SERIES_INTRADAY"
    :interval  "5min"
    :spacing   5
    :format-fn (partial keep (fn [[x y]]
                               (let [cur-date (-> (utils/date-now) (string/split #" ") first)
                                     [date time] (string/split x #" ")]
                                 (when (= date cur-date)
                                   [(subs time 0 5) (-> y (get "4. close") js/parseFloat)]))))}
   :stock-daily
   {:freq      "TIME_SERIES_DAILY"
    :spacing   10
    :format-fn (partial map (fn [[x y]]
                              [x (-> y (get "4. close") js/parseFloat)]))}})

(defn- alpha-vantage-url
  "Generate URL for AlphaVantage endpoint based on frequency and symbol"
  [w-key sym]
  (let [baseURL  "https://www.alphavantage.co/query?apikey=SC171S2F4AAA4NRW"
        function (str "&function=" (get-in config-map [w-key :freq]))
        symbol   (str "&symbol=" sym)
        interval (when-let [interval (get-in config-map [w-key :interval])]
                   (str "&interval=" interval))]
    (str baseURL function symbol interval)))

(defn graph
  "Draw graph for data using canvas context"
  [ctx data width height]
  (let [high (inc (reduce max data))
        low  (dec (reduce min data))
        data (->> data
                  (mapv #(- % low))
                  (mapv #(/ % (- high low)))
                  (mapv #(* % height)))
        step (/ width (count data))]
    (.beginPath ctx)
    (.moveTo ctx 0 (utils/abs (- height (first data))))
    (doseq [[i y] (map-indexed vector data)]
      (.lineTo ctx (* i step) (utils/abs (- height y))))
    (.stroke ctx)))

(defn data->chart
  "Transform data into a canvas and return it in the form of a string"
  [data width height color]
  (let [width'  (* 2 (dec width))
        height' (* 4 (dec height))
        ctx     (new context width' height')]
    ;; Columns
    (set! (.-strokeStyle ctx) "white")
    (.beginPath ctx)
    (doseq [i (range 1 5)
            :let [x (* i (quot width' 5))]]
      (.moveTo ctx x height')
      (.lineTo ctx x (- height' 4)))
    (.stroke ctx)

    ;; Graph
    (set! (.-strokeStyle ctx) (name color))
    (graph ctx data width' height')

    ;; Borders
    (set! (.-strokeStyle ctx) "white")
    (.beginPath ctx)
    (.moveTo ctx 0 0)
    (.lineTo ctx 0 (dec height'))
    (.lineTo ctx width' (dec height'))
    (.stroke ctx)

    ;; Draw
    (.frame (.-canvas ctx))))

(defn x-axis
  "Parse X data and return x-axis labels with proper spacing"
  [w-key width data]
  (let [spacing (get-in config-map [w-key :spacing])
        space   (string/join (repeat (- (quot width 5) spacing) " "))]
    (->> data
         (take-nth (max 1 (quot (count data) 5)))
         (drop 1)
         (interleave (repeat space))
         (reduce str)
         (str (string/join (repeat (quot spacing 2) " "))))))

(defn y-axis
  "Parse Y data and return y-axis labels with proper spacing"
  [_ height data]
  (let [high  (inc (reduce max data))
        low   (dec (reduce min data))
        scale (for [i (range 6)]
                (let [delta (- high low)]
                  (str " " (js/Math.round (- high (* i (/ delta 5)))))))]
    (->> scale
         (interpose (repeat (quot (- height 6) 5) " "))
         flatten)))

(defn format-data
  "Format data and split into x and y data"
  [w-key data]
  (let [format-fn (get-in config-map [w-key :format-fn])
        data'     (some->> data
                           (js/JSON.parse)
                           (js/Object.values)
                           second
                           (js/Object.entries)
                           js->clj
                           reverse
                           format-fn
                           )]
    {:x (mapv first data')
     :y (mapv second data')}))

(defn get-stock-data
  "Fetch stock data from Alphavantage"
  [w-key]
  (let [url (alpha-vantage-url w-key symbol)]
    (shell/run-shell-command {:event     [:set-data w-key]
                              :format-fn (partial format-data w-key)
                              :cmd       "curl"
                              :args      ["-X" "GET" url]})))

(defn stock-chart
  "Display stock data as a graph"
  [{:keys [w-key data height width]}]
  (let [[start end] ((juxt first last) (:y data))
        left  4
        color (let [perc ((fnil / 1 1) end start)]
                (cond
                  (= perc 1.0) :yellow
                  (> perc 1.0) :green
                  (< perc 1.0) :red
                  :else :blue))]
    [:<>
     [:text
      {:top     0
       :left    left
       :height  (dec height)
       :width   (- width left)
       :content (data->chart
                  (:y data)
                  (dec (- width left))
                  (dec height)
                  color)}]
     [:text
      {:top     0
       :right   0
       :align   :right
       :content (let [plus-minus (utils/round-dec (- end start) 2)
                      percent    (utils/round-dec (/ end start) 2)]
                  (utils/color
                    (str plus-minus " (" percent "%)")
                    color))}]
     [:text
      {:top     (dec height)
       :left    left
       :height  1
       :width   (- width left)
       :content (x-axis w-key (- width left) (:x data))}]
     [:list
      {:height (dec height)
       :width  left
       :items  (y-axis w-key (dec height) (:y data))}]]))