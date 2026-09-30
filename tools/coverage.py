"""Validate the mathematical registry and generate reviewable reports (stdlib only)."""
import argparse
import collections
import csv
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATABASE = ROOT / "mathematics-coverage.json"
REPRESENTATIONS = ("DIRECTLY_SUPPORTED", "SUPPORTED_WITH_COMPOSITION", "REQUIRES_EXTENSION", "NOT_FAITHFULLY_REPRESENTABLE")
IMPLEMENTATIONS = ("IMPLEMENTED", "PARTIAL", "CATALOG_ONLY", "PROPOSED")
SEMANTICS = ("SCALAR", "LIST", "SET", "MULTISET", "SEQUENCE", "DISTRIBUTION", "SOLUTION_FAMILY")
REQUIRED = (
    "id mathematical_area subfield concept specification_section record_kind domain_A domain_B domain_C "
    "operation_signature arity scalar_or_flat representation_scope required_invariants representation_status "
    "framework_mapping implementation_status implementation_paths tests machine_tested human_review_status "
    "reviewer review_date review_notes formal_verification_status known_limitations required_extension "
    "references epistemic_status provenance").split()


def normalized(text):
    return re.sub(r"[^a-z0-9]", "", text.lower())


def requested_concepts(spec):
    """The supplied topic lists in sections 10-30, deduplicated within each section."""
    section = 0
    result = set()
    for line in spec.splitlines():
        match = re.match(r"^#{1,2} (\d+)\.", line)
        if match:
            section = int(match[1])
        if 10 <= section <= 30 and line.startswith("* "):
            result.add((section, normalized(line[2:])))
    return result


