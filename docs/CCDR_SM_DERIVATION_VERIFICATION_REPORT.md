# CCDR Standard Model Derivation: Verification Report

**Verification date:** 2026-11-22  
**Repository commit tested:** `d577dca`  
**Branch tested:** `codex/ccdr-v710-synthesis-v39-pipeline`  
**Build:** Java 17.0.4.1, Apache Maven 3.9.11, JUnit 4 / Surefire 3.2.5  
**Command:** `mvn -f groupimp/pom.xml test` after `mvn -f groupimp/pom.xml clean`

The header above is only the original 937-test audit on `d577dca`. Later `master` runs are separate:

| Run | Branch | Commit | Tests | Section |
| --- | --- | --- | --- | --- |
| Original structural and empirical audit | `codex/ccdr-v710-synthesis-v39-pipeline` | `d577dca` | 95 classes, 937 passed | 1-5 |
| Dynamical spectral action | `master` | `c2055e2` | 96 classes, 941 passed | 6 |
| Gauge, triality, and cosmology transfers | `master` | `d25b805` | 97 classes, 948 passed | 7 |
| Historical-generator invariance | `master` | `b8021d0` plus this audit | 98 classes, 951 passed, 0 failures, about 80 s | 8 |
| Historical-gauge derivation audit | `master` | `3203e8f9` → `7cbd17e2` | 99 classes, 969 passed, 0 failures | 8 |


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

## 6. Dynamical Spectral Action and Minimal Left-Ideal Verification

A later clean run, `mvn -f groupimp/pom.xml clean` then `mvn -f groupimp/pom.xml test`, executed **96 test classes and 941 tests: 941 passed, 0 failures, 0 errors, 0 skipped**. Maven reported about 77 seconds. The new class is `CCDRDynamicalDerivationTest` (4 tests). Sections 1–5 above remain the record of the earlier 95-class, 937-test structural and empirical audit. This section does not reopen those non-claims except where the new code changes the scope.

### Rank-16 Witt left ideal

`DixonMinimalLeftIdeal` builds sixteen Dixon vectors as occupation monomials times the Witt idempotent `(1+i_C l)/2`. Over Q their coordinate matrix has rank 16. Each vector is an eigenvector of the self-adjoint hypercharge, T3, and electric-charge operators on that span. The eigenvalues are exactly the one-generation table in `StandardModelWeylGeneration`, including Q=T3+Y, Tr(T3²)=2, and Tr(Q²)=16/3. The weak projector used beside the Witt vacuum is `(1+i_C i_H)/2`: quaternion i_H squares to −1, so the unit that squares to +1 is i_C i_H.

Every octonion-plane element `1/2(−e_u+i e_v)` with distinct imaginary basis units satisfies the canonical relations α²=0 and {α,α†}=1 inside this product. The charge operators are still the occupation-basis operators above. This run does not show that the eight `su(3)` stabilizer matrices and three quaternion adjoint matrices are simultaneously diagonal on these sixteen vectors.

### Finite Dirac operator and spectral traces

On the 128 rational coordinates, γ_F is the octonion grading involution, J_F swaps the two octonion grades and conjugates Q(i), and D_F pairs those grades across the C2 blocks. Exact matrix checks give

J_F² = +1, D_F² = +1, γ_F² = +1, J_F D_F = D_F J_F, J_F γ_F = −γ_F J_F, {D_F, γ_F} = 0.

The first-order condition holds for complex-scalar algebra elements. It is not claimed for a general nonassociative Dixon product. The symmetrized block-odd fluctuation H produces

Tr(D_A⁴) = 128 − 256 φ² + 128 φ⁴ = 128 (φ² − 1)².

The quartic coefficient is +128, so λ>0. The quadratic coefficient is −256, which is the tachyonic −μ²|Φ|² term before the quartic minimum at φ²=1. N_g=1 is the matrix size used here. Three generations are three copies of the 16-state ideal, 48 Weyl states, not a 384-dimensional matrix.

