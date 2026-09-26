package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialIsomorphismSearchTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex simplex(int start,int count) { int[] v=new int[count]; for(int i=0;i<count;i++) v[i]=start+i; return complex(v); }
    private static FiniteSimplicialComplex line(int start,int edges) { int[][] f=new int[edges][2]; for(int i=0;i<edges;i++) f[i]=new int[]{start+i,start+i+1}; return complex(f); }
    private static FiniteSimplicialComplex cycle(int start,int size) { int[][] f=new int[size][2]; for(int i=0;i<size;i++) f[i]=new int[]{start+i,start+(i+1)%size}; return complex(f); }
    private static FiniteSimplicialComplex points(int count) { int[][] f=new int[count][1]; for(int i=0;i<count;i++) f[i][0]=i; return complex(f); }
    private static RelativeSimplicialComplex absolute(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static void failure(MathFailure.Kind kind,Runnable body) { assertEquals(kind,assertThrows(MathFailure.class,body::run).kind()); }
    private static boolean closed(int mask,int labels) {
        for(int face=1;face<(1<<labels);face++) if((mask&(1<<(face-1)))!=0)
            for(int sub=(face-1)&face;sub>0;sub=(sub-1)&face) if((mask&(1<<(sub-1)))==0) return false;
        return true;
    }
    private static List<Integer> masks(int labels) { List<Integer> result=new ArrayList<>(); for(int mask=0;mask<(1<<((1<<labels)-1));mask++) if(closed(mask,labels)) result.add(mask); return result; }
    private static FiniteSimplicialComplex fromMask(int mask,int labels) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int face=1;face<(1<<labels);face++) if((mask&(1<<(face-1)))!=0) { List<Integer> v=new ArrayList<>(); for(int i=0;i<labels;i++) if((face&(1<<i))!=0) v.add(i); faces.add(new FiniteSet<>(v)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static List<Integer> vertices(int mask,int labels) { List<Integer> result=new ArrayList<>(); for(int v=0;v<labels;v++) if((mask&(1<<((1<<v)-1)))!=0) result.add(v); return result; }
    private static int image(int mask,int labels,Map<Integer,Integer> map) {
        int result=0;
        for(int face=1;face<(1<<labels);face++) if((mask&(1<<(face-1)))!=0) { int moved=0; for(int v=0;v<labels;v++) if((face&(1<<v))!=0) moved|=1<<map.get(v); result|=1<<(moved-1); }
        return result;
    }
    // Oracle enumerates all label permutations and compares exact face bitsets, with no production pruning.
    private static List<List<BigInteger>> oracle(int source,int sourceA,int target,int targetA,int labels) {
        List<Integer> from=vertices(source,labels),to=vertices(target,labels); List<List<BigInteger>> result=new ArrayList<>();
        if(from.size()==to.size()) permute(source,sourceA,target,targetA,labels,from,to,new TreeMap<>(),new HashSet<>(),result); return result;
    }
    private static void permute(int source,int sourceA,int target,int targetA,int labels,List<Integer> from,List<Integer> to,Map<Integer,Integer> map,Set<Integer> used,List<List<BigInteger>> result) {
        if(map.size()==from.size()) {
            if(image(source,labels,map)==target && image(sourceA,labels,map)==targetA) { List<BigInteger> values=new ArrayList<>(); for(int v : from) values.add(z(map.get(v))); result.add(values); } return;
        }
        int v=from.get(map.size());
        for(int w : to) if(used.add(w)) { map.put(v,w); permute(source,sourceA,target,targetA,labels,from,to,map,used,result); map.remove(v); used.remove(w); }
    }
    @Test public void all27889PairsOfFourLabelComplexesMatchPermutationOracle() {
        List<Integer> masks=masks(4); assertEquals(167,masks.size()); Map<Integer,RelativeSimplicialComplex> pairs=new HashMap<>(); for(int mask : masks) pairs.put(mask,absolute(fromMask(mask,4)));
        for(int source : masks) for(int target : masks) {
            List<List<BigInteger>> expected=oracle(source,0,target,0,4),actual=new ArrayList<>();
            for(RelativeSimplicialMap map : SimplicialIsomorphismSearch.isomorphismsTo(pairs.get(source),pairs.get(target))) { actual.add(map.ambientMap().vertexImages()); assertTrue(map.isIsomorphism()); }
            assertEquals(expected,actual); assertEquals(!expected.isEmpty(),SimplicialIsomorphismSearch.isIsomorphicTo(pairs.get(source),pairs.get(target)));
            if(!expected.isEmpty()) assertEquals(expected.get(0),SimplicialIsomorphismSearch.isomorphismTo(pairs.get(source),pairs.get(target)).ambientMap().vertexImages());
        }
    }
    @Test public void all21904PairsOfThreeLabelPairsMatchBothComponentBitsets() {
        List<int[]> masks=new ArrayList<>(); List<RelativeSimplicialComplex> pairs=new ArrayList<>();
        for(int ambient : masks(3)) for(int subcomplex : masks(3)) if((ambient&subcomplex)==subcomplex) { masks.add(new int[]{ambient,subcomplex}); pairs.add(new RelativeSimplicialComplex(fromMask(ambient,3),fromMask(subcomplex,3))); }
        assertEquals(148,pairs.size());
        for(int i=0;i<pairs.size();i++) for(int j=0;j<pairs.size();j++) {
            List<List<BigInteger>> expected=oracle(masks.get(i)[0],masks.get(i)[1],masks.get(j)[0],masks.get(j)[1],3),actual=new ArrayList<>();
            for(RelativeSimplicialMap map : SimplicialIsomorphismSearch.isomorphismsTo(pairs.get(i),pairs.get(j))) { actual.add(map.ambientMap().vertexImages()); assertTrue(map.isIsomorphism()); }
            assertEquals(expected,actual);
        }
    }
    @Test public void sameSkeletonAndFaceCountsDoNotReplaceFullSimplicialChecks() {
        List<int[]> base=new ArrayList<>(); for(int i=0;i<5;i++) for(int j=i+1;j<5;j++) base.add(new int[]{i,j});
        FiniteSimplicialComplex skeleton=complex(base.toArray(new int[0][]));
        RelativeSimplicialComplex a=absolute(skeleton.union(complex(new int[]{0,1,2},new int[]{0,1,3}))),b=absolute(skeleton.union(complex(new int[]{0,1,2},new int[]{0,3,4})));
        assertFalse(SimplicialIsomorphismSearch.isIsomorphicTo(a,b));
        // Both have all vertices and edges, and two triangles, but the triangles intersect in different dimensions.
        assertEquals(a.ambient().skeleton(1),b.ambient().skeleton(1)); assertEquals(a.ambient().simplices(2).size(),b.ambient().simplices(2).size());
    }
    @Test public void nonisomorphismHasDistinctPredicatePartialAndFlatResults() {
        RelativeSimplicialComplex a=absolute(cycle(0,3)),b=absolute(line(0,2));
        assertFalse(SimplicialIsomorphismSearch.isIsomorphicTo(a,b)); assertTrue(SimplicialIsomorphismSearch.isomorphismsTo(a,b).isEmpty());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialIsomorphismSearch.isomorphismTo(a,b));
        assertFalse(SimplicialIsomorphismSearch.isIsomorphicTo(new RelativeSimplicialComplex(line(0,2),simplex(0,1)),new RelativeSimplicialComplex(line(0,2),simplex(1,1))));
        assertFalse(SimplicialIsomorphismSearch.isIsomorphicTo(absolute(cycle(0,3).union(cycle(3,3))),absolute(cycle(0,6))));
    }
    @Test public void emptyExtremeLabelsAndInsertionOrderKeepDeterministicImages() {
        RelativeSimplicialComplex empty=absolute(complex()); assertEquals(Collections.singletonList(RelativeSimplicialMap.identity(empty)),SimplicialIsomorphismSearch.isomorphismsTo(empty,empty));
        RelativeSimplicialComplex source=absolute(complex(new int[]{Integer.MIN_VALUE,0},new int[]{0,Integer.MAX_VALUE})),target=absolute(complex(new int[]{30,20},new int[]{20,10}));
        assertEquals(Arrays.asList(z(10),z(20),z(30)),SimplicialIsomorphismSearch.isomorphismTo(source,target).ambientMap().vertexImages());
        RelativeSimplicialComplex reversed=absolute(complex(new int[]{10,20},new int[]{20,30})); assertEquals(SimplicialIsomorphismSearch.isomorphismsTo(source,target),SimplicialIsomorphismSearch.isomorphismsTo(source,reversed));
        assertThrows(UnsupportedOperationException.class,() -> SimplicialIsomorphismSearch.isomorphismsTo(source,target).clear());
    }
    @Test public void enumerationCapsDoNotLimitFirstWitnessSearchOrReturnPrefixes() {
        RelativeSimplicialComplex six=absolute(points(6)),seven=absolute(points(7)); assertEquals(720,SimplicialIsomorphismSearch.isomorphismsTo(six,six).size());
        assertTrue(SimplicialIsomorphismSearch.isIsomorphicTo(seven,seven)); assertEquals(RelativeSimplicialMap.identity(seven),SimplicialIsomorphismSearch.isomorphismTo(seven,seven));
        MathFailure error=assertThrows(MathFailure.class,() -> SimplicialIsomorphismSearch.isomorphismsTo(seven,seven)); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,error.kind()); assertTrue(error.getMessage().contains("1024"));
    }
    @Test public void vertexAndExhaustiveWorkLimitsRemainImplementationFailures() {
        RelativeSimplicialComplex atLimit=absolute(points(64)); assertEquals(RelativeSimplicialMap.identity(atLimit),SimplicialIsomorphismSearch.isomorphismTo(atLimit,atLimit));
        RelativeSimplicialComplex tooLarge=absolute(points(65)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialIsomorphismSearch.isIsomorphicTo(tooLarge,tooLarge));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialIsomorphismSearch.isomorphismTo(tooLarge,tooLarge)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialIsomorphismSearch.isomorphismsTo(tooLarge,tooLarge));
        RelativeSimplicialComplex a=absolute(points(8).union(cycle(8,3)).union(cycle(11,3))),b=absolute(points(8).union(cycle(8,6)));
        for(Runnable run : Arrays.<Runnable>asList(() -> SimplicialIsomorphismSearch.isIsomorphicTo(a,b),() -> SimplicialIsomorphismSearch.isomorphismTo(a,b),() -> SimplicialIsomorphismSearch.isomorphismsTo(a,b))) {
            MathFailure error=assertThrows(MathFailure.class,run::run); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,error.kind()); assertTrue(error.getMessage().contains("5000000"));
        }
    }
    private static int graphMask(int edges) {
        int result=(1<<0)|(1<<1)|(1<<3)|(1<<7),index=0;
        for(int i=0;i<4;i++) for(int j=i+1;j<4;j++) if((edges&(1<<index++))!=0) result|=1<<((1<<i)+(1<<j)-1); return result;
    }
    private static int graphCore(int mask) {
        while(true) {
            int remove=-1;
            for(int v=0;v<4 && remove<0;v++) if((mask&(1<<((1<<v)-1)))!=0) { int degree=0; for(int w=0;w<4;w++) if(v!=w && (mask&(1<<((1<<v)+(1<<w)-1)))!=0) degree++; if(degree==1) remove=v; }
            if(remove<0) return mask;
            for(int face=1;face<16;face++) if((face&(1<<remove))!=0) mask&=~(1<<(face-1));
        }
    }
    private static void inverseMaps(SimplicialHomotopyEquivalence e,int degree) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(e.homologyMaps(z(degree)),e.cohomologyMaps(z(degree)))) {
            AbelianGroupHomomorphism f=maps.get(0),g=maps.get(1); assertEquals(AbelianGroupHomomorphism.identity(f.source()),g.compose(f)); assertEquals(AbelianGroupHomomorphism.identity(f.target()),f.compose(g));
        }
    }
    @Test public void all4096GraphPairsAgreeWithIndependentLeafCorePermutationOracle() {
        for(int a=0;a<64;a++) for(int b=0;b<64;b++) {
            int left=graphMask(a),right=graphMask(b); boolean expected=!oracle(graphCore(left),0,graphCore(right),0,4).isEmpty();
            assertEquals(expected,SimplicialStrongCollapse.stronglyEquivalent(fromMask(left,4),fromMask(right,4)));
            if(expected) { SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongEquivalenceTo(fromMask(left,4),fromMask(right,4)); inverseMaps(e,1); }
        }
    }
    @Test public void circleSubdivisionsDistinguishStrongTypeFromOrdinaryHomotopyAndHomology() {
        FiniteSimplicialComplex triangle=cycle(0,3),square=cycle(10,4);
        assertEquals(triangle.integralHomology(z(1)),square.integralHomology(z(1))); assertFalse(SimplicialStrongCollapse.stronglyEquivalent(triangle,square));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialStrongCollapse.strongEquivalenceTo(triangle,square));
        assertFalse(SimplicialStrongCollapse.stronglyEquivalent(complex(),simplex(0,1))); assertTrue(SimplicialStrongCollapse.stronglyEquivalent(complex(),complex()));
    }
    @Test public void unequalSizeRelabelledTreesYieldExactInverseWitnessesAndTypedFillings() {
        FiniteSimplicialComplex source=line(0,3),target=line(10,5); SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongEquivalenceTo(source,target);
        assertEquals(absolute(source),e.source()); assertEquals(absolute(target),e.target()); assertEquals(z(3),e.sourceHomotopy().stepCount()); assertEquals(z(5),e.targetHomotopy().stepCount()); inverseMaps(e,0);
        assertEquals(e.backward().compose(e.forward()),e.sourceHomotopy().to()); assertEquals(e.forward().compose(e.backward()),e.targetHomotopy().to());
        RelativeSimplicialChain cycle=new RelativeSimplicialChain(e.source(),z(0),new mathematics.linear.IntegerVector(z(2),z(0),z(0),z(0)));
        assertEquals(cycle.pushforward(e.forward()).pushforward(e.backward()).subtract(cycle),e.sourceHomotopy().onChain(cycle).boundary());
        assertTrue(SimplicialStrongCollapse.stronglyEquivalent(line(0,70),simplex(-100,1)));
    }
    @Test public void relabelledProjectivePlanesWithLeavesRetainIntegralTorsion() {
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        List<FiniteSet<Integer>> renamed=new ArrayList<>(); for(int degree=0;degree<=2;degree++) for(FiniteSet<Integer> face : rp2.simplices(degree)) { List<Integer> v=new ArrayList<>(); for(int value : face.members()) v.add(20-value); renamed.add(new FiniteSet<>(v)); }
        SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongEquivalenceTo(rp2.union(line(5,2)),new FiniteSimplicialComplex(renamed).union(line(20,3)));
        assertEquals(Collections.singletonList(z(2)),e.forwardHomologyMap(z(1)).source().type().invariantFactors()); assertEquals(Collections.singletonList(z(2)),e.forwardCohomologyMap(z(2)).source().type().invariantFactors()); inverseMaps(e,1); inverseMaps(e,2);
    }
    @Test public void twoCoreReductionsAndSearchShareOneBudget() {
        FiniteSimplicialComplex large=simplex(1000,11).union(line(0,50)); assertNotNull(SimplicialStrongCollapse.strongCoreAbsolute(large));
        for(Runnable run : Arrays.<Runnable>asList(() -> SimplicialStrongCollapse.stronglyEquivalent(large,large),() -> SimplicialStrongCollapse.strongEquivalenceTo(large,large))) {
            MathFailure error=assertThrows(MathFailure.class,run::run); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,error.kind()); assertTrue(error.getMessage().contains("5000000"));
        }
    }
    @Test public void nativeCustomResultAndFlatFlowsUseRegisteredWrappersAndSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=absolute(line(0,2));
        IAlgebraItem<RelativeSimplicialComplex> item=math.relativeComplexes.algebra().buildAlgebraItem(pair);
        List<IAlgebraItem<RelativeSimplicialMap>> maps=item.performCustomResultFlatOperation("RelativeMap.isomorphisms-to",pair); assertEquals(2,maps.size()); for(IAlgebraItem<RelativeSimplicialMap> map : maps) assertSame(math.relativeMaps.algebra(),map.getAlgebra());
        assertSame(math.relativeMaps.algebra(),item.performCustomResultOperation("RelativeMap.isomorphism-to",pair).getAlgebra());
        IAlgebraFlow<Boolean> flow=math.flow(math.complexes,Collections.singletonList(line(0,3))).<SimplicialHomotopyEquivalence>performCustomResultOperation("HomotopyEquivalence.strong-equivalence-to",line(10,5))
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("true","true"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","true"),math.flow(math.relativeComplexes,Collections.singletonList(pair)).<RelativeSimplicialMap>performFlatCustomResultOperation("RelativeMap.isomorphisms-to",pair).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
