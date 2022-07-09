(ns service-platform.models.order-test
  (:require [service-platform.models.order :as order]
            [clojure.test :as t]
            [mount.core :as mount]))

(def created-order
  #:order{:title          "Repeair the iphone"
          :description    "Change the battery"
          :applicant      "Steve Jobs"
          :executor       "Steve Wozniak"
          :execution-date #inst "2008-07-10T00:00:00.000-00:00"})

(def updated-order
  #:order{:title          "Sell a lot of iphones"
          :description    "To earn BBillions $"
          :applicant      "Steve Jobs"
          :executor       "Zombie Marceting"
          :execution-date #inst "2008-07-10T00:00:00.000-00:00"})

(t/use-fixtures
  :once
  (fn [f]
    (mount/start
      #'service-platform.config/config)
    (f)))

(t/use-fixtures
  :each
  (fn [f]
    (mount/start
      #'service-platform.db.core/*db*)
    (f)
    (mount/stop
      #'service-platform.db.core/*db*)))

(t/deftest create-order
  (t/is (= (dissoc (order/create-order created-order)
                   :db/id :order/status :order/id)
           created-order)))

(t/deftest update-order
  (let [new-order (order/create-order created-order)
        upd-order (order/update-order (merge new-order updated-order))
        upd-order (dissoc upd-order :db/id :order/id :order/status)]
    (t/is (= upd-order
             updated-order))))

(t/deftest delete-orders
  (doseq [order (repeat 3 created-order)]
    (order/create-order order))
  (t/is (= 3 (count (order/get-orders))))
  (doseq [ord (order/get-orders)]
    (order/delete-order (:order/id ord)))
  (t/is (= 0 (count (order/get-orders)))))
