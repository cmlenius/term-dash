(ns term-dash.widgets.components.table
  (:require
    [clojure.string :as string]
    [term-dash.utils :as utils]))

(defn rows->columns
  "Turns a vector of maps representing rows into a list of vectors representing columns"
  [headers rows]
  (for [[idx header] (map-indexed vector headers)]
    (cons (nth headers idx) (map #(str (get % header)) rows))))

(defn gen-column-widths
  "Generates a list of max column widths with an offset"
  [columns offset]
  (->> columns
       (mapv #(map utils/strip-color %))
       (mapv (fn [col]
               (let [max-len (reduce max (map count col))]
                 (+ offset max-len))))))

(defn table
  "Table component using a number of lists as columns"
  [{:keys [headers rows spacing]
    :or {spacing 4}}]
  (let [columns    (rows->columns headers rows)
        col-widths (gen-column-widths columns spacing)]
    [:<>
     (map-indexed (fn [i col]
                    (let [col-width (- (nth col-widths i) spacing)
                          left      (reduce + (take i col-widths))]
                      [:list {:key   i
                              :items col
                              :left  left}]))
                  columns)]))


(table {:headers ["Symbol" "Price" "Change" "Change %"]
        :rows    (mapv #(get @(re-frame.core/subscribe [:data :stock-ticker]) %) ["vfv.to" "xef.to"])})