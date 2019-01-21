(ns waifoo.client
  (:require [waifoo.config :as config]
            [waifoo.util.defservice :refer-macros [defservice]]
            [waifoo.util.logging :refer [warn]]
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

(defmulti handler :id)

(defmethod handler :default [{event :event}]
  (warn "Unhandled" event))

(defmethod handler :chsk/handshake [message]
  (send! [:app/init]))

(defmethod handler :todos/all [{[_ new-todos] :event}]
  (reset! todos new-todos))

(defmethod handler :todos/insert [{[_ todo] :event}]
  (swap! todos #(assoc % (:todo/id todo) todo)))

(defmethod handler :todos/remove [{[_ id] :event}]
  (swap! todos #(dissoc % id)))

(defn view-new-todo-form []
  (reagent/with-let
    [new-description (reagent/atom "")
     
     handle-submit
     (fn [event]
       (.preventDefault event)
       (send! [:todos/create @new-description])
       (reset! new-description ""))
     
     handle-change
     (fn [event]
       (let [value (-> event .-currentTarget .-value)]
         (reset! new-description value)))]

    [:form {:on-submit handle-submit}
      [:input {:value @new-description
               :on-change handle-change
               :required true}]
      [:button "Add"]]))

(defn view-todo [{:keys [:todo/id :todo/description :todo/active?]}]
  [:div
   [:button {:on-click #(send! [:todos/remove id])} "remove"]
   [:button {:on-click #(send! [:todos/toggle id])} "toggle"]
   [:span {:style {:text-decoration (if active? :none :line-through)}} " " description]])

(defn view-todos []
  (if (nil? @todos)
    [:div "Loading..."]
    [:ul (for [todo (vals @todos)]
          ^{:key (:todo/id todo)}
          [view-todo todo])]))

(defn view []
  [:div
   [view-new-todo-form]
   [view-todos]])

(defservice ws-router
  (sente/start-client-chsk-router!
    (:ch-recv socket)
    handler))

(let [root (js/document.getElementById "root")]
  (reagent/render [view] root))
  
