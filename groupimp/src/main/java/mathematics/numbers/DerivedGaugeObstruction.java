package mathematics.numbers;

import java.io.Serializable;

/**
 * Aggregated exact status of the historical gauge derivation. The color stabilizer
 * closes as a compact su(3) and its Casimir is 4/3 on the six-dimensional octonion
 * module, but the proposed occupation module is complex dimension 8, the adjoint weak
 * action has no doublet, the dictionary hypercharge is not color invariant, and the
 * color derivations do not commute with the finite Dirac data. The joint Standard
 * Model representation is therefore falsified on this module.
 */
public final class DerivedGaugeObstruction implements Serializable {
    private static final long serialVersionUID=1L;
    private static final DerivedGaugeObstruction MODEL=new DerivedGaugeObstruction();
    private final RestrictedColorStabilizer color;
    private final HistoricalWeakSector weak;
    private final HistoricalIdealInvariance invariance;
    private final DiracGaugeCompatibility dirac;

    private DerivedGaugeObstruction(){
        this.color=RestrictedColorStabilizer.model();
        this.weak=HistoricalWeakSector.model();
        this.invariance=HistoricalIdealInvariance.model();
        this.dirac=DiracGaugeCompatibility.model();
    }

    public static DerivedGaugeObstruction model(){return MODEL;}
    public RestrictedColorStabilizer color(){return color;}
    public HistoricalWeakSector weak(){return weak;}
    public HistoricalIdealInvariance invariance(){return invariance;}
    public DiracGaugeCompatibility dirac(){return dirac;}

    public boolean colorIsSu3(){
        return color.independentCount()==8&&color.closed()&&color.jacobi()&&color.compact();
    }
    public int colorModuleComplexDimension(){return invariance.complexDimension();}
    public boolean colorModuleIsTwoCopiesOfOctonionModule(){return color.twoCopiesOfOctonionModule();}
    public boolean weakAdjointHasDoublet(){return weak.adjointContainsDoublet();}
    public boolean weakPlaneInvariant(){return weak.proposedPlaneInvariant();}
    public boolean hyperchargeColorInvariant(){return color.hyperchargeNonCommutingCount()==0;}
    public boolean jointRepresentationEstablished(){
        return colorIsSu3()&&weakAdjointHasDoublet()&&weakPlaneInvariant()&&hyperchargeColorInvariant();
    }
    public boolean colorAnomalyVanishes(){return color.cubicAnomalyVanishes();}
    public boolean derivedAnomalyCancellationEstablished(){
        return jointRepresentationEstablished()&&colorAnomalyVanishes();
    }
    public boolean diracColorCompatible(){return dirac.colorActionCompatibleWithDiracData();}
    public boolean diracWeakCompatible(){return dirac.weakAdjointCompatibleWithDiracData();}
    public boolean higgsCovariantUnderDerivedGauge(){
        return dirac.higgsCovariantUnderColor()&&dirac.higgsCovariantUnderWeakAdjoint();
    }
    /** The dictionary traces are the encoded ones, not anomalies of a derived joint representation. */
    public Rational dictionaryHyperchargeTrace(){return WittGaugeAction.model().hypercharge().trace();}
    public Rational dictionaryCubicHyperchargeTrace(){
        Rational sum=Rational.ZERO;
        for(DixonMinimalLeftIdeal.State state:DixonMinimalLeftIdeal.model().states())
            sum=sum.add(state.hypercharge().pow(3));
        return sum;
    }
    public String status(){
        return jointRepresentationEstablished()?"VERIFIED":"FALSIFIED";
    }
}
