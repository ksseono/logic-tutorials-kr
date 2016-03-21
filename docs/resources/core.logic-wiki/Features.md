## Simple in-memory database

Sometimes it's useful to create a list of facts that you want to run queries over. Use <code>defrel</code> and <code>fact</code>.

```clj
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
   [likes 'Ricky 'Lucy]))

(def facts1 (-> facts0 (pldb/db-fact fun 'Lucy)))

(pldb/with-db facts1
  (run* [q]
    (fresh [x y]
      (fun y)
      (likes x y)
      (== q [x y])))) ; ([Ricky Lucy])
```

It's important to index relationships so that the time to run queries doesn't grow linearly with the number of facts. You can create indexes for any element of the fact tuple. Note that this has implications for memory usage.

```clj
(pldb/db-rel likes ^:index p1 ^:index p2)
```

## A la carte unifier

core.logic comes with a unifier that can be used much like [core.unify](https://github.com/clojure/core.unify):

```clj
(unifier '(?x ?y ?z) '(1 2 ?y)) ; (1 2 _.0)
```

## Constraint Logic Programming (CLP)

core.logic has rapidly evolving support for different forms of [constraint logic programming](http://en.wikipedia.org/wiki/Constraint_logic_programming), CLP for short. core.logic is designed to be extensible to different constraint domains. Out of the box it supports disequality constraints over trees, known as CLP(Tree), and constraint over finite domains, known as CLP(FD).

### CLP(Tree)

CLP(Tree) is pretty simple and adds only one new operator `!=`. Given two terms the use of the `!=` operator will guarantee that the two terms will never unify, in some sense this is "opposite" of what is provided by the unification operator `==`.

The most straightforward use is to just check that something is not equal to some other simple value.

```clj
(run* [q]
  (!= q 1)) ; => ((_0 :- (!= _0 1)))
```

The strange return value is what happens when a var that has never been given a specific value has a constraint on it. This can be read as anything, as long as that anything is not equal to 1.

You can of course use disequality on much more complex terms:

```clj
(run* [q]
  (fresh [x y]
    (!= [1 x] [y 2])
    (== q [x y])))
```

This means something slightly different than what you might think at first glance. It actually reads as "It should never be the case the x is equal to 2 AND y is equal to 1." Thus if `y` gets unified with the value 3 this whole constraint can be discarded. However if `y` gets unified with 1 then the constraint will continue checking to ensure that `x` never becomes unified with 2.

### CLP(FD)

core.logic now has useful operators for constraint logic programming over finite domains, that is positive integers. Prior to these additions arithmetic often required copious use of `project` making these core.logic programs non-relational.

core.logic include the following CLP(FD) operators in the `clojure.core.logic.fd` namespace: `+`, `-`, `*`, `quot`, `==`, `!=`, `<`, `<=`, `>`, `>=`, and `distinct`. The following examples assume that you've required the `clojure.core.logic.fd` namespace and aliased it to `fd`.

In order to use these finite domain operators on logic vars, domains must be declared with `fd/in`.

```clj
(run* [q]
  (fd/in q (fd/interval 1 5))) ; => (1 2 3 4 5)
```

A fairly simple example that quickly reveals how interesting this functionality is:

```clj
(run* [q]
  (fresh [x y]
    (fd/in x y (fd/interval 1 10))
    (fd/+ x y 10)
    (== q [x y]))) ; => ([1 9] [2 8] [3 7] [4 6] [5 5] [6 4] [7 3] [8 2] [9 1])
```

Also included is a macro for writing equations to eliminate the tedium that arises from entering in constraint operations in terms of only two or three arguments, there are no variadic CLP(FD) operators as of yet. Even if there were you still need to create fresh transient intermediate vars to communicate between the various operators.

Enter `fd/eq`. `fd/eq` is a macro that allows you to write arithmetic expressions in normal Lisp syntax which will be expanded into the appropriate series of CLP(FD) operators. It will create the transient intermediate vars for you and domain inference will be applied.

Thus you can write the following:

```clj
(run* [q]
  (fresh [x y]
    (fd/in x y (fd/interval 0 9))
    (fd/eq
      (= (+ x y) 9)
      (= (+ (* x 2) (* y 4)) 24))
    (== q [x y])))
```

`distinct` is a particular useful constraint, it guarantees that all the finite domain variables passed to do not ever take on the same value.

```clj
(run* [q]
  (fresh [x y]
    (fd/in x y (fd/interval 1 10))
    (fd/+ x y 10)
    (fd/distinct [x y])
    (== q [x y]))) ; => ([1 9] [2 8] [3 7] [4 6] [6 4] [7 3] [8 2] [9 1])
```

Note that `[5 5]` is no longer in the set of returned solutions.

## Tabling

core.logic as of version 0.5.4 supports tabling. Certain kinds of logic programs that would not terminate in Prolog will terminate in core.logic if you create a tabled goal.

```clj
(defne arco [x y]
  ([:a :b])
  ([:b :a])
  ([:b :d]))

(def patho
  (tabled [x y]
    (conde
     [(arco x y)]
     [(fresh [z]
        (arco x z)
        (patho z y))])))

;; (:b :a :d)
(run* [q] (patho :a q))
```

## Nominal Logic Programming
core.logic as of version 0.8.0 contains an extension package, [core.logic.nominal](https://github.com/clojure/core.logic/wiki/core.logic.nominal), for nominal logic programming. Nominal logic programming makes it easier to write programs that must reason about binding and scope. As an example, substitution can be defined without worrying about variable capture:

```clj
(defn substo [e new a out]
  (conde
    [(== ['var a] e) (== new out)]
    [(fresh [y]
       (== ['var y] e)
       (== ['var y] out)
       (nom/hash a y))]
    [(fresh [rator ratorres rand randres]
       (== ['app rator rand] e)
       (== ['app ratorres randres] out)
       (substo rator new a ratorres)
       (substo rand new a randres))]
    [(fresh [body bodyres]
       (nom/fresh [c]
         (== ['lam (nom/tie c body)] e)
         (== ['lam (nom/tie c bodyres)] out)
         (nom/hash c a)
         (nom/hash c new)
         (substo body new a bodyres)))]))

(run* [q]
  (nom/fresh [a b]
    (substo ['lam (nom/tie a ['app ['var a] ['var b]])]
            ['var b] a q)))
;; => [['lam (nom/tie 'a_0 '(app (var a_0) (var a_1)))]]

(run* [q]
  (nom/fresh [a b]
    (substo ['lam (nom/tie a ['var b])]
            ['var a]
            b
            q)))
;; => [['lam (nom/tie 'a_0 '(var a_1))]]
```
Find out more on the [core.logic.nominal wiki page](https://github.com/clojure/core.logic/wiki/core.logic.nominal).

## Definite Clause Grammars (Experimental)

core.logic has Prolog-type [Definite Clause Grammar](http://en.wikipedia.org/wiki/Definite_clause_grammar) syntax for parsing. Until core.logic gets support for environment trimming this feature should be considered for experimental use only.

```clj
(def-->e verb [v]
  ([[:v 'eats]] '[eats]))

(def-->e noun [n]
  ([[:n 'bat]] '[bat])
  ([[:n 'cat]] '[cat]))

(def-->e det [d]
  ([[:d 'the]] '[the])
  ([[:d 'a]] '[a]))

(def-->e noun-phrase [n]
  ([[:np d n]] (det d) (noun n)))

(def-->e verb-phrase [n]
  ([[:vp v np]] (verb v) (noun-phrase np)))

(def-->e sentence [s]
  ([[:s np vp]] (noun-phrase np) (verb-phrase vp)))

(run* [parse-tree]
  (sentence parse-tree '[the bat eats a cat] []))

;; ([:s [:np [:d the] [:n bat]] [:vp [:v eats] [:np [:d a] [:n cat]]]])
```