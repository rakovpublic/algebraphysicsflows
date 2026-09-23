package mathematics.calculus;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;

/** Exact rational-root theorem search. Resource exhaustion never returns an incomplete root list. */
final class RationalPolynomialRoots {
    static final int MAX_DEGREE=64, MAX_TRIAL_DIVISIONS=100000, MAX_CANDIDATES=100000;
    private RationalPolynomialRoots() {}
    private static final class Work {
        int trials, coefficients;
        void scan(int count) {
            coefficients+=count;
            if(coefficients>1000000) throw limit("coefficient visits");
        }
    }
    private static MathFailure limit(String detail) {
        return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Rational root search exceeds its limit for "+detail);
    }
    private static void require(Polynomial p) {
        if(p.degree()<0) throw MathFailure.undefined("The zero polynomial has infinitely many roots and no finite root multiplicity");
        if(p.degree()>MAX_DEGREE) throw limit("degree (64)");
    }
    static int multiplicity(Polynomial p,Rational root) {
        require(p); return multiplicity(p,root,new Work());
    }
    private static int multiplicity(Polynomial p,Rational root,Work work) {
        int result=0; Polynomial divisor=new Polynomial(root.negate(),Rational.ONE);
        while(p.degree()>0) {
            work.scan(p.degree()+1);
            if(p.evaluate(root).signum()!=0) break;
            work.scan(2*(p.degree()+1));
            p=p.divideExact(divisor); result++;
        }
        return result;
    }
    static List<Rational> roots(Polynomial input) {
        require(input); Work work=new Work(); Polynomial p=input;
        SortedSet<Rational> roots=new TreeSet<>();
        // Strip zero roots before applying the rational-root theorem to nonzero constant terms.
        int zeros=0;
        while(zeros<p.degree() && p.coefficient(zeros).signum()==0) zeros++;
        if(zeros>0) {
            roots.add(Rational.ZERO); Rational[] shifted=new Rational[p.degree()-zeros+1];
            for(int i=0;i<shifted.length;i++) shifted[i]=p.coefficient(i+zeros);
            p=new Polynomial(shifted);
        }
        if(p.degree()==1) roots.add(p.coefficient(0).negate().divide(p.coefficient(1)));
        else if(p.degree()>1) {
            BigInteger lcm=BigInteger.ONE;
            for(int i=0;i<=p.degree();i++) {
                BigInteger denominator=p.coefficient(i).denominator();
                lcm=lcm.divide(lcm.gcd(denominator)).multiply(denominator);
            }
            BigInteger[] integral=new BigInteger[p.degree()+1]; BigInteger content=BigInteger.ZERO;
            for(int i=0;i<integral.length;i++) {
                integral[i]=p.coefficient(i).numerator().multiply(lcm.divide(p.coefficient(i).denominator()));
                content=content.gcd(integral[i]);
            }
            for(int i=0;i<integral.length;i++) integral[i]=integral[i].divide(content);
            List<BigInteger> numerators=divisors(integral[0].abs(),work);
            List<BigInteger> denominators=divisors(integral[p.degree()].abs(),work);
            // Counting pairs before deduplication also bounds construction work.
            if(2L*numerators.size()*denominators.size()>MAX_CANDIDATES) throw limit("candidate fractions (100000)");
            SortedSet<Rational> candidates=new TreeSet<>();
            for(BigInteger numerator : numerators) for(BigInteger denominator : denominators) {
                Rational candidate=new Rational(numerator,denominator);
                candidates.add(candidate); candidates.add(candidate.negate());
            }
            for(Rational candidate : candidates) {
                work.scan(p.degree()+1);
                if(isRoot(integral,candidate)) roots.add(candidate);
            }
        }
        return Collections.unmodifiableList(new ArrayList<>(roots));
    }
    /** Evaluate b^degree * p(a/b) in integers, avoiding a rational gcd at every Horner step. */
    private static boolean isRoot(BigInteger[] coefficients,Rational candidate) {
        BigInteger numerator=candidate.numerator(),denominator=candidate.denominator();
        BigInteger value=coefficients[coefficients.length-1],denominatorPower=denominator;
        for(int i=coefficients.length-2;i>=0;i--) {
            value=value.multiply(numerator).add(coefficients[i].multiply(denominatorPower));
            if(i>0) denominatorPower=denominatorPower.multiply(denominator);
        }
        return value.signum()==0;
    }
    private static List<BigInteger> divisors(BigInteger value,Work work) {
        List<BigInteger> values=new ArrayList<>(); values.add(BigInteger.ONE);
        BigInteger factor=BigInteger.valueOf(2),remaining=value;
        while(factor.multiply(factor).compareTo(remaining)<=0) {
            if(++work.trials>MAX_TRIAL_DIVISIONS) throw limit("trial divisions (100000)");
            int exponent=0;
            while(remaining.mod(factor).signum()==0) {
                if(++work.trials>MAX_TRIAL_DIVISIONS) throw limit("trial divisions (100000)");
                remaining=remaining.divide(factor); exponent++;
            }
            appendPowers(values,factor,exponent);
            factor=factor.equals(BigInteger.valueOf(2))?BigInteger.valueOf(3):factor.add(BigInteger.valueOf(2));
        }
        if(remaining.compareTo(BigInteger.ONE)>0) appendPowers(values,remaining,1);
        return values;
    }
    private static void appendPowers(List<BigInteger> values,BigInteger prime,int exponent) {
        int original=values.size();
        if((long)original*(exponent+1)>MAX_CANDIDATES/2) throw limit("divisor count");
        BigInteger power=BigInteger.ONE;
        for(int e=1;e<=exponent;e++) {
            power=power.multiply(prime);
            for(int i=0;i<original;i++) values.add(values.get(i).multiply(power));
        }
    }
}
