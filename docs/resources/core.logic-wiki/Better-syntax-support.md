Clojure syntax-quote leaves much to be desired when doing the symbolic
manipulations that are common with the nominal functionality of
core.logic. For example consider the following alphaProlog fragment
program for describing the operational semantics of the pi-calculus:

```prolog
step (tau P) tau_a P.
step (sum (P, Q)) A (P') :- step P A P'.
step (sum (P, Q)) A (Q') :- step Q A Q'.
step (par (P, Q)) A (par (P', Q)) :- step P A P'.
step (par (P, Q)) A (par (P, Q')) :- step Q A Q'.
step (par (ina,P)) tau_a P.
step (par (P,ina)) tau_a P.
step (par (P,Q)) tau_a (res (n\par(P',Q'))) 
	:- step' P (n\in_a(X,n)) (n\P'), step' Q (n\out_a(X,n)) (n\Q').
step (par (P,Q)) tau_a (res (n\par(P',Q'))) 
	:- step' P (n\out_a(X,n)) (n\P'), step' Q (n\in_a(X,n)) (n\Q').

step (res (n\P)) A (res (n\P')) :- step P A P', n # A.
step (out (X, Y, P)) (out_a (X, Y)) P.
step (par (P,Q)) tau_a (par (P'',Q'))  :- 
	step' P (y\in_a(X,Y)) (y\P'), 
	step Q (out_a(X,Z)) Q', 
	rename (y\P',Z,P'').
step (par (P,Q)) tau_a (par (P',Q''))  :- 
	step' Q (y\in_a(X,y)) (y\Q'), 
	step P (out_a(X,Z)) P', 
	rename (y\Q',Z,Q'').

step (rep P) A P' :- step (par (P, (rep P))) A P'.
```

This is 21 lines of code not counting the blank lines.

Nada Amin's translation to core.logic currently looks like the
following

