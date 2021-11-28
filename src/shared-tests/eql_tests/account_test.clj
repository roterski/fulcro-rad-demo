(ns eql-tests.account-test
  (:require
   [clojure.test :refer [use-fixtures]]
   [fulcro-spec.core :refer [specification assertions behavior =>]]
   [clojure.set :refer [map-invert]]
   [mount.core :as mount]
   [com.example.components.parser :refer [parser]]
   [com.fulcrologic.fulcro.algorithms.tempid :as ti]))

(defn start-test-env []
  (mount/start-with-args {:config "config/test.edn"}))

(defn with-env [tests]
  (start-test-env)
  (tests)
  (mount/stop))

(use-fixtures :each with-env)

(specification
 "Account"
 (let [name->tempid {:account (ti/tempid)
                     :address-1 (ti/tempid)
                     :address-2 (ti/tempid)
                     :address-3 (ti/tempid)}
       request `[{(com.fulcrologic.rad.form/save-form
                   {:com.fulcrologic.rad.form/id ~(:account name->tempid)
                    :com.fulcrologic.rad.form/delta
                    {[:account/id ~(:account name->tempid)]
                     {:account/name {:before nil, :after "Joe Doe"}
                      :account/role {:before nil, :after nil}
                      :account/email {:before nil, :after "JOE@example.com"}
                      :account/active? {:before true, :after true}
                      :account/primary-address
                      {:before nil
                       :after
                       [:address/id ~(:address-1 name->tempid)]}
                      :account/addresses
                      {:before []
                       :after
                       [[:address/id ~(:address-2 name->tempid)]
                        [:address/id ~(:address-3 name->tempid)]]}
                      :account/files {:before [], :after []}}
                     [:address/id ~(:address-1 name->tempid)]
                     {:address/street {:before nil, :after "Main St 1"}
                      :address/city {:before nil, :after "Sacramento"}
                      :address/state {:before nil, :after :address.state/CA}
                      :address/zip {:before nil, :after "99999"}}
                     [:address/id ~(:address-2 name->tempid)]
                     {:address/street {:before nil, :after "Dunder 5"}
                      :address/city {:before nil, :after "Scranton"}
                      :address/state {:before nil, :after :address.state/PA}
                      :address/zip {:before nil, :after "44444"}}
                     [:address/id ~(:address-3 name->tempid)]
                     {:address/street {:before nil, :after "222 Other"}
                      :address/city {:before nil, :after "Sacramento"}
                      :address/state {:before nil, :after :address.state/CA}
                      :address/zip {:before nil, :after "999955"}}}
                    :com.fulcrologic.rad.form/master-pk :account/id})
                  [:account/id
                   :account/name
                   :account/email
                   :account/active?
                   {:account/primary-address
                    [:address/id
                     :address/street
                     :address/city
                     :address/state
                     :address/zip]}
                   {:account/addresses
                    [:address/id
                     :address/street
                     :address/city
                     :address/state
                     :address/zip]}
                   :tempids ;; move to pathom3 equivalent of mutation-join-globals
                   :com.wsscode.pathom.core/errors]}]
       response (parser {:ring/request {}} request)
       tempids (get-in response ['com.fulcrologic.rad.form/save-form :tempids])
       name->uuid (->> (map-invert name->tempid)
                       (reduce (fn [acc [tempid name]]
                                 (assoc acc name (get tempids tempid)))
                               {}))]
   (behavior
    "lifecycle"
    (assertions
     "save-form request returns tempids map and query result"
     response => `{com.fulcrologic.rad.form/save-form
                   {:tempids
                    {~(:account name->tempid)   ~(:account name->uuid)
                     ~(:address-1 name->tempid) ~(:address-1 name->uuid)
                     ~(:address-2 name->tempid) ~(:address-2 name->uuid)
                     ~(:address-3 name->tempid) ~(:address-3 name->uuid)}
                    :account/id ~(:account name->uuid)
                    :account/active? true
                    :account/email "JOE@example.com"
                    :account/addresses
                    [{:address/id ~(:address-2 name->uuid)
                      :address/street "Dunder 5"
                      :address/city "Scranton"
                      :address/state :address.state/PA
                      :address/zip "44444"}
                     {:address/id ~(:address-3 name->uuid)
                      :address/street "222 Other"
                      :address/city "Sacramento"
                      :address/state :address.state/CA
                      :address/zip "999955"}]
                    :account/primary-address
                    {:address/id ~(:address-1 name->uuid)
                     :address/street "Main St 1"
                     :address/city "Sacramento"
                     :address/state :address.state/CA
                     :address/zip "99999"}
                    :account/name "Joe Doe"}}

     "account should be accessible in subsequent queries"
     (parser {:ring/request {}}
             '[({:account/all-accounts [:account/id :account/name :account/active?
                                        {:account/primary-address [:address/id
                                                                   :address/city]}]}
                {:show-inactive? false})
               :com.wsscode.pathom.core/errors]) => #:account{:all-accounts [#:account{:id (:account name->uuid)
                                                                                       :name "Joe Doe"
                                                                                       :active? true
                                                                                       :primary-address {:address/id (:address-1 name->uuid)
                                                                                                         :address/city "Sacramento"}}]}
     "then the account is deleted"
     (parser {:ring/request {}} `[{(com.fulcrologic.rad.form/delete-entity
                                    {:account/id ~(:account name->uuid)})
                                   [:com.wsscode.pathom.core/errors]}]) => '{com.fulcrologic.rad.form/delete-entity {}}

     "and the account should no longer be accessible"
     (parser {:ring/request {}} '[({:account/all-accounts [:account/id :account/name :account/active?]}
                                   {:show-inactive? false})
                                  :com.wsscode.pathom.core/errors]) => #:account{:all-accounts []}))))

(comment
  
  (clojure.test/run-tests)
)
