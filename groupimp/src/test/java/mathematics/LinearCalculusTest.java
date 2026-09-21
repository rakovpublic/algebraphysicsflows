package mathematics;
import mathematics.core.*;
import mathematics.numbers.*;
import mathematics.linear.*;
import mathematics.calculus.*;
import mathematics.catalog.StandardMathematics;
import mathematics.foundations.FiniteSet;
import mathematics.structures.LawChecks;
import org.junit.Test;
import java.util.Random;
import static org.junit.Assert.*;
public class LinearCalculusTest {
    private RationalMatrix matrix(long a,long b,long c,long d) { return new RationalMatrix(new Rational[][]{{Rational.of(a),Rational.of(b)},{Rational.of(c),Rational.of(d)}}); }
    @Test public void exactMatricesAndNoncommutativityCounterexample() {
        RationalMatrix a=matrix(1,2,3,4);
        assertEquals(Rational.of(-2),a.determinant()); assertEquals(2,a.rank()); assertEquals(Rational.of(5),a.trace());
        assertEquals(RationalMatrix.identity(2),a.multiply(a.inverse()));
        assertEquals(a,a.transpose().transpose());
        RationalMatrix b=matrix(0,1,0,0);
        assertEquals(MathStatus.Epistemic.COUNTEREXAMPLE_FOUND,LawChecks.commutativity(FiniteSet.of(a,b),RationalMatrix::multiply).status);
        assertEquals(MathStatus.Epistemic.EMPIRICALLY_TESTED,LawChecks.associativity(FiniteSet.of(a,b),RationalMatrix::multiply).status);
        assertEquals(MathStatus.Epistemic.EMPIRICALLY_TESTED,LawChecks.identity(FiniteSet.of(a,b),RationalMatrix.identity(2),RationalMatrix::multiply).status);
        assertThrows(MathFailure.class,() -> matrix(1,2,2,4).inverse());
        assertEquals(1,matrix(1,2,2,4).rank());
        assertThrows(MathFailure.class,() -> new RationalMatrix(new Rational[][]{{Rational.ONE},{Rational.ONE,Rational.ONE}}));
    }
    @Test public void mixedDomainsPreserveOutputCarrierAndCanTransformFirstOperand() {
        StandardMathematics math=new StandardMathematics();
        RationalVector vector=new RationalVector(Rational.of(2),Rational.of(3));
        assertEquals(Rational.of(13),math.dot.apply(vector,vector));
        assertEquals(vector.scale(Rational.of(2)),math.scale.apply(Rational.of(2),vector));
        assertEquals(vector.scale(Rational.of(2)),math.scaleRight.apply(vector,Rational.of(2)));
        assertNotEquals(vector,math.scaleRight.apply(vector,Rational.of(2)));
        assertEquals(new RationalVector(Rational.of(8),Rational.of(18)),math.matrixVector.apply(matrix(1,2,3,4),vector));
        assertEquals(MathFailure.Kind.INVALID_MEMBER,math.dot.evaluate(vector,new RationalVector(Rational.ONE)).failure().get().kind());
    }
    @Test public void polynomialDifferentiationIntegrationAndConstants() {
        Polynomial xSquared=new Polynomial(Rational.ZERO,Rational.ZERO,Rational.ONE);
        assertEquals(Rational.of(4),xSquared.evaluate(Rational.of(2)));
        assertEquals(Rational.of(4),xSquared.derivative().evaluate(Rational.of(2)));
        assertEquals(Rational.of(1,3),xSquared.integrate(Rational.ZERO,Rational.ONE));
        assertEquals(Rational.of(-1,3),xSquared.integrate(Rational.ONE,Rational.ZERO));
        assertEquals(xSquared,new PrimitiveFamily(xSquared).at(Rational.of(9)).derivative());
        assertNotEquals(new PrimitiveFamily(xSquared).at(Rational.ZERO),new PrimitiveFamily(xSquared).at(Rational.ONE));
        assertEquals(Rational.of(7),xSquared.anchoredPrimitive(Rational.of(2),Rational.of(7)).evaluate(Rational.of(2)));
        assertEquals(-1,xSquared.derivative(3).degree());
        Random random=new Random(91);
        for(int i=0;i<100;i++) {
            Polynomial p=new Polynomial(Rational.of(random.nextInt(9)-4),Rational.of(random.nextInt(9)-4),Rational.of(random.nextInt(9)-4));
            assertEquals(p,p.primitive(Rational.of(3)).derivative());
            assertEquals(p.derivative().multiply(xSquared).add(p.multiply(xSquared.derivative())),p.multiply(xSquared).derivative());
        }
    }
}
