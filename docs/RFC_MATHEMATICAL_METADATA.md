# RFC: mathematical metadata and checked transformations

Status: implemented incrementally; human mathematical review pending.

## Recurring problem

Modular arithmetic and matrix dimensions, differentiable functions and measures,
conditional distributions and partial inverses all need constraints beyond Class<T>.
Group laws, category laws and probability normalization need explicit evidence.
CAS, numerical and symbolic outputs need accuracy/provenance labels.

## Alternatives

Changing every legacy generic signature breaks callers. Per-field carrier classes
alone duplicate validation and cannot express unknown membership. Treating
everything as Object loses compatibility guarantees. A separate CAS would replace
the project rather than extend its domain/operation model.

## Smallest reusable extension

Add immutable descriptive metadata and a checked domain wrapper backed by
Algebra<T>. Keep domain compatibility conservative: shared domain objects or an
explicit checked embedding, never equal Java classes or names alone.
Introduce typed unary/binary transformations, finite flat transformations,
structured outcomes, and inspectable unary composition. These are typed adapters
over the existing operation forms, not a native n-ary execution engine.
Keep collection semantics in explicit mathematical member types.
Store laws as statements/evidence; do not infer proofs from tests.

Membership can be true, false or unknown. Unknown is not false. Definitions of
infinite domains do not require enumeration. Computations accept only membership
that can be established by the supplied rule and validate output closure.

## Deferred decisions

Dependent codomains, quotient equality witnesses, certified analytic membership
and arbitrary infinitary evaluation remain RFCs. No automatic proof status
promotion. No claim that all real numbers have finite executable representations.
The new layer is local and synchronous; legacy cluster scaffolding remains
host-dependent and is not certified by the local test suite.
