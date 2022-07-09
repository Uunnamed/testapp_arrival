(ns service-platform.models.order
  (:require [service-platform.db.core :refer [*db*]]
            [datomic.api :as d]))


(defn get-order-by-id
  [id]
  (d/pull (d/db *db*) '[*] [:order/id id]))

(defn create-order
  [order]
  (let [id (random-uuid)]
    @(d/transact *db* [(assoc order :order/id id :order/status :order.status/active)])
    (get-order-by-id id)))

(defn update-order
  [{:keys [:order/id] :as order}]
  (if-let [old-order (get-order-by-id id)]
    (do
      @(d/transact *db* [(merge old-order order)])
      (get-order-by-id id))
    (throw (Exception. (format "Order with id: %s does not exist" id)))))

(defn get-orders
  []
  (->> (d/q '[:find (pull ?o [* {:order/status [*]}])
              :in $
              :where
              [?o :order/id]
              [?o :order/status :order.status/active]] (d/db *db*))
       (mapv first)))

(defn delete-order
  [id]
  (let [order (get-order-by-id id)]
    (if (:order/id order)
      @(d/transact *db* [(assoc order :order/status :order.status/deleted)])
      (throw (Exception. (format "Order with id: %s does not exist" id))))))
