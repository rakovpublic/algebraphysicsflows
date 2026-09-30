package mathematics.foundations;

import mathematics.core.*;
import java.io.Serializable;
import java.util.*;

/** Finite extensional sets of canonical Java-equality members. Other equivalences need explicit quotients. */
public final class FiniteSet<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Set<T> members;
    public FiniteSet(Collection<T> values) {
        Set<T> copy=new LinkedHashSet<>(values);
        for (T value : copy) Objects.requireNonNull(value);
        members=Collections.unmodifiableSet(copy);
    }
    @SafeVarargs public static <T> FiniteSet<T> of(T... values) { return new FiniteSet<>(Arrays.asList(values)); }
    public Set<T> members() { return members; }
    public int size() { return members.size(); }
    public boolean contains(T member) { return members.contains(member); }
    public boolean subsetOf(FiniteSet<T> other) { return other.members.containsAll(members); }
    public FiniteSet<T> union(FiniteSet<T> b) { Set<T> result=new LinkedHashSet<>(members); result.addAll(b.members); return new FiniteSet<>(result); }
    public FiniteSet<T> intersection(FiniteSet<T> b) { Set<T> result=new LinkedHashSet<>(members); result.retainAll(b.members); return new FiniteSet<>(result); }
    public FiniteSet<T> difference(FiniteSet<T> b) { Set<T> result=new LinkedHashSet<>(members); result.removeAll(b.members); return new FiniteSet<>(result); }
    public FiniteSet<T> complementIn(FiniteSet<T> universe) {
        if (!subsetOf(universe)) throw MathFailure.invalid("Set is not contained in the declared universe");
        return universe.difference(this);
    }
    public <B> FiniteSet<Pair<T,B>> product(FiniteSet<B> b) {
        List<Pair<T,B>> result=new ArrayList<>();
        for (T a : members) for (B value : b.members) result.add(new Pair<>(a,value));
        return new FiniteSet<>(result);
    }
    public <B> FiniteSet<B> image(Functions.Unary<T,B> function) {
        List<B> values=new ArrayList<>(); for (T value : members) values.add(function.apply(value));
        return new FiniteSet<>(values);
    }
    public <B> FiniteSet<T> preimage(Functions.Unary<T,B> function, FiniteSet<B> target) {
        List<T> values=new ArrayList<>(); for (T value : members) if (target.contains(function.apply(value))) values.add(value);
        return new FiniteSet<>(values);
    }
    public FiniteSet<FiniteSet<T>> powerSet() {
        if (size()>20) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Power set materialization limited to 20 elements; use a declarative domain for larger sets");
        List<FiniteSet<T>> result=new ArrayList<>(); result.add(FiniteSet.of());
        for (T value : members) {
            int count=result.size();
            for (int i=0;i<count;i++) result.add(result.get(i).union(FiniteSet.of(value)));
        }
        return new FiniteSet<>(result);
    }
    public boolean every(Functions.Unary<T,Boolean> predicate) { for (T value : members) if (!predicate.apply(value)) return false; return true; }
    public boolean some(Functions.Unary<T,Boolean> predicate) { for (T value : members) if (predicate.apply(value)) return true; return false; }
    @Override public boolean equals(Object b) { return b instanceof FiniteSet && members.equals(((FiniteSet<?>)b).members); }
    @Override public int hashCode() { return members.hashCode(); }
    @Override public String toString() { return members.toString(); }
}
