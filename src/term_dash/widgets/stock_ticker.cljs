(ns term-dash.widgets.stock-ticker
  (:require
    [term-dash.shell :as shell]
    [term-dash.utils :as utils]
    [term-dash.widgets.components.table :refer [table]]))

(def baseURL "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&apikey=SC171S2F4AAA4NRW")
(def headers ["Symbol" "Price" "Change" "Change %"])
(def stock-list ["VFV.TO" "XEF.TO" "ZCN.TO" "MG" "AC"])

(defn format-data
  "Format data to be displayed in table format"
  [data]
  (let [data'  (some-> data
                       (js/JSON.parse)
                       js->clj
                       (get "Global Quote")
                       (clojure.set/rename-keys {"01. symbol"         "Symbol"
                                                 "05. price"          "Price"
                                                 "09. change"         "Change"
                                                 "10. change percent" "Change %"})
                       (select-keys headers))
        color  (if (-> (get data' "Change") js/parseFloat neg?) :red :green)]
    (reduce-kv
      (fn [m k value]
        (let [rounded (utils/round-dec value 2)]
          (assoc m k (utils/color
                       (case k
                         "Symbol" value
                         "Change %" (str rounded "%")
                         rounded)
                       color))))
      {}
      data')))

(defn get-stock-data
  "Fetch stock data from Alphavantage"
  []
  (doseq [idx (range (count stock-list))]
    (let [stock (nth stock-list idx)
          url   (str baseURL "&symbol=" stock)]
      (js/setTimeout
        #(shell/run-shell-command {:event     [:set-data [:stock-ticker stock]]
                                   :format-fn format-data
                                   :cmd       "curl"
                                   :args      ["-X" "GET" url]})
        (* idx 60000)))))

(defn stock-ticker
  "Displays stock data in a list"
  [{:keys [data]}]
  [table {:headers headers
          :rows    (mapv #(get data %) stock-list)}])
