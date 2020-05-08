(ns term-dash.shell
  (:require-macros
    [cljs.core.async.macros :refer [go]])
  (:require
    [cljs.nodejs :as nodejs]
    [cljs.core.async :refer [put! <! chan alts! timeout]]
    [clojure.string :as string]
    [re-frame.core :as rf]))

;; For debugging
(def result* (atom nil))

(nodejs/enable-util-print!)
(def spawn (.-spawn (js/require "child_process")))

(defn exec-chan
  "Spawns a child process for cmd with args. routes stdout, stderr, and
   the exit code to a channel. returns the channel immediately."
  [cmd args]
  (let [c (chan 5)
        p (spawn cmd args)]
    (.on (.-stdout p) "data" #(put! c [:out (str %)]))
    (.on (.-stderr p) "data" #(put! c [:err (str %)]))
    (.on p "close" #(put! c [:exit (str %)]))
    c))

(defn exec
  "Executes cmd with args. returns a channel immediately which
   will eventually receive a result vector of pairs [:kind data-str]
   with the last pair being [:exit code]"
  [cmd & args]
  (let [c (exec-chan cmd (clj->js args))]
    (go (loop [output (<! c)
               result []]
          (if (= :exit (first output))
            (conj result output)
            (recur (<! c) (conj result output)))))))

(defn run-shell-command
  "Runs a shell command then saves the result via event after applying format-fn"
  [{:keys [event format-fn cmd args]}]
  (go (let [result (<! (apply exec cmd args))]
        (reset! result* result)
        (rf/dispatch (into event (vector (some->> result
                                                  (keep #(when (= (first %) :out)
                                                           (second %)))
                                                  (string/join "")
                                                  format-fn)))))))
