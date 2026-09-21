package mathematics.foundations;

import java.math.BigInteger;
import java.util.*;

/** Finite multiplicities, with no sequence order or probability normalization implied. */
public final class Multiset<T> {
    private final Map<T,BigInteger> counts;
    public Multiset(Collection<T> input) {
        Map<T,BigInteger> result=new LinkedHashMap<>();
        for (T value : input) result.merge(Objects.requireNonNull(value),BigInteger.ONE,BigInteger::add);
        counts=Collections.unmodifiableMap(result);
    }
    public BigInteger count(T value) { return counts.getOrDefault(value,BigInteger.ZERO); }
    public Map<T,BigInteger> counts() { return counts; }
}
