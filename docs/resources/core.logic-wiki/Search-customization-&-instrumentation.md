Currently if you want search different from miniKanren search, you're out of luck. Jamie Brandon has done some work to make this more flexible but I think we can take it a step further.

The key insight is that search is completely driven by data structures. The problem is that what data structures we produce is hard coded to a particular type, `Substitution`. Instead `Substitution` should contain a new field, call it `st` for "strategy" which is an `ISearch` (a protocol) instance. We could imagine that `ISearch` looks something like the following:

```clj
(defprotocol ISearch
  (conjunct [_])
  (disjunct [_])
  (commit [_])
  (cut [_]))
```

We should of course be skeptical of `commit` and `cut`, perhaps  better to put those in a separate protocol.

```clj
(defprotocl IMKSearch
  (commit [_])
  (cut [_]))
```

Eventually we should see if these could be constructed on lower level primitives such as described in [Search Combinators](http://arxiv.org/abs/1203.1095).

Going down this route would also free us to experiment with instrumenting search for debugging, performance analysis, etc. This could be done cleanly without mucking around with the basic search behavior.

The main thing to be concerned with here is degrading search perfomance, but seeing as it's only one more inline protocol dispatch I'm pretty confident that this can perform well.

## Implementation Ideas

We will leave `ITake` mostly alone for now. We want search to be pluggable - we remove the monadic terminology from the concrete types. `IConjunct`, `IDisjunct` and `-conjuct`, `-disjunct` might be better names. For now it's probably ok if we implement `IMKPrune` which will consist of `-commit` and `-cut`. Obviously then every data structure related to search will need to have strategy information. So, we probably need a real failure type `Fail`. This opens the door for a search strategy which does not discard failures!

`Fail` would contain the failed substitution.

I believe it's enough if the substitution has a strategy which is delegated to - this strategy will return data structures `Choice`, `Fail` which themselves will implement the protocols. One question is thunks. Perhaps we should return these as `Step`?

Then say we want to debug failures. We would create a strategy which does not eliminate them.

```clj
(-conjuct a-failure g)
```

`a-failure` would have a substitution which has the strategy, and this strategy would simply pass the failure along. The implementation for `Fail` might look something like this:

```clj
(-conjunct (:st a-failure) a-failure)
```

This way people can reuse `Fail`. The story is similar for `Choice` and `Step`. Then an type implementing the search protocols looks like something like this:

```clj
(deftype MKSearch []
  IConjunct
  (-conjuct [_ x]
    (cond
      (fail? x) ...
      (unit? x) ...
      (step? x) ...
      (choice? x) ...)))  
```

Perhaps a more flexible / efficient approach is for each search strategy to always provide its own types. miniKanren search would involve a very small number of types and implementations so perhaps it's not a big deal.

Looking again at [Search Combinators](http://arxiv.org/abs/1203.1095), the paper now seems more relevant for the CLP(FD) portion of core.logic. For better or worse we're married to the Prolog w/ rich constraints approach
