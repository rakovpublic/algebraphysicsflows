package operations;

import algebra.concrete.ConcreteMathematics;
import mathematics.calculus.Polynomial;
import mathematics.numbers.*;
import org.junit.Test;
import java.math.*;
import static org.junit.Assert.*;

public class NativeAdvancedAlgebraExtensionsTest {
    @Test public void cyclotomicArithmeticAndDecimalEvaluationAreExactAndConsistent(){
        RationalCyclotomic z=RationalCyclotomic.zeta(12),i=RationalCyclotomic.fromComplex(12,new RationalComplex(Rational.ZERO,Rational.ONE));
        assertEquals(new Polynomial(Rational.ONE,Rational.ZERO,Rational.of(-1),Rational.ZERO,Rational.ONE),z.cyclotomicPolynomial());
        assertEquals(RationalCyclotomic.fromRational(12,Rational.of(-1)),z.pow(6));
        assertEquals(i,z.pow(3));
        assertEquals(RationalCyclotomic.one(12),z.multiply(z.inverse()));
        assertEquals(new RationalComplex(Rational.ZERO,Rational.ONE),i.toComplex());
        RationalCyclotomic.DecimalComplex approximation=z.evaluate(35);
        assertEquals(Math.sqrt(3)/2,approximation.real.doubleValue(),1e-30);
        assertEquals(0.5,approximation.imaginary.doubleValue(),1e-30);
    }
    @Test public void rankThreeSignTwistAndNonSurjectiveModeRespectProduct(){
        int rank=3; RationalTwistedLatticeAlgebra.Exponent e1=new RationalTwistedLatticeAlgebra.Exponent(1,0,0),
                e2=new RationalTwistedLatticeAlgebra.Exponent(0,1,0),e3=new RationalTwistedLatticeAlgebra.Exponent(0,0,1);
        RationalTwistedLatticeAlgebra x=RationalTwistedLatticeAlgebra.basis(rank,e2,RationalQuaternion.ONE),
                y=RationalTwistedLatticeAlgebra.basis(rank,e3,RationalQuaternion.ONE);
        assertEquals(-1,RationalTwistedLatticeAlgebra.tau(e2,e3));
        assertNotEquals(x.multiply(y),y.multiply(x));
        RationalTwistedLatticeAlgebra residue=RationalTwistedLatticeAlgebra.basis(rank,e1,RationalQuaternion.J);
        assertTrue(residue.hasOddFirstCoordinateSupport());
        assertFalse(residue.darkMatterEndomorphism().hasOddFirstCoordinateSupport());
        RationalTwistedLatticeAlgebra a=residue.add(x).add(y),b=x.add(y).add(residue);
        assertEquals(a.multiply(b).darkMatterEndomorphism(),a.darkMatterEndomorphism().multiply(b.darkMatterEndomorphism()));
    }
    @Test public void dixonProjectorsAreIdempotentAndComplementary(){
        RationalDixonZ2 gp=RationalDixonZ2.groupIdempotent(true),gm=RationalDixonZ2.groupIdempotent(false);
        RationalDixonZ2 wp=RationalDixonZ2.wittIdempotent(true),wm=RationalDixonZ2.wittIdempotent(false);
        assertTrue(gp.isIdempotent());assertTrue(gm.isIdempotent());assertEquals(RationalDixonZ2.ONE,gp.add(gm));assertEquals(RationalDixonZ2.ZERO,gp.multiply(gm));
        assertTrue(wp.isIdempotent());assertTrue(wm.isIdempotent());assertEquals(RationalDixonZ2.ONE,wp.add(wm));assertEquals(RationalDixonZ2.ZERO,wp.multiply(wm));
        assertEquals(gp.leftMultiplicationMatrix(),gp.leftMultiplicationMatrix().multiply(gp.leftMultiplicationMatrix()));
        assertEquals(wp.leftMultiplicationMatrix(),wp.leftMultiplicationMatrix().multiply(wp.leftMultiplicationMatrix()));
    }
    @Test public void exactOctonionAndQuaternionGaugeCandidatesHaveExpectedDimensions(){
        java.util.List<mathematics.linear.RationalMatrix> su3=RationalOctonionDerivations.su3StabilizerCandidates();
        assertEquals(8,su3.size());
        assertEquals(3,RationalOctonionDerivations.quaternionAdjointGenerators().size());
        Rational[] zero=new Rational[8];java.util.Arrays.fill(zero,Rational.ZERO);
        for(mathematics.linear.RationalMatrix d:su3){
            assertEquals(new mathematics.linear.RationalVector(Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO),
                    d.multiply(new mathematics.linear.RationalVector(Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ONE,Rational.ZERO,Rational.ZERO,Rational.ZERO)));
            for(int a=0;a<8;a++)for(int b=0;b<8;b++){
                Rational[] xa=zero.clone(),xb=zero.clone();xa[a]=Rational.ONE;xb[b]=Rational.ONE;
                RationalOctonion x=new RationalOctonion(xa),y=new RationalOctonion(xb),xy=x.multiply(y);
                RationalOctonion left=oct(d.multiply(vector(xy)));
                RationalOctonion right=oct(d.multiply(vector(x))).multiply(y).add(x.multiply(oct(d.multiply(vector(y)))));
                assertEquals(left,right);
            }
        }
        mathematics.linear.RationalMatrix phase=RationalOctonionDerivations.complexPhaseGenerator();
        assertEquals(RationalMatrixIdentity2.negate(),phase.multiply(phase));
        mathematics.linear.RationalMatrix dixonPhase=RationalDixonZ2.complexPhaseGeneratorMatrix();
        assertEquals(mathematics.linear.RationalMatrix.identity(128).scale(Rational.of(-1)),dixonPhase.multiply(dixonPhase));
    }
    private static mathematics.linear.RationalVector vector(RationalOctonion o){Rational[] c=new Rational[8];for(int i=0;i<8;i++)c[i]=o.coordinate(i);return new mathematics.linear.RationalVector(c);}
    private static RationalOctonion oct(mathematics.linear.RationalVector v){Rational[] c=new Rational[8];for(int i=0;i<8;i++)c[i]=v.get(i);return new RationalOctonion(c);}
    private static final class RationalMatrixIdentity2 {static mathematics.linear.RationalMatrix negate(){return new mathematics.linear.RationalMatrix(new Rational[][]{{Rational.of(-1),Rational.ZERO},{Rational.ZERO,Rational.of(-1)}});}}
    @Test public void chiTriggerAndRegisteredBridgesWork(){
        ConcreteMathematics m=new ConcreteMathematics();
        assertTrue(m.operations().containsKey("HMN(H(Q);Z^3,sigma,tau).dark-matter-endomorphism"));
        assertTrue(m.operations().containsKey("Q(zeta_12).embed-complex"));
        assertEquals(java.util.Collections.singletonList("Q(zeta_12)Q[x][1]"),m.flow(m.booleans,java.util.Collections.singletonList(true))
                .<RationalCyclotomic>performAlgebraTransfer("to-cyclotomic").collect());
        assertEquals(java.util.Collections.singletonList("Quaternion[1, 0, 0, 0]"),m.flow(m.booleans,java.util.Collections.singletonList(true))
                .<RationalTwistedLatticeAlgebra>performAlgebraTransfer("to-twisted-lattice")
                .<RationalQuaternion>performAlgebraTransfer("to-quaternion").collect());
        assertEquals(RationalDixonZ2.fromQuaternion(RationalQuaternion.I),RationalTwistedHahnSeries.GROUP_GENERATOR.projectChi5ToOneInDixon());
        RationalTwistedHahnSeries pole=RationalTwistedHahnSeries.scalar(new mathematics.calculus.RationalFunction(
                new mathematics.calculus.Polynomial(Rational.ONE),new mathematics.calculus.Polynomial(Rational.of(-1),Rational.ONE)));
        assertThrows(RuntimeException.class,pole::projectChi5ToOneInDixon);
        BigDecimal koide=HighPrecisionAlgebraMetrics.koide(BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE,30);
        assertTrue(koide.subtract(new BigDecimal("0.333333333333333333333333333333")).abs().compareTo(new BigDecimal("1e-29"))<0);
        BigDecimal j=HighPrecisionAlgebraMetrics.ckmJarlskog(BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE,
                BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE,RationalCyclotomic.zeta(12),20);
        assertEquals(0.5,j.doubleValue(),1e-18);
    }
}
