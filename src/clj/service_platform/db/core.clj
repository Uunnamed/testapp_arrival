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
