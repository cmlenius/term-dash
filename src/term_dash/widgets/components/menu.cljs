(ns term-dash.widgets.components.menu
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :refer [join]]
    [re-frame.core :as rf]
    [reagent.core :as r]
    [term-dash.screen :refer [screen]]
    [term-dash.keys :refer [with-keys]]))

(defn loader
  "Shows a mock-loader progress bar for dramatic effect.
  - Uses a js interval to update it every 15 ms until progress is 100.
  - Starts the timer on each mount."
  [_]
  (r/with-let [progress (r/atom 0)
               interval (atom nil)]
    (r/create-class
      {:component-did-mount
       (fn [_]
         (reset! interval (js/setInterval #(swap! progress inc) 15))
         )

       :component-did-update
       (fn [_]
         (when (>= @progress 100)
           (js/clearInterval interval)))

       :reagent-render
       (fn []
         [:box#loader
          {:top   0
           :width "100%"}
          [:box
           {:top     1
            :width   "100%"
            :align   :center
            :content "Loading Demo"}]
          [:box
           {:top     2
            :width   "100%"
            :align   :center
            :style   {:fg :gray}
            :content "Loading..."}]
          [:progressbar
           {:orientation :horizontal
            :style       {:bar     {:bg :magenta}
                          :border  {:fg :cyan}
                          :padding 1}
            :border      {:type :line}
            :filled      @progress
            :left        0
            :right       0
            :width       "100%"
            :height      3
            :top         4
            :label       " progress "}]])})))

;; =========================================================================

(defn router
  "Takes a map of props:
  :views map     - Map of values (usually keywords) to hiccup view functions
  :view  keyword - Current view to display. Should be the key of :views map

  Returns the hiccup vector returned by the selected view-fn.

  Example:
  (router {:views {:home home-fn
                   :about about-fn}
           :view :home})
  "
  [{:keys [views view] :as props}]
  [(get views view) props])

(defn- find-index
  "Takes a target value and a map of options.
  Returns index of target value if found in map of options or nil if target
  was not found."
  [target options]
  (some->> options
           (keys)
           (map-indexed vector)
           (filter (fn [[idx v]] (= v target)))
           (first)
           (first)))

(defn- next-option
  "Takes the current keyword key of the options map and a map of options.
  Returns the next key of the map or the first key of the map."
  [current options]
  (let [total       (count options)
        current-idx (find-index current options)
        next-idx    (inc current-idx)]
    (-> options
        (vec)
        (nth (if (< next-idx total) next-idx 0))
        (key))))

(defn- prev-option
  "Takes the current keyword key of the options map and a map of options.
  Returns the previous key of options map or the last key of the options map."
  [current options]
  (let [total       (count options)
        current-idx (find-index current options)
        prev-idx    (dec current-idx)]
    (-> options
        (vec)
        (nth (if (< prev-idx 0) (dec total) prev-idx))
        (key))))

(defn vertical-menu
  "Display an interactive vertical-menu component.

  Takes a hash-map of props:
  :bg        keyword|str - Background color of highlighted item.
  :box       hash-map    - Map of props to merge into menu box properties.
  :default   keyword     - Selected options map keyword key
  :fg        keyword|str - Text color of highlighted item.
  :on-select function    - Function to call when item is selected
  :options   hash-map    - Map of keyword keys to item labels

  Returns a reagent hiccup view element.

  Example:
  (vertical-menu
   {:bg :cyan
    :box {:top 3}
    :default :a
    :fg :white
    :on-select #(println \"selected: \" %)
    :options {:a \"Item A\"
              :b \"Item B\"
              :c \"Item C\"}})"
  [{:keys [bg box default fg on-select options]}]
  (r/with-let [selected (r/atom (or default (->> options first key)))]
    (with-keys @screen {["j" "down"]  #(swap! selected next-option options)
                        ["k" "up"]    #(swap! selected prev-option options)
                        ["l" "enter"] #(on-select @selected)}
               (let [current @selected]
                 [:box#menu
                  (merge
                    {:top    1
                     :left   1
                     :right  1
                     :bottom 1}
                    box)
                  (for [[idx [value label]] (map-indexed vector options)]
                    [:box {:key     value
                           :top     idx
                           :style   {:bg (when (= value current) (or bg :green))
                                     :fg (when (= value current) (or fg :white))}
                           :height  1
                           :content label}])]))))

(defn menu
  "Displays a blessed js box with a vertical-menu used for navigation.
  User can use j/k or up/down to navigate items and either enter or l to view
  a page. Dispatches re-frame :update to set :router/view in app db.
  Returns a hiccup :box vector."
  [_]
  [vertical-menu
   {:options   {:home      "Intro"
                :about     "About"
                :resources "Resources"
                :credits   "Credits"}
    :bg        :magenta
    :fg        :black
    :on-select #(rf/dispatch [:set :router/view %])}])
