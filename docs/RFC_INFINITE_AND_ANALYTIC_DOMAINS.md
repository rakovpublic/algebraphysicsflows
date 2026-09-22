# RFC: infinite and analytic domains

Status: PROPOSED general layer; exact restricted examples implemented.

## Existing representations

An infinite carrier can be defined intensionally without enumeration. N and Z use finite BigInteger members with a predicate. LazySequence computes indexed terms and exposes explicit finite prefixes. Polynomial supports exact differentiation and integration in Q[x]. FiniteDistribution represents normalized finite probability masses.

These do not represent every real, every sequence, all C1 functions or general measures. RealExpression is a finite symbolic syntax, not the continuum. An arbitrary sequence callback need not terminate; a finite prefix does not certify convergence.

## Repeated missing obligations

| General object/operation | Evidence that must be explicit |
| --- | --- |
| Computable real | Chosen name/representation and effective approximation/error contract; equality may remain undecided |
| Limit or series | Convergence assumptions and modulus/error bound where computable |
| C1 -> C0 derivative | A differentiable representation and derivative/regularity witness |
| Gradient/divergence/curl | Domain, coordinates, dimension, regularity and relevant geometric structure |
| Banach/Hilbert space | Norm/inner product laws and completeness, not just a vector callback |
| Measure space | Carrier, sigma algebra, measurable sets/maps and countable-additivity obligations |
| Lp/Sobolev space | Measure, integrability/weak derivative conditions and almost-everywhere quotient equality |
| Continuous distribution | Measure or validated density/CDF representation with normalization evidence |
| Fourier/integral transform | Function-space contract, convergence convention, normalization and result space |
| Numerical algorithm | Tolerances, stopping conditions, error estimate/certificate and nonconvergence outcome |

General integration is not automatically a unique scalar operation for every function. Specify integrability and bounds; improper integrals have convergence conditions. An indefinite integral is a family. The implemented PrimitiveFamily explicitly varies rational constants; uniqueness follows only after an appropriate condition and assumptions.

## Proposed extension and alternatives

Use restricted analytic representations with membership witnesses and optional verified enclosures. A separate measure-space bundle supplies measurable-map and integrability evidence; combine it with quotient-aware equality for Lp. Numerical adapters must report accuracy independently of existence, uniqueness and formal evidence.

Restricted polynomial calculations, finite distributions and finite prefixes are useful alternatives today. No unrestricted smoothness, convergence, integrability or all-real equality decision procedure is proposed. No exact operation silently converts to double.

Validation should distinguish divergent sequences with long stable prefixes, equivalent almost-everywhere representatives, singular integrals, nondifferentiable inputs, unknown membership and certified interval containment. Backend conventions must be recorded. [NIST DLMF](https://dlmf.nist.gov/) is a reference for analytic conventions and identities; it is not evidence that this Java implementation covers them.
