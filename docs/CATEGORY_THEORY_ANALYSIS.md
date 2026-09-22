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
| Functor | Object and morphism maps preserving source/target, identities and composition | Two unrelated callbacks do not establish preservation |
| Natural transformation | Component for every object with naturality squares | Indexed family plus quantified coherence |
| Product/coproduct | Projections/injections and the relevant universal property | Pair is a concrete finite set product; no general universal-property engine |
| Limit/colimit | Diagram, cone/cocone and unique factorization | Initial/terminal objects computed for finite tables; broader constructions depend on the ambient category |
| Adjunction | Hom correspondence or unit/counit with triangle identities | Needs law/equivalence witnesses, not a method-name convention |
| Monoidal/enriched category | Tensor/enrichment and specified coherence | Ordinary carrier graph is insufficient |
| Higher/infinity category | Higher cells or a chosen model and coherence data | No general encoding or checked coherence implemented |
| Topos | Particular categorical structure and axioms | No generic topos implementation |

## Smallest defensible extensions

First use [dependent domain families](RFC_DEPENDENT_DOMAINS.md) to describe Hom(A,B). Use [law and equivalence witnesses](RFC_LAWS_AND_EQUIVALENCE.md) for identities, associativity, preservation and universal properties. Only then evaluate a generic diagram/coherence layer against at least two concrete examples. Higher categories require a selected mathematical model; a universal HigherCategory Java class would hide that choice.

The implemented finite alternative checks tables with at most 128 arrows and objects. It supports discrete categories, conversion from finite preorders, opposite categories, hom sets, arrow inverses, initial/terminal objects and the underlying existence relation. Full labelled-table equality does not identify isomorphic presentations. General functors, natural transformations, diagrams and external proof-object checking remain unimplemented. The coverage registry keeps general category concepts distinct from these concrete native registrations.

The breadth of existing formal developments is a useful discovery source: [mathlib documentation](https://leanprover-community.github.io/mathlib4_docs/Mathlib.html). The present Java mappings are not extracted from or proved by mathlib.
