# CCDR Standard Model Derivation: Verification Report

**Verification date:** 2026-11-22  
**Repository commit tested:** `d577dca`  
**Branch tested:** `codex/ccdr-v710-synthesis-v39-pipeline`  
**Build:** Java 17.0.4.1, Apache Maven 3.9.11, JUnit 4 / Surefire 3.2.5  
**Command:** `mvn -f groupimp/pom.xml test` after `mvn -f groupimp/pom.xml clean`

## 1. Executive Summary and Build Telemetry

The structural pipeline checks execute through exact rational carriers, high-precision numerical carriers, and native algebra flows. The physical-data audit reproduces the PDG CKM Jarlskog central value within its quoted uncertainty, and the PDG tau-mass one-sigma interval for the computed Koide ratio contains 2/3. The audit also records cases where supplied model identifications do not follow from implemented equations or cited measurements.

The clean Surefire reports contain **95 test classes and 937 tests: 937 passed, 0 failures, 0 errors, and 0 skipped**. Maven reported **66 seconds** total elapsed time; the sum of per-suite Surefire test times is 53.958 seconds. The two CCDR suites account for 7 tests: 2 structural pipeline tests and 5 empirical audit tests. The broader test run includes octonion, Dixon, twisted Hahn-series, topology, chain-cone, and other algebra tests; there were no test regressions in this run.

