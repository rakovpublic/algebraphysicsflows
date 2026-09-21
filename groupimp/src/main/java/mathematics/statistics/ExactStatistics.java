package mathematics.statistics;
import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.util.List;
public final class ExactStatistics {
    private ExactStatistics() { }
    public static Rational mean(List<Rational> sample) {
        if(sample.isEmpty()) throw MathFailure.undefined("Empty sample has no arithmetic mean");
        Rational sum=Rational.ZERO; for(Rational value : sample) sum=sum.add(value);
        return sum.divide(Rational.of(sample.size()));
    }
    public static Rational variance(List<Rational> sample,boolean unbiasedSampleEstimate) {
        int denominator=sample.size()-(unbiasedSampleEstimate?1:0);
        if(denominator<=0) throw MathFailure.undefined("Insufficient observations for this variance convention");
        Rational mean=mean(sample), sum=Rational.ZERO;
        for(Rational value : sample) { Rational delta=value.subtract(mean); sum=sum.add(delta.multiply(delta)); }
        return sum.divide(Rational.of(denominator));
    }
    public static Rational covariance(List<Rational> x,List<Rational> y,boolean unbiasedSampleEstimate) {
        if(x.size()!=y.size()) throw MathFailure.invalid("Paired samples must have the same size");
        int denominator=x.size()-(unbiasedSampleEstimate?1:0);
        if(denominator<=0) throw MathFailure.undefined("Insufficient paired observations");
        Rational mx=mean(x),my=mean(y),sum=Rational.ZERO;
        for(int i=0;i<x.size();i++) sum=sum.add(x.get(i).subtract(mx).multiply(y.get(i).subtract(my)));
        return sum.divide(Rational.of(denominator));
    }
}
