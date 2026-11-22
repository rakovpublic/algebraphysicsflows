package operations;

import algebraflow.IAlgebraFlow;
import algebra.concrete.ConcreteMathematics;
import mathematics.calculus.Polynomial;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.numbers.*;
import mathematics.topology.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static org.junit.Assert.*;

/** End-to-end executable checks for the structural CCDR v7.10 / Synthesis v3.9 pipeline. */
public class CCDRStandardModelPipelineTest {
    private static BigInteger z(long n){return BigInteger.valueOf(n);}

    @Test public void fullTwoTierDerivationPipelineRunsThroughNativeCarriersAndFlows(){
        ConcreteMathematics math=new ConcreteMathematics();

        // Stage 1: exercise two runtime lattice ranks and a non-trivial cocycle.
        RationalTwistedLatticeAlgebra bulk5=bulkCascade(5,math);
        RationalTwistedLatticeAlgebra bulk8=bulkCascade(8,math);
        assertEquals(5,bulk5.rank());
        assertEquals(8,bulk8.rank());

        // Stage 2: signed subdivision and an elementary collapse determine the fold sign.
        FiniteSimplicialComplex triangle=simplex(3);
        SimplicialSubdivision subdivision=SimplicialSubdivision.absolute(triangle);
        assertEquals(triangle.dimension(),subdivision.subdivided().ambient().dimension());
        assertEquals(triangle.eulerCharacteristic(),subdivision.subdivided().ambient().eulerCharacteristic());
        SimplicialCollapse collapse=SimplicialCollapse.absolute(triangle,face(0,2));
        List<FiniteSet<BigInteger>> edgeBasis=collapse.source().simplexBasis(z(1));
        int orientation=collapse.chainHomotopyMatrix(z(1)).get(0,edgeBasis.indexOf(face(0,2))).intValueExact();
        assertEquals(-1,orientation);
        CCDRChiralFold fold=new CCDRChiralFold(orientation,1,orientation,orientation);
        assertEquals(orientation,fold.chiralitySign());
        assertTrue(fold.smD11Consistent());
        assertTrue(fold.pA30Consistent());
        assertTrue(fold.cl9Consistent());
        assertFalse(fold.isLeftChiral());
        assertTrue(new CCDRChiralFold(orientation,-1,-orientation,-orientation).isLeftChiral());
        assertTrue(new CCDRChiralFold(-orientation,1,-orientation,-orientation).isLeftChiral());

        // Stage 3: Dixon parity, idempotents, regular matrices, and non-associative boundary.
        RationalOctonion l=octonionBasis(4);
        RationalDixonZ2 octonionBoundary=RationalDixonZ2.fromOctonion(l);
        assertEquals(octonionBoundary.negate(),octonionBoundary.gradingInvolution());
        assertEquals(octonionBoundary.gradingInvolution().multiply(RationalDixonZ2.U),
                RationalDixonZ2.U.multiply(octonionBoundary));
        RationalDixonZ2 mixedDixon=RationalDixonZ2.fromQuaternion(RationalQuaternion.I)
                .add(RationalDixonZ2.U).add(RationalDixonZ2.basis(1,4));
        assertEquals(mixedDixon,mixedDixon.evenPart().add(mixedDixon.oddPart()));
        assertTrue(mixedDixon.evenPart().oddPart().isZero());
        assertEquals(128,mixedDixon.leftMultiplicationMatrix().rows());
        assertEquals(128,mixedDixon.leftMultiplicationMatrix().columns());
        RationalDixonZ2 wp=RationalDixonZ2.wittIdempotent(true),wm=RationalDixonZ2.wittIdempotent(false);
        assertTrue(wp.isIdempotent());assertTrue(wm.isIdempotent());
        assertEquals(RationalDixonZ2.ONE,wp.add(wm));
        assertEquals(RationalDixonZ2.ZERO,wp.multiply(wm));

        RationalOctonion e1=octonionBasis(1),e2=octonionBasis(2),e4=octonionBasis(4);
        RationalOctonion octonionAssociator=e1.multiply(e2).multiply(e4).subtract(e1.multiply(e2.multiply(e4)));
        assertNotEquals(RationalOctonion.ZERO,octonionAssociator);
        RationalDixonZ2 dx=RationalDixonZ2.fromOctonion(e1),dy=RationalDixonZ2.fromOctonion(e2),dz=RationalDixonZ2.fromOctonion(e4);
        assertNotEquals(dx.multiply(dy).multiply(dz),dx.multiply(dy.multiply(dz)));
        RationalTwistedHahnSeries hx=RationalTwistedHahnSeries.fromQuaternion(new RationalQuaternion(Rational.ONE,Rational.of(2),Rational.of(3),Rational.of(4)));
        RationalTwistedHahnSeries hy=RationalTwistedHahnSeries.GROUP_GENERATOR;
        RationalTwistedHahnSeries hz=RationalTwistedHahnSeries.fromRational(Rational.of(3));
        assertEquals(hx.multiply(hy).multiply(hz),hx.multiply(hy.multiply(hz)));
        assertThrows(RuntimeException.class,() -> RationalTwistedHahnSeries.fromOctonion(l));
        assertThrows(RuntimeException.class,() -> RationalTwistedHahnSeries.fromDixonZ2(octonionBoundary));
        assertEquals(RationalDixonZ2.fromQuaternion(RationalQuaternion.I),RationalTwistedHahnSeries.GROUP_GENERATOR.projectChi5ToOneInDixon());
        assertEquals(Collections.singletonList(RationalDixonZ2.fromQuaternion(RationalQuaternion.I).toString()),
                math.flow(math.twistedHahnMalcevNeumann,Collections.singletonList(RationalTwistedHahnSeries.GROUP_GENERATOR))
                        .<RationalDixonZ2>performAlgebraTransfer("chi5-to-one-dixon").collect());
        RationalMatrix octonionLeft=e1.leftMultiplicationMatrix();
        assertEquals(8,octonionLeft.rows());assertEquals(8,octonionLeft.columns());

        List<RationalMatrix> su3=RationalOctonionDerivations.su3StabilizerCandidates();
        List<RationalMatrix> su2=RationalOctonionDerivations.quaternionAdjointGenerators();
        assertEquals(8,su3.size());assertEquals(3,su2.size());
        assertLieBracketClosed(su3);assertLieBracketClosed(su2);

        // Stage 4: exact 16-state charges and the perturbative gauge anomaly traces.
        StandardModelWeylGeneration generation=new StandardModelWeylGeneration();
        assertEquals(16,generation.stateCount());
        assertEquals(Rational.ZERO,generation.hyperchargeTrace());
        assertEquals(Rational.ZERO,generation.cubicHyperchargeTrace());
        assertEquals(Rational.ZERO,StandardModelWeylGeneration.mixedSu2HyperchargeTrace());
        assertEquals(Rational.ZERO,StandardModelWeylGeneration.mixedSu3HyperchargeTrace());
        assertEquals(0,StandardModelWeylGeneration.cubicSu3AnomalyIndex());
        assertEquals(0,StandardModelWeylGeneration.su2DoubletParity());
        assertTrue(StandardModelWeylGeneration.anomalyFreeHyperchargeSignature());

        // Stage 5: exact Q(zeta_12), controlled decimal phase, Jarlskog and Koide values.
        RationalCyclotomic zeta=RationalCyclotomic.zeta(12);
        RationalCyclotomic imaginary=RationalCyclotomic.fromComplex(12,new RationalComplex(Rational.ZERO,Rational.ONE));
        assertEquals(imaginary,zeta.pow(3));
        assertEquals(RationalCyclotomic.fromRational(12,Rational.of(-1)),zeta.pow(6));
        assertEquals(RationalCyclotomic.one(12),zeta.multiply(zeta.inverse()));
        assertEquals(new Polynomial(Rational.ONE,Rational.ZERO,Rational.of(-1),Rational.ZERO,Rational.ONE),zeta.cyclotomicPolynomial());
        RationalCyclotomic.DecimalComplex phase=zeta.evaluate(40);
        assertEquals(Math.sqrt(3)/2,phase.real.doubleValue(),1e-38);
        assertEquals(0.5,phase.imaginary.doubleValue(),1e-38);
        BigDecimal jarlskog=HighPrecisionAlgebraMetrics.ckmJarlskog(new BigDecimal("0.225"),new BigDecimal("0.041"),new BigDecimal("0.0037"),
                new BigDecimal("0.9744"),new BigDecimal("0.9992"),new BigDecimal("0.999993"),zeta,30);
        assertTrue(jarlskog.compareTo(new BigDecimal("0.000016"))>0);
        assertTrue(jarlskog.compareTo(new BigDecimal("0.000017"))<0);
        BigDecimal koide=HighPrecisionAlgebraMetrics.koide(new BigDecimal("0.51099895"),new BigDecimal("105.6583755"),new BigDecimal("1776.86"),30);
        assertTrue(koide.subtract(new BigDecimal("0.666666666666666666666666666667")).abs().compareTo(new BigDecimal("0.0001"))<0);

        // Stage 6: the native flow applies a binary collapse projection and unary chain boundary.
        SimplicialChain triangleEdge=new SimplicialChain(triangle,z(1),new IntegerVector(z(0),z(1),z(0)));
        IAlgebraFlow<mathematics.linear.IntegerVector> chainFlow=math.flow(math.complexes,Collections.singletonList(triangle))
                .<SimplicialCollapse,FiniteSet<BigInteger>>performAlgebraUnsafe("SimplicialCollapse.from-absolute-face",face(0,2))
                .performLeftProjectionOperation("on-absolute-chain",triangleEdge)
                .performOneOperandOperation("boundary")
                .<mathematics.linear.IntegerVector>performAlgebraTransfer("coordinates");
        assertEquals(math.simplicialChains.algebra().getAlgebraName(),
                math.flow(math.complexes,Collections.singletonList(triangle))
                        .<SimplicialCollapse,FiniteSet<BigInteger>>performAlgebraUnsafe("SimplicialCollapse.from-absolute-face",face(0,2))
                        .performLeftProjectionOperation("on-absolute-chain",triangleEdge).getCurrentAlgebraName());
        assertEquals(Collections.singletonList(collapse.onAbsoluteChain(triangleEdge).boundary().coordinates().toString()),chainFlow.collect());
    }

