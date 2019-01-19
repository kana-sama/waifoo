(ns waifoo.core
  (:require [waifoo.config :as config]
            [clojure.core.match :refer [match]]
            [org.httpkit.server :as http-kit]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [compojure.core :as compojure]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(def socket
  (sente/make-channel-socket! (get-sch-adapter) {:csrf-token-fn nil}))

(defn send! [uid event]
  ((:send-fn socket) uid event))

(defn broadcast! [event]
  (doseq [uid (-> socket :connected-uids deref :any)]
    (send! uid event)))

(defonce state
  (atom 0))

(defn handler [{:keys [id uid event]}]
  (println ">" event)
  (match event
    [:value/get] (send! uid [:value/set @state])
    [:value/dec] (broadcast! [:value/set (swap! state dec)])
    [:value/inc] (broadcast! [:value/set (swap! state inc)])
    :else (println "?" id)))

(compojure/defroutes routes
  (compojure/GET "/chsk" req ((:ajax-get-or-ws-handshake-fn socket) req))
  (compojure/POST "/chsk" req ((:ajax-post-fn socket) req)))

(-> routes
  (wrap-cors :access-control-allow-origin #".*", :access-control-allow-methods #{:get})
  (wrap-keyword-params)
  (wrap-params)
  (wrap-reload)
  (http-kit/run-server {:port config/port}))

(sente/start-server-chsk-router! (:ch-recv socket) handler)
