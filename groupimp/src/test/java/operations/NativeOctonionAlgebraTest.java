package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import mathematics.core.MathFailure;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import mathematics.numbers.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/** Exact Cayley-Dickson arithmetic, division, and registered octonion connections. */
public final class NativeOctonionAlgebraTest {
    private static Rational r(long value) { return Rational.of(value); }
    private static RationalOctonion scalar(long value) { return RationalOctonion.scalar(r(value)); }
    private static void undefined(Runnable action) {
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind());
    }

    @Test public void multiplicationIsAlternativeButNotAssociativeAndNormIsMultiplicative() {
        ConcreteMathematics math=new ConcreteMathematics();
        RationalOctonion i=RationalOctonion.I,j=RationalOctonion.J,k=RationalOctonion.K,l=RationalOctonion.L;
        assertEquals(k,i.multiply(j));
        assertEquals(k.negate(),j.multiply(i));
        assertEquals(RationalOctonion.KL,i.multiply(RationalOctonion.JL).negate());
        RationalOctonion left=i.multiply(j).multiply(l),right=i.multiply(j.multiply(l));
        assertNotEquals(left,right);
        assertEquals(RationalOctonion.KL.scale(r(2)),left.subtract(right));
        RationalOctonion x=new RationalOctonion(r(1),r(2),r(3),r(4),r(5),r(6),r(7),r(8));
        RationalOctonion y=new RationalOctonion(r(2),r(-1),r(3),r(1),r(0),r(2),r(-2),r(4));
        assertEquals(x.normSquared().multiply(y.normSquared()),x.multiply(y).normSquared());
        assertEquals(RationalOctonion.ONE,x.multiply(x.inverse()));
        assertEquals(RationalOctonion.ONE,x.inverse().multiply(x));
        assertEquals(x.multiply(x).multiply(y),x.multiply(x.multiply(y)));
        assertEquals(x.multiply(y.multiply(y)),x.multiply(y).multiply(y));
        IAlgebraItem<RationalOctonion> xi=math.octonions.algebra().buildAlgebraItem(x);
        assertEquals(RationalOctonion.ONE,xi.performOneOperandOperation("inverse").perform().getResult().multiply(x));
        assertEquals(x.multiply(y.inverse()),xi.performOperation("divide-right",y).perform().getResult());
        assertEquals(y.inverse().multiply(x),xi.performOperation("divide-left",y).perform().getResult());
        undefined(() -> math.octonions.algebra().buildAlgebraItem(RationalOctonion.ZERO).performOneOperandOperation("inverse").perform());
    }

    @Test public void canonicalScalarSubalgebraVectorBooleanAndMatrixConnectionsRoundTrip() {
        ConcreteMathematics math=new ConcreteMathematics();
        RationalOctonion value=new RationalOctonion(r(1),r(2),r(3),r(4),r(5),r(6),r(7),r(8));
        IAlgebraItem<RationalOctonion> item=math.octonions.algebra().buildAlgebraItem(value);
        assertEquals(RationalOctonion.scalar(r(3)),math.rationals.algebra().buildAlgebraItem(r(3))
                .<RationalOctonion>performAlgebraTransfer("O(Q).embed-rational").perform().getResult());
        assertEquals(RationalOctonion.fromComplex(new RationalComplex(r(1),r(2))),math.complexRationals.algebra()
                .buildAlgebraItem(new RationalComplex(r(1),r(2))).<RationalOctonion>performAlgebraTransfer("O(Q).embed-complex").perform().getResult());
        assertEquals(RationalOctonion.fromImaginary(new RationalImaginary(r(3))),math.imaginaries.algebra()
                .buildAlgebraItem(new RationalImaginary(r(3))).<RationalOctonion>performAlgebraTransfer("O(Q).embed-imaginary").perform().getResult());
        assertEquals(Rational.of(6),math.octonions.algebra().buildAlgebraItem(RationalOctonion.scalar(r(6)))
                .<Rational>performAlgebraTransfer("to-rational").perform().getResult());
        assertEquals(Boolean.TRUE,math.octonions.algebra().buildAlgebraItem(scalar(1))
                .<Boolean>performAlgebraTransfer("to-boolean").perform().getResult());
        assertEquals(new RationalComplex(r(1),r(2)),math.octonions.algebra().buildAlgebraItem(new RationalOctonion(r(1),r(2),Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO))
                .<RationalComplex>performAlgebraTransfer("to-complex").perform().getResult());
        assertEquals(new RationalQuaternion(r(1),r(2),r(3),r(4)),math.quaternions.algebra().buildAlgebraItem(new RationalQuaternion(r(1),r(2),r(3),r(4)))
                .<RationalOctonion>performAlgebraTransfer("O(Q).embed-quaternion").perform().getResult().toQuaternion());
        assertEquals(new RationalImaginary(r(2)),math.octonions.algebra().buildAlgebraItem(new RationalOctonion(Rational.ZERO,r(2),Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO))
                .<RationalImaginary>performAlgebraTransfer("to-imaginary").perform().getResult());
        RationalOctonion pureVectorOct=new RationalOctonion(Rational.ZERO,Rational.ZERO,r(2),r(3),r(4),r(5),r(6),r(7));
        assertEquals(pureVectorOct,math.finiteVectors.algebra().buildAlgebraItem(new RationalVector(Rational.ZERO,r(2),r(3),r(4),r(5),r(6),r(7)))
                .<RationalOctonion>performAlgebraTransfer("O(Q).from-vector").perform().getResult());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,Arrays.asList(false,true))
                .<RationalOctonion>performAlgebraTransfer("to-octonion").<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertEquals(math.octonions.algebra(),item.performAlgebraTransfer("left-multiplication-matrix")
                .<RationalOctonion>performAlgebraTransfer("O(Q).from-left-multiplication-matrix").perform().getAlgebra());
        RationalMatrix representation=item.<RationalMatrix>performAlgebraTransfer("left-multiplication-matrix").perform().getResult();
        assertEquals(value,math.rectangularMatrices.algebra().buildAlgebraItem(representation)
                .<RationalOctonion>performAlgebraTransfer("O(Q).from-left-multiplication-matrix").perform().getResult());
        assertEquals(new RationalVector(Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO),
                math.octonions.algebra().buildAlgebraItem(RationalOctonion.ZERO)
                        .<RationalVector>performAlgebraTransfer("to-vector").perform().getResult());
        undefined(() -> item.performAlgebraTransfer("to-complex"));
        undefined(() -> item.performAlgebraTransfer("to-imaginary"));
        undefined(() -> item.performAlgebraTransfer("to-quaternion"));
        undefined(() -> math.finiteVectors.algebra().buildAlgebraItem(new RationalVector(r(1),r(2))).performAlgebraTransfer("O(Q).from-vector"));
        undefined(() -> math.rectangularMatrices.algebra().buildAlgebraItem(new RationalMatrix(new Rational[][]{{Rational.ONE}})).performAlgebraTransfer("O(Q).from-left-multiplication-matrix"));
        undefined(() -> math.octonions.algebra().buildAlgebraItem(new RationalOctonion(r(2),Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO))
                .performAlgebraTransfer("to-boolean"));
    }
}
