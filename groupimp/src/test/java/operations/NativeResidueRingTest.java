package operations;

import algebra.IAlgebraItem;
import algebra.concrete.*;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.Unit;
import mathematics.numbers.ModularInteger;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeResidueRingTest {
    @Test public void compositeModuliDistinguishUnitsZeroDivisorsAndPartialInverses() {
        ConcreteMathematics m=new ConcreteMathematics(); ResidueRing ring=m.residues;
        assertEquals(ring.member(0),ring.algebra().buildAlgebraItem(ring.member(2)).performOperation("multiply",ring.member(3)).perform().getResult());
        for(int value=0;value<6;value++) {
            boolean unit=value==1 || value==5;
            assertEquals(unit,ring.algebra().buildAlgebraItem(ring.member(value)).<Boolean>performAlgebraTransfer("is-unit").getResult());
            assertEquals(value!=0 && !unit,ring.algebra().buildAlgebraItem(ring.member(value)).<Boolean>performAlgebraTransfer("is-zero-divisor").getResult());
            final ModularInteger element=ring.member(value);
            if(unit) assertEquals(ring.member(1),ring.algebra().buildAlgebraItem(element).performOneOperandOperation("inverse").performOperation("multiply",element).perform().getResult());
            else assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                    () -> ring.algebra().buildAlgebraItem(element).performOneOperandOperation("inverse")).kind());
        }
        assertThrows(MathFailure.class,() -> ring.algebra().buildAlgebraItem(ring.member(4)).performOperation("divide",ring.member(2)).perform());
        assertNull(ring.algebra().buildAlgebraItem(new ModularInteger(1,5)));
        assertThrows(MathFailure.class,() -> new ResidueRing(BigInteger.ONE,m.integers,m.booleans));
    }
    @Test public void nativeCongruenceSolutionsAgreeWithExhaustiveSearchForEverySmallEquation() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int modulus=2;modulus<=20;modulus++) {
            ResidueRing ring=new ResidueRing(BigInteger.valueOf(modulus),m.integers,m.booleans);
            ring.register(new MathTool("equations-mod-"+modulus));
            for(int a=0;a<modulus;a++) for(int b=0;b<modulus;b++) {
                List<ModularInteger> expected=new ArrayList<>(),actual=new ArrayList<>();
                for(int x=0;x<modulus;x++) if((a*x)%modulus==b) expected.add(ring.member(x));
                for(IAlgebraItem<ModularInteger> item : ring.algebra().buildAlgebraItem(ring.member(a)).performFlatOperation("solve-multiply",ring.member(b))) {
                    assertSame(ring.algebra(),item.getAlgebra()); actual.add(item.getResult());
                }
                assertEquals(a+"*x="+b+" modulo "+modulus,expected,actual);
            }
        }
    }
    @Test public void multipleEmptyAndUniversalSolutionSetsComposeWithTransfers() {
        ConcreteMathematics m=new ConcreteMathematics(); ResidueRing ring=m.residues;
        assertEquals(Arrays.asList("-2","-5"),m.flow(ring,Collections.singletonList(ring.member(2)))
                .performFlatOperation("solve-multiply",ring.member(4)).<BigInteger>performAlgebraTransfer("lift").performOneOperandOperation("negate").collect());
        assertTrue(m.flow(ring,Collections.singletonList(ring.member(2))).performFlatOperation("solve-multiply",ring.member(3)).collect().isEmpty());
        assertEquals(6,ring.algebra().buildAlgebraItem(ring.member(0)).performFlatOperation("solve-multiply",ring.member(0)).size());
        assertEquals(6,m.unit.buildAlgebraItem(Unit.INSTANCE).performAlgebraFlatTransfer("Z/6Z.elements").size());
        assertEquals(Collections.singletonList("5 (mod 6)"),m.flow(m.integers,Collections.singletonList(BigInteger.valueOf(-7)))
                .<ModularInteger>performAlgebraTransfer("Z/6Z.reduce").collect());
    }
    @Test public void powersAndLargeModuliPreserveExactnessAndResourceLimits() {
        ConcreteMathematics m=new ConcreteMathematics(); ResidueRing ring=m.residues;
        assertEquals(ring.member(5),ring.algebra().buildAlgebraItem(ring.member(5)).performCustomMemberOperation("power",BigInteger.valueOf(-3)).getResult());
        assertEquals(ring.member(1),ring.algebra().buildAlgebraItem(ring.member(0)).performCustomMemberOperation("power",BigInteger.ZERO).getResult());
        assertThrows(MathFailure.class,() -> ring.algebra().buildAlgebraItem(ring.member(2)).performCustomMemberOperation("power",BigInteger.valueOf(-1)));
        BigInteger modulus=BigInteger.ONE.shiftLeft(100);
        ResidueRing large=new ResidueRing(modulus,m.integers,m.booleans); large.register(m.mathTool);
        assertEquals(large.member(modulus.subtract(BigInteger.ONE)),large.member(-1));
        assertEquals(large.member(1),large.member(-1).power(BigInteger.ONE.shiftLeft(200)));
        assertEquals(Collections.singletonList(large.member(2)),large.member(3).solveMultiply(large.member(6)));
        assertTrue(large.member(0).solveMultiply(large.member(1)).isEmpty());
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,
                () -> large.member(0).solveMultiply(large.member(0))).kind());
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,
                () -> m.unit.buildAlgebraItem(Unit.INSTANCE).performAlgebraFlatTransfer(large.algebra().getAlgebraName()+".elements")).kind());
    }
    @Test public void congruenceFlowSurvivesSerializationAndRepeatedCollection() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics();
        IAlgebraFlow<BigInteger> flow=m.flow(m.residues,Collections.singletonList(m.residues.member(2)))
                .performFlatOperation("solve-multiply",m.residues.member(4)).<BigInteger>performAlgebraTransfer("lift");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("2","5"),restored.collect()); assertEquals(Arrays.asList("2","5"),restored.collect());
    }
}
