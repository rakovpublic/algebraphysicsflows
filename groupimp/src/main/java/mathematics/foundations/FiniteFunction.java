package mathematics.foundations;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import java.io.Serializable;
import java.util.*;
import static operations.OperationMembers.require;

/** A total function between explicit finite subsets of two actual Algebra instances. */
public final class FiniteFunction<A,B> implements Serializable {
    private static final long serialVersionUID=1L;
    public final Algebra<A> source;
    public final Algebra<B> target;
    public final FiniteSet<A> domain;
    public final FiniteSet<B> codomain;
    private final Map<A,B> mapping;

    public FiniteFunction(Algebra<A> source,Algebra<B> target,FiniteSet<A> domain,
                          FiniteSet<B> codomain,Map<A,B> mapping) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
        this.domain=Objects.requireNonNull(domain); this.codomain=Objects.requireNonNull(codomain);
        for(A value : domain.members()) require(source,value);
        for(B value : codomain.members()) require(target,value);
        if(!domain.members().equals(Objects.requireNonNull(mapping).keySet()))
            throw MathFailure.invalid("Function keys must equal its entire declared finite domain");
        Map<A,B> copy=new LinkedHashMap<>();
        for(A value : domain.members()) {
            B result=mapping.get(value);
            if(!codomain.contains(result)) throw MathFailure.invalid("Function value is outside its declared finite codomain");
            copy.put(value,result);
        }
        this.mapping=Collections.unmodifiableMap(copy);
    }
    public Map<A,B> mapping() { return mapping; }
    public B apply(A value) {
        require(source,value);
        if(!domain.contains(value)) throw MathFailure.undefined("Point is outside the function's finite domain");
        return mapping.get(value);
    }
    /** this after before; the declared middle finite sets must agree exactly. */
    public <C> FiniteFunction<C,B> compose(FiniteFunction<C,A> before) { return before.andThen(this); }
    public <C> FiniteFunction<A,C> andThen(FiniteFunction<B,C> next) {
        if(target!=next.source || !codomain.equals(next.domain))
            throw MathFailure.undefined("Function composition requires the same middle Algebra and finite set");
        Map<A,C> result=new LinkedHashMap<>();
        for(A value : domain.members()) result.put(value,next.apply(mapping.get(value)));
        return new FiniteFunction<>(source,next.target,domain,next.codomain,result);
    }
    public FiniteSet<B> range() { return new FiniteSet<>(mapping.values()); }
    public boolean isInjective() { return range().size()==domain.size(); }
    public boolean isSurjective() { return range().equals(codomain); }
    public boolean isBijective() { return isInjective() && isSurjective(); }
    public FiniteFunction<B,A> inverse() {
        if(!isBijective()) throw MathFailure.undefined("Only a bijection has an inverse on its declared codomain");
        Map<B,A> result=new LinkedHashMap<>();
        for(Map.Entry<A,B> entry : mapping.entrySet()) result.put(entry.getValue(),entry.getKey());
        return new FiniteFunction<>(target,source,codomain,domain,result);
    }
    public FiniteSet<B> image(FiniteSet<A> subset) {
        if(!subset.subsetOf(domain)) throw MathFailure.undefined("Image input must be a subset of the finite domain");
        List<B> result=new ArrayList<>();
        for(A value : subset.members()) result.add(apply(value));
        return new FiniteSet<>(result);
    }
    public FiniteSet<A> preimage(FiniteSet<B> subset) {
        if(!subset.subsetOf(codomain)) throw MathFailure.undefined("Preimage input must be a subset of the declared finite codomain");
        List<A> result=new ArrayList<>();
        for(A value : domain.members()) if(subset.contains(mapping.get(value))) result.add(value);
        return new FiniteSet<>(result);
    }
    /** Retains the original codomain, including values that restriction makes unreachable. */
    public FiniteFunction<A,B> restrict(FiniteSet<A> subset) {
        if(!subset.subsetOf(domain)) throw MathFailure.undefined("Restriction input must be a subset of the finite domain");
        Map<A,B> result=new LinkedHashMap<>();
        for(A value : subset.members()) result.put(value,mapping.get(value));
        return new FiniteFunction<>(source,target,subset,codomain,result);
    }
    /** Domain iteration order, retaining equal images of different arguments. */
    public List<B> values() { return Collections.unmodifiableList(new ArrayList<>(mapping.values())); }
    public List<A> preimageOf(B value) {
        require(target,value);
        if(!codomain.contains(value)) throw MathFailure.undefined("Fiber point must belong to the declared finite codomain");
        return Collections.unmodifiableList(new ArrayList<>(preimage(FiniteSet.of(value)).members()));
    }
    public FiniteRelation<A,B> graph() {
        List<Pair<A,B>> pairs=new ArrayList<>();
        for(Map.Entry<A,B> entry : mapping.entrySet()) pairs.add(new Pair<>(entry.getKey(),entry.getValue()));
        return new FiniteRelation<>(source,target,new FiniteSet<>(pairs));
    }
    public static <T> FiniteFunction<T,T> identity(Algebra<T> algebra,FiniteSet<T> domain) {
        Map<T,T> result=new LinkedHashMap<>();
        for(T value : domain.members()) result.put(value,value);
        return new FiniteFunction<>(algebra,algebra,domain,domain,result);
    }
    public static <A,B> FiniteFunction<A,B> fromRelation(FiniteRelation<A,B> relation,
                                                       FiniteSet<A> domain,FiniteSet<B> codomain) {
        if(!relation.isTotalFunctionOn(domain) || !relation.range().subsetOf(codomain))
            throw MathFailure.undefined("Relation must specify exactly one value per domain member, all in the declared codomain");
        Map<A,B> result=new LinkedHashMap<>();
        for(Pair<A,B> pair : relation.pairs.members()) result.put(pair.first,pair.second);
        return new FiniteFunction<>(relation.source,relation.target,domain,codomain,result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteFunction)) return false;
        FiniteFunction<?,?> f=(FiniteFunction<?,?>)other;
        return source==f.source && target==f.target && domain.equals(f.domain)
                && codomain.equals(f.codomain) && mapping.equals(f.mapping);
    }
    @Override public int hashCode() { return Objects.hash(source,target,domain,codomain,mapping); }
    @Override public String toString() { return "Function"+domain+"->"+codomain+mapping; }
}
