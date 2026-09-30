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

public class NativeSimplicialCollapseTest {
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
    private static void identities(SimplicialCollapse collapse,int k) {
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
    private static void inverseMaps(SimplicialCollapse c,int k) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(c.homologyMaps(z(k)),c.cohomologyMaps(z(k)))) {
            AbelianGroupHomomorphism f=maps.get(0),g=maps.get(1); assertEquals(AbelianGroupHomomorphism.identity(f.source()),g.compose(f)); assertEquals(AbelianGroupHomomorphism.identity(f.target()),f.compose(g));
        }
        assertEquals(c.homologyMaps(z(k)).get(0),c.homologyMap(z(k))); assertEquals(c.homologyMaps(z(k)).get(1),c.inverseHomologyMap(z(k)));
        assertEquals(c.cohomologyMaps(z(k)).get(0),c.cohomologyMap(z(k))); assertEquals(c.cohomologyMaps(z(k)).get(1),c.inverseCohomologyMap(z(k)));
    }
    @Test public void all167FourLabelComplexesMatchStrictCofaceOracleAndChainIdentities() {
        int checked=0; for(int mask=0;mask<(1<<15);mask++) if(closed(mask,4)) {
            checked++; RelativeSimplicialComplex pair=abs(fromMask(mask,4)); Map<Integer,Integer> free=oracle(mask,0,4); Set<FiniteSet<BigInteger>> expected=new HashSet<>(); for(int f : free.keySet()) expected.add(fromBits(f,4));
            assertEquals(expected,new HashSet<>(SimplicialCollapse.freeFaces(pair))); assertEquals(!free.isEmpty(),SimplicialCollapse.hasFreeFace(pair));
            for(Map.Entry<Integer,Integer> move : free.entrySet()) {
                SimplicialCollapse c=new SimplicialCollapse(pair,fromBits(move.getKey(),4)); int remaining=mask&~(1<<(move.getKey()-1))&~(1<<(move.getValue()-1));
                assertEquals(abs(fromMask(remaining,4)),c.target()); assertEquals(fromBits(move.getValue(),4),c.coface());
                for(int k=0;k<=3;k++) identities(c,k);
            }
        } assertEquals(167,checked);
    }
    @Test public void all148ThreeLabelPairsPreserveBothComponentsAndIntegralInverses() {
        int checked=0; for(int x=0;x<128;x++) if(closed(x,3)) for(int a=0;a<128;a++) if(closed(a,3) && (x&a)==a) {
            checked++; RelativeSimplicialComplex pair=new RelativeSimplicialComplex(fromMask(x,3),fromMask(a,3)); Map<Integer,Integer> free=oracle(x,a,3); Set<FiniteSet<BigInteger>> expected=new HashSet<>(); for(int f : free.keySet()) expected.add(fromBits(f,3)); assertEquals(expected,new HashSet<>(SimplicialCollapse.freeFaces(pair)));
            for(Map.Entry<Integer,Integer> move : free.entrySet()) {
                int removed=(1<<(move.getKey()-1))|(1<<(move.getValue()-1)); SimplicialCollapse c=new SimplicialCollapse(pair,fromBits(move.getKey(),3));
                assertEquals(new RelativeSimplicialComplex(fromMask(x&~removed,3),fromMask(a&~removed,3)),c.target());
                for(int k=0;k<=2;k++) { identities(c,k); inverseMaps(c,k); }
            }
        } assertEquals(148,checked);
    }
    @Test public void all1024GraphAndVertexSubcomplexPairsCollapseExactlyUnprotectedLeaves() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int graph=0;graph<64;graph++) {
            FiniteSimplicialComplex ambient=points(0,4); int[] degrees=new int[4];
            for(int e=0;e<6;e++) if((graph&(1<<e))!=0) { ambient=ambient.union(complex(edges[e])); degrees[edges[e][0]]++; degrees[edges[e][1]]++; }
            for(int a=0;a<16;a++) {
                FiniteSimplicialComplex sub=complex(); Set<FiniteSet<BigInteger>> expected=new HashSet<>();
                for(int v=0;v<4;v++) { if((a&(1<<v))!=0) sub=sub.union(complex(new int[]{v})); else if(degrees[v]==1) expected.add(face(v)); }
                assertEquals(expected,new HashSet<>(SimplicialCollapse.freeFaces(new RelativeSimplicialComplex(ambient,sub))));
            }
        }
    }
    @Test public void everySimplexFacetThroughDimensionSevenHasTheCorrectSignedContraction() {
        for(int count=2;count<=8;count++) for(int omitted=0;omitted<count;omitted++) {
            int[] f=new int[count-1]; for(int i=0,j=0;i<count;i++) if(i!=omitted) f[j++]=i; SimplicialCollapse c=SimplicialCollapse.absolute(simplex(count),face(f));
            List<FiniteSet<BigInteger>> basis=c.source().simplexBasis(z(count-2)); assertEquals(z((omitted&1)==0?1:-1),c.chainHomotopyMatrix(z(count-2)).get(0,basis.indexOf(face(f))));
            for(int k=0;k<count;k++) identities(c,k);
        }
    }
    @Test public void aTriangleEdgeRetractsToTheOtherTwoEdgesWithoutASimplicialVertexRetraction() {
        SimplicialCollapse c=SimplicialCollapse.absolute(simplex(3),face(0,2)); SimplicialChain edge=new SimplicialChain(simplex(3),z(1),new IntegerVector(z(0),z(1),z(0)));
        assertEquals(complex(new int[]{0,1},new int[]{1,2}),c.target().ambient()); assertEquals(new IntegerVector(z(1),z(1)),c.onAbsoluteChain(edge).coordinates());
        assertEquals(new IntegerVector(z(-1)),c.homotopyOnAbsoluteChain(edge).coordinates());
        Map<BigInteger,BigInteger> identity=new TreeMap<>(); for(int v=0;v<3;v++) identity.put(z(v),z(v));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new FiniteSimplicialMap(c.source().ambient(),c.target().ambient(),identity));
        assertEquals(edge.subtract(c.onAbsoluteChain(edge).pushforward(c.inclusion().ambientMap())),c.homotopyOnAbsoluteChain(edge).boundary());
    }
    @Test public void all729TernaryChainsAndCochainsSatisfyTypedIdentitiesAndPairing() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2},new int[]{2,3},new int[]{3,4},new int[]{4,5}); SimplicialCollapse c=SimplicialCollapse.absolute(x,face(0,2));
        RelativeSimplicialCochain target=new RelativeSimplicialCochain(c.target(),z(1),new IntegerVector(z(1),z(2),z(3),z(4),z(5)));
        for(int code=0;code<729;code++) {
            BigInteger[] values=new BigInteger[6]; int remaining=code; for(int i=0;i<6;i++) { values[i]=z(remaining%3-1); remaining/=3; }
            RelativeSimplicialChain chain=new RelativeSimplicialChain(c.source(),z(1),new IntegerVector(values));
            assertEquals(chain.subtract(c.onChain(chain).pushforward(c.inclusion())),c.homotopyOnChain(chain).boundary().add(c.homotopyOnChain(chain.boundary())));
            RelativeSimplicialCochain cochain=new RelativeSimplicialCochain(c.source(),z(1),new IntegerVector(values));
            assertEquals(cochain.subtract(c.onCochain(cochain.pullback(c.inclusion()))),c.homotopyOnCochain(cochain).coboundary().add(c.homotopyOnCochain(cochain.coboundary())));
            assertEquals(c.onChain(chain).evaluate(target),chain.evaluate(c.onCochain(target)));
        }
    }
    @Test public void relativeCollapseRejectsProtectedFacesAndAllowsCancellationInsideTheSubcomplex() {
        RelativeSimplicialComplex disk=new RelativeSimplicialComplex(simplex(3),simplex(3).skeleton(1)); assertFalse(SimplicialCollapse.hasFreeFace(disk));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialCollapse(disk,face(0,2)));
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(new int[]{0,1,2},new int[]{1,2,3}),simplex(3)); SimplicialCollapse c=new SimplicialCollapse(pair,face(0,2));
        assertEquals(complex(new int[]{0,1},new int[]{1,2}),c.target().subcomplex());
        for(int k=0;k<=2;k++) { assertEquals(IntegerMatrix.identity(pair.simplexCount(z(k))),c.chainMatrix(z(k))); assertEquals(IntegerMatrix.zero(pair.simplexCount(z(k+1)),pair.simplexCount(z(k))),c.chainHomotopyMatrix(z(k))); inverseMaps(c,k); }
    }
    @Test public void projectivePlaneWithAnAttachedTriangleRetainsTorsionAndTypedCycleFillings() {
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        assertFalse(SimplicialCollapse.hasFreeFace(abs(rp2))); SimplicialCollapse c=SimplicialCollapse.absolute(rp2.union(complex(new int[]{0,1,6})),face(0,6));
        assertEquals(Collections.singletonList(z(2)),c.homologyMap(z(1)).target().type().invariantFactors()); assertEquals(Collections.singletonList(z(2)),c.cohomologyMap(z(2)).source().type().invariantFactors());
        for(int k=0;k<=2;k++) inverseMaps(c,k);
        for(RelativeSimplicialChain cycle : RelativeSimplicialChain.zero(c.source(),z(1)).cycleGenerators()) {
            assertEquals(cycle.subtract(c.onChain(cycle).pushforward(c.inclusion())),c.homotopyOnChain(cycle).boundary()); assertTrue(c.onChain(cycle).isCycle());
        }
    }
    @Test public void emptyClosedAndDisconnectedComplexesKeepTheirUnreducedComponents() {
        for(FiniteSimplicialComplex x : Arrays.asList(complex(),points(0,4),simplex(4).skeleton(2),complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}))) assertFalse(SimplicialCollapse.hasFreeFace(abs(x)));
        SimplicialCollapse c=SimplicialCollapse.absolute(complex(new int[]{0,1},new int[]{5}),face(1)); assertEquals(points(0,1).union(points(5,6)),c.target().ambient()); assertEquals(z(2),c.homologyMap(z(0)).target().type().freeRank()); inverseMaps(c,0);
    }
    @Test public void invalidFacesAndWrongTypedContextsAreUndefined() {
        RelativeSimplicialComplex triangle=abs(simplex(3));
        for(FiniteSet<BigInteger> f : Arrays.asList(face(),face(0),face(0,1,2),face(8),FiniteSet.of(BigInteger.TEN.pow(100)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialCollapse(triangle,f));
        SimplicialCollapse c=SimplicialCollapse.absolute(simplex(2),face(1)); RelativeSimplicialComplex wrong=RelativeSimplicialComplex.diagonal(c.source().ambient());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c.onChain(RelativeSimplicialChain.zero(wrong,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c.onCochain(RelativeSimplicialCochain.zero(c.source(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c.homotopyOnChain(RelativeSimplicialChain.zero(c.target(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c.homotopyOnCochain(RelativeSimplicialCochain.zero(wrong,z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c.homotopyOnCochain(RelativeSimplicialCochain.zero(c.source(),z(0))));
        SimplicialCollapse relative=new SimplicialCollapse(RelativeSimplicialComplex.diagonal(simplex(2)),face(1));
        for(Runnable action : Arrays.<Runnable>asList(() -> relative.onAbsoluteChain(SimplicialChain.zero(simplex(2),z(0))),() -> relative.onAbsoluteCochain(SimplicialCochain.zero(relative.target().ambient(),z(0))),() -> relative.homotopyOnAbsoluteChain(SimplicialChain.zero(simplex(2),z(0))),() -> relative.homotopyOnAbsoluteCochain(SimplicialCochain.zero(simplex(2),z(1))))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
    }
    @Test public void extremeLabelsAndFacetOrderGiveDeterministicFacesAndWitnesses() {
        SimplicialCollapse c=SimplicialCollapse.absolute(complex(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE}),face(Integer.MIN_VALUE));
        assertEquals(face(Integer.MAX_VALUE),FiniteSimplicialMap.vertexSet(c.target().ambient())); assertEquals(new IntegerMatrix(new BigInteger[][]{{z(-1),z(0)}}),c.chainHomotopyMatrix(z(0)));
        SimplicialCollapse a=SimplicialCollapse.absolute(complex(new int[]{0,1,2},new int[]{3,4}),face(0,2)),b=SimplicialCollapse.absolute(complex(new int[]{4,3},new int[]{2,1,0}),face(2,0));
        assertEquals(a,b); assertEquals(a.hashCode(),b.hashCode()); assertEquals(a.chainMatrices(),b.chainMatrices()); assertEquals(Arrays.asList(face(3),face(4),face(0,1),face(0,2),face(1,2)),SimplicialCollapse.freeFaces(a.source()));
    }
    @Test public void geometricLimitsAreSeparateFromFilteredBasisLimits() {
        FiniteSimplicialComplex many=points(0,4095).union(simplex(2)); SimplicialCollapse large=SimplicialCollapse.absolute(many,face(1));
        assertEquals(4094,FiniteSimplicialMap.vertexSet(large.target().ambient()).size()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.chainMatrix(z(0)));
        SimplicialCollapse filtered=new SimplicialCollapse(new RelativeSimplicialComplex(many,points(2,4095)),face(1)); assertEquals(new IntegerMatrix(new BigInteger[][]{{z(1),z(1)}}),filtered.chainMatrix(z(0))); inverseMaps(filtered,0);
        int[] facet=new int[11]; for(int i=0;i<11;i++) facet[i]=i; SimplicialCollapse high=SimplicialCollapse.absolute(simplex(12),face(facet));
        assertEquals(1,high.chainHomotopyMatrix(z(10)).rows()); assertEquals(12,high.chainHomotopyMatrix(z(10)).columns()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,high::chainMatrices);
    }
    @Test public void degreeConventionsAndImmutableListsRetainAllZeroShapes() {
        SimplicialCollapse c=SimplicialCollapse.absolute(simplex(2),face(1)); BigInteger huge=BigInteger.TEN.pow(100);
        assertEquals(IntegerMatrix.zero(0,0),c.chainMatrix(huge)); assertEquals(IntegerMatrix.zero(0,0),c.chainHomotopyMatrix(huge)); assertTrue(c.homologyMap(huge).isIsomorphism()); assertTrue(c.cohomologyMap(huge).isIsomorphism());
        assertEquals(IntegerMatrix.zero(0,2),c.cochainHomotopyMatrix(z(0))); assertEquals(RelativeSimplicialChain.zero(c.target(),z(-2)),c.onChain(RelativeSimplicialChain.zero(c.source(),z(-2)))); assertEquals(RelativeSimplicialChain.zero(c.source(),z(0)),c.homotopyOnChain(RelativeSimplicialChain.zero(c.source(),z(-1))));
        for(Runnable action : Arrays.<Runnable>asList(() -> c.chainMatrix(z(-1)),() -> c.cochainMatrix(z(-1)),() -> c.chainHomotopyMatrix(z(-1)),() -> c.cochainHomotopyMatrix(z(-1)),() -> c.homologyMap(z(-1)),() -> c.inverseHomologyMap(z(-1)),() -> c.homologyMaps(z(-1)),() -> c.cohomologyMap(z(-1)),() -> c.inverseCohomologyMap(z(-1)),() -> c.cohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        assertEquals(2,c.chainMatrices().size()); assertEquals(3,c.cochainHomotopyMatrices().size());
        for(List<?> list : Arrays.asList(c.chainMatrices(),c.cochainMatrices(),c.chainHomotopyMatrices(),c.cochainHomotopyMatrices(),c.homologyMaps(z(0)),c.cohomologyMaps(z(0)),SimplicialCollapse.freeFaces(c.source()))) assertThrows(UnsupportedOperationException.class,list::clear);
    }
    @Test public void theTwoIntegralMapListsShareTheirWholeComputationBudget() {
        SimplicialCollapse c=SimplicialCollapse.absolute(points(0,75).union(simplex(2)),face(1));
        assertNotNull(c.homologyMap(z(0))); assertNotNull(c.inverseHomologyMap(z(0))); assertNotNull(c.cohomologyMap(z(0))); assertNotNull(c.inverseCohomologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> c.homologyMaps(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> c.cohomologyMaps(z(0)));
    }
    @Test public void allEightTypedActionsUseTheActualSecondOperandWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCollapse c=SimplicialCollapse.absolute(simplex(2),face(1)); IAlgebraItem<SimplicialCollapse> item=math.collapses.algebra().buildAlgebraItem(c);
        assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation("on-chain",RelativeSimplicialChain.zero(c.source(),z(0))).getAlgebra());
        assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation("on-cochain",RelativeSimplicialCochain.zero(c.target(),z(0))).getAlgebra());
        assertSame(math.simplicialChains.algebra(),item.performLeftProjectionOperation("on-absolute-chain",SimplicialChain.zero(c.source().ambient(),z(0))).getAlgebra());
        assertSame(math.cochains.algebra(),item.performLeftProjectionOperation("on-absolute-cochain",SimplicialCochain.zero(c.target().ambient(),z(0))).getAlgebra());
        assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation("homotopy-on-chain",RelativeSimplicialChain.zero(c.source(),z(0))).getAlgebra());
        assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation("homotopy-on-cochain",RelativeSimplicialCochain.zero(c.source(),z(1))).getAlgebra());
        assertSame(math.simplicialChains.algebra(),item.performLeftProjectionOperation("homotopy-on-absolute-chain",SimplicialChain.zero(c.source().ambient(),z(0))).getAlgebra());
        assertSame(math.cochains.algebra(),item.performLeftProjectionOperation("homotopy-on-absolute-cochain",SimplicialCochain.zero(c.source().ambient(),z(1))).getAlgebra());
    }
    @Test public void nativeScalarAndFlatFlowsSerializeAndCollectRepeatedly() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraFlow<Boolean> flow=math.flow(math.complexes,Collections.singletonList(simplex(3))).<SimplicialCollapse,FiniteSet<BigInteger>>performAlgebraUnsafe("SimplicialCollapse.from-absolute-face",face(0,2))
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism");
        IAlgebraFlow<BigInteger> faces=math.flow(math.relativeComplexes,Collections.singletonList(abs(simplex(3)))).<FiniteSet<BigInteger>>performFlatAlgebraTransfer("SimplicialCollapse.free-faces").<BigInteger>performAlgebraTransfer("cardinality");
        for(IAlgebraFlow<?> original : Arrays.asList(flow,faces)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Arrays.asList("true","true"),flow.collect()); assertEquals(Arrays.asList("2","2","2"),faces.collect());
    }
}
