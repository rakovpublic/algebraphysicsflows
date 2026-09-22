package mathematics.structures;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Finite L:C->D and R:D->C with a natural unit/counit satisfying both triangle identities. */
public final class FiniteAdjunction implements Serializable {
    private static final long serialVersionUID=1L;
    public final FiniteFunctor left,right;
    public final FiniteNaturalTransformation unit,counit;

    public FiniteAdjunction(FiniteFunctor left,FiniteFunctor right,
                            FiniteNaturalTransformation unit,FiniteNaturalTransformation counit) {
        this.left=Objects.requireNonNull(left); this.right=Objects.requireNonNull(right);
        this.unit=Objects.requireNonNull(unit); this.counit=Objects.requireNonNull(counit);
        validate(left,right,unit,counit);
    }
    /** Also used by adjoint equivalences; invertibility is a separate, stronger obligation. */
    static void validate(FiniteFunctor left,FiniteFunctor right,
                         FiniteNaturalTransformation unit,FiniteNaturalTransformation counit) {
        if(!left.source.equals(right.target) || !left.target.equals(right.source))
            throw MathFailure.invalid("Adjunction functors must have opposite category boundaries");
        if(!unit.source.equals(FiniteFunctor.identity(left.source)) || !unit.target.equals(right.compose(left))
                || !counit.source.equals(left.compose(right)) || !counit.target.equals(FiniteFunctor.identity(left.target)))
            throw MathFailure.invalid("Adjunction unit or counit has incorrect functor boundaries");
        for(BigInteger object : left.source.objects().members())
            if(!left.target.compose(left.mapArrow(unit.component(object)),counit.component(left.mapObject(object)))
                    .equals(left.target.identity(left.mapObject(object))))
                throw MathFailure.invalid("Adjunction violates the left triangle identity");
        for(BigInteger object : left.target.objects().members())
            if(!left.source.compose(unit.component(right.mapObject(object)),right.mapArrow(counit.component(object)))
                    .equals(left.source.identity(right.mapObject(object))))
                throw MathFailure.invalid("Adjunction violates the right triangle identity");
    }
    public static FiniteAdjunction identity(FiniteCategory category) {
        FiniteFunctor id=FiniteFunctor.identity(category); FiniteNaturalTransformation identity=FiniteNaturalTransformation.identity(id);
        return new FiniteAdjunction(id,id,identity,identity);
    }
    public static FiniteAdjunction fromEquivalence(FiniteEquivalence equivalence) {
        return new FiniteAdjunction(equivalence.forward,equivalence.backward,equivalence.unit,equivalence.counit);
    }
    public boolean isEquivalence() { return unit.isIsomorphism() && counit.isIsomorphism(); }
    public FiniteEquivalence toEquivalence() {
        if(!isEquivalence()) throw MathFailure.undefined("Adjunction unit and counit must both be invertible to give an equivalence");
        return new FiniteEquivalence(left,right,unit,counit);
    }
    /** Hom_D(L(c),d) -> Hom_C(c,R(d)); c is explicit because L need not be injective on objects. */
    public BigInteger transpose(BigInteger sourceObject,BigInteger targetArrow) {
        if(!left.target.source(targetArrow).equals(left.mapObject(sourceObject)))
            throw MathFailure.undefined("Transpose requires an arrow with source L(c)");
        return left.source.compose(unit.component(sourceObject),right.mapArrow(targetArrow));
    }
    /** Hom_C(c,R(d)) -> Hom_D(L(c),d); d is explicit because R need not be injective on objects. */
    public BigInteger untranspose(BigInteger targetObject,BigInteger sourceArrow) {
        if(!left.source.target(sourceArrow).equals(right.mapObject(targetObject)))
            throw MathFailure.undefined("Untranspose requires an arrow with target R(d)");
        return left.target.compose(left.mapArrow(sourceArrow),counit.component(targetObject));
    }
    public Map<BigInteger,BigInteger> homMap(BigInteger sourceObject,BigInteger targetObject) {
        // Validate both objects even when their hom sets are empty.
        BigInteger image=left.mapObject(sourceObject); right.mapObject(targetObject);
        Map<BigInteger,BigInteger> result=new TreeMap<>();
        for(BigInteger arrow : left.target.hom(image,targetObject)) result.put(arrow,transpose(sourceObject,arrow));
        return Collections.unmodifiableMap(result);
    }
    /** Opposite reverses the adjoint roles: R.op is left adjoint to L.op. */
    public FiniteAdjunction opposite() {
        return new FiniteAdjunction(right.opposite(),left.opposite(),counit.opposite(),unit.opposite());
    }
    /** Compose left adjoints with the right operand first, and right adjoints in reverse order. */
    public FiniteAdjunction compose(FiniteAdjunction before) {
        if(!before.left.target.equals(left.source)) throw MathFailure.undefined("Adjunction composition requires equal labelled middle categories");
        FiniteNaturalTransformation composedUnit=before.unit.andThen(unit.precompose(before.left).postcompose(before.right));
        FiniteNaturalTransformation composedCounit=before.counit.precompose(right).postcompose(left).andThen(counit);
        return new FiniteAdjunction(left.compose(before.left),before.right.compose(right),composedUnit,composedCounit);
    }
    /** Construct a right adjoint by searching for a universal arrow L(c)->d at every d. */
    public static FiniteAdjunction fromLeft(FiniteFunctor left) {
        Objects.requireNonNull(left);
        Map<BigInteger,BigInteger> objects=new TreeMap<>(),epsilon=new TreeMap<>();
        for(BigInteger targetObject : left.target.objects().members()) {
            boolean found=false;
            for(BigInteger candidate : left.source.objects().members()) {
                for(BigInteger arrow : left.target.hom(left.mapObject(candidate),targetObject))
                    if(isUniversal(left,candidate,targetObject,arrow)) {
                        objects.put(targetObject,candidate); epsilon.put(targetObject,arrow); found=true; break;
                    }
                if(found) break;
            }
            if(!found) throw MathFailure.undefined("Functor has no right adjoint: no universal arrow at target object "+targetObject);
        }
        Map<BigInteger,BigInteger> arrows=new TreeMap<>();
        for(BigInteger arrow : left.target.arrows().keySet()) {
            BigInteger source=left.target.source(arrow),target=left.target.target(arrow);
            arrows.put(arrow,lift(left,objects.get(source),objects.get(target),epsilon.get(target),
                    left.target.compose(epsilon.get(source),arrow)));
        }
        FiniteFunctor right=new FiniteFunctor(left.target,left.source,objects,arrows);
        Map<BigInteger,BigInteger> eta=new TreeMap<>();
        for(BigInteger source : left.source.objects().members()) {
            BigInteger image=left.mapObject(source);
            eta.put(source,lift(left,source,objects.get(image),epsilon.get(image),left.target.identity(image)));
        }
        return new FiniteAdjunction(left,right,
                new FiniteNaturalTransformation(FiniteFunctor.identity(left.source),right.compose(left),eta),
                new FiniteNaturalTransformation(left.compose(right),FiniteFunctor.identity(left.target),epsilon));
    }
    /** Dual construction using universal arrows c->R(d). */
    public static FiniteAdjunction fromRight(FiniteFunctor right) { return fromLeft(right.opposite()).opposite(); }
    private static boolean isUniversal(FiniteFunctor left,BigInteger candidate,BigInteger target,BigInteger epsilon) {
        for(BigInteger source : left.source.objects().members()) {
            Set<BigInteger> images=new HashSet<>();
            for(BigInteger arrow : left.source.hom(source,candidate))
                if(!images.add(left.target.compose(left.mapArrow(arrow),epsilon))) return false;
            if(!images.equals(new HashSet<>(left.target.hom(left.mapObject(source),target)))) return false;
        }
        return true;
    }
    private static BigInteger lift(FiniteFunctor left,BigInteger source,BigInteger target,BigInteger epsilon,BigInteger arrow) {
        BigInteger result=null;
        for(BigInteger candidate : left.source.hom(source,target))
            if(left.target.compose(left.mapArrow(candidate),epsilon).equals(arrow)) {
                if(result!=null) throw constructionFailure("Nonunique universal-arrow lift");
                result=candidate;
            }
        if(result==null) throw constructionFailure("Missing universal-arrow lift");
        return result;
    }
    private static MathFailure constructionFailure(String message) { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,message); }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteAdjunction)) return false;
        FiniteAdjunction a=(FiniteAdjunction)other;
        return left.equals(a.left) && right.equals(a.right) && unit.equals(a.unit) && counit.equals(a.counit);
    }
    @Override public int hashCode() { return Objects.hash(left,right,unit,counit); }
    @Override public String toString() { return "Adjunction(left="+left+", right="+right+", unit="+unit.componentMap()+", counit="+counit.componentMap()+")"; }
}
