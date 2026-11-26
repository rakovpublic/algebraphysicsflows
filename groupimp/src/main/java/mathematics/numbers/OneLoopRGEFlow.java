package mathematics.numbers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * One-loop Standard Model running from a unification boundary sin^2 theta_W = 3/8
 * down to M_Z. The electromagnetic coupling at M_Z is an external scale; the algebra
 * does not derive it. b1 is GUT-normalized.
 */
public final class OneLoopRGEFlow implements Serializable {
    private static final long serialVersionUID=1L;
    public static final BigDecimal Z_MASS_GEV=new BigDecimal("91.1876");
    /** PDG-scale MSbar electromagnetic inverse coupling, used only as the second boundary datum. */
    public static final BigDecimal ELECTROMAGNETIC_INVERSE=new BigDecimal("127.952");
    public static final BigDecimal TARGET_SIN_SQUARED=new BigDecimal("0.23122");
    public static final Rational B1=Rational.of(41,10);
    public static final Rational B2=Rational.of(-19,6);
    public static final Rational B3=Rational.of(-7);

    private OneLoopRGEFlow(){}

    public static final class Boundary implements Serializable {
        private static final long serialVersionUID=1L;
        public final BigDecimal compactificationGeV;
        public final BigDecimal alphaFold;
        public final BigDecimal sinSquaredAtZ;
        public final BigDecimal log10Compactification;
        public Boundary(BigDecimal compactificationGeV,BigDecimal alphaFold,BigDecimal sinSquaredAtZ,BigDecimal log10Compactification){
            this.compactificationGeV=compactificationGeV;this.alphaFold=alphaFold;this.sinSquaredAtZ=sinSquaredAtZ;this.log10Compactification=log10Compactification;
        }
        @Override public String toString(){
            return "Lambda="+compactificationGeV.toPlainString()+" GeV, log10="+log10Compactification.toPlainString()
                    +", alpha_fold="+alphaFold.toPlainString()+", sin^2(MZ)="+sinSquaredAtZ.toPlainString();
        }
    }

    public static Boundary solve(int precision){
        if(precision<8||precision>80) throw new IllegalArgumentException("Precision must be between 8 and 80");
        MathContext mc=new MathContext(precision+8,RoundingMode.HALF_EVEN);
        BigDecimal pi=pi(mc);
        BigDecimal twoPi=pi.multiply(BigDecimal.valueOf(2),mc);
        BigDecimal sin2=TARGET_SIN_SQUARED;
        BigDecimal cos2=BigDecimal.ONE.subtract(sin2,mc);
        BigDecimal alphaEm=BigDecimal.ONE.divide(ELECTROMAGNETIC_INVERSE,mc);
        BigDecimal alpha2=alphaEm.divide(sin2,mc);
        BigDecimal alphaY=alphaEm.divide(cos2,mc);
        BigDecimal alpha1=alphaY.multiply(new BigDecimal("5"),mc).divide(new BigDecimal("3"),mc);
        BigDecimal inverse1=BigDecimal.ONE.divide(alpha1,mc);
        BigDecimal inverse2=BigDecimal.ONE.divide(alpha2,mc);
        BigDecimal b1=decimal(B1,mc),b2=decimal(B2,mc);
        BigDecimal logLambdaOverZ=twoPi.multiply(inverse1.subtract(inverse2,mc),mc).divide(b1.subtract(b2,mc),mc);
        BigDecimal lambda=Z_MASS_GEV.multiply(exp(logLambdaOverZ,mc),mc);
        BigDecimal inverseFold=inverse1.subtract(b1.divide(twoPi,mc).multiply(logLambdaOverZ,mc),mc);
        BigDecimal alphaFold=BigDecimal.ONE.divide(inverseFold,mc);
        BigDecimal reconstructed=reconstructSinSquared(alphaFold,logLambdaOverZ,b1,b2,twoPi,mc);
        MathContext out=new MathContext(precision,RoundingMode.HALF_EVEN);
        return new Boundary(lambda.round(out),alphaFold.round(out),reconstructed.round(out),log10(lambda,mc).round(out));
    }

