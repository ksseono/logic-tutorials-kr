The miniKanren folks are working on two new constraints `presento` and
`absento`. One interesting thing that has come up in the discussions
about their semantics is whether miniKanren (and of course core.logic)
should produce answers if there is no possibility of satisifying the
constraints.

For example consider `boolo` which constrains a value to be a
boolean. This seems innocuous on the surface but consider the
following program:

```clj
(run* [q]
  (boolo q)
  (!= q true)
  (!= q false))
;; => ((_0 ((_0 :- (boolo _0) (!= _0 true) (!= _0 false)))))
```

If `boolo` is implemented as a predicate constraint this program will
have unsound behavior! The user will get an answer that says `q` is
boolean that is not `true` or `false`. This is of course impossible, yet
core.logic will say that some answer exists as long `q` is not `true` and
not `false`. Reified constraints make this seem ok since in some sense
reified constraints are meant to be to be verified by the user, but
now consider this program:

```clj
(run* [q]
  (fresh [x]
    (boolo x)
    (!= x true)
    (!= x false)))
;; => (_0)
```

This is considerably more troubling. `x` is not part of the answer and
the miniKanren program says that some value for `q` exists such that
this program is satisfiable.

While this issue may not be a big deal for many applications - there
are applications where this will be unacceptable. It may make sense
for core.logic to support the notion of a trusted constraint set. In
this case `boolo` will not belong to the trusted constraint set and
the program will fail immediately. core.logic or someone may write a
`boolo` that correctly treats `x`'s value as a finite domain and if
the user trusts this, we can perhaps provide a hook so users can
specify which constraints they trust.

presento
----

`presento` provides some interesting challenges as far as trusted
constraint go. `presento` is used to determine whether a particular
term appears in some other term. Both parameters to `presento` may be
fresh.

```clj
(run* [q]
  (presento :a '(:a :b :c)))
;; => (_0)
```

But consider the following program:

```clj
(run* [q]
  (fresh [a b c d
          l x y z]
    (distincto [a b c d])
    (= l [x y z])
    (presento a l)
    (presento b l)
    (presento c l)
    (presento d l)))
```

Upon closer inspection if the user states that `a`, `b`, `c`, and `d`
are all symbols via a constraint like `symbolo` this should fail! This
is because the list can only contain 3 values, not 4. However as long
as one of terms in `l` is a tree term than this may be satisfiable.

`presento` needs to carefully watch changes (including constraints!)
on all the terms involved. Given this can `presento` be made
efficient?
