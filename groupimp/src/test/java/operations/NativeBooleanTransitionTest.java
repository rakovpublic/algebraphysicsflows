package operations;

import algebra.IAlgebraItem;
import algebraflow.IAlgebraFlow;
import algebra.concrete.*;
import mathematics.calculus.Polynomial;
import mathematics.calculus.RationalFunction;
import mathematics.core.MathFailure;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalTensor;
import mathematics.numbers.*;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

/** Boolean truth tables and exact 0/1 transitions through the registered MathTool operations. */
public final class NativeBooleanTransitionTest {
    private static final List<Boolean> BITS=Arrays.asList(false,true);
    private static void undefined(Runnable action) {
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind());
    }

    @Test public void allBooleanDerivedOperationsAgreeWithTheirTruthTables() {
        ConcreteMathematics math=new ConcreteMathematics();
        for(boolean a : BITS) for(boolean b : BITS) {
            IAlgebraItem<Boolean> item=math.booleans.algebra().buildAlgebraItem(a);
            assertEquals(!(a && b),item.performOperation("nand",b).perform().getResult());
            assertEquals(!(a || b),item.performOperation("nor",b).perform().getResult());
            assertEquals(a==b,item.performOperation("equivalent",b).perform().getResult());
            assertEquals(a && !b,item.performOperation("difference",b).perform().getResult());
            assertEquals(!a || b,item.performOperation("less-or-equal",b).perform().getResult());
            assertEquals(!b || a,item.performOperation("greater-or-equal",b).perform().getResult());
        }
    }

    @Test public void eachScalarTransitionRoundTripsOnlyTheExactZeroAndOneValues() {
        ConcreteMathematics math=new ConcreteMathematics();
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<BigInteger>performAlgebraTransfer("to-natural").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<BigInteger>performAlgebraTransfer("to-integer").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<Rational>performAlgebraTransfer("to-rational").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<RationalComplex>performAlgebraTransfer("to-complex").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<RationalQuaternion>performAlgebraTransfer("to-quaternion").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<Polynomial>performAlgebraTransfer("to-polynomial").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<RationalFunction>performAlgebraTransfer("to-rational-function").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<RationalTensor>performAlgebraTransfer("to-tensor").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<RationalMatrix>performAlgebraTransfer("to-matrix").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<ModularInteger>performAlgebraTransfer("to-residue-6").<Boolean>performAlgebraTransfer("to-boolean").collect());
        for(PrimeField field : math.primeFields)
            assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,BITS).<ModularInteger>performAlgebraTransfer("to-prime-"+field.prime).<Boolean>performAlgebraTransfer("to-boolean").collect());

        undefined(() -> math.naturals.algebra().buildAlgebraItem(BigInteger.valueOf(2)).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.integers.algebra().buildAlgebraItem(BigInteger.ONE.negate()).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.rationals.algebra().buildAlgebraItem(Rational.of(1,2)).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.complexRationals.algebra().buildAlgebraItem(new RationalComplex(Rational.ONE,Rational.ONE)).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.quaternions.algebra().buildAlgebraItem(RationalQuaternion.I).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.polynomials.algebra().buildAlgebraItem(new Polynomial(Rational.ZERO,Rational.ONE)).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.rationalFunctions.algebra().buildAlgebraItem(RationalFunction.of(new Polynomial(Rational.ZERO,Rational.ONE))).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.tensors.algebra().buildAlgebraItem(new RationalTensor(new int[]{1},Rational.ONE)).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.matrices.algebra().buildAlgebraItem(new RationalMatrix(new Rational[][]{{Rational.ONE,Rational.ONE},{Rational.ZERO,Rational.ONE}})).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.residues.algebra().buildAlgebraItem(math.residues.member(2)).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.primeFields.get(0).algebra().buildAlgebraItem(math.primeFields.get(0).member(2)).performAlgebraTransfer("to-boolean"));
    }

    @Test public void scalarBooleanFlowsUseActualWrappersAndSurviveSerialization() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraFlow<Boolean> original=math.flow(math.booleans,BITS)
                .<Rational>performAlgebraTransfer("to-rational")
                .<Boolean>performAlgebraTransfer("to-boolean")
                .performOneOperandOperation("not");
        assertEquals(Arrays.asList("true","false"),original.collect());
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream output=new ObjectOutputStream(bytes)) { output.writeObject(original); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream input=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)input.readObject(); }
        assertEquals(original.collect(),restored.collect());
        assertSame(math.rationals.algebra(),math.booleans.algebra().buildAlgebraItem(true).<Rational>performAlgebraTransfer("to-rational").getAlgebra());
        assertSame(math.booleans.algebra(),math.rationals.algebra().buildAlgebraItem(Rational.ZERO).<Boolean>performAlgebraTransfer("to-boolean").getAlgebra());
    }
}
