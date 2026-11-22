package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import mathematics.core.MathFailure;
import mathematics.numbers.*;
import mathematics.linear.RationalVector;
import mathematics.linear.RationalMatrix;
import org.junit.Test;
import static org.junit.Assert.*;

/** Crossed-product grading, exact factor embeddings, and checked reverse maps. */
public final class NativeDixonZ2AlgebraTest {
    private static Rational r(long n) { return Rational.of(n); }
    private static void undefined(Runnable action) {
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind());
    }

    @Test public void crossedProductAndGradingRelationsHold() {
        RationalDixonZ2 u=RationalDixonZ2.U;
        RationalDixonZ2 x=RationalDixonZ2.fromOctonion(new RationalOctonion(r(1),r(2),r(3),r(4),r(5),r(6),r(7),r(8)));
        assertEquals(RationalDixonZ2.ONE,u.multiply(u));
        assertEquals(x.gradingInvolution().multiply(u),u.multiply(x));
        assertEquals(x,x.gradingInvolution().gradingInvolution());
        RationalDixonZ2 y=RationalDixonZ2.fromQuaternion(new RationalQuaternion(r(2),r(1),r(-1),r(3)));
        assertEquals(x.gradingInvolution().multiply(y.gradingInvolution()),x.multiply(y).gradingInvolution());
        assertEquals(RationalDixonZ2.ZERO,x.evenPart().oddPart());
        assertEquals(x,x.evenPart().add(x.oddPart()));
        assertNotEquals(RationalDixonZ2.ZERO,
                RationalDixonZ2.fromOctonion(RationalOctonion.I).multiply(RationalDixonZ2.fromOctonion(RationalOctonion.J)).multiply(RationalDixonZ2.fromOctonion(RationalOctonion.L))
                        .subtract(RationalDixonZ2.fromOctonion(RationalOctonion.I).multiply(RationalDixonZ2.fromOctonion(RationalOctonion.J).multiply(RationalDixonZ2.fromOctonion(RationalOctonion.L)))));
    }

    @Test public void registeredMapsConnectAllDeclaredScalarFactorsAndBoolean() {
        ConcreteMathematics math=new ConcreteMathematics();
        RationalDixonZ2 value=RationalDixonZ2.fromOctonion(new RationalOctonion(r(1),r(2),r(3),r(4),r(5),r(6),r(7),r(8)));
        assertEquals(value,math.octonions.algebra().buildAlgebraItem(value.toOctonion())
                .<RationalDixonZ2>performAlgebraTransfer("DixonZ2(Q).embed-octonion").perform().getResult());
        assertEquals(value.toOctonion(),math.dixonZ2.algebra().buildAlgebraItem(value)
                .<RationalOctonion>performAlgebraTransfer("to-octonion").perform().getResult());
        RationalQuaternion h=new RationalQuaternion(r(1),r(2),r(3),r(4));
        assertEquals(h,math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.fromQuaternion(h))
                .<RationalQuaternion>performAlgebraTransfer("to-quaternion").perform().getResult());
        RationalComplex z=new RationalComplex(r(3),r(-2));
        assertEquals(z,math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.fromComplex(z))
                .<RationalComplex>performAlgebraTransfer("to-complex").perform().getResult());
        assertEquals(RationalImaginary.I,math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.fromImaginary(RationalImaginary.I))
                .<RationalImaginary>performAlgebraTransfer("to-imaginary").perform().getResult());
        assertEquals(Rational.ONE,math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.fromRational(Rational.ONE))
                .<Rational>performAlgebraTransfer("to-rational").perform().getResult());
        assertEquals(Boolean.TRUE,math.booleans.algebra().buildAlgebraItem(Boolean.TRUE)
                .<RationalDixonZ2>performAlgebraTransfer("to-dixon-z2").perform().getResult()
                .toBoolean());
        assertEquals(Boolean.TRUE,math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.ONE)
                .<Boolean>performAlgebraTransfer("to-boolean").perform().getResult());
        RationalVector vector=value.toVector();
        assertEquals(128,vector.dimension());
        assertEquals(value,math.finiteVectors.algebra().buildAlgebraItem(vector)
                .<RationalDixonZ2>performAlgebraTransfer("DixonZ2(Q).from-vector").perform().getResult());
        RationalMatrix representation=value.leftMultiplicationMatrix();
        assertEquals(128,representation.rows());
        assertEquals(value,math.rectangularMatrices.algebra().buildAlgebraItem(representation)
                .<RationalDixonZ2>performAlgebraTransfer("DixonZ2(Q).from-left-multiplication-matrix").perform().getResult());
        assertEquals(value,math.dixonZ2.algebra().buildAlgebraItem(value).performOneOperandOperation("grading-involution")
                .perform().getResult().gradingInvolution());
        assertEquals(64,math.dixonZ2.algebra().buildAlgebraItem(value).<RationalComplex>performAlgebraFlatTransfer("components").size());
        assertEquals(Boolean.TRUE,math.dixonZ2.algebra().buildAlgebraItem(value).<Boolean>performCustomResultOperation("equal",value).perform().getResult());
        undefined(() -> math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.U).performAlgebraTransfer("to-octonion"));
        undefined(() -> math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.fromComplex(new RationalComplex(r(2),r(1))))
                .performAlgebraTransfer("to-boolean"));
        undefined(() -> math.finiteVectors.algebra().buildAlgebraItem(new RationalVector(Rational.ONE))
                .performAlgebraTransfer("DixonZ2(Q).from-vector"));
    }
}
