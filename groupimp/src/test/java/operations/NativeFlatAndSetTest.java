package operations;

import algebra.concrete.ConcreteMathematics;
import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebraflow.IAlgebraFlow;
import exceptions.NotMemberException;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import operations.flat.*;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class NativeFlatAndSetTest {
    @Test public void actualSetAlgebraSatisfiesFiniteBooleanIdentities() {
        ConcreteMathematics math=new ConcreteMathematics();
        Algebra<FiniteSet<BigInteger>> algebra=math.integerSets.algebra();
        FiniteSet<BigInteger> universe=FiniteSet.of(BigInteger.ZERO,BigInteger.ONE,BigInteger.valueOf(2));
        for(FiniteSet<BigInteger> a : universe.powerSet().members()) for(FiniteSet<BigInteger> b : universe.powerSet().members()) {
            FiniteSet<BigInteger> union=algebra.buildAlgebraItem(a).performOperation("union",b).perform().getResult();
            FiniteSet<BigInteger> notUnion=algebra.buildAlgebraItem(union).performOperation("complement-in",universe).perform().getResult();
            FiniteSet<BigInteger> notA=algebra.buildAlgebraItem(a).performOperation("complement-in",universe).perform().getResult();
            FiniteSet<BigInteger> notB=algebra.buildAlgebraItem(b).performOperation("complement-in",universe).perform().getResult();
            assertEquals(notUnion,algebra.buildAlgebraItem(notA).performOperation("intersection",notB).perform().getResult());
            assertEquals(a,algebra.buildAlgebraItem(a).performOperation("intersection",union).perform().getResult());
        }
        assertEquals(FiniteSet.of(BigInteger.ONE,BigInteger.TEN),algebra.buildAlgebraItem(FiniteSet.of(BigInteger.ONE))
                .performCustomMemberOperation("insert",BigInteger.TEN).getResult());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                () -> algebra.buildAlgebraItem(FiniteSet.of(BigInteger.TEN)).performOperation("complement-in",universe).perform()).kind());
    }
    @Test public void unarySubsetExpansionComposesWithCrossDomainFlatTransfer() {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraFlow<BigInteger> result=math.flow(math.integerSets,Collections.singletonList(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2))))
                .performOneOperandFlatOperation("subsets")
                .<BigInteger>performFlatAlgebraTransfer("elements")
                .performOneOperandOperation("negate");
        assertEquals(Arrays.asList("-1","-2","-1","-2"),result.collect());
        assertEquals("Z",result.getCurrentAlgebraName());
        assertTrue(math.flow(math.integerSets,Collections.singletonList(FiniteSet.<BigInteger>of()))
                .<BigInteger>performFlatAlgebraTransfer("elements").performOperation("negate").collect().isEmpty());
    }
    @Test public void flatUnaryWrapperChecksClosureAndKeepsDuplicates() {
        ConcreteMathematics math=new ConcreteMathematics();
        Algebra<BigInteger> integers=math.integers.algebra();
        integers.addOneOperandFlatOperation("twice",new OneOperandFlatOperation<>("Repeat",integers,n -> Arrays.asList(n,n)));
        assertEquals(Arrays.asList("3","3"),math.flow(math.integers,Collections.singletonList(BigInteger.valueOf(3))).performFlatOperation("twice").collect());
        integers.addOneOperandFlatOperation("wrong-algebra",new IOneOperandFlatOperation<BigInteger>() {
            public List<IAlgebraItem<BigInteger>> performOperation(BigInteger n) { return Collections.singletonList(math.naturals.algebra().buildAlgebraItem(n)); }
            public String getDescription() { return "Incorrect wrapper"; }
            public String getAlgebraName() { return "Z"; }
            public Class<?> getResultBaseClass() { return BigInteger.class; }
            public Class<?> getSecondElementClass() { return Void.class; }
        });
        assertThrows(NotMemberException.class,() -> integers.buildAlgebraItem(BigInteger.ONE).performOneOperandFlatOperation("wrong-algebra"));
    }
    @Test public void nativeCrossDomainFlowSurvivesJavaSerialization() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraFlow<BigInteger> original=math.flow(math.integerSets,Collections.singletonList(FiniteSet.of(BigInteger.ONE,BigInteger.TEN)))
                .<BigInteger>performFlatAlgebraTransfer("elements").performOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream input=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)input.readObject(); }
        assertEquals(Arrays.asList("-1","-10"),restored.collect());
        assertEquals(Arrays.asList("-1","-10"),restored.collect());
    }
    @Test public void absentFlatTransferDoesNotDamageTheFlowPlan() {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraFlow<BigInteger> original=math.flow(math.integers,Collections.singletonList(BigInteger.ONE));
        assertThrows(exceptions.UnsupportedOperationException.class,() -> original.performFlatAlgebraTransfer("missing"));
        assertThrows(exceptions.UnsupportedOperationException.class,() -> original.performOneOperandFlatOperation("missing"));
        assertEquals(Collections.singletonList("1"),original.collect());
    }
}

