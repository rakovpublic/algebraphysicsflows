package mathematics.calculus;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.Objects;

/** Formal Q(x) element: coprime numerator/denominator with monic denominator. */
public final class RationalFunction implements Serializable {
    private static final long serialVersionUID=1L;
    public static final RationalFunction ZERO=of(Polynomial.ZERO);
    public static final RationalFunction ONE=of(Polynomial.ONE);
    private final Polynomial numerator,denominator;

    public RationalFunction(Polynomial numerator,Polynomial denominator) {
        Objects.requireNonNull(numerator); Objects.requireNonNull(denominator);
        if(denominator.degree()<0) throw MathFailure.undefined("Zero rational-function denominator");
        if(numerator.degree()<0) { this.numerator=Polynomial.ZERO; this.denominator=Polynomial.ONE; return; }
        Polynomial gcd=numerator.gcd(denominator);
        Polynomial reducedNumerator=numerator.divideExact(gcd),reducedDenominator=denominator.divideExact(gcd);
        Rational scale=Rational.ONE.divide(reducedDenominator.coefficient(reducedDenominator.degree()));
        this.numerator=reducedNumerator.scale(scale); this.denominator=reducedDenominator.scale(scale);
    }
    public static RationalFunction of(Polynomial value) { return new RationalFunction(value,Polynomial.ONE); }
    public Polynomial numerator() { return numerator; }
    public Polynomial denominator() { return denominator; }
    public boolean isZero() { return numerator.degree()<0; }
    public RationalFunction add(RationalFunction other) {
        return new RationalFunction(numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),denominator.multiply(other.denominator));
    }
    public RationalFunction negate() { return new RationalFunction(numerator.negate(),denominator); }
    public RationalFunction subtract(RationalFunction other) { return add(other.negate()); }
    public RationalFunction multiply(RationalFunction other) {
        return new RationalFunction(numerator.multiply(other.numerator),denominator.multiply(other.denominator));
    }
    public RationalFunction inverse() {
        if(isZero()) throw MathFailure.undefined("Zero rational function has no inverse");
        return new RationalFunction(denominator,numerator);
    }
    public RationalFunction divide(RationalFunction other) { return multiply(other.inverse()); }
    public RationalFunction derivative() {
        return new RationalFunction(numerator.derivative().multiply(denominator).subtract(numerator.multiply(denominator.derivative())),denominator.multiply(denominator));
    }
    /** Evaluate the canonical fraction; cancelled factors do not preserve excluded points. */
    public Rational evaluate(Rational value) { return numerator.evaluate(value).divide(denominator.evaluate(value)); }
    /** Formal substitution this(inner(x)); undefined when the substituted denominator is identically zero. */
    public RationalFunction compose(RationalFunction inner) {
        return substitute(numerator,inner).divide(substitute(denominator,inner));
    }
    private static RationalFunction substitute(Polynomial polynomial,RationalFunction inner) {
        RationalFunction result=ZERO;
        for(int degree=polynomial.degree();degree>=0;degree--)
            result=result.multiply(inner).add(of(new Polynomial(polynomial.coefficient(degree))));
        return result;
    }
    @Override public boolean equals(Object other) {
        return other instanceof RationalFunction && numerator.equals(((RationalFunction)other).numerator)
                && denominator.equals(((RationalFunction)other).denominator);
    }
    @Override public int hashCode() { return Objects.hash(numerator,denominator); }
    @Override public String toString() { return "Q(x)("+numerator+")/("+denominator+")"; }
}
