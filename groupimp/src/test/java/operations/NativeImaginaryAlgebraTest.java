package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import mathematics.core.MathFailure;
import mathematics.numbers.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/** Exact pure-imaginary arithmetic and its registered scalar relations. */
public final class NativeImaginaryAlgebraTest {
    private static Rational r(long value) { return Rational.of(value); }
    private static RationalImaginary i(long coefficient) { return new RationalImaginary(r(coefficient)); }
    private static void undefined(Runnable action) {
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind());
    }

    @Test public void pureImaginaryArithmeticReturnsTheMathematicallyCorrectCarrier() {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraItem<RationalImaginary> twoI=math.imaginaries.algebra().buildAlgebraItem(i(2));
        assertEquals(i(5),twoI.performOperation("add",i(3)).perform().getResult());
        assertEquals(i(-1),twoI.performOperation("subtract",i(3)).perform().getResult());
        assertEquals(i(-2),twoI.performOneOperandOperation("conjugate").perform().getResult());
        assertEquals(r(-6),twoI.<Rational>performCustomResultOperation("multiply",i(3)).perform().getResult());
        assertEquals(Rational.of(2,3),twoI.<Rational>performCustomResultOperation("divide",i(3)).perform().getResult());
        assertEquals(r(4),twoI.<Rational>performAlgebraTransfer("norm-squared").perform().getResult());
        assertEquals(r(-4),twoI.<Rational>performAlgebraTransfer("square").perform().getResult());
        assertEquals(i(6),twoI.performCustomMemberOperation("scale",r(3)).perform().getResult());
        assertEquals(i(6),math.rationals.algebra().buildAlgebraItem(r(3)).performLeftProjectionOperation("iQ.scale-left",i(2)).perform().getResult());
        assertFalse(twoI.<Boolean>performCustomResultOperation("equal",i(3)).perform().getResult());
        undefined(() -> twoI.<Rational>performCustomResultOperation("divide",RationalImaginary.ZERO).perform());
    }

    @Test public void allCanonicalScalarRelationsAndBooleanEncodingUseActualWrappers() {
        ConcreteMathematics math=new ConcreteMathematics();
        assertEquals(i(3),math.rationals.algebra().buildAlgebraItem(r(3))
                .<RationalImaginary>performAlgebraTransfer("iQ.from-rational").perform().getResult());
        assertEquals(new RationalComplex(Rational.ZERO,r(3)),math.imaginaries.algebra().buildAlgebraItem(i(3))
                .<RationalComplex>performAlgebraTransfer("to-complex").perform().getResult());
        assertEquals(i(3),math.complexRationals.algebra().buildAlgebraItem(new RationalComplex(Rational.ZERO,r(3)))
                .<RationalImaginary>performAlgebraTransfer("iQ.from-complex").perform().getResult());
        assertEquals(new RationalQuaternion(Rational.ZERO,r(3),Rational.ZERO,Rational.ZERO),math.imaginaries.algebra().buildAlgebraItem(i(3))
                .<RationalQuaternion>performAlgebraTransfer("to-quaternion").perform().getResult());
        assertEquals(i(3),math.quaternions.algebra().buildAlgebraItem(new RationalQuaternion(Rational.ZERO,r(3),Rational.ZERO,Rational.ZERO))
                .<RationalImaginary>performAlgebraTransfer("iQ.from-quaternion").perform().getResult());
        assertEquals(Arrays.asList("false","true"),math.flow(math.booleans,Arrays.asList(false,true))
                .<RationalImaginary>performAlgebraTransfer("to-imaginary")
                .<Boolean>performAlgebraTransfer("to-boolean").collect());
        assertSame(math.imaginaries.algebra(),math.booleans.algebra().buildAlgebraItem(true)
                .<RationalImaginary>performAlgebraTransfer("to-imaginary").getAlgebra());
        undefined(() -> math.imaginaries.algebra().buildAlgebraItem(i(2)).performAlgebraTransfer("to-boolean"));
        undefined(() -> math.complexRationals.algebra().buildAlgebraItem(new RationalComplex(Rational.ONE,Rational.ONE)).performAlgebraTransfer("iQ.from-complex"));
        undefined(() -> math.quaternions.algebra().buildAlgebraItem(RationalQuaternion.J).performAlgebraTransfer("iQ.from-quaternion"));
    }
}
