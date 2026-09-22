package mathematics.foundations;

import mathematics.core.*;
import algebra.imp.Algebra;
import java.io.Serializable;
import static operations.OperationMembers.require;
import java.util.*;

public final class FiniteRelation<A,B> implements Serializable {
    private static final long serialVersionUID=1L;
    public final Algebra<A> source;
    public final Algebra<B> target;
    public final FiniteSet<Pair<A,B>> pairs;
    public FiniteRelation(Domain<A> source, Domain<B> target, FiniteSet<Pair<A,B>> pairs) {
        this(source.algebra(),target.algebra(),pairs);
    }
    public FiniteRelation(Algebra<A> source,Algebra<B> target,FiniteSet<Pair<A,B>> pairs) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target); this.pairs=Objects.requireNonNull(pairs);
        for (Pair<A,B> pair : pairs.members()) { require(source,pair.first); require(target,pair.second); }
    }
    public boolean relates(A a,B b) { require(source,a); require(target,b); return pairs.contains(new Pair<>(a,b)); }
    public FiniteRelation<B,A> inverse() {
        return new FiniteRelation<>(target,source,pairs.image(pair -> new Pair<>(pair.second,pair.first)));
    }
    public <C> FiniteRelation<A,C> andThen(FiniteRelation<B,C> next) {
        if (target!=next.source) throw new IllegalArgumentException("Relation middle algebras differ");
        List<Pair<A,C>> result=new ArrayList<>();
        for (Pair<A,B> ab : pairs.members()) for (Pair<B,C> bc : next.pairs.members()) {
            if (ab.second.equals(bc.first)) result.add(new Pair<>(ab.first,bc.second));
        }
        return new FiniteRelation<>(source,next.target,new FiniteSet<>(result));
    }
    /** Only the explicitly supplied finite carrier is quantified. */
    public boolean isTotalFunctionOn(FiniteSet<A> carrier) {
        for (A value : carrier.members()) {
            require(source,value); int count=0;
            for (Pair<A,B> pair : pairs.members()) if (pair.first.equals(value)) count++;
            if (count != 1) return false;
        }
        for (Pair<A,B> pair : pairs.members()) if (!carrier.contains(pair.first)) return false;
        return true;
    }
    private void sameCarriers(FiniteRelation<A,B> other) {
        if(source!=other.source || target!=other.target) throw new IllegalArgumentException("Relation algebras differ");
    }
    public FiniteRelation<A,B> union(FiniteRelation<A,B> other) {
        sameCarriers(other); return new FiniteRelation<>(source,target,pairs.union(other.pairs));
    }
    public FiniteRelation<A,B> intersection(FiniteRelation<A,B> other) {
        sameCarriers(other); return new FiniteRelation<>(source,target,pairs.intersection(other.pairs));
    }
    public boolean subrelationOf(FiniteRelation<A,B> other) { sameCarriers(other); return pairs.subsetOf(other.pairs); }
    public FiniteSet<A> domain() { return pairs.image(pair -> pair.first); }
    public FiniteSet<B> range() { return pairs.image(pair -> pair.second); }
    public FiniteSet<B> image(FiniteSet<A> input) {
        for(A value : input.members()) require(source,value);
        List<B> result=new ArrayList<>();
        for(Pair<A,B> pair : pairs.members()) if(input.contains(pair.first)) result.add(pair.second);
        return new FiniteSet<>(result);
    }
    public FiniteSet<A> preimage(FiniteSet<B> input) { return inverse().image(input); }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteRelation)) return false;
        FiniteRelation<?,?> relation=(FiniteRelation<?,?>)other;
        return source==relation.source && target==relation.target && pairs.equals(relation.pairs);
    }
    @Override public int hashCode() { return Objects.hash(source,target,pairs); }
    @Override public String toString() { return "Relation"+pairs; }
}