### One-loop weak angle

`OneLoopRGEFlow` uses b1=41/10, b2=−19/6, b3=−7, M_Z=91.1876 GeV, and the external electromagnetic datum α_em⁻¹(M_Z)=127.952. That second datum is not derived by the algebra; without it the unification scale is not fixed by sin²θ_W alone. At 20-digit working precision the solved boundary is

Λ_fold = 1.0310981847430148637×10^13 GeV, log10(Λ_fold)=13.013300022278532513, α_fold=0.023578074326987960369.

Running back, sin²θ_W(M_Z) reconstructs as 0.23122000000000000000, inside 10^−8 of the input 0.23122. The scale sits in the 10^13–10^15 GeV window. Exact beta-function rationals are registered on the `DynamicalSM` algebra and executed through `AlgebraFlow`.

### Triality and the Jarlskog scan

ζ_12⁴ has order 3. The coordinate cycle on il, jl, kl has order 3 and is not claimed to be an octonion automorphism. The 3×3 Yukawa diagonalizer, fed the PDG angles and δ=1.147 rad, returns J=3.11699351480×10^−5, agreeing with `HighPrecisionAlgebraMetrics` to better than 10^−12. The pure ζ_12 phase gives J=1.70975094186×10^−5, still outside the PDG one-sigma band. A grid that keeps the measured mixing angles and varies only the phase finds a nearby J=3.1592125×10^−5 at δ≈1.963 rad. That matches the Jarlskog central value more closely than ζ_12 does; it does not derive the measured phase or the mixing angles.

### Open questions

The embedded charges are eigenvalues of operators defined on this ideal basis. They are not yet the joint spectrum of the historical su(3) stabilizer and quaternion adjoint generators. The first-order condition is verified on Q(i), not on every Dixon element. Yukawa eigenvalue ratios are an ansatz, not a spectrum of D_A. The compactification scale uses an external α_em. Scheme conversion beyond one-loop beta functions is not implemented.

## 7. Gauge representation, graded fold, and cosmological transfers

A clean run after these additions executed **97 test classes and 948 tests: 948 passed, 0 failures, 0 errors, 0 skipped**, in about 93 seconds. `CCDRDerivationUpgradeTest` adds the checks below. The historical stabilizer and adjoint matrices are not identified with the occupation-basis gauge action. The ν-perturbed mass matrix does not move J or the Koide ratio into the PDG bands. The raw P-A26 formula does not reproduce 7.2 ± 0.6. Those items stay **Tension** or **Conditional**.

### Unified occupation-basis gauge action

`WittGaugeAction` builds exact 16×16 rational matrices on the Witt occupation basis: diagonal T₃ and Y, the rational weak swap Tₓ = σₓ/2 on each doublet, and a color 3-cycle on each quark triplet. Hypercharge commutes with T₃, Tₓ, and the color cycle. The color cycle commutes with T₃. Doublet Casimir T₃² + 2 Tₓ², using Tᵧ² = Tₓ², is 3/4; lepton singlets give 0. Color triplets are assigned Casimir 4/3 and leptons 0. The real 4×4 form of (σₓ²+σᵧ²+σ_z²)/4 on one doublet is exactly (3/4) I. The sixteen labels are

(1, 2)_{−1/2}, (3, 2)_{1/6}, (1, 1)_1, (1, 1)_0, (3̄, 1)_{−2/3}, (3̄, 1)_{1/3},

with three colors in each quark multiplet. These operators run through `DynamicalSM` as `su2-casimir-doublet` = 3/4 and `su3-casimir-triplet` = 4/3.

The same test lifts the eight historical octonion-stabilizer matrices and the three quaternion-adjoint matrices to the 128 rational coordinates and measures the squared length of the part orthogonal to the 16-dimensional span. That residual is **32, not zero**. The span is not invariant, so T₃ on the occupation basis is not the restriction of a historical quaternion Cartan. Status of that identification: **not established**.

