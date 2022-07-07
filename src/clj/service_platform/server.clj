(ns service-platform.server
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [compojure.core :refer [defroutes DELETE GET POST PUT]]
   [ring.adapter.jetty :refer [run-jetty]]
   [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
   [ring.util.anti-forgery :refer [anti-forgery-field]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
   [ring.middleware.resource :refer [wrap-resource]]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [ring.util.response :refer [bad-request response]]
   [mount.core :refer [defstate]]
   [service-platform.config :refer [config]]
   [service-platform.models.order :as order]
   [service-platform.validation :refer [validate-order]]
   [clojure.string :as str]
   [ring.middleware.format-params :refer [wrap-transit-json-params]]
   [ring.middleware.format-response :refer [wrap-transit-json-response]]
   [cognitect.transit :as t]))


(defn main-page
  [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (-> "public/index.html"
                io/resource
                slurp
                (str/replace #"\{\{csrf-field\}\}" (anti-forgery-field)))})

(defn create-order
  [{:keys [params]}]
  (let [[err valids-params] (validate-order params)]
    (if err
      (bad-request {:errors err})
      (let [result (order/create-order valids-params)]
        (response {:success true :result result})))))


(defn get-orders
  []
  (response (order/get-orders)))


(defn update-order
  [{:keys [params]}]
  (let [[err valids-params] (validate-order params)]
    (if err
      (bad-request {:errors err})
      (try
        (let [result (order/update-order valids-params)]
          (response {:success true :result result}))
        (catch Throwable e
          (bad-request {:error (ex-message e)}))))))

(defn delete-order
  [{{id :id} :params}]
  (throw (Exception. "Some server error"))
  (order/delete-order id)
  (response {:success true}))


(defn wrap-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Throwable e
        (bad-request {:error (.getMessage e)})))))

(defn page-404
  [_]
  {:status 404
   :body   "page not found"})

(defroutes app
  (GET "/" req (main-page req))
  (GET "/api/orders" _ (get-orders))
  (POST "/api/order" req (create-order req))
  (PUT "/api/order" req (update-order req))
  (DELETE "/api/order" req (delete-order req))
  page-404)

(defn wrap-print-req-resp [handler]
  (fn [req]
    (log/info "req: " (dissoc req :body))
    (let [resp (handler req)]
      (log/info "resp: " (dissoc resp :body))
      resp)))

(defn wrap-csrf [handler]
  (wrap-anti-forgery
    handler
    {:error-response
     {:status  403
      :headers {"Content-Type" "application/transit+json"}
      :body    (let [out (java.io.ByteArrayOutputStream.)]
                 (t/write (t/writer out :json) {:error "Missing or Invalid anti-forgery token. Just reload page!"})
                 (.toByteArray out))}}))

(def handler
  (-> #'app
      wrap-transit-json-response
      wrap-exception
      wrap-print-req-resp
      (wrap-resource "public/")
      wrap-csrf
      wrap-transit-json-params
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in  [:session :store] (cookie-store))))
      ))

(defstate ^{:on-reload :noop} server
:start
(let [{serv-opt :server} config]
  (run-jetty #'handler serv-opt))
:stop
(.stop server))
