package operations;

import algebra.concrete.ConcreteMathematics;
import mathematics.calculus.Polynomial;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerVector;
import mathematics.numbers.*;
import mathematics.topology.*;
import org.junit.Test;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

/** Executable algebra/topology flow for the documented structural sign predictions. */
public class CCDRStandardModelFlowTest {
    private static BigInteger z(long n){return BigInteger.valueOf(n);}
    @Test public void collapseSignsFlowThroughRuntimeRankHmnDixonChiralityAndCyclotomics(){
        FiniteSimplicialComplex simplex=new FiniteSimplicialComplex(Collections.singletonList(new FiniteSet<>(Arrays.asList(0,1,2))));
        SimplicialCollapse collapse=SimplicialCollapse.absolute(simplex,new FiniteSet<>(Arrays.asList(z(0),z(2))));
        List<FiniteSet<BigInteger>> edges=collapse.source().simplexBasis(z(1));
        int signedIncidence=collapse.chainHomotopyMatrix(z(1)).get(0,edges.indexOf(new FiniteSet<>(Arrays.asList(z(0),z(2))))).intValueExact();
        assertEquals(-1,signedIncidence);

        // A vector determines N at runtime. The cocycle matrix is a separate exact carrier parameter.
        IntegerVector exponent=new IntegerVector(z(signedIncidence),z(0),z(0),z(0));
        int[][] cocycle=new int[4][4];cocycle[1][2]=cocycle[2][1]=1;
        RationalTwistedLatticeAlgebra bulk=RationalTwistedLatticeAlgebra.basis(4,cocycle,
                new RationalTwistedLatticeAlgebra.Exponent(exponent.entries()),RationalQuaternion.scalar(Rational.of(signedIncidence)));
        assertEquals(4,bulk.rank());
        assertEquals(-1,RationalTwistedLatticeAlgebra.tau(new RationalTwistedLatticeAlgebra.Exponent(0,1,0,0),
                new RationalTwistedLatticeAlgebra.Exponent(0,0,1,0),cocycle));

        // Leading coefficient carries the signed collapse weight into the quaternion slice,
        // then the crossed-product generator selects the odd chirality block.
        ConcreteMathematics math=new ConcreteMathematics();
        RationalQuaternion leading=bulk.leadingCoefficient();
        RationalDixonZ2 dixon=RationalDixonZ2.fromQuaternion(leading).multiply(RationalDixonZ2.U);
        CCDRChiralFold fold=new CCDRChiralFold(signedIncidence,1,signedIncidence,signedIncidence);
        assertTrue(fold.cl9Consistent());
        assertFalse(fold.isLeftChiral());
        assertEquals(dixon.oddPart(),fold.projectDixonParity(dixon));
        assertFalse(fold.projectDixonParity(dixon).isZero());
        assertEquals(16,StandardModelWeylGeneration.stateCount());
        assertTrue(StandardModelWeylGeneration.anomalyFreeHyperchargeSignature());

        RationalCyclotomic zeta=RationalCyclotomic.zeta(12);
        RationalCyclotomic.DecimalComplex decimal=zeta.evaluate(40);
        assertEquals(0.5,decimal.imaginary.doubleValue(),1e-38);
        assertEquals(Math.sqrt(3)/2,decimal.real.doubleValue(),1e-38);
        assertEquals(new Polynomial(Rational.ONE,Rational.ZERO,Rational.of(-1),Rational.ZERO,Rational.ONE),zeta.cyclotomicPolynomial());

        // Exercise the registered rank-N carrier and its generated vector-to-basis transfer.
        assertTrue(math.operations().containsKey("HMN(H(Q);Z^N,sigma,tau).group-basis"));
        RationalTwistedLatticeAlgebra registeredBasis=math.integerVectors.algebra().buildAlgebraItem(exponent)
                .<RationalTwistedLatticeAlgebra>performAlgebraTransfer("HMN(H(Q);Z^N,sigma,tau).group-basis").perform().getResult();
        assertEquals(4,registeredBasis.rank());
    }
}
