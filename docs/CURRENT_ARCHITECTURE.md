# Current architecture audit

Audit baseline: `ef1a9a548aea4ea75fc98d6798952f373de48da9` (2026-09-21).
This document records the repository **before** the universal mathematics extension.
Every tracked package, operation interface, example, resource and test was inspected.
The uncommitted `testjson` experiment in the original checkout is outside this branch.

## Existing model

`Algebra<T>` is a named carrier representation (a raw Java Class), a list of
membership predicates, and registries of operations. It does not enumerate the
carrier, so it already accommodates infinite domains with decidable membership.
Its name/class pair does not encode dimension, modulus, smoothness, measure or
other mathematical structure. `buildAlgebraItem` returns null when a rule fails;
without rules it does not enforce the carrier class or reject null.

`AlgebraItem<T>` stores an algebra and a value. `AlgebraItemDecorator<T>` delays
same-carrier binary evaluation until `perform()`. Other item operations evaluate
their preceding computation and invoke a registered operation.
`AbstractAlgebra<T>` implements this dispatch.
`MathTool` is a name-indexed registry of algebras, not a theorem prover.
`IValidationRule` has a Boolean test and description; it cannot report unknown
membership, assumptions or proof evidence.

## Exact interface/signature inventory

Here A, B and C denote mathematical domains, not necessarily different Java classes.
The method-level type variables in several legacy interfaces are caller-selected
and consequently rely on unchecked casts and honest implementations.

| Existing interface | Mathematical shape | Actual runtime result / caveat |
| --- | --- | --- |
| IOneOperandOperation<A> | A → A | Raw A; declared only, no Algebra registry or flow dispatch |
| ITransferOperation<A> | A → B | IAlgebraItem<B>; also usable when B = A |
| IOperation<A> | A × A → A | Raw A; deferred item decorator |
| ICustomResultOperation<A> | A × A → B | IAlgebraItem<B> |
| IUnsafeOperation<A> | A × B → C | IAlgebraItem<C>; includes C = B and C = A |
| ICustomMemberOperation<A> | A × B → A | IAlgebraItem<A>; may transform A |
| ILeftProjectionOperation<A,B> | A × B → A | Raw A; supplied implementation returns first unchanged |
| IFlatOperation<A> | A × A → List(A) | List<IAlgebraItem<A>> |
| ICustomResultFlatOperation<A> | A × A → List(B) | List<IAlgebraItem<B>> |
| IUnsafeFlatOperation<A> | A × B → List(C) | List<IAlgebraItem<C>> |
| ICustomMemberFlatOperation<A> | A × B → List(A) | List<IAlgebraItem<A>> |
| ILeftProjectionFlatOperation<A,B> | A × B → List(A) | Supplied implementation emits one item |
| ITransferFlatOperation<A> | **declared A → B, not A → List(B)** | Returns a single IAlgebraItem; AbstractAlgebra casts it to List |

A × B → B requires no new mathematical operation family: specialize the result
domain of an unsafe operation to B. A × B → A is not in general a projection:
scalar-dependent transformation of a vector can change its value. The existing
left-projection classes implement only the special identity-on-first operation.
All working legacy flat results are finite eager ordered lists with duplicates;
they do not imply sets, distributions or complete solution families.

## Flow and execution

`AlgebraFlow` builds a shared mutable list of `IFlowInvoke` closures and executes
it on collection. Cross-domain methods return another facade with a different
current algebra. Closures capture their originating facade's mutable currentFlow.
Chains crossing more than one facade can therefore read stale/null state.
InputFormat, IPart, IWriter and collection methods all use finite Lists.
There is no native infinite streaming executor or inspectable typed dependency graph.

## Findings affecting faithful mathematics

* Custom-member and unsafe registration have reversed containsKey branches: a
  first registration dereferences a missing list. Existing name overloads also
  cannot be trusted until corrected and tested.
* Unsafe flow dispatch consults the custom-member registry.
* Flat transfer flow checks the scalar transfer registry, and its result signature
  is inconsistent as described above.
* Item dispatch selects the first custom/unsafe overload without checking B's
  class, although some flow methods inspect the class.
* Cross-domain compatibility is based on names and raw classes, not mathematical
  definitions. Integer residues with different moduli would share a Java class.
* Invalid membership, undefined operations, numerical failure and programmer
  errors have no distinct result protocol. Custom exceptions extend NPE.
* Laws, provenance, computational accuracy, review and proof status are absent.
* generateDescription omits operation registries; its JSON builder does not escape
  names/descriptions. No sound categorical interpretation follows from this alone.

These are baseline observations, not claims that every legacy path is verified.

## Other packages inspected

| Package | Role and limitations |
| --- | --- |
| example | Integer sum/subtraction/comparison, Boolean conjunction, eager list input |
| cluster / factory | Host-supplied queue, partitions, properties and launchers; Runner has null configuration placeholders; counter increments are not atomic; no tested distributed deployment |
| filesystems | Host filesystem/path/pointer/format interfaces, eager parsing and ad-hoc partition serialization |
| dbinput | JDBC split/read scaffolding; zero-based parameter/column indexes and unclosed resources require separate remediation |
| webinput | Marker splitter interface |
| utils | Property and input-queue contracts |
| algebraflow.IPhysicsFlow / PhysicsItem | Unimplemented API / empty placeholder, not a physics model |
| exceptions | Historical NPE subclasses |
| resources | Log4j file-appender configuration |
| tests | Five left-projection integration tests; baseline Maven test succeeds |

Java source/target is 8. The baseline Log4j API 2.17.1/core 2.25.4 pairing emits
a fallback logger diagnostic. This project must not equate successful compilation
with correctness of untested mathematical or cluster behavior.

## Extension boundary

The recurring gaps are domain identity/constraints, typed composition,
partial-result semantics, provenance/laws, and lazy mathematical members.
An additive `mathematics` layer can attach these to existing Algebra carriers and
adapt operations to the legacy interfaces without deleting or changing those APIs.
Dependent domains, quotient witnesses and infinitary evaluation need explicit RFCs;
they are not solved by labeling an arbitrary Java object with a mathematical name.

## Changes since the audit

This baseline is retained for comparison. Current code fixes registration/overload dispatch, shared flow state and flat transfers; adds native scalar/flat unary support; and changes ILeftProjectionOperation to A x B -> IAlgebraItem<B> as requested. Concrete builders in algebra/concrete register implementations from operations directly in MathTool. The optional mathematics.core prototype is not their executor. See [the current operation model](MATHEMATICAL_OPERATION_MODEL.md) and [implementation status](IMPLEMENTATION_STATUS.md) for current contracts and limits.
