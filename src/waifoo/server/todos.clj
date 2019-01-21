(ns waifoo.server.todos)

(defonce todos
  (atom {}))

(defn new-id []
  (rand-int 1000000))

(defn all-as-map []
  @todos)

(defn insert! [todo]
  (swap! todos #(assoc % (:todo/id todo) todo)))

(defn create! [& {:keys [description active?] :or {description "", active? true}}]
  (let [todo #:todo{:id (new-id), :description description, :active? active?}]
    (insert! todo)
    todo))

(defn remove! [id]
  (swap! todos #(dissoc % id)))

(defn toggle! [id]
  (swap! todos #(update-in % [id :todo/active?] not))
  (get @todos id))
