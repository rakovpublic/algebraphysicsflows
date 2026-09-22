package mathematics.structures;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** An adjoint equivalence of finite category tables with explicit, checked unit and counit. */
public final class FiniteEquivalence implements Serializable {
    private static final long serialVersionUID=1L;
    public final FiniteFunctor forward,backward;
    public final FiniteNaturalTransformation unit,counit;

    public FiniteEquivalence(FiniteFunctor forward,FiniteFunctor backward,
                             FiniteNaturalTransformation unit,FiniteNaturalTransformation counit) {
        this.forward=Objects.requireNonNull(forward); this.backward=Objects.requireNonNull(backward);
        this.unit=Objects.requireNonNull(unit); this.counit=Objects.requireNonNull(counit);
        if(!forward.source.equals(backward.target) || !forward.target.equals(backward.source))
            throw MathFailure.invalid("Equivalence functors must have opposite category boundaries");
        if(!unit.source.equals(FiniteFunctor.identity(forward.source)) || !unit.target.equals(backward.compose(forward))
                || !counit.source.equals(forward.compose(backward)) || !counit.target.equals(FiniteFunctor.identity(forward.target)))
            throw MathFailure.invalid("Equivalence unit or counit has incorrect functor boundaries");
        if(!unit.isIsomorphism() || !counit.isIsomorphism()) throw MathFailure.invalid("Equivalence unit and counit must be natural isomorphisms");
        for(BigInteger object : forward.source.objects().members())
            if(!forward.target.compose(forward.mapArrow(unit.component(object)),counit.component(forward.mapObject(object)))
                    .equals(forward.target.identity(forward.mapObject(object))))
                throw MathFailure.invalid("Equivalence violates the forward triangle identity");
        for(BigInteger object : forward.target.objects().members())
            if(!forward.source.compose(unit.component(backward.mapObject(object)),backward.mapArrow(counit.component(object)))
                    .equals(forward.source.identity(backward.mapObject(object))))
                throw MathFailure.invalid("Equivalence violates the backward triangle identity");
    }
    public static FiniteEquivalence identity(FiniteCategory category) {
        FiniteFunctor f=FiniteFunctor.identity(category); FiniteNaturalTransformation id=FiniteNaturalTransformation.identity(f);
        return new FiniteEquivalence(f,f,id,id);
    }
    /** Choose a deterministic finite quasi-inverse, preferring exact image objects over isomorphic ones. */
    public static FiniteEquivalence fromFunctor(FiniteFunctor f) {
        if(!f.isEquivalence()) throw MathFailure.undefined("Witness construction requires a full, faithful and essentially surjective functor");
        Map<BigInteger,BigInteger> objects=new TreeMap<>(),epsilon=new TreeMap<>(),epsilonInverse=new TreeMap<>();
        for(BigInteger object : f.target.objects().members()) {
            BigInteger chosen=null,iso=null;
            for(BigInteger candidate : f.source.objects().members()) if(f.mapObject(candidate).equals(object)) {
                chosen=candidate; iso=f.target.identity(object); break;
            }
            if(chosen==null) {
                for(BigInteger candidate : f.source.objects().members()) {
                    for(BigInteger arrow : f.target.hom(f.mapObject(candidate),object)) if(f.target.isIsomorphism(arrow)) {
                        chosen=candidate; iso=arrow; break;
                    }
                    if(chosen!=null) break;
                }
            }
            if(chosen==null) throw constructionFailure("No representative for an essentially surjective functor");
            objects.put(object,chosen); epsilon.put(object,iso); epsilonInverse.put(object,f.target.inverseOf(iso).get(0));
        }
        Map<BigInteger,BigInteger> arrows=new TreeMap<>();
        for(BigInteger arrow : f.target.arrows().keySet()) {
            BigInteger source=f.target.source(arrow),target=f.target.target(arrow);
            BigInteger conjugated=f.target.compose(f.target.compose(epsilon.get(source),arrow),epsilonInverse.get(target));
            arrows.put(arrow,lift(f,objects.get(source),objects.get(target),conjugated));
        }
        FiniteFunctor g=new FiniteFunctor(f.target,f.source,objects,arrows);
        Map<BigInteger,BigInteger> eta=new TreeMap<>();
        for(BigInteger object : f.source.objects().members())
            eta.put(object,lift(f,object,g.mapObject(f.mapObject(object)),epsilonInverse.get(f.mapObject(object))));
        FiniteNaturalTransformation unit=new FiniteNaturalTransformation(FiniteFunctor.identity(f.source),g.compose(f),eta);
        FiniteNaturalTransformation counit=new FiniteNaturalTransformation(f.compose(g),FiniteFunctor.identity(f.target),epsilon);
        return new FiniteEquivalence(f,g,unit,counit);
    }
    private static BigInteger lift(FiniteFunctor f,BigInteger source,BigInteger target,BigInteger arrow) {
        BigInteger result=null;
        for(BigInteger candidate : f.source.hom(source,target)) if(f.mapArrow(candidate).equals(arrow)) {
            if(result!=null) throw constructionFailure("Nonunique lift for a faithful functor");
            result=candidate;
        }
        if(result==null) throw constructionFailure("Missing lift for a full functor");
        return result;
    }
    private static MathFailure constructionFailure(String message) { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,message); }
    public FiniteEquivalence inverse() { return new FiniteEquivalence(backward,forward,counit.inverse(),unit.inverse()); }
    public FiniteEquivalence opposite() {
        return new FiniteEquivalence(forward.opposite(),backward.opposite(),unit.opposite().inverse(),counit.opposite().inverse());
    }
    /** this after before, composing the supplied witnesses rather than choosing new ones. */
    public FiniteEquivalence compose(FiniteEquivalence before) {
        if(!before.forward.target.equals(forward.source)) throw MathFailure.undefined("Equivalence composition requires equal labelled middle categories");
        FiniteNaturalTransformation composedUnit=before.unit.andThen(unit.precompose(before.forward).postcompose(before.backward));
        FiniteNaturalTransformation composedCounit=before.counit.precompose(backward).postcompose(forward).andThen(counit);
        return new FiniteEquivalence(forward.compose(before.forward),before.backward.compose(backward),composedUnit,composedCounit);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteEquivalence)) return false;
        FiniteEquivalence e=(FiniteEquivalence)other;
        return forward.equals(e.forward) && backward.equals(e.backward) && unit.equals(e.unit) && counit.equals(e.counit);
    }
    @Override public int hashCode() { return Objects.hash(forward,backward,unit,counit); }
    @Override public String toString() { return "Equivalence(forward="+forward+", backward="+backward+", unit="+unit.componentMap()+", counit="+counit.componentMap()+")"; }
}
