(ns service-platform.models.order
  (:require [service-platform.db.core :refer [*db*]]
            [datomic.api :as d])
  (:import java.util.UUID))


(defn get-order-by-id
  [id]
  (d/pull (d/db *db*) '[*] [:order/id id]))

(defn create-order
  [order]
  (let [id (UUID/randomUUID)]
    @(d/transact *db* [(assoc order :order/id id)])
    (get-order-by-id id)))

(defn update-order
  [{:keys [:order/id] :as order}]
  (if-let [old-order (get-order-by-id id)]
    (do
      @(d/transact *db* [(merge old-order order)])
      (get-order-by-id id))
    (throw (ex-info "Order does not exist" order))))

(defn get-orders
  []
  (->> (d/q '[:find (pull ?id [*])
              :in $
              :where
              [?id :order/id]] (d/db *db*))
       (mapv first)))

(defn delete-order
  [id]
  (let [id    (UUID/fromString id)
        order (get-order-by-id id)]
    (if order
      ;;(swap! db dissoc id)
      (throw (Exception. (format "Order with %s id does not exist" id))))))

(update-keys
  {:user/id "foo" :accont/id "bar"}
  (comp keyword name))



(comment
  (def id (:order/id (create-order {:order/id             #uuid "7f6851d9-21c2-479d-b85a-23ea09e1aaff"
                                    :order/title          "Repeair the iphone"
                                    :order/description    "Change the battery"
                                    :order/applicant      "Steve Jobs"
                                    :order/executor       "Steve Wozniak"
                                    :order/execution-date #inst "1999-01-01T00:00:00.000-00:00"})))
  (get-order-by-id id)
  (get-order-by-id #uuid "98734df0-28ac-4ec2-b894-b5f5be870c61")

  (delete-order 4)
  (get-orders)
  (update-order {:id             2
                 :title          "Some a great task"
                 :description    "The big deal2"
                 :applicant      "God"
                 :executor       "X"
                 :execution-date "27-03-2024"})

  )
