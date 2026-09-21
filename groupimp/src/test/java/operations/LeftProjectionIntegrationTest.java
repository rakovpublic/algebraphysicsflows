package operations;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import algebraflow.imp.AlgebraFlow;
import algebraflow.imp.ListAlgebraInput;
import exceptions.UnsupportedOperationException;
import operations.flat.*;
import operations.simple.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/** The historical LeftProjection name now denotes A x B -> B, as requested. */
public class LeftProjectionIntegrationTest {
    private final Algebra<Integer> integers=new Algebra<>("integer",Integer.class,"Integers");
    private final Algebra<String> text=new Algebra<>("text",String.class,"Text");
    private final MathTool tool=new MathTool("projection-test");
    public LeftProjectionIntegrationTest() {
        integers.addLeftProjectionOperation("second",new LeftProjectionOperation<Integer,String>(text,String.class));
        integers.addLeftProjectionFlatOperation("second-flat",new LeftProjectionFlatOperation<Integer,String>(text,String.class));
        text.addOneOperandOperation("upper",new OneOperandOperation<>("Uppercase",text,String::toUpperCase));
        text.addOperation("append",new ClosedOperation<>("Append",text,String::concat));
        tool.addAlgebra(integers); tool.addAlgebra(text);
    }
    private AlgebraFlow<Integer> flow(List<Integer> input) {
        return new AlgebraFlow<>(new ListAlgebraInput<>(integers,input),() -> tool,"integer");
    }
    @Test public void resultWrapperContainsTheSecondValueAndItsAlgebra() {
        String second=new String("result");
        IAlgebraItem<String> result=integers.buildAlgebraItem(3).performLeftProjectionOperation("second",second);
        assertSame(second,result.getResult()); assertSame(text,result.getAlgebra());
        List<IAlgebraItem<String>> flat=integers.buildAlgebraItem(3).performLeftProjectionFlatOperation("second-flat",second);
        assertEquals(1,flat.size()); assertSame(second,flat.get(0).getResult()); assertSame(text,flat.get(0).getAlgebra());
        assertEquals(String.class,integers.getLeftProjectionOperation("second",String.class).getResultBaseClass());
        assertFalse(integers.hasCustomMemberOperation("second"));
    }
    @Test public void flowSwitchesAlgebraAndRunsItsUnaryAndBinaryOperations() {
        IAlgebraFlow<String> result=flow(Arrays.asList(1,2)).performLeftProjectionOperation("second","ab")
                .performOneOperandOperation("upper").performOperation("append","!");
        assertEquals("text",result.getCurrentAlgebraName());
        assertEquals(Arrays.asList("AB!","AB!"),result.collect());
        assertEquals(Arrays.asList("AB!","AB!"),result.collect());
        assertEquals(Arrays.asList("X","X"),flow(Arrays.asList(1,2))
                .performLeftProjectionFlatOperation("second-flat","x").performOperation("upper").collect());
    }
    @Test public void flatOperationsCanTransformSecondTypeAndPreserveDuplicates() {
        integers.addLeftProjectionFlatOperation("repeat",new SecondResultFlatOperation<>("Repeat",integers,text,
                (count,value) -> Arrays.asList(value+count,value+count)));
        assertEquals(Arrays.asList("X2","X2"),flow(Collections.singletonList(2))
                .performLeftProjectionFlatOperation("repeat","x").performOneOperandOperation("upper").collect());
    }
    @Test public void customMemberOperationStillReturnsTheFirstType() {
        integers.addCustomMemberOperation("plus-length",new CustomMemberOperation<>("Length action",integers,text,(number,value) -> number+value.length()));
        integers.addCustomMemberFlatOperation("lengths",new CustomMemberFlatOperation<>("Two lengths",integers,text,(number,value) -> Arrays.asList(number,number+value.length())));
        IAlgebraItem<Integer> result=integers.buildAlgebraItem(5).performCustomMemberOperation("plus-length","abc");
        assertEquals(Integer.valueOf(8),result.getResult()); assertSame(integers,result.getAlgebra());
        assertEquals(Arrays.asList("5","8"),flow(Collections.singletonList(5)).performFlatCustomMemberOperation("lengths","abc").collect());
    }
    @Test public void emptyFlowsAndWrongSecondTypesAreHandledWithoutChangingThePlan() {
        assertTrue(flow(Collections.emptyList()).performLeftProjectionOperation("second","x").collect().isEmpty());
        AlgebraFlow<Integer> original=flow(Arrays.asList(5,10));
        assertThrows(UnsupportedOperationException.class,() -> original.performLeftProjectionOperation("second",false));
        assertThrows(UnsupportedOperationException.class,() -> original.performLeftProjectionFlatOperation("second-flat",42));
        assertThrows(UnsupportedOperationException.class,() -> original.performLeftProjectionOperation("missing","x"));
        assertEquals(Arrays.asList("5","10"),original.collect());
    }
    @Test public void absentResultAlgebraFailsBeforeSchedulingTheOperation() {
        MathTool incomplete=new MathTool("missing-result"); incomplete.addAlgebra(integers);
        AlgebraFlow<Integer> original=new AlgebraFlow<>(new ListAlgebraInput<>(integers,Collections.singletonList(3)),() -> incomplete,"integer");
        assertThrows(exceptions.AlgebraNotExistsException.class,() -> original.performLeftProjectionOperation("second","x"));
        assertEquals(Collections.singletonList("3"),original.collect());
    }
}
