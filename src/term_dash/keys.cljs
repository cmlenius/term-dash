(ns term-dash.keys
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]))

(def global-bindings
  {["C-c"] #(.exit js/process 0)})

(defn bind-keys
  "Set key bindings mapping keys to functions. {[key-bindings] #(..)}"
  [screen key-bindings]
  (doseq [[hotkeys f] key-bindings]
    (.key screen (clj->js hotkeys) f)))

(defn unbind-keys
  "Remove key bindings from blessed screen instance."
  [screen key-bindings]
  (doseq [[hotkeys f] key-bindings]
    (.unkey screen (clj->js hotkeys) f)))

(defn setup
  "Bind global-bindings to blessed screen instance."
  [screen]
  (bind-keys screen global-bindings))

(defn with-keys
  "Wrap a hiccup element with key-bindings. The bindings are created when
   the component is mounted and removed when the component is removed.
   Takes a blessed screen instance, map of key bindings, and a hiccup element:

   screen       blessed/screen - A blessed screen instance
   key-bindings hash-map       - Map of keybindings to handler functions
   content      vector         - A hiccup element vector to wrap

   Returns a wrapped hiccup reagent element.

   Example:
   (with-keys screen {[\"q\" \"esc\"] #(rf/dispatch [:app/quit])}
     [:box \"Quit me.\"])"
  [screen key-bindings content]
  (r/with-let [_ (bind-keys screen key-bindings)]
    content
    (finally
      (unbind-keys screen key-bindings))))
