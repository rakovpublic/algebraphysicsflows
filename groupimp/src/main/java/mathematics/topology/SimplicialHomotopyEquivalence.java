package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Opposite simplicial pair maps with supplied, checked contiguity paths from identities to both composites. */
public final class SimplicialHomotopyEquivalence implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialMap forward,backward;
    private final SimplicialHomotopyPath sourceHomotopy,targetHomotopy;

    public SimplicialHomotopyEquivalence(RelativeSimplicialMap forward,RelativeSimplicialMap backward,
                                        SimplicialHomotopyPath sourceHomotopy,SimplicialHomotopyPath targetHomotopy) {
        this(forward,backward,sourceHomotopy,targetHomotopy,new Computation());
    }
    private SimplicialHomotopyEquivalence(RelativeSimplicialMap forward,RelativeSimplicialMap backward,
                                         SimplicialHomotopyPath sourceHomotopy,SimplicialHomotopyPath targetHomotopy,Computation work) {
        this.forward=Objects.requireNonNull(forward); this.backward=Objects.requireNonNull(backward);
        this.sourceHomotopy=Objects.requireNonNull(sourceHomotopy); this.targetHomotopy=Objects.requireNonNull(targetHomotopy);
        if(!forward.source().equals(backward.target()) || !forward.target().equals(backward.source()))
            throw MathFailure.undefined("Homotopy equivalence requires opposite maps of the same full labelled pairs");
        if(!sourceHomotopy.from().equals(RelativeSimplicialMap.identity(source(),work)) || !sourceHomotopy.to().equals(backward.compose(forward,work)))
            throw MathFailure.undefined("The source witness must run from the full source identity to backward after forward");
        if(!targetHomotopy.from().equals(RelativeSimplicialMap.identity(target(),work)) || !targetHomotopy.to().equals(forward.compose(backward,work)))
            throw MathFailure.undefined("The target witness must run from the full target identity to forward after backward");
    }
    public RelativeSimplicialMap forward() { return forward; }
    public RelativeSimplicialMap backward() { return backward; }
    public RelativeSimplicialComplex source() { return forward.source(); }
    public RelativeSimplicialComplex target() { return forward.target(); }
    public SimplicialHomotopyPath sourceHomotopy() { return sourceHomotopy; }
    public SimplicialHomotopyPath targetHomotopy() { return targetHomotopy; }
    private static SimplicialHomotopyPath stationary(RelativeSimplicialMap map,Computation work) {
        return new SimplicialHomotopyPath(Collections.singletonList(map),work);
    }
    public static SimplicialHomotopyEquivalence identity(RelativeSimplicialComplex pair) {
        Computation work=new Computation(); RelativeSimplicialMap identity=RelativeSimplicialMap.identity(pair,work); SimplicialHomotopyPath path=stationary(identity,work);
        return new SimplicialHomotopyEquivalence(identity,identity,path,path,work);
    }
    public static SimplicialHomotopyEquivalence fromIsomorphism(RelativeSimplicialMap map) {
        Computation work=new Computation(); RelativeSimplicialMap inverse=map.inverse(work);
        return new SimplicialHomotopyEquivalence(map,inverse,stationary(RelativeSimplicialMap.identity(map.source(),work),work),
                stationary(RelativeSimplicialMap.identity(map.target(),work),work),work);
    }
    public SimplicialHomotopyEquivalence inverse() { return new SimplicialHomotopyEquivalence(backward,forward,targetHomotopy,sourceHomotopy); }
    /** Apply before first; transport and concatenate its supplied witnesses without searching for new paths. */
    public SimplicialHomotopyEquivalence compose(SimplicialHomotopyEquivalence before) {
        if(!source().equals(before.target())) throw MathFailure.undefined("Homotopy-equivalence composition requires the same full middle pair");
        Computation work=new Computation(); RelativeSimplicialMap f=forward.compose(before.forward,work),g=before.backward.compose(backward,work);
        SimplicialHomotopyPath sourcePath=before.sourceHomotopy.then(sourceHomotopy.precompose(before.forward,work).postcompose(before.backward,work),work);
        SimplicialHomotopyPath targetPath=targetHomotopy.then(before.targetHomotopy.precompose(backward,work).postcompose(forward,work),work);
        return new SimplicialHomotopyEquivalence(f,g,sourcePath,targetPath,work);
    }
    public AbelianGroupHomomorphism forwardHomologyMap(BigInteger degree) { return forward.homologyMap(degree); }
    public AbelianGroupHomomorphism backwardHomologyMap(BigInteger degree) { return backward.homologyMap(degree); }
    /** Forward then backward, with a single budget for both integral maps. */
    public List<AbelianGroupHomomorphism> homologyMaps(BigInteger degree) {
        Computation work=new Computation(); return Collections.unmodifiableList(Arrays.asList(forward.homologyMap(degree,work),backward.homologyMap(degree,work)));
    }
    public AbelianGroupHomomorphism forwardCohomologyMap(BigInteger degree) { return RelativeSimplicialCochain.cohomologyMap(forward,degree); }
    public AbelianGroupHomomorphism backwardCohomologyMap(BigInteger degree) { return RelativeSimplicialCochain.cohomologyMap(backward,degree); }
    /** Pullback by the forward map, then pullback by the backward map; both reverse the geometric direction. */
    public List<AbelianGroupHomomorphism> cohomologyMaps(BigInteger degree) {
        Computation work=new Computation(); return Collections.unmodifiableList(Arrays.asList(RelativeSimplicialCochain.cohomologyMap(forward,degree,work),
                RelativeSimplicialCochain.cohomologyMap(backward,degree,work)));
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof SimplicialHomotopyEquivalence)) return false; SimplicialHomotopyEquivalence e=(SimplicialHomotopyEquivalence)other;
        return forward.equals(e.forward) && backward.equals(e.backward) && sourceHomotopy.equals(e.sourceHomotopy) && targetHomotopy.equals(e.targetHomotopy);
    }
    @Override public int hashCode() { return Objects.hash(forward,backward,sourceHomotopy,targetHomotopy); }
    @Override public String toString() { return "HomotopyEquivalence(forward="+forward+", backward="+backward+", source-homotopy="+sourceHomotopy+", target-homotopy="+targetHomotopy+")"; }
}
