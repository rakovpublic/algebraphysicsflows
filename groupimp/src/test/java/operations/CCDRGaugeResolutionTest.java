package operations;

import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import mathematics.numbers.DerivedGaugeObstruction;
import mathematics.numbers.DiracGaugeCompatibility;
import mathematics.numbers.DixonMinimalLeftIdeal;
import mathematics.numbers.FiniteDiracSpectralAction;
import mathematics.numbers.HistoricalIdealInvariance;
import mathematics.numbers.HistoricalWeakSector;
import mathematics.numbers.Rational;
import mathematics.numbers.RationalOctonionDerivations;
import mathematics.numbers.RestrictedColorStabilizer;
import mathematics.numbers.WittGaugeAction;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Exact audit of the historical CCDR gauge construction.
 *
 * <p>The tests recompute every claimed number from the actual restricted matrices. They
 * assert positive facts (independence, closure, structure constants, Jacobi, compactness,
 * Casimir multiplicities) and the negative facts (weak non-invariance, absence of a weak
 * doublet, failure of hypercharge to commute with the derived color action, and failure of
 * the color derivations to commute with the finite Dirac data). Any violation flips an
 * assertion.</p>
 */
public class CCDRGaugeResolutionTest {

    // ---------------------------------------------------------------- residuals

    @Test public void columnSpanAgreesWithTheOrthogonalResidual(){
        for(HistoricalIdealInvariance.GeneratorResidual residual:HistoricalIdealInvariance.model().residuals()){
            assertEquals(residual.invariant(),residual.columnSpanInvariant());
            if(residual.invariant()) assertEquals(16,residual.columnSpanRank());
            else assertTrue(residual.columnSpanRank()>16);
        }
    }

    @Test public void invarianceSurvivesAnInvertibleBasisChange(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        RationalMatrix basis=invariance.basis();
        Rational[][] sheared=new Rational[128][16];
        for(int column=0;column<16;column++) for(int row=0;row<128;row++) sheared[row][column]=basis.get(row,column);
        for(int row=0;row<128;row++) sheared[row][0]=sheared[row][0].add(sheared[row][1]);
        RationalMatrix changed=new RationalMatrix(sheared);
        RationalMatrix adjoint=RationalOctonionDerivations.quaternionAdjointGenerators().get(0);
        assertEquals(16,changed.rank());
        assertTrue(augmented(changed,adjoint)>16);
        assertTrue(augmented(basis,adjoint)>16);
    }

    @Test public void everyResidualHasTheReportedRankAndNorm(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        assertEquals(11,invariance.residuals().size());
        for(int index=0;index<8;index++){
            HistoricalIdealInvariance.GeneratorResidual residual=invariance.residuals().get(index);
            assertEquals("octonion-stabilizer",residual.family());
            assertEquals(0,residual.rank());
            assertEquals(Rational.ZERO,residual.frobeniusSquared());
            assertTrue(residual.invariant());
        }
        int[] failing={8,10};
        for(int index:failing){
            HistoricalIdealInvariance.GeneratorResidual residual=invariance.residuals().get(index);
            assertEquals("quaternion-adjoint",residual.family());
            assertEquals(8,residual.rank());
            assertEquals(Rational.of(16),residual.frobeniusSquared());
            assertFalse(residual.invariant());
            assertEquals(24,residual.columnSpanRank());
        }
        HistoricalIdealInvariance.GeneratorResidual centre=invariance.residuals().get(9);
        assertEquals(0,centre.rank());
        assertEquals(Rational.ZERO,centre.frobeniusSquared());
        assertEquals(Rational.of(32),invariance.totalFrobeniusSquared());
    }

    // ---------------------------------------------------------------- colour

