"""Synchronize native registration facts; preserve the separately authored survey."""
import argparse
import copy
import csv
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "mathematics-coverage.json"
MANIFEST = ROOT / "groupimp/src/test/resources/mathematics/concrete-catalog.tsv"
DATE = "2026-09-24"
OWNERS = {
    "BooleanAlgebra": ("Boolean", "Boolean truth values with Boolean operations"),
    "NaturalSemiring": ("N", "Nonnegative arbitrary precision integers with addition and multiplication"),
    "IntegerRing": ("Z", "Arbitrary precision integers with ring operations and truncated quotient"),
    "RationalField": ("Q", "Canonical exact rational field"),
    "PrimeField": ("Z/5Z", "Residues in the default prime field F5; configurable exactly checked prime int modulus"),
    "ResidueRing": ("Z/6Z", "Residue ring modulo six by default; configurable arbitrary-precision modulus greater than one"),
    "RationalComplexField": ("Q(i)", "Pairs of rational coordinates; a proper subfield of the complex numbers"),
    "RationalQuaternionAlgebra": ("H(Q)", "Hamilton's noncommutative division algebra with four rational coordinates and exact rational three-dimensional rotations"),
    "RationalVectorSpace": ("Q^2", "Fixed-dimensional rational vectors; default dimension two"),
    "RationalMatrixAlgebra": ("Mat2(Q)", "Fixed positive-dimensional square rational matrices; default dimension two"),
    "RationalVectorFamily": ("Vec(Q)", "Finite rational vectors of varying nonnegative dimensions with checked partial dimension-sensitive operations"),
    "RationalMatrixFamily": ("Mat(Q)", "Positive rectangular rational matrices with shape-checked operations and exact row-reduction bases"),
    "IntegerVectorFamily": ("Vec(Z)", "Finite integer coordinate vectors with retained nonnegative dimension"),
    "IntegerMatrixFamily": ("Mat(Z)", "Exact matrices between finite free integer modules, including zero-sized shapes and constructive Smith witnesses"),
    "RationalAffineSpaceAlgebra": ("Affine(Q)", "Canonical affine solution sets of finite rational linear systems, including empty sets with retained ambient dimension"),
    "RationalTensorAlgebra": ("Tensor(Q)", "Dense finite rational coordinate tensors with explicit nonnegative axis dimensions and standard coordinate contraction"),
    "RationalExteriorAlgebra": ("Exterior(Q)", "Sparse graded exterior elements over standard oriented rational coordinate spaces with retained ambient dimension"),
    "RationalPolynomialRing": ("Q[x]", "Finite univariate polynomials with canonical rational coefficients"),
    "RationalMultivariatePolynomialAlgebra": ("Poly(Q)", "Sparse exact rational polynomials with an explicit ordered positive input dimension"),
    "RationalPolynomialMapAlgebra": ("PolynomialMap(Q)", "Ordered tuples of exact rational polynomials representing maps between explicit positive coordinate dimensions"),
    "PolynomialDifferentialFormAlgebra": ("PolynomialForm(Q)", "Mixed-degree differential forms with rational polynomial coefficients in standard positive-dimensional coordinate spaces"),
    "PolynomialCellAlgebra": ("PolynomialCell(Q)", "Rational polynomial parametrizations of positively oriented real unit cubes, including explicit point cells"),
    "PolynomialChainAlgebra": ("PolynomialChain(Q)", "Finite rational formal sums of polynomial cells with a retained ambient dimension and integer degree"),
    "RationalFunctionField": ("Q(x)", "Formal univariate rational functions over Q, normalized to coprime polynomials with monic denominator"),
    "IntegerSetAlgebra": ("FiniteSet(Z)", "Finite integer sets under canonical equality, with polynomial optimization over explicit feasible sets"),
    "RationalSampleAlgebra": ("Sample(Q)", "Finite ordered rational samples retaining repeated observations"),
    "FiniteProbabilityAlgebra": ("FiniteDistribution(Z)", "Finite integer distributions with exact nonnegative rational masses summing to one"),
    "FiniteMarkovAlgebra": ("FiniteMarkov(Z)", "Exact rational stochastic kernels between explicit finite integer state sets, including empty source sets"),
    "FiniteSimplicialAlgebra": ("FiniteComplex", "Finite abstract simplicial complexes with integer labels, unreduced integral homology types and coefficient-specific Betti numbers"),
    "AbelianGroupTypeAlgebra": ("AbelianGroupType", "Canonical finitely generated abelian-group isomorphism types, with free rank and cyclic torsion invariant factors"),
    "PresentedAbelianGroupAlgebra": ("PresentedAbelianGroup", "Quotients of finite free integer modules by retained relation matrices, with explicit Smith-coordinate maps"),
    "AbelianGroupElementAlgebra": ("AbelianGroupElement", "Elements of retained finitely presented abelian groups, with canonical finite residues and free integer coordinates"),
    "FiniteIntegerRelationAlgebra": ("FiniteRelation(Z,Z)", "Finite-support relations on the actual registered integer Algebra"),
    "FiniteIntegerFunctionAlgebra": ("FiniteFunction(Z,Z)", "Total maps between explicit finite integer sets, preserving declared domain and codomain"),
    "FiniteCategoryAlgebra": ("FiniteCategory", "Finite categories with integer object/arrow labels and exhaustively checked composition tables"),
    "FiniteFunctorAlgebra": ("FiniteFunctor", "Covariant functors between validated finite category tables with total object and arrow maps"),
    "FiniteNaturalTransformationAlgebra": ("FiniteNaturalTransformation", "Natural transformations between parallel finite functors with every naturality square checked"),
    "FiniteEquivalenceAlgebra": ("FiniteEquivalence", "Finite adjoint equivalences with explicit quasi-inverse, unit, counit and checked triangle identities"),
    "FiniteAdjunctionAlgebra": ("FiniteAdjunction", "Adjunctions of finite category tables with checked unit/counit and constructive finite adjoint search"),
    "FiniteConeAlgebra": ("FiniteCone", "Validated cones over finite diagrams with bounded exhaustive limit construction and unique-factorization checks"),
    "FiniteCoconeAlgebra": ("FiniteCocone", "Validated cocones over finite diagrams with bounded exhaustive colimit construction through opposite categories"),
    "SymmetricGroup": ("S3", "Symmetric group on the zero-based labels 0,1,2; configurable fixed nonnegative degree"),
}
INTERFACES = {
    "IOperation": "simple/ClosedOperation",
    "IOneOperandOperation": "simple/OneOperandOperation",
    "ITransferOperation": "simple/TransferOperation",
    "ICustomResultOperation": "simple/CustomResultOperation",
    "ICustomMemberOperation": "simple/CustomMemberOperation",
    "ILeftProjectionOperation": "simple/SecondResultOperation",
    "IUnsafeOperation": "simple/MixedOperation",
    "IFlatOperation": "flat/ClosedFlatOperation",
    "IOneOperandFlatOperation": "flat/OneOperandFlatOperation",
    "ITransferFlatOperation": "flat/TransferFlatOperation",
    "ICustomResultFlatOperation": "flat/CustomResultFlatOperation",
    "ICustomMemberFlatOperation": "flat/CustomMemberFlatOperation",
    "ILeftProjectionFlatOperation": "flat/SecondResultFlatOperation",
    "IUnsafeFlatOperation": "flat/MixedFlatOperation",
}
EXTRA_TESTS = {
    "RationalVectorFamily": "NativeRectangularLinearTest",
    "RationalMatrixFamily": "NativeRectangularLinearTest",
    "IntegerVectorFamily": "NativeIntegerLinearTest",
    "IntegerMatrixFamily": "NativeIntegerLinearTest",
    "RationalAffineSpaceAlgebra": "NativeRectangularLinearTest",
    "RationalTensorAlgebra": "NativeTensorTest",
    "RationalExteriorAlgebra": "NativeExteriorTest",
    "RationalQuaternionAlgebra": "NativeQuaternionTest",
    "IntegerSetAlgebra": "NativeFlatAndSetTest",
    "RationalPolynomialRing": "NativeDynamicsTest",
    "RationalMultivariatePolynomialAlgebra": "NativeMultivariateCalculusTest",
    "RationalPolynomialMapAlgebra": "NativeMultivariateCalculusTest",
    "PolynomialDifferentialFormAlgebra": "NativePolynomialFormsTest",
    "PolynomialCellAlgebra": "NativePolynomialChainsTest",
    "PolynomialChainAlgebra": "NativePolynomialChainsTest",
    "RationalFunctionField": "NativeRationalFunctionTest",
    "RationalSampleAlgebra": "NativeStatisticsProbabilityTest",
    "FiniteProbabilityAlgebra": "NativeStatisticsProbabilityTest",
    "FiniteMarkovAlgebra": "NativeMarkovTest",
    "FiniteSimplicialAlgebra": "NativeTopologyTest",
    "AbelianGroupTypeAlgebra": "NativeAbelianGroupTest",
    "PresentedAbelianGroupAlgebra": "NativePresentedAbelianTest",
    "AbelianGroupElementAlgebra": "NativePresentedAbelianTest",
    "FiniteIntegerRelationAlgebra": "NativeRelationTest",
    "FiniteIntegerFunctionAlgebra": "NativeFiniteFunctionTest",
    "FiniteCategoryAlgebra": "NativeCategoryTest",
    "FiniteFunctorAlgebra": "NativeFunctorTest",
    "FiniteNaturalTransformationAlgebra": "NativeNaturalTransformationTest",
    "FiniteEquivalenceAlgebra": "NativeEquivalenceTest",
    "FiniteAdjunctionAlgebra": "NativeAdjunctionTest",
    "FiniteConeAlgebra": "NativeConeTest",
    "FiniteCoconeAlgebra": "NativeCoconeTest",
    "SymmetricGroup": "NativePermutationTest",
    "ResidueRing": "NativeResidueRingTest",
}
CONDITIONS = {
    "divide": "The divisor must be nonzero.",
    "divide-rational": "The divisor must be nonzero.",
    "quotient": "The divisor must be nonzero; the quotient truncates toward zero.",
    "remainder": "The divisor must be nonzero; the remainder is a - truncate(a/b)*b.",
    "quotient-remainder": "The divisor must be nonzero; return quotient then signed remainder.",
    "inverse": "The value must be nonzero, or the square matrix must be nonsingular.",
    "solve": "The matrix must be nonsingular; singular systems, even consistent ones, are outside this operation.",
    "mean": "The sample must be nonempty.",
    "center": "The sample must be nonempty.",
    "population-variance": "The sample must be nonempty; denominator n.",
    "sample-variance": "The sample must contain at least two observations; denominator n - 1.",
    "population-covariance": "Samples must have equal positive lengths; denominator n.",
    "sample-covariance": "Samples must have equal lengths of at least two; denominator n - 1.",
    "condition": "The event must have strictly positive probability.",
    "complement-in": "The first set must be a subset of the second, the explicit universe.",
    "derivative-order": "The derivative order is a nonnegative BigInteger; orders above degree yield the zero polynomial.",
    "betti-number": "The degree is a nonnegative BigInteger; degrees above the complex dimension yield zero. Coefficients are F2.",
    "skeleton": "The degree is nonnegative; higher degrees return the same complex.",
    "simplex-count": "The degree is nonnegative; higher degrees yield zero.",
    "argmin": "The finite feasible set must be nonempty; every tied minimizer is retained.",
    "argmax": "The finite feasible set must be nonempty; every tied maximizer is retained.",
    "minimum": "The finite feasible set must be nonempty; the result is its exact minimum objective value.",
    "maximum": "The finite feasible set must be nonempty; the result is its exact maximum objective value.",
    "minimizers": "The finite feasible set must be nonempty; emit every tied minimizer in set iteration order.",
    "maximizers": "The finite feasible set must be nonempty; emit every tied maximizer in set iteration order.",
    "iterate": "The initial value is rational and the iteration count nonnegative; zero steps return the initial value.",
    "orbit": "Return the initial rational value followed by each iterate, preserving order and repeated states.",
    "compose": "Apply the first relation, then the second; middle Algebra instances must be identical.",
    "transitive-closure": "Include positive-length paths; do not add reflexive pairs except when a cycle implies them.",
    "is-function-on": "Exactly one result for each member of the supplied finite carrier and no relation pairs outside it.",
    "identity-on": "Identity pairs are created only on the explicit finite set, not on all integers.",
}


