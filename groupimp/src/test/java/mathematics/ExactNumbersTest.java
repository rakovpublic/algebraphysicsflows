package mathematics;
import mathematics.core.*;
import mathematics.numbers.*;
import org.junit.Test;
import java.math.BigInteger;
import java.util.Random;
import static org.junit.Assert.*;
public class ExactNumbersTest {
    @Test public void canonicalExactRationalsAndLargeValues() {
        assertEquals(Rational.of(1,2),Rational.of(-2,-4));
        assertEquals(Rational.of(1,2).hashCode(),Rational.of(2,4).hashCode());
        BigInteger huge=BigInteger.ONE.shiftLeft(300);
        assertEquals(Rational.of(huge.multiply(BigInteger.valueOf(3))),Rational.of(huge).multiply(Rational.of(3)));
        assertEquals(Rational.of(9,4),Rational.of(2,3).pow(-2));
        assertThrows(MathFailure.class,() -> Rational.ZERO.pow(-1));
        assertThrows(MathFailure.class,() -> Rational.of(1,0));
    }
    @Test public void seededFieldPropertiesAreTestsNotProofs() {
        Random random=new Random(721);
        for(int i=0;i<300;i++) {
            Rational a=Rational.of(random.nextInt(101)-50,random.nextInt(50)+1);
            Rational b=Rational.of(random.nextInt(101)-50,random.nextInt(50)+1);
            Rational c=Rational.of(random.nextInt(101)-50,random.nextInt(50)+1);
            assertEquals(a.add(b).add(c),a.add(b.add(c)));
            assertEquals(a.multiply(b.add(c)),a.multiply(b).add(a.multiply(c)));
            assertEquals(a,a.add(b).subtract(b));
            if(b.signum()!=0) assertEquals(a,a.multiply(b).divide(b));
        }
    }
    @Test public void modularDomainsAndPartialInverses() {
        ModularInteger x=new ModularInteger(-1,7);
        assertEquals(new ModularInteger(6,7),x);
        assertEquals(new ModularInteger(1,7),x.multiply(x.inverse()));
        assertThrows(MathFailure.class,() -> new ModularInteger(2,6).inverse());
        assertThrows(MathFailure.class,() -> x.add(new ModularInteger(6,5)));
        assertEquals(MathStatus.Membership.NOT_MEMBER,NumberDomains.residues(5).contains(x));
        assertThrows(MathFailure.class,() -> NumberDomains.primeField(9));
        assertEquals(MathStatus.Membership.MEMBER,NumberDomains.primeField(7).contains(x));
        assertEquals(BigInteger.valueOf(30),NumberTheory.lcm(BigInteger.valueOf(-6),BigInteger.valueOf(15)));
        assertEquals(BigInteger.valueOf(120),NumberTheory.factorial(5));
        assertFalse(NumberTheory.isPrime(1)); assertTrue(NumberTheory.isPrime(97));
    }
    @Test public void complexFieldAndSymbolicRealScope() {
        RationalComplex i=new RationalComplex(Rational.ZERO,Rational.ONE);
        assertEquals(new RationalComplex(Rational.of(-1),Rational.ZERO),i.multiply(i));
        RationalComplex z=new RationalComplex(Rational.of(2),Rational.of(3));
        assertEquals(z,z.multiply(i).divide(i));
        assertEquals(new RationalComplex(Rational.of(13),Rational.ZERO),z.multiply(z.conjugate()));
        assertThrows(MathFailure.class,() -> z.divide(new RationalComplex(Rational.ZERO,Rational.ZERO)));
        assertEquals(SymbolicReal.pi(),SymbolicReal.pi());
        assertNotEquals(SymbolicReal.rational(Rational.of(2)),SymbolicReal.rational(Rational.ONE).add(SymbolicReal.rational(Rational.ONE)));
        assertThrows(MathFailure.class,() -> SymbolicReal.sqrt(Rational.of(-1)));
    }
}
