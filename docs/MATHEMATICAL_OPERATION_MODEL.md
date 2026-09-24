# Mathematical operation model

Status: native concrete implementation and scoped survey. Human mathematical review is UNREVIEWED; formal verification is UNVERIFIED.

## Architecture and carriers

The execution path is the original Algebra -> IAlgebraItem -> AlgebraFlow, with algebras registered in MathTool. Concrete builders live in [algebra/concrete](../groupimp/src/main/java/algebra/concrete); executable operation classes live in [operations/simple](../groupimp/src/main/java/operations/simple) and [operations/flat](../groupimp/src/main/java/operations/flat) and implement those packages' interfaces.

Algebra is final, so ConcreteAlgebra builds an instance and installs native operations into it. ConcreteMathematics implements IMathToolInitializer. OperationRegistration describes carriers, alias, interface, flatness and partiality for reports; execution does not go through that description.

A carrier is defined by its representation and membership rules, without enumerating its members. N and Z both use BigInteger but have different predicates. Fixed dimension, prime modulus, finite support and probability normalization are checked where applicable. TypedMembershipRule rejects null, a wrong representation and values violating its predicate. Native operations validate operands and results through their actual Algebra.

Names and Java classes alone do not prove mathematical compatibility. Concrete registration rejects a different Algebra instance occupying a carrier name. ListAlgebraInput requires the same carrier instance on reading. Overload dispatch uses the second operand's Java class: exact matches first, then the most specific assignable class; ambiguous matches are rejected. Different mathematical domains sharing one Java class need distinct operation names or explicit conversions.

## Native operation forms

A and B denote carriers; returning A or B can produce a changed value.

| Shape | Scalar interface and result | Flat interface and result |
| --- | --- | --- |
| A -> A | IOneOperandOperation<A>: raw A, wrapped by item dispatch | IOneOperandFlatOperation<A>: List<IAlgebraItem<A>> |
| A -> B | ITransferOperation<A>: IAlgebraItem<B> | ITransferFlatOperation<A>: List<IAlgebraItem<B>> |
| A x A -> A | IOperation<A>: raw A, wrapped by item dispatch | IFlatOperation<A>: List<IAlgebraItem<A>> |
| A x A -> B | ICustomResultOperation<A>: IAlgebraItem<B> | ICustomResultFlatOperation<A>: List<IAlgebraItem<B>> |
| A x B -> A | ICustomMemberOperation<A>: IAlgebraItem<A> | ICustomMemberFlatOperation<A>: List<IAlgebraItem<A>> |
| A x B -> B | ILeftProjectionOperation<A,B>: IAlgebraItem<B> | ILeftProjectionFlatOperation<A,B>: List<IAlgebraItem<B>> |
| A x B -> C | IUnsafeOperation<A>: IAlgebraItem<C> | IUnsafeFlatOperation<A>: List<IAlgebraItem<C>> |

The historical name ILeftProjectionOperation is retained at the user's request for **A x B -> B**. It can transform the second value, as in rational scalar times vector. The pure projection returns the second value unchanged, wrapped in its Algebra. ICustomMemberOperation continues to provide **A x B -> A**.

Some original interfaces select B/C through method-level type parameters. Their compile-time guarantees are limited; native implementations retain concrete operand/result algebras and validate their values. A caller can still request a false generic result type through the old API.

Constants use Unit -> carrier transfers. Finite n-ary inputs can use a product member: polynomial integration is Q[x] x Pair(Q,Q) -> Q. See [the n-ary RFC](RFC_NARY_OPERATIONS.md).

## Items and flows

Same-carrier binary item operations build the existing deferred decorator chain. Call perform() before reading its result. Transfers, mixed operations and unary operations evaluate the preceding chain before applying their native implementation.

Items and flows support performOneOperandOperation(name), also available as performOperation(name). Flat counterparts are performOneOperandFlatOperation(name) and performFlatOperation(name). Unary flat item dispatch checks every result wrapper belongs to the same Algebra, validates its value and retains order and duplicates.

AlgebraFlow appends deferred invocations and executes them on collect(). Cross-domain facades share execution state. Result-algebra availability is checked before a transfer or custom-result invocation is appended; rejection leaves the existing plan usable. Repeated collection rereads the input and runs the plan. Collection returns the legacy List<String> representation.

The executor is finite and eager at collection. Shared flow facades are a mutable pipeline, not persistent independent branches. Local serialization is tested; concurrent mutation and distributed execution are not certified.

## Partiality, failures and evidence

| Situation | Native API behavior |
| --- | --- |
| Invalid member passed to buildAlgebraItem | Existing method returns null |
| Native operation operand/result outside its Algebra | NotMemberException |
| Valid member outside an operation's definition | MathFailure with OPERATION_UNDEFINED |
| Missing operation or incompatible overload | Existing UnsupportedOperationException |
| Missing result algebra in a flow | AlgebraNotExistsException before plan mutation |
| Explicit materialization limit | MathFailure with IMPLEMENTATION_FAILURE |

