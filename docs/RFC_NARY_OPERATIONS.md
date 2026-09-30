# RFC: finite n-ary operations

Status: DEFERRED; no native n-ary interface added.

## Repeated need

Multilinear maps, statistics, polynomial evaluation with several parameters and solver configurations have several inputs. The existing scalar/binary operation layer need not grow one interface per arity.

A constant is Unit -> C. A finite operation A1 x ... x An -> B is unary on a product carrier, constructed from nested Pair values or a domain-specific immutable tuple. Membership checks each component; order and component domains are explicit. The native polynomial integral uses a Pair-valued bounds Algebra. The optional FoundationDomains.product prototype also supports recursive binary products; its separate FoundationsTest covers membership and function-valued members.

This is representation by composition. It does not assert the performance, ergonomics or universal property of an unimplemented general tuple library.

## Decision and alternatives

Keep the unary/binary public layer. Add a native arity-indexed operation only if several real implementations show repeated tuple unpacking, a need for schema-driven invocation, or execution/planning costs that products cannot handle. The smallest candidate would carry an ordered list of operand domains and validate arity and each argument before invoking a body. Its list must not erase mathematical domain identity, and varargs of Object must not become the user-facing proof of type safety.

A finite tuple does not represent an infinitary operation over every sequence term. Limits, infinite sums and path integrals require convergence, measure or other witnesses; they belong to [the analytic RFC](RFC_INFINITE_AND_ANALYTIC_DOMAINS.md). A variable finite sample is one sequence member, not automatically a changing operation signature.

Acceptance tests for any native addition must compare it with product composition, test wrong arity and same-Java-class/different-domain mismatches, preserve nullary Unit semantics, and demonstrate an actual use beyond syntax convenience.
