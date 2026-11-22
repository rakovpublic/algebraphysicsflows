package mathematics.numbers;

import mathematics.calculus.Polynomial;
import mathematics.calculus.RationalFunction;
import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Exact rational-function division subring of H(Q)((Z; sigma, 1)).
 * Here sigma(q)=i*q*i^-1, the group generator X satisfies X*q=sigma(q)*X,
 * and the central Laurent parameter is t=i^-1*X. Values are H(Q)(t), represented
 * by four central Q(t) coordinates. This is the rational-function subring of the
 * full Hahn-Mal'cev-Neumann completion, not a representation of every Hahn series.
 */
public final class RationalTwistedHahnSeries implements Serializable {
    private static final long serialVersionUID=1L;
    private static final RationalFunction ZERO=RationalFunction.ZERO;
    private static final RationalFunction ONE=RationalFunction.ONE;
    public static final RationalTwistedHahnSeries ZERO_VALUE=scalar(ZERO);
    public static final RationalTwistedHahnSeries ONE_VALUE=scalar(ONE);
    public static final RationalTwistedHahnSeries GROUP_GENERATOR=groupElement(BigInteger.ONE);
    private final RationalFunction a,b,c,d; // a+b*i+(c+d*i)*X; Q(t) coordinates are central.

    public RationalTwistedHahnSeries(RationalFunction a,RationalFunction b,RationalFunction c,RationalFunction d) {
        this.a=Objects.requireNonNull(a); this.b=Objects.requireNonNull(b);
        this.c=Objects.requireNonNull(c); this.d=Objects.requireNonNull(d);
    }
    public static RationalTwistedHahnSeries scalar(RationalFunction value) { return new RationalTwistedHahnSeries(value,ZERO,ZERO,ZERO); }
    public static RationalTwistedHahnSeries fromRational(Rational value) { return scalar(RationalFunction.of(new Polynomial(value))); }
    public static RationalTwistedHahnSeries fromPolynomial(Polynomial value) { return scalar(RationalFunction.of(value)); }
    public static RationalTwistedHahnSeries fromQuaternion(RationalQuaternion q) {
        return new RationalTwistedHahnSeries(constant(q.real),constant(q.i),constant(q.j),constant(q.k));
    }
    public static RationalTwistedHahnSeries fromComplex(RationalComplex z) {
        return new RationalTwistedHahnSeries(constant(z.real),constant(z.imaginary),ZERO,ZERO);
    }
    public static RationalTwistedHahnSeries fromImaginary(RationalImaginary value) {
        return new RationalTwistedHahnSeries(ZERO,constant(value.coefficient),ZERO,ZERO);
    }
    public static RationalTwistedHahnSeries fromOctonion(RationalOctonion value) { return fromQuaternion(value.toQuaternion()); }
    public static RationalTwistedHahnSeries fromDixonZ2(RationalDixonZ2 value) { return fromQuaternion(value.toQuaternion()); }
    public static RationalTwistedHahnSeries fromBoolean(Boolean value) { return fromRational(value?Rational.ONE:Rational.ZERO); }
    public static RationalTwistedHahnSeries fromRationalFunction(RationalFunction value) { return scalar(value); }
    private static RationalFunction constant(Rational value) { return RationalFunction.of(new Polynomial(value)); }

