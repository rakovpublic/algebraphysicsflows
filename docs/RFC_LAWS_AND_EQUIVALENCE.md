# RFC: laws, equivalence and quotients

Status: PROPOSED beyond native law descriptions and the optional Structure/LawChecks prototype.

## The actual gap

ConcreteAlgebra.laws() retains native law descriptions. The optional Structure prototype can additionally retain carrier roles, operation signatures and evidence descriptions. This supports describing an algebraic specification. It cannot interpret a quantified law or check a submitted proof. That is a gap in **certified membership and evidence**, not a claim that uncertified groups cannot be described with typed operations.

Canonical rationals and fixed-modulus residues have constructive normal forms. Arbitrary quotient spaces need an explicit equivalence relation and well-definedness: equivalent operands must produce equivalent results. Java equals, isomorphism, homeomorphism, homotopy equivalence and approximate numerical agreement are different relations.

## Smallest proposed layer

Add an explicit equivalence specification carrying a relation, its scope and evidence for reflexivity, symmetry and transitivity. A quotient domain references this specification. Operations descending to the quotient must reference well-definedness obligations. If normalization is available, state its soundness and completeness scope; do not require every quotient to have a computable canonical representative.

A law should have typed variables, assumptions, a proposition representation and independently checked evidence. An external formal artifact should record the exact statement, checker and version, input hash and verification result. An unverified proof object is data, not a successful certificate. Human review remains separate and names the reviewer, date and scope.

Finite LawChecks search associativity, commutativity and identity over a supplied sample. A failure supplies a counterexample; passing a sample yields EMPIRICALLY_TESTED. It does not set PROVED, HUMAN_REVIEWED or FORMALLY_VERIFIED. Even exhaustive finite checking needs an explicit assertion and validation that the carrier was completely enumerated.

## Alternatives, boundaries and validation

Keep canonical concrete domains and explicit assumptions where witnesses are unavailable. Return UNKNOWN for undecided relations. Keep approximate comparisons separate from equivalence: common epsilon comparisons are not transitive.

Unrestricted program extensional equality cannot generally be decided by executing more examples; termination already has a formal impossibility boundary. [Mathlib's halting development](https://leanprover-community.github.io/mathlib4_docs/Mathlib/Computability/Halting.html) is a formal reference for that boundary. This does not prevent representing function definitions, decidable restricted families or proof-bearing equality.

Acceptance tests must include representative independence, a relation that fails transitivity, a callback violating closure, a matrix counterexample to commutativity and a forged/mismatched proof artifact. No proof checker or general quotient engine is shipped on this branch.
