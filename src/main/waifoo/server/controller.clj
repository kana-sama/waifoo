(ns waifoo.server.controller
  (:require [mount.core :refer [defstate]]
            [waifoo.util.logging :refer [warn]]
            [waifoo.server.store :as store]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer [get-sch-adapter]]))

(defstate socket :start
  (sente/make-channel-socket!
    (get-sch-adapter)
    {:csrf-token-fn nil}))

(defn send! [uid event]
  ((:send-fn socket) uid event))

(defn broadcast! [event]
  (doseq [uid (-> socket :connected-uids deref :any)]
    (send! uid event)))

(add-watch store/state :store-watcher
  (fn [_ _ _ new-state]
    (broadcast! [:waifoo/update-state new-state])))

(defmulti handler :id)

(defmethod handler :default [{event :event}]
  (warn "Unhandled" event))

(defmethod handler :waifoo/init [{uid :uid}]
  (send! uid [:waifoo/update-state @store/state]))

(defmethod handler :users/create [{[_ name] :event}]
  (store/create-user! :name name))

(defmethod handler :users/remove [{[_ id] :event}]
  (store/delete-user! id))

(defn start-controller []
  (sente/start-server-chsk-router!
    (:ch-recv socket) handler))

(defstate controller
  :start (start-controller)
  :stop (controller))
