(ns term-dash.widgets.docker
  (:require
    [re-frame.core :as rf]
    [term-dash.widgets.components.table :refer [table]]))

(def si (js/require "systeminformation"))

(def headers ["NAME" "STATE" "CPU %" "MEM USAGE" "IMAGE" "PIDS"])
(def docker-cols [:name :state :cpu_percent :mem_usage :image :pids])

(defn format-docker-data
  "Format data to be displayed in table format"
  [data]
  (let [bytes->megabytes #(str (/ (quot % 10000) 100) " MB")
        decimal->percent #(str (subs (str %) 0 3) "%")]
    (some->> data
             (mapv #(select-keys % docker-cols))
             (mapv #(-> (update % :mem_usage bytes->megabytes)
                        (update :cpu_percent decimal->percent)
                        (clojure.set/rename-keys (->> (map vector docker-cols headers)
                                                      (into {}))))))))

(defn get-docker-data
  "Fetch docker data"
  []
  (.dockerAll si #(rf/dispatch [:set-data :docker (format-docker-data (js->clj % :keywordize-keys true))])))

(defn docker
  "Displays docker data in a table"
  [{:keys [data]}]
  [table {:headers headers
          :rows    data}])