package mathematics.calculus;

import mathematics.numbers.Rational;
import mathematics.core.MathFailure;
import mathematics.foundations.Pair;
import java.io.Serializable;
import java.util.*;

/** Univariate Q[x]. Polynomial smoothness is structural; arbitrary callbacks are not accepted. */
public final class Polynomial implements Serializable {
    private static final long serialVersionUID=1L;
    public static final Polynomial ZERO=new Polynomial(Rational.ZERO);
    public static final Polynomial ONE=new Polynomial(Rational.ONE);
    private final List<Rational> coefficients;
    public Polynomial(Rational... input) {
        List<Rational> values=new ArrayList<>(Arrays.asList(input));
        for(Rational value : values) Objects.requireNonNull(value);
        if(values.isEmpty()) values.add(Rational.ZERO);
        while(values.size()>1 && values.get(values.size()-1).signum()==0) values.remove(values.size()-1);
        coefficients=Collections.unmodifiableList(values);
    }
    public int degree() { return coefficients.size()==1 && coefficients.get(0).signum()==0 ? -1 : coefficients.size()-1; }
    public Rational coefficient(int power) {
        if(power<0) throw new IllegalArgumentException("Negative exponent");
        return power<coefficients.size()?coefficients.get(power):Rational.ZERO;
    }
    public Rational evaluate(Rational x) {
        Rational result=Rational.ZERO;
        for(int i=coefficients.size()-1;i>=0;i--) result=result.multiply(x).add(coefficients.get(i));
        return result;
    }
    /** Distinct rational roots in increasing order. Irrational and nonreal roots are not emitted. */
    public List<Rational> rationalRoots() { return RationalPolynomialRoots.roots(this); }
    /** Zero for a nonroot; undefined for the zero polynomial. */
    public int rootMultiplicity(Rational root) { return RationalPolynomialRoots.multiplicity(this,root); }
    public Polynomial add(Polynomial b) {
        Rational[] values=new Rational[Math.max(coefficients.size(),b.coefficients.size())];
        for(int i=0;i<values.length;i++) values[i]=coefficient(i).add(b.coefficient(i));
        return new Polynomial(values);
    }
    public Polynomial scale(Rational factor) {
        Rational[] values=new Rational[coefficients.size()];
        for(int i=0;i<values.length;i++) values[i]=coefficients.get(i).multiply(factor);
        return new Polynomial(values);
    }
    public Polynomial negate() { return scale(Rational.of(-1)); }
    public Polynomial subtract(Polynomial b) { return add(b.negate()); }
    public Polynomial multiply(Polynomial b) {
        if(degree()<0 || b.degree()<0) return ZERO;
        Rational[] values=new Rational[coefficients.size()+b.coefficients.size()-1]; Arrays.fill(values,Rational.ZERO);
        for(int i=0;i<coefficients.size();i++) for(int j=0;j<b.coefficients.size();j++) values[i+j]=values[i+j].add(coefficient(i).multiply(b.coefficient(j)));
        return new Polynomial(values);
    }
    /** Euclidean division: this = divisor * quotient + remainder, deg(remainder) < deg(divisor). */
    public Pair<Polynomial,Polynomial> divideAndRemainder(Polynomial divisor) {
        int divisorDegree=divisor.degree();
        if(divisorDegree<0) throw MathFailure.undefined("Polynomial division by zero");
        Rational[] remainder=coefficients.toArray(new Rational[0]);
        Rational[] quotient=new Rational[Math.max(1,degree()-divisorDegree+1)];
        Arrays.fill(quotient,Rational.ZERO);
        int remainderDegree=degree();
        while(remainderDegree>=divisorDegree) {
            int shift=remainderDegree-divisorDegree;
            Rational factor=remainder[remainderDegree].divide(divisor.coefficient(divisorDegree));
            quotient[shift]=quotient[shift].add(factor);
            for(int i=0;i<=divisorDegree;i++)
                remainder[i+shift]=remainder[i+shift].subtract(factor.multiply(divisor.coefficient(i)));
            while(remainderDegree>=0 && remainder[remainderDegree].signum()==0) remainderDegree--;
        }
        return new Pair<>(new Polynomial(quotient),new Polynomial(remainder));
    }
    public Polynomial quotient(Polynomial divisor) { return divideAndRemainder(divisor).first; }
    public Polynomial remainder(Polynomial divisor) { return divideAndRemainder(divisor).second; }
    public Polynomial divideExact(Polynomial divisor) {
        Pair<Polynomial,Polynomial> division=divideAndRemainder(divisor);
        if(division.second.degree()>=0) throw MathFailure.undefined("Polynomial division has nonzero remainder");
        return division.first;
    }
    public Polynomial monic() {
        if(degree()<0) throw MathFailure.undefined("Zero polynomial has no monic normalization");
        return scale(Rational.ONE.divide(coefficient(degree())));
    }
    /** The monic gcd, with gcd(0,0)=0. */
    public Polynomial gcd(Polynomial other) {
        Polynomial a=this,b=other;
        while(b.degree()>=0) { Polynomial remainder=a.remainder(b); a=b; b=remainder; }
        return a.degree()<0?ZERO:a.monic();
    }
    /** Substitution this(inner(x)). */
    public Polynomial compose(Polynomial inner) {
        Polynomial result=ZERO;
        for(int i=coefficients.size()-1;i>=0;i--) result=result.multiply(inner).add(new Polynomial(coefficients.get(i)));
        return result;
    }
    public Polynomial derivative() {
        Rational[] values=new Rational[Math.max(1,coefficients.size()-1)]; Arrays.fill(values,Rational.ZERO);
        for(int i=1;i<coefficients.size();i++) values[i-1]=coefficient(i).multiply(Rational.of(i));
        return new Polynomial(values);
    }
    public Polynomial derivative(int order) {
        if(order<0) throw new IllegalArgumentException("Negative derivative order");
        Polynomial result=this;
        for(int i=0;i<order && result.degree()>=0;i++) result=result.derivative();
        return result;
    }
    public Polynomial primitive(Rational integrationConstant) {
        Rational[] values=new Rational[coefficients.size()+1]; values[0]=Objects.requireNonNull(integrationConstant);
        for(int i=0;i<coefficients.size();i++) values[i+1]=coefficient(i).divide(Rational.of(i+1));
        return new Polynomial(values);
    }
    public Polynomial anchoredPrimitive(Rational anchor, Rational valueAtAnchor) {
        Polynomial normalized=primitive(Rational.ZERO);
        return primitive(valueAtAnchor.subtract(normalized.evaluate(anchor)));
    }
    public Rational integrate(Rational lower,Rational upper) {
        Polynomial primitive=primitive(Rational.ZERO);
        return primitive.evaluate(upper).subtract(primitive.evaluate(lower));
    }
    @Override public boolean equals(Object b) { return b instanceof Polynomial && coefficients.equals(((Polynomial)b).coefficients); }
    @Override public int hashCode() { return coefficients.hashCode(); }
    @Override public String toString() { return "Q[x]" + coefficients; }
}
