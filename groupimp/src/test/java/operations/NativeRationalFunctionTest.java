package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.calculus.*;
import mathematics.core.MathFailure;
import mathematics.foundations.Pair;
import mathematics.numbers.Rational;
import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeRationalFunctionTest {
    private static Polynomial polynomial(long... coefficients) {
        Rational[] values=new Rational[coefficients.length];
        for(int i=0;i<values.length;i++) values[i]=Rational.of(coefficients[i]);
        return new Polynomial(values);
    }
    @Test public void polynomialDivisionGcdAndCompositionUseExistingRegistries() {
        ConcreteMathematics m=new ConcreteMathematics();
        Polynomial dividend=polynomial(-1,0,0,1),divisor=polynomial(-1,1);
        assertEquals(polynomial(1,1,1),m.polynomials.algebra().buildAlgebraItem(dividend).performOperation("divide-exact",divisor).perform().getResult());
        assertEquals(Arrays.asList("Q[x][1, 1, 1]","Q[x][0]"),m.flow(m.polynomials,Collections.singletonList(dividend))
                .performFlatOperation("quotient-remainder",divisor).collect());
        assertEquals(polynomial(-1,1),m.polynomials.algebra().buildAlgebraItem(dividend)
                .performOperation("gcd",polynomial(-1,0,1)).perform().getResult());
        assertEquals(polynomial(1,2,1),m.polynomials.algebra().buildAlgebraItem(polynomial(0,0,1))
                .performOperation("compose",polynomial(1,1)).perform().getResult());
        assertEquals(polynomial(1,2),polynomial(-2,-4).monic().scale(Rational.of(2)));
        assertEquals(Polynomial.ZERO,Polynomial.ZERO.gcd(Polynomial.ZERO));
        assertEquals(divisor,divisor.scale(Rational.of(-3)).gcd(Polynomial.ZERO));
    }
    @Test public void euclideanDivisionReconstructsIndependentConstructedExamples() {
        Random rng=new Random(2718);
        for(int i=0;i<120;i++) {
            Polynomial divisor=polynomial(rng.nextInt(9)-4,rng.nextInt(9)-4,rng.nextInt(4)+1);
            Polynomial quotient=new Polynomial(Rational.of(rng.nextInt(9)-4,3),Rational.of(rng.nextInt(9)-4,2));
            Polynomial remainder=polynomial(rng.nextInt(9)-4,rng.nextInt(9)-4);
            Polynomial dividend=divisor.multiply(quotient).add(remainder);
            Pair<Polynomial,Polynomial> actual=dividend.divideAndRemainder(divisor);
            assertEquals(quotient,actual.first); assertEquals(remainder,actual.second);
            assertTrue(actual.second.degree()<divisor.degree());
        }
        assertEquals(Polynomial.ZERO,Polynomial.ZERO.quotient(polynomial(2)));
        assertEquals(polynomial(1),polynomial(1).remainder(polynomial(0,1)));
        assertEquals(new Polynomial(Rational.of(1,2)),polynomial(1).divideExact(polynomial(2)));
    }
    @Test public void canonicalFractionsCancelFactorsAndNormalizeTheDenominator() {
        RationalFunction reduced=new RationalFunction(polynomial(-2,0,2),polynomial(-2,2));
        assertEquals(RationalFunction.of(polynomial(1,1)),reduced);
        assertEquals(Polynomial.ONE,reduced.denominator());
        assertEquals(reduced.hashCode(),RationalFunction.of(polynomial(1,1)).hashCode());
        assertEquals(Rational.of(2),reduced.evaluate(Rational.ONE)); // The cancelled point is part of the canonical fraction's domain.
        RationalFunction scaled=new RationalFunction(polynomial(2,2),polynomial(0,-4));
        assertEquals(new Polynomial(Rational.of(-1,2),Rational.of(-1,2)),scaled.numerator());
        assertEquals(polynomial(0,1),scaled.denominator());
        assertEquals(RationalFunction.ZERO,new RationalFunction(Polynomial.ZERO,polynomial(2,1)));
        assertEquals(Polynomial.ONE,new RationalFunction(Polynomial.ZERO,polynomial(2,1)).denominator());
    }
    @Test public void quotientDerivativeCompositionAndEvaluationRemainExact() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalFunction reciprocal=new RationalFunction(Polynomial.ONE,polynomial(0,1));
        RationalFunction shifted=RationalFunction.of(polynomial(1,1));
        assertEquals(new RationalFunction(polynomial(-1),polynomial(0,0,1)),m.rationalFunctions.algebra().buildAlgebraItem(reciprocal)
                .performOneOperandOperation("derivative").getResult());
        assertEquals(new RationalFunction(Polynomial.ONE,polynomial(1,1)),m.rationalFunctions.algebra().buildAlgebraItem(reciprocal)
                .performOperation("compose",shifted).perform().getResult());
        IAlgebraItem<Rational> evaluated=m.rationalFunctions.algebra().buildAlgebraItem(reciprocal).performLeftProjectionOperation("evaluate",Rational.of(3));
        assertSame(m.rationals.algebra(),evaluated.getAlgebra()); assertEquals(Rational.of(1,3),evaluated.getResult());
        assertEquals(Collections.singletonList("-1/4"),m.flow(m.polynomials,Collections.singletonList(polynomial(0,1)))
                .<RationalFunction>performAlgebraTransfer("Q(x).embed-polynomial")
                .performOneOperandOperation("inverse").performOneOperandOperation("derivative")
                .performLeftProjectionOperation("evaluate",Rational.of(2)).collect());
    }
    @Test public void zeroDivisorsPolesAndNonexactPolynomialDivisionAreUndefined() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalFunction reciprocal=new RationalFunction(Polynomial.ONE,polynomial(0,1));
        List<Runnable> undefined=Arrays.asList(
                () -> new RationalFunction(Polynomial.ONE,Polynomial.ZERO),
                () -> Polynomial.ONE.quotient(Polynomial.ZERO),
                () -> Polynomial.ONE.divideExact(polynomial(0,1)),
                () -> Polynomial.ZERO.monic(),
                () -> reciprocal.evaluate(Rational.ZERO),
                () -> reciprocal.divide(RationalFunction.ZERO),
                () -> RationalFunction.ZERO.inverse(),
                () -> reciprocal.compose(RationalFunction.ZERO),
                () -> m.flow(m.rationalFunctions,Collections.singletonList(reciprocal)).performLeftProjectionOperation("evaluate",Rational.ZERO).collect());
        for(Runnable operation : undefined) assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,
                assertThrows(MathFailure.class,operation::run).kind());
        assertEquals(RationalFunction.ZERO,RationalFunction.ZERO.compose(reciprocal));
    }
    @Test public void fieldIdentitiesAndFormalChainRuleHoldForDeterministicSamples() {
        Random rng=new Random(31415);
        for(int i=0;i<24;i++) {
            RationalFunction a=new RationalFunction(polynomial(rng.nextInt(7)-3,1),polynomial(rng.nextInt(7)-3,1));
            RationalFunction b=new RationalFunction(polynomial(rng.nextInt(7)-3,2),polynomial(rng.nextInt(7)-3,1));
            RationalFunction c=new RationalFunction(polynomial(rng.nextInt(7)-3,3),polynomial(rng.nextInt(7)-3,1));
            assertEquals(a.multiply(b.add(c)),a.multiply(b).add(a.multiply(c)));
            assertEquals(RationalFunction.ONE,a.multiply(a.inverse()));
            assertEquals(a,a.add(b).subtract(b));
            // Polynomial inner function avoids a constant pole while checking formal substitution and derivatives.
            RationalFunction inner=RationalFunction.of(polynomial(1,0,1));
            assertEquals(a.compose(inner).derivative(),a.derivative().compose(inner).multiply(inner.derivative()));
        }
    }
    @Test public void rationalFunctionFlowSurvivesSerialization() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics();
        IAlgebraFlow<Rational> flow=m.flow(m.rationalFunctions,Collections.singletonList(new RationalFunction(Polynomial.ONE,polynomial(1,1))))
                .performOneOperandOperation("derivative").performLeftProjectionOperation("evaluate",Rational.ONE);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("-1/4"),restored.collect());
        assertEquals(Collections.singletonList("-1/4"),restored.collect());
    }
}