The code exercises a two-tier pattern: a runtime-rank, finite-support twisted lattice carrier over `H(Q)\), followed by an additive parity-class fold into the 128-rational-coordinate `RationalDixonZ2` carrier. That boundary carrier is non-associative. The fold is explicitly tested as surjective and additive, but **not multiplicative**. The `RationalTwistedHahnSeries` implementation is a rational-function subring of the rank-one twisted Hahn construction; it does not materialize every element of the full Hahn–Mal'cev–Neumann completion.

The tested checkout is branch `codex/ccdr-v710-synthesis-v39-pipeline`; the task attachment's reference to `master` did not match the checkout. Telemetry is tied to the commit and branch actually run, not to `master`.

## 2. Stage-by-Stage Algebraic Verification Matrix

| Stage | Executed construction and exact inputs | Verified invariants | Scope and interpretation |
|---|---|---|---|
| 1. Runtime-rank twisted bulk and BSM1 map | `RationalTwistedLatticeAlgebra` is constructed at ranks N=5 and N=8. The test uses a zero-diagonal sign matrix with entries c12=c21=1 and all others zero; entries are in F2, so the matrix is alternating (symmetric in characteristic two). The coefficient action conjugates by quaternion i when exponent coordinate 1 is odd. Supports include g=(0,1,0,...), h=(0,0,1,...), and a residue at coordinate 0. | The selected g,h basis products are noncommuting and include the cocycle sign. Rank-5 and rank-8 BSM1 images map first exponent coordinate 1 to 2, remove odd first-coordinate support, and preserve the tested product for the selected inputs. | This is a finite-support twisted skew-group algebra carrier. Runtime ranks 5 and 8 are exercised; this does not prove arbitrary Hahn series are stored or that every proposed dark-sector parameter is derived by the endomorphism. |
| 2. Simplicial collapse and chiral-fold sign checks | An absolute collapse of the 2-simplex at face (0,2) is inspected in degree 1. The selected chain-homotopy matrix entry is exactly -1. The tested fold uses (epsilon5, foliation arrow, simplex sign, CMB sign)=(-1,+1,-1,-1). | `chiralitySign()=-1`; `smD11Consistent()`, `pA30Consistent()`, and `cl9Consistent()` are all true; the implementation classifies sign -1 as right-chiral. | The fold class checks equality of supplied signs; it does not infer those inputs from independent physical data. OP20 degeneracy is explicit: both (+1,-1) and (-1,+1) give product -1, so the product does not identify a unique factor pair. |
| 3. Boundary fold, Dixon representation, and Lie-bracket closure | All 128 basis coordinates (32 parity classes from five binary parities times 4 quaternion components) are checked against the 5-to-4 parity fold. Selected rank-5 and rank-8 bulk examples have equal folded boundaries. A rank-one HMN example specializes the group generator at t=1. Octonion units e1,e2,e4, Dixon left multiplication, Witt idempotents, and generator lists are exercised. | The parity fold is surjective and additive for the checked coordinates, and a product witness proves it is not multiplicative. The rank-one HMN example is associative on the tested triple, while the e1,e2,e4 octonion and Dixon examples have nonzero associators. Dixon left-multiplication matrices are 128x128; octonion left multiplication is 8x8. Witt elements satisfy P+²=P+, P-²=P-, P+ + P- = 1, and P+ * P- = 0. Eight octonion stabilizer candidates and three quaternion-adjoint generators are returned; every pairwise bracket is checked to lie in the exact rational span by rank comparison (`inSpan`). The `chi5-to-one-dixon` flow sends the tested group generator to quaternion i in the Dixon even slice. | The parity fold is not an algebra homomorphism. The `chi5-to-one` operation is t=1 specialization followed by quaternion inclusion, **not** a leading-term or valuation projection. The 8- and 3-element closed candidate families are algebraic constructions; this test does not identify them with physical gauge generators. |
| 4. One-generation Weyl charges and anomalies | `StandardModelWeylGeneration.states()` exposes the individual 16 left-handed Weyl fields, including conjugates of right-handed fields and a sterile nu_R^c. Exact T3, Y, and Q=T3+Y values are checked. | Exact traces: Tr(Y)=0, Tr(Y³)=0, Tr(T3²)=2, Tr(Q²)=16/3; mixed SU(2)²U(1) and SU(3)²U(1) traces are 0; cubic SU(3) index is 0; SU(2) doublet parity is 0. The trace ratio is exactly 2/(16/3)=3/8. | This verifies the rational charge signature and anomaly formulas encoded by the class. It does not assign states to a minimal left ideal or derive renormalization-group evolution. |
| 5. Cyclotomic arithmetic and high-precision metrics | In `RationalCyclotomic`, zeta_12 is the class of x modulo Phi_12(x)=x^4-x^2+1. The test verifies zeta_12³=i, zeta_12⁶=-1, and zeta_12 times its inverse equals 1. Decimal evaluation is requested at 40 digits; the structural pipeline also evaluates Koide and CKM metrics at 30 digits. | The 40-digit phase is evaluated as sqrt(3)/2 + i/2 to the test tolerance. The empirical suite computes Koide and measured-phase CKM invariants at 40-digit precision. | Cyclotomic identities are exact over Q; decimal outputs are approximations at the requested BigDecimal precision. A phase of pi/6 is not treated as the measured CKM phase. |
| 6. Native `IAlgebraFlow` execution | A simplicial-complex flow constructs a collapse, applies binary left projection `on-absolute-chain`, applies unary `boundary`, then transfers to chain coordinates. Separate flows exercise the rank-lattice endomorphism and named parity / `chi5-to-one` transfers. | The typed flow result is an `IAlgebraFlow<IntegerVector>`; its collected boundary coordinates equal the direct carrier calculation. The left projection reports the expected simplicial-chain algebra. | The collapse is initially constructed with `performAlgebraUnsafe`; subsequent operation results are statically typed. This verifies integration and outputs, not full compile-time typing at every operation-lookup boundary. |

### 16-state charge table

The representations and charges below are the assignments encoded in `StandardModelWeylGeneration`. Here Y uses Q=T3+Y, not the alternative convention Q=T3+Y/2.

| Weyl state | SU(3)C | SU(2)L | T3 | Y | Q |
|---|---:|---:|---:|---:|---:|
| nu_L | 1 | 2 | +1/2 | -1/2 | 0 |
| e_L | 1 | 2 | -1/2 | -1/2 | -1 |
| u_L^r | 3 | 2 | +1/2 | +1/6 | +2/3 |
| u_L^g | 3 | 2 | +1/2 | +1/6 | +2/3 |
| u_L^b | 3 | 2 | +1/2 | +1/6 | +2/3 |
| d_L^r | 3 | 2 | -1/2 | +1/6 | -1/3 |
| d_L^g | 3 | 2 | -1/2 | +1/6 | -1/3 |
| d_L^b | 3 | 2 | -1/2 | +1/6 | -1/3 |
| e_R^c | 1 | 1 | 0 | +1 | +1 |
| nu_R^c | 1 | 1 | 0 | 0 | 0 |
| u_R^{c,r} | anti-3 | 1 | 0 | -2/3 | -2/3 |
| u_R^{c,g} | anti-3 | 1 | 0 | -2/3 | -2/3 |
| u_R^{c,b} | anti-3 | 1 | 0 | -2/3 | -2/3 |
| d_R^{c,r} | anti-3 | 1 | 0 | +1/3 | +1/3 |
| d_R^{c,g} | anti-3 | 1 | 0 | +1/3 | +1/3 |
| d_R^{c,b} | anti-3 | 1 | 0 | +1/3 | +1/3 |

## 3. Empirical Physical Data Audit and Residual Analysis

PDG 2025 lepton masses are me=0.51099895000 MeV, mmu=105.6583755 MeV, and mtau=1776.93 +/- 0.09 MeV. The CKM benchmark values are from the PDG 2025 global fit. The weak-angle comparison uses the PDG 2025 physical-constants table. External measurements are not algebraically derived by this repository.

| Observable / quantity | Calculated result in the executed checks | External / target value | Residual and status |
|---|---:|---:|---|
| Koide ratio, PDG 2025 central lepton masses | Q=0.6666644634145001750698620530 (40 digits requested); Q-2/3 is approximately -2.20325217e-6. At mtau +/- 0.09 MeV, Q is in [0.666659382322724, 0.666669544238346]. | 2/3=0.666666666666...; mtau=1776.93 +/- 0.09 MeV. Historical mtau=1776.86 MeV yields Q=0.666660511477386. | The central value is within the specified 1.5e-5 tolerance, and the tau one-sigma interval brackets 2/3: **Pass**. [PDG lepton summary](https://pdg.lbl.gov/2025/tables/rpp2025-sum-leptons.pdf), [PDG tau listing](https://pdg.lbl.gov/2025/listings/rpp2025-list-tau.pdf) |
| CKM J_CP, measured phase | J=3.1169935287554704725726246712e-5 using s12=0.22501, s23=0.04183, s13=0.003732, derived cij=sqrt(1-sij²), and delta=1.147 rad. Residual from central value: -3.00647e-8. | PDG global fit J=(3.12 +0.13/-0.12)e-5, delta=1.147 +/- 0.026 rad. | Calculated invariant lies within the quoted uncertainty: **Pass**. [PDG 2025 CKM review](https://pdg.lbl.gov/2025/reviews/rpp2025-rev-ckm-matrix.pdf) |
| CKM zeta_12 phase and (N-4)nu scaling | With the same mixing angles, exact zeta_12=exp(i*pi/6) gives J=1.7097509558246345705944971779e-5. The measured phase has sin(1.147)=0.911534372341411. The residuals from nu=0.00508 +/- 0.0012 and (N-4)nu=0.02032 are 0.9064543723 and 0.8912143723, respectively. | PDG J=(3.12 +0.13/-0.12)e-5, delta=1.147 +/- 0.026; nu is a model input, not a PDG measurement. | The zeta_12 J residual from the PDG central value is -1.4102490442e-5, outside its one-sigma interval; neither nu nor 4nu approximates sin(delta) at the supplied tolerance. Interpreting these as the measured phase relation is **Tension**. |
| Electroweak trace ratio | Exact Tr(T3²)/Tr(Q²)=2/(16/3)=3/8=0.375. | PDG sin²(theta_W)(MZ) in MS scheme=0.23122 +/- 0.00006. Difference of central values: 0.14378. | The exact trace value is an ultraviolet normalization target; the MS value is at MZ. Direct comparison requires running and scheme matching, which this pipeline does not implement: **Scale-Dependent (RGE Required)**. [PDG physical constants](https://pdg.lbl.gov/2025/reviews/rpp2025-rev-phys-constants.pdf) |
| Rank-8 BSM1 kinematics | Exponent (1,0,0,0,0,0,0,0) maps to (2,0,0,0,0,0,0,0). With assumed nu=0.00508 and mDM=100 GeV, delta=nu*mDM=0.50800 GeV. Assuming sigma_eff=5e-49 cm² and kappa=nu implies sigma_0=sigma_eff/nu²=1.9375038750e-44 cm²; sigma_eff is 20 times below the stated approximate floor. | The attached brief supplies an approximate 1e-47 cm² reference floor. LZ 4.2 tonne-year results report their strongest SI exclusion, 2.2e-48 cm², at 40 GeV/c², not a scalar bound at 100 GeV. | Exponent doubling and mass-splitting multiplication are verified. Cross-section suppression assumes both kappa=nu and sigma_0; no detector likelihood, inelastic-recoil calculation, or significance calculation is implemented. Comparison to a 100 GeV experimental limit is **Conditional / Unverified Mapping**. [LZ 4.2 tonne-year result](https://journals.aps.org/prl/abstract/10.1103/4dyc-z8zf) |
| Void kurtosis and parity sign | (8-4)²=16. The fold maps sign pair (+1,-1) and (-1,+1) to the same product -1. | Supplied anchor: alleged VAST VoidFinder excess kurtosis 4.45 +/- 0.20. No primary source for this numerical statistic was identified in project data or the VAST software publication. | The numeric difference between factor 16 and the claimed central value is 11.55, but the test provides no map between these distinct quantities. The model code verifies the integer factor and sign-product equality; it contains no function converting 16 into kurtosis 4.45. Sign factors are not uniquely identified. The kurtosis identification is **Conditional / Unverified Mapping**; a claim of unique sign inference has **Tension** with OP20 degeneracy. [VAST toolkit paper](https://doi.org/10.21105/joss.04033) (software/method reference; it does not establish the quoted kurtosis value) |

## 4. Mathematical Scope and Non-Claims

### What the executed software verifies

- Exact rational multiplication, selected cocycle signs, noncommutativity witnesses, and the tested first-coordinate doubling endomorphism.
- Exhaustive images of the 128 basis coordinates under the implemented 5-to-4 parity fold; additivity and surjectivity on that finite coordinate basis, plus a counterexample to multiplicativity.
- The tested octonion and Dixon non-associativity witnesses; exact left-regular matrix dimensions; complementary Witt idempotents; and rational-span closure checks for the returned 8- and 3-element generator lists.
- Exact 16-state rational charge assignments, the listed anomaly traces, and the 3/8 trace ratio encoded by the selected T3, Y, and Q assignments.
- Exact Q(zeta_12) identities and controlled decimal evaluations for the specified inputs and precision; the numerical Koide and CKM residuals shown above.
- The sign-consistency booleans and the non-uniqueness of the two-factor sign decomposition.

### What it deliberately does not assert

- A physical assignment of the 16 Weyl states to minimal left ideals or specific Dixon coordinates. The state class records charges and does not implement such an identification.
- A derivation of Standard Model gauge groups from the candidate derivations; closure in an exact rational matrix span is an algebraic result, not a physical gauge identification.
- Renormalization-group evolution or scheme matching from the 3/8 ultraviolet trace ratio to the PDG MS value at MZ.
- That nu, the assumed unsuppressed cross section, or an effective direct-detection cross section is experimentally measured, or that the arithmetic establishes a 25-sigma exclusion or detector viability.
- A map from (N-4)² to an observed void-kurtosis statistic, a sourced VAST measurement of 4.45 +/- 0.20, or a unique determination of epsilon5 and the foliation arrow from their product.
- A full arbitrary-support Hahn–Mal'cev–Neumann division ring implementation. The runtime-rank lattice carrier is finite-support; the rank-one `RationalTwistedHahnSeries` is the documented rational-function subring. The `chi5-to-one-dixon` bridge specializes t=1; it is not the leading coefficient at a valuation.

## 5. Reproduction Details

From the repository root, run:

```bash
mvn -f groupimp/pom.xml clean
mvn -f groupimp/pom.xml test
```

The clean step prevents stale Surefire XML reports from affecting totals. The recorded run used Maven 3.9.11 with Java 17.0.4.1 and Surefire 3.2.5. The project targets Java source/bytecode level 8. Maven emitted a platform-encoding warning (Windows Cp1251); it did not affect compilation or test results.

Primary benchmark references: [PDG 2025 lepton summary](https://pdg.lbl.gov/2025/tables/rpp2025-sum-leptons.pdf), [PDG 2025 tau listing](https://pdg.lbl.gov/2025/listings/rpp2025-list-tau.pdf), [PDG 2025 CKM review](https://pdg.lbl.gov/2025/reviews/rpp2025-rev-ckm-matrix.pdf), [PDG 2025 physical constants](https://pdg.lbl.gov/2025/reviews/rpp2025-rev-phys-constants.pdf), and [LZ 4.2 tonne-year paper](https://journals.aps.org/prl/abstract/10.1103/4dyc-z8zf).