OWNER_CONDITIONS = {
    "PresentedAbelianGroupAlgebra": {
        "from-matrix": "An m by n integer matrix presents Z^m modulo the subgroup generated by its columns. Retain the relation matrix and a Smith-coordinate map U, where U*A*V=D.",
        "from-type": "Construct a diagonal presentation with nontrivial torsion generators first and free generators last. The total generator count must fit the implementation dimension cap; oversized ranks are implementation failures.",
        "relation-matrix": "Return the retained column relation matrix, including redundant or zero relations and empty dimensions.",
        "as-type": "Forget the chosen presentation and return its canonical abelian-group isomorphism type.",
        "generator-count": "Return the number of original generators, equal to the relation matrix row count; this need not be the minimal number of generators.",
        "relation-count": "Return the number of supplied relation columns, including zero or redundant relations.",
        "is-finite": "Test whether the classified free rank is zero.",
        "order": "The presented group must be finite; return its cardinality, including one for the trivial group. Infinite order is not encoded by a natural-number sentinel.",
        "equal": "Compare the retained relation matrix and stored Smith-coordinate map, not merely group isomorphism types.",
        "isomorphic": "Compare canonical free ranks and torsion factors. This decides group isomorphism in this scope without constructing an isomorphism between the presentations.",
        "direct-sum": "Build the block-diagonal relation matrix, with the first presentation's generators and relations preceding the second. The combined shape remains subject to the implementation cap.",
        "trivial": "The zero-generator, zero-relation presentation gives the trivial group.",
    },
    "AbelianGroupElementAlgebra": {
        "add": "Both elements must retain the same presentation and Smith-coordinate map. Add and reduce finite coordinates; isomorphic groups are not implicitly identified.",
        "subtract": "Both presentations and coordinate maps must agree; subtract and normalize within that group.",
        "negate": "Return the additive inverse in the same presented group.",
        "scale": "Multiply by any integer, including negative and zero scalars. Return the first element carrier's wrapper with its presentation retained.",
        "equal": "Equality includes the presentation, stored coordinate map and every canonical Smith coordinate; elements of different presentations compare unequal.",
        "group": "Return the element's retained presented group.",
        "smith-coordinates": "Return the full canonical vector in the retained Smith basis: residues in [0,d_i) for nonzero diagonal factors, zero at unit factors, and arbitrary integers in free coordinates.",
        "representative": "Return one original-generator coordinate vector obtained by solving U*x=canonical Smith coordinates. Reprojection recovers the element; the lift is not generally additive and is not a minimum-norm representative.",
        "is-zero": "All canonical Smith coordinates are zero.",
        "is-torsion": "All free coordinates are zero. Torsion elements may belong to an infinite presented group; the zero element is torsion.",
        "order": "The element must be torsion, even if its ambient group is infinite. Return the lcm of d_i/gcd(d_i,a_i), including order one for zero; infinite order is undefined in N.",
        "zero-like": "Return the additive identity of the same presentation.",
        "cyclic-subgroup": "The element has finite order. Emit zero, a, 2*a, ... up to but excluding order(a)*a, with no repeated endpoint. This enumerates its finite cyclic subgroup, subject to the output cap.",
        "multiplication-preimages": "Emit all x with n*x=a for the integer second operand n. Nonzero n has finite fibers, possibly empty. For n=0, a nonzero target gives an empty list; a zero target requires a finite ambient group. Infinite fibers are undefined, while oversized finite fibers raise IMPLEMENTATION_FAILURE. Enumeration uses mixed-radix Smith coordinates with the first coordinate varying fastest; return first-carrier wrappers.",
        "project": "The vector dimension equals the original generator count. Apply the retained map U, reduce finite Smith coordinates and return an element of this quotient. Every relation column projects to zero.",
        "from-smith": "The vector has the full original generator count but is interpreted in the retained Smith basis, then normalized. Original-generator and Smith-coordinate inputs are different conventions.",
        "zero": "Construct the additive identity in the supplied presented group.",
        "generators": "Emit the projected original coordinate basis in generator order, retaining zero or redundant generators and duplicates.",
        "smith-generators": "Emit the unit vectors in nonunit finite and free Smith coordinates, in coordinate order. Killed coordinates are omitted; this is a minimal generating list.",
        "elements": "The supplied group is finite. Emit every canonical element once, beginning with zero, in mixed-radix Smith order with the first coordinate varying fastest. The trivial group emits one element, not an empty list.",
        "reduce": "The vector dimension matches the original generator count. Return its canonical Smith coordinates in the actual second Vec(Z) carrier's IAlgebraItem wrapper; this output is not an original-coordinate representative.",
    },
    "IntegerVectorFamily": {
        "add": "Vector dimensions must agree; add integer coordinates.",
        "subtract": "Vector dimensions must agree; subtract the second vector from the first.",
        "negate": "Negate every coordinate, retaining dimension.",
        "scale": "Multiply coordinates by the integer second operand; return the first vector carrier's wrapper.",
        "dot": "Dimensions must agree. Use the standard integer coordinate pairing; the empty pairing is zero.",
        "dimension": "Return the retained nonnegative coordinate count.",
        "entries": "Emit integer coordinates in order, preserving duplicates and the empty list.",
        "zero-like": "Return the zero vector of the same dimension.",
        "equal": "Compare dimensions and every exact coordinate.",
        "to-rational": "Embed coordinates in the actual Vec(Q) carrier; dimension zero is supported.",
        "from-rational": "Every rational coordinate must have denominator one; rounding is not performed.",
        "empty": "Return the unique vector in dimension zero.",
    },
    "IntegerMatrixFamily": {
        "add": "Both row and column dimensions must agree; add entries over Z.",
        "subtract": "Both row and column dimensions must agree; subtract the second matrix from the first.",
        "multiply": "The first column count equals the second row count. Composition applies the second matrix first; zero inner dimensions retain the outer shape.",
        "negate": "Negate entries while retaining both dimensions.",
        "transpose": "Exchange row and column dimensions, including empty dimensions.",
        "scale": "Scale every entry by the integer second operand and return the first matrix wrapper.",
        "apply": "The vector dimension equals the matrix column count. Return the actual second Vec(Z) carrier's wrapper, with dimension equal to the row count.",
        "equal": "Compare both retained dimensions and all entries; distinct empty shapes remain different.",
        "row-count": "Return the retained number of rows, including zero.",
        "column-count": "Return the retained number of columns, including zero.",
        "rows": "Emit one Vec(Z) row per row index; an m by 0 matrix emits m empty vectors.",
        "columns": "Emit one Vec(Z) column per column index; a 0 by n matrix emits n empty vectors.",
        "zero-like": "Return the zero matrix of the same retained shape.",
        "identity-on-domain": "Return the identity on Z^columns, including the 0 by 0 identity.",
        "identity-on-codomain": "Return the identity on Z^rows, including the 0 by 0 identity.",
        "smith-invariant-factors": "Emit positive nonzero Smith factors, including ones, each dividing the next. Zero diagonal entries are omitted.",
        "smith-form": "Return D with the same rectangular shape and positive nonzero Smith factors followed by zero diagonal entries.",
        "smith-decomposition": "Emit exactly [U,D,V] with U*A*V=D; U and V are square unimodular integer matrices on the codomain and domain. D is canonical, while the witnesses are algorithm-selected and need not be canonical.",
        "kernel-basis": "Emit the trailing columns of the Smith right witness V. They form a basis of the whole integer kernel lattice, not merely a rational nullspace basis; an injective map emits an empty list.",
        "image-basis": "Emit A times the first rank(A) columns of V. This is a basis of the actual image lattice, preserving its index in its rational span; it is not the saturation.",
        "cokernel": "Classify Z^rows/image(A) in the actual AbelianGroupType carrier. The free rank is rows minus rank(A), and nonunit Smith factors give torsion; matrix columns are relations.",
        "rank": "Return the number of nonzero Smith factors, equal to rank over Q.",
        "nullity": "Return columns minus rank, the rank of the integer kernel.",
        "has-integer-solution": "The right-hand side has one coordinate per row. Test divisibility of U*b by the nonzero Smith factors and zero residual coordinates. Resource exhaustion raises IMPLEMENTATION_FAILURE, never false.",
        "solve-particular": "The right-hand side matches the rows and an integer solution must exist. Set free Smith coordinates to zero and return x=V*y in the second Vec(Z) carrier; this is not a minimum-norm solution.",
        "solve-generators": "The right-hand side matches the rows and an integer solution must exist. Emit a particular solution first, then an integral kernel basis; all solutions are x0 plus arbitrary integer combinations of the remaining vectors. This finite list parametrizes solutions rather than enumerating them.",
        "inverse-unimodular": "The matrix is square and all Smith factors are one with full rank, equivalently determinant is plus or minus one. Return V*U; the empty square matrix is invertible. Rational invertibility alone is insufficient.",
        "to-rational": "Both dimensions must be positive because the current Mat(Q) carrier excludes empty shapes. Embed entries exactly in the actual rational matrix carrier.",
        "from-rational": "Every entry must have denominator one; preserve both dimensions. Rounding is not performed.",
    },
    "AbelianGroupTypeAlgebra": {
        "direct-sum": "Return the isomorphism type of the finite direct sum, combining free ranks and canonically normalizing cyclic factors.",
        "tensor-product": "Tensor over Z. Free ranks multiply; free-torsion pairs contribute repeated cyclic factors and torsion pairs contribute gcd factors.",
        "hom-group": "Return the abelian-group isomorphism type Hom_Z(first,second), with source first and target second. Individual homomorphisms and chosen generators are not returned.",
        "tor1": "Return Tor_1^Z(first,second); free summands contribute zero and pairs of cyclic torsion orders contribute their gcd.",
        "ext1": "Return Ext^1_Z(first,second), with source first and target second. A cyclic source of order n contributes target/n*target; free source summands contribute zero. No extension witness is constructed.",
        "equal": "Compare canonical free rank and torsion factors; this is isomorphism-type equality, not equality of chosen presentations.",
        "free-rank": "Return the nonnegative arbitrary-precision rank of the free direct summand.",
        "torsion-factor-count": "Return the number of nontrivial canonical cyclic torsion invariant factors.",
        "minimal-generators": "Return free rank plus the number of nontrivial invariant factors, the minimal number of group generators.",
        "is-finite": "A finitely generated abelian group is finite exactly when its free rank is zero.",
        "is-torsion-free": "The torsion factor list is empty; this includes the trivial group.",
        "is-cyclic": "Accept a finite group with at most one torsion factor, or Z. The trivial group is cyclic.",
        "is-trivial": "Both the free rank and the torsion factor count are zero.",
        "order": "The free rank must be zero. Return the product of torsion factors; the trivial group has order one. Infinite order is undefined in N.",
        "exponent": "The free rank must be zero. Return the largest invariant factor, or one for the trivial group; no finite exponent exists for a nonzero free summand.",
        "torsion-part": "Retain the torsion invariant factors and set free rank to zero.",
        "free-part": "Retain the free rank and discard torsion factors, representing the quotient by torsion up to isomorphism.",
        "invariant-factors": "Emit nontrivial cyclic torsion orders, each greater than one and dividing the next. Free rank is separate; a torsion-free group emits an empty list.",
        "repeat": "Take the direct sum of the supplied natural number of copies; zero copies give the trivial group. Return the first group carrier's wrapper.",
        "free-on": "Construct Z^n for the supplied natural number n; zero gives the trivial group.",
        "cyclic": "Construct Z/nZ for n in N, with n=0 denoting Z and n=1 the trivial group.",
        "zero": "The direct-sum identity is the trivial group, with rank zero and no torsion factors.",
        "one": "The tensor-product identity is Z, with rank one and no torsion factors.",
    },
    "FiniteSimplicialAlgebra": {
        "boundary-matrix": "Return the oriented integer boundary from degree k to k-1 in Mat(Z). Bases are lexicographically ordered increasing-vertex simplices. Degree zero is 0 by vertex-count; dimension+1 retains the top-degree row count and zero columns; higher degrees are 0 by 0. Adjacent boundaries multiply to zero.",
        "integral-homology": "Return unreduced H_k(-;Z) as a canonical abelian-group isomorphism type. The natural degree may be arbitrarily large; degrees above the complex dimension give the trivial group. Boundaries use increasing vertex orientation.",
        "integral-homology-groups": "Emit unreduced integral homology types in degrees zero through the complex dimension, including trivial groups. The empty complex emits an empty list; all degrees share one Smith-reduction budget.",
        "rational-betti-number": "Return the integral free rank, equal to dim_Q H_k(-;Q). This differs from the existing F2 Betti number when torsion contributes; above-dimension degrees give zero.",
        "rational-betti-numbers": "Emit unreduced rational Betti numbers in ascending degree order; these are the integral free ranks. The empty complex emits an empty list.",
        "boundary-invariant-factors": "Emit positive nonzero Smith factors of the oriented integral boundary from degree k to k-1, including unit factors. Degree zero and above-dimension boundaries emit an empty list; these are not just homology torsion factors.",
    },
    "FiniteMarkovAlgebra": {
        "compose": "Apply the second kernel first and then the first kernel. The declared middle finite state sets and actual outcome Algebra must agree exactly; unused codomain labels still participate in typing. Transition matrices multiply P_second * P_first.",
        "domain": "Return the complete declared source set in increasing integer-label order, including states with no incoming transitions.",
        "codomain": "Return the complete declared target set in increasing integer-label order, including zero-probability targets.",
        "domain-size": "Return the number of declared source states, including zero for an empty kernel.",
        "codomain-size": "Return the number of declared target states; an empty source may have a nonempty target.",
        "is-chain": "Test equality of the declared source and target finite sets, including the empty endokernel.",
        "equal": "Compare actual outcome Algebra identity, both declared boundaries and every exact row probability, independently of input insertion order.",
        "row": "The argument belongs to the declared source set. Return its conditional probability distribution on the target states in the existing FiniteDistribution(Z) carrier.",
        "rows": "Emit all conditional row distributions in increasing source-label order; identical distributions in different rows are retained.",
        "apply": "The distribution uses the kernel's outcome Algebra and has support inside its domain. Return pi*P in the second operand's FiniteDistribution(Z) IAlgebraItem wrapper, summing coincident contributions exactly.",
        "transition-probability": "The first state is in the source and the second state in the target. Return the exact transition probability, including zero for an absent supported edge.",
        "to-matrix": "Both boundary sizes are positive. Emit the row-stochastic matrix whose rows and columns follow increasing source and target labels; the current matrix carrier cannot hold zero-sized shapes.",
        "from-matrix": "Matrix shape equals the two boundary sizes, with labels sorted independently. Every entry is nonnegative and every row sums exactly to one; invalid stochastic data make the operation undefined.",
        "from-function": "Embed a total finite integer function as Dirac row measures, retaining its complete domain and codomain.",
        "to-function": "Every row must be a Dirac distribution. Return the total finite function with exactly the same declared boundaries; an empty source is permitted.",
        "is-deterministic": "Every conditional row has support size one; true vacuously for an empty source.",
        "identity-on": "Build a kernel with one Dirac self-transition at each supplied integer state; the empty set gives the empty identity.",
        "identity-on-domain": "Build the identity kernel on the complete declared source set.",
        "identity-on-codomain": "Build the identity kernel on the complete declared target set.",
        "power": "Source equals target and exponent is nonnegative. Return the exact n-step kernel; exponent zero gives the identity on all declared states. Composition shares one work budget across the whole exponentiation.",
        "orbit": "Source equals target, the initial distribution is supported there, and step count is nonnegative. Emit the initial law followed by each successive time marginal, retaining repetitions; these are distributions, not random sample paths. Preflight the whole orbit against one aggregate work budget.",
        "communicating-classes": "Source equals target. Emit strongly connected components of the positive-transition graph, ordered by their least integer label; a zero-length path supplies self-reachability.",
        "recurrent-classes": "Source equals target. Emit closed communicating classes, with no positive transition leaving the class, ordered by their least label.",
        "transient-states": "Source equals target. Return all declared states outside the closed communicating classes.",
        "absorbing-states": "Source equals target. Return precisely states whose self-transition probability is one.",
        "is-irreducible": "Source equals target. Return whether there is exactly one communicating class; false for the empty state space.",
        "stationary-extremes": "Source equals target. Emit one normalized exact stationary distribution per recurrent class, in class order. Every rational stationary law is a rational convex mixture of these extremes; this does not enumerate all stationary laws or assert convergence of powers. Empty state space emits no laws.",
        "stationary": "Source equals target and there is exactly one recurrent class. Return its unique stationary law, which may vanish at transient states. Undefined for the empty chain or multiple stationary extremes.",
        "is-stationary": "Source equals target and the distribution is supported there with matching outcome Algebra. Test the exact equality pi*P=pi.",
        "reverse": "Source equals target. The supplied distribution is stationary and strictly positive at every declared state. Return Q_ij=pi_j*P_ji/pi_i; rows at zero-mass states are deliberately undefined.",
        "is-reversible": "Source equals target and the distribution is supported there. Test detailed balance pi_i*P_ij=pi_j*P_ji for every pair, allowing zero masses; detailed balance implies stationarity.",
        "absorbing-on": "Source equals target and the event is a subset of the state space. Replace event-state rows by Dirac self-transitions, retaining all boundaries and other rows.",
        "hitting-probabilities": "Source equals target and the event is a subset of the state space. Return the eventual first-hit probability from each state in increasing label order. First hitting includes time zero; target entries are one and unreachable entries zero. Output is a vector of probabilities, not a normalized distribution.",
        "mean-hitting-times": "Source equals target and the event is a subset of the state space. Return exact mean first-hit times including time zero, ordered by state label. Undefined if any state has infinite expectation, since Q cannot represent infinity; the empty chain returns an empty vector.",
    },
    "PolynomialCellAlgebra": {
        "product": "Concatenate ambient coordinates and parameter coordinates, putting first-cell parameters first; this fixes product orientation and includes point factors.",
        "dimension": "Return parameter dimension, including zero for a point; this is not the rank or geometric image dimension.",
        "ambient-dimension": "Return the positive target coordinate dimension.",
        "from-map": "Restrict the exact polynomial map to the positively oriented real unit cube in its input coordinates; no injectivity or full-rank condition is required.",
        "to-map": "The cell has positive parameter dimension; return its polynomial parametrization, whose algebraic domain extends beyond the restricted unit cube.",
        "from-point": "The rational vector has positive dimension; construct a zero-dimensional cell at that point.",
        "to-point": "The cell has parameter dimension zero; positive-dimensional constant parametrizations do not qualify.",
        "segment": "Endpoint dimensions are positive and equal; t maps to start+t*(end-start) on [0,1], retaining parameter dimension one for coincident endpoints.",
        "lower-face": "The zero-based axis is in the parameter space; set it to zero and remove it, retaining the other parameter order. A one-cell face is a point.",
        "upper-face": "The zero-based axis is in the parameter space; set it to one and remove it, retaining the other parameter order. A one-cell face is a point.",
        "faces": "Emit lower then upper faces for each increasing parameter axis, without boundary signs; duplicate faces are retained and a point emits an empty list.",
        "vertices": "Emit endpoint evaluations in increasing binary-mask order, with bit i choosing parameter i. Repeated points are retained; a point emits itself once.",
        "evaluate": "Supply exactly one rational parameter per cell dimension, all in [0,1]; a point accepts the empty vector. Return a wrapper of the actual second operand Vec(Q) carrier with ambient output dimension.",
        "pushforward": "The polynomial map input dimension equals cell ambient dimension; compose it after the cell parametrization, or evaluate it at a point cell.",
        "integrate": "The form ambient dimension agrees and every nonzero form component has the cell degree. Integrate its pullback exactly over the oriented unit cube; no absolute Jacobian is taken. A point evaluates a zero-form.",
        "equal": "Compare canonical parametrizations or point coordinates, including parameter and ambient dimensions; geometric images, reversed parametrizations and homology classes are not identified.",
    },
    "PolynomialChainAlgebra": {
        "add": "Ambient dimensions and degrees agree; combine rational coefficients of identical polynomial cells and discard zero coefficients.",
        "subtract": "Ambient dimensions and degrees agree; subtract coefficients of identical cells.",
        "negate": "Negate all chain coefficients and retain ambient dimension and degree.",
        "scale": "Scale rational chain coefficients, retaining ambient dimension and degree even when the result is zero.",
        "product": "Distribute the oriented cell product bilinearly; ambient dimensions and chain degrees add, including zero chains in negative degrees.",
        "boundary": "Lower degree by one and sum (-1)^i*(upper_i-lower_i) with each cell coefficient. Point boundaries are zero in degree -1; zero chains retain all negative integer degrees.",
        "pushforward": "Map input dimension equals chain ambient dimension. Compose every cell with the map using a shared work budget and combine identical resulting cells; degree is retained.",
        "integrate": "Form ambient dimension and homogeneous degree match the chain, even for a zero chain. Sum coefficient-weighted oriented cell integrals with one shared work budget; only the zero form can pair with a negative-degree zero chain.",
        "ambient-dimension": "Return the retained positive ambient coordinate dimension.",
        "degree": "Return the retained integer chain degree in Z; every negative degree contains only the zero chain.",
        "is-zero": "The canonical cell support is empty.",
        "cell-count": "Count nonzero canonical cell coefficients, without counting repeated vertices or monomials inside parametrizations.",
        "cells": "Emit occupied cells in canonical structural order, matching the coefficient list order.",
        "coefficients": "Emit nonzero rational coefficients in canonical cell order, retaining repeated coefficients.",
        "coefficient": "The cell has the declared ambient dimension and degree; return its stored coefficient, or zero if absent.",
        "equal": "Compare ambient dimension, integer degree and canonical rational cell coefficients, including for zero chains.",
        "from-cell": "Give the cell rational coefficient one in its ambient dimension and parameter degree.",
        "boundary-of-cell": "Transfer the oriented cubical boundary to the actual PolynomialChain(Q) carrier, combining coincident faces with signed coefficients.",
        "zero-like": "Return the zero chain in the same ambient dimension and integer degree.",
    },
    "PolynomialDifferentialFormAlgebra": {
        "integrate-unit-cube": "Only top-degree components in the form coordinate dimension may be nonzero. Integrate the top coefficient over the positively oriented real unit cube exactly; the zero form integrates to zero.",
        "add": "Coordinate dimensions agree; add coefficients at matching differential basis masks and remove zeros.",
        "subtract": "Coordinate dimensions agree; subtract coefficients at matching differential basis masks and remove zeros.",
        "wedge": "Coordinate dimensions agree. Repeated differentials vanish; increasing coordinate order fixes signs. Polynomial products and basis-pair visits share one work budget.",
        "negate": "Negate each polynomial coefficient, retaining the coordinate dimension.",
        "scale": "Multiply each polynomial coefficient by the rational scalar.",
        "multiply-polynomial": "The scalar polynomial has the form coordinate dimension; multiply every coefficient with a shared work budget.",
        "exterior-derivative": "For each coefficient f of dx_I, sum partial_j(f)*dx_j wedge dx_I. The degree increases by one and d squared is zero, including mixed-degree forms.",
        "pullback": "For F:Q^m -> Q^n and a form on Q^n, substitute f(F) in coefficients and replace dx_i by dF_i, yielding a form on Q^m. Pullback is contravariant, preserves wedge and commutes with d; all substitutions and wedges share one work budget.",
        "interior": "The polynomial vector field maps the form coordinate space to itself. Use left insertion with the natural covector-vector pairing and one shared coefficient-product budget.",
        "lie-derivative": "The polynomial vector field maps the form coordinate space to itself. Use Cartan's formula L_X=i_X*d+d*i_X; both insertions share a work budget.",
        "hodge-star": "Use the standard oriented Euclidean coframe, preserving polynomial coefficients and taking signed complementary differential basis masks.",
        "grade-involution": "Multiply degree-k components by (-1)^k; this supplies the graded product rule even for mixed-degree forms.",
        "ambient-dimension": "Return the retained positive coordinate dimension, including for zero and scalar forms.",
        "basis-count": "Count occupied differential basis masks, not scalar polynomial monomials.",
        "coefficient-term-count": "Sum the nonzero monomial counts of all polynomial coefficients; the representation cap applies to this total.",
        "degrees": "Emit occupied differential degrees in increasing order; zero has no occupied degrees.",
        "terms": "Emit one wrapped form per occupied differential basis mask in increasing order, retaining its whole polynomial coefficient.",
        "coefficients": "Emit the nonzero polynomial coefficients in increasing differential-mask order, retaining duplicates.",
        "basis-masks": "Emit occupied masks in ascending order; bit i means dx_i and mask zero means the scalar basis.",
        "grade": "Keep the requested nonnegative differential degree; every degree above the coordinate dimension returns zero.",
        "evaluate": "The point dimension agrees; evaluate polynomial coefficients and identify the standard coordinate coframe with the registered Exterior(Q) basis, without claiming a general tangent/cotangent identification.",
        "equal": "Compare coordinate dimensions and all canonical polynomial coefficients, including for constants and zero.",
        "is-zero": "Zero is exactly empty canonical differential support.",
        "scalar-part": "Return the degree-zero polynomial coefficient, or zero in the same coordinate dimension.",
        "to-polynomial": "All positive-degree components vanish; return the scalar polynomial in the same coordinate dimension.",
        "from-polynomial": "Embed the scalar polynomial as a differential zero-form, retaining its input dimension.",
        "from-exterior": "The exterior dimension is positive; use the standard coordinate basis identification to create constant polynomial form coefficients.",
        "to-exterior": "Every coefficient is a constant polynomial; use the standard coordinate coframe identification with the registered exterior basis.",
        "from-vector-field": "Input and output dimensions agree; lower the polynomial vector field to a one-form with the standard Euclidean metric.",
        "to-vector-field": "Only degree-one components are present; raise coefficients to a square polynomial vector field with the standard Euclidean metric. Zero maps to the zero field.",
        "zero-like": "Return the zero form in the same coordinate dimension.",
        "one-like": "Return the constant scalar unit form in the same coordinate dimension.",
        "volume-like": "Return dx_0 wedge ... wedge dx_(n-1) with constant unit coefficient and positive coordinate orientation.",
        "basis": "Emit dx_0 through dx_(n-1) as wrapped one-forms in coordinate order.",
    },
    "RationalMultivariatePolynomialAlgebra": {
        "integrate-unit-cube": "Integrate over the real unit cube in all declared variables by summing coefficient/prod_i(exponent_i+1); return an exact rational.",
        "add": "Both polynomials retain the same input dimension; add coefficients and discard zeros.",
        "subtract": "Both polynomials retain the same input dimension; subtract coefficients and discard zeros.",
        "multiply": "Input dimensions agree. Multiply sparse monomials and combine equal exponent tuples within the expansion budget.",
        "negate": "Negate every coefficient and retain the declared input dimension.",
        "scale": "Multiply every coefficient by the rational scalar; zero retains its input dimension.",
        "variable-count": "Return the declared input dimension, including for constant and zero polynomials.",
        "degree": "Return total degree; the zero polynomial has degree -1, in Z rather than N.",
        "term-count": "Return the number of nonzero canonical monomial coefficients.",
        "is-zero": "Zero is exactly empty canonical support in the retained input dimension.",
        "equal": "Compare input dimensions and all canonical coefficients; constants in different input spaces remain distinct.",
        "evaluate": "The rational point has exactly one coordinate per declared variable; return the exact rational value.",
        "partial": "The zero-based variable index is smaller than the declared input dimension; differentiate in that coordinate.",
        "partials": "Emit one wrapped partial derivative per variable in coordinate order, retaining zero and repeated derivatives.",
        "directional": "The constant direction has the declared input dimension. Return the polynomial sum v_i*partial_i(f); the direction is not normalized.",
        "gradient-at": "The point has the declared input dimension; return ordered partial values in a wrapper of the second operand's Vec(Q) carrier.",
        "hessian-at": "The point has the declared input dimension; return the exact symmetric matrix of second partials, with rows and columns in variable order.",
        "laplacian": "Sum the unmixed second partials in standard Euclidean coordinates; this equals the trace of the Hessian at each point.",
        "primitive": "The variable index is in range. Integrate with respect to that variable, choosing the entire variable-independent polynomial to be zero.",
        "pow": "The exponent belongs to N; the zeroth power, including 0^0 in the polynomial ring, is the unit in the same input dimension.",
        "terms": "Emit the nonzero monomials with coefficients in ascending lexicographic exponent-tuple order; zero emits an empty list.",
        "coefficients": "Emit nonzero coefficients in ascending lexicographic exponent-tuple order, preserving repeated coefficients.",
        "zero-like": "Return the additive zero in the input polynomial's declared space.",
        "one-like": "Return the multiplicative unit in the input polynomial's declared space.",
        "from-univariate": "Transfer Q[x] to a polynomial with exactly one variable, preserving exact coefficients.",
        "to-univariate": "Exactly one variable must be declared, even for constants; preserve coefficients in Q[x].",
        "constant-part": "Return the coefficient of the all-zero exponent tuple, or zero if absent.",
        "to-rational": "All nonconstant coefficients vanish; forget the input dimension and return the rational constant.",
        "variables": "Emit the coordinate polynomials x_0 through x_(n-1) in variable order, in the same input space.",
    },
    "RationalPolynomialMapAlgebra": {
        "add": "Both input and output dimensions agree; add corresponding components.",
        "subtract": "Both input and output dimensions agree; subtract corresponding components.",
        "negate": "Negate every component, retaining input and output dimensions.",
        "scale": "Scale every component by the rational scalar, retaining both dimensions.",
        "compose": "Return F(G(x)), applying the right operand first; inner output dimension equals outer input dimension. All components share one substitution work budget.",
        "evaluate": "The point dimension equals the map input dimension; return all component values in the second operand's Vec(Q) carrier, with the map output dimension.",
        "partial": "The zero-based variable index is in the map's input space; differentiate each component in that variable.",
        "components": "Emit all wrapped component polynomials in output order, preserving zero and repeated components.",
        "component": "The zero-based index is in the map's output space; return that wrapped scalar polynomial.",
        "input-dimension": "Return the retained positive number of input variables.",
        "output-dimension": "Return the retained positive number of scalar components.",
        "jacobian-at": "The point has the input dimension. Matrix rows index output components and columns index input variables; this is the total derivative at the point.",
        "divergence": "Input and output dimensions agree; return the sum of component i differentiated in variable i, the Jacobian trace.",
        "curl": "Both dimensions are three. Return (partial_y F_z-partial_z F_y, partial_z F_x-partial_x F_z, partial_x F_y-partial_y F_x) in the standard right-handed orientation.",
        "from-polynomial": "Make a one-output map, retaining the polynomial input dimension.",
        "to-polynomial": "Exactly one output component is required; return that component with its input dimension.",
        "gradient": "Transfer a scalar polynomial to the ordered map of its partial derivatives; its Jacobian is the polynomial Hessian.",
        "identity-on-input": "Return the identity map on the input space, independent of the original output dimension.",
        "from-matrix": "Interpret the rational matrix as a linear column-vector map; input dimension is column count and output dimension is row count.",
        "linear-part": "Return the homogeneous degree-one coefficient matrix, equivalently the Jacobian at the origin; nonlinear terms are not preserved.",
        "constant-part": "Return the vector of constant coefficients, equivalently evaluation at the origin.",
        "equal": "Compare both declared dimensions and every ordered canonical scalar polynomial.",
        "substitute": "There is one inner map component per scalar polynomial variable; substitute simultaneously and return a wrapped polynomial on the inner input space.",
    },
    "RationalQuaternionAlgebra": {
        "multiply": "Hamilton product in operand order; i*j=k and j*i=-k. Multiplication is associative but noncommutative.",
        "divide-right": "The second quaternion is nonzero; return a*b^-1, the solution x to x*b=a.",
        "divide-left": "The second quaternion is nonzero; return b^-1*a, the solution x to b*x=a.",
        "conjugate": "Negate the three imaginary coefficients; conjugation reverses multiplication order.",
        "inverse": "The quaternion is nonzero; return its conjugate divided by the sum of squared rational components.",
        "norm-squared": "Return w*w+x*x+y*y+z*z as a nonnegative exact rational. Its square root is not generally rational and is not returned.",
        "real-part": "Return the coefficient of the scalar unit.",
        "imaginary-part": "Return the i,j,k coefficients as a three-dimensional Vec(Q) member, without requiring the real part to vanish.",
        "components": "Emit all four rational coefficients in scalar,i,j,k order, preserving zeros and duplicates.",
        "scale": "Multiply all coefficients by the rational scalar.",
        "equal": "Compare all four exact coefficients; scalar multiples generally differ as algebra elements.",
        "same-rotation": "Both quaternions are nonzero. They represent the same active rotation exactly when they differ by a nonzero rational scalar factor.",
        "embed-rational": "Embed q as (q,0,0,0), in the center of the quaternion algebra.",
        "to-rational": "All three imaginary coefficients vanish.",
        "embed-complex": "Embed the selected rational complex subfield Q(i) as (real,imaginary,0,0).",
        "to-complex": "The j and k coefficients vanish; return a member of the selected Q(i) subfield.",
        "from-vector": "The input vector has dimension three; embed it as a pure quaternion with zero real part.",
        "to-vector": "The real coefficient vanishes; return the three imaginary coordinates in Vec(Q).",
        "rotate": "The quaternion is nonzero and the vector has dimension three. Return the active right-handed column-vector rotation q*(0,v)*q^-1, wrapped in the second operand carrier. Unit normalization is unnecessary.",
        "to-rotation-matrix": "The quaternion is nonzero. Return its exact rational orthogonal 3 by 3 active rotation matrix with determinant one.",
        "from-rotation-matrix": "The matrix is exactly 3 by 3, orthogonal and of determinant one; no approximation or orthogonalization is applied. Return a rational projective quaternion with its first nonzero component equal to one, handling trace-minus-one half-turns separately.",
        "zero": "The additive zero quaternion.",
        "one": "The multiplicative unit quaternion.",
        "i": "The first imaginary basis generator, with i*i=-1.",
        "j": "The second imaginary basis generator, with j*j=-1.",
        "k": "The third imaginary basis generator, with k*k=-1 and i*j=k.",
    },
    "RationalExteriorAlgebra": {
        "add": "Both elements have the same ambient dimension; add coefficients and remove zero terms.",
        "subtract": "Both elements have the same ambient dimension; subtract coefficients and remove zero terms.",
        "wedge": "Both elements have the same ambient dimension. Repeated basis factors vanish; distinct factors are sorted with the permutation sign. The operation supports mixed grades and is subject to a sparse product work cap.",
        "scale": "Multiply every coefficient by the rational scalar, retaining ambient dimension even for zero.",
        "grade-involution": "Multiply each homogeneous grade k by (-1)^k.",
        "reverse": "Reverse wedge factor order, multiplying grade k by (-1)^(k*(k-1)/2); this is an algebra antiautomorphism.",
        "hodge-star": "Use the standard positive oriented orthonormal coordinate basis: e_I wedge star(e_I) is the volume element, and star squared on grade k is (-1)^(k*(n-k)).",
        "ambient-dimension": "Return the retained coordinate space dimension, including for zero and scalar elements.",
        "term-count": "Return the number of nonzero canonical basis coefficients.",
        "degrees": "Emit distinct occupied homogeneous degrees in ascending order; the zero element emits an empty list.",
        "terms": "Emit one wrapped nonzero monomial per basis mask in ascending mask order, with retained coefficient and ambient dimension.",
        "coefficients": "Emit nonzero rational coefficients in ascending basis-mask order, retaining repeated coefficients.",
        "basis-masks": "Emit occupied nonnegative basis masks in ascending order; bit i denotes e_i and mask zero denotes the scalar unit.",
        "grade": "The supplied degree is nonnegative. Retain only that degree; every degree above ambient dimension returns zero in the same ambient space.",
        "interior": "The vector dimension equals the exterior ambient dimension. Apply left insertion i_v, a degree-minus-one antiderivation, using the standard coordinate identification with the dual.",
        "dot": "The ambient dimensions agree. Pair identical increasing basis monomials as an orthonormal basis and distinct monomials as orthogonal.",
        "norm-squared": "Return the exact nonnegative sum of squared canonical coefficients, not a square root.",
        "equal": "Equality includes ambient dimension and all canonical nonzero coefficients; zeros in different dimensions are distinct.",
        "is-zero": "True exactly when the canonical coefficient map is empty.",
        "scalar-part": "Return the coefficient of the empty basis mask, or zero if it is absent.",
        "to-scalar": "All nonzero terms have degree zero; zero is accepted and maps to rational zero.",
        "to-vector": "All nonzero terms have degree one; zero maps to the zero vector of its retained ambient dimension.",
        "from-vector": "Embed a rational coordinate vector in grade one with the vector's ambient dimension; the empty vector gives the exterior zero in dimension zero.",
        "apply": "Matrix columns equal the exterior source dimension. The covariant induced map sends each basis generator to its matrix column and respects wedge products and scalars; the second-type wrapper has the matrix row count as its new ambient dimension.",
        "zero-in": "The natural-number input is the ambient dimension; construct its zero exterior element, subject to the representation dimension cap.",
        "one-in": "The natural-number input is the ambient dimension; construct the scalar wedge unit in that dimension, subject to the representation cap.",
        "volume-in": "The natural-number input is the ambient dimension; construct e_0 wedge ... wedge e_(n-1), with the empty wedge equal to one in dimension zero.",
    },
    "RationalTensorAlgebra": {
        "add": "Both tensors have exactly the same ordered shape; add coordinatewise.",
        "subtract": "Both tensors have exactly the same ordered shape; subtract coordinatewise.",
        "hadamard": "Both tensors have exactly the same ordered shape; multiply coordinatewise, retaining shape.",
        "tensor-product": "Concatenate the first shape and second shape, multiplying entries in row-major order. Order-zero scalar tensors are allowed; the result remains subject to materialization caps.",
        "scale": "Multiply every rational entry by the scalar, retaining the full shape including zero axes.",
        "order": "Return the number of axes, not tensor decomposition rank or matrix rank; scalar tensors have order zero.",
        "size": "Return the product of axis dimensions; any zero axis makes size zero, while scalar tensors have one entry.",
        "shape": "Emit axis dimensions in their stored order, retaining repetitions and zeros; scalar tensors emit an empty list.",
        "entries": "Emit exact coordinates in row-major order with the final axis varying fastest; zero-sized tensors emit no entries.",
        "norm-squared": "Return the sum of squared rational coordinates in the standard coordinate inner product, not a square root.",
        "dot": "Both tensors have exactly the same ordered shape; sum coordinatewise products in the standard coordinate inner product.",
        "equal": "Equality includes ordered shape and every coordinate; a scalar and a singleton-axis tensor are distinct.",
        "swap-axes": "Both zero-based axis indices must exist. Exchange their dimensions and coordinates; equal indices leave the tensor unchanged.",
        "contract": "The zero-based axis indices must be distinct, present and equal-sized. Sum over equal coordinates of these axes and retain other axes in order; contracting two zero-sized axes yields zeros in the remaining shape.",
        "from-scalar": "Embed the rational as an order-zero tensor with exactly one entry.",
        "to-scalar": "The tensor must have order zero; a singleton-axis tensor is not a scalar tensor.",
        "from-vector": "Embed a finite rational vector as an order-one tensor, including an empty vector as shape [0].",
        "to-vector": "The tensor must have order one; retain the axis length, including zero.",
        "from-matrix": "Embed the positive rectangular matrix as an order-two tensor with row-major coordinates.",
        "to-matrix": "The tensor must have order two and both dimensions positive, matching the existing matrix representation.",
        "zero-like": "Replace every coordinate with zero, retaining the exact ordered shape.",
        "one": "The order-zero scalar tensor one is the identity for tensor product, not a matrix identity of every shape.",
    },
    "RationalVectorFamily": {
        "add": "Both vectors have the same dimension, including dimension zero.",
        "subtract": "Both vectors have the same dimension.",
        "dot": "Both vectors have the same dimension; the empty dot product is zero.",
        "scale": "Multiply every entry by the rational scalar, retaining dimension.",
        "dimension": "Return the stored nonnegative vector dimension.",
        "entries": "Emit entries in coordinate order, retaining repeated values; an empty vector emits no entries.",
        "zero-like": "Return the zero vector in the input's dimension.",
        "from-fixed": "Embed the existing fixed-dimensional vector carrier into the family without changing coordinates.",
        "to-fixed": "The vector dimension must match the configured fixed-dimensional target carrier.",
        "empty": "The unique zero-dimensional vector, not a zero vector of every dimension.",
    },
    "RationalMatrixFamily": {
        "add": "Both matrices have identical row and column counts.",
        "subtract": "Both matrices have identical row and column counts.",
        "multiply": "The first matrix's column count equals the second matrix's row count; the result retains the outer dimensions.",
        "transpose": "Swap the positive row and column dimensions.",
        "apply": "Vector dimension equals matrix column count; the wrapped result vector has the matrix's row count.",
        "rref": "Exact Gauss-Jordan elimination returns reduced row-echelon form with the original positive shape.",
        "rank": "Return the exact rational matrix rank.",
        "nullity": "Return column count minus rank.",
        "pivot-columns": "Emit zero-based pivot columns in ascending order.",
        "nullspace-basis": "Emit one canonical kernel vector per free column, setting that free coordinate to one and the other free coordinates to zero; full column rank emits no vectors.",
        "row-space-basis": "Emit the nonzero rows of the reduced row-echelon form, in pivot order.",
        "column-space-basis": "Emit the original matrix's pivot columns, not the reduced matrix's columns.",
        "rows": "Emit all row vectors in order, retaining zero and repeated rows.",
        "columns": "Emit all column vectors in order, retaining zero and repeated columns.",
        "from-fixed": "Embed the configured square matrix carrier into the family.",
        "to-fixed": "Both dimensions must match the configured fixed square matrix carrier.",
        "zero-like": "Return the zero matrix of exactly the input shape.",
        "inverse": "The matrix must be square and nonsingular.",
        "determinant": "The matrix must be square; singular matrices have determinant zero.",
        "trace": "The matrix must be square.",
        "equal": "Equality includes both dimensions and every exact rational entry.",
        "pseudoinverse": "Return the unique rational Moore-Penrose inverse with transposed shape, satisfying A*A+*A=A, A+*A*A+=A+ and symmetry of A*A+ and A+*A. Defined for every positive shape and rank, including zero matrices.",
        "column-projector": "Return A*A+, the symmetric idempotent orthogonal projector onto the column space, of shape row-count by row-count.",
        "row-projector": "Return A+*A, the symmetric idempotent orthogonal projector onto the row space, of shape column-count by column-count.",
        "project-column": "Point dimension equals row count. Return its closest point in the column space under the standard Euclidean inner product, retaining the row dimension.",
        "least-squares-minimum-norm": "Right-hand side dimension equals row count. Return A+*b, the unique least-squares minimizer of smallest Euclidean norm; singular and inconsistent systems are accepted.",
        "least-squares-residual": "Right-hand side dimension equals row count. Return b minus its fitted value A*A+*b, perpendicular to every matrix column.",
        "least-squares-error": "Right-hand side dimension equals row count. Return the exact nonnegative sum of squared residual coordinates, not its square root.",
    },
    "RationalAffineSpaceAlgebra": {
        "solve": "Right-hand side dimension equals the positive matrix row count. Inconsistent systems return an empty affine set; consistent singular and rectangular systems return a particular solution plus canonical kernel directions.",
        "particular": "The affine set is nonempty; return its canonical solution with every free coordinate zero.",
        "directions": "The affine set is nonempty; emit a finite nullspace basis in ascending free-column order. A unique solution emits no directions.",
        "dimension": "The affine set is nonempty; return the number of independent free rational parameters.",
        "ambient-dimension": "Return the original positive number of unknowns, including for empty solution sets.",
        "is-empty": "Check whether the original rational linear system is inconsistent.",
        "is-unique": "True exactly for a nonempty zero-dimensional affine set.",
        "contains": "Compare the point against the canonical parametrization; empty sets or vectors of the wrong ambient dimension return false.",
        "at": "The affine set is nonempty and the parameter vector has one entry per free variable. Return particular plus the corresponding linear combination of directions, wrapped in Vec(Q).",
        "equal": "Compare canonical solution sets within the same ambient dimension, independently of the input equation presentation.",
        "least-squares": "Right-hand side dimension equals matrix row count. Return all minimizers of the squared Euclidean residual as the nonempty affine solution set of A^T*A*x=A^T*b, including rank-deficient and inconsistent original systems.",
        "closest-point": "The affine set is nonempty and the point has its ambient dimension. Return the unique closest point in the standard Euclidean inner product; points already in the set are unchanged.",
        "minimum-norm": "The affine set is nonempty. Return its unique point of smallest Euclidean norm, which need not equal the free-zero particular point.",
    },
    "FiniteCoconeAlgebra": {
        "diagram": "Return the retained finite diagram J->C.",
        "vertex": "Return the target vertex of all cocone legs; retain it even for an empty shape.",
        "leg": "The argument is an existing shape object j; return the arrow F(j)->vertex in the integer algebra.",
        "leg-map": "Transfer to a finite function from every shape object to the full target-category arrow set.",
        "legs": "Emit leg labels in ascending shape-object order, retaining duplicates.",
        "natural-transformation": "Return the checked transformation from the retained diagram to the constant diagram at its vertex.",
        "is-colimit": "Exactly one commuting arrow must leave this cocone for every cocone over the same diagram. Resource exhaustion raises IMPLEMENTATION_FAILURE, never false.",
        "equal": "Equality includes the labelled diagram, all legs and the vertex even for an empty shape.",
        "descend": "The first cocone must satisfy the entire colimit property; the second has exactly the same diagram. Return the unique arrow from the first vertex to the second.",
        "mediators": "Both cocones have exactly the same diagram; emit all commuting arrows from the first vertex to the second in ascending label order, possibly none or several.",
        "reindex": "The functor targets the diagram shape; pull back the legs along its object map. The colimit property need not be preserved.",
        "map": "The functor starts at the diagram target category; map the vertex and legs. The colimit property need not be preserved.",
        "colimit": "Search the opposite diagram for a limit and return its dual cocone, choosing least vertex then lexicographically least leg labels. Exhaustion differs from mathematical nonexistence.",
        "cocones-at": "The integer is an existing target-category object; emit all cocones at it in lexicographic leg order. Exhaustion never returns a truncated list.",
        "from-transformation": "The transformation must end at the constant diagram on the explicit vertex; its source becomes the cocone diagram.",
        "opposite": "Return a cone over the opposite diagram, reversing all category arrows while retaining labels.",
        "opposite-cone": "Convert a cone to a cocone over its opposite diagram; this conversion is registered on the cone carrier.",
    },
    "FiniteConeAlgebra": {
        "diagram": "Return the retained finite functor J->C.",
        "vertex": "Return the cone vertex object label in C, retained even for an empty shape J.",
        "leg": "The argument must be an object of J; return its leg arrow label wrapped in the integer algebra.",
        "leg-map": "Transfer to a finite function from all shape objects to the entire target-category arrow set.",
        "legs": "Emit leg arrow labels in ascending shape-object order, retaining duplicate labels.",
        "natural-transformation": "Return the checked natural transformation from the constant diagram at the vertex to the retained diagram.",
        "is-limit": "Every cone over this exact diagram must have exactly one commuting mediator into this cone. Search exhaustion raises IMPLEMENTATION_FAILURE, never false.",
        "equal": "Equality includes the labelled diagram, vertex and every leg, including the vertex for empty shapes.",
        "lift": "The first operand must be a limit cone for the entire diagram; the second has the same exact diagram. Return its unique commuting arrow into the first vertex.",
        "mediators": "Both cones have the same exact diagram; emit all commuting arrows from the second vertex to the first in ascending arrow-label order, possibly none or several.",
        "reindex": "The additional functor targets the diagram shape. Pull the legs back along its object map; the limit property need not be preserved.",
        "map": "The additional functor starts at the target category. Map the vertex and legs; the limit property need not be preserved.",
        "limit": "Find a cone with exactly one mediator from every cone. Select least vertex then lexicographically least legs; nonexistence is OPERATION_UNDEFINED only after complete bounded search.",
        "cones-at": "The argument is an existing target-category object; enumerate every commuting cone there in lexicographic leg order. A valid empty list differs from exhausted search.",
        "from-transformation": "The source functor must be constant at the explicit vertex; the target functor becomes the cone diagram.",
    },
    "FiniteAdjunctionAlgebra": {
        "compose": "Compose the left functors with the right operand first and the right functors in reverse order; labelled middle categories must agree. Retain and combine the supplied witnesses.",
        "opposite": "Swap adjoint roles and reverse categories: L adjoint to R becomes R.op adjoint to L.op. The opposite counit is the new unit.",
        "source": "Return the source category of the left adjoint.",
        "target": "Return the target category of the left adjoint.",
        "left": "Return the retained left adjoint L:C->D.",
        "right": "Return the retained right adjoint R:D->C.",
        "unit": "Return the checked natural transformation Id_C -> R.L; it need not be invertible.",
        "counit": "Return the checked natural transformation L.R -> Id_D; it need not be invertible.",
        "is-equivalence": "Both retained unit and counit must be natural isomorphisms.",
        "to-equivalence": "Both retained unit and counit must be invertible; preserve all chosen functor and witness data.",
        "equal": "Equality includes both labelled functors and the chosen unit and counit.",
        "identity-on": "Construct identity functors and identity witnesses on the supplied finite category.",
        "from-equivalence": "Retain the equivalence's forward/backward functors and its unit and counit as an adjunction.",
        "from-left": "A right adjoint must exist: at every target object d choose the least pair (c,epsilon:L(c)->d) inducing bijections on all relevant hom sets; construct and validate the adjoint and both witnesses.",
        "from-right": "A left adjoint must exist; construct it by the dual universal-arrow search on opposite categories.",
        "transpose": "Input pair (c,h) supplies c in C and h:L(c)->d in D; return eta_c followed by R(h). The explicit c removes ambiguity when L identifies objects.",
        "untranspose": "Input pair (d,k) supplies d in D and k:c->R(d) in C; return L(k) followed by epsilon_d. The explicit d removes ambiguity when R identifies objects.",
        "hom-map": "Input pair (c,d) contains existing objects. Return the finite bijection Hom_D(L(c),d) -> Hom_C(c,R(d)), including an empty bijection when both hom sets are empty.",
    },
    "FiniteEquivalenceAlgebra": {
        "compose": "Apply the right operand first; labelled middle categories must agree. Compose the supplied quasi-inverses and witnesses without choosing new representatives.",
        "inverse": "Every validated equivalence can be reversed by swapping the functors and using the inverse counit and inverse unit as witnesses.",
        "opposite": "Reverse both categories and invert the opposite unit and counit to retain their required directions.",
        "source": "Return the source category of the forward functor.",
        "target": "Return the target category of the forward functor.",
        "forward": "Return the chosen forward functor.",
        "backward": "Return the chosen quasi-inverse functor, which need not be a strict inverse.",
        "unit": "Return the checked natural isomorphism Id_C -> G.F.",
        "counit": "Return the checked natural isomorphism F.G -> Id_D.",
        "equal": "Equality includes both functors and both chosen natural isomorphisms; equivalent categories alone do not determine equal witnesses.",
        "identity-on": "Construct the identity functor with identity unit and counit on the supplied category.",
        "from-functor": "The functor must be full, faithful and essentially surjective. Choose representatives deterministically, preferring exact image objects, then construct and validate both triangle identities.",
    },
    "FiniteNaturalTransformationAlgebra": {
        "compose": "Vertical composition beta.compose(alpha) applies alpha then beta; their middle functors must be equal.",
        "inverse": "Every component must be an isomorphism in the codomain category; return the reversed natural transformation.",
        "opposite": "Reverse both categories and the transformation direction: alpha:F=>G becomes alpha.op:G.op=>F.op.",
        "source": "Return the source functor, including both of its labelled categories and maps.",
        "target": "Return the target functor, including both of its labelled categories and maps.",
        "component": "The argument is an object of the common source category; return the component's codomain arrow label.",
        "is-isomorphism": "Check whether every component arrow is invertible; the empty transformation satisfies this vacuously.",
        "component-map": "Transfer to a finite integer function from source objects to the entire codomain arrow set.",
        "components": "Emit component arrow labels in ascending source-object order, preserving duplicates.",
        "component-fiber": "The codomain arrow label exists; emit source objects having that component in ascending order, possibly none.",
        "equal": "Equality includes both parallel functors and every component.",
        "identity-on": "Each component is the identity at the supplied functor's image object.",
        "precompose": "The additional functor targets the common source category; pull components back along its object map.",
        "postcompose": "The additional functor starts at the common codomain category; map components through its arrow map.",
        "horizontal": "For alpha:F=>G and beta:H=>K on adjacent categories, return H.F=>K.G with component H(alpha_x) followed by beta_(Gx).",
    },
    "FiniteFunctorAlgebra": {
        "constant-at": "The integer is an existing target object, even for an empty shape. Send every shape object to it and every arrow to its identity.",
        "empty-diagram": "Construct the unique functor from the empty category into the supplied category; retain that target category.",
        "compose": "F.compose(G) is F(G(-)); G.target and F.source must be equal labelled category tables.",
        "inverse": "Strict inverse requires bijections on both objects and arrows; an equivalence alone is insufficient.",
        "map-object": "The argument must be an object label in the source category.",
        "map-arrow": "The argument must be an arrow label in the source category.",
        "is-faithful": "Check injectivity separately on each source hom set, not on all arrow labels together.",
        "is-full": "For each source object pair, the induced map onto the corresponding target hom set is surjective.",
        "is-essentially-surjective": "Every target object is isomorphic to an image object; equality with an image is not required.",
        "is-equivalence": "Check fullness, faithfulness and essential surjectivity; no quasi-inverse witness is constructed.",
        "is-isomorphism": "Check bijectivity of the object and arrow maps for a strict category isomorphism.",
        "object-map": "Transfer the stored object map into a finite function with the complete source and target object sets.",
        "arrow-map": "Transfer the stored arrow map into a finite function with the complete source and target arrow sets.",
        "object-images": "Emit mapped objects in ascending source-label order, preserving duplicates.",
        "arrow-images": "Emit mapped arrows in ascending source-label order, preserving duplicates.",
        "object-fiber": "The target object label exists; emit all its source preimages in ascending order, possibly none.",
        "arrow-fiber": "The target arrow label exists; emit all its source preimages in ascending order, possibly none.",
        "identity-on": "Construct the identity functor on the supplied finite category.",
        "from-discrete-map": "Create discrete source and target categories from the finite function's domain and codomain.",
        "opposite": "Reverse both categories while retaining the same object and arrow maps.",
        "equal": "Compare complete labelled source/target categories and both maps; natural isomorphism is not equality.",
    },
    "FiniteCategoryAlgebra": {
        "compose": "The pair (f,g) is in path order: first f, then g; both arrows exist and target(f)=source(g).",
        "source": "The argument is an existing arrow label; return its source object label.",
        "target": "The argument is an existing arrow label; return its target object label.",
        "identity": "The argument is an existing object label; return its designated identity arrow label.",
        "hom": "Both object labels exist; emit all arrows between them in ascending label order, possibly none.",
        "inverse-of": "The arrow label exists; emit its unique two-sided inverse if it is an isomorphism, otherwise an empty list.",
        "is-isomorphism": "The arrow label exists; a two-sided inverse must satisfy both identity equations.",
        "endomorphisms": "The object label exists; emit all arrows from that object to itself.",
        "equal": "Compare labelled objects, arrows, identities and composition exactly; this does not decide categorical equivalence.",
        "from-preorder": "The finite relation is reflexive on its support and transitive; arrows are lexicographically ordered pairs labelled from zero.",
        "underlying-relation": "A pair of objects is related exactly when a morphism exists; parallel arrows collapse to one pair.",
        "initial-objects": "Emit each object with exactly one morphism to every object, including itself.",
        "terminal-objects": "Emit each object with exactly one morphism from every object, including itself.",
        "discrete-on": "Each supplied object label is also its sole identity arrow label; there are no other arrows.",
        "opposite": "Reverse every arrow and composition order, preserving labels and identities.",
    },
    "FiniteIntegerFunctionAlgebra": {
        "compose": "Composition is f(g(x)); declared middle finite sets and actual Algebra instances must match exactly.",
        "inverse": "The function must be a bijection onto its entire declared finite codomain.",
        "apply": "The point must belong to the function's declared finite domain.",
        "image": "The input set must be a subset of the declared finite domain.",
        "preimage": "The input set must be a subset of the declared finite codomain.",
        "restrict": "The input is a subset of the domain; restriction retains the original declared codomain.",
        "values": "Emit one image per domain member in domain iteration order, preserving repeated values.",
        "preimage-of": "The point belongs to the declared codomain; emit its fiber in domain order, possibly empty.",
        "equal": "Equality includes source/target Algebra identity, declared domain and codomain, and every mapped value.",
        "identity-on": "Construct identity on the supplied finite integer set, with that set also as codomain.",
        "from-relation": "The relation assigns exactly one value to each declared domain member and no others; every value belongs to the declared codomain.",
        "graph": "Return the finite relation of mapped pairs; the graph does not retain unused codomain members.",
    },
    "ResidueRing": {
        "divide": "The divisor must be a unit: gcd(divisor,modulus)=1; nonzero alone is insufficient.",
        "inverse": "The residue must be a unit: gcd(value,modulus)=1.",
        "power": "Nonnegative integer powers are total, with 0^0=1; negative powers require a unit base.",
        "solve-multiply": "Emit all x satisfying a*x=b in increasing canonical representative order; no solution gives an empty list.",
        "is-zero-divisor": "A zero divisor is nonzero and not a unit in this finite residue ring; zero itself is excluded.",
        "lift": "Return the unique integer representative between zero inclusive and the modulus exclusive.",
        "reduce": "Reduce any integer modulo this ring's fixed modulus.",
        "elements": "Emit all residue classes in increasing canonical representative order, within the resource cap.",
    },
    "RationalPolynomialRing": {
        "rational-roots": "The polynomial is nonzero. Emit all distinct rational roots in increasing order; irrational and nonreal roots are excluded. Constants emit an empty list; search exhaustion raises IMPLEMENTATION_FAILURE, never a partial list.",
        "root-multiplicity": "The polynomial is nonzero. Return the order of its zero at the rational argument, or zero for a nonroot. The zero polynomial has no finite root multiplicity.",
        "quotient": "The divisor is nonzero; return the Euclidean polynomial quotient over Q.",
        "remainder": "The divisor is nonzero; the Euclidean remainder has smaller degree than the divisor.",
        "quotient-remainder": "The divisor is nonzero; emit quotient then remainder with a = b*q + r and deg(r) < deg(b).",
        "divide-exact": "The divisor is nonzero and the polynomial remainder must be zero.",
        "gcd": "The gcd is monic; gcd(0,0) is defined as zero.",
        "monic": "The polynomial must be nonzero.",
        "compose": "Substitute the second polynomial into the first: f(g(x)).",
    },
    "RationalFunctionField": {
        "compose": "Formal f(g(x)); undefined if substitution makes the reduced denominator identically zero.",
        "evaluate": "The reduced denominator must be nonzero at the rational evaluation point.",
        "inverse": "The rational function must be nonzero.",
    },
    "SymmetricGroup": {
        "compose": "Composition is p(q(i)); the right operand acts first, and degrees must match.",
        "inverse": "Every permutation has an inverse in the same fixed-degree group.",
        "apply": "The natural-number point must be less than the permutation degree.",
        "orbit": "The point is less than the degree; emit its finite cycle beginning at that point, without repeating the endpoint.",
        "power": "The exponent is any BigInteger, including negative values; cycle lengths determine the result.",
        "cycles": "Emit nontrivial disjoint cycle permutations on the same full carrier; identity emits an empty list.",
        "elements": "Enumerate the entire fixed-degree group in lexicographic image order, within the explicit resource cap.",
    },
}

