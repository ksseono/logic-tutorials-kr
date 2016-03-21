## Differences from core.logic in Clojure

Basic core.logic works with ClojureScript. Some things that work in Clojure have not yet been implemented for ClojureScript including the following:

* disequality constraints
* tabling
* simple unifier

## Using core.logic from ClojureScript

Using core.logic from ClojureScript requires doing a little bit more work as there are a large number of macros in addition to runtime functionality.

```clojure
(ns example
  (:require [cljs.core.logic :as m :refer [membero]]))

(m/run* [q]
  (membero q '(:cat :dog :bird :bat :debra)))
```