package operations;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.imp.AlgebraFlow;
import example.IntSumOperation;
import example.ListInput;
import exceptions.UnsupportedOperationException;
import operations.flat.ILeftProjectionFlatOperation;
import operations.flat.LeftProjectionFlatOperation;
import operations.simple.ILeftProjectionOperation;
import operations.simple.LeftProjectionOperation;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class LeftProjectionIntegrationTest {
    private Algebra<Integer> integers() {
        Algebra<Integer> algebra = new Algebra<>("integer", Integer.class, "Integers");
        algebra.addOperation("sum", new IntSumOperation());
        algebra.addLeftProjectionOperation("left", new LeftProjectionOperation<>(algebra, String.class));
        algebra.addLeftProjectionFlatOperation("left", new LeftProjectionFlatOperation<>(algebra, Number.class));
        return algebra;
    }

    private AlgebraFlow<Integer> flow(List<Integer> input) {
        MathTool mathTool = new MathTool("left-projection-test");
        mathTool.addAlgebra(integers());
        return new AlgebraFlow<>(new ListInput(input), () -> mathTool, "integer");
    }

    @Test
    public void mixedOperandTypesComposeWithExistingOperations() {
        List<String> result = flow(Arrays.asList(5, 10, 5))
                .performOperation("sum", 5)
                .performLeftProjectionOperation("left", "ignored")
                .performLeftProjectionFlatOperation("left", 2.5)
                .performOperation("sum", 1)
                .collect();
        assertEquals(Arrays.asList("11", "16", "11"), result);
    }

    @Test
    public void flatResultRetainsTheOriginalReferenceAndAlgebra() {
        Algebra<String> algebra = new Algebra<>("text", String.class, "Text");
        ILeftProjectionOperation<String, Integer> simple = new LeftProjectionOperation<>(algebra, Integer.class);
        ILeftProjectionFlatOperation<String, Boolean> flat = new LeftProjectionFlatOperation<>(algebra, Boolean.class);
        algebra.addLeftProjectionOperation("left", simple);
        algebra.addLeftProjectionFlatOperation("left", flat);
        String first = new String("first");
        List<IAlgebraItem<String>> result = algebra.buildAlgebraItem(first)
                .performLeftProjectionOperation("left", 42)
                .performLeftProjectionFlatOperation("left", false);
        assertEquals(1, result.size());
        assertSame(first, result.get(0).getResult());
        assertSame(algebra, result.get(0).getAlgebra());
        assertEquals(Integer.class, simple.getSecondElementClass());
        assertEquals(Boolean.class, flat.getSecondElementClass());
        assertEquals(String.class, flat.getResultBaseClass());
        assertFalse(algebra.hasCustomMemberOperation("left"));
        assertFalse(algebra.hasCustomMemberFlatOperation("left"));
    }

    @Test
    public void emptyFlowsRemainEmpty() {
        assertTrue(flow(Collections.emptyList())
                .performLeftProjectionOperation("left", "ignored")
                .performLeftProjectionFlatOperation("left", 42)
                .collect().isEmpty());
    }

    @Test
    public void rejectsWrongOperandTypesWithoutAddingAnInvocation() {
        AlgebraFlow<Integer> flow = flow(Arrays.asList(5, 10));
        assertThrows(UnsupportedOperationException.class,
                () -> flow.performLeftProjectionOperation("left", false));
        assertThrows(UnsupportedOperationException.class,
                () -> flow.performLeftProjectionFlatOperation("left", "wrong type"));
        assertEquals(Arrays.asList("5", "10"), flow.collect());
        IAlgebraItem<Integer> item = integers().buildAlgebraItem(5);
        assertThrows(UnsupportedOperationException.class,
                () -> item.performLeftProjectionOperation("left", 42));
        assertThrows(UnsupportedOperationException.class,
                () -> item.performLeftProjectionFlatOperation("left", false));
    }

    @Test
    public void missingOperationsFailClearly() {
        AlgebraFlow<Integer> flow = flow(Collections.singletonList(5));
        assertThrows(UnsupportedOperationException.class,
                () -> flow.performLeftProjectionOperation("missing", "ignored"));
        assertThrows(UnsupportedOperationException.class,
                () -> flow.performLeftProjectionFlatOperation("missing", 42));
    }
}
