The entanglement implementation in master is exponential since we
store etangled var set (set) information directly on the var. So if
you have 9 vars in an eset one call to `update-dom` will trigger a lot
of unnecessary computation.

A better implementation would store entangled var sets elsewhere. Then
we have to consider data structure representation. We can't quite do
what we do with the constraint store because if two vars that belong
to two different esets become entangled we need to merge the esets
together. However we can make it work so that we never do more than
two lookups.

Consider the following:

```clj
{a 0 b 0 c 0
 x 1 y 1 z 1}
```

Where `0` points to the eset `#{a b c}` and 1 points to the eset `#{x
y z}`. Now imagine that `a` and `x` become entangled. We can merge the
two esets and add a new entry `2`. `0` will now point to `2` and `1`
will also point to `2`.

We could imagine another eset `#{p q r}` where the vars point to
`3`. If `p` becomes entangled with `a` then we just need to merge the
contents of `2` with `3` and add a new entry `4`. We can then delete
`2` and the eset stored there and repoint `0` to `4` and `1` to `4`
and `3` to `4`.

The cost of updates grow with the number of esets that must be
merged. One way to prevent merging of esets early on is to have a
threshold, say 8. Prior to this threshold we simply update the
individual vars.