    private static RationalTwistedLatticeAlgebra bulkCascade(int rank,ConcreteMathematics math){
        int[][] cocycle=new int[rank][rank];
        cocycle[1][2]=cocycle[2][1]=1;
        RationalTwistedLatticeAlgebra.Exponent g=exponent(rank,0,1,0),h=exponent(rank,0,0,1),residue=exponent(rank,1,0,0);
        RationalQuaternion q=new RationalQuaternion(Rational.ONE,Rational.of(2),Rational.ZERO,Rational.ONE);
        RationalQuaternion r=new RationalQuaternion(Rational.of(2),Rational.ZERO,Rational.ONE,Rational.ZERO);
        RationalTwistedLatticeAlgebra gx=RationalTwistedLatticeAlgebra.basis(rank,cocycle,g,q);
        RationalTwistedLatticeAlgebra hy=RationalTwistedLatticeAlgebra.basis(rank,cocycle,h,r);
        RationalQuaternion conjugated=new RationalQuaternion(r.real,r.i,r.j.negate(),r.k.negate());
        assertEquals(RationalTwistedLatticeAlgebra.basis(rank,cocycle,g.add(h),q.multiply(conjugated).negate()),gx.multiply(hy));
        assertNotEquals(gx.multiply(hy),hy.multiply(gx));

        RationalTwistedLatticeAlgebra oddResidue=RationalTwistedLatticeAlgebra.basis(rank,cocycle,residue,q);
        assertTrue(oddResidue.hasOddFirstCoordinateSupport());
        RationalTwistedLatticeAlgebra image=oddResidue.darkMatterEndomorphism();
        assertEquals(BigInteger.TWO,image.valuation().coordinate(0));
        assertFalse(image.hasOddFirstCoordinateSupport());
        assertEquals(oddResidue.darkMatterEndomorphism().toString(),
                math.flow(math.twistedLattice,Collections.singletonList(oddResidue))
                        .performOneOperandOperation("dark-matter-endomorphism").collect().get(0));
        assertEquals(gx.multiply(hy).darkMatterEndomorphism(),gx.darkMatterEndomorphism().multiply(hy.darkMatterEndomorphism()));
        return gx.multiply(hy).add(oddResidue);
    }

