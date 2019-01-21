(ns waifoo.server.core
  (:require [waifoo.config :as config]
            [waifoo.util.defservice :refer [defservice]]
            [waifoo.util.logging :refer [warn]]
            [waifoo.server.todos :as todos]
            [org.httpkit.server :as http-kit]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [compojure.core :as compojure]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [datomic.client.api :as datomic]))

(def socket
  (sente/make-channel-socket!
    (get-sch-adapter)
    {:csrf-token-fn nil}))

(defn send! [uid event]
  ((:send-fn socket) uid event))

(defn broadcast! [event]
  (doseq [uid (-> socket :connected-uids deref :any)]
    (send! uid event)))

(defmulti handler :id)

(defmethod handler :default [{event :event}]
  (warn "Unhandled" event))

(defmethod handler :app/init [{uid :uid}]
  (send! uid [:todos/all (todos/all-as-map)]))

(defmethod handler :todos/create [{[_ description] :event}]
  (let [todo (todos/create! :description description)]
    (broadcast! [:todos/insert todo])))

(defmethod handler :todos/toggle [{[_ id] :event}]
  (let [todo (todos/toggle! id)]
    (broadcast! [:todos/insert todo])))

(defmethod handler :todos/remove [{[_ id] :event}]
  (todos/remove! id)
  (broadcast! [:todos/remove id]))

(compojure/defroutes routes
  (compojure/GET "/chsk" req ((:ajax-get-or-ws-handshake-fn socket) req))
  (compojure/POST "/chsk" req ((:ajax-post-fn socket) req)))

(defservice web-server
  (-> routes
    (wrap-cors :access-control-allow-origin #".*", :access-control-allow-methods #{:get})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-reload)
    (http-kit/run-server {:port config/port})))
 
(defservice ws-router
  (sente/start-server-chsk-router! (:ch-recv socket) handler))