    @Test public void colorGeneratorsAreIndependentAndClose(){
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        assertEquals(8,color.generators().size());
        assertEquals(8,color.independentCount());
        assertEquals(0,color.commutatorsOutsideSpan());
        assertTrue(color.closed());
        assertTrue(color.jacobi());
        System.out.println("COLOR independent="+color.independentCount()+" outside="+color.commutatorsOutsideSpan()
                +" jacobi="+color.jacobi()+" rank="+color.rank()+" center="+color.centerDimension());
    }

    @Test public void colorStructureConstantsReproduceTheCommutators(){
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        List<RationalMatrix> generators=color.generators();
        Rational[][][] f=color.structureConstants();
        int count=generators.size();
        for(int a=0;a<count;a++) for(int b=0;b<count;b++){
            RationalMatrix rebuilt=zero16();
            for(int c=0;c<count;c++) rebuilt=rebuilt.add(generators.get(c).scale(f[a][b][c]));
            assertEquals("["+a+","+b+"] must be reproduced by f",commutator(generators.get(a),generators.get(b)),rebuilt);
        }
        for(int a=0;a<count;a++) for(int b=0;b<count;b++) for(int c=0;c<count;c++)
            assertEquals("antisymmetry "+a+","+b+","+c,Rational.ZERO,f[a][b][c].add(f[b][a][c]));
        for(int a=0;a<count;a++) for(int b=0;b<count;b++) for(int c=0;c<count;c++) for(int d=0;d<count;d++){
            Rational sum=Rational.ZERO;
            for(int e=0;e<count;e++)
                sum=sum.add(f[a][b][e].multiply(f[e][c][d]))
                        .add(f[b][c][e].multiply(f[e][a][d]))
                        .add(f[c][a][e].multiply(f[e][b][d]));
            assertEquals("Jacobi "+a+","+b+","+c+","+d,Rational.ZERO,sum);
        }
        // Lower the third index with the invariant trace form. Compactness makes f_abc totally antisymmetric.
        RationalMatrix metric=color.traceForm();
        for(int a=0;a<count;a++) for(int b=0;b<count;b++) for(int c=0;c<count;c++){
            Rational lowered=Rational.ZERO;
            for(int d=0;d<count;d++) lowered=lowered.add(f[a][b][d].multiply(metric.get(d,c)));
            Rational cyclic=Rational.ZERO;
            for(int d=0;d<count;d++) cyclic=cyclic.add(f[b][c][d].multiply(metric.get(d,a)));
            assertEquals("total antisymmetry (cyclic) "+a+","+b+","+c,lowered,cyclic);
            Rational swap=Rational.ZERO;
            for(int d=0;d<count;d++) swap=swap.add(f[a][c][d].multiply(metric.get(d,b)));
            assertEquals("total antisymmetry (swap) "+a+","+b+","+c,lowered,swap.multiply(Rational.ONE.negate()));
        }
    }

    @Test public void colorTraceFormIsNegativeDefiniteCompactRealForm(){
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        RationalMatrix trace=color.traceForm();
        assertTrue("trace form must be a compact real form",color.compact());
        for(int a=0;a<8;a++) assertTrue("diagonal entry "+a+" must be negative",trace.get(a,a).signum()<0);
        assertEquals(8,trace.rows());
        assertEquals(8,trace.rank());
    }

    @Test public void colorCasimirHasTheExactMultiplicities(){
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        // On the real eight-dimensional octonion module: 0 (mult 2) and 4/3 (mult 6).
        assertEquals(2,color.octonionCasimir().eigenvalueMultiplicity(Rational.ZERO));
        assertEquals(6,color.octonionCasimir().eigenvalueMultiplicity(Rational.of(4,3)));
        assertEquals(Rational.of(8),color.octonionCasimir().trace());
        // On the real sixteen-dimensional occupation module: 0 (mult 4) and 2/3 (mult 12).
        assertEquals(4,color.casimir().eigenvalueMultiplicity(Rational.ZERO));
        assertEquals(12,color.casimir().eigenvalueMultiplicity(Rational.of(2,3)));
        assertEquals(Rational.of(8),color.casimir().trace());
        System.out.println("CAS8="+color.octonionCasimirEigenvalues()+" CAS16="+color.casimirEigenvalues());
    }

