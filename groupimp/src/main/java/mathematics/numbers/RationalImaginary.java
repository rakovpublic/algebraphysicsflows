package mathematics.numbers;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.util.Objects;

/** A pure imaginary rational number coefficient*i, retained separately from Q(i). */
public final class RationalImaginary implements Serializable {
    private static final long serialVersionUID=1L;
    public static final RationalImaginary ZERO=new RationalImaginary(Rational.ZERO);
    public static final RationalImaginary I=new RationalImaginary(Rational.ONE);
    public final Rational coefficient;

    public RationalImaginary(Rational coefficient) { this.coefficient=Objects.requireNonNull(coefficient); }
    public RationalImaginary add(RationalImaginary other) { return new RationalImaginary(coefficient.add(other.coefficient)); }
    public RationalImaginary subtract(RationalImaginary other) { return new RationalImaginary(coefficient.subtract(other.coefficient)); }
    public RationalImaginary negate() { return new RationalImaginary(coefficient.negate()); }
    public RationalImaginary conjugate() { return negate(); }
    public Rational multiply(RationalImaginary other) { return coefficient.multiply(other.coefficient).negate(); }
    public Rational divide(RationalImaginary other) { return coefficient.divide(other.coefficient); }
    public Rational normSquared() { return coefficient.multiply(coefficient); }
    public Rational square() { return normSquared().negate(); }
    public RationalImaginary scale(Rational scalar) { return new RationalImaginary(coefficient.multiply(scalar)); }
    public RationalComplex toComplex() { return new RationalComplex(Rational.ZERO,coefficient); }
    public static RationalImaginary fromComplex(RationalComplex value) {
        if(!value.real.equals(Rational.ZERO)) throw MathFailure.undefined("A complex value must have zero real part to be pure imaginary");
        return new RationalImaginary(value.imaginary);
    }
    public RationalQuaternion toQuaternion() { return new RationalQuaternion(Rational.ZERO,coefficient,Rational.ZERO,Rational.ZERO); }
    public static RationalImaginary fromQuaternion(RationalQuaternion value) {
        if(!value.real.equals(Rational.ZERO) || !value.j.equals(Rational.ZERO) || !value.k.equals(Rational.ZERO))
            throw MathFailure.undefined("A quaternion must lie on the i axis to be pure imaginary");
        return new RationalImaginary(value.i);
    }
    @Override public boolean equals(Object other) { return other instanceof RationalImaginary && coefficient.equals(((RationalImaginary)other).coefficient); }
    @Override public int hashCode() { return coefficient.hashCode(); }
    @Override public String toString() { return "("+coefficient+")i"; }
}
