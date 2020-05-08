(ns term-dash.widgets.processes
  (:require
    [re-frame.core :as rf]
    [term-dash.widgets.components.table :refer [table]]))

(def si (js/require "systeminformation"))

(def headers ["PID" "CPU %" "MEM %" "NAME"])
(def proc-cols [:pid :pcpu :pmem :name])

(defn format-data [data]
  "Format data to be displayed in table format"
  (some->> data
           (sort-by :pcpu #(compare %2 %1))
           (mapv #(select-keys % proc-cols))
           (mapv #(clojure.set/rename-keys % (->> (map vector proc-cols headers)
                                                  (into {}))))))

(defn get-processes
  "Fetch cpu processes data"
  []
  (.processes si #(rf/dispatch [:set-data :processes (-> (js->clj % :keywordize-keys true)
                                                         :list
                                                         format-data)])))

(defn processes
  "Displays processes in a list sorted by cpu% usage"
  [{:keys [data height]}]
  [table {:headers headers
          :rows    (take (dec height) data)}])
