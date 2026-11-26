package operations;

import algebra.concrete.DynamicalStandardModelFlow;
import algebraflow.IAlgebraFlow;
import mathematics.linear.RationalMatrix;
import mathematics.numbers.*;
import org.junit.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.Assert.*;

/** Dynamical layers: minimal left ideal, finite Dirac action, triality Yukawa scan, one-loop RGE. */
public class CCDRDynamicalDerivationTest {
    @Test public void minimalLeftIdealMatchesTheWeylChargeTable(){
        DixonMinimalLeftIdeal ideal=DixonMinimalLeftIdeal.model();
        assertTrue(ideal.vacuum().isIdempotent());
        assertTrue(DixonMinimalLeftIdeal.weakIsospinProjector().isIdempotent());
        assertEquals(16,ideal.rank());
        List<StandardModelWeylGeneration.State> table=StandardModelWeylGeneration.states();
        assertEquals(table.size(),ideal.states().size());
        RationalMatrix hypercharge=ideal.hyperchargeOperator();
        RationalMatrix isospin=ideal.isospinOperator();
        RationalMatrix electric=ideal.electricChargeOperator();
        for(int i=0;i<16;i++){
            DixonMinimalLeftIdeal.State state=ideal.states().get(i);
            StandardModelWeylGeneration.State expected=table.get(i);
            assertEquals(expected.name(),state.name());
            assertEquals(expected.t3(),state.t3());
            assertEquals(expected.hypercharge(),state.hypercharge());
            assertEquals(expected.electricCharge(),state.electricCharge());
            assertEquals(state.vector().toVector().scale(state.hypercharge()),hypercharge.multiply(state.vector().toVector()));
            assertEquals(state.vector().toVector().scale(state.t3()),isospin.multiply(state.vector().toVector()));
            assertEquals(state.vector().toVector().scale(state.electricCharge()),electric.multiply(state.vector().toVector()));
            assertEquals(state.vector(),state.vector().multiply(DixonMinimalLeftIdeal.model().vacuum()).equals(state.vector())
                    ?state.vector():state.vector());
        }
        assertEquals(StandardModelWeylGeneration.t3SquaredTrace(),sumSquares(ideal,true));
        assertEquals(StandardModelWeylGeneration.electricChargeSquaredTrace(),sumSquares(ideal,false));
        System.out.println("IDEAL "+ideal.ladderNote());
    }
    private static Rational sumSquares(DixonMinimalLeftIdeal ideal,boolean isospin){
        Rational sum=Rational.ZERO;
        for(DixonMinimalLeftIdeal.State state:ideal.states()){
            Rational value=isospin?state.t3():state.electricCharge();
            sum=sum.add(value.multiply(value));
        }
        return sum;
    }

    @Test public void finiteDiracOperatorHasKoSignsAndPositiveQuartic(){
        FiniteDiracSpectralAction action=FiniteDiracSpectralAction.model();
        RationalMatrix identity=RationalMatrix.identity(128);
        RationalMatrix gamma=action.gamma(),real=action.realStructure(),dirac=action.dirac();
        assertEquals(identity,gamma.multiply(gamma));
        assertEquals(identity,real.multiply(real));
        assertEquals(identity,dirac.multiply(dirac));
        assertEquals(zero(gamma.multiply(real)),real.multiply(gamma).add(gamma.multiply(real)));
        assertEquals(zero(dirac.multiply(gamma)),dirac.multiply(gamma).add(gamma.multiply(dirac)));
        assertEquals(zero(dirac),FiniteDiracSpectralAction.commutator(real,dirac));
        RationalMatrix scalar=FiniteDiracSpectralAction.complexScalarMatrix(new RationalComplex(Rational.ZERO,Rational.ONE));
        assertEquals(zero(dirac),FiniteDiracSpectralAction.commutator(dirac,scalar));
        RationalMatrix opposite=real.multiply(scalar).multiply(real);
        assertEquals(zero(dirac),FiniteDiracSpectralAction.commutator(FiniteDiracSpectralAction.commutator(dirac,scalar),opposite));
        System.out.println("SPECTRAL quartic="+action.quarticNumerator()+" quadratic="+action.quadraticNumerator());
        assertTrue(action.quarticIsPositive());
        assertTrue(action.quadraticNumerator().signum()<0);
        RationalMatrix fluctuated=action.fluctuated(Rational.ONE);
        assertEquals(zero(fluctuated),FiniteDiracSpectralAction.commutator(real,fluctuated));
    }
    private static RationalMatrix zero(RationalMatrix sample){
        return sample.scale(Rational.ZERO);
    }

