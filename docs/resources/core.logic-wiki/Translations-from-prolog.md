Ok, we are going to see how to translate the knight's moves from prolog to core.logic. First of all forgive me if my terminology is not always correct/logic-oriented (i am just starting!)...

right, so here is the prolog version (it seems a lot but don't worry):

```prolog
move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X + 1 < Xmax,
Y + 2 < Ymax,
A is X + 1,
B is Y + 2. ;;---------------

move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X + 2 < Xmax,
Y + 1 < Ymax,
A is X + 2,
B is Y + 1.;;------------------

move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X + 2 < Xmax,
Y - 1 >= 0,
A is X + 2,
B is Y - 1.;;------------------

move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X + 1 < Xmax,
Y - 2 >= 0,
A is X + 1,
B is Y - 2. ;--------------

move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X - 1 >= 0,
Y - 2 >= 0,
A is X - 1,
B is Y - 2. ;;------------------

move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X - 2 >= 0,
Y - 1 >= 0,
A is X - 2,
B is Y - 1. ;;--------------------

move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X - 2 >= 0,
Y + 1 < Ymax,
A is X - 2,
B is Y + 1. ;;--------------------

move([X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]) :-
X - 1 >= 0,
Y + 2 < Ymax,
A is X - 1,
B is Y + 2.
```

Ok, we basically have 8 clauses (you can think of them as possibilities or potential moves). Xmax and Ymax are not interesting at all as they are constants on a particular grid (e.g. 8x8). Let's however look at the argument of move (same in all cases): [X, Y, Xmax, Ymax], [A, B, Xmax, Ymax]

if you're familiar with pattern-matching you can immediately see what is happening here. x corresponds to a and y to b. [x,y] denote where we are now and [a,b] where we can potentially go. The actual implementation of each clause simply declares all the goals that must succeed in order for the whole clause to succeed. So, if we think about it the outer clauses (all the move) are being OR-ed and the inner clauses (inside each 'move') are being AND-ed. This is where it clicked for me (honestly I know no prolog whatsoever!)...

Let's look at the core.logic equivalent:
```clojure
(defn knight-moves 
"Returns the available moves for a knight (on a 8x8 grid) given its current position." 
 [x y]
(let [xmax 8 ymax 8]
 (run* [q] ;bring back all possible solutions
 (fresh [a b] ;like 'let' but for logic variables
  (conde ;;like OR
    [(< (+ x 1) xmax) (< (+ y 2) ymax) (== a (+ x 1)) (== b (+ y 2))] ;1st possibility
    [(< (+ x 2) xmax) (< (+ y 1) ymax) (== a (+ x 2)) (== b (+ y 1))] ;2nd possibility
    [(< (+ x 2) xmax) (>= (- y 1)   0) (== a (+ x 2)) (== b (- y 1))] ;3rd possibility
    [(< (+ x 1) xmax) (>= (- y 2)   0) (== a (+ x 1)) (== b (- y 2))] ;4th possibility
    [(>= (- x 1)   0) (>= (- y 2)   0) (== a (- x 1)) (== b (- y 2))] ;5th possibility
    [(>= (- x 2)   0) (>= (- y 1)   0) (== a (- x 2)) (== b (- y 1))] ;6th possibility
    [(>= (- x 2)   0) (< (+ y 1) ymax) (== a (- x 2)) (== b (+ y 1))] ;7th possibility
    [(>= (- x 1)   0) (< (+ y 2) ymax) (== a (- x 1)) (== b (+ y 2))] ;8th possibility
  ) 
   (== q [a b]))))) ;return each solution in a vector [x, y]
```
Ok, so we have a regular function that declares our boring constants as regular clojure vars (xmax,ymax) before doing any sort of querying. Inside 'conde' we see 8 vector-clauses. These 8 clauses are the outer clauses from the prolog example which were being OR-ed. In the same way clauses inside a conde will be OR-ed but without stopping at the first success. In other words, conde will try all clauses. Now, each vector-clause has several forms. These forms are essentially the inner clauses from previously which were being AND-ed. Let's isolate the first vector:

```clojure
 [(< (+ x 1) xmax) 
  (< (+ y 2) ymax) 
  (== a (+ x 1)) 
  (== b (+ y 2))]
```

Isn't this exactly the same with this? (first move clause from prolog):
```prolog
X + 1 < Xmax,
Y + 2 < Ymax,
A is X + 1,
B is Y + 2.
```

I hope this makes sense...It's worth mentioning that operators like "<", ">", "=" etc etc are non-relational and exist in the core.logic.arithmetic namespace. For this particular example I am using it like this:
```clojure
(ns XXXXX.xxx
       (:refer-clojure :exclude [== >= <= > < =])
       (:use clojure.core.logic 
             clojure.core.logic.arithmetic))
```

In exactly the same way as we did the knight we can do the king and the pawn as well! let's look at the king:

```clojure
(defn king-moves 
"Returns the available moves for a king (on a 8x8 grid) given its current position."
[x y]
(let [xmax 8 ymax 8]
 (run* [q]
 (fresh [a b]
  (conde 
    [(< (+ x 1) xmax) (< (+ y 1) ymax) (== a (+ x 1)) (== b (+ y 1))] ;1st possibility (diagonally)
    [(>= (- x 1) 0) (>= (- y 1) 0) (== a (- x 1)) (== b (- y 1))]     ;2nd possibility (diagonally)
    [(< (+ y 1) ymax) (== a x) (== b (+ y 1))]                        ;3rd possibility (x is constant)
    [(>= (- y 1) 0) (== a x) (== b (- y 1))]                          ;4th possibility (x is constant)
    [(>= (- x 1) 0) (== b y) (== a (- x 1))]                          ;5th possibility (y is constant)
    [(< (+ x 1) xmax) (== b y) (== a (+ x 1))]                        ;6th possibility (y is constant)
    [(< (+ x 1) xmax) (> (- y 1) 0) (== a (+ x 1)) (== b (- y 1))]    ;7th possibility (diagonally)
    [(>= (- x 1) 0) (< (+ y 1) ymax) (== a (- x 1)) (== b (+ y 1))]   ;8th possibility (diagonally)
  ) 
   (== q [a b]))))) ;return each solution in a vector [x, y]
```

Unfortunately, these are the only 3 chess-pieces that can be implemented this way (simply by enumerating cases). For other pieces I used something slightly different which I don't know if it is idiomatic but at least seems to be and it is working just fine. However I need to make clear that I wrote this today and so it has not been thoroughly tested:

```clojure
(def ^:const board (vec (range 8)))

(defn rook-moves 
"Returns the available moves for a rook (on a 8x8 grid) given its current position."
[x y]
 (run* [q]
 (fresh [a b]
 (conde 
  [(membero a board) (!= a x) (== b y)]  ;y remains constant
  [(membero b board) (!= b y) (== a x)]) ;x remains constant
     (== q [a b]))))

(defn bishop-moves 
"Returns the available moves for a bishop (on a 8x8 grid) given its current position and direction."
[x y]
(run* [q] 
(fresh [a b] 
  (membero a board) 
  (membero b board)
   (!= a x) 
   (!= b y)
    (project [x y a b]
    (== (Math/abs (- x a)) 
        (Math/abs (- y b)))
           (== q [a b])))))
```

A nice exercise would be to translate 'king-moves' to prolog!

Hope that helps someone... :) Jim
