Nominal logic programming is particularly suited for writing operational semantics, type inferencers, and other programs that must reason about scope and binding.

`core.logic.nominal` extends `core.logic` with three primitives for nominal logic programming: `fresh`, `hash` and `tie`. `fresh` introduces new _noms_, `hash` constraints a nom to be free in a term, and `tie` constructs a _binder_: a term in which a given nom is bound.

## Basics

For these snippets, set your namespace as follows:
```clojure
(ns nominal-tutorial
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic :exclude [is] :as l]
        [clojure.core.logic.nominal :exclude [fresh hash] :as nom]
        [clojure.test]))
```

Like a unique object, a _nom_ unifies only with itself or an unbound variable:
```clojure
(is (= (run* [q] (nom/fresh [a] (== a a))) '(_0)))
(is (= (run* [q] (fresh [x] (nom/fresh [a] (== a x)))) '(_0)))
(is (= (run* [q] (nom/fresh [a] (== a 5))) ()))
(is (= (run* [q] (nom/fresh [a b] (== a b))) ()))
```

Like a variable, a _nom_ in the answer is reified to a canonical form, prefixed by `a` to distinguish it from a variable:
```clojure
(is (= (run* [q] (nom/fresh [b] (== b q))) '(a_0)))
```

A _binder_ unifies with another binder up to [alpha-equivalence](http://en.wikipedia.org/wiki/Lambda_calculus#Alpha_equivalence):
```clojure
(is (= (run* [q] (nom/fresh [a b] (== (nom/tie a a) (nom/tie b b)) '(_0)))))
(is (= (run* [q] (nom/fresh [a b] (== (nom/tie a b) (nom/tie b b)) ()))))
(is (= (run* [q] (nom/fresh [a b] (== (nom/tie a b) (nom/tie b a)) ()))))
```

A _#_ constraint ensures that a given term contains no _free_ occurrences of a given nom:
```clojure
(is (= (run* [q] (nom/fresh [a] (nom/hash a a))) ()))
(is (= (run* [q] (nom/fresh [a b] (nom/hash a b))) '(_0)))
(is (= (run* [q] (nom/fresh [a b] (nom/hash a (nom/tie a a)))) '(_0)))
```

Like other constraints, _#_ constraints may get propagated and reified.
```clojure
(is (= (run* [q] (fresh [x] (nom/fresh [a] (nom/hash a x) (== q [a x]))))
      '(([a_0 _1] :- a_0#_1))))
(is (= (run* [q] (fresh [x y] (nom/fresh [a b] (nom/hash a [x y b]) (== q [a b x y]))))
      '(([a_0 a_1 _2 _3] :- a_0#_3 a_0#_2))))
```

For simplicity, we only reify _#_ constraints when its nom and variables are reified too:
```clojure
(is (= (run* [q] (fresh [x y] (nom/fresh [a b] (nom/hash a [x y b])))) '(_0)))
```

## Under the leaky hood

Nominal unification equates alpha-equivalent binders. How does it work when binders of distinct noms contain variables? Let's try it:
```clojure
(is (= (run* [q]
         (fresh [x y]
           (nom/fresh [a b]
             (== (nom/tie a x) (nom/tie b y))
             (== [a b x y] q))))
      '(([a_0 a_1 _2 _3] :- a_1#_2 (swap [a_0 a_1] _3 _2)))))
```
This example reads: `[a] x` and `[b] y` unify when `a#y` and `swap [a b] x y`. The `swap [a b] x y` constraint represents a _suspension_: once `x` and `y` are bound, we want to check that `x` unifies with `y` after swapping all `a`s and `b`s symmetrically at once. The `a#y` constraint ensures that since `a` cannot appear free in `[a] x`, `a` should not appear free in `[b] y`. Symmetrically, we could have used `b#x` instead. This # constraint prevents unifying `[a] b` with `[b] a`, while swapping enables unifying `[a] a` with `[b] b`.

What if `x==y`? Let's try it:
```
(is (= (run* [q]
         (fresh [x y]
           (nom/fresh [a b]
             (== (nom/tie a x) (nom/tie b y))
             (== [a b x y] q)
             (== x y))))
      '(([a_0 a_1 _2 _2] :- a_1#_2 a_0#_2 a_1#_2 a_0#_2 a_1#_2))))
```
(Note that the duplication of constraints in the answer is just an inefficiency and deciphering inconvenience.) This example reads: `[a] x` and `[b] x` unify if `a#x` and `b#x`. Indeed, `a` cannot appear free in `[a] x` and `b` cannot appear free in `[b] x`.

To summarize the findings from the examples above, nominal unification is specified using nom-swaps and #-constraints.

Two binders `t1` and `t2` unify when either:

* `t1` is `[a] c1`, `t2` is `[a] c2` and `c1` unifies with `c2`
* `t1` is `[a] c1`, `t2` is `[b] c2`, `a#c2`, and `c1` unifies with the term `c2` with all `a`s and `b`s swapped.

Swapping introduces suspensions, because when we encounter a variable during swapping, we must _delay_ the swap until the variable is bound.

In `core.logic.nominal`, we implement suspensions as constraints. During swapping of `a` and `b`, whenever we encounter a variable `x`, we replace it with a fresh variable `x'` and add the suspension constraint `swap [a b] x' x`. This swap constraint is executed under one of two conditions:

* `x` and `x'` both become bound -- the swapping can resume
* `x` and `x'` become equal -- we enforce `a#x'` and `b#x'` and drop the swap constraint

