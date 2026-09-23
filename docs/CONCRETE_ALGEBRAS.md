# Concrete algebras connected to MathTool

`new ConcreteMathematics()` creates real `Algebra<T>` instances and registers their native operations in the existing `MathTool`. It includes N, Z, Q, Q(i), Boolean, Q^2, Mat2(Q), Vec(Q), Mat(Q), Affine(Q), Q[x], Q(x), S3, Z/6Z, finite sets of integers, rational samples, finite integer probability measures, finite simplicial complexes, finite integer relations/functions, finite categories/functors/natural transformations, and F5 (named Z/5Z). Each construction owns its algebra instances; separate tools do not share mutable registrations.

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
| SymmetricGroup / Sn | compose | inverse, order, sign, fixed-point-count, flat cycles | apply and flat orbit at N; integer powers; equality; enumeration |
| BooleanAlgebra / Boolean | and, or, xor, implies, equal | not | None |
| RationalComplexField / Q(i) | add, subtract, multiply, divide | negate, conjugate, norm-squared -> Q | Q(i).embed-rational on Q -> Q(i) |
| RationalVectorSpace / Q^n | add, subtract | negate | scale by Q -> Q^n; dot -> Q; flat scale-flat and scale-signs |
| RationalMatrixAlgebra / Matn(Q) | add, subtract, multiply | negate, transpose, inverse, determinant -> Q, trace -> Q, rank -> N | scale by Q; apply to Q^n; solve with nonsingular matrix |
| RationalVectorFamily / Vec(Q) | partial add, subtract, dot -> Q | negate; dimension -> N; flat entries -> Q; zero-like | scale by Q; fixed-carrier conversions; empty vector constant |
| RationalMatrixFamily / Mat(Q) | partial add, subtract, multiply; equal -> Boolean | transpose, RREF; rank/nullity -> N; flat pivot columns -> N; flat nullspace/row-space/column-space bases -> Vec(Q); rows, columns, shape counts | scale by Q; apply -> Vec(Q); fixed-carrier conversions; zero-like; partial inverse/determinant/trace |
| RationalAffineSpaceAlgebra / Affine(Q) | equal -> Boolean | particular -> Vec(Q); flat directions -> Vec(Q); dimension/ambient-dimension -> N; is-empty/is-unique | solve Mat(Q) x Vec(Q) -> Affine(Q); contains -> Boolean; at -> Vec(Q) |
| RationalPolynomialRing / Q[x] | add, subtract, multiply | negate, derivative | derivative-order with N; evaluate at Q -> Q; primitive/integrate; iterate and flat orbit with Pair(Q,N) |

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


The default initializer currently installs 28 algebras and 407 named operations. Every registered operation is exercised through its native interface with an independently specified expected result in ConcreteAlgebrasTest. Sample statistics distinguish population and sample denominators; finite probability measures retain normalized rational masses. Conditioning on probability zero is undefined. Distributions retain their actual outcome Algebra, so two different carriers with the same Java member class are not silently identified.

For overloaded custom-member and unsafe operations, the most specific compatible second-operand class is selected (exact matches take priority). Re-registering the same second class replaces that overload. Ambiguous supertypes are rejected. Mathematical domains sharing one Java class need distinct operation names; overload selection does not infer a domain from a value.

Fixed `Matn(Q).solve` returns the exact vector solving M x = b for nonsingular square M. Singular matrices are outside that operation; the affine solution operation below handles them. `rank` returns a member of N. `derivative-order` accepts arbitrary nonnegative BigInteger orders and returns zero when the order exceeds the finite polynomial degree.

## Rectangular matrices and affine solution sets

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
