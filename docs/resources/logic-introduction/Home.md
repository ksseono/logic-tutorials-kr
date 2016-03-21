# Introduction to Logic Programming with Clojure

This tutorial assumes zero experience with Logic Programming, but some experience with Clojure.

Please direct any feedback to Ambrose Bonnaire-Sergeant via
http://twitter.com/#!/ambrosebs or abonnairesergeant@gmail.com

# Other Pages

[Arithmetic with core.logic](https://github.com/frenchy64/Logic-Starter/wiki/Arithmetic)

# Thanks

Many thanks to these Clojurists for their invaluable feedback and encouragement!

David Nolen [Twitter](http://twitter.com/#!/swannodette), [Github](https://github.com/swannodette), [Blog](http://dosync.posterous.com/)

Jim Duey    [Twitter](http://twitter.com/#!/jimduey)


# How to use this Tutorial

This tutorial is meant to be used with a Clojure REPL handy. An [example project](https://github.com/frenchy64/Logic-Starter) has been set up.

Clone with:

`git clone git@github.com:frenchy64/Logic-Starter.git`

You should be able to run all code examples in the `logic-introduction.core` namespace.

# Quick note on formatting

If any code examples appear to be missing pairs of square braces, please check the wiki source and separate each brace with a space (ie. `[ [` and `] ]`). If you know how to escape this character sequence in a code block with Markdown, please let me know.

# Introduction

Welcome!

In the following sections, we will explore an implementation of miniKanren, a logic
programming system.

At least two ports of miniKanren to Clojure have been made: 

- [mini-kanren](https://github.com/jduey/mini-kanren) by [Jim Duey](http://twitter.com/#!/jimduey)
- [core.logic](https://github.com/clojure/core.logic) by [David Nolen](http://twitter.com/#!/swannodette)

This tutorial will use `core.logic`s implementation, which resides in the namespace `clojure.core.logic.minikanren`. `core.logic`s port is rather faithful to miniKanren, which has its roots in Scheme.

Because of this, it carries some historical baggage. 

Function names are rather terse compared to idiomatic Clojure, but we will try and help by providing some mnemonics. But don't worry, a little practice and they'll be like old friends.

We also follow the convention of naming relations with the suffix "o".

This tutorial will explore some basic logic programming concepts, and is intended as motivation for more
involved texts, such as The Reasoned Schemer.

Enjoy playing with the type checker, but keep an eye on it ... I think I saw it move!


# Motivation: Simple Type Checker


Let's say we want to check the resulting type for this expression:


```clojure
(let [f (fn [a] a)
      g 1]
  (f g))
```



We propose an interface for a type checker:


```clojure
(typed
  <environment>
  <expression>
  <expected-type>)
;=> <boolean>
```


An initial version of `typed` (aka. "Type Determine") takes three arguments:

1. an environment (map of variables to values), 
2. an expression, 
3. and an expected resulting type.

`typed` returns `true` if the resulting type of executing the expression in the given environment is equal to the third argument.

In other words, return `true` if it type-checks successfully.

Here are some imaginary executions of `typed`.

```clojure
(typed
  [f (fn [a] a)
   g 1]
  (f g)
  Integer)
;=> true


(typed
  [f (fn [a] a)
   g 1]
  (f g)
  Float)
;=> false
```


# Converting a function into a relation

___
> **Logic Programming Concept: Relations**

> A relation is simply a function that returns a **goal** as its value.


> **Logic Programming Concept: Goals**

> A goal can either succeed or fail.
>
> All relations return goals.
>
> There are two special goals that are used like constants:
> 
> - `clojure.core.logic.minikanren/succeed` represents the successful goal
> - `clojure.core.logic.minikanren/fail` represents a failed goal.
> 
> We will see examples of using these two goals throughout the tutorial.


Let's propose a new interface for `typed` that converts it into a relation.


```clojure
(typedo
  <environment>
  <expression>
  <expected-type>)
;=> <goal>
```


By convention, relations end their name with "o".

Compare to `typed`s calling interface:


```clojure
(typed
  <environment>
  <expression>
  <expected-type>)
;=> <boolean>
```


Note what we changed:

1. We renamed `typed` to `typedo`
2. `typedo` returns a goal instead of a Boolean value




# Running the relational type checker

___
> **Logic Programming Concept: run**

> To execute a logic expression, we use `run*`.

> `run*` is not a relation; it does not return goal.
___

As an example, let's roughly translate the following code, 

```clojure
(let [f (fn [a] a)
      g 1]
  (f g))
```

into our type checker, and test that it returns an integer.  


```clojure
(run* [q]
      (typedo [ ['f :- [Integer :> Integer] ] ;; 'f is of type Integer -> Integer
               ['g :- Integer]              ;; 'g is of type Integer
               ] 
              [:apply 'f 'g] ;; Determine the resulting type of ('f 'g) ..
              Integer)       ;;  and succeed if it is Integer
      )
;=> (_.0)
```


Here are some facts:

- `:-` is pronounced "is of type".

- `[Integer :> Integer]`  represents the type of a function that take a single argument of type `Integer` and returns a value of type `Integer`.

- `['f :- [Integer :> Integer] ]` is called a type association.

- ` [ ['f :- [Integer :> Integer] ] ['g :- Integer] ]`
  is an environment (represented by a vector of type associations).

- `[:apply x y]` represents the function call `(x y)`.

___

There are still mysteries about `run*` that we will leave for now:

1. What does `run*`s first argument, `[q]`, mean?
2. What exactly is `run*`s return value? We've already said it's not a relation, therefore it doesn't return a goal, 
but what is it?



# Play with the type checker

Let's get a feel for our type checker.
   

```clojure
(run* [q]
      (typedo [ ['f :- [Float :> Integer] ]
               ['g :- Integer]
               ] 
              [:apply 'f 'g]
              Integer)
      )
;=> ()
```


The run returns `()` because `typedo` fails.

> `typedo` fails because it returns an unsuccessful goal.

> Expanded, the run is equivalent to

```clojure
(run* [q]
      fail
      )
```

Why did `typedo` fail?
___
   

```clojure
(run* [q]
      (typedo [ ['f :- [Float :> Integer] ]
               ['g :- Float]
               ] 
              [:apply 'f 'g]
              Integer)
      )
;=> (_.0)
```


The run returns a non-empty list because no goals failed. 
   
> `typedo` succeeds because it returns a successful goal.   

> Expanded, the run is equivalent to

```clojure
(run* [q]
      succeed
      )
```

Why did `typedo` succeed?
___


```clojure
(run* [q]
      (typedo [ ['f :- [Float :> Integer] ]
               ['g :- Float]
               ['h :- [Integer :> Float] ]
               ] 
              [:apply 'h [:apply 'f 'g] ]
              Float)
      )
;=> (_.0)
```


The run returns a non-empty list because no goals failed. (Why does `typedo` succeed?)


# Familiar faces

```clojure
(run* [q]
      (typedo [ ['max :- [Integer :> [Integer :> Integer] ]]
               ['a :- Integer]
               ['b :- Integer]
               ] 
              [:apply [:apply 'max 'a] 'b]
              Integer)
      )
;=> (_.0)
```


The run returns a non-empty list because no goals failed. (Why does `typedo` succeed? What does 'max probably do?)
   


```clojure
(run* [q]
      (typedo [ ['and :- [Boolean :> [Boolean :> Boolean] ]]
               ['x :- Boolean]
               ['y :- Boolean]
               ] 
              [:apply [:apply 'and 'x] 'y]
              Boolean)
      )
;=> (_.0)
```

The run returns a non-empty list because no goals failed. (Why does `typedo` succeed? What does 'and probably do?)


# Polymorphism


Does our type checker support polymorphism?


```clojure
(run* [q]
      (typedo [ ['int :- [Number :> Integer] ]
               ['x :- Double]
               ] 
              [:apply 'int 'x]
              Integer)
      )
;=> ()
```


The run returns `()` because `typedo` returns a failed goal. 

Why does `typedo` fail? 
Where does `typedo` fail?

Hint: `(= true (isa? Double Number))`
   

```clojure
(run* [q]
      (typedo [ ['int :- [Double :> Integer] ]
               ['x :- Double]
               ] 
              [:apply 'int 'x]
              Integer)
      (typedo [ ['int :- [Float :> Integer] ]
               ['x :- Float]
               ] 
              [:apply 'int 'x]
              Integer)
      (typedo [ ['int :- [Integer :> Integer] ]
               ['x :- Integer]
               ] 
              [:apply 'int 'x]
              Integer)
      )
;=> (_.0)
```


The run returns a non-empty list because no goals failed.


# Review

In what situations does `run*` return an empty list?

In what situations does `run*` return a non-empty list?



# Interlude: Logic variables

A logic variable is a lexically scoped variable that can be assigned to
exactly once after it is "fresh".

A fresh variable is conceptually similar to a declared variable that has no useful value.

`run*`s first argument is a vector containing a name. The name is declared as a fresh logic variable.

`run*` returns a list of values assigned to `q` if no goals in the run fail.


```clojure
(run* [q])
;=> (_.0)
```


A fresh variable is printed with a non-negative number prefixed by "_.".



# Interlude: Unification


In logic programming, assignment and equality tests are performed by unification.

Unification is represented by the macro `clojure.core.logic.minikanren/==`, and
is pronounced "unify". 

> Unify is `==` for historical reasons, being faithful to the Scheme implementation of miniKanren.
> Don't confuse it with `clojure.core/==`

`==` is a relation; it returns an successful goal if unification is successful, otherwise
an unsuccessful goal.


```clojure
(run* [q]
     (== 1 1))
;=> (_.0)
```


`1` is the same as `1`, so unification succeeds.

The run returns a non-empty list of the values of `q` because no goals failed.

Because `q` is fresh, `(_.0)` is returned.

```clojure
(run* [q]
     (== 0 1))
;=> ()
```


`0` is not same as `1`, so unification fails.

The run returns `()` because a goal failed. (Which goal failed?)




___
> The Law of Fresh
>  
> If `x` is fresh, then (== v x) succeeds and associates `x` with `v`.
> (The Reasoned Schemer)
___



```clojure
(run* [q]
     (== q 1))
;=> (1)
```

As `q` is fresh, it is associated with `1`, and the expression succeeds.

No goals fail, and `q` is `1`, so the expression returns `(1)`



```clojure
(run* [q]
     (== 1 q)
     (== q 1))
;=> (1)
```


As `q` is fresh, it is associated with `1`, and the first unification succeeds. 

___
> Order does not matter with unification. (== q 1) is identical to (== 1 q)
___

As `q` is `1`, which is the same as `1`, the second unification succeeds.

No goals fail, so the expression returns `(1)`.


```clojure
(run* [q]
     (== q 1)
     (== q 2))
;=> ()
```

As `q` is fresh, it is associated with `1`, and the first unification succeeds.

`q` (which is now associated with `1`) is not the same as `2`, so the second unification fails. 

A goal fails, so the expression returns `()`


```clojure
(run* [q]
      (typedo [ ['int :- [Double :> Integer] ]
               ['x :- Integer]
               ] 
              [:apply 'int 'x]
              Integer)
      (== q true))
;=> ()
```


Why does this run return `()`?



```clojure
(run* [q]
      (typedo [ ['int :- [Double :> Integer] ]
               ['x :- Double]
               ] 
              [:apply 'int 'x]
              Integer)
      (== q true))
;=> (true)
```


Why does this run return `(true)`?


# Surprises



Why does this run return `(java.lang.Integer)`?


```clojure
(run* [q]
      (typedo [ ['int :- [Integer :> q] ]
               ['x :- Integer]
               ] 
              [:apply 'int 'x]
              Integer))
;=> (java.lang.Integer)
```


Because `typedo` would succeed if `q` was associated with `java.lang.Integer`. (Why?)

___
Why does this run return `(java.lang.Integer)`?


```clojure
(run* [q]
      (typedo [ ['int :- [Integer :> Integer] ]
               ['x :- q]
               ] 
              [:apply 'int 'x]
              Integer))
;=> (java.lang.Integer)
```


Because `typedo` would succeed if `q` was associated with `java.lang.Integer`. (Why?)

___
Why does this run return `([java.lang.Integer :> java.lang.Integer])`?


```clojure
(run* [q]
      (typedo [ ['int :- q]
               ['x :- Integer]
               ] 
              [:apply 'int 'x]
              Integer))
;=> ([java.lang.Integer :> java.lang.Integer])
```


Because `typedo` would succeed if `q` was associated with `[java.lang.Integer :> java.lang.Integer]`. (Why?)

___
Why does this run return `()`?

```clojure
(run* [q]
      (typedo [ ['int :- [Integer :> Double] ]
               ['x :- q]
               ] 
              [:apply 'int 'x]
              q))
;=> ()
```

Because no values can be associated with `q` such that `typedo` succeeds. (Why?)


# Multiple Variables
___
> **Logic Programming Concept: fresh**

> `fresh` takes a vector of names which are initialized to fresh logic variables.
___   




>  When one variable is associated with another, we say they co-refer, or share.
>  (The Reasoned Schemer, pg 9)



```clojure
(run* [q]
      (fresh [a]
             (== q a)
             (== a 1)))
;=> (1)
```


Both `q` and `a` are fresh when they are associated with each other. `q` gets whatever
associations `a` gets.

`a` is associated with `1`, which is `q`.

No goals fail, so the expression returns `(1)`.

___

Why does this expression return `( [java.lang.Double java.lang.Integer] )`?

```clojure
(run* [q]
      (fresh [a b]
             (typedo [ ['int :- [Integer :> Double] ]
                      ['x :- b]
                      ] 
                     [:apply 'int 'x]
                     a)
             (== q [a b])))
;=> ([java.lang.Double java.lang.Integer])
```

Because `typedo` would succeed if both `a` was associated with `java.lang.Double` and `b` was associated with `java.lang.Integer`. (Why?)

___

Why does this expression return `([ [_.0 :> java.lang.Integer] _.0])`?

```clojure
(run* [q]
      (fresh [a b]
             (typedo [ ['int :- a]
                      ['x :- b]
                      ] 
                     [:apply 'int 'x]
                     Integer)
             (== q [a b])))
;=> ([ [_.0 :> java.lang.Integer] _.0])
```


Because `typedo` would succeed if `a` was associated with `[_.0 :> java.lang.Integer]` and `b` was associated with `_.0`.

`_.0` represents a fresh variable. The `typedo` would succeed if all instances of `_.0` are substituted with the same (arbitrary) type.

Verify this by substituting all `_.0`s with a type.

___

Why does this expression return `( [ [_.0 :> _.1] _.0 _.1] )`?


```clojure
(run* [q]
      (fresh [a b c]
             (typedo [ ['int :- a]
                      ['x :- b]
                      ] 
                     [:apply 'int 'x]
                     c)
             (== q [a b c])))
;=> ([ [_.0 :> _.1] _.0 _.1])
```

Because `typedo` would succeed if 

- `a` was associated with `[_.0 :> _.1]` and 
- `b` was associated with `_.0` and
- `c` was associated with `_.1`.


The `typedo` would succeed 

- if all instances of `_.0` are substituted with the same (arbitrary) type and
- if all instances of `_.1` are substituted with the same (arbitrary) type.

Verify this by substituting all `_.0`s with a type and substituting all `_.1`s with a type.


# Infinite results


Why does the following expression not yield a value?

```clojure
(run* [q]
     (fresh [a x]
            (typedo [ ['f :- [Integer :> a] ]
                     ['g :- Integer] ]
                    x
                    Float)
            (== [x a] q)))
;
```

Because there are infinite values of `q` that satisfy `typedo`.

___
Why does the following expression yield a value?

```clojure
(run 2 [q]
     (fresh [a x]
            (typedo [ ['f :- [Integer :> a] ]
                     ['g :- Integer] ]
                    x
                    Float)
            (== [x a] q)))
;=> ([ [:apply f g] java.lang.Float] 
;    [ [:apply [:apply f g] g] [java.lang.Integer :> java.lang.Float] ])
```

Because we requested for 2 values of `q` that satisfy `typedo`.

Verify each combination satisfies `typedo`.

___
Why does the following expression yield a value?

```clojure
(run 4 [q]
     (fresh [a x]
            (typedo [ ['f :- [Integer :> a] ]
                     ['g :- Integer] ]
                    x
                    Float)
            (== [x a] q)))
;=> ([ [:apply f g] java.lang.Float] 
;    [ [:apply [:apply f g] g] [java.lang.Integer :> java.lang.Float] ] 
;    [ [:apply [:apply [:apply f g] g] g] [java.lang.Integer :> [java.lang.Integer :> java.lang.Float] ]] 
;    [ [:apply [:apply [:apply [:apply f g] g] g] g] [java.lang.Integer :> [java.lang.Integer :> [java.lang.Integer :> java.lang.Float] ]] ]) 
```

Because we requested for 4 values of `q` that satisfy `typedo`.


# Logic Programming Concept: conde

> The macro `conde` resides at `clojure.core.logic.minikanren/conde

Why does this expression return nil?

```clojure
(cond
  false true)
;=> nil
```

Because the question is falsy, `cond` falls though and returns nil.

___
Why does this expression fail?


```clojure
(run* [q]
      (conde
        (fail succeed)))
;=> ()
```


Because the question fails, `conde` falls through and fails.

___

Why does this expression return true?


```clojure
(cond
  true true)
;=> true
```

Because the question is truthy, and the answer is `true`.


Why does this expression succeed?

```clojure
(run* [q]
      (conde
        (succeed succeed)))
;=> (_.0)
```

Because the question succeeds, and the answer is successful.



> `conde` clauses have 1 question and 0 or more answers.
___

Why does this expression succeed?


```clojure
(run* [q]
      (conde
        (succeed succeed succeed)
        (succeed fail)
        (succeed succeed)))
;=> (_.0 _.0)
```

The first clause succeeds because the question and the answers succeed.
`q` is still fresh.

`q` is refreshed to a new fresh value.
The second clause fails because the answer fails.

`q` is refreshed to a new fresh value.
The third clause succeeds because the question and the answers succeed.
`q` is still fresh.

At least one clause succeeds, so `conde` succeeds.

Two fresh values are returned, one from each successful clause.


___
Why does this expression succeed?

```clojure
(run* [q]
      (conde
        ((== 'olive q) succeed)
        ((== 'oil q) succeed)))
;=> (olive oil)
```

Because `(== 'olive q)` succeeds, and therefore the answer is `succeed`. 
The succeed preserves the association of `q` to 'olive.

To get the second value we "pretend" that `(== 'olive q)` fails; this
imagined failure "refreshes" `q`.

Then `(== 'oil q)` succeeds.
The succeed preserves the association of `q` to `'oil`.

We then pretend that `(== 'oil q)` fails, which once again refreshes `q`.

Since no more goals succeed, we are done.

  (The Reasoned Schemer, Pg 11)

Since at least one `conde` clause succeeded, the `conde` expression succeeds.

Since no goals fail, the expression succeeds.


>  The Law of conde

>  To get more values from conde, pretend
>  that the successful conde line has failed,
>  refreshing all variables that got an association
>  from that line.

>  (The Reasoned Schemer)



The "e" in `conde` stands for "every line", since every line can succeed.

(The Reasoned Schemer, Pg 12)



# Logic Programming Concept: run n


Why isn't the value of this expression `(olive oil)`?

```clojure
(run 1 [q]
      (conde
        ((== 'olive q) succeed)
        ((== 'oil q) succeed)))
;=> (olive)
```

Because `(== 'olive q)` succeeds and because `run 1` produces at most _one_ value of `q`.
(The Reasoned Schemer, pg 12)



Experiment with the number of clauses and with varying the number of output values.

___
Why is the value of this expression `(extra virgin)`?

```clojure
(run 2 [q]
      (conde
        ((== 'extra q) succeed)
        ((== 'virgin q) succeed)
        ((== 'olive q) succeed)
        ((== 'oil q) succeed)))
;=> (extra virgin)
```

Because `run 2` produces at most two values.


___

Why is the value of this expression `(extra virgin olive oil)`?

```clojure
(run* [q]
      (conde
        ((== 'extra q) succeed)
        ((== 'virgin q) succeed)
        ((== 'olive q) succeed)
        ((== 'oil q) succeed)))
;=> (extra virgin olive oil)
```

Because all clauses succeed and because `run*` keeps producing values until
they are exhausted.


# matche

`matche` is a syntactic variation on `conde` that introduces pattern matching.

The following expressions are equivalent.

```clojure
(run* [q]
      (conde
        ((== 'extra q) succeed)
        ((== 'virgin q) succeed)
        ((== 'olive q) succeed)
        ((== 'oil q) succeed)))
;=> (extra virgin olive oil)
```

```clojure
(run* [q]
      (matche [q]
              (['extra]  succeed)
              (['virgin] succeed)
              (['olive]  succeed)
              (['oil]    succeed)))
;=> (extra virgin olive oil)
```

# matche sugar: Wildcards

`matche` supports wildcards with `_`.

```clojure
(run* [q]
  (fresh [a o]
    (== a [1 2 3 4 5])
    (matche [a]
            ([_]
             (== q "first"))
            ([ [1 2 3 4 5] ]
             (== q "second"))
            (["a"]
             (== q "third")))))
;=> ("first" "second")
```

The first clause matches because the wildcard matches `[1 2 3 4 5]`.
The second clause matches because `(== [1 2 3 4 5] [1 2 3 4 5] )` succeeds.
The third clause fails because `(== [1 2 3 4 5] "a")` fails.


# matche sugar: List destructuring

`matche` supports destructuring with `.`.

```clojure
(run* [q]
  (fresh [a o]
    (== a [1 2 3 4 5])
    (matche [a]
            ([ [1 2 . [3 4 5] ]]
             (== q "first"))
            ([ [1 2 3 . [4 5] ]]
             (== q "second"))
            ([ [1 . _] ]
             (== q "third")))))
;=> ("first" 
;    "second"
;    "third")
```


The first clause matches because `[1 2 . [3 4 5] ]` matches `[1 2 3 4 5]`
The second clause matches because `[1 2 3 . [4 5] ]` matches `[1 2 3 4 5]`
The third clause matches because `[1 . _]` matches `[1 2 3 4 5]` when the wildcard is replaced with `[2 3 4 5]`


# matche sugar: Combining wildcards and destructuring

Wildcards match the minimum possible amount to satisfy matching.

```clojure
(run* [q]
  (fresh [a o]
    (== a [1 2 3 4 5])
    (matche [a]
            ([ [1 . _] ]
             (== q "first"))
            ([ [_ . o] ]
             (== q ["second" o])))))
;=> ("first" 
;    ["second" (2 3 4 5)])
```

The `_` in the second clause is guaranteed just to match `1`, as that is
the absolute minimum required to satisfy matching.


# matche sugar: Implicit variables

`matche` implicitly declares variables prefixed by "?".

```clojure
(run* [q]
  (fresh [a o]
    (== a [1 2 3 4 5])
    (matche [a]
            ([ [1 . o] ]
             (== q ["one" o]))
            ([ [1 2 . ?o] ]
             (== q ["two" ?o]))
            ([ [o . ?o] ]
             (== q ["third" o ?o])))))
;=> (["one" (2 3 4 5)] 
;    ["two" (3 4 5)] 
;    ["third" 1 (2 3 4 5)] 
```

`matche` declares `?o` for us, and `?o` acts like any other variable.



# Utility Function: geto


Before we go on to more advanced uses of `typedo` let's explore a utility function it uses, `geto`.


```clojure
(ns logic-introduction.core
  (:refer-clojure :exclude [inc reify ==])
  (:use [clojure.core.logic minikanren prelude nonrel match disequality]))

(defn geto [key env value]
  "Succeed if type association [key :- value] is found in vector env."
  (matche [env]
          ([ [[key :- value] . _] ])
          ([ [_ . ?rest] ] (geto key ?rest value))))
```

Here are some sample executions of `geto`


```clojure
(run* [q]
      (geto 'f 
            [ ['f :- Integer] ] 
            Integer)
      (== q true))
;=> (true)
```

The type association `['f :- Integer]` occurs in the environment  `[ ['f :- Integer] ]`, so `geto` succeeds.
___


```clojure
(run* [q]
      (geto 'g 
            [ ['f :- Integer] ] 
            Integer)
      (== q true))
;=> ()
```

The type association `['g :- Integer]` does not occur in the environment `[ ['f :- Integer] ]`, so `geto` fails.

___

```clojure
(run* [q]
      (geto 'c
            [ ['f :- [Integer :- Float] ]
             ['a :- Integer]
             ['b :- Integer]
             ['c :- Integer]
             ['d :- Integer] ]
            Integer)
      (== q true))
;=> (true)
```

The type association `['c :- Integer]` occurs in the environment given, so `geto` succeeds.

__

Can `geto` do anything else interesting?

What is `a` in the following expression?

```clojure
(run* [q]
      (fresh [a]
             (geto 'c
                   [a] 
                   Integer)
             (== q a)))
;=> ([c :- java.lang.Integer])
```

`a` is the type association needed to satisfy `geto`.
___


What is `a` in the following expression?

```clojure
(run* [q]
      (fresh [a]
             (geto 'a
                   [ ['f :- [Integer :- Float] ]
                    a
                    ['b :- Integer]
                    ['c :- Integer]
                    ['d :- Integer] ]
                   Integer)
             (== q a)))
;=> ([a :- java.lang.Integer])
```


`a` is the type association needed to satisfy `geto`.
___


What is interesting about the following expression?

```clojure
(run* [q]
      (fresh [a]
             (geto 'c
                   [ ['f :- [Integer :- Float] ]
                    a
                    ['b :- Integer]
                    ['c :- Integer]
                    ['d :- Integer] ]
                   Integer)
             (== q a)))
;=> ([c :- java.lang.Integer] _.0)
```

It returns two values of `a` satisfying `geto`.

The first value, `[c :- java.lang.Integer]`, gets associated on the 2nd line of the environment, similar to the previous example.
This satisfies `geto`, so `a` successfully gets associated a value.

It looks like `a` is then made fresh, and the rest of environment is searched. 

Then the 4th line of the environment satisfies `geto`, because it is `['c :- Integer]`.
This satisfies `geto`, so `a` successfully gets a value, that happens to be fresh.

___

What does the result of this expression tell us?

```clojure
(run* [q]
      (fresh [a b c d]
             (geto 'c
                   [a b c d]
                   Integer)
             (== q [a b c d])))
;=> ([ [c :- java.lang.Integer] _.0 _.1 _.2] 
;     [_.0 [c :- java.lang.Integer] _.1 _.2] 
;     [_.0 _.1 [c :- java.lang.Integer] _.2] 
;     [_.0 _.1 _.2 [c :- java.lang.Integer] ])
```

The result tells us that `geto` always searches to the end of the environment vector, collecting results along the way, 
regardless of whether it found a match previously.

This seems inefficient.

> Advanced, non-relational variants of `conde` allow control over how many clauses are executed,
> trimming results for increased efficiency. This comes at the cost of flexibility, so they should be used carefully.
> (see `conda` and `conde` in The Reasoned Schemer)

## Something to think about

Explain the following expression, and its result:

```clojure
(run 6 [q]
      (fresh [a]
             (geto 'c
                   a
                   Integer)
             (== q a)))
;=> (([c :- java.lang.Integer] . _.0) 
;    (_.0 [c :- java.lang.Integer] . _.1) 
;    (_.0 _.1 [c :- java.lang.Integer] . _.2) 
;    (_.0 _.1 _.2 [c :- java.lang.Integer] . _.3) 
;    (_.0 _.1 _.2 _.3 [c :- java.lang.Integer] . _.4) 
;    (_.0 _.1 _.2 _.3 _.4 [c :- java.lang.Integer] . _.5))
```

What does the list destructuring, and the fresh variable and the end of every result mean?

What does `_.0` represent in the first item of the result?



# Logic Programming Concept: Disequality

Disequality constraints guarantee that two terms can never become equal. It is comparable to the inverse of unification.

`!=` is used to describe this relationship.


This expression succeeds

```clojure
(run* [q]
      (!= q 2)
      (== q 1))
;=> (1)
```

because `(!= q 2)` guarantees `q` not to be associated with `2`.

`(== q 1)` assigns `q` to `1` successfully.


This expression fails

```clojure
(run* [q]
      (!= q 2)
      (== q 2))
;=> ()
```

because `(!= q 2)` guarantees `q` not to be associated with 2. 

The disequality constraint causes `(== q 2)` to fail.

Therefore the run yields `()`.


# Advanced uses: `typedo`
