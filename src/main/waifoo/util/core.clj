(ns waifoo.util.core)

(defn uuid! []
  (str (java.util.UUID/randomUUID)))

(defn one? [x]
  (== x 1))

(defn dissoc-in [map keys]
  (if (one? (count keys))
    (dissoc map (first keys))
    (update-in map (butlast keys) dissoc (last keys))))

(defn dissoc-in
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (assoc m k newmap))
      m)
    (dissoc m k)))
