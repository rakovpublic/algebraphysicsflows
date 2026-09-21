# Concrete algebras connected to MathTool

`new ConcreteMathematics()` creates real `Algebra<T>` instances and registers their checked operations in the existing `MathTool`. It includes N, Z, Q, Q(i), Boolean, Q^2, Mat2(Q), Q[x], and F5 (named Z/5Z). Each construction owns its domain/algebra instances; separate tools do not share mutable registrations.

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

`DomainInput<T>` is also available for constructing an AlgebraFlow with an existing MathTool initializer. It copies a finite input list, checks membership, and rejects a different domain instance.

## Registered operation names

| Class / carrier | Same-carrier binary operations | Unary transfers | Mixed/custom-result/flat operations |
| --- | --- | --- | --- |
| NaturalSemiring / N | add, multiply | successor, to-integer | — |
| IntegerRing / Z | add, subtract, multiply, gcd, lcm, quotient, remainder | negate, to-rational | greater/equal -> Boolean; divide-rational -> Q; flat quotient-remainder |
| RationalField / Q | add, subtract, multiply, divide | negate, inverse | greater/equal -> Boolean; flat add-subtract |
| PrimeField / Z/pZ | add, subtract, multiply, divide | negate, inverse | Fixed prime membership, exact residues |
| BooleanAlgebra / Boolean | and, or, xor, implies, equal | not | — |
| RationalComplexField / Q(i) | add, subtract, multiply, divide | negate, conjugate, norm-squared -> Q | Q(i).embed-rational on Q -> Q(i) |
| RationalVectorSpace / Q^n | add, subtract | negate | scale by Q -> Q^n; dot -> Q; flat scale-flat and scale-signs |
| RationalMatrixAlgebra / Matn(Q) | add, subtract, multiply | negate, transpose, inverse, determinant -> Q, trace -> Q | scale by Q; apply to Q^n |
| RationalPolynomialRing / Q[x] | add, subtract, multiply | negate, derivative | evaluate at Q -> Q; primitive with rational constant; integrate with bounds -> Q |

Each algebra registers zero/one constants where applicable on Unit with qualified names such as `Q.zero`, `Z.one`, `Mat2(Q).one`. Vector spaces have zero. Constants are Unit -> carrier transfers.

Ordinary binary names use `performOperation`. Equal-input/different-result operations such as dot use `performCustomResultOperation`. Mixed-operand operations use `performUnsafeOperation` on items or `performAlgebraUnsafe` on flows; the legacy name says unsafe, but the new adapters validate both mathematical operands and the result. Flat mixed operations use `performUnsafeFlatOperation` / `performFlatAlgebraUnsafe`. Same-carrier flat operations use `performFlatOperation`.

## A x B -> A and its flat version

These are actual transformations, not projections or custom-member aliases:

```java
RationalVector v = new RationalVector(Rational.ONE, Rational.of(2));
List<String> scaled = math.flow(math.vectors, Collections.singletonList(v))
        .<RationalVector, Rational>performAlgebraUnsafe("scale", Rational.of(2))
        .<RationalVector, Rational>performFlatAlgebraUnsafe("scale-flat", Rational.of(3))
        .collect(); // ["[6, 12]"]
```

`scale-flat` returns one transformed vector. `scale-signs` returns the ordered pair of positive and negative scaled vectors, preserving duplicates at zero. The opposite-order operation Q x Q^n -> Q^n is registered on Q as `Q^n.scale-left`.

Polynomial integration demonstrates three independent carrier types: Q[x] x (Q x Q bounds) -> Q. Bounds are a Pair of exact rationals, so integration does not disguise both inputs as one Java type.

## Laws, partiality and integration limits

These are concrete mathematical structures with explicit law declarations. Tests exercise exact arithmetic, ring/field identities, Boolean identities, closure, shape/modulus rejection, noncommutative matrix multiplication and calculus identities. Structure declarations remain DEFINED; tests do not claim formal proof.

Division by zero and singular inversion are undefined. Natural subtraction is intentionally absent because N is not closed under it. Integer quotient truncates toward zero; quotient-remainder returns that quotient followed by the corresponding signed remainder. Q(i) is a proper subfield of C. Polynomial integration is in the stated rational scope.

Legacy `buildAlgebraItem` returns null for invalid membership; legacy operand validation throws NotMemberException. Checked Domain/Operation APIs instead report MathFailure. The bridge preserves those existing entry-point conventions.

See [ConcreteAlgebrasTest](../groupimp/src/test/java/mathematics/ConcreteAlgebrasTest.java) for complete working imports and [the executable example](../groupimp/src/main/java/mathematics/examples/ConcreteAlgebrasExample.java). Existing flat-transfer signature and distributed-executor limitations remain documented in the architecture audit.

