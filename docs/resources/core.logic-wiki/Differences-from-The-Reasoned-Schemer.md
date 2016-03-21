The following are the most important differences from the version of miniKanren described in The Reasoned Schemer (TRS).

* **#s** is **s#**
* **#u** is **u#**
* Clojure core.logic's **conde** is actually the book's **condi**. Core.logic offers no **conde** as is presented in the book. This means the order of results may not match what is shown in the book when you use **conde**.
* **conde** does not support defining an **else** clause. Just use a **(s# ...)** at the end of your **conde**.
* Clojure has no way to create pairs (sequences with improper tails). The core.logic **lcons** constructor fn provides this behavior. **llist** is a convenience macro that expands out into nested **lcons** expressions.
* **nullo** is **emptyo**
* **nilo** unifies with nil
* **caro** is **firsto**
* **cdro** is **resto**

For example TRS 2-52 (Chapter 2, #52) is written like so in Scheme:

```scheme
(run #f (r)
  (fresh (x y)
    (== (cons x (cons y 'salad)) r)))
```

It can be written like this in core.logic:

```clj
(run* [r]
  (fresh [x y]
    (== (lcons x (lcons y 'salad)) r)))
```

TRS 3-10 is written like so in Scheme:

```scheme
(run 1 (x)
  (listo `(a b c . ,x)))
```

Can be written like this in core.logic:

```clj
(run 1 [x]
  (listo (llist 'a 'b 'c x)))
```

There is no predicate **pair?**, however you can provide one yourself with the following:

```clj
(defn pair? [x]
   (or (lcons? x) (and (coll? x) (seq x))))
```

Related, implementing **list?** as shown in TRS 3-1 is unnecessary. **seq?** is more appropriate in Clojure. This is because proper list-like things and pairs are not conflated in Clojure as they are in Scheme. In general you should not use vectors when working through TRS. Use **list** or a quoted **list**. TRS examples that use Scheme **quasiquote** will need to written like so (TRS 3-7):

```clj
(run* [x]
   (listo (list 'a 'b x 'd)))
```
