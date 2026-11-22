package operations;

import mathematics.numbers.*;
import org.junit.Test;

import java.math.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Empirical-input audit for the CCDR algebra carriers. Measured values are kept
 * separate from model anchors: a test passes when the arithmetic is reproducible,
 * including when the comparison exposes tension or an unsupported identification.
 * PDG inputs are the 2025 update (tau and lepton masses, CKM, and weak angle).
 */
public class CCDRPhysicalDataValidationTest {
    private static final int PRECISION=40;
    private static final BigDecimal TWO_THIRDS=new BigDecimal("2").divide(new BigDecimal("3"),new MathContext(PRECISION,RoundingMode.HALF_EVEN));
    private static BigDecimal bd(String s){return new BigDecimal(s);}

    @Test public void pdgLeptonMassesGiveKoideNearAndBracketedAtTwoThirds(){
        BigDecimal electron=bd("0.51099895000"),muon=bd("105.6583755");
        BigDecimal historical=HighPrecisionAlgebraMetrics.koide(electron,muon,bd("1776.86"),PRECISION);
        BigDecimal tau=bd("1776.93"),tauError=bd("0.09");
        BigDecimal current=HighPrecisionAlgebraMetrics.koide(electron,muon,tau,PRECISION);
        BigDecimal low=HighPrecisionAlgebraMetrics.koide(electron,muon,tau.subtract(tauError),PRECISION);
        BigDecimal high=HighPrecisionAlgebraMetrics.koide(electron,muon,tau.add(tauError),PRECISION);
        assertTrue(historical.subtract(TWO_THIRDS).abs().compareTo(bd("1.5e-5"))<0);
        assertTrue(current.subtract(TWO_THIRDS).abs().compareTo(bd("1.5e-5"))<0);
        assertTrue("The PDG tau 1-sigma interval should bracket 2/3",low.compareTo(TWO_THIRDS)<=0&&high.compareTo(TWO_THIRDS)>=0);
        System.out.println("PDG Koide oldTau="+historical+" currentTau="+current+" tau1sigma=["+low+", "+high+"] target="+TWO_THIRDS+" (bracket)");
    }

    @Test public void measuredCkmPhaseAndCyclotomicPhaseAreDistinctBenchmarks(){
        BigDecimal s12=bd("0.22501"),s23=bd("0.04183"),s13=bd("0.003732");
        BigDecimal c12=sqrt(BigDecimal.ONE.subtract(s12.multiply(s12)),PRECISION),
                c23=sqrt(BigDecimal.ONE.subtract(s23.multiply(s23)),PRECISION),
                c13=sqrt(BigDecimal.ONE.subtract(s13.multiply(s13)),PRECISION);
        BigDecimal delta=bd("1.147"),jObserved=bd("3.12e-5"),jError=bd("0.13e-5");
        BigDecimal measured=HighPrecisionAlgebraMetrics.ckmJarlskog(s12,s23,s13,c12,c23,c13,delta,PRECISION);
        BigDecimal measuredResidual=measured.subtract(jObserved).abs();
        assertTrue("Measured CKM inputs should reproduce the PDG J central value within its quoted band",measuredResidual.compareTo(jError)<0);

        RationalCyclotomic zeta12=RationalCyclotomic.zeta(12);
        BigDecimal cyclotomic=HighPrecisionAlgebraMetrics.ckmJarlskog(s12,s23,s13,c12,c23,c13,zeta12,PRECISION);
        assertTrue("The Q(zeta12) pi/6 phase is not the measured CKM phase",cyclotomic.subtract(jObserved).abs().compareTo(jError)>0);
        BigDecimal nu=bd("5.08e-3"),nuError=bd("1.2e-3");
        BigDecimal sinMeasured=RationalCyclotomic.sinRadians(delta,PRECISION);
        BigDecimal amplified=nu.multiply(bd("4")); // N-4 for N=8, as supplied in the brief.
        assertTrue("sin(delta_CP) and nu are not equal within the stated nu band",sinMeasured.subtract(nu).abs().compareTo(nuError)>0);
        assertTrue("The proposed (N-4) nu amplitude still does not match sin(delta_CP)",sinMeasured.subtract(amplified).abs().compareTo(nuError.multiply(bd("4")))>0);
        System.out.println("CKM J(PDG angles,delta)="+measured+" J(PDG fit)="+jObserved+" +/- "+jError+
                " J(zeta12)="+cyclotomic+" sin(delta)="+sinMeasured+" nu="+nu+" +/- "+nuError+" (model relation tension)");
    }