def validate(data, root=ROOT, check_spec=True):
    errors = []
    def require(condition, message):
        if not condition:
            errors.append(message)

    def local_path(path, context):
        if not isinstance(path, str):
            errors.append(context + ": path must be text")
            return
        target = (root / path).resolve()
        require(target.is_relative_to(root.resolve()) and target.is_file(), context + ": missing/unsafe path " + path)

    require(data.get("schema_version") == 1, "unsupported schema_version")
    for field in ("scope", "classification_basis", "survey_date", "baseline_commit"):
        require(bool(data.get(field)), "missing " + field)
    concepts = data.get("concepts", [])
    require(bool(concepts), "empty concept registry")
    extensions = {x["id"]: x for x in data.get("extensions", [])}
    require(len(extensions) == len(data.get("extensions", [])), "duplicate extension id")
    for extension in extensions.values():
        local_path(extension["rfc"], extension["id"])
        for field in ("reason", "smallest_required_extension", "affected_mathematical_areas", "alternative_representation"):
            require(bool(extension.get(field)), extension["id"] + ": missing " + field)
    seen = set()
    runtime = set()
    for record in concepts:
        rid = record.get("id", "<missing>")
        require(rid not in seen, "duplicate concept id: " + rid)
        seen.add(rid)
        missing = set(REQUIRED) - record.keys()
        require(not missing, rid + ": missing fields " + ", ".join(sorted(missing)))
        if missing:
            continue
        require(record["representation_status"] in REPRESENTATIONS, rid + ": invalid representation_status")
        require(record["implementation_status"] in IMPLEMENTATIONS, rid + ": invalid implementation_status")
        require(record["scalar_or_flat"] in SEMANTICS, rid + ": invalid result semantics")
        require(record["record_kind"] in ("OPERATION", "DOMAIN_OR_STRUCTURE"), rid + ": invalid record_kind")
        for field in ("concept", "mathematical_area", "subfield", "representation_scope", "framework_mapping", "domain_C"):
            require(isinstance(record[field], str) and bool(record[field].strip()), rid + ": empty " + field)
        for field in ("required_invariants", "known_limitations", "references"):
            require(isinstance(record[field], list) and bool(record[field]), rid + ": empty " + field)
        for reference in record["references"]:
            require(reference.startswith("https://"), rid + ": reference is not an HTTPS source")
        for path in record["implementation_paths"] + record["tests"]:
            local_path(path, rid)
        tested = record["machine_tested"]
        require(type(tested) is bool, rid + ": machine_tested must be Boolean")
        if record["implementation_status"] in ("IMPLEMENTED", "PARTIAL"):
            require(bool(record["implementation_paths"]) and bool(record["tests"]) and tested,
                    rid + ": implemented scope needs source and test evidence")
        else:
            require(not tested, rid + ": catalog-only record cannot claim machine tests")
        if tested:
            require(bool(record["tests"]), rid + ": machine_tested without tests")
        extension = record["required_extension"]
        if record["representation_status"] == "REQUIRES_EXTENSION":
            require(isinstance(extension, dict) and extension.get("id") in extensions, rid + ": missing required extension")
        if isinstance(extension, dict):
            require(extension.get("id") in extensions, rid + ": unknown extension")
            for field in ("reason", "smallest_required_extension", "affected_mathematical_areas", "alternative_representation"):
                require(bool(extension.get(field)), rid + ": extension missing " + field)
        if record["record_kind"] == "OPERATION":
            require(record["arity"] in (1, 2), rid + ": operations require one or two explicit domains")
            require(bool(record["domain_A"]) and bool(record["operation_signature"]), rid + ": missing operation signature")
            require(record.get("total_or_partial") in ("TOTAL", "PARTIAL"), rid + ": missing partiality")
            require((record["domain_B"] is not None) == (record["arity"] == 2), rid + ": arity/domain_B mismatch")
        else:
            require(record["arity"] is None and record["operation_signature"] is None, rid + ": object forced into operation")
        runtime_id = record.get("runtime_operation_id")
        if runtime_id:
            require(runtime_id not in runtime, rid + ": duplicate runtime operation")
            runtime.add(runtime_id)
        review = record["human_review_status"]
        require(review in ("UNREVIEWED", "HUMAN_REVIEWED"), rid + ": invalid human review")
        if review == "HUMAN_REVIEWED":
            require(bool(record["reviewer"]) and bool(record["review_date"]) and bool(record["review_notes"]), rid + ": unsupported human-review claim")
        else:
            require(record["reviewer"] is None and record["review_date"] is None, rid + ": unreviewed record has reviewer/date")
        formal = record["formal_verification_status"]
        require(formal in ("UNVERIFIED", "FORMALLY_VERIFIED"), rid + ": invalid formal status")
        if formal == "FORMALLY_VERIFIED":
            require(bool(record.get("formal_artifact")), rid + ": formal claim requires artifact")
            if record.get("formal_artifact"):
                local_path(record["formal_artifact"], rid)
        if record["epistemic_status"] in ("PROVED", "FORMALLY_VERIFIED", "MACHINE_CHECKED"):
            require(bool(record.get("proof_evidence")), rid + ": proof status without evidence")
        provenance = record["provenance"]
        for field in ("definition_source", "generated_by_model", "version", "date", "assumptions"):
            require(bool(provenance.get(field)), rid + ": missing provenance " + field)
        if record["implementation_paths"]:
            require(bool(provenance.get("implementation_source")), rid + ": implementation provenance missing")
    domains = data.get("domains", [])
    domain_ids = {x["id"] for x in domains}
    require(len(domain_ids) == len(domains), "duplicate domain metadata id")
    for domain in domains:
        for field in ("name", "description", "member_representation", "membership_rules", "invariants", "references", "provenance"):
            require(bool(domain.get(field)), domain["id"] + ": missing domain metadata " + field)
        for related in domain.get("parent_domains", []) + domain.get("subdomains", []) + domain.get("related_domains", []):
            require(related in domain_ids, domain["id"] + ": unknown related domain " + related)
        for operation in domain.get("supported_operations", []):
            require(operation in runtime, domain["id"] + ": unknown runtime operation " + operation)
        expected_operations = {r["runtime_operation_id"] for r in concepts if r.get("runtime_operation_id")
                               and domain["id"] in (r["domain_A"], r["domain_B"], r["domain_C"])}
        require(set(domain.get("supported_operations", [])) == expected_operations,
                domain["id"] + ": domain operation participation drift")
    for item in data.get("discovery_backlog", []):
        require(item.get("concept_id") in seen, "backlog refers to unknown concept")
    if check_spec:
        expected = requested_concepts((root / "docs/UNIVERSAL_MATHEMATICS_SPEC.md").read_text(encoding="utf-8"))
        actual = {(x["specification_section"], normalized(x["concept"])) for x in concepts}
        for section, concept in sorted(expected - actual):
            errors.append("missing specification concept: section %s %s" % (section, concept))
        catalog = (root / "groupimp/src/main/java/mathematics/catalog/StandardMathematics.java").read_text(encoding="utf-8")
        declared = set(re.findall(r'new (?:Unary|Binary|Flat)Operation<>[^\n]*?Metadata\.of\("([^"]+)"', catalog))
        standard = {r.get("runtime_operation_id") for r in concepts if r.get("runtime_catalog") == "StandardMathematics"}
        require(standard == declared, "runtime operation registry drift: " + str(sorted(standard ^ declared)))
        with (root / "groupimp/src/test/resources/mathematics/concrete-catalog.tsv").open(encoding="utf-8-sig") as manifest:
            concrete = {row["id"]: row for row in csv.DictReader(manifest, delimiter="\t")}
        mapped = {r["runtime_operation_id"]: r for r in concepts if r.get("runtime_catalog") == "ConcreteMathematics"}
        require(mapped.keys() == concrete.keys(), "runtime operation registry drift: concrete algebra operation ids")
        for operation_id in mapped.keys() & concrete.keys():
            record, row = mapped[operation_id], concrete[operation_id]
            expected = (row["first"], None if row["second"] == "-" else row["second"], row["result"], row["semantics"], row["partiality"], row["alias"], row["interface"])
            actual = tuple(record.get(k) for k in ("domain_A", "domain_B", "domain_C", "scalar_or_flat", "total_or_partial", "legacy_operation_name", "legacy_operation_interface"))
            require(expected == actual, "concrete runtime signature drift: " + operation_id)
    return errors


