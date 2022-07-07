(ns service-platform.db.core
  (:require [datomic.api :as d]
            [mount.core :refer [defstate]]
            [service-platform.config :refer [config]]
            [clojure.java.io :as io]))

(defn read-edn
  [filename]
  (-> filename
      io/resource
      slurp
      read-string))

(defn load-schema
  [conn filename]
  @(d/transact conn (read-edn filename)))


(defstate ^:dynamic *db*
  :start (let [uri           (-> config :db :db-uri)
               just-created? (d/create-database uri)
               conn          (d/connect uri)]
           (when just-created?
             (load-schema conn "schema/0001-init.edn"))
           conn)
  :stop (d/release *db*))

(comment
  (d/transact *db* [{:order/id             #uuid "8da00da7-16d1-4692-b81f-c507b05c4281"
                     :order/title          "Repeair the iphone"
                     :order/description    "Change the battery"
                     :order/applicant      "Steve Jobs"
                     :order/executor       "Steve Wozniak"
                     :order/execution-date #inst "1999-01-01T00:00:00.000-00:00"}
                    {:order/id             #uuid "8da00da7-16d1-4692-b81f-c507b05c4282"
                     :order/title          "Sell PayPal"
                     :order/description    "Sell Paypal to found Space X"
                     :order/applicant      "Elon Mask"
                     :order/executor       "EBay"
                     :order/execution-date #inst "1999-01-01T00:00:00.000-00:00"}
                    {:order/id             #uuid "8da00da7-16d1-4692-b81f-c507b05c4283"
                     :order/title          "Some a great task"
                     :order/description    "The big deal"
                     :order/applicant      "God"
                     :order/executor       "X"
                     :order/execution-date #inst "1999-01-01T00:00:00.000-00:00"}])

  (->> (d/q '[:find (pull ?id [*])
              :in $
              :where
              [?id :order/id]] (d/db *db*))
       (map first))

  (d/q '[:find ?e
         :where [_ :order/id ?e]] (d/db *db*))
  ;; => #{[#uuid "8da00da7-16d1-4692-b81f-c507b05c4281"]
  ;; [#uuid "8da00da7-16d1-4692-b81f-c507b05c4283"]
  ;; [#uuid "8da00da7-16d1-4692-b81f-c507b05c4282"]}
  )
