(ns waifoo.core
  (:require [compojure.core :as compojure]
            [ring.middleware.cors :refer [wrap-cors]]))

(def value
  (atom 0))

(compojure/defroutes handler
  (compojure/context "/value" []
    (compojure/GET "/get" [] (str @value))
    (compojure/GET "/inc" [] (str (swap! value inc)))
    (compojure/GET "/dec" [] (str (swap! value dec)))))


(def app
  (wrap-cors handler
    :access-control-allow-origin #".*"
    :access-control-allow-methods #{:get}))

; (def app
;   (-> handler
;     (wrap-cors :access-control-allow-origin #".*"
;                :access-control-allow-methods #{:get})))

(defn -main [& args]
  (println "Hello from BE"))
