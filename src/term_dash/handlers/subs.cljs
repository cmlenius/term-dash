(ns term-dash.handlers.subs
  "Re-frame app db subscriptions. Essentially maps a keyword describing a
  result to a function that retrieves the current value from the app db."
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  :db
  (fn [db _]
    db))

(rf/reg-sub
  :view
  (fn [db _]
    (:router/view db)))

(rf/reg-sub
  :data
  (fn [db [_ path]]
    (let [db-path (if (vector? path) path [path])]
      (get-in db (into [:data] db-path)))))

(rf/reg-sub
  :size
  (fn [db _]
    (:size db)))