### Triality automorphism and the perturbed spectrum

`RationalOctonion.orderThreeAutomorphism` cycles i→j→k→i and il→jl→kl→il, fixes 1 and l, has order 3, and preserves the tested products i·j and i·l. It is an automorphism of the Cayley–Dickson product, unlike the earlier cycle of il, jl, kl alone.

`TrialityFlavorModule.perturb(0.00508)` adds that catalogue residue to δ = π/6 and to a generation-permutation mass matrix. It does not insert PDG masses or δ = 1.147. At 12-digit rounding the outputs are

δ = 0.528678775598 rad, J = −5.20257470380×10⁻⁴⁶, Q = 0.333334404498.

J is not inside (3.12 +0.13/−0.12)×10⁻⁵. Q is not inside the τ one-sigma Koide window [0.666659, 0.666670]. A shift of size ν does not bridge π/6 to 1.147 rad. Status: **Tension**.

The first-order defect [[D_F, i_C], J_F b J_F⁻¹] is exactly 0 when b is i_C and when b is quaternion i_H. That is a checked slice of ℂ ⊗ ℍ, not a proof for every element of that subalgebra.

### Graded leading term and the oriented frame

`leadingTerm` on the finite-support lattice and on the rational Hahn subring keeps the minimal-grade piece. For single-term inputs, leading(xy) equals leading(x)·leading(y), including the lattice cocycle. This is not a multiplicative map of the full 5→4 parity fold onto DixonZ2(Q). The t=1 specialization remains a separate operation.

`CCDRChiralFold.fromOrientedBoundary(incidence, frameHandedness)` sets ε₅ to the incidence sign and the foliation arrow to the frame sign. `fromOrientedBoundary(−1,+1)` and `fromOrientedBoundary(+1,−1)` have the same chirality product and different factors. Supplying an ordered frame selects one factorization. The unordered product is still degenerate, so OP20 is not removed for that product. Status: **partial**.

### P-A26, P-A32, and BSM1

The raw sheet formula at N=8, ν=0.00508, and r_wall/r_grain=0.40 is

k₄ = 3 + (ν π²/6) (0.40)² (N−4)² = 3.02139203855.

(N−4)² = 16 and 16/6 = 8/3 are exact. The pre-registered window 6.4 < k₄ < 8.0 does not contain 3.021, so the raw value is `FAIL_3SIGMA` against that window. The quoted benchmark 7.2 ± 0.6 has excess 4.2 over 3. The ratio of that excess to the raw excess is **196.334724701**. That factor is an external normalization audit, not an output of the algebra. First-order propagation of the catalogue uncertainties δν=0.0012 and δ(r)=0.05 gives about 0.0104 on the raw k₄, which does not turn 3.021 into 7.2. Status of the 7.2 identification: **Conditional / formula mismatch**.

The model anisotropy axis and the model parity axis are both (0,0,1), so their absolute cosine is 1. An externally supplied cosine of 0 against a 3σ threshold 0.5 returns `FAIL_3SIGMA`. This checks the predicate. It is not a measurement of a CMB axis.

The rank-8 BSM1 endomorphism doubles the first exponent coordinate. That algebraic eigenvalue is 2, registered as `bsm1-doubling`. It is not equal to ν. The mass splitting Δ = ν m_DM = 0.508 GeV at the assumed inputs ν=0.00508 and m_DM=100 GeV remains a product of catalogue numbers. Status: **Conditional**.

## 8. Historical generators on the occupation ideal

