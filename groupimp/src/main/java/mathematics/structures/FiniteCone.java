package mathematics.structures;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A cone over a finite diagram, with exhaustive bounded searches for the limit universal property. */
public final class FiniteCone implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_CONES=10000;
    public static final int MAX_SEARCH_STEPS=1000000;
    public final FiniteFunctor diagram;
    public final BigInteger vertex;
    private final FiniteNaturalTransformation transformation;

    public FiniteCone(FiniteFunctor diagram,BigInteger vertex,Map<BigInteger,BigInteger> legs) {
        this.diagram=Objects.requireNonNull(diagram); this.vertex=Objects.requireNonNull(vertex);
        if(!diagram.target.objects().contains(vertex)) throw MathFailure.invalid("Cone vertex must belong to the diagram's target category");
        transformation=new FiniteNaturalTransformation(FiniteFunctor.constant(diagram.source,diagram.target,vertex),diagram,legs);
    }
    private FiniteCone(BigInteger vertex,FiniteNaturalTransformation checkedLegs) {
        this.vertex=vertex; this.transformation=checkedLegs; this.diagram=checkedLegs.target;
    }
    public static FiniteCone fromTransformation(FiniteNaturalTransformation legs,BigInteger vertex) {
        if(!legs.source.equals(FiniteFunctor.constant(legs.target.source,legs.target.target,vertex)))
            throw MathFailure.undefined("A cone transformation must start at the constant diagram on the supplied vertex");
        return new FiniteCone(vertex,legs);
    }
    public Map<BigInteger,BigInteger> legMap() { return transformation.componentMap(); }
    public BigInteger leg(BigInteger object) { return transformation.component(object); }
    public List<BigInteger> legValues() { return transformation.componentValues(); }
    public FiniteNaturalTransformation asTransformation() { return transformation; }
    public FiniteCone reindex(FiniteFunctor before) { return new FiniteCone(vertex,transformation.precompose(before)); }
    /** A functor maps cones, but need not preserve their limit property. */
    public FiniteCone map(FiniteFunctor after) {
        if(!diagram.target.equals(after.source)) throw MathFailure.undefined("Cone mapping requires equal labelled middle categories");
        return new FiniteCone(after.mapObject(vertex),transformation.postcompose(after));
    }
    /** All arrows from other.vertex to this.vertex commuting with the legs, in ascending label order. */
    public List<BigInteger> mediatorsFrom(FiniteCone other) {
        requireSameDiagram(other);
        return Collections.unmodifiableList(mediators(other,new Budget()));
    }
    private void requireSameDiagram(FiniteCone other) {
        if(!diagram.equals(other.diagram)) throw MathFailure.undefined("Cone comparison requires exactly the same labelled diagram");
    }
    private List<BigInteger> mediators(FiniteCone other,Budget budget) {
        budget.step(); List<BigInteger> result=new ArrayList<>();
        for(BigInteger arrow : diagram.target.hom(other.vertex,vertex)) {
            budget.step(); boolean commutes=true;
            for(BigInteger object : diagram.source.objects().members()) {
                budget.step();
                if(!diagram.target.compose(arrow,leg(object)).equals(other.leg(object))) { commutes=false; break; }
            }
            if(commutes) result.add(arrow);
        }
        return result;
    }
    private boolean universal(List<FiniteCone> cones,Budget budget) {
        for(FiniteCone other : cones) if(mediators(other,budget).size()!=1) return false;
        return true;
    }
    public boolean isLimit() {
        Budget budget=new Budget(); return universal(all(diagram,budget),budget);
    }
    /** Require the entire universal property, not just a unique arrow for this particular cone. */
    public BigInteger lift(FiniteCone other) {
        requireSameDiagram(other); Budget budget=new Budget();
        if(!universal(all(diagram,budget),budget)) throw MathFailure.undefined("Lift requires a limit cone");
        return mediators(other,budget).get(0);
    }
    /** Least vertex, then lexicographically least leg labels among all validated limit cones. */
    public static FiniteCone limit(FiniteFunctor diagram) {
        Budget budget=new Budget(); List<FiniteCone> cones=all(diagram,budget);
        for(FiniteCone candidate : cones) if(candidate.universal(cones,budget)) return candidate;
        throw MathFailure.undefined("The finite diagram has no limit in its target category");
    }
    public static List<FiniteCone> at(FiniteFunctor diagram,BigInteger vertex) {
        List<FiniteCone> result=new ArrayList<>(); enumerate(diagram,vertex,new Budget(),result);
        return Collections.unmodifiableList(result);
    }
    private static List<FiniteCone> all(FiniteFunctor diagram,Budget budget) {
        List<FiniteCone> result=new ArrayList<>();
        for(BigInteger vertex : diagram.target.objects().members()) enumerate(diagram,vertex,budget,result);
        return result;
    }
    private static void enumerate(FiniteFunctor diagram,BigInteger vertex,Budget budget,List<FiniteCone> result) {
        FiniteFunctor constant=FiniteFunctor.constant(diagram.source,diagram.target,vertex);
        List<BigInteger> objects=new ArrayList<>(diagram.source.objects().members());
        List<List<BigInteger>> choices=new ArrayList<>();
        for(BigInteger object : objects) choices.add(diagram.target.hom(vertex,diagram.mapObject(object)));
        enumerate(diagram,constant,vertex,objects,choices,0,new TreeMap<>(),budget,result);
    }
    private static void enumerate(FiniteFunctor diagram,FiniteFunctor constant,BigInteger vertex,List<BigInteger> objects,
            List<List<BigInteger>> choices,int index,Map<BigInteger,BigInteger> legs,Budget budget,List<FiniteCone> result) {
        budget.step();
        if(index==objects.size()) {
            budget.cone(); result.add(new FiniteCone(vertex,new FiniteNaturalTransformation(constant,diagram,legs))); return;
        }
        BigInteger object=objects.get(index);
        for(BigInteger choice : choices.get(index)) {
            budget.step(); legs.put(object,choice);
            boolean compatible=true;
            for(BigInteger arrow : diagram.source.arrows().keySet()) {
                BigInteger first=legs.get(diagram.source.source(arrow)),second=legs.get(diagram.source.target(arrow));
                budget.step();
                if(first!=null && second!=null && !diagram.target.compose(first,diagram.mapArrow(arrow)).equals(second)) {
                    compatible=false; break;
                }
            }
            if(compatible) enumerate(diagram,constant,vertex,objects,choices,index+1,legs,budget,result);
        }
        legs.remove(object);
    }
    private static final class Budget {
        private int steps,cones;
        void step() { if(++steps>MAX_SEARCH_STEPS) throw exhausted("Finite cone search exceeds 1000000 search steps"); }
        void cone() { if(++cones>MAX_CONES) throw exhausted("Finite cone search exceeds 10000 cones"); }
        private MathFailure exhausted(String message) { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,message); }
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteCone)) return false;
        FiniteCone c=(FiniteCone)other;
        return vertex.equals(c.vertex) && diagram.equals(c.diagram) && legMap().equals(c.legMap());
    }
    @Override public int hashCode() { return Objects.hash(diagram,vertex,legMap()); }
    @Override public String toString() { return "Cone(diagram="+diagram+", vertex="+vertex+", legs="+legMap()+")"; }
}
