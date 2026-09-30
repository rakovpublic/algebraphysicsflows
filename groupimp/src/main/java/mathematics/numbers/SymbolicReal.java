package mathematics.numbers;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.util.Objects;

/** Finite real expressions only. Structural equals is not extensional real equality. */
public final class SymbolicReal implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum Kind { RATIONAL, PI, E, SQRT_RATIONAL, ADD, MULTIPLY }
    public final Kind kind;
    private final Rational rational;
    private final SymbolicReal left, right;
    private SymbolicReal(Kind kind, Rational rational, SymbolicReal left, SymbolicReal right) {
        this.kind=kind; this.rational=rational; this.left=left; this.right=right;
    }
    public static SymbolicReal rational(Rational value) { return new SymbolicReal(Kind.RATIONAL,Objects.requireNonNull(value),null,null); }
    public static SymbolicReal pi() { return new SymbolicReal(Kind.PI,null,null,null); }
    public static SymbolicReal e() { return new SymbolicReal(Kind.E,null,null,null); }
    public static SymbolicReal sqrt(Rational value) {
        if (value.signum() < 0) throw MathFailure.undefined("Negative rational has no real square root");
        return new SymbolicReal(Kind.SQRT_RATIONAL,value,null,null);
    }
    public SymbolicReal add(SymbolicReal b) { return new SymbolicReal(Kind.ADD,null,this,Objects.requireNonNull(b)); }
    public SymbolicReal multiply(SymbolicReal b) { return new SymbolicReal(Kind.MULTIPLY,null,this,Objects.requireNonNull(b)); }
    @Override public boolean equals(Object b) {
        if (!(b instanceof SymbolicReal)) return false;
        SymbolicReal other=(SymbolicReal)b;
        return kind==other.kind && Objects.equals(rational,other.rational) && Objects.equals(left,other.left) && Objects.equals(right,other.right);
    }
    @Override public int hashCode() { return Objects.hash(kind,rational,left,right); }
    @Override public String toString() {
        switch(kind) {
            case RATIONAL: return rational.toString();
            case PI: return "pi";
            case E: return "e";
            case SQRT_RATIONAL: return "sqrt(" + rational + ")";
            default: return "(" + left + (kind==Kind.ADD ? "+" : "*") + right + ")";
        }
    }
}
