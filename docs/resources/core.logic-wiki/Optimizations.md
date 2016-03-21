There are many optimizations I have in mind that we haven't had time to investigate.

### Pattern Matching

In @timowest's [symbol repo](http://github.com/timowest/symbol) we have the following unfortunate bit of code:

```clj
(defnu typedo
  [env form new-env]
  ([_ nil _] (== env new-env))
  ([_ ['ns* ?name] _] (== env new-env))
  ([_ ['comment . _] _] (== env new-env))
  ([_ ['if . _] _] (ifo env form new-env))
  ([_ ['fn* . _] _] (fno env form new-env))
  ([_ ['let* . _] _] (leto env form new-env))
  ([_ ['loop* . _] _] (loopo env form new-env))
  ([_ ['recur* . _] _] (recuro env form new-env))
  ([_ [?dot . _] _] (== ?dot '.) (dot env form new-env))
  ([_ ['new . _] _] (newo env form new-env))
  ([_ ['def . _] _] (defo env form new-env))
  ([_ ['do . _] _] (doo env form new-env))
  ([_ [?fn . _] _] (applyo env form new-env))
  ([_ ['include . _] _] (includeo env form new-env))
  ([_ ['array . _] _] (arrayo env form new-env))
  ([_ ['struct . _] _] (structo env form new-env))
  ([_ _ _] (condu ((fresh [type] 
                         (membero [form type] env)
                         (== env new-env)))
                  ((annotatedo env form new-env))
                  ((literalo env form new-env))))) 
```

Pruning is used here for performance - this is because only one branch needs to match. But this would be unnecessary if `defne/a/u` intelligently indexed their clauses. We should extend those macros to perform an analysis step so that at runtime if we have a (partially) ground term we can make a very efficent call and ignore the other branches.

### Constraints

Early performance analysis of the cKanren work revealed that we spend a lot of time looking for root vars. In particular where this hurts is when we run a constraint. A constraint when run will involve a call to `runnable?` as well the actually invocation, and finally a call to `relevant?`. All three of these operations will involve walking vars and domains over and over again. While from a design perspective it's nice to have these concerns separate, it's a lot of redundant work.

Perhaps it's worth investigating an optimization for people who understand how the cKanren architecture works -provide caching versions of `IRunnable`, `IRelevant`, and constraint invocation. These protocols would look identical to their non-caching counterparts with the addition of a cache argument - a mutable array or something like that. Then in the body of these implementations, implementers can just grab the relevant walked/rooted values/domains etc. immediately out of the cache.
