package mathematics.foundations;

import mathematics.core.*;
import java.util.*;

public final class FiniteRelation<A,B> {
    public final Domain<A> source;
    public final Domain<B> target;
    public final FiniteSet<Pair<A,B>> pairs;
    public FiniteRelation(Domain<A> source, Domain<B> target, FiniteSet<Pair<A,B>> pairs) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target); this.pairs=Objects.requireNonNull(pairs);
        for (Pair<A,B> pair : pairs.members()) { source.require(pair.first); target.require(pair.second); }
    }
    public boolean relates(A a,B b) { source.require(a); target.require(b); return pairs.contains(new Pair<>(a,b)); }
    public FiniteRelation<B,A> inverse() {
        return new FiniteRelation<>(target,source,pairs.image(pair -> new Pair<>(pair.second,pair.first)));
    }
    public <C> FiniteRelation<A,C> andThen(FiniteRelation<B,C> next) {
        if (!target.compatibleWith(next.source)) throw new IllegalArgumentException("Relation middle domains differ");
        List<Pair<A,C>> result=new ArrayList<>();
        for (Pair<A,B> ab : pairs.members()) for (Pair<B,C> bc : next.pairs.members()) {
            if (ab.second.equals(bc.first)) result.add(new Pair<>(ab.first,bc.second));
        }
        return new FiniteRelation<>(source,next.target,new FiniteSet<>(result));
    }
    /** Only the explicitly supplied finite carrier is quantified. */
    public boolean isTotalFunctionOn(FiniteSet<A> carrier) {
        for (A value : carrier.members()) {
            source.require(value); int count=0;
            for (Pair<A,B> pair : pairs.members()) if (pair.first.equals(value)) count++;
            if (count != 1) return false;
        }
        for (Pair<A,B> pair : pairs.members()) if (!carrier.contains(pair.first)) return false;
        return true;
    }
}
