### A classic AI program:

```clj
(ns classic-ai-example
   (:refer-clojure :exclude [==])
   (:use clojure.core.logic))

(defne moveo [before action after]
  ([[:middle :onbox :middle :hasnot]
    :grasp
    [:middle :onbox :middle :has]])
  ([[pos :onfloor pos has]
    :climb
    [pos :onbox pos has]])
  ([[pos1 :onfloor pos1 has]
    :push
    [pos2 :onfloor pos2 has]])
  ([[pos1 :onfloor box has]
    :walk
    [pos2 :onfloor box has]]))

(defne cangeto [state out]
  ([[_ _ _ :has] true])
  ([_ _] (fresh [action next]
           (moveo state action next)
           (cangeto next out))))

(run 1 [q]
  (cangeto [:atdoor :onfloor :atwindow :hasnot] q)) ; (true)
```

### Sudoku

```clj
(ns sudoku
  (:refer-clojure :exclude [==])
  (:use clojure.core.logic)
  (:require [clojure.core.logic.fd :as fd]))

(defn get-square [rows x y]
  (for [x (range x (+ x 3))
        y (range y (+ y 3))]
    (get-in rows [x y])))

(defn init [vars hints]
  (if (seq vars)
    (let [hint (first hints)]
      (all
        (if-not (zero? hint)
          (== (first vars) hint)
          succeed)
        (init (next vars) (next hints))))
    succeed))

(defn sudokufd [hints]
  (let [vars (repeatedly 81 lvar)
        rows (->> vars (partition 9) (map vec) (into []))
        cols (apply map vector rows)
        sqs  (for [x (range 0 9 3)
                   y (range 0 9 3)]
               (get-square rows x y))]
    (run 1 [q]
      (== q vars)
      (everyg #(fd/in % (fd/domain 1 2 3 4 5 6 7 8 9)) vars)
      (init vars hints)
      (everyg fd/distinct rows)
      (everyg fd/distinct cols)
      (everyg fd/distinct sqs))))

(def hints
  [2 0 7 0 1 0 5 0 8
   0 0 0 6 7 8 0 0 0
   8 0 0 0 0 0 0 0 6
   0 7 0 9 0 6 0 5 0
   4 9 0 0 0 0 0 1 3
   0 3 0 4 0 1 0 2 0
   5 0 0 0 0 0 0 0 1
   0 0 0 2 9 4 0 0 0
   3 0 6 0 8 0 4 0 9])

(sudokufd hints)
;=>
; ((2 6 7 3 1 9 5 4 8
;   9 5 4 6 7 8 1 3 2 
;   8 1 3 5 4 2 7 9 6 
;   1 7 2 9 3 6 8 5 4
;   4 9 5 8 2 7 6 1 3
;   6 3 8 4 5 1 9 2 7
;   5 4 9 7 6 3 2 8 1
;   7 8 1 2 9 4 3 6 5
;   3 2 6 1 8 5 4 7 9))

```
### A type inferencer for the simply typed lambda calculus

```clj
(ns simple-typed-lambda-calculus
   (:refer-clojure :exclude [==])
   (:use clojure.core.logic))

(defna findo [x l o]
  ([_ [[y :- o] . _] _] 
    (project [x y] (== (= x y) true)))
  ([_ [_ . c] _] (findo x c o)))

(defn typedo [c x t]
  (conda
    [(lvaro x) (findo x c t)]
    [(matche [c x t]
       ([_ [[y] :>> a] [s :> t]]
          (fresh [l]
            (conso [y :- s] c l)
            (typedo l a t)))
       ([_ [:apply a b] _]
          (fresh [s]
            (typedo c a [s :> t])
            (typedo c b s))))]))

(comment
  ;; ([_.0 :> _.1])
  (run* [q]
    (fresh [f g a b t]
     (typedo [[f :- a] [g :- b]] [:apply f g] t)
     (== q a)))

  ;; ([:int :> _.0])
  (run* [q]
    (fresh [f g a t]
     (typedo [[f :- a] [g :- :int]] [:apply f g] t)
     (== q a)))

  ;; (:int)
  (run* [q]
    (fresh [f g a t]
     (typedo [[f :- [:int :> :float]] [g :- a]] 
       [:apply f g] t)
     (== q a)))

  ;; ()
  (run* [t]
    (fresh [f a b]
      (typedo [f :- a] [:apply f f] t)))

  ;; ([_.0 :> [[_.0 :> _.1] :> _.1]])
  (run* [t]
    (fresh [x y]
      (typedo [] 
        [[x] :>> [[y] :>> [:apply y x]]] t)))
  )
```