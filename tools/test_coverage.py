"""Evidence claims must fail closed when registry entries drift."""
import copy
import json
import unittest
import coverage
import csv
import sync_native_catalog


class CoverageTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.data = json.loads(coverage.DATABASE.read_text(encoding="utf-8"))

    def changed(self, predicate, **updates):
        data = copy.deepcopy(self.data)
        next(x for x in data["concepts"] if predicate(x)).update(updates)
        return coverage.validate(data)

    def test_repository_registry_is_valid(self):
        self.assertEqual([], coverage.validate(self.data))

    def test_missing_extension_is_rejected(self):
        errors = self.changed(lambda r: r["representation_status"] == "REQUIRES_EXTENSION", required_extension=None)
        self.assertTrue(any("missing required extension" in x for x in errors))

    def test_missing_test_file_is_rejected(self):
        errors = self.changed(lambda r: r["machine_tested"], tests=["missing/DoesNotExistTest.java"])
        self.assertTrue(any("missing/unsafe path" in x for x in errors))

    def test_proof_claim_needs_evidence(self):
        errors = self.changed(lambda r: True, formal_verification_status="FORMALLY_VERIFIED", epistemic_status="PROVED")
        self.assertTrue(any("formal claim requires artifact" in x for x in errors))
        self.assertTrue(any("proof status without evidence" in x for x in errors))

    def test_human_review_needs_a_named_reviewer(self):
        errors = self.changed(lambda r: True, human_review_status="HUMAN_REVIEWED")
        self.assertTrue(any("unsupported human-review claim" in x for x in errors))

    def test_duplicate_identifier_is_rejected(self):
        data = copy.deepcopy(self.data)
        data["concepts"].append(data["concepts"][0])
        self.assertTrue(any("duplicate concept id" in x for x in coverage.validate(data)))

    def test_arity_cannot_disagree_with_operands(self):
        errors = self.changed(lambda r: r["record_kind"] == "OPERATION" and r["arity"] == 2, domain_B=None)
        self.assertTrue(any("arity/domain_B mismatch" in x for x in errors))

    def test_catalog_only_cannot_claim_tests(self):
        errors = self.changed(lambda r: r["implementation_status"] == "CATALOG_ONLY", machine_tested=True)
        self.assertTrue(any("catalog-only record cannot claim" in x for x in errors))

    def test_missing_requested_concept_is_rejected(self):
        data = copy.deepcopy(self.data)
        data["concepts"] = [r for r in data["concepts"] if r["id"] != "foundations.singleton"]
        self.assertTrue(any("missing specification concept" in x for x in coverage.validate(data)))

    def test_runtime_catalog_drift_is_rejected(self):
        errors = self.changed(lambda r: bool(r.get("runtime_operation_id")), runtime_operation_id="unknown-operation")
        self.assertTrue(any("runtime operation registry drift" in x for x in errors))

    def test_reports_are_deterministic(self):
        reversed_data = copy.deepcopy(self.data)
        reversed_data["concepts"].reverse()
        self.assertEqual(coverage.reports(self.data), coverage.reports(reversed_data))

    def test_native_interface_drift_is_rejected(self):
        errors = self.changed(lambda r: r.get("runtime_catalog") == "ConcreteMathematics",
                              legacy_operation_interface="ICustomMemberOperation")
        self.assertTrue(any("concrete runtime signature drift" in x for x in errors))

    def test_operation_conditions_are_scoped_to_the_algebra(self):
        records = {r.get("runtime_operation_id"): r for r in self.data["concepts"] if r.get("runtime_operation_id")}
        polynomial = " ".join(records["Q[x].quotient"]["required_invariants"])
        self.assertIn("Euclidean", polynomial)
        self.assertNotIn("truncates", polynomial)
        self.assertIn("truncates", " ".join(records["Z.quotient"]["required_invariants"]))
        self.assertIn("f(g(x))", " ".join(records["Q(x).compose"]["required_invariants"]))
        self.assertIn("first relation", " ".join(records["FiniteRelation(Z,Z).compose"]["required_invariants"]))
        self.assertIn("Every permutation", " ".join(records["S3.inverse"]["required_invariants"]))
        self.assertNotIn("10000", " ".join(records["S3.orbit"]["known_limitations"]))
        self.assertIn("10000", " ".join(records["Q[x].orbit"]["known_limitations"]))
        self.assertIn("must be a unit", " ".join(records["Z/6Z.inverse"]["required_invariants"]))
        self.assertIn("nonzero alone is insufficient", " ".join(records["Z/6Z.divide"]["required_invariants"]))
        self.assertIn("empty list", " ".join(records["Z/6Z.solve-multiply"]["required_invariants"]))
        self.assertIn("bijection", " ".join(records["FiniteFunction(Z,Z).inverse"]["required_invariants"]))
        self.assertIn("f(g(x))", " ".join(records["FiniteFunction(Z,Z).compose"]["required_invariants"]))
        self.assertIn("path order", " ".join(records["FiniteCategory.compose"]["required_invariants"]))
        self.assertIn("arrow label", " ".join(records["FiniteCategory.source"]["required_invariants"]))
        self.assertIn("Strict inverse", " ".join(records["FiniteFunctor.inverse"]["required_invariants"]))
        self.assertIn("each source hom set", " ".join(records["FiniteFunctor.is-faithful"]["required_invariants"]))
        self.assertIn("Every component", " ".join(records["FiniteNaturalTransformation.inverse"]["required_invariants"]))
        self.assertIn("Vertical composition", " ".join(records["FiniteNaturalTransformation.compose"]["required_invariants"]))
        self.assertIn("Every validated equivalence", " ".join(records["FiniteEquivalence.inverse"]["required_invariants"]))
        self.assertIn("full, faithful and essentially surjective", " ".join(records["FiniteEquivalence.from-functor"]["required_invariants"]))
        self.assertIn("right adjoint must exist", " ".join(records["FiniteAdjunction.from-left"]["required_invariants"]))
        self.assertIn("Swap adjoint roles", " ".join(records["FiniteAdjunction.opposite"]["required_invariants"]))
        self.assertIn("must be invertible", " ".join(records["FiniteAdjunction.to-equivalence"]["required_invariants"]))
        self.assertIn("entire diagram", " ".join(records["FiniteCone.lift"]["required_invariants"]))
        self.assertIn("never false", " ".join(records["FiniteCone.is-limit"]["required_invariants"]))
        self.assertIn("1000000", " ".join(records["FiniteCone.limit"]["known_limitations"]))

    def test_domain_links_use_exact_domains(self):
        data = copy.deepcopy(self.data)
        next(d for d in data["domains"] if d["id"] == "QxQ.bounds")["supported_operations"].append("Q.add")
        self.assertTrue(any("domain operation participation drift" in x for x in coverage.validate(data)))

    def test_native_sync_is_idempotent_and_preserves_the_survey(self):
        with sync_native_catalog.MANIFEST.open(encoding="utf-8-sig") as source:
            rows = list(csv.DictReader(source, delimiter="\t"))
        result = sync_native_catalog.synchronize(self.data, rows)
        self.assertEqual(self.data, result)
        self.assertEqual(result, sync_native_catalog.synchronize(result, rows))
        reviewed = copy.deepcopy(self.data)
        next(r for r in reviewed["concepts"] if r["id"].startswith("concrete-operation."))["human_review_status"] = "HUMAN_REVIEWED"
        with self.assertRaisesRegex(ValueError, "manual evidence-preserving"):
            sync_native_catalog.synchronize(reviewed, rows)


if __name__ == "__main__":
    unittest.main()
