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

(def state
  (reagent/atom nil))

(defmulti handler :id)

(defmethod handler :default [{event :event}]
  (warn "Unhandled" event))

(defmethod handler :chsk/handshake [message]
  (send! [:waifoo/init]))

(defmethod handler :waifoo/update-state [{[_ new-state] :event}]
  (reset! state new-state))

(defn view-new-user-form []
  (reagent/with-let
    [new-name (reagent/atom "")
     
     handle-submit
     (fn [event]
       (.preventDefault event)
       (send! [:users/create @new-name])
       (reset! new-name ""))
     
     handle-change
     (fn [event]
       (let [value (.-currentTarget.value event)]
         (reset! new-name value)))]

    [:form {:on-submit handle-submit}
     [:input {:value @new-name
              :on-change handle-change
              :required true}]
     [:button "Add"]]))

(defn view-user [{:user/keys [id name]}]
  [:div
   [:button {:on-click #(send! [:users/remove id])} "x"]
   [:span name]])

(defn view-users-list []
  (if (nil? @state)
    "Loading..."
    [:ul {:style {:padding 0}}
      (for [user (-> @state :users vals)]
        ^{:key (:user/id user)}
        [view-user user])]))

(defn view-users []
  [:div
   [view-new-user-form]
   [view-users-list]])

(defn view []
  [view-users])

(defn start-controller []
  (sente/start-client-chsk-router!
    (:ch-recv socket) handler))

(defn render []
  (let [root (js/document.getElementById "root")]
    (reagent/render [view] root)))

(defn restart []
  (start-controller)
  (render))

(restart)
