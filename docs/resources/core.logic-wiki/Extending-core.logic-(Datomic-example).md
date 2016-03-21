## Unifying Custom Data Structures

core.logic was designed with extensibility in mind. You may want to unify your own custom data structures - not only the ones provided by Clojure. To see how this can be done we'll use Datomic datums as an example.

For the following examples we assume you have created a Datomic test db and the following ns declaration:

```clojure
(ns example
  (:use [datomic.api :only [db q] :as d])
  (:use [clojure.core.logic]))

(def uri "datomic:dev://localhost:4334/test")
(def conn (d/connect uri))
```

In order for your custom data structure to participate in unification it must implement <code>IUnifyTerms</code>. 

We can implement <code>IUnifyTerms</code> For Datomic datums like so:

```clojure
   (extend-type datomic.Datom
     clojure.core.logic.protocols.IUnifyTerms
     (unify-terms [u v s]
       (unify-with-datom* u v s)))
```

<code>u</code> is of course the datum, <code>v</code> is other data structure being unified with and <code>s</code> is the substitutions map.

This code simply calls the actual function that we will define later. A separate function is used so that other types can reuse it to unify with Datoms without the overhead of double dispatch.

We would like unification to possibly succeed with instances of <code>clojure.lang.PersistentVector</code>. This is because datums are 4 element tuples. We now implement a simple function that does this. Again returning <code>false</code>(0.7.5) or <code>nil</code>(>= 0.8.0) for failed unification. 

```clojure
   (defn unify-with-datom* [u v s]
     (when (and (instance? clojure.lang.PersistentVector v) (> (count v) 1))
       (loop [i 0 v v s s]
         (if (empty? v)
           s
           (when-let [s (unify s (first v) (nth u i))]
             (recur (inc i) (next v) s))))))
```

<code>v</code> will be an instance of <code>clojure.lang.PersistentVector</code>, <code>u</code> will be the datum and <code>s</code> will be the current substitutions map. It should be clear here that datums may only unify with instances of <code>clojure.lang.PersistentVector</code> containing only 4 elements. This code simply unifies each element in the current substition - creating a new substitution which _must_ be used for the next unification attempt. If all the elements of <code>v</code> and <code>u</code> unify, we return the new (possibly changed) substitutions map.

Unification is a binary operation - we must handle the possibility of a datum appearing as the left or right operand. Note the use of <code>unify-with-sequential*</code> from the <code>clojure.core.logic</code>.

```clojure
   (extend-type clojure.lang.PersistentVector
     IUnifyTerms
     (unify-terms [u v s]
       (if (datom? v)
         (unify-with-datom* v u s)
         (when (sequential? v)
           (unify-with-sequential* u v s)))))
```

There is nothing more to do. Datums can now participate in unification.

## Custom Data Sources

In order to write any interesting relational programs against Datomic we need to be able to unify with external data sources. This can be accomplished with <code>to-stream</code>.

```clojure
(defn datomic-rel [q]
  (fn [a]
    (l/to-stream
      (map #(l/unify a % q) (d/datoms (db conn) :eavt)))))
```

<code>to-stream</code> creates a stream of choices suitable for core.logic's operation. It can take any seqable data structure. In this case we simply read out raw index data from Datomic. We unify each datum with the closed over argument <code>q</code>. This is important - core.logic relations are just closures. They must return a function that takes a single argument (in this case <code>a</code>) which is the current substitution.

You can now run core.logic queries against Datomic:

```clojure
(run* [q]
  (fresh [e a v t]
    (== v true)
    (datomic-rel [e a v t])
    (== q [e a v t])))
```