package algebra.concrete;

import mathematics.core.MathFailure;
import mathematics.statistics.*;
import java.math.BigInteger;

/** Exact finite-sample statistics registered in the original Algebra operation families. */
public final class RationalSampleAlgebra extends ConcreteAlgebra<RationalSample> {
    public RationalSampleAlgebra(RationalField rationals,NaturalSemiring naturals) {
        super(carrier("Sample(Q)",RationalSample.class,"Finite ordered rational observations",sample -> true),rationals.unit());
        closed("concatenate",false,RationalSample::concatenate);
        unary("size",algebra(),naturals.algebra(),false,sample -> BigInteger.valueOf(sample.size()));
        unary("mean",algebra(),rationals.algebra(),true,sample -> ExactStatistics.mean(sample.values()));
        unary("population-variance",algebra(),rationals.algebra(),true,sample -> ExactStatistics.variance(sample.values(),false));
        unary("sample-variance",algebra(),rationals.algebra(),true,sample -> ExactStatistics.variance(sample.values(),true));
        unary("center",algebra(),algebra(),true,RationalSample::centered);
        binary("population-covariance",algebra(),algebra(),rationals.algebra(),true,(a,b) -> covariance(a,b,false));
        binary("sample-covariance",algebra(),algebra(),rationals.algebra(),true,(a,b) -> covariance(a,b,true));
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalSample::scale);
        unaryFlat("elements",algebra(),rationals.algebra(),false,RationalSample::values);
        constant("empty",RationalSample.of());
        law("Observations have list semantics: order and repetitions matter for paired statistics.");
        law("Population variance divides by n; sample variance and covariance divide by n-1.");
        law("Mean of an empty sample and sample variance with fewer than two observations are undefined.");
    }
    private static mathematics.numbers.Rational covariance(RationalSample a,RationalSample b,boolean sample) {
        if(a.size()!=b.size()) throw MathFailure.undefined("Paired covariance requires equal sample lengths");
        return ExactStatistics.covariance(a.values(),b.values(),sample);
    }
}

