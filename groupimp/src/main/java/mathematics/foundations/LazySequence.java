package mathematics.foundations;

import mathematics.core.*;
import java.math.BigInteger;
import java.util.*;

/** A zero-indexed sequence specified by a term rule, never implicitly collected. */
public final class LazySequence<T> {
    private final Domain<T> domain;
    private final Functions.Unary<BigInteger,T> term;
    public LazySequence(Domain<T> domain, Functions.Unary<BigInteger,T> term) { this.domain=Objects.requireNonNull(domain); this.term=Objects.requireNonNull(term); }
    public T at(BigInteger index) {
        if (index.signum() < 0) throw MathFailure.invalid("Sequence indices must be nonnegative");
        return domain.require(term.apply(index));
    }
    public List<T> prefix(int count) {
        if (count < 0) throw new IllegalArgumentException("Negative prefix length");
        List<T> result=new ArrayList<>();
        for (int i=0;i<count;i++) result.add(at(BigInteger.valueOf(i)));
        return Collections.unmodifiableList(result);
    }
}
