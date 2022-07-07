(ns service-platform.core
  (:require service-platform.config
            service-platform.server
            service-platform.db.core
            [mount.core :as mount])
  (:gen-class))



(defn start []
  (mount/start))


(defn stop []
  (mount/stop))

(defn -main [& args]
  (start))
