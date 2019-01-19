(ns waifoo.core
  (:require [clojure.core.match :refer [match]]
            [reagent.core :as reagent]
            [taoensso.sente :as sente]))

(def socket
  (sente/make-channel-socket! "/chsk" nil {:type :auto, :host "localhost:8081"}))

(defn send! [event]
  ((:send-fn socket) event))

(def state
  (reagent/atom :not-initialized)) 

(defn handler [{:keys [id event]}]
  (println ">" event)
  (match event
    [:chsk/handshake _] (send! [:value/get])
    [:chsk/recv [:value/set value]] (reset! state value)
    :else (println "?" id)))

(defn view []
  (case @state
    :not-initialized [:div "Loading..."]
    [:div "Counter: "
      [:button {:on-click #(send! [:value/dec])} "-"]
      [:span @state]
      [:button {:on-click #(send! [:value/inc])} "+"]]))

(sente/start-client-chsk-router! (:ch-recv socket) handler)
(reagent/render [view] (js/document.getElementById "root"))
