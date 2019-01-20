(ns waifoo.util.logging
  (:require [clojure.string :refer [join]]))

(def color->code-map
  {:reset   0, :black  30, :red   31
   :green  32, :yellow 33, :blue  34
   :purple 35, :cyan   36, :white 37})

(defn color->code [color]
  (str "\u001b[" (get color->code-map color 0) "m"))

(defn colorize [color text]
  (str (color->code color) text (color->code :reset)))

(defn log [& args]
  (println (colorize :reset (join " " args))))

(defn info [& args]
  (println (colorize :cyan (join " " args))))

(defn warn [& args]
  (println (colorize :yellow (join " " args))))

(defn error [& args]
  (println (colorize :red (join " " args))))
