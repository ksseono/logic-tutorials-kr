core.logic 특징
==============================================

예제 소스코드: src/main/clojure/logic_wiki/features.clj

단순한 인메모리 데이터베이스
-----------------------------------------------

때로는 질의 대상이 되는 사실(fact)들을 리스트로 생성하는 것이 유용할 수 있다. 이 때 ``defrel`` 과 ``fact`` 를 사용한다.

.. code-block:: clojure

	(ns logic-wiki.features
	  (:use [clojure.core.logic])
	  (:require [clojure.core.logic.pldb :as pldb]))

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

	(pldb/with-db facts1
	  (run* [q]
	    (fresh [x y]
	      (fun y)
	      (likes x y)
	      (== q [x y]))))
	;;=> ([Richy Lucy])

여기서 중요한 것은 사실(fact)의 갯수가 늘어남에 따라 질의 처리 시간이 함께 증가하지 않도록 관계(relationship)들을 인덱싱하는 것이다. 사실(fact) 튜플의 요소들을 위한 인덱스들을 생성해둘 수 있는데, 메모리를 소비하게 된다는 점은 주의해야 한다.

.. code-block:: clojure

    (pldb/db-rel likes ^:index p1 ^:index p2)

개별적 단일화(unification)
-----------------------------------------------
core.logic를 다룰 때는 core.unify와 상당히 유사하게 사용되는 단일화(unification)가 수반된다. 

.. code-block:: clojure

    (unifier ['(?x ?y ?z) '(1 2 ?y)])
    ;;=> {?y 2, ?x 1, ?z 2}

    (logic/run* [?x ?y ?z]
    	(logic/== [?x ?y ?z] [1 2 ?y]))
    ;;=> ([1 2 2])


  