    /** Group basis X^n, where X=i*t and n is any integer fitting a finite polynomial exponent. */
    public static RationalTwistedHahnSeries groupElement(BigInteger exponent) {
        BigInteger[] division=exponent.divideAndRemainder(BigInteger.valueOf(4));
        int residue=division[1].intValue(); if(residue<0) residue+=4;
        RationalQuaternion factor;
        switch(residue) {
            case 0: factor=RationalQuaternion.ONE; break;
            case 1: factor=RationalQuaternion.I; break;
            case 2: factor=RationalQuaternion.ONE.negate(); break;
            default: factor=RationalQuaternion.I.negate();
        }
        BigInteger magnitude=exponent.abs();
        if(magnitude.compareTo(BigInteger.valueOf(Integer.MAX_VALUE-1L))>0)
            throw MathFailure.undefined("Group exponent is too large for an explicitly stored rational-function monomial");
        int power=magnitude.intValue(); RationalFunction monomial;
        if(exponent.signum()>=0) {
            Rational[] coefficients=new Rational[power+1]; java.util.Arrays.fill(coefficients,Rational.ZERO); coefficients[power]=Rational.ONE;
            monomial=RationalFunction.of(new Polynomial(coefficients));
        } else {
            Rational[] coefficients=new Rational[power+1]; java.util.Arrays.fill(coefficients,Rational.ZERO); coefficients[power]=Rational.ONE;
            monomial=new RationalFunction(Polynomial.ONE,new Polynomial(coefficients));
        }
        return new RationalTwistedHahnSeries(monomial.multiply(constant(factor.real)),monomial.multiply(constant(factor.i)),
                monomial.multiply(constant(factor.j)),monomial.multiply(constant(factor.k)));
    }
    private RationalFunction[] components() { return new RationalFunction[]{a,b,c,d}; }
    public RationalTwistedHahnSeries add(RationalTwistedHahnSeries other) {
        return new RationalTwistedHahnSeries(a.add(other.a),b.add(other.b),c.add(other.c),d.add(other.d));
    }
    public RationalTwistedHahnSeries negate() { return new RationalTwistedHahnSeries(a.negate(),b.negate(),c.negate(),d.negate()); }
    public RationalTwistedHahnSeries subtract(RationalTwistedHahnSeries other) { return add(other.negate()); }
    /** Hamilton product over the central field Q(t); the skew group rule is X*q=sigma(q)*X. */
    public RationalTwistedHahnSeries multiply(RationalTwistedHahnSeries y) {
        return new RationalTwistedHahnSeries(
                a.multiply(y.a).subtract(b.multiply(y.b)).subtract(c.multiply(y.c)).subtract(d.multiply(y.d)),
                a.multiply(y.b).add(b.multiply(y.a)).add(c.multiply(y.d)).subtract(d.multiply(y.c)),
                a.multiply(y.c).subtract(b.multiply(y.d)).add(c.multiply(y.a)).add(d.multiply(y.b)),
                a.multiply(y.d).add(b.multiply(y.c)).subtract(c.multiply(y.b)).add(d.multiply(y.a)));
    }
    public RationalFunction normSquared() { return a.multiply(a).add(b.multiply(b)).add(c.multiply(c)).add(d.multiply(d)); }
    public RationalTwistedHahnSeries conjugate() { return new RationalTwistedHahnSeries(a,b.negate(),c.negate(),d.negate()); }
    public RationalTwistedHahnSeries inverse() {
        RationalFunction norm=normSquared();
        if(norm.isZero()) throw MathFailure.undefined("Zero has no inverse in the twisted Hahn division subring");
        RationalFunction scale=norm.inverse(); RationalTwistedHahnSeries conjugate=conjugate();
        return new RationalTwistedHahnSeries(conjugate.a.multiply(scale),conjugate.b.multiply(scale),conjugate.c.multiply(scale),conjugate.d.multiply(scale));
    }
    public RationalTwistedHahnSeries divideRight(RationalTwistedHahnSeries other) { return multiply(other.inverse()); }
    public RationalTwistedHahnSeries divideLeft(RationalTwistedHahnSeries other) { return other.inverse().multiply(this); }
    /** Extend sigma(q)=i*q*i^-1 coefficientwise, fixing central t. */
    public RationalTwistedHahnSeries applySigma() { return new RationalTwistedHahnSeries(a,b,c.negate(),d.negate()); }
    public RationalFunction centralPart() {
        if(!b.isZero() || !c.isZero() || !d.isZero()) throw MathFailure.undefined("Element is not in the central Q(t) subfield");
        return a;
    }
    public RationalFunction toRationalFunction() { return centralPart(); }
    public Polynomial toPolynomial() {
        RationalFunction value=centralPart();
        if(!value.denominator().equals(Polynomial.ONE)) throw MathFailure.undefined("Central rational function is not a polynomial");
        return value.numerator();
    }
    public RationalQuaternion toQuaternion() {
        Rational[] values=new Rational[4]; RationalFunction[] entries=components();
        for(int n=0;n<4;n++) values[n]=constantValue(entries[n]);
        return new RationalQuaternion(values[0],values[1],values[2],values[3]);
    }
    private static Rational constantValue(RationalFunction value) {
        if(value.numerator().degree()>0 || value.denominator().degree()!=0)
            throw MathFailure.undefined("Element has nonconstant t-dependence");
        return value.numerator().coefficient(0).divide(value.denominator().coefficient(0));
    }
    public Rational toRational() { return toQuaternion().toRational(); }
    public RationalComplex toComplex() { return toQuaternion().toComplex(); }
    public RationalImaginary toImaginary() { return RationalImaginary.fromQuaternion(toQuaternion()); }
    public RationalOctonion toOctonion() { return RationalOctonion.fromQuaternion(toQuaternion()); }
    public RationalDixonZ2 toDixonZ2() { return RationalDixonZ2.fromQuaternion(toQuaternion()); }
    public boolean isZero() { return a.isZero() && b.isZero() && c.isZero() && d.isZero(); }
    public boolean isOne() { return equals(ONE_VALUE); }
    public boolean toBoolean() {
        if(isZero()) return false;
        if(isOne()) return true;
        throw MathFailure.undefined("Only zero or one in the twisted Hahn division subring converts to Boolean");
    }
    private static BigInteger tOrder(Polynomial p) {
        if(p.degree()<0) return null;
        for(int n=0;n<=p.degree();n++) if(p.coefficient(n).signum()!=0) return BigInteger.valueOf(n);
        return null;
    }
    private static BigInteger valuation(RationalFunction f) {
        if(f.isZero()) return null;
        return tOrder(f.numerator()).subtract(tOrder(f.denominator()));
    }
    private static Rational initialCoefficient(RationalFunction f) {
        if(f.isZero()) return Rational.ZERO;
        int numerator=tOrder(f.numerator()).intValueExact(),denominator=tOrder(f.denominator()).intValueExact();
        return f.numerator().coefficient(numerator).divide(f.denominator().coefficient(denominator));
    }
    /** Least exponent in the associated Z-ordered formal Hahn expansion. */
    public BigInteger groupValuation() {
        BigInteger least=null;
        for(RationalFunction value:components()) {
            BigInteger candidate=valuation(value);
            if(candidate!=null && (least==null || candidate.compareTo(least)<0)) least=candidate;
        }
        if(least==null) throw MathFailure.undefined("Zero has no Hahn valuation");
        return least;
    }
    /** Leading coefficient in K=H(Q) for the ordered group coordinate X^n. */
    public RationalQuaternion leadingCoefficient() {
        BigInteger least=groupValuation(); RationalFunction[] values=components(); Rational[] coefficient=new Rational[4];
        for(int n=0;n<4;n++) coefficient[n]=least.equals(valuation(values[n]))?initialCoefficient(values[n]):Rational.ZERO;
        RationalQuaternion leading=new RationalQuaternion(coefficient[0],coefficient[1],coefficient[2],coefficient[3]);
        int residue=least.negate().mod(BigInteger.valueOf(4)).intValue();
        RationalQuaternion factor;
        switch(residue) {
            case 0: factor=RationalQuaternion.ONE; break;
            case 1: factor=RationalQuaternion.I; break;
            case 2: factor=RationalQuaternion.ONE.negate(); break;
            default: factor=RationalQuaternion.I.negate();
        }
        // h_m*t^m = h_m*(i^-1)^m*X^m; factor=(-i)^m.
        return leading.multiply(factor);
    }
    public boolean equals(Object other) {
        if(!(other instanceof RationalTwistedHahnSeries)) return false;
        RationalTwistedHahnSeries value=(RationalTwistedHahnSeries)other;
        return a.equals(value.a)&&b.equals(value.b)&&c.equals(value.c)&&d.equals(value.d);
    }
    @Override public int hashCode() { return Objects.hash(a,b,c,d); }
    @Override public String toString() { return "HMN(H(Q);Z,sigma,1)["+a+","+b+"i,"+c+"X,"+d+"iX]"; }
}
