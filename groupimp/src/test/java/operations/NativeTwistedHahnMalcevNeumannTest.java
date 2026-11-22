package operations;

import algebra.concrete.ConcreteMathematics;
import mathematics.calculus.Polynomial;
import mathematics.calculus.RationalFunction;
import mathematics.core.MathFailure;
import mathematics.numbers.*;
import org.junit.Test;
import java.math.BigInteger;
import static org.junit.Assert.*;

/** Exact rational subring of a twisted HMN completion and its scalar/quaternion/Dixon links. */
public final class NativeTwistedHahnMalcevNeumannTest {
    private static Rational r(long n) { return Rational.of(n); }
    private static void undefined(Runnable action) {
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind());
    }
    private static RationalTwistedHahnSeries group(long exponent) {
        return RationalTwistedHahnSeries.groupElement(BigInteger.valueOf(exponent));
    }

    @Test public void twistedGroupLawDivisionValuationAndLeadingCoefficient() {
        RationalTwistedHahnSeries x=RationalTwistedHahnSeries.GROUP_GENERATOR;
        RationalQuaternion q=new RationalQuaternion(r(2),r(3),r(5),r(7));
        RationalTwistedHahnSeries coefficient=RationalTwistedHahnSeries.fromQuaternion(q);
        assertEquals(RationalTwistedHahnSeries.fromQuaternion(new RationalQuaternion(q.real,q.i,q.j.negate(),q.k.negate()))
                .multiply(x),x.multiply(coefficient));
        assertEquals(group(8),group(3).multiply(group(5)));
        assertEquals(RationalTwistedHahnSeries.ONE_VALUE,group(0));
        assertEquals(RationalTwistedHahnSeries.ONE_VALUE,group(7).multiply(group(-7)));
        assertEquals(BigInteger.valueOf(-3),group(-3).groupValuation());
        assertEquals(RationalQuaternion.ONE,group(-3).leadingCoefficient());
        assertEquals(RationalTwistedHahnSeries.ONE_VALUE,x.multiply(x.inverse()));
        RationalFunction t=RationalFunction.of(new Polynomial(Rational.ZERO,Rational.ONE));
        RationalTwistedHahnSeries value=new RationalTwistedHahnSeries(t,RationalFunction.ZERO,RationalFunction.ONE,RationalFunction.ZERO);
        assertEquals(BigInteger.ZERO,value.groupValuation());
        assertEquals(RationalQuaternion.J,value.leadingCoefficient());
        assertEquals(value,value.multiply(value.inverse()).multiply(value));
    }

    @Test public void registeredTransitionsIncludeDixonQuaternionSliceAndRejectNonassociativeRemainder() {
        ConcreteMathematics math=new ConcreteMathematics();
        RationalQuaternion h=new RationalQuaternion(r(1),r(2),r(3),r(4));
        RationalTwistedHahnSeries constant=math.quaternions.algebra().buildAlgebraItem(h)
                .<RationalTwistedHahnSeries>performAlgebraTransfer("HMN(H(Q);Z,sigma,1).embed-quaternion").perform().getResult();
        assertEquals(h,math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(constant)
                .<RationalQuaternion>performAlgebraTransfer("to-quaternion").perform().getResult());
        RationalDixonZ2 dixonQuaternion=RationalDixonZ2.fromQuaternion(h);
        RationalTwistedHahnSeries fromDixon=math.dixonZ2.algebra().buildAlgebraItem(dixonQuaternion)
                .<RationalTwistedHahnSeries>performAlgebraTransfer("HMN(H(Q);Z,sigma,1).embed-dixon-z2").perform().getResult();
        assertEquals(dixonQuaternion,math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(fromDixon)
                .<RationalDixonZ2>performAlgebraTransfer("to-dixon-z2").perform().getResult());
        assertEquals(Boolean.TRUE,math.booleans.algebra().buildAlgebraItem(Boolean.TRUE)
                .<RationalTwistedHahnSeries>performAlgebraTransfer("to-hmn").perform().getResult()
                .toBoolean());
        RationalComplex complex=new RationalComplex(r(3),r(2));
        assertEquals(complex,math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(
                RationalTwistedHahnSeries.fromComplex(complex)).<RationalComplex>performAlgebraTransfer("to-complex").perform().getResult());
        RationalImaginary imaginary=new RationalImaginary(r(5));
        assertEquals(imaginary,math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(
                RationalTwistedHahnSeries.fromImaginary(imaginary)).<RationalImaginary>performAlgebraTransfer("to-imaginary").perform().getResult());
        RationalFunction rationalFunction=RationalFunction.of(new Polynomial(Rational.ONE,Rational.ONE)).inverse();
        assertEquals(rationalFunction,math.rationalFunctions.algebra().buildAlgebraItem(rationalFunction)
                .<RationalTwistedHahnSeries>performAlgebraTransfer("HMN(H(Q);Z,sigma,1).embed-rational-function").perform().getResult()
                .toRationalFunction());
        RationalTwistedHahnSeries sigma=math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(constant)
                .performOneOperandOperation("sigma").perform().getResult();
        assertEquals(new RationalQuaternion(h.real,h.i,h.j.negate(),h.k.negate()),sigma.toQuaternion());
        assertEquals(RationalTwistedHahnSeries.GROUP_GENERATOR,math.integers.algebra().buildAlgebraItem(BigInteger.ONE)
                .<RationalTwistedHahnSeries>performAlgebraTransfer("HMN(H(Q);Z,sigma,1).group-basis").perform().getResult());
        assertEquals(Rational.of(2),math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(
                RationalTwistedHahnSeries.fromRational(r(2))).<Rational>performAlgebraTransfer("to-rational").perform().getResult());
        assertEquals(RationalQuaternion.ONE,math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(
                RationalTwistedHahnSeries.GROUP_GENERATOR).<RationalQuaternion>performAlgebraTransfer("leading-coefficient").perform().getResult());
        assertEquals(BigInteger.ONE,math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(
                RationalTwistedHahnSeries.GROUP_GENERATOR).<BigInteger>performAlgebraTransfer("group-valuation").perform().getResult());
        assertEquals(RationalOctonion.fromQuaternion(h),math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(constant)
                .<RationalOctonion>performAlgebraTransfer("to-octonion").perform().getResult());
        undefined(() -> math.dixonZ2.algebra().buildAlgebraItem(RationalDixonZ2.U)
                .performAlgebraTransfer("HMN(H(Q);Z,sigma,1).embed-dixon-z2"));
        undefined(() -> math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(RationalTwistedHahnSeries.GROUP_GENERATOR)
                .performAlgebraTransfer("to-quaternion"));
        undefined(() -> math.twistedHahnMalcevNeumann.algebra().buildAlgebraItem(RationalTwistedHahnSeries.GROUP_GENERATOR)
                .performAlgebraTransfer("to-dixon-z2"));
        undefined(() -> math.octonions.algebra().buildAlgebraItem(RationalOctonion.L)
                .performAlgebraTransfer("HMN(H(Q);Z,sigma,1).embed-octonion"));
    }
}
