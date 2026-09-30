# RFC: dependent domain families

Status: PROPOSED; fixed carrier instances work today, general dependent families do not.

## Repeated limitation

A tangent vector belongs to T_p M at a particular point p. A morphism belongs to Hom(A,B). A matrix shape depends on dimensions, and a residue belongs to a modulus. A section of a bundle chooses a member of the corresponding fiber at every base point. These are value-indexed constraints, not simply different Java classes.

Current Algebra predicates enforce a fixed dimension or modulus, and native operations connect chosen instances. Result algebras are fixed at registration. The optional Domain/Signature prototype also stores fixed domains; neither API computes a dependent result carrier from operand values. A metadata string describing a tangent space does not repair that missing constraint.

## Smallest extension

Propose a DomainFamily<I,T> with an index domain and a stable fiber resolver. A checked dependent pair stores its index, value, family identity and membership evidence. Dependent operations declare how input indices determine the result fiber. The resolver must preserve index equality semantics; arbitrary Java equality cannot identify geometric base points or isomorphic objects.

Required obligations include validity of the base index, validity in the selected fiber, transport rules under any permitted index equivalence, and composition of index transformations. Unknown evidence remains UNKNOWN. No unchecked cast is a substitute for transport.

For sheaves and bundles, local data also need restrictions, overlaps, compatibility and gluing/uniqueness evidence. The resolver is necessary but insufficient. This motivates the separate gluing proposal in the registry; no universal sheaf implementation is claimed. Algebraic geometry uses several layers of such structures; [the Stacks Project](https://stacks.math.columbia.edu/) is a source for decomposing them into precise obligations.

## Alternatives and affected areas

For a single problem, construct fixed domains such as Q^2 and Mat2(Q), or package a matrix with shape checks. For finite indexed families, explicitly register each fiber and legal transition. These approaches already fit the framework and should be preferred when sufficient.

General tangent/cotangent bundles, parameterized solution spaces, categorical hom families, sheaves and moduli problems need further work. Proposed tests must reject a vector moved to the wrong base point, composition with incompatible fibers, nonstable resolvers, and incompatible gluing data. No changes to existing Algebra registration or optional Domain compatibility are proposed until such examples and tests exist.
