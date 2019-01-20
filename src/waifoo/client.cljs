(ns waifoo.client
  (:require [waifoo.config :as config]
            [clojure.core.match :refer [match]]
            [reagent.core :as reagent]
            [taoensso.timbre :refer [warn]]
            [taoensso.sente :as sente]))

(def socket
  (sente/make-channel-socket! "/chsk" nil
    {:type :auto
     :host (str "localhost:" config/port)
     :wrap-recv-evs? false}))

(defn send! [event]
  ((:send-fn socket) event))

(def state
  (reagent/atom :not-initialized)) 

(defn handler [{:keys [id event]}]
  (match event
    [:chsk/handshake _] (send! [:value/get])
    [:value/set value] (reset! state value)
    :else (warn "Unhandled" event)))

(defn view []
  (case @state
    :not-initialized [:div "Loading..."]
    [:div "Counter: "
      [:button {:on-click #(send! [:value/dec])} "-"]
      [:span @state]
      [:button {:on-click #(send! [:value/inc])} "+"]]))

(sente/start-client-chsk-router! (:ch-recv socket) handler)
(reagent/render [view] (js/document.getElementById "root"))
