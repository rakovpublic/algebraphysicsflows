package operations;

import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.imp.AlgebraFlow;
import algebraflow.imp.ListAlgebraInput;
import operations.simple.*;
import operations.flat.*;
import org.junit.Test;
import java.io.Serializable;
import java.util.*;
import static org.junit.Assert.*;

public class OperandOverloadTest {
    private final Algebra<Integer> integers=new Algebra<>("integers",Integer.class,"Integers");
    private final Algebra<String> text=new Algebra<>("text",String.class,"Text");
    private final Algebra<Boolean> booleans=new Algebra<>("bool",Boolean.class,"Truth values");
    private AlgebraFlow<Integer> flow(MathTool tool) {
        return new AlgebraFlow<>(new ListAlgebraInput<>(integers,Collections.singletonList(5)),() -> tool,"integers");
    }
    private MathTool tool() {
        MathTool tool=new MathTool("overloads"); tool.addAlgebra(integers); tool.addAlgebra(text); tool.addAlgebra(booleans); return tool;
    }
    @Test public void itemAndFlowSelectCustomMemberOverloadsBySecondType() {
        integers.addCustomMemberOperation("action",new CustomMemberOperation<>("Text length",integers,text,(a,b) -> a+b.length()));
        integers.addCustomMemberOperation("action",new CustomMemberOperation<>("Integer addition",integers,integers,(a,b) -> a+b));
        integers.addCustomMemberFlatOperation("actions",new CustomMemberFlatOperation<>("Text pair",integers,text,(a,b) -> Arrays.asList(a,a+b.length())));
        integers.addCustomMemberFlatOperation("actions",new CustomMemberFlatOperation<>("Number pair",integers,integers,(a,b) -> Arrays.asList(a,a+b)));
        assertEquals(Integer.valueOf(8),integers.buildAlgebraItem(5).performCustomMemberOperation("action",3).getResult());
        assertEquals(Integer.valueOf(7),integers.buildAlgebraItem(5).performCustomMemberOperation("action","ab").getResult());
        assertEquals(Arrays.asList("8","10"),flow(tool()).performCustomMemberOperation("action",3).performFlatCustomMemberOperation("actions","ab").collect());
        assertEquals(Arrays.asList("5","8"),flow(tool()).performFlatCustomMemberOperation("actions",3).collect());
    }
    @Test public void unsafeOverloadsCanSelectDifferentResultAlgebras() {
        integers.addUnsafeOperation("convert",new MixedOperation<>("Greater",integers,integers,booleans,(a,b) -> a>b));
        integers.addUnsafeOperation("convert",new MixedOperation<>("Text",integers,text,text,(a,b) -> b+a));
        integers.addUnsafeOperationFlat("convert-flat",new MixedFlatOperation<>("Boolean copies",integers,integers,booleans,(a,b) -> Arrays.asList(a>b,a>b)));
        integers.addUnsafeOperationFlat("convert-flat",new MixedFlatOperation<>("Text copies",integers,text,text,(a,b) -> Arrays.asList(b+a,b+a)));
        assertEquals("x5",integers.buildAlgebraItem(5).<String,String>performUnsafeOperation("convert","x").getResult());
        assertEquals(Boolean.TRUE,integers.buildAlgebraItem(5).<Boolean,Integer>performUnsafeOperation("convert",3).getResult());
        assertEquals(Arrays.asList("x5","x5"),flow(tool()).<String,String>performFlatAlgebraUnsafe("convert-flat","x").collect());
        assertEquals(Collections.singletonList("true"),flow(tool()).<Boolean,Integer>performAlgebraUnsafe("convert",3).collect());
    }
    @Test public void rejectedOverloadAndMissingTargetLeaveThePlanUnchanged() {
        integers.addCustomMemberOperation("action",new CustomMemberOperation<>("Text length",integers,text,(a,b) -> a+b.length()));
        AlgebraFlow<Integer> source=flow(tool());
        assertThrows(exceptions.UnsupportedOperationException.class,() -> source.performCustomMemberOperation("action",3));
        assertThrows(exceptions.UnsupportedOperationException.class,() -> source.performCustomMemberOperation("action",null));
        assertEquals(Collections.singletonList("5"),source.collect());
        integers.addUnsafeOperation("convert",new MixedOperation<>("Compare",integers,integers,booleans,(a,b) -> a>b));
        MathTool incomplete=new MathTool("incomplete"); incomplete.addAlgebra(integers);
        AlgebraFlow<Integer> missing=flow(incomplete);
        assertThrows(exceptions.AlgebraNotExistsException.class,() -> missing.performAlgebraUnsafe("convert",3));
        assertEquals(Collections.singletonList("5"),missing.collect());
    }
    @Test public void exactTypesTakePriorityAndAmbiguousSupertypeMatchesAreRejected() {
        Algebra<Number> numbers=new Algebra<>("numbers",Number.class,"Numbers");
        integers.addCustomMemberOperation("action",new CustomMemberOperation<>("Number",integers,numbers,(a,b) -> a+b.intValue()));
        integers.addCustomMemberOperation("action",new CustomMemberOperation<>("Integer",integers,integers,(a,b) -> a+b+100));
        assertEquals(Integer.valueOf(108),integers.buildAlgebraItem(5).performCustomMemberOperation("action",3).getResult());
        assertEquals(Integer.valueOf(7),integers.buildAlgebraItem(5).performCustomMemberOperation("action",2.0).getResult());
        integers.addCustomMemberOperation("action",new CustomMemberOperation<>("Replacement",integers,integers,(a,b) -> a-b));
        assertEquals(Integer.valueOf(2),integers.buildAlgebraItem(5).performCustomMemberOperation("action",3).getResult());
        Algebra<Serializable> serializable=new Algebra<>("serializable",Serializable.class,"Serializable values");
        Algebra<Comparable> comparable=new Algebra<>("comparable",Comparable.class,"Comparable values");
        integers.addCustomMemberOperation("ambiguous",new CustomMemberOperation<>("First",integers,serializable,(a,b) -> a+1));
        integers.addCustomMemberOperation("ambiguous",new CustomMemberOperation<>("Second",integers,comparable,(a,b) -> a+2));
        assertThrows(exceptions.UnsupportedOperationException.class,() -> integers.buildAlgebraItem(5).performCustomMemberOperation("ambiguous","x"));
    }
}

