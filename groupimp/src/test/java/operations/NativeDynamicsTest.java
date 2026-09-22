package operations;

import algebra.concrete.ConcreteMathematics;
import mathematics.calculus.Polynomial;
import mathematics.core.MathFailure;
import mathematics.foundations.Pair;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeDynamicsTest {
    @Test public void exactPolynomialOrbitsIncludeInitialStateAndComposeWithRationalOperations() {
        ConcreteMathematics m=new ConcreteMathematics();
        Polynomial step=new Polynomial(Rational.ONE,Rational.of(2));
        Pair<Rational,BigInteger> request=new Pair<>(Rational.ONE,BigInteger.valueOf(3));
        assertEquals(Rational.of(15),m.polynomials.algebra().buildAlgebraItem(step)
                .<Rational,Pair<Rational,BigInteger>>performUnsafeOperation("iterate",request).getResult());
        assertEquals(Arrays.asList("1","3","7","15"),m.flow(m.polynomials,Collections.singletonList(step))
                .<Rational,Pair<Rational,BigInteger>>performFlatAlgebraUnsafe("orbit",request).collect());
        assertEquals(Arrays.asList("-1","-3","-7","-15"),m.flow(m.polynomials,Collections.singletonList(step))
                .<Rational,Pair<Rational,BigInteger>>performFlatAlgebraUnsafe("orbit",request).performOneOperandOperation("negate").collect());
        Polynomial half=new Polynomial(Rational.ZERO,Rational.of(1,2));
        assertEquals(Rational.of(1,8),m.polynomials.algebra().buildAlgebraItem(half)
                .<Rational,Pair<Rational,BigInteger>>performUnsafeOperation("iterate",request).getResult());
    }
    @Test public void zeroStepsAndPeriodicOrbitsKeepExactValuesAndDuplicates() {
        ConcreteMathematics m=new ConcreteMathematics();
        Polynomial negate=new Polynomial(Rational.ZERO,Rational.of(-1));
        assertEquals(Collections.singletonList("2"),m.flow(m.polynomials,Collections.singletonList(negate))
                .<Rational,Pair<Rational,BigInteger>>performFlatAlgebraUnsafe("orbit",new Pair<>(Rational.of(2),BigInteger.ZERO)).collect());
        assertEquals(Rational.of(2),m.polynomials.algebra().buildAlgebraItem(negate)
                .<Rational,Pair<Rational,BigInteger>>performUnsafeOperation("iterate",new Pair<>(Rational.of(2),BigInteger.ZERO)).getResult());
        assertEquals(Arrays.asList("2","-2","2","-2"),m.flow(m.polynomials,Collections.singletonList(negate))
                .<Rational,Pair<Rational,BigInteger>>performFlatAlgebraUnsafe("orbit",new Pair<>(Rational.of(2),BigInteger.valueOf(3))).collect());
    }
    @Test public void invalidCountsAndImplementationLimitsAreDifferentFailures() {
        ConcreteMathematics m=new ConcreteMathematics(); Polynomial identity=new Polynomial(Rational.ZERO,Rational.ONE);
        assertThrows(exceptions.NotMemberException.class,() -> m.polynomials.algebra().buildAlgebraItem(identity)
                .performUnsafeOperation("iterate",new Pair<>(Rational.ONE,BigInteger.valueOf(-1))));
        assertThrows(exceptions.NotMemberException.class,() -> m.polynomials.algebra().buildAlgebraItem(identity)
                .performUnsafeOperation("iterate",new Pair<>(Rational.ONE,Rational.ONE)));
        for(String name : Arrays.asList("iterate","orbit")) {
            MathFailure failure=assertThrows(MathFailure.class,() -> {
                if(name.equals("iterate")) m.polynomials.algebra().buildAlgebraItem(identity)
                        .performUnsafeOperation(name,new Pair<>(Rational.ONE,BigInteger.ONE.shiftLeft(80)));
                else m.polynomials.algebra().buildAlgebraItem(identity)
                        .performUnsafeFlatOperation(name,new Pair<>(Rational.ONE,BigInteger.valueOf(10001)));
            });
            assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,failure.kind());
        }
    }
}
