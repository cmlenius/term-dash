(ns term-dash.handlers.events
  (:require
    [re-frame.core :as rf]))

;; ===== GENERAL =====
(rf/reg-event-db
  :init
  (fn [_ [_ opts terminal-size]]
    {:opts opts
     :size terminal-size
     :data {:clock "true"}}))

(rf/reg-event-db
  :update-size
  (fn [db [_ size]]
    (assoc db :size size)))

(rf/reg-event-db
  :set
  (fn [db [_ db-key value]]
    (assoc db db-key value)))

;; ===== DATA =====
(rf/reg-event-db
  :set-data
  (fn [db [_ path data]]
    (let [db-path (if (vector? path) path [path])]
      (assoc-in db (into [:data] db-path) data))))


(rf/reg-event-db
  :set-cpu-usage
  (fn [db [_ data]]
    (update-in db [:data :cpu-usage]
               (fn [cur-val]
                 (let [c (count cur-val)
                       x 200]
                   (cond
                     (= c 0) [data]
                     (< c x) (conj cur-val data)
                     :else (conj (vec (drop 1 cur-val)) data)))))))

(rf/reg-event-db
  :set-network-speed
  (fn [db [_ {:keys [rx_sec tx_sec]}]]
    (if (and (not (neg? rx_sec)) (not (neg? tx_sec)))
      (let [append (fn [cur-val new-val]
                     (let [c (count cur-val)
                           x 200]
                       (cond
                         (= c 0) [new-val]
                         (< c x) (conj cur-val new-val)
                         :else (conj (vec (drop 1 cur-val)) new-val))))]
        (update-in db [:data :network-usage :speed]
                   #(-> (assoc % :rx_sec (append (:rx_sec %) rx_sec))
                        (assoc :tx_sec (append (:tx_sec %) tx_sec)))))
      db)))
