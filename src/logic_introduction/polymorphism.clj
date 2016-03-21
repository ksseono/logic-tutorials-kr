(ns logic-introduction.polymorphism
  (:refer-clojure :exclude [==])
  (:use [logic-introduction.facts])
  (:use [clojure.core.logic]))

(defn env-assoc 
  "env is an environment such that the expression key is
  associated with the type value"
  [exp env type]
  (matche [env]
          ([[[exp :- type] . _]])
          ([[_ . ?rest]] (env-assoc exp ?rest type))))

(defn same-or-subtype 
  "parent is a simple type such that type child-or-same is a subtype
  or the same as parent"
  [parent child-or-same]
  (conde
    ((== parent child-or-same))
    ((derived-ancestor parent child-or-same))))

(defn polymorphic-type 
  "parent is a (simple or compound) type such that (simple or compound) type child 
  is equal or subtype of parent"
  [parent child-or-same]
  (matche [parent child-or-same]
          ([parent child-or-same]
           (same-or-subtype parent child-or-same))
          ([[?p :> . [?ps . ()]]
            [?c :> . [?cs . ()]]]
           (same-or-subtype ?p ?c)
           (polymorphic-type ?ps ?cs))))

(defn expression-check 
  "context is an environment such that expression exp executed in
  environment context results in type result-type"
  [context exp result-type]
  (conde
    ((fresh [poly-res-type]
            (polymorphic-type result-type poly-res-type)
            (env-assoc exp context poly-res-type)))
    ((matche [exp]
             ([[:apply ?fun ?arg]]
              (fresh [arg-type fun-type]
                     (!= ?fun ?arg)
                     (expression-check context ?arg arg-type)
                     (expression-check context ?fun [arg-type :> result-type])))))))
