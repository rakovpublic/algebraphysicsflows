# RFC: external mathematical systems and solution evidence

Status: minimal adapter contract implemented; no production CAS, numerical or proof-system integration installed.

## Current contract

ExternalMathAdapter identifies a system, version and provenance, checks whether it supports a Signature, and evaluates supplied arguments to an Outcome. It is an optional boundary independent of the core and CCDR/Synthesis. There is no shell command builder, network service, implicit process launch or universal object codec.

The differential test fixtures are the one executed external reference calculation: Python's standard-library Fraction arithmetic and a permutation-expansion determinant. They are deterministic and checked into test resources. [Fraction's documentation](https://docs.python.org/3/library/fractions.html) describes the exact rational representation used for those fixtures. Agreement on 104 cases is empirical validation, not a formal equivalence proof.

## Proposed implementation requirements

A concrete adapter must define supported carriers, lossless codecs, version capture, variable assumptions, branch conventions and resource limits. Exact-to-approximate conversion is an explicit operation. Imported results are checked against the declared domain. Solver errors, undefined mathematical requests, numerical failure and implementation failure remain distinct.

Candidate backends include SymPy/SageMath for symbolic algebra, validated numerical libraries for enclosures, and Lean/Coq/Isabelle for proof evidence. These are candidates, not bundled dependencies or verified integrations. Start with one narrow contract that has an independent local test oracle.

A solver returning a value does not establish existence, uniqueness or completeness of a solution family. Return the candidate or family with separate solution and evidence labels. ODE/PDE results must specify equation, function space, initial/boundary conditions and assumptions. An optimizer must distinguish a feasible candidate from an optimum and an infeasible problem from a failed search.

## Acceptance and alternatives

Acceptance tests must check round trips, unsupported signatures, domain mismatches, missing assumptions, backend version drift, nonconvergence, malformed output and a rejected proof certificate. Cross-system disagreements must preserve both inputs/results for diagnosis. A timeout is not mathematical nonexistence.

Until a concrete adapter meets these requirements, retain symbolic specifications or use the existing exact rational, polynomial and finite exhaustive algorithms. General solution certificates and symbolic-backend gaps remain REQUIRES_EXTENSION in the registry.