Starting point: `master` at `3203e8f9aada9ffda7cfa6f50a78d01aafe34ea4` ("Record the historical-generator invariance obstruction."). Implementation commit: `7cbd17e29b839b06af952eb557f31fe4235acf7e`; this report is committed immediately after it. Java 17.0.4.1, Apache Maven 3.9.11, JUnit 4, Surefire 3.2.5. Baseline `mvn -f groupimp/pom.xml clean test` before any modification: BUILD SUCCESS, **98 test classes, 951 tests, 0 failures, 0 errors, 0 skipped**, Maven total 1:22. Final `mvn -f groupimp/pom.xml clean test` after the change: BUILD SUCCESS, **99 test classes, 969 tests, 0 failures, 0 errors, 0 skipped**, Maven total 2:28. No branch history was rewritten and nothing was force-pushed.

Changed files in this audit:

- `HistoricalIdealInvariance.java` (modified): column-span rank of every residual and the central complex structure restricted to the ideal.
- `WittGaugeAction.java` (modified): public full 128x128 lifts `liftOctonion`, `liftQuaternion`.
- `RestrictedColorStabilizer.java` (new): exact restriction, closure, structure constants, Jacobi, trace form, Casimir, decomposition, commutant.
- `HistoricalWeakSector.java` (new): adjoint versus left action, Casimir spectra, invariant closures.
- `DiracGaugeCompatibility.java` (new): commutators with `gamma_F`, `D_F`, `J_F`, and the Higgs direction.
- `DerivedGaugeObstruction.java` (new): aggregated status, no new projector or redefined generator.
- `CCDRGaugeResolutionTest.java` (new): 18 exact tests.

No Gell-Mann matrices, no Pauli matrices, and no new free parameters were introduced. The historical generators are used as they are; the only new constructions are the restriction (defined because the residual vanishes) and the exact analysis of the existing lifts.

### 8.1 Exact residual audit

\(B\) is the \(128\times16\) matrix whose columns are the occupation vectors, and \(P=B(B^TB)^{-1}B^T\). Exact checks: \(\operatorname{rank}B=16\), \((B^TB)(B^TB)^{-1}=I_{16}\), \(P\) fixes every column of \(B\), and \(P^2v=Pv\) for all 128 standard basis vectors. Status of the projector: **VERIFIED**.

For each historical generator \(G\), \(R_G=(I-P)GB\) is kept as the full \(128\times16\) matrix. Invariance is also tested without the projector as \(\operatorname{rank}[B\,|\,GB]=16\). The two tests agree for every generator.

| Generator | \(\operatorname{rank}R_G\) | \(\|R_G\|_F^2\) | \(\operatorname{rank}[B\,|\,GB]\) | Invariant |
| --- | ---: | ---: | ---: | --- |
| Octonion stabilizer 0-7 | 0 | 0 | 16 | yes |
| Quaternion adjoint 0, \(\operatorname{ad} i_H\) | 8 | 16 | 24 | no |
| Quaternion adjoint 1, \(\operatorname{ad} j_H\) | 0 | 0 | 16 | yes |
| Quaternion adjoint 2, \(\operatorname{ad} k_H\) | 8 | 16 | 24 | no |

The eight stabilizer residuals vanish. The two failing residuals each have rank 8 and squared norm 16; their sum is 32. Nine of eleven generators preserve the span. Minimal counterexample: \(\operatorname{ad} i_H\) applied to `e_L` has orthogonal component 1 at rational coordinate 48. Status of full historical invariance: **FALSIFIED** (unchanged).

### 8.2 Color: the eight stabilizers form a compact \(\mathfrak{su}(3)\)

Because all eight octonion-stabilizer residuals vanish, their restriction to the ideal is well defined:
\(D_a=(B^TB)^{-1}B^T G_a B\), a \(16\times16\) rational matrix. This is a restriction, not a projection of a failed generator.

Computed from the eight \(D_a\):

