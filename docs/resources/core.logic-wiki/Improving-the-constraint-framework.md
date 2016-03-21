Now that we've lived with the constraint framework for eight months
there are some clearer paths towards improvement.

## root-var

Now that the improved constraint framework and its performance
benefits are landed in master we should take a closer look at
`root-var`. YourKit consistently shows `root-var` and `root-val` as
the worst offenders with respect to "Own Time". The reason we need
`root-var` is because of var aliasing and `root-val` is needed because
of how we store domains.

However we've already gone down the path of "rooting" all constraint
parameters in `addcg`. Perhaps `cgoal` could do this step instead and
construct the real constraint with only roots. This means that
anything on the constraint call paths will be guaranteed to be a root
making calls calls to `root-var` unnecessary.

One issue is this optimization may make the most sense for CLP(FD)
where the constraint parameters are not complex terms. But perhaps
not? Tree constraints tend to recursive apply the constraint to
sub terms instead of.

## Disjunctive Constraints

Disequality is a disjunctive constraint. Currently our disequality
implementation involves some ugliness and inefficiency because we
cannot express disjunctive constraints in the current framework - only
conjuctive constraints. We would like to express that some constraint
is satisfied if even one of its subconstraints is satisfied. At the
same time we would still like to reify disequality constraints in a
straightforward manner.

## Subsumed Constraints

If we gave each constraint an identifier we could know if a constraint
is already in the constraint store.

``` (+fd x y z) ;; identifier [+fd x y z] ```

`+fd` here is a symbol and `x`, `y`, `z` are root vars. The constraint
store could have a new field which simply stores this representation
in a Clojure set.

Perhaps this system is good enough for disequality constraint
subsumption since we'll be storing the subconstraints? If so we could
avoid normalizing disequality constraints in the constraint store.
