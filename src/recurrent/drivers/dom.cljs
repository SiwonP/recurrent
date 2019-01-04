(ns recurrent.drivers.dom
  (:require 
    [dommy.core :as dommy :include-macros true]
    [hipo.core :as hipo]
    [hipo.interceptor :as interceptor]
    [ulmus.signal :as ulmus]))

(defn create!
  [parent]
  (with-meta
    (fn [vdom-$]
      (let [elem-$ (ulmus/signal)
            elem (hipo/create [:div])]
        (set! (.-innerHTML parent) "")
        (.appendChild parent elem)
        (ulmus/subscribe! vdom-$
          (fn [vdom]
            (hipo/reconciliate! elem vdom)
            (ulmus/>! elem-$ elem)))

        (fn [selector event]
          (let [events-$ (ulmus/signal)
                handler (fn [e] (ulmus/>! events-$ e))]
            (ulmus/subscribe! 
              elem-$
              (fn [elem]
                (doseq [e (dommy/sel elem selector)]
                  (dommy/unlisten! e event handler)
                  (dommy/listen! e event handler))))
            events-$))))
    {:recurrent/driver? true}))

(defn for-id!
  [id]
  (create!
    (.getElementById js/document id)))

(defn isolate
  [Component]
  (fn [props sources]
    (let [scope (gensym)
          scoped-dom (fn [selector event] 
                       ((:recurrent/dom-$ sources)
                        (str "." scope " " (if (not= selector :root) selector) " ")
                        event))
          component-sinks (Component props (assoc sources
                                                  :recurrent/dom-$ scoped-dom))]
      (assoc component-sinks
             :recurrent/dom-$ (ulmus/map (fn [dom]
                                           (if (map? (second dom))
                                             (update-in dom [1 :class] (fn [class-string] (str "recurrent-component " scope " " class-string)))
                                             (assoc-in dom [1] {:class (str "recurrent-component " scope)})))
                                         (:recurrent/dom-$ component-sinks))))))
