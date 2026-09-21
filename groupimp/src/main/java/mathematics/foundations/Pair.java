package mathematics.foundations;
import java.io.Serializable;
import java.util.Objects;
public final class Pair<A,B> implements Serializable {
    private static final long serialVersionUID = 1L;
    public final A first; public final B second;
    public Pair(A first,B second) { this.first=Objects.requireNonNull(first); this.second=Objects.requireNonNull(second); }
    @Override public boolean equals(Object b) { return b instanceof Pair && first.equals(((Pair<?,?>)b).first) && second.equals(((Pair<?,?>)b).second); }
    @Override public int hashCode() { return Objects.hash(first,second); }
    @Override public String toString() { return "(" + first + "," + second + ")"; }
}
