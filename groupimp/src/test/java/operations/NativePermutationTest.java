package operations;

import algebra.IAlgebraItem;
import algebra.concrete.*;
import algebraflow.IAlgebraFlow;
import algebraflow.imp.*;
import mathematics.core.MathFailure;
import mathematics.foundations.Unit;
import mathematics.structures.Permutation;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativePermutationTest {
    @Test public void s3GroupLawsAreCheckedOnEveryElementAndTripleThroughNativeOperations() {
        ConcreteMathematics m=new ConcreteMathematics();
        Set<Permutation> elements=new HashSet<>(Arrays.asList(new Permutation(0,1,2),new Permutation(0,2,1),
                new Permutation(1,0,2),new Permutation(1,2,0),new Permutation(2,0,1),new Permutation(2,1,0)));
        assertEquals(elements,new HashSet<>(Permutation.all(3)));
        Permutation identity=Permutation.identity(3);
        for(Permutation a : elements) {
            assertEquals(a,m.permutations.algebra().buildAlgebraItem(a).performOperation("compose",identity).perform().getResult());
            Permutation inverse=m.permutations.algebra().buildAlgebraItem(a).performOneOperandOperation("inverse").getResult();
            assertEquals(identity,m.permutations.algebra().buildAlgebraItem(a).performOperation("compose",inverse).perform().getResult());
            for(Permutation b : elements) {
                Permutation ab=m.permutations.algebra().buildAlgebraItem(a).performOperation("compose",b).perform().getResult();
                assertTrue(elements.contains(ab));
                for(Permutation c : elements) {
                    Permutation bc=m.permutations.algebra().buildAlgebraItem(b).performOperation("compose",c).perform().getResult();
                    assertEquals(m.permutations.algebra().buildAlgebraItem(ab).performOperation("compose",c).perform().getResult(),
                            m.permutations.algebra().buildAlgebraItem(a).performOperation("compose",bc).perform().getResult());
                }
            }
        }
        Permutation cycle=new Permutation(1,2,0),swap=new Permutation(1,0,2);
        assertNotEquals(cycle.compose(swap),swap.compose(cycle));
        assertEquals(new Permutation(2,1,0),cycle.compose(swap));
    }
    @Test public void cyclePowersSignAndOrdersAgreeWithIndependentChecks() {
        for(Permutation permutation : Permutation.all(4)) {
            int inversions=0;
            for(int i=0;i<4;i++) for(int j=i+1;j<4;j++) if(permutation.image(i)>permutation.image(j)) inversions++;
            assertEquals(inversions%2==0?1:-1,permutation.sign());
            Permutation product=Permutation.identity(4); int order=0;
            do { product=product.compose(permutation); order++; } while(!product.equals(Permutation.identity(4)));
            assertEquals(BigInteger.valueOf(order),permutation.order());
            for(int power=-7;power<=7;power++) {
                Permutation expected=Permutation.identity(4),factor=power<0?permutation.inverse():permutation;
                for(int i=0;i<Math.abs(power);i++) expected=expected.compose(factor);
                assertEquals(expected,permutation.power(BigInteger.valueOf(power)));
            }
        }
        Permutation mixed=new Permutation(1,0,3,4,2);
        assertEquals(BigInteger.valueOf(6),mixed.order()); assertEquals(-1,mixed.sign());
        Permutation restored=Permutation.identity(5);
        for(Permutation cycle : mixed.disjointCycles()) restored=restored.compose(cycle);
        assertEquals(mixed,restored);
        assertEquals(mixed.power(BigInteger.valueOf(4)),mixed.power(BigInteger.ONE.shiftLeft(200))); // 2^200 mod 6 = 4.
    }
    @Test public void actionAndFlatOrbitReturnTheSecondTypeWrapperAndComposeInFlows() {
        ConcreteMathematics m=new ConcreteMathematics(); Permutation cycle=new Permutation(1,2,0);
        IAlgebraItem<BigInteger> result=m.permutations.algebra().buildAlgebraItem(cycle).performLeftProjectionOperation("apply",BigInteger.ZERO);
        assertSame(m.naturals.algebra(),result.getAlgebra()); assertEquals(BigInteger.ONE,result.getResult());
        assertEquals(Arrays.asList("1","2","3"),m.flow(m.permutations,Collections.singletonList(cycle))
                .performLeftProjectionFlatOperation("orbit",BigInteger.ZERO).performOneOperandOperation("successor").collect());
        assertEquals(cycle.inverse(),m.permutations.algebra().buildAlgebraItem(cycle).performCustomMemberOperation("power",BigInteger.valueOf(-1)).getResult());
        assertTrue(m.flow(m.permutations,Collections.singletonList(Permutation.identity(3))).performOneOperandFlatOperation("cycles").collect().isEmpty());
        assertEquals(Collections.singletonList("Perm[1, 2, 0]"),m.flow(m.permutations,Collections.singletonList(cycle))
                .performOneOperandFlatOperation("cycles").collect());
    }
    @Test public void invalidPermutationsActionsAndEnumerationLimitsRemainDistinct() {
        ConcreteMathematics m=new ConcreteMathematics();
        assertThrows(MathFailure.class,() -> new Permutation(0,0));
        assertThrows(MathFailure.class,() -> new Permutation(0,2));
        assertThrows(MathFailure.class,() -> new Permutation(-1));
        assertNull(m.permutations.algebra().buildAlgebraItem(Permutation.identity(4)));
        assertThrows(exceptions.NotMemberException.class,() -> m.permutations.algebra().buildAlgebraItem(Permutation.identity(3))
                .performLeftProjectionOperation("apply",BigInteger.valueOf(-1)));
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                () -> Permutation.identity(3).image(BigInteger.ONE.shiftLeft(100))).kind());
        SymmetricGroup large=new SymmetricGroup(9,m.naturals,m.integers,m.booleans); large.register(m.mathTool);
        assertNotNull(large.algebra().buildAlgebraItem(Permutation.identity(9)));
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,
                () -> m.unit.buildAlgebraItem(Unit.INSTANCE).performAlgebraFlatTransfer("S9.elements")).kind());
        assertEquals(Collections.singletonList(Permutation.identity(0)),Permutation.all(0));
        assertEquals(BigInteger.ONE,Permutation.identity(0).order());
        assertEquals(1,Permutation.identity(0).sign());
    }
    @Test public void groupEnumerationAndSerializedFlowsUseExistingRegistries() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics();
        IAlgebraFlow<Permutation> all=new AlgebraFlow<>(new ListAlgebraInput<>(m.unit,Collections.singletonList(Unit.INSTANCE)),m,"Unit")
                .<Permutation>performFlatAlgebraTransfer("S3.elements").performCustomMemberOperation("power",BigInteger.ZERO);
        assertEquals(Collections.nCopies(6,"Perm[0, 1, 2]"),all.collect());
        IAlgebraFlow<BigInteger> flow=m.flow(m.permutations,Collections.singletonList(new Permutation(1,2,0)))
                .performCustomMemberOperation("power",BigInteger.valueOf(-1)).performLeftProjectionOperation("apply",BigInteger.ZERO)
                .performOneOperandOperation("successor");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("3"),restored.collect()); assertEquals(Collections.singletonList("3"),restored.collect());
    }
}
