package mathematics.linear;
import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.*;
public final class RationalVector implements Serializable {
    private static final long serialVersionUID=1L;
    private final List<Rational> entries;
    public RationalVector(Rational... values) {
        List<Rational> copy=new ArrayList<>(Arrays.asList(values));
        for (Rational value : copy) Objects.requireNonNull(value);
        entries=Collections.unmodifiableList(copy);
    }
    public int dimension() { return entries.size(); }
    public Rational get(int i) { return entries.get(i); }
    private void sameDimension(RationalVector b) { if (dimension()!=b.dimension()) throw MathFailure.invalid("Vector dimensions differ"); }
    public RationalVector add(RationalVector b) {
        sameDimension(b); Rational[] values=new Rational[dimension()];
        for (int i=0;i<values.length;i++) values[i]=get(i).add(b.get(i));
        return new RationalVector(values);
    }
    public RationalVector scale(Rational scalar) {
        Rational[] values=new Rational[dimension()];
        for (int i=0;i<values.length;i++) values[i]=get(i).multiply(scalar);
        return new RationalVector(values);
    }
    public Rational dot(RationalVector b) {
        sameDimension(b); Rational result=Rational.ZERO;
        for (int i=0;i<dimension();i++) result=result.add(get(i).multiply(b.get(i)));
        return result;
    }
    @Override public boolean equals(Object b) { return b instanceof RationalVector && entries.equals(((RationalVector)b).entries); }
    @Override public int hashCode() { return entries.hashCode(); }
    @Override public String toString() { return entries.toString(); }
}
