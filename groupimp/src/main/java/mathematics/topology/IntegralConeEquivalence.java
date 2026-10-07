package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Opposite retained cone maps with retained witnesses GF -> id_source and FG -> id_target. */
public final class IntegralConeEquivalence implements Serializable {
    private static final long serialVersionUID=1L;
    private final Data data;

    /** Individually valid maps and homotopies; their mutual compatibility is checked by from-data. */
    public static final class Data implements Serializable {
        private static final long serialVersionUID=1L;
        private final IntegralChainConeMap forward,backward;
        private final IntegralConeHomotopy sourceHomotopy,targetHomotopy;
        public Data(IntegralChainConeMap forward,IntegralChainConeMap backward,IntegralConeHomotopy sourceHomotopy,IntegralConeHomotopy targetHomotopy) {
            this.forward=Objects.requireNonNull(forward); this.backward=Objects.requireNonNull(backward);
            this.sourceHomotopy=Objects.requireNonNull(sourceHomotopy); this.targetHomotopy=Objects.requireNonNull(targetHomotopy);
        }
        public IntegralChainConeMap forward() { return forward; }
        public IntegralChainConeMap backward() { return backward; }
        public IntegralConeHomotopy sourceHomotopy() { return sourceHomotopy; }
        public IntegralConeHomotopy targetHomotopy() { return targetHomotopy; }
        @Override public boolean equals(Object other) {
            if(!(other instanceof Data)) return false; Data d=(Data)other;
            return forward.equals(d.forward) && backward.equals(d.backward) && sourceHomotopy.equals(d.sourceHomotopy) && targetHomotopy.equals(d.targetHomotopy);
        }
        @Override public int hashCode() { return Objects.hash(forward,backward,sourceHomotopy,targetHomotopy); }
        @Override public String toString() { return "ConeEquivalenceData("+contents()+")"; }
        private String contents() { return "forward="+forward+", backward="+backward+", source-homotopy="+sourceHomotopy+", target-homotopy="+targetHomotopy; }
    }
    public IntegralConeEquivalence(Data data) { this(data,new Computation()); }
    public IntegralConeEquivalence(IntegralChainConeMap forward,IntegralChainConeMap backward,IntegralConeHomotopy sourceHomotopy,IntegralConeHomotopy targetHomotopy) {
        this(new Data(forward,backward,sourceHomotopy,targetHomotopy));
    }
    IntegralConeEquivalence(Data data,Computation work) {
        this.data=Objects.requireNonNull(data);
        if(!source().equals(backward().target()) || !target().equals(backward().source())) throw MathFailure.undefined("Cone equivalence requires opposite maps of the same full defining cone maps");
        requireWitnessPair(sourceHomotopy(),source()); requireWitnessPair(targetHomotopy(),target());
        if(!sourceHomotopy().from().equals(backward().compose(forward(),work)) || !sourceHomotopy().to().equals(IntegralChainConeMap.identity(source(),work)))
            throw MathFailure.undefined("The source witness must run from backward after forward to the full source identity");
        if(!targetHomotopy().from().equals(forward().compose(backward(),work)) || !targetHomotopy().to().equals(IntegralChainConeMap.identity(target(),work)))
            throw MathFailure.undefined("The target witness must run from forward after backward to the full target identity");
    }
    private static void requireWitnessPair(IntegralConeHomotopy h,IntegralChainMappingCone pair) {
        if(!pair.equals(h.source()) || !pair.equals(h.target())) throw MathFailure.undefined("Each cone-equivalence witness must retain its full defining cone map");
    }
    public static IntegralConeEquivalence fromMap(IntegralChainConeMap map) { return IntegralConeInverseSolver.equivalence(map,new Computation()); }
    public static IntegralConeEquivalence identity(IntegralChainMappingCone pair) {
        Computation work=new Computation(); IntegralChainConeMap id=IntegralChainConeMap.identity(pair,work); IntegralConeHomotopy h=IntegralConeHomotopy.stationary(id,work);
        return new IntegralConeEquivalence(new Data(id,id,h,h),work);
    }
    /** Strict inverse of the retained square; total cone-matrix invertibility alone is insufficient. */
    public static IntegralConeEquivalence fromSquareIsomorphism(IntegralChainConeMap map) {
        Computation work=new Computation(); IntegralChainConeMap inverse=map.inverseSquare(work);
        IntegralConeHomotopy h=IntegralConeHomotopy.stationary(inverse.compose(map,work),work),
                k=IntegralConeHomotopy.stationary(map.compose(inverse,work),work);
        return new IntegralConeEquivalence(new Data(map,inverse,h,k),work);
    }
    public Data data() { return data; }
    public IntegralChainMappingCone source() { return forward().source(); }
    public IntegralChainMappingCone target() { return forward().target(); }
    public IntegralChainConeMap forward() { return data.forward; }
    public IntegralChainConeMap backward() { return data.backward; }
    public IntegralConeHomotopy sourceHomotopy() { return data.sourceHomotopy; }
    public IntegralConeHomotopy targetHomotopy() { return data.targetHomotopy; }
    public List<IntegralChainConeMap> maps() { return Collections.unmodifiableList(Arrays.asList(forward(),backward())); }
    public List<IntegralConeHomotopy> homotopies() { return Collections.unmodifiableList(Arrays.asList(sourceHomotopy(),targetHomotopy())); }
    /** Swap both maps and both witnesses, without negating or solving again. */
    public IntegralConeEquivalence inverse() { return new IntegralConeEquivalence(backward(),forward(),targetHomotopy(),sourceHomotopy()); }
    /** This after before. Retain transported supplied witnesses, including nonzero loops. */
    public IntegralConeEquivalence compose(IntegralConeEquivalence before) {
        if(!source().equals(before.target())) throw MathFailure.undefined("Cone-equivalence composition requires the same full defining middle cone map");
        Computation work=new Computation(); IntegralChainConeMap f=forward().compose(before.forward(),work),g=before.backward().compose(backward(),work);
        IntegralConeHomotopy h=sourceHomotopy().precompose(before.forward(),work).postcompose(before.backward(),work).then(before.sourceHomotopy(),work);
        IntegralConeHomotopy k=before.targetHomotopy().precompose(backward(),work).postcompose(forward(),work).then(targetHomotopy(),work);
        return new IntegralConeEquivalence(new Data(f,g,h,k),work);
    }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return forward().homologyMap(degree); }
    public AbelianGroupHomomorphism inverseHomologyMap(BigInteger degree) { return backward().homologyMap(degree); }
    public AbelianGroupHomomorphism cohomologyMap(BigInteger degree) { return forward().cohomologyMap(degree); }
    public AbelianGroupHomomorphism inverseCohomologyMap(BigInteger degree) { return backward().cohomologyMap(degree); }
    private List<AbelianGroupHomomorphism> integralMaps(BigInteger degree,boolean dual) {
        if(degree.signum()<0) throw MathFailure.undefined("Cone-equivalence integral-map degrees must be nonnegative");
        Computation work=new Computation(); return Collections.unmodifiableList(Arrays.asList(forward().induced(degree,dual,work),backward().induced(degree,dual,work)));
    }
    public List<AbelianGroupHomomorphism> homologyMaps(BigInteger degree) { return integralMaps(degree,false); }
    public List<AbelianGroupHomomorphism> cohomologyMaps(BigInteger degree) { return integralMaps(degree,true); }
    public IntegralConeChain onChain(IntegralConeChain chain) { return forward().onChain(chain); }
    public IntegralConeChain inverseOnChain(IntegralConeChain chain) { return backward().onChain(chain); }
    public IntegralConeCochain onCochain(IntegralConeCochain cochain) { return forward().onCochain(cochain); }
    public IntegralConeCochain inverseOnCochain(IntegralConeCochain cochain) { return backward().onCochain(cochain); }
    public IntegralConeChain sourceHomotopyOnChain(IntegralConeChain chain) { return sourceHomotopy().onChain(chain); }
    public IntegralConeChain targetHomotopyOnChain(IntegralConeChain chain) { return targetHomotopy().onChain(chain); }
    public IntegralConeCochain sourceHomotopyOnCochain(IntegralConeCochain cochain) { return sourceHomotopy().onCochain(cochain); }
    public IntegralConeCochain targetHomotopyOnCochain(IntegralConeCochain cochain) { return targetHomotopy().onCochain(cochain); }
    @Override public boolean equals(Object other) { return other instanceof IntegralConeEquivalence && data.equals(((IntegralConeEquivalence)other).data); }
    @Override public int hashCode() { return data.hashCode(); }
    @Override public String toString() { return "ConeEquivalence("+data.contents()+")"; }
}
