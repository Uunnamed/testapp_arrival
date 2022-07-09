(ns service-platform.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [reagent.dom :as dom]
            [day8.re-frame.http-fx]
            [service-platform.ajax :refer [as-transit load-interceptors!]]
            [service-platform.validation :refer [validate-order]]))
;; helpers
(defn vec-delete [v i]
  (into (subvec v 0 i) (subvec v (inc i))))

(defn date->str
  [date]
  (->> (js/Date. date) (.toISOString) (take 10) (apply str)))

;; (rf/reg-event-fx
;;   :app/initialize
;;   (fn [_ _] {:db {:some true}}))

(rf/reg-sub
  :route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :page
  :<- [:route]
  (fn [route _]
    (-> route :data :view)))

;; modal

(rf/reg-event-db
  :app/show-modal
  (fn [db [_ modal-id]]
    (assoc-in db [:app/active-modals modal-id] true)))

(rf/reg-event-db
  :app/hide-modal
  (fn [db [_ modal-id]]
    (-> db
        (update :app/active-modals dissoc modal-id)
        (assoc :server-errors nil))))

(rf/reg-event-db
  :app/modal-title
  (fn [db [_ title]]
    (assoc db :app/modal-title title)))

(rf/reg-sub
  :app/active-modals
  (fn [db _]
    (:app/active-modals db {})))

(rf/reg-sub
  :app/modal-showing?
  :<- [:app/active-modals]
  (fn [modals [_ modal-id]]
    (get modals modal-id false)))


;; alert
(rf/reg-sub
  :app/alert-error
  (fn [db _]
    (-> db :server-errors :error)))

(rf/reg-event-db
  :app/alert-close
  (fn [db _]
    (assoc-in db [:server-errors :error] nil)))


;; dispatchers

(rf/reg-event-db
  :change-active-order
  (fn [db [_ id value]]
    (assoc-in db [:form/active-order id] value)))

(rf/reg-event-db
  :set-active-order
  (fn [db [_ idx]]
    (if-let [order (get-in db [:orders idx])]
      (-> db
          (assoc :form/active-order order)
          (assoc-in  [:form/active-order :idx] idx)
          (update-in [:form/active-order :order/execution-date] date->str))
      (assoc db :form/active-order {:order/execution-date (date->str (.now js/Date))}))))

(rf/reg-event-db
  :unset-active-order
  (fn [db [_ _]]
    (assoc db :form/active-order nil)))

(rf/reg-event-db
  :set-orders
  (fn [db [_ orders]]
    (assoc db :orders orders)))


(rf/reg-event-db
  :db/create-order
  (fn [db [_ resp]]
    (let [order (:result resp)]
      (update db :orders conj order))))

(rf/reg-event-db
  :db/update-order
  (fn [db [_ idx resp]]
    (let [order (:result resp)]
      (update-in db [:orders idx] merge order))))

(rf/reg-event-db
  :db/delete-order
  (fn [db [_ idx]]
    (assoc db :orders (vec-delete (:orders db) idx))))

(rf/reg-event-fx
  :api/create-order
  (fn [_ [_ order]]
    {:http-xhrio (as-transit {:method     :post
                              :params     order
                              :uri        "/testapp/api/order"
                              :on-success [:db/create-order]
                              :on-failure [:set-server-errors]})}))
(rf/reg-event-fx
  :api/update-order
  (fn [_ [_ order]]
    {:http-xhrio (as-transit {:method     :put
                              :params     (dissoc order :idx)
                              :uri        "/testapp/api/order"
                              :on-success [:db/update-order (:idx order)]
                              :on-failure [:set-server-errors]})}))
(rf/reg-event-fx
  :api/delete-order
  (fn [_ [_ order]]
    {:http-xhrio (as-transit {:method     :delete
                              :uri        "/testapp/api/order"
                              :params     order
                              :on-success [:db/delete-order (:idx order)]
                              :on-failure [:set-server-errors]})}))

(rf/reg-event-db
  :set-server-errors
  [(rf/path :server-errors)]
  (fn [_ [_ resp]]
    (-> resp :response)))

(rf/reg-event-db
  :clear-errors
  (fn [db _]
    (assoc db :server-errors nil)))

(rf/reg-event-fx
  :fetch-orders
  (fn [_ _]
    {:http-xhrio (as-transit {:method     :get
                              :uri        "/testapp/api/orders"
                              :on-success [:set-orders]
                              :on-failure [:set-server-errors]})}))

;;subscriptions

