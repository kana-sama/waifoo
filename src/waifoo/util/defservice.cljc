(ns waifoo.util.defservice
  (:require [taoensso.timbre :refer [info]]))

(defn color-str [color & args]
  #?(:clj  (apply taoensso.timbre/color-str color args)  
     :cljs (apply str args)))

(defmacro defservice
  "Define service (start and stop commands) which will automatically stops on reload."
  [service constructor]
  (let [service-name (name service)
        instance (symbol service-name)
        stop (symbol (str "stop-" service-name "!"))
        start (symbol (str "start-" service-name "!"))]
    `(do (defonce ~instance (atom nil))
         (defn ~stop []
           (when-not (nil? @~instance)
             (@~instance)
             (reset! ~instance nil)))
         (defn ~start []
           (~stop)
           (reset! ~instance ~constructor)
           (info (color-str :blue "[Re]Starting " ~service-name)))
         (~start))))