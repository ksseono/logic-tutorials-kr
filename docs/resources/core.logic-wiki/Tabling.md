The tabling implementation could use some attending to. Currently the tabling implementation generates a lot of redundant information instead of sharing information across tabled calls. For example currently after some calls to a tabled goal - the goal table looks like this:

```
t = { arg: #{ans0, ans1, ans2, ..., ansN}
     ans0: #{ans1, ans2, ..., ansN}
     ans1: #{ans2, ..., ansN}
     ... }
```

Yuck! :)

It maybe the case that tabling interacts oddly with constraints. In Nada Amin's [TAPL repo](http://github.com/namin/TAPL-in-miniKanren-cKanren-core.logic), the tabled reducer shows the correct results but also shows some spurious contraints. We should investigate.
