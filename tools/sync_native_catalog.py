"""Synchronize native registration facts; preserve the separately authored survey."""
import argparse
import copy
import csv
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "mathematics-coverage.json"
MANIFEST = ROOT / "groupimp/src/test/resources/mathematics/concrete-catalog.tsv"
DATE = "2026-09-26"
OWNERS = {
    "SimplicialHomotopyEquivalenceAlgebra": ("HomotopyEquivalence", "Opposite simplicial pair maps with supplied contiguity-path inverse witnesses and mutually inverse integral homology/cohomology maps"),
    "SimplicialHomotopyPathAlgebra": ("HomotopyPath", "Finite supplied paths of contiguous simplicial pair maps, chronological concatenation and accumulated integral chain/cochain prisms"),
    "SimplicialHomotopyAlgebra": ("SimplicialHomotopy", "Explicit integral prism witnesses for contiguous absolute and relative simplicial maps, with dual cochain operators"),
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
    "AbelianGroupHomomorphismAlgebra": ("AbelianGroupHomomorphism", "Relation-respecting homomorphisms between retained abelian presentations with canonical Smith-coordinate matrices"),
    "IntegralHomologyAlgebra": ("IntegralHomology", "Constructive integral homology in one degree, retaining consecutive boundaries, an integral cycle basis and a quotient presentation"),
    "FiniteSimplicialMapAlgebra": ("SimplicialMap", "Total simplex-preserving vertex maps between labelled finite complexes, with oriented integral chain matrices and induced homology maps"),
    "RelativeSimplicialAlgebra": ("RelativeComplex", "Labelled simplicial pairs with integral quotient chains, constructive relative homology and long exact sequence maps"),
    "RelativeSimplicialMapAlgebra": ("RelativeMap", "Simplicial maps of labelled pairs with functorial integral relative homology and natural long exact sequence maps"),
    "SimplicialCoverAlgebra": ("SimplicialCover", "Ordered two-subcomplex covers with constructive integral homological and cohomological Mayer-Vietoris sequences, direct sums and simplicial excision maps"),
    "SimplicialCoverMapAlgebra": ("CoverMap", "Maps of ordered two-subcomplex covers with covariant homology, contravariant cohomology, natural Mayer-Vietoris sequences and excision diagrams"),
    "SimplicialCochainAlgebra": ("SimplicialCochain", "Homogeneous integral simplicial cochains with coboundary, cup products, constructive cohomology and contravariant pullbacks"),
    "SimplicialChainAlgebra": ("SimplicialChain", "Homogeneous integral simplicial chains with boundary, pushforward, Kronecker pairing and cap products inducing homology maps"),
    "RelativeSimplicialChainAlgebra": ("RelativeChain", "Integral quotient chains on labelled pairs with connecting cycles, pairing, absolute-cochain action and relative-cochain cap products into absolute homology"),
    "RelativeCapProductAlgebra": ("RelativeCap", "Integral relative cap products with independently supplied cochain and target subcomplexes, retaining a chain context and inducing homology and cohomology maps"),
    "RelativeSimplicialTripleAlgebra": ("RelativeTriple", "Nested labelled simplicial triples with constructive integral homology and cohomology exact sequences and typed connecting representatives"),
    "RelativeSimplicialTripleMapAlgebra": ("TripleMap", "Simplicial maps preserving both nested subcomplexes, with covariant homology, contravariant cohomology and natural exact-sequence diagrams"),
    "RelativeSimplicialCochainAlgebra": ("RelativeCochain", "Integral cochains on labelled pairs with relative cup products, constructive cohomology and natural long exact sequences"),
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
    "SimplicialHomotopyEquivalenceAlgebra": "NativeSimplicialHomotopyEquivalenceTest",
    "SimplicialHomotopyPathAlgebra": "NativeSimplicialHomotopyPathTest",
    "SimplicialHomotopyAlgebra": "NativeSimplicialHomotopyTest",
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
    "AbelianGroupHomomorphismAlgebra": "NativeAbelianHomomorphismTest",
    "IntegralHomologyAlgebra": "NativeConstructiveHomologyTest",
    "FiniteSimplicialMapAlgebra": "NativeSimplicialMapTest",
    "RelativeSimplicialAlgebra": "NativeRelativeHomologyTest",
    "RelativeSimplicialMapAlgebra": "NativeRelativeSimplicialMapTest",
    "SimplicialCoverAlgebra": "NativeMayerVietorisTest",
    "SimplicialCoverMapAlgebra": "NativeSimplicialCoverMapTest",
    "SimplicialCochainAlgebra": "NativeSimplicialCochainTest",
    "SimplicialChainAlgebra": "NativeSimplicialChainTest",
    "RelativeSimplicialChainAlgebra": "NativeRelativeChainTest",
    "RelativeCapProductAlgebra": "NativeRelativeCapProductTest",
    "RelativeSimplicialTripleAlgebra": "NativeRelativeTripleTest",
    "RelativeSimplicialTripleMapAlgebra": "NativeRelativeTripleMapTest",
    "RelativeSimplicialCochainAlgebra": "NativeRelativeCochainTest",
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
    "SimplicialHomotopyEquivalenceAlgebra": {
        "maps": "Collect the ordered forward/backward RelativeMaps as (f,g), using the actual registered product carrier. This collection does not yet require opposite boundaries. Registered as HomotopyEquivalence.maps on RelativeMap.",
        "from-maps": "Consume map pair (f,g) and witness pair (H_X,H_Y). Require opposite full labelled pairs and paths Id_X -> g after f and Id_Y -> f after g, checking exact endpoint vertex maps. Registered as HomotopyEquivalence.from-maps on RelativeMap.pair; output is the actual HomotopyEquivalence wrapper.",
        "identity-on": "Return identity maps on the supplied full RelativeComplex pair with stationary source and target witnesses. Registered as HomotopyEquivalence.identity-on on RelativeComplex.",
        "from-isomorphism": "Require a simplicial isomorphism of the full pair, including its subcomplex. Construct its strict inverse and stationary identity witnesses; a homotopy equivalence alone does not satisfy this constructor.",
        "compose": "Apply the right operand first and require the same full middle pair. For (f,g,H_X,K_Y) followed by (h,k,H_Y,K_Z), return maps h after f and g after k, source path H_X then g H_Y f, and target path K_Z then h K_Y k. One shared budget covers both map compositions, both transported/concatenated paths and final endpoint validation.",
        "inverse": "Swap forward/backward maps and source/target witnesses. Do not reverse either path. This gives a homotopy inverse, whose composites need not equal the strict identity witness.",
        "forward": "Return the retained full RelativeMap f from source to target.",
        "backward": "Return the retained full RelativeMap g from target to source.",
        "source": "Return the full source pair of f, equal to the target pair of g.",
        "target": "Return the full target pair of f, equal to the source pair of g.",
        "source-homotopy": "Return the supplied HomotopyPath from Id_X to g after f; its prism fills composite-minus-identity differences on source cycles.",
        "target-homotopy": "Return the supplied HomotopyPath from Id_Y to f after g; its prism fills composite-minus-identity differences on target cycles.",
        "equal": "Compare both full maps and both complete witness stage lists, including repetitions. Equal forward maps or endpoint pairs alone are insufficient.",
        "forward-homology-map": "For nonnegative n return f_*: H_n(X,A)->H_n(Y,B), retaining integral presentations and torsion; its inverse is the backward homology map.",
        "backward-homology-map": "For nonnegative n return g_*: H_n(Y,B)->H_n(X,A), retaining integral presentations and torsion; its inverse is the forward homology map.",
        "homology-maps": "For nonnegative n emit exactly [f_*,g_*] in forward-then-backward order. Both integral maps share one work budget; the list is immutable and never partially emitted.",
        "forward-cohomology-map": "For nonnegative n return f^*: H^n(Y,B)->H^n(X,A). Pullback reverses geometric direction and retains integral torsion; its inverse is the backward cohomology map.",
        "backward-cohomology-map": "For nonnegative n return g^*: H^n(X,A)->H^n(Y,B). Pullback reverses geometric direction and retains integral torsion; its inverse is the forward cohomology map.",
        "cohomology-maps": "For nonnegative n emit exactly [f^*,g^*], each reversing its geometric map direction. Both integral maps share one work budget; the list is immutable and never partially emitted.",
    },
    "SimplicialHomotopyPathAlgebra": {
        "from-homotopy": "Retain the two ordered endpoint RelativeMaps of a SimplicialHomotopy as a one-step path. Registered as HomotopyPath.from-homotopy on SimplicialHomotopy.",
        "stationary-on": "Retain one supplied RelativeMap as a zero-step path with correctly shaped zero chain/cochain operators. Registered as HomotopyPath.stationary-on on RelativeMap.",
        "append": "Append the next RelativeMap after the current final stage. Require equal full source/target pairs and contiguity in both target components. Keep repeated stages and return the first carrier wrapper.",
        "then": "Chronological concatenation: run the first path, then the second. Require equal full joining maps, including vertex assignments and both pairs; include the shared joining stage once. Endpoint contiguity is not required.",
        "reverse": "Reverse the complete stage list and recompute consecutive prisms. Reversing twice restores the path; the recomputed matrix need not negate the original prism.",
        "from": "Return the first retained RelativeMap.",
        "to": "Return the last retained RelativeMap; it need not be directly contiguous to the first map.",
        "source": "Return the full source pair common to every stage.",
        "target": "Return the full target pair common to every stage.",
        "step-count": "Return the nonnegative number of steps, equal to the number of stages minus one. A stationary path has zero steps.",
        "stages": "Emit all retained RelativeMaps in chronological order, including repeated stages. A stationary path emits one map.",
        "steps": "Emit the consecutive SimplicialHomotopy witnesses in chronological order. A stationary path emits an empty list.",
        "equal": "Compare the complete ordered stage lists, including repetitions, vertex assignments and full pairs. Equal endpoint maps alone do not make paths equal.",
        "chain-matrix": "For nonnegative n, sum every step prism D_n: C_n(source)->C_(n+1)(target). The telescoping identity is boundary D + D boundary = to# - from#. The whole sum uses one work budget.",
        "cochain-matrix": "For nonnegative p, return transpose(D_(p-1)): C^p(target)->C^(p-1)(source). At degree zero retain zero rows and the target C^0 columns. One work budget covers every step.",
        "chain-matrices": "Emit accumulated D_n in ascending degrees zero through the maximum quotient dimension d. Share one budget across every degree and every step, without partial lists.",
        "cochain-matrices": "Emit transpose(D_(p-1)) in ascending degrees zero through d+1, where d is the maximum quotient dimension. Include the degree-zero zero-row matrix and share one budget across every degree and every step.",
        "on-chain": "Require a RelativeChain on the full source pair. Apply D, raise degree by one and return the full target pair through ILeftProjectionOperation. Negative-degree zero chains are allowed; cycles give explicit fillings of to#c - from#c.",
        "on-cochain": "Require a positive-degree RelativeCochain on the full target pair. Apply the transpose of D, lower degree by one and return the full source pair through ILeftProjectionOperation. Cocycles give primitives of to*phi - from*phi; for degree zero use cochain-matrix.",
        "on-absolute-chain": "Require empty subcomplexes at both boundaries and a SimplicialChain on the full source ambient complex. Apply D and return the target SimplicialChain wrapper with degree raised by one.",
        "on-absolute-cochain": "Require empty subcomplexes at both boundaries and a positive-degree SimplicialCochain on the full target ambient complex. Return the source SimplicialCochain wrapper with degree lowered by one.",
        "precompose": "Compose each stage after the supplied RelativeMap, checking the full middle pair. Revalidate the path with one shared composition/validation budget. Recompute prisms from the resulting vertex maps; these need not equal the old prism multiplied by the supplied chain matrix.",
        "postcompose": "Compose the supplied RelativeMap after every stage, checking the full middle pair. Revalidate the path with one shared composition/validation budget. The recomputed prism agrees with postcomposition by the target chain map.",
    },
    "SimplicialHomotopyAlgebra": {
        "between": "Require two RelativeMaps with the same full source and target pairs, contiguous in both the ambient target and its subcomplex. Retain ordered endpoints from and to. Registered on RelativeMap as SimplicialHomotopy.between.",
        "between-absolute": "Require two SimplicialMaps with the same full source and target complexes and contiguous simplex images. Retain them as maps of pairs with empty subcomplexes. Registered on SimplicialMap as SimplicialHomotopy.between-absolute.",
        "from": "Return the retained initial RelativeMap, including the full source and target pairs.",
        "to": "Return the retained final RelativeMap, including the full source and target pairs.",
        "source": "Return the full source pair shared by both endpoint maps.",
        "target": "Return the full target pair shared by both endpoint maps.",
        "reverse": "Exchange the endpoints and recompute the prism on demand. This is generally not the negative of the original matrix, though both satisfy their respective chain-homotopy identities.",
        "equal": "Compare both ordered endpoint RelativeMaps with their full labelled pairs and vertex assignments.",
        "chain-matrix": "In nonnegative degree n, return P_n: C_n(source)->C_(n+1)(target). Sum oriented front-from/back-to simplices, omit repeated vertices and target-subcomplex simplices. Boundary P + P boundary equals to# - from#.",
        "cochain-matrix": "In nonnegative degree p, return Q^p=transpose(P_(p-1)): C^p(target)->C^(p-1)(source). At p=0 preserve zero rows and the target C^0 column count. Coboundary Q + Q coboundary equals to* - from*.",
        "chain-matrices": "Emit P_n in ascending degrees zero through the maximum quotient dimension of the source and target pairs. Empty quotient complexes give an empty list. One budget covers the entire list.",
        "cochain-matrices": "Emit Q^p in ascending degrees zero through one plus the maximum quotient dimension. Include Q^0 with its zero row count; empty quotient complexes give one 0-by-0 matrix. One budget covers the entire list.",
        "on-chain": "Require the full source pair. Return P applied to the chain on the full target pair, raising degree by one, through ILeftProjectionOperation and the actual second carrier wrapper. Negative-degree zero chains remain valid. For cycles this fills the difference to#c - from#c.",
        "on-cochain": "Require the full target pair and positive degree. Return Q applied to the cochain on the full source pair, lowering degree by one, through ILeftProjectionOperation. For cocycles its coboundary equals to*phi - from*phi. At degree zero use cochain-matrix because typed cochains exclude degree minus one.",
        "on-absolute-chain": "Require empty subcomplexes in both endpoint pairs and a chain on the full source ambient complex. Return the target SimplicialChain with degree raised by one through the actual second carrier wrapper.",
        "on-absolute-cochain": "Require empty subcomplexes in both endpoint pairs and a positive-degree cochain on the full target ambient complex. Return the source SimplicialCochain with degree lowered by one through the actual second carrier wrapper.",
    },
    "RelativeSimplicialTripleMapAlgebra": {
        "from-map": "Require the ambient simplicial map to have exactly the full X and Y of the supplied ordered source/target triples. Validate A maps into C and B maps into D, retaining three compatible pair maps. Registered on SimplicialMap as TripleMap.from-map.",
        "compose": "Apply the right operand first. All three full labelled components of the middle triple must agree; composition and validation share one work budget.",
        "inverse": "Require an ambient simplicial isomorphism whose inverse also preserves both nested subcomplexes. An ambient bijection alone does not suffice.",
        "source": "Return the full retained source triple (X,A,B).",
        "target": "Return the full retained target triple (Y,C,D).",
        "ambient-map": "Return the validated full ambient simplicial map X->Y.",
        "outer-map": "Return the actual RelativeMap (X,A)->(Y,C), suitable for native quotient-chain pushforward and relative-cochain pullback.",
        "total-map": "Return the actual RelativeMap (X,B)->(Y,D), retaining both full labelled pairs.",
        "inner-map": "Return the actual RelativeMap (A,B)->(C,D), using the restricted ambient vertex map.",
        "equal": "Compare full source and target triples and all ambient vertex images, not only their induced maps.",
        "is-isomorphism": "True exactly for an ambient simplicial isomorphism surjective on both middle and base subcomplexes; ambient vertex bijectivity alone is insufficient.",
        "identity-on": "Construct the identity preserving every component of the supplied full triple.",
        "inclusion": "Require labelled inclusions of all three source components into the corresponding target components and return their common inclusion map.",
        "contiguous": "Require the same full source and target triples. Check simplex image unions separately in the target ambient, middle and base complexes with one shared budget. Triple contiguity implies equal induced integral maps but is not a general homotopy decision.",
        "image": "Return the triple (f(X),f(A),f(B)) of simplex-wise images, retaining the nested inclusions rather than replacing either smaller image by an intersection with the target.",
        "corestrict-image": "Return the same vertex assignment with target exactly the image triple; its inclusion into the old target composes to the original map.",
        "restrict": "Require a subtriple included componentwise in the full source triple. Restrict the vertex assignment, retain the original full target and return the first TripleMap wrapper.",
        "outer-homology-map": "Return the covariant H_k(X,A)->H_k(Y,C) induced map in nonnegative degree, retaining integer presentations and torsion.",
        "total-homology-map": "Return the covariant H_k(X,B)->H_k(Y,D) induced map in nonnegative degree, retaining integer presentations and torsion.",
        "inner-homology-map": "Return the covariant H_k(A,B)->H_k(C,D) induced map in nonnegative degree, retaining integer presentations and torsion.",
        "long-exact-maps": "Emit four source-to-target maps on inner degree k, total degree k, outer degree k and inner degree k-1, in that order. They commute with the three homology exact-sequence arrows. At k=0 retain the typed degree-minus-one presentations at both ends. One shared budget covers the entire list.",
        "outer-cohomology-map": "Return the contravariant H^k(Y,C)->H^k(X,A) induced map in nonnegative degree. Preserve the full quotient presentations, which may include killed Smith coordinates.",
        "total-cohomology-map": "Return the contravariant H^k(Y,D)->H^k(X,B) induced map in nonnegative degree, retaining full integral presentations rather than reducing to group rank.",
        "inner-cohomology-map": "Return the contravariant H^k(C,D)->H^k(A,B) induced map in nonnegative degree, retaining full integral presentations and torsion.",
        "long-exact-cohomology-maps": "Emit four target-to-source maps on outer degree k, total degree k, inner degree k and outer degree k+1, in that order. They commute with the three cohomology exact-sequence arrows. One shared budget covers the entire list and every quotient model it constructs.",
    },
    "RelativeSimplicialTripleAlgebra": {
        "from-pair": "Construct a triple from the full outer pair (X,A) and a labelled base subcomplex B. Require B contained in A; retain outer (X,A), total (X,B) and inner (A,B). Registered on RelativeComplex as RelativeTriple.from-pair.",
        "outer-pair": "Return the complete retained outer pair (X,A), including all labelled simplices.",
        "total-pair": "Return the complete retained total pair (X,B).",
        "inner-pair": "Return the complete retained inner pair (A,B).",
        "equal": "Compare all labelled complexes X, A and B; equality of ranks or isomorphism types is insufficient.",
        "inclusion-map": "Return the actual RelativeMap (A,B)->(X,B) induced by labelled inclusion, retaining both full pairs.",
        "quotient-map": "Return the actual RelativeMap (X,B)->(X,A) induced by the identity on X and inclusion B into A.",
        "inclusion-matrix": "In nonnegative degree k, include quotient simplex coordinates C_k(A,B)->C_k(X,B). Both bases are filtered modulo B before dimension limits.",
        "quotient-matrix": "In nonnegative degree k, discard A/B coordinates from C_k(X,B) to obtain C_k(X,A). Both quotient bases are filtered before limits.",
        "lift-matrix": "Insert zeros on A/B into C_k(X,B). This section of the quotient is generally not a chain map; it retains the same labelled triples and nonnegative degree.",
        "connecting-chain-matrix": "Take the (A,B) coordinates of the boundary of a zero-on-A/B lift of a chain on (X,A). This C_k(X,A)->C_(k-1)(A,B) matrix maps cycles to cycles and is zero for k=0; arbitrary noncycles need not map to cycles.",
        "inclusion-homology": "Return H_k(A,B)->H_k(X,B), using the filtered inclusion matrix and one shared budget for both integral quotient models and induction.",
        "quotient-homology": "Return H_k(X,B)->H_k(X,A), using the filtered projection and one shared budget for both integral quotient models and induction.",
        "connecting-homology": "Return H_k(X,A)->H_(k-1)(A,B), taking the boundary of a lift modulo B. For k=0 the target is the zero group with the same retained presentation as a typed degree-minus-one chain on (A,B), which need not equal a canonical zero presentation.",
        "long-exact-segment": "Emit three maps in order: inclusion, quotient, connecting, from H_k(A,B) through H_k(X,B), H_k(X,A), H_(k-1)(A,B). One budget covers all four quotient models and the entire list; no partial list is returned on exhaustion.",
        "connect-cycle": "Require a relative cycle on the full outer pair (X,A). Return its lifted boundary modulo B as a RelativeChain on the full inner pair (A,B), lowering the integer degree. Negative-degree inputs are zero. Return the second operand carrier's IAlgebraItem through ILeftProjectionOperation.",
        "extension-matrix": "Transpose the quotient matrix to extend C^k(X,A)->C^k(X,B) by zero on A/B; this is a cochain map.",
        "restriction-matrix": "Transpose the inclusion matrix to restrict C^k(X,B)->C^k(A,B); this is a cochain map.",
        "connecting-cochain-matrix": "Transpose the connecting chain matrix in degree k+1, giving C^k(A,B)->C^(k+1)(X,A) from zero extension followed by coboundary. It induces the connecting homomorphism on cocycles.",
        "extension-cohomology": "Return H^k(X,A)->H^k(X,B), retaining both integral presentations and one shared budget across models and induction.",
        "restriction-cohomology": "Return H^k(X,B)->H^k(A,B), retaining both integral presentations and one shared budget across models and induction.",
        "connecting-cohomology": "Return H^k(A,B)->H^(k+1)(X,A), with integral torsion and one shared budget across both quotient models and induction.",
        "long-exact-cohomology-segment": "Emit extension, restriction, connecting in order through H^k(X,A), H^k(X,B), H^k(A,B), H^(k+1)(X,A). One budget covers all four quotient models and the entire list; consecutive images equal kernels over Z.",
        "connect-cocycle": "Require a relative cocycle on the full inner pair (A,B). Extend by zero on X outside A, take coboundary modulo the pair data and return a RelativeCochain on the full outer pair (X,A) in degree k+1, using the second operand carrier's IAlgebraItem wrapper.",
    },
    "RelativeCapProductAlgebra": {
        "on": "Bind a RelativeChain on (X,D) to an explicit target pair (X,B). Require identical full labelled ambient complexes and B contained in D. This constructor is registered on the source chain algebra as RelativeCap.on.",
        "chain": "Return the retained RelativeChain, including its full source pair (X,D), integer degree and coordinates.",
        "target-pair": "Return the explicitly retained target (X,B); B is never inferred by subtracting subcomplexes.",
        "with-chain": "Require the same full source pair (X,D); replace the chain's coordinates and possibly its integer degree, retaining the target pair in the first RelativeCap wrapper.",
        "boundary": "Replace the stored chain by its quotient boundary, lowering degree by one and retaining both full pairs. This original unary operation does not require a cycle.",
        "equal": "Compare the complete retained chain and target pair, including labels, degree and coordinates.",
        "cap": "Require the cochain pair (X,A) to have the same full X and D exactly equal to A union B. Evaluate front faces outside A, retain back faces outside B and return a RelativeChain on (X,B) of degree n-p. A and B may overlap or be empty; their simplex-wise union is required.",
        "cap-class": "Require compatible full pairs, D exactly equal to A union B, a relative cycle and a relative cocycle. Return their class in H_(n-p)(X,B), independent of representatives. Validation, cap, quotient model and projection share one work budget.",
        "cap-matrix": "Require compatible full pairs and D exactly equal to A union B. Fix the supplied cochain and return C_n(X,D)->C_(n-p)(X,B); ignore the stored chain coordinates, retaining its source pair and degree. No cycle or cocycle condition is imposed.",
        "cap-homology-map": "Require compatible full pairs, D exactly equal to A union B and a cocycle. Ignore the stored chain coordinates. Return H_n(X,D)->H_(n-p)(X,B), with both quotient models and induction sharing one work budget.",
        "cap-cohomology-matrix": "Require compatible full pairs and D exactly equal to A union B. Fix the stored chain; use only the template cochain pair and nonnegative degree, ignoring its coordinates. Return C^p(X,A)->C_(n-p)(X,B), without cycle or cocycle requirements.",
        "cap-cohomology-map": "Require compatible full pairs, D exactly equal to A union B and a relative cycle. The template supplies only its pair and degree; its coordinates are ignored and it need not be a cocycle. Return H^p(X,A)->H_(n-p)(X,B), sharing one work budget across both quotient models and induction. No manifold or fundamental-class certificate is inferred.",
    },
    "RelativeSimplicialChainAlgebra": {
        "zero-on": "Construct a zero quotient chain on the supplied full pair and integer degree. Negative-degree groups are zero under the unreduced convention.",
        "basis-on": "Emit positive simplex basis chains outside A, in increasing-vertex lexicographic order. Filter out A before checking the basis limit; negative and above-dimension degrees give empty lists.",
        "absolute": "Embed an absolute SimplicialChain as a relative chain on (X,empty), retaining degree and coordinates.",
        "from-absolute": "Require the absolute chain's full complex to equal X, then discard all coefficients on A. This quotient projection commutes with boundary; unlike relative cochain conversion, the input need not vanish on A.",
        "add": "Require the same full labelled pair and integer degree, then add integral coordinates.",
        "subtract": "Require the same full labelled pair and integer degree, then subtract integral coordinates.",
        "negate": "Negate all coefficients and retain the pair and degree.",
        "scale": "Scale integral coordinates by the second integer operand and return the first relative chain wrapper.",
        "equal": "Compare the full pair, integer degree and quotient-chain coordinates; equal ambient complexes alone do not suffice.",
        "pair": "Return the complete retained pair (X,A), including all labelled simplices.",
        "degree": "Return the integer degree, including negative degrees of zero chains.",
        "coordinates": "Return integral coordinates on the oriented simplices of X outside A.",
        "with-coordinates": "Require exactly the quotient-basis dimension, retaining the pair and degree in the first wrapper.",
        "is-zero": "True exactly when every quotient coordinate vanishes; this is distinct from having zero homology class.",
        "boundary": "Take the alternating oriented boundary modulo A and lower degree by one. Zero-chain boundaries lie in the zero group in degree -1.",
        "is-cycle": "True exactly when the quotient boundary is zero. Its zero-on-A lift can have nonzero absolute boundary in A.",
        "is-boundary": "Test integral solvability of the next relative boundary matrix, retaining torsion and integer lattice information.",
        "bounding-coordinates": "Return one relative filling in the next degree when it exists over Z. Undefined if there is no integral filling; torsion multiples can admit fillings.",
        "homologous": "Require two relative cycles with the same full pair and degree, and test whether their difference is an integral relative boundary with one shared work budget.",
        "homology": "Return the retained IntegralHomology quotient model for the pair and integer degree; negative-degree homology is zero. Adjacent quotient bases are filtered before their bounds.",
        "class-of": "Require a relative cycle and return its class in the retained integral presentation, sharing one budget across construction and projection.",
        "representative": "The input class must belong to this exact relative homology presentation. Return one relative cycle retaining this full pair and degree; the representative section need not be additive.",
        "cycle-generators": "Emit representatives of minimal Smith generators, nontrivial torsion first then free generators. One work budget covers the model and entire generator list.",
        "pushforward": "Require the full pair to equal the RelativeMap source. Apply its oriented quotient chain matrix; collapsed simplices and images lying in the target subcomplex vanish. Retain the complete target pair and degree.",
        "evaluate": "Pair with a RelativeCochain on the same full pair in equal degree by dot product. This pairing is adjoint to relative boundary/coboundary and pushforward/pullback.",
        "lift-absolute": "Insert zero coefficients on A to obtain an absolute SimplicialChain. This is a chain-group section, generally not a chain map, and requires the full ambient basis to fit.",
        "connect-cycle": "Require a relative cycle; take the boundary of its zero-on-A lift and return its coordinates as a SimplicialChain on the full subcomplex A in degree n-1. In positive source degree its class realizes the retained connecting homomorphism; in nonpositive source degrees the resulting cycle is zero under the unreduced convention. The chosen lift need not be natural on chains.",
        "cap": "Cap with a SimplicialCochain on the full ambient X by the front/back formula. Return a RelativeChain on the same pair in degree n-p; back faces lying in A vanish in this relative target.",
        "cap-class": "Require a relative cycle and an absolute cocycle on the same ambient X, returning their product in H_(n-p)(X,A). Validation, cap, model and projection share one work budget.",
        "relative-cap": "Require a RelativeCochain on the same full pair. Front faces in A have zero cochain value, so the product is well-defined in absolute SimplicialChain on X; back faces in A must be retained. Degree n-p can be negative with zero result.",
        "relative-cap-class": "Require a relative cycle and relative cocycle on the same pair. Return the class in absolute H_(n-p)(X), independent of both representatives, with one shared budget.",
        "cap-matrix": "Use the first chain's pair and integer source degree but ignore its coordinates. For the supplied absolute cochain return C_n(X,A)->C_(n-p)(X,A), without requiring a cocycle.",
        "cap-homology-map": "Require the supplied absolute cochain to be a cocycle; the first chain supplies only pair and source degree. Return H_n(X,A)->H_(n-p)(X,A), retaining integral presentations and one shared budget for both models and induction.",
        "cap-cohomology-matrix": "Fix the first relative n-chain and use the second nonnegative cochain degree p. Return C^p(X)->C_(n-p)(X,A) without requiring a cycle.",
        "cap-cohomology-map": "Require the first relative n-chain to be a cycle. Return H^p(X)->H_(n-p)(X,A) in the second nonnegative degree, with both models and induction sharing one budget. No fundamental-class or manifold claim is inferred.",
        "relative-cap-matrix": "Use the first chain's pair and integer source degree but ignore its coordinates. For a relative cochain on the same pair return C_n(X,A)->C_(n-p)(X), without requiring a cocycle.",
        "relative-cap-homology-map": "Require a relative cocycle on the same pair; the first chain supplies only pair and source degree. Return H_n(X,A)->H_(n-p)(X), with retained integral presentations and one work budget across both models and induction.",
        "relative-cap-cohomology-matrix": "Fix the first relative n-chain and use nonnegative p. Return C^p(X,A)->C_(n-p)(X) without requiring a cycle, retaining the full absolute target basis.",
        "relative-cap-cohomology-map": "Require the first relative n-chain to be a cycle. Return H^p(X,A)->H_(n-p)(X), retaining presentations. Both models and induction share one budget; the result need not be an isomorphism or certify Lefschetz duality.",
    },
    "SimplicialChainAlgebra": {
        "zero-on": "Construct the zero chain on the full labelled complex in the supplied integer degree. Negative degrees have the zero group under the unreduced convention.",
        "basis-on": "Emit the positively oriented simplex basis in lexicographic increasing-vertex order. Negative degrees and degrees above the complex dimension give empty lists.",
        "add": "Require the same full complex and degree, then add integral coordinates. Equal coordinate dimensions alone do not suffice.",
        "subtract": "Require the same full complex and degree, then subtract integral coordinates.",
        "negate": "Negate all integral coefficients and retain the complex and degree.",
        "scale": "Scale integral coefficients by the second integer operand and retain the first chain carrier.",
        "equal": "Compare the full labelled complex, integer degree and integral coordinates.",
        "complex": "Return the retained full finite complex, including simplices outside the chain's support.",
        "degree": "Return the integer degree, including negative degrees of zero chains.",
        "coordinates": "Return integral coordinates in the lexicographic increasing-vertex simplex basis.",
        "with-coordinates": "The new vector must have exactly the required simplex-basis dimension; retain the same complex and degree and return the first chain wrapper.",
        "is-zero": "True exactly when all retained coefficients vanish. It does not assert that a nonzero chain represents a nonzero homology class.",
        "boundary": "Apply the alternating oriented boundary and lower degree by one. Zero-chain boundaries lie in the zero group in degree -1; this is not the augmented complex.",
        "is-cycle": "True exactly when the oriented boundary is zero. Resource exhaustion raises IMPLEMENTATION_FAILURE instead of returning false.",
        "is-boundary": "Test integral solvability of the next boundary matrix against this chain. Retain integer lattice information, including torsion.",
        "bounding-coordinates": "Return coordinates of one chain in the next degree with this boundary. Undefined when no integral filling exists; a torsion multiple may have a filling even when the original cycle does not.",
        "homologous": "Require the same full complex and degree and two cycles, then test whether their difference is an integral boundary with a shared work budget.",
        "homology": "Return the constructive IntegralHomology model for this complex and degree with both adjacent boundaries and integral quotient presentation. Negative-degree homology is zero.",
        "class-of": "Require a cycle and return its class in the retained integral homology presentation. Model construction and class projection share one work budget.",
        "representative": "The input class must belong to this exact retained homology presentation. Return one cycle representative with this chain's full complex and degree; the section need not be additive.",
        "cycle-generators": "Emit chain representatives of minimal Smith generators, nontrivial torsion first then free generators. One work budget covers the model and entire list; zero homology gives an empty list.",
        "pushforward": "Require the chain's full complex to equal the simplicial map source. Use oriented chain matrices; collapsed simplices map to zero. Retain the full target complex and the same integer degree.",
        "evaluate": "Pair a chain and cochain on the same full complex and in equal degrees by integral dot product. On cycles and cocycles this is the Kronecker pairing; boundary and coboundary are adjoint.",
        "cap": "For each n-simplex evaluate the p-cochain on its first p+1 vertices and retain its last n-p+1 vertices. Require the same full complex. The result is a chain of degree n-p in the first carrier; p>n gives zero in negative degree.",
        "cap-class": "Require a cycle and a cocycle on the same full complex. Return the class of their cap product in H_(n-p), independent of cycle and cocycle representatives. Validation, cap calculation, model and projection share one work budget.",
        "cap-matrix": "The first operand is a cochain and the second an integer chain degree n. Return the matrix C_n->C_(n-p) for cap with the fixed p-cochain, without requiring a cocycle. This operation is registered on the cochain carrier with the SimplicialChain prefix.",
        "cap-homology-map": "Require the first cochain to be a cocycle. In the second integer degree n, return H_n->H_(n-p) with retained integral presentations. Both models and induction share one work budget. The map depends only on the cocycle class.",
        "cap-cohomology-matrix": "Fix the first n-chain and use the second nonnegative degree p. Return the matrix C^p->C_(n-p) without requiring a cycle; rows and columns retain the corresponding simplex bases.",
        "cap-cohomology-map": "Require the first n-chain to be a cycle. Return H^p->H_(n-p) for the second nonnegative degree p, sharing one work budget across both models and induction. No manifold or fundamental-class assumption is inferred, and the map need not be an isomorphism.",
        "augmentation": "Sum the coefficients of a zero-chain. Undefined in every other degree; this is a separate operation and does not change the unreduced boundary.",
    },
    "RelativeSimplicialCochainAlgebra": {
        "zero-on": "Construct the zero cochain on the retained pair (X,A) in a nonnegative degree, using only simplices outside A. Above the ambient dimension retain the requested degree with empty coordinates.",
        "basis-on": "Emit the coordinate cochains dual to the ordered quotient-chain basis of X outside A. Filter out A before applying the basis-size limit.",
        "absolute": "Regard a SimplicialCochain on X as a relative cochain on (X,empty), retaining its degree and coordinates.",
        "from-absolute": "The cochain must retain exactly X and vanish on every A simplex in its degree. Select the coordinates outside A; a nonvanishing restriction is undefined.",
        "add": "Both full labelled pairs and degrees must agree; add integral coordinates.",
        "subtract": "Both full labelled pairs and degrees must agree; subtract the second coordinates from the first.",
        "cup": "Require equal full ambient complexes. The product C^p(X,A) times C^q(X,B) lands in C^(p+q)(X,A union B). Use the increasing-vertex Alexander-Whitney formula with zero on missing input faces; the two subcomplexes may differ.",
        "negate": "Negate all coordinates and retain the pair and degree.",
        "scale": "Multiply by the supplied integer and retain the first relative cochain wrapper.",
        "equal": "Compare the full labelled pair, degree and coordinates, not only ambient complex or cohomology class.",
        "pair": "Return the retained labelled relative pair (X,A).",
        "degree": "Return the retained nonnegative degree, including arbitrarily large degrees with zero coordinates.",
        "coordinates": "Return the vector dual to the lexicographic increasing-vertex simplices of X outside A in the retained degree.",
        "with-coordinates": "The vector must have exactly the relative basis dimension; replace coordinates in the first relative cochain wrapper.",
        "coboundary": "Apply the transpose of the relative degree-(k+1) boundary and raise degree. Consecutive relative coboundaries compose to zero.",
        "is-zero": "Check that all relative coordinates are zero.",
        "is-cocycle": "Check whether the relative coboundary is zero over the integers.",
        "is-coboundary": "Test integral solvability against the transpose of the relative degree-k boundary. In degree zero only the zero cochain is a coboundary.",
        "cohomologous": "Require two cocycles on exactly the same full pair and degree. Their difference must be an integral relative coboundary; noncocycles are undefined.",
        "cohomology": "Return the existing IntegralHomology kernel/quotient model with transposed relative boundaries in reversed order, retaining integral torsion and the actual relative cochain basis.",
        "class-of": "Require a relative cocycle and project to its retained presented group. One budget covers the cohomology construction and projection.",
        "representative": "The element must belong to exactly the retained relative cohomology presentation. Return one representative in the first relative cochain wrapper; the chosen section need not be additive.",
        "cobounding-coordinates": "Return one integral primitive in degree k-1 when the cochain is a coboundary. In degree zero only zero has a primitive, the empty vector for C^(-1)=0.",
        "cocycle-generators": "Emit representatives of minimal Smith generators, nontrivial torsion first then free generators. These generate relative cohomology, not the full cocycle lattice. One work budget covers construction and all representatives.",
        "cup-class": "Require two cocycles with the same full ambient X, possibly vanishing on different A and B. Return their product class on (X,A union B) in degree p+q, independently of representatives. One work budget covers validation, product and projection.",
        "evaluate": "Pair with relative integer chain coordinates of the matching dimension. A raw vector does not carry or validate another pair's labels.",
        "pullback": "The cochain's full pair must equal the RelativeMap target. Transpose its relative chain matrix to obtain a cochain on the source. Collapsed simplices and images in the target subcomplex give zero.",
        "extend-by-zero": "Insert zeros on A to obtain an absolute SimplicialCochain on X. This relative-to-absolute inclusion is a cochain map and requires the full ambient basis to fit.",
        "connect-cocycle": "Require a cocycle on exactly A. Extend by zero outside A, apply coboundary and retain coordinates outside A, producing a relative cocycle in degree k+1 with no extra sign.",
        "cohomology-on": "Construct unreduced integral relative cohomology in the supplied nonnegative degree, using the dual quotient bases.",
        "cohomology-degrees": "Emit relative cohomology models from zero through the ambient dimension. The entire list shares one work budget, including all Smith reductions. The empty ambient complex gives an empty list.",
        "extension-matrix": "Transpose the quotient chain projection: C^k(X,A)->C^k(X), inserting zeros on A. Both required bases must fit the dimension limit.",
        "restriction-matrix": "Transpose the chain inclusion: C^k(X)->C^k(A), selecting the subcomplex coordinates.",
        "connecting-cochain-matrix": "Transpose the degree-(k+1) homology connecting chain matrix to map C^k(A)->C^(k+1)(X,A). It sends cocycles to cocycles; its chosen chain-level formula need not be natural for pair maps.",
        "ambient-cohomology-map": "Return H^k(X,A)->H^k(X) induced by the extension-by-zero cochain inclusion, retaining both integral presentations.",
        "restriction-cohomology-map": "Return H^k(X)->H^k(A) induced by restriction to A, retaining both integral presentations.",
        "connecting-cohomology-map": "Return the degree-raising connecting homomorphism H^k(A)->H^(k+1)(X,A), with one budget across both models and the induced map.",
        "long-exact-segment": "Emit extension, restriction and connecting in that order: H^k(X,A)->H^k(X)->H^k(A)->H^(k+1)(X,A). Consecutive images equal kernels over Z, including torsion. One work budget covers the entire three-map segment.",
        "cohomology-map": "Return the contravariant map H^k(Y,B)->H^k(X,A) induced by (X,A)->(Y,B). Full pair equality is retained; equal ambients alone are insufficient.",
        "cohomology-maps": "Emit contravariant relative cohomology maps from zero through the larger ambient dimension. The entire list shares one work budget and preserves zero presentations.",
        "long-exact-maps": "Emit all four vertical maps from the target pair's cohomology segment to the source pair's segment, on relative k, ambient k, subcomplex k, relative k+1, in that order. All three squares commute on cohomology. One budget covers all four maps.",
    },
    "SimplicialCochainAlgebra": {
        "zero-on": "Construct the zero integral cochain on the supplied full labelled complex in the nonnegative degree. Above the dimension, retain the requested degree with an empty coordinate vector.",
        "unit-on": "Construct the degree-zero cochain equal to one at every vertex. It is a two-sided cup unit; on the empty complex it is also zero.",
        "basis-on": "Emit coordinate cochains dual to the lexicographic increasing-vertex simplex basis in the supplied degree. The list is empty above the dimension.",
        "add": "Both cochains must retain the same labelled complex and degree. Add their integer coordinates.",
        "subtract": "Both cochains must retain the same labelled complex and degree. Subtract the second coordinate vector from the first.",
        "cup": "Require the same labelled complex; degrees p and q may differ. On [v0,...,v(p+q)], multiply the first value on [v0,...,vp] by the second on [vp,...,v(p+q)]. This Alexander-Whitney product is associative but not generally graded commutative on cochains.",
        "negate": "Negate all coordinates, retaining the complex and degree.",
        "scale": "Multiply coordinates by the supplied arbitrary-precision integer, retaining the first cochain wrapper.",
        "equal": "Compare the full labelled complex, degree and coordinate vector. This is cochain equality, not equality modulo coboundaries.",
        "complex": "Return the complete retained labelled complex.",
        "degree": "Return the retained nonnegative degree, including arbitrarily large degrees with zero coordinates.",
        "coordinates": "Return integer coordinates dual to the lexicographic increasing-vertex simplex basis.",
        "with-coordinates": "The vector length must equal the number of simplices in the retained degree. Keep the complex and degree, using the first cochain wrapper.",
        "coboundary": "Apply the transpose of the oriented boundary in degree k+1 and raise the cochain degree by one. Consecutive coboundaries compose to zero.",
        "is-zero": "Check that every coordinate is zero without computing cohomology.",
        "is-cocycle": "Check whether the coboundary is zero using exact integer coordinates.",
        "is-coboundary": "Check integral solvability against the transpose of the degree-k boundary. Rational solvability is insufficient. In degree zero only the zero cochain is a coboundary.",
        "cohomologous": "Require the same complex and degree and two cocycles; noncocycles are undefined. Return whether their difference is an integral coboundary.",
        "cohomology": "Construct ker(delta_k)/im(delta_(k-1)) in the existing IntegralHomology carrier, using transposed boundaries in reversed order. Retain the actual cochain basis and integral quotient presentation.",
        "class-of": "Require a cocycle and project to its retained integral cohomology presentation. Construction and projection share one work budget.",
        "representative": "The supplied AbelianGroupElement must have exactly the cohomology presentation associated with this cochain's complex and degree. Return one cocycle in the first cochain wrapper; the chosen section need not be additive.",
        "cobounding-coordinates": "Require an integral coboundary and return one primitive coordinate vector in degree k-1. At k=0 only zero has a primitive, the empty vector for the zero group C^(-1).",
        "cocycle-generators": "Emit cocycle representatives of minimal Smith generators, nontrivial torsion first then free generators. These generate cohomology, not the full cocycle lattice. One work budget covers construction and all representatives.",
        "cup-class": "Require two cocycles on the same full complex; degrees may differ. Return the class of their cup product in degree p+q with one shared work budget. The result is independent of cocycle representatives and graded commutative on cohomology.",
        "evaluate": "Pair with an integer chain coordinate vector of exactly the retained degree's dimension. The chain need not be a cycle, and no differently labelled complex is inferred from the raw vector.",
        "pullback": "The cochain's full complex must equal the map target. Apply the transpose of the oriented simplicial chain matrix, returning a cochain on the source in the first carrier wrapper. Collapsed simplices give zero in positive degree; composition reverses.",
        "cohomology-on": "Construct unreduced integral cohomology of the supplied complex in a nonnegative degree as an IntegralHomology value with outgoing delta_k and incoming delta_(k-1).",
        "cohomology-degrees": "Emit constructive integral cohomology from degree zero through the complex dimension. The entire list shares one work budget, including all Smith reductions; the empty complex gives an empty list.",
        "cohomology-map": "Return the contravariant homomorphism H^k(target)->H^k(source), induced by the transpose of the simplicial chain matrix. Both cohomology constructions and the induced map share one work budget.",
        "cohomology-maps": "Emit contravariant homomorphisms from degree zero through the larger boundary dimension. The entire list shares one work budget and retains zero presentations; two empty boundaries give an empty list.",
    },
    "SimplicialCoverMapAlgebra": {
        "sum-cochain-matrix": "Transpose the oriented block chain matrix to pull back cochains from target pieces to source pieces. Coordinates are left then right, and combined ranks must fit the matrix bound.",
        "sum-cochain-matrices": "Emit contravariant sum cochain matrices from zero through the larger union dimension, retaining zero-sized shapes. The entire list shares one work budget; two empty unions give an empty list.",
        "sum-cohomology-map": "Return the contravariant map from target sum cohomology to source sum cohomology, retaining actual integral presentations including torsion. Both models and induction share one work budget; composition reverses order.",
        "sum-cohomology-maps": "Emit contravariant sum cohomology maps from zero through the larger union dimension. All model constructions and induced maps share one budget across the entire list; exhaustion produces no partial list.",
        "source-sum-cohomology": "Return the source cover's sum cohomology model in the nonnegative degree. This is the codomain model of the contravariant sum cohomology map.",
        "target-sum-cohomology": "Return the target cover's sum cohomology model in the nonnegative degree. This is the domain model of the contravariant sum cohomology map.",
        "union-cohomology-map": "Return H^k(U')->H^k(U), induced contravariantly by the retained union map, with integral presentations.",
        "intersection-cohomology-map": "Return H^k(I')->H^k(I), induced contravariantly by the map of actual intersections.",
        "left-cohomology-map": "Return H^k(A')->H^k(A), compatible with the sum cohomology inclusion and projection maps.",
        "right-cohomology-map": "Return H^k(B')->H^k(B), compatible with the sum cohomology inclusion and projection maps.",
        "long-exact-cohomology-maps": "Emit four target-to-source vertical maps on H^k(U), sum cohomology, H^k(I), H^(k+1)(U), in order. All three cohomological Mayer-Vietoris squares commute. One work budget covers all four maps; no artificial H^-1 term is used.",
        "from-map": "The ambient SimplicialMap must have exactly the supplied source and target unions. It must carry every simplex of A into A' and every simplex of B into B'; checking vertices alone is insufficient. The boundary pair contains source cover first.",
        "compose": "Apply the right operand first. The complete ordered middle covers must agree, not only their unions.",
        "inverse": "The union map must be a simplicial isomorphism and carry both ordered pieces onto the corresponding target pieces. An ambient isomorphism alone is insufficient.",
        "source": "Return the complete ordered source cover.",
        "target": "Return the complete ordered target cover.",
        "union-map": "Return the retained simplicial map U->U' on the full unions.",
        "left-map": "Return the simplicial restriction A->A', retaining the full target left piece.",
        "right-map": "Return the simplicial restriction B->B', retaining the full target right piece.",
        "intersection-map": "Return the simplicial restriction I->I', where each intersection is that of its actual cover pieces.",
        "equal": "Compare full ordered source and target covers and the total union vertex map.",
        "is-isomorphism": "True exactly when the union map is a simplicial isomorphism and both ordered target pieces are the images of their source pieces.",
        "identity-on": "Construct the identity on the supplied ordered cover, including its full labelled pieces.",
        "inclusion": "Require A contained in A' and B contained in B' simplex by simplex, and use the identity on source vertex labels.",
        "swap": "Swap both source and target pieces simultaneously while retaining the union vertex map. This is an involution preserving composition.",
        "sum-chain-matrix": "Return the block diagonal of the left and right oriented chain matrices in the nonnegative degree, with left coordinates first. Combined source and target ranks must fit the matrix bound.",
        "sum-chain-matrices": "Emit sum chain matrices from degree zero through the larger union dimension, retaining zero-sized shapes. The entire list shares one work budget; two empty unions give an empty list.",
        "sum-homology-map": "Induce the map between the actual source and target block-chain homology presentations. Both homology constructions and the induced map share one work budget.",
        "sum-homology-maps": "Emit sum homology maps from degree zero through the larger union dimension. The entire list shares one work budget, including all homology constructions; exhaustion returns no partial list.",
        "source-sum-homology": "Return the source cover's IntegralHomology of C(A) direct-sum C(B) in the supplied nonnegative degree.",
        "target-sum-homology": "Return the target cover's IntegralHomology of C(A') direct-sum C(B') in the supplied nonnegative degree.",
        "union-homology-map": "Return H_k(U)->H_k(U') induced by the retained union simplicial map.",
        "intersection-homology-map": "Return H_k(I)->H_k(I') induced by the restriction to the cover intersections.",
        "left-homology-map": "Return H_k(A)->H_k(A') induced by the left restriction, compatible with the sum inclusion and projection maps.",
        "right-homology-map": "Return H_k(B)->H_k(B') induced by the right restriction, compatible with the sum inclusion and projection maps.",
        "long-exact-maps": "Emit all four vertical maps on H_k(I), sum homology, H_k(U), H_(k-1)(I), in order, between the two Mayer-Vietoris segments. All three squares commute on homology. At k=0 the fourth map is 0->0. One work budget covers all four maps.",
        "left-relative-map": "Return the actual RelativeMap (A,I)->(A',I') induced by the left restriction.",
        "union-relative-map": "Return the actual RelativeMap (U,B)->(U',B') induced by the union map.",
        "excision-maps": "Emit the left-relative and union-relative maps, in that order, sharing one construction budget. They commute with the excision inclusions as RelativeMap values, hence also on relative chains and homology.",
        "contiguous": "Require equal ordered source and target covers, then check contiguity in each target piece. Ambient contiguity alone is insufficient. Piecewise contiguity implies equal induced homology maps.",
        "image": "Return the ordered cover (f(A),f(B)). Its intersection is f(A) intersection f(B), which can strictly contain f(I).",
        "corestrict-image": "Keep the complete source cover and vertex map, replacing the target by the actual image cover (f(A),f(B)).",
        "restrict": "The supplied cover must be contained componentwise in the source cover. Restrict the union vertex map and retain the full original target, using the first CoverMap wrapper.",
    },
    "SimplicialCoverAlgebra": {
        "sum-coboundary-matrix": "Return the transpose of the sum boundary in degree k+1, the differential on C^k(A) direct-sum C^k(B). Degree k is nonnegative; left coordinates precede right coordinates and combined ranks obey the matrix bound.",
        "sum-coboundary-matrices": "Emit sum coboundary matrices from degree zero through the union dimension, including the top zero differential. One work budget covers the entire list; the empty union gives an empty list.",
        "sum-cohomology": "Return ker(d^k)/im(d^(k-1)) on the left-then-right sum cochains in the existing IntegralHomology carrier. Retain the integral presentation including torsion; both differentials and Smith calculations share one work budget.",
        "sum-cohomology-degrees": "Emit sum integral cohomology models from zero through the union dimension. The entire list shares one work budget across all degree computations and returns no partial list on exhaustion.",
        "left-cohomology": "Return H^k(A;Z) as its constructive IntegralHomology quotient model in nonnegative degree k.",
        "right-cohomology": "Return H^k(B;Z) as its constructive IntegralHomology quotient model in nonnegative degree k.",
        "intersection-cohomology": "Return H^k(I;Z) on the actual intersection, with its integral quotient presentation.",
        "union-cohomology": "Return H^k(U;Z) on the actual simplex-wise union, with its integral quotient presentation.",
        "restriction-matrix": "Restrict a union cochain to each piece, left then right. This is the transpose of the chain addition matrix and commutes with coboundary.",
        "difference-matrix": "Restrict both sum components to the intersection and take left minus right. Together with restriction this gives a short exact cochain sequence over Z.",
        "connecting-cochain-matrix": "For an intersection cocycle in degree k, extend by zero to the left piece, differentiate, and glue with zero on the right to obtain a union cocycle in degree k+1. The chosen formula on arbitrary cochains need not commute with cover maps.",
        "restriction-cohomology-map": "Return H^k(U)->H^k(C(A) direct-sum C(B)) induced by restriction to both pieces, retaining integral presentations and a shared work budget.",
        "difference-cohomology-map": "Return sum cohomology -> H^k(I), induced by left-minus-right restriction and retaining integral presentations including torsion.",
        "connecting-cohomology-map": "Return H^k(I)->H^(k+1)(U) from the left-extension differential, with no extra sign. Swapping pieces negates this homomorphism; it is natural on cohomology. All models and induction share one work budget.",
        "long-exact-cohomology-segment": "Emit restriction, difference and connecting maps in order: H^k(U)->sum cohomology->H^k(I)->H^(k+1)(U). Images equal the next kernels over Z including torsion. One work budget covers the entire three-map segment.",
        "left-cohomology-inclusion-map": "Embed H^k(A) into the retained sum cohomology using the positive left coordinate inclusion.",
        "right-cohomology-inclusion-map": "Embed H^k(B) into the retained sum cohomology using the positive right coordinate inclusion.",
        "left-cohomology-projection-map": "Project sum cohomology onto H^k(A); this is a left inverse of the left inclusion and kills the right inclusion.",
        "right-cohomology-projection-map": "Project sum cohomology onto H^k(B); the two inclusion-projection composites add to the identity on sum cohomology, including torsion.",
        "from-complexes": "Retain ordered left and right complexes A and B. The ambient complex is exactly their simplex-wise union, so every ambient simplex belongs to a piece. This does not infer a cover of an externally supplied larger complex from vertex coverage.",
        "left": "Return the complete labelled left complex A.",
        "right": "Return the complete labelled right complex B.",
        "union": "Return the simplex-wise union U=A union B, the ambient complex covered by these pieces.",
        "intersection": "Return the simplex-wise intersection I=A intersection B, including its actual labels and simplices.",
        "swap": "Exchange the ordered pieces. This exchanges the direct-sum blocks and negates the induced connecting homomorphism on the same union/intersection presentations.",
        "equal": "Compare both ordered labelled pieces. Equal unions alone do not imply equal covers.",
        "euler-characteristic": "Return chi(A)+chi(B)-chi(I), equal to chi(U).",
        "sum-boundary-matrix": "Return the block diagonal of the two oriented boundaries, with all left chain coordinates before all right coordinates in both degrees. Combined rows and columns must each fit the matrix bound.",
        "sum-boundary-matrices": "Emit block-diagonal boundaries for degrees zero through dim(U), including zero-sized shapes. The entire list shares one work budget; the empty union yields an empty list.",
        "sum-homology": "Construct homology of the block-diagonal chain complex C(A) direct-sum C(B). It retains an actual IntegralHomology presentation; it is not implicitly identified with a separately normalized group direct sum.",
        "sum-homology-degrees": "Emit constructive sum homology for degrees zero through dim(U). A single work budget covers the entire list, including boundaries and Smith calculations; exhaustion returns no partial list.",
        "left-homology": "Return the left complex's unreduced integral homology in the supplied nonnegative degree, retaining its chain basis and quotient presentation.",
        "right-homology": "Return the right complex's unreduced integral homology in the supplied nonnegative degree, retaining its chain basis and quotient presentation.",
        "intersection-homology": "Return constructive unreduced integral homology of the actual intersection I in the supplied degree.",
        "union-homology": "Return constructive unreduced integral homology of the actual simplex-wise union U in the supplied degree.",
        "intersection-matrix": "Use the signed inclusion (i,-j): each intersection simplex maps with coefficient +1 into the left block and -1 into the right block.",
        "union-matrix": "Send (a,b) to a+b in union coordinates, including addition of coefficients of shared simplices. Together with (i,-j), this forms a short exact sequence of integral chain groups.",
        "split-matrix": "A chain-group section of the union matrix, assigning shared simplices to the left. It is generally not a chain map and is not a homology splitting.",
        "connecting-chain-matrix": "Take the intersection component of the boundary of the left part of a union chain. On union cycles that whole boundary lies in the intersection; arbitrary chains need not have that property.",
        "intersection-homology-map": "Return the integral homomorphism H_k(I)->H_k(C(A) direct-sum C(B)) induced by (i,-j), retaining both presentations and one shared work budget.",
        "union-homology-map": "Return the integral homomorphism from sum homology to H_k(U) induced by addition, retaining both presentations and one shared work budget.",
        "connecting-homology-map": "Send a union cycle class to the class of the boundary of its left part in H_(k-1)(I), with no extra sign. At degree zero H_-1(I)=0 under the unreduced convention. All homology and map calculations share one work budget.",
        "long-exact-segment": "Emit intersection, union, connecting maps in that order: H_k(I)->H_k(C(A) direct-sum C(B))->H_k(U)->H_(k-1)(I). Images equal the next kernels over Z, including torsion. One work budget covers the entire three-map segment.",
        "left-inclusion-map": "Embed H_k(A) into the retained sum homology via inclusion of the left chain block. This map supplies the actual presentation-aware direct-sum identification.",
        "right-inclusion-map": "Embed H_k(B) into the retained sum homology via inclusion of the right chain block, with positive sign.",
        "left-projection-map": "Project the retained sum homology onto H_k(A) using the left chain coordinates. It is a left inverse to the left inclusion and kills the right inclusion.",
        "right-projection-map": "Project the retained sum homology onto H_k(B) using the right chain coordinates. The two inclusion-projection composites sum to the identity on sum homology.",
        "excision-map": "Return the RelativeMap inclusion (A,I)->(U,B). Its ordered quotient simplex bases agree, so it induces a relative chain isomorphism and integral homology isomorphism. Its ambient simplicial map need not be invertible.",
    },
    "RelativeSimplicialMapAlgebra": {
        "from-map": "The first operand is the ambient SimplicialMap and the second contains source then target RelativeComplex values. Require exact ambient boundaries and f(A) contained in B simplex by simplex, not just on vertex sets.",
        "compose": "Apply the right operand first. Both labelled components of the middle pair must be equal; equality of ambient complexes alone is insufficient.",
        "inverse": "Defined exactly when the ambient map is a simplicial isomorphism and f(A)=B. The inverse must also preserve the chosen subcomplexes.",
        "source": "Return the retained source pair (X,A), including both full labelled complexes.",
        "target": "Return the retained target pair (Y,B), including simplices not reached by the map.",
        "ambient-map": "Return the validated ambient SimplicialMap X->Y with its complete original boundaries.",
        "subcomplex-map": "Return the restricted SimplicialMap A->B with the full declared B as target, not just the image of A.",
        "equal": "Compare both labelled pairs and the ambient vertex map; equality of homology maps or group types is insufficient.",
        "is-isomorphism": "True exactly when the ambient map is a simplicial isomorphism and its restriction maps A onto all of B.",
        "identity-on": "Construct the identity pair map, with identity relative chain matrices and induced homology maps.",
        "inclusion": "Require componentwise inclusions X contained in Y and A contained in B. Use identity vertex labels and retain both full target components.",
        "absolute": "Extend a SimplicialMap X->Y to (X,empty)->(Y,empty). Relative chain and homology maps agree with the original unreduced maps.",
        "diagonal": "Extend a SimplicialMap X->Y to (X,X)->(Y,Y). All relative chain groups vanish, while ambient and subcomplex maps remain available.",
        "chain-matrix": "Use ordered quotient simplex bases and increasing-vertex orientation signs. Collapsed simplices and images in B give zero columns. Rows index the target relative degree and columns the source; no full ambient matrix is required.",
        "chain-matrices": "Emit relative chain matrices from degree zero through the larger ambient dimension, retaining zero-sized shapes. Two empty ambient complexes give an empty list; one work budget covers the entire list.",
        "homology-map": "Construct the integral map H_k(X,A)->H_k(Y,B) with both actual retained presentations. Source and target homology construction and the induced map share one work budget.",
        "homology-maps": "Emit relative integral homology maps from zero through the larger ambient dimension. A single work budget covers the entire list, including homology constructions and matrix calculations.",
        "source-homology": "Return the retained source pair's constructive integral relative homology in the supplied nonnegative degree.",
        "target-homology": "Return the retained target pair's constructive integral relative homology in the supplied nonnegative degree.",
        "ambient-homology-map": "Return the unreduced integral homology map H_k(X)->H_k(Y) induced by the ambient simplicial map.",
        "subcomplex-homology-map": "Return the unreduced integral homology map H_k(A)->H_k(B) induced by the restricted simplicial map.",
        "long-exact-maps": "Emit the four vertical maps on H_k(A), H_k(X), H_k(X,A), H_(k-1)(A) into the corresponding target groups, in that order. They commute with inclusion, quotient and connecting maps. At degree zero the fourth map is 0->0 because H_-1=0. One shared work budget covers all four maps; no partial list is returned.",
        "contiguous": "Require equal source and target pairs. For every X simplex, the union of its images must lie in Y; for every A simplex, that union must also lie in B. Ambient contiguity alone is insufficient. Pair-contiguous maps induce equal relative homology maps.",
        "image": "Return the pair (f(X),f(A)) of actual image subcomplexes, which need not be the induced complexes on their vertex sets.",
        "corestrict-image": "Retain the full source pair and vertex map and replace the target by (f(X),f(A)). This need not be an isomorphism when the map collapses simplices or vertices.",
        "restrict": "The supplied pair (C,D) must satisfy C contained in X and D contained in A. Restrict the ambient vertex map to C and retain the full original target pair, using the first RelativeMap wrapper.",
    },
    "RelativeSimplicialAlgebra": {
        "from-complexes": "The first complex is X and the second is A. Require A to be a labelled subcomplex of X, not just a complex with a subset of its vertices. Return the RelativeComplex wrapper.",
        "ambient": "Return the complete retained ambient complex X, including all simplex labels.",
        "subcomplex": "Return the complete retained subcomplex A, including all simplex labels.",
        "equal": "Compare both labelled complexes, not only relative homology types or quotient chain dimensions.",
        "dimension": "Return the largest dimension of a simplex of X outside A, or -1 if the relative chain complex is zero.",
        "euler-characteristic": "Return chi(X)-chi(A), equivalently the alternating sum of relative integral Betti numbers.",
        "simplex-count": "Count the degree-k simplices of X outside A. The nonnegative degree uses the second N carrier wrapper; the selected basis is capped at 256 simplices.",
        "simplex-basis": "Emit wrapped FiniteSet(Z) simplices of X outside A in lexicographic increasing-vertex order, matching the relative matrix and vector coordinates.",
        "boundary-matrix": "Use the alternating oriented face boundary and discard faces in A. Rows index relative degree k-1, columns degree k. Degree zero has zero rows in the unreduced convention.",
        "boundary-matrices": "Emit quotient boundaries for degrees zero through dim(X), including zero-sized shapes. An empty ambient complex gives an empty list; the entire list shares one work budget.",
        "homology": "Construct H_k(X,A;Z) with its full integral cycle lattice and boundary quotient presentation. Return the actual IntegralHomology wrapper, retaining cycle representatives and bounding-chain operations.",
        "homology-degrees": "Emit constructive relative homology for degrees zero through dim(X). Every boundary construction and Smith calculation for the entire list shares one work budget; exhaustion returns no partial list.",
        "homology-type": "Return the canonical isomorphism type of H_k(X,A;Z), including torsion factors, in the actual AbelianGroupType wrapper.",
        "homology-types": "Emit integral relative homology types for degrees zero through dim(X), including trivial groups. A single work budget covers the entire list.",
        "betti-number": "Return the free rank of integral relative homology, equivalently the relative rational Betti number, using the second N wrapper.",
        "is-acyclic-degree": "True exactly when integral relative homology is trivial in the supplied degree, including absence of torsion; free rank zero alone is insufficient.",
        "projection-matrix": "The chain map C_k(X)->C_k(X,A) kills A simplices and preserves other ordered generators. Rows are relative and columns ambient coordinates.",
        "lift-matrix": "Transpose the projection matrix to give the zero-on-A chain-group section. Projection after lift is identity; this is generally not a chain map.",
        "inclusion-matrix": "The chain map C_k(A)->C_k(X) embeds each ordered A simplex with coefficient one, using ambient rows and subcomplex columns.",
        "connecting-chain-matrix": "Take the A-component of the boundary of a zero-on-A lift. Rows index A in degree k-1 and columns relative chains in degree k. It sends relative cycles to cycles in A; arbitrary relative chains need not give cycles.",
        "inclusion-homology": "Construct the induced map H_k(A)->H_k(X) with both actual retained presentations. All construction and map calculations share one work budget.",
        "quotient-homology": "Construct the induced map H_k(X)->H_k(X,A) from the quotient chain matrix, retaining both presentations and one shared work budget.",
        "connecting-homology": "Send a relative cycle to the class of its lifted boundary in H_(k-1)(A). The unreduced convention sets H_-1(A)=0, so degree zero gives the zero map into the trivial group. A single work budget covers both homologies and the induced map.",
        "long-exact-segment": "Emit inclusion, quotient, connecting in that order: H_k(A)->H_k(X)->H_k(X,A)->H_(k-1)(A). Consecutive compositions vanish and images equal the next kernels. One work budget covers the entire three-map segment, including homology constructions.",
        "absolute": "Construct (X,empty), whose relative chains and homology agree with the original unreduced chains and homology of X.",
        "diagonal": "Construct (X,X), whose relative chain groups vanish in every degree. Its displayed degree list still runs through dim(X).",
        "inclusion": "Return the actual SimplicialMap inclusion A->X, so existing vertex, chain and homology operations can be reused.",
    },
    "FiniteSimplicialMapAlgebra": {
        "compose": "Apply the right operand first. The full labelled middle complexes must be equal, not just their vertex sets or homology types.",
        "inverse": "Defined exactly for simplicial isomorphisms: a vertex bijection whose image contains every target simplex. Vertex bijectivity alone is insufficient.",
        "source": "Return the complete retained source complex, including its labels and simplices.",
        "target": "Return the complete retained target complex, including simplices not reached by the map.",
        "vertex-map": "Return a FiniteFunction using the actual registered integer Algebra, with domain and codomain equal to the declared complex vertex sets.",
        "map-vertex": "The input must be a source vertex. Return its image in the second Z carrier wrapper.",
        "vertex-images": "Emit wrapped vertex images in ascending source-label order, preserving repetitions and an empty list for an empty source.",
        "image": "Return the subcomplex consisting of images of source simplices. This need not be the induced subcomplex on the image vertices.",
        "is-injective": "True exactly when the vertex map is injective, equivalently a simplicial isomorphism onto its image subcomplex.",
        "is-surjective": "True exactly when every target simplex is an image simplex. Surjectivity on vertices alone is insufficient.",
        "is-isomorphism": "Require an injective vertex map and equality of the image subcomplex with the full target complex.",
        "is-vertex-surjective": "Compare the image vertex set with the full target vertex set, without asserting surjectivity on higher simplices.",
        "equal": "Compare both labelled boundary complexes and the complete vertex map, not merely the induced chain or homology maps.",
        "identity-on": "Construct the identity on every vertex of the supplied complex; all chain and homology maps are identities.",
        "inclusion": "The first complex must be a labelled subcomplex of the second; retain the second as the full target.",
        "from-function": "The boundary pair is source then target. Function domain and codomain must equal their vertex sets, and the image of every source simplex must be a target simplex.",
        "chain-matrix": "Use lexicographic simplex bases with increasing-vertex orientations. Each noncollapsed simplex maps with its permutation sign; collapsed simplices map to zero. Rows index target simplices and columns source simplices in the supplied nonnegative degree.",
        "chain-matrices": "Emit degree matrices from zero through the larger complex dimension, including zero-sized rectangular shapes. Two empty complexes give an empty list; all degrees share one work budget.",
        "homology-map": "Return the induced integral homomorphism in the supplied nonnegative degree using the actual retained homology presentations. Source and target homology construction and the induced map share one work budget.",
        "homology-maps": "Emit induced integral homomorphisms from degree zero through the larger complex dimension. A single work budget covers the entire list, including both homology constructions and all products; exhaustion returns no partial list.",
        "source-homology": "Construct the retained source's unreduced integral homology in the supplied degree, with its cycle basis and presented quotient.",
        "target-homology": "Construct the retained target's unreduced integral homology in the supplied degree, with its cycle basis and presented quotient.",
        "contiguous": "Require equal source and target complexes. For every source simplex, the union of both image vertex sets must be a target simplex. Contiguity implies equal induced homology maps but is not a general homotopy decision.",
        "vertex-fiber": "The vertex must belong to the declared target. Emit all source vertices mapping to it in ascending order using second Z wrappers; unreachable target vertices give an empty list.",
        "restrict": "The second operand must be a source subcomplex. Restrict the vertex map and retain the full original target, returning the first SimplicialMap carrier wrapper.",
        "corestrict-image": "Retain the full source and vertex map, replacing the target by the actual image subcomplex.",
        "map-simplex": "The input must be a nonempty source simplex. Return its image vertex set in the second FiniteSet(Z) wrapper; collapsed images have lower dimension.",
        "simplex-basis": "Emit the nonempty simplices in the supplied nonnegative degree in lexicographic increasing-vertex order, matching all boundary and chain matrices. Each degree is capped at 256 simplices.",
        "constant-at": "The supplied integer must label a target vertex, even for an empty source. Retain the boundaries and send every source vertex to that point.",
        "empty-to": "Construct the unique map from the empty complex into the supplied full target complex.",
    },
    "IntegralHomologyAlgebra": {
        "from-boundaries": "The first matrix is outgoing d_k and the second is incoming d_(k+1). Middle dimensions must match and outgoing times incoming must be zero over Z.",
        "at-degree": "Compute unreduced integral homology in the supplied nonnegative degree. Chains use lexicographically ordered simplices, each oriented by increasing vertex labels. Empty and above-top degrees retain the actual adjacent matrix shapes.",
        "outgoing-boundary": "Return the retained matrix d_k from the chosen chain group to the preceding chain group.",
        "incoming-boundary": "Return the retained matrix d_(k+1) into the chosen chain group, including redundant and zero columns.",
        "cycle-matrix": "Columns give a basis K of the full integral cycle lattice, not a rational kernel with independently cleared denominators.",
        "boundary-coordinates": "Return the unique matrix R with K*R=incoming, where K is the retained integral cycle basis. Its columns present the homology quotient.",
        "group": "Return the presented group Z^cycle-rank modulo boundary coordinates. Its original generators refer to the retained cycle basis.",
        "as-type": "Return the canonical integral homology isomorphism type, including torsion and free rank.",
        "chain-rank": "Return the rank of the chosen free chain group, equal to the outgoing column and incoming row count.",
        "cycle-rank": "Return the rank of the full integral kernel of the outgoing boundary.",
        "boundary-rank": "Return the rank of the incoming image, equal to cycle-rank minus the homology free rank.",
        "betti-number": "Return the integral homology free rank, equivalently the rational Betti number in this degree; torsion does not contribute.",
        "is-acyclic": "True exactly when homology in this single degree is trivial, including the absence of torsion. This does not assert that an entire complex is acyclic.",
        "equal": "Compare both retained boundary matrices and their dimensions, not just the resulting homology types.",
        "cycle-basis": "Emit the columns of the full integral cycle basis in order as wrapped vectors in the original chain coordinates.",
        "boundary-basis": "Emit an integral basis of the actual incoming image, retaining its lattice index rather than replacing it with its saturation.",
        "generators": "Emit chain representatives of minimal Smith generators: nontrivial torsion factors followed by free coordinates. Omit killed generators and emit an empty list for trivial homology.",
        "is-cycle": "The vector must have the chosen chain dimension; true exactly when its outgoing boundary is zero.",
        "is-boundary": "The vector must have the chosen chain dimension; test integral solvability against the incoming matrix. Noncycles return false; resource failures propagate rather than returning false.",
        "class-of": "The input must be an integral cycle. Solve in the cycle basis and project to the retained presented homology group, returning an AbelianGroupElement wrapper.",
        "representative": "The class must belong to the retained homology presentation. Return one cycle in original chain coordinates. This section need not be additive and is not a shortest-cycle algorithm.",
        "bounding-chain": "The input must be a boundary. Return one vector in the next chain group whose incoming boundary is the input, using the second Vec(Z) wrapper; undefined for a nonboundary.",
        "cycle-coordinates": "The input must be a cycle. Return its unique coordinates in the retained integral cycle basis using the second Vec(Z) wrapper.",
        "from-cycle-coordinates": "The input dimension must equal the cycle rank. Apply the cycle matrix to obtain a cycle in the original chain coordinates using the second Vec(Z) wrapper.",
        "projection": "Return the surjective AbelianGroupHomomorphism from the free cycle-coordinate group onto the retained presented homology group, not a projection defined on all chains.",
        "induced-map": "The pair supplies target homology and a matrix with target-chain rows and source-chain columns. It must preserve cycles and boundaries. Return the induced map between the retained homology presentations; no adjacent chain-map components are inferred.",
        "zero-class": "Return the zero AbelianGroupElement of the retained homology presentation.",
    },
    "AbelianGroupHomomorphismAlgebra": {
        "compose": "Apply the right operand first. The first source presentation must equal the second target presentation, including its coordinate map.",
        "add": "Source and target presentations must agree for both maps; add their images pointwise in the target group.",
        "subtract": "Source and target presentations must agree; subtract images pointwise in the target group.",
        "negate": "Negate all generator images in the target, retaining both presentations.",
        "scale": "Multiply all generator images by the supplied integer, with canonical target residues.",
        "source": "Return the retained source presentation, not just its isomorphism type.",
        "target": "Return the retained target presentation, not just its isomorphism type.",
        "smith-matrix": "Return target rows by source columns in full Smith coordinates, including killed coordinates. Each target row is reduced modulo its nonzero Smith factor.",
        "matrix-lift": "Return one integer lift in the original generator coordinates: U_target^-1 * M * U_source. Different lifts may define the same quotient map.",
        "apply": "The element must belong to the retained source presentation; the result belongs to the target presentation, wrapped by the second AbelianGroupElement Algebra.",
        "generator-images": "Emit wrapped target elements in original source generator order, retaining zero or redundant images and an empty list for no generators.",
        "is-zero": "All source generator images must be zero in the target quotient.",
        "equal": "Compare both retained boundary presentations and canonical generator-image matrices, not raw integer lifts or only isomorphism types.",
        "identity-on": "Construct the identity on the supplied presentation; killed Smith-coordinate columns normalize to zero.",
        "zero-between": "Construct the zero map from the first presentation to the second, retaining both boundaries.",
        "zero-like": "Construct the zero map with the same source and target presentations.",
        "kernel": "Let K be an integral basis of x such that M*x is a target relation. Present the kernel by coordinates C solving K*C=R_source in full Smith coordinates.",
        "kernel-inclusion": "Return the injective map from the computed kernel presentation into the original source. Its composition with the original map is zero.",
        "image": "Present the image as the source Smith-coordinate free module modulo the lattice K of vectors sent to target relations.",
        "image-inclusion": "Return the injective map from the computed image presentation into the original target.",
        "image-projection": "Return the surjective map from the source to the computed image presentation. Image inclusion composed with this projection equals the original map.",
        "cokernel": "Present the target modulo the image by adjoining the map columns to the target Smith relation columns.",
        "cokernel-projection": "Return the surjective map from the original target to the computed cokernel; its composition with the original map is zero.",
        "is-injective": "True exactly when the kernel is trivial. Resource exhaustion propagates as IMPLEMENTATION_FAILURE, never false.",
        "is-surjective": "True exactly when the cokernel is trivial. Resource exhaustion propagates as IMPLEMENTATION_FAILURE, never false.",
        "is-isomorphism": "The kernel and cokernel must both be trivial; all reductions share one work budget.",
        "inverse": "Defined only for an isomorphism. Solve for generator preimages, check source relations and the inverse identity; return the map with exchanged presentations.",
        "has-preimage": "The element must belong to the target presentation; test integer solvability of M*x - R_target*y = element. Resource failures propagate, never returning false.",
        "preimage": "Return one source element mapping to the supplied target element, in the second AbelianGroupElement carrier wrapper. Undefined when no preimage exists; the full fiber is a coset by the kernel.",
        "from-matrix": "The paired boundaries are source then target. Columns are images of original source generators in original target coordinates; every source relation must map to a target relation.",
        "from-smith-matrix": "The paired boundaries are source then target. Use full Smith coordinates, with target rows and source columns; every source relation must map to a target relation.",
        "scaling-on": "Construct the endomorphism multiplying every element of the supplied presentation by the integer operand.",
    },
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
    if owner == "SimplicialHomotopyEquivalenceAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/SimplicialHomotopyEquivalence.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialHomotopyPath.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "SimplicialHomotopyPathAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/SimplicialHomotopyPath.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialHomotopy.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "SimplicialHomotopyAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/SimplicialHomotopy.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialChain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
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
    if owner == "AbelianGroupHomomorphismAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/structures/PresentedAbelianGroup.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerMatrix.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "IntegralHomologyAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralSimplicialHomology.java",
                         "groupimp/src/main/java/mathematics/structures/PresentedAbelianGroup.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "FiniteSimplicialMapAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/FiniteSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/FiniteSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralSimplicialHomology.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java"]
    if owner == "RelativeSimplicialAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralSimplicialHomology.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "RelativeSimplicialMapAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/FiniteSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "SimplicialCoverAlgebra":
        tests.append("groupimp/src/test/java/operations/NativeCohomologicalMayerVietorisTest.java")
        paths = paths + ["groupimp/src/main/java/mathematics/topology/SimplicialCover.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "SimplicialCochainAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/SimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/FiniteSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralSimplicialHomology.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "SimplicialChainAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/SimplicialChain.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/FiniteSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "RelativeCapProductAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/RelativeCapProduct.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialChain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialChain.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "RelativeSimplicialTripleAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/RelativeSimplicialTriple.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialChain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "RelativeSimplicialTripleMapAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/RelativeSimplicialTripleMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialTriple.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/FiniteSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "RelativeSimplicialChainAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/RelativeSimplicialChain.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialChain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "RelativeSimplicialCochainAlgebra":
        paths = paths + ["groupimp/src/main/java/mathematics/topology/RelativeSimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
    if owner == "SimplicialCoverMapAlgebra":
        tests.append("groupimp/src/test/java/operations/NativeCohomologicalMayerVietorisTest.java")
        paths = paths + ["groupimp/src/main/java/mathematics/topology/SimplicialCoverMap.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialCochain.java",
                         "groupimp/src/main/java/mathematics/topology/SimplicialCover.java",
                         "groupimp/src/main/java/mathematics/topology/FiniteSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialMap.java",
                         "groupimp/src/main/java/mathematics/topology/RelativeSimplicialComplex.java",
                         "groupimp/src/main/java/mathematics/topology/IntegralHomology.java",
                         "groupimp/src/main/java/mathematics/structures/AbelianGroupHomomorphism.java",
                         "groupimp/src/main/java/mathematics/linear/IntegerSmithNormalForm.java"]
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
            "These homology operations return isomorphism types. IntegralHomology.at-degree separately constructs cycle representatives, bounding chains and degreewise induced maps. SimplicialCochain supplies integral cohomology and cup products. No persistence or homeomorphism decision is implemented. Existing betti-number and betti-numbers retain F2 coefficients."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATchapters.html")
        value["references"].append("https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex_examples.html")
    if owner == "AbelianGroupTypeAlgebra":
        value["required_invariants"].append("Free rank is nonnegative; canonical torsion factors are greater than one and each divides the next. Equality classifies group isomorphism types.")
        value["known_limitations"] += ["At most 256 canonical torsion factors and 1024 supplied/intermediate cyclic factors; exhaustion raises IMPLEMENTATION_FAILURE. Free rank and coefficient bit lengths are arbitrary precision. Normalization uses gcd/lcm without prime factorization.",
            "No chosen group elements, presentations, generators, homomorphisms or extension witnesses are represented. Hom, tensor, Tor_1 and Ext^1 classify resulting abelian groups only; arbitrary modules and higher derived functors are outside this scope."]
        value["references"].append("https://doc.sagemath.org/html/en/reference/groups/sage/groups/additive_abelian/additive_abelian_group.html")
    if owner in ("IntegerVectorFamily", "IntegerMatrixFamily"):
        value["known_limitations"].append("Each coordinate dimension is at most 256; integer coefficient bit lengths remain unbounded. Oversized representations raise IMPLEMENTATION_FAILURE. No floating-point approximation or coordinate rounding is used.")
    if owner == "AbelianGroupHomomorphismAlgebra":
        value["required_invariants"].append("The normalized target Smith-coordinate columns respect all source relations. Both actual boundary presentations are retained; isomorphic groups are not implicitly identified.")
        value["known_limitations"] += ["Every auxiliary matrix, including [M,-R_target], is limited to 256 rows and columns. Each compound map computation shares a 5000000-unit integer work budget across reductions, solves and products. Dense costs apply even to identities; exhaustion raises IMPLEMENTATION_FAILURE and never a false decision. Integer bit lengths remain unbounded.",
            "Only finitely presented abelian groups over Z. Preimage returns one representative, not a full fiber or a minimal-norm lift. No automatic isomorphism search, arbitrary module coefficients, subgroup lattice enumeration or nonabelian maps are implemented. IntegralHomology separately constructs induced maps from supplied degree matrices."]
        value["references"].append("https://doc.sagemath.org/html/en/reference/modules/sage/modules/fg_pid/fgp_morphism.html")
    if owner == "IntegralHomologyAlgebra":
        value["required_invariants"].append("Consecutive integer boundaries compose to zero. Cycles use a full integral kernel basis; homology retains the quotient by actual integral boundaries. Induced maps respect these quotients.")
        value["known_limitations"] += ["Every chain and auxiliary matrix dimension is at most 256. Each construction, solve, generator computation or induced-map calculation shares a 5000000-unit work budget across Smith reductions and matrix products. Dense costs apply to sparse and identity matrices; exhaustion raises IMPLEMENTATION_FAILURE, never false or a truncated witness list. Integer bit lengths remain unbounded.",
            "One degree of a finite free integer chain complex only. Chain coordinates require the retained basis convention; quotient elements retain the resulting presented group. A representative is a set-theoretic section, not generally an additive section or shortest cycle. The supplied degree map is checked for cycle and boundary preservation. SimplicialMap constructs vertex-defined chain maps; RelativeComplex constructs relative homology and long exact sequence maps. SimplicialCochain supplies integral cohomology using dual differentials and cup products. Arbitrary complete chain-map builders, reduced homology and persistence remain outside scope."]
        value["references"] += ["https://doc.sagemath.org/html/en/reference/homology/sage/homology/chain_complex.html",
                                "https://doc.sagemath.org/html/en/reference/homology/sage/homology/homology_morphism.html"]
    if owner == "FiniteSimplicialMapAlgebra":
        value["required_invariants"].append("Vertex maps are total on the declared source and preserve every nonempty simplex. Chain matrices commute with the oriented integral boundaries and respect composition, including collapsed simplices.")
        value["known_limitations"] += ["Map construction allows at most 4096 nonempty simplices per complex. Each matrix degree and required homology boundary has at most 256 simplices. Each compound chain/homology calculation has a shared 5000000-unit integer work budget; flat degree lists share it across all outputs. Resource exhaustion raises IMPLEMENTATION_FAILURE, never false or a partial list; coefficient bit lengths remain unbounded.",
            "Finite abstract complexes with Java int vertex labels, exposed through Z-valued native operations. Homology is unreduced over Z. RelativeComplex separately supplies relative homology and long exact sequence maps; SimplicialCochain supplies contravariant integral cohomology maps. No subdivision, simplicial approximation, arbitrary chain-map builder, contiguity-chain or general homotopy search or persistent homology is provided. Vertex-surjective maps need not hit every simplex."]
        value["references"] += ["https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex_morphism.html",
                                "https://doc.sagemath.org/html/en/reference/homology/sage/homology/chain_complex_morphism.html"]
    if owner == "RelativeSimplicialAlgebra":
        value["required_invariants"].append("The retained A is a labelled subcomplex of X. Relative chains are the quotient C(X)/C(A), with increasing-vertex orientations and unreduced integral homology. Inclusion, quotient and connecting maps retain their actual homology presentations.")
        value["known_limitations"] += ["Each complex has at most 4096 nonempty simplices. Each required matrix basis has at most 256 simplices; relative bases are filtered before this bound, while ambient/subcomplex matrices require their own full bases. A compound homology or map calculation, whole degree list or long-exact segment shares a 5000000-unit integer work budget. Exhaustion raises IMPLEMENTATION_FAILURE, never a false predicate or a partial list; integer bit lengths remain unbounded.",
            "Finite labelled simplicial pairs over Z only. The zero-on-A lift is a chain-group section, generally not a chain map. The connecting chain matrix need only preserve cycles and boundaries. RelativeMap supplies simplicial maps between pairs; SimplicialCover supplies excision for a two-subcomplex union. RelativeCochain supplies relative cohomology, cup products and natural long exact cohomology sequences. Reduced homology, persistent homology, general topological excision and homotopy search remain outside scope."]
        value["references"] += ["https://pi.math.cornell.edu/~hatcher/AT/AT.pdf",
                                "https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex.html"]
    if owner == "RelativeSimplicialMapAlgebra":
        value["required_invariants"].append("The full ambient map is simplicial and carries every A simplex into B. Relative chain matrices commute with boundaries; relative homology preserves composition and commutes with all three long exact sequence squares.")
        value["known_limitations"] += ["Each boundary complex has at most 4096 nonempty simplices. Each required matrix basis has at most 256 simplices. Relative matrices filter both bases before this bound; ambient and subcomplex homology operations require their own full bases. Each compound homology computation, entire degree list or four-map naturality list shares a 5000000-unit work budget. Exhaustion raises IMPLEMENTATION_FAILURE, never a false predicate or a partial list; integer bit lengths remain unbounded.",
            "Finite labelled simplicial maps over Z only. Contiguity is a sufficient condition, not a general homotopy decision. SimplicialCover constructs the excision map for a two-subcomplex union separately. RelativeCochain supplies contravariant relative cohomology maps and natural exact sequences. No arbitrary relative chain-map builder, explicit chain-homotopy witness, reduced homology, persistent homology, subdivision or continuous-map representation is supplied."]
        value["references"] += ["https://pi.math.cornell.edu/~hatcher/AT/AT.pdf",
                                "https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex_morphism.html"]
    if owner == "SimplicialCoverAlgebra":
        value["required_invariants"].append("The ordered pieces cover their actual simplex-wise union. Sum chains use left then right coordinates; intersection inclusion is (i,-j), union is addition, and connecting homology uses the boundary of the left part. Homology and cohomology are unreduced over Z with retained presentations. The dual short exact cochain sequence gives restriction, left-minus-right difference and degree-raising connecting cohomology maps.")
        value["known_limitations"] += ["The union has at most 4096 nonempty simplices. Every required matrix dimension is at most 256, including the sum of both chain ranks for block matrices. Each compound homology/cohomology/map calculation, whole degree list or three-map exact segment shares a 5000000-unit integer work budget. Exhaustion raises IMPLEMENTATION_FAILURE, never a false result or truncated list; coefficient bit lengths remain unbounded.",
            "Two finite labelled subcomplexes only. The splitting is a chain-group section, not generally a chain map or homology splitting. Excision is the explicit simplicial inclusion (A,A intersection B)->(A union B,B), not arbitrary topological excision. CoverMap supplies piece-preserving simplicial maps and Mayer-Vietoris/excision naturality. Many-set covers, reduced/relative Mayer-Vietoris, spectral sequences and continuous covers remain outside scope."]
        value["references"] += ["https://pi.math.cornell.edu/~hatcher/AT/AT.pdf", "https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf"]
    if owner == "RelativeSimplicialCochainAlgebra":
        value["required_invariants"].append("Relative cochains vanish on the subcomplex and use dual quotient bases. Extension into absolute cochains commutes with coboundary. Cup products vanish on the union of both input subcomplexes. The cohomology connecting map raises degree; pair maps reverse direction and commute with all long exact sequence squares.")
        value["known_limitations"] += ["Each pair complex has at most 4096 nonempty simplices. Every required vector/matrix basis has at most 256 simplices; relative bases are filtered before the limit, whereas absolute extension, restriction and exact sequences need the required full ambient/subcomplex bases. Each compound cohomology/class/product/map computation, entire degree list, three-map segment or four-map naturality list shares a 5000000-unit work budget. Exhaustion raises IMPLEMENTATION_FAILURE without false predicates or partial lists; integer bit lengths remain unbounded.",
            "Only homogeneous unreduced integral cochains on labelled simplicial pairs are represented. There is no general unit on a nonempty relative subcomplex; absolute cochains give the two-sided module action. Addition requires the same full pair and degree; cup products may change the pair by taking the union of subcomplexes. Abelian class wrappers retain presentations, while the cochain provides geometric context for representatives. The connecting cochain formula need not commute with pair maps, although its cohomology map does. Mixed-degree ring carriers, other coefficients, explicit cup homotopies, higher cohomology operations, persistence and continuous maps remain outside scope."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf")
    if owner == "SimplicialCochainAlgebra":
        value["required_invariants"].append("Homogeneous integer cochains retain the full complex and degree. The differential is dual to the oriented boundary; the Alexander-Whitney cup product satisfies the signed Leibniz rule. Pullback commutes with coboundary and reverses composition. Cup commutativity and naturality for arbitrary vertex maps hold on cohomology, not generally on cochains.")
        value["known_limitations"] += ["Each complex has at most 4096 nonempty simplices, and every required vector/matrix basis has at most 256 simplices. Each compound cohomology construction, class projection, representative computation, cup-class or induced map shares a 5000000-unit integer work budget; each entire degree list shares that budget across all outputs. Exhaustion raises IMPLEMENTATION_FAILURE, never false or a truncated list. Integer bit lengths remain unbounded.",
            "Only homogeneous unreduced integral cochains on finite labelled abstract complexes are represented. Addition requires equal degree and complex; cup adds degrees, so this carrier is a family rather than one additive group across all inputs. Classes retain an abelian presentation, not geometric labels; the cochain supplies the complex and degree for reconstruction. RelativeCochain supplies integral relative cup products and natural long exact cohomology sequences; SimplicialChain supplies absolute cap products and Kronecker pairing, and RelativeChain supplies the two standard relative cap products, and RelativeCap supplies independently chosen subcomplexes A and B. No mixed-degree ring carrier, other coefficient rings, explicit cup homotopies, Steenrod operations, persistence, subdivision or continuous maps are supplied."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf")
    if owner == "SimplicialCoverMapAlgebra":
        value["required_invariants"].append("The union map preserves each ordered piece simplex by simplex. Block-diagonal sum maps commute with boundaries and the signed intersection and union maps. Induced homology maps are covariant and cohomology maps contravariant; both commute with their three Mayer-Vietoris squares. The two relative maps commute with excision.")
        value["known_limitations"] += ["Each union has at most 4096 nonempty simplices. Each required matrix dimension is at most 256, including the combined left and right chain ranks. Every compound homology/cohomology computation, entire degree list or four-map naturality list shares a 5000000-unit work budget. Exhaustion raises IMPLEMENTATION_FAILURE, never a false predicate or a partial list; integer bit lengths remain unbounded.",
            "Only piece-preserving simplicial maps of ordered two-subcomplex covers are represented. The chosen chain-group splittings and connecting chain matrices need not commute with cover maps; connecting cochain matrices also need not commute, while both connecting squares are natural on homology and cohomology. Contiguity is sufficient, not a general homotopy decision. No arbitrary chain-map builder, explicit chain-homotopy witness, many-set covers, reduced/relative Mayer-Vietoris, persistence, subdivision or continuous-map representation is supplied."]
        value["references"] += ["https://pi.math.cornell.edu/~hatcher/AT/AT.pdf", "https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf"]
    if owner == "SimplicialChainAlgebra":
        value["required_invariants"].append("Integral chains retain the full complex and integer degree; negative degrees contain only zero. Cap evaluates on the front face and retains the back face, satisfying boundary(c cap phi)=(-1)^p(boundary(c) cap phi-c cap coboundary(phi)). Cycle-cocycle cap products descend to homology, obey the cup-module law and are natural on homology under arbitrary simplicial vertex maps.")
        value["known_limitations"] += ["Each complex has at most 4096 nonempty simplices; every required matrix/vector basis has at most 256 simplices, including adjacent degrees and cap source/target bases. Each compound homology, class, cap-class, induced-map or generator-list computation shares one 5000000-unit integer work budget. Exhaustion raises IMPLEMENTATION_FAILURE without a false predicate or partial list. Integer coefficient bit lengths are unbounded.",
            "Only homogeneous unreduced integral chains on finite labelled abstract complexes are represented. Chain addition requires equal full complexes and degrees. Homology class wrappers retain presentations, while chains supply geometric context. The chosen sorted cap formula is not generally natural on chains under arbitrary vertex relabelling, although it is natural on homology. RelativeChain supplies quotient chains and the two standard relative cap products. A cap cohomology map does not certify manifold status, orientation or a fundamental class. RelativeCap supplies general cap products with explicit A and B. Automatic fundamental-class construction, other coefficients, mixed-degree chains, Poincare-duality certification and explicit cap homotopies remain outside scope."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf")
    if owner == "SimplicialHomotopyEquivalenceAlgebra":
        value["required_invariants"].append("Forward and backward maps have opposite full labelled pairs. The retained source path runs from Id_X to g after f, and the target path from Id_Y to f after g. Their integral prisms show that forward/backward induced homology maps are mutual inverses, as are the contravariant cohomology maps. Equality includes both complete witnesses.")
        value["known_limitations"] += ["Each witness has 1 through 256 stages, and each boundary complex at most 4096 nonempty simplices. Required quotient bases are filtered before the 256-simplex bound. Each equivalence construction, entire composition including transported-path revalidation, and entire two-map homology/cohomology list shares one 5000000-unit work budget. Exhaustion raises IMPLEMENTATION_FAILURE without partial results; integer coefficient bit lengths are unbounded.",
            "Only supplied finite contiguity paths are checked. There is no homotopy search, general homotopy-equivalence decision, subdivision, arbitrary chain-homotopy carrier or other coefficient ring. The data need not be a strict simplicial isomorphism or a deformation retraction, and no deformation-retraction conditions are automatically certified. Composition with the homotopy inverse need not equal the strict identity witness. Induced map degrees are nonnegative."]
        value["references"] += ["https://pi.math.cornell.edu/~hatcher/AT/ATch0.pdf", "https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf"]
    if owner == "SimplicialHomotopyPathAlgebra":
        value["required_invariants"].append("Every stage retains the same full source and target pairs, and every consecutive pair is contiguous in both target components. Accumulated prisms telescope between the first and last maps, with dual cochain identities. Concatenation preserves the full ordered path; stationary paths are its units. Native chain/cochain actions use the actual second operand wrappers.")
        value["known_limitations"] += ["At least one and at most 256 stages, giving at most 255 steps. Each boundary complex has at most 4096 nonempty simplices and every required quotient basis at most 256, filtered before the basis bound. Each constructor, composition including revalidation, matrix sum, typed action and entire flat degree list shares one 5000000-unit work budget. Resource failures raise IMPLEMENTATION_FAILURE without partial results; coefficient bit lengths are unbounded.",
            "Stages must be supplied; there is no homotopy search, subdivision, arbitrary chain-homotopy carrier, other coefficient ring or general continuous homotopy-equivalence decision. Endpoint equality does not identify paths; loops can produce nonzero cycles. Reversal recomputes prisms and need not negate them. Precomposition recomputes the sorted prism and need not equal right multiplication by the source chain map. Typed cochain actions require positive degree; matrix APIs retain the degree-zero zero-row shape. Absolute actions require empty subcomplexes."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf")
    if owner == "SimplicialHomotopyAlgebra":
        value["required_invariants"].append("Ordered contiguous maps retain equal full source/target pairs and contiguity in both target components. The oriented integral prism obeys boundary P + P boundary = to# - from#, and its transpose obeys the dual cochain identity. Typed actions retain complete contexts and use actual second operand wrappers.")
        value["known_limitations"] += ["At most 4096 nonempty simplices per boundary complex and 256 simplices per required quotient basis, filtered before the dimension check. Each matrix, typed action and entire flat degree list shares one 5000000-unit work budget. Resource failures are IMPLEMENTATION_FAILURE without partial results. Integer bit lengths are unbounded.",
            "This is a specific prism for a single contiguous pair, not a search for homotopies or an arbitrary chain-homotopy carrier. HomotopyPath concatenates supplied witnesses, and HomotopyEquivalence checks supplied inverse witnesses; no general homotopy-equivalence decision is supplied. Endpoint reversal need not negate the prism. Matrix degrees are nonnegative; typed chain actions allow negative zero groups and typed cochain actions require positive degree. Absolute actions require empty subcomplexes. Other coefficients, subdivision and higher cup/cap homotopies remain outside scope."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf")
    if owner == "RelativeSimplicialTripleAlgebra":
        value["required_invariants"].append("The full labelled inclusions B subset A subset X are enforced. Filtered quotient chains give a short exact sequence, and the dual cochains give the reversed short exact sequence. The induced long exact sequences retain integral presentations and torsion; they are natural under maps preserving both subcomplexes. Typed connecting outputs use the actual registered second operand wrappers.")
        value["known_limitations"] += ["At most 4096 nonempty simplices per complex and 256 simplices in each required quotient basis, filtered before dimension checks. Adjacent degrees must fit for homology/cohomology. Every compound map and each entire three-map segment shares one 5000000-unit work budget across all models and induction; failure is IMPLEMENTATION_FAILURE, without false predicates or partial lists. Integer bit lengths are unbounded.",
            "Only finite labelled simplicial triples and unreduced integral (co)homology are represented. Matrix and exact-sequence operations require nonnegative degrees; typed chain operations allow negative zero groups. Zero-on-A/B sections and connecting matrices need not be natural on arbitrary chains/cochains, although their induced maps are natural. TripleMap supplies maps of triples and both naturality diagrams. SimplicialHomotopy supplies prism witnesses for contiguous endpoint pair maps. Arbitrary chain-map builders, other coefficients, subdivision and continuous-map representations remain outside scope."]
        value["references"] += ["https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf", "https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf"]
    if owner == "RelativeSimplicialTripleMapAlgebra":
        value["required_invariants"].append("The common ambient vertex map preserves both nested subcomplexes and induces compatible outer, total and inner RelativeMaps. Composition is covariant for homology and contravariant for cohomology. Four-map lists retain source/target presentations and make all six exact-sequence naturality squares commute. All results belong to actual registered Algebra instances.")
        value["known_limitations"] += ["At most 4096 nonempty simplices per complex and 256 simplices per required quotient basis, filtered before dimension checks. Induced maps require adjacent bases to fit. Every scalar induced map and each entire four-map list shares one 5000000-unit work budget across its quotient models and induction. Construction, composition, restriction, image/corestriction and contiguity also share their own work budget. Exhaustion raises IMPLEMENTATION_FAILURE, without partial lists or false predicates; integer bit lengths are unbounded.",
            "Only finite labelled simplicial triples and their simplex-preserving vertex maps are represented. Degrees are nonnegative; the degree-zero homology naturality list retains the typed degree-minus-one zero presentations. Connecting chain/cochain matrices need not themselves be natural, although induced maps are. Contiguity is sufficient for homotopy, not a complete homotopy decision. SimplicialHomotopy supplies prism witnesses for the exposed contiguous pair maps. No arbitrary chain maps, general homotopy search, other coefficient rings, subdivision or continuous-map representation is supplied."]
        value["references"] += ["https://pi.math.cornell.edu/~hatcher/AT/ATch2.pdf", "https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf"]
    if owner == "RelativeCapProductAlgebra":
        value["required_invariants"].append("All three labelled pairs are checked: chain on (X,A union B), cochain on (X,A), target (X,B). The signed boundary identity holds on chains. Integral homology products are independent of representatives, natural on classes under maps preserving A and B, and compatible with relative cup products. All wrappers use the original registered algebras.")
        value["known_limitations"] += ["Each ambient complex has at most 4096 nonempty simplices. Each required quotient basis has at most 256 simplices, filtered before bounds; no full absolute basis is required unless selected by an empty subcomplex. Adjacent degrees must also fit for induced maps. Every compound class or induced-map operation shares one 5000000-unit integer work budget across validation, models and induction; exhaustion raises IMPLEMENTATION_FAILURE. Integer coefficient bit lengths are unbounded.",
            "Only homogeneous unreduced integral simplicial chains and cochains are represented. A and B must be explicit subcomplexes with the exact required union. Sorted cap representatives need not be natural on chains under arbitrary vertex maps; naturality is asserted on classes. Contexts fix a chain and target, not a bilinear-map carrier. Automatic orientations, fundamental classes, manifold/duality certification, arbitrary continuous maps, other coefficient rings, subdivision and explicit cap homotopies remain outside scope."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf")
    if owner == "RelativeSimplicialChainAlgebra":
        value["required_invariants"].append("Quotient chains retain the full pair and integer degree. Projection commutes with boundary but zero-on-A lifting generally does not. Absolute cochains cap into relative chains; relative cochains on the same pair cap into absolute chains. Both products obey the signed boundary identity, descend to integral homology and are natural on classes under maps of pairs. All output wrappers belong to the actual registered result algebra.")
        value["known_limitations"] += ["Each boundary complex has at most 4096 nonempty simplices; every required matrix/vector basis has at most 256 simplices. Relative bases are filtered first, while absolute outputs/cochain actions and their adjacent homology degrees require the full corresponding bases to fit. Each compound homology, class, cap-class, induced-map or generator-list computation shares one 5000000-unit integer work budget. Exhaustion raises IMPLEMENTATION_FAILURE without false predicates or partial outputs; integer bit lengths are unbounded.",
            "Only homogeneous unreduced integral chains on labelled pairs are represented. Negative degrees contain only zero. Canonical lifts and sorted cap representatives need not be natural on chains, although the induced homology operations are natural. Supplied relative cycles are not certified fundamental classes. RelativeCap supplies general cap products with explicit A and B. Automatic orientation/fundamental-class construction, mixed-degree chains, other coefficients, explicit cap homotopies and manifold/Lefschetz-duality certification remain outside scope."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATch3.pdf")
    if owner in ("PresentedAbelianGroupAlgebra", "AbelianGroupElementAlgebra"):
        value["required_invariants"].append("Relations are integer matrix columns. Each presentation retains the Smith-coordinate map; element equality and arithmetic respect that presentation, not only its abstract isomorphism type.")
        value["known_limitations"] += ["Presentations allow at most 256 generators and 256 relations. Construction and representative lifting use bounded integer Smith calculations with a 5000000-unit budget per calculation; exhaustion is IMPLEMENTATION_FAILURE. Coefficient bit lengths remain unbounded.",
            "Finite element, cyclic-subgroup and scaling-fiber enumeration is capped at 4096 results; oversized finite outputs raise IMPLEMENTATION_FAILURE without a truncated result. Infinite outputs are outside these flat operations.",
            "Only finitely presented abelian groups over Z are represented. Explicit maps and their kernel/image/cokernel presentations live in AbelianGroupHomomorphism; IntegralHomology supplies homology generators. These carriers alone do not enumerate subgroups or search for isomorphisms. Nonabelian group presentations are outside scope."]
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
        ("HomotopyEquivalence", "Supplied simplicial homotopy equivalences and inverse integral maps", "mathematics.topology.SimplicialHomotopyEquivalence",
         ["Opposite forward/backward maps of the full labelled pairs", "Supplied contiguity paths from each identity to its backward-forward or forward-backward composite", "Mutually inverse integral homology and contravariant cohomology maps retaining torsion", "Composition transports and concatenates the supplied paths under one shared budget", "Equality retains both maps and both full witness stage lists"], ["RelativeMap", "RelativeMap.pair", "RelativeComplex", "HomotopyPath", "HomotopyPath.pair", "AbelianGroupHomomorphism", "N", "Boolean"]),
        ("RelativeMap.pair", "Ordered forward and backward maps for homotopy equivalence construction", "mathematics.foundations.Pair<RelativeSimplicialMap,RelativeSimplicialMap>",
         ["Both entries belong to the actual RelativeMap Algebra", "First entry is forward f and second backward g; equivalence construction checks opposite boundaries"], ["RelativeMap", "HomotopyEquivalence"]),
        ("HomotopyPath.pair", "Ordered source and target inverse witnesses", "mathematics.foundations.Pair<SimplicialHomotopyPath,SimplicialHomotopyPath>",
         ["Both entries belong to the actual HomotopyPath Algebra", "First entry is the source witness and second the target witness; equivalence construction checks identities and composite endpoints"], ["HomotopyPath", "HomotopyEquivalence"]),
        ("HomotopyPath", "Finite contiguity paths and accumulated integral prisms", "mathematics.topology.SimplicialHomotopyPath",
         ["One to 256 ordered stage maps retaining equal full source/target pairs", "Every consecutive pair is contiguous in both target components", "Accumulated integral prisms and their transposes satisfy telescoping homotopy identities", "Chronological concatenation checks full joining-map equality and retains repetitions", "Matrix sums, flat lists and composed-stage validation each share one computation budget"], ["SimplicialHomotopy", "RelativeMap", "RelativeComplex", "RelativeChain", "RelativeCochain", "SimplicialChain", "SimplicialCochain", "Mat(Z)", "N", "Boolean"]),
        ("SimplicialHomotopy", "Integral prism witnesses for contiguous simplicial maps", "mathematics.topology.SimplicialHomotopy",
         ["Ordered maps of equal full labelled pairs, contiguous in both ambient and subcomplex targets", "Degree-raising integral prism and degree-lowering transpose satisfying the chain and cochain homotopy identities", "Filtered quotient bases, exact orientation signs and actual chain/cochain wrappers"], ["SimplicialMap", "RelativeMap", "RelativeComplex", "SimplicialChain", "SimplicialCochain", "RelativeChain", "RelativeCochain", "Mat(Z)", "N", "Boolean"]),
        ("TripleMap", "Simplicial maps of triples and natural integral exact sequences", "mathematics.topology.RelativeSimplicialTripleMap",
         ["Full ordered source and target triples with one ambient vertex map preserving both nested subcomplexes", "Compatible outer, total and inner pair maps with oriented quotient-chain maps", "Covariant integral homology and contravariant cohomology retaining full presentations", "Four vertical maps commuting with each exact sequence under one shared computation budget"], ["RelativeTriple", "RelativeTriple.pair", "SimplicialMap", "RelativeMap", "N", "Boolean", "AbelianGroupHomomorphism"]),
        ("RelativeTriple.pair", "Ordered source and target triples for a simplicial triple map", "mathematics.foundations.Pair<RelativeSimplicialTriple,RelativeSimplicialTriple>",
         ["Both entries belong to the actual RelativeTriple Algebra", "First entry is the full source triple and second the full target triple"], ["RelativeTriple", "TripleMap", "SimplicialMap"]),
        ("RelativeTriple", "Integral exact homology and cohomology of nested simplicial triples", "mathematics.topology.RelativeSimplicialTriple",
         ["Retained full labelled inclusions B subset A subset X and outer/total/inner quotient pairs", "Short exact quotient chain sequence and its dual cochain sequence", "Typed degree-lowering connecting cycles and degree-raising connecting cocycles", "Integral long exact sequences with retained quotient presentations and shared whole-segment work budgets"], ["RelativeComplex", "FiniteComplex", "RelativeMap", "RelativeChain", "RelativeCochain", "Mat(Z)", "N", "Boolean", "AbelianGroupHomomorphism"]),
        ("RelativeCap", "General integral relative cap products with explicit targets", "mathematics.topology.RelativeCapProduct",
         ["Retained chain on (X,D) and explicit target (X,B), with B contained in D", "Cochain on (X,A) checked against the exact simplex-wise union D=A union B", "Filtered source, cochain and target quotient bases with signed boundary and relative cup compatibility", "Induced integral homology/cohomology maps with retained presentations and shared computation budgets"], ["RelativeChain", "RelativeComplex", "RelativeCochain", "Mat(Z)", "Boolean", "AbelianGroupElement", "AbelianGroupHomomorphism"]),
        ("RelativeChain", "Integral quotient chains and relative cap products", "mathematics.topology.RelativeSimplicialChain",
         ["Full labelled pair, integer degree and integral coordinates outside the subcomplex", "Unreduced quotient boundary and connecting cycles on the full subcomplex", "Pair-map pushforward and pairing with relative cochains", "Absolute cochains act on relative chains; relative cochains cap into absolute chains", "Induced integral cap maps retain quotient presentations and share computation budgets"], ["RelativeComplex", "RelativeMap", "SimplicialChain", "SimplicialCochain", "RelativeCochain", "Vec(Z)", "Mat(Z)", "Z", "N", "Boolean", "IntegralHomology", "AbelianGroupElement", "AbelianGroupHomomorphism"]),
        ("SimplicialChain", "Integral simplicial chains, pairing and cap products", "mathematics.topology.SimplicialChain",
         ["Full labelled complex, integer degree and oriented integral simplex coordinates", "Unreduced boundary with only zero chains in negative degrees", "Covariant simplicial pushforward and pairing adjoint to cochain pullback", "Degree-lowering cap products and induced integral maps retaining quotient presentations"], ["FiniteComplex", "SimplicialMap", "SimplicialCochain", "Vec(Z)", "Mat(Z)", "Z", "N", "Boolean", "IntegralHomology", "AbelianGroupElement", "AbelianGroupHomomorphism"]),
        ("RelativeCochain", "Relative integral cochains, cup products and natural exact cohomology sequences", "mathematics.topology.RelativeSimplicialCochain",
         ["Full labelled simplicial pair, nonnegative degree and integral coordinates dual to the quotient-chain basis", "Relative cup products land on the union of both input subcomplexes", "Degree-raising connecting cohomology and contravariant pair-map naturality", "Actual integral quotient presentations and cocycle representatives"], ["RelativeComplex", "RelativeMap", "SimplicialCochain", "Vec(Z)", "Mat(Z)", "Z", "N", "Boolean", "IntegralHomology", "AbelianGroupElement", "AbelianGroupHomomorphism"]),
        ("SimplicialCochain", "Integral cochains, cup products and contravariant cohomology", "mathematics.topology.SimplicialCochain",
         ["Full labelled finite complex, nonnegative degree and integer coordinates dual to the oriented simplex basis", "Transpose-boundary differential and Alexander-Whitney cup product", "Constructive integral quotient classes, cocycle representatives and contravariant simplicial maps", "Cochain equality distinguished from cohomology equality"], ["FiniteComplex", "SimplicialMap", "Vec(Z)", "Z", "N", "Boolean", "IntegralHomology", "AbelianGroupElement", "AbelianGroupHomomorphism"]),
        ("CoverMap", "Maps of ordered covers with natural Mayer-Vietoris and excision diagrams", "mathematics.topology.SimplicialCoverMap",
         ["Retained full ordered source and target covers", "Union simplicial map preserving each ordered piece", "Covariant sum homology, contravariant sum cohomology and their four vertical Mayer-Vietoris maps with retained integral presentations", "Two actual relative maps commuting with the excision inclusions"], ["SimplicialCover", "SimplicialCover.pair", "SimplicialMap", "Mat(Z)", "IntegralHomology", "AbelianGroupHomomorphism", "RelativeMap", "N", "Boolean"]),
        ("SimplicialCover.pair", "Source and target ordered covers for a cover map", "mathematics.foundations.Pair<SimplicialCover,SimplicialCover>",
         ["Both values belong to the actual SimplicialCover Algebra", "First entry is the source cover and second is the target cover"], ["SimplicialCover", "CoverMap", "SimplicialMap"]),
        ("SimplicialCover", "Two-subcomplex covers with integral Mayer-Vietoris maps", "mathematics.topology.SimplicialCover",
         ["Ordered finite labelled pieces A and B with ambient complex exactly their simplex-wise union", "Left-then-right sum chain coordinates and signed intersection inclusion (i,-j)", "Retained integral homology and cohomology presentations, exact sequence maps and simplicial excision"], ["FiniteComplex", "Z", "N", "Boolean", "Mat(Z)", "IntegralHomology", "AbelianGroupHomomorphism", "RelativeMap"]),
        ("RelativeMap", "Simplicial maps of pairs and natural relative homology maps", "mathematics.topology.RelativeSimplicialMap",
         ["Retained full source and target labelled pairs", "Ambient simplicial map carrying every source subcomplex simplex into the target subcomplex", "Oriented quotient chain matrices and integral homology maps natural with the long exact sequence"], ["RelativeComplex", "RelativeComplex.pair", "SimplicialMap", "Mat(Z)", "IntegralHomology", "AbelianGroupHomomorphism", "N", "Boolean"]),
        ("RelativeComplex.pair", "Source and target pairs for a relative simplicial map", "mathematics.foundations.Pair<RelativeSimplicialComplex,RelativeSimplicialComplex>",
         ["Both values belong to the actual RelativeComplex Algebra", "First entry is the source pair and second is the target pair"], ["RelativeComplex", "RelativeMap", "SimplicialMap"]),
        ("RelativeComplex", "Relative integral simplicial homology and long exact sequence maps", "mathematics.topology.RelativeSimplicialComplex",
         ["Retained labelled ambient complex X and subcomplex A", "Quotient chains on simplices of X outside A with increasing-vertex orientations", "Unreduced integral relative homology and inclusion, quotient, connecting maps"], ["FiniteComplex", "FiniteSet(Z)", "Z", "N", "Boolean", "Mat(Z)", "IntegralHomology", "AbelianGroupType", "AbelianGroupHomomorphism", "SimplicialMap"]),
        ("SimplicialMap", "Finite simplicial maps and induced integral maps", "mathematics.topology.FiniteSimplicialMap",
         ["Complete labelled source and target complexes", "Total vertex map preserving every source simplex", "Increasing-vertex chain orientations; collapsed simplices map to zero"], ["FiniteComplex", "FiniteComplex.pair", "FiniteFunction(Z,Z)", "FiniteSet(Z)", "Z", "N", "Boolean", "Mat(Z)", "IntegralHomology", "AbelianGroupHomomorphism"]),
        ("FiniteComplex.pair", "Source and target complexes for a vertex map", "mathematics.foundations.Pair<FiniteSimplicialComplex,FiniteSimplicialComplex>",
         ["Both values belong to the registered FiniteComplex Algebra", "First entry is the source and second is the target"], ["FiniteComplex", "FiniteFunction(Z,Z)", "SimplicialMap"]),
        ("IntegralHomology", "Constructive integral homology in one degree", "mathematics.topology.IntegralHomology",
         ["Consecutive boundary matrices share the middle dimension and compose to zero", "Retained full integral cycle basis and boundary coordinates", "Presented quotient with chain representatives and degreewise induced maps"], ["FiniteComplex", "Mat(Z)", "Vec(Z)", "PresentedAbelianGroup", "AbelianGroupType", "AbelianGroupElement", "AbelianGroupHomomorphism", "IntegralHomology.map-input", "N", "Boolean"]),
        ("IntegralHomology.map-input", "Target homology and a degree matrix", "mathematics.foundations.Pair<IntegralHomology,IntegerMatrix>",
         ["First entry is the target homology value", "Second entry belongs to the actual Mat(Z) Algebra", "Execution checks chain dimensions and preservation of cycles and boundaries"], ["IntegralHomology", "Mat(Z)", "AbelianGroupHomomorphism"]),
        ("AbelianGroupHomomorphism", "Explicit abelian-group homomorphisms", "mathematics.structures.AbelianGroupHomomorphism",
         ["Retained source and target presentations", "Normalized target Smith-coordinate generator images", "Every source relation maps to zero in the target quotient"], ["PresentedAbelianGroup", "AbelianGroupElement", "PresentedAbelianGroup.pair", "Mat(Z)", "Z", "Boolean"]),
        ("PresentedAbelianGroup.pair", "Source and target abelian presentations", "mathematics.foundations.Pair<PresentedAbelianGroup,PresentedAbelianGroup>",
         ["Both entries belong to the registered PresentedAbelianGroup Algebra", "The first entry is the source and the second is the target"], ["PresentedAbelianGroup", "AbelianGroupHomomorphism", "Mat(Z)"]),
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
