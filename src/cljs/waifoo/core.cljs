(ns waifoo.core
  (:require [reagent.core :as reagent]
            [taoensso.sente :as sente]))

(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" nil {:type :auto, :host "localhost:8081"})]
  (def chsk       chsk)
  (def ch-chsk    ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(def counter-value (reagent/atom :not-initialized)) 

(defmulti user-handler first)
(defmethod user-handler :default [event]
  (println "unhandled"))
(defmethod user-handler :value/set [[_ value]]
  (reset! counter-value value))

(defmulti ws-handler* :id)
(defmethod ws-handler* :default [message]
  (println "unhandled"))
(defmethod ws-handler* :chsk/handshake [message]
  (chsk-send! [:value/get]))
(defmethod ws-handler* :chsk/recv [{:as ev-msg :keys [id data event]}]
  (user-handler (second event)))

(defn ws-handler [{:as message :keys [event]}]
  (println ">" event)
  (ws-handler* message))

(defn counter []
  (case @counter-value
    :not-initialized [:div "Loading..."]
    [:div "Counter: "
      [:button {:on-click #(chsk-send! [:value/dec])} "-"]
      [:span @counter-value]
      [:button {:on-click #(chsk-send! [:value/inc])} "+"]]))

(defn start! []
  (sente/start-client-chsk-router! ch-chsk ws-handler)
  (reagent/render [counter] (js/document.getElementById "root")))

(start!)