- **Independence.** The flattened 8-tuple has rank 8.
- **Closure.** All 28 commutators lie in the span; the number outside the span is 0.
- **Structure constants.** The exact tensor \(f_{abc}\) defined by \([D_a,D_b]=\sum_c f_{ab}{}^c D_c\) is computed over \(\mathbb{Q}\). It reproduces every commutator exactly, satisfies \(f_{ab}{}^c+f_{ba}{}^c=0\), satisfies the Jacobi identity for all index quadruples, and the lowered tensor \(f_{abc}=\sum_d f_{ab}{}^d g_{dc}\) is totally antisymmetric, as compactness requires. The full tensor is exposed by `RestrictedColorStabilizer.structureConstants()`.
- **Trace form.** On the 16-dimensional restriction,
  \[
  g=\begin{pmatrix}
  -96&0&0&48&0&0&0&0\\
  0&-288&0&0&0&0&0&0\\
  0&0&-288&0&0&0&0&0\\
  48&0&0&-96&0&0&0&0\\
  0&0&0&0&-72&0&0&0\\
  0&0&0&0&0&-288&0&0\\
  0&0&0&0&0&0&-288&0\\
  0&0&0&0&0&0&0&-288
  \end{pmatrix}.
  \]
  Its leading principal minors alternate in sign, so \(g\) is negative definite and the real form is compact. The 8-dimensional octonion trace form is one half of this matrix; its leading minors are \(-48,6912,-995328,\ldots,3851755393646592\).
- **Center and rank.** Center dimension 0, rank 2. A compact, centerless, 8-dimensional real Lie algebra of rank 2 is \(\mathfrak{su}(3)\) (type \(A_2\)); this identification uses the classification of compact simple Lie algebras, not a matching of matrices to Gell-Mann.
- **Casimir from the actual restricted matrices.** With the inverse trace form,
  - on the 8-dimensional octonion module: eigenvalues \(0\) (mult 2) and \(4/3\) (mult 6), trace 8;
  - on the 16-dimensional occupation module: eigenvalues \(0\) (mult 4) and \(2/3\) (mult 12), trace 8.
  The 16-dimensional trace form is twice the 8-dimensional one, so the normalized eigenvalue is halved; as an operator the 16-dimensional Casimir is \(\tfrac12(C_8\oplus C_8)\).
- **Two-copy decomposition.** In the adapted basis \([e_c v\,|\,j e_c v]\), every restricted matrix is block diagonal with two equal \(8\times8\) blocks. The real commutant has dimension 24, the octonion commutant dimension 6, and the common fixed subspace dimension 2.
- **Complex structure.** The central \(i_C\) preserves the ideal (coordinate rank 16) and commutes with all eight \(D_a\), so the real 16-dimensional module is complex dimension 8. The centralizer of \(i_C\) inside the commutant has dimension 16.
- **Decomposition without SM labels.** The real 8-dimensional octonion module has Casimir \(0\) on a 2-dimensional part and \(4/3\) on a 6-dimensional part, i.e. \((1\oplus1)\oplus(3\oplus\bar3)\) after complexification. The 16-real module is two copies, so its complexification is \(2\cdot3+2\cdot\bar3+4\cdot1\). The commutant dimension \(24=2^2+2^2+4^2\) is consistent with exactly that multiplicity pattern.

Status of color: the eight restricted matrices **VERIFIED** to form a compact \(\mathfrak{su}(3)\), with exact structure constants, Jacobi, total antisymmetry, compact trace form, and Casimir \(4/3\) on the octonion triplet. This is a derivation, not a label assignment. The complexification of the occupation module carries the same color multiplicities as one Standard Model generation, \(2\cdot3+2\cdot\bar3+4\cdot1\).

Caveat: the physical real module has complex dimension 8, not 16. The 16 complex states of a generation are not obtained from the 16 real occupation vectors unless one doubles by \(i_C\); that doubling is an additional assumption, not a consequence of the construction.

### 8.3 Weak: the adjoint action cannot contain a doublet

