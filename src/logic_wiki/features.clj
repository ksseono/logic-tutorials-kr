(ns logic-wiki.features
  (:refer-clojure :exclude [==])
  (:require [clojure.core.logic :refer :all]
            [clojure.core.logic.pldb :as pldb]
            [clojure.core.logic.unifier :as u]))

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

  (u/unifier ['(?x ?y ?z) '(1 2 ?y)])
  ;;=> {?y 2, ?x 1, ?z 2}

  (u/unify ['(?x ?y ?z) '(1 2 ?y)])
  ;;=> (1 2 2)

  (run* [?x ?y ?z]
    (== [?x ?y ?z] [1 2 ?y]))
  ;;=> ([1 2 2])
  )


;;
;; CLP (Tree)
;;
(comment
  (run* [q]
    (!= q 1))
  ;;=> ((_0 :- (!= (_0 1))))

  (run* [q]
    (fresh [x y]
      (!= [1 x] [y 2])
      (== q [x y])))
  ;;=> (([_0 _1] :- (!= (_1 1) (_0 2))))
  )
