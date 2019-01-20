(ns waifoo.util.logging
  (:require [clojure.string :refer [join]]))

(defn log [& args]
  (.log js/console (join " " args)))

(defn info [& args]
  (.info js/console (join " " args)))

(defn warn [& args]
  (.warn js/console (join " " args)))

(defn error [& args]
  (.error js/console (join " " args)))
