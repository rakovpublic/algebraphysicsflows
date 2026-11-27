package operations;

import algebra.concrete.DynamicalStandardModelFlow;
import algebraflow.IAlgebraFlow;
import mathematics.linear.RationalMatrix;
import mathematics.numbers.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;
import static org.junit.Assert.*;

/** Gauge representation, graded leading terms, oriented fold, and cosmological transfers. */
public class CCDRDerivationUpgradeTest {
    @Test public void gaugeActionHasStandardModelCasimirsAndCommutingHypercharge(){
        WittGaugeAction gauge=WittGaugeAction.model();
        assertTrue(gauge.hyperchargeCommutesWithGauge());
        assertTrue(gauge.colorCommutesWithIsospin());
        assertEquals(Rational.of(3,4),gauge.su2Casimir(0));
        assertEquals(Rational.ZERO,gauge.su2Casimir(8));
        assertEquals(Rational.of(4,3),gauge.su3Casimir(2));
        assertEquals(Rational.ZERO,gauge.su3Casimir(0));
        assertEquals("(1, 2)_-1/2",gauge.representationLabel(0));
        assertEquals("(3, 2)_1/6",gauge.representationLabel(2));
        assertEquals("(1, 1)_1",gauge.representationLabel(8));
        assertEquals("(3bar, 1)_-2/3",gauge.representationLabel(10));
        RationalMatrix casimir=WittGaugeAction.realDoubletCasimir();
        assertEquals(RationalMatrix.identity(4).scale(Rational.of(3,4)),casimir);
        System.out.println("GAUGE historicalResidual="+gauge.historicalResidual()+" invariant="+gauge.historicallyInvariant());
        IAlgebraFlow<Rational> flow=DynamicalStandardModelFlow.flow();
        assertEquals("3/4",flow.performOneOperandOperation("su2-casimir-doublet").collect().get(0));
        assertEquals("4/3",flow.performOneOperandOperation("su3-casimir-triplet").collect().get(0));
    }

    @Test public void orderThreeMapPreservesOctonionProducts(){
        RationalOctonion i=RationalOctonion.I,j=RationalOctonion.J,l=RationalOctonion.L;
        assertEquals(i,i.orderThreeAutomorphism().orderThreeAutomorphism().orderThreeAutomorphism());
        assertEquals(i.multiply(j).orderThreeAutomorphism(),i.orderThreeAutomorphism().multiply(j.orderThreeAutomorphism()));
        assertEquals(i.multiply(l).orderThreeAutomorphism(),i.orderThreeAutomorphism().multiply(l.orderThreeAutomorphism()));
        assertEquals(j,i.orderThreeAutomorphism());
    }

    @Test public void residuePerturbationDoesNotInsertTheMeasuredPhase(){
        TrialityFlavorModule.Perturbation value=TrialityFlavorModule.perturb(new BigDecimal("0.00508"),12);
        System.out.println("PERTURB "+value);
        assertTrue(value.deltaRadians.subtract(new BigDecimal("0.52868")).abs().compareTo(new BigDecimal("0.001"))<0);
        assertNotNull(value.jarlskog);
        assertNotNull(value.koide);
    }

    @Test public void leadingGradePreservesSingleTermProducts(){
        RationalTwistedLatticeAlgebra left=RationalTwistedLatticeAlgebra.basis(3,new RationalTwistedLatticeAlgebra.Exponent(1,0,0),RationalQuaternion.I);
        RationalTwistedLatticeAlgebra right=RationalTwistedLatticeAlgebra.basis(3,new RationalTwistedLatticeAlgebra.Exponent(0,1,0),RationalQuaternion.J);
        assertEquals(left.multiply(right).leadingTerm(),left.leadingTerm().multiply(right.leadingTerm()));
        RationalTwistedHahnSeries x=RationalTwistedHahnSeries.groupElement(BigInteger.valueOf(2));
        RationalTwistedHahnSeries y=RationalTwistedHahnSeries.groupElement(BigInteger.ONE);
        assertEquals(x,x.leadingTerm());
        assertEquals(x.multiply(y).leadingTerm(),x.leadingTerm().multiply(y.leadingTerm()));
    }