Examples of undefined operations include division by zero, singular matrix inversion or fixed Matn(Q).solve, sample variance with fewer than two observations, and conditioning on a zero-probability event. Affine(Q).solve accepts singular and rectangular systems with matching right-hand sides; inconsistency yields an empty affine solution set. Operations on variable-dimensional vectors and matrices check dimensions at execution. Native execution does not automatically convert arbitrary callback exceptions to structured outcomes.

Partiality is a declaration, not a proof of totality or termination. Algebra predicates and bodies are trusted application code. Law descriptions do not confer proof status. Exact arithmetic introduces no floating-point approximation; it does not promise resource-unbounded computation or formal verification.

## Distinct result semantics

| Member/result type | Meaning |
| --- | --- |
| Native flat LIST | Finite ordered wrapped members; duplicates and empty lists retained |
| FiniteSet | Finite set under member equality |
| Multiset | Finite support with exact nonnegative integer multiplicities |
| LazySequence | Natural-indexed generator; only requested terms/prefixes computed |
| FiniteDistribution | Finite support with nonnegative rational masses summing to one |
| PrimitiveFamily | Rational constant parameter -> polynomial primitive |
| RationalAffineSpace | Empty solution set or canonical particular point plus a finite basis of rational directions |

Sets, distributions and solution families can be single scalar members. They are flattened only by an explicit operation. Materializing all subsets is capped at 20 input elements; exceeding it is an implementation restriction, not mathematical nonexistence.

## Concrete scopes and optional prototype

The default initializer has 45 algebra builders and 870 native registrations. [Concrete examples](CONCRETE_ALGEBRAS.md) cover arithmetic, finite sets/functions/categories, statistics, probability, vectors, rectangular matrices, affine solution sets, exact least-squares, characteristic/minimal polynomials and rational spectral decompositions, coordinate tensors, exterior algebras, rational quaternion rotations, polynomials, exact polynomial vector calculus and polynomial differential forms/cells/chains. Vec(Q), Mat(Q) and Tensor(Q) are families with partial dimension-sensitive operations; they are not one vector space or ring across all dimensions. Pseudoinverses and projections use standard Euclidean inner products over rational coordinates. Square-matrix polynomial calculus returns exact Q[x] values and matrix wrappers. Rational spectra emit distinct rational roots; diagonalization emits [P,D] only for a full rational eigenbasis, with A*P=P*D. Resource exhaustion propagates separately from mathematical nonexistence. Tensor contraction uses standard coordinate pairings with explicit matching axis sizes. Q(i) is a proper subfield of C. Finite symbolic expressions are not all R. Poly(Q) retains a positive input dimension, and PolynomialMap(Q) retains both input and output dimensions; arithmetic and composition check compatibility. Gradients, Jacobians and Hessians connect these carriers to the actual Vec(Q) and Mat(Q) instances. PolynomialForm(Q) supplies mixed differential degrees with polynomial coefficients; pullback uses coefficient substitution and dF_i, distinct from the exterior carrier's covariant multivector action. Hodge and vector-field conversions use the standard Euclidean metric. PolynomialCell(Q) retains a parametrized unit cube or point; PolynomialChain(Q) retains a finite rational formal sum and its integer degree. Integration requires matching ambient dimensions and homogeneous form degree, and retains orientation and multiplicity. Polynomial differentiation does not decide differentiability of arbitrary callbacks.

The earlier mathematics.core Domain/Signature/UnaryOperation/BinaryOperation/FlatOperation/Outcome prototype remains separately available and tested, with StandardMathematics and LegacyAdapters. **ConcreteMathematics does not use that prototype to execute operations.** Its Domain compatibility is identity-based; membership can be MEMBER, NOT_MEMBER or UNKNOWN. Its evaluate method captures categorized failures and composition tracks accuracy labels. These are prototype properties, not native Algebra guarantees. Prototype Outcome.trace lists planned pipeline ids, not executed events.

Function, symbolic-real and sequence utilities also have explicit restricted scopes. Finite relations, finite topology, integer-set polynomial optimization and polynomial orbits are registered natively. The coverage registry distinguishes their source/test evidence from native registrations. See [implementation status](IMPLEMENTATION_STATUS.md) for advanced structures requiring further work.

## Native example

~~~java
ConcreteMathematics math = new ConcreteMathematics();
RationalVector v = new RationalVector(Rational.ONE, Rational.of(2));
IAlgebraItem<RationalVector> scaled =
        math.rationals.algebra().buildAlgebraItem(Rational.of(3))
                .performLeftProjectionOperation("Q^2.scale-left", v);
