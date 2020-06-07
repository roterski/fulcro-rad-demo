(ns com.example.typecheck
  (:require
    ["react-dom" :refer [render]]
    [com.example.sample]
    [com.fulcrologic.guardrails.static.checker :as checker]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.dom :as dom]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [clojure.string :as str]))

(defn start []
  (let [registered-functions (keys @checker/registry)
        results              (sort-by :symbol
                               (reduce
                                 (fn [acc f]
                                   (conj acc {:symbol f
                                              :errors (checker/check f)}))
                                 []
                                 registered-functions))]
    (render (dom/div
              (map-indexed
                (fn [i {:keys [symbol errors]}]
                  (comp/fragment {:key i}
                    (dom/h3 "Checked " (str symbol))
                    (dom/ul
                      (map-indexed
                        (fn [idx {:keys [message]}]
                          (dom/li {:key idx} (str message)))
                        errors))))
                results))
      (.getElementById js/document "app"))))
