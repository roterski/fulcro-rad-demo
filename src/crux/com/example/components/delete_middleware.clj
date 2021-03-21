(ns com.example.components.delete-middleware
  (:require
   [com.fulcrologic.rad.database-adapters.crux :as crux]))

(def middleware (crux/wrap-crux-delete))
