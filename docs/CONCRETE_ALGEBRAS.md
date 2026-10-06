# Concrete algebras connected to MathTool

`new ConcreteMathematics()` creates real `Algebra<T>` instances and registers their native operations in the existing `MathTool`. It includes N, Z, Q, Q(i), H(Q), Boolean, Q^2, Mat2(Q), Vec(Q), Mat(Q), Vec(Z), Mat(Z), Affine(Q), Tensor(Q), Exterior(Q), Q[x], Q(x), Poly(Q), PolynomialMap(Q), PolynomialForm(Q), PolynomialCell(Q), PolynomialChain(Q), S3, Z/6Z, finite sets of integers, rational samples, finite integer probability measures and stochastic kernels, finite simplicial complexes and vertex maps with constructive integral homology, relative pairs and their maps, ordered covers and their maps with natural Mayer-Vietoris/excision diagrams, integral and relative cochains with cup products and natural contravariant cohomology sequences, typed integral and relative chains with connecting cycles, pairing and cap products, finitely generated abelian-group types, explicit abelian presentations, quotient elements and homomorphisms, finite integer relations/functions, finite categories/functors/natural transformations, and F5 (named Z/5Z). Each construction owns its algebra instances; separate tools do not share mutable registrations.

`new ConcreteMathematics(3, 5, 7)` instead uses dimension three and includes both prime fields. Dimension must be positive for the matrix algebra. Each prime is checked exactly; composite or duplicate field parameters are rejected.

The default initializer currently installs 1644 native operations from 71 algebra builders.

## Existing API usage

```java
ConcreteMathematics math = new ConcreteMathematics();
MathTool tool = math.mathTool;

Algebra<Rational> q = math.rationals.algebra(); // the instance in tool.getAlgebra("Q")
Rational value = q.buildAlgebraItem(Rational.of(1, 2))
        .performOperation("add", Rational.of(1, 3))
        .perform().getResult(); // 5/6
```

The legacy item API queues same-carrier operations; call `perform()` before reading their result. Transfers and custom-result operations evaluate the pending chain. Flow `collect()` evaluates it automatically.

```java
List<String> values = math.flow(math.naturals,
        Arrays.asList(BigInteger.ONE, BigInteger.valueOf(2)))
        .performOperation("add", BigInteger.ONE)
        .<BigInteger>performAlgebraTransfer("to-integer")
        .<Rational>performAlgebraTransfer("to-rational")
        .performOperation("divide", Rational.of(2))
        .collect(); // ["1", "3/2"]
```

`ListAlgebraInput<T>` is also available for constructing an AlgebraFlow with an existing MathTool initializer. It copies a finite input list, checks membership, and rejects a different algebra instance.

## Registered operation names

| Class / carrier | Same-carrier binary operations | Unary operations/transfers | Mixed/custom-result/flat operations |
| --- | --- | --- | --- |
| FiniteIntegerFunctionAlgebra / FiniteFunction(Z,Z) | partial compose | inverse; domain/codomain/range; graph; size; injective/surjective/bijective; flat values | apply; image/preimage; restrict; flat fibers; identity-on; from-relation |
| FiniteCategoryAlgebra / FiniteCategory | equality -> Boolean | opposite; objects/arrows; counts; groupoid/thin; underlying relation; flat isomorphisms/initial/terminal objects | source/target/identity; composition; flat hom/inverses/endomorphisms; discrete and preorder constructions |
| FiniteFunctorAlgebra / FiniteFunctor | compose, equality -> Boolean | strict inverse; opposite; source/target categories; full/faithful/equivalence checks; object/arrow maps; flat images | map-object/map-arrow; flat object/arrow fibers; identity-on; from-discrete-map; constant-at; empty-diagram |
| FiniteNaturalTransformationAlgebra / FiniteNaturalTransformation | vertical compose, horizontal, equality -> Boolean | inverse; opposite; source/target functors; component-map; flat components | component; flat component-fiber; precompose/postcompose by a functor; identity-on |
| FiniteEquivalenceAlgebra / FiniteEquivalence | compose; equality -> Boolean | inverse; opposite; source/target categories; forward/backward functors; unit/counit transformations | identity-on; from-functor |
| FiniteAdjunctionAlgebra / FiniteAdjunction | compose; equality -> Boolean | opposite; source/target categories; left/right functors; unit/counit transformations; is-equivalence; to-equivalence | identity-on; from-equivalence; from-left; from-right; transpose; untranspose; hom-map |
| FiniteConeAlgebra / FiniteCone | equality -> Boolean; lift -> Z; flat mediators -> Z | diagram; vertex; leg-map; flat legs; natural-transformation; is-limit | leg; reindex/map by a functor; limit; flat cones-at; from-transformation |
| FiniteCoconeAlgebra / FiniteCocone | equality -> Boolean; descend -> Z; flat mediators -> Z | diagram; vertex; leg-map; flat legs; natural-transformation; is-colimit; opposite -> Cone | leg; reindex/map by a functor; colimit; flat cocones-at; from-transformation; opposite-cone |
| FiniteIntegerRelationAlgebra / FiniteRelation(Z,Z) | union, intersection, compose | inverse, transitive-closure; domain/range -> finite set; cardinality -> N | image/preimage -> second set carrier; contains/function checks -> Boolean; finite identity |
| FiniteSimplicialAlgebra / FiniteComplex | union, intersection | dimension/Euler characteristic -> Z; vertex-count -> N; flat integral homology types and F2/rational Betti numbers | equality/subcomplex -> Boolean; skeleton; degree-indexed simplex count, integral homology, oriented integer boundary matrix and F2/rational Betti number; flat integral boundary invariant factors |
| AbelianGroupTypeAlgebra / AbelianGroupType | direct-sum, tensor-product, hom-group, tor1, ext1; equality -> Boolean | free rank; minimal generator and torsion factor counts; finite/cyclic/trivial/torsion-free checks; finite order/exponent; torsion/free parts; flat invariant factors | repeat by N -> first group carrier; free-on and cyclic from N; trivial and Z constants |
| PresentedAbelianGroupAlgebra / PresentedAbelianGroup | direct-sum; presentation equality/isomorphism -> Boolean | relation matrix -> Mat(Z); isomorphism type; generator/relation counts; finiteness and finite order | construction from relation matrix or group type; trivial presentation |
| AbelianGroupElementAlgebra / AbelianGroupElement | add/subtract within one presentation; equality -> Boolean | negate; retained group; Smith coordinates/original representative -> Vec(Z); zero/torsion checks; finite element order; flat cyclic subgroup | integer scale; flat multiplication-preimages; projection and Smith-coordinate construction; flat original/minimal generators and finite elements; reduction -> second Vec(Z) carrier |
| AbelianGroupHomomorphismAlgebra / AbelianGroupHomomorphism | compose/add/subtract; equality -> Boolean | negate; source/target; Smith and original matrices; flat generator images; kernel/image/cokernel presentations and maps; injectivity/surjectivity; inverse | integer scaling; apply/preimage -> second element carrier; existence of preimages; construction from matrices and paired presentations; identity and zero maps |
| IntegralHomologyAlgebra / IntegralHomology | retained-boundary equality -> Boolean | boundary/cycle matrices; presented group/type; ranks; flat cycle/boundary bases and class representatives; cycle-to-homology projection | consecutive-boundary and simplicial-degree construction; cycle/boundary tests; class-of and representative; bounding chains; maps induced by degree matrices |
| FiniteSimplicialMapAlgebra / SimplicialMap | compose; equality/contiguity -> Boolean | inverse; source/target/image; vertex function; injectivity and simplex/vertex surjectivity; flat vertex images, chain matrices and homology maps | construction from finite functions and complex pairs; inclusions; restrictions; simplex images/fibers; degreewise chain/homology maps; ordered simplex bases |
| RelativeSimplicialAlgebra / RelativeComplex | labelled-pair equality -> Boolean | ambient/subcomplex; dimension and Euler characteristic; flat boundaries and constructive homology by degree; inclusion simplicial map | pair construction; quotient chains and bases; homology and torsion types; inclusion, quotient, lift and connecting matrices; scalar/flat long exact sequence maps |
| RelativeSimplicialMapAlgebra / RelativeMap | compose; equality/pair contiguity -> Boolean | inverse; source/target pairs; ambient/subcomplex maps; image/corestriction; flat relative chain and homology maps | construction from an ambient map and pair boundaries; bounded pair-isomorphism decisions, witnesses and enumeration; identities/inclusions; extensions/restriction; integral maps and naturality lists |
| SimplicialCoverAlgebra / SimplicialCover | ordered-cover equality -> Boolean | left/right/union/intersection; swap; Euler characteristic; flat sum boundaries/coboundaries and homology/cohomology; excision RelativeMap | cover construction; direct-sum homology/cohomology and component maps; signed intersection/addition/splitting and restriction/difference matrices; scalar/flat homological and cohomological Mayer-Vietoris maps |
| SimplicialCoverMapAlgebra / CoverMap | compose; equality/piecewise contiguity -> Boolean | inverse; source/target covers; union/left/right/intersection maps; swap both covers; image/corestriction; flat sum matrices/maps; two excision RelativeMaps | construction from a union map and paired covers; identity/inclusion; restriction; degreewise sum/component homology and contravariant cohomology maps; four-map Mayer-Vietoris naturality lists for each theory |
| RelativeSimplicialChainAlgebra / RelativeChain | pair/degree-checked add/subtract; equality/homology comparison -> Boolean | boundary; negate; pair/degree/coordinates; cycle/boundary predicates; fillings; homology/class; flat cycle generators; absolute lift and connecting cycle | projection from absolute chains; integer scale; pair-map pushforward; relative pairing; both standard relative cap products and their matrices/induced maps; zero and flat basis constructors |
| RelativeCapProductAlgebra / RelativeCap | equality of retained chain and target -> Boolean | chain; target pair; boundary | bind a chain to an explicit target pair; replace chain; general relative cap, class, matrices and induced homology/cohomology maps |
| RelativeSimplicialTripleAlgebra / RelativeTriple | equality of the full nested triple -> Boolean | outer, total and inner pairs; inclusion and quotient pair maps | quotient chain/cochain matrices; homology and cohomology maps; flat exact segments; typed connecting cycles/cocycles |
| RelativeSimplicialTripleMapAlgebra / TripleMap | compose; equality/contiguity -> Boolean | inverse; source/target triples; ambient and three pair maps; isomorphism; image/corestriction | construct from a vertex map and ordered triples; inclusion/restriction; six induced integral maps; flat four-map naturality lists |
| SimplicialHomotopyAlgebra / SimplicialHomotopy | ordered endpoint equality -> Boolean | endpoint maps; source/target pairs; reverse; flat chain/cochain matrices | construct between contiguous absolute or relative maps; degreewise prism matrices; typed chain/cochain actions through second operand wrappers |
| SimplicialHomotopyPathAlgebra / HomotopyPath | chronological concatenation; full stage equality -> Boolean | endpoints/pairs; reverse; step count; flat stages, steps and chain/cochain matrices | stationary and one-step constructors; append a map; precompose/postcompose all stages; accumulated prism matrices and typed actions |
| SimplicialSubdivisionAlgebra / SimplicialSubdivision | equality of full original pairs -> Boolean | original/subdivided pairs; vertex count; flat face dictionary; last-vertex map | pair/complex constructors; dictionary lookups; induced subdivision maps; naturality witnesses; scalar/flat inverse integral maps; signed subdivision chain/cochain actions and explicit inverse homotopies |
| SimplicialCollapseAlgebra / SimplicialCollapse | equality of source pair and free face -> Boolean | source/reduced pairs; free face/coface; inclusion; flat chain/cochain matrices and homotopies | compatible free-face search and constructors; signed chain retraction and dual cochain actions; typed homotopies; scalar/flat inverse integral maps |
| SimplicialCollapseSequenceAlgebra / CollapseSequence | chronological then; full step equality -> Boolean | original/final pairs; flat steps/stages; step count; terminal check; inclusion; flat matrices | identity and one-step constructors; greedy reduction; exhaustive collapse decisions/witnesses/enumeration; append; composite chain/cochain actions and homotopies; scalar/flat inverse integral maps |
| SimplicialHomotopyEquivalenceAlgebra / HomotopyEquivalence | composition; full map/witness equality -> Boolean | inverse; maps, pairs and homotopy witnesses | supplied inverse witnesses; compatible vertex collapses and strong-core reduction; absolute strong-type decisions and constructed equivalences; inverse integral maps |
| SimplicialChainAlgebra / SimplicialChain | context-checked add/subtract; equality/homology comparison -> Boolean | boundary; negate; complex/degree/coordinates; cycle/boundary predicates; fillings; homology/class; flat cycle generators; augmentation | integer scale; coordinates; pushforward; pairing; cap and cap-class; fixed-chain/cochain cap matrices and induced maps; zero and flat basis constructors |
| SimplicialCochainAlgebra / SimplicialCochain | same-context add/subtract; cup; equality/cohomologous -> Boolean; cup-class -> AbelianGroupElement | negate; complex/degree/coordinates; coboundary; zero/cocycle/coboundary tests; cohomology model/class; cobounding coordinates; flat cocycle generators | degreewise zero/basis construction and unit; integer scaling; coordinate replacement; representative; evaluation; pullback; scalar/flat cohomology models and contravariant maps |
| RelativeSimplicialCochainAlgebra / RelativeCochain | same-pair add/subtract; cup on union pairs; equality/cohomologous -> Boolean; cup-class -> AbelianGroupElement | negate; pair/degree/coordinates; coboundary; cocycle/coboundary tests; cohomology model/class; primitives; flat cocycle generators; absolute extension | zero/basis construction; absolute conversion; scaling/coordinates/representative/evaluation; pair-map pullback; connecting cocycle; scalar/flat relative cohomology and natural exact-sequence maps |
| RationalSampleAlgebra / Sample(Q) | concatenate | size -> N, mean/variance -> Q, center | covariance -> Q; scale by Q; flat transfer elements -> Q |
| FiniteProbabilityAlgebra / FiniteDistribution(Z) | — | support -> finite set, support-size -> N, expectation/variance -> Q | event/point probability -> Q; conditioning; flat transfer outcomes -> Z; point mass from Z |
| FiniteMarkovAlgebra / FiniteMarkov(Z) | typed compose; equality -> Boolean | domain/codomain; counts; row distributions; deterministic function and matrix conversions; communicating/recurrent classes; stationary extremes and unique stationary law | apply to a distribution -> second carrier; powers; flat marginal orbits; transition probabilities; time reversal; detailed balance; absorbing events; hitting probabilities and mean times -> Vec(Q) |
| IntegerSetAlgebra (extends FiniteSetAlgebra) / FiniteSet(Z) | union, intersection, difference, symmetric-difference, complement-in | cardinality -> N | subset/equal -> Boolean; contains; insert/remove; unary flat subsets; flat transfer elements -> Z |
| NaturalSemiring / N | add, multiply | successor, to-integer | None |
| IntegerRing / Z | add, subtract, multiply, gcd, lcm, quotient, remainder | negate, to-rational | greater/equal -> Boolean; divide-rational -> Q; flat quotient-remainder |
| RationalField / Q | add, subtract, multiply, divide | negate, inverse | greater/equal -> Boolean; flat add-subtract |
| PrimeField / Z/pZ | add, subtract, multiply, divide | negate, inverse | Fixed prime membership, exact residues |
| ResidueRing / Z/nZ | add, subtract, multiply, unit-only divide | negate, unit-only inverse, is-unit, is-zero-divisor, lift to Z | integer powers; reduction from Z; flat solve-multiply; enumeration |
| RationalFunctionField / Q(x) | add, subtract, multiply, divide, compose | negate, inverse, derivative, numerator/denominator | evaluate at Q; equality; polynomial embedding |
| RationalMultivariatePolynomialAlgebra / Poly(Q) | dimension-checked add, subtract, multiply; equality -> Boolean | negate; degree -> Z; variable/term counts -> N; Laplacian; constant-part -> Q; flat partials, variables, terms and coefficients | partial, primitive by variable index; rational scale; natural powers; evaluate at Vec(Q) -> Q; directional derivative; gradient-at -> second vector carrier; hessian-at -> Mat(Q); Q[x] conversions |
| RationalPolynomialMapAlgebra / PolynomialMap(Q) | shape-checked add, subtract, compose; equality -> Boolean | negate; input/output dimensions; flat components; divergence -> Poly(Q); curl; constant-part -> Vec(Q); linear-part -> Mat(Q) | evaluate -> second vector carrier; jacobian-at -> Mat(Q); partial/component by index; rational scale; scalar gradient and embedding; matrix embedding; simultaneous substitution into a scalar polynomial |
| PolynomialDifferentialFormAlgebra / PolynomialForm(Q) | dimension-checked add, subtract, wedge; equality -> Boolean | exterior-derivative; Hodge star; grade involution; scalar-part -> Poly(Q); flat terms, coefficients, masks, degrees and coordinate differentials | rational/polynomial scaling; pullback, insertion and Lie derivative by PolynomialMap(Q); evaluation -> Exterior(Q); grade selection; polynomial, exterior and vector-field conversions |
| PolynomialCellAlgebra / PolynomialCell(Q) | oriented product; equality -> Boolean | parameter/ambient dimensions; flat faces and vertices; map/point conversions | lower/upper faces by axis; segment from two vectors; evaluation -> second Vec(Q) carrier; polynomial pushforward; form integration -> Q |
| PolynomialChainAlgebra / PolynomialChain(Q) | dimension/degree-checked add, subtract; oriented product; equality -> Boolean | negate; boundary; integer degree; ambient dimension; cell count; flat cells and coefficients; cell embedding/boundary | rational scale; coefficient lookup; polynomial pushforward; homogeneous-form integration -> Q |
| SymmetricGroup / Sn | compose | inverse, order, sign, fixed-point-count, flat cycles | apply and flat orbit at N; integer powers; equality; enumeration |
| BooleanAlgebra / Boolean | and, or, xor, implies, equal | not | None |
| RationalComplexField / Q(i) | add, subtract, multiply, divide | negate, conjugate, norm-squared -> Q | Q(i).embed-rational on Q -> Q(i) |
| RationalQuaternionAlgebra / H(Q) | add, subtract, Hamilton multiply, left/right division; equal/same-rotation -> Boolean | negate, conjugate, inverse; norm-squared/real-part -> Q; imaginary-part -> Vec(Q); flat components -> Q | rational/complex embeddings; pure vector conversions; rotations -> Vec(Q); exact rotation matrix conversions; zero/one/i/j/k |
| RationalVectorSpace / Q^n | add, subtract | negate | scale by Q -> Q^n; dot -> Q; flat scale-flat and scale-signs |
| RationalMatrixAlgebra / Matn(Q) | add, subtract, multiply | negate, transpose, inverse, determinant -> Q, trace -> Q, rank -> N; characteristic/minimal polynomials; flat rational eigenvalues and diagonalization | scale by Q; apply to Q^n; solve with nonsingular matrix; polynomial evaluation; natural powers; ordinary/generalized eigenspace bases and eigenvalue multiplicity |
| RationalVectorFamily / Vec(Q) | partial add, subtract, dot -> Q | negate; dimension -> N; flat entries -> Q; zero-like | scale by Q; fixed-carrier conversions; empty vector constant |
| IntegerVectorFamily / Vec(Z) | partial add, subtract, dot -> Z; equality -> Boolean | negate; dimension -> N; flat entries -> Z; zero-like; rational embedding | integer scaling; exact conversion from integral rational vectors; empty vector constant |
| IntegerMatrixFamily / Mat(Z) | shape-checked add, subtract, multiply; equality -> Boolean | transpose; shape counts; flat rows/columns; Smith form and flat [U,D,V] decomposition; integral kernel/image bases; cokernel -> AbelianGroupType; rank/nullity; unimodular inverse | integer scale; apply/solve-particular -> second Vec(Z) carrier; flat solve-generators; solvability -> Boolean; exact rational conversions |
| RationalMatrixFamily / Mat(Q) | partial add, subtract, multiply; equal -> Boolean | transpose, RREF, pseudoinverse, row/column projectors; rank/nullity -> N; flat pivot columns and linear-space bases; rows, columns, shape counts; characteristic/minimal polynomials; rational spectra and diagonalization | scale; apply/project-column/least-squares-minimum-norm/least-squares-residual -> Vec(Q); least-squares-error -> Q; fixed-carrier conversions; zero-like; square inverse/determinant/trace; matrix polynomial evaluation, powers, companion matrices, eigenspace bases and multiplicity |
| RationalAffineSpaceAlgebra / Affine(Q) | equal -> Boolean | particular/minimum-norm -> Vec(Q); flat directions -> Vec(Q); dimension/ambient-dimension -> N; is-empty/is-unique | solve/least-squares Mat(Q) x Vec(Q) -> Affine(Q); contains -> Boolean; at/closest-point -> Vec(Q) |
| RationalTensorAlgebra / Tensor(Q) | add, subtract, Hadamard and tensor products; dot -> Q; equal -> Boolean | negate; order/size -> N; flat shape -> N; flat entries -> Q; norm-squared -> Q; zero-like | scale by Q; axis-pair swap/contraction; scalar/vector/matrix conversions; tensor-product unit |
| RationalExteriorAlgebra / Exterior(Q) | add, subtract, wedge; dot -> Q; equal -> Boolean | negate, grade involution, reverse, Hodge star; dimension/term count -> N; flat degrees/masks -> N, coefficients -> Q, terms -> Exterior(Q); scalar/vector transfers | scale; grade selection; vector insertion; induced matrix action; dimension-indexed zero/unit/volume |
| RationalPolynomialRing / Q[x] | add, subtract, multiply | negate, derivative; flat rational roots | derivative-order with N; evaluate at Q -> Q; primitive/integrate; iterate and flat orbit with Pair(Q,N); rational-root multiplicity -> N |

Each algebra registers zero/one constants where applicable on Unit with qualified names such as `Q.zero`, `Z.one`, `Mat2(Q).one`. Vector spaces have zero. Constants are Unit -> carrier transfers.

Ordinary binary names use `performOperation(name, second)`. Same-algebra unary operations use `performOneOperandOperation(name)` or `performOperation(name)`, backed by IOneOperandOperation. Cross-algebra unary operations use ITransferOperation and `performAlgebraTransfer`.

Same-input/different-result operations such as dot use ICustomResultOperation. Mixed-input operations returning the first type use ICustomMemberOperation and `performCustomMemberOperation`; their flat form uses `performFlatCustomMemberOperation` on flows. Mixed-input operations returning the second type use ILeftProjectionOperation and `performLeftProjectionOperation`; their flat form uses `performLeftProjectionFlatOperation`. Despite the historical interface name, its result is now `IAlgebraItem<B>` for A x B -> B. The flat interface returns `List<IAlgebraItem<B>>`. Fully independent result types use IUnsafeOperation, as in the existing architecture.

Implementations live directly under `operations/simple` and `operations/flat`. Concrete algebra builders live under `algebra/concrete`. They build the original Algebra, add IValidationRule implementations, and register the native operations in MathTool. They do not execute through Domain, Signature, Outcome, or the separate checked-operation prototype. OperationRegistration is a read-only description for reports; it is not an executor. ConcreteMathematics implements IMathToolInitializer.

## A x B -> A and its flat version

These transformations implement the existing ICustomMemberOperation and ICustomMemberFlatOperation interfaces:

```java
RationalVector v = new RationalVector(Rational.ONE, Rational.of(2));
List<String> scaled = math.flow(math.vectors, Collections.singletonList(v))
        .performCustomMemberOperation("scale", Rational.of(2))
        .performFlatCustomMemberOperation("scale-flat", Rational.of(3))
        .collect(); // ["[6, 12]"]
```

`scale-flat` returns one transformed vector. `scale-signs` returns the ordered pair of positive and negative scaled vectors, preserving duplicates at zero. The opposite-order operation Q x Q^n -> Q^n is registered on Q as `Q^n.scale-left`.

Polynomial integration demonstrates three independent carrier types: Q[x] x (Q x Q bounds) -> Q. Bounds are a Pair of exact rationals, so integration does not disguise both inputs as one Java type.

## Laws, partiality and integration limits

These are concrete mathematical structures with explicit law descriptions (not machine-checked proofs). Tests exercise exact arithmetic, ring/field identities, Boolean identities, closure, shape/modulus rejection, noncommutative matrix multiplication and calculus identities. Law descriptions remain declarations; tests do not claim formal proof.

Division by zero and singular inversion are undefined. Natural subtraction is intentionally absent because N is not closed under it. Integer quotient truncates toward zero; quotient-remainder returns that quotient followed by the corresponding signed remainder. Q(i) is a proper subfield of C. Polynomial integration is in the stated rational scope.

Existing `buildAlgebraItem` returns null for invalid membership; legacy operand validation throws NotMemberException. Native operation implementations validate their inputs and results through Algebra. Undefined exact arithmetic uses MathFailure; membership failures use NotMemberException.

See [ConcreteAlgebrasTest](../groupimp/src/test/java/mathematics/ConcreteAlgebrasTest.java) for complete working imports and [the executable example](../groupimp/src/main/java/mathematics/examples/ConcreteAlgebrasExample.java). The existing flat-transfer signature now correctly returns a list of wrapped results, and its flow lookup uses the flat-transfer registry. Native local flows have a serialization round-trip test; distributed scheduling remains outside that test.



Same-algebra unary flat operations use `IOneOperandFlatOperation` and `performOneOperandFlatOperation(name)` (or `performFlatOperation(name)`). Finite-set `subsets` is an example. Cross-algebra unary flat operations use the existing `ITransferFlatOperation`, whose return type is corrected to `List<IAlgebraItem<V>>`; `elements` transfers a finite set to its member algebra. Empty results remain valid, and every emitted member keeps its target algebra.


The default initializer currently installs 48 algebras and 952 named operations. Every registered operation is exercised through its native interface with an independently specified expected result in ConcreteAlgebrasTest. Sample statistics distinguish population and sample denominators; finite probability measures retain normalized rational masses. Conditioning on probability zero is undefined. Distributions retain their actual outcome Algebra, so two different carriers with the same Java member class are not silently identified.

For overloaded custom-member and unsafe operations, the most specific compatible second-operand class is selected (exact matches take priority). Re-registering the same second class replaces that overload. Ambiguous supertypes are rejected. Mathematical domains sharing one Java class need distinct operation names; overload selection does not infer a domain from a value.

Fixed `Matn(Q).solve` returns the exact vector solving M x = b for nonsingular square M. Singular matrices are outside that operation; the affine solution operation below handles them. `rank` returns a member of N. `derivative-order` accepts arbitrary nonnegative BigInteger orders and returns zero when the order exceeds the finite polynomial degree.

## Finite exact Markov kernels and chains

`math.markovKernels` registers `FiniteMarkov(Z)`: exact stochastic kernels between two explicit finite integer state sets. Each source state has a FiniteDistribution(Z) row with nonnegative rational masses summing exactly to one. Rows use the same actual integer Algebra as the probability carrier. Both boundaries are retained, including unused target labels, and labels are canonicalized into increasing integer order. Row support alone does not determine a kernel's codomain.

`from-matrix` accepts a Mat(Q) member and the existing finite-function boundary pair; row and column counts must match the sorted source and target sets. Entries must be nonnegative and rows must sum exactly to one. `to-matrix` preserves this ordering. `from-function` embeds a finite function as Dirac rows; `to-function` requires every row to be Dirac. `identity-on` is registered on FiniteSet(Z); `identity-on-domain` and `identity-on-codomain` work directly on a kernel. Empty sources are allowed, including the empty identity, but cannot transfer to the current positive-shape matrix carrier.

`compose(K,L)` applies L first, then K, and requires exact equality of the middle declared sets and outcome Algebra. For row-stochastic matrices its order is P_L*P_K. `apply` advances a distribution as pi*P, requiring support within the source set. It uses ILeftProjectionOperation and returns the existing FiniteDistribution(Z) wrapper. `row` returns one conditional law, while flat `rows` preserves duplicate row laws. `power` and flat `orbit` require equal source and target sets. An orbit contains the initial distribution followed by each time marginal, retaining repetitions; it does not generate a random sample path.

