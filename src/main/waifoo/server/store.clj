(ns waifoo.server.store
  (:require [waifoo.util.core :refer [uuid! dissoc-in]]
            [inflections.core :refer [singular]]))

(def initial-state
  {:users (array-map)})

(defonce state
  (atom initial-state))

; (defn field [resource field]
;   (keyword (name resource) (name field)))

; (defn command [resource command]
;   (symbol (str (name command) "-" (name resource) "!")))

; (defmacro defresource
;   [resource &
;    [{:as config
;      :keys [path resource-singular]
;      :or {path [(keyword resource)]
;           resource-singular (singular (name resource))}}]]
;   `(do
;     (defn ~resource []
;       (~(keyword resource) state))
;     (defn ~(command resource :insert) [{~(field resource-singular :keys) [id] :as item}]
;       (swap! state assoc-in [~(keyword resource) id] item))))

(defn users []
  (:users @state))

(defn insert-user! [{:user/keys [id] :as user}]
  (swap! state assoc-in [:users id] user))

(defn create-user! [& {:keys [name]}]
  (let [user #:user{:id (uuid!), :name name}]
    (insert-user! user)))

(defn delete-user! [id]
  (swap! state dissoc-in [:users id]))
