package mathematics;
import mathematics.core.*;
import mathematics.numbers.*;
import mathematics.foundations.*;
import mathematics.probability.FiniteDistribution;
import mathematics.statistics.ExactStatistics;
import mathematics.topology.FiniteSimplicialComplex;
import mathematics.applied.*;
import mathematics.catalog.StandardMathematics;
import org.junit.Test;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;
public class ProbabilityAppliedTest {
    @Test public void exactMassesConditioningAndPushforward() {
        Map<BigInteger,Rational> masses=new LinkedHashMap<>();
        masses.put(BigInteger.ZERO,Rational.of(1,4)); masses.put(BigInteger.ONE,Rational.of(3,4));
        FiniteDistribution<BigInteger> distribution=new FiniteDistribution<>(NumberDomains.INTEGERS,masses);
        assertEquals(Rational.of(3,4),distribution.expectation(Rational::of));
        assertEquals(Rational.ONE,distribution.condition(n -> n.signum()>0).masses().get(BigInteger.ONE));
        assertThrows(MathFailure.class,() -> distribution.condition(n -> n.signum()<0));
        assertEquals(Collections.singletonMap(false,Rational.ONE),distribution.pushForward(NumberDomains.BOOLEAN,n -> false).masses());
        masses.put(BigInteger.ZERO,Rational.of(-1,4));
        assertThrows(MathFailure.class,() -> new FiniteDistribution<>(NumberDomains.INTEGERS,masses));
        assertThrows(MathFailure.class,() -> new FiniteDistribution<>(NumberDomains.INTEGERS,Collections.singletonMap(BigInteger.ONE,Rational.of(1,3))));
    }
    @Test public void statisticsConventionsAndUndefinedCases() {
        List<Rational> sample=Arrays.asList(Rational.ONE,Rational.of(2),Rational.of(3));
        assertEquals(Rational.of(2),ExactStatistics.mean(sample));
        assertEquals(Rational.of(2,3),ExactStatistics.variance(sample,false));
        assertEquals(Rational.ONE,ExactStatistics.variance(sample,true));
        assertEquals(Rational.ONE,ExactStatistics.covariance(sample,sample,true));
        assertThrows(MathFailure.class,() -> ExactStatistics.mean(Collections.emptyList()));
        assertThrows(MathFailure.class,() -> ExactStatistics.variance(Collections.singletonList(Rational.ONE),true));
    }
    @Test public void finiteHomologyDistinguishesCircleFromFilledTriangle() {
        FiniteSimplicialComplex circle=new FiniteSimplicialComplex(Arrays.asList(FiniteSet.of(0,1),FiniteSet.of(1,2),FiniteSet.of(0,2)));
        FiniteSimplicialComplex disk=new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(0,1,2)));
        assertEquals(1,circle.bettiNumber(0)); assertEquals(1,circle.bettiNumber(1));
        assertEquals(1,disk.bettiNumber(0)); assertEquals(0,disk.bettiNumber(1)); assertEquals(0,disk.bettiNumber(2));
        assertEquals(Arrays.asList(BigInteger.ONE,BigInteger.ONE),new StandardMathematics().bettiNumbers.apply(circle,Unit.INSTANCE));
        assertEquals(0,new FiniteSimplicialComplex(Collections.emptyList()).bettiNumber(0));
    }
    @Test public void finiteOptimizationKeepsTiesAndDynamicsKeepsDomain() {
        assertEquals(FiniteSet.of(-1,1),FiniteOptimization.minimize(FiniteSet.of(-2,-1,1,2),n -> Rational.of(n*n)));
        assertThrows(MathFailure.class,() -> FiniteOptimization.minimize(FiniteSet.<Integer>of(),Rational::of));
        UnaryOperation<BigInteger,BigInteger> step=new UnaryOperation<>(Metadata.of("successor","Discrete successor"),NumberDomains.NATURALS,NumberDomains.NATURALS,MathStatus.Computation.EXACT,false,n -> n.add(BigInteger.ONE));
        assertEquals(Arrays.asList(BigInteger.ZERO,BigInteger.ONE,BigInteger.valueOf(2)),DiscreteDynamics.trajectory(step,BigInteger.ZERO,2));
    }
}
