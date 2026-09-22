# Concrete algebras connected to MathTool

`new ConcreteMathematics()` creates real `Algebra<T>` instances and registers their native operations in the existing `MathTool`. It includes N, Z, Q, Q(i), Boolean, Q^2, Mat2(Q), Q[x], finite sets of integers, rational samples, finite integer probability measures, finite simplicial complexes, and F5 (named Z/5Z). Each construction owns its algebra instances; separate tools do not share mutable registrations.

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
| FiniteSimplicialAlgebra / FiniteComplex | union, intersection | dimension/Euler characteristic -> Z; vertex-count -> N | equality/subcomplex -> Boolean; skeleton; degree-indexed simplex count/Betti number; flat Betti numbers -> N |
| RationalSampleAlgebra / Sample(Q) | concatenate | size -> N, mean/variance -> Q, center | covariance -> Q; scale by Q; flat transfer elements -> Q |
| FiniteProbabilityAlgebra / FiniteDistribution(Z) | — | support -> finite set, support-size -> N, expectation/variance -> Q | event/point probability -> Q; conditioning; flat transfer outcomes -> Z; point mass from Z |
| IntegerSetAlgebra (extends FiniteSetAlgebra) / FiniteSet(Z) | union, intersection, difference, symmetric-difference, complement-in | cardinality -> N | subset/equal -> Boolean; contains; insert/remove; unary flat subsets; flat transfer elements -> Z |
| NaturalSemiring / N | add, multiply | successor, to-integer | None |
| IntegerRing / Z | add, subtract, multiply, gcd, lcm, quotient, remainder | negate, to-rational | greater/equal -> Boolean; divide-rational -> Q; flat quotient-remainder |
| RationalField / Q | add, subtract, multiply, divide | negate, inverse | greater/equal -> Boolean; flat add-subtract |
| PrimeField / Z/pZ | add, subtract, multiply, divide | negate, inverse | Fixed prime membership, exact residues |
| BooleanAlgebra / Boolean | and, or, xor, implies, equal | not | None |
| RationalComplexField / Q(i) | add, subtract, multiply, divide | negate, conjugate, norm-squared -> Q | Q(i).embed-rational on Q -> Q(i) |
| RationalVectorSpace / Q^n | add, subtract | negate | scale by Q -> Q^n; dot -> Q; flat scale-flat and scale-signs |
| RationalMatrixAlgebra / Matn(Q) | add, subtract, multiply | negate, transpose, inverse, determinant -> Q, trace -> Q, rank -> N | scale by Q; apply to Q^n; solve with nonsingular matrix |
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


The default initializer currently installs 13 algebras and 148 named operations. Every registered operation is exercised through its native interface with an independently specified expected result in ConcreteAlgebrasTest. Sample statistics distinguish population and sample denominators; finite probability measures retain normalized rational masses. Conditioning on probability zero is undefined. Distributions retain their actual outcome Algebra, so two different carriers with the same Java member class are not silently identified.

For overloaded custom-member and unsafe operations, the most specific compatible second-operand class is selected (exact matches take priority). Re-registering the same second class replaces that overload. Ambiguous supertypes are rejected. Mathematical domains sharing one Java class need distinct operation names; overload selection does not infer a domain from a value.

`solve` returns the exact vector solving M x = b for nonsingular square M. Singular matrices are outside this operation, including consistent systems with multiple solutions. `rank` returns a member of N. `derivative-order` accepts arbitrary nonnegative BigInteger orders and returns zero when the order exceeds the finite polynomial degree.

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
