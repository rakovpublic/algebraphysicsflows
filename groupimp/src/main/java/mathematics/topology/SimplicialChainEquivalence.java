package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
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
    private interface WitnessMatrix { IntegerMatrix at(BigInteger degree); }
    private static SimplicialChainHomotopy witness(SimplicialChainMap composite,WitnessMatrix factory,Computation work) {
        List<IntegerMatrix> matrices=new ArrayList<>();
        for(int k=0;k<composite.chainMatrices().size();k++) matrices.add(factory.at(BigInteger.valueOf(k)));
        return new SimplicialChainHomotopy(new SimplicialChainHomotopy.Data(composite,SimplicialChainMap.identity(composite.source(),work),matrices),work);
    }
    private static IntegerMatrix zeroWitness(RelativeSimplicialComplex pair,BigInteger degree,Computation work) {
        int rows=pair.basis(degree.add(BigInteger.ONE)).size(),columns=pair.basis(degree).size();
        work.use((long)rows*columns); return IntegerMatrix.zero(rows,columns);
    }
    private static SimplicialChainEquivalence witnessed(SimplicialChainMap f,SimplicialChainMap g,WitnessMatrix source,WitnessMatrix target,Computation work) {
        SimplicialChainHomotopy h=witness(g.compose(f,work),source,work),k=witness(f.compose(g,work),target,work);
        return new SimplicialChainEquivalence(new Data(f,g,h,k),work);
    }
    /** Strict degreewise unimodular inversion, without the simultaneous homotopy-inverse search. */
    public static SimplicialChainEquivalence fromIsomorphism(SimplicialChainMap map) {
        Computation work=new Computation(); SimplicialChainMap inverse=map.inverse(work);
        return witnessed(map,inverse,d -> zeroWitness(map.source(),d,work),d -> zeroWitness(map.target(),d,work),work);
    }
    private static IntegerMatrix negatePrism(SimplicialHomotopyPath path,BigInteger degree,Computation work) {
        IntegerMatrix matrix=path.prism(degree,work); work.use((long)matrix.rows()*matrix.columns()); return matrix.scale(BigInteger.ONE.negate());
    }
    /** Geometric paths run identity -> composite: negate their actual prisms, preserving their choices. */
    public static SimplicialChainEquivalence fromHomotopyEquivalence(SimplicialHomotopyEquivalence equivalence) {
        Computation work=new Computation(); SimplicialChainMap f=SimplicialChainMap.fromSimplicial(equivalence.forward(),work),g=SimplicialChainMap.fromSimplicial(equivalence.backward(),work);
        return witnessed(f,g,d -> negatePrism(equivalence.sourceHomotopy(),d,work),d -> negatePrism(equivalence.targetHomotopy(),d,work),work);
    }
    public static SimplicialChainEquivalence fromCollapse(SimplicialCollapse collapse) {
        Computation work=new Computation(); SimplicialChainMap f=SimplicialChainMap.fromCollapse(collapse,work),
                g=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(collapse.target(),collapse.source(),work),work);
        return witnessed(f,g,d -> collapse.homotopy(d,work),d -> zeroWitness(collapse.target(),d,work),work);
    }
    public static SimplicialChainEquivalence fromCollapseSequence(SimplicialCollapseSequence sequence) {
        Computation work=new Computation(); SimplicialChainMap f=SimplicialChainMap.fromCollapseSequence(sequence,work),
                g=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(sequence.target(),sequence.source(),work),work);
        return witnessed(f,g,d -> sequence.homotopy(d,work),d -> zeroWitness(sequence.target(),d,work),work);
    }
    public static SimplicialChainEquivalence fromSubdivision(SimplicialSubdivision subdivision) {
        Computation work=new Computation(); SimplicialChainMap f=SimplicialChainMap.fromSubdivision(subdivision,work),
                g=SimplicialChainMap.fromSimplicial(subdivision.lastVertexMap(work),work);
        SimplicialSubdivisionChains chains=new SimplicialSubdivisionChains(subdivision,work);
        return witnessed(f,g,d -> zeroWitness(subdivision.original(),d,work),chains::homotopyMatrix,work);
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
    public RelativeSimplicialChain sourceHomotopyOnChain(RelativeSimplicialChain chain) { return sourceHomotopy().onChain(chain); }
    public RelativeSimplicialChain targetHomotopyOnChain(RelativeSimplicialChain chain) { return targetHomotopy().onChain(chain); }
    public RelativeSimplicialCochain sourceHomotopyOnCochain(RelativeSimplicialCochain cochain) { return sourceHomotopy().onCochain(cochain); }
    public RelativeSimplicialCochain targetHomotopyOnCochain(RelativeSimplicialCochain cochain) { return targetHomotopy().onCochain(cochain); }
    public SimplicialChainMapClass forwardClass() { return SimplicialChainMapClass.fromMap(forward()); }
    public SimplicialChainMapClass backwardClass() { return SimplicialChainMapClass.fromMap(backward()); }
    @Override public boolean equals(Object other) { return other instanceof SimplicialChainEquivalence && data.equals(((SimplicialChainEquivalence)other).data); }
    @Override public int hashCode() { return data.hashCode(); }
    @Override public String toString() { return "ChainEquivalence("+data.contents()+")"; }
}
