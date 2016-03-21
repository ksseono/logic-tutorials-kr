CLP(Set) would be a welcome additions to the constraint solvers provided out of the box. It would remove the need for inefficient usage of `membero` you see so often in core.logic code.

CLP(Set) for completely ground sets is not difficult and could be accomplished easily via [[Constraints & Modes]]. However CLP(Set) which allows for partially instantiated sets would be extremly powerful and could support writing some very useful source analysis tools.

Imagine the following code:

```clj
(unionc seta setb oset)
```

If `seta` and `setb` are completely ground there's nothing more to do. But let's imagine `seta` and `setb` look like the following:

```
seta = #{:cat x y}
setb = #{:dog :bird z}
```

Here we have three fresh vars. There are some things we know immediately:

* x & y must be distinct from `:cat` and each other (`distinctfd` constraint)
* z must be distinct from `:dog` and `:bird`

`oset` can be of size 3, 4, 5 or 6.

These possibilities look something like this:

```
#{:cat :dog :bird}
#{:cat :dog :bird x}
#{:cat :dog :bird y}
#{:cat :dog :bird z}
#{:cat :dog :bird x y}
#{:cat :dog :bird x z}
#{:cat :dog :bird y z}
#{:cat :dob :bird x y z}
```

So if `oset` ever unifies with a set larger than count 6 or smaller than count 3 we can immediately fail. If `oset` ever unifies with a set of specific size we can discard the other possibilities which are not of that size. Where complications arise is that these possibilities alone are not enough, each possibility comes along with a permutation of variable assignments.

For example for the first case we have the following permutations of variable assignments:

```
x = :dog,  y = :bird, z = :cat
x = :dog,  y = :cat,  z = :bird
x = :bird, y = :dog,  z = :cat
x = :bird, y = :cat,  z = :dog
x = :cat,  y = :dog,  z = :bird
x = :cat,  y = :bird, z = :dog
```

We are already getting a taste of how this can get exponential. It seems though in general as the set size becomes larger the permutations are smaller, for example in the case of `#{:cat :dog :bird x y}` we know `z` must be `:cat`.

Still this is work we do not want to do unless we must. So we can take a page out of cKanren's book - we don't actually enumerate these possibilities until right before reification, by delaying this process we hope that we can constraint `oset` enough to eliminate pointless permutations.

There are many ways to constrain `oset`. If any of the involved vars becomes ground we know several things - a more refined notion of the set size, and which possibilities and permutations do not apply.

So we could imagine a series of new domain types to support CLP(Set), similar to how CLP(FD) has a series of data types. These domain types could store the size of the set as well as the remaining vars involved in permutations.

Note we could imagine the most complex case - the cartesian product of two these new set domain types, i.e.:

```clj
(unionc seta setb setc)
```

Where `seta` and `setb` are not ground and already have set domains. This will be slow if `setc` reaches reification without `seta` or `setb` becoming ground - and that should be expected.

## Unification

Consider `oset` from above and the following unification where `p` and `q` are fresh and not equal to `x`, `y`, or `z`:

```clj
(== oset {:cat p q})
```

It's clear only the first case applies, the set of size 3. The most efficient thing to do would be to give `p` and `p` delayed domains, as soon as `oset` becomes ground, `p` and `q` take on the values in `oset` except `:cat`. Again we have implicit `distinctfd` consraint betweent `p` and `q`.

Note this means we don't actually implement set unification at all. Rather unification will trigger a constraint on `oset` to which will add a constraint on `p` and `q` that will give them domains of possible values from `oset` excluding `:cat`.

This means using the above to check for set membership will be an anti-pattern. If a user wants to check for the existing of the item `:cat` they should use the much more efficient `memberc` constraint:

```clj
(memberc oset :cat)
```

## Difference & Intersection

Possibilities based on the above example are the following + variable assignment permutations:

```
#{}
#{x}
#{y}
#{x y}
#{:cat}
#{:cat x}
#{:cat y}
#{:cat x y}
```

## CLP(FD) & CLP(Set) Implementation correlation

It seems there will be some deep correlations between CLP(Set) & CLP(FD) at least as far as the nature of the types involved. For example in CLP(FD) we have notion of a singleton domain, i.e. `1`. But this is also the case for a single Clojure set!, it is kind of like a "singleton" set domain, there's only one possibility.

Then we have the more complex set domain type, this is like `IntervalFD`. And finally we have the cartesian products which seems to correspond very closely to `MultiIntervalFD`.

We could easily imagine a unification with a cartesian product of two set domains to propagate size information to the underlying sets. There probably other propagation opportunities that escape me at the moment.
