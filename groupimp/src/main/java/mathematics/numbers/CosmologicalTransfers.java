package mathematics.numbers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Algebraic pieces of the cosmological transfers, kept separate from catalogue inputs.
 * The raw sheet kurtosis is not rescaled to the 7.2 benchmark.
 */
public final class CosmologicalTransfers implements Serializable {
    private static final long serialVersionUID=1L;
    public static final Rational NU_NUMERATOR_PLACEHOLDER=Rational.of(508,100000);
    public static final BigDecimal QUOTED_SHEET_KURTOSIS=new BigDecimal("7.2");
    public static final BigDecimal QUOTED_SHEET_UNCERTAINTY=new BigDecimal("0.6");
    public static final BigDecimal CATALOGUE_NU=new BigDecimal("0.00508");
    public static final BigDecimal CATALOGUE_NU_UNCERTAINTY=new BigDecimal("0.0012");
    public static final BigDecimal CATALOGUE_WALL_TO_GRAIN=new BigDecimal("0.40");
    public static final BigDecimal CATALOGUE_WALL_UNCERTAINTY=new BigDecimal("0.05");

    private CosmologicalTransfers(){}

    public static int algebraicDepth(int rank){
        if(rank<4) throw new IllegalArgumentException("Rank must be at least 4");
        return (rank-4)*(rank-4);
    }
    /** Exact rational (N-4)^2 at N=8 is 16. The coefficient 16/6 of a unit ratio is 8/3. */
    public static Rational depthOverSix(int rank){return Rational.of(algebraicDepth(rank),6);}

    public static BigDecimal rawExcess(BigDecimal nu,BigDecimal wallToGrain,int rank,int precision){
        MathContext mc=context(precision);
        BigDecimal pi2=OneLoopRGEFlow.pi(mc).pow(2,mc);
        BigDecimal ratio2=wallToGrain.multiply(wallToGrain,mc);
        return nu.multiply(pi2,mc).divide(BigDecimal.valueOf(6),mc).multiply(ratio2,mc).multiply(BigDecimal.valueOf(algebraicDepth(rank)),mc);
    }
    public static BigDecimal rawSheetKurtosis(BigDecimal nu,BigDecimal wallToGrain,int rank,int precision){
        return BigDecimal.valueOf(3).add(rawExcess(nu,wallToGrain,rank,precision),context(precision));
    }
    /**
     * Factor that would turn the raw excess into the quoted 7.2-3 excess.
     * It is an external normalization audit, not an algebraic output.
     */
    public static BigDecimal quotedNormalizationFactor(int precision){
        MathContext mc=context(precision);
        BigDecimal raw=rawExcess(CATALOGUE_NU,CATALOGUE_WALL_TO_GRAIN,8,precision+4);
        BigDecimal quotedExcess=QUOTED_SHEET_KURTOSIS.subtract(BigDecimal.valueOf(3),mc);
        return quotedExcess.divide(raw,mc);
    }
    public static BigDecimal propagatedUncertainty(int precision){
        MathContext mc=context(precision);
        BigDecimal nu=CATALOGUE_NU,ratio=CATALOGUE_WALL_TO_GRAIN,depth=BigDecimal.valueOf(16);
        BigDecimal pi2=OneLoopRGEFlow.pi(mc).pow(2,mc);
        BigDecimal coefficient=nu.multiply(pi2,mc).divide(BigDecimal.valueOf(6),mc);
        BigDecimal ratioTerm=BigDecimal.valueOf(2).multiply(coefficient,mc).multiply(ratio,mc).multiply(depth,mc).multiply(CATALOGUE_WALL_UNCERTAINTY,mc).abs();
        BigDecimal nuTerm=rawExcess(BigDecimal.ONE,ratio,8,precision+4).multiply(CATALOGUE_NU_UNCERTAINTY,mc).abs();
        return ratioTerm.add(nuTerm,mc);
    }
    public static String kurtosisWindow(BigDecimal value){
        if(value.compareTo(new BigDecimal("6.4"))>0&&value.compareTo(new BigDecimal("8.0"))<0) return "PASS_2SIGMA";
        if(value.compareTo(new BigDecimal("5.5"))<0||value.compareTo(new BigDecimal("9.0"))>0) return "FAIL_3SIGMA";
        return "INCONCLUSIVE";
    }

    public static Rational[] anisotropyAxis(){return new Rational[]{Rational.ZERO,Rational.ZERO,Rational.ONE};}
    public static Rational[] parityAxis(){return anisotropyAxis();}
    public static Rational dot(Rational[] left,Rational[] right){
        return left[0].multiply(right[0]).add(left[1].multiply(right[1])).add(left[2].multiply(right[2]));
    }
    public static boolean exactlyCollinear(Rational[] left,Rational[] right){
        Rational[] cross={
                left[1].multiply(right[2]).subtract(left[2].multiply(right[1])),
                left[2].multiply(right[0]).subtract(left[0].multiply(right[2])),
                left[0].multiply(right[1]).subtract(left[1].multiply(right[0]))};
        return cross[0].signum()==0&&cross[1].signum()==0&&cross[2].signum()==0;
    }
    /** External axes. A robust axis with absolute cosine below the 3σ cosine threshold fails. */
    public static String alignmentStatus(BigDecimal absoluteCosine,BigDecimal threeSigmaCosine){
        if(absoluteCosine.compareTo(threeSigmaCosine)<0) return "FAIL_3SIGMA";
        return "CONSISTENT";
    }

    /** Algebraic eigenvalue of the first-coordinate doubling, distinct from the catalogue nu. */
    public static Rational algebraicDoubling(){return Rational.of(2);}
    public static BigDecimal phenomenologicalSplitting(BigDecimal nu,BigDecimal darkMass){return nu.multiply(darkMass);}

    private static MathContext context(int precision){return new MathContext(precision,RoundingMode.HALF_EVEN);}
}
