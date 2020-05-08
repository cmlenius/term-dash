(ns term-dash.core
  "Main application entrypoint. Defines root UI view, cli-options,
  arg parsing logic, and initialization routine"
  (:require
    [clojure.tools.cli :refer [parse-opts]]
    [mount.core :refer [defstate] :as mount]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [term-dash.handlers.events]
    [term-dash.handlers.subs]
    [term-dash.resize :refer [size]]
    [term-dash.screen :refer [render screen]]
    [term-dash.dashboard :refer [dashboard]]))


;; =====================================================================

(def cli-options
  [["-p" "--port PORT" "port number"
    :default 80
    :parse-fn #(js/Number %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-v" nil "Verbosity level"
    :id :verbosity
    :default 0
    :update-fn inc]
   ["-h" "--help"]])

(defn args->opts
  "Takes a list of arguments. Returns a map of parsed CLI args."
  [args]
  (parse-opts args cli-options))

(defn init!
  "Initialize the application. Takes a root UI view function that returns a hiccup
   element and optionally a map of parsed CLI args. Returns rendered reagent view."
  [ui & {:keys [opts]}]
  (mount/start)
  (rf/dispatch-sync [:init (:options opts) (size @screen)])
  (-> (r/reactify-component ui)
      (r/create-element #js {})
      (render @screen)))

(defn -main
  "Main application entry point function. Takes list of CLI args.
   Returns rendered reagent view."
  [& args]
  (init! dashboard :opts (args->opts args)))

(set! *main-cli-fn* -main)
