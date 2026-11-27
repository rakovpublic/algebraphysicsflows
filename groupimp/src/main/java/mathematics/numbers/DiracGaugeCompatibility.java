package mathematics.numbers;

import mathematics.linear.RationalMatrix;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Exact compatibility of the historical gauge generators with the finite Dirac data.
 * The color derivations act on the octonion grade and therefore do not commute with
 * gamma_F, D_F, or J_F. The quaternion adjoints act only on the quaternion factor and
 * do commute with all three, but they carry no doublet. The block-odd Higgs direction
 * is covariant under the adjoints and not under the color derivations.
 */
public final class DiracGaugeCompatibility implements Serializable {
    private static final long serialVersionUID=1L;
    private static final DiracGaugeCompatibility MODEL=compute();
    private final int colorGradingFailures;
    private final int colorDiracFailures;
    private final int colorRealStructureFailures;
    private final int weakGradingFailures;
    private final int weakDiracFailures;
    private final int weakRealStructureFailures;
    private final int higgsColorFailures;
    private final int higgsWeakFailures;

    private DiracGaugeCompatibility(int colorGradingFailures,int colorDiracFailures,int colorRealStructureFailures,
            int weakGradingFailures,int weakDiracFailures,int weakRealStructureFailures,
            int higgsColorFailures,int higgsWeakFailures){
        this.colorGradingFailures=colorGradingFailures;
        this.colorDiracFailures=colorDiracFailures;
        this.colorRealStructureFailures=colorRealStructureFailures;
        this.weakGradingFailures=weakGradingFailures;
        this.weakDiracFailures=weakDiracFailures;
        this.weakRealStructureFailures=weakRealStructureFailures;
        this.higgsColorFailures=higgsColorFailures;
        this.higgsWeakFailures=higgsWeakFailures;
    }

    public static DiracGaugeCompatibility model(){return MODEL;}
    public int colorGradingFailures(){return colorGradingFailures;}
    public int colorDiracFailures(){return colorDiracFailures;}
    public int colorRealStructureFailures(){return colorRealStructureFailures;}
    public int weakGradingFailures(){return weakGradingFailures;}
    public int weakDiracFailures(){return weakDiracFailures;}
    public int weakRealStructureFailures(){return weakRealStructureFailures;}
    public int higgsColorFailures(){return higgsColorFailures;}
    public int higgsWeakFailures(){return higgsWeakFailures;}
    public boolean colorActionCompatibleWithDiracData(){
        return colorGradingFailures==0&&colorDiracFailures==0&&colorRealStructureFailures==0;
    }
    public boolean weakAdjointCompatibleWithDiracData(){
        return weakGradingFailures==0&&weakDiracFailures==0&&weakRealStructureFailures==0;
    }
    public boolean higgsCovariantUnderColor(){return higgsColorFailures==0;}
    public boolean higgsCovariantUnderWeakAdjoint(){return higgsWeakFailures==0;}
    public boolean higgsDirectionIsAnsatz(){return FiniteDiracSpectralAction.model().higgsDirectionIsAnsatz();}

    private static DiracGaugeCompatibility compute(){
        FiniteDiracSpectralAction action=FiniteDiracSpectralAction.model();
        List<RationalMatrix> colorLifts=liftAll(RationalOctonionDerivations.su3StabilizerCandidates(),true);
        List<RationalMatrix> weakLifts=liftAll(RationalOctonionDerivations.quaternionAdjointGenerators(),false);
        return new DiracGaugeCompatibility(
                failures(action.gamma(),colorLifts),failures(action.dirac(),colorLifts),failures(action.realStructure(),colorLifts),
                failures(action.gamma(),weakLifts),failures(action.dirac(),weakLifts),failures(action.realStructure(),weakLifts),
                failures(action.higgsDirection(),colorLifts),failures(action.higgsDirection(),weakLifts));
    }
    private static List<RationalMatrix> liftAll(List<RationalMatrix> generators,boolean octonion){
        List<RationalMatrix> lifts=new ArrayList<RationalMatrix>();
        for(RationalMatrix generator:generators)
            lifts.add(octonion?WittGaugeAction.liftOctonion(generator):WittGaugeAction.liftQuaternion(generator));
        return lifts;
    }
    private static int failures(RationalMatrix target,List<RationalMatrix> lifts){
        int count=0;
        for(RationalMatrix lift:lifts) if(!commutator(target,lift).equals(zero())) count++;
        return count;
    }
    private static RationalMatrix commutator(RationalMatrix left,RationalMatrix right){
        return left.multiply(right).add(right.multiply(left).scale(Rational.ONE.negate()));
    }
    private static RationalMatrix zero(){
        Rational[][] rows=new Rational[128][128];
        for(int row=0;row<128;row++) for(int column=0;column<128;column++) rows[row][column]=Rational.ZERO;
        return new RationalMatrix(rows);
    }
}