def cell(text):
    return str(text).replace("|", r"\|").replace("\n", " ")


def table(headers, rows):
    return "\n".join(["| " + " | ".join(headers) + " |", "| " + " | ".join("---" for _ in headers) + " |"]
                     + ["| " + " | ".join(cell(x) for x in row) + " |" for row in rows])


def reports(data):
    records = sorted(data["concepts"], key=lambda r: (r["mathematical_area"], r["concept"]))
    counts = collections.Counter(x["representation_status"] for x in records)
    counts.update({"IMPLEMENTED": sum(x["implementation_status"] == "IMPLEMENTED" for x in records),
                   "PARTIAL": sum(x["implementation_status"] == "PARTIAL" for x in records),
                   "CATALOG_ONLY": sum(x["implementation_status"] == "CATALOG_ONLY" for x in records),
                   "MACHINE_TESTED": sum(x["machine_tested"] for x in records),
                   "HUMAN_REVIEWED": sum(x["human_review_status"] == "HUMAN_REVIEWED" for x in records),
                   "FORMALLY_VERIFIED": sum(x["formal_verification_status"] == "FORMALLY_VERIFIED" for x in records)})
    lines = ["# Mathematics coverage", "",
             "<!-- Generated by tools/coverage.py. Edit mathematics-coverage.json instead. -->", "",
             data["scope"], "", data["classification_basis"], "",
             "Each row is a scoped assessment, not a unit of all known mathematics. No universal coverage percentage is defined. "
             "CATALOG_ONLY records are provisional triage, not working implementations. MACHINE_TESTED counts linked implementation scopes; "
             "it does not count independent tests or certify an entire field. Sources aid discovery and do not certify these mappings.", "",
             table(["Measure", "Count"], [("Scoped records", len(records))] + [(k, counts[k]) for k in REPRESENTATIONS]
                   + [(k, counts[k]) for k in ("IMPLEMENTED", "PARTIAL", "CATALOG_ONLY", "MACHINE_TESTED", "HUMAN_REVIEWED", "FORMALLY_VERIFIED")]),
             "", "## By mathematical area", ""]
    areas = sorted({r["mathematical_area"] for r in records})
    rows = []
    for area in areas:
        group = [r for r in records if r["mathematical_area"] == area]
        rows.append([area, len(group)] + [sum(r["representation_status"] == status for r in group) for status in REPRESENTATIONS]
                    + [sum(r["machine_tested"] for r in group),
                       sum(r["human_review_status"] == "HUMAN_REVIEWED" for r in group),
                       sum(r["formal_verification_status"] == "FORMALLY_VERIFIED" for r in group)])
    lines.extend([table(["Area", "Records", "Direct", "Composition", "Extension", "Boundary", "Tested", "Reviewed", "Formal"], rows), "",
                  "## Scoped mappings", "", "Full invariants, test links, provenance, unresolved obligations, references and extension rationales "
                  "are in [mathematics-coverage.json](../mathematics-coverage.json). "
                  "Implementation progress is described in [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md).", ""])
    for area in areas:
        lines += ["### " + area, "", table(["Concept", "Representation", "Implementation", "Scope"],
                    [(r["concept"], r["representation_status"], r["implementation_status"], r["representation_scope"])
                     for r in records if r["mathematical_area"] == area]), ""]
    lines += ["## Open extensions", ""]
    for extension in data["extensions"]:
        lines += ["- [" + extension["id"] + "](" + extension["rfc"].removeprefix("docs/") + "): " + extension["reason"]]
    lines += ["", "## Discovery backlog", ""]
    for entry in data["discovery_backlog"]:
        lines += ["- " + entry["concept_id"] + ": " + entry["reason"] + " [Discovery source](" + entry["reference"] + ")."]
    lines += ["", "Reproduce with python tools/coverage.py; validate freshness with python tools/coverage.py --check.", ""]
    operations = [r for r in records if r["record_kind"] == "OPERATION"]
    cross = ["# Cross-domain operation catalog", "",
             "<!-- Generated by tools/coverage.py. Edit mathematics-coverage.json instead. -->", "",
             "Includes same-carrier operations to make all signature families comparable. "
             "Domains with the same Java class can have different mathematical membership. "
             "A result labeled LIST has finite ordered sequence semantics with duplicates; a solution family is a single mathematical object, not an eager list.", "",
             "## Executable catalog and scoped library mappings", ""]
    def row(r):
        links = ", ".join("[" + Path(p).stem + "](../" + p + ")" for p in r["tests"])
        return (r["concept"], r["operation_signature"], r.get("legacy_operation_interface", r.get("runtime_catalog", "Scoped library / proposal")), r["total_or_partial"], r["scalar_or_flat"],
                r["implementation_status"], r["representation_scope"], links or "No implementation")
    headers = ["Operation", "Signature", "Native interface / scope", "Partiality", "Result", "Implementation", "Scope", "Tests"]
    cross += [table(headers, [row(r) for r in operations if r["machine_tested"]]), "",
              "## Catalog-only candidates", "", table(headers, [row(r) for r in operations if not r["machine_tested"]]), "",
              "A catalog signature is a proposed mathematical contract. It does not assert an available solver, decidable membership, "
              "existence of a result, uniqueness, or complete enumeration. See each record's required_extension and invariants in the JSON registry.", "",
              "See [the operation model](MATHEMATICAL_OPERATION_MODEL.md) for native execution, composition, failures and the optional prototype.", ""]
    return {"docs/MATHEMATICS_COVERAGE.md": "\n".join(lines),
            "docs/CROSS_DOMAIN_OPERATION_CATALOG.md": "\n".join(cross)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Validate without writing; fail on stale generated reports.")
    args = parser.parse_args()
    data = json.loads(DATABASE.read_text(encoding="utf-8"))
    errors = validate(data)
    if not errors:
        for path, content in reports(data).items():
            target = ROOT / path
            if args.check:
                if not target.exists() or target.read_text(encoding="utf-8") != content:
                    errors.append("stale generated report: " + path)
            else:
                target.write_text(content, encoding="utf-8", newline="\n")
    if errors:
        for error in errors:
            print(error)
        return 1
    print("Validated %s scoped concepts, %s domain descriptors, %s extension proposals." %
          (len(data["concepts"]), len(data["domains"]), len(data["extensions"])))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
