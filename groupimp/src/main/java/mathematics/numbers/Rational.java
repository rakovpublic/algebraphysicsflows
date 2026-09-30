package mathematics.numbers;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/** Canonical exact rational: denominator positive, gcd(numerator,denominator)=1. */
public final class Rational implements Comparable<Rational>, Serializable {
    private static final long serialVersionUID = 1L;
    public static final Rational ZERO=of(0), ONE=of(1);
    private final BigInteger numerator, denominator;
    public Rational(BigInteger numerator, BigInteger denominator) {
        Objects.requireNonNull(numerator); Objects.requireNonNull(denominator);
        if (denominator.signum() == 0) throw MathFailure.undefined("A rational denominator cannot be zero");
        if (denominator.signum() < 0) { numerator=numerator.negate(); denominator=denominator.negate(); }
        BigInteger gcd=numerator.gcd(denominator);
        this.numerator=numerator.divide(gcd); this.denominator=denominator.divide(gcd);
    }
    public static Rational of(long value) { return new Rational(BigInteger.valueOf(value),BigInteger.ONE); }
    public static Rational of(long numerator, long denominator) { return new Rational(BigInteger.valueOf(numerator),BigInteger.valueOf(denominator)); }
    public static Rational of(BigInteger value) { return new Rational(value,BigInteger.ONE); }
    public BigInteger numerator() { return numerator; }
    public BigInteger denominator() { return denominator; }
    public Rational add(Rational b) { return new Rational(numerator.multiply(b.denominator).add(b.numerator.multiply(denominator)),denominator.multiply(b.denominator)); }
    public Rational negate() { return new Rational(numerator.negate(),denominator); }
    public Rational subtract(Rational b) { return add(b.negate()); }
    public Rational multiply(Rational b) { return new Rational(numerator.multiply(b.numerator),denominator.multiply(b.denominator)); }
    public Rational divide(Rational b) {
        if (b.numerator.signum() == 0) throw MathFailure.undefined("Division by zero");
        return new Rational(numerator.multiply(b.denominator),denominator.multiply(b.numerator));
    }
    public Rational pow(int exponent) {
        if (exponent == Integer.MIN_VALUE) throw new IllegalArgumentException("Exponent outside supported resource range");
        return exponent < 0 ? ONE.divide(this).pow(-exponent) : new Rational(numerator.pow(exponent),denominator.pow(exponent));
    }
    public int signum() { return numerator.signum(); }
    @Override public int compareTo(Rational b) { return numerator.multiply(b.denominator).compareTo(b.numerator.multiply(denominator)); }
    @Override public boolean equals(Object other) { return other instanceof Rational && numerator.equals(((Rational)other).numerator) && denominator.equals(((Rational)other).denominator); }
    @Override public int hashCode() { return Objects.hash(numerator,denominator); }
    @Override public String toString() { return denominator.equals(BigInteger.ONE) ? numerator.toString() : numerator + "/" + denominator; }
}
