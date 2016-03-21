core.logic unfortunately to this day continues to emphasize operating on sequence-like data structures due to its Prolog & Scheme roots. This often leads to much worse performance than one would like. Now that we have constraints it should be possible to write constraint versions of Clojure functions.

Currently we have a pretty useless experimental `defc` in the master branch. It requires all arguments to be completely ground before it executes. What about a `defc` with support for "mode" declarations?

```clj
(defc assocc
  [^partial m ^ground k v o]
  ...
  [m ^ground k v ^partial o]
  ...)
```

The body of the constraint would run when one of the modes have been satisfied. We could probably use the mode information to automatically generate the body of the `IRunnable` implementation.

### Complications

It turns out that implementing assoc isn't quite as simple as it sounds. When going in "reverse" there actually multiple possibilities if the first argument to `assocc` is fresh. This leads me to think a better test of this functionality would be a proper implementation of `conjo` which would be polymorphic on its input and output arguments.

Imagine the following where `v` is fresh and `o` is a bound to a map.

```clj
(run* [q]
  (fresh [v o]
    (== o {:foo 1})
    (conjo q [:foo v] o)))
```

There are two possiblities, either `q` is a map that doesn't include the entry, or it had the entry for some yet unknown value. So we should see the following results:

```clj
({} {:foo _0})
```

We now have to deal with precisely the same issues that are present in CLP(Set).
