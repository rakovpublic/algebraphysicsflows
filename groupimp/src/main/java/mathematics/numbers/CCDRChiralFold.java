package mathematics.numbers;

import java.io.Serializable;
import java.math.BigDecimal;

/** Exact sign bookkeeping for the terminal 5-to-4 fold prediction in Synthesis v3.9. */
public final class CCDRChiralFold implements Serializable {
    private static final long serialVersionUID=1L;
    private final int epsilon5;
    private final int foliationArrow;
    private final int simplexOrientation;
    private final int cmbParityOddSign;

    public CCDRChiralFold(int epsilon5,int foliationArrow,int simplexOrientation,int cmbParityOddSign) {
        this.epsilon5=sign(epsilon5,"epsilon5");
        this.foliationArrow=sign(foliationArrow,"foliation arrow");
        this.simplexOrientation=sign(simplexOrientation,"SM-D11 simplex orientation");
        this.cmbParityOddSign=sign(cmbParityOddSign,"P-A30 parity-odd CMB sign");
    }
    private static int sign(int value,String name) {
        if(value!=1&&value!=-1)throw new IllegalArgumentException(name+" must be +1 or -1");
        return value;
    }
    /** Documented fold prediction: chirality is sign(epsilon_5 times foliation arrow). */
    public int chiralitySign(){return epsilon5*foliationArrow;}
    /** SM-D11 requires the oriented simplex asymmetry to select the same chirality. */
    public boolean smD11Consistent(){return simplexOrientation==chiralitySign();}
    /** P-A30 predicts the parity-odd CMB sign agrees with the weak chirality sign. */
    public boolean pA30Consistent(){return cmbParityOddSign==chiralitySign();}
    /** CL9 is the three-way sign agreement of the fold, simplex, and CMB prediction. */
    public boolean cl9Consistent(){return smD11Consistent()&&pA30Consistent();}
    public boolean isLeftChiral(){return chiralitySign()>0;}
    public boolean isRightChiral(){return chiralitySign()<0;}
    /** Selects the corresponding crossed-product group-parity block as a candidate chirality projection. */
    public RationalDixonZ2 projectDixonParity(RationalDixonZ2 value){return isLeftChiral()?value.evenPart():value.oddPart();}
    /** Orients an exact cyclotomic CP phase by the signed terminal-fold chirality. */
    public RationalCyclotomic orientCyclotomicPhase(RationalCyclotomic phase){return isLeftChiral()?phase:phase.conjugate();}
    /** Signed SM-D8 sine proxy from the oriented phase; the model's nu remains an explicit input. */
    public BigDecimal smD8SignedSine(RationalCyclotomic phase,int precision){return orientCyclotomicPhase(phase).evaluate(precision).imaginary;}
    /** Checks the stated delta_CP ~ sin(theta_fold) ~ nu relation at caller-selected precision/tolerance. */
    public boolean smD8MatchesNu(RationalCyclotomic phase,BigDecimal nu,BigDecimal tolerance,int precision){
        if(tolerance.signum()<0)throw new IllegalArgumentException("SM-D8 tolerance must be nonnegative");
        return smD8SignedSine(phase,precision).subtract(nu).abs().compareTo(tolerance)<=0;
    }
    public int epsilon5(){return epsilon5;}
    public int foliationArrow(){return foliationArrow;}
    public int simplexOrientation(){return simplexOrientation;}
    public int cmbParityOddSign(){return cmbParityOddSign;}
}
