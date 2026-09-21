package mathematics.statistics;

import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.*;

/** Finite ordered observations; repeated observations are retained. */
public final class RationalSample implements Serializable {
    private static final long serialVersionUID=1L;
    private final List<Rational> values;
    public RationalSample(List<Rational> values) {
        List<Rational> copy=new ArrayList<>(values);
        for(Rational value : copy) Objects.requireNonNull(value);
        this.values=Collections.unmodifiableList(copy);
    }
    public static RationalSample of(Rational... values) { return new RationalSample(Arrays.asList(values)); }
    public List<Rational> values() { return values; }
    public int size() { return values.size(); }
    public RationalSample concatenate(RationalSample other) {
        List<Rational> joined=new ArrayList<>(values); joined.addAll(other.values); return new RationalSample(joined);
    }
    public RationalSample scale(Rational scalar) {
        List<Rational> result=new ArrayList<>();
        for(Rational value : values) result.add(value.multiply(scalar));
        return new RationalSample(result);
    }
    public RationalSample centered() {
        Rational mean=ExactStatistics.mean(values); List<Rational> result=new ArrayList<>();
        for(Rational value : values) result.add(value.subtract(mean));
        return new RationalSample(result);
    }
    @Override public boolean equals(Object other) { return other instanceof RationalSample && values.equals(((RationalSample)other).values); }
    @Override public int hashCode() { return values.hashCode(); }
    @Override public String toString() { return "Sample"+values; }
}

