# Concrete algebras connected to MathTool

`new ConcreteMathematics()` creates real `Algebra<T>` instances and registers their native operations in the existing `MathTool`. It includes N, Z, Q, Q(i), H(Q), Boolean, Q^2, Mat2(Q), Vec(Q), Mat(Q), Affine(Q), Tensor(Q), Exterior(Q), Q[x], Q(x), Poly(Q), PolynomialMap(Q), PolynomialForm(Q), PolynomialCell(Q), PolynomialChain(Q), S3, Z/6Z, finite sets of integers, rational samples, finite integer probability measures, finite simplicial complexes, finite integer relations/functions, finite categories/functors/natural transformations, and F5 (named Z/5Z). Each construction owns its algebra instances; separate tools do not share mutable registrations.

`new ConcreteMathematics(3, 5, 7)` instead uses dimension three and includes both prime fields. Dimension must be positive for the matrix algebra. Each prime is checked exactly; composite or duplicate field parameters are rejected.

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
| FiniteSimplicialAlgebra / FiniteComplex | union, intersection | dimension/Euler characteristic -> Z; vertex-count -> N | equality/subcomplex -> Boolean; skeleton; degree-indexed simplex count/Betti number; flat Betti numbers -> N |
| RationalSampleAlgebra / Sample(Q) | concatenate | size -> N, mean/variance -> Q, center | covariance -> Q; scale by Q; flat transfer elements -> Q |
| FiniteProbabilityAlgebra / FiniteDistribution(Z) | — | support -> finite set, support-size -> N, expectation/variance -> Q | event/point probability -> Q; conditioning; flat transfer outcomes -> Z; point mass from Z |
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


The default initializer currently installs 36 algebras and 644 named operations. Every registered operation is exercised through its native interface with an independently specified expected result in ConcreteAlgebrasTest. Sample statistics distinguish population and sample denominators; finite probability measures retain normalized rational masses. Conditioning on probability zero is undefined. Distributions retain their actual outcome Algebra, so two different carriers with the same Java member class are not silently identified.

For overloaded custom-member and unsafe operations, the most specific compatible second-operand class is selected (exact matches take priority). Re-registering the same second class replaces that overload. Ambiguous supertypes are rejected. Mathematical domains sharing one Java class need distinct operation names; overload selection does not infer a domain from a value.

Fixed `Matn(Q).solve` returns the exact vector solving M x = b for nonsingular square M. Singular matrices are outside that operation; the affine solution operation below handles them. `rank` returns a member of N. `derivative-order` accepts arbitrary nonnegative BigInteger orders and returns zero when the order exceeds the finite polynomial degree.

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

The reduction and solution conventions follow standard exact linear algebra, as documented by [SymPy's matrix API](https://docs.sympy.org/latest/modules/matrices/matrices.html); execution here is Java rational arithmetic. NativeRectangularLinearTest checks all 729 matrices of shape 2 by 3 with entries in {-1,0,1}, each with nine right-hand sides, using an independent minor-based rank oracle and substitution. It also checks empty, unique and infinite solutions, basis independence, large exact coefficients, canonical equality, shape failures and serialized native flows. Zero-sized matrices and sparse representations are not implemented.

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

The constructor closes facets under nonempty faces. Betti numbers are unreduced dimensions over F2; equality compares labelled simplex sets. The empty complex has dimension -1, Euler characteristic zero and an empty Betti list. Facet materialization is limited to 20 vertices. There is no integral torsion or persistent-homology computation. The homology and Euler-characteristic conventions follow the standard finite simplicial definitions in [Hatcher, chapter 2](https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf). NativeTopologyTest checks circles, filled triangles, the tetrahedron boundary, all 64 labelled graphs on four vertices and serialized flows.

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
