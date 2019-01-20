(ns waifoo.client
  (:require [waifoo.config :as config]
            [waifoo.util.defservice :refer-macros [defservice]]
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

(def todos
  (reagent/atom nil))

(defn remove-todo! [id]
  (swap! todos #(filter (fn [todo] (not= id (:todo/id todo))) %)))

(defn toggle-todo! [id]
  (swap! todos
    #(map (fn [todo]
            (if (== (:todo/id todo) id)
              (update todo :todo/active? not)
              todo)) %)))

(defn handler [{:keys [id event]}]
  (match event
    [:chsk/handshake _] (send! [:waifoo/init])
    [:waifoo/set-todos todos*] (reset! todos todos*)
    [:waifoo/new-todo todo] (swap! todos #(cons todo %))
    [:waifoo/remove-todo todo-id] (remove-todo! todo-id)
    [:waifoo/toggle-todo todo-id] (toggle-todo! todo-id)
    :else (warn "Unhandled" event)))

(def new-description
  (reagent/atom ""))

(defn handle-new-todo-form-submit [event]
  (.preventDefault event)
  (send! [:waifoo/new-todo @new-description])
  (reset! new-description ""))

(defn handle-new-description-change [event]
  (let [value (-> event .-currentTarget .-value)]
    (reset! new-description value)))

(defn view-new-todo-form []
  [:form {:on-submit handle-new-todo-form-submit}
   [:input {:value @new-description, :on-change handle-new-description-change}]])

(defn view-todo [{:keys [:todo/id :todo/description :todo/active?]}]
  [:div
   [:button {:on-click #(send! [:waifoo/remove-todo id])} "remove"]
   [:button {:on-click #(send! [:waifoo/toggle-todo id])} "toggle"]
   [:span {:style {:text-decoration (if active? :none :line-through)}} id ". " description]])

(defn view []
  [:div
   [view-new-todo-form]
   (if (nil? @todos)
     [:div "Loading..."]
     [:ul (for [todo @todos] ^{:key (:todo/id todo)} [view-todo todo])])])

(defservice ws-router
  (sente/start-client-chsk-router!
    (:ch-recv socket)
    handler))

(let [root (js/document.getElementById "root")]
  (reagent/render [view] root))
  