    @Test public void exactWeylChargesAndGUTTraceRatioAreNotMzWeakAngle(){
        List<StandardModelWeylGeneration.State> states=StandardModelWeylGeneration.states();
        assertEquals(16,states.size());
        String[] names={"nu_L","e_L","u_L^r","u_L^g","u_L^b","d_L^r","d_L^g","d_L^b","e_R^c","nu_R^c",
                "u_R^{c,r}","u_R^{c,g}","u_R^{c,b}","d_R^{c,r}","d_R^{c,g}","d_R^{c,b}"};
        Rational[] charges={Rational.ZERO,Rational.of(-1),Rational.of(2,3),Rational.of(2,3),Rational.of(2,3),
                Rational.of(-1,3),Rational.of(-1,3),Rational.of(-1,3),Rational.ONE,Rational.ZERO,
                Rational.of(-2,3),Rational.of(-2,3),Rational.of(-2,3),Rational.of(1,3),Rational.of(1,3),Rational.of(1,3)};
        for(int i=0;i<states.size();i++){
            assertEquals(names[i],states.get(i).name());
            assertEquals("Q=T3+Y for "+names[i],charges[i],states.get(i).electricCharge());
        }
        Rational ratio=StandardModelWeylGeneration.treeLevelGUTWeinbergRatio();
        assertEquals(Rational.of(3,8),ratio);
        BigDecimal pdgMz=bd("0.23122"),error=bd("0.00006"),gut=new BigDecimal(ratio.numerator()).divide(new BigDecimal(ratio.denominator()),new MathContext(PRECISION,RoundingMode.HALF_EVEN));
        assertTrue("3/8 at the GUT scale is distinct from the PDG MSbar(MZ) value",gut.subtract(pdgMz).abs().compareTo(error)>0);
        assertTrue(StandardModelWeylGeneration.anomalyFreeHyperchargeSignature());
        System.out.println("Weyl states="+states.size()+" Tr(T3^2)="+StandardModelWeylGeneration.t3SquaredTrace()+
                " Tr(Q^2)="+StandardModelWeylGeneration.electricChargeSquaredTrace()+" GUT ratio="+ratio+
                " PDG MSbar(MZ)="+pdgMz+" +/- "+error+" (different scales/schemes)");
    }

    @Test public void rankEightBsmMapAndDarkMatterEstimatesAreAuditableInputs(){
        RationalTwistedLatticeAlgebra.Exponent odd=new RationalTwistedLatticeAlgebra.Exponent(1,0,0,0,0,0,0,0);
        RationalTwistedLatticeAlgebra state=RationalTwistedLatticeAlgebra.basis(8,odd,RationalQuaternion.ONE);
        assertTrue(state.hasOddFirstCoordinateSupport());
        RationalTwistedLatticeAlgebra image=state.darkMatterEndomorphism();
        assertEquals(8,image.rank());assertEquals(BigInteger.valueOf(2),image.valuation().coordinate(0));assertFalse(image.hasOddFirstCoordinateSupport());

        BigDecimal nu=bd("5.08e-3"),mass=bd("100"),splitting=nu.multiply(mass);
        assertEquals(0,bd("0.508").compareTo(splitting));
        BigDecimal kappaSquared=nu.multiply(nu),effective=bd("5e-49"),referenceFloor=bd("1e-47");
        BigDecimal impliedUnsuppressed=effective.divide(kappaSquared,new MathContext(PRECISION,RoundingMode.HALF_EVEN));
        assertTrue(effective.compareTo(referenceFloor)<0);
        assertTrue("The effective cross section is only a conditional estimate; sigma0 is an inferred input",impliedUnsuppressed.compareTo(BigDecimal.ZERO)>0);
        System.out.println("BSM1 rank="+image.rank()+" odd residue -> exponent "+image.valuation()+"; assumed nu="+nu+
                " splitting at 100 GeV="+splitting+" GeV; assumed sigmaEff="+effective+" cm^2 < approximate reference "+referenceFloor+
                " cm^2; implied sigma0="+impliedUnsuppressed+" cm^2 (not measured)");
    }

    @Test public void foldSignsAndVoidDepthDoNotEstablishTheClaimedObservationalIdentifications(){
        // The parity-odd weak sign is an observed convention; epsilon5 and foliation direction remain model choices.
        CCDRChiralFold choiceA=new CCDRChiralFold(1,-1,-1,-1),choiceB=new CCDRChiralFold(-1,1,-1,-1);
        assertTrue(choiceA.isRightChiral());assertTrue(choiceA.cl9Consistent());
        assertTrue(choiceB.isRightChiral());assertTrue(choiceB.cl9Consistent());
        assertEquals(choiceA.chiralitySign(),choiceB.chiralitySign());
        int n=8,depth=(n-4)*(n-4);BigDecimal claimedKurtosis=bd("4.45"),claimedError=bd("0.20");
        assertEquals(16,depth);
        assertTrue("A dimensionless cascade factor is not a prediction of the kurtosis observable",bd(Integer.toString(depth)).subtract(claimedKurtosis).abs().compareTo(claimedError)>0);
        System.out.println("P-A30/SM-D11 sign=-1 is consistent for multiple (epsilon5,arrow) pairs; (N-4)^2="+depth+
                " versus unverified VAST kurtosis anchor="+claimedKurtosis+" +/- "+claimedError+" (no derived mapping)");
    }

    private static BigDecimal sqrt(BigDecimal value,int precision){
        MathContext mc=new MathContext(precision+8,RoundingMode.HALF_EVEN);
        BigDecimal x=new BigDecimal(Math.sqrt(value.doubleValue()),mc);
        BigDecimal tolerance=BigDecimal.ONE.scaleByPowerOfTen(-precision-2);
        for(int i=0;i<precision*2;i++){
            BigDecimal next=x.add(value.divide(x,mc),mc).divide(BigDecimal.valueOf(2),mc);
            if(next.subtract(x).abs().compareTo(tolerance)<0)return next;
            x=next;
        }
        return x;
    }
}
