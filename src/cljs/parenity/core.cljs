(ns parenity.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [clojure.pprint :as pprint]
              [clojure.core.reducers :as reducers]))

(def exprs
  (atom
    '(label equal (lambda (x y) (cond))
        ((atom x) (cond ((atom y) (eq x y)) ((quote t) (quote f))))
        ((equal (car x) (car y)) (equal (cdr x) (cdr y)))
        ((quote t)(quote f)))))

;; -------------------------
;; Views

(defn atm [i text]
  [:button.ui.button.basic.huge text])


(defn s-expr [i children]
 [:div
  (cond
   (list? children)
   [:div.s-expr
    [:span "("]
    [:div.ui.basic.buttons
      (map-indexed s-expr children)]
    [:span ")"]]
   :else [atm i children])])


(defn editor [text]
   [:pre text])


(defn add-pluses [expr]
  (if (list? expr)
    (concat (map add-pluses expr) '(+))
    expr))


(defn home-page []
  [editor (pprint/write (add-pluses @exprs) :stream nil)])

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
