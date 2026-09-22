package mathematics.structures;

import mathematics.core.MathFailure;
import mathematics.foundations.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A covariant functor between complete finite category tables, checked on every arrow and composition. */
public final class FiniteFunctor implements Serializable {
    private static final long serialVersionUID=1L;
    public final FiniteCategory source,target;
    private final Map<BigInteger,BigInteger> objects,arrows;

    public FiniteFunctor(FiniteCategory source,FiniteCategory target,Map<BigInteger,BigInteger> objects,
                         Map<BigInteger,BigInteger> arrows) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
        this.objects=checkedMap(objects,source.objects(),target.objects(),"object");
        this.arrows=checkedMap(arrows,source.arrowLabels(),target.arrowLabels(),"arrow");
        for(BigInteger arrow : source.arrows().keySet()) {
            BigInteger image=this.arrows.get(arrow);
            if(!target.source(image).equals(this.objects.get(source.source(arrow)))
                    || !target.target(image).equals(this.objects.get(source.target(arrow))))
                throw MathFailure.invalid("Functor arrow map does not preserve source and target");
        }
        for(BigInteger object : source.objects().members())
            if(!this.arrows.get(source.identity(object)).equals(target.identity(this.objects.get(object))))
                throw MathFailure.invalid("Functor does not preserve identities");
        for(Map.Entry<Pair<BigInteger,BigInteger>,BigInteger> entry : source.composition().entrySet())
            if(!this.arrows.get(entry.getValue()).equals(target.compose(this.arrows.get(entry.getKey().first),this.arrows.get(entry.getKey().second))))
                throw MathFailure.invalid("Functor does not preserve composition");
    }
    private static Map<BigInteger,BigInteger> checkedMap(Map<BigInteger,BigInteger> map,
            FiniteSet<BigInteger> domain,FiniteSet<BigInteger> codomain,String role) {
        if(!Objects.requireNonNull(map).keySet().equals(domain.members()))
            throw MathFailure.invalid("Functor "+role+" map must cover exactly its source labels");
        Map<BigInteger,BigInteger> copy=new TreeMap<>();
        for(BigInteger label : domain.members()) {
            BigInteger image=map.get(label);
            if(!codomain.contains(image)) throw MathFailure.invalid("Functor "+role+" image is outside its target category");
            copy.put(label,image);
        }
        return Collections.unmodifiableMap(copy);
    }
    public Map<BigInteger,BigInteger> objectMap() { return objects; }
    public Map<BigInteger,BigInteger> arrowMap() { return arrows; }
    private static BigInteger apply(Map<BigInteger,BigInteger> map,BigInteger label) {
        BigInteger image=map.get(Objects.requireNonNull(label));
        if(image==null) throw MathFailure.undefined("Unknown source label for functor application");
        return image;
    }
    public BigInteger mapObject(BigInteger object) { return apply(objects,object); }
    public BigInteger mapArrow(BigInteger arrow) { return apply(arrows,arrow); }
    /** this after before: F.compose(G) is F(G(-)). */
    public FiniteFunctor compose(FiniteFunctor before) { return before.andThen(this); }
    public FiniteFunctor andThen(FiniteFunctor after) {
        if(!target.equals(after.source)) throw MathFailure.undefined("Functor composition requires equal labelled middle categories");
        Map<BigInteger,BigInteger> objectMap=new TreeMap<>(),arrowMap=new TreeMap<>();
        for(BigInteger label : objects.keySet()) objectMap.put(label,after.mapObject(objects.get(label)));
        for(BigInteger label : arrows.keySet()) arrowMap.put(label,after.mapArrow(arrows.get(label)));
        return new FiniteFunctor(source,after.target,objectMap,arrowMap);
    }
    public static FiniteFunctor identity(FiniteCategory category) {
        Map<BigInteger,BigInteger> objects=new TreeMap<>(),arrows=new TreeMap<>();
        for(BigInteger label : category.objects().members()) objects.put(label,label);
        for(BigInteger label : category.arrows().keySet()) arrows.put(label,label);
        return new FiniteFunctor(category,category,objects,arrows);
    }
    public static FiniteFunctor fromDiscreteMap(FiniteFunction<BigInteger,BigInteger> function) {
        return new FiniteFunctor(FiniteCategory.discrete(function.domain),FiniteCategory.discrete(function.codomain),
                function.mapping(),function.mapping());
    }
    public FiniteFunctor opposite() { return new FiniteFunctor(source.opposite(),target.opposite(),objects,arrows); }
    public boolean isFaithful() {
        for(BigInteger a : source.objects().members()) for(BigInteger b : source.objects().members()) {
            Set<BigInteger> images=new HashSet<>();
            for(BigInteger arrow : source.hom(a,b)) if(!images.add(arrows.get(arrow))) return false;
        }
        return true;
    }
    public boolean isFull() {
        for(BigInteger a : source.objects().members()) for(BigInteger b : source.objects().members()) {
            Set<BigInteger> images=new HashSet<>();
            for(BigInteger arrow : source.hom(a,b)) images.add(arrows.get(arrow));
            if(!images.equals(new HashSet<>(target.hom(objects.get(a),objects.get(b))))) return false;
        }
        return true;
    }
    public boolean isEssentiallySurjective() {
        Set<BigInteger> images=new HashSet<>(objects.values());
        for(BigInteger object : target.objects().members()) {
            boolean reached=false;
            for(BigInteger image : images) {
                if(image.equals(object)) { reached=true; break; }
                for(BigInteger arrow : target.hom(image,object)) if(target.isIsomorphism(arrow)) { reached=true; break; }
                if(reached) break;
            }
            if(!reached) return false;
        }
        return true;
    }
    /** Finite fully-faithful/essentially-surjective criterion; no quasi-inverse is constructed. */
    public boolean isEquivalence() { return isFaithful() && isFull() && isEssentiallySurjective(); }
    /** Strict isomorphism of the labelled category tables, stronger than equivalence. */
    public boolean isIsomorphism() {
        return bijective(objects,target.objects().size()) && bijective(arrows,target.arrows().size());
    }
    private static boolean bijective(Map<BigInteger,BigInteger> map,int targetSize) {
        return map.size()==targetSize && new HashSet<>(map.values()).size()==targetSize;
    }
    public FiniteFunctor inverse() {
        if(!isIsomorphism()) throw MathFailure.undefined("Strict functor inverse requires bijections on both objects and arrows");
        Map<BigInteger,BigInteger> inverseObjects=new TreeMap<>(),inverseArrows=new TreeMap<>();
        for(Map.Entry<BigInteger,BigInteger> entry : objects.entrySet()) inverseObjects.put(entry.getValue(),entry.getKey());
        for(Map.Entry<BigInteger,BigInteger> entry : arrows.entrySet()) inverseArrows.put(entry.getValue(),entry.getKey());
        return new FiniteFunctor(target,source,inverseObjects,inverseArrows);
    }
    public List<BigInteger> objectImages() { return Collections.unmodifiableList(new ArrayList<>(objects.values())); }
    public List<BigInteger> arrowImages() { return Collections.unmodifiableList(new ArrayList<>(arrows.values())); }
    private static List<BigInteger> fiber(Map<BigInteger,BigInteger> map,FiniteSet<BigInteger> target,BigInteger label) {
        if(!target.contains(Objects.requireNonNull(label))) throw MathFailure.undefined("Fiber label is outside the target category");
        List<BigInteger> result=new ArrayList<>();
        for(Map.Entry<BigInteger,BigInteger> entry : map.entrySet()) if(entry.getValue().equals(label)) result.add(entry.getKey());
        return Collections.unmodifiableList(result);
    }
    public List<BigInteger> objectFiber(BigInteger label) { return fiber(objects,target.objects(),label); }
    public List<BigInteger> arrowFiber(BigInteger label) { return fiber(arrows,target.arrowLabels(),label); }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteFunctor)) return false;
        FiniteFunctor f=(FiniteFunctor)other;
        return source.equals(f.source) && target.equals(f.target) && objects.equals(f.objects) && arrows.equals(f.arrows);
    }
    @Override public int hashCode() { return Objects.hash(source,target,objects,arrows); }
    @Override public String toString() { return "Functor(source="+source+", target="+target+", objects="+objects+", arrows="+arrows+")"; }
}
