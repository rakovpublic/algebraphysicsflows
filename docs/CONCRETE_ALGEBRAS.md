# Concrete algebras connected to MathTool

`new ConcreteMathematics()` creates real `Algebra<T>` instances and registers their native operations in the existing `MathTool`. It includes N, Z, Q, Q(i), Boolean, Q^2, Mat2(Q), Q[x], Q(x), S3, Z/6Z, finite sets of integers, rational samples, finite integer probability measures, finite simplicial complexes, finite integer relations/functions, finite categories/functors/natural transformations, and F5 (named Z/5Z). Each construction owns its algebra instances; separate tools do not share mutable registrations.

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
| FiniteFunctorAlgebra / FiniteFunctor | compose, equality -> Boolean | strict inverse; opposite; source/target categories; full/faithful/equivalence checks; object/arrow maps; flat images | map-object/map-arrow; flat object/arrow fibers; identity-on; from-discrete-map |
| FiniteNaturalTransformationAlgebra / FiniteNaturalTransformation | vertical compose, horizontal, equality -> Boolean | inverse; opposite; source/target functors; component-map; flat components | component; flat component-fiber; precompose/postcompose by a functor; identity-on |
| FiniteEquivalenceAlgebra / FiniteEquivalence | compose; equality -> Boolean | inverse; opposite; source/target categories; forward/backward functors; unit/counit transformations | identity-on; from-functor |
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


The default initializer currently installs 22 algebras and 308 named operations. Every registered operation is exercised through its native interface with an independently specified expected result in ConcreteAlgebrasTest. Sample statistics distinguish population and sample denominators; finite probability measures retain normalized rational masses. Conditioning on probability zero is undefined. Distributions retain their actual outcome Algebra, so two different carriers with the same Java member class are not silently identified.

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

Construction makes deterministic finite choices: prefer the least source object mapping exactly to each target object, otherwise choose the least source object and least isomorphism to it in the target. Fullness and faithfulness give unique lifts for the backward arrow map and unit. This works for equivalent categories with different numbers of objects. Unknown or non-equivalence functors are not converted into witnesses.

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

Tests check all functors between indiscrete categories with one through three objects against independent representative formulas, nontrivial cyclic-group unit/counit choices, composition and associativity, invalid triangles and serialized native flows. The finite category cap stays at 128 arrows/objects; arbitrary adjunctions with noninvertible witnesses and infinite categories are outside this carrier.
