(ns term-dash.widgets.weather
  (:require
    [clojure.string :as string]
    [reagent.core :as r]
    [term-dash.shell :as shell]
    [term-dash.utils :as utils]))

(def times ["Morning" "Afternoon" "Evening" "Night"])
(def days ["Today" "Tomorrow" "Day After"])
(def horizontal-spacing 30)

(defn days->color [day]
  (get {"Today"     :green
        "Tomorrow"  :blue
        "Day After" :red} day))

(defn get-time-of-day [hour]
  (cond
    (< hour 6) "Morning"
    (< hour 12) "Afternoon"
    (< hour 18) "Evening"
    :else "Night"))

(defn weather-for
  "Weather component for a time of day"
  [top left data [day time :as day-time]]
  (let [label (utils/color
                (str day " - " time)
                (days->color day))]
    [:text {:top     top
            :left    left
            :content (str "       " label "\n" (get-in data day-time))}]))

(defn weather-rows
  "Component for a row of weather components"
  [{:keys [width data times-of-day double-row?]}]
  (let [num-of-cols  (quot width horizontal-spacing)
        num-of-boxes (if double-row?
                       (* 2 num-of-cols)
                       num-of-cols)]
    [:<>
     (map-indexed
       (fn [idx day-time]
         (with-meta
           (if (< idx num-of-cols)
             [weather-for 0 (* idx horizontal-spacing) data day-time]
             [weather-for 7 (* (- idx num-of-cols) horizontal-spacing) data day-time])
           {:key idx}))
       (take num-of-boxes times-of-day))]))

(defn format-weather-data
  "Format data for display"
  [data]
  (let [data        (string/split data #"\n")
        parse-times (fn [strings]
                      (for [i (range 4)]
                        [(get times i)
                         (->> (mapv #(nth % i) strings)
                              (string/join "\n"))]))
        parse-days  (fn [strings]
                      (->> strings
                           (mapv #(string/split % #"│"))
                           (mapv (comp vec rest))
                           parse-times
                           (into {})))]
    {:location   (-> (first data) (string/split #" ") last)
     :now        (string/join "\n" (subvec data 2 7))
     "Today"     (parse-days (subvec data 11 16))
     "Tomorrow"  (parse-days (subvec data 21 26))
     "Day After" (parse-days (subvec data 31 36))}))

(defn get-weather-data
  "Fetch weather data using wttr.in"
  []
  (shell/run-shell-command {:event     [:set-data :weather]
                            :format-fn format-weather-data
                            :cmd       "curl"
                            :args      ["wttr.in/Toronto"]}))

(defn weather
  "Displays weather information in a table using ascii images"
  []
  (let [hour (r/atom (.getHours (js/Date.)))]
    (fn [{:keys [data width height] :as props}]
      (let [times-of-day (let [offset (inc (.indexOf times (get-time-of-day (- @hour 6))))]
                           (->> (for [i (range 12)
                                      :when (>= i offset)]
                                  (let [idx  i
                                        time (nth times (rem idx 4))
                                        day  (nth days (quot idx 4))]
                                    [day time]))))
            double-row?  (>= height 13)]
        [:<>
         [:text {:top     (- (quot height 2) 3)
                 :left    0
                 :content (str (utils/color
                                 (str "     Location: " (:location data))
                                 :green)
                               "\n" (:now data))}]
         [:box {:top    (- (quot height 2) (if double-row? 6 3))
                :left   horizontal-spacing
                :width  (- width horizontal-spacing)
                :height (if double-row? 13 6)}
          [weather-rows (merge props {:width       (- width horizontal-spacing)
                                      :times-of-day times-of-day
                                      :double-row? double-row?})]]]))))