    @Test public void colorModuleIsTwoCopiesOfTheOctonionModule(){
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        assertTrue("adapted basis must split the ideal into two equal octonion copies",color.twoCopiesOfOctonionModule());
        assertEquals(24,color.commutantDimension());
        assertEquals(6,color.octonionCommutantDimension());
        assertEquals(2,color.fixedSubspaceDimension());
        assertEquals(0,color.centerDimension());
        assertEquals(2,color.rank());
    }

    @Test public void colorRestrictionIsConjugateUnderABasisChange(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        RationalMatrix basis=invariance.basis();
        RationalMatrix inverse=invariance.gramInverse();
        RationalMatrix generator=RationalOctonionDerivations.su3StabilizerCandidates().get(0);
        RationalMatrix restricted=restrict(basis,inverse,generator);
        // Shear the spanning set by an invertible integer matrix S acting on coordinates.
        Rational[][] entries=new Rational[16][16];
        for(int row=0;row<16;row++) for(int column=0;column<16;column++) entries[row][column]=row==column?Rational.ONE:Rational.ZERO;
        entries[0][1]=Rational.ONE;
        RationalMatrix shear=new RationalMatrix(entries);
        RationalMatrix changed=basis.multiply(shear);
        RationalMatrix changedInverse=changed.transpose().multiply(changed).inverse();
        RationalMatrix changedRestricted=restrict(changed,changedInverse,generator);
        assertEquals(shear.inverse().multiply(restricted).multiply(shear),changedRestricted);
        assertEquals(restricted.characteristicPolynomial(),changedRestricted.characteristicPolynomial());
    }

    // ---------------------------------------------------------------- weak

    @Test public void historicalWeakAdjointsDoNotMakeTheIdealAnSu2Module(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        assertEquals("quaternion-adjoint",invariance.residuals().get(8).family());
        assertFalse(invariance.residuals().get(8).columnSpanInvariant());
        assertTrue(invariance.residuals().get(9).columnSpanInvariant());
        assertFalse(invariance.residuals().get(10).columnSpanInvariant());
        assertFalse(WittGaugeAction.model().historicallyInvariant());
    }

    @Test public void weakAdjointSectorHasNoDoubletAndDoesNotPreserveTheIdeal(){
        HistoricalWeakSector weak=HistoricalWeakSector.model();
        assertEquals(3,weak.adjointGenerators().size());
        assertEquals(3,weak.leftGenerators().size());
        assertEquals(1,weak.commonKernelDimension());
        assertEquals(3,weak.adjointImageDimension());
        // ad(H) on H splits as the centre (0) plus the three imaginary quaternions (-8).
        assertEquals(1,weak.adjointCasimir().eigenvalueMultiplicity(Rational.ZERO));
        assertEquals(3,weak.adjointCasimir().eigenvalueMultiplicity(Rational.of(-8)));
        assertFalse("the adjoint action contains no spin-1/2 doublet",weak.adjointContainsDoublet());
        // Left multiplication is a single real four-dimensional spinor with Casimir -3.
        assertEquals(4,weak.leftCasimir().eigenvalueMultiplicity(Rational.of(-3)));
        assertTrue(weak.leftContainsDoublet());
        assertFalse("the left spinor has no trivial summand",weak.leftContainsSinglet());
        // Neither action preserves the proposed two-real-dimensional weak plane.
        assertFalse(weak.proposedPlaneInvariant());
        // The invariant closure of the ideal under either action is 32 real dimensions.
        assertEquals(32,weak.adjointClosureDimension());
        assertEquals(32,weak.leftClosureDimension());
        assertEquals(8,weak.proposedModuleComplexDimension());
        // Content of the larger invariant closures, fixed by the exact Casimir trace.
        // Adjoint: trace = -8 * (triplet dimension) = -192, so 24 triplet + 8 singlet real dimensions.
        assertEquals(Rational.of(-192),weak.adjointClosureCasimirTrace());
        assertEquals(24,weak.adjointClosureTripletDimension());
        assertEquals(8,weak.adjointClosureSingletDimension());
        // Left: trace = -3 * 32 = -96, so all 32 real dimensions are doublet-type and there is no singlet.
        assertEquals(Rational.of(-96),weak.leftClosureCasimirTrace());
        assertEquals(32,weak.leftClosureDoubletDimension());
        System.out.println("WEAK adjoint="+weak.adjointCasimirEigenvalues()+" left="+weak.leftCasimirEigenvalues()
                +" adjClosure="+weak.adjointClosureDimension()+" leftClosure="+weak.leftClosureDimension()
                +" adjTrace="+weak.adjointClosureCasimirTrace()+" leftTrace="+weak.leftClosureCasimirTrace());
    }

