(ns logic-wiki.features
  (:require [clojure.core.logic :as logic]
            [clojure.core.logic.pldb :as pldb]
            [clojure.core.logic.unifier :refer :all]))

(pldb/db-rel man p)
(pldb/db-rel woman p)
(pldb/db-rel likes p1 p2)
(pldb/db-rel fun p)

(def facts0
  (pldb/db
   [man 'Bob]
   [man 'John]
   [man 'Ricky]

   [woman 'Mary]
   [woman 'Martha]
   [woman 'Lucy]

   [likes 'Bob 'Mary]
   [likes 'John 'Martha]
   [likes 'Richy 'Lucy]))

(def facts1 (-> facts0 (pldb/db-fact fun 'Lucy)))

(comment
  (pldb/with-db facts1
    (run* [q]
      (fresh [x y]
        (fun y)
        (likes x y)
        (== q [x y]))))
  ;;=> ([Richy Lucy])

  (pldb/db-rel likes ^:index p1 ^:index p2)

  (unifier ['(?x ?y ?z) '(1 2 ?y)])
  ;;=> {?y 2, ?x 1, ?z 2}

  (unify ['(?x ?y ?z) '(1 2 ?y)])
  ;;=> (1 2 2)

  (logic/run* [?x ?y ?z]
    (logic/== [?x ?y ?z] [1 2 ?y]))
  ;;=> ([1 2 2])
  )
