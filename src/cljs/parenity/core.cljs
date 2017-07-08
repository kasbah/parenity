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
                    escaped (= (nth accum 2) :escaped)
                    prev (first (first accum))
                    join (cons (string/join [prev s]) (rest (first accum)))
                    create (cons s (first accum))]
                (cond
                  (and quoted escaped) [join :quoted :unescaped]
                  quoted (case s
                           ("\"") [join :unquoted :unescaped]
                           ("\\") [join :quoted :escaped]
                           [join :quoted :unescaped])
                  :else
                    (case s
                      ("\"") [create :quoted :unescaped]
                      ("]" "[" "{" "}" ")" "(" " ") [create :unquoted :unescaped]
                      (case prev
                        ("]" "[" "{" "}" ")" "(" " " "\"") [create :unquoted :unescaped]
                        [join :unquoted :unescaped])))))
            [[] :unquoted :unescaped]
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
                escaped (= (nth accum 2) :escaped)
                prev (first (first accum))
                join (cons (string/join [prev s]) (rest (first accum)))
                create (cons s (first accum))]
            (cond
              (and quoted escaped) [join :quoted :unescaped]
              quoted (case s
                       "\"" [join :unquoted :unescaped]
                       "\\" [join :quoted :escaped]
                       [join :quoted :unescaped])
              :else
                (case s
                  "\"" [create :quoted :unescaped]
                  ("]" "[" "{" "}" ")" "(" " ") [create :unquoted :unescaped]
                  (case prev
                    ("]" "[" "{" "}" ")" "(" " " "\"")
                    [create :unquoted :unescaped]
                    [join :unquoted :unescaped])))))
        [[] :unquoted :unescaped]
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