The three historical weak generators are \(\operatorname{ad} i_H\), \(\operatorname{ad} j_H\), \(\operatorname{ad} k_H\) on the quaternion factor. Their exact residuals are rank 8/norm 16 for \(i_H\) and \(k_H\), and rank 0/norm 0 for \(j_H\). Only the \(j_H\) adjoint preserves the 16-state ideal. Status of the three-generator action on the ideal: **FALSIFIED**.

The algebraic reason, computed exactly:

- The common kernel of the three adjoints has dimension 1 (the center \(\mathbb{R}\cdot1\)); the image has dimension 3 (\(\operatorname{Im}\mathbb{H}\)).
- The adjoint Casimir on \(\mathbb{H}\) has eigenvalues \(0\) (mult 1) and \(-8\) (mult 3). This is the real \(1\oplus3\) decomposition. The spin-\(\tfrac12\) Casimir is \(-3\); it does not occur.
- The adjoint representation of a compact group is integer-spin. Its complexification contains only integer-spin irreps, and tensoring with a commuting color representation cannot create a \(2\). Therefore **no invariant module built from the adjoint action, at any dimension, contains a weak doublet**. This is an exact obstruction, not a numerical accident.

The four routes considered:

- **A. Different idempotent or basis.** Any invertible change of basis of the same 16-state span leaves \(\operatorname{rank}[B\,|\,GB]\) unchanged (tested with a shear). A different idempotent does not change the fact that the adjoint action is \(1\oplus3\). Status: **NOT ESTABLISHED**.
- **B. Left multiplication by \(\operatorname{Im}\mathbb{H}\)** (the historically natural spinor action on the same quaternion factor). The \(4\times4\) left-multiplication generators have Casimir \(-3\) with multiplicity 4 on \(\mathbb{H}\), i.e. a real four-dimensional spinor whose complexification is \(2\oplus2\). It does contain the spin-\(\tfrac12\) Casimir, but it has no singlet and it does not preserve the 16-state ideal. Its invariant closure has 32 real dimensions with restricted Casimir trace \(-96\), so all 32 real dimensions are doublet-type and there is no singlet. It cannot by itself reproduce the lepton doublet plus charged-lepton singlet. Status: **NOT ESTABLISHED**.
- **C. Larger invariant module.** The smallest invariant closure of the ideal under the adjoint action has 32 real dimensions (\(=16\) complex). Its restricted adjoint Casimir trace is \(-192\), i.e. 24 triplet dimensions and 8 singlet dimensions (eight copies of \(3\) plus eight copies of \(1\)); there is still no doublet. Under left multiplication the closure is also 32 real dimensions, with 32 doublet dimensions and no singlet. Neither closure yields the Standard Model weak content, and neither is the 16-state ideal. Status: **FALSIFIED for the Standard Model**.
- **D. Impossibility proof for the historical adjoint generators.** Given above: the adjoint action is \(1\oplus3\), so no invariant module contains a \(2\). Status: **VERIFIED (negative result)**.

### 8.4 Joint representation and hypercharge

- **Cross-sector commutation.** The color derivations act on the octonion factor and the quaternion lifts on the quaternion factor; their 128x128 lifts commute exactly. But the weak adjoint does not act on the 16-state ideal, so there is no joint action there.
- **Hypercharge.** The occupation-dictionary \(Y\) is diagonal on the 16 states, with \(\operatorname{Tr}Y=0\) and \(\sum Y^3=0\). It fails to commute with **7 of the 8** derived color generators. Hence \(Y\) is not a color-invariant hypercharge for the derived color action, and \(Q=T_3+Y\) is not a derived operator. Status: **FALSIFIED**.
- **Exact eigenspaces.** The eigenspaces of the dictionary \(Y\) are the encoded charge multiplicities; they are not color singlets of the derived action.
- **Anomalies.** The symmetrized cubic color anomaly \(\operatorname{Tr}(D_a\{D_b,D_c\})\) vanishes identically and every \(D_a\) is traceless. The dictionary traces \(\operatorname{Tr}Y=0\), \(\operatorname{Tr}Y^3=0\) are the encoded ones, not anomalies of a derived joint representation. Status of derived anomaly cancellation: **NOT ESTABLISHED**.
- **Joint representation.** Status: **FALSIFIED** on the 16-state module. There is no derived hypercharge and no weak doublet.