    private static RationalTwistedLatticeAlgebra.Exponent exponent(int rank,long... nonzero){
        long[] coordinates=new long[rank];
        for(int i=0;i<Math.min(rank,nonzero.length);i++)coordinates[i]=nonzero[i];
        return new RationalTwistedLatticeAlgebra.Exponent(coordinates);
    }
    private static FiniteSimplicialComplex simplex(int count){
        List<Integer> vertices=new ArrayList<>();for(int i=0;i<count;i++)vertices.add(i);
        return new FiniteSimplicialComplex(Collections.singletonList(new FiniteSet<>(vertices)));
    }
    private static FiniteSet<BigInteger> face(int... vertices){
        List<BigInteger> result=new ArrayList<>();for(int v:vertices)result.add(z(v));return new FiniteSet<>(result);
    }
    private static RationalOctonion octonionBasis(int index){
        Rational[] coordinates=new Rational[8];Arrays.fill(coordinates,Rational.ZERO);coordinates[index]=Rational.ONE;return new RationalOctonion(coordinates);
    }
    private static void assertLieBracketClosed(List<RationalMatrix> basis){
        for(RationalMatrix a:basis)for(RationalMatrix b:basis){
            RationalMatrix bracket=a.multiply(b).add(b.multiply(a).scale(Rational.of(-1)));
            assertTrue("Lie bracket escaped the registered generator span",inSpan(basis,bracket));
        }
    }
    private static boolean inSpan(List<RationalMatrix> basis,RationalMatrix value){
        int dimension=basis.get(0).rows()*basis.get(0).columns();
        Rational[][] columns=new Rational[dimension][basis.size()+1];
        for(int k=0;k<basis.size();k++)for(int r=0;r<basis.get(k).rows();r++)for(int c=0;c<basis.get(k).columns();c++)
            columns[r*basis.get(k).columns()+c][k]=basis.get(k).get(r,c);
        for(int r=0;r<value.rows();r++)for(int c=0;c<value.columns();c++)columns[r*value.columns()+c][basis.size()]=value.get(r,c);
        // Keep the span check over exact Q, not a floating approximation.
        RationalMatrix joined=new RationalMatrix(columns);
        Rational[][] original=new Rational[dimension][basis.size()];
        for(int r=0;r<dimension;r++)System.arraycopy(columns[r],0,original[r],0,basis.size());
        return new RationalMatrix(original).rank()==joined.rank();
    }
}