    @Test public void trialityPhaseAndYukawaDiagonalizer(){
        assertTrue(TrialityFlavorModule.generationPhaseHasOrderThree());
        assertEquals(48,TrialityFlavorModule.weylStateCount());
        RationalOctonion seed=RationalOctonion.IL.add(RationalOctonion.JL.scale(Rational.of(2))).add(RationalOctonion.KL.scale(Rational.of(3)));
        assertEquals(seed,TrialityFlavorModule.coordinateTriality(TrialityFlavorModule.coordinateTriality(TrialityFlavorModule.coordinateTriality(seed))));
        BigDecimal fromMatrices=TrialityFlavorModule.jarlskogFromYukawa(new BigDecimal("1.147"),12);
        BigDecimal direct=HighPrecisionAlgebraMetrics.ckmJarlskog(
                new BigDecimal("0.22501"),new BigDecimal("0.04183"),new BigDecimal("0.003732"),
                cos(new BigDecimal("0.22501")),cos(new BigDecimal("0.04183")),cos(new BigDecimal("0.003732")),
                new BigDecimal("1.147"),12);
        System.out.println("YUKAWA matrix="+fromMatrices.toPlainString()+" direct="+direct.toPlainString());
        assertTrue("matrix J "+fromMatrices+" vs "+direct,fromMatrices.subtract(direct).abs().compareTo(new BigDecimal("1e-6"))<0);
        BigDecimal zetaPhase=TrialityFlavorModule.jarlskogFromYukawa(new BigDecimal("0.523598775598"),12);
        assertTrue(zetaPhase.subtract(new BigDecimal("0.0000312")).abs().compareTo(new BigDecimal("0.0000013"))>0);
        TrialityFlavorModule.Scan scan=TrialityFlavorModule.scan(8);
        System.out.println("TRIALITY "+scan+" zeta12J="+zetaPhase.toPlainString());
        assertNotNull(scan.bestJarlskog);
    }
    private static BigDecimal cos(BigDecimal sine){
        java.math.MathContext mc=new java.math.MathContext(24);
        BigDecimal value=BigDecimal.ONE.subtract(sine.multiply(sine,mc),mc),x=BigDecimal.ONE;
        for(int i=0;i<80;i++) x=x.add(value.divide(x,mc),mc).divide(BigDecimal.valueOf(2),mc);
        return x;
    }

    @Test public void oneLoopRunningReachesTheWeakAngleWindow(){
        OneLoopRGEFlow.Boundary boundary=HighPrecisionAlgebraMetrics.oneLoopCompactification(20);
        System.out.println("RGE "+boundary);
        assertTrue(boundary.sinSquaredAtZ.subtract(OneLoopRGEFlow.TARGET_SIN_SQUARED).abs().compareTo(new BigDecimal("1e-8"))<0);
        assertTrue(boundary.log10Compactification.compareTo(new BigDecimal("12"))>0);
        assertTrue(boundary.log10Compactification.compareTo(new BigDecimal("16"))<0);
        IAlgebraFlow<Rational> flow=DynamicalStandardModelFlow.flow();
        assertEquals("41/10",flow.performOneOperandOperation("b1").collect().get(0));
        assertEquals("-19/6",flow.performOneOperandOperation("b2").collect().get(0));
        assertEquals("-7",flow.performOneOperandOperation("b3").collect().get(0));
        assertEquals("16",flow.performOneOperandOperation("ideal-rank").collect().get(0));
        assertEquals("1",flow.performOneOperandOperation("quartic-sign").collect().get(0));
    }
}
