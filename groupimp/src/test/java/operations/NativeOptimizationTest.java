package operations;

import algebra.concrete.ConcreteMathematics;
import mathematics.calculus.Polynomial;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeOptimizationTest {
    private static FiniteSet<BigInteger> set(long... values) {
        List<BigInteger> members=new ArrayList<>(); for(long value : values) members.add(BigInteger.valueOf(value));
        return new FiniteSet<>(members);
    }
    @Test public void tiesArePreservedAsSetsAndFlatWrappedMembers() {
        ConcreteMathematics m=new ConcreteMathematics();
        Polynomial squareMinusOne=new Polynomial(Rational.of(-1),Rational.ZERO,Rational.ONE);
        FiniteSet<BigInteger> feasible=set(-2,-1,1,2);
        assertEquals(set(-1,1),m.integerSets.algebra().buildAlgebraItem(feasible).performCustomMemberOperation("argmin",squareMinusOne).getResult());
        assertEquals(set(-2,2),m.integerSets.algebra().buildAlgebraItem(feasible).performCustomMemberOperation("argmax",squareMinusOne).getResult());
        assertEquals(Arrays.asList("-1","1"),m.flow(m.integerSets,Collections.singletonList(feasible))
                .<BigInteger,Polynomial>performFlatAlgebraUnsafe("minimizers",squareMinusOne).collect());
        assertEquals(Arrays.asList("-2","2"),m.flow(m.integerSets,Collections.singletonList(feasible))
                .<BigInteger,Polynomial>performFlatAlgebraUnsafe("maximizers",squareMinusOne).collect());
        assertEquals(Collections.singletonList("2"),m.flow(m.integerSets,Collections.singletonList(feasible))
                .performCustomMemberOperation("argmin",squareMinusOne).<BigInteger>performAlgebraTransfer("cardinality").collect());
    }
    @Test public void objectiveValuesAreExactAndFlowIntoRationalOperations() {
        ConcreteMathematics m=new ConcreteMathematics();
        Polynomial quadratic=new Polynomial(Rational.of(1,3),Rational.ZERO,Rational.of(1,2));
        assertEquals(Rational.of(5,6),m.integerSets.algebra().buildAlgebraItem(set(-2,-1,1,2))
                .<Rational,Polynomial>performUnsafeOperation("minimum",quadratic).getResult());
        assertEquals(Collections.singletonList("7/6"),m.flow(m.integerSets,Collections.singletonList(set(-2,-1,1,2)))
                .<Rational,Polynomial>performAlgebraUnsafe("maximum",quadratic).performOperation("divide",Rational.of(2)).collect());
        Polynomial constant=new Polynomial(Rational.of(7,5));
        assertEquals(set(4,1,9),m.integerSets.algebra().buildAlgebraItem(set(4,1,9)).performCustomMemberOperation("argmin",constant).getResult());
        assertEquals(Arrays.asList("4","1","9"),m.flow(m.integerSets,Collections.singletonList(set(4,1,9)))
                .<BigInteger,Polynomial>performFlatAlgebraUnsafe("minimizers",constant).collect());
    }
    @Test public void emptyFeasibleSetsAreUndefinedInEveryOptimizationFamily() {
        ConcreteMathematics m=new ConcreteMathematics(); Polynomial objective=new Polynomial(Rational.ONE);
        for(String name : Arrays.asList("argmin","argmax"))
            assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                    () -> m.integerSets.algebra().buildAlgebraItem(set()).performCustomMemberOperation(name,objective)).kind());
        for(String name : Arrays.asList("minimum","maximum"))
            assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                    () -> m.integerSets.algebra().buildAlgebraItem(set()).performUnsafeOperation(name,objective)).kind());
        for(String name : Arrays.asList("minimizers","maximizers"))
            assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                    () -> m.flow(m.integerSets,Collections.singletonList(set())).performFlatAlgebraUnsafe(name,objective).collect()).kind());
    }
    @Test public void finiteFeasibleScopeDoesNotClaimUnboundedOptimization() {
        ConcreteMathematics m=new ConcreteMathematics();
        Polynomial linear=new Polynomial(Rational.ZERO,Rational.ONE);
        BigInteger huge=BigInteger.ONE.shiftLeft(150);
        FiniteSet<BigInteger> feasible=FiniteSet.of(huge,huge.negate());
        assertEquals(Rational.of(huge.negate()),m.integerSets.algebra().buildAlgebraItem(feasible)
                .<Rational,Polynomial>performUnsafeOperation("minimum",linear).getResult());
        assertEquals(FiniteSet.of(huge),m.integerSets.algebra().buildAlgebraItem(feasible).performCustomMemberOperation("argmax",linear).getResult());
    }
}
