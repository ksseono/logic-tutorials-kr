[JaCoP](http://jacop.osolpro.com) is a finite domain solver written in
pure Java that has been in continuous development since 2001. In the
yearly [MiniZinc](http://www.minizinc.org) constraint challenges it
has received the Silver award in the fixed category for the past three
years.

Some basic testing seems to show that JaCoP is anywhere from 10X-100X
faster than core.logic at solving Finite Domain problems. While there
is a considerable amount of work to be done to improve the performance
of core.logic's general constraint framework, it's unlikely we'll
achieve JaCoP finite domain solving performance in the near
future. Thus JaCoP integration is attractive.

More research needs to be done to do determine what this integration
should look like and what assumptions in core.logic will need to be
modified to allow any such integration.