    @Test public void colorAndQuaternionLiftsCommuteOnTheDixonAlgebra(){
        List<RationalMatrix> color=RationalOctonionDerivations.su3StabilizerCandidates();
        List<RationalMatrix> weak=RationalOctonionDerivations.quaternionAdjointGenerators();
        for(RationalMatrix c:color) for(RationalMatrix w:weak)
            assertTrue("the two tensor factors commute on the 128-dimensional algebra",
                    commutator(WittGaugeAction.liftOctonion(c),WittGaugeAction.liftQuaternion(w)).equals(zero128()));
    }

    // ---------------------------------------------------------------- hypercharge

    @Test public void dictionaryHyperchargeDoesNotCommuteWithDerivedColor(){
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        RationalMatrix hypercharge=WittGaugeAction.model().hypercharge();
        assertEquals("seven of the eight color generators fail",7,color.hyperchargeNonCommutingCount());
        int failures=0;
        for(RationalMatrix generator:color.generators())
            if(!commutator(hypercharge,generator).equals(zero16())) failures++;
        assertEquals(7,failures);
        // The encoded dictionary traces are zero, but they are not anomalies of a derived joint representation.
        assertEquals(Rational.ZERO,hypercharge.trace());
        Rational cubic=Rational.ZERO;
        for(DixonMinimalLeftIdeal.State state:DixonMinimalLeftIdeal.model().states())
            cubic=cubic.add(state.hypercharge().pow(3));
        assertEquals(Rational.ZERO,cubic);
        assertFalse(DerivedGaugeObstruction.model().hyperchargeColorInvariant());
    }

    @Test public void colorCubicAnomalyVanishesButJointCancellationIsNotEstablished(){
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        assertTrue("the symmetrized cubic color anomaly Tr(T_a{T_b,T_c}) vanishes",color.cubicAnomalyVanishes());
        for(RationalMatrix generator:color.generators()) assertEquals(Rational.ZERO,generator.trace());
        DerivedGaugeObstruction obstruction=DerivedGaugeObstruction.model();
        assertFalse(obstruction.derivedAnomalyCancellationEstablished());
    }

    // ---------------------------------------------------------------- Dirac

    @Test public void diracColorActionIsNotCompatibleButWeakAdjointIs(){
        DiracGaugeCompatibility dirac=DiracGaugeCompatibility.model();
        assertEquals(5,dirac.colorGradingFailures());
        assertEquals(5,dirac.colorDiracFailures());
        assertEquals(5,dirac.colorRealStructureFailures());
        assertFalse("color derivations change the octonion grade and do not commute with gamma, D, or J",
                dirac.colorActionCompatibleWithDiracData());
        assertEquals(0,dirac.weakGradingFailures());
        assertEquals(0,dirac.weakDiracFailures());
        assertEquals(0,dirac.weakRealStructureFailures());
        assertTrue(dirac.weakAdjointCompatibleWithDiracData());
        assertEquals(5,dirac.higgsColorFailures());
        assertEquals(0,dirac.higgsWeakFailures());
        assertFalse(dirac.higgsCovariantUnderColor());
        assertTrue(dirac.higgsCovariantUnderWeakAdjoint());
        assertFalse(DerivedGaugeObstruction.model().higgsCovariantUnderDerivedGauge());
        assertTrue(dirac.higgsDirectionIsAnsatz());
    }

