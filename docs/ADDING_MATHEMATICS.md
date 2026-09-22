# Adding mathematics

## Use the existing architecture

Create or extend a builder in algebra/concrete using the original Algebra. Implement the appropriate interface in operations/simple or operations/flat, or reuse a native implementation with a typed body. [The operation model](MATHEMATICAL_OPERATION_MODEL.md) lists signatures.

Reuse actual carrier instances for dependencies and register through ConcreteMathematics/MathTool. Explicitly check dimension, modulus, normalization and other invariants. A x B -> A is custom member; A x B -> B uses the left-projection interface returning IAlgebraItem<B>. Use unary registries for one-operand operations.

Specify scope, input/output invariants, exactness, partiality and list/set/family semantics. Resource limits are separate from mathematical undefinedness. Law text is documentation until independently checked evidence is supplied.

## Tests and coverage

1. Add independent expected examples, undefined/edge cases and an actual item/flow path. Extend ConcreteAlgebrasTest's complete registration examples.
2. Compile and regenerate the native manifest with ConcreteAlgebrasExample --catalog. It reports actual carriers, alias, partiality and interface. The Java test compares the resource to runtime registrations.
3. Review tools/sync_native_catalog.py's owner descriptions/preconditions, then run it. This updates native facts and exact domain participation, keeping the authored survey. Reviewed/proved native records require manual evidence-preserving updates.
4. Update mathematics-coverage.json with scoped invariants, sources/tests, references, limitations and extensions. One working special case does not make an entire field IMPLEMENTED.
5. Run tools/coverage.py to regenerate the reports. Update implementation status and concrete usage for changed public behavior.
6. Run the checks below and inspect the diff.

From repository root (Python 3.10+ and JDK/Maven):

~~~text
mvn -B -f groupimp/pom.xml clean verify
python tools/sync_native_catalog.py --check
python tools/coverage.py --check
python -m unittest discover -s tools -p "test_*.py"
python tools/generate_reference_fixtures.py --check
git diff --check
~~~

To regenerate the native manifest, compile with Maven and run mathematics.examples.ConcreteAlgebrasExample --catalog with groupimp/target/classes and module runtime dependencies on the Java classpath. Save UTF-8 output to groupimp/src/test/resources/mathematics/concrete-catalog.tsv. Without --catalog the executable prints usage examples. The manifest proves registration agreement, not mathematical correctness.

## Survey and evidence

DIRECTLY_SUPPORTED applies to the stated representation scope, including explicitly identified utilities/prototypes; it does not imply every catalog entry has code. SUPPORTED_WITH_COMPOSITION needs a concrete encoding strategy. REQUIRES_EXTENSION needs a missing obligation and RFC. NOT_FAITHFULLY_REPRESENTABLE needs a precisely bounded impossibility claim with a source.

The inventory checks all requested bullets in sections 10-30. It cannot establish survey correctness or completeness. Broad fields need deeper assessment and specialist review.

Generated drafts are UNREVIEWED. Tests supply empirical evidence. Record the actual reviewer/date/scope or checked formal artifact before changing human/formal labels. Keep exact, symbolic, numerical, approximate and stochastic descriptions separate.
