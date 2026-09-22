package mathematics.structures;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Components of a natural transformation between parallel finite functors, checked on every source arrow. */
public final class FiniteNaturalTransformation implements Serializable {
    private static final long serialVersionUID=1L;
    public final FiniteFunctor source,target;
    private final Map<BigInteger,BigInteger> components;

    public FiniteNaturalTransformation(FiniteFunctor source,FiniteFunctor target,Map<BigInteger,BigInteger> components) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
        if(!source.source.equals(target.source) || !source.target.equals(target.target))
            throw MathFailure.invalid("A natural transformation requires parallel functors with equal labelled categories");
        if(!Objects.requireNonNull(components).keySet().equals(source.source.objects().members()))
            throw MathFailure.invalid("There must be exactly one component for every source object");
        Map<BigInteger,BigInteger> copy=new TreeMap<>(); FiniteCategory codomain=source.target;
        for(BigInteger object : source.source.objects().members()) {
            BigInteger arrow=components.get(object);
            if(arrow==null || !codomain.arrows().containsKey(arrow)
                    || !codomain.source(arrow).equals(source.mapObject(object))
                    || !codomain.target(arrow).equals(target.mapObject(object)))
                throw MathFailure.invalid("Component has incompatible functor endpoints");
            copy.put(object,arrow);
        }
        for(BigInteger arrow : source.source.arrows().keySet()) {
            BigInteger a=source.source.source(arrow),b=source.source.target(arrow);
            if(!codomain.compose(source.mapArrow(arrow),copy.get(b)).equals(codomain.compose(copy.get(a),target.mapArrow(arrow))))
                throw MathFailure.invalid("Components do not satisfy naturality for every source arrow");
        }
        this.components=Collections.unmodifiableMap(copy);
    }
    public Map<BigInteger,BigInteger> componentMap() { return components; }
    public BigInteger component(BigInteger object) {
        BigInteger result=components.get(Objects.requireNonNull(object));
        if(result==null) throw MathFailure.undefined("Component requested at an unknown source object");
        return result;
    }
    public static FiniteNaturalTransformation identity(FiniteFunctor functor) {
        Map<BigInteger,BigInteger> components=new TreeMap<>();
        for(BigInteger object : functor.source.objects().members()) components.put(object,functor.target.identity(functor.mapObject(object)));
        return new FiniteNaturalTransformation(functor,functor,components);
    }
    /** Vertical composition: beta.compose(alpha) is alpha followed by beta. */
    public FiniteNaturalTransformation compose(FiniteNaturalTransformation before) { return before.andThen(this); }
    public FiniteNaturalTransformation andThen(FiniteNaturalTransformation after) {
        if(!target.equals(after.source)) throw MathFailure.undefined("Vertical composition requires equal middle functors");
        Map<BigInteger,BigInteger> result=new TreeMap<>();
        for(BigInteger object : components.keySet()) result.put(object,source.target.compose(components.get(object),after.component(object)));
        return new FiniteNaturalTransformation(source,after.target,result);
    }
    public boolean isIsomorphism() {
        for(BigInteger arrow : components.values()) if(!source.target.isIsomorphism(arrow)) return false;
        return true;
    }
    public FiniteNaturalTransformation inverse() {
        if(!isIsomorphism()) throw MathFailure.undefined("Natural inverse requires every component to be an isomorphism");
        Map<BigInteger,BigInteger> result=new TreeMap<>();
        for(Map.Entry<BigInteger,BigInteger> entry : components.entrySet())
            result.put(entry.getKey(),source.target.inverseOf(entry.getValue()).get(0));
        return new FiniteNaturalTransformation(target,source,result);
    }
    /** Opposite reverses the transformation as well as both functor categories. */
    public FiniteNaturalTransformation opposite() {
        return new FiniteNaturalTransformation(target.opposite(),source.opposite(),components);
    }
    /** Precompose both functors by before and pull components back along its object map. */
    public FiniteNaturalTransformation precompose(FiniteFunctor before) {
        if(!before.target.equals(source.source)) throw MathFailure.undefined("Precomposition requires the functor's target to equal the transformation domain");
        Map<BigInteger,BigInteger> result=new TreeMap<>();
        for(BigInteger object : before.source.objects().members()) result.put(object,component(before.mapObject(object)));
        return new FiniteNaturalTransformation(source.compose(before),target.compose(before),result);
    }
    /** Postcompose both functors by after and map components through its arrow map. */
    public FiniteNaturalTransformation postcompose(FiniteFunctor after) {
        if(!source.target.equals(after.source)) throw MathFailure.undefined("Postcomposition requires the functor's source to equal the transformation codomain");
        Map<BigInteger,BigInteger> result=new TreeMap<>();
        for(Map.Entry<BigInteger,BigInteger> entry : components.entrySet()) result.put(entry.getKey(),after.mapArrow(entry.getValue()));
        return new FiniteNaturalTransformation(after.compose(source),after.compose(target),result);
    }
    /** alpha:F=>G then beta:H=>K gives H.F=>K.G with component H(alpha_x); beta_(Gx). */
    public FiniteNaturalTransformation horizontal(FiniteNaturalTransformation after) {
        if(!source.target.equals(after.source.source)) throw MathFailure.undefined("Horizontal composition requires equal middle categories");
        Map<BigInteger,BigInteger> result=new TreeMap<>();
        for(BigInteger object : components.keySet()) result.put(object,
                after.source.target.compose(after.source.mapArrow(components.get(object)),after.component(target.mapObject(object))));
        return new FiniteNaturalTransformation(after.source.compose(source),after.target.compose(target),result);
    }
    public List<BigInteger> componentValues() { return Collections.unmodifiableList(new ArrayList<>(components.values())); }
    public List<BigInteger> componentFiber(BigInteger arrow) {
        if(!source.target.arrows().containsKey(Objects.requireNonNull(arrow))) throw MathFailure.undefined("Component fiber requires an existing codomain arrow");
        List<BigInteger> result=new ArrayList<>();
        for(Map.Entry<BigInteger,BigInteger> entry : components.entrySet()) if(entry.getValue().equals(arrow)) result.add(entry.getKey());
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteNaturalTransformation)) return false;
        FiniteNaturalTransformation n=(FiniteNaturalTransformation)other;
        return source.equals(n.source) && target.equals(n.target) && components.equals(n.components);
    }
    @Override public int hashCode() { return Objects.hash(source,target,components); }
    @Override public String toString() { return "NaturalTransformation(source="+source+", target="+target+", components="+components+")"; }
}
