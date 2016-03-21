Clone `git clone git@github.com:frenchy64/Logic-Starter.git`

Start a REPL in namespace `logic-introduction.numbers`.

Feedback to http://twitter.com/#!/ambrosebs

___


Numbers are defined recursively. Zero is `0`, and the next number is the
previous, but wrapped in a list.

```clojure
logic-introduction.numbers=> zero
0
logic-introduction.numbers=> one
(0)
logic-introduction.numbers=> two
((0))
logic-introduction.numbers=> three
(((0)))
```

`s` returns the next number after its argument.

```clojure
logic-introduction.numbers=> (s one)
((0))
logic-introduction.numbers=> (s four)
(((((0)))))
```

`q` is the number after zero.

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (== q (s zero)))
((0))
```

`q` is the number before one.

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (== one (s q)))
(0)
```

`q` is the number before zero.

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (== zero (s q)))
()
```

We can only represent non-negative numbers.

___

Question: Do we need a function `p`, that returns the previous number of its argument?
___

Let's define a relation called `natural-number`.

```clojure
(defn natural-number [x]
  "A relation where x is a natural number"
  (matche [x]
          ([zero])
          ([(s ?x)] (natural-number ?x))))
```

What can this relation do?

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (natural-number one))
(_.0)
logic-introduction.numbers=> (run 1 [q]
                                  (natural-number two))
(_.0)
logic-introduction.numbers=> (run 1 [q]
                                  (natural-number q))
(0)
logic-introduction.numbers=> (run 6 [q]
                                  (natural-number q))
(0 (0) ((0)) (((0))) ((((0)))) (((((0))))))
```

Let's revise the documentation string for `natural-number`.

```clojure
"A relation where x is a natural number"
```

This reads like a constraint: x is a natural number.

We read this "one is a natural number".

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (natural-number one))
(_.0)
```

This satisfies the constraint.

The logic engine has some surprisingly flexible ways of satisfying this constraint

We read this "`q` is a natural number".

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (natural-number q))
(0)
```

The constraint is satisfied by assigning `q` to zero.

What happens if we ask for six results? 

```clojure
logic-introduction.numbers=> (run 6 [q]
                                  (natural-number q))
(0 (0) ((0)) (((0))) ((((0)))) (((((0))))))
```

We get six values, each satisfying the constraint.

We read this "`\a` is a natural number".

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (natural-number \a))
()
```

The constraint cannot be satisfied.

____


Let's define a relation called `<=o`.

```clojure
(defn <=o [x y]
  "x and y are natural numbers, such that x is less than or
  equal to y"
  (matche [x y]
          ([zero _] (natural-number y))
          ([(s ?x) (s ?y)] (<=o ?x ?y))))
```

What can this relation do?

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (<=o one two))
(_.0)
logic-introduction.numbers=> (run 1 [q]
                                  (<=o two one))
()
logic-introduction.numbers=> (run 1 [q]
                                  (<=o one q))
((0))
logic-introduction.numbers=> (run 4 [q]
                                  (<=o one q))
((0) ((0)) (((0))) ((((0)))))
logic-introduction.numbers=> (run* [q]
                                   (<=o q two))
(0 (0) ((0)))
```

What are the constraints to satisfy this relation?

```clojure
"x and y are natural numbers, such that x is less than or equal to y"
```

We read this "one and two are natural numbers, such that one is less than or equal to two".

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (<=o one two))
(_.0)
```

The relation is satisfied.

`one` is less than or equal to `q`, and retrieve 4 results.

```clojure
logic-introduction.numbers=> (run 4 [q]
                                  (<=o one q))
((0) ((0)) (((0))) ((((0)))))
```

One, and the next three numbers after one each satisfy this constraint.

`q` is less than or equal to `two`, and collect all results. 

```clojure
logic-introduction.numbers=> (run* [q]
                                   (<=o q two))
(0 (0) ((0)))
```

There are only 3 numbers less than or equal to `two`.

___


Let's define a relation called `plus`.

```clojure
(defn plus [x y z]
  "x, y, and z are natural numbers such that z is the sum of
  x and y"
  (matche [x y z]
          ([zero ?x ?x] (natural-number ?x))
          ([(s ?x) _ (s ?z)] (plus ?x y ?z))))
```

What can this relation do?

```clojure
logic-introduction.numbers=> (run 1 [q]
                                   (plus one two three))
(_.0)
logic-introduction.numbers=> (run 1 [q]
                                  (plus one q two))
((0))
logic-introduction.numbers=> (run 1 [q]
                                  (plus q q two))
((0))
logic-introduction.numbers=> (run 3 [q]
                                  (plus q zero q))
(0 (0) ((0)))
logic-introduction.numbers=> (run 3 [q]
                                  (fresh [x y z]
                                         (== q [x y z])
                                         (plus x y z)))
([0 0 0] [0 (0) (0)] [0 ((0)) ((0))])
```

What are the constraints to satisfy this relation?

```clojure
"x, y, and z are natural numbers such that z is the sum of x and y"
```

`three` is the sum of `one` and `two`.

```.clojure
logic-introduction.numbers=> (run 1 [q]
                                   (plus one two three))
(_.0)
```

The relation is satisfied.

`three` is the sum of `one` and `q`.

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (plus one q three))
(((0)))
```

Equivalently, `one` is the subtraction of `q` from `three`.

___

Exercise 1: Implement the relation `subtract` in terms of `plus` with this constraint:

```clojure
"x, y, and z are natural numbers such that z is the subtraction of y from x"
```

___

Let's define a relation called `times`.

```clojure
(defn times [x y z]
  "x, y, and z are natural numbers such that z is the product
  of x and y"
  (matche [x y z]
          ([zero _ zero])
          ([(s ?x) _ _] (fresh [xy]
                               (times ?x y xy)
                               (plus xy y z)))))
```

What are the constraints to satisfy this relation?

```clojure
"x, y, and z are natural numbers such that z is the product of x and y"
```

What can this relation do?

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (times one three three))
(_.0)
logic-introduction.numbers=> (run 1 [q]
                                  (times one three q))
((((0))))
logic-introduction.numbers=> (run 2 [q]
                                  (times q q q))
(0 (0))
logic-introduction.numbers=> (run 1 [q]
                                  (times two q six))
((((0))))
```

`six` is the product of `two` and `q`.

```clojure
logic-introduction.numbers=> (run 1 [q]
                                  (times two q six))
((((0))))
```

Equivalently, `q` is the result of `six` divided by `two`.

___

Exercise 2: Implement the relation `divide` in terms of `times` with this constraint:

```clojure
"x, y, and z are natural numbers such that z is the result of x divided by y"
```

___
