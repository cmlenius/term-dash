(ns term-dash.config
  (:require
    [term-dash.widgets.clock :as clock]
    [term-dash.widgets.cpu-usage :as cpu-usage]
    [term-dash.widgets.docker :as docker]
    [term-dash.widgets.network-usage :as network-usage]
    [term-dash.widgets.processes :as processes]
    [term-dash.widgets.stock-chart :as stock-chart]
    [term-dash.widgets.stock-ticker :as stock-ticker]
    [term-dash.widgets.weather :as weather]))

(def DAILY 1440000)
(def HOURLY 3600000)
(def MINUTE 60000)

(def config
  {:clock          {:top            "0%"
                    :left           "0%"
                    :width          "30%"
                    :height         "20%"
                    :label          "Clock"
                    :render         clock/clock
                    :interval-fn    clock/set-time
                    :interval-delay 1000}
   :cpu-usage      {:top            "0%"
                    :left           "50%"
                    :width          "25%"
                    :height         "30%"
                    :label          "CPU Usage"
                    :render         cpu-usage/cpu-usage
                    :interval-fn    cpu-usage/get-cpu-usage
                    :interval-delay 400}
   :docker         {:top            "60%"
                    :left           "50%"
                    :width          "50%"
                    :height         "15%"
                    :label          "Docker"
                    :render         docker/docker
                    :interval-fn    docker/get-docker-data
                    :interval-delay 10000}
   :network-usage  {:top            "30%"
                    :left           "50%"
                    :width          "25%"
                    :height         "30%"
                    :label          "Network Usage"
                    :render         network-usage/network-usage
                    :interval-fn    network-usage/get-net-usage
                    :interval-delay 500}
   :processes      {:top            "0%"
                    :left           "75%"
                    :width          "25%"
                    :height         "58%"
                    :label          "Processes"
                    :render         processes/processes
                    :interval-fn    processes/get-processes
                    :interval-delay 7500}
   :stock-intraday {:top            "20%"
                    :left           "0%"
                    :width          "50%"
                    :height         "40%"
                    :label          "Stock Chart Intraday"
                    :render         stock-chart/stock-chart
                    :interval-fn    (partial stock-chart/get-stock-data :stock-intraday)
                    :interval-delay (* MINUTE 10)}
   :stock-daily    {:top            "60%"
                    :left           "0%"
                    :width          "50%"
                    :height         "40%"
                    :label          "Stock Chart Daily"
                    :render         stock-chart/stock-chart
                    :interval-fn    (partial stock-chart/get-stock-data :stock-daily)
                    :interval-delay DAILY}
   :stock-ticker   {:top            "0%"
                    :left           "30%"
                    :width          "20%"
                    :height         "20%"
                    :label          "Stock Ticker"
                    :render         stock-ticker/stock-ticker
                    :interval-fn    stock-ticker/get-stock-data
                    :interval-delay (* MINUTE 10)}
   :weather        {:top            "75%"
                    :left           "50%"
                    :width          "50%"
                    :height         "25%"
                    :label          "Weather"
                    :render         weather/weather
                    :interval-fn    weather/get-weather-data
                    :interval-delay HOURLY}})