// Wrapper in math.vectors.algebra(), value [3, 6]

List<String> values = math.flow(math.polynomials,
        Collections.singletonList(new Polynomial(Rational.ZERO, Rational.ZERO, Rational.ONE)))
        .performOneOperandOperation("derivative")
        .performLeftProjectionOperation("evaluate", Rational.of(3))
        .collect(); // ["6"]
~~~

Imports and executable assertions are in [ConcreteAlgebrasTest](../groupimp/src/test/java/mathematics/ConcreteAlgebrasTest.java). [The baseline audit](CURRENT_ARCHITECTURE.md) records behavior before the corrections; it is not the current API contract.

FiniteMarkov(Z) represents rational stochastic kernels with explicit finite integer boundaries and actual outcome Algebra identity. Apply returns the second FiniteDistribution(Z) wrapper, composition checks the complete middle boundary, and scalar/flat transfers connect matrices, deterministic functions, state sets and stationary laws. Stationary extremes form a finite generating list for a convex family; they do not enumerate all stationary distributions. Hitting probabilities and finite mean hitting times use Vec(Q), with infinite means reported as undefined.

AbelianGroupType represents finitely generated abelian-group isomorphism types by nonnegative free rank and canonical cyclic torsion factors. Direct sum, tensor, Hom, Tor_1 and Ext^1 operate over Z and return wrapped types; individual elements and morphisms are not represented. FiniteComplex integral homology returns these types through scalar mixed and flat transfer operations. Rational Betti numbers are their free ranks; the original F2 operations retain their coefficients. Integral boundaries use increasing vertex orientation and bounded exact Smith reduction, with shared resource exhaustion reported separately from mathematical nonexistence.

Vec(Z) and Mat(Z) represent finite free integer coordinate modules and their matrices, with zero-sized dimensions retained. Smith decomposition emits [U,D,V], where U*A*V=D and U,V are unimodular. Kernel and image operations return integral lattice bases. Scalar solve-particular and flat solve-generators return the second Vec(Z) carrier wrappers; the latter parametrizes all solutions by a particular vector followed by a kernel basis. Cokernel transfers classify integer presentations in AbelianGroupType, while FiniteComplex.boundary-matrix connects oriented simplicial chains to Mat(Z). Resource limits remain distinct from the absence of integer solutions.

PresentedAbelianGroup retains an integer column-relation matrix and its Smith-coordinate map. AbelianGroupElement retains that presentation and canonical coordinates; addition checks presentation compatibility. Projection and construction from Smith coordinates use mixed native operations, while reduction returns the second Vec(Z) wrapper. Integer scaling and flat multiplication-preimages return the first element carrier wrappers. Element equality is presentation-aware, and the separate isomorphic predicate compares group types without implicitly transporting elements. Finite enumeration is complete within the output cap; infinite requested lists are undefined and oversized finite lists are implementation failures.

AbelianGroupHomomorphism retains both presentations and a relation-respecting matrix of normalized generator images in Smith coordinates. Composition requires equal middle presentations and applies the right operand first. Application and preimage operations return the second AbelianGroupElement carrier wrapper, with the appropriate target or source presentation retained in the value. Unary kernel/image/cokernel transfers produce presented groups; same-carrier unary inclusion/projection operations produce explicit maps that compose in AlgebraFlow. Inverses require actual isomorphisms. Original-coordinate matrix lifts and paired-boundary constructors connect these maps to Mat(Z). All compound integer calculations share a work budget, and implementation exhaustion propagates separately from mathematical undefinedness or a false predicate.

IntegralHomology retains two consecutive integer boundaries and checks that they compose to zero. A full integral cycle basis turns incoming boundaries into a presented abelian quotient. Native scalar and flat operations expose matrices, cycle representatives and bounding chains. Class-of returns AbelianGroupElement; representative chooses a cycle without claiming an additive section. Cycle-coordinate conversion and bounding-chain use second Vec(Z) wrappers. Induced-map accepts a target homology value and degree matrix, checks preservation of cycles and boundaries, and returns an AbelianGroupHomomorphism usable in the existing flows. FiniteComplex construction uses the original oriented simplex order; no complete chain map or simplicial map is inferred from one degree matrix.

SimplicialMap supplies the vertex-defined case: it validates totality and simplex preservation against both full labelled complexes. Chain matrices use the existing simplex bases, permutation orientation signs and zero columns for collapsed simplices. Native degreewise and flat transfers connect them to Mat(Z), IntegralHomology and AbelianGroupHomomorphism. FiniteFunction construction and extraction use the actual Z carrier; vertex and simplex images retain second-operand wrappers. Composition requires equal middle complexes, and inverse requires simplex preservation in both directions. Homology-map construction shares the integer work budget across all intermediate models; flat homology maps share it across the entire degree list.
