package operations;

import mathematics.numbers.DixonMinimalLeftIdeal;
import mathematics.numbers.FiniteDiracSpectralAction;
import mathematics.numbers.HistoricalIdealInvariance;
import mathematics.numbers.OneLoopRGEFlow;
import mathematics.numbers.Rational;
import mathematics.numbers.WittGaugeAction;
import org.junit.Test;
import java.math.BigInteger;
import static org.junit.Assert.*;

/**
 * The historical CCDR generators are tested for invariance on the occupation ideal.
 * A nonzero residual is the obstruction. It is not projected away.
 */
public class CCDRHistoricalInvarianceTest {
    @Test public void projectorHasRankSixteenAndIsIdempotent(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        assertEquals(16,invariance.basis().rank());
        assertTrue(invariance.gramInverseIsExact());
        assertTrue(invariance.projectorFixesBasis());
        assertTrue(invariance.projectorIsIdempotentOnCoordinates());
    }

    @Test public void historicalGeneratorsDoNotPreserveTheOccupationIdeal(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        assertEquals(11,invariance.residuals().size());
        int failed=0;
        Rational total=Rational.ZERO;
        for(HistoricalIdealInvariance.GeneratorResidual residual:invariance.residuals()){
            System.out.println("RESIDUAL "+residual.family()+"["+residual.index()+"] rank="+residual.rank()+" frobeniusSquared="+residual.frobeniusSquared());
            assertEquals(128,residual.residual().rows());
            assertEquals(16,residual.residual().columns());
            if(!residual.invariant()){
                failed++;
                assertTrue(residual.rank()>0);
                assertTrue(residual.frobeniusSquared().signum()>0);
            }
            total=total.add(residual.frobeniusSquared());
        }
        assertEquals(invariance.totalFrobeniusSquared(),total);
        assertEquals(Rational.of(32),total);
        assertEquals(2,failed);
        assertEquals(9,invariance.invariantGeneratorCount());
        assertTrue(invariance.residuals().get(0).invariant());
        assertTrue(invariance.residuals().get(7).invariant());
        assertFalse(invariance.residuals().get(8).invariant());
        assertTrue(invariance.residuals().get(9).invariant());
        assertFalse(invariance.residuals().get(10).invariant());
        assertEquals(8,invariance.residuals().get(8).rank());
        assertEquals(Rational.of(16),invariance.residuals().get(8).frobeniusSquared());
        assertEquals(8,invariance.residuals().get(10).rank());
        assertEquals(Rational.of(16),invariance.residuals().get(10).frobeniusSquared());
        HistoricalIdealInvariance.Counterexample counterexample=invariance.firstCounterexample();
        assertNotNull(counterexample);
        System.out.println("COUNTEREXAMPLE "+counterexample);
        assertEquals("quaternion-adjoint",counterexample.family);
        assertEquals(0,counterexample.generatorIndex);
        assertEquals("e_L",counterexample.stateName);
        assertEquals(48,counterexample.coordinate);
        assertEquals(Rational.ONE,counterexample.value);
    }

    @Test public void occupationOperatorsAreNotAHistoricalRestriction(){
        assertFalse(WittGaugeAction.model().historicallyInvariant());
        assertEquals(Rational.of(32),WittGaugeAction.model().historicalResidual());
        assertTrue(FiniteDiracSpectralAction.model().higgsDirectionIsAnsatz());
        assertEquals("reconstruction",OneLoopRGEFlow.RESULT_KIND);
        Rational hyperchargeTrace=WittGaugeAction.model().hypercharge().trace();
        assertEquals(Rational.ZERO,hyperchargeTrace);
        Rational cubic=Rational.ZERO;
        for(DixonMinimalLeftIdeal.State state:DixonMinimalLeftIdeal.model().states())
            cubic=cubic.add(state.hypercharge().pow(3));
        assertEquals(Rational.ZERO,cubic);
        int doublets=0;
        for(DixonMinimalLeftIdeal.State state:DixonMinimalLeftIdeal.model().states())
            if(state.t3().equals(Rational.of(1,2))) doublets++;
        assertEquals(4,doublets);
        assertEquals(BigInteger.ZERO,BigInteger.valueOf(doublets).mod(BigInteger.valueOf(2)));
    }
}
