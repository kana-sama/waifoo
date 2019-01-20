(ns waifoo.util.defservice
  (:require [waifoo.util.logging :refer [info]]))

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
           (info "[Re]Starting" ~service-name))
         (~start))))
