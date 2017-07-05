(ns parenity.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]))

(def exprs (atom '(a b c)))

;; -------------------------
;; Views

(defn atm [i text]
  [:button.ui.button.basic.huge text])


(defn s-expr [children]
  [:div.s-expr
   [:span "("]
   [:div.ui.buttons
     (map-indexed atm children)
     [:button.ui.huge.basic.circular.icon.button [:i.icon.plus]]]
   [:span ")"]])

(defn editor [exprs]
  [:div
   [:p (str exprs)]])



(defn home-page []
  [s-expr @exprs])

(defn about-page []
  [:div [:h2 "About parenity"]
   [:div [:a {:href "/"} "go to the home page"]]])

;; -------------------------
;; Routes

(def page (atom #'home-page))

(defn current-page []
  [:div [@page]])

(secretary/defroute "/" []
  (reset! page #'home-page))

(secretary/defroute "/about" []
  (reset! page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [path]
       (secretary/dispatch! path))
     :path-exists?
     (fn [path]
       (secretary/locate-route path))})
  (accountant/dispatch-current!)
  (mount-root))
