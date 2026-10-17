package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupElement;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** An integral chain-homotopy class with its full labelled endpoints retained. */
public final class SimplicialChainMapClass implements Serializable {
    private static final long serialVersionUID=1L;
    private final SimplicialChainMapSpace space;
    private final AbelianGroupElement element;
    private final SimplicialChainMap representative;

    // Only the map-space reduction constructs this triple, including the canonical lift.
    SimplicialChainMapClass(SimplicialChainMapSpace space,AbelianGroupElement element,SimplicialChainMap representative) {
        this.space=space; this.element=element; this.representative=representative;
    }
    public static SimplicialChainMapClass fromMap(SimplicialChainMap map) { return fromMap(map,new Computation()); }
    private static SimplicialChainMapClass fromMap(SimplicialChainMap map,Computation work) {
        return new SimplicialChainMapSpace(map.source(),map.target()).typedClassOf(map,work);
    }
    /** The supplied element must use this exact Hom presentation; group type alone is insufficient. */
    public static SimplicialChainMapClass fromElement(SimplicialChainMapSpace space,AbelianGroupElement element) {
        return space.typedElement(Objects.requireNonNull(element),new Computation());
    }
    public static SimplicialChainMapClass zeroIn(SimplicialChainMapSpace space) { return space.typedZero(new Computation()); }
    public static SimplicialChainMapClass identityOn(RelativeSimplicialComplex pair) {
        Computation work=new Computation(); return fromMap(SimplicialChainMap.identity(pair,work),work);
    }
    /** Nonzero minimal Smith generators, torsion first, then free, all in the supplied context. */
    public static List<SimplicialChainMapClass> generatorsIn(SimplicialChainMapSpace space) { return space.typedGenerators(new Computation()); }
    public RelativeSimplicialComplex source() { return space.source(); }
    public RelativeSimplicialComplex target() { return space.target(); }
    public SimplicialChainMapSpace space() { return space; }
    public AbelianGroupElement element() { return element; }
    /** Deterministic Smith section; choosing representatives is not generally additive or functorial. */
    public SimplicialChainMap representative() { return representative; }
    public boolean isZero() { return element.isZero(); }
    public boolean isIdentity() { return source().equals(target()) && equals(identityOn(source())); }
    public boolean hasFiniteOrder() { return element.isTorsion(); }
    public BigInteger order() { return element.order(); }
    private void requireParallel(SimplicialChainMapClass other) {
        if(!space.equals(other.space)) throw MathFailure.undefined("Chain-class addition requires identical full labelled source and target pairs");
    }
    private SimplicialChainMapClass combine(SimplicialChainMapClass other,boolean subtract) {
        requireParallel(other); Computation work=new Computation(); work.use((subtract?2L:1L)*element.smithCoordinates().dimension());
        return space.typedElement(element.add(subtract?other.element.scale(BigInteger.ONE.negate()):other.element),work);
    }
    public SimplicialChainMapClass add(SimplicialChainMapClass other) { return combine(other,false); }
    public SimplicialChainMapClass subtract(SimplicialChainMapClass other) { return combine(other,true); }
    public SimplicialChainMapClass scale(BigInteger scalar) {
        Objects.requireNonNull(scalar); Computation work=new Computation(); work.use(element.smithCoordinates().dimension());
        return space.typedElement(element.scale(scalar),work);
    }
    public SimplicialChainMapClass negate() { return scale(BigInteger.ONE.negate()); }
    /** This after before: [A] compose [B] = [A B], with the complete middle pair checked first. */
    public SimplicialChainMapClass compose(SimplicialChainMapClass before) {
        if(!source().equals(before.target())) throw MathFailure.undefined("Chain-class composition requires identical full labelled joining pairs");
        Computation work=new Computation(); return fromMap(representative.compose(before.representative,work),work);
    }
    public boolean isIsomorphism() { return SimplicialChainInverseSolver.isHomotopyEquivalence(representative); }
    /** Inverse in the homotopy category, with the full labelled endpoints reversed. */
    public SimplicialChainMapClass inverse() {
        Computation work=new Computation(); return fromMap(SimplicialChainInverseSolver.inverse(representative,work),work);
    }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return representative.homologyMap(degree); }
    public AbelianGroupHomomorphism cohomologyMap(BigInteger degree) { return representative.cohomologyMap(degree); }
    public List<AbelianGroupHomomorphism> homologyMaps() { return representative.homologyMaps(); }
    public List<AbelianGroupHomomorphism> cohomologyMaps() { return representative.cohomologyMaps(); }
    @Override public boolean equals(Object other) {
        return other instanceof SimplicialChainMapClass && space.equals(((SimplicialChainMapClass)other).space) && element.equals(((SimplicialChainMapClass)other).element);
    }
    @Override public int hashCode() { return Objects.hash(space,element); }
    @Override public String toString() { return "ChainMapClass(space="+space+", element="+element+")"; }
}
