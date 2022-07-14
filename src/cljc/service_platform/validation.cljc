(ns service-platform.validation
  (:require [struct.core :as st]
            [tick.core :as t]))

(defn str->date
  [date]
  (try
    (t/inst (str date "T00:00:00.001Z"))
    (catch #?(:cljs :default :clj Throwable) e
      false)))

(def order-schema
  [[:order/title
    st/required
    st/string]
   [:order/description
    st/required
    st/string]
   [:order/applicant
    st/required
    st/string]
   [:order/executor
    st/required
    st/string]
   [:order/execution-date
    st/required
    [st/string :coerce str->date]
    {:message  "execution date is not valid"
     :validate inst?}]])


(defn validate-order
  [params]
  (st/validate params order-schema))
