(ns service-platform.validation
  (:require [struct.core :as st]
            [tick.core :as t]
            [tick.locale-en-us]))

(defn str->date
  [date]
  (try
    (-> (t/parse-date (subs date 0 10) (t/formatter "yyyy-MM-dd"))
        t/beginning
        t/inst)
    (catch #?(:cljs :default :clj Throwable) e
      (println e)
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

(comment
  (validate-order {:id             1
                   :title          "Ivanov Ivan Ivanovich"
                   :description    "man"
                   :execution-date "1999-12-01"
                   :applicant      "123 avenue"
                   :executor       "123"})
  (t/instant "1999-12-31")

  )
