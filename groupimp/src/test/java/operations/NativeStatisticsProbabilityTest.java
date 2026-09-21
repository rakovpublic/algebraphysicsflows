package operations;

import algebra.concrete.*;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.numbers.Rational;
import mathematics.probability.FiniteDistribution;
import mathematics.statistics.RationalSample;
import org.junit.Test;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class NativeStatisticsProbabilityTest {
    private FiniteDistribution<BigInteger> distribution(ConcreteMathematics math) {
        Map<BigInteger,Rational> masses=new LinkedHashMap<>();
        masses.put(BigInteger.ONE,Rational.of(1,4)); masses.put(BigInteger.valueOf(3),Rational.of(3,4));
        return new FiniteDistribution<>(math.integers.algebra(),masses);
    }
    @Test public void everyConcreteRegistrationImplementsAnOriginalOperationFamily() {
        ConcreteMathematics math=new ConcreteMathematics();
        assertSame(math.mathTool,math.initialize());
        for(OperationRegistration registration : math.operations().values()) {
            assertTrue(registration.id,registration.operation.getClass().getPackage().getName().startsWith("operations."));
            assertSame(registration.first,math.mathTool.getAlgebra(registration.first.getAlgebraName()));
            assertSame(registration.result,math.mathTool.getAlgebra(registration.result.getAlgebraName()));
        }
    }
    @Test public void sampleStatisticsComposeThroughMathToolWithExplicitConventions() {
        ConcreteMathematics math=new ConcreteMathematics();
        RationalSample observations=RationalSample.of(Rational.ONE,Rational.of(2),Rational.of(3));
        assertEquals(Collections.singletonList("2"),math.flow(math.samples,Collections.singletonList(observations)).<Rational>performAlgebraTransfer("mean").collect());
        assertEquals(Collections.singletonList("0"),math.flow(math.samples,Collections.singletonList(observations)).performOneOperandOperation("center")
                .<Rational>performAlgebraTransfer("mean").collect());
        assertEquals(Collections.singletonList("4"),math.flow(math.samples,Collections.singletonList(observations)).performCustomMemberOperation("scale",Rational.of(2))
                .<Rational>performAlgebraTransfer("sample-variance").collect());
        assertEquals(Arrays.asList("1","2","3"),math.flow(math.samples,Collections.singletonList(observations)).<Rational>performFlatAlgebraTransfer("elements").collect());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,() -> math.samples.algebra().buildAlgebraItem(RationalSample.of())
                .performAlgebraTransfer("mean")).kind());
        assertThrows(MathFailure.class,() -> math.samples.algebra().buildAlgebraItem(observations)
                .performCustomResultOperation("sample-covariance",RationalSample.of(Rational.ONE)));
    }
    @Test public void finiteProbabilityUsesExactMassesAndActualOutcomeAlgebra() {
        ConcreteMathematics math=new ConcreteMathematics();
        FiniteDistribution<BigInteger> distribution=distribution(math);
        assertSame(math.integers.algebra(),distribution.outcomes());
        assertEquals(Collections.singletonList("5/2"),math.flow(math.integerProbabilities,Collections.singletonList(distribution))
                .<Rational>performAlgebraTransfer("expectation").collect());
        assertEquals(Collections.singletonList("3/4"),math.flow(math.integerProbabilities,Collections.singletonList(distribution))
                .<Rational,FiniteSet<BigInteger>>performAlgebraUnsafe("event-probability",FiniteSet.of(BigInteger.valueOf(3))).collect());
        assertEquals(Collections.singletonList("3"),math.flow(math.integerProbabilities,Collections.singletonList(distribution))
                .performCustomMemberOperation("condition",FiniteSet.of(BigInteger.valueOf(3)))
                .<Rational>performAlgebraTransfer("expectation").collect());
        FiniteDistribution<BigInteger> foreign=new FiniteDistribution<>(math.naturals.algebra(),Collections.singletonMap(BigInteger.ONE,Rational.ONE));
        assertNull(math.integerProbabilities.algebra().buildAlgebraItem(foreign));
        assertThrows(MathFailure.class,() -> new FiniteDistribution<>(math.integers.algebra(),Collections.singletonMap(BigInteger.ONE,Rational.of(1,2))));
        assertThrows(MathFailure.class,() -> math.integerProbabilities.algebra().buildAlgebraItem(distribution)
                .performCustomMemberOperation("condition",FiniteSet.of(BigInteger.TEN)));
    }
    @Test public void probabilitySupportCanFlowThroughSetAndNumberOperations() {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraFlow<BigInteger> result=math.flow(math.integerProbabilities,Collections.singletonList(distribution(math)))
                .<FiniteSet<BigInteger>>performAlgebraTransfer("support")
                .<BigInteger>performFlatAlgebraTransfer("elements")
                .performOneOperandOperation("negate");
        assertEquals(Arrays.asList("-1","-3"),result.collect());
        assertEquals(Collections.singletonList("6"),math.flow(math.integers,Collections.singletonList(BigInteger.valueOf(6)))
                .<FiniteDistribution<BigInteger>>performAlgebraTransfer("FiniteDistribution(Z).point-mass")
                .<Rational>performAlgebraTransfer("expectation").collect());
    }
}

