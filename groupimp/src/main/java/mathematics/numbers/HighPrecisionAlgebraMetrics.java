package mathematics.numbers;

import java.math.*;

/** Decimal evaluators for the Koide ratio and CKM Jarlskog invariant. */
public final class HighPrecisionAlgebraMetrics {
    private HighPrecisionAlgebraMetrics(){}
    public static BigDecimal koide(BigDecimal m1,BigDecimal m2,BigDecimal m3,int precision){
        if(precision<1)throw new IllegalArgumentException("Precision must be positive");
        BigDecimal[] masses={m1,m2,m3};for(BigDecimal m:masses)if(m.signum()<0)throw new IllegalArgumentException("Masses must be nonnegative");
        MathContext mc=new MathContext(precision+8,RoundingMode.HALF_EVEN);
        BigDecimal sum=m1.add(m2,mc).add(m3,mc),roots=sqrt(m1,mc).add(sqrt(m2,mc),mc).add(sqrt(m3,mc),mc);
        if(roots.signum()==0)throw new ArithmeticException("Koide ratio is undefined when all masses are zero");
        return sum.divide(roots.multiply(roots,mc),new MathContext(precision,RoundingMode.HALF_EVEN));
    }
    /** Inputs are the six real sine/cosine factors and an exact cyclotomic phase e^(i delta). */
    public static BigDecimal ckmJarlskog(BigDecimal s12,BigDecimal s23,BigDecimal s13,
            BigDecimal c12,BigDecimal c23,BigDecimal c13,RationalCyclotomic phase,int precision){
        if(!phase.multiply(phase.conjugate()).equals(RationalCyclotomic.one(phase.order())))
            throw new IllegalArgumentException("CKM phase must have exact cyclotomic norm one");
        BigDecimal sinDelta=phase.evaluate(precision+4).imaginary;
        return ckmJarlskogWithSine(s12,s23,s13,c12,c23,c13,sinDelta,precision);
    }
    /** Jarlskog invariant from measured real mixing-angle factors and a measured phase in radians. */
    public static BigDecimal ckmJarlskog(BigDecimal s12,BigDecimal s23,BigDecimal s13,
            BigDecimal c12,BigDecimal c23,BigDecimal c13,BigDecimal deltaRadians,int precision){
        return ckmJarlskogWithSine(s12,s23,s13,c12,c23,c13,RationalCyclotomic.sinRadians(deltaRadians,precision+4),precision);
    }
    private static BigDecimal ckmJarlskogWithSine(BigDecimal s12,BigDecimal s23,BigDecimal s13,
            BigDecimal c12,BigDecimal c23,BigDecimal c13,BigDecimal sinDelta,int precision){
        if(precision<1)throw new IllegalArgumentException("Precision must be positive");
        MathContext mc=new MathContext(precision+8,RoundingMode.HALF_EVEN);
        BigDecimal result=s12.multiply(s23,mc).multiply(s13,mc).multiply(c12,mc).multiply(c23,mc).multiply(c13,mc).multiply(c13,mc).multiply(sinDelta,mc);
        return result.round(new MathContext(precision,RoundingMode.HALF_EVEN));
    }
    private static BigDecimal sqrt(BigDecimal value,MathContext mc){
        if(value.signum()==0)return BigDecimal.ZERO;
        int decimalExponent=value.precision()-value.scale()-1;
        BigDecimal x=BigDecimal.ONE.scaleByPowerOfTen(Math.floorDiv(decimalExponent,2));
        BigDecimal tolerance=BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision());
        for(int i=0;i<mc.getPrecision()*2;i++){
            BigDecimal next=x.add(value.divide(x,mc),mc).divide(BigDecimal.valueOf(2),mc);
            if(next.subtract(x).abs().compareTo(tolerance)<0)return next;
            x=next;
        }
        return x;
    }
}
