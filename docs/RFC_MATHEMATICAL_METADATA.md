# RFC: mathematical metadata and checked transformations

Status: native architecture decision implemented; optional metadata prototype retained. General evidence support remains proposed.

## Problem and current decision

Dimensions, moduli, differentiability and normalization need more than Class<T>. Laws, assumptions, computation accuracy and evidence need explicit descriptions.

Concrete execution remains Algebra, its operation registries, IAlgebraItem, AlgebraFlow and MathTool. Implementations are in operations and builders in algebra/concrete. TypedMembershipRule checks concrete membership. OperationRegistration describes native registrations; it is not a parallel executor. ConcreteAlgebra.laws() returns declarations without claiming proof.

The initial Domain/Structure/Outcome experiment remains a separately tested prototype. It supports tri-state membership, metadata and checked composition but is not required by ConcreteMathematics. This supersedes the initial proposal to route concrete algebras through that layer.

## Available metadata

The JSON registry records carriers, operation shapes, invariants, representation scope, implementation/test paths, partiality, review/formal status and provenance. Native facts are synchronized from the registered operations' checked manifest. Survey classifications are separately authored and provisional.

Tests do not promote HUMAN_REVIEWED, PROVED or FORMALLY_VERIFIED. Human review needs a reviewer, date and scoped notes; formal claims need evidence artifacts. No specialist review or formal proof is recorded for this implementation.

## Deferred extensions

General unknown membership, dependent codomains, quotient witnesses, certified analytic contracts, accuracy propagation and formal law evidence are not native Algebra features. Use the dedicated RFCs and demonstrate repeated concrete needs before extending those contracts. Do not silently accept UNKNOWN as Boolean true.

An infinite domain definition does not require enumeration. There is no claim that all real numbers have finite executable representations. Local tests do not certify the host-dependent distributed runner.
