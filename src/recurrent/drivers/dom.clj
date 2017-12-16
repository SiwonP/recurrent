(ns recurrent.drivers.dom)

(defmacro collect [class-name dom-signal-key & components]
  `(elmalike.signal/map
     (fn [components#]
       ^{:hipo/key ~class-name}
       `[:div {:class ~~class-name}
         ~@components#])
         (elmalike.signal/latest 
           ~@(map (fn [component-name] `(~(keyword dom-signal-key) ~component-name)) components))))
