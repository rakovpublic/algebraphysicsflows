package operations;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.imp.AlgebraFlow;
import algebraflow.imp.ListAlgebraInput;
import exceptions.NotMemberException;
import operations.simple.*;
import org.junit.Test;
import rules.TypedMembershipRule;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public class OneOperandIntegrationTest {
    private Algebra<Integer> integers() {
        Algebra<Integer> algebra=new Algebra<>("integer",Integer.class,"Integers");
        algebra.addOperation("add",new ClosedOperation<>("Add",algebra,(a,b) -> a+b));
        algebra.addOperation("multiply",new ClosedOperation<>("Multiply",algebra,(a,b) -> a*b));
        algebra.addOneOperandOperation("negate",new OneOperandOperation<>("Negate",algebra,n -> -n));
        return algebra;
    }
    private AlgebraFlow<Integer> flow(Algebra<Integer> algebra,List<Integer> values) {
        MathTool tool=new MathTool("unary"); tool.addAlgebra(algebra);
        return new AlgebraFlow<>(new ListAlgebraInput<>(algebra,values),() -> tool,algebra.getAlgebraName());
    }
    @Test public void itemUnaryOperationEvaluatesPendingBinaryWorkAndRetainsItsAlgebra() {
        Algebra<Integer> algebra=integers();
        IAlgebraItem<Integer> result=algebra.buildAlgebraItem(5).performOperation("add",2)
                .performOperation("negate").performOperation("multiply",3).perform();
        assertEquals(Integer.valueOf(-21),result.getResult());
        assertSame(algebra,result.getAlgebra());
        assertTrue(algebra.hasOneOperandOperation("negate"));
        assertEquals(Void.class,algebra.getOneOperandOperation("negate").getSecondElementClass());
    }
    @Test public void flowUnaryWorkIsDeferredAndCanBeCollectedRepeatedly() {
        Algebra<Integer> algebra=integers(); AtomicInteger calls=new AtomicInteger();
        algebra.addOneOperandOperation("square",new OneOperandOperation<>("Square",algebra,n -> { calls.incrementAndGet(); return n*n; }));
        algebraflow.IAlgebraFlow<Integer> values=flow(algebra,Arrays.asList(2,3)).performOneOperandOperation("square").performOperation("negate");
        assertEquals(0,calls.get());
        assertEquals(Arrays.asList("-4","-9"),values.collect()); assertEquals(2,calls.get());
        assertEquals(Arrays.asList("-4","-9"),values.collect()); assertEquals(4,calls.get());
        assertEquals("integer",values.getCurrentAlgebraName());
    }
    @Test public void absentUnaryOperationDoesNotModifyThePlanAndEmptyFlowsStayEmpty() {
        Algebra<Integer> algebra=integers(); AlgebraFlow<Integer> values=flow(algebra,Arrays.asList(1,2));
        assertThrows(exceptions.UnsupportedOperationException.class,() -> values.performOneOperandOperation("missing"));
        assertEquals(Arrays.asList("1","2"),values.collect());
        assertTrue(flow(algebra,Collections.emptyList()).performOneOperandOperation("negate").collect().isEmpty());
    }
    @Test public void arbitraryInterfaceImplementationsMustStillPreserveClosure() {
        Algebra<Integer> naturals=new Algebra<>("natural",Integer.class,"Natural numbers");
        naturals.addValidationRule(new TypedMembershipRule<>(Integer.class,n -> n>=0,"Nonnegative"));
        naturals.addOneOperandOperation("broken",new IOneOperandOperation<Integer>() {
            public Integer performOperation(Integer input) { return -1; }
            public String getDescription() { return "Intentionally violates closure"; }
            public String getAlgebraName() { return "natural"; }
            public Class<?> getResultBaseClass() { return Integer.class; }
            public Class<?> getSecondElementClass() { return Void.class; }
        });
        assertThrows(NotMemberException.class,() -> naturals.buildAlgebraItem(2).performOneOperandOperation("broken"));
        assertThrows(NotMemberException.class,() -> flow(naturals,Collections.singletonList(2)).performOneOperandOperation("broken").collect());
    }
}

