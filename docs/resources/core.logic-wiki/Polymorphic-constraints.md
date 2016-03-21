Polymorphic constraint may be possible we do an extremely narrow
version of core.match that assumes the patterns are disjoint and add
the ability to extend matches later. The problem seems simpler if we
assume dispatching on the type of the first parameter (input) or the
type of the last parameter (output).

This work is probably pretty closely related to [[Constraints & Modes]].