```clojure
(defn stepo [p a q]
  (conde
    [(== `(~'tau ~q) p)
     (== a 'tau_a)]
    [(fresh [p1 p2]
       (== `(~'sum ~p1 ~p2) p)
       (stepo p1 a q))]
    [(fresh [p1 p2]
       (== `(~'sum ~p1 ~p2) p)
       (stepo p2 a q))]
    [(fresh [p1 p2 q1]
       (== `(~'par ~p1 ~p2) p)
       (== `(~'par ~q1 ~p2) q)
       (stepo p1 a q1))]
    [(fresh [p1 p2 q2]
       (== `(~'par ~p1 ~p2) p)
       (== `(~'par ~p1 ~q2) q)
       (stepo p2 a q2))]
    [(== `(~'par ~'ina ~q) p)
     (== a 'tau_a)]
    [(== `(~'par ~q ~'ina) p)
     (== a 'tau_a)]
    [(fresh [p1 p2 q1 q2 x]
       (nom/fresh [n]
         (== `(~'par ~p1 ~p2) p)
         (== a 'tau_a)
         (== `(~'res ~(nom/tie n `(~'par ~q1 ~q2))) q)
         (b-stepo p1 (nom/tie n `(~'in_a ~x ~n)) (nom/tie n q1))
         (b-stepo p2 (nom/tie n `(~'out_a ~x ~n)) (nom/tie n q2))))]
    [(fresh [p1 p2 q1 q2 x]
       (nom/fresh [n]
         (== `(~'par ~p1 ~p2) p)
         (== a 'tau_a)
         (== `(~'res ~(nom/tie n `(~'par ~q1 ~q2))) q)
         (b-stepo p1 (nom/tie n `(~'out_a ~x ~n)) (nom/tie n q1))
         (b-stepo p2 (nom/tie n `(~'in_a ~x ~n)) (nom/tie n q2))))]
    [(fresh [p0 q0]
       (nom/fresh [n]
         (== `(~'res ~(nom/tie n p0)) p)
         (== `(~'res ~(nom/tie n q0)) q)
         (stepo p0 a q0)
         (nom/hash n a)))]
    [(fresh [x y]
       (== `(~'out ~x ~y ~q) p)
       (== a `(~'out_a ~x ~y)))]
    [(fresh [p1 p2 q1 q2 r1 x y z]
       (nom/fresh [y]
         (== `(~'par ~p1 ~p2) p)
         (== a 'tau_a)
         (== `(~'par ~r1 ~q1) q)
         (b-stepo p1 (nom/tie y `(~'in_a ~x ~y)) (nom/tie y q1))
         (stepo p2 `(~'out_a ~x ~z) q2)
         (renameo y z q1 r1)))]
    [(fresh [p1 p2 q1 q2 r2 x z]
       (nom/fresh [y]
         (== `(~'par ~p1 ~p2) p)
         (== a 'tau_a)
         (== `(~'par ~q1 ~r2) q)
         (b-stepo p2 (nom/tie y `(~'in_a ~x ~y)) (nom/tie y q2))
         (stepo p1 `(~'out_a ~x ~z) q1)
         (renameo y z q2 r2)))]))
```

This is 60 lines of code, nearly 3X more than alphaProlog!

While it can be partially cleaned up with the `defne` pattern matching
sugar, something more is necessary. We should perhaps provide a
`letsyn` macro which delivers the intelligent handling of unquoted
vars. Both `letfn` and `defne` should support simple extensible
parsing.

A cleaned up pi-calculus step relation might look something like the
following:

```clojure
(defne stepo [p a q]
  (['(tau ~q) 'tau_a _])
  (['(sum ~p1 ~p2)] (stepo p1 a q))
  (['(par ~p1 ~p2)] (stepo p2 a q))
  (['(par ~p1 ~p2) _ '(par ~q1 ~p2)] (stepo p1 a q1))
  (['(par ~p1 ~p2) _ '(par ~p1 ~q2)] (stepo p2 a q2))
  (['(par ina ~q) 'tau_a _])
  (['(par ~q ina) 'tau_a _])
  (['(par ~p1 ~p2) 'tau_a '(res (\n n (par ~q1 ~q2)))]
     (letsyn [t0 (\ n (in_a ~x ~n))
              t1 (\ n ~q1)
              t2 (\ n (out_a ~x ~n))
              t3 (\ n ~q2)]
       (stepo' p1 t0 t1)
       (stepo' p2 t2 t3)))
  (['(par ~p1 ~p2) 'tau_a '(res (\ n (par ~q1 ~q2)))]
     (letsyn [t0 (\ n (out_a ~x ~n))
              t1 (\ n ~q1)
              t2 (\ n (in_a ~x ~n))
              t3 (\ n ~q2)]
       (stepo' p1 t0 t1)
       (stepo' p2 t2 t3)))
  (['(res (nom/tie n p0)) _ '(res (\ n q0))]
     (stepo p0 a q0)
     (nom/hash n a))
  (['(out ~x ~y ~q) '(out_a ~x ~y) _])
  (['(par ~p1 ~p2) 'tau_a '(par ~r1 ~q1)]
     (letsyn [t0 (\ y (in_a ~x ~y))
              t1 (\ y ~q1)
              t2 (out_a ~x ~z)]
       (stepo' p1 t0 t1)
       (stepo p2 t2 q2)
       (renameo y z q1 r1)))
  (['(par ~p1 ~p2) 'tau_a '(par ~q1 ~r2)]
     (letsyn [t0 (\ y (in_a ~x ~y))
              t1 (\ y ~q2)
              t2 (out_a ~x ~z)]
       (stepo' p2 t0 t1)
       (stepo p1 t2 q1)
       (renameo y z q2 r2))))
```

We're down to 39 lines of code, so about twice as verbose as the alphaProlog.

An even more radical step would be to support a preprocess phase on
the body of each clause:

```clojure
(defne stepo [p a q]
  (['(tau ~q) 'tau_a _])
  (['(sum ~p1 ~p2)] (stepo p1 a q))
  (['(par ~p1 ~p2)] (stepo p2 a q))
  (['(par ~p1 ~p2) _ '(par ~q1 ~p2)] (stepo p1 a q1))
  (['(par ~p1 ~p2) _ '(par ~p1 ~q2)] (stepo p2 a q2))
  (['(par ina ~q) 'tau_a _])
  (['(par ~q ina) 'tau_a _])
  (['(par ~p1 ~p2) 'tau_a '(res (nom/tie n (par ~q1 ~q2)))]
   (stepo' p1 (nom/tie n (in_a ~x ~n)) (nom/tie n ~q1))
   (stepo' p2 (nom/tie n (out_a ~x ~n)) (nom/tie n ~q2)))
  (['(par ~p1 ~p2) 'tau_a '(res (nom/tie n (par ~q1 ~q2)))]
   (stepo' p1 (nom/tie n (out_a ~x ~n)) (nom/tie n ~q1))
   (stepo' p2 (nom/tie n (in_a ~x ~n)) (nom/tie n ~q2)))
  (['(res (nom/tie n p0)) _ '(res (nom/tie n q0))]
   (stepo p0 a q0)
   (nom/hash n a))
  (['(out ~x ~y ~q) '(out_a ~x ~y) _])
  (['(par ~p1 ~p2) 'tau_a '(par ~r1 ~q1)]
   (stepo' p1 (nom/tie y (in_a ~x ~y)) (nom/tie y ~q1))
   (stepo p2 (out_a ~x ~z) q2)
   (renameo y z q1 r1))
  (['(par ~p1 ~p2) 'tau_a '(par ~q1 ~r2)]
   (stepo' p2 (nom/tie y (in_a ~x ~y)) (nom/tie y ~q2))
   (stepo p1 (out_a ~x ~z) q1)
   (renameo y z q2 r2)))
```

This is 25 lines of code, only a little bit more verbose than
alphaProlog. Because we want support multiple preprocessors, we can't
use a short name like `\` for `nom/tie`. A preprocessing pass seems
more appealing then `letsyn` at the moment.

What follows is a sketch of what the preprocessor api and extension
might look like:

```clojure
(defmethod preprocess 'unquote
  [env sym]
  {:env (update-in env [:scope :fresh] conj nom)
   :form sym})

(defmethod preprocess :default
  [env [x & xs]]
  {:env env
   :form `('~x ~@(map preprocess xs))})

(defmethod preprocess 'nom/tie
  [env [nom body]]
  {:env (update-in env [:noms] conj nom)
   :form `(nom/tie ~nom ~(preprocess body)})

(defmethod emit-scope [:scope :fresh]
  [vs body]
  `(clojure.core.logic/fresh [~@vs]
     ~body))

(defmethod emit-scope [:scope :nom]
  [vs body]
  `(clojure.core.logic.nominal/fresh [~@vs]
     ~body))
```

As pointed out by Nada Amin here's another nice enhancement. In the
alphaProlog code it's not necessary to name parameters.

```prolog
step (sum (P, Q)) A (P') :- step P A P'.
```

And this translates to:

```clj
(defne stepo [p a q]
  ... 
  (['(sum ~p1 ~p2)] (stepo p1 a q))
  ...)
```

But if `defne` supported dropping the vector we could do something like this:

```
(defne stepo
  ...
  (['(sum ~p ~q) a p'] (stepo p a p'))
  ...)
```

We could make this work by inferring the argument vector from the patterns.