    /** Prompt normalization sin^2 = alpha1 / (alpha1 + 5/3 alpha2) after running GUT-normalized alpha1. */
    static BigDecimal reconstructSinSquared(BigDecimal alphaFold,BigDecimal logLambdaOverZ,BigDecimal b1,BigDecimal b2,BigDecimal twoPi,MathContext mc){
        BigDecimal inverseFold=BigDecimal.ONE.divide(alphaFold,mc);
        BigDecimal inverse1=inverseFold.add(b1.divide(twoPi,mc).multiply(logLambdaOverZ,mc),mc);
        BigDecimal inverse2=inverseFold.add(b2.divide(twoPi,mc).multiply(logLambdaOverZ,mc),mc);
        BigDecimal alpha1=BigDecimal.ONE.divide(inverse1,mc);
        BigDecimal alpha2=BigDecimal.ONE.divide(inverse2,mc);
        BigDecimal alphaY=alpha1.multiply(new BigDecimal("3"),mc).divide(new BigDecimal("5"),mc);
        return alphaY.divide(alphaY.add(alpha2,mc),mc);
    }

    private static BigDecimal decimal(Rational value,MathContext mc){
        return new BigDecimal(value.numerator()).divide(new BigDecimal(value.denominator()),mc);
    }
    static BigDecimal pi(MathContext mc){
        return arctanInverse(5,mc).multiply(BigDecimal.valueOf(16),mc).subtract(arctanInverse(239,mc).multiply(BigDecimal.valueOf(4),mc),mc);
    }
    private static BigDecimal arctanInverse(int inverse,MathContext mc){
        BigDecimal x=BigDecimal.ONE.divide(BigDecimal.valueOf(inverse),mc),x2=x.multiply(x,mc),term=x,sum=x;
        for(int k=1;k<mc.getPrecision()*2;k++){
            term=term.multiply(x2,mc).negate();
            BigDecimal add=term.divide(BigDecimal.valueOf(2L*k+1),mc);
            sum=sum.add(add,mc);
            if(add.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0) break;
        }
        return sum;
    }
    static BigDecimal exp(BigDecimal value,MathContext mc){
        int n=value.setScale(0,RoundingMode.DOWN).intValue();
        BigDecimal fraction=value.subtract(new BigDecimal(n),mc);
        return seriesExp(fraction,mc).multiply(expInteger(n,mc),mc);
    }
    private static BigDecimal seriesExp(BigDecimal value,MathContext mc){
        BigDecimal series=BigDecimal.ONE,term=BigDecimal.ONE;
        for(int k=1;k<mc.getPrecision()*2;k++){
            term=term.multiply(value,mc).divide(BigDecimal.valueOf(k),mc);
            series=series.add(term,mc);
            if(term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0) break;
        }
        return series;
    }
    private static BigDecimal expInteger(int n,MathContext mc){
        if(n==0) return BigDecimal.ONE;
        BigDecimal e=seriesExp(BigDecimal.ONE,mc);
        BigDecimal result=BigDecimal.ONE; int power=Math.abs(n);
        for(int i=0;i<power;i++) result=result.multiply(e,mc);
        return n>0?result:BigDecimal.ONE.divide(result,mc);
    }
    static BigDecimal ln(BigDecimal value,MathContext mc){
        if(value.signum()<=0) throw new IllegalArgumentException("Logarithm requires a positive argument");
        int digits=value.precision()-value.scale()-1;
        BigDecimal scaled=value.scaleByPowerOfTen(-digits);
        BigDecimal ln10=lnSeries(BigDecimal.TEN,mc);
        BigDecimal base=lnSeries(scaled,mc);
        return base.add(BigDecimal.valueOf(digits).multiply(ln10,mc),mc);
    }
    private static BigDecimal lnSeries(BigDecimal value,MathContext mc){
        BigDecimal y=value.subtract(BigDecimal.ONE,mc).divide(value.add(BigDecimal.ONE,mc),mc);
        BigDecimal y2=y.multiply(y,mc),term=y,sum=y;
        for(int k=1;k<mc.getPrecision()*2;k++){
            term=term.multiply(y2,mc);
            BigDecimal add=term.divide(BigDecimal.valueOf(2*k+1),mc);
            sum=sum.add(add,mc);
            if(add.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0) break;
        }
        return sum.multiply(BigDecimal.valueOf(2),mc);
    }
    static BigDecimal log10(BigDecimal value,MathContext mc){return ln(value,mc).divide(ln(BigDecimal.TEN,mc),mc);}
}
