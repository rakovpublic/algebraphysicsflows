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
