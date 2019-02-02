(ns waifoo.server.core
  (:require [mount.core :refer [defstate]]
            [waifoo.config :as config]
            [waifoo.server.controller :refer [socket]]
            [org.httpkit.server :as http-kit]
            [compojure.core :as compojure]
            [compojure.route]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))

(compojure/defroutes routes
  (compojure.route/resources "/")
  (compojure/GET "/chsk" req ((:ajax-get-or-ws-handshake-fn socket) req))
  (compojure/POST "/chsk" req ((:ajax-post-fn socket) req)))

(defn start-server []
  (-> routes
    (wrap-cors :access-control-allow-origin #".*", :access-control-allow-methods #{:get})
    (wrap-keyword-params)
    (wrap-params)
    (http-kit/run-server {:port config/port})))

(defstate server
  :start (start-server)
  :stop (server))
