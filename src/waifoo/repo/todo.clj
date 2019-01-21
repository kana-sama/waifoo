(ns waifoo.repo.todo)

(defonce todos
  (atom {}))

(defn gen-id []
  (rand-int 1000000))

(defn get-all [] @todos)

(defn add! [description]
  (let [id (gen-id)
        todo #:todo{:id id, :description description, :active? true}]
    (swap! todos #(assoc % id todo))
    todo))

(defn remove! [id]
  (swap! todos #(dissoc % id)))

(defn toggle! [id]
  (do
    (swap! todos #(update-in % [id :todo/active?] not))
    (get @todos id)))