    @Test public void orientedFrameSelectsOneSignFactorization(){
        CCDRChiralFold oriented=CCDRChiralFold.fromOrientedBoundary(-1,1);
        CCDRChiralFold flipped=CCDRChiralFold.fromOrientedBoundary(1,-1);
        assertEquals(-1,oriented.epsilon5());
        assertEquals(1,oriented.foliationArrow());
        assertEquals(oriented.chiralitySign(),flipped.chiralitySign());
        assertFalse(oriented.epsilon5()==flipped.epsilon5()&&oriented.foliationArrow()==flipped.foliationArrow());
        IAlgebraFlow<Rational> flow=DynamicalStandardModelFlow.flow();
        assertEquals("16",flow.performOneOperandOperation("algebraic-depth").collect().get(0));
        assertEquals("2",flow.performOneOperandOperation("bsm1-doubling").collect().get(0));
    }

    @Test public void cosmologicalTransfersKeepCatalogueInputsOutsideTheAlgebra(){
        BigDecimal raw=CosmologicalTransfers.rawSheetKurtosis(CosmologicalTransfers.CATALOGUE_NU,CosmologicalTransfers.CATALOGUE_WALL_TO_GRAIN,8,12);
        BigDecimal factor=CosmologicalTransfers.quotedNormalizationFactor(12);
        System.out.println("KURTOSIS raw="+raw.toPlainString()+" factor="+factor.toPlainString()+" window="+CosmologicalTransfers.kurtosisWindow(raw)+" uncertainty="+CosmologicalTransfers.propagatedUncertainty(8).toPlainString());
        assertEquals("FAIL_3SIGMA",CosmologicalTransfers.kurtosisWindow(raw));
        assertEquals("PASS_2SIGMA",CosmologicalTransfers.kurtosisWindow(CosmologicalTransfers.QUOTED_SHEET_KURTOSIS));
        assertTrue(raw.subtract(new BigDecimal("3.021")).abs().compareTo(new BigDecimal("0.01"))<0);
        assertEquals(Rational.of(8,3),CosmologicalTransfers.depthOverSix(8));
        assertTrue(CosmologicalTransfers.exactlyCollinear(CosmologicalTransfers.anisotropyAxis(),CosmologicalTransfers.parityAxis()));
        assertEquals(Rational.ONE,CosmologicalTransfers.dot(CosmologicalTransfers.anisotropyAxis(),CosmologicalTransfers.parityAxis()));
        assertEquals("FAIL_3SIGMA",CosmologicalTransfers.alignmentStatus(BigDecimal.ZERO,new BigDecimal("0.5")));
        assertEquals(Rational.of(2),CosmologicalTransfers.algebraicDoubling());
        assertTrue(CosmologicalTransfers.algebraicDoubling().equals(Rational.of(2)));
        assertEquals(0,new BigDecimal("0.508").compareTo(CosmologicalTransfers.phenomenologicalSplitting(new BigDecimal("0.00508"),new BigDecimal("100"))));
    }

    @Test public void firstOrderHoldsForComplexScalarsAndIsMeasuredOnQuaternions(){
        Rational complex=FiniteDiracSpectralAction.associativeSliceDefect(RationalDixonZ2.fromComplex(new RationalComplex(Rational.ZERO,Rational.ONE)));
        Rational quaternion=FiniteDiracSpectralAction.associativeSliceDefect(RationalDixonZ2.fromQuaternion(RationalQuaternion.I));
        System.out.println("FIRST-ORDER complex="+complex+" quaternion="+quaternion);
        assertEquals(Rational.ZERO,complex);
    }
}