SPECTRAL_CONDITIONS = {
    "characteristic-polynomial": "The matrix is square. Return the monic polynomial det(x*I-A) over Q, including repeated factors; it annihilates A by Cayley-Hamilton.",
    "minimal-polynomial": "The matrix is square. Return the monic annihilating polynomial of least degree. It divides the characteristic polynomial; repeated Jordan factors are retained.",
    "evaluate-polynomial": "The matrix is square. Substitute A in the rational polynomial with constants interpreted as scalar identity matrices; return the first matrix carrier's wrapper.",
    "evaluate-at-matrix": "The second operand is square. Substitute that matrix in the first polynomial, using scalar identity matrices for constants; return the second matrix carrier's IAlgebraItem wrapper.",
    "rational-eigenvalues": "The matrix is square. Emit all distinct rational eigenvalues in increasing order, without multiplicities. This can be empty even when nonrational eigenvalues exist; resource exhaustion never emits an incomplete list.",
    "eigenspace-basis": "The matrix is square. Emit a rational basis of ker(A-lambda*I) in ascending free-column order. A non-eigenvalue emits an empty list; this is a basis, not enumeration of all eigenvectors.",
    "generalized-eigenspace-basis": "The matrix is square of dimension n. Emit a basis of ker((A-lambda*I)^n) in ascending free-column order. This is the full generalized eigenspace, not a Jordan chain; a non-eigenvalue emits an empty list.",
    "eigenvalue-multiplicity": "The matrix is square. Return the algebraic multiplicity of the rational argument in det(x*I-A), including zero for a non-eigenvalue; this need not equal the ordinary eigenspace dimension.",
    "is-diagonalizable-over-q": "The matrix is square. Return whether it has a full eigenbasis over Q. Irrational-only or defective spectra return false; resource exhaustion is IMPLEMENTATION_FAILURE, never false.",
    "diagonalize-over-q": "The matrix is square and has a full rational eigenbasis. Emit exactly [P,D], with P invertible, D diagonal in ascending eigenvalue order and A*P=P*D. P's columns are canonical free-coordinate eigenvectors, without Euclidean normalization.",
    "pow": "The matrix is square and the exponent is nonnegative. Compute the exact matrix power, including A^0=I for singular and zero matrices; exponent and arithmetic limits are implementation failures.",
}
for _matrix_owner in ("RationalMatrixAlgebra", "RationalMatrixFamily"):
    OWNER_CONDITIONS.setdefault(_matrix_owner, {}).update(SPECTRAL_CONDITIONS)
