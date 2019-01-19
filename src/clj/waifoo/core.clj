(ns waifoo.core
  (:require [org.httpkit.server :as http-kit]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [compojure.core :as compojure]
            [compojure.route]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]))


(let [{:keys [ajax-post-fn ajax-get-or-ws-handshake-fn ch-recv send-fn connected-uids]}
      (sente/make-channel-socket! (get-sch-adapter) {:csrf-token-fn nil})]
  (def ring-ajax-post                ajax-post-fn) 
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn) 
  (def ch-chsk                       ch-recv)
  (def chsk-send!                    send-fn) 
  (def connected-uids                connected-uids))


(defonce value (atom 0))


(compojure/defroutes handler
  (compojure/GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (compojure/POST "/chsk" req (ring-ajax-post                req)))


(def web-handler
  (-> handler
    (wrap-cors :access-control-allow-origin #".*", :access-control-allow-methods #{:get})
    (wrap-keyword-params)
    (wrap-params)
    (wrap-reload)))


(defn broadcast! [event]
  (doseq [uid (:any @connected-uids)]
    (chsk-send! uid event)))


(defmulti ws-handler* :id)
(defmethod ws-handler* :default [message] (println "unhandled"))
(defmethod ws-handler* :value/get [{:as message :keys [uid]}]
  (chsk-send! uid [:value/set @value]))
(defmethod ws-handler* :value/dec [ev-msg]
  (broadcast! [:value/set (swap! value dec)]))
(defmethod ws-handler* :value/inc [ev-msg]
  (broadcast! [:value/set (swap! value inc)]))


(defn ws-handler [{:as message :keys [event]}]
  (println ">" event)
  (ws-handler* message))


(defonce web-server (atom nil))
(defn stop-web-server! []
  (when-not (nil? @web-server)
    (@web-server)
    (reset! web-server nil)))
(defn start-web-server! [] 
  (stop-web-server!)
  (reset! web-server (http-kit/run-server web-handler {:port 8081})))


(defonce ws-server (atom nil))
(defn stop-ws-server! []
  (when-not (nil? @ws-server) 
    (@ws-server)
    (reset! ws-server nil)))
(defn start-ws-server! []
  (stop-ws-server!)
  (reset! ws-server (sente/start-server-chsk-router! ch-chsk ws-handler)))


(defn stop! []
  (stop-ws-server!)
  (stop-web-server!))

(defn start! []
  (start-web-server!)
  (start-ws-server!))


(defn -main [& args]
  (start!))

(start!)