(rf/reg-sub
  :server-errors
  (fn [db _]
    (:server-errors db)))

(rf/reg-sub
  :validation-errors
  :<- [:form/active-order]
  (fn [order _]
    (when order
      (-> order
          validate-order
          first))))

(rf/reg-sub
  :validation-errors?
  :<- [:validation-errors]
  (fn [errors _]
    (seq errors)))

(rf/reg-sub
  :validation-error
  :<- [:validation-errors]
  (fn [errors [_ id]]
    (get errors id)))

(rf/reg-sub
  :orders
  (fn [db _]
    (:orders db)))


(rf/reg-sub
  :form/active-order
  (fn [db _]
    (:form/active-order db)))

(rf/reg-sub
  :order
  ;; :<- [:orders]
  (fn [{:keys [orders]} [_ idx]]
    (get orders idx)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(defn errors-component [id]
  (when-let [error @(rf/subscribe [:validation-error id])]
    [:div.notification.is-danger error]))


(defn nav-link [uri title page]
  [:a.navbar-item
   {:href  uri
    :class (when (= page @(rf/subscribe [:page])) :is-active)
    }
   title])

(defn alert [error]
  [:div    {:class (when-not @(rf/subscribe [:app/alert-error]) "is-hidden")}
   [:div.column.is-center
    [:div.notification.is-danger
     [:button.delete
      {:on-click #(rf/dispatch [:app/alert-close])}]
     error
     ]]])

(defn navbar []
  (r/with-let [expanded? (r/atom false)]
    [:nav.navbar.is-info>div.container
     [:div.navbar-brand
      [:a.navbar-item {:href "/" :style {:font-weight :bold}} "Service Platform"]
      [:span.navbar-burger.burger
       {:data-target :nav-menu
        :on-click    #(swap! expanded? not)
        :class       (when @expanded? :is-active)}
       [:span][:span][:span]]]
     [:div#nav-menu.navbar-menu
      {:class (when @expanded? :is-active)}
      [:div.navbar-start
       [:div.navbar-item.has-dropdown.is-hoverable
        [:div.navbar-link "Stuff"]
        [:div.navbar-dropdown
         [nav-link "#/orders" "Orders" :orders]]]]]]))

(defn modal-card
  [id title body footer]
  [:div.modal
   {:class (when @(rf/subscribe [:app/modal-showing? id]) "is-active")}
   [:div.modal-background
    {:on-click #(rf/dispatch [:app/hide-modal id])}]
   [:div.modal-card
    [:header.modal-card-head
     [:p.modal-card-title title]
     [:button.delete
      {:on-click #(rf/dispatch [:app/hide-modal id])}]]
    [:section.modal-card-body
     body]
    [:footer.modal-card-foot
     footer]]])

(defn order-form
  [order]
  (let [{:keys [order/title
                order/description
                order/execution-date
                order/applicant
                order/executor]} @order]
    [:div
     [errors-component :message]
     [:div.field
      [:div.label "Title"]
      [errors-component :order/title]
      [:div.control
       [:input.input
        {:type      "text"
         :value     title
         :on-change #(rf/dispatch [:change-active-order :order/title (.. % -target -value)])}]]]
     [:div.field
      [:div.label "Description"]
      [errors-component :order/description]
      [:div.control
       [:input.input
        {:type      "text"
         :value     description
         :on-change #(rf/dispatch [:change-active-order :order/description (.. % -target -value)])}]]]
     [:div.field
      [:div.label "Execution date"]
      [errors-component :order/execution-date]
      [:div.control
       [:input.input
        {:type      "date"
         :value     (date->str execution-date)
         :on-change #(rf/dispatch [:change-active-order :order/execution-date (.. % -target -value)])}]]]
     [:div.field
      [:div.label "Applicant"]
      [errors-component :order/applicant]
      [:div.control
       [:input.input
        {:type      "text"
         :value     applicant
         :on-change #(rf/dispatch [:change-active-order :order/applicant (.. % -target -value)])}]]]
     [:div.field
      [:div.label "Executor"]
      [errors-component :order/executor]
      [:div.control
       [:input.input
        {:type      "text"
         :value     executor
         :on-change #(rf/dispatch [:change-active-order :order/executor (.. % -target -value)])}]]]
     ]))

(defn add-order-btn []
  (r/with-let [order (rf/subscribe [:form/active-order])]
    [:div
     [:button.button.is-primary.fa.fa-edit.is-small
      {:on-click #(mapv rf/dispatch [[:app/show-modal :order/add]
                                     [:set-active-order]])}
      "Add order"]
     [modal-card :order/add
      ;; Title
      "Add order"
      ;; Body
      (when @(rf/subscribe [:app/modal-showing? :order/add])
        (order-form order))
      ;; Footer
      [:button.button.is-primary.is-fullwidth
       {:on-click #(when-not @(rf/subscribe [:validation-errors?])
                     (rf/dispatch [:app/hide-modal :order/add])
                     (rf/dispatch [:api/create-order @order]))}
       "Add"]]]))

(defn edit-order-btn [idx]
  [:button.button.is-primary.fa.fa-edit.is-small
   {:on-click #(mapv rf/dispatch [[:set-active-order idx]
                                  [:app/show-modal :order/edit]])}])
(defn edit-modal []
  (r/with-let [order (rf/subscribe [:form/active-order])]
    [modal-card :order/edit
     ;; Title
     "Edit order"
     ;; Body
     (when @(rf/subscribe [:app/modal-showing? :order/edit])
       (order-form order))
     ;; Footer
     [:button.button.is-primary.is-fullwidth
      {:on-click #(do
                    (println @(rf/subscribe [:validation-errors?]))
                    (when-not @(rf/subscribe [:validation-errors?])
                      (rf/dispatch [:app/hide-modal :order/edit])
                      (rf/dispatch [:api/update-order @order])))}
      "Edit"]]))

(defn delete-order-btn [idx]
  [:div
   [:button.button.is-danger.fa.fa-trash-alt.is-small
    {:on-click #(do
                  (rf/dispatch [:app/show-modal :order/delete])
                  (rf/dispatch [:set-active-order idx]))}]])

(defn delete-modal []
  (r/with-let [order (rf/subscribe [:form/active-order])]
    [modal-card :order/delete "Delete"
     ;;Body
     [:div
      [:div.content.is-large.has-text-centered "Do you want to delete the order?"]
      [:dl
       [:dt.is-centered "Title: " (:order/title @order)]
       [:dt.is-centered "Description: " (:order/description @order)]
       [:dt.is-centered "Execution-date: " (:order/execution-date @order)]
       [:dt.is-centered "Applicant: " (:order/applicant @order)]
       [:dt.is-centered "Executor: " (:order/executor @order)]]]
     ;; Footer
     [:div.columns.is-centered
      [:button.button.is-primary.is-danger
       {:on-click #(mapv rf/dispatch [[:api/delete-order @order] [:app/hide-modal :order/delete]])}
       "Yes"]
      [:button.button.is-primary.is-fullwidth
       {:on-click #(rf/dispatch [:app/hide-modal :order/delete])}
       "No"]]]))



(defn table-orders [orders]
  [:div.table-container
   (add-order-btn)
   [:table.is-bordered
    [:thead
     [:tr
      [:th.has-text-centered "#"]
      [:th.has-text-centered "Title"]
      [:th.has-text-centered "Description"]
      [:th.has-text-centered "Applicant"]
      [:th.has-text-centered "Executor"]
      [:th.has-text-centered "Execution date"]
      [:th.has-text-centered "Action"]]
     ]
    [:tbody
     (doall (map-indexed
              (fn [idx {:keys [order/title
                               order/description
                               order/execution-date
                               order/applicant
                               order/executor]}]
                ^{:key (str idx)}
                [:tr
                 [:td.has-text-centered (inc idx)]
                 [:td.has-text-centered title]
                 [:td.has-text-centered description]
                 [:td.has-text-centered applicant]
                 [:td.has-text-centered executor]
                 [:td.has-text-centered (date->str execution-date)]
                 [:td
                  [:div.columns.is-centered
                   [:div.column.is-one-fifth (edit-order-btn idx)]
                   [:div.column.is-one-fifth (delete-order-btn idx)]]]])
              orders))]]])



(defn app []
  [:div
   (navbar)
   (let [orders @(rf/subscribe [:orders])]
     [:section.section>div.container>div.content
      (when-let [error @(rf/subscribe [:app/alert-error])]
        (alert error))
      (table-orders orders)
      (edit-modal)
      (delete-modal)])])

(defn ^:dev/after-load mount-components []
  (rf/clear-subscription-cache!)
  (.log js/console "Mounting Components...")
  (dom/render [#'app] (.getElementById js/document "app"))
  (.log js/console "Components Mounted!"))

(defn init []
  (.log js/console "Initializing App...")
  (load-interceptors!)
  (rf/dispatch [:fetch-orders])
  (mount-components))