OWNER_CONDITIONS["RationalMatrixFamily"]["companion"] = "The polynomial has positive degree n. Return the n by n column companion matrix with subdiagonal ones and last column minus the lower coefficients divided by the leading coefficient; both characteristic and minimal polynomials equal its monic normalization."


def record(identifier, owner, concept, paths, operation=None):
    carrier, scope = OWNERS[owner]
    tests = ["groupimp/src/test/java/mathematics/ConcreteAlgebrasTest.java"]
    if owner in EXTRA_TESTS:
        tests.append("groupimp/src/test/java/operations/" + EXTRA_TESTS[owner] + ".java")
    if owner == "IntegerSetAlgebra":
        tests.append("groupimp/src/test/java/operations/NativeOptimizationTest.java")
        paths = paths + ["groupimp/src/main/java/algebra/concrete/FiniteSetAlgebra.java"]
    if owner == "RationalPolynomialRing":
        tests.append("groupimp/src/test/java/operations/NativeRationalFunctionTest.java")
    if owner in ("RationalMatrixFamily", "RationalAffineSpaceAlgebra"):
        tests.append("groupimp/src/test/java/operations/NativeLeastSquaresTest.java")
    if owner in ("RationalMatrixAlgebra", "RationalMatrixFamily", "RationalPolynomialRing"):
        tests.append("groupimp/src/test/java/operations/NativeSpectralLinearTest.java")
        paths = paths + ["groupimp/src/main/java/mathematics/calculus/RationalPolynomialRoots.java"]
    if owner in ("RationalMatrixAlgebra", "RationalMatrixFamily"):
        paths = paths + ["groupimp/src/main/java/algebra/concrete/MatrixSpectralOperations.java",
                         "groupimp/src/main/java/mathematics/linear/RationalMatrixSpectral.java"]
    if owner in ("RationalMultivariatePolynomialAlgebra", "PolynomialDifferentialFormAlgebra"):
        tests.append("groupimp/src/test/java/operations/NativePolynomialChainsTest.java")
    if owner == "FiniteMarkovAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/probability/FiniteMarkovKernel.java"]
    if owner == "AbelianGroupTypeAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/structures/AbelianGroupType.java"]
    if owner in ("PresentedAbelianGroupAlgebra", "AbelianGroupElementAlgebra"):
        paths = paths + ["groupimp/src/main/java/mathematics/structures/PresentedAbelianGroup.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupElement.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "IntegerVectorFamily":
        paths = paths + ["groupimp/src/main/java/mathematics/linear/IntegerVector.java"]
    if owner == "IntegerMatrixFamily":
        paths = paths + ["groupimp/src/main/java/mathematics/linear/IntegerMatrix.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "FiniteSimplicialAlgebra":
        tests.append("groupimp/src/test/java/operations/NativeIntegralHomologyTest.java")
        tests.append("groupimp/src/test/java/operations/NativeIntegerLinearTest.java")
        paths = paths + ["groupimp/src/main/java/mathematics/topology/FiniteSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralSimplicialHomology.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    value = {
        "id": identifier, "mathematical_area": "Concrete MathTool algebras",
        "subfield": owner, "concept": concept, "specification_section": 0,
        "record_kind": "DOMAIN_OR_STRUCTURE", "domain_A": None, "domain_B": None,
        "domain_C": carrier, "operation_signature": None, "arity": None,
        "scalar_or_flat": "SCALAR", "representation_scope": scope,
        "required_invariants": [
            "Operands and results satisfy membership in the actual Algebra instances.",
            "Dimension, modulus, normalization and canonical representation are enforced where applicable.",
            "Law descriptions and passing tests are not proofs."
        ],
        "representation_status": "DIRECTLY_SUPPORTED",
        "framework_mapping": "Original Algebra with native operations; ConcreteMathematics installs registrations in the existing MathTool.",
        "implementation_status": "IMPLEMENTED", "implementation_paths": paths,
        "tests": tests, "machine_tested": True, "human_review_status": "UNREVIEWED",
        "reviewer": None, "review_date": None,
        "review_notes": "No specialist review recorded; scoped implementation has empirical tests.",
        "formal_verification_status": "UNVERIFIED",
        "known_limitations": [
            "Only this concrete carrier is implemented, not every structure in its mathematical field.",
            "Arbitrary precision computations and eager finite outputs remain subject to memory/time limits."
        ],
        "required_extension": None,
        "references": ["https://msc2020.org/", "https://leanprover-community.github.io/mathlib4_docs/Mathlib.html"],
        "epistemic_status": "DEFINED",
        "provenance": {
            "definition_source": "docs/CONCRETE_ALGEBRAS.md", "implementation_source": paths[0],
            "implemented_by": "Codex", "generated_by_model": "not recorded", "reviewed_by": None,
            "version": "2", "date": DATE,
            "assumptions": ["Classification is scoped; passing tests do not prove laws."],
            "related_concepts": []
        },
        "assessment_depth": "TESTED_RESTRICTED_IMPLEMENTATION"
    }
    if operation:
        first, second, result = operation["first"], operation["second"], operation["result"]
        second = None if second == "-" else second
        target = "List(" + result + ")" if operation["semantics"] == "LIST" else result
        value.update(
            record_kind="OPERATION", domain_A=first, domain_B=second, domain_C=result,
            operation_signature=first + (" x " + second if second else "") + " -> " + target,
            arity=2 if second else 1, scalar_or_flat=operation["semantics"],
            representation_scope=scope + "; native operation " + operation["id"],
            total_or_partial=operation["partiality"], runtime_operation_id=operation["id"],
            runtime_catalog="ConcreteMathematics", legacy_operation_name=operation["alias"],
            legacy_operation_interface=operation["interface"],
            framework_mapping=operation["interface"] + " installed in the original Algebra registry; invoked by IAlgebraItem and AlgebraFlow."
        )
        operation_name = operation["id"].rsplit(".", 1)[-1]
        condition = OWNER_CONDITIONS.get(owner, {}).get(operation_name, CONDITIONS.get(operation_name))
        if condition:
            value["required_invariants"].append(condition)
        if operation["semantics"] == "LIST":
            value["required_invariants"].append("Finite ordered wrapped results; duplicates and empty lists are retained.")
        if operation_name == "subsets":
            value["known_limitations"].append("Materialization is capped at 20 input elements; exceeding it is IMPLEMENTATION_FAILURE, not mathematical nonexistence.")
        if owner == "RationalPolynomialRing" and operation_name in ("iterate", "orbit"):
            value["known_limitations"].append("Iteration is capped at 10000 steps; exceeding it is IMPLEMENTATION_FAILURE. Exact values can still grow rapidly within this limit.")
        if operation_name in ("argmin", "argmax", "minimum", "maximum", "minimizers", "maximizers"):
            value["known_limitations"].append("Optimality is relative only to the explicit finite feasible set, not all integers or reals.")
    if owner == "FiniteMarkovAlgebra":
        value["required_invariants"].append("Rows are normalized nonnegative rational measures using the actual integer Algebra. Complete source/target finite sets are retained and ordered by integer label, separately from positive support.")
        value["known_limitations"].append("Each boundary has at most 64 states; powers and marginal orbits accept at most 10000 steps. Each whole composition, power, orbit, graph analysis or linear-system calculation has a conservative 5000000-unit work budget, with dense shape-based charges even for sparse rows. Exhaustion raises IMPLEMENTATION_FAILURE, never a false stationary result or truncated list. Coefficient bit lengths are unbounded.")
        value["known_limitations"].append("Finite discrete-time kernels on integer states only. Empty sources are allowed; chain operations require equal declared boundaries. There is no continuous-time generator, path sampler, convergence certification, mixing-time calculation or infinite-state process. Mean hitting times are an all-state rational vector and are undefined if any entry is infinite.")
        value["references"].append("https://quanteconpy.readthedocs.io/en/latest/markov/core.html")
        value["references"].append("https://python.quantecon.org/finite_markov.html")
    if owner == "FiniteSimplicialAlgebra":
        value["known_limitations"] += ["Construction materializes faces and caps each input facet at 20 vertices.",
            "Integral homology, rational Betti numbers and boundary Smith factors allow at most 256 simplices in each required degree and share a 5000000-unit Smith-reduction budget per complete calculation. Exhaustion raises IMPLEMENTATION_FAILURE, never a partial invariant list. BigInteger coefficient bit lengths are unbounded.",
            "Homology returns isomorphism types, not cycle representatives, chosen generators or induced maps. No persistence, cup products or homeomorphism decision is implemented. Existing betti-number and betti-numbers retain F2 coefficients."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATchapters.html")
        value["references"].append("https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex_examples.html")
    if owner == "AbelianGroupTypeAlgebra":
        value["required_invariants"].append("Free rank is nonnegative; canonical torsion factors are greater than one and each divides the next. Equality classifies group isomorphism types.")
        value["known_limitations"] += ["At most 256 canonical torsion factors and 1024 supplied/intermediate cyclic factors; exhaustion raises IMPLEMENTATION_FAILURE. Free rank and coefficient bit lengths are arbitrary precision. Normalization uses gcd/lcm without prime factorization.",
            "No chosen group elements, presentations, generators, homomorphisms or extension witnesses are represented. Hom, tensor, Tor_1 and Ext^1 classify resulting abelian groups only; arbitrary modules and higher derived functors are outside this scope."]
        value["references"].append("https://doc.sagemath.org/html/en/reference/groups/sage/groups/additive_abelian/additive_abelian_group.html")
    if owner in ("IntegerVectorFamily", "IntegerMatrixFamily"):
        value["known_limitations"].append("Each coordinate dimension is at most 256; integer coefficient bit lengths remain unbounded. Oversized representations raise IMPLEMENTATION_FAILURE. No floating-point approximation or coordinate rounding is used.")
    if owner in ("PresentedAbelianGroupAlgebra", "AbelianGroupElementAlgebra"):
        value["required_invariants"].append("Relations are integer matrix columns. Each presentation retains the Smith-coordinate map; element equality and arithmetic respect that presentation, not only its abstract isomorphism type.")
        value["known_limitations"] += ["Presentations allow at most 256 generators and 256 relations. Construction and representative lifting use bounded integer Smith calculations with a 5000000-unit budget per calculation; exhaustion is IMPLEMENTATION_FAILURE. Coefficient bit lengths remain unbounded.",
            "Finite element, cyclic-subgroup and scaling-fiber enumeration is capped at 4096 results; oversized finite outputs raise IMPLEMENTATION_FAILURE without a truncated result. Infinite outputs are outside these flat operations.",
            "Only finitely presented abelian groups over Z are represented. No arbitrary quotient-module morphisms, isomorphism witnesses between presentations, general subgroup presentations, nonabelian group presentations or homology generators are implemented by these carriers."]
        value["references"] += ["https://doc.sagemath.org/html/en/reference/groups/sage/groups/additive_abelian/additive_abelian_group.html",
                                "https://doc.sagemath.org/html/en/reference/modules/sage/modules/fg_pid/fgp_module.html"]
    if owner == "IntegerMatrixFamily":
        value["known_limitations"].append("Each matrix product, Smith reduction, lattice basis, inverse or solve calculation has a 5000000-unit integer work budget shared across reduction, witness updates and subsequent products. Dense costs apply even to sparse or identity inputs; exhaustion may occur below the dimension cap and is IMPLEMENTATION_FAILURE, never nonexistence or an incomplete basis.")
        value["known_limitations"].append("Matrices represent maps between finite free Z-modules. Cokernels return abelian-group isomorphism types, without quotient elements, quotient maps or arbitrary finitely presented module morphisms. No Hermite form, lattice reduction, shortest vector or minimal-norm integer optimization is provided.")
        value["references"].append("https://doc.sagemath.org/html/en/reference/matrices/sage/matrix/matrix_integer_dense.html")
    if owner == "FiniteIntegerRelationAlgebra":
        value["known_limitations"].append("Finite support only; equality includes source/target Algebra identity and pair equality. Function totality is restricted to an explicit finite carrier.")
    if owner == "FiniteIntegerFunctionAlgebra":
        value["known_limitations"].append("Finite stored maps only; this does not implement arbitrary callback or infinite-domain function spaces. Codomain equality is required for typed composition even if the actual range is smaller.")
        value["references"].append("https://doc.sagemath.org/html/en/reference/sets/sage/sets/finite_set_maps.html")
    if owner == "FiniteCategoryAlgebra":
        value["required_invariants"].append("Construction checks every composable pair and associativity triple, both identity laws and source/target typing.")
        value["known_limitations"].append("Table validation is capped at 128 arrows and objects; larger presentations raise IMPLEMENTATION_FAILURE. Functors have separate native registrations; arbitrary infinite categories and a proof-assistant kernel are not implemented.")
        value["references"].append("https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Category/Basic.html")
    if owner == "FiniteFunctorAlgebra":
        value["required_invariants"].append("Construction checks total maps, endpoint typing, identity preservation and every source composition.")
        value["known_limitations"].append("Both categories use the existing 128-arrow/object table cap. Only finite covariant functors are represented; is-equivalence is a decision criterion. Explicit witnesses are constructed separately by FiniteEquivalenceAlgebra.")
        value["references"] += ["https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Functor/Basic.html",
                                "https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Equivalence.html"]
    if owner == "FiniteNaturalTransformationAlgebra":
        value["required_invariants"].append("Functors are parallel, every component has the required endpoints, and all finite naturality squares commute.")
        value["known_limitations"].append("Underlying category tables retain the 128-arrow/object cap. Components are explicit finite maps; arbitrary infinite natural transformations are unimplemented. Finite diagram limits have separate bounded registrations in FiniteConeAlgebra.")
        value["references"] += ["https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/NatTrans.html",
                                "https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/NatIso.html"]
    if owner == "FiniteEquivalenceAlgebra":
        value["required_invariants"].append("Oppositely directed functors have an invertible unit Id_C -> G.F and counit F.G -> Id_D; both triangle identities are checked at every object.")
        value["known_limitations"].append("Finite category tables retain the 128-arrow/object cap. These are chosen adjoint-equivalence witnesses; composition with the reversed witness need not be strictly equal to identity witness data. General adjunctions and infinite categories are not implemented by this carrier.")
        value["references"].append("https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Equivalence.html")
    if owner == "FiniteAdjunctionAlgebra":
        value["required_invariants"].append("Oppositely directed functors, typed natural unit/counit and both triangle identities are checked; invertibility is not required.")
        value["known_limitations"].append("Finite categories retain the 128-arrow/object cap. Search is exhaustive only for these explicit finite tables, chooses one adjoint deterministically and does not enumerate all possible witnesses or handle infinite categories.")
        value["references"] += ["https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Adjunction/Basic.html",
                                "https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Adjunction/Comma.html",
                                "https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Adjunction/Opposites.html"]
    if owner == "FiniteConeAlgebra":
        value["required_invariants"].append("A retained target-category vertex has one typed leg to each diagram object, and all cone equations commute; limits additionally require unique factorization from every cone.")
        value["known_limitations"].append("Categories retain the 128-arrow/object cap. Each search is bounded at 10000 enumerated cones and 1000000 search steps; exhaustion raises IMPLEMENTATION_FAILURE. This is finite table search, not an infinite limit algorithm or formal proof.")
        value["references"] += ["https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Limits/Cones.html",
                                "https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Limits/IsLimit.html"]
    if owner == "FiniteCoconeAlgebra":
        value["required_invariants"].append("Each leg is F(j)->vertex, and F(f);leg_k = leg_j for every shape arrow f:j->k. Colimits require unique outgoing factorization for every cocone.")
        value["known_limitations"].append("The shared opposite-cone search retains the 128-arrow/object category cap, 10000 enumerated cones and 1000000 search steps. Exhaustion raises IMPLEMENTATION_FAILURE. Only explicit finite diagrams and categories are supported.")
        value["references"] += ["https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Limits/Cones.html",
                                "https://leanprover-community.github.io/mathlib4_docs/Mathlib/CategoryTheory/Limits/IsLimit.html"]
    if owner in ("RationalVectorFamily", "RationalMatrixFamily", "RationalAffineSpaceAlgebra"):
        value["references"].append("https://docs.sympy.org/latest/modules/matrices/matrices.html")
    if owner == "RationalTensorAlgebra":
        value["references"].append("https://docs.sympy.org/latest/modules/tensor/array.html")
        value["known_limitations"].append("Dense materialization is capped at order 32 and 1000000 entries per tensor; exceeding a cap raises IMPLEMENTATION_FAILURE, not mathematical nonexistence. Removing zero axes by contraction can exceed the result materialization cap.")
        value["known_limitations"].append("Standard coordinate spaces and coordinate contraction only; no covariant/contravariant axis labels, arbitrary metrics, basis-change witnesses, sparse storage or tensor bundles.")
        value["required_invariants"].append("Every axis dimension is nonnegative, scalar order is zero, coordinates are immutable and row-major, and entry count equals the shape product with the empty product equal to one.")
    if owner == "RationalExteriorAlgebra":
        value["references"].append("https://doc.sagemath.org/html/en/reference/algebras/sage/algebras/clifford_algebra_element.html")
        value["required_invariants"].append("Canonical immutable sparse coefficients use increasing coordinate basis wedges, remove zeros and retain ambient dimension; scalar and zero values do not erase that dimension.")
        value["known_limitations"].append("Dimension is capped at 20 and sparse intermediate/output support at 100000 terms. Wedge and each complete induced matrix action allow at most 1000000 candidate coefficient products; cap exhaustion raises IMPLEMENTATION_FAILURE.")
        value["known_limitations"].append("Coordinate multivectors over Q only, with the standard positive Euclidean metric and orientation for Hodge star and insertion. No arbitrary metrics, basis-change witnesses, differential forms on manifolds or exterior derivative are supplied by this carrier.")
    if owner == "RationalQuaternionAlgebra":
        value["references"].append("https://docs.sympy.org/latest/modules/algebras.html")
        value["required_invariants"].append("All four Hamilton coefficients are exact rationals. Every nonzero member has positive squared norm and an inverse; noncommutative multiplication order is retained.")
        value["known_limitations"].append("This is the rational Hamilton division algebra, not every real quaternion. Rotation recovery returns a projective representative, which need not have unit norm; no irrational normalization, Euler angles or approximate matrix fitting is provided.")
    if owner in ("RationalMultivariatePolynomialAlgebra", "RationalPolynomialMapAlgebra"):
        value["references"] += ["https://docs.sympy.org/latest/modules/polys/reference.html",
                                "https://docs.sympy.org/latest/modules/matrices/matrices.html"]
        value["required_invariants"].append("Each scalar polynomial retains its positive variable count, with immutable nonnegative exponent tuples and canonical nonzero rational coefficients. Map components share an input space and retain output order.")
        value["known_limitations"].append("At most 32 input variables and 32 map components, total monomial degree 10000, and 100000 nonzero terms per scalar intermediate/output. A multiply, power or complete substitution/composition allows 1000000 candidate coefficient products; exponents for pow are capped at 10000. Cap exhaustion raises IMPLEMENTATION_FAILURE, not mathematical undefinedness.")
        value["known_limitations"].append("Exact rational polynomial coordinate calculus only, with positive dimensions and standard Euclidean coordinates. No arbitrary differentiable callbacks, general smooth functions, manifolds, Groebner bases or multivariate rational functions; no formal proof of the stated identities.")
    if owner == "PolynomialDifferentialFormAlgebra":
        value["references"] += ["https://doc.sagemath.org/html/en/reference/manifolds/sage/manifolds/differentiable/diff_form.html",
                                "https://doc.sagemath.org/html/en/reference/manifolds/sage/manifolds/differentiable/diff_map.html"]
        value["required_invariants"].append("Immutable sparse differential masks carry nonzero canonical rational polynomials with the same retained coordinate dimension; mixed degrees are allowed. Pullback uses dF_i, while the older exterior matrix action remains a covariant multivector action.")
        value["known_limitations"].append("Dimensions are positive and at most 20; coefficient polynomial total degree is at most 10000. There are at most 100000 nonzero scalar monomials across all coefficients of an intermediate/output form. Wedge, polynomial multiplication, insertion, Lie derivative and complete pullback each share a budget of 1000000 basis-work visits plus candidate polynomial products. Exhaustion is IMPLEMENTATION_FAILURE.")
        value["known_limitations"].append("Polynomial forms in fixed standard coordinate spaces only. Hodge star and vector-field/exterior identifications use the standard Euclidean metric and orientation. Integration covers top-degree unit cubes and compatible polynomial cells/chains; arbitrary manifold charts, metric fields, smooth coefficients, global topology and general integration domains remain unsupported.")
    if owner in ("PolynomialCellAlgebra", "PolynomialChainAlgebra"):
        value["references"] += ["https://doc.sagemath.org/html/en/reference/topology/sage/topology/cubical_complex.html",
                                "https://doc.sagemath.org/html/en/reference/homology/sage/homology/chains.html"]
        value["required_invariants"].append("Rational coefficient data define polynomial parametrizations of real unit cubes with the standard parameter orientation. Chains are immutable canonical finite formal sums with retained ambient dimension and integer degree; all negative degrees are zero-only.")
        value["known_limitations"].append("Ambient dimension is 1 through 20 and positive cell/chain degree at most 10. A chain has at most 10000 occupied cells, including intermediate support; boundary and product allow at most 100000 candidate face/cell visits. Each whole evaluation, face list, vertex list, boundary, product, pushforward or integral additionally shares 1000000 polynomial work units across cells, coefficient-coordinate scans and candidate coefficient products. Exhaustion raises IMPLEMENTATION_FAILURE.")
        value["known_limitations"].append("Polynomial degrees, coefficient support and pullback forms retain their existing representation caps. Exact coefficient bit lengths are unbounded. Parametrizations need not be injective; integrals retain orientation and multiplicity. Degenerate cells and reparametrizations are not quotiented out, and no geometric image equality, standard cubical homology groups, arbitrary manifolds or general numerical quadrature are provided.")
    if owner == "RationalVectorFamily":
        value["known_limitations"].append("This is a family of different vector spaces, not one vector space across all dimensions. Dimension checks run at operation execution; entries are materialized exactly.")
    if owner == "RationalMatrixFamily":
        value["known_limitations"].append("Dense exact matrices with positive row and column counts only; zero-sized matrices, sparse algorithms, general algebraic/complex eigenvalue representations and numerical error contracts are not supplied by this carrier.")
        value["required_invariants"].append("Shapes are stored explicitly. RREF uses exact rational row operations; bases preserve rank-nullity and declared coordinate dimensions.")
        value["known_limitations"].append("Pseudoinverse and least-squares use exact rational arithmetic in the standard Euclidean inner products; no floating-point rank tolerance, weighted metric or approximation error estimate is provided.")
    if owner in ("RationalMatrixAlgebra", "RationalMatrixFamily"):
        value["known_limitations"].append("Spectral and matrix polynomial operations require square dimension at most 32. Matrix polynomial evaluation and natural powers allow degree/exponent at most 10000. Each calculation shares 5000000 arithmetic work units across matrix products, diagonal shifts, power elimination and eigenspace reductions. Exceeding a cap raises IMPLEMENTATION_FAILURE, not a mathematical nonexistence result. Other matrix operations keep their existing scope.")
        value["required_invariants"].append("Spectral arithmetic is exact over Q. Characteristic/minimal polynomials retain nonrational factors; eigenvalue enumeration and diagonalization remain over Q. Diagonalization emits P then D with A*P=P*D.")
        value["references"].append("https://docs.sympy.org/latest/modules/matrices/matrices.html")
        value["references"].append("https://doc.sagemath.org/html/en/reference/matrices/sage/matrix/matrix2.html")
    if owner in ("RationalMatrixAlgebra", "RationalMatrixFamily", "RationalPolynomialRing"):
        value["known_limitations"].append("Rational root search and root multiplicity accept degree at most 64. Search uses exact trial division of primitive integer coefficients with at most 100000 trial divisions, 100000 signed candidate pairs, 50000 divisors per coefficient, and 1000000 coefficient visits; it may fail for large coefficients even at degree two. Exhaustion raises IMPLEMENTATION_FAILURE and never yields a partial root list. Coefficient bit lengths are unbounded. These caps do not restrict existing polynomial arithmetic.")
    if owner == "RationalAffineSpaceAlgebra":
        value["known_limitations"].append("Finite linear systems over Q with a positive number of equations and unknowns. Infinite solution sets are parametrized by a finite basis, never enumerated. Empty sets retain ambient dimension and have no particular point, direction basis or affine dimension operation result.")
        value["required_invariants"].append("The private representation is constructed by exact row reduction. Free-zero particular points and ordered unit-free-coordinate basis vectors give a canonical affine parametrization.")
        value["required_invariants"].append("Least-squares and closest-point operations use the standard Euclidean inner product and preserve exact rational results.")
    if owner == "RationalFunctionField":
        value["known_limitations"].append("Formal fraction-field equality; cancelled factors do not retain excluded points from an original expression. Only rational-coefficient univariate functions are implemented.")
        value["references"].append("https://docs.sympy.org/latest/modules/polys/domainsref.html")
    if owner == "SymmetricGroup":
        value["known_limitations"].append("Labels are zero-based and the degree is fixed. Full enumeration is capped at degree 8; higher-degree groups and individual operations remain representable.")
        value["references"].append("https://doc.sagemath.org/html/en/reference/combinat/sage/combinat/permutation.html")
    if owner == "ResidueRing":
        value["known_limitations"].append("Only fixed moduli greater than one are supported. Enumeration and congruence solution lists are capped at 10000 outputs; exceeding the cap is IMPLEMENTATION_FAILURE, not mathematical nonexistence.")
        value["references"].append("https://doc.sagemath.org/html/en/reference/finite_rings/sage/rings/finite_rings/integer_mod.html")
    return value


def synchronize(data, rows):
    data = copy.deepcopy(data)
    owners = {row["class"] for row in rows}
    if owners != OWNERS.keys():
        raise ValueError("Review OWNERS for the current concrete algebra classes.")
    old_native = [r for r in data["concepts"] if r["id"].startswith(("concrete-operation.", "concrete-algebra."))]
    if any(r["human_review_status"] != "UNREVIEWED" or r["formal_verification_status"] != "UNVERIFIED"
           or r["epistemic_status"] in ("PROVED", "FORMALLY_VERIFIED", "MACHINE_CHECKED") for r in old_native):
        raise ValueError("Reviewed/proved native records require manual evidence-preserving updates.")
    data["concepts"] = [r for r in data["concepts"] if r not in old_native]
    for owner in sorted(owners):
        path = "groupimp/src/main/java/algebra/concrete/" + owner + ".java"
        data["concepts"].append(record("concrete-algebra." + owner, owner, owner, [path]))
    for row in rows:
        path = "groupimp/src/main/java/algebra/concrete/" + row["class"] + ".java"
        implementation = "groupimp/src/main/java/operations/" + INTERFACES[row["interface"]] + ".java"
        data["concepts"].append(record("concrete-operation." + row["id"], row["class"], row["id"],
                                       [path, implementation], row))
    descriptors = [
        ("PresentedAbelianGroup", "Retained finite abelian presentations", "mathematics.structures.PresentedAbelianGroup",
         ["An explicit integer matrix presents Z^rows modulo its column image", "A retained unimodular Smith-coordinate map", "Equality includes presentation and coordinate map; isomorphism type is separate"], ["Mat(Z)", "Vec(Z)", "AbelianGroupType", "AbelianGroupElement", "N", "Boolean"]),
        ("AbelianGroupElement", "Elements of finitely presented abelian groups", "mathematics.structures.AbelianGroupElement",
         ["Retained presented group and Smith-coordinate map", "Canonical residues at nonzero Smith factors and arbitrary free integer coordinates", "Additive operations require matching presentations"], ["PresentedAbelianGroup", "Vec(Z)", "Z", "N", "Boolean"]),
        ("Vec(Z)", "Finite free integer coordinate modules", "mathematics.linear.IntegerVector",
         ["Retained nonnegative dimension", "Immutable arbitrary-precision integer entries", "Addition and dot product require equal dimensions"], ["Z", "N", "Boolean", "Vec(Q)", "Mat(Z)"]),
        ("Mat(Z)", "Integer matrices with Smith witnesses", "mathematics.linear.IntegerMatrix",
         ["Explicit nonnegative row and column counts, including empty shapes", "Immutable rectangular arbitrary-precision entries", "Maps Z^columns to Z^rows; composition checks the middle dimension"], ["Z", "N", "Boolean", "Vec(Z)", "Mat(Q)", "AbelianGroupType", "FiniteComplex"]),
        ("AbelianGroupType", "Finitely generated abelian-group isomorphism types", "mathematics.structures.AbelianGroupType",
         ["Nonnegative arbitrary-precision free rank", "Canonical cyclic torsion factors greater than one, each dividing the next", "No chosen elements, generators or maps; equality is group isomorphism-type equality"], ["N", "Boolean", "FiniteComplex"]),
        ("FiniteMarkov(Z)", "Finite exact stochastic kernels", "mathematics.probability.FiniteMarkovKernel",
         ["Actual integer outcome Algebra retained", "Explicit source and target sets in increasing label order", "One normalized rational row measure per source state, supported in the target set"],
         ["Z", "Q", "N", "FiniteSet(Z)", "FiniteDistribution(Z)", "FiniteFunction(Z,Z)", "Mat(Q)", "Vec(Q)"]),
        ("ZxZ.markov", "Source and destination transition states", "mathematics.foundations.Pair<BigInteger,BigInteger>",
         ["Both states belong to the actual integer Algebra; kernel boundary membership is checked at execution"], ["Z", "FiniteMarkov(Z)"]),
        ("FiniteDistribution(Z)xN.markov", "Markov marginal orbit inputs", "mathematics.foundations.Pair<FiniteDistribution<BigInteger>,BigInteger>",
         ["Initial finite measure uses the actual integer outcome Algebra", "Step count is a nonnegative integer; execution additionally enforces state support and resource limits"], ["FiniteDistribution(Z)", "N", "FiniteMarkov(Z)"]),
        ("PolynomialCell(Q)", "Polynomially parametrized cubical cells", "mathematics.calculus.PolynomialCell",
         ["Positive ambient coordinate dimension and nonnegative parameter dimension", "Explicit rational point or ordered polynomial parametrization on the real unit cube", "Parameter orientation retained; equality is parametrization equality"], ["PolynomialMap(Q)", "PolynomialForm(Q)", "Vec(Q)", "Q", "N", "PolynomialChain(Q)"]),
        ("PolynomialChain(Q)", "Finite rational polynomial cubical chains", "mathematics.calculus.PolynomialChain",
         ["Retained positive ambient dimension and integer degree", "Canonical nonzero rational coefficients on cells of the declared degree", "All negative degrees contain only zero; boundary lowers degree"], ["PolynomialCell(Q)", "PolynomialMap(Q)", "PolynomialForm(Q)", "Q", "N", "Z"]),
        ("PolynomialForm(Q)", "Polynomial coordinate differential forms", "mathematics.calculus.PolynomialDifferentialForm",
         ["Retained positive ordered coordinate dimension", "Increasing differential basis masks with canonical polynomial coefficients", "All coefficient variable counts equal the coordinate dimension; mixed differential degrees allowed"], ["Poly(Q)", "PolynomialMap(Q)", "Exterior(Q)", "Vec(Q)", "Q", "N"]),
        ("Poly(Q)", "Exact rational multivariate polynomial family", "mathematics.calculus.MultivariatePolynomial",
         ["Retained positive ordered variable count", "Nonnegative exponent tuples and exact nonzero rational coefficients", "Canonical immutable sparse support; dimension-sensitive arithmetic is partial"], ["Q", "Q[x]", "N", "Z", "Vec(Q)", "Mat(Q)", "PolynomialMap(Q)"]),
        ("PolynomialMap(Q)", "Exact rational polynomial maps", "mathematics.calculus.PolynomialMap",
         ["Positive explicit input and output dimensions", "Ordered scalar polynomial components share an input dimension", "Composition checks the common middle dimension; Jacobian rows are output components"], ["Poly(Q)", "Vec(Q)", "Mat(Q)", "Q", "N"]),
        ("H(Q)", "Rational Hamilton quaternion algebra", "mathematics.numbers.RationalQuaternion",
         ["Four canonical rational coefficients in scalar,i,j,k order", "Hamilton multiplication with i*j=k and j*i=-k", "Rotation actions use standard right-handed coordinates and require a nonzero quaternion"], ["Q", "Q(i)", "Vec(Q)", "Mat(Q)"]),
        ("Exterior(Q)", "Rational coordinate exterior algebras", "mathematics.linear.RationalExterior",
         ["Retained nonnegative ambient dimension", "Nonzero rational coefficients on increasing coordinate basis masks", "Standard Euclidean orientation and pairing; mixed homogeneous grades allowed"], ["Q", "Vec(Q)", "Mat(Q)", "N"]),
        ("Tensor(Q)", "Finite rational coordinate tensors", "mathematics.linear.RationalTensor",
         ["Ordered nonnegative axis dimensions", "Row-major rational coordinates with checked shape product", "Order-zero scalars have one entry; zero-sized axes have no entries"], ["Q", "Vec(Q)", "Mat(Q)", "N", "NxN.tensor-axes"]),
        ("NxN.tensor-axes", "Ordered tensor axis pair", "mathematics.foundations.Pair<BigInteger,BigInteger>",
         ["Two nonnegative integer indices", "Each operation checks existence and compatibility in the actual tensor shape"], ["N"]),
        ("Vec(Q)", "Family of finite rational vectors", "mathematics.linear.RationalVector",
         ["Finite nonnegative dimension", "Exact rational entries", "Dimension-sensitive operations check compatible input lengths"], ["Q", "Q^2", "N"]),
        ("Mat(Q)", "Family of positive rectangular rational matrices", "mathematics.linear.RationalMatrix",
         ["Positive row and column counts", "Rectangular immutable exact entries", "Shape-sensitive operations check dimensions"], ["Q", "Vec(Q)", "Mat2(Q)", "N"]),
        ("Affine(Q)", "Exact rational affine solution sets", "mathematics.linear.RationalAffineSpace",
         ["Retained positive ambient dimension", "Empty set or canonical particular point and ordered independent kernel basis", "Every parameter tuple determines exactly one solution"], ["Mat(Q)", "Vec(Q)", "Q", "N"]),
        ("FiniteCocone", "Validated cocones over finite diagrams", "mathematics.structures.FiniteCocone",
         ["Finite diagram and retained target vertex", "Typed legs from every diagram object to the vertex", "Every cocone equation checked; empty shapes retain the vertex"], ["FiniteCone", "FiniteFunctor", "FiniteCategory", "FiniteNaturalTransformation", "FiniteFunction(Z,Z)", "Z"]),
        ("FiniteCone", "Validated cones over finite diagrams", "mathematics.structures.FiniteCone",
         ["Finite diagram with retained target vertex", "Exactly one typed leg per shape object", "Every cone equation checked; an empty shape still retains its vertex"], ["FiniteFunctor", "FiniteCategory", "FiniteNaturalTransformation", "FiniteFunction(Z,Z)", "Z"]),
        ("FiniteAdjunction", "Validated finite adjunctions", "mathematics.structures.FiniteAdjunction",
         ["Oppositely directed finite functors", "Typed natural unit and counit", "Both triangle identities checked at every object; components need not be invertible"], ["FiniteCategory", "FiniteFunctor", "FiniteNaturalTransformation", "FiniteEquivalence", "ZxZ.category", "FiniteFunction(Z,Z)"]),
        ("FiniteEquivalence", "Validated finite adjoint equivalences", "mathematics.structures.FiniteEquivalence",
         ["Oppositely directed finite functors", "Invertible typed unit and counit", "Both triangle identities checked at every object"], ["FiniteCategory", "FiniteFunctor", "FiniteNaturalTransformation"]),
        ("FiniteNaturalTransformation", "Validated finite natural transformations", "mathematics.structures.FiniteNaturalTransformation",
         ["Parallel finite functors", "Exactly one typed component for every source object", "Naturality checked for every source arrow"], ["FiniteFunctor", "FiniteCategory", "Z", "FiniteFunction(Z,Z)"]),
        ("FiniteFunctor", "Validated finite functors", "mathematics.structures.FiniteFunctor",
         ["Finite labelled source and target categories", "Total object and arrow maps", "Preservation of endpoints, identities and every composition"], ["FiniteCategory", "Z", "FiniteFunction(Z,Z)"]),
        ("FiniteCategory", "Validated finite category tables", "mathematics.structures.FiniteCategory",
         ["Integer object and arrow labels", "Complete typed composition and designated identities", "Both identity laws and associativity checked exhaustively within the resource cap"], ["Z", "FiniteSet(Z)", "FiniteRelation(Z,Z)"]),
        ("ZxZ.category", "Ordered category label pair", "mathematics.foundations.Pair<BigInteger,BigInteger>",
         ["Two integer labels", "Each operation checks object or arrow membership in its category"], ["Z"]),
        ("FiniteFunction(Z,Z)", "Functions between finite integer sets", "mathematics.foundations.FiniteFunction<BigInteger,BigInteger>",
         ["Explicit finite domain and codomain in the registered integer Algebra", "Exactly one value in the codomain for every domain member", "Equality retains the declared codomain"], ["Z", "FiniteSet(Z)", "FiniteRelation(Z,Z)"]),
        ("FiniteSet(Z)xFiniteSet(Z).function", "Declared finite function boundaries", "mathematics.foundations.Pair<FiniteSet<BigInteger>,FiniteSet<BigInteger>>",
         ["Both coordinates are finite integer sets", "First is domain and second is codomain"], ["FiniteSet(Z)"]),
        ("Z/6Z", "Residue ring modulo six", "mathematics.numbers.ModularInteger",
         ["Fixed modulus six", "Canonical representatives from zero through five", "Composite modulus permits nonzero nonunits"], ["Z"]),
        ("S3", "Symmetric group on three labels", "mathematics.structures.Permutation",
         ["Images bijectively cover 0,1,2", "Composition uses the same fixed degree"], ["N", "Z"]),
        ("Q(x)", "Rational function field", "mathematics.calculus.RationalFunction",
         ["Coprime rational-coefficient numerator and denominator", "Denominator is nonzero and monic; zero is 0/1"], ["Q", "Q[x]"]),
        ("FiniteRelation(Z,Z)", "Finite integer relations", "mathematics.foundations.FiniteRelation<BigInteger,BigInteger>",
         ["Finite set of integer pairs", "Source and target are the actual registered integer Algebra"], ["Z", "FiniteSet(Z)"]),
        ("ZxZ.relation", "Integer relation pair", "mathematics.foundations.Pair<BigInteger,BigInteger>",
         ["Both coordinates belong to the registered integer Algebra"], ["Z"]),
        ("QxN.iteration", "Rational polynomial iteration inputs", "mathematics.foundations.Pair<Rational,BigInteger>",
         ["Rational initial value", "Nonnegative integer step count; execution separately enforces its resource limit"], ["Q", "N"]),
        ("FiniteSet(Z)", "Finite integer sets", "mathematics.foundations.FiniteSet<BigInteger>",
         ["Finite membership; every member belongs to the registered integer Algebra"], ["Z"]),
        ("Sample(Q)", "Finite rational samples", "mathematics.statistics.RationalSample",
         ["Ordered finite list of rational observations; duplicates retained"], ["Q"]),
        ("FiniteDistribution(Z)", "Finite integer distributions", "mathematics.probability.FiniteDistribution<BigInteger>",
         ["Same outcome Algebra identity", "Finite support with nonnegative rational masses summing exactly to one"],
         ["Z", "Q", "FiniteSet(Z)"]),
    ]
    existing = {d["id"] for d in data["domains"]}
    for identifier, name, representation, invariants, related in descriptors:
        if identifier in existing:
            continue
        data["domains"].append({
            "id": identifier, "name": name, "description": name, "member_representation": representation,
            "membership_rules": invariants, "parent_domains": [], "subdomains": [],
            "related_domains": related, "supported_operations": [], "invariants": invariants,
            "references": ["https://leanprover-community.github.io/mathlib4_docs/Mathlib.html"],
            "implementation_status": "IMPLEMENTED", "human_review_status": "UNREVIEWED",
            "formal_verification_status": "UNVERIFIED",
            "provenance": {"definition_source": "docs/CONCRETE_ALGEBRAS.md", "reviewer": None,
                           "review_date": None, "review_notes": "No human review performed", "date": DATE}
        })
    runtime = [r for r in data["concepts"] if r.get("runtime_operation_id")]
    for domain in data["domains"]:
        # Participation is exact domain equality, never substring matching (Q != QxQ.bounds).
        domain["supported_operations"] = sorted(r["runtime_operation_id"] for r in runtime
            if domain["id"] in (r["domain_A"], r["domain_B"], r["domain_C"]))
    data["survey_date"] = DATE
    data["classification_basis"] = (
        "Representation status applies only to each record's explicit scope. Catalog-only rows are initial triage, "
        "not established minimality results. Native registrations use Algebra and the existing operations interfaces; "
        "the optional Domain/Structure prototype does not execute those registrations. Implementation, empirical "
        "testing and mathematical proof are independent axes.")
    return data


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    data = json.loads(DATABASE.read_text(encoding="utf-8"))
    with MANIFEST.open(encoding="utf-8-sig") as source:
        rows = list(csv.DictReader(source, delimiter="\t"))
    updated = synchronize(data, rows)
    if args.check:
        if updated != data:
            print("Native catalog metadata is stale; run python tools/sync_native_catalog.py.")
            return 1
    else:
        DATABASE.write_text(json.dumps(updated, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print("Synchronized native facts for %s operations and %s algebra builders." % (len(rows), len(OWNERS)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
