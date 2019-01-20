(ns waifoo.server
  (:require [waifoo.config :as config]
            [waifoo.util.defservice :refer [defservice]]
            [clojure.core.match :refer [match]]
            [org.httpkit.server :as http-kit]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]
            [taoensso.timbre :refer [color-str info warn]]
            [compojure.core :as compojure]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [datomic.client.api :as datomic]))

(def socket
  (sente/make-channel-socket! (get-sch-adapter) {:csrf-token-fn nil}))

(defn send! [uid event]
  ((:send-fn socket) uid event))

(defn broadcast! [event]
  (doseq [uid (-> socket :connected-uids deref :any)]
    (send! uid event)))

; (def database-client
;   (datomic/client
;     {:server-type :peer-server
;      :access-key config/database-access-key
;      :secret config/database-secret
;      :endpoint config/database-endpoint}))

; (def database-connection
;   (datomic/connect
;     database-client
;     {:db-name config/database-name}))

; (def todo-schema
;   [{:db/ident :counter/description
;     :db/valueType :db.type/string
;     :db/cardinality :db.cardinality/one}
;    {:db/ident :counter/done?
;     :db/valueType :db.type/boolean
;     :db/cardinality :db.cardinality/one}])

; (datomic/transact database-connection
;   {:tx-data initial-counter-state})

; (datomic/q
;   '[:find ?e ?v :where [?e :counter/value ?v]]
;   (datomic/db database-connection))

; (datomic/transact database-connection
;   {:tx-data [[:db/cas 17592186045445 :counter/value + 1]]})

(defonce todos
  (atom []))

(defn new-id []
  (if (empty? @todos)
    0
    (->> @todos (map :todo/id) (apply max) inc)))

(defn add-todo! [description]
  (let [todo #:todo{:id (new-id), :description description, :active? true}]
    (swap! todos #(cons todo %))
    (broadcast! [:waifoo/new-todo todo])))

(defn remove-todo! [id]
  (swap! todos #(filter (fn [todo] (not= id (:todo/id todo))) %))
  (broadcast! [:waifoo/remove-todo id]))

(defn toggle-todo! [id]
  (swap! todos
    #(map (fn [todo]
            (if (== (:todo/id todo) id)
              (update todo :todo/active? not)
              todo)) %))
  (broadcast! [:waifoo/toggle-todo id]))

(defn handler [{:keys [id uid event]}]
  (match event
    [:waifoo/init] (send! uid [:waifoo/set-todos @todos])
    [:waifoo/new-todo description] (add-todo! description)
    [:waifoo/remove-todo todo-id] (remove-todo! todo-id)
    [:waifoo/toggle-todo todo-id] (toggle-todo! todo-id)
    :else (warn (color-str :yellow "Unhandled " event))))

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
