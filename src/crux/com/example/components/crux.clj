(ns com.example.components.crux
  (:require
   [com.fulcrologic.rad.database-adapters.crux :as crux]
   [com.fulcrologic.rad.database-adapters.crux-options :as co]
   [mount.core :refer [defstate]]
   [com.example.components.config :refer [config]]))

(defstate ^{:on-reload :noop} crux-nodes
  :start
  (crux/start-databases (crux/symbolize-crux-modules config)))