    @Test public void jointRepresentationIsFalsifiedOnTheOccupationModule(){
        DerivedGaugeObstruction obstruction=DerivedGaugeObstruction.model();
        assertTrue("the eight color generators do form su(3)",obstruction.colorIsSu3());
        assertEquals(8,obstruction.colorModuleComplexDimension());
        assertTrue(obstruction.colorModuleIsTwoCopiesOfOctonionModule());
        assertFalse(obstruction.weakAdjointHasDoublet());
        assertFalse(obstruction.weakPlaneInvariant());
        assertFalse(obstruction.hyperchargeColorInvariant());
        assertFalse(obstruction.jointRepresentationEstablished());
        assertFalse(obstruction.diracColorCompatible());
        assertTrue(obstruction.diracWeakCompatible());
        assertFalse(obstruction.higgsCovariantUnderDerivedGauge());
        assertEquals("FALSIFIED",obstruction.status());
    }

    @Test public void complexStructurePreservesTheIdealAndCommutesWithColor(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        assertEquals(16,invariance.complexStructureRank());
        assertEquals(8,invariance.complexDimension());
        RestrictedColorStabilizer color=RestrictedColorStabilizer.model();
        assertEquals(16,color.complexCentralizerDimension());
        // The physical module is therefore complex dimension eight, not the sixteen complex states of a generation.
        assertNotEquals(16,invariance.complexDimension());
    }

    @Test public void higgsDirectionIsStillAnAnsatzAndSpectralActionIsConditional(){
        FiniteDiracSpectralAction action=FiniteDiracSpectralAction.model();
        assertTrue(action.higgsDirectionIsAnsatz());
        assertFalse(DerivedGaugeObstruction.model().jointRepresentationEstablished());
    }

    // ---------------------------------------------------------------- helpers

    private static int augmented(RationalMatrix basis,RationalMatrix adjoint){
        Rational[][] rows=new Rational[128][32];
        for(int column=0;column<16;column++){
            RationalVector image=WittGaugeAction.applyQuaternion(basis.column(column),adjoint);
            for(int row=0;row<128;row++){
                rows[row][column]=basis.get(row,column);
                rows[row][16+column]=image.get(row);
            }
        }
        return new RationalMatrix(rows).rank();
    }

    private static RationalMatrix restrict(RationalMatrix basis,RationalMatrix inverse,RationalMatrix generator){
        Rational[][] entries=new Rational[16][16];
        for(int column=0;column<16;column++){
            RationalVector image=WittGaugeAction.applyOctonion(basis.column(column),generator);
            RationalVector coordinates=inverse.multiply(basis.transpose().multiply(image));
            for(int row=0;row<16;row++) entries[row][column]=coordinates.get(row);
        }
        return new RationalMatrix(entries);
    }

    private static RationalMatrix commutator(RationalMatrix a,RationalMatrix b){
        return a.multiply(b).add(b.multiply(a).scale(Rational.ONE.negate()));
    }

    private static RationalMatrix zero16(){
        Rational[][] entries=new Rational[16][16];
        for(int row=0;row<16;row++) for(int column=0;column<16;column++) entries[row][column]=Rational.ZERO;
        return new RationalMatrix(entries);
    }

    private static RationalMatrix zero128(){
        Rational[][] entries=new Rational[128][128];
        for(int row=0;row<128;row++) for(int column=0;column<128;column++) entries[row][column]=Rational.ZERO;
        return new RationalMatrix(entries);
    }
}
