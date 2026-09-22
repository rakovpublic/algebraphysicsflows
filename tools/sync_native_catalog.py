"""Synchronize native registration facts; preserve the separately authored survey."""
import argparse
import copy
import csv
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "mathematics-coverage.json"
MANIFEST = ROOT / "groupimp/src/test/resources/mathematics/concrete-catalog.tsv"
DATE = "2026-09-22"
OWNERS = {
    "BooleanAlgebra": ("Boolean", "Boolean truth values with Boolean operations"),
    "NaturalSemiring": ("N", "Nonnegative arbitrary precision integers with addition and multiplication"),
    "IntegerRing": ("Z", "Arbitrary precision integers with ring operations and truncated quotient"),
    "RationalField": ("Q", "Canonical exact rational field"),
    "PrimeField": ("Z/5Z", "Residues in the default prime field F5; configurable exactly checked prime int modulus"),
    "ResidueRing": ("Z/6Z", "Residue ring modulo six by default; configurable arbitrary-precision modulus greater than one"),
    "RationalComplexField": ("Q(i)", "Pairs of rational coordinates; a proper subfield of the complex numbers"),
    "RationalVectorSpace": ("Q^2", "Fixed-dimensional rational vectors; default dimension two"),
    "RationalMatrixAlgebra": ("Mat2(Q)", "Fixed positive-dimensional square rational matrices; default dimension two"),
    "RationalPolynomialRing": ("Q[x]", "Finite univariate polynomials with canonical rational coefficients"),
    "RationalFunctionField": ("Q(x)", "Formal univariate rational functions over Q, normalized to coprime polynomials with monic denominator"),
    "IntegerSetAlgebra": ("FiniteSet(Z)", "Finite integer sets under canonical equality, with polynomial optimization over explicit feasible sets"),
    "RationalSampleAlgebra": ("Sample(Q)", "Finite ordered rational samples retaining repeated observations"),
    "FiniteProbabilityAlgebra": ("FiniteDistribution(Z)", "Finite integer distributions with exact nonnegative rational masses summing to one"),
    "FiniteSimplicialAlgebra": ("FiniteComplex", "Finite abstract simplicial complexes with integer labels and unreduced homology over F2"),
    "FiniteIntegerRelationAlgebra": ("FiniteRelation(Z,Z)", "Finite-support relations on the actual registered integer Algebra"),
    "FiniteIntegerFunctionAlgebra": ("FiniteFunction(Z,Z)", "Total maps between explicit finite integer sets, preserving declared domain and codomain"),
    "FiniteCategoryAlgebra": ("FiniteCategory", "Finite categories with integer object/arrow labels and exhaustively checked composition tables"),
    "FiniteFunctorAlgebra": ("FiniteFunctor", "Covariant functors between validated finite category tables with total object and arrow maps"),
    "FiniteNaturalTransformationAlgebra": ("FiniteNaturalTransformation", "Natural transformations between parallel finite functors with every naturality square checked"),
    "FiniteEquivalenceAlgebra": ("FiniteEquivalence", "Finite adjoint equivalences with explicit quasi-inverse, unit, counit and checked triangle identities"),
    "FiniteAdjunctionAlgebra": ("FiniteAdjunction", "Adjunctions of finite category tables with checked unit/counit and constructive finite adjoint search"),
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
    "IntegerSetAlgebra": "NativeFlatAndSetTest",
    "RationalPolynomialRing": "NativeDynamicsTest",
    "RationalFunctionField": "NativeRationalFunctionTest",
    "RationalSampleAlgebra": "NativeStatisticsProbabilityTest",
    "FiniteProbabilityAlgebra": "NativeStatisticsProbabilityTest",
    "FiniteSimplicialAlgebra": "NativeTopologyTest",
    "FiniteIntegerRelationAlgebra": "NativeRelationTest",
    "FiniteIntegerFunctionAlgebra": "NativeFiniteFunctionTest",
    "FiniteCategoryAlgebra": "NativeCategoryTest",
    "FiniteFunctorAlgebra": "NativeFunctorTest",
    "FiniteNaturalTransformationAlgebra": "NativeNaturalTransformationTest",
    "FiniteEquivalenceAlgebra": "NativeEquivalenceTest",
    "FiniteAdjunctionAlgebra": "NativeAdjunctionTest",
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
    if owner == "FiniteSimplicialAlgebra":
        value["known_limitations"] += ["Construction materializes faces and caps each input facet at 20 vertices.",
            "Homology computes unreduced F2 dimensions only; no integral torsion, persistence or homeomorphism decision."]
        value["references"].append("https://pi.math.cornell.edu/~hatcher/AT/ATchapters.html")
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
        value["known_limitations"].append("Underlying category tables retain the 128-arrow/object cap. Components are explicit finite maps; arbitrary infinite natural transformations and general diagram limits remain unimplemented.")
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
