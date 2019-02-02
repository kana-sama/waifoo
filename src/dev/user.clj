(ns user
  (:require [mount.core :as mount]
            [clojure.tools.namespace.repl :refer [clear refresh refresh-all]]))

(defn start []
  (mount/start))

(defn stop []  
  (mount/stop))

(defn reset []
  (stop)
  (refresh :after 'user/start))
