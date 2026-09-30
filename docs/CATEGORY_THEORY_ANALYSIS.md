# Category theory analysis

Status: finite table implementation, analysis and proposed extensions. No categorical completeness theorem or formal verification is claimed.

## What the existing model supplies

Native Algebra carriers and their registered operations form a useful directed graph. AlgebraFlow chains operations through the actual MathTool registrations. Unit and explicit Pair carriers support constants and finite product inputs. The optional Domain prototype separately provides checked unary composition; it is not the native flow executor. Neither execution path proves category laws.

The conventional category of sets and total functions requires a specified universe of objects, an identity for each object, closed composition, and identity/associativity laws under extensional equality. Our callbacks can throw, fail membership checks, mutate captured state or diverge. Java reference identity and callback equality do not decide extensional equality. Therefore the executable catalog is not automatically that category.

Composition of pure total checked functions has the expected mathematical interpretation conditional on their contracts; the implementation tests some finite examples. That conditional interpretation is separate from a universal proof about arbitrary callbacks.

FiniteIntegerFunctionAlgebra now stores total maps between explicit finite sets, checks typed composition and compares maps extensionally within that finite representation. FiniteCategoryAlgebra separately registers explicit category tables in the original Algebra/MathTool architecture. Its immutable members validate objects, arrow endpoints, complete composition, identities and associativity at construction. This finite validation is executable Java code with tests; it is not formal verification by a trusted proof checker.

## Effects and flat results

Partial maps can be studied in a suitable category, and finite-list-valued maps can be composed by concatenation in the list Kleisli category under appropriate laws. The framework does not silently choose either interpretation. Ordered duplicates, exceptions, unknown membership in the optional prototype, state and resource limits are observable. Mixing lists with sets or probability distributions changes the mathematical composition.

A future effect-aware operation description would need an explicit result constructor, unit, composition, laws and equality semantics. Merely tagging an operation LIST supplies none of those laws. Empty lists can mean a valid empty result; failure is a different outcome.

## Structures requiring more than an operation graph

| Concept | Required data/obligation | Present boundary |
| --- | --- | --- |
| Category | Objects, indexed hom domains, identity, composability, associativity | Finite labelled tables implemented; general hom families still need indexed domains and law evidence |
| Functor | Object and morphism maps preserving source/target, identities and composition | FiniteFunctorAlgebra checks total maps and all preservation equations for finite tables |
| Natural transformation | Component for every object with naturality squares | FiniteNaturalTransformationAlgebra checks every component and square, with vertical/horizontal composition and pre/postcomposition |
| Product/coproduct | Projections/injections and the relevant universal property | Products and coproducts are constructed as finite diagram limits and colimits, with projections/injections and unique factorizations checked |
| Limit/colimit | Diagram, cone/cocone and unique factorization | FiniteConeAlgebra and FiniteCoconeAlgebra check diagrams and search finite table limits/colimits with unique mediators |
| Adjunction | Hom correspondence or unit/counit with triangle identities | FiniteAdjunctionAlgebra constructs finite adjoints and hom bijections; checks naturality and both triangles even for noninvertible components |
| Monoidal/enriched category | Tensor/enrichment and specified coherence | Ordinary carrier graph is insufficient |
| Higher/infinity category | Higher cells or a chosen model and coherence data | No general encoding or checked coherence implemented |
| Topos | Particular categorical structure and axioms | No generic topos implementation |

## Smallest defensible extensions

First use [dependent domain families](RFC_DEPENDENT_DOMAINS.md) to describe Hom(A,B). Use [law and equivalence witnesses](RFC_LAWS_AND_EQUIVALENCE.md) for identities, associativity, preservation and universal properties. Only then evaluate a generic diagram/coherence layer against at least two concrete examples. Higher categories require a selected mathematical model; a universal HigherCategory Java class would hide that choice.

The implemented finite alternative checks tables with at most 128 arrows and objects. It supports discrete categories, conversion from finite preorders, opposite categories, hom sets, arrow inverses, initial/terminal objects and the underlying existence relation. Finite functors preserve all table equations by construction and support composition, opposite, strict inverse, hom-wise full/faithful checks and an essential-surjectivity criterion. Finite natural transformations now check all components and naturality squares, support vertical/horizontal composition and pre/postcomposition, and invert exactly when every component is invertible. Full labelled-table equality does not identify isomorphic presentations. Finite diagram limits are implemented separately below; infinite categorical structures and external proof-object checking remain unimplemented. The coverage registry keeps general category concepts distinct from these concrete native registrations.

FiniteEquivalenceAlgebra constructs a quasi-inverse for a full, faithful and essentially surjective finite functor and validates explicit unit/counit natural isomorphisms and both triangle identities. Composition retains and combines the chosen witnesses; equality compares those choices. This supplies concrete adjoint equivalences without a generic law-evidence engine.

FiniteAdjunctionAlgebra also allows noninvertible units and counits. Finite universal-arrow search constructs a right adjoint to a supplied left functor, or dually a left adjoint to a supplied right functor. Hom correspondences transfer to the existing finite-function algebra and preserve their declared hom sets. The search decides existence only for the supplied finite tables within the existing category cap; no infinite adjoint functor theorem or proof-assistant kernel is implemented.

FiniteConeAlgebra implements finite diagram cones as checked natural transformations and verifies unique factorization for limits. It constructs products and equalizers when they exist in the supplied finite ambient category, and empty-diagram limits recover terminal objects. Search is explicitly bounded; exhaustion differs from a proof of nonexistence. Mapping and reindexing retain cone equations but may lose the universal property. Infinite limits and external proof checking remain outside this implementation.

FiniteCoconeAlgebra supplies the dual finite constructions: cocones, colimits, outgoing mediators, coproducts, coequalizers and empty-diagram initial objects. Opposite-diagram conversion reuses the checked cone search and its resource bounds. The tests independently check disjoint unions and quotient examples inside a small full category of finite sets; these algorithms do not assume that every ambient category contains the requested construction.

The breadth of existing formal developments is a useful discovery source: [mathlib documentation](https://leanprover-community.github.io/mathlib4_docs/Mathlib.html). The present Java mappings are not extracted from or proved by mathlib.
