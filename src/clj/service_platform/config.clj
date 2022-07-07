(ns service-platform.config
  (:require
   [mount.core :refer [defstate]]
   [cprop.core :refer [load-config]]
   [cprop.source :as source]))

(defstate config
  :start
  (load-config
    :merge
    [(source/from-system-props)
     (source/from-env)]))
