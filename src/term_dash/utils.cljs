(ns term-dash.utils
  (:require
    [clojure.string :as string]))

(def colors
  {"black"   "30m" :black   "30m"
   "red"     "31m" :red     "31m"
   "green"   "32m" :green   "32m"
   "yellow"  "33m" :yellow  "33m"
   "blue"    "34m" :blue    "34m"
   "magenta" "35m" :magenta "35m"
   "cyan"    "36m" :cyan    "36m"
   "white"   "37m" :white   "37m"})

(defn abs [n]
  (max n (- n)))

(defn color [data color]
  (str "\033[" (get colors color) data "\033[0m"))

(defn date-now []
  (.toLocaleString (js/Date.) "se-SE" #js{:timeZone "America/New_York"}))

(defn get-inner-size [{:keys [cols rows]} widthp heightp]
  (let [width  (-> cols (* 0.01 (js/parseInt widthp)) int (- 2))
        height (-> rows (* 0.01 (js/parseInt heightp)) int (- 2))]
    {:width  width
     :height height}))

(defn round-dec [num digits]
  (let [num    (js/parseFloat num)
        factor (if (pos-int? digits)
                 (reduce * (repeat digits 10))
                 1)]
    (-> num (* factor) int (/ factor))))

(defn strip-color [s]
  (string/replace s #"(\033\[([0-9]{2})m|\033\[0m)" ""))

(defn space-before [s num]
  (let [diff (- num (count (strip-color s)))]
    (if (pos-int? diff)
      (reduce #(str " " %) s (range diff))
      s)))