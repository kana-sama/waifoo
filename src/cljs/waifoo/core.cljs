(ns waifoo.core
  (:require [reagent.core :as reagent]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!] :refer-macros [go]]))

(defonce counter-value
  (reagent/atom 0))

(def loading?
  (reagent/atom true))

(defn fetch [action]
  (-> (str "http://localhost:3001/value/" action)
    (http/get {:with-credentials? false}) 
    (:body)
    (int)))

(defn dispatch [action]
  (go (reset! loading? true)
      (reset! counter-value (<! (fetch action)))
      (reset! loading? false)))

(defn counter []
  (if @loading?
    [:div "Loading..."]
    [:div "Counter: "
     [:button {:on-click #(dispatch "dec")} "-"]
     [:span @counter-value]
     [:button {:on-click #(dispatch "inc")} "+"]]))

(defn run []
  (dispatch "get")
  (reagent/render [counter] (js/document.getElementById "root")))

(run)