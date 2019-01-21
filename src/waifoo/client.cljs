(ns waifoo.client
  (:require [waifoo.config :as config]
            [waifoo.util.defservice :refer-macros [defservice]]
            [waifoo.util.logging :refer [warn]]
            [clojure.core.match :refer [match]]
            [reagent.core :as reagent]
            [taoensso.sente :as sente]))

(def socket
  (sente/make-channel-socket! "/chsk" nil
    {:type :auto
     :host (str "localhost:" config/port)
     :wrap-recv-evs? false}))

(defn send! [event]
  ((:send-fn socket) event))

(def todos
  (reagent/atom nil))

(defn handler [{:keys [id event]}]
  (match event
    [:chsk/handshake _] (send! [:app/init])
    [:todo/init todos*] (reset! todos todos*)
    [:todo/upsert todo] (swap! todos #(assoc % (:todo/id todo) todo))
    [:todo/remove todo-id] (swap! todos #(dissoc % todo-id))
    :else (warn "Unhandled" event)))

(def new-description
  (reagent/atom ""))

(defn handle-new-todo-form-submit [event]
  (.preventDefault event)
  (send! [:todo/add @new-description])
  (reset! new-description ""))

(defn handle-new-description-change [event]
  (let [value (-> event .-currentTarget .-value)]
    (reset! new-description value)))

(defn view-new-todo-form []
  [:form {:on-submit handle-new-todo-form-submit}
   [:input {:value @new-description, :on-change handle-new-description-change}]
   [:button "Add"]])

(defn view-todo [{:keys [:todo/id :todo/description :todo/active?]}]
  [:div
   [:button {:on-click #(send! [:todo/remove id])} "remove"]
   [:button {:on-click #(send! [:todo/toggle id])} "toggle"]
   [:span {:style {:text-decoration (if active? :none :line-through)}} " " description]])

(defn view []
  [:div
   [view-new-todo-form]
   (if (nil? @todos)
     [:div "Loading..."]
     [:ul (for [todo (vals @todos)] ^{:key (:todo/id todo)} [view-todo todo])])])

(defservice ws-router
  (sente/start-client-chsk-router!
    (:ch-recv socket)
    handler))

(let [root (js/document.getElementById "root")]
  (reagent/render [view] root))
  