### 8.5 Finite Dirac operator, Higgs, and spectral action

- **Color lifts.** For 5 of the 8 generators the lifts fail to commute with \(\gamma_F\), \(D_F\), and \(J_F\): the derivations change the octonion grade. Color gauge covariance of the finite Dirac operator is **FALSIFIED**.
- **Weak adjoint lifts.** The quaternion adjoints commute with \(\gamma_F\), \(D_F\), and \(J_F\) (0 failures). This compatibility is vacuous for the weak sector because the adjoint carries no doublet.
- **Higgs direction.** The block-odd Higgs direction has 5 color failures and 0 weak-adjoint failures. It is not covariant under the derived color action. `higgsDirectionIsAnsatz()` remains true.
- **Spectral action.** The trace identity \(\operatorname{Tr}(D_A^4)=128(\phi^2-1)^2\) and the spectral-action coefficients are unchanged and remain **CONDITIONAL** on the chosen Higgs direction. They are not a consequence of the derived representation.
- **First-order condition.** \([[D_F,i_C],J_F b J_F^{-1}]=0\) is still verified only for \(b=i_C\) and \(b=i_H\), not for the full Dixon algebra or all of \(\mathbb{C}\otimes\mathbb{H}\). Status: **CONDITIONAL**.

### 8.6 Unresolved physics (unchanged)

- Triality and CP remain unresolved.
- No measured CKM element, phase, or mass was inserted. The \(\nu\)-perturbed flavor result remains a failure: \(\delta=0.528678775598\), \(J\approx0\), \(Q=0.333334404498\). Status: **FALSIFIED**.
- P-A26 \(k_4\approx3.02139203855\) against the window \(6.4\)-\(8.0\) remains unresolved. Status: **TENSION**.
- No 196.33 factor was introduced, no refit, and no relabel.

### 8.7 Overall verdict

| Claim | Status |
| --- | --- |
| The eight historical stabilizers restrict to a compact \(\mathfrak{su}(3)\) | **VERIFIED** |
| Color content \(2\cdot3+2\cdot\bar3+4\cdot1\) | **VERIFIED on the complexification**; the physical module is complex dimension 8 |
| The three historical weak generators act on the 16-state ideal | **FALSIFIED** (only \(j_H\)) |
| A weak doublet from the adjoint action | **FALSIFIED** (no invariant module) |
| The left-multiplication alternative | **NOT ESTABLISHED** (\(2\oplus2\), no singlet) |
| A joint \(\mathrm{SU}(3)\times\mathrm{SU}(2)\) representation | **FALSIFIED** |
| Hypercharge commuting with the derived color action | **FALSIFIED** |
| Anomalies from a derived representation | **NOT ESTABLISHED** |
| Dirac/Higgs gauge covariance | **FALSIFIED** for color; **CONDITIONAL** for the spectral action |
| Spectral-action prediction | **CONDITIONAL** |

The historical Dixon/octonion construction does **not** derive the Standard Model \(\mathrm{SU}(3)\times\mathrm{SU}(2)\times\mathrm{U}(1)\) representation on the 16-state Witt occupation ideal. Color is genuinely \(\mathfrak{su}(3)\) and its complexified content matches the color multiplicities of one generation, but the weak adjoint cannot produce a doublet at any module size, the real module is half the required complex size, and the dictionary hypercharge is not color invariant. This is a rigorous negative result, not a numerical shortfall.

The cosmological statements in Section 7 are unchanged. The raw sheet value remains about 3.021392, the audit factor remains about 196.33, and the BSM1 doubling remains 2 rather than \(\nu\).
