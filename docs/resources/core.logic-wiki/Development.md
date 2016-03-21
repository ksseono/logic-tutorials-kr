## Roadmap

### Immediate
* [[Improving the Constraint Framework]]
* [[External Solvers]]
* [[Better Syntax Support]]
* [[Polymorphic Constraints]]
* [[Trusted Constraints]]
* [[Search Customization & Instrumentation]]
* [[Constraints & Modes]]
* [[Entanglement]]
* [[Tabling]]
* [[Optimizations]]
* [[CLP(Set)]]
* [[CLP(Prob)]]

### Interesting Branches
* Parallel Execution, [fork-join](http://github.com/clojure/core.logic/tree/fork-join)
  more information [here](http://www.clojure.net/2012/03/26/Messin-with-core.logic/)
* Fair Conjunction, [fair-conj2](http://github.com/clojure/core.logic/tree/fair-conj2)
  more information [here](http://clj-me.cgrand.net/2012/04/06/fair-conjunction-status-report/)

### Future
* **Environment Trimming**
Definite Clause Grammars (DCGs) are quite slow in miniKanren. This may
be due to a lack of groundness analysis or it may be because we are
not trimming the environment of needless logic variables. It looks
like the original Kanren paper may have some good approaches.
* **Groundness Analysis**
Initial research on feasibility done. It does in fact give significant
performance boosts (2-3X). Seems to close many performance gaps
between SWI-Prolog and miniKanren. However maintaining correctness
seems difficult. Perhaps limit optimization to DCGs and pattern
matching sugar. Again, the original Kanren paper may have insights
here

## Issues & Contributing

Please open tickets using [JIRA](http://dev.clojure.org/jira/browse/LOGIC)

If you would like to contribute submit your [Contributor Agreement](http://clojure.org/contributing).
