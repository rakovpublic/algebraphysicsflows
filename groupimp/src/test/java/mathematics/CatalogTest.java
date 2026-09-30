package mathematics;

import mathematics.catalog.StandardMathematics;
import mathematics.calculus.Polynomial;
import mathematics.core.*;
import mathematics.foundations.*;
import mathematics.linear.*;
import mathematics.numbers.*;
import mathematics.topology.FiniteSimplicialComplex;
import org.junit.Test;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class CatalogTest {
    @Test public void everyPublishedOperationHasAnExecutableExample() {
        StandardMathematics math=new StandardMathematics();
        assertEquals(16,math.catalog.operations().size());
        assertEquals(BigInteger.valueOf(2),math.naturalToInteger.apply(BigInteger.valueOf(2)));
        assertEquals(Rational.of(-3),math.integerToRational.apply(BigInteger.valueOf(-3)));
        assertEquals(SymbolicReal.rational(Rational.of(2)),math.rationalToRealExpression.apply(Rational.of(2)));
        assertEquals(new RationalComplex(Rational.of(2),Rational.ZERO),math.rationalToComplex.apply(Rational.of(2)));
        assertEquals(Rational.of(5),math.add.apply(Rational.of(2),Rational.of(3)));
        assertEquals(Rational.of(2,3),math.divide.apply(Rational.of(2),Rational.of(3)));
        assertTrue(math.greater.apply(BigInteger.ONE,BigInteger.ZERO));
        RationalVector v=new RationalVector(Rational.ONE,Rational.of(2));
        RationalVector scaled=new RationalVector(Rational.of(3),Rational.of(6));
        assertEquals(Rational.of(5),math.dot.apply(v,v));
        assertEquals(scaled,math.scale.apply(Rational.of(3),v));
        assertEquals(scaled,math.scaleRight.apply(v,Rational.of(3)));
        RationalMatrix matrix=new RationalMatrix(new Rational[][] {
            {Rational.ONE,Rational.of(2)}, {Rational.of(3),Rational.of(4)}});
        assertEquals(Rational.of(-2),math.determinant.apply(matrix));
        assertEquals(new RationalVector(Rational.of(5),Rational.of(11)),math.matrixVector.apply(matrix,v));
        Polynomial square=new Polynomial(Rational.ZERO,Rational.ZERO,Rational.ONE);
        assertEquals(Rational.of(9),math.evaluatePolynomial.apply(square,Rational.of(3)));
        assertEquals(new Polynomial(Rational.ZERO,Rational.of(2)),math.derivative.apply(square));
        assertEquals(Rational.ZERO,math.zero.apply(Unit.INSTANCE));
        FiniteSimplicialComplex circle=new FiniteSimplicialComplex(Arrays.asList(
                FiniteSet.of(0,1),FiniteSet.of(1,2),FiniteSet.of(0,2)));
        assertEquals(Arrays.asList(BigInteger.ONE,BigInteger.ONE),math.bettiNumbers.apply(circle,Unit.INSTANCE));
    }

    @Test public void catalogRejectsInvalidOperandsAndReportsPartialResults() {
        StandardMathematics math=new StandardMathematics();
        assertEquals(MathFailure.Kind.INVALID_MEMBER,
                math.naturalToInteger.evaluate(BigInteger.valueOf(-1)).failure().get().kind());
        assertEquals(MathFailure.Kind.INVALID_MEMBER,
                math.dot.evaluate(new RationalVector(Rational.ONE),new RationalVector(Rational.ONE)).failure().get().kind());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,math.divide.evaluate(Rational.ONE,Rational.ZERO).failure().get().kind());
        assertEquals(MathStatus.Computation.SYMBOLIC,math.rationalToRealExpression.evaluate(Rational.ONE).computation());
    }
}
