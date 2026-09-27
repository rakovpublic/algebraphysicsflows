package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialCollapseSequenceTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex simplex(int n) { int[] v=new int[n]; for(int i=0;i<n;i++) v[i]=i; return complex(v); }
    private static FiniteSimplicialComplex points(int from,int until) { int[][] f=new int[until-from][1]; for(int i=from;i<until;i++) f[i-from][0]=i; return complex(f); }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static FiniteSet<BigInteger> face(int... labels) { List<BigInteger> v=new ArrayList<>(); for(int x : labels) v.add(z(x)); return new FiniteSet<>(v); }
    private static void failure(MathFailure.Kind kind,Runnable body) { assertEquals(kind,assertThrows(MathFailure.class,body::run).kind()); }
    private static boolean closed(int mask,int labels) {
        for(int f=1;f<(1<<labels);f++) if((mask&(1<<(f-1)))!=0) for(int s=(f-1)&f;s>0;s=(s-1)&f) if((mask&(1<<(s-1)))==0) return false; return true;
    }
    private static FiniteSet<BigInteger> fromBits(int bits,int labels) { List<BigInteger> v=new ArrayList<>(); for(int i=0;i<labels;i++) if((bits&(1<<i))!=0) v.add(z(i)); return new FiniteSet<>(v); }
    private static FiniteSimplicialComplex fromMask(int mask,int labels) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int f=1;f<(1<<labels);f++) if((mask&(1<<(f-1)))!=0) { List<Integer> v=new ArrayList<>(); for(BigInteger x : fromBits(f,labels).members()) v.add(x.intValueExact()); faces.add(new FiniteSet<>(v)); } return new FiniteSimplicialComplex(faces);
    }
    // Independent oracle counts all strict cofaces, not just immediate incidences.
    private static Map<Integer,Integer> oracle(int x,int a,int labels) {
        Map<Integer,Integer> result=new TreeMap<>();
        for(int f=1;f<(1<<labels);f++) if((x&(1<<(f-1)))!=0) {
            int unique=0,count=0;
            for(int s=1;s<(1<<labels);s++) if(f!=s && (x&(1<<(s-1)))!=0 && (f&s)==f) { unique=s; count++; }
            if(count==1 && Integer.bitCount(unique)==Integer.bitCount(f)+1 && ((a&(1<<(f-1)))!=0)==((a&(1<<(unique-1)))!=0)) result.put(f,unique);
        }
        return result;
    }
    private static IntegerMatrix difference(IntegerMatrix a,IntegerMatrix b) { return a.add(b.scale(z(-1))); }
    private static void identities(SimplicialCollapseSequence collapse,int k) {
        BigInteger n=z(k); IntegerMatrix r=collapse.chainMatrix(n),i=collapse.inclusion().chainMatrix(n),h=collapse.chainHomotopyMatrix(n);
        assertEquals(IntegerMatrix.identity(r.rows()),r.multiply(i));
        IntegerMatrix previous=k==0?IntegerMatrix.zero(0,0):collapse.chainMatrix(z(k-1));
        assertEquals(collapse.target().boundaryMatrix(n).multiply(r),previous.multiply(collapse.source().boundaryMatrix(n)));
        IntegerMatrix previousH=k==0?collapse.cochainHomotopyMatrix(z(0)).transpose():collapse.chainHomotopyMatrix(z(k-1));
        assertEquals(difference(IntegerMatrix.identity(r.columns()),i.multiply(r)),collapse.source().boundaryMatrix(z(k+1)).multiply(h).add(previousH.multiply(collapse.source().boundaryMatrix(n))));
        assertEquals(r.transpose(),collapse.cochainMatrix(n)); assertEquals(h.transpose(),collapse.cochainHomotopyMatrix(z(k+1)));
        assertEquals(IntegerMatrix.zero(h.rows(),i.columns()),h.multiply(i));
        assertEquals(IntegerMatrix.zero(collapse.target().simplexCount(z(k+1)),h.columns()),collapse.chainMatrix(z(k+1)).multiply(h));
    }
    private static void inverseMaps(SimplicialCollapseSequence c,int k) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(c.homologyMaps(z(k)),c.cohomologyMaps(z(k)))) {
            AbelianGroupHomomorphism f=maps.get(0),g=maps.get(1); assertEquals(AbelianGroupHomomorphism.identity(f.source()),g.compose(f)); assertEquals(AbelianGroupHomomorphism.identity(f.target()),f.compose(g));
        }
        assertEquals(c.homologyMaps(z(k)).get(0),c.homologyMap(z(k))); assertEquals(c.homologyMaps(z(k)).get(1),c.inverseHomologyMap(z(k)));
        assertEquals(c.cohomologyMaps(z(k)).get(0),c.cohomologyMap(z(k))); assertEquals(c.cohomologyMaps(z(k)).get(1),c.inverseCohomologyMap(z(k)));
    }
    private static List<int[]> oracleSequence(int x,int a,int labels) {
        List<int[]> result=new ArrayList<>();
        while(true) {
            List<Integer> faces=new ArrayList<>(oracle(x,a,labels).keySet());
            if(faces.isEmpty()) return result;
            faces.sort((u,v) -> {
                int count=Integer.compare(Integer.bitCount(u),Integer.bitCount(v)); if(count!=0) return count;
                for(int bit=0;bit<labels;bit++) if(((u^v)&(1<<bit))!=0) return (u&(1<<bit))!=0?-1:1; return 0;
            });
            int f=faces.get(0),s=oracle(x,a,labels).get(f),removed=(1<<(f-1))|(1<<(s-1));
            x&=~removed; a&=~removed; result.add(new int[]{f,s,x,a});
        }
    }
    private static void checkOracle(int x,int a,int labels) {
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(fromMask(x,labels),fromMask(a,labels));
        SimplicialCollapseSequence sequence=SimplicialCollapseSequence.reduce(pair); List<int[]> expected=oracleSequence(x,a,labels);
        assertEquals(z(expected.size()),sequence.stepCount()); assertEquals(pair,sequence.source()); assertTrue(sequence.isTerminal());
        assertEquals(expected.size()+1,sequence.stages().size());
        for(int k=0;k<expected.size();k++) {
            int[] step=expected.get(k); assertEquals(fromBits(step[0],labels),sequence.steps().get(k).freeFace());
            assertEquals(fromBits(step[1],labels),sequence.steps().get(k).coface());
            assertEquals(new RelativeSimplicialComplex(fromMask(step[2],labels),fromMask(step[3],labels)),sequence.stages().get(k+1));
        }
        for(int k=0;k<labels;k++) identities(sequence,k);
    }
    @Test public void all167FourLabelComplexesFollowIndependentGreedyFaceDeletionAndTelescopingIdentities() {
        int count=0; for(int x=0;x<(1<<15);x++) if(closed(x,4)) { checkOracle(x,0,4); count++; } assertEquals(167,count);
    }
    @Test public void all148ThreeLabelPairsFollowIndependentRelativeDeletionsAndIntegralInverses() {
        int count=0; for(int x=0;x<128;x++) if(closed(x,3)) for(int a=0;a<128;a++) if(closed(a,3) && (x&a)==a) {
            checkOracle(x,a,3); SimplicialCollapseSequence s=SimplicialCollapseSequence.reduce(new RelativeSimplicialComplex(fromMask(x,3),fromMask(a,3)));
            for(int k=0;k<=2;k++) inverseMaps(s,k); count++;
        } assertEquals(148,count);
    }
    @Test public void all1024GraphPairsAgreeWithIndependentAscendingLeafDeletion() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int graph=0;graph<64;graph++) for(int sub=0;sub<16;sub++) {
            FiniteSimplicialComplex x=points(0,4),a=complex(); boolean[] active={true,true,true,true};
            for(int e=0;e<6;e++) if((graph&(1<<e))!=0) x=x.union(complex(edges[e]));
            for(int v=0;v<4;v++) if((sub&(1<<v))!=0) a=a.union(complex(new int[]{v}));
            List<FiniteSet<BigInteger>> removed=new ArrayList<>();
            while(true) {
                int chosen=-1;
                for(int v=0;v<4;v++) if(active[v] && (sub&(1<<v))==0) {
                    int degree=0; for(int e=0;e<6;e++) if((graph&(1<<e))!=0 && active[edges[e][0]] && active[edges[e][1]] && (edges[e][0]==v || edges[e][1]==v)) degree++;
                    if(degree==1) { chosen=v; break; }
                }
                if(chosen<0) break; active[chosen]=false; removed.add(face(chosen));
            }
            SimplicialCollapseSequence s=SimplicialCollapseSequence.reduce(new RelativeSimplicialComplex(x,a));
            List<FiniteSet<BigInteger>> actual=new ArrayList<>(); for(SimplicialCollapse step : s.steps()) actual.add(step.freeFace()); assertEquals(removed,actual);
            List<BigInteger> survivors=new ArrayList<>(); for(int v=0;v<4;v++) if(active[v]) survivors.add(z(v));
            assertEquals(new FiniteSet<>(survivors),FiniteSimplicialMap.vertexSet(s.target().ambient())); assertEquals(a,s.target().subcomplex());
        }
    }
    @Test public void chronologicalConcatenationHasUnitsAssociativityAndTransportedHomotopies() {
        SimplicialCollapseSequence all=SimplicialCollapseSequence.reduceAbsolute(simplex(3)); assertEquals(z(3),all.stepCount());
        SimplicialCollapseSequence a=SimplicialCollapseSequence.fromCollapse(all.steps().get(0)),b=SimplicialCollapseSequence.fromCollapse(all.steps().get(1)),c=SimplicialCollapseSequence.fromCollapse(all.steps().get(2));
        assertEquals(all,a.then(b).then(c)); assertEquals(all,a.then(b.then(c))); assertEquals(all,SimplicialCollapseSequence.identity(all.source()).then(all)); assertEquals(all,all.then(SimplicialCollapseSequence.identity(all.target())));
        assertEquals(all,a.append(b.steps().get(0)).append(c.steps().get(0)));
        SimplicialCollapseSequence after=b.then(c);
        for(int k=0;k<3;k++) {
            BigInteger n=z(k); assertEquals(all.chainMatrix(n),after.chainMatrix(n).multiply(a.chainMatrix(n)));
            assertEquals(all.chainHomotopyMatrix(n),a.chainHomotopyMatrix(n).add(a.inclusion().chainMatrix(z(k+1)).multiply(after.chainHomotopyMatrix(n)).multiply(a.chainMatrix(n))));
        }
        assertFalse(a.isTerminal()); assertTrue(all.isTerminal());
    }
    @Test public void sequenceEqualityRetainsChoicesEvenWithEqualEndpoints() {
        RelativeSimplicialComplex x=abs(simplex(3));
        SimplicialCollapse firstA=new SimplicialCollapse(x,face(0,1)),firstB=new SimplicialCollapse(x,face(0,2));
        SimplicialCollapseSequence a=SimplicialCollapseSequence.fromCollapse(firstA).then(SimplicialCollapseSequence.reduce(firstA.target()));
        SimplicialCollapseSequence b=SimplicialCollapseSequence.fromCollapse(firstB).then(SimplicialCollapseSequence.reduce(firstB.target()));
        assertEquals(a.source(),b.source()); assertEquals(a.target(),b.target()); assertNotEquals(a,b); assertNotEquals(a.chainHomotopyMatrix(z(1)),b.chainHomotopyMatrix(z(1)));
        assertEquals(a,new SimplicialCollapseSequence(a.source(),a.steps())); assertEquals(a.hashCode(),new SimplicialCollapseSequence(a.source(),a.steps()).hashCode());
    }
    @Test public void all729TernaryChainsAndCochainsSatisfyCompositeTypedIdentities() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2},new int[]{2,3},new int[]{3,4},new int[]{4,5}); SimplicialCollapseSequence s=SimplicialCollapseSequence.reduceAbsolute(x);
        for(int code=0;code<729;code++) {
            BigInteger[] values=new BigInteger[6]; int remaining=code; for(int j=0;j<6;j++) { values[j]=z(remaining%3-1); remaining/=3; }
            RelativeSimplicialChain c=new RelativeSimplicialChain(s.source(),z(1),new IntegerVector(values));
            assertEquals(c.subtract(s.onChain(c).pushforward(s.inclusion())),s.homotopyOnChain(c).boundary().add(s.homotopyOnChain(c.boundary())));
            RelativeSimplicialCochain q=new RelativeSimplicialCochain(s.source(),z(1),new IntegerVector(values));
            assertEquals(q.subtract(s.onCochain(q.pullback(s.inclusion()))),s.homotopyOnCochain(q).coboundary().add(s.homotopyOnCochain(q.coboundary())));
        }
        RelativeSimplicialCochain q=new RelativeSimplicialCochain(s.target(),z(0),new IntegerVector(z(7)));
        RelativeSimplicialChain c=new RelativeSimplicialChain(s.source(),z(0),new IntegerVector(z(1),z(2),z(3),z(4),z(5),z(6)));
        assertEquals(c.evaluate(s.onCochain(q)),s.onChain(c).evaluate(q));
    }
    @Test public void simplexReductionsThroughDimensionFourHaveSignedIntegralWitnesses() {
        for(int vertices=2;vertices<=5;vertices++) {
            SimplicialCollapseSequence s=SimplicialCollapseSequence.reduceAbsolute(simplex(vertices)); assertEquals(z((1<<(vertices-1))-1),s.stepCount());
            assertEquals(complex(new int[]{vertices-1}),s.target().ambient()); for(int k=0;k<vertices;k++) { identities(s,k); inverseMaps(s,k); }
        }
    }
    @Test public void relativeReductionPreservesProtectedBoundariesAndCancelsWithinBothComponents() {
        RelativeSimplicialComplex disk=new RelativeSimplicialComplex(simplex(3),simplex(3).skeleton(1)); SimplicialCollapseSequence stationary=SimplicialCollapseSequence.reduce(disk);
        assertEquals(z(0),stationary.stepCount()); assertEquals(disk,stationary.target());
        SimplicialCollapseSequence diagonal=SimplicialCollapseSequence.reduce(RelativeSimplicialComplex.diagonal(simplex(3)));
        assertEquals(z(3),diagonal.stepCount()); assertEquals(diagonal.target().ambient(),diagonal.target().subcomplex());
        for(int k=0;k<3;k++) { identities(diagonal,k); assertEquals(IntegerMatrix.zero(0,0),diagonal.chainMatrix(z(k))); }
        RelativeSimplicialComplex based=new RelativeSimplicialComplex(complex(new int[]{0,1},new int[]{1,2}),complex(new int[]{0}));
        SimplicialCollapseSequence s=SimplicialCollapseSequence.reduce(based); assertEquals(RelativeSimplicialComplex.diagonal(complex(new int[]{0})),s.target()); for(int k=0;k<3;k++) identities(s,k);
    }
    @Test public void projectivePlaneWithAnAttachedDiskRetainsIntegralTorsionAndFillings() {
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialCollapseSequence s=SimplicialCollapseSequence.reduceAbsolute(rp2.union(complex(new int[]{0,1,6},new int[]{1,6,7})));
        assertEquals(rp2,s.target().ambient()); assertEquals(z(4),s.stepCount()); assertEquals(Collections.singletonList(z(2)),s.homologyMap(z(1)).target().type().invariantFactors()); assertEquals(Collections.singletonList(z(2)),s.cohomologyMap(z(2)).source().type().invariantFactors());
        for(int k=0;k<3;k++) { inverseMaps(s,k); identities(s,k); }
        for(RelativeSimplicialChain cycle : RelativeSimplicialChain.zero(s.source(),z(1)).cycleGenerators()) assertEquals(cycle.subtract(s.onChain(cycle).pushforward(s.inclusion())),s.homotopyOnChain(cycle).boundary());
    }
    @Test public void emptyDiscreteClosedAndExtremeLabelInputsAreDeterministic() {
        for(FiniteSimplicialComplex x : Arrays.asList(complex(),points(0,4),simplex(4).skeleton(2))) {
            SimplicialCollapseSequence s=SimplicialCollapseSequence.reduceAbsolute(x); assertEquals(SimplicialCollapseSequence.identity(abs(x)),s); assertTrue(s.isTerminal()); for(int k=0;k<3;k++) identities(s,k);
        }
        SimplicialCollapseSequence a=SimplicialCollapseSequence.reduceAbsolute(complex(new int[]{Integer.MIN_VALUE,0,Integer.MAX_VALUE})),b=SimplicialCollapseSequence.reduceAbsolute(complex(new int[]{Integer.MAX_VALUE,0,Integer.MIN_VALUE}));
        assertEquals(a,b); assertEquals(complex(new int[]{Integer.MAX_VALUE}),a.target().ambient()); assertEquals(a.chainHomotopyMatrices(),b.chainHomotopyMatrices());
    }
    @Test public void mismatchedMiddlePairsAndTypedContextsAreRejected() {
        SimplicialCollapseSequence s=SimplicialCollapseSequence.reduceAbsolute(simplex(2)); SimplicialCollapse step=s.steps().get(0);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialCollapseSequence(s.target(),s.steps()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.append(step)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.then(s));
        RelativeSimplicialComplex wrong=RelativeSimplicialComplex.diagonal(s.target().ambient());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.then(SimplicialCollapseSequence.identity(wrong)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.onChain(RelativeSimplicialChain.zero(s.target(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.onCochain(RelativeSimplicialCochain.zero(s.source(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.homotopyOnChain(RelativeSimplicialChain.zero(s.target(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> s.homotopyOnCochain(RelativeSimplicialCochain.zero(s.source(),z(0))));
        SimplicialCollapseSequence relative=SimplicialCollapseSequence.reduce(RelativeSimplicialComplex.diagonal(simplex(2)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> relative.onAbsoluteChain(SimplicialChain.zero(simplex(2),z(0))));
    }
    @Test public void degreeConventionsAndImmutableCopiedSequencesRetainZeroShapes() {
        List<SimplicialCollapse> mutable=new ArrayList<>(); SimplicialCollapseSequence identity=new SimplicialCollapseSequence(abs(simplex(2)),mutable); mutable.add(SimplicialCollapse.absolute(simplex(2),face(0))); assertEquals(z(0),identity.stepCount());
        SimplicialCollapseSequence s=SimplicialCollapseSequence.fromCollapse(mutable.get(0)); BigInteger huge=BigInteger.TEN.pow(100);
        assertEquals(IntegerMatrix.zero(0,0),s.chainMatrix(huge)); assertEquals(IntegerMatrix.zero(0,0),s.chainHomotopyMatrix(huge)); assertTrue(s.homologyMap(huge).isIsomorphism()); assertTrue(s.cohomologyMap(huge).isIsomorphism());
        assertEquals(IntegerMatrix.zero(0,2),s.cochainHomotopyMatrix(z(0))); assertEquals(RelativeSimplicialChain.zero(s.target(),z(-2)),s.onChain(RelativeSimplicialChain.zero(s.source(),z(-2)))); assertEquals(RelativeSimplicialChain.zero(s.source(),z(0)),s.homotopyOnChain(RelativeSimplicialChain.zero(s.source(),z(-1))));
        for(Runnable action : Arrays.<Runnable>asList(() -> s.chainMatrix(z(-1)),() -> s.cochainMatrix(z(-1)),() -> s.chainHomotopyMatrix(z(-1)),() -> s.cochainHomotopyMatrix(z(-1)),() -> s.homologyMap(z(-1)),() -> s.cohomologyMap(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        for(List<?> list : Arrays.asList(s.steps(),s.stages(),s.chainMatrices(),s.cochainMatrices(),s.chainHomotopyMatrices(),s.cochainHomotopyMatrices(),s.homologyMaps(z(0)),s.cohomologyMaps(z(0)))) assertThrows(UnsupportedOperationException.class,list::clear);
        assertEquals(2,s.chainMatrices().size()); assertEquals(3,s.cochainHomotopyMatrices().size()); assertEquals(1,SimplicialCollapseSequence.identity(abs(complex())).cochainHomotopyMatrices().size());
    }
    private static FiniteSimplicialComplex interval(int edges) { int[][] facets=new int[edges][2]; for(int k=0;k<edges;k++) facets[k]=new int[]{k,k+1}; return complex(facets); }
    @Test public void stepLimitsAndFilteredBasisLimitsRemainDistinct() {
        SimplicialCollapseSequence full=SimplicialCollapseSequence.reduceAbsolute(interval(256)); assertEquals(z(256),full.stepCount()); assertEquals(complex(new int[]{256}),full.target().ambient());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialCollapseSequence.reduceAbsolute(interval(257)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new SimplicialCollapseSequence(full.source(),Collections.nCopies(257,full.steps().get(0))));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> full.chainMatrix(z(0)));
        FiniteSimplicialComplex many=points(0,4094).union(simplex(3));
        // 4094 vertices and four positive-dimensional faces exceed the geometric cap.
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialCollapseSequence.reduceAbsolute(many));
        FiniteSimplicialComplex large=points(0,4092).union(simplex(3));
        SimplicialCollapseSequence geometric=SimplicialCollapseSequence.reduceAbsolute(large); assertEquals(z(3),geometric.stepCount()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> geometric.chainMatrix(z(0)));
        SimplicialCollapseSequence filtered=SimplicialCollapseSequence.reduce(new RelativeSimplicialComplex(large,points(3,4092))); assertEquals(new IntegerMatrix(new BigInteger[][]{{z(1),z(1),z(1)}}),filtered.chainMatrix(z(0))); inverseMaps(filtered,0);
    }
    @Test public void wholeReductionAndIntegralMapListsShareTheirComputationBudgets() {
        FiniteSimplicialComplex large=points(0,3600).union(interval(200));
        assertNotNull(SimplicialCollapse.absolute(large,face(0)));
        MathFailure exhausted=assertThrows(MathFailure.class,() -> SimplicialCollapseSequence.reduceAbsolute(large));
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,exhausted.kind()); assertTrue(exhausted.getMessage().contains("5000000"));
        SimplicialCollapseSequence s=SimplicialCollapseSequence.fromCollapse(SimplicialCollapse.absolute(points(0,75).union(simplex(2)),face(1)));
        assertNotNull(s.homologyMap(z(0))); assertNotNull(s.inverseHomologyMap(z(0))); assertNotNull(s.cohomologyMap(z(0))); assertNotNull(s.inverseCohomologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> s.homologyMaps(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> s.cohomologyMaps(z(0)));
    }
    @Test public void allEightNativeTypedActionsReturnActualSecondCarrierWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCollapseSequence s=SimplicialCollapseSequence.reduceAbsolute(simplex(3)); IAlgebraItem<SimplicialCollapseSequence> item=math.collapseSequences.algebra().buildAlgebraItem(s);
        assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation("on-chain",RelativeSimplicialChain.zero(s.source(),z(0))).getAlgebra());
        assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation("on-cochain",RelativeSimplicialCochain.zero(s.target(),z(0))).getAlgebra());
        assertSame(math.simplicialChains.algebra(),item.performLeftProjectionOperation("on-absolute-chain",SimplicialChain.zero(s.source().ambient(),z(0))).getAlgebra());
        assertSame(math.cochains.algebra(),item.performLeftProjectionOperation("on-absolute-cochain",SimplicialCochain.zero(s.target().ambient(),z(0))).getAlgebra());
        assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation("homotopy-on-chain",RelativeSimplicialChain.zero(s.source(),z(0))).getAlgebra());
        assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation("homotopy-on-cochain",RelativeSimplicialCochain.zero(s.source(),z(1))).getAlgebra());
        assertSame(math.simplicialChains.algebra(),item.performLeftProjectionOperation("homotopy-on-absolute-chain",SimplicialChain.zero(s.source().ambient(),z(0))).getAlgebra());
        assertSame(math.cochains.algebra(),item.performLeftProjectionOperation("homotopy-on-absolute-cochain",SimplicialCochain.zero(s.source().ambient(),z(1))).getAlgebra());
    }
    @Test public void nativeScalarFlatAndAppendFlowsSerializeAndCollectRepeatedly() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCollapse step=SimplicialCollapse.absolute(simplex(2),face(1));
        IAlgebraFlow<Boolean> maps=math.flow(math.complexes,Collections.singletonList(simplex(3))).<SimplicialCollapseSequence>performAlgebraTransfer("CollapseSequence.reduce-absolute")
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism");
        IAlgebraFlow<BigInteger> counts=math.flow(math.complexes,Collections.singletonList(simplex(3))).<SimplicialCollapseSequence>performAlgebraTransfer("CollapseSequence.reduce-absolute")
                .<SimplicialCollapse>performFlatAlgebraTransfer("steps").<FiniteSet<BigInteger>>performAlgebraTransfer("free-face").<BigInteger>performAlgebraTransfer("cardinality");
        IAlgebraFlow<BigInteger> appended=math.flow(math.relativeComplexes,Collections.singletonList(step.source())).<SimplicialCollapseSequence>performAlgebraTransfer("CollapseSequence.identity-on")
                .performCustomMemberOperation("append",step).<BigInteger>performAlgebraTransfer("step-count");
        for(IAlgebraFlow<?> original : Arrays.asList(maps,counts,appended)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Arrays.asList("true","true"),maps.collect()); assertEquals(Arrays.asList("2","1","1"),counts.collect()); assertEquals(Collections.singletonList("1"),appended.collect());
    }
}
