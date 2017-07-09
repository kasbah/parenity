(ns parenity.core
    (:require [reagent.core :as reagent :refer [atom]]
              [secretary.core :as secretary :include-macros true]
              [accountant.core :as accountant]
              [clojure.string :as string]
              [clojure.core.reducers :as reducers]
              [fipp.clojure :refer [pprint]]))

(def exprs
  (atom
    '(defn tokenize [text]
      (reverse
        (first
          (reduce
            (fn [accum c]
              (let [s (str c)
                    quoted (= (second accum) :quoted)
                    escaped (= (nth accum 2 nil) :escaped)
                    result (first accum)
                    prev (first result)
                    append-to-prev (conj (rest result) (string/join [prev s]))
                    create (conj result s)]
                (cond
                  (and quoted escaped) [append-to-prev :quoted nil]
                  quoted (case s
                           "\"" [append-to-prev nil]
                           "\\" [append-to-prev :quoted :escaped]
                           [append-to-prev :quoted])
                  :else
                    (case s
                      "\"" [create :quoted]
                      ("]" "[" "{" "}" ")" "(" " ") [create]
                      (case prev
                        ("]" "[" "{" "}" ")" "(" " " "\"")
                        [create]
                        [append-to-prev])))))
            ['() nil nil]
            text))))))

;; -------------------------
;; Views


(defn tile [c]
  (case c
    (" ") [:span.tile.space "_"]
    (")" "]" "}") [:span.tile.closing c]
    [:span.tile c]))

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

(defn tokenize [text]
  (reverse
    (first
      (reduce
        (fn [accum c]
          (let [s (str c)
                quoted (= (second accum) :quoted)
                escaped (= (nth accum 2 nil) :escaped)
                result (first accum)
                prev (first result)
                append-to-prev (conj (rest result) (string/join [prev s]))
                create (conj result s)]
            (cond
              (and quoted escaped) [append-to-prev :quoted nil]
              quoted (case s
                       "\"" [append-to-prev nil]
                       "\\" [append-to-prev :quoted :escaped]
                       [append-to-prev :quoted])
              :else
                (case s
                  "\"" [create :quoted]
                  ("]" "[" "{" "}" ")" "(" " ") [create]
                  (case prev
                    ("]" "[" "{" "}" ")" "(" " " "\"")
                    [create]
                    [append-to-prev])))))
        ['() nil nil]
        text))))

(defn line [text]
  [:div.line
    (map tile (tokenize text))])


(defn editor [text]
  (let [lines (string/split-lines text)]
    [:div (map line lines)]))

(defn add-pluses [expr]
  (if (list? expr)
    (concat (map add-pluses expr) '(+))
    expr))


(defn home-page []
  [editor (with-out-str (pprint @exprs {:width 80}))])

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
