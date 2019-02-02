(ns waifoo.config
  #?(:clj  (:require [waifoo.util.env :refer [env]])
     :cljs (:require-macros [waifoo.util.env :refer [env]])))

(def parse-int
  #?(:clj  #(Integer/parseInt %)
     :cljs js/parseInt))

(def port (parse-int (env :PORT)))
