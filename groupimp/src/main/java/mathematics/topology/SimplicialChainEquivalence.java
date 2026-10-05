package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Opposite integral chain maps with retained witnesses GF -> id_source and FG -> id_target. */
public final class SimplicialChainEquivalence implements Serializable {
    private static final long serialVersionUID=1L;
    private final Data data;

    /** Individually valid maps and homotopies; their mutual compatibility is checked by from-data. */
    public static final class Data implements Serializable {
        private static final long serialVersionUID=1L;
        private final SimplicialChainMap forward,backward;
        private final SimplicialChainHomotopy sourceHomotopy,targetHomotopy;
        public Data(SimplicialChainMap forward,SimplicialChainMap backward,SimplicialChainHomotopy sourceHomotopy,SimplicialChainHomotopy targetHomotopy) {
            this.forward=Objects.requireNonNull(forward); this.backward=Objects.requireNonNull(backward);
            this.sourceHomotopy=Objects.requireNonNull(sourceHomotopy); this.targetHomotopy=Objects.requireNonNull(targetHomotopy);
        }
        public SimplicialChainMap forward() { return forward; }
        public SimplicialChainMap backward() { return backward; }
        public SimplicialChainHomotopy sourceHomotopy() { return sourceHomotopy; }
        public SimplicialChainHomotopy targetHomotopy() { return targetHomotopy; }
        @Override public boolean equals(Object other) {
            if(!(other instanceof Data)) return false; Data d=(Data)other;
            return forward.equals(d.forward) && backward.equals(d.backward) && sourceHomotopy.equals(d.sourceHomotopy) && targetHomotopy.equals(d.targetHomotopy);
        }
        @Override public int hashCode() { return Objects.hash(forward,backward,sourceHomotopy,targetHomotopy); }
        @Override public String toString() { return "ChainEquivalenceData("+contents()+")"; }
        private String contents() { return "forward="+forward+", backward="+backward+", source-homotopy="+sourceHomotopy+", target-homotopy="+targetHomotopy; }
    }
    public SimplicialChainEquivalence(Data data) { this(data,new Computation()); }
    public SimplicialChainEquivalence(SimplicialChainMap forward,SimplicialChainMap backward,SimplicialChainHomotopy sourceHomotopy,SimplicialChainHomotopy targetHomotopy) {
        this(new Data(forward,backward,sourceHomotopy,targetHomotopy));
    }
    SimplicialChainEquivalence(Data data,Computation work) {
        this.data=Objects.requireNonNull(data);
        if(!source().equals(backward().target()) || !target().equals(backward().source())) throw MathFailure.undefined("Chain equivalence requires opposite maps of the same full labelled pairs");
        requireWitnessPair(sourceHomotopy(),source()); requireWitnessPair(targetHomotopy(),target());
        if(!sourceHomotopy().from().equals(backward().compose(forward(),work)) || !sourceHomotopy().to().equals(SimplicialChainMap.identity(source(),work)))
            throw MathFailure.undefined("The source witness must run from backward after forward to the full source identity");
        if(!targetHomotopy().from().equals(forward().compose(backward(),work)) || !targetHomotopy().to().equals(SimplicialChainMap.identity(target(),work)))
            throw MathFailure.undefined("The target witness must run from forward after backward to the full target identity");
    }
    private static void requireWitnessPair(SimplicialChainHomotopy h,RelativeSimplicialComplex pair) {
        if(!pair.equals(h.source()) || !pair.equals(h.target())) throw MathFailure.undefined("Each chain-equivalence witness must retain its full endpoint pair");
    }
    public static SimplicialChainEquivalence fromMap(SimplicialChainMap map) { return SimplicialChainInverseSolver.equivalence(map,new Computation()); }
    public static SimplicialChainEquivalence identity(RelativeSimplicialComplex pair) {
        Computation work=new Computation(); SimplicialChainMap id=SimplicialChainMap.identity(pair,work); SimplicialChainHomotopy h=SimplicialChainHomotopy.stationary(id,work);
        return new SimplicialChainEquivalence(new Data(id,id,h,h),work);
    }
    public Data data() { return data; }
    public RelativeSimplicialComplex source() { return forward().source(); }
    public RelativeSimplicialComplex target() { return forward().target(); }
    public SimplicialChainMap forward() { return data.forward; }
    public SimplicialChainMap backward() { return data.backward; }
    public SimplicialChainHomotopy sourceHomotopy() { return data.sourceHomotopy; }
    public SimplicialChainHomotopy targetHomotopy() { return data.targetHomotopy; }
    public List<SimplicialChainMap> maps() { return Collections.unmodifiableList(Arrays.asList(forward(),backward())); }
    public List<SimplicialChainHomotopy> homotopies() { return Collections.unmodifiableList(Arrays.asList(sourceHomotopy(),targetHomotopy())); }
    /** Swap both maps and both witnesses, without negating or solving again. */
    public SimplicialChainEquivalence inverse() { return new SimplicialChainEquivalence(backward(),forward(),targetHomotopy(),sourceHomotopy()); }
    /** This after before. Retain transported supplied witnesses, including nonzero loops. */
    public SimplicialChainEquivalence compose(SimplicialChainEquivalence before) {
        if(!source().equals(before.target())) throw MathFailure.undefined("Chain-equivalence composition requires the same full labelled middle pair");
        Computation work=new Computation(); SimplicialChainMap f=forward().compose(before.forward(),work),g=before.backward().compose(backward(),work);
        SimplicialChainHomotopy h=sourceHomotopy().precompose(before.forward(),work).postcompose(before.backward(),work).then(before.sourceHomotopy(),work);
        SimplicialChainHomotopy k=before.targetHomotopy().precompose(backward(),work).postcompose(forward(),work).then(targetHomotopy(),work);
        return new SimplicialChainEquivalence(new Data(f,g,h,k),work);
    }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return forward().homologyMap(degree); }
    public AbelianGroupHomomorphism inverseHomologyMap(BigInteger degree) { return backward().homologyMap(degree); }
    public AbelianGroupHomomorphism cohomologyMap(BigInteger degree) { return forward().cohomologyMap(degree); }
    public AbelianGroupHomomorphism inverseCohomologyMap(BigInteger degree) { return backward().cohomologyMap(degree); }
    private List<AbelianGroupHomomorphism> integralMaps(BigInteger degree,boolean dual) {
        Computation work=new Computation(); return Collections.unmodifiableList(Arrays.asList(forward().induced(degree,dual,work),backward().induced(degree,dual,work)));
    }
    public List<AbelianGroupHomomorphism> homologyMaps(BigInteger degree) { return integralMaps(degree,false); }
    public List<AbelianGroupHomomorphism> cohomologyMaps(BigInteger degree) { return integralMaps(degree,true); }
    public RelativeSimplicialChain onChain(RelativeSimplicialChain chain) { return forward().onChain(chain); }
    public RelativeSimplicialChain inverseOnChain(RelativeSimplicialChain chain) { return backward().onChain(chain); }
    public RelativeSimplicialCochain onCochain(RelativeSimplicialCochain cochain) { return forward().onCochain(cochain); }
    public RelativeSimplicialCochain inverseOnCochain(RelativeSimplicialCochain cochain) { return backward().onCochain(cochain); }
    public SimplicialChainMapClass forwardClass() { return SimplicialChainMapClass.fromMap(forward()); }
    public SimplicialChainMapClass backwardClass() { return SimplicialChainMapClass.fromMap(backward()); }
    @Override public boolean equals(Object other) { return other instanceof SimplicialChainEquivalence && data.equals(((SimplicialChainEquivalence)other).data); }
    @Override public int hashCode() { return data.hashCode(); }
    @Override public String toString() { return "ChainEquivalence("+data.contents()+")"; }
}
