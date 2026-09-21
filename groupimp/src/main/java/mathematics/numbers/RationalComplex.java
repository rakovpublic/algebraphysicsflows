package mathematics.numbers;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.util.Objects;

/** Q(i), an exact subfield of C. This class does not claim to represent every complex number. */
public final class RationalComplex implements Serializable {
    private static final long serialVersionUID = 1L;
    public final Rational real, imaginary;
    public RationalComplex(Rational real, Rational imaginary) { this.real=Objects.requireNonNull(real); this.imaginary=Objects.requireNonNull(imaginary); }
    public RationalComplex add(RationalComplex b) { return new RationalComplex(real.add(b.real),imaginary.add(b.imaginary)); }
    public RationalComplex negate() { return new RationalComplex(real.negate(),imaginary.negate()); }
    public RationalComplex subtract(RationalComplex b) { return add(b.negate()); }
    public RationalComplex conjugate() { return new RationalComplex(real,imaginary.negate()); }
    public Rational normSquared() { return real.multiply(real).add(imaginary.multiply(imaginary)); }
    public RationalComplex multiply(RationalComplex b) { return new RationalComplex(real.multiply(b.real).subtract(imaginary.multiply(b.imaginary)),real.multiply(b.imaginary).add(imaginary.multiply(b.real))); }
    public RationalComplex divide(RationalComplex b) {
        Rational norm=b.normSquared();
        if (norm.signum() == 0) throw MathFailure.undefined("Complex division by zero");
        RationalComplex product=multiply(b.conjugate());
        return new RationalComplex(product.real.divide(norm),product.imaginary.divide(norm));
    }
    @Override public boolean equals(Object b) { return b instanceof RationalComplex && real.equals(((RationalComplex)b).real) && imaginary.equals(((RationalComplex)b).imaginary); }
    @Override public int hashCode() { return Objects.hash(real,imaginary); }
    @Override public String toString() { return "(" + real + ")+(" + imaginary + ")i"; }
}
