(ns waifoo.config
  #?(:clj  (:require [waifoo.util.env :refer [env]])
     :cljs (:require-macros [waifoo.util.env :refer [env]])))

(def parse-int
  #?(:clj  #(Integer/parseInt %)
     :cljs js/parseInt))

(def port (parse-int (env :PORT)))
(def database-endpoint (env :DB-ENDPOINT))
(def database-access-key (env :DB-ACCESS-KEY))
(def database-secret (env :DB-SECRET))
(def database-name (env :DB-NAME))