For a chain, `communicating-classes` uses the graph of positive-probability edges. `recurrent-classes` selects closed classes; `transient-states` returns their complement. `stationary-extremes` emits one exact stationary law per recurrent class, ordered by the class's least label. Their rational convex mixtures give all rational stationary laws. Scalar `stationary` requires exactly one recurrent class, including cases with transient states. Periodic chains can have a unique stationary law without their successive distributions converging to it. These conventions follow [QuantEcon's Markov-chain definitions](https://quanteconpy.readthedocs.io/en/latest/markov/core.html).

`is-stationary` checks pi*P=pi. `is-reversible` checks detailed balance, including candidate distributions with zero masses. `reverse` requires a stationary law strictly positive at every declared state, then computes Q_ij=pi_j*P_ji/pi_i. With a zero-mass state, its reverse row is not determined by this formula, so the operation is undefined.

`absorbing-on` replaces event-state rows with self-transitions. `hitting-probabilities` returns the eventual probability of first reaching an event from each state, in sorted label order. It includes time zero: event-state entries are one and unreachable-state entries are zero. Reachability fixes the zero boundary before an exact rational linear system is solved. `mean-hitting-times` has event-state entries zero and solves the corresponding first-step equations. It returns a Vec(Q) only when every starting state's mean is finite; otherwise it raises OPERATION_UNDEFINED because Q cannot hold infinity. A probability vector here need not sum to one, so it is not a distribution carrier member.

```java
FiniteSet<BigInteger> states = FiniteSet.of(BigInteger.ZERO, BigInteger.ONE);
FiniteMarkovKernel kernel = math.markovKernels.fromMatrix(new RationalMatrix(new Rational[][] {
        {Rational.of(1,2), Rational.of(1,2)},
        {Rational.of(1,4), Rational.of(3,4)}}), states, states);
List<String> stationary = math.flow(math.markovKernels, Collections.singletonList(kernel))
        .<FiniteDistribution<BigInteger>>performAlgebraTransfer("stationary")
        .collect(); // ["Distribution{0=1/3, 1=2/3}"]
List<String> meanTimes = math.flow(math.markovKernels, Collections.singletonList(kernel))
        .<RationalVector,FiniteSet<BigInteger>>performAlgebraUnsafe("mean-hitting-times",
                FiniteSet.of(BigInteger.ONE))
        .collect(); // ["[2, 0]"]
FiniteDistribution<BigInteger> initial = new FiniteDistribution<>(math.integers.algebra(),
        Collections.singletonMap(BigInteger.ZERO, Rational.ONE));
List<String> marginals = math.flow(math.markovKernels, Collections.singletonList(kernel))
        .<FiniteDistribution<BigInteger>,Pair<FiniteDistribution<BigInteger>,BigInteger>>performFlatAlgebraUnsafe(
                "orbit", new Pair<>(initial, BigInteger.valueOf(2)))
        .collect(); // point mass at 0; [1/2,1/2]; [3/8,5/8]
```

The empty chain has no stationary laws, no communicating classes, and empty hitting vectors; its scalar stationary operation is undefined. Boundary mismatch, out-of-domain states, or an unsupported finite/infinite result is OPERATION_UNDEFINED. Invalid constructed rows are INVALID_MEMBER. Limits are 64 states per boundary, 10000 power/orbit steps, and a conservative 5000000-unit work budget per whole computation. Composition and powers share the budget, and orbits preflight their full cost. Dense shape-based costs also apply to sparse rows. Exhaustion is IMPLEMENTATION_FAILURE; coefficient bit lengths remain unbounded.

NativeMarkovTest checks 25 two-state chains against closed-form stationarity and hitting times, weighted path enumeration for 64 three-state chains, cycle laws for all 27 deterministic three-state functions, and every one of the 343 nonempty-row support graphs on three states against graph search and independent directed spanning-tree stationary weights. It also verifies gambler's ruin, reducible and periodic examples, partial/infinite hitting times, time reversal, empty boundaries, exact large coefficients, resource failures, actual carrier identity, and serialized native flows. Continuous-time generators, random path sampling, convergence rates and infinite-state processes remain outside this implementation.

## Rectangular matrices and affine solution sets

The matrix and vector families also connect to the exterior and quaternion algebras below, using their actual registered carrier instances.

`math.finiteVectors`, `math.rectangularMatrices` and `math.affineSpaces` register `Vec(Q)`, `Mat(Q)` and `Affine(Q)` in the same MathTool. Vec(Q) holds finite rational vectors, including the zero-dimensional vector. Mat(Q) holds dense rational matrices with positive row and column counts. Addition and dot products require matching dimensions; matrix multiplication requires matching inner dimensions. These carriers are families of spaces with partial operations across shapes. `apply` uses ILeftProjectionOperation and returns a Vec(Q) wrapper whose dimension is the number of matrix rows.

RREF uses exact Gauss-Jordan elimination. `pivot-columns` emits zero-based pivot indices in ascending order. `nullspace-basis` emits one direction for each free variable in ascending column order. `row-space-basis` emits nonzero RREF rows; `column-space-basis` emits original pivot columns. These are finite bases, while `rows` and `columns` retain all vectors, including zero vectors and duplicates. Rank plus nullity equals the column count. Determinant and trace require square matrices; inverse also requires nonsingularity.

`Affine(Q).solve`, registered on Mat(Q), accepts a right-hand side with one entry per row. It returns the complete solution set as one member: empty for an inconsistent system, a single point for a unique solution, or a canonical particular point and nullspace directions for an infinite family. The particular solution sets free variables to zero. `at` accepts one rational parameter per direction and returns a Vec(Q) wrapper. `directions` flattens the basis, not the infinitely many solutions. On an empty set, particular, directions, dimension and at are undefined; is-empty, is-unique, ambient-dimension, contains and equality remain defined. Contains returns false for a point in a different ambient dimension. Equality compares canonical solution sets, including ambient dimension, independently of the equation presentation.

~~~java
RationalMatrix matrix = new RationalMatrix(new Rational[][] {
        {Rational.ONE, Rational.of(2), Rational.of(3)},
        {Rational.of(2), Rational.of(4), Rational.of(6)}});
List<String> point = math.flow(math.rectangularMatrices, Collections.singletonList(matrix))
        .<RationalAffineSpace, RationalVector>performAlgebraUnsafe("Affine(Q).solve",
                new RationalVector(Rational.ONE, Rational.of(2)))
        .performLeftProjectionOperation("at", new RationalVector(Rational.of(-1), Rational.of(2)))
        .collect(); // ["[-3, -1, 2]"]
// General solution: [1, 0, 0] + s*[-2, 1, 0] + t*[-3, 0, 1].
~~~

The original fixed carriers remain available through `math.vectors` and `math.matrices`. Their `Vec(Q).from-fixed` and `Mat(Q).from-fixed` transfers embed values into the families; family `to-fixed` transfers require the configured fixed dimensions. These conversions use the actual registered Algebra instances.

The reduction and solution conventions follow standard exact linear algebra, as documented by [SymPy's matrix API](https://docs.sympy.org/latest/modules/matrices/matrices.html); execution here is Java rational arithmetic. NativeRectangularLinearTest checks all 729 matrices of shape 2 by 3 with entries in {-1,0,1}, each with nine right-hand sides, using an independent minor-based rank oracle and substitution. It also checks empty, unique and infinite solutions, basis independence, large exact coefficients, canonical equality, shape failures and serialized native flows. Zero-sized rational matrices and sparse representations are not implemented.

## Exact matrix polynomials and rational spectra

Both `math.matrices` (fixed Matn(Q)) and `math.rectangularMatrices` (Mat(Q)) expose `characteristic-polynomial`, `minimal-polynomial`, `evaluate-polynomial`, natural `pow`, flat `rational-eigenvalues`, flat `eigenspace-basis` and `generalized-eigenspace-basis`, `eigenvalue-multiplicity`, `is-diagonalizable-over-q`, and unary flat `diagonalize-over-q`. The family operations require a square input. Characteristic and minimal polynomials transfer to the actual Q[x] algebra. Constants in matrix polynomial evaluation mean scalar identity matrices, and exponent zero returns identity even for a zero matrix.

The characteristic polynomial uses the monic convention det(x*I-A). Exact Faddeev-LeVerrier recurrence computes it over Q. The minimal polynomial comes from the first linear dependence among I,A,...,A^n, with coefficient transformations retained during elimination. Both annihilate the matrix, and the minimal polynomial divides the characteristic polynomial. The Q[x] transfer `Mat(Q).companion` builds the column companion of a positive-degree polynomial, normalizing its leading coefficient; its characteristic and minimal polynomials are that monic normalization.

`Q[x].rational-roots` emits distinct rational roots in ascending order. `root-multiplicity` returns the multiplicity of a supplied rational value, including zero for a nonroot. Both operations are undefined for the zero polynomial. Rational root search clears denominators and content, then uses exact integer trial division and the rational-root theorem. Irrational and nonreal roots remain represented in the polynomial and are excluded from the rational output list. No floating-point approximation or external CAS is used.

`eigenspace-basis` computes ker(A-lambda*I); `generalized-eigenspace-basis` computes ker((A-lambda*I)^n). A rational non-eigenvalue produces empty bases and multiplicity zero. Bases follow ascending free-column order, with a unit free coordinate; they do not enumerate the entire eigenspace. The generalized basis is not a Jordan-chain decomposition. `diagonalize-over-q` requires a full rational eigenbasis and emits exactly two matrix wrappers, [P,D], with A*P=P*D. Columns of P are the canonical basis vectors grouped by increasing eigenvalue; D repeats each eigenvalue on the corresponding diagonal positions. A defective matrix or one with nonrational eigenvalues has no such rational diagonalization. These conventions follow [SymPy's matrix API](https://docs.sympy.org/latest/modules/matrices/matrices.html) and [Sage's matrix documentation](https://doc.sagemath.org/html/en/reference/matrices/sage/matrix/matrix2.html).

~~~java
RationalMatrix a = new RationalMatrix(new Rational[][] {
        {Rational.of(2), Rational.ONE}, {Rational.ZERO, Rational.of(3)}});
List<String> roots = math.flow(math.rectangularMatrices, Collections.singletonList(a))
        .<Polynomial>performAlgebraTransfer("characteristic-polynomial")
        .<Rational>performFlatAlgebraTransfer("rational-roots")
        .collect(); // ["2", "3"]
List<String> decomposition = math.flow(math.rectangularMatrices, Collections.singletonList(a))
        .performOneOperandFlatOperation("diagonalize-over-q")
        .collect(); // ["[[1, 1], [0, 1]]", "[[2, 0], [0, 3]]"]
IAlgebraItem<RationalMatrix> evaluated = math.polynomials.algebra()
        .buildAlgebraItem(new Polynomial(Rational.ONE, Rational.of(2), Rational.ONE))
        .performLeftProjectionOperation("Mat(Q).evaluate-at-matrix", a);
// (A+I)^2 = [[9, 7], [0, 16]], wrapped in math.rectangularMatrices.algebra().
~~~

`evaluate-polynomial` uses the original custom-member contract Mat x Q[x] -> Mat. The reverse `Mat(Q).evaluate-at-matrix` and `Matn(Q).evaluate-at-matrix`, registered on Q[x], use ILeftProjectionOperation and return the second matrix carrier's IAlgebraItem. Eigenspace bases use native mixed flat operations and return Vec(Q) or the configured Q^n wrappers. Existing constructor signatures remain available; ConcreteMathematics uses the constructors that install the spectral registrations.

These new matrix operations accept square dimensions through 32. Polynomial evaluation and powers accept degree/exponent through 10000, with a shared 5000000-unit arithmetic budget for each matrix calculation. Rational root search and multiplicity accept polynomial degree through 64. Search allows 100000 trial divisions, 50000 divisors per coefficient, 100000 signed candidate pairs before deduplication, and 1000000 coefficient visits. Large coefficients can exhaust the search even for a quadratic; coefficient bit lengths are unbounded. Exhaustion raises IMPLEMENTATION_FAILURE, never a partial root list or false diagonalizability claim. These limits apply to the new operations, leaving the scope of prior matrix and polynomial operations unchanged.

NativeSpectralLinearTest compares all 512 binary 3 by 3 characteristic polynomials with an independent permutation determinant, verifies minimality through independent matrix-power ranks, checks all 81 ternary 2 by 2 spectra against the quadratic formula, and checks 624 nonzero small polynomials with independent root and derivative evaluations. Further tests cover defective Jordan blocks, repeated eigenspaces, nonrational spectra, companion matrices, exact similarities, large coefficients, resource failures, wrapper identity and serialized scalar/flat flows.

## Exact least-squares and orthogonal projection

`Mat(Q).pseudoinverse` returns the Moore-Penrose inverse A+ of any positive-shape rational matrix, including rectangular and rank-deficient matrices. The result has the transposed shape. The implementation uses a rank factorization A=C*R, with original independent columns C and their RREF coordinates R, and computes R^T*(R*R^T)^-1*(C^T*C)^-1*C^T. A zero matrix returns the transposed zero matrix. All arithmetic remains rational; there is no floating-point rank tolerance or square-root approximation. The four defining identities and least-squares interpretation are described in [SciPy's pseudoinverse documentation](https://docs.scipy.org/doc/scipy/reference/generated/scipy.linalg.pinv.html); this implementation has no SciPy dependency.

`column-projector` returns A*A+ and `row-projector` returns A+*A. Both are symmetric and idempotent in the standard Euclidean inner product. `project-column` maps a point to its closest point in the column space. Its input and output vectors have the matrix's row dimension.

`Affine(Q).least-squares`, registered on Mat(Q), returns all minimizers of the squared residual through the exact normal equations A^T*A*x=A^T*b. It is defined for every right-hand side with the matrix's row dimension, and its result is always nonempty. `least-squares-minimum-norm` returns the unique smallest-norm minimizer A+*b as a Vec(Q) wrapper. `least-squares-residual` returns b-A*A+*b and `least-squares-error` returns the sum of squared residual entries. Error is a rational squared norm, not a square root. These operations use the standard unweighted Euclidean inner products.

~~~java
// Using the rectangular matrix from the preceding example:
List<String> minimum = math.flow(math.rectangularMatrices, Collections.singletonList(matrix))
        .<RationalAffineSpace, RationalVector>performAlgebraUnsafe("Affine(Q).least-squares",
                new RationalVector(Rational.ONE, Rational.of(3)))
        .<RationalVector>performAlgebraTransfer("minimum-norm")
        .collect(); // ["[1/10, 1/5, 3/10]"]
// No exact solution to these original equations exists.
// Fitted values: [7/5, 14/5]; residual: [-2/5, 1/5]; squared error: 1/5.
~~~

For any nonempty Affine(Q) member, `closest-point` projects a Vec(Q) point of the same ambient dimension onto the affine set. `minimum-norm` projects zero onto it; this point can differ from the canonical free-zero `particular`. Both operations are undefined on an empty set. A singleton projects every compatible point to its sole member; projection onto the full ambient space returns the input.

NativeLeastSquaresTest checks the four Moore-Penrose equations, projector symmetry/idempotence, transpose compatibility, residual orthogonality and minimum-norm identities for all 729 ternary matrices of shape 2 by 3 and their transposes. It also checks known rank-three and square cases, zero matrices, closest-point distance identities, large exact coefficients, wrapper identity, incompatible dimensions and serialized native flows.

## Rational coordinate tensors

`math.tensors` registers Tensor(Q) using immutable `RationalTensor` members with ordered shapes and row-major entries (last axis varying fastest). A scalar has shape `[]` and one entry; a vector has one axis; a matrix has two. `order` reports the number of axes, not tensor decomposition rank. Zero-sized axes are supported and retained, with zero stored entries. Shape-sensitive addition, subtraction, Hadamard multiplication and dot product require identical ordered shapes.

`tensor-product` concatenates operand shapes and multiplies coordinates. The scalar tensor one is its identity. `swap-axes` exchanges two zero-based axes, including their dimensions. `contract` sums entries with equal coordinates on two distinct, equal-sized axes and removes those axes, keeping the others in order. This uses the standard coordinate pairing; no covariant/contravariant axis labels or arbitrary metric are inferred. These coordinate conventions agree with [SymPy's array operations](https://docs.sympy.org/latest/modules/tensor/array.html). Contracting a square matrix tensor yields its trace, while tensor product followed by contraction reproduces matrix multiplication.

Axis operations use the native ICustomMemberOperation interface with a `Pair<BigInteger,BigInteger>` in the supporting NxN.tensor-axes carrier. For example, using the matrix from the rectangular example:

~~~java
List<String> trace = math.flow(math.rectangularMatrices, Collections.singletonList(matrix))
        .<RationalTensor>performAlgebraTransfer("Tensor(Q).from-matrix")
        .performOperation("tensor-product", RationalTensor.fromMatrix(matrix.transpose()))
        .performCustomMemberOperation("contract", new Pair<>(BigInteger.ONE, BigInteger.valueOf(2)))
        .<RationalMatrix>performAlgebraTransfer("to-matrix")
        .<Rational>performAlgebraTransfer("trace")
        .collect(); // ["70"]
~~~

`from-scalar`, `from-vector` and `from-matrix` transfers are qualified on their source carriers. Reverse transfers check tensor order; conversion to Mat(Q) also requires positive dimensions. An empty vector converts to and from shape `[0]`. Scalar tensor shape `[]` remains distinct from shape `[1]`. Flat `shape` and `entries` transfers preserve axis or coordinate order; `norm-squared` returns an exact rational squared coordinate norm.

Dense construction and results are capped at order 32 and 1,000,000 entries. Exceeding either cap raises IMPLEMENTATION_FAILURE, distinct from OPERATION_UNDEFINED for incompatible shapes or axes. Contracting two zero-sized axes yields an empty sum of zero for every remaining coordinate, and the resulting shape is subject to the same cap. Sparse tensors, basis-independent variance contracts and tensor bundles remain outside this implementation.

NativeTensorTest compares product/contraction against independent coordinate sums for all 6,561 pairs of ternary 2 by 2 matrices. It also checks nonadjacent axis operations, associativity/bilinearity, shape-preserving empty tensors, order-zero scalars, immutability, resource limits, actual carrier wrappers and serialized flows from matrices through tensors back to vectors and scalars.

## Exterior algebra and Hodge duality

`math.exterior` registers Exterior(Q), a family of exterior algebras over rational coordinate spaces. `RationalExterior` stores a retained ambient dimension and a canonical sparse map from basis masks to nonzero rational coefficients. Bit i denotes the generator e_i; mask 3 denotes e_0 wedge e_1, and mask zero denotes the scalar unit. Factors in each basis monomial are ordered by increasing index. Mixed homogeneous degrees are allowed. Zero has empty support and retains its ambient dimension.

`wedge` requires equal ambient dimensions. A repeated basis factor makes that product zero; otherwise sorting concatenated factors supplies the permutation sign. `grade` selects a nonnegative homogeneous degree, with degrees above dimension returning zero. `grade-involution` multiplies degree k by (-1)^k; `reverse` multiplies it by (-1)^(k*(k-1)/2). Flat `terms`, `coefficients` and `basis-masks` use ascending mask order; `degrees` emits occupied degrees in ascending order. Empty support emits no terms or degrees.

`hodge-star` uses the standard positively oriented orthonormal coordinate basis. For every basis monomial e_I, e_I wedge star(e_I) is the positive volume. On degree k in dimension n, applying star twice multiplies by (-1)^(k*(n-k)). `interior` inserts a compatible vector on the left, identifying it with a covector through the standard dot product. It obeys the graded antiderivation rule. The coordinate pairing convention is explicit, as in [Sage's exterior algebra documentation](https://doc.sagemath.org/html/en/reference/algebras/sage/algebras/clifford_algebra_element.html).

~~~java
List<String> cross = math.flow(math.finiteVectors,
        Collections.singletonList(new RationalVector(Rational.ONE, Rational.of(2), Rational.of(3))))
        .<RationalExterior>performAlgebraTransfer("Exterior(Q).from-vector")
        .performOperation("wedge", RationalExterior.fromVector(
                new RationalVector(Rational.of(4), Rational.of(5), Rational.of(6))))
        .performOneOperandOperation("hodge-star")
        .<RationalVector>performAlgebraTransfer("to-vector")
        .collect(); // ["[-3, 6, -3]"]
~~~

`Exterior(Q).apply` is registered on Mat(Q) as ILeftProjectionOperation. It sends each generator to the corresponding matrix column and extends by wedge and linearity, returning the exterior wrapper with the matrix row count as its ambient dimension. Thus it is the covariant induced map on multivectors. Matrix columns must match the source dimension. This operation preserves scalars and wedge products, composes in matrix order, and acts on top exterior degree by the determinant for square matrices. It does not implement pullback of differential forms.

`from-vector` embeds in degree one. `to-vector` rejects nonzero terms outside degree one; zero maps to the zero vector of its retained dimension. `to-scalar` accepts only scalar support, while `scalar-part` is always defined. `zero-in`, `one-in` and `volume-in` are qualified transfers from N, taking ambient dimension as input. Dimension zero has volume equal to the scalar unit.

The representation caps ambient dimension at 20 and sparse support at 100,000 terms, including intermediate results. Wedge and each complete induced matrix action cap candidate coefficient products at 1,000,000. Exhaustion raises IMPLEMENTATION_FAILURE. Arbitrary metrics, manifold differential forms and exterior derivatives remain outside this carrier. NativeExteriorTest covers basis sign oracles, associativity, Hodge and insertion identities, all 729 ternary cross-product pairs, all 729 ternary 2 by 3 minor maps, all 512 binary 3 by 3 determinants, exact large coefficients, resource limits and serialized native flows.

## Rational Hamilton quaternions and rotations

`math.quaternions` registers H(Q), the Hamilton division algebra with four exact rational coefficients w+x*i+y*j+z*k. It uses i*i=j*j=k*k=i*j*k=-1, so i*j=k while j*i=-k. Every nonzero element has inverse conjugate(q)/norm-squared(q). Multiplication is associative and noncommutative. The `divide-right` operation returns a*b^-1, solving x*b=a; `divide-left` returns b^-1*a, solving b*x=a. Their order is explicit in the API. These are the standard Hamilton conventions documented by [SymPy's quaternion API](https://docs.sympy.org/latest/modules/algebras.html).

`H(Q).embed-rational` and `H(Q).embed-complex` connect Q and the chosen Q(i) subfield. Reverse transfers reject components outside those subfields. `H(Q).from-vector` accepts exactly three coordinates and builds a pure quaternion; `to-vector` requires zero real part. `imaginary-part` always returns the i,j,k coordinates. Flat `components` retains all four entries, including zero and repeated coordinates. `norm-squared` stays rational; no generally irrational square root is substituted.

For any nonzero q, `rotate` returns q*(0,v)*q^-1 as a Vec(Q) wrapper. This is an active right-handed rotation of column vectors. It requires dimension three and does not require unit norm. All nonzero rational scalar multiples represent the same rotation; `same-rotation` checks this explicitly, while `equal` compares all coefficients as algebra elements. Both operands of same-rotation must be nonzero.

~~~java
RationalQuaternion quarterTurn = new RationalQuaternion(
        Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ONE);
List<String> rotated = math.flow(math.quaternions, Collections.singletonList(quarterTurn))
        .<RationalMatrix>performAlgebraTransfer("to-rotation-matrix")
        .<RationalQuaternion>performAlgebraTransfer("H(Q).from-rotation-matrix")
        .performLeftProjectionOperation("rotate",
                new RationalVector(Rational.ONE, Rational.of(2), Rational.of(3)))
        .collect(); // ["[-2, 1, 3]"]
~~~

`to-rotation-matrix` returns an exact rational 3 by 3 orthogonal matrix with determinant one. The reverse operation checks these conditions exactly, rejects approximate or improper rotations, and returns a rational projective quaternion whose first nonzero component equals one. Its norm need not equal one. For trace different from -1, recovery uses (1+trace, R_21-R_12, R_02-R_20, R_10-R_01) before projective scaling. A half-turn uses a nonzero column of R+I as its pure quaternion axis. Neither path takes square roots. Quaternion multiplication composes rotations with the right operand acting first.

This represents rational Hamilton quaternions and rational rotation matrices, not all real quaternions or arbitrary-angle symbolic rotations. Euler-angle extraction and approximate matrix fitting are not implemented. NativeQuaternionTest checks all 6,561 ternary quaternion products against an independent basis table, all 624 nonzero quaternions with components in {-2,-1,0,1,2} for exact rotation identities and matrix round-trips, both division orders, half-turns, wrong shapes, near-orthogonal rejection, large rational coefficients and serialized MathTool flows.

## Multivariate polynomials and polynomial vector calculus

`math.multivariatePolynomials` registers Poly(Q), a family of polynomial rings in a declared positive number of ordered variables. `MultivariatePolynomial` stores an immutable sparse map from nonnegative exponent tuples to rational coefficients. Zero coefficients disappear, but the input dimension remains part of equality, including for zero and constants. Arithmetic between different input dimensions is undefined. The zero polynomial has degree -1, so the `degree` transfer returns Z. Flat `terms` and `coefficients` use ascending lexicographic exponent order; flat `variables` and `partials` use coordinate order and retain zero and repeated results.

`partial` and `primitive` take a zero-based variable index in N. The primitive sets the entire polynomial independent of that variable to zero. `directional` takes a constant rational vector and computes the sum of its coordinates times the corresponding partial derivatives; it does not normalize the vector. `gradient-at` returns Vec(Q), `hessian-at` returns Mat(Q), and `laplacian` returns a polynomial. Coordinate order determines every vector and matrix entry. `from-univariate` embeds Q[x] with one declared variable, and `to-univariate` requires exactly one variable even for constants. These use the standard coefficient differentiation and integration conventions in [SymPy's polynomial reference](https://docs.sympy.org/latest/modules/polys/reference.html).

`math.polynomialMaps` registers PolynomialMap(Q), an ordered tuple of scalar polynomials with a common input dimension and an explicit output dimension. `compose` applies the right operand first and checks the middle dimension. `evaluate` returns a wrapper of the actual second-operand Vec(Q) algebra; its output dimension can differ from the input point's dimension. `jacobian-at` returns a matrix whose rows index outputs and columns index inputs, representing the total derivative at the point. `PolynomialMap(Q).gradient` transfers a scalar polynomial to a polynomial map, whose Jacobian is its Hessian. These are the [standard Jacobian and Hessian conventions](https://docs.sympy.org/latest/modules/matrices/matrices.html).

```java
MultivariatePolynomial f = MultivariatePolynomial.monomial(Rational.ONE, 2, 1, 0)
        .add(MultivariatePolynomial.monomial(Rational.of(3), 0, 1, 2));
RationalVector point = new RationalVector(Rational.of(2), Rational.of(3), Rational.of(-1));

List<String> gradient = math.flow(math.multivariatePolynomials, Collections.singletonList(f))
        .<PolynomialMap>performAlgebraTransfer("PolynomialMap(Q).gradient")
        .performLeftProjectionOperation("evaluate", point).collect();
// [[12, 7, -18]]

List<String> hessian = math.flow(math.multivariatePolynomials, Collections.singletonList(f))
        .<PolynomialMap>performAlgebraTransfer("PolynomialMap(Q).gradient")
        .<RationalMatrix,RationalVector>performAlgebraUnsafe("jacobian-at", point).collect();
// [[[6, 4, 0], [4, 0, -6], [0, -6, 18]]]
```

`divergence` requires equal input and output dimensions. `curl` requires dimension three on both sides and uses the standard right-handed orientation. `PolynomialMap(Q).from-matrix` builds a linear map directly from a registered Mat(Q) member. `linear-part` returns the degree-one coefficient matrix (the Jacobian at zero); `constant-part` returns the constant vector. `PolynomialMap(Q).substitute`, registered on Poly(Q), simultaneously substitutes a polynomial map into a scalar polynomial, returning a wrapper in the first operand's Poly(Q) carrier. Scalar extraction from a map requires one output component. Same-carrier and transfer operations, flat component lists, and mixed operations use the existing interfaces in `operations/simple` and `operations/flat`.

Representation limits are 32 variables, 32 map components, total monomial degree 10,000, and 100,000 nonzero terms per scalar intermediate or output. Each multiply, power, or complete substitution/composition has a budget of 1,000,000 candidate coefficient products; map composition shares the budget across all components. Natural powers are capped at exponent 10,000. Exceeding a limit raises IMPLEMENTATION_FAILURE. Wrong dimensions, missing variable/component indices, and unsupported scalar conversions raise OPERATION_UNDEFINED. Coefficient bit lengths remain arbitrary precision and can still grow quickly. The implementation covers polynomial coordinate calculus with positive dimensions; it does not provide arbitrary smooth-function spaces, zero-dimensional maps, multivariate rational functions, Groebner bases, or manifold calculus.

NativeMultivariateCalculusTest checks all 729 bivariate quadratics with coefficients in {-1,0,1} against independently evaluated exact centered differences. It also checks explicit higher-degree derivatives, mixed partials, product and chain rules, exact Taylor restrictions, rectangular Jacobians, right-handed curl, divergence-of-curl and curl-of-gradient identities, primitive conventions, large rational coefficients, immutable representations, shared expansion limits, actual result wrappers and serialized MathTool flows. These checks provide empirical evidence; no formal verification is claimed.

## Polynomial coordinate differential forms

`math.polynomialForms` installs PolynomialForm(Q) in MathTool. A `PolynomialDifferentialForm` is a canonical finite sum of polynomial coefficients times increasing coordinate differentials. Bit i in a basis mask denotes dx_i; mask zero denotes a scalar. Every coefficient has the form's declared variable count, and mixed differential degrees are allowed. Zero coefficients disappear without erasing dimension. `basis-count` counts occupied differential masks; `coefficient-term-count` counts all nonzero scalar monomials across their coefficients. Flat `terms` emits one whole polynomial coefficient times its differential basis element. Flat `basis` emits the coordinate one-forms in order.

`exterior-derivative` computes d(f dx_I) as the sum of partial_j(f) dx_j wedge dx_I. It raises differential degree, squares to zero, and obeys the graded product rule. `grade-involution` supplies the sign for mixed forms. `interior` inserts a polynomial vector field using the natural covector-vector pairing. `lie-derivative` uses Cartan's formula, i_X d + d i_X. Insertion and Lie derivative require a square vector field on the same coordinate space. The [Sage differential-form reference](https://doc.sagemath.org/html/en/reference/manifolds/sage/manifolds/differentiable/diff_form.html) documents these coordinate exterior operations and identities.

For F:Q^m -> Q^n, `pullback` takes a form on Q^n to one on Q^m. It substitutes F into every coefficient and replaces each dx_i by dF_i. Composition is contravariant: (F composed with G)* applies F* first, then G*. Pullbacks preserve wedge and commute with exterior differentiation. At a point u, this agrees with evaluating the form at F(u), then applying the transpose of J_F(u) to the coordinate coframe. The pre-existing `Exterior(Q).apply` remains the covariant induced matrix action on multivectors. See [Sage's differentiable-map pullback documentation](https://doc.sagemath.org/html/en/reference/manifolds/sage/manifolds/differentiable/diff_map.html).

Polynomial zero-forms transfer from Poly(Q) with `PolynomialForm(Q).from-polynomial`; extraction requires all positive-degree components to vanish. `evaluate` returns the actual registered Exterior(Q) wrapper by identifying the standard coordinate coframe with its basis. `from-exterior` creates constant coefficients and requires positive dimension; `to-exterior` requires every coefficient to be constant. Hodge star and conversions to/from square polynomial vector fields use the standard oriented Euclidean metric. They do not infer a metric on arbitrary manifolds.

```java
PolynomialMap rotation = new PolynomialMap(
        MultivariatePolynomial.variable(3, 1).negate(),
        MultivariatePolynomial.variable(3, 0),
        MultivariatePolynomial.constant(3, Rational.ZERO));
List<String> curl = math.flow(math.polynomialMaps, Collections.singletonList(rotation))
        .<PolynomialDifferentialForm>performAlgebraTransfer("PolynomialForm(Q).from-vector-field")
        .performOneOperandOperation("exterior-derivative")
        .performOneOperandOperation("hodge-star")
        .<PolynomialMap>performAlgebraTransfer("to-vector-field")
        .performLeftProjectionOperation("evaluate",
                new RationalVector(Rational.of(2), Rational.of(3), Rational.of(4)))
        .collect(); // [[0, 0, 2]]
```

Forms support dimensions 1 through 20, coefficient total degree at most 10,000, and at most 100,000 scalar monomials across all coefficients of each intermediate/output form. Wedge, polynomial multiplication, insertion, Lie derivative and complete pullback each share a budget of 1,000,000 basis-work visits plus candidate polynomial products. Exceeding a bound raises IMPLEMENTATION_FAILURE. A whole-form cap prevents many small coefficients from bypassing the limit, and compound pullbacks share the expansion budget across coefficients and differential factors. Exact coefficient bit lengths remain unbounded.

NativePolynomialFormsTest checks independent wedge signs, d squared, graded product and insertion rules, Euler-field Lie derivatives, gradient/curl/divergence correspondences, nonlinear pullback composition and 729 ternary rectangular matrix pullbacks against independent minors. An oriented rectangle regression pulls a polynomial one-form back to each edge and integrates through Q[x], obtaining the same exact value as the area integral of its exterior derivative. The cell/chain integration API below generalizes this regression to compatible polynomial parametrizations; no formal proof is claimed. Serialized native flows, wrapper identities, shape failures and aggregate resource limits are also tested. General manifold charts, arbitrary smooth coefficients and metrics, global de Rham cohomology and integration on arbitrary domains remain outside this implementation.

## Finite topology through the existing flow API

~~~java
FiniteSimplicialComplex circle = new FiniteSimplicialComplex(Arrays.asList(
        FiniteSet.of(0, 1), FiniteSet.of(1, 2), FiniteSet.of(0, 2)));
List<String> betti = math.flow(math.complexes, Collections.singletonList(circle))
        .<BigInteger>performFlatAlgebraTransfer("betti-numbers").collect(); // ["1", "1"]
~~~

The constructor closes facets under nonempty faces; equality compares labelled simplex sets. The existing `betti-number` and `betti-numbers` operations use unreduced F2 homology. The empty complex has dimension -1, Euler characteristic zero and empty degree lists. Facet materialization is limited to 20 vertices. The homology and Euler-characteristic conventions follow the standard finite simplicial definitions in [Hatcher, chapter 2](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf).

`integral-homology` returns H_k(-;Z) in the actual `math.abelianGroups` carrier; `integral-homology-groups` emits degrees zero through the complex dimension, including trivial groups. `rational-betti-number` and its flat plural return their free ranks. `boundary-invariant-factors` emits positive nonzero Smith factors of the oriented boundary from degree k to k-1, including ones. Boundary orientation uses increasing vertex labels. Degrees above the dimension give a trivial group, zero Betti number or empty boundary list, including arbitrarily large BigInteger degrees.

~~~java
FiniteSimplicialComplex projectivePlane = new FiniteSimplicialComplex(Arrays.asList(
        FiniteSet.of(0,1,2), FiniteSet.of(0,1,3), FiniteSet.of(0,2,4), FiniteSet.of(0,3,5), FiniteSet.of(0,4,5),
        FiniteSet.of(1,2,5), FiniteSet.of(1,3,4), FiniteSet.of(1,4,5), FiniteSet.of(2,3,4), FiniteSet.of(2,3,5)));
List<String> h1 = math.flow(math.complexes, Collections.singletonList(projectivePlane))
        .<AbelianGroupType,BigInteger>performAlgebraUnsafe("integral-homology", BigInteger.ONE)
        .collect(); // ["AbelianGroup(rank=0, torsion=[2])"]
List<String> torsion = math.flow(math.complexes, Collections.singletonList(projectivePlane))
        .<AbelianGroupType>performFlatAlgebraTransfer("integral-homology-groups")
        .<BigInteger>performFlatAlgebraTransfer("invariant-factors").collect(); // ["2"]
~~~

This projective plane has integral homology [Z, Z/2Z, 0], rational Betti numbers [1,0,0], and F2 Betti numbers [1,1,1]. Integral calculations allow at most 256 simplices in each required degree and share a 5,000,000-unit Smith-reduction budget across an entire degree list. Exhaustion raises IMPLEMENTATION_FAILURE; coefficient bit lengths remain unbounded. These results describe group isomorphism types; IntegralHomology separately supplies cycle representatives, bounding chains and induced homology maps. Cup products and persistence remain unimplemented. The existing three-argument FiniteSimplicialAlgebra constructor retains its original operations; the new four-argument overload adds these operations using the supplied AbelianGroupTypeAlgebra.

NativeIntegralHomologyTest checks Smith factors against independent minors for 625 integer 2 by 2 and 512 binary 3 by 3 matrices, and homology against spheres, balls, a torus, the projective plane, Moore spaces with torsion orders 3 through 10, suspensions and disconnected examples. NativeTopologyTest preserves the original F2 checks. Native wrappers and serialized homology flows are also covered.

## Finitely generated abelian-group types

`math.abelianGroups` registers `AbelianGroupType`, a canonical representation of Z^r plus finite cyclic summands with orders d_1 | ... | d_t, each greater than one. It classifies groups up to isomorphism, without chosen elements or homomorphisms. Direct sum and tensor product over Z form a commutative semiring of these types, with the trivial group as zero and Z as one. Normalization uses gcd/lcm and requires no prime factorization.

`hom-group` and `ext1` take the source first and target second; `tor1` computes Tor_1 over Z. Results are group types rather than individual maps or extension witnesses. `repeat` takes a natural multiplicity; `free-on` constructs Z^n and `cyclic` constructs Z/nZ, including n=0 for Z and n=1 for the trivial group. `order` and `exponent` require finite groups and both return one for the trivial group. `minimal-generators` returns r+t. Flat `invariant-factors` emits torsion orders only.

~~~java
AbelianGroupType c6 = AbelianGroupType.cyclic(BigInteger.valueOf(6));
AbelianGroupType c4 = AbelianGroupType.cyclic(BigInteger.valueOf(4));
List<String> tensor = math.flow(math.abelianGroups, Collections.singletonList(c6))
        .performOperation("tensor-product", c4)
        .<BigInteger>performFlatAlgebraTransfer("invariant-factors").collect(); // ["2"]
~~~

Construction and arithmetic allow at most 256 canonical torsion factors and 1024 supplied/intermediate cyclic factors; larger calculations raise IMPLEMENTATION_FAILURE. Free rank and coefficient sizes are arbitrary precision. NativeAbelianGroupTest compares normalization with independent element-order histograms for 144 pairs of cyclic groups, counts finite homomorphisms independently, checks tensor/Hom/Tor/Ext identities and validates actual wrappers and serialized flows. Explicit presentations and homomorphisms over Z are supplied by the dedicated builders below; arbitrary coefficient rings remain outside scope.

## Finite optimization and discrete dynamics

IntegerSetAlgebra extends the finite-set algebra with polynomial objectives. Its argmin/argmax custom-member operations return every tied optimum as a finite set. minimum/maximum return an exact rational objective value through IUnsafeOperation. Flat minimizers/maximizers return integer wrappers through IUnsafeFlatOperation, in the feasible set's iteration order. Empty feasible sets make all six operations undefined. These operations optimize only over the supplied finite set, not over all integers or reals.

~~~java
FiniteSet<BigInteger> feasible = FiniteSet.of(
        BigInteger.valueOf(-2), BigInteger.valueOf(-1), BigInteger.ONE, BigInteger.valueOf(2));
Polynomial square = new Polynomial(Rational.ZERO, Rational.ZERO, Rational.ONE);
List<String> minimizers = math.flow(math.integerSets, Collections.singletonList(feasible))
        .<BigInteger, Polynomial>performFlatAlgebraUnsafe("minimizers", square)
        .collect(); // ["-1", "1"]

Polynomial step = new Polynomial(Rational.ONE, Rational.of(2)); // x -> 2*x + 1
Pair<Rational, BigInteger> request = new Pair<>(Rational.ONE, BigInteger.valueOf(3));
List<String> orbit = math.flow(math.polynomials, Collections.singletonList(step))
        .<Rational, Pair<Rational, BigInteger>>performFlatAlgebraUnsafe("orbit", request)
        .collect(); // ["1", "3", "7", "15"]
~~~

iterate returns the final value; orbit returns the initial value followed by each step. Zero steps preserve the initial value. Exact rationals avoid rounding, but their sizes can grow rapidly. Execution caps requests at 10000 steps and reports IMPLEMENTATION_FAILURE above that limit. This is a finite computation, without a claim about long-time convergence, stability or attractors.

## Finite relations

FiniteIntegerRelationAlgebra registers finite-support relations on the tool's actual integer Algebra. compose applies its first relation and then the second. inverse, domain, range, image and preimage retain the declared native carriers. image/preimage use ILeftProjectionOperation because their result belongs to the second operand's finite-set carrier.

transitive-closure includes positive-length paths; it adds a reflexive pair only if a cycle implies it. is-function-on checks exactly one result for each member of an explicit finite set and rejects relation pairs outside that set. identity-on is registered on FiniteSet(Z) as FiniteRelation(Z,Z).identity-on; it constructs a finite identity, not identity on all integers. Equality requires the same source/target Algebra instances and equal pair sets.

FiniteRelation now stores Algebra source/target fields and is serializable. Its Domain-based constructor remains a compatibility adapter to the domains' existing Algebra instances; native callers use the Algebra constructor directly. Tests compare transitive closure with an independent Warshall implementation for all 512 directed graphs on three vertices, reject foreign algebra instances and round-trip relation flows through serialization.

## Polynomial division and rational functions

Q[x] now registers quotient, remainder, divide-exact, gcd, compose, monic and flat quotient-remainder. Euclidean division satisfies a = b*q + r with deg(r) < deg(b); zero divisors are undefined. divide-exact additionally requires zero remainder. GCD is monic, with gcd(0,0)=0. The zero polynomial has no monic normalization.

RationalFunctionField registers Q(x) directly in MathTool. Fractions have coprime polynomial numerators/denominators, monic nonzero denominators and canonical zero 0/1. Operations include field arithmetic, derivative, inverse, evaluation, numerator/denominator transfers, equality and polynomial embedding. compose means f(g(x)); it is undefined if a constant substitution makes the reduced denominator identically zero. The polynomial-ring/fraction-field distinction matches [the standard domain distinction documented by SymPy](https://docs.sympy.org/latest/modules/polys/domainsref.html); this implementation has no SymPy dependency.

These are formal fraction-field elements. Cancelling (x^2-1)/(x-1) gives x+1, which evaluates to 2 at x=1. Original expression exclusions are not retained. Remaining poles are undefined, and evaluation uses ILeftProjectionOperation to return IAlgebraItem<Rational>.

~~~java
List<String> result = math.flow(math.polynomials,
        Collections.singletonList(new Polynomial(Rational.ZERO, Rational.ONE)))
        .<RationalFunction>performAlgebraTransfer("Q(x).embed-polynomial")
        .performOneOperandOperation("inverse")
        .performOneOperandOperation("derivative")
        .performLeftProjectionOperation("evaluate", Rational.of(2))
        .collect(); // ["-1/4"]
~~~

NativeRationalFunctionTest covers canonical cancellation, poles, constant-pole composition, 120 constructed Euclidean divisions, sampled field/chain-rule identities and serialized native flows.

## Symmetric groups

The default initializer includes SymmetricGroup of degree 3 (S3). Construct SymmetricGroup(n, naturals, integers, booleans) for another nonnegative degree and register it in a tool with those same carrier instances. Permutation members store the images of zero-based labels 0,...,n-1; duplicates, out-of-range images and wrong degrees are rejected.

compose means p(q(i)), with the right operand acting first. This is the usual function-composition convention; see [Sage's permutation documentation](https://doc.sagemath.org/html/en/reference/combinat/sage/combinat/permutation.html). Integer powers use cycle lengths, so negative and very large exponents do not require repeated composition. Order is the lcm of cycle lengths; sign is checked against inversion parity.

apply returns a natural-number wrapper via ILeftProjectionOperation. Its flat counterpart orbit returns the cycle starting at the supplied point, without repeating its endpoint. cycles is a unary flat operation returning nontrivial disjoint cycle permutations on the full carrier; identity emits an empty list. The Unit flat transfer S3.elements enumerates all six members. Full enumeration is capped at degree 8; defining and operating on higher-degree groups remains supported. S0 has one empty identity permutation.

~~~java
Permutation cycle = new Permutation(1, 2, 0);
List<String> orbit = math.flow(math.permutations, Collections.singletonList(cycle))
        .performLeftProjectionFlatOperation("orbit", BigInteger.ZERO)
        .performOneOperandOperation("successor")
        .collect(); // ["1", "2", "3"]
~~~

NativePermutationTest checks every S3 triple for associativity, all S3 inverses/closure, independent S4 sign/order/power calculations, wrong-degree and action failures, the enumeration cap and serialized flows. These checks are empirical evidence with explicit finite scope.

## Composite residue rings

ResidueRing registers Z/nZ for any fixed BigInteger modulus n > 1; the default initializer includes Z/6Z. Canonical representatives lie in [0,n). A residue is a unit precisely when it is coprime to n; inverse and divide require units. The nonzero-zero-divisor convention excludes zero itself. These distinctions follow the [standard modular integer operations](https://doc.sagemath.org/html/en/reference/finite_rings/sage/rings/finite_rings/integer_mod.html). Nonnegative powers include 0^0=1, while negative powers require a unit base.

solve-multiply is a native flat operation: given a and b, it emits every x satisfying a*x=b, ordered by canonical representative. An unsolvable equation emits an empty list. For 0*x=0 it emits the whole ring. Both full-ring enumeration and solution lists are capped at 10000 outputs; larger requests report IMPLEMENTATION_FAILURE. Arithmetic itself accepts arbitrarily large moduli subject to available resources.

~~~java
List<String> solutions = math.flow(math.residues,
        Collections.singletonList(math.residues.member(2)))
        .performFlatOperation("solve-multiply", math.residues.member(4))
        .<BigInteger>performAlgebraTransfer("lift")
        .collect(); // ["2", "5"]
~~~

The integer transfer Z/6Z.reduce maps any integer into the default residue ring. lift returns its canonical integer representative. NativeResidueRingTest compares every linear congruence modulo 2 through 20 against brute-force search and checks composite nonunits, large moduli, resource limits and serialized flows.

## Total functions between finite sets

FiniteIntegerFunctionAlgebra registers FiniteFunction(Z,Z). Members retain explicit finite domains and codomains, their actual integer Algebra instances, and an immutable map assigning exactly one value to every domain member. The codomain can contain values outside the range, so equal graphs need not be equal functions. This follows the explicit-domain/codomain model of [finite set maps](https://doc.sagemath.org/html/en/reference/sets/sage/sets/finite_set_maps.html).

compose means f(g(x)) and requires g's declared codomain to equal f's declared domain. Matching only the actual range is insufficient. inverse requires bijectivity onto the entire codomain. Evaluation outside the finite domain is undefined. image and preimage accept subsets of the declared domain and codomain respectively. restrict retains the original codomain.

Evaluation, image and preimage return the second operand carrier's IAlgebraItem wrapper. The flat preimage-of operation returns all arguments mapping to a specified codomain point; an unused codomain point has an empty fiber. The unary flat transfer values emits one image per domain member, retaining repeated values. Iteration order follows the explicitly stored finite sets.

~~~java
Map<BigInteger, BigInteger> values = new LinkedHashMap<>();
values.put(BigInteger.ONE, BigInteger.TEN);
values.put(BigInteger.valueOf(2), BigInteger.TEN);
FiniteFunction<BigInteger, BigInteger> f = math.integerFunctions.member(
        FiniteSet.of(BigInteger.ONE, BigInteger.valueOf(2)),
        FiniteSet.of(BigInteger.TEN), values);
List<String> images = math.flow(math.integerFunctions, Collections.singletonList(f))
        .<BigInteger>performFlatAlgebraTransfer("values")
        .performOneOperandOperation("negate")
        .collect(); // ["-10", "-10"]
~~~

The graph transfer connects to FiniteRelation(Z,Z). The reverse operation FiniteFunction(Z,Z).from-relation is registered on the relation algebra; its second operand is Pair(domain,codomain), validated by the supporting FiniteSet(Z)xFiniteSet(Z).function carrier. It rejects missing values, multiple values, extra domain points and values outside the codomain. identity-on constructs a finite identity from a set; empty is the unique map from the empty set to itself.

NativeFiniteFunctionTest checks every map between canonical sets of size zero through three, every composition of the 27 three-point endomaps and all 19683 associativity triples. It also checks mismatched boundaries, distinct Java source/target classes, empty maps, immutable inputs, serialization and flat-result multiplicity. This finite table representation does not implement arbitrary infinite-domain functions or decide equality of callbacks.

## Finite categories with validated tables

FiniteCategoryAlgebra registers FiniteCategory in the original MathTool. A member consists of integer-labelled objects and arrows, source/target pairs, an identity arrow for every object and a complete composition table. Construction checks endpoint typing, both identity laws and associativity for every composable triple. The obligations follow the [standard category definition](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Category/Basic.html); this Java implementation is not extracted from mathlib or formally verified by Lean.

The composition key Pair(f,g) uses path order: first f, then g. compose accepts a category and this pair, returning a wrapped integer arrow label through IUnsafeOperation. source, target and identity use ILeftProjectionOperation. Object labels and arrow labels are separate roles even when the same integer labels occur in both. Unknown labels and incompatible composition are undefined.

hom returns all arrows between two existing objects in ascending label order. inverse-of returns the unique two-sided inverse of a known arrow, or an empty list for a non-isomorphism. Empty hom sets are valid; unknown objects are not. is-groupoid checks every arrow; is-thin checks that each hom set has at most one member. Initial and terminal objects require exactly one arrow to or from every object, including themselves, and are emitted by unary flat transfers.

The discrete-on transfer constructs identity arrows on an explicit finite set. from-preorder converts a finite relation that is reflexive on its support and transitive. It derives objects from that support; represent isolated objects by their diagonal pairs. Arrow labels start at zero in lexicographic pair order, so relation iteration order does not change the category. underlying-relation forgets arrow multiplicity and records whether each hom set is nonempty. opposite reverses arrows and composition while retaining labels.

~~~java
List<String> initial = math.flow(math.integerSets,
        Collections.singletonList(FiniteSet.of(BigInteger.TEN)))
        .<FiniteCategory>performAlgebraTransfer("FiniteCategory.discrete-on")
        .<BigInteger>performFlatAlgebraTransfer("initial-objects")
        .collect(); // ["10"]
~~~

Validation is capped at 128 arrows and objects; larger inputs report IMPLEMENTATION_FAILURE. Equality compares complete labelled tables and does not decide categorical equivalence. NativeCategoryTest classifies all 81 unital three-arrow multiplication tables by an independent associativity check, tests conversion of all 512 three-point relations, checks a noncommutative transformation monoid, rejects malformed tables and round-trips category flows through serialization. Finite functors and natural transformations are described below; arbitrary infinite categories remain outside this implementation.

## Finite functors

FiniteFunctorAlgebra registers covariant functors between the finite category tables. Construction checks total object and arrow maps, endpoint typing, identities and every composition, following the [functor obligations](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Functor/Basic.html). compose means F(G(-)) and requires exact equality of the labelled middle categories. inverse requires bijective object and arrow maps. opposite retains the maps and reverses both categories.

Faithfulness and fullness are tested on each hom set. Essential surjectivity allows target objects isomorphic to image objects. is-equivalence uses the [full, faithful and essentially surjective criterion](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Equivalence.html), while is-isomorphism requires strict bijections. Thus an equivalence can lack a strict inverse. The predicate returns a Boolean; FiniteEquivalenceAlgebra separately constructs the quasi-inverse and coherence witnesses.

map-object and map-arrow return wrapped integers via ILeftProjectionOperation. Their flat fibers return all source preimages of an existing target label, possibly none. Flat image transfers retain duplicates. object-map and arrow-map transfer to the existing finite-function algebra with complete domain/codomain sets. from-discrete-map constructs a functor from a finite integer function; identity-on is registered on FiniteCategory.

~~~java
List<String> images = math.flow(math.categories,
        Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
        .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.identity-on")
        .performLeftProjectionOperation("map-object", BigInteger.TEN)
        .collect(); // ["10"]
~~~

Both categories retain the 128-arrow/object cap. NativeFunctorTest exhaustively checks small cyclic-group maps and two-object discrete endofunctors, distinguishes local faithfulness from global arrow injectivity, and covers equivalences without strict inverses, invalid maps, wrappers, resource limits and serialized flows.

## Finite natural transformations

FiniteNaturalTransformationAlgebra registers transformations between parallel finite functors. Construction requires exactly one component F(x) -> G(x) for each source object and checks F(f);eta_y = eta_x;G(f) for every source arrow f:x->y. These are the [standard naturality obligations](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/NatTrans.html). All functor boundaries and component maps are retained in equality.

compose is vertical composition with the right operand first: beta.compose(alpha) applies alpha then beta. horizontal takes alpha:F=>G and beta:H=>K on adjacent categories and returns H.F=>K.G; its component is H(alpha_x) followed by beta_(Gx). precompose pulls components back along a functor's object map; postcompose maps components along a functor's arrow map. Both implement ICustomMemberOperation with a different second operand class. opposite reverses the transformation direction as well as the categories.

inverse requires every component to be an isomorphism, following the [component criterion for natural isomorphisms](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/NatIso.html). component returns the codomain arrow label wrapped in the integer algebra. The flat components transfer retains repeated labels; component-fiber returns source objects sharing a specified codomain arrow. An unused existing arrow has an empty fiber, while an unknown arrow is undefined. component-map transfers to the finite-function algebra with the entire codomain arrow set.

~~~java
List<String> components = math.flow(math.categories,
        Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
        .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.identity-on")
        .<FiniteNaturalTransformation>performAlgebraTransfer("FiniteNaturalTransformation.identity-on")
        .performLeftProjectionOperation("component", BigInteger.TEN)
        .collect(); // ["10"]
~~~

The underlying category cap remains 128 arrows/objects. Tests include independent naturality checks in a noncommutative transformation monoid, vertical and horizontal composition against cyclic-group arithmetic, 729 interchange cases, noninvertible components, changed labels under pre/postcomposition, invalid boundaries and serialized flows. The implementation supplies finite operations and empirical tests, not proof-assistant verification or a general algorithm for diagram limits.

## Finite equivalence witnesses

FiniteEquivalenceAlgebra registers explicit [adjoint equivalences](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Equivalence.html). A value retains F:C->D, its chosen quasi-inverse G:D->C, the natural isomorphisms unit:Id_C->G.F and counit:F.G->Id_D, and checks both triangle identities at every object. These data can be supplied directly or constructed with FiniteEquivalence.from-functor for a full, faithful and essentially surjective functor.

Construction makes deterministic finite choices: prefer the least source object mapping exactly to each target object d, otherwise choose the least source object c and least isomorphism F(c)->d. Fullness and faithfulness give unique lifts for the backward arrow map and unit. This works for equivalent categories with different numbers of objects. Non-equivalence functors are not converted into witnesses.

compose applies the right operand first and combines the supplied units and counits. inverse reverses the equivalence using the inverse counit and inverse unit. opposite reverses the categories with the appropriately directed witnesses. Equality includes all chosen data: reversing an equivalence and composing it back need not give the identity witness by strict equality. The underlying functors are related by the retained natural isomorphisms.

~~~java
List<String> unitComponents = math.flow(math.categories,
        Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
        .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.identity-on")
        .<FiniteEquivalence>performAlgebraTransfer("FiniteEquivalence.from-functor")
        .<FiniteNaturalTransformation>performAlgebraTransfer("unit")
        .performLeftProjectionOperation("component", BigInteger.TEN)
        .collect(); // ["10"]
~~~

Tests check all functors between indiscrete categories with one through three objects against independent representative formulas, nontrivial cyclic-group unit/counit choices, composition and associativity, invalid triangles and serialized native flows. The finite category cap stays at 128 arrows/objects. FiniteAdjunctionAlgebra below separately handles noninvertible units and counits.

## Finite adjunctions and hom correspondences

FiniteAdjunctionAlgebra represents L:C->D left adjoint to R:D->C with unit:Id_C->R.L and counit:L.R->Id_D. Construction checks the typed natural transformations and both triangle identities, following the [unit/counit definition of an adjunction](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Adjunction/Basic.html). Components may be noninvertible: for example, inclusion of the initial object of a finite chain is left adjoint to the unique functor from that chain to the one-object category.

FiniteAdjunction.from-left takes a functor L and searches for a right adjoint. For every object d it chooses the least pair (c,epsilon:L(c)->d) for which a |-> L(a);epsilon is a bijection Hom_C(x,c)->Hom_D(L(x),d) for every x. This is a [finite universal-arrow construction](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Adjunction/Comma.html); it checks injectivity and surjectivity, including parallel arrows. It then constructs R's arrow map and the unit by unique lifts. from-right uses the dual construction to find a left adjoint to the supplied R. If an adjoint does not exist for the finite tables, construction reports OPERATION_UNDEFINED.

transpose takes (c,h) with h:L(c)->d and returns eta_c;R(h). untranspose takes (d,k) with k:c->R(d) and returns L(k);epsilon_d. Explicit object labels matter because a functor can identify objects. These operations accept the existing ZxZ.category pair carrier and return wrapped integers through IUnsafeOperation. hom-map takes (c,d) and returns the corresponding bijection in FiniteFunction(Z,Z), including empty hom sets. It retains both hom sets as the function's declared domain and codomain.

~~~java
List<String> arrows = math.flow(math.categories,
        Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
        .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.identity-on")
        .<FiniteAdjunction>performAlgebraTransfer("FiniteAdjunction.from-left")
        .<BigInteger,Pair<BigInteger,BigInteger>>performAlgebraUnsafe(
                "transpose", new Pair<>(BigInteger.TEN, BigInteger.TEN))
        .collect(); // ["10"]
~~~

compose applies the right operand's left functor first and composes right adjoints in reverse order. It combines the retained witnesses. opposite swaps the adjoint roles, giving R.op left adjoint to L.op. from-equivalence retains an equivalence's existing witnesses; to-equivalence succeeds exactly when both the unit and counit are invertible. Equality includes all chosen data.

Tests compare both adjoint constructors against independent min/max formulas for every monotone map between chains of size zero through four, check hom bijections with parallel noncommuting arrows, reject each triangle failure separately, and exercise composition, empty hom sets, preserved witness choices and serialized flows. The 128-arrow/object category cap remains in force. Search constructs one deterministic finite adjoint; arbitrary infinite adjoint functor theorems and enumeration of every witness remain outside this implementation.

## Finite diagram cones and limits

FiniteConeAlgebra registers cones over any supplied finite functor F:J->C. A cone retains a vertex c in C and one leg c->F(j) for each object j. Construction checks every equation leg_j;F(f)=leg_k for f:j->k. Internally this uses a natural transformation from the constant diagram at c to F, following the [cone definition](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Limits/Cones.html). Equality includes the vertex even when J is empty.

FiniteCone.cones-at, registered on the functor algebra, enumerates all cones at a supplied vertex by ascending leg labels. FiniteCone.limit searches all vertices and chooses the least vertex and then lexicographically least legs satisfying the [limit universal property](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Limits/IsLimit.html): every cone has exactly one commuting arrow into the chosen cone. Nonexistence reports OPERATION_UNDEFINED. is-limit checks the same property for an existing cone.

lift takes a limit cone and another cone over the same exact diagram, returning the unique arrow from the second vertex to the first through ICustomResultOperation. It requires the whole universal property, not merely uniqueness for that one input. The flat mediators operation works for any two cones over the same diagram and returns all commuting arrows in ascending label order, including empty or multiple results. leg uses ILeftProjectionOperation and returns a wrapped integer; flat legs retains repeated labels. leg-map and natural-transformation transfer into the existing native algebras.

map applies a functor to the target category, vertex and legs. reindex pulls a cone back along a functor into its shape. Both preserve cone equations; neither operation promises to preserve limits. FiniteCone.from-transformation accepts a natural transformation whose source is constant at an explicit vertex. FiniteFunctor.constant-at constructs a constant diagram with the same source and target categories as its first operand. FiniteFunctor.empty-diagram, registered on categories, provides the empty shape whose limits are terminal objects.

~~~java
List<String> vertices = math.flow(math.categories,
        Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
        .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.empty-diagram")
        .<FiniteCone>performAlgebraTransfer("FiniteCone.limit")
        .<BigInteger>performAlgebraTransfer("vertex")
        .collect(); // ["10"]
~~~

Search retains the category cap of 128 arrows/objects and additionally allows at most 10000 enumerated cones and 1000000 search steps per invocation. Exceeding either bound raises IMPLEMENTATION_FAILURE; it never yields a false existence claim or a truncated flat list. The bounds apply to enumeration, limit construction, is-limit and lift. Tests cover finite-chain products, products and equalizers in the full category of sets of sizes zero through two, fixed-point diagrams, missing limits, nonunique mediators, empty diagrams, loss of limits under mapping/reindexing, both resource caps and serialized native flows.

## Finite cocones and colimits

FiniteCoconeAlgebra represents cocones F(j)->c, checking F(f);leg_k=leg_j for each shape arrow f:j->k. Its natural-transformation transfer runs from F to the constant diagram at c, dual to the [cone construction](https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Limits/Cones.html). The vertex remains part of the value even for an empty shape.

FiniteCocone.colimit, registered on the functor algebra, searches for a cocone with exactly one outgoing commuting arrow to every other cocone. It uses the shared limit search on the opposite diagram and returns the least vertex and lexicographically least legs among valid choices. descend requires this entire colimit property and returns the unique arrow from the first cocone's vertex to the second. The flat mediators operation returns every such commuting arrow for any pair of cocones on the same exact diagram, including none or several. These operations return the existing integer wrappers.

cocones-at enumerates all cocones at a specified vertex; legs retains duplicate arrow labels, while leg-map transfers to the existing finite-function algebra. map and reindex preserve the cocone equations but may lose the colimit property. from-transformation requires a transformation ending at the constant diagram on the supplied vertex. opposite transfers a cocone into the cone algebra over the opposite diagram; FiniteCocone.opposite-cone is the reverse transfer registered on cones.

~~~java
List<String> vertices = math.flow(math.categories,
        Collections.singletonList(FiniteCategory.discrete(FiniteSet.of(BigInteger.TEN))))
        .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.empty-diagram")
        .<FiniteCocone>performAlgebraTransfer("FiniteCocone.colimit")
        .<BigInteger>performAlgebraTransfer("vertex")
        .collect(); // ["10"]
~~~

Empty-diagram colimits are initial objects. Tests independently check maxima in finite chains, disjoint-union sizes for coproducts in the category of sets of sizes zero through two, coequalizers and group-action orbits, outgoing arrow direction, nonunique mediators, loss of colimits under mapping/reindexing, dual conversions and serialized native flows. The shared 128-arrow/object, 10000-cone and 1000000-step bounds apply; exhaustion reports IMPLEMENTATION_FAILURE, and completed searches without a colimit report OPERATION_UNDEFINED. These finite algorithms do not construct infinite colimits or provide formal proof artifacts.


## Polynomial cells, oriented chains and exact integration

`math.polynomialCells` registers PolynomialCell(Q), whose members are polynomial parametrizations of oriented unit cubes, plus explicit rational points as zero-dimensional cells. The rational data define polynomial maps on real unit cubes [0,1]^k; integration is continuous oriented form integration with exact rational output. `from-map` uses the standard parameter orientation. `from-point` accepts a positive-dimensional rational coordinate vector. `PolynomialCell(Q).segment`, registered on Vec(Q), sends t to start+t*(end-start), including coincident endpoints. The parameter dimension remains part of the cell, independently of image dimension or rank. `evaluate` checks that every parameter lies in [0,1]; point cells accept the empty parameter vector and return their ambient coordinates wrapped in the actual second Vec(Q) carrier.

`lower-face` and `upper-face` fix a zero-based parameter axis at zero or one and remove it, keeping the other coordinates in order. Flat `faces` lists lower then upper for each axis, retaining duplicates. Flat `vertices` uses ascending binary masks, with bit i selecting the endpoint of parameter i. A point has no faces and one vertex. The product concatenates ambient and parameter coordinates, placing the first cell's coordinates first. `pushforward` composes an ambient polynomial map after the parametrization and preserves parameter dimension.

`math.polynomialChains` registers PolynomialChain(Q), immutable finite rational formal sums of cells with the same ambient dimension and integer degree. Equal canonical parametrizations combine; zero coefficients disappear. Zero chains retain their dimension and degree. Boundary uses sum_i (-1)^i*(upper_i-lower_i) and lowers degree by one. A point has zero boundary in degree -1; all negative integer degrees contain only zero chains, so repeated boundaries remain correctly graded. The signed face and chain conventions follow the standard cubical boundary described in [Sage's cubical-complex documentation](https://doc.sagemath.org/html/en/reference/topology/sage/topology/cubical_complex.html) and [chain documentation](https://doc.sagemath.org/html/en/reference/homology/sage/homology/chains.html).

`Poly(Q).integrate-unit-cube` sums each monomial coefficient divided by the product of its exponents plus one. `PolynomialForm(Q).integrate-unit-cube` requires a top-degree form and integrates its top coefficient. `PolynomialCell(Q).integrate` and `PolynomialChain(Q).integrate` accept a form of matching ambient dimension and homogeneous degree. They pull it back to each parameter cube and sum exact integrals, weighted by chain coefficients. Point integration evaluates a scalar zero-form. These operations reject nonmatching or mixed degrees even for a zero chain. The zero form can pair with any matching ambient chain degree, including negative-degree zero chains.

Orientation and multiplicity are preserved. A reversed parameter direction changes the corresponding integral sign; no absolute Jacobian is substituted. Constant and folded parametrizations remain formal cells even when their integral vanishes. This is a free chain representation with degenerate cells retained: equality does not identify geometric images, reparametrizations, homology classes or oriented reversals. No standard cubical homology groups are computed by this carrier.

```java
PolynomialCell rectangle = PolynomialCell.parameterized(new PolynomialMap(
        MultivariatePolynomial.variable(2, 0).scale(Rational.of(2)),
        MultivariatePolynomial.variable(2, 1).scale(Rational.of(3))));
Map<Integer, MultivariatePolynomial> coefficients = new TreeMap<>();
coefficients.put(1, MultivariatePolynomial.monomial(Rational.ONE, 2, 1));
coefficients.put(2, MultivariatePolynomial.monomial(Rational.ONE, 1, 2));
PolynomialDifferentialForm omega = new PolynomialDifferentialForm(2, coefficients);

List<String> boundaryIntegral = math.flow(math.polynomialCells, Collections.singletonList(rectangle))
        .<PolynomialChain>performAlgebraTransfer("PolynomialChain(Q).from-cell")
        .performOneOperandOperation("boundary")
        .<Rational,PolynomialDifferentialForm>performAlgebraUnsafe("integrate", omega)
        .collect(); // [10]
List<String> interiorIntegral = math.flow(math.polynomialCells, Collections.singletonList(rectangle))
        .<Rational,PolynomialDifferentialForm>performAlgebraUnsafe("integrate", omega.exteriorDerivative())
        .collect(); // [10]
```

The implementation caps ambient dimension at 20, parameter dimension and positive chain degree at 10, and intermediate/output chain support at 10,000 cells. Boundary and product also cap candidate face/cell visits at 100,000. Each complete cell evaluation, face or vertex list, boundary, product, pushforward or integral shares 1,000,000 polynomial work units across its cells, coefficient-coordinate scans and candidate coefficient products. Existing polynomial and form degree/support caps still apply. Exceeding a cap raises IMPLEMENTATION_FAILURE, while a wrong dimension, degree, face index, conversion or parameter domain raises OPERATION_UNDEFINED. Coefficient bit lengths remain unbounded.

NativePolynomialChainsTest compares 729 polynomial integrals with independent tensor-Simpson quadrature and integrates constant two-forms over 729 ternary rectangular linear cells against independent signed minors. It also checks nonlinear Stokes identities through dimension four, boundary squared, the graded boundary rule for products, pushforward naturality, point and negative-degree behavior, folded/orientation-reversing cells, formal equality, aggregate computation limits, actual wrappers and serialized boundary-integration flows. The scope is exact polynomial cubical integration; arbitrary manifolds, nonpolynomial integrands, general integration regions, quotient homology and numerical error contracts remain future work.

## Integer matrices and Diophantine systems

`math.integerVectors` and `math.integerMatrices` register Vec(Z) and Mat(Z). Coordinates are exact BigInteger values. Matrices retain their row and column dimensions, including m by 0 and 0 by n; they represent maps Z^n -> Z^m. Vector addition, matrix addition and composition check the relevant dimensions. This family of free modules is not one vector space or one matrix ring across all shapes.

`smith-decomposition` emits exactly [U,D,V] with U*A*V=D and square unimodular U,V. D has the input shape and positive nonzero invariant factors dividing their successors, followed by zeros. D is canonical; U,V and the resulting bases are algorithm-selected witnesses. `smith-form` and flat `smith-invariant-factors` avoid computing the witnesses. The transformation convention agrees with [Sage's integer-matrix documentation](https://doc.sagemath.org/html/en/reference/matrices/sage/matrix/matrix_integer_dense.html); our emitted list order is [U,D,V].

`kernel-basis` emits the trailing columns of V, giving the whole integer kernel. Clearing denominators in a rational basis does not in general give this lattice. `image-basis` emits A times the first rank(A) columns of V and retains the actual image index: the image of 2*I is generated by twice the standard basis, not the standard basis. `cokernel` returns the type Z^m/image(A), with free rank m-rank(A) and torsion from the nonunit Smith factors. Matrix columns are relations, so this operation classifies finitely generated abelian presentations.

~~~java
IntegerMatrix equation = new IntegerMatrix(new BigInteger[][] {
        {BigInteger.valueOf(2), BigInteger.valueOf(3)}
});
List<String> generators = math.flow(math.integerMatrices, Collections.singletonList(equation))
        .performLeftProjectionFlatOperation("solve-generators", new IntegerVector(BigInteger.ONE))
        .collect(); // ["[-1, 1]", "[3, -2]"]
~~~

These are the solutions of 2*x+3*y=1: (-1,1)+t*(3,-2), for every integer t. `solve-particular` returns the first vector alone; `solve-generators` emits it followed by an integer kernel basis, even when the particular solution is zero. The list parametrizes solutions and does not enumerate them. Incompatible right-hand-side dimensions or the absence of an integer solution are undefined. `has-integer-solution` returns false for a correctly shaped unsolvable system; resource exhaustion raises IMPLEMENTATION_FAILURE. No minimum-norm claim is made.

`inverse-unimodular` requires a square matrix with determinant plus or minus one, including the empty square identity. It returns V*U. A matrix invertible over Q may have no inverse over Z. `to-rational` embeds coordinates exactly; reverse conversions require denominator one for every entry. Vec(Q) accepts empty vectors, but Mat(Q) currently requires positive dimensions, so integer matrices with an empty dimension cannot transfer into it.

The five-argument FiniteSimplicialAlgebra constructor, used by ConcreteMathematics, adds `boundary-matrix`: FiniteComplex x N -> Mat(Z). Its rows and columns use lexicographically ordered increasing-vertex simplices in degrees k-1 and k. Degree zero has shape 0 by vertex-count; degree dimension+1 has top-simplex-count rows and no columns. Larger degrees return 0 by 0, even for arbitrarily large BigInteger arguments. Adjacent boundaries multiply to zero. Applying `kernel-basis` gives integral cycle coordinates; a boundary cokernel is generally not the corresponding homology group because homology also imposes the outgoing cycle condition.

Each dimension is limited to 256. Matrix multiplication, Smith reduction, kernel/image construction, inverse and solve operations have a 5,000,000-unit work budget. One computation shares that budget across witness updates and subsequent products; even sparse or identity inputs receive dense charges. Exhaustion may occur below the dimension cap and never returns a truncated basis or a false solvability result. Coefficient bit lengths are unbounded. The quotient-element carriers below build on these matrices. Induced homology maps, Hermite forms, lattice reduction and shortest-vector optimization remain outside this implementation.

NativeIntegerLinearTest verifies U*A*V=D and unit determinants for 729 ternary 2 by 3 matrices and their transposes, checks 6,561 linear systems against independent lattice-index criteria from minors, and tests integer inverses on 625 square matrices. Regressions cover saturated kernels, non-saturated image lattices, free/torsion cokernels, empty shapes, huge exact coefficients, resource failures, boundary composition, actual carrier wrappers and serialized flows.

## Presented abelian groups and their elements

`math.presentedAbelianGroups` registers PresentedAbelianGroup for Z^m/image(A), with the m by n integer relation matrix A and its Smith-coordinate map retained. `math.abelianGroupElements` registers the corresponding family of element values. These extend the earlier isomorphism-type calculations with quotient elements, projection, arithmetic and lifting. Construction is available from Mat(Z) through `PresentedAbelianGroup.from-matrix`, or from AbelianGroupType through `PresentedAbelianGroup.from-type`.

If U*A*V=D, an original-generator coordinate vector x represents the canonical Smith vector U*x, reduced modulo each positive diagonal entry of D. Coordinates for unit entries become zero; free coordinates remain arbitrary integers. `AbelianGroupElement.project` takes original coordinates, while `AbelianGroupElement.from-smith` takes the full vector in the retained Smith basis. `smith-coordinates` returns that canonical vector. `representative` solves U*x=canonical coordinates to produce one original-coordinate lift; reprojection recovers the element, but lifting is not generally additive.

`AbelianGroupElement.generators` emits the images of the original basis in order, retaining zero and redundant generators. `AbelianGroupElement.smith-generators` omits killed coordinates and emits a minimal generating list. This distinction between supplied and Smith generators is also described in [Sage's additive abelian group documentation](https://doc.sagemath.org/html/en/reference/groups/sage/groups/additive_abelian/additive_abelian_group.html). The quotient representation follows finite integer presentations, a special case of the [finitely generated PID module model](https://doc.sagemath.org/html/en/reference/modules/sage/modules/fg_pid/fgp_module.html).

~~~java
IntegerMatrix relations = new IntegerMatrix(new BigInteger[][] {
        {BigInteger.valueOf(2), BigInteger.ZERO},
        {BigInteger.ZERO, BigInteger.valueOf(3)}
});
List<String> orders = math.flow(math.integerMatrices, Collections.singletonList(relations))
        .<PresentedAbelianGroup>performAlgebraTransfer("PresentedAbelianGroup.from-matrix")
        .<AbelianGroupElement>performFlatAlgebraTransfer("AbelianGroupElement.elements")
        .<BigInteger>performAlgebraTransfer("order").collect(); // ["1", "6", "3", "2", "3", "6"]
~~~

This presentation has two original generators of orders two and three; its minimal Smith generating list has one element of order six. Group `equal` compares the retained relation matrix and coordinate map. `isomorphic` compares the canonical group type, without constructing an isomorphism or identifying elements. Addition/subtraction require matching presentations, while negation, integer scaling and zero-like retain the presentation. Abstractly isomorphic presentations cannot be mixed implicitly. `direct-sum` retains the ordered block-diagonal presentation.

Element `order` is defined for torsion elements, including those in infinite groups; zero has order one. A nonzero free coordinate means infinite order and is undefined as an N-valued result. `cyclic-subgroup` enumerates zero, a, 2*a and so on, stopping before the first repeated zero. Group `elements` enumerates every element of a finite group in mixed-radix Smith order, with the first coordinate varying fastest. The trivial group has one enumerated element.

~~~java
List<String> roots = math.flow(math.integerMatrices, Collections.singletonList(relations))
        .<PresentedAbelianGroup>performAlgebraTransfer("PresentedAbelianGroup.from-matrix")
        .<AbelianGroupElement,IntegerVector>performAlgebraUnsafe("AbelianGroupElement.from-smith",
                new IntegerVector(BigInteger.ZERO, BigInteger.valueOf(2)))
        .performFlatCustomMemberOperation("multiplication-preimages", BigInteger.valueOf(2))
        .<IntegerVector>performAlgebraTransfer("smith-coordinates").collect(); // ["[0, 1]", "[0, 4]"]
~~~

These are all solutions of 2*x=(0,2) in the retained Smith coordinates. `multiplication-preimages` accepts negative and zero scalars as well. Nonzero scalars have finite solution sets, possibly empty, even in infinite finitely generated groups. For scalar zero, a nonzero target has no preimages; a zero target has the entire group as preimage, so enumeration requires that group to be finite. Oversized finite outputs raise IMPLEMENTATION_FAILURE, while infinite requested lists are undefined. The operation returns first-carrier element wrappers; `AbelianGroupElement.reduce` instead returns canonical Smith coordinates in the second Vec(Z) wrapper.

Presentations are limited to 256 generators and 256 relations. Construction and representative lifting use the existing 5,000,000-unit Smith computation budget; exact coefficient bit lengths are unbounded. Each finite enumeration is capped at 4096 elements and either returns its complete result or fails. Explicit homomorphisms and their kernel/image/cokernel presentations are available through the homomorphism builder below. IntegralHomology supplies homology generators. Automatic isomorphism search, arbitrary subgroup lattices and nonabelian presentations remain future work.

NativePresentedAbelianTest checks quotient equality on 15,625 vectors against independent determinantal-divisor criteria, compares 64 finite cyclic products with independently counted element orders, and exhaustively checks all scaling preimages for scalars -6 through 6 in 36 small cyclic products. Free coordinates, killed generators, relation invariance, lift round-trips, distinct isomorphic presentations, finite/infinite output behavior, resource limits, actual wrappers and serialized flows are also covered.

## Homomorphisms of presented abelian groups

`math.abelianHomomorphisms` registers 32 operations on AbelianGroupHomomorphism through the existing native operation interfaces. Each value retains source and target presentations and a matrix M of generator images in full Smith coordinates. Rows belong to the target and columns to the source; killed coordinates remain present. Each finite target coordinate is reduced modulo its Smith factor, and construction checks that every source relation maps to zero. Equality compares both presentations and the normalized matrix. Different integer lifts can represent the same map; different isomorphic boundary presentations are not implicitly identified.

`from-matrix` takes Mat(Z) and a `Pair<PresentedAbelianGroup,PresentedAbelianGroup>` containing source then target. Its matrix F uses the original presentation generators, and converts to M = U_target * F * U_source^-1. `from-smith-matrix` accepts M directly. `matrix-lift` returns one original-coordinate lift; `generator-images` emits target elements in original source generator order, including repeated and zero images. These conventions are compatible with the explicit-generator model for [finitely generated abelian module morphisms](https://doc.sagemath.org/html/en/reference/modules/sage/modules/fg_pid/fgp_morphism.html).

`compose` applies the right operand first and checks equality of the middle presentations. `add` and `subtract` require the same source and target. Negation, integer scaling, identity and zero maps retain their declared boundaries. `apply` requires an element from the source; `preimage` requires an element from the target and returns one source element. Both use the historical second-result interface and return an IAlgebraItem in the registered AbelianGroupElement carrier. `has-preimage` tests exact integer solvability. The complete fiber of a solvable element is the returned preimage plus the kernel; the operation does not enumerate it. `inverse` is defined exactly for an isomorphism, with exchanged source and target presentations.

`kernel`, `image` and `cokernel` return actual PresentedAbelianGroup values. `kernel-inclusion` embeds the computed kernel in the source. `image-projection` maps the source onto the computed image and `image-inclusion` embeds that image in the target; their composition equals the original map. `cokernel-projection` maps the target onto the quotient by the image. The original map composed with kernel inclusion, and cokernel projection composed with the original map, are zero. These outputs can be applied and composed in subsequent MathTool flows.

In Smith coordinates let R_source and R_target contain the nonzero diagonal relation columns. An integral kernel basis of [M,-R_target], restricted to its source rows, gives an independent lattice basis K of vectors mapped to target relations. Solving K*C=R_source presents the kernel by C. The image is presented by K in the source Smith-coordinate free module; the cokernel is presented by [R_target,M] in the target Smith-coordinate free module. Smith coordinate changes on these new presentations determine the explicit maps.

```java
PresentedAbelianGroup group = new PresentedAbelianGroup(
        new IntegerMatrix(new BigInteger[][] {{BigInteger.valueOf(6)}}));
List<String> kernelType = math.flow(math.presentedAbelianGroups, Collections.singletonList(group))
        .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe(
                "AbelianGroupHomomorphism.scaling-on", BigInteger.valueOf(2))
        .<PresentedAbelianGroup>performAlgebraTransfer("kernel")
        .<AbelianGroupType>performAlgebraTransfer("as-type").collect();
// [AbelianGroup(rank=0, torsion=[2])]
```

Every matrix, including augmented auxiliary matrices, has at most 256 rows and columns. Each compound computation shares a 5,000,000-unit budget across reductions, solves and products. Dense costs apply even to identity or sparse matrices; exhaustion raises IMPLEMENTATION_FAILURE and never a false injectivity or solvability result. Exact coefficient bit lengths remain unbounded. The scope is finitely presented abelian groups over Z; automatic isomorphism search, arbitrary coefficient rings, subgroup lattice enumeration, nonabelian maps remain outside this implementation. IntegralHomology supplies degreewise induced maps from matrices that preserve cycles and boundaries.

NativeAbelianHomomorphismTest compares every homomorphism between cyclic groups of orders one through eight with independent modular arithmetic and checks finite product maps against enumeration. It also covers free and mixed groups, kernel/image/cokernel factorizations, inverses between distinct presentations, original versus Smith matrix lifts, zero dimensions, killed generators, relation and boundary failures, shared resource limits, actual carrier wrappers and serialized flows. All 32 registrations additionally have explicit expected results in ConcreteAlgebrasTest.

## Constructive integral homology

`math.integralHomology` registers IntegralHomology, the homology in one degree of two consecutive maps between finite free integer chain groups. `IntegralHomology.from-boundaries` takes the outgoing matrix d_k first and incoming matrix d_(k+1) second; construction checks their shared middle dimension and d_k*d_(k+1)=0. `IntegralHomology.at-degree` instead takes FiniteComplex and N. It uses the same lexicographic simplex order and increasing-vertex orientation as `boundary-matrix`. Homology is unreduced, so a nonempty connected complex has H_0 = Z.

The value retains both boundaries and a matrix K whose columns form a basis of the full integer kernel of d_k. Solving K*R=d_(k+1) gives the incoming boundaries in cycle coordinates. `group` returns the presentation Z^columns(K)/image(R); `as-type` forgets the presentation and returns its classified group type. `cycle-matrix`, `boundary-coordinates`, `outgoing-boundary` and `incoming-boundary` expose the exact matrices. Flat `cycle-basis` and `boundary-basis` emit integral lattice bases in the original chain coordinates. `generators` emits chain representatives for the minimal nontrivial torsion and free Smith generators. This distinction between homology groups and representatives follows the constructive conventions described in [Sage's chain-complex documentation](https://doc.sagemath.org/html/en/reference/homology/sage/homology/chain_complex.html).

`class-of` accepts a cycle and returns an AbelianGroupElement in the presented homology group. `representative` returns one original-coordinate cycle for such a class. It is a set-theoretic section: class-of after representative is identity, but the section need not preserve addition. `is-cycle` tests d_k*z=0; `is-boundary` tests integral solvability of d_(k+1)*b=z. `bounding-chain` returns one such b and is undefined if no filling exists. `cycle-coordinates` and `from-cycle-coordinates` convert through K. All three vector-valued operations use the native second-result wrapper, with dimensions checked by the retained value. `projection` returns the actual AbelianGroupHomomorphism from the free cycle-coordinate group onto homology. It is defined on cycle coordinates, not arbitrary chains.

```java
FiniteSimplicialComplex circle = new FiniteSimplicialComplex(Arrays.asList(
        FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2)));
List<String> cycles = math.flow(math.complexes, Collections.singletonList(circle))
        .<IntegralHomology,BigInteger>performAlgebraUnsafe(
                "IntegralHomology.at-degree", BigInteger.ONE)
        .<IntegerVector>performFlatAlgebraTransfer("generators").collect();
// [[1, -1, 1]] in edge order 01,02,12.
```

`induced-map` accepts the source IntegralHomology value and a pair containing target IntegralHomology and a matrix F on the chain groups in this degree. F must send source cycles into target cycles and source boundaries into target boundaries; both conditions are checked over Z. The result is a native AbelianGroupHomomorphism, which supports application, composition and kernel/image/cokernel operations. Identity and composition agree with the induced matrices, and changing a cycle by a boundary leaves its resulting class unchanged. This follows the usual [induced homology map](https://doc.sagemath.org/html/en/reference/homology/sage/homology/homology_morphism.html) construction; the API checks exactly the degreewise data supplied and does not infer adjacent chain-map components or a simplicial vertex map.

`chain-rank`, `cycle-rank`, `boundary-rank` and `betti-number` return natural numbers; Betti number is the integral free rank, equivalently the rational Betti number. `is-acyclic` checks trivial homology in this single degree, including torsion. Equality compares both retained boundary matrices, rather than just the group type. Coordinate vectors use the retained basis convention; quotient elements use the resulting presented-group convention. Above-top degrees and zero-sized chain groups retain their actual matrix dimensions.

Each matrix dimension is capped at 256. A whole construction, representative/coordinate solve, generator computation or induced-map calculation shares a 5,000,000-unit integer budget across Smith reductions, solves and products. Dense costs apply even to sparse or identity matrices. Resource exhaustion is IMPLEMENTATION_FAILURE, never a false predicate or a partial witness list; coefficient bit lengths remain unbounded. SimplicialMap supplies vertex-defined maps and their chain matrices separately. RelativeComplex supplies relative homology and long exact sequence maps. SimplicialCochain uses this same kernel/quotient implementation for integral cohomology and supplies cup products. Reduced homology, arbitrary complete chain-map builders, shortest cycles and persistence remain outside scope.

NativeConstructiveHomologyTest checks 336 small chain complexes against primitive-lattice arithmetic, all 64 four-vertex graphs against independent connectivity and cycle counts, and an explicit order-two cycle and filling in the projective plane. Further checks cover circle reflection, inclusion into a filled disk, induced-map composition and boundary invariance, nonprimitive boundary lattices, nonadditive sections, invalid maps, empty dimensions, resource limits, native wrappers and serialized flows. Every one of the 27 registrations also has an explicit expected result in ConcreteAlgebrasTest.

## Simplicial maps from vertices to homology

`math.simplicialMaps` registers 30 operations on SimplicialMap. A value retains full labelled source and target complexes and a total vertex map, and validates that every source simplex has an image simplex in the target. Repeated vertex images are allowed. `SimplicialMap.from-function` accepts a native FiniteFunction(Z,Z) and a pair of complexes, source first. The function's declared domain and codomain must equal the complete vertex sets. Vertex labels are the existing complex's Java int labels, exposed through the actual registered Z carrier; arbitrary out-of-domain BigInteger labels are rejected.

`chain-matrix` constructs the integral chain map in one nonnegative degree. Rows index target simplices and columns source simplices in the same lexicographic order as the boundary matrices. Vertices within each oriented simplex are increasing. Distinct images contribute the sign needed to sort the image vertex sequence; a simplex with repeated image vertices maps to zero in that degree. The matrices satisfy d_target*F_k=F_(k-1)*d_source, including collapsed simplices, and compose in the same order as the vertex maps. These are the standard conventions for [simplicial morphisms and their chain maps](https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex_morphism.html).

`homology-map` constructs both retained IntegralHomology values and returns the induced AbelianGroupHomomorphism, usable with application, composition, inversion and kernel/image/cokernel operations. `source-homology` and `target-homology` expose the coordinate models. Flat `chain-matrices` and `homology-maps` emit degrees zero through the larger complex dimension, retaining zero-sized rectangular matrices and trivial-group maps. Two empty complexes give empty lists. `SimplicialMap.simplex-basis`, registered on FiniteComplex, emits the ordered simplex labels needed to interpret chain vectors and matrices. All homology is unreduced over Z, using the [chain-complex morphism conventions](https://doc.sagemath.org/html/en/reference/homology/sage/homology/chain_complex_morphism.html).

```java
FiniteSimplicialComplex circle = new FiniteSimplicialComplex(Arrays.asList(
        FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2)));
Map<BigInteger,BigInteger> vertices = new TreeMap<>();
vertices.put(BigInteger.ZERO, BigInteger.ONE);
vertices.put(BigInteger.ONE, BigInteger.ZERO);
vertices.put(BigInteger.valueOf(2), BigInteger.valueOf(2));
FiniteSimplicialMap reflection = new FiniteSimplicialMap(circle, circle, vertices);

List<String> induced = math.flow(math.simplicialMaps, Collections.singletonList(reflection))
        .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("homology-map", BigInteger.ONE)
        .<IntegerMatrix>performAlgebraTransfer("smith-matrix").collect();
// [ZMatrix(1x1)[[-1]]]: reflection reverses the integral circle generator.
```

`compose` applies the right operand first and requires equality of the full labelled middle complexes. `inverse` requires a simplicial isomorphism: a vertex bijection whose image contains every target simplex. For example, inclusion of a circle into a filled triangle is bijective on vertices but has no simplicial inverse. `is-vertex-surjective` distinguishes vertex coverage from `is-surjective`, which checks the entire image complex. `image` returns the actual image subcomplex, not the induced subcomplex on its vertices. `restrict` accepts a source subcomplex and retains the original target; `corestrict-image` changes only the target to the actual image. Identity, inclusion, constant-at and empty-to constructors preserve these boundary conventions.

`map-vertex` and flat `vertex-fiber` return second Z wrappers. Fiber results and `vertex-images` follow ascending source labels, retaining repetitions in image lists and empty fibers for unreachable target vertices. `vertex-map` returns a FiniteFunction using the registered integer Algebra. `map-simplex` accepts a nonempty source simplex and returns its image as a second FiniteSet(Z) wrapper, including lower-dimensional collapsed images. `contiguous` checks that, for each source simplex, the union of the two image vertex sets is a target simplex. It requires parallel maps and implies equal induced homology maps; it does not decide general homotopy or search for chains of contiguous maps.

Map construction allows at most 4096 nonempty simplices in each complex. Each matrix degree or required homology boundary is limited to 256 simplices. Compound computations use a 5,000,000-unit work budget; a homology-map call shares it across source and target construction and the induced map, and a flat homology-maps call shares it across all degrees. Exhaustion raises IMPLEMENTATION_FAILURE and returns no partial list. Integer coefficient bit lengths remain unbounded. SimplicialCochain supplies contravariant integral cohomology maps. Subdivision, simplicial approximation, arbitrary chain-map construction, homotopy search and persistent homology remain outside scope. RelativeComplex supplies relative homology and long exact sequence maps separately.

NativeSimplicialMapTest checks all 256 vertex maps of a tetrahedron against independent determinant signs and boundary identities, all 27 circle maps and 729 compositions against winding numbers, a degree-two covering and its index-two homology image, collapsed simplices, restrictions, empty maps and projective-plane torsion. It also checks native wrapper and serialization behavior and an aggregate budget failure where each degree succeeds individually. Every registration has an explicit expected result in ConcreteAlgebrasTest.

## Relative simplicial homology and the long exact sequence

`math.relativeComplexes` registers 27 operations on RelativeComplex. `RelativeComplex.from-complexes`, registered on the existing FiniteComplex algebra, takes X first and A second and requires every labelled simplex of A to belong to X. The retained pair represents the integral quotient chain complex C(X)/C(A). Relative degree-k bases consist of the simplices of X outside A, in the same lexicographic increasing-vertex order as the absolute boundaries. `simplex-basis` exposes that order in FiniteSet(Z) wrappers; `boundary-matrix` discards oriented faces in A. `dimension` is the highest nonzero relative chain degree, or -1 for (X,X), and `euler-characteristic` is chi(X)-chi(A).

`homology` returns an actual IntegralHomology value, so cycle representatives, quotient elements, class-of, torsion orders and bounding-chain witnesses use the existing operations. `homology-type` and `betti-number` expose the integral group type and its free rank. `is-acyclic-degree` also checks torsion. Flat `boundary-matrices`, `homology-degrees` and `homology-types` emit degrees zero through dim(X), including zero groups even when the relative dimension is smaller. An empty X gives empty degree lists. `RelativeComplex.absolute` constructs (X,empty), and `RelativeComplex.diagonal` constructs (X,X). All homology uses the unreduced convention over Z.

`inclusion-matrix` gives J:C_k(A)->C_k(X), while `projection-matrix` gives P:C_k(X)->C_k(X,A). Both commute with boundaries. `lift-matrix` is P transposed: a section on chain groups that inserts zeros on A. It is generally **not a chain map**. `connecting-chain-matrix` keeps the A-component of the boundary of this lift; it sends relative cycles to cycles in A, though arbitrary relative chains need not have that property. These are the quotient-chain and connecting-homomorphism conventions of [Hatcher, Algebraic Topology, section 2.1](https://pi.math.cornell.edu/~hatcher/AT/AT.pdf).

`inclusion-homology`, `quotient-homology` and `connecting-homology` return AbelianGroupHomomorphism values with their actual source and target presentations. Flat `long-exact-segment(k)` returns these three maps in order:

```text
H_k(A) -> H_k(X) -> H_k(X,A) -> H_(k-1)(A)
```

Consecutive maps compose to zero, and each image equals the following kernel. The connecting map takes the class of the lifted boundary without an extra sign. At degree zero its target is the trivial H_-1(A), consistent with unreduced homology. The separate `inclusion` transfer returns the actual SimplicialMap A->X.

```java
FiniteSimplicialComplex interval = new FiniteSimplicialComplex(
    Collections.singletonList(FiniteSet.of(0, 1)));
FiniteSimplicialComplex endpoints = new FiniteSimplicialComplex(
    Arrays.asList(FiniteSet.of(0), FiniteSet.of(1)));
math.flow(math.complexes, Collections.singletonList(interval))
    .<RelativeSimplicialComplex>performCustomResultOperation(
        "RelativeComplex.from-complexes", endpoints)
    .<AbelianGroupHomomorphism, BigInteger>performAlgebraUnsafe(
        "connecting-homology", BigInteger.ONE)
    .<IntegerMatrix>performAlgebraTransfer("smith-matrix")
    .collect(); // [ZMatrix(2x1)[[-1], [1]]]
```

Thus the relative edge generator maps to the right endpoint minus the left endpoint. For a filled triangle relative to its boundary circle, the degree-two connecting map is an isomorphism Z->Z. For the projective plane relative to its one-skeleton, the connecting map is injective between rank-ten free groups with cokernel Z/2, retaining the integral index information.

Each complex is limited to 4096 nonempty simplices. Each required matrix basis is limited to 256 simplices; relative bases are filtered before this bound, so a large diagonal pair can have computable zero relative homology even when its absolute matrices exceed the bound. Operations involving full ambient/subcomplex matrices require those bases to fit too. A 5,000,000-unit work budget is shared across each compound homology calculation, degree list or three-map segment. Exhaustion raises IMPLEMENTATION_FAILURE without partial lists; integer coefficient bit lengths remain unbounded. RelativeMap supplies simplicial maps between pairs and naturality maps separately. RelativeCochain supplies integral relative cohomology, cup products and natural long exact cohomology sequences. Arbitrary relative chain maps, reduced homology, persistence and general topological excision remain outside scope. SimplicialCover supplies the excision map for a two-subcomplex union.

NativeRelativeHomologyTest checks 1,024 graph/vertex-subcomplex pairs against independent connectivity formulas, disks relative to boundary spheres through dimension four, projective-plane torsion and its index-two connecting image, exactness over Z, chain identities and orientation signs, empty and huge degrees, basis and aggregate work limits, actual wrappers and serialized flows. All 27 registrations also have explicit expected results in ConcreteAlgebrasTest.

## Simplicial maps of pairs

`math.relativeMaps` registers 26 operations on RelativeMap. A map (X,A)->(Y,B) retains the full source and target pairs and an existing SimplicialMap X->Y. Construction validates exact agreement of the ambient boundaries and requires the image of every A simplex to belong to B. Checking only the vertices would be insufficient when B is not an induced subcomplex. The `ambient-map` and `subcomplex-map` transfers expose the actual maps X->Y and A->B.

`RelativeMap.from-map` accepts the ambient map followed by `Pair<RelativeSimplicialComplex,RelativeSimplicialComplex>` with source first. `RelativeMap.identity-on` and `RelativeMap.inclusion` construct identities and componentwise inclusions. `RelativeMap.absolute` extends X->Y to (X,empty)->(Y,empty); `RelativeMap.diagonal` extends it to (X,X)->(Y,Y). Composition applies the right operand first and requires equality of both components of the middle pair. Inversion requires an ambient simplicial isomorphism that maps A onto B. An ambient isomorphism alone need not give a pair isomorphism.

Relative chain matrices use the existing quotient simplex bases and orientation signs. A column is zero if its simplex collapses or its image lies in B. The implementation constructs the quotient matrix directly, so it does not require a potentially larger ambient matrix. `homology-map` returns the induced integral AbelianGroupHomomorphism, retaining source and target presentations and torsion. `source-homology` and `target-homology` expose its coordinate models. Flat matrix/map lists run from degree zero through the larger ambient dimension and retain zero groups and empty matrix dimensions.

`long-exact-maps(k)` emits four vertical maps, in order, for the two existing long-exact segments:

```text
H_k(A)  -> H_k(X) -> H_k(X,A) -> H_(k-1)(A)
  |          |          |            |
  v          v          v            v
H_k(B)  -> H_k(Y) -> H_k(Y,B) -> H_(k-1)(B)
```

All three squares commute, including the square with the connecting homomorphism. The fourth vertical map at degree zero is 0->0 under the unreduced convention. The first two maps are also available as `subcomplex-homology-map` and `ambient-homology-map`. These follow the [naturality of the long exact sequence of a pair](https://pi.math.cornell.edu/~hatcher/AT/AT.pdf).

```java
Map<BigInteger, BigInteger> vertices = new TreeMap<>();
vertices.put(BigInteger.ZERO, BigInteger.ONE);
vertices.put(BigInteger.ONE, BigInteger.ZERO);
FiniteSimplicialMap reflection = new FiniteSimplicialMap(interval, interval, vertices);
RelativeSimplicialComplex pair = new RelativeSimplicialComplex(interval, endpoints);
math.flow(math.simplicialMaps, Collections.singletonList(reflection))
    .<RelativeSimplicialMap, Pair<RelativeSimplicialComplex, RelativeSimplicialComplex>>
        performAlgebraUnsafe("RelativeMap.from-map", new Pair<>(pair, pair))
    .<AbelianGroupHomomorphism, BigInteger>
        performAlgebraUnsafe("homology-map", BigInteger.ONE)
    .<IntegerMatrix>performAlgebraTransfer("smith-matrix")
    .collect(); // [ZMatrix(1x1)[[-1]]]
```

Here `interval` and `endpoints` are the complexes in the preceding example. Reversing the interval changes the relative edge class by -1 and swaps the endpoints, as required by the connecting square. Pair contiguity is stricter than ambient contiguity: unions of images of A simplices must lie in B as well. In particular, the interval identity and reflection are ambient-contiguous but are not contiguous as maps relative to the two endpoints. `image` returns (f(X),f(A)), `corestrict-image` retains that target pair, and `restrict` accepts a componentwise source subpair while retaining the complete original target.

The existing bounds apply: 4096 nonempty simplices per boundary complex, 256 simplices per required matrix basis, and a shared 5,000,000-unit integer budget per compound homology computation, entire degree list or four-map naturality list. Relative bases are filtered before the matrix bound; ambient/subcomplex homology needs the corresponding full bases. Exhaustion raises IMPLEMENTATION_FAILURE without partial lists. Coefficient bit lengths remain unbounded. RelativeCochain supplies contravariant relative cohomology maps and long exact sequence naturality. Arbitrary relative chain maps, explicit chain-homotopy witnesses, reduced homology, persistence, subdivision and general topological excision remain outside scope. SimplicialCover supplies the excision map for a two-subcomplex union.

NativeRelativeSimplicialMapTest checks 256 tetrahedron pair maps against determinant signs, all 27 triangle pair maps and 729 compositions, all three naturality squares, a degree-two covering whose relative map is surjective, projective-plane torsion, pair contiguity, zero quotient images, pair isomorphisms, image/restriction behavior, empty and relabelled boundaries, shared degree/four-map budgets, actual native wrappers and serialized flows. Each of the 26 registrations has an explicit expected result in ConcreteAlgebrasTest.

## Constructive Mayer-Vietoris and simplicial excision

`math.simplicialCovers` registers 29 operations on SimplicialCover. A value retains two ordered labelled complexes A and B, their union U, and their intersection I. `SimplicialCover.from-complexes` is registered on FiniteComplex. U is exactly the simplex-wise union, so every U simplex belongs to at least one piece. No open-cover condition or subdivision is needed for this finite simplicial chain construction, and covering only the vertices of some larger external complex is not asserted to cover that complex.

The chain sequence and its signs are explicit:

```text
0 -> C_k(I) --(i,-j)--> C_k(A) + C_k(B) --addition--> C_k(U) -> 0
```

The middle group is a direct sum, with all ordered A simplices followed by all ordered B simplices, including a separate copy in each block for shared simplices. `sum-boundary-matrix` is block diagonal. `intersection-matrix` puts +1 in the left block and -1 in the right; `union-matrix` adds the two copies. `split-matrix` chooses the left copy whenever a simplex is shared. It is a section on chain groups and generally does not commute with boundaries, so it does not assert a split homology sequence.

`sum-homology` returns homology of this actual block chain complex in an IntegralHomology wrapper. Its retained presentation can differ from a separately normalized presentation of H_k(A) plus H_k(B). The four component inclusion/projection operations supply explicit maps between these homologies: projections after their matching inclusions are identities, cross-composites vanish, and the two inclusion-projection composites sum to the identity. The left, right, intersection and union homology operations expose each corresponding coordinate model. Flat sum boundary and homology lists run from degree zero through dim(U), with empty lists when U is empty.

The three scalar homology-map operations and flat `long-exact-segment(k)` give:

```text
H_k(I) -> H_k(C(A) + C(B)) -> H_k(U) -> H_(k-1)(I)
```

The connecting map takes a union cycle z, splits it as a+b with shared simplices assigned left, and returns the class of the boundary of a in I. `connecting-chain-matrix` exposes this intersection component on chains. Only on union cycles must the entire boundary of a lie in I. The degree-zero target is the trivial unreduced H_-1(I). These conventions implement the [simplicial Mayer-Vietoris sequence](https://pi.math.cornell.edu/~hatcher/AT/AT.pdf); the sequence remains exact over integers, including nonprimitive images and torsion. Swapping the two pieces negates the connecting homomorphism.

```java
FiniteSimplicialComplex arc = new FiniteSimplicialComplex(
    Arrays.asList(FiniteSet.of(0, 1), FiniteSet.of(1, 2)));
FiniteSimplicialComplex closingEdge = new FiniteSimplicialComplex(
    Collections.singletonList(FiniteSet.of(0, 2)));
math.flow(math.complexes, Collections.singletonList(arc))
    .<SimplicialCover>performCustomResultOperation(
        "SimplicialCover.from-complexes", closingEdge)
    .<AbelianGroupHomomorphism, BigInteger>performAlgebraUnsafe(
        "connecting-homology-map", BigInteger.ONE)
    .<IntegerMatrix>performAlgebraTransfer("smith-matrix")
    .collect(); // [ZMatrix(2x1)[[-1], [1]]]
```

This circle generator maps to the difference of the two intersection vertices. Splitting the projective plane into a triangle and the remaining subcomplex gives an intersection map with index two in the rank-one sum homology; its cokernel retains Z/2.

`excision-map` returns the actual RelativeMap inclusion (A,I)->(U,B). The relative ordered simplex bases and boundaries on both sides agree, making its relative chain matrices identities and its homology maps isomorphisms. Its ambient inclusion A->U need not have an inverse. This is the finite simplicial excision witness for this cover; it does not represent general topological excision.

The union is limited to 4096 nonempty simplices. Every required matrix dimension is limited to 256, including the combined chain ranks of A and B in sum matrices. Each compound homology/map computation, whole degree list or three-map segment shares a 5,000,000-unit integer budget. Exhaustion raises IMPLEMENTATION_FAILURE without a partial list; integer bit lengths remain unbounded. CoverMap supplies piece-preserving simplicial maps and Mayer-Vietoris/excision naturality. Many-set covers, reduced/relative Mayer-Vietoris, spectral sequences and continuous covers remain outside scope.

NativeMayerVietorisTest checks 64 ordered graph covers against independent Betti formulas and integral exactness, sphere covers through dimension four, circle orientation, projective-plane torsion and an index-two image, direct-sum identities with torsion, swap signs, chain sections, excision isomorphisms, empty and nested covers, aggregate limits, native wrappers and serialized flows. All 48 cover registrations, including the cohomology operations below, have explicit expected results in ConcreteAlgebrasTest.

## Maps of ordered covers and naturality

`math.coverMaps` registers 32 operations on CoverMap. A SimplicialCoverMap retains ordered source (A,B) and target (A',B') covers and a simplicial map f:U->U' carrying each A simplex into A' and each B simplex into B'. The ambient map must have exactly the declared unions as its boundaries. Checking vertex membership alone would miss simplices absent from a target piece, so validation checks every simplex. Unary transfers expose the union map and its left, right and intersection restrictions in the existing SimplicialMap algebra.

`CoverMap.from-map` takes the union SimplicialMap followed by `Pair<SimplicialCover,SimplicialCover>`, source first. `CoverMap.identity-on` and `CoverMap.inclusion` construct identities and componentwise inclusions. Composition applies the right operand first and requires equality of the complete ordered middle cover. Inversion requires an ambient simplicial isomorphism carrying each source piece onto its corresponding target piece; equal unions alone are insufficient. `swap` exchanges both covers' pieces simultaneously and preserves composition.

`sum-chain-matrix(k)` is block diagonal on the two restrictions, with left coordinates first. These matrices commute with the sum boundaries, the signed intersection maps (i,-j), and union addition. `sum-homology-map(k)` retains the actual source and target sum-chain presentations, also available through the two sum-homology transfers. The four individual union/intersection/left/right homology maps retain the corresponding absolute presentations and agree with the sum component inclusions/projections. Flat sum matrices and homology maps range from degree zero through the larger union dimension, preserving zero-sized shapes; two empty unions give empty lists.

`long-exact-maps(k)` emits the four vertical maps in this order:

```text
H_k(I)  -> H_k(C(A) + C(B))   -> H_k(U)  -> H_(k-1)(I)
  |               |               |             |
  v               v               v             v
H_k(I') -> H_k(C(A') + C(B')) -> H_k(U') -> H_(k-1)(I')
```

All three squares commute, including torsion and the connecting homomorphism; the fourth map at k=0 is 0->0. This implements [naturality of the Mayer-Vietoris sequence](https://pi.math.cornell.edu/~hatcher/AT/AT.pdf). The deterministic left-first chain splittings and connecting chain matrices need not commute with a cover map. For example, enlarging the left arc of a circle cover to the entire circle changes the chosen lift. Naturality holds on homology even when that chain-level equality fails.

Using the `arc` and `closingEdge` from the preceding example, reflection reverses the circle class and exchanges the two intersection vertices:

```java
SimplicialCover cover = new SimplicialCover(arc, closingEdge);
Map<BigInteger, BigInteger> vertices = new TreeMap<>();
vertices.put(BigInteger.ZERO, BigInteger.valueOf(2));
vertices.put(BigInteger.ONE, BigInteger.ONE);
vertices.put(BigInteger.valueOf(2), BigInteger.ZERO);
FiniteSimplicialMap reflection = new FiniteSimplicialMap(
    cover.union(), cover.union(), vertices);
math.flow(math.simplicialMaps, Collections.singletonList(reflection))
    .<SimplicialCoverMap, Pair<SimplicialCover, SimplicialCover>>
        performAlgebraUnsafe("CoverMap.from-map", new Pair<>(cover, cover))
    .<AbelianGroupHomomorphism, BigInteger>
        performFlatAlgebraUnsafe("long-exact-maps", BigInteger.ONE)
    .<Boolean>performAlgebraTransfer("is-isomorphism")
    .collect(); // [true, true, true, true]
```

`left-relative-map` gives (A,I)->(A',I') and `union-relative-map` gives (U,B)->(U',B'); unary flat `excision-maps` emits them in that order. They commute with the existing excision inclusions as actual RelativeMap values. Their relative chain and homology maps therefore give the corresponding commuting excision diagrams through the existing operations.

`contiguous` requires the same ordered boundaries and tests contiguity inside each target piece; ambient contiguity alone is insufficient. Piecewise contiguous maps induce equal homology maps. `image` is the cover (f(A),f(B)): its intersection can strictly contain f(I), as when disjoint source vertices in different pieces map to one common target vertex. `corestrict-image` uses that full image cover. `restrict` takes a componentwise source subcover and retains the original target, returning the first CoverMap carrier wrapper through ICustomMemberOperation.

Each union has at most 4096 nonempty simplices, and every required matrix dimension is at most 256, including combined left/right chain ranks. Each compound homology computation, whole degree list or four-map naturality list shares one 5,000,000-unit integer budget; the two excision-map constructions also share one budget. Exhaustion raises IMPLEMENTATION_FAILURE without partial lists, independently of mathematical undefinedness; coefficient bit lengths remain unbounded. Arbitrary chain-map builders, explicit chain-homotopy witnesses, many-set covers, reduced/relative Mayer-Vietoris, persistence, subdivision and continuous covers remain outside scope.

NativeSimplicialCoverMapTest checks all 27 maps of a doubled circle and 729 compositions against independent winding numbers, circle/sphere reflection signs, projective-plane torsion, all three Mayer-Vietoris squares and the excision diagram, nonnatural chain splittings, piecewise contiguity, strict growth of image intersections, cover inversion and restriction, empty and extreme-labelled complexes, shared list budgets, actual wrappers and serialized flows. All 43 cover-map registrations, including the cohomology operations below, have explicit expected results in ConcreteAlgebrasTest.

## Integral simplicial cochains and cup products

`math.cochains` registers 30 operations on SimplicialCochain and its supporting carriers. A value retains a full labelled complex X, a nonnegative BigInteger degree k, and an IntegerVector dual to the lexicographic increasing-vertex basis of C_k(X). Addition requires the same complex and degree; multiplication adds degrees. This is a family of homogeneous cochain groups, with partial addition across the carrier, rather than a mixed-degree ring value. Above the complex dimension, the cochain is zero but retains its requested degree.

`SimplicialCochain.zero-on(X,k)` supplies a context for `with-coordinates`. `SimplicialCochain.basis-on` emits the dual coordinate cochains, and `SimplicialCochain.unit-on` returns the degree-zero function equal to one on every vertex. Coordinate replacement, scaling, pullback and representative selection use the existing first-result custom-member interface. Each scalar or flat result retains the actual Algebra wrapper.

Coboundary applies the transpose of the oriented boundary d_(k+1), and raises degree by one. It squares to zero and satisfies the evaluation identity `(delta a)(z)=a(d z)`. `is-cocycle` tests its kernel; `is-coboundary` tests integral solvability against d_k transposed. `cobounding-coordinates` returns one primitive vector in degree k-1 when it exists. For k=0, only zero is a coboundary, with the empty primitive vector for C^(-1)=0.

```java
FiniteSimplicialComplex circle = new FiniteSimplicialComplex(Arrays.asList(
    FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2)));
math.flow(math.complexes, Collections.singletonList(circle))
    .<SimplicialCochain, BigInteger>performAlgebraUnsafe(
        "SimplicialCochain.zero-on", BigInteger.ZERO)
    .performCustomMemberOperation("with-coordinates",
        new IntegerVector(BigInteger.ZERO, BigInteger.ONE, BigInteger.valueOf(3)))
    .performOneOperandOperation("coboundary")
    .<IntegerVector>performAlgebraTransfer("coordinates")
    .collect(); // [[1, 3, 2]] on edges 01, 02, 12
```

`cohomology` returns the existing IntegralHomology carrier, now with outgoing differential d_(k+1) transposed and incoming differential d_k transposed. Its group is ker(delta_k)/im(delta_(k-1)); the reusable kernel/quotient implementation retains integer torsion and concrete cocycle coordinates. `class-of` requires a cocycle and returns AbelianGroupElement. `representative` requires that exact retained presentation and returns one cocycle; the section need not be additive. The element wrapper records a presentation, while the cochain provides the complex and degree for reconstruction. `cohomologous` requires two cocycles with matching context and tests whether their difference is an integral coboundary. Ordinary `equal` compares the cochains themselves.

Unary flat `cocycle-generators` emits representatives of minimal Smith generators, nontrivial torsion first and then free generators. It generates cohomology, not the full cocycle lattice. The model's existing `cycle-basis` operation supplies the latter in integer coordinates. `SimplicialCochain.cohomology-on` and flat `cohomology-degrees`, registered on FiniteComplex, construct models without first creating a cochain. The degree list runs from zero through dim(X), and is empty for an empty complex. For the projective plane, the integral cohomology types are Z, 0, Z/2; its torsion appears in degree two, whereas integral homology has Z/2 in degree one.

For degrees p and q, `cup` evaluates on an increasing simplex by multiplying the first cochain on vertices 0 through p by the second on vertices p through p+q. This [Alexander-Whitney formula](https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf) gives an associative, unital product satisfying `delta(a cup b) = delta(a) cup b + (-1)^p a cup delta(b)`. It is generally not graded commutative on cochains. `cup-class` requires both inputs to be cocycles and returns their product class; it is independent of representatives and is graded commutative on cohomology. The torus tests construct integral degree-one classes x,y with x squared and y squared zero in cohomology, and x cup y evaluating to +1 on an independently oriented fundamental cycle.

`pullback` accepts a simplicial map f:X->Y when the cochain is on exactly Y, and applies the transpose of its oriented chain matrix. It preserves degree, commutes with coboundary, reverses composition, and sends collapsed positive-degree simplices to zero. Scalar `SimplicialCochain.cohomology-map` and unary flat `cohomology-maps`, registered on SimplicialMap, return H^k(Y)->H^k(X). The flat list runs through the larger boundary dimension. For arbitrary vertex maps, cup naturality holds on cohomology; equality of the cochain products is not asserted. Reversing an edge already supplies a counterexample for nonconstant degree-zero cochains, while the torus axis swap preserves cup classes and reverses the top class.

Each complex has at most 4096 nonempty simplices, and each required vector or matrix basis has at most 256 simplices. A 5,000,000-unit integer budget covers each compound cohomology construction, projection, representative computation, generator list, cup-class or induced map. Each whole cohomology-model/map degree list shares one budget across all outputs; exhaustion raises IMPLEMENTATION_FAILURE without a partial list. Coefficient bit lengths remain unbounded. The scope is homogeneous unreduced integral cochains on finite abstract complexes. RelativeCochain supplies relative cup products and natural long exact cohomology sequences. Other coefficient rings, mixed-degree ring carriers, explicit cup homotopies, Steenrod operations, persistence and continuous maps remain future work.

NativeSimplicialCochainTest checks 729 ternary cochains against independent cup coefficients and differential identities, all 256 tetrahedron vertex maps against signed pullback formulas, all 27 circle maps and 729 contravariant compositions, and all 64 four-vertex graphs against independent connectivity counts. Further tests check the integral torus cup ring, representative independence, nonnatural cochain products, projective-plane torsion, dual evaluation, primitives, empty/high-degree/extreme-label cases, whole-list resource budgets, native wrappers and serialized flows. All 30 registrations have explicit expected results in ConcreteAlgebrasTest.

## Relative cochains and natural exact cohomology sequences

`math.relativeCochains` registers 41 operations on RelativeCochain and the existing pair/map carriers. A RelativeSimplicialCochain retains (X,A), a nonnegative degree k and integer coordinates dual to the ordered simplices of X outside A. It represents a cochain vanishing on A. The omitted simplices are removed before applying the basis-size limit, so a large diagonal pair (X,X) can still have computable zero relative cohomology. The degree remains explicit even above dim(X).

`RelativeCochain.zero-on` and flat `basis-on` construct cochains from a RelativeComplex and degree. `with-coordinates`, scaling, representative selection and pair-map pullback retain the first cochain carrier wrapper. Addition requires equality of the entire pair and degree. `absolute` converts a SimplicialCochain on X to (X,empty). `from-absolute` additionally takes a pair, checks exact ambient equality and requires the cochain to vanish on A. `extend-by-zero` inserts zero coordinates on A and returns a SimplicialCochain on X; this inclusion commutes with coboundary.

The relative differential is the transpose of the quotient-chain boundary and squares to zero. `cohomology`, `class-of`, `representative`, `cohomologous`, `cobounding-coordinates` and flat `cocycle-generators` use the existing constructive IntegralHomology and abelian quotient carriers. They retain torsion and distinguish a cocycle from its class. Primitive coordinates have degree k-1; for k=0 only zero has a primitive, the empty vector. Flat `cohomology-degrees` on RelativeComplex emits models from zero through the ambient dimension, with an empty list for empty X.

The relative cup product allows different vanishing subcomplexes:

```text
C^p(X,A) x C^q(X,B) -> C^(p+q)(X,A union B)
```

Both ambient complexes must agree. The shared Alexander-Whitney implementation treats omitted input faces as zero, so the product vanishes on the union. It is associative and obeys the signed Leibniz rule. `cup-class` requires cocycles and yields their relative product class, independently of representatives and with graded commutativity on cohomology. Absolute cochains act on either side by first converting to (X,empty); there is no general relative unit when A is nonempty. These are the [relative cup products for subcomplex pairs](https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf). A triangulated square with opposite vertical edges in A and horizontal edges in B has degree-one relative classes whose product evaluates to one on the fundamental relative two-cycle of (square,boundary).

The scalar cohomology maps and flat `RelativeCochain.long-exact-segment(k)` on RelativeComplex return, in order:

```text
H^k(X,A) --extension--> H^k(X) --restriction--> H^k(A) --connecting--> H^(k+1)(X,A)
```

Consecutive images equal kernels over Z, including nonprimitive images and torsion. The connecting map raises degree: extend a cocycle on A by zero outside A, apply coboundary, then keep the coordinates outside A. `connect-cocycle` performs this construction and returns an actual RelativeCochain; it requires the complete A and a cocycle. `extension-matrix`, `restriction-matrix` and `connecting-cochain-matrix` expose the corresponding coordinate formulas. The extension from A used for the connecting construction is a cochain-group section and need not commute with coboundary.

```java
FiniteSimplicialComplex edge = new FiniteSimplicialComplex(
    Collections.singletonList(FiniteSet.of(0, 1)));
FiniteSimplicialComplex endpoints = new FiniteSimplicialComplex(
    Arrays.asList(FiniteSet.of(0), FiniteSet.of(1)));
RelativeSimplicialComplex pair = new RelativeSimplicialComplex(edge, endpoints);
SimplicialCochain values = new SimplicialCochain(endpoints, BigInteger.ZERO,
    new IntegerVector(BigInteger.valueOf(2), BigInteger.valueOf(7)));
math.flow(math.cochains, Collections.singletonList(values))
    .<RelativeSimplicialCochain, RelativeSimplicialComplex>
        performAlgebraUnsafe("RelativeCochain.connect-cocycle", pair)
    .<IntegerVector>performAlgebraTransfer("coordinates")
    .collect(); // [[5]], the value on the oriented edge 01
```

For a RelativeMap f:(X,A)->(Y,B), `pullback` uses the transpose of its relative chain matrix and requires exactly the target pair. Scalar `RelativeCochain.cohomology-map` and unary flat `cohomology-maps`, registered on RelativeMap, return H^k(Y,B)->H^k(X,A). They reverse composition and retain the actual quotient presentations. Flat `long-exact-maps(k)` returns the four downward maps in this diagram, sharing a budget across all four:

```text
H^k(Y,B) -> H^k(Y) -> H^k(B) -> H^(k+1)(Y,B)
    |           |          |             |
    v           v          v             v
H^k(X,A) -> H^k(X) -> H^k(A) -> H^(k+1)(X,A)
```

All three squares commute. The chosen connecting cochain matrices need not commute before taking cohomology; tests include such a pair inclusion. Existing simplicial excision RelativeMaps induce relative cohomology isomorphisms in the reversed direction, even when their ambient maps are not invertible.

Each boundary complex has at most 4096 nonempty simplices and each required matrix/vector basis at most 256. Relative bases are filtered first; operations involving full ambient or subcomplex cochains, such as extension and exact sequences, also require those bases to fit. Each compound cohomology, class, cup-class or map computation shares a 5,000,000-unit integer budget, as does each whole degree list, three-map exact segment or four-map naturality list. Exhaustion raises IMPLEMENTATION_FAILURE without partial lists; integer bit lengths remain unbounded. Other coefficients, mixed-degree ring carriers, explicit cup homotopies, higher cohomology operations, persistence and continuous maps remain outside scope.

NativeRelativeCochainTest checks 1,024 graph/vertex-subcomplex pairs against independent connectivity formulas, all 27 triangle pair maps and 729 compositions against vertex-potential formulas, and 729 relative cochain products. It also checks square products and orientation reversal, disks through dimension four, projective-plane torsion and index-two images, integer exactness, all naturality squares, excision, zero extensions and primitives, filtered basis limits, whole-list resource failures, actual wrappers and serialized flows. All 41 registrations have explicit expected results in ConcreteAlgebrasTest.


## Cohomological Mayer-Vietoris in the existing cover algebras

`math.simplicialCovers` and `math.coverMaps` also support integral cohomological Mayer-Vietoris, with 19 and 11 additional operations respectively. They reuse the same ordered cover values and the existing `Mat(Z)`, `IntegralHomology` and `AbelianGroupHomomorphism` algebras. No new operation interfaces or execution engine are introduced.

For `U = A union B` and `I = A intersection B`, the cochain sequence is

```
0 -> C^k(U) --restriction--> C^k(A) direct-sum C^k(B) --difference--> C^k(I) -> 0
```

Sum coordinates place A before B. Restriction records both restrictions; difference takes left minus right. The sum differential in degree k is the transpose of the sum chain boundary in degree k+1. Its kernel modulo the preceding image is retained as an actual integral quotient presentation, so torsion is preserved. The mathematical sequence follows the dual construction in [Hatcher, section 3.1](https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf); here covers are simplex-wise unions of finite subcomplexes.

`long-exact-cohomology-segment(k)` emits three homomorphisms in order:

```
H^k(U) -> H^k(A) direct-sum H^k(B) -> H^k(I) -> H^(k+1)(U)
```

The connecting formula extends an intersection cocycle by zero in A, differentiates, and glues that result with zero on B. The matrix equals the transpose of `connecting-chain-matrix(k+1)`; its well-defined induced map raises degree and has no extra sign. Swapping A and B negates the induced map. The chosen formula on arbitrary cochains need not commute with cover maps, while the induced maps on cohomology do. For a projective plane covered by a punctured plane and a triangle, the difference map in degree one has index two; its connecting map surjects onto `H^2(U;Z) = Z/2Z`.

| Owner | Operations |
| --- | --- |
| SimplicialCover | `sum-coboundary-matrix`, flat `sum-coboundary-matrices`, `sum-cohomology`, flat `sum-cohomology-degrees` |
| SimplicialCover | `left-cohomology`, `right-cohomology`, `intersection-cohomology`, `union-cohomology` |
| SimplicialCover | `restriction-matrix`, `difference-matrix`, `connecting-cochain-matrix` |
| SimplicialCover | `restriction-cohomology-map`, `difference-cohomology-map`, `connecting-cohomology-map`, flat `long-exact-cohomology-segment` |
| SimplicialCover | `left-cohomology-inclusion-map`, `right-cohomology-inclusion-map`, `left-cohomology-projection-map`, `right-cohomology-projection-map` |
| CoverMap | `sum-cochain-matrix`, flat `sum-cochain-matrices`, `sum-cohomology-map`, flat `sum-cohomology-maps` |
| CoverMap | `source-sum-cohomology`, `target-sum-cohomology`, `union-cohomology-map`, `intersection-cohomology-map`, `left-cohomology-map`, `right-cohomology-map` |
| CoverMap | flat `long-exact-cohomology-maps` |

A cover map from `(A,B)` to `(A',B')` induces cohomology maps from target to source. `source-sum-cohomology(k)` is therefore the codomain model, and `target-sum-cohomology(k)` the domain model. `long-exact-cohomology-maps(k)` emits four target-to-source vertical maps on union degree k, sum degree k, intersection degree k, and union degree k+1. All three squares commute, and composition reverses order. Component inclusions/projections give explicit direct-sum identifications, including torsion. Existing excision relative maps induce contravariant cohomology isomorphisms through `RelativeCochain.cohomology-map`.

```java
FiniteSimplicialComplex arc = new FiniteSimplicialComplex(
    Arrays.asList(FiniteSet.of(0, 1), FiniteSet.of(1, 2)));
FiniteSimplicialComplex closingEdge = new FiniteSimplicialComplex(
    Collections.singletonList(FiniteSet.of(0, 2)));
SimplicialCover cover = new SimplicialCover(arc, closingEdge);

List<String> surjective = math.flow(math.simplicialCovers,
    Collections.singletonList(cover))
    .<AbelianGroupHomomorphism, BigInteger>performAlgebraUnsafe(
        "connecting-cohomology-map", BigInteger.ZERO)
    .<Boolean>performAlgebraTransfer("is-surjective")
    .collect(); // ["true"]

List<String> identitySquares = math.flow(math.coverMaps,
    Collections.singletonList(SimplicialCoverMap.identity(cover)))
    .<AbelianGroupHomomorphism, BigInteger>performFlatAlgebraUnsafe(
        "long-exact-cohomology-maps", BigInteger.ZERO)
    .<Boolean>performAlgebraTransfer("is-isomorphism")
    .collect(); // ["true", "true", "true", "true"]
```

All degree arguments are nonnegative. Degree lists run from zero through the union dimension (the larger union dimension for a cover map), with empty lists for empty unions. Scalar computations retain zero-sized shapes above the dimension, including arbitrarily large BigInteger degrees. Each union has at most 4096 nonempty simplices; every required matrix dimension is at most 256, including the combined left/right cochain rank. Each compound model/map calculation, whole degree list, three-map segment or four-map naturality list shares one 5,000,000-unit integer work budget. Exhaustion raises IMPLEMENTATION_FAILURE without partial outputs; integer coefficient bit lengths are unbounded. Many-set covers, reduced/relative Mayer-Vietoris, other coefficients, spectral sequences, persistent cohomology and continuous covers remain outside scope.

NativeCohomologicalMayerVietorisTest checks exactness on 64 graph covers, sphere connecting maps through dimension four, the projective plane's index-two difference and degree-two torsion, direct-sum identities, all 27 discrete maps and 729 contravariant compositions, reflection signs, all naturality squares, nonnatural cochain formulas, swapping signs, relative excision compatibility, empty shapes, negative/huge degrees, combined-rank and aggregate budgets, native wrappers and serialized flows. ConcreteAlgebrasTest supplies independent expected values for all 30 added registrations.


## Integral simplicial chains, pairing and cap products

`math.simplicialChains` supplies 30 operations on the `SimplicialChain` algebra. Each chain retains its full labelled complex, integer degree and integral coordinates in the increasing-vertex simplex basis. Addition requires both the complex and degree to agree. Negative degrees contain only zero chains, so the unreduced boundary of a zero-chain is a zero chain in degree -1. `augmentation` separately sums zero-chain coefficients; it does not turn the boundary into an augmented differential.

| Operations | Meaning |
| --- | --- |
| `SimplicialChain.zero-on`, flat `SimplicialChain.basis-on` | Construct chains from a FiniteComplex and an integer degree |
| `add`, `subtract`, `negate`, `scale`, `equal`, `is-zero` | Context-preserving integral arithmetic |
| `complex`, `degree`, `coordinates`, `with-coordinates` | Retain and access chain context |
| `boundary`, `is-cycle`, `is-boundary`, `bounding-coordinates`, `homologous` | Oriented differential, integral filling and cycle comparison |
| `homology`, `class-of`, `representative`, flat `cycle-generators` | Existing IntegralHomology and AbelianGroupElement presentations with typed cycle representatives |
| `pushforward` | Apply a simplicial map with the exact source complex; collapsed simplices vanish |
| `evaluate`, `augmentation` | Chain-cochain pairing in equal degrees; coefficient sum for zero-chains |
| `cap`, `cap-class` | Cap a chain with a cochain; pass to homology when both are closed |
| `SimplicialChain.cap-matrix`, `SimplicialChain.cap-homology-map` | Operations on a cochain and integer chain degree, fixing the cochain |
| `cap-cohomology-matrix`, `cap-cohomology-map` | Operations on a chain and nonnegative cochain degree, fixing the chain |

The cap convention is

```
[v0,...,vn] cap phi = phi([v0,...,vp]) [vp,...,vn]
boundary(c cap phi) = (-1)^p (boundary(c) cap phi - c cap coboundary(phi))
(c cap a) cap b = c cap (a cup b)
```

This is the front/back convention in [Hatcher, section 3.3](https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf). The cap result has degree n-p, including a zero chain in negative degree if p>n. A cycle and cocycle therefore give a homology class independent of both representatives. Pairing is adjoint to boundary/coboundary and to pushforward/pullback; evaluating `a cup b` on a chain agrees with evaluating b on its cap with a.

`cap-homology-map(phi,n)` requires phi to be a cocycle and returns `H_n(X;Z) -> H_(n-p)(X;Z)`. `cap-cohomology-map(c,p)` requires c to be a cycle and returns `H^p(X;Z) -> H_(n-p)(X;Z)`. Both retain the actual integer quotient presentations, including torsion. The matrix operations accept arbitrary cochains/chains and do not impose these closedness conditions. The induced map for a supplied cycle does not certify that the complex is a manifold or that the cycle is a fundamental class; multiplying a torus fundamental cycle by two, for example, gives a nonsurjective map in degree one.

For arbitrary vertex maps, the sorted-simplex cap formula need not be natural on chains. Its induced operation on homology satisfies `f_*(c cap f^*(phi)) = f_*(c) cap phi`. Full source/target complexes are checked even when the coordinate dimensions happen to agree.

```java
FiniteSimplicialComplex circle = new FiniteSimplicialComplex(Arrays.asList(
    FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2)));
SimplicialChain cycle = new SimplicialChain(circle, BigInteger.ONE,
    new IntegerVector(BigInteger.ONE, BigInteger.ONE.negate(), BigInteger.ONE));
SimplicialCochain cocycle = new SimplicialCochain(circle, BigInteger.ONE,
    new IntegerVector(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO));

List<String> period = math.flow(math.simplicialChains,
    Collections.singletonList(cycle))
    .performCustomMemberOperation("cap", cocycle)
    .<BigInteger>performAlgebraTransfer("augmentation")
    .collect(); // ["1"]

List<String> duality = math.flow(math.simplicialChains,
    Collections.singletonList(cycle))
    .<AbelianGroupHomomorphism, BigInteger>performAlgebraUnsafe(
        "cap-cohomology-map", BigInteger.ONE)
    .<Boolean>performAlgebraTransfer("is-isomorphism")
    .collect(); // ["true"]
```

`cap` is an actual `ICustomMemberOperation`: chain A times cochain B returns the first chain algebra's `IAlgebraItem` wrapper. `boundary` uses `IOneOperandOperation`, and `cycle-generators` uses `IOneOperandFlatOperation`. All other registrations likewise reuse `operations/simple` and `operations/flat` through the original Algebra and AlgebraFlow.

Each complex has at most 4096 nonempty simplices and each required vector/matrix basis at most 256 simplices. This includes adjacent degrees for homology and the source, cochain and target degrees for cap matrices. Each compound homology, class, cap-class, induced-map or generator-list calculation shares one 5,000,000-unit integer work budget, including both models of an induced map. Exhaustion raises IMPLEMENTATION_FAILURE without false predicates or partial lists. Integer bit lengths are unbounded. The scope is homogeneous unreduced integral chains; mixed-degree chain carriers, other coefficients, automatic fundamental-class construction, manifold/orientation certification and explicit cap homotopies remain outside scope.

NativeSimplicialChainTest checks independent cap coefficients and differential signs for 729 ternary cochains, cup-module identities, explicit torus intersection signs, sphere duality maps through dimension four, projective-plane torsion, invariance under changing representatives, all 27 circle maps and 729 pushforward compositions, all 256 tetrahedron maps, cap naturality on classes, zero and negative-degree conventions, invalid contexts, basis and shared-map limits, original wrappers and repeatable serialized flows. All 30 registrations have explicit independent expected values in ConcreteAlgebrasTest.


## Relative simplicial chains and the two standard relative cap products

`math.relativeChains` adds 39 native operations on `RelativeChain`. A value retains a complete labelled pair `(X,A)`, an integer degree and integral coordinates on simplices of X outside A. Quotient bases are filtered before the dimension bound. As with SimplicialChain, negative-degree groups contain only zero chains.

`RelativeChain.from-absolute` projects an absolute chain modulo A; the input can have nonzero coefficients on A. Projection commutes with boundary. `lift-absolute` inserts zeros on A and is a section of that projection, but generally fails to commute with boundary. `connect-cycle` requires a relative cycle and returns the boundary of its zero-on-A lift as a SimplicialChain on the full subcomplex A. In positive source degree its homology class equals the existing connecting homomorphism; in nonpositive degrees it is zero under the unreduced convention.

The two cap products use the existing front/back formula:

```
cap:          C_n(X,A) x C^p(X)   -> C_(n-p)(X,A)
relative-cap: C_n(X,A) x C^p(X,A) -> C_(n-p)(X)
```

For `cap`, back faces in A vanish in the quotient target. For `relative-cap`, front faces in A have zero cochain value, which makes the result independent of the choice of chain lift; back faces in A must remain in the absolute target. Both products satisfy the signed boundary identity and descend to integral homology. These are the two standard relative forms described in [Hatcher, section 3.3](https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf). Naturality under arbitrary pair maps holds on homology classes; chosen sorted chain representatives need not agree.

| Operations | Meaning |
| --- | --- |
| `RelativeChain.zero-on`, flat `RelativeChain.basis-on` | Pair and integer degree -> relative chains |
| `RelativeChain.absolute`, `RelativeChain.from-absolute` | Absolute embedding on `(X,empty)` or projection onto a supplied pair |
| `add`, `subtract`, `negate`, `scale`, `equal`, `is-zero` | Integral arithmetic retaining full pair and degree |
| `pair`, `degree`, `coordinates`, `with-coordinates` | Context and quotient coordinates |
| `boundary`, `is-cycle`, `is-boundary`, `bounding-coordinates`, `homologous` | Quotient differential, integral filling and relative cycle comparison |
| `homology`, `class-of`, `representative`, flat `cycle-generators` | Existing integral quotient presentations and typed cycle representatives |
| `pushforward`, `evaluate` | Pair-map action and pairing with a RelativeCochain on the same pair and degree |
| `lift-absolute`, `connect-cycle` | Zero-on-A absolute lift and connecting cycle on A |
| `cap`, `cap-class`, `relative-cap`, `relative-cap-class` | The two cap products and their homology classes |
| `cap-matrix`, `cap-homology-map`, `relative-cap-matrix`, `relative-cap-homology-map` | Fix a supplied cochain; use the first chain's pair and source degree, ignoring its coordinates |
| `cap-cohomology-matrix`, `cap-cohomology-map`, `relative-cap-cohomology-matrix`, `relative-cap-cohomology-map` | Fix the supplied relative chain and a nonnegative cochain degree |

For a fixed p-cocycle, the homology operations give `H_n(X,A)->H_(n-p)(X,A)` or `H_n(X,A)->H_(n-p)(X)`, respectively. For a fixed relative n-cycle, the cohomology operations give `H^p(X)->H_(n-p)(X,A)` or `H^p(X,A)->H_(n-p)(X)`. All maps preserve the actual integer presentations, including torsion. Matrix operations accept arbitrary cochains/chains; induced maps require the fixed operand to be closed. A supplied relative cycle is not automatically a certified fundamental class, and the resulting map need not be an isomorphism.

```java
RelativeSimplicialComplex pair = new RelativeSimplicialComplex(
    new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0, 1))),
    new FiniteSimplicialComplex(Arrays.asList(FiniteSet.of(0), FiniteSet.of(1))));
RelativeSimplicialChain interval = new RelativeSimplicialChain(
    pair, BigInteger.ONE, new IntegerVector(BigInteger.ONE));
RelativeSimplicialCochain cocycle = new RelativeSimplicialCochain(
    pair, BigInteger.ONE, new IntegerVector(BigInteger.valueOf(3)));

List<String> period = math.flow(math.relativeChains,
    Collections.singletonList(interval))
    .<SimplicialChain, RelativeSimplicialCochain>performAlgebraUnsafe(
        "relative-cap", cocycle)
    .<BigInteger>performAlgebraTransfer("augmentation")
    .collect(); // ["3"]

List<String> endpoints = math.flow(math.relativeChains,
    Collections.singletonList(interval))
    .<SimplicialChain>performAlgebraTransfer("connect-cycle")
    .<IntegerVector>performAlgebraTransfer("coordinates")
    .collect(); // ["[-1, 1]"]
```

`cap` returns the first RelativeChain wrapper through ICustomMemberOperation. `relative-cap` returns the actual SimplicialChain wrapper through the existing mixed-operation interface. Boundary and flat cycle generators use the original unary operation interfaces. No parallel execution engine is introduced.

Each boundary complex has at most 4096 nonempty simplices and each required basis has at most 256 simplices. Relative bases are filtered first. Absolute lifts, absolute cap outputs, absolute cochain actions and adjacent homology degrees require the corresponding full bases to fit; a small quotient does not remove these additional requirements. Each compound homology, class, cap-class, induced-map or generator-list calculation shares one 5,000,000-unit integer work budget, including both models of each induced map. Exhaustion raises IMPLEMENTATION_FAILURE without partial lists or false predicates. Integer bit lengths are unbounded. RelativeCap provides general cap products with explicit subcomplexes A and B. Automatic fundamental-class/orientation construction, mixed-degree chains, other coefficients, explicit cap homotopies and manifold/Lefschetz-duality certification remain outside scope.

NativeRelativeChainTest checks both cap maps for disks through dimension four and an annulus, 729 independent cap coefficient calculations, signed boundary and cup-module identities, quotient projection versus non-chain-map lifts, all 27 triangle pair maps and 729 pushforward compositions, connecting and cap naturality on classes, representative independence, projective-plane torsion fillings, filtered bases, negative/huge degrees, shared budgets and serialized native flows. All 39 registrations have explicit expected results in ConcreteAlgebrasTest.

## General relative cap products

`math.relativeCaps` adds 12 native operations on `RelativeCap`. A value binds an integral chain on `(X,D)` to an explicit target pair `(X,B)`, requiring the same full labelled ambient complex and `B` contained in `D`. When supplied a cochain on `(X,A)`, every cap operation checks that `D` is exactly the simplex-wise union `A union B`. Both subcomplexes can be nonempty and can overlap.

The product is

```text
C_n(X,A union B) x C^p(X,A) -> C_(n-p)(X,B).
```

The front/back formula evaluates the cochain on the front face, omitting faces in A, and retains the back face modulo B. For simplicial subcomplexes, every simplex of the union lies in A or B, so this formula is defined directly on the quotient chains. It obeys the signed boundary identity and descends to the corresponding integral homology product. See [Hatcher, section 3.3](https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf).

| Operation | Contract |
| --- | --- |
| `RelativeCap.on` | RelativeChain and target RelativeComplex -> RelativeCap; registered on the source chain algebra |
| `chain`, `target-pair` | Return the full retained chain or target pair |
| `with-chain` | Replace the chain on the same full source pair; its degree may change |
| `boundary` | Unary quotient boundary on the stored chain, retaining the target |
| `equal` | Compare the full chain and target pair |
| `cap` | Return a RelativeChain on `(X,B)` in degree `n-p` |
| `cap-class` | Require a cycle and cocycle; return their class in `H_(n-p)(X,B)` |
| `cap-matrix`, `cap-homology-map` | Fix the supplied cochain and use only the stored chain's pair and degree; the induced map requires a cocycle |
| `cap-cohomology-matrix`, `cap-cohomology-map` | Fix the stored chain; use the supplied cochain only as a pair-and-degree template, ignoring its coordinates; the induced map requires a cycle |

The two map operations retain the actual integral presentations. A cohomology template need not be a cocycle, since its coefficients are ignored. A homology-map context need not store a cycle, since its coefficients are ignored. Raw matrices impose neither cycle nor cocycle conditions. All full pair and union checks still apply.

For a square triangulated by `[0,1,2]` and `[0,2,3]`, let A be edges `[0,1]` and `[2,3]`, B be edges `[0,3]` and `[1,2]`, and `squareFundamental` have coefficients `[1,1]` relative to the boundary. The degree-one relative cocycle `verticalDifference` on `(X,A)` has coordinates `[1,1,1]` on edges `[0,2]`, `[0,3]`, `[1,2]`:

```java
List<String> result = math.flow(math.relativeChains,
    Collections.singletonList(squareFundamental))
    .<RelativeCapProduct,RelativeSimplicialComplex>performAlgebraUnsafe(
        "RelativeCap.on", new RelativeSimplicialComplex(square, verticalEdges))
    .<RelativeSimplicialChain,RelativeSimplicialCochain>performAlgebraUnsafe(
        "cap", verticalDifference)
    .<IntegerVector>performAlgebraTransfer("coordinates")
    .collect(); // ["[0, 0, 1]"] on edges 01, 02, 23 outside B
```

The executable example constructs these inputs. `RelativeCap.on` and `cap` use the existing mixed-result interface and return wrappers belonging to the registered result algebras. `with-chain` uses ICustomMemberOperation, and `boundary` uses the original unary interface. The context supplies the extra target information needed to express this product with binary operations.

All required quotient bases are filtered before the 256-simplex bound. The ambient complex has at most 4096 nonempty simplices. Each class or induced-map calculation shares one 5,000,000-unit integer work budget across validation, models and induction. Exceeding these limits raises IMPLEMENTATION_FAILURE. Integer bit lengths remain unbounded. Negative chain degrees contain only zero; cochain degrees are nonnegative. Sorted representatives need not be natural on chains under arbitrary relabelling, although the induced classes are natural. No automatic orientation, fundamental-class construction or manifold/duality certification is supplied.

NativeRelativeCapProductTest checks explicit square duality, 729 independent coefficient calculations, boundary identities for all 361 pairs of triangle subcomplexes, three-subcomplex cup compatibility, both standard special cases, representative independence, eight square symmetries, projective-plane torsion, ignored-coordinate contracts, invalid contexts, filtered basis bounds, shared work budgets, empty/huge degrees and serialized native flows. All 12 registrations have explicit expected results in ConcreteAlgebrasTest.

## Nested relative triples

`math.relativeTriples` adds 24 native operations on `RelativeTriple`, retaining a labelled inclusion `B subset A subset X`. Construct it from the outer pair `(X,A)` and base B. The three pair accessors return `outer-pair = (X,A)`, `total-pair = (X,B)` and `inner-pair = (A,B)`.

The quotient chain groups form a short exact sequence, yielding the homology sequence of a triple and its cohomological counterpart:

```text
0 -> C_k(A,B) -> C_k(X,B) -> C_k(X,A) -> 0
H_k(A,B) -> H_k(X,B) -> H_k(X,A) -> H_(k-1)(A,B)
H^k(X,A) -> H^k(X,B) -> H^k(A,B) -> H^(k+1)(X,A)
```

The chain inclusion and quotient are selectors on the filtered simplex bases. Connecting homology takes the boundary of a zero-on-A/B lift, modulo B. The connecting cochain matrix is the transpose of the connecting chain matrix one degree higher. See [Hatcher, section 2.1](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf) for the sequence of a triple and [chapter 3](https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf) for dual cochain sequences.

| Operations | Contract |
| --- | --- |
| `RelativeTriple.from-pair` | Outer RelativeComplex and base FiniteComplex -> validated triple; registered on RelativeComplex |
| `outer-pair`, `total-pair`, `inner-pair`, `equal` | Inspect full labelled pairs or compare triples |
| `inclusion-map`, `quotient-map` | Actual RelativeMap values `(A,B)->(X,B)` and `(X,B)->(X,A)` |
| `inclusion-matrix`, `quotient-matrix`, `lift-matrix` | Chain inclusion, quotient and zero-on-A/B section |
| `connecting-chain-matrix` | Degree-lowering matrix from outer to inner quotient chains |
| `inclusion-homology`, `quotient-homology`, `connecting-homology` | The three integral homology maps above |
| flat `long-exact-segment` | Those three maps in sequence order, with a shared work budget |
| `connect-cycle` | RelativeChain on the full outer pair -> connecting RelativeChain on the full inner pair |
| `extension-matrix`, `restriction-matrix`, `connecting-cochain-matrix` | Dual cochain matrices, with connecting degree raised by one |
| `extension-cohomology`, `restriction-cohomology`, `connecting-cohomology` | The three integral cohomology maps above |
| flat `long-exact-cohomology-segment` | Those three maps in sequence order, with a shared work budget |
| `connect-cocycle` | RelativeCochain on the full inner pair -> connecting RelativeCochain on the full outer pair |

The typed connecting operations require cycles or cocycles, preserve the complete output pair, and return the second operand carrier's actual `IAlgebraItem` through `ILeftProjectionOperation`. Scalar maps and flat segments retain integer quotient presentations, including torsion. Inclusion and quotient maps can also be used with the existing pushforward and pullback operations.

For the interval X with A both endpoints and B endpoint 0, the connecting cycle of the oriented edge is endpoint 1 modulo B:

```java
List<String> result = math.flow(math.relativeComplexes,
    Collections.singletonList(intervalPair))
    .<RelativeSimplicialTriple,FiniteSimplicialComplex>performAlgebraUnsafe(
        "RelativeTriple.from-pair", endpointZero)
    .performLeftProjectionOperation("connect-cycle", orientedEdge)
    .<IntegerVector>performAlgebraTransfer("coordinates")
    .collect(); // ["[1]"]
```

The executable example also runs the flat cohomology sequence. With B empty, positive-degree connecting homology agrees with the existing pair sequence. At degree zero the target is the zero group in degree -1, retaining the same presentation as a typed chain on `(A,B)`; that presentation can differ from the canonical zero presentation in the older pair API. Matrix and exact-sequence operations accept nonnegative degrees. Typed chains permit negative zero groups; connecting cochains always raise a nonnegative degree.

Every required quotient basis is filtered before its 256-simplex limit; each complex has at most 4096 nonempty simplices. A single 5,000,000-unit integer budget covers each compound map, and each entire flat segment shares it across all four quotient models and three induced maps. Exhaustion raises IMPLEMENTATION_FAILURE without a partial list; integer bit lengths remain unbounded. The zero-on-A/B section is generally not a chain map, and the connecting matrices need not be natural on raw representatives. Induced maps are natural under maps preserving A and B. TripleMap supplies maps of triples. SimplicialHomotopy supplies prism witnesses for contiguous pair maps. Arbitrary chain maps, other coefficients, subdivision and continuous maps remain outside this carrier's scope.

NativeRelativeTripleTest checks 216 graph triples against independent connectivity ranks and integral exactness, chain identities for every nested pair of triangle subcomplexes, disks through dimension four, projective-plane index-two images and cohomological torsion, both pair-sequence specializations, representative independence, all 27 triangle-boundary vertex maps and all six naturality squares, nonnatural connecting matrices, filtered bases, shared segment budgets, invalid inputs, empty/huge degrees, actual second-operand wrappers and serialized flows. All 24 registrations have explicit expected results in ConcreteAlgebrasTest.

## Maps of nested relative triples

`math.tripleMaps` adds 25 native operations on `TripleMap`. A value retains a complete source triple `(X,A,B)`, target triple `(Y,C,D)` and simplicial vertex map `f: X -> Y`. Construction checks both `f(A) subset C` and `f(B) subset D`, including simplex preservation. The support carrier `RelativeTriple.pair` holds the ordered source and target triples and validates both against the actual RelativeTriple algebra.

The three exposed RelativeMaps are `(X,A)->(Y,C)`, `(X,B)->(Y,D)` and `(A,B)->(C,D)`. They work with existing chain matrices, chain pushforward, cochain pullback and induced-map operations. Their homology maps commute with the inclusion, quotient and connecting maps of the triple exact sequence. Cohomology reverses direction and gives the corresponding three naturality squares. See [Hatcher, section 2.1, Naturality](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf).

| Operations | Contract |
| --- | --- |
| `TripleMap.from-map` | SimplicialMap and ordered `RelativeTriple.pair` -> validated TripleMap |
| `compose`, `inverse` | Apply the right operand first; require the full middle triple to agree; inverses must preserve both nested subcomplexes |
| `source`, `target`, `ambient-map` | Retained full triples and ambient map |
| `outer-map`, `total-map`, `inner-map` | The three compatible RelativeMaps above |
| `equal`, `is-isomorphism` | Compare full maps, or check ambient isomorphism and surjectivity on both nested components |
| `TripleMap.identity-on`, `TripleMap.inclusion` | Construct an identity or a componentwise labelled inclusion |
| `contiguous` | Check image unions in the target ambient, middle and base complexes separately |
| `image`, `corestrict-image`, `restrict` | Compute `(f(X),f(A),f(B))`, change the target to that image, or restrict to a componentwise source subtriple |
| `outer-homology-map`, `total-homology-map`, `inner-homology-map` | Covariant integral maps on the three pairs |
| flat `long-exact-maps` | Four source-to-target maps on inner `H_k`, total `H_k`, outer `H_k`, inner `H_(k-1)`, in that order |
| `outer-cohomology-map`, `total-cohomology-map`, `inner-cohomology-map` | Contravariant integral maps on the three pairs |
| flat `long-exact-cohomology-maps` | Four target-to-source maps on outer `H^k`, total `H^k`, inner `H^k`, outer `H^(k+1)`, in that order |

All degrees supplied to these operations are nonnegative. At degree zero the homology list's fourth map retains the two typed degree-minus-one presentations used by the triple connecting maps. Both groups are zero, but their retained relation matrices may differ. Induced maps preserve full quotient presentations generally, including killed Smith coordinates; matrix size is not necessarily the abstract group's rank.

For an interval reflection interchanging endpoints, let `basedInterval` have B endpoint 0 and `oppositeBasedInterval` have B endpoint 1. The outer relative homology map is multiplication by -1:

```java
List<String> result = math.flow(math.simplicialMaps,
    Collections.singletonList(intervalReflection))
    .<RelativeSimplicialTripleMap,Pair<RelativeSimplicialTriple,RelativeSimplicialTriple>>
        performAlgebraUnsafe("TripleMap.from-map",
            new Pair<>(basedInterval, oppositeBasedInterval))
    .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe(
        "outer-homology-map", BigInteger.ONE)
    .<IntegerMatrix>performAlgebraTransfer("smith-matrix")
    .collect(); // ["ZMatrix(1x1)[[-1]]"]
```

`restrict` returns the first TripleMap wrapper through ICustomMemberOperation. Unary transfers return the registered triple, simplicial-map or relative-map wrappers. The flat naturality lists return actual AbelianGroupHomomorphism wrappers through the original flat mixed-result interface; serialized flows preserve their contexts and direction.

Each complex has at most 4096 nonempty simplices, and each required quotient basis at most 256, filtered before its bound. A small quotient can therefore work even when the full ambient chain basis exceeds the limit. Each scalar induced map and each entire four-map list shares one 5,000,000-unit integer work budget across all models and induction. Construction, composition, restriction, image/corestriction and contiguity also share a budget within each call. Exhaustion raises IMPLEMENTATION_FAILURE without partial lists or false predicates; integer bit lengths are unbounded. Connecting matrices themselves need not be natural on raw representatives. Contiguity is sufficient for homotopy, not a complete decision procedure. SimplicialHomotopy supplies prism witnesses for the exposed contiguous pair maps. Arbitrary chain maps, general homotopy search, other coefficients, subdivision and continuous maps remain outside scope.

NativeRelativeTripleMapTest checks 27 circle maps against winding numbers, all 729 compositions, all 256 tetrahedron vertex maps against independent oriented quotient matrices, a degree-two circle covering with index two, interval reflection signs, projective-plane torsion, both exact-sequence diagrams, contiguity in each nested component, image factorization, restrictions, full triple isomorphisms, degree-minus-one presentations, nonnatural connecting matrices, invalid inputs, empty/huge degrees, extreme labels, filtered bases, shared four-map budgets and serialized native flows. All 25 registrations have explicit expected results in ConcreteAlgebrasTest.

## Explicit simplicial prism homotopies

`math.simplicialHomotopies` adds 16 operations on `SimplicialHomotopy`. It retains ordered maps `f,g: (X,A)->(Y,B)` with identical full boundaries, requiring contiguity in Y and separately in B. Absolute construction uses empty subcomplexes. Each witness computes the integral prism on sorted oriented simplices:

```text
P_n[v0,...,vn] = sum_i (-1)^i [f(v0),...,f(vi),g(vi),...,g(vn)]
boundary P + P boundary = g# - f#
Q^p = transpose(P_(p-1))
coboundary Q + Q coboundary = g* - f*
```

Repeated vertices make a term zero; distinct image vertices are sorted with the orientation sign. Terms in B are zero in the quotient. Thus P applied to a cycle explicitly fills the difference of its two pushforwards. Q applied to a positive-degree cocycle supplies a primitive for the difference of its pullbacks. See [Hatcher, section 2.1, prism operators and relative homotopy invariance](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf).

| Operations | Contract |
| --- | --- |
| `SimplicialHomotopy.between` | Two RelativeMaps -> witness; registered on RelativeMap; checks contiguity in both target components |
| `SimplicialHomotopy.between-absolute` | Two SimplicialMaps -> witness on their absolute pairs |
| `from`, `to`, `source`, `target` | Return the retained endpoint RelativeMaps or full RelativeComplex pairs |
| `reverse`, `equal` | Swap endpoints, or compare their ordered full maps; reversing recomputes the prism and need not negate its matrix |
| `chain-matrix`, `cochain-matrix` | Return P_n or Q^p in nonnegative degree |
| flat `chain-matrices`, `cochain-matrices` | Ascending degrees 0 through d for P, 0 through d+1 for Q, where d is the maximum dimension of the two quotient complexes |
| `on-chain`, `on-cochain` | Apply P to a RelativeChain on the source, or Q to a positive-degree RelativeCochain on the target; return the other pair with changed degree |
| `on-absolute-chain`, `on-absolute-cochain` | Corresponding SimplicialChain/SimplicialCochain actions; both endpoint subcomplexes must be empty |

The chain and cochain actions use `ILeftProjectionOperation` and return the actual second carrier's wrapper. Construction uses the existing custom-result operation; matrix lists use unary flat transfers. Typed chain inputs allow negative-degree zero chains. Typed cochains have no degree-minus-one values, so their action requires positive input degree; `cochain-matrix(0)` still returns the correctly shaped zero-row matrix. Empty quotient pairs give no chain matrices and one 0-by-0 cochain matrix.

For the two maps of a point to opposite interval endpoints, the prism on three times that point is three times the oriented edge:

```java
List<String> boundary = math.flow(math.simplicialMaps,
    Collections.singletonList(endpointStart))
    .<SimplicialHomotopy>performCustomResultOperation(
        "SimplicialHomotopy.between-absolute", endpointEnd)
    .performLeftProjectionOperation("on-absolute-chain", weightedPoint)
    .performOneOperandOperation("boundary")
    .<IntegerVector>performAlgebraTransfer("coordinates")
    .collect(); // ["[-3, 3]"]
```

The executable example builds all inputs. Each required quotient basis is filtered before the 256-simplex bound; each boundary complex has at most 4096 nonempty simplices. Matrix construction, each typed action and each entire flat list share a 5,000,000-unit computation budget. Resource exhaustion raises IMPLEMENTATION_FAILURE; integer coefficient bit lengths remain unbounded. These witnesses cover a single contiguous pair; HomotopyPath concatenates supplied witnesses. They do not search for homotopies, represent arbitrary chain homotopies, perform subdivision, or certify continuous homotopy equivalence. Cup/cap homotopies and other coefficients remain outside scope.

NativeSimplicialHomotopyTest checks 729 triangle-map pairs against independent vertex-incidence determinants, 324 relative triangle-map pairs against quotient projection, both homotopy identities, typed cycle/cocycle fillings on distinct pairs, noncycles and adjoint pairings, simplex contractions through dimension six, projective-plane cycles in a cone, circle maps, noncontiguity, non-negating endpoint reversal, extreme labels, negative/huge degrees, filtered basis limits, actual wrappers, flat flows and serialized repeated execution. All 16 registrations have explicit expected results in ConcreteAlgebrasTest.

## Finite contiguity paths

`math.homotopyPaths` adds 23 operations on `HomotopyPath`. A path retains stages `f_0,...,f_m: (X,A)->(Y,B)`, with each consecutive pair contiguous in both Y and B. The endpoints need not be directly contiguous. Summing the existing step prisms gives an explicit witness:

```text
D_n = P(f_0,f_1)_n + ... + P(f_(m-1),f_m)_n
boundary D + D boundary = (f_m)# - (f_0)#
Q^p = transpose(D_(p-1))
coboundary Q + Q coboundary = (f_m)* - (f_0)*
```

The identities telescope by summing the [prism identities](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf). Consequently the first and last maps induce equal integral homology and cohomology maps. Supplied cycles and positive-degree cocycles produce explicit fillings and primitives of their image differences.

| Operations | Contract |
| --- | --- |
| `HomotopyPath.from-homotopy` | SimplicialHomotopy -> one-step path retaining its two endpoints |
| `HomotopyPath.stationary-on` | RelativeMap -> zero-step path retaining one stage |
| `append` | Append a RelativeMap after the final stage; check full pairs and contiguity |
| `then` | Chronological concatenation: this path followed by the second path; full joining maps must agree, including vertex assignments |
| `reverse`, `equal` | Reverse every stage, or compare complete ordered stage lists, retaining repetitions |
| `from`, `to`, `source`, `target`, `step-count` | Inspect endpoint maps, common pairs or the number of steps |
| flat `stages`, `steps` | Emit all stage RelativeMaps or all consecutive SimplicialHomotopy witnesses in order |
| `chain-matrix`, `cochain-matrix` | Accumulated D_n or its transpose Q^p, in nonnegative degree |
| flat `chain-matrices`, `cochain-matrices` | Ascending degrees 0 through d for D, 0 through d+1 for Q, where d is the maximum quotient dimension |
| `on-chain`, `on-cochain` | Typed RelativeChain/RelativeCochain actions, with full input pair checks and changed output pair/degree |
| `on-absolute-chain`, `on-absolute-cochain` | Corresponding absolute actions when both endpoint subcomplexes are empty |
| `precompose`, `postcompose` | Compose every stage with a supplied RelativeMap on the appropriate side; check the full middle pair and recompute each resulting prism |

`then` is associative where defined, with stationary paths as units. It includes the joining stage once. This is a carrier of supplied paths: equal endpoint maps do not identify two paths. For example, a point walking around a three-edge circle gives a nonzero integral cycle even though the initial and final maps agree. Reversal recomputes the step prisms and need not negate the accumulated matrix. Precomposition also recomputes the sorted prism, which can differ from multiplying the old matrix by the source chain map; postcomposition agrees with the target chain map action.

For the two-edge interval `0--1--2`, let `pathStart`, `pathMiddle`, and `pathEnd` send a point to its three vertices. The endpoint maps are not directly contiguous:

```java
List<String> boundary = math.flow(math.relativeMaps,
    Collections.singletonList(pathStart))
    .<SimplicialHomotopyPath>performAlgebraTransfer("HomotopyPath.stationary-on")
    .performCustomMemberOperation("append", pathMiddle)
    .performCustomMemberOperation("append", pathEnd)
    .performLeftProjectionOperation("on-absolute-chain", weightedPoint)
    .performOneOperandOperation("boundary")
    .<IntegerVector>performAlgebraTransfer("coordinates")
    .collect(); // ["[-3, 0, 3]"]
```

The executable example constructs the inputs. Appending and composing use ICustomMemberOperation; typed actions return the actual second carrier wrapper through ILeftProjectionOperation. Path construction and inspection use existing unary transfers, and stage/step/matrix lists use unary flat transfers. Negative-degree zero chains are valid. Typed cochain actions require positive degree; degree-zero cochain matrices retain their zero-row shapes. Stationary paths return zero matrices with the correct source and target dimensions.

There must be 1 through 256 stages, giving at most 255 steps. Each boundary complex has at most 4096 nonempty simplices; every required quotient basis is filtered before its 256-simplex bound. One 5,000,000-unit budget covers all stage validation in a constructor, all compositions and revalidation in pre/postcomposition, each complete matrix sum or typed action, and every degree and step in an entire flat matrix list. Resource exhaustion raises IMPLEMENTATION_FAILURE without partial results; integer bit lengths remain unbounded. Stages must be supplied. Homotopy search, subdivision, arbitrary chain-homotopy carriers, other coefficients and general continuous homotopy-equivalence decisions remain outside scope.

NativeSimplicialHomotopyPathTest checks all 178 five-stage walks on a four-vertex interval against signed edge counts and potential differences, 729 triangle paths, 324 relative triangle paths, both telescoping identities, noncontiguous endpoints, nonzero circle loops, concatenation laws, immutable repeated stages, precomposition distinctions and postcomposition compatibility, relative fillings, exact zero shapes, basis and stage bounds, shared validation/composition/matrix-list budgets, actual wrappers and serialized repeated flows. All 23 registrations have independent expected results in ConcreteAlgebrasTest.

## Supplied simplicial homotopy equivalences

`math.homotopyEquivalences` adds 27 operations, including six constructive collapse operations and two absolute strong-type operations described below. The first 19 operations retain opposite maps of full labelled pairs, `f: (X,A)->(Y,B)` and `g: (Y,B)->(X,A)`, together with two supplied contiguity paths:

```text
H_X: Id_(X,A) -> g compose f
H_Y: Id_(Y,B) -> f compose g
```

Construction checks both complete endpoint maps, including vertex assignments and subcomplexes. Each path has already checked consecutive contiguity in both target components. The resulting data exhibit a homotopy equivalence of pairs in the sense of [Hatcher, chapter 0](https://pi.math.cornell.edu/~hatcher/AT/ATch0.pdf); the [integral prism identities](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf) show that both induced homology maps, and both cohomology pullbacks, are mutual inverses. These maps retain integral presentations and torsion.

| Operations | Contract |
| --- | --- |
| `HomotopyEquivalence.maps` | Two RelativeMaps -> ordered product `(f,g)` on the actual `RelativeMap.pair` carrier; boundary compatibility is checked by `from-maps` |
| `HomotopyEquivalence.from-maps` | `(f,g)` and ordered `HomotopyPath.pair` `(H_X,H_Y)` -> checked equivalence |
| `HomotopyEquivalence.identity-on` | RelativeComplex -> identity maps and stationary witnesses |
| `HomotopyEquivalence.from-isomorphism` | RelativeMap -> its strict simplicial inverse and stationary witnesses; requires an isomorphism of the full pair |
| `compose`, `inverse`, `equal` | Apply the right equivalence first; swap maps and witnesses; or compare both maps and both complete witness stage lists |
| `forward`, `backward`, `source`, `target` | Inspect the actual RelativeMaps and full RelativeComplex pairs |
| `source-homotopy`, `target-homotopy` | Inspect the supplied HomotopyPaths, retaining access to their chain/cochain matrices and typed actions |
| `forward-homology-map`, `backward-homology-map` | Integral `f_*: H_n(X,A)->H_n(Y,B)` and `g_*` in the reverse direction |
| flat `homology-maps` | Exactly `[f_*,g_*]`, sharing one computation budget |
| `forward-cohomology-map`, `backward-cohomology-map` | Integral `f^*: H^n(Y,B)->H^n(X,A)` and `g^*` in the reverse direction |
| flat `cohomology-maps` | Exactly `[f^*,g^*]`; each pullback reverses its geometric map's direction, sharing one computation budget |

For `(f,g,H_X,K_Y)` followed by `(h,k,H_Y,K_Z)`, composition produces forward map `h compose f`, backward map `g compose k`, source witness `H_X then g H_Y f`, and target witness `K_Z then h K_Y k`. Here the surrounding maps precompose/postcompose every stage. The full middle pair must agree. Inversion swaps the two maps and two paths without reversing either path. Equality retains the witnesses, so composing with the homotopy inverse need not equal the strict identity witness.

For an interval retraction onto a point, the executable example constructs a section, a path from the interval identity to section-after-retraction, and a stationary point witness. The complete construction and both induced maps execute through the existing flow:

```java
List<String> isomorphisms = math.flow(math.relativeMaps,
    Collections.singletonList(intervalRetraction))
    .<Pair<RelativeSimplicialMap,RelativeSimplicialMap>>performCustomResultOperation(
        "HomotopyEquivalence.maps", intervalSection)
    .<SimplicialHomotopyEquivalence,Pair<SimplicialHomotopyPath,SimplicialHomotopyPath>>
        performAlgebraUnsafe("HomotopyEquivalence.from-maps",
            new Pair<>(intervalSourceWitness, intervalTargetWitness))
    .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe(
        "homology-maps", BigInteger.ZERO)
    .<Boolean>performAlgebraTransfer("is-isomorphism")
    .collect(); // ["true", "true"]
```

The support product carriers validate both runtime types and membership in the actual component algebras. Map collection uses the existing custom-result interface, witness construction uses the mixed-result interface, and the two-map outputs use its flat counterpart. Transfers return the actual registered wrappers.

The general constructor checks supplied finite contiguity paths. Dominated-vertex reduction below constructs a restricted family automatically; subdivision and general homotopy-equivalence decisions remain outside scope. General supplied data need not be a strict simplicial isomorphism or a deformation retraction. Degrees of induced maps are nonnegative. Each witness allows 1 through 256 stages, and each boundary complex at most 4096 nonempty simplices. Every required quotient basis is filtered before the 256-simplex limit. One 5,000,000-unit budget covers each construction, each complete composition including transported-path revalidation, and each whole two-map list. Resource exhaustion raises IMPLEMENTATION_FAILURE without partial outputs; integer coefficient bit lengths remain unbounded.

NativeSimplicialHomotopyEquivalenceTest checks absolute and based interval contractions with one through eight edges, a circle with an attached edge and a relative rank-two group, projective-plane homology/cohomology torsion, all six circle automorphisms, composition and exact units, inverse witness ordering, invalid endpoints and full-pair mismatches, nontrivial retained loop witnesses, typed fillings, empty and extreme-labelled pairs, shared composition/two-map budgets, stage bounds, product carriers, wrappers and serialized repeated flows. All 19 registrations have independent expected results in ConcreteAlgebrasTest.

## Constructive dominated-vertex collapses

The six additional operations construct homotopy witnesses automatically. A vertex `v` is dominated by a distinct vertex `w` when every simplex containing `v` extends to a simplex after adjoining `w`. Equivalently, every maximal simplex containing `v` contains `w`. Deleting all simplices containing `v` admits a retraction sending `v` to `w` and fixing the remaining vertices; composing with inclusion is contiguous to the identity. These are elementary strong collapses, as described by [Barmak and Minian, section 2](https://arxiv.org/abs/0907.2954).

For a pair `(X,A)`, the implementation checks domination in X and, when v belongs to A, separately in A with the same w. The target is the induced pair obtained by deleting v in both components. A vertex outside A leaves A unchanged. This condition can block all reductions even when the ambient complex has dominated vertices: a filled triangle relative to its entire boundary is an example.

| Operation alias | Contract |
| --- | --- |
| `HomotopyEquivalence.dominators` | RelativeComplex and existing integer vertex v -> ascending list of all compatible dominators w |
| `HomotopyEquivalence.dominated-vertices` | RelativeComplex -> ascending list of all vertices admitting a compatible dominator |
| `HomotopyEquivalence.is-strong-core` | RelativeComplex -> whether no compatible deletion remains; this tests the pair, not its ambient complex alone |
| `HomotopyEquivalence.collapse-vertex` | RelativeComplex and `StrongCollapse.vertices` ordered pair `(v,w)` -> checked HomotopyEquivalence deleting v in favor of w |
| `HomotopyEquivalence.strong-core` | RelativeComplex -> HomotopyEquivalence from the original pair to a terminal induced subpair |
| `HomotopyEquivalence.strong-core-absolute` | FiniteComplex -> the same reduction with empty subcomplex |

An individual collapse supplies a one-step source witness and stationary target witness. Repeated reduction always selects the least removable integer label, then its least compatible dominator. It retains an initial identity stage and one source stage per deletion. All stages fix every final target vertex; the backward map is inclusion, and forward-after-backward is exactly the target identity. Thus the generated witnesses exhibit deformation retractions of pairs. The result exposes the existing `target`, `source-homotopy`, integral maps, typed fillings and inverse operations.

For the interval `0--1--2`, `twoEdgeInterval` in the executable example needs no supplied vertex maps or homotopy paths:

```java
List<String> isomorphisms = math.flow(math.complexes,
    Collections.singletonList(twoEdgeInterval))
    .<SimplicialHomotopyEquivalence>performAlgebraTransfer(
        "HomotopyEquivalence.strong-core-absolute")
    .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe(
        "homology-maps", BigInteger.ZERO)
    .<Boolean>performAlgebraTransfer("is-isomorphism")
    .collect(); // ["true", "true"]
```

Here the final complex is the vertex 2. Adding the subcomplex consisting of vertex 0 forces that basepoint to survive instead. Disconnected components retain separate surviving vertices; the empty complex remains empty. The output lists are immutable. `dominators` uses the existing ILeftProjectionFlatOperation and returns the actual Z wrappers. Unknown labels, labels outside the signed int vertex range, self-collapses and incompatible domination are undefined. The input label-pair carrier validates both entries against the registered Z Algebra.

One 5,000,000-unit budget covers the entire search or reduction, including all component scans, deletions, map construction and final path validation. The retained witness allows at most 255 deletions. Each component has at most 4096 nonempty simplices. Geometric reduction does not require homology matrices, so it can operate on complexes with more than 256 vertices; later homology computations retain their existing basis limits. Exhaustion raises IMPLEMENTATION_FAILURE without a partial core or false predicate.

These operations search only for compatible dominated vertices. They do not decide general contractibility or homotopy equivalence, search for general elementary free-face collapses, perform subdivision, or classify the output up to relabelling. Label choices are deterministic; no uniqueness assertion is made for terminal relative pairs.

NativeSimplicialStrongCollapseTest compares all 167 complexes on four labels with an independent maximal-face bitmask oracle, and all 1,024 graph/vertex-subcomplex pairs with a leaf-deletion oracle. It checks integral homology and cohomology inverse identities, fixed-target witnesses, based intervals, relative edge and disk-boundary restrictions, projective-plane torsion, empty/disconnected complexes, extreme labels, insertion-order independence, typed fillings, immutable lists, shared reduction budgets, the stage bound, larger geometric bases, actual second-result wrappers and serialized repeated flows. All six new registrations have explicit expected results in ConcreteAlgebrasTest.

## Isomorphism search and absolute strong homotopy type

Three further operations on `math.relativeMaps` search for simplicial isomorphisms of full labelled pairs. Two operations on `math.homotopyEquivalences` use this search to compare absolute strong cores and construct complete equivalence witnesses.

| Operation alias | Inputs and result |
| --- | --- |
| `RelativeMap.is-isomorphic-to` | Two RelativeComplex values -> whether a simplicial isomorphism preserves both components |
| `RelativeMap.isomorphism-to` | Two RelativeComplex values -> first isomorphism in lexicographic image order; undefined if none exists |
| flat `RelativeMap.isomorphisms-to` | Two RelativeComplex values -> immutable list of all isomorphisms in that order; empty when none exists |
| `HomotopyEquivalence.strongly-equivalent` | Two FiniteComplex values -> whether their absolute strong cores are simplicially isomorphic |
| `HomotopyEquivalence.strong-equivalence-to` | Two FiniteComplex values -> constructed HomotopyEquivalence; undefined for nonisomorphic cores |

The isomorphism search enumerates vertex bijections with source labels in increasing order and candidate target labels in increasing order. It prunes candidates using incident simplex counts by dimension in each component, and by checking the partially assigned image of every affected simplex. Every completed assignment preserves all simplices. Matching face counts in each dimension, together with bijectivity, ensures preservation by the inverse as well. Subcomplexes must map onto the entire target subcomplex. Matching skeletons, face counts or homology alone is insufficient. Empty pairs have one empty isomorphism. Output wrappers are the registered RelativeMap algebra; enumeration uses ICustomResultFlatOperation and scalar results use the existing custom-result interface.

Finite absolute complexes have the same strong homotopy type exactly when their strong cores are isomorphic ([Barmak and Minian, theorem 2.11 and corollary 2.12](https://arxiv.org/abs/0907.2954)). Write the two computed retractions as `r_X: X->X0`, `r_Y: Y->Y0`, their inclusions as `i_X,i_Y`, and the selected core isomorphism as `p: X0->Y0`. The constructed maps are:

```text
forward  = i_Y compose p compose r_X
backward = i_X compose inverse(p) compose r_Y
```

The original source paths for the two reductions are already witnesses from the respective identities to these inverse composites: each core retraction composed with its inclusion is strictly the core identity. The constructor checks these endpoint equations again. Both maps expose the existing integral homology/cohomology operations and typed chain/cochain fillings, retaining torsion and full geometric boundaries.

The executable example compares a two-edge interval with a one-edge interval without supplied maps or paths:

```java
List<String> isomorphisms = math.flow(math.complexes,
    Collections.singletonList(twoEdgeInterval))
    .<SimplicialHomotopyEquivalence>performCustomResultOperation(
        "HomotopyEquivalence.strong-equivalence-to", intervalCapPair.ambient())
    .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe(
        "homology-maps", BigInteger.ZERO)
    .<Boolean>performAlgebraTransfer("is-isomorphism")
    .collect(); // ["true", "true"]
```

The decision concerns **strong simplicial homotopy type**. A three-edge circle and a four-edge circle return false despite having the same ordinary homotopy type and integral homology. This does not decide ordinary homotopy equivalence or general contractibility. The comparison accepts absolute complexes only; no classification theorem for the terminal relative pairs is assumed. The chosen witnesses depend deterministically on labels and are not canonical up to relabelling.

Isomorphism search accepts at most 64 vertices in each ambient complex. Enumeration permits at most 1024 maps; exceeding that bound fails without returning a prefix. First-witness and predicate searches stop at the first match and do not enumerate the remaining automorphisms. One 5,000,000-unit budget covers the entire search and all output map validation. Absolute strong-type comparison shares one such budget across both reductions, core search, map composition and final endpoint validation. The original complexes may exceed 64 vertices if their computed cores satisfy the search limit; the existing 4096-simplex and 256-stage reduction bounds still apply. Limits raise IMPLEMENTATION_FAILURE, never false or an empty list standing for an incomplete search. Search can require factorial work.

NativeSimplicialIsomorphismSearchTest compares all 27,889 pairs of four-label complexes and all 21,904 comparisons among the 148 three-label pairs with independent unpruned permutation/face-bitset oracles. It checks all 4,096 four-vertex graph pairs against leaf-core/permutation decisions, integer inverse maps, relative restrictions, higher-dimensional faces, lexicographic order, empty and extreme labels, 720 automorphisms, enumeration/vertex/work limits, circle subdivision distinctions, unequal-size trees, projective-plane torsion, typed fillings, shared two-reduction budgets, actual wrappers and serialized scalar/flat flows. All five new registrations have independent expected results in ConcreteAlgebrasTest.

## Barycentric subdivision and integral comparison maps

`math.subdivisions` supplies 34 native operations on `SimplicialSubdivision`. A context retains its original pair `(X,A)`, the subdivided pair `(sd X,sd A)`, and a shared face-to-vertex dictionary. This makes subdivision executable through the existing `MathTool`, `IAlgebraItem` and `AlgebraFlow` interfaces.

A subdivision vertex represents one nonempty face of X. Faces are ordered by cardinality, then lexicographically by their sorted original integer labels, and receive indices starting at zero. A simplex in sd X is a strict inclusion chain of those faces. The subcomplex sd A uses the same ambient indices. For example, the edge `{1,2}` inside the triangle `{0,1,2}` uses subdivision vertices `{1,2,5}`; subdividing that edge alone would instead use `{0,1,2}`. Dictionary access preserves this distinction.

| Operation alias | Inputs and result |
| --- | --- |
| `SimplicialSubdivision.from-pair` | RelativeComplex -> subdivision context |
| `SimplicialSubdivision.from-complex` | FiniteComplex -> context with empty subcomplex |
| `original` | Context -> original RelativeComplex |
| `subdivided` | Context -> subdivided RelativeComplex |
| `vertex-count` | Context -> number of nonempty original ambient faces |
| `vertex-face` | Context and integer index -> original face as FiniteSet(Z) |
| flat `vertex-faces` | Context -> all original faces in dictionary-index order |
| `face-vertex` | Context and original nonempty face -> its dictionary index |
| `last-vertex-map` | Context -> RelativeMap `l_X: (sd X,sd A) -> (X,A)` |
| `map` | Context and RelativeMap f with matching full original source -> sd(f) |
| `naturality-homotopy` | Context and matching f -> SimplicialHomotopy from `l_Y compose sd(f)` to `f compose l_X` |
| `homology-map` | Context and degree n -> `l_*: H_n(sd X,sd A) -> H_n(X,A)` |
| `inverse-homology-map` | Context and degree n -> inverse of l_* |
| flat `homology-maps` | Context and degree n -> `[l_*, inverse(l_*)]` |
| `cohomology-map` | Context and degree n -> `l^*: H^n(X,A) -> H^n(sd X,sd A)` |
| `inverse-cohomology-map` | Context and degree n -> inverse of l^* |
| flat `cohomology-maps` | Context and degree n -> `[l^*, inverse(l^*)]` |
| `equal` | Two contexts -> equality of full original labelled pairs |

For a map f, each face-index vertex maps to the target dictionary index of its face image. Images may have fewer vertices; repeated target faces are allowed. The full target pair is retained, including simplices outside the image. This construction preserves identities and composition. The `map` operation uses `ILeftProjectionOperation` and returns the actual second operand's RelativeMap wrapper.

The last-vertex map sends each face to its largest original vertex. On a chain of faces, the resulting vertices lie in its largest face, so this is a simplicial map of pairs. Its realization is homotopic to the barycentric subdivision identification within each original simplex. Consequently it induces integral homology and cohomology isomorphisms, including torsion. The subdivision construction and its chain-homotopy foundation are described in [Hatcher, chapter 2](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf); [Sage's simplicial-complex reference](https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex.html) also documents barycentric subdivisions and a subdivided-circle example.

For arbitrary vertex maps, the two last-vertex composites need not be equal: an order-reversing interval or circle map can distinguish them. They are contiguous because their images over each face chain lie in the image of its largest face, including within the target subcomplex. `naturality-homotopy` exposes this witness through the existing prism algebra, whose typed chain/cochain actions give explicit fillings. The resulting induced homology and cohomology squares commute.

The executable example uses the three-edge `circle` already defined there:

```java
List<String> isomorphisms = math.flow(math.complexes,
    Collections.singletonList(circle))
    .<SimplicialSubdivision>performAlgebraTransfer(
        "SimplicialSubdivision.from-complex")
    .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe(
        "homology-maps", BigInteger.ONE)
    .<Boolean>performAlgebraTransfer("is-isomorphism")
    .collect(); // ["true", "true"]
```

Empty complexes, empty subcomplexes and repeated subdivisions are supported. Invalid dictionary indices, nonfaces and mismatched full source pairs are undefined. Degrees are nonnegative; arbitrarily large degrees above dimension give correctly shaped zero maps. Equality is deterministic under facet insertion order, but depends on the full original labels. Returned face and map lists are immutable.

Each input and output complex has at most 4096 nonempty simplices. Construction fails before emitting a simplex beyond this output bound. A 5-simplex exceeds it after subdivision, while 4096 isolated points remain constructible. Each constructor, induced map including target construction, naturality witness including map compositions, and integral map with its inverse shares one 5,000,000-unit budget. Flat map pairs also share one budget. Integral computations retain the existing 256-simplex bound on each required quotient basis, applied after removing subcomplex faces. Limits raise IMPLEMENTATION_FAILURE without partial results; integer coefficient sizes remain unbounded.

This carrier represents abstract barycentric subdivision. It does not retain geometric barycenter coordinates, construct arbitrary subdivisions or simplicial approximations, or construct general chain homotopies. Signed subdivision and its specific inverse chain-homotopy witnesses are described below; the integral inverse maps retain presented groups. Subdivision need not preserve strong simplicial homotopy type: the three-edge circle and its six-edge subdivision illustrate the distinction in [Barmak and Minian](https://arxiv.org/abs/0907.2954). No HomotopyEquivalence between those complexes is asserted.

NativeSimplicialSubdivisionTest compares all 167 four-label complexes and all 148 three-label pairs with an independent comparable-subset oracle. It checks simplex face counts through dimension four, all 256 tetrahedron vertex maps, 729 circle map compositions, last-vertex naturality, relative disks, projective-plane torsion, repeated subdivision, dictionary ordering, invalid inputs, geometric versus filtered basis bounds, shared inverse budgets, actual wrappers and serialized flat flows. ConcreteAlgebrasTest checks all 18 registrations against independent expected values.

## Signed subdivision chains and inverse homotopies

Sixteen further operations on `math.subdivisions` expose a subdivision chain map `S: C_n(X,A) -> C_n(sd X,sd A)` and its transpose on cochains. An oriented simplex maps to the sum of its complete face flags, with the sign of each vertex permutation. In the implementation, `S([v])=[{v}]` and higher simplices are constructed by coning the subdivided boundary to the vertex encoding the entire simplex. Orientation signs are retained over Z. The recursive cone identity is the same chain-level tool used for subdivision in [Hatcher, chapter 2, pages 121-122](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf).

Write `L` for the last-vertex chain map. The operators satisfy:

```text
boundary S = S boundary
L S = identity on C_*(X,A)
boundary P + P boundary = identity - S L on C_*(sd X,sd A)
```

For a subdivided simplex sigma, its vertices form a chain of original faces. Let b encode the largest original face. The implementation constructs
`P(sigma) = cone_b(sigma - S L(sigma) - P(boundary sigma))`.
The expression being coned is a cycle supported inside that face's subdivision. Recursion retains absolute sparse chains until the final quotient projection. Dropping subcomplex terms before coning can give an incorrect relative result, since the later cone vertex may lie outside the subcomplex. All input and output bases are filtered before their dimension limits are checked.

The transpose `S*` preserves cochain degree and maps from the subdivision to the original pair. The homotopy transpose `Q^p = transpose(P_(p-1))` lowers degree and satisfies `coboundary Q + Q coboundary = identity - L* S*`. Thus S induces the existing inverse homology comparison, and S* induces the existing inverse cohomology comparison. The tests check these equalities on retained integral presentations, including projective-plane torsion.

| Operation alias | Inputs and result |
| --- | --- |
| `chain-matrix` | Context and nonnegative n -> S_n, rows in the subdivision and columns in the original pair |
| `cochain-matrix` | Context and nonnegative n -> transpose(S_n) |
| flat `chain-matrices` | Context -> S_0 through S_d in ascending degree |
| flat `cochain-matrices` | Context -> transpose(S_0) through transpose(S_d) |
| `on-chain` | Context and RelativeChain on the original pair -> subdivided chain of the same degree |
| `on-cochain` | Context and RelativeCochain on the subdivided pair -> original cochain of the same degree |
| `on-absolute-chain` | Context with empty subcomplex and SimplicialChain on X -> chain on sd X |
| `on-absolute-cochain` | Context with empty subcomplex and SimplicialCochain on sd X -> cochain on X |
| `chain-homotopy-matrix` | Context and nonnegative n -> P_n on the subdivided pair, raising degree by one |
| `cochain-homotopy-matrix` | Context and nonnegative p -> Q^p on the subdivided pair, lowering degree by one |
| flat `chain-homotopy-matrices` | Context -> P_0 through P_d in ascending degree |
| flat `cochain-homotopy-matrices` | Context -> Q^0 through Q^(d+1), including endpoint zero shapes |
| `homotopy-on-chain` | Context and RelativeChain on the subdivided pair -> P applied to the chain |
| `homotopy-on-cochain` | Context and positive-degree RelativeCochain on the subdivided pair -> Q applied to the cochain |
| `homotopy-on-absolute-chain` | Context with empty subcomplex and SimplicialChain on sd X -> its P image |
| `homotopy-on-absolute-cochain` | Context with empty subcomplex and positive-degree SimplicialCochain on sd X -> its Q image |

All eight typed actions use `ILeftProjectionOperation` and return the actual second operand's `IAlgebraItem`. They check the full labelled pair or complex. For a subdivided cycle c, `homotopy-on-chain` gives a chain bounding `c - S(L(c))`. For a subdivided cocycle a, `homotopy-on-cochain` gives a cochain whose coboundary is `a - L*(S*(a))`. Actions also accept noncycles and noncocycles, where the full two-term homotopy identities apply.

The executable example subdivides the oriented circle cycle:

```java
List<String> cycles = math.flow(math.complexes,
    Collections.singletonList(circle))
    .<SimplicialSubdivision>performAlgebraTransfer(
        "SimplicialSubdivision.from-complex")
    .performLeftProjectionOperation("on-absolute-chain", orientedCircle)
    .<Boolean>performAlgebraTransfer("is-cycle")
    .collect(); // ["true"]
```

Here d is the original ambient dimension. Empty complexes give empty lists except for `cochain-homotopy-matrices`, which emits its single degree-zero 0-by-0 matrix. Q^0 has zero rows and retains the subdivided degree-zero column count. Typed cochain homotopies require positive degree because the existing cochain carriers exclude negative degrees; typed chain actions allow negative zero groups. Degrees above dimension retain the appropriate zero shapes.

Each matrix, typed action and entire flat matrix list shares one 5,000,000-unit work budget. Sparse caches are local to that computation. The existing 256-simplex limit applies to every required quotient basis, independently of the geometric subdivision limit. For example, a subdivided 4-simplex has 120 top-dimensional simplices, so its top S matrix is available, while intermediate bases can exceed 256. An operation requiring one of those larger bases fails with IMPLEMENTATION_FAILURE. No partial flat list is returned.

S is natural under arbitrary simplicial pair maps, including collapsed faces. The chosen homotopy P depends on the original vertex order and need not be natural under relabelling. These are chain and cochain witnesses, with no claim of a simplicial vertex-map inverse, strong simplicial homotopy equivalence or multiplicativity of the raw cochain operator.

NativeSimplicialSubdivisionChainsTest checks all 167 four-label complexes against an independent signed-permutation formula, all 148 three-label pairs against absolute construction followed by quotient projection, top-dimensional factorial coefficients through dimension four, and all 256 tetrahedron vertex maps for strict naturality. It checks 729 ternary circle chains and cochains, higher-dimensional relative fillings, induced integral inverses and torsion, repeated subdivision, order-dependent homotopies, empty and extreme degrees, filtered basis limits, all eight typed wrappers and serialized scalar/flat flows. ConcreteAlgebrasTest checks the 16 new registrations against explicit expected values.

## Elementary free-face collapses and integral witnesses

`math.collapses` supplies 32 native operations on `SimplicialCollapse`. A context retains the original pair `(X,A)`, a nonempty free face `tau`, its unique codimension-one maximal coface `sigma`, and the reduced pair `(Y,B)`. The construction deletes exactly those two simplices. This is the elementary collapse documented in the [Macaulay2 simplicial-complex reference](https://macaulay2.com/doc/Macaulay2-1.21/share/doc/Macaulay2/SimplicialComplexes/html/_elementary__Collapse_lp__Simplicial__Complex_cm__Ring__Element_rp.html); the preservation of homology under such collapses is discussed in [Strong's notes, proposition 2.7 and lemma 2.8](https://e.math.cornell.edu/people/Kimball_Strong/notes/evasiveness.pdf).

For relative pairs, both removed simplices must belong to A or both must lie outside A. In the first case both are removed from X and A; in the second, A is unchanged. A free face lying in A with its coface outside A is rejected. An isolated vertex cannot collapse to the empty complex. Free-face lists are ordered by cardinality and then lexicographically by the original signed integer labels. `has-free-face` checks for one eligible move; it does not decide collapsibility.

Let `epsilon` be the coefficient of tau in the oriented boundary of sigma, with vertices in increasing order. Write `boundary(sigma) = epsilon*tau + a`. The chain retraction and homotopy are

```text
R(tau) = -epsilon*a,   R(sigma) = 0,   R(other simplex) = that simplex
H(tau) = epsilon*sigma,   H(other simplex) = 0
R i# = identity on C(Y,B)
boundary H + H boundary = identity on C(X,A) - i# R
```

Here `i` is the actual simplicial inclusion `(Y,B) -> (X,A)`. All matrices use the existing ordered quotient bases. When both removed simplices belong to A, R is the identity on the quotient and H is zero. The cochain operators are `R* = transpose(R)` and `Q^p = transpose(H_(p-1))`, satisfying `coboundary Q + Q coboundary = identity - R* i*` on the original pair. These are specific integral chain and cochain witnesses retained by the collapse context.

| Native operation | Input and result |
| --- | --- |
| `SimplicialCollapse.free-faces`, `SimplicialCollapse.has-free-face` | RelativeComplex -> flat eligible FiniteSet(Z) faces, or Boolean |
| `SimplicialCollapse.from-face` | RelativeComplex and FiniteSet(Z) -> checked collapse context |
| `SimplicialCollapse.from-absolute-face` | FiniteComplex and FiniteSet(Z) -> context with empty subcomplex |
| `source`, `target` | Context -> original or reduced RelativeComplex |
| `free-face`, `coface` | Context -> retained FiniteSet(Z) |
| `inclusion` | Context -> RelativeMap from reduced to original pair |
| `equal` | Two contexts -> equality of full source pair and selected face |
| `chain-matrix`, `cochain-matrix` | Context and nonnegative degree -> R or its transpose |
| `chain-matrices`, `cochain-matrices` | Context -> flat matrices in degrees 0 through original ambient dimension d |
| `chain-homotopy-matrix`, `cochain-homotopy-matrix` | Context and nonnegative degree -> H or Q |
| `chain-homotopy-matrices`, `cochain-homotopy-matrices` | Context -> flat H in degrees 0 through d, or Q in degrees 0 through d+1 |
| `on-chain`, `on-absolute-chain` | Context and original chain -> reduced chain of the same degree |
| `on-cochain`, `on-absolute-cochain` | Context and reduced cochain -> original cochain of the same degree |
| `homotopy-on-chain`, `homotopy-on-absolute-chain` | Context and original chain -> original chain one degree higher |
| `homotopy-on-cochain`, `homotopy-on-absolute-cochain` | Context and positive-degree original cochain -> original cochain one degree lower |
| `homology-map`, `inverse-homology-map`, `homology-maps` | Context and nonnegative degree -> R_*, i_*, or flat pair [R_*, i_*] |
| `cohomology-map`, `inverse-cohomology-map`, `cohomology-maps` | Context and nonnegative degree -> R*, i*, or flat pair [R*, i*] on cohomology |

The four absolute typed actions require an empty source subcomplex. All eight typed actions check full geometric contexts and return the actual second operand's `IAlgebraItem` wrapper through `ILeftProjectionOperation`. Cochain homotopy matrices retain the degree-zero zero-row shape; typed cochain homotopies require positive degree. Typed chains support the existing negative-degree zero groups. Integral maps retain presented groups and torsion, with the cohomology direction reversed: R* goes from the reduced pair to the original pair.

For example, removing edge `{0,2}` and the filled triangle `{0,1,2}` retains all three vertices and the two other edges. Its retraction sends `[0,2]` to `[0,1]+[1,2]`:

```java
FiniteSimplicialComplex triangle = new FiniteSimplicialComplex(
        Collections.singletonList(FiniteSet.of(0, 1, 2)));
SimplicialChain edge = new SimplicialChain(triangle, BigInteger.ONE,
        new IntegerVector(BigInteger.ZERO, BigInteger.ONE, BigInteger.ZERO));
List<String> coordinates = math.flow(math.complexes, Collections.singletonList(triangle))
        .<SimplicialCollapse, FiniteSet<BigInteger>>performAlgebraUnsafe(
                "SimplicialCollapse.from-absolute-face", FiniteSet.of(BigInteger.ZERO, BigInteger.valueOf(2)))
        .performLeftProjectionOperation("on-absolute-chain", edge)
        .<IntegerVector>performAlgebraTransfer("coordinates").collect(); // ["[1, 1]"]
```

This chain retraction cannot be a vertex map fixing the reduced complex: its vertices are unchanged, but its removed edge is absent from the target. The collapse context therefore exposes inclusion and chain witnesses without claiming a `HomotopyEquivalence` value in the existing contiguity-path carrier. Compatible dominated-vertex reductions remain available separately.

Each complex has at most 4096 nonempty simplices. Free-face discovery and geometric construction do not impose a chain-basis bound. Each required quotient basis is filtered before the 256-simplex limit for matrix, typed and integral operations. One 5,000,000-unit computation budget covers each search, constructor, matrix, typed action, complete flat matrix list, integral map or complete two-map list. Exhaustion raises IMPLEMENTATION_FAILURE without a partial result or false predicate. Integer coefficient bit lengths remain unbounded. This carrier represents one collapse; CollapseSequence below supplies checked composition and deterministic greedy sequences. Expansions, general discrete Morse matchings and contractibility decisions remain outside scope.

NativeSimplicialCollapseTest checks all 167 four-label complexes against an independent strict-coface oracle, all 148 three-label relative pairs, and all 1,024 graph/vertex-subcomplex pairs. Its 16 tests also cover signed simplex facets through dimension seven, 729 ternary chain/cochain actions, both homotopy identities, projective-plane torsion, inverse integral maps, pair compatibility, empty/disconnected and extreme-label inputs, filtered basis limits, shared flat-map budgets, all eight actual wrappers and serialized scalar/flat flows. ConcreteAlgebrasTest checks all 32 registrations against explicit expected values.

## Collapse sequences and deterministic reduction

`math.collapseSequences` supplies 45 operations on `CollapseSequence`, represented by `SimplicialCollapseSequence`: 36 sequence/reduction operations below and nine exhaustive search operations in the next section. It retains a full original pair and an ordered list of compatible elementary collapses, including every intermediate pair. Empty sequences provide identity retractions. A sequence can be constructed from one collapse, extended with another collapse, or concatenated chronologically with a sequence whose full source pair equals its final pair. Equal endpoints alone do not identify sequences: different choices can produce different chain homotopies.

The greedy reducer repeatedly chooses the eligible nonempty face of smallest cardinality, breaking ties lexicographically by original integer labels. It updates the geometric pair and searches again until no compatible face remains. Pair compatibility is checked at every step, so protected boundary faces stay protected, while face/coface pairs inside the subcomplex can be cancelled in both components. The result records the actual elementary collapses, consistent with the notion of a collapse sequence in [Strong and Vogeli's topology notes, section 2.1](https://e.math.cornell.edu/people/Kimball_Strong/notes/evasiveness.pdf).

If the step retractions are `R_1, ..., R_m`, inclusions `i_1, ..., i_m` and homotopies `H_1, ..., H_m`, the composite witnesses are

```text
R = R_m ... R_2 R_1
i = i_1 i_2 ... i_m
H = H_1 + i_1 H_2 R_1 + i_1 i_2 H_3 R_2 R_1 + ...
R i = identity on the final quotient chains
boundary H + H boundary = identity on the original quotient chains - i R
```

Matrices retain sorted integral quotient bases at every stage. Prefix retractions transport each H term from the original chains; inclusions place it back into the original complex. The cochain operators are the transposes of R and degree-shifted H, satisfying `coboundary Q + Q coboundary = identity - R* i*`. Integral maps retain presented groups and torsion. R induces the map from original to final homology, while its cohomology map goes from final to original; inclusion induces their inverses.

| Native operation | Input and result |
| --- | --- |
| `CollapseSequence.identity-on` | RelativeComplex -> empty sequence on that full pair |
| `CollapseSequence.from-collapse` | SimplicialCollapse -> one-step sequence |
| `CollapseSequence.reduce`, `CollapseSequence.reduce-absolute` | RelativeComplex or FiniteComplex -> complete deterministic greedy sequence |
| `append` | Sequence and SimplicialCollapse -> extended sequence, requiring the same joining pair |
| `then` | Two sequences -> first followed by second, requiring the same joining pair |
| `equal` | Two sequences -> equality of full original pair and ordered steps |
| `source`, `target` | Sequence -> original or final RelativeComplex |
| `steps`, `stages` | Sequence -> flat ordered collapses, or all full pairs including the original |
| `step-count`, `is-terminal` | Sequence -> N count, or whether its final pair has no eligible free face |
| `inclusion` | Sequence -> actual RelativeMap from final to original pair |
| `chain-matrix`, `cochain-matrix` | Sequence and nonnegative degree -> composite R or its transpose |
| `chain-matrices`, `cochain-matrices` | Sequence -> flat matrices in degrees 0 through original ambient dimension d |
| `chain-homotopy-matrix`, `cochain-homotopy-matrix` | Sequence and nonnegative degree -> composite H or degree-shifted transpose Q |
| `chain-homotopy-matrices`, `cochain-homotopy-matrices` | Sequence -> flat H in degrees 0 through d, or Q in degrees 0 through d+1 |
| `on-chain`, `on-absolute-chain` | Sequence and original chain -> final chain of the same degree |
| `on-cochain`, `on-absolute-cochain` | Sequence and final cochain -> original cochain of the same degree |
| `homotopy-on-chain`, `homotopy-on-absolute-chain` | Sequence and original chain -> original chain one degree higher |
| `homotopy-on-cochain`, `homotopy-on-absolute-cochain` | Sequence and positive-degree original cochain -> original cochain one degree lower |
| `homology-map`, `inverse-homology-map`, `homology-maps` | Sequence and nonnegative degree -> R_*, i_*, or flat pair [R_*, i_*] |
| `cohomology-map`, `inverse-cohomology-map`, `cohomology-maps` | Sequence and nonnegative degree -> R*, i*, or flat pair [R*, i*] on cohomology |

`append` uses the existing `ICustomMemberOperation` and returns the first operand carrier. The eight typed actions use `ILeftProjectionOperation` and return the actual second operand's `IAlgebraItem` wrapper. Absolute actions require an empty original subcomplex, and all typed actions check full contexts. Typed chains retain negative-degree zero groups. Cochain homotopy matrices support degree zero with zero rows; typed cochain homotopies require positive degree.

```java
List<String> counts = math.flow(math.complexes, Collections.singletonList(triangle))
        .<SimplicialCollapseSequence>performAlgebraTransfer("CollapseSequence.reduce-absolute")
        .<BigInteger>performAlgebraTransfer("step-count").collect(); // ["3"]

List<String> faceSizes = math.flow(math.complexes, Collections.singletonList(triangle))
        .<SimplicialCollapseSequence>performAlgebraTransfer("CollapseSequence.reduce-absolute")
        .<SimplicialCollapse>performFlatAlgebraTransfer("steps")
        .<FiniteSet<BigInteger>>performAlgebraTransfer("free-face")
        .<BigInteger>performAlgebraTransfer("cardinality").collect(); // ["2", "1", "1"]
```

For the filled triangle on `{0,1,2}`, the reducer removes edge `{0,1}`, then vertex `{0}`, then vertex `{1}`, retaining vertex `{2}`. An empty sequence need not be terminal, and a terminal pair need not be a point. Greedy choices depend on labels and do not backtrack: they do not decide collapsibility or contractibility, produce a canonical core, or search through expansions. General discrete Morse matchings remain outside scope. A composite chain retraction need not come from a simplicial vertex map, so it does not assert a contiguity-path `HomotopyEquivalence`.

Sequences allow zero through 256 steps and at most 4096 nonempty simplices per component. Geometry does not require small chain bases. Every required matrix basis is filtered before its 256-simplex bound, including intermediate quotient bases. Each entire reduction shares one 5,000,000-unit budget across all searches, constructions and final validation. Each append/concatenation, composite matrix, typed action, complete flat matrix list, integral map and complete two-map list also shares one budget. Step or work exhaustion raises IMPLEMENTATION_FAILURE without partial sequences, partial lists or false mathematical conclusions. Coefficient bit lengths remain unbounded.

NativeSimplicialCollapseSequenceTest has 16 tests covering independent greedy deletion oracles for all 167 four-label complexes, 148 three-label pairs and 1,024 graph/vertex-subcomplex pairs. It checks chronological composition, identity units, retained choices, 729 ternary chain/cochain actions, signed simplex reductions through dimension four, projective-plane torsion, typed fillings, relative restrictions, empty and extreme labels, step and shared-work limits, filtered bases, actual wrappers and serialized scalar/flat/append flows. ConcreteAlgebrasTest checks all 36 registrations against explicit expected values.

## Exhaustive elementary-collapse search

Nine further operations on `math.collapseSequences` search all compatible elementary collapse choices within explicit resource bounds. They return the existing sequence carrier, so every found witness immediately supports the chain/cochain, homology and flow operations above. Greedy `reduce` remains available as a separate operation.

| Native operation | Input and result |
| --- | --- |
| `CollapseSequence.can-collapse-to` | Two RelativeComplex values -> whether a compatible sequence reaches the exact target pair |
| `CollapseSequence.collapse-to` | Two RelativeComplex values -> first sequence to that target; undefined if none exists |
| `CollapseSequence.collapses-to` | Two RelativeComplex values -> complete flat list of ordered sequences to that target |
| `CollapseSequence.absolute-can-collapse-to` | Two FiniteComplex values -> existence of a collapse to the exact target complex |
| `CollapseSequence.absolute-collapse-to` | Two FiniteComplex values -> first sequence with empty subcomplexes |
| `CollapseSequence.absolute-collapses-to` | Two FiniteComplex values -> complete flat list of sequences with empty subcomplexes |
| `CollapseSequence.is-collapsible` | FiniteComplex -> whether a sequence reaches any single vertex |
| `CollapseSequence.collapse-to-point` | FiniteComplex -> first sequence to a single vertex; undefined if none exists |
| `CollapseSequence.collapses-to-point` | FiniteComplex -> complete flat list of sequences ending at any single vertex |

For an exact target `(Y,B)` from `(X,A)`, Y must be a labelled subcomplex of X and `B = A intersection Y`. Every move removes both simplices from A or neither, so this intersection condition is necessary. Both component Euler characteristics must also agree. The search checks these obstructions, then explores eligible faces in depth-first order, with choices ordered by face cardinality and lexicographic labels. Faces of the requested target are protected from deletion. Every sequence to that target has `(number of faces of X - number of faces of Y)/2` steps.

Unsuccessful intermediate pairs are cached. Successful intermediate pairs are revisited for each distinct prefix when enumerating: two orders of independent collapses remain different witnesses. First-witness search stops at the first solution in the same order used by the complete list. Matching source and target give an empty identity sequence and a singleton enumeration. Absence gives false, an undefined scalar witness, or an empty flat list, according to the operation. A single vertex is collapsible with an empty sequence; the empty complex is not collapsible to a point in this unreduced convention.

Collapsibility does not mean contractibility or trivial positive-degree homology. The [dunce-hat example documented by SageMath](https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex_examples.html#sage.topology.simplicial_complex_examples.DunceHat) is contractible but not collapsible. Our fixture has no eligible first move, and the search returns false even though its positive-degree integral homology vanishes. Choosing a greedy sequence also differs from searching all choices; the limitations of local collapse choices are studied by [Adiprasito, Benedetti and Lutz](https://arxiv.org/abs/1404.4239).

For the filled triangle `triangle` on `{0,1,2}`, there are four ordered collapse sequences ending at vertex `{0}`, and twelve ending at any vertex:

```java
FiniteSimplicialComplex point = new FiniteSimplicialComplex(
        Collections.singletonList(FiniteSet.of(0)));
List<String> counts = math.flow(math.complexes, Collections.singletonList(triangle))
        .<SimplicialCollapseSequence>performFlatCustomResultOperation(
                "CollapseSequence.absolute-collapses-to", point)
        .<BigInteger>performAlgebraTransfer("step-count").collect(); // ["3", "3", "3", "3"]
```

To preserve edge `{0,1}`, target search instead first deletes edge `{0,2}` with the triangle, then deletes vertex `{2}` with its remaining incident edge. Its witness has two steps and retains that exact labelled edge. This also shows why an unconstrained greedy result cannot answer a specified-target query.

Every call shares one 5,000,000-unit budget across all branches, free-face discovery, collapse construction and validation of every returned sequence. Inputs allow at most 4096 nonempty faces per component, searches at most 4096 distinct intermediate pairs, each witness at most 256 steps, and enumerations at most 1024 sequences. Structural obstructions are checked before the required-step bound. Resource exhaustion raises IMPLEMENTATION_FAILURE; it never returns false, an undefined witness or a truncated list. The geometric search does not impose a matrix-basis limit, but subsequent integral operators retain their 256-simplex filtered-basis bound. Searches do not use expansions, relabellings, subdivisions or general discrete Morse matchings, and do not decide contractibility.

NativeSimplicialCollapseSearchTest has 14 tests. Independent face-bitset breadth-first traversal checks all 27,889 comparisons among four-label complexes and all 21,904 comparisons among three-label relative pairs. Independent path enumeration checks every ordered relative witness on three labels and all 480 tetrahedron collapses to a specified vertex. Further tests cover all 64 four-vertex graph decisions, target protection, 720 star-collapse orders, enumeration and work exhaustion, failed-state caching, step limits, the dunce hat, projective-plane torsion, full relative contexts, empty and extreme-label inputs, geometry versus quotient-basis limits, and serialized decision/witness/flat flows. ConcreteAlgebrasTest checks the nine new registrations against explicit expected values.

## Arbitrary integral chain maps

`math.chainMaps` installs 33 operations for `SimplicialChainMap`. A value retains the full labelled pairs `(X,A)` and `(Y,B)` and integral matrices `F_k: C_k(X,A) -> C_k(Y,B)`. Rows use the target quotient basis, columns use the source quotient basis, and each basis uses ascending vertex orientation and lexicographic simplex order. Construction checks `d_Y F_k = F_(k-1) d_X` in every positive degree, including the highest source degree. Chains are unreduced: there is no augmentation equation in degree zero.

The supporting `ChainMap.data` carrier holds a `SimplicialChainMap.Data` value with both endpoints and exactly one matrix per degree from zero through the maximum ambient dimension. Supply zero matrices with their actual rectangular shapes when either group vanishes. Two empty ambient complexes require an empty list. Data is immutable but may fail the shape or chain equations; `ChainMap.from-data` checks both. The public `SimplicialChainMap(source,target,matrices)` constructor performs the same validation.

| Operation | Native signature and behavior |
| --- | --- |
| `identity-on` | `RelativeComplex -> ChainMap`; identity on quotient chains |
| `zero` | `RelativeComplex x RelativeComplex -> ChainMap`; source first, target second |
| `from-data` | `ChainMap.data -> ChainMap`; validate all supplied matrices |
| `from-simplicial` | `RelativeMap -> ChainMap`; oriented quotient chain map |
| `from-absolute-simplicial` | `SimplicialMap -> ChainMap`; both subcomplexes empty |
| `from-collapse` | `SimplicialCollapse -> ChainMap`; elementary retraction |
| `from-collapse-sequence` | `CollapseSequence -> ChainMap`; composite retraction |
| `from-subdivision` | `SimplicialSubdivision -> ChainMap`; original to subdivision |
| `source`, `target` | `ChainMap -> RelativeComplex`; full labelled endpoints |
| `data` | `ChainMap -> ChainMap.data`; retained endpoints and matrix list |
| `equal` | `ChainMap x ChainMap -> Boolean`; equality of full data |
| `is-zero`, `is-identity` | `ChainMap -> Boolean`; coefficient tests, with equal endpoints for identity |
| `add`, `subtract` | `ChainMap x ChainMap -> ChainMap`; require parallel full endpoints |
| `negate` | `ChainMap -> ChainMap` |
| `scale` | `ChainMap x Z -> ChainMap`; actual first carrier wrapper |
| `compose` | `ChainMap x ChainMap -> ChainMap`; right operand first, exact joining pair |
| `is-isomorphism`, `inverse` | Require every matrix to be square and unimodular over Z |
| `chain-matrix`, `cochain-matrix` | `ChainMap x N -> Mat(Z)`; `F_k` and its transpose |
| `chain-matrices`, `cochain-matrices` | `ChainMap -> List<Mat(Z)>`; complete ascending degree lists |
| `on-chain`, `on-cochain` | Push source relative chains forward or pull target relative cochains back; preserve degree and use the actual second wrapper |
| `on-absolute-chain`, `on-absolute-cochain` | Corresponding absolute actions; both endpoint subcomplexes must be empty |
| `homology-map`, `cohomology-map` | `ChainMap x N -> AbelianGroupHomomorphism`; integral induced maps retaining presentations and torsion |
| `homology-maps`, `cohomology-maps` | Complete ascending degree lists through the maximum ambient dimension |

Construction aliases are prefixed `ChainMap.` when their input belongs to another carrier. Once a flow contains chain maps, use the short aliases in the table. For example, this map fixes the vertices of a triangle boundary and sends its oriented cycle to three times itself:

```java
ConcreteMathematics math = new ConcreteMathematics();
FiniteSimplicialComplex circle = new FiniteSimplicialComplex(Arrays.asList(
        FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2)));
RelativeSimplicialComplex pair = RelativeSimplicialComplex.absolute(circle);
BigInteger zero = BigInteger.ZERO, one = BigInteger.ONE;
SimplicialChainMap.Data input = new SimplicialChainMap.Data(pair, pair,
        Arrays.asList(IntegerMatrix.identity(3), new IntegerMatrix(new BigInteger[][] {
            {BigInteger.valueOf(3), zero, zero},
            {BigInteger.valueOf(-2), one, zero},
            {BigInteger.valueOf(2), zero, one}
        })));
SimplicialChainMap map = math.chainMaps.inputs.buildAlgebraItem(input)
        .<SimplicialChainMap>performAlgebraTransfer("ChainMap.from-data")
        .perform().getResult();
List<String> induced = math.flow(math.chainMaps, Collections.singletonList(map))
        .<AbelianGroupHomomorphism, BigInteger>performAlgebraUnsafe("homology-map", one)
        .<IntegerMatrix>performAlgebraTransfer("smith-matrix").collect();
// [ZMatrix(1x1)[[3]]]
```

Composition and addition induce the corresponding operations on homology. Transpose pullback reverses composition on cohomology. A strict inverse requires degreewise unimodularity; a collapse retraction can induce homology isomorphisms without having a strict chain inverse. These are arbitrary integral chain maps: they need not arise from vertex maps, preserve augmentation or cup products, extend to compatible maps of ambient and subcomplex chain complexes, or represent a homotopy equivalence. The underlying algebraic conventions follow [Hatcher, Chapter 2](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf).

Each pair component permits at most 4096 nonempty simplices, and each required quotient basis permits 256 simplices after filtering out the subcomplex. One 5000000-unit work budget covers all degree calculations in construction, conversion, composition or inversion, including final validation. Each entire homology/cohomology list shares one budget. Exhaustion raises `IMPLEMENTATION_FAILURE` without a partial result. Nonnegative matrix/map degrees above both ambient dimensions return zero groups; typed chain actions also preserve negative-degree zero groups. Integer coefficient bit lengths remain unbounded.

`NativeSimplicialChainMapTest` supplies 17 tests: independent endpoint equations for all 243 ternary interval matrix families and relative quotient variants, determinant-oracle inversion for 625 two-by-two matrices, arbitrary circle degrees and noncommuting contravariant composition, 729 typed coordinate checks and 729 simplicial compositions, relative disk and projective-plane torsion, collapse/subdivision comparisons, invalid full contexts and top differentials, immutable data, empty shapes, filtered basis bounds, aggregate computation limits and serialized native scalar/flat flows.

## Integral chain homotopies

`math.chainHomotopies` installs 32 operations for `SimplicialChainHomotopy`, including three bounded integral solving operations described below. A value retains parallel integral chain maps `F` and `G` and specific matrices `H_k: C_k(source) -> C_(k+1)(target)` satisfying `dH + Hd = G - F`. Construction checks the full labelled pairs, every shifted matrix shape, and every equation, including degree zero and the top degree. The dual operator is `Q^p = transpose(H_(p-1))`, with `dQ + Qd = G* - F*`.

The supporting `ChainHomotopy.data` carrier stores two validated chain maps and an immutable supplied matrix list. Membership in this data carrier does not assert parallel endpoints or the homotopy equations. `ChainHomotopy.from-data` performs those checks. As with ChainMap, the list has one entry in every degree from zero through the largest ambient dimension. This uses ambient dimensions even when a relative quotient vanishes in every degree. Matrix equality retains the witness: distinct homotopies between equal endpoints remain distinct.

| Operation | Native behavior |
| --- | --- |
| `from-data` | `ChainHomotopy.data -> ChainHomotopy`; check all shapes and equations |
| `stationary` | `ChainMap -> ChainHomotopy`; zero witness from a map to itself |
| `from-prism` | `SimplicialHomotopy -> ChainHomotopy`; retain the actual prism |
| `from-path` | `HomotopyPath -> ChainHomotopy`; retain the accumulated prism, including loops |
| `from-collapse`, `from-collapse-sequence` | Convert the witness from inclusion after retraction to identity on the original pair |
| `from-subdivision` | Convert the witness from subdivision after last-vertex to identity on the subdivided pair |
| `from`, `to` | `ChainHomotopy -> ChainMap`; full endpoint maps |
| `source`, `target` | `ChainHomotopy -> RelativeComplex`; common full pairs |
| `data` | `ChainHomotopy -> ChainHomotopy.data`; retain endpoints and matrices |
| `reverse` | Exchange endpoints and negate the actual matrices |
| `then` | Chronological concatenation; require the exact joining chain map and add witnesses |
| `add` | Add initial maps, terminal maps and witness matrices; require parallel full pairs |
| `scale` | `ChainHomotopy x Z -> ChainHomotopy`; scale both endpoints and the witness |
| `precompose` | `ChainHomotopy x ChainMap -> ChainHomotopy`; compose endpoints with `B` and use `H_k B_k` |
| `postcompose` | `ChainHomotopy x ChainMap -> ChainHomotopy`; compose endpoints with `A` and use `A_(k+1) H_k` |
| `equal` | Compare both endpoint maps and every witness matrix |
| `chain-matrix`, `cochain-matrix` | `ChainHomotopy x N -> Mat(Z)`; `H_k` and shifted transpose `Q^k` |
| `chain-matrices`, `cochain-matrices` | Complete lists `H_0,...,H_d` and `Q^0,...,Q^(d+1)` |
| `on-chain`, `on-absolute-chain` | Raise source-chain degree by one, using the actual second carrier wrapper |
| `on-cochain`, `on-absolute-cochain` | Lower positive target-cochain degree by one, using the actual second carrier wrapper |
| `homology-maps`, `cohomology-maps` | Emit exactly the two equal endpoint maps in the requested nonnegative degree, retaining presentations and torsion |

Construction aliases use the `ChainHomotopy.` prefix on other carriers. This example constructs a nonzero homotopy from a point-to-circle map to itself. Its action sends the point generator to the oriented circle cycle:

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex point = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0))));
RelativeSimplicialComplex circle = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Arrays.asList(
                FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2))));
BigInteger zero = BigInteger.ZERO, one = BigInteger.ONE;
SimplicialChainMap f = new SimplicialChainMap(point, circle, Arrays.asList(
        new IntegerMatrix(new BigInteger[][] {{one}, {zero}, {zero}}),
        IntegerMatrix.zero(3, 0)));
SimplicialChainHomotopy.Data data = new SimplicialChainHomotopy.Data(f, f,
        Arrays.asList(new IntegerMatrix(new BigInteger[][] {{one}, {one.negate()}, {one}}),
                IntegerMatrix.zero(0, 0)));
SimplicialChainHomotopy h = math.chainHomotopies.inputs.buildAlgebraItem(data)
        .<SimplicialChainHomotopy>performAlgebraTransfer("ChainHomotopy.from-data")
        .perform().getResult();
RelativeSimplicialChain vertex = new RelativeSimplicialChain(point, zero, new IntegerVector(one));
List<String> cycle = math.flow(math.chainHomotopies, Collections.singletonList(h))
        .performLeftProjectionOperation("on-chain", vertex)
        .<IntegerVector>performAlgebraTransfer("coordinates").collect();
// [[1, -1, 1]]
```

For a cycle `c`, `H(c)` fills `G(c)-F(c)`; for a cocycle `a`, `Q(a)` fills `G*(a)-F*(a)`. On arbitrary chains and cochains, both terms in the homotopy identity are retained. Algebraic reversal negates the chosen witness and is an involution; the existing geometric prism reversal recomputes a prism and can give different matrices. Addition also differs from concatenation: it adds both endpoint maps. These conventions use the standard [chain-homotopy identity](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf).

`Q^0` has zero rows and retains the number of target vertices outside the subcomplex as its column count. Typed cochain actions require positive degree because their carrier excludes negative degrees. Typed chain actions accept negative degrees; degree -1 maps to the correctly sized zero chain in target degree zero. Both empty complexes give an empty chain-matrix list and one 0 by 0 cochain matrix. Absolute actions require empty subcomplexes at both endpoints.

One 5000000-unit budget covers each whole conversion or compound operation, including endpoint construction and final witness validation. Each two-map homology/cohomology list shares its budget. Every required quotient basis is filtered before its 256-simplex bound, within the existing 4096-simplex pair-component bound. Resource exhaustion is `IMPLEMENTATION_FAILURE`, never a partial result. Geometric homotopy search, geometric realization, higher coherence and other coefficient rings remain outside scope; the following operations solve bounded integral chain-homotopy systems.

`NativeSimplicialChainHomotopyTest` has 15 tests. They check all 2,601 ternary interval candidates against independent endpoint equations, 729 two-degree triangle witnesses using explicit oriented boundary matrices, relative quotient witnesses, nonzero loops, shifted pre/postcomposition, 729 prism conversions, 729 typed chain/cochain identities, path/collapse/subdivision conversions, projective-plane torsion, invalid full contexts and top-degree equations, empty and negative-degree shapes, filtered bases, aggregate endpoint/validation/two-map budgets, actual wrappers and serialized native flows.

## Solving integral chain homotopies

Three further operations take two `ChainMap` operands with identical full labelled source and target pairs. They assemble every equation `dH + Hd = G - F` into one integer linear system and use a Smith decomposition. All degree matrices are solved together, including degree zero, the top degree and relative quotient groups. Equal induced homology maps alone are insufficient: a map from the integral chains of the projective plane to the relative chains of a disk can have zero induced homology maps and still have a nonzero integral homotopy obstruction.

| Operation | Native result | Completed solve with no integral solution |
| --- | --- | --- |
| `ChainHomotopy.are-homotopic` | `Boolean` through `ICustomResultOperation` | `false` |
| `ChainHomotopy.between` | A validated `ChainHomotopy` through `ICustomResultOperation` | `OPERATION_UNDEFINED` |
| `ChainHomotopy.solution-generators` | Flat `ChainHomotopy` list through `ICustomResultFlatOperation` | Empty list |

The flat result is `[H, L_1, ..., L_r]`: one particular witness `H: F -> G`, followed by a full integral kernel basis whose members have zero chain maps at both endpoints. Every homotopy is uniquely `H + n_1 L_1 + ... + n_r L_r` for integers `n_i`. Existing `add` and `scale` operations construct these witnesses while retaining `F` and `G`. The list parametrizes an affine lattice; it does not enumerate infinitely many homotopies. A unique solution gives a singleton list, and equal endpoints may still have nonzero homogeneous generators. The particular solution sets free Smith coordinates to zero, making it deterministic for the retained bases without claiming a shortest or geometric witness.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex point = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0))));
RelativeSimplicialComplex circle = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Arrays.asList(
                FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2))));
BigInteger zero = BigInteger.ZERO, one = BigInteger.ONE;
SimplicialChainMap f = new SimplicialChainMap(point, circle, Arrays.asList(
        new IntegerMatrix(new BigInteger[][] {{one}, {zero}, {zero}}),
        IntegerMatrix.zero(3, 0)));
SimplicialChainMap g = new SimplicialChainMap(point, circle, Arrays.asList(
        new IntegerMatrix(new BigInteger[][] {{zero}, {zero}, {one}}),
        IntegerMatrix.zero(3, 0)));
RelativeSimplicialChain vertex = new RelativeSimplicialChain(point, zero, new IntegerVector(one));
List<String> boundary = math.flow(math.chainMaps, Collections.singletonList(f))
        .<SimplicialChainHomotopy>performCustomResultOperation("ChainHomotopy.between", g)
        .performLeftProjectionOperation("on-chain", vertex)
        .performOneOperandOperation("boundary")
        .<IntegerVector>performAlgebraTransfer("coordinates").collect();
// [[-1, 0, 1]]: vertex two minus vertex zero.
List<String> generators = math.flow(math.chainMaps, Collections.singletonList(f))
        .<SimplicialChainHomotopy>performFlatCustomResultOperation("ChainHomotopy.solution-generators", g)
        .collect();
// Two entries: a path from vertex zero to vertex two, and a primitive oriented circle loop.
```

The solver permits at most **256 total equations** `sum_k rank(target C_k) * rank(source C_k)` and **256 total unknown coefficients** `sum_k rank(target C_(k+1)) * rank(source C_k)`. It counts zero equations and equal endpoints; these aggregate limits are stronger than the carrier's per-basis limits. The largest successful generator list has 257 entries. One **5000000-unit budget** covers system construction, Smith reduction, decoding and validation of every returned witness. Producing a full generator list can exceed the budget even when constructing one witness succeeds. Shape or work exhaustion raises `IMPLEMENTATION_FAILURE`, never mathematical absence or a truncated list. Mismatched full pairs raise `OPERATION_UNDEFINED`. Integer coefficient bit lengths remain unbounded.

`NativeSimplicialChainHomotopySolverTest` adds 14 tests: independent augmentation and endpoint formulas for all 289 interval map comparisons; all 25 relative interval contexts; independent winding-number decisions for 729 circle map comparisons; complete integral affine path families; projective-plane parity obstructions including 1025-bit coefficients and equal homology maps; coupled two-degree equations; geometric endpoints; empty shapes; full-context rejection; exact equation and unknown bounds; all 257 maximal-kernel generators; shared solve/whole-list work limits; actual wrappers and serialized scalar/flat flows.

## Chain maps modulo integral homotopy

`math.chainMapSpaces` adds 18 native operations for the full additive space of integral chain maps between two labelled simplicial pairs, including composition actions described below. `SimplicialChainMapSpace` retains both pairs; its algebraic queries compute the free group of chain maps and its quotient by null-homotopic maps. These are additive groups of chain-map classes, not the geometric homotopy groups of a space.

The computation uses degree zero of the integral Hom complex. Its outgoing differential is `delta_0(F) = d_target F - F d_source`, and its incoming differential is `delta_1(H) = d_target H + H d_source`. Thus cycles are chain maps, boundaries are null-homotopic maps, and the quotient is `H_0(Hom)`. The implementation builds both matrices and passes them to the existing constructive `IntegralHomology`, which checks their composite, computes the full integral kernel and retains the resulting abelian presentation. This is the standard [Hom-complex construction](https://stacks.math.columbia.edu/tag/0A8H), in [homological grading](https://math.mit.edu/~larsh/teaching/S2006/l9/).

| Operation | Native behavior |
| --- | --- |
| `ChainMapSpace.from-pairs` | `RelativeComplex x RelativeComplex -> ChainMapSpace`; first pair is source, second is target |
| `source`, `target` | Return the full labelled endpoint pairs |
| `equal` | Compare both full pairs |
| `zero` | Return the zero `ChainMap` with all retained degree shapes |
| `homology` | Return constructive `IntegralHomology` for degree zero of Hom |
| `homotopy-group` | Return the retained `PresentedAbelianGroup` of chain-map classes |
| `homotopy-type` | Return its free rank and torsion invariant factors |
| `map-generators` | Flat `ChainMap` list giving a full integral basis before taking homotopy classes |
| `class-of` | `ChainMapSpace x ChainMap -> AbelianGroupElement`; require exact endpoint pairs |
| `representative` | `ChainMapSpace x AbelianGroupElement -> ChainMap`; require the retained presentation |
| `representatives` | Flat `ChainMap` list lifting the nonzero minimal Smith generators of the quotient |

Map entries are flattened in ascending source degree, target row, then source column. `homology` exposes those coordinates and both signed differentials, so existing integer-matrix and integral-homology operations remain usable. `map-generators` and `representatives` have different meanings: interval endomaps have three independent integer parameters, but their homotopy classes form one copy of Z. Circle endomaps have ten map generators, while their classes form Z squared, recording independent actions on H0 and H1.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex circle = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Arrays.asList(
                FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2))));
SimplicialChainMapSpace space = math.relativeComplexes.algebra().buildAlgebraItem(circle)
        .<SimplicialChainMapSpace>performCustomResultOperation("ChainMapSpace.from-pairs", circle)
        .perform().getResult();
List<String> type = math.flow(math.chainMapSpaces, Collections.singletonList(space))
        .<AbelianGroupType>performAlgebraTransfer("homotopy-type").collect();
// [AbelianGroup(rank=2, torsion=[])]
List<String> representatives = math.flow(math.chainMapSpaces, Collections.singletonList(space))
        .<SimplicialChainMap>performFlatAlgebraTransfer("representatives").collect();
// Two maps: one representative for each independent class generator.
SimplicialChainMap identity = SimplicialChainMap.identity(circle);
AbelianGroupElement identityClass = math.chainMapSpaces.algebra().buildAlgebraItem(space)
        .<AbelianGroupElement, SimplicialChainMap>performUnsafeOperation("class-of", identity)
        .perform().getResult();
SimplicialChainMap chosen = math.chainMapSpaces.algebra().buildAlgebraItem(space)
        .<SimplicialChainMap, AbelianGroupElement>performUnsafeOperation("representative", identityClass)
        .perform().getResult();
// chosen is integrally chain-homotopic to identity; equality of their matrices is not required.
```

Class projection is additive, and two maps have equal classes exactly when they are integrally chain-homotopic. In particular, the maps from projective-plane chains to the relative chains of a triangle modulo its boundary have a Z/2 homotopy quotient, although all their induced homology maps are zero. A chosen representative is a section on sets and need not preserve addition. The flat quotient representatives put torsion generators first and free generators next, omitting killed Smith coordinates. They generate all classes without enumerating infinitely many maps.

`representative` checks the exact presentation and Smith coordinate map. Generic `AbelianGroupElement` values retain that algebraic context, without geometric endpoint provenance; the same retained presentation may be used across different spaces. Returned chain maps always use the receiving space's full endpoints. Labelled pairs determine deterministic bases, but neither the presentation nor the chosen representatives are asserted to be invariant under relabelling.

Each component of a pair has the existing 4096-simplex bound. Algebraic queries filter out subcomplex simplices before applying the 256-element quotient-basis bound. They also cap each of the three Hom ranks `sum_k rank(target C_(k+j)) * rank(source C_k)` at **256**, separately for `j = -1, 0, 1`. One **5000000-unit budget** covers matrix construction, all kernel and quotient reductions, projection or lifting, and validation of every map in a complete flat list. Exhaustion raises `IMPLEMENTATION_FAILURE`, never an empty basis, a trivial group, or a truncated list. Context creation and endpoint access do not run reductions; `zero` uses only the existing chain-map limits. Integer coefficients remain unbounded. Higher Hom homology, direct binary composition on generic class elements, geometric-map classification and other coefficient rings remain outside this scope. Fixed chain maps act on classes through the operations below.

`NativeSimplicialChainMapSpaceTest` adds 13 tests. Independent checks cover all 625 small two-point matrices, integral basis saturation and 289 interval class comparisons, all 25 relative interval contexts, 81 independent circle actions, projective-plane parity including large coefficients, mixed free/torsion groups and nonadditive representatives, explicit signed Hom matrices, exact endpoints and presentations, empty and filtered large diagonal pairs, all three aggregate bounds, shared reduction/flat-list limits, actual wrappers and serialized native flows.

## Composition actions on chain-map classes

For a space `Hom(S,T)`, precomposition by `B:R -> S` sends `F` to `F B` and changes the space to `Hom(R,T)`. Postcomposition by `A:T -> U` sends `F` to `A F` and changes the space to `Hom(S,U)`. Each action induces a homomorphism of the retained integral homotopy-class groups. It depends only on the homotopy class of the fixed map, including torsion classes, and commutes with the action on the other side. This is [composition in the Hom complex](https://stacks.math.columbia.edu/tag/0A8H).

| Operation | Native result |
| --- | --- |
| `precompose-space`, `postcompose-space` | Changed `ChainMapSpace` through `ICustomMemberOperation` |
| `precompose-map`, `postcompose-map` | `AbelianGroupHomomorphism` on classes through `IUnsafeOperation` |
| `precompose-matrices`, `postcompose-matrices` | Exactly three `Mat(Z)` values through `IUnsafeFlatOperation` |

All six operations require the exact full labelled middle pair and reject incompatible inputs with `OPERATION_UNDEFINED`. The flat lists are ordered by Hom degree **[-1, 0, 1]**. In degree `j`, precomposition uses `T_k B_k` and postcomposition uses `A_(k+j) T_k`; the shifted target degree matters. Matrix rows follow the changed space and columns the original space, each flattened by source degree, target row and source column. Rectangular empty blocks are retained. Both differential squares commute. Homotopic fixed maps may produce different raw action matrices while inducing equal homomorphisms on classes.

Precomposition reverses composition order in the source argument: applying `B` and then `C` gives the action of `B C`. Postcomposition by `A` and then `D` gives the action of `D A`. Both preserve identity and addition. Their images of a class are the classes of the composed chain maps, independently of the representative chosen.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex circle = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Arrays.asList(
                FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2))));
SimplicialChainMapSpace space = new SimplicialChainMapSpace(circle, circle);
BigInteger zero = BigInteger.ZERO, one = BigInteger.ONE;
SimplicialChainMap degreeThree = new SimplicialChainMap(circle, circle, Arrays.asList(
        IntegerMatrix.identity(3), new IntegerMatrix(new BigInteger[][] {
                {BigInteger.valueOf(3), zero, zero},
                {BigInteger.valueOf(-2), one, zero},
                {BigInteger.valueOf(2), zero, one}})));
AbelianGroupElement identityClass = space.classOf(SimplicialChainMap.identity(circle));
List<String> agrees = math.flow(math.chainMapSpaces, Collections.singletonList(space))
        .<AbelianGroupHomomorphism, SimplicialChainMap>performAlgebraUnsafe("postcompose-map", degreeThree)
        .performLeftProjectionOperation("apply", identityClass)
        .<Boolean>performCustomResultOperation("equal", space.classOf(degreeThree)).collect();
// [true]: composing the identity class with degreeThree gives its class.
List<String> matrices = math.flow(math.chainMapSpaces, Collections.singletonList(space))
        .<IntegerMatrix, SimplicialChainMap>performFlatAlgebraUnsafe("postcompose-matrices", degreeThree)
        .collect();
// Three action matrices, in Hom degrees -1, 0 and 1.
```

Context changes check endpoints without running reductions. Matrix lists check all three Hom-rank bounds on both spaces and share one budget, without computing quotient groups. Induced homomorphisms use one **5000000-unit budget** for both complete Hom reductions, the action matrix, cycle-coordinate solving and relation validation. Two successful standalone group calculations do not imply that the combined action fits. Exhaustion remains `IMPLEMENTATION_FAILURE`, never a zero action or partial list.

`NativeSimplicialChainMapCompositionTest` adds 13 tests: independent coordinate sums for 6,561 ternary point-map pairs, rectangular row-major matrices, noncommuting variance and commuting left/right actions, circle winding multiplication and signed differential squares, homotopy invariance, a nonzero Z-to-Z/2 action invisible on homology, torsion scaling with large coefficients, changing ambient dimensions, empty shapes, exact middle pairs, aggregate bounds, shared two-reduction budgets, actual wrappers and serialized native scalar/flat flows.

## Contextual chain-homotopy classes

`math.chainMapClasses` installs 26 native operations on `SimplicialChainMapClass`. A value retains its full labelled source and target pairs, its canonical element of `H_0(Hom)`, and a validated deterministic representative chain map. This makes direct composition of classes available through the existing closed operation interface. The definition and well-definedness of composition follow the [chain-complex conventions](https://stacks.math.columbia.edu/tag/010V) and [Hom-complex construction](https://stacks.math.columbia.edu/tag/0A8H).

| Operations | Behavior |
| --- | --- |
| `ChainMapClass.from-map` | `ChainMap -> ChainMapClass`; project and retain endpoints |
| `ChainMapClass.from-element` | `ChainMapSpace x AbelianGroupElement -> ChainMapClass`; require the exact Hom presentation and Smith coordinate map |
| `ChainMapClass.zero-in`, `ChainMapClass.identity-on` | Construct zero on a map space or the identity class on a full pair |
| `source`, `target`, `space`, `element`, `representative` | Return retained components through their actual registered carrier wrappers |
| `equal`, `is-zero`, `is-identity` | Compare full contexts and integral classes, including torsion |
| `add`, `subtract`, `negate`, `scale` | Additive arithmetic; binary addition/subtraction require identical full endpoint pairs |
| `compose` | `[A] compose [B] = [A B]`, applying the right operand first; require the complete joining pair |
| `has-finite-order`, `order` | Finite **additive** order; zero has order one, and infinite order makes `order` undefined |
| `homology-map`, `cohomology-map` | Degreewise covariant homology and contravariant cohomology homomorphisms for nonnegative degrees |
| `homology-maps`, `cohomology-maps` | Flat lists from degree zero through the largest endpoint ambient dimension, retaining zero groups and maps |
| `ChainMapClass.generators-in` | Flat minimal Smith generators as contextual classes, torsion first and then free; empty for a zero quotient |
| `is-isomorphism`, `inverse` | Decide and construct an inverse in the chain-homotopy category; see bounded solving below |

Construction from a generic element explicitly attaches the supplied context; it cannot recover that element's original geometric provenance. Once attached, class equality and arithmetic retain both complete pairs. Abstractly isomorphic groups, matching matrix sizes, or identical induced homology maps do not identify contextual classes.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex point = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0))));
SimplicialChainMap twice = SimplicialChainMap.identity(point).scale(BigInteger.valueOf(2));
SimplicialChainMapClass three = SimplicialChainMapClass.fromMap(
        SimplicialChainMap.identity(point).scale(BigInteger.valueOf(3)));
List<String> product = math.flow(math.chainMaps, Collections.singletonList(twice))
        .<SimplicialChainMapClass>performAlgebraTransfer("ChainMapClass.from-map")
        .performOperation("compose", three)
        .<AbelianGroupHomomorphism, BigInteger>performAlgebraUnsafe("homology-map", BigInteger.ZERO)
        .<IntegerMatrix>performAlgebraTransfer("smith-matrix").collect();
// [ZMatrix(1x1)[[6]]]
SimplicialChainMapSpace space = new SimplicialChainMapSpace(point, point);
List<String> generators = math.flow(math.chainMapSpaces, Collections.singletonList(space))
        .<SimplicialChainMapClass>performFlatAlgebraTransfer("ChainMapClass.generators-in")
        .<Boolean>performAlgebraTransfer("is-identity").collect();
// [true]
```

Composition is associative, unital and additive in both arguments. A representative is a deterministic **set-theoretic section**: it need not preserve addition, composition or identity matrices. For example, the identity class of an interval may have a constant-map representative. In a relative acyclic complex the identity class equals zero. Induced homology and cohomology maps are independent of the representative; arbitrary raw chain or cochain actions are not supplied as class operations.

Hom reductions retain the existing bound of **256 total coefficients in each Hom degree -1, 0 and 1**, after quotient-basis filtering. One **5000000-unit budget** covers a whole construction or arithmetic/composition operation, including representative multiplication, matrix validation, Hom reduction, projection and lifting. Flat generator lists use one reduction and one Smith lift matrix and validate every output under the shared budget. Degreewise integral-map lists likewise share one budget. Shape/work exhaustion is `IMPLEMENTATION_FAILURE`, never a false equality, mathematical nonexistence or partial list. Integer coefficient bit lengths remain unbounded. Bounded inverse solving is described below; general geometric homotopy classification remains outside scope.

`NativeSimplicialChainMapClassTest` covers 6,561 independent point-matrix compositions, noncommuting composition and contravariance, associative and distributive laws, 125 interval maps classified independently by augmentation, circle degree actions, torsion invisible on homology, mixed free/torsion generators, nonadditive lifts, exact full-pair and presentation checks, changing ambient dimensions, empty shapes, aggregate and shared-list limits, actual native wrappers, and serialized repeatable scalar/flat flows.

## Constructive integral homotopy inverses

Five operations now decide and construct inverses up to integral chain homotopy. For a supplied `F:S -> T`, the solver finds a reverse chain map `G:T -> S` and degree-raising operators `H` on `S` and `K` on `T` satisfying

```text
d_S G = G d_T
GF + d_S H + H d_S = id_S
FG + d_T K + K d_T = id_T
```

These equations are linear in the unknown coefficients of `G`, `H` and `K`, because `F` is fixed. A single Smith decomposition solves them over the integers and sets free Smith coordinates to zero for a deterministic choice. Both inverse identities are required; a left or right inverse alone is insufficient. The witness orientation follows the existing `to - from` convention. This implements the [definition of chain-homotopy equivalence](https://stacks.math.columbia.edu/tag/010V) for the retained finite integral quotient chains.

| Operation | Native result |
| --- | --- |
| `ChainMap.is-homotopy-equivalence` | Unary Boolean transfer; `false` precisely for an integral obstruction within the computation bounds |
| `ChainMap.homotopy-inverse` | Unary closed operation returning one validated `G` with full endpoints reversed |
| `ChainHomotopy.inverse-homotopies` | Unary flat transfer on a `ChainMap`; exactly `[GF -> id_S, FG -> id_T]` for the same solved `G` |
| `ChainMapClass.is-isomorphism` | Unary Boolean transfer testing invertibility in the homotopy category |
| `ChainMapClass.inverse` | Unary closed operation returning the unique inverse class with its canonical representative |

Nonexistence makes the two inverse operations and the flat witness operation `OPERATION_UNDEFINED`. The flat operation never returns an empty or partial list to signal failure. Decisions propagate computation failures. Existing `ChainMap.is-isomorphism` and `ChainMap.inverse` retain their strict degreewise meaning. A constant map of an interval is a homotopy equivalence despite its singular matrices. On a relative complex with only `Z/2` homology, multiplication by any odd integer is a homotopy equivalence. Multiplication by three on the circle is not: rational invertibility does not satisfy the integral equations.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex interval = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0, 1))));
BigInteger zero = BigInteger.ZERO, one = BigInteger.ONE;
SimplicialChainMap constant = new SimplicialChainMap(interval, interval, Arrays.asList(
        new IntegerMatrix(new BigInteger[][] {{one, one}, {zero, zero}}),
        IntegerMatrix.zero(1, 1)));
List<String> decision = math.flow(math.chainMaps, Collections.singletonList(constant))
        .<Boolean>performAlgebraTransfer("is-homotopy-equivalence").collect();
// [true]
List<String> witnesses = math.flow(math.chainMaps, Collections.singletonList(constant))
        .<SimplicialChainHomotopy>performFlatAlgebraTransfer("ChainHomotopy.inverse-homotopies")
        .collect();
// Exactly two explicit witnesses, source first and target second.
SimplicialChainMapClass value = SimplicialChainMapClass.fromMap(constant);
List<String> identity = math.flow(math.chainMapClasses, Collections.singletonList(value))
        .performOneOperandOperation("inverse").performOperation("compose", value)
        .<Boolean>performAlgebraTransfer("is-identity").collect();
// [true]
```

With quotient ranks `s_k` and `t_k`, the whole system permits at most **256 equations**, counted as `sum_k(s_k^2 + t_k^2 + s_(k-1)*t_k)`, and **256 unknown coefficients**, counted as `sum_k(s_k*t_k + s_(k+1)*s_k + t_(k+1)*t_k)`. Missing degrees have rank zero. The counts include zero equations and obvious identity maps. Quotient filtering precedes these bounds. The two returned homotopies retain their respective endpoint degree ranges, which can differ.

One **5000000-unit budget** covers matrix assembly, Smith solving, reverse-map validation, composite and identity construction, and both witness validations. Class inversion also shares this budget with the reverse Hom reduction, projection and canonical lift, which keep their existing Hom-rank bounds. Successful standalone stages need not fit when combined. Exhaustion raises `IMPLEMENTATION_FAILURE`, never a false decision, mathematical nonexistence or partial list. Coefficients have no bit-length cap. Representative choices need not be involutive or geometric; inverse **classes** are unique, involutive, and reverse composition order. This does not decide geometric homotopy equivalence of spaces.

`NativeSimplicialChainInverseTest` adds 13 tests covering independent determinants and adjugates for 625 point maps, 125 interval maps, all 25 relative interval contexts, one-sided inverses, 49 independent circle degree pairs, relative `Z/2` units and 1025-bit coefficients, direct boundary checks for both homotopies, different labels and dimensions, empty/acyclic contexts, inverse-class laws, aggregate bounds, shared normalization budgets and serialized native scalar/flat flows.

## Retained integral chain equivalences

`math.chainEquivalences` installs 36 operations on `ChainEquivalence`. A value retains `F:S -> T`, `G:T -> S`, and the actual homotopies `H:GF -> id_S` and `K:FG -> id_T`. Their equations use the existing `dH + Hd = to - from` convention. The carrier implements integral chain-homotopy equivalence as [defined for chain complexes](https://stacks.math.columbia.edu/tag/010V), with explicit witnesses and full labelled relative pairs.

`ChainEquivalence.from-map` solves once and retains the complete bundle. `ChainEquivalence.from-data` validates a `SimplicialChainEquivalence.Data` containing four individually valid inputs: both maps must have opposite full endpoints and each witness must join the exact composite to the exact identity. Homotopic but unequal endpoint maps are rejected. Data-carrier membership alone does not assert these joins. Identity construction and supplied-data validation do not search for an inverse.

| Operations | Native behavior |
| --- | --- |
| `from-map`, `from-data`, `identity-on` | Unary transfers from `ChainMap`, `ChainEquivalence.data`, and `RelativeComplex` |
| `source`, `target`, `forward`, `backward`, `source-homotopy`, `target-homotopy`, `data` | Unary transfers exposing the retained values |
| `inverse`, `compose`, `equal` | Unary swap of maps and witnesses, binary right-first composition, and equality including witness choices |
| `maps`, `homotopies` | Unary flat transfers returning exactly `[F,G]` and `[H,K]`, even for empty complexes |
| `homology-map`, `inverse-homology-map`, `homology-maps` | Scalar and flat mixed operations with a nonnegative degree; the flat result is exactly `[H(F),H(G)]` |
| `cohomology-map`, `inverse-cohomology-map`, `cohomology-maps` | Contravariant integral maps; the flat result is exactly `[H*(F),H*(G)]` |
| `on-chain`, `inverse-on-chain`, `on-cochain`, `inverse-on-cochain` | `ILeftProjectionOperation` returning the actual second carrier's `IAlgebraItem` wrapper |
| `forward-class`, `backward-class` | Unary transfers to mutually inverse contextual `ChainMapClass` values |

For `before=(F,G,H,K)` and `after=(A,B,J,L)`, `after.compose(before)` retains maps `AF`, `GB` and witnesses `GJF+H`, `AKB+L`. In degree `k`, the transported terms are `G_(k+1) J_k F_k` and `A_(k+1) K_k B_k`; the shifted degree matters. Every intermediate map and the final bundle are validated under one budget. This transports the supplied witnesses without running the inverse solver.

Composition is associative and unital on the retained data. Inversion swaps both maps and both witnesses, without negation; it is involutive and reverses composition. Nonzero stationary loops remain distinguishable. Composing with the inverse need not give literal identity witness data or fix raw chains/cochains. The projected homotopy classes and induced integral maps are mutual inverses, including torsion and exact presentations.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex interval = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0, 1))));
BigInteger zero = BigInteger.ZERO, one = BigInteger.ONE;
SimplicialChainMap constant = new SimplicialChainMap(interval, interval, Arrays.asList(
        new IntegerMatrix(new BigInteger[][] {{one, one}, {zero, zero}}),
        IntegerMatrix.zero(1, 1)));
SimplicialChainEquivalence value = math.chainMaps.algebra().buildAlgebraItem(constant)
        .<SimplicialChainEquivalence>performAlgebraTransfer("ChainEquivalence.from-map")
        .perform().getResult();
List<String> inverseMaps = math.flow(math.chainEquivalences, Collections.singletonList(value))
        .<AbelianGroupHomomorphism, BigInteger>performFlatAlgebraUnsafe("homology-maps", zero)
        .<Boolean>performAlgebraTransfer("is-isomorphism").collect();
// [true, true]
List<String> witnesses = math.flow(math.chainEquivalences, Collections.singletonList(value))
        .performOneOperandOperation("inverse").performOperation("compose", value)
        .<SimplicialChainHomotopy>performFlatAlgebraTransfer("homotopies").collect();
// Exactly two retained transported witnesses.
RelativeSimplicialChain edge = new RelativeSimplicialChain(interval, one, new IntegerVector(one));
List<String> image = math.flow(math.chainEquivalences, Collections.singletonList(value))
        .performLeftProjectionOperation("on-chain", edge)
        .<IntegerVector>performAlgebraTransfer("coordinates").collect();
// [[0]]: the forward map contracts the edge.
```

Every required quotient basis is limited to **256 simplices** after filtering. Construction, composition (including all transports and validation), and whole two-map integral lists each share a **5000000-unit budget**. `from-map` additionally retains the inverse solver's aggregate **256-equation/256-unknown** limits, including final bundle validation in the solve budget. Supplied data, identities, swapping and composition do not invoke this search; an identity on twelve points can be supplied and composed even though its inverse-search system exceeds the equation bound. Class projections use the existing Hom-rank limits. Exhaustion raises `IMPLEMENTATION_FAILURE`, never a partial list or a claim of nonexistence. Coefficient bit lengths are unbounded.

Typed chains may have negative degree with zero coordinates. Cochains and integral-map degrees are nonnegative. Actions require exact full pairs. This represents finite integral chain equivalences; no geometric homotopy equivalence, higher coherence or preservation of cochain products is asserted.

`NativeSimplicialChainEquivalenceTest` adds 17 tests: 25 solved interval bundles, 1,296 independent signed circle-witness compositions, retained loops and inverse laws, noncommuting point maps and dual variance, changing dimensions with both boundary identities, exact endpoint rejection, relative torsion with 1025-bit coefficients, typed actions, empty and diagonal pairs, construction beyond solver bounds, shared composition and two-map budgets, actual native wrappers, and serialized repeatable scalar/flat flows.

## Constructive conversions to chain equivalences

Five unary transfers now reuse the witnesses constructed by existing algebras. Their aliases on the input carrier begin with `ChainEquivalence.`. They return the registered `ChainEquivalence` wrapper and can feed the existing scalar or flat operations immediately.

| Conversion | Retained maps and witnesses |
| --- | --- |
| `from-isomorphism` on `ChainMap` | The supplied map, its strict integral inverse, and two zero homotopies. Every degree matrix must be square and unimodular. |
| `from-homotopy-equivalence` on `HomotopyEquivalence` | Both supplied simplicial maps and the negatives of both actual accumulated prism matrices. |
| `from-collapse` on `SimplicialCollapse` | Retraction `R`, inclusion `i`, the constructed source witness `iR -> id`, and a zero target witness since `Ri=id`. |
| `from-collapse-sequence` on `CollapseSequence` | Chronological composite retraction, final inclusion, accumulated source homotopy, and a zero target witness. |
| `from-subdivision` on `SimplicialSubdivision` | Signed subdivision `S`, last-vertex map `L`, a zero source witness since `LS=id`, and the constructed target witness `SL -> id`. |

The geometric `HomotopyEquivalence` convention runs from identity to composite. Conversion negates the actual prism matrices to obtain the opposite orientation required by `ChainEquivalence`; it does not recompute prisms along reversed geometric paths. Nonzero loops and supplied choices remain visible in equality. Conversions retain full labelled relative pairs, quotient projection and integral coefficients. A singular chain map may admit `from-map` but fail `from-isomorphism`, because the latter specifically requires a strict inverse.

Four binary witness actions use `ILeftProjectionOperation` and return the actual second carrier's wrapper: `source-homotopy-on-chain`, `target-homotopy-on-chain`, `source-homotopy-on-cochain`, and `target-homotopy-on-cochain`. For chains they raise degree by one and satisfy the full identities `dH + Hd = id - GF` and `dK + Kd = id - FG`. For cycles the first term alone fills the difference from inverse composition. Shifted transposes lower cochain degree and satisfy the dual identities. Typed cochain homotopy actions require positive degree; degree-zero matrices remain available by transferring to the retained homotopy and using its `cochain-matrix` operation. Typed chain actions allow negative-degree zero chains. All actions require the exact full source or target pair.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex interval = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0, 1))));
SimplicialCollapse collapse = new SimplicialCollapse(interval, FiniteSet.of(BigInteger.ONE));
SimplicialChainEquivalence value = math.collapses.algebra().buildAlgebraItem(collapse)
        .<SimplicialChainEquivalence>performAlgebraTransfer("ChainEquivalence.from-collapse")
        .perform().getResult();
RelativeSimplicialChain vertex = new RelativeSimplicialChain(interval, BigInteger.ZERO,
        new IntegerVector(BigInteger.ZERO, BigInteger.ONE));
List<String> boundary = math.flow(math.chainEquivalences, Collections.singletonList(value))
        .performLeftProjectionOperation("source-homotopy-on-chain", vertex)
        .performOneOperandOperation("boundary")
        .<IntegerVector>performAlgebraTransfer("coordinates").collect();
// [[-1, 1]]: vertex one minus its image under inclusion after retraction.
List<String> inverseMaps = math.flow(math.collapses, Collections.singletonList(collapse))
        .<SimplicialChainEquivalence>performAlgebraTransfer("ChainEquivalence.from-collapse")
        .<AbelianGroupHomomorphism, BigInteger>performFlatAlgebraUnsafe("homology-maps", BigInteger.ZERO)
        .<Boolean>performAlgebraTransfer("is-isomorphism").collect();
// [true, true]
```

None of these five conversions invokes the simultaneous inverse solver or its aggregate equation/unknown limits. For example, a strict identity on twelve points and a supplied contraction of a twelve-edge interval convert successfully beyond those search bounds. Each conversion still shares **one 5000000-unit budget** across both map constructions, both witnesses, and final bundle validation; strict conversion also includes integral matrix inversion. Individually successful components can exceed this budget when combined. Every required quotient basis remains bounded by **256 simplices**, after filtering. Exhaustion raises `IMPLEMENTATION_FAILURE`, with no replacement witnesses or partial bundle. The original geometric objects retain their own construction limits. Exact chain witnesses do not assert new geometric homotopy equivalences or higher coherence.

`NativeChainEquivalenceConversionsTest` adds 15 tests: 625 independent strict inverse decisions, signed interval fillings and relative projection, preserved nonzero geometric loops, all simplex facets through dimension five, chronological collapse composition, all 148 three-label relative subdivisions, extreme labels and 1025-bit coefficients, projective-plane torsion, both typed differential identities, exact-context and degree failures, empty and filtered diagonal pairs, conversion beyond solver bounds, shared-budget failures for all five converters, actual wrappers, and serialized repeatable flows.

## Integral mapping cones and quasi-isomorphisms

`IntegralChainMappingConeAlgebra` registers `ChainCone` directly in MathTool. Its immutable value retains a validated integral chain map `F: S -> T` with both full labelled relative pairs. Equality compares this defining map. The chain groups are `Cone(F)_n = T_n + S_(n-1)`, with target coordinates first and differential `[[d_T, F], [0, -d_S]]`. Inclusion is `j(t)=(t,0)` and shifted projection is `p(t,s)=s`. The minus sign gives `D*D=0`; projection anticommutes with the unshifted source differential. See the [homological cone convention](https://stacks.math.columbia.edu/tag/0621) and [cones and split exact sequences](https://stacks.math.columbia.edu/tag/014D).

The 24 operations use the existing interfaces in `operations/simple` and `operations/flat`:

| Operation aliases | Result |
| --- | --- |
| `from-map`, `map`, `source`, `target` | Construct a cone from a `ChainMap`, or recover its defining map and pairs. |
| `equal`, `dimension`, `chain-rank` | Compare exact data, obtain the formal top degree, or count coordinates at a natural degree. |
| `boundary-matrix`, `boundary-matrices` | Signed differential in one degree, or the complete ordered list. |
| `homology`, `homology-type`, `homology-types` | Constructive `IntegralHomology`, its abelian type, or all degreewise types. |
| `cohomology`, `cohomology-type`, `cohomology-types` | The corresponding calculations for transposed differentials. |
| `inclusion-matrix`, `projection-matrix` | Matrices of `j_n: T_n -> Cone_n` and `p_n: Cone_n -> S_(n-1)`. |
| `inclusion-homology-map`, `projection-homology-map` | `H_n(T) -> H_n(Cone)` and `H_n(Cone) -> H_(n-1)(S)`. |
| `inclusion-cohomology-map`, `projection-cohomology-map` | `H^n(Cone) -> H^n(T)` and `H^(n-1)(S) -> H^n(Cone)`. |
| `long-exact-segment`, `long-exact-cohomology-segment` | Three composable homomorphisms in the orders below. |
| `is-acyclic` | Whether every integral cone homology group vanishes. |

At degree `n`, the homology segment returns `[H_n(F), j_*, p_*]`, with endpoints `H_n(S) -> H_n(T) -> H_n(Cone) -> H_(n-1)(S)`. The cohomology segment returns `[j^*, H^n(F), p^* at n+1]`, with endpoints `H^n(Cone) -> H^n(T) -> H^n(S) -> H^(n+1)(Cone)`. Presentations at each shared endpoint agree exactly. Constructive homology exposes cycle/cocycle representatives, quotient classes and bounding chains through the existing `IntegralHomology` algebra.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex point = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0))));
SimplicialChainMap doubling = SimplicialChainMap.identity(point).scale(BigInteger.valueOf(2));
IntegralChainMappingCone cone = math.chainMaps.algebra().buildAlgebraItem(doubling)
        .<IntegralChainMappingCone>performAlgebraTransfer("ChainCone.from-map")
        .perform().getResult();
List<String> homology = math.flow(math.chainCones, Collections.singletonList(cone))
        .<AbelianGroupType>performFlatAlgebraTransfer("homology-types").collect();
// [AbelianGroup(rank=0, torsion=[2]), AbelianGroup(rank=0, torsion=[])]
List<String> cohomology = math.flow(math.chainCones, Collections.singletonList(cone))
        .<AbelianGroupType>performFlatAlgebraTransfer("cohomology-types").collect();
// [AbelianGroup(rank=0, torsion=[]), AbelianGroup(rank=0, torsion=[2])]
List<String> ranks = math.flow(math.chainCones, Collections.singletonList(cone))
        .performLeftProjectionOperation("chain-rank", BigInteger.ONE).collect();
// [1], using the actual N wrapper for the second operand's carrier.
List<String> quasi = math.flow(math.chainMaps, Collections.singletonList(doubling))
        .<Boolean>performAlgebraTransfer("is-quasi-isomorphism").collect();
// [false]
List<String> segment = math.flow(math.chainCones, Collections.singletonList(cone))
        .<AbelianGroupHomomorphism, BigInteger>performFlatAlgebraUnsafe("long-exact-segment", BigInteger.ZERO)
        .<Boolean>performAlgebraTransfer("is-surjective").collect();
// [false, true, true]: Z --2--> Z --> Z/2 --> 0.
```

`ChainMap.is-quasi-isomorphism` tests cone acyclicity under one shared budget. It returns true only after every cone degree has zero integral homology; a nonzero degree gives false. It does not construct inverse witnesses or invoke the simultaneous inverse solver. For example, the identity on twelve points passes beyond that solver's equation/unknown bounds. Resource exhaustion remains `IMPLEMENTATION_FAILURE`, rather than a negative decision.

Public degrees are natural numbers. Degreewise lists include degrees zero through `max(dim(T), dim(S)+1)`, retaining zero slots of diagonal relative pairs; an empty source contributes no shifted ambient degree. Two empty endpoints have dimension -1 and empty lists. Differentials immediately above the top degree retain their rows and zero columns. At degree zero, the homological projection retains the negative endpoint presentation with zero generators and `rank(S_0)` zero relations; its dual endpoint has a zero-by-zero presentation.

Construction retains the map without eager matrix assembly or Smith reduction. Each required quotient basis is bounded by 256 after filtering, and each combined cone group has at most **256 target plus shifted-source coordinates**. Each complete list, exact segment or acyclicity decision shares **one 5000000-unit computation budget**, including its reductions and induced maps. A scalar degree calculation can succeed while the complete list exceeds that budget. Acyclicity checks all combined ranks before reducing degrees. The carrier does not add a general chain-complex algebra or a geometric realization. Maps between cones with supplied square witnesses are provided by `ChainConeMap` below.

`NativeIntegralChainConeTest` checks 125 signed interval maps and 625 point matrices against independent integer oracles; both exact sequences; rectangular, empty and filtered pairs; 1025-bit torsion and explicit bounding chains; and integral extensions from projective-plane maps whose induced homology maps coincide while their cones differ. It also checks quasi-isomorphism beyond inverse-search bounds, whole-degree and exact-segment budget failures, actual second-carrier wrappers and serialized repeatable scalar/flat flows.

## Maps between integral cones with retained homotopies

`IntegralChainConeMapAlgebra` registers `ChainConeMap` in MathTool. For defining maps `F:S->T` and `G:Sprime->Tprime`, supply vertical maps `a:S->Sprime`, `b:T->Tprime` and an actual `ChainHomotopy` from `bF` to `Ga`. The existing witness convention gives `dH+Hd=Ga-bF`, so the cone-map matrix in degree `n` is `[[b_n,-H_(n-1)],[0,a_(n-1)]]`. It commutes with cone boundaries, inclusion and shifted projection. This implements the [construction from a square commuting up to homotopy](https://stacks.math.columbia.edu/tag/014D), with homological indexing and the repository's witness orientation.

The chosen homotopy is part of the value. Even a stationary square can have nonzero loop witnesses that change the induced cone homology map. Composition applies the right operand first and transports witnesses as `b_after H_before + H_after a_before`. Equality retains all this data; composition is associative and unital.

| Operation aliases | Result |
| --- | --- |
| `from-data`, `data` | Validate or recover immutable `ChainConeMap.data` with both cones, both vertical maps and the chosen homotopy. |
| `identity-on`, `from-homotopy` | Build a cone identity, or a cone isomorphism from an existing `ChainHomotopy`. |
| `source`, `target`, `source-map`, `target-map`, `homotopy` | Recover exact cone contexts, vertical maps `a,b`, or the witness `H:bF->Ga`. |
| `equal`, `compose` | Compare all retained data or compose maps with the exact joining cone. |
| `chain-matrix`, `cochain-matrix`, `chain-matrices`, `cochain-matrices` | One degree matrix or the complete list; cochain pullback uses transposes. |
| `homology-map`, `cohomology-map`, `homology-maps`, `cohomology-maps` | Induced integral maps in one degree or every formal degree. |
| `homology-naturality-maps`, `cohomology-naturality-maps` | The four vertical maps between the corresponding exact sequence segments. |
| `on-chain`, `on-cochain` | Typed pushforward and contravariant pullback, returning the actual second carrier wrapper. |

These 23 operations use the existing `operations/simple` and `operations/flat` interfaces. The homology naturality list is `[H_n(a), H_n(b), H_n(cone map), H_(n-1)(a)]`. The cohomology list is `[H^n(cone map), H^n(b), H^n(a), H^(n+1)(cone map)]`, running from the target segment back to the source segment. All three squares commute in each diagram, with equality of the actual presentations at their joins.

The example below retains a nonzero loop from a point into the circle. Its cone map changes homology even though both vertical chain maps are identities:

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex point = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0))));
RelativeSimplicialComplex circle = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Arrays.asList(FiniteSet.of(0, 1), FiniteSet.of(0, 2), FiniteSet.of(1, 2))));
SimplicialChainMap zero = SimplicialChainMap.zero(point, circle);
SimplicialChainHomotopy loop = new SimplicialChainHomotopy(zero, zero, Arrays.asList(
        new IntegerMatrix(new BigInteger[][] {{BigInteger.ONE}, {BigInteger.ONE.negate()}, {BigInteger.ONE}}),
        IntegerMatrix.zero(0, 0)));
IntegralChainConeMap map = math.chainHomotopies.algebra().buildAlgebraItem(loop)
        .<IntegralChainConeMap>performAlgebraTransfer("ChainConeMap.from-homotopy").perform().getResult();
List<String> matrix = math.flow(math.chainConeMaps, Collections.singletonList(map))
        .<IntegerMatrix, BigInteger>performAlgebraUnsafe("chain-matrix", BigInteger.ONE).collect();
// [ZMatrix(4x4)[[1, 0, 0, -1], [0, 1, 0, 1], [0, 0, 1, -1], [0, 0, 0, 1]]]
List<String> identity = math.flow(math.chainConeMaps, Collections.singletonList(map))
        .<AbelianGroupHomomorphism, BigInteger>performAlgebraUnsafe("homology-map", BigInteger.ONE)
        .<Boolean>performCustomResultOperation("equal", AbelianGroupHomomorphism.identity(map.source().homology(BigInteger.ONE).group())).collect();
// [false]
List<String> isomorphisms = math.flow(math.chainConeMaps, Collections.singletonList(map))
        .<AbelianGroupHomomorphism, BigInteger>performFlatAlgebraUnsafe("cohomology-naturality-maps", BigInteger.ZERO)
        .<Boolean>performAlgebraTransfer("is-isomorphism").collect();
// [true, true, true, true]
```

Converting the reverse homotopy gives a strict inverse to `from-homotopy`, including its retained witness data. General squares need a `Data` value and `ChainConeMap.from-data`; this validates all four full labelled endpoint pairs and both witness composites. Merely equal homology groups, matching dimensions or an oppositely oriented witness are insufficient. Supplied component objects are immutable and individually valid, while `Data` membership does not assert their mutual compatibility.

Public degrees are nonnegative. Lists run through the larger formal cone dimension, retaining zero slots and rectangular zero shapes; both empty cones give empty lists. The degree-zero homology naturality list retains the adjacent negative endpoint presentations. Each required quotient basis and combined cone rank is at most **256**. A single **5000000-unit budget** covers each full constructor, composition with both witness transports and validation, induced map with both cone reductions, whole-degree list, or four-map naturality list. Valid data may exceed later evaluation limits; exhaustion raises `IMPLEMENTATION_FAILURE`. Integer coefficients have no bit-length cap. Typed actions on `ConeChain` and `ConeCochain` are described below. This carrier does not add arbitrary cone-map matrix input, a geometric realization, or a homotopy-independent choice of cone morphism.

`NativeIntegralChainConeMapTest` adds 17 tests covering 125 signed interval homotopies, 729 independent witness compositions and 6,561 point-matrix compositions. Further cases check associativity, both variances, all six naturality squares, projective-plane torsion, relative degree shifts, empty and filtered pairs, negative endpoint presentations, 1025-bit coefficients, shared composition/list/naturality budgets, actual wrappers and serialized repeatable flows.

## Typed chains and cochains on integral cones

`IntegralConeChainAlgebra` and `IntegralConeCochainAlgebra` register `ConeChain` and `ConeCochain`, with 26 and 27 operations. Each immutable value retains the exact defining cone map, a homogeneous degree, and integer coordinates ordered target first and shifted source second. Chain degrees are integers; negative groups contain only zero. Cochain degrees are nonnegative. Context checks use the full defining map and labelled pairs, even when two cones have equal homology groups or equal coordinate ranks.

| Operations on both carriers | Result |
| --- | --- |
| `zero-on`, `basis-on` on `ChainCone` | A zero value or the ordered coordinate unit basis at the requested degree. |
| `add`, `subtract`, `negate`, `scale`, `equal`, `is-zero` | Integral arithmetic or equality on exact contexts and degrees. |
| `cone`, `degree`, `coordinates`, `with-coordinates` | Context inspection or replacement of correctly sized coordinates. |
| `class-of`, `representative` | Projection to an integral quotient class, or a typed representative in the chosen cone and degree. |
| `target-part`, `source-part` | Components on the actual relative target in degree `n` and source in degree `n-1`. |

`ConeChain` additionally supplies `boundary`, `is-cycle`, `is-boundary`, `bounding-coordinates`, `bounding-chain`, `homologous`, `homology`, `cycle-generators`, `include-target`, and `lift-source`. `ConeCochain` supplies `coboundary`, `is-cocycle`, `is-coboundary`, `cobounding-coordinates`, `cobounding-cochain`, `cohomologous`, `cohomology`, `cocycle-generators`, `include-source`, `lift-target`, and `evaluate`. The two generator operations are native unary flat operations and return typed values with the original cone and degree.

For a cone chain `(t,s)`, boundary is `(d_T t + F s, -d_S s)`. Target inclusion `t -> (t,0)` commutes with boundary. The source coordinate lift raises degree and satisfies `D lift(s) = include(Fs) - lift(ds)`; it is generally not a chain map. Dually, coboundary on `(u,v)` is `(delta_T u, F* u - delta_S v)`. Source inclusion raises degree and anticommutes with coboundary, while target lift satisfies `delta lift(u) = lift(delta u) + include(F* u)`. Component extraction preserves these exact relative contexts. Pairing is the integer dot product and satisfies `<delta u,c> = <u,Dc>`.

`ChainConeMap.on-chain` and `on-cochain` use `ILeftProjectionOperation`: the map is the first operand, and the returned `IAlgebraItem` belongs to the second operand's actual `ConeChain` or `ConeCochain` algebra. Pushforward requires the exact source cone; pullback requires the exact target cone. Both preserve degree and commute with differentials, and pairing is preserved under pushforward/pullback. `with-coordinates` and `scale` use `ICustomMemberOperation` and retain the first carrier.

```java
ConcreteMathematics math = new ConcreteMathematics();
RelativeSimplicialComplex point = RelativeSimplicialComplex.absolute(
        new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0))));
IntegralChainMappingCone cone = new IntegralChainMappingCone(
        SimplicialChainMap.identity(point).scale(BigInteger.valueOf(2)));
List<String> orders = math.flow(math.chainCones, Collections.singletonList(cone))
        .<IntegralConeChain, BigInteger>performAlgebraUnsafe("ConeChain.zero-on", BigInteger.ZERO)
        .performOneOperandFlatOperation("cycle-generators")
        .<AbelianGroupElement>performAlgebraTransfer("class-of")
        .<BigInteger>performAlgebraTransfer("order").collect();
// [2]
List<String> primitive = math.flow(math.chainCones, Collections.singletonList(cone))
        .<IntegralConeChain, BigInteger>performAlgebraUnsafe("ConeChain.zero-on", BigInteger.ZERO)
        .performCustomMemberOperation("with-coordinates", new IntegerVector(BigInteger.valueOf(2)))
        .performOneOperandOperation("bounding-chain")
        .<IntegerVector>performAlgebraTransfer("coordinates").collect();
// [[1]]: the returned chain has degree one, and its boundary is [2].
IntegralChainConeMap identity = IntegralChainConeMap.identity(cone);
IntegralConeCochain unit = new IntegralConeCochain(cone, BigInteger.ONE, new IntegerVector(BigInteger.ONE));
List<String> pulledOrder = math.flow(math.chainConeMaps, Collections.singletonList(identity))
        .performLeftProjectionOperation("on-cochain", unit)
        .<AbelianGroupElement>performAlgebraTransfer("class-of")
        .<BigInteger>performAlgebraTransfer("order").collect();
// [2], now in cohomological degree one.
```

A typed primitive is one integer solution, not the complete affine family. `class-of` and (co)homology comparison require (co)cycles; primitives require integral divisibility. `representative` checks the exact abelian presentation and uses its receiver only for the cone and degree. Anonymous abelian classes retain a presentation, not independent geometric provenance. At cochain degree zero, only the zero cochain is a coboundary and its `cobounding-coordinates` are empty. `cobounding-cochain` and cochain `source-part` require positive degree because their results would otherwise need negative typed cochains. Negative chain classes retain adjacent zero presentation shapes.

The combined cone coordinate rank is bounded by **256** after relative filtering. Membership checks the current degree; an adjacent boundary or reduction can still exceed its bounds. Each complete class projection, representative lift or generator list shares **one 5000000-unit budget** with its homology/cohomology reduction. The two cycle checks and solve for (co)homology comparison likewise share one budget. Coefficients have no bit-length cap. Resource exhaustion is `IMPLEMENTATION_FAILURE`, without partial generator lists or false mathematical decisions. No cup or cap product on arbitrary algebraic cones is assumed.

`NativeIntegralConeElementsTest` adds 17 tests: 3,375 independent signed differential cases, 729 independent shear/transpose actions, 243 adjoint pairings, structural inclusion and section signs, integer divisibility and typed primitives, free and torsion class actions, projective-plane generators, negative and relative degrees, full-context rejection, 1025-bit arithmetic, combined-rank and shared class/generator budgets, actual wrappers and serialized repeatable flows.
