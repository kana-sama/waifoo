(ns waifoo.util.env
  (:require [dotenv]))

(defmacro env [key]
  (dotenv/env key))
