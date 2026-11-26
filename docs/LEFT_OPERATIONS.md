# Left operations

Left operations are the mixed shape **A x B -> B**. The historical names `ILeftProjectionOperation` and `ILeftProjectionFlatOperation` are the scalar and flat interfaces. The result is the second operand's carrier, not the first.

`ICustomMemberOperation` remains **A x B -> A**. Do not use a left operation when the result must stay in the first algebra.

## Interfaces

| Shape | Interface | Implementation | Result |
| --- | --- | --- | --- |
| A x B -> B, second value unchanged | `ILeftProjectionOperation<A,B>` | `LeftProjectionOperation` | `IAlgebraItem<B>` |
| A x B -> B, second value transformed | `ILeftProjectionOperation<A,B>` | `SecondResultOperation` | `IAlgebraItem<B>` |
| A x B -> List(B), second value unchanged | `ILeftProjectionFlatOperation<A,B>` | `LeftProjectionFlatOperation` | `List<IAlgebraItem<B>>` |
| A x B -> List(B), second value transformed | `ILeftProjectionFlatOperation<A,B>` | `SecondResultFlatOperation` | `List<IAlgebraItem<B>>` |

Register on the **first** operand's algebra with `addLeftProjectionOperation` or `addLeftProjectionFlatOperation`. Pass the **second** operand's algebra to the constructor: that algebra builds the result wrapper and supplies `getAlgebraName()` and `getResultBaseClass()`.

Lookup is one operation per name. The call matches when the registered second class is assignable from the runtime second class. A missing name or an incompatible second class is `UnsupportedOperationException`. Re-registering the same name replaces that entry. This is not the most-specific overload table used by custom-member operations.

`LeftProjectionOperation` checks only the second value through its algebra, then wraps that same value. `SecondResultOperation` checks both operands, runs the body, and checks the body result before wrapping it. An invalid member is `NotMemberException`. Flat forms keep order, duplicates and empty lists.

## Item and flow calls

```java
Algebra<Integer> integers = new Algebra<>("integer", Integer.class, "Integers");
Algebra<String> text = new Algebra<>("text", String.class, "Text");
integers.addLeftProjectionOperation("second", new LeftProjectionOperation<>(text, String.class));
integers.addLeftProjectionFlatOperation("second-flat", new LeftProjectionFlatOperation<>(text, String.class));
integers.addLeftProjectionOperation("tagged",
        new SecondResultOperation<>("Tag", integers, text, (count, value) -> value + count));

IAlgebraItem<String> kept = integers.buildAlgebraItem(5)
        .performLeftProjectionOperation("second", "kept");
// kept.getResult() is "kept"; kept.getAlgebra() is text
```

On a flow, `performLeftProjectionOperation` and `performLeftProjectionFlatOperation` append the call and switch the current algebra to B. Later unary and closed operations run in B. Both algebras must already be in the `MathTool`. If B is missing, scheduling throws `AlgebraNotExistsException` and leaves the existing plan usable.

Native builders choose this interface automatically when the result algebra is the second operand's algebra. Examples include `Q^n.scale-left`, matrix `apply`, and typed chain or cochain actions that return the second carrier's wrapper. The operation model table is in [MATHEMATICAL_OPERATION_MODEL.md](MATHEMATICAL_OPERATION_MODEL.md). Behavior is covered by [LeftProjectionIntegrationTest](../groupimp/src/test/java/operations/LeftProjectionIntegrationTest.java).
