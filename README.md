This framework designed for math modelling and analysis. It's based on group theory (https://en.wikipedia.org/wiki/Group_theory) and my expirience with big data tools. You can read about key concepts and how to use in manual.

Concrete exact algebras are available through `algebra.concrete.ConcreteMathematics`, which implements the existing `IMathToolInitializer`. It registers N, Z, Q, Q(i), Boolean, prime fields, composite residue rings, rational vectors, matrices, polynomials, rational functions, symmetric groups, finite sets, finite relations, finite functions, finite categories, samples, probability measures and simplicial complexes directly in `MathTool` using implementations of the interfaces in `operations`.

See [concrete algebras and usage](docs/CONCRETE_ALGEBRAS.md). Same-algebra unary operations now use `IOneOperandOperation` and `performOneOperandOperation(name)` on items and flows. `ICustomMemberOperation` returns the first operand type; `ILeftProjectionOperation` returns the second operand type, wrapped in `IAlgebraItem`.

Run the tests with `mvn -f groupimp/pom.xml test`.

See [implementation status](docs/IMPLEMENTATION_STATUS.md), [the operation model](docs/MATHEMATICAL_OPERATION_MODEL.md), [coverage](docs/MATHEMATICS_COVERAGE.md), and [maintenance instructions](docs/ADDING_MATHEMATICS.md).
