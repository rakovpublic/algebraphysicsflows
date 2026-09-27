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

public class NativeSimplicialCollapseSearchTest {
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
    private static long key(int x,int a) { return ((long)x<<32)|(a&0xffffffffL); }
    // Breadth-first traversal of face bitsets, without Euler pruning, target protection or native collapse objects.
    private static Set<Long> reachable(int x,int a,int labels) {
        Set<Long> seen=new HashSet<>(); ArrayDeque<Long> queue=new ArrayDeque<>(); seen.add(key(x,a)); queue.add(key(x,a));
        while(!queue.isEmpty()) {
            long state=queue.remove(); int ambient=(int)(state>>>32),sub=(int)state;
            for(Map.Entry<Integer,Integer> move : oracle(ambient,sub,labels).entrySet()) {
                int removed=(1<<(move.getKey()-1))|(1<<(move.getValue()-1)); long next=key(ambient&~removed,sub&~removed);
                if(seen.add(next)) queue.add(next);
            }
        }
        return seen;
    }
    private static List<Integer> masks(int labels) { List<Integer> result=new ArrayList<>(); for(int x=0;x<(1<<((1<<labels)-1));x++) if(closed(x,labels)) result.add(x); return result; }
    private static RelativeSimplicialComplex pair(int x,int a,int labels) { return new RelativeSimplicialComplex(fromMask(x,labels),fromMask(a,labels)); }
    @Test public void all27889FourLabelComplexComparisonsMatchUnprunedReachability() {
        List<Integer> masks=masks(4); assertEquals(167,masks.size());
        for(int x : masks) {
            Set<Long> expected=reachable(x,0,4); FiniteSimplicialComplex source=fromMask(x,4); boolean point=false;
            for(int y : masks) {
                FiniteSimplicialComplex target=fromMask(y,4); boolean can=expected.contains(key(y,0));
                assertEquals(can,SimplicialCollapseSearch.absoluteCanCollapseTo(source,target));
                if(can) { SimplicialCollapseSequence s=SimplicialCollapseSearch.absoluteCollapseTo(source,target); assertEquals(abs(source),s.source()); assertEquals(abs(target),s.target()); }
                if(Integer.bitCount(y)==1 && can) point=true;
            }
            assertEquals(point,SimplicialCollapseSearch.isCollapsible(source));
        }
    }
    private static List<List<Integer>> oraclePaths(int x,int a,int y,int b,int labels) {
        List<List<Integer>> result=new ArrayList<>();
        if(x==y && a==b) { result.add(Collections.emptyList()); return result; }
        Map<Integer,Integer> moves=oracle(x,a,labels); List<Integer> faces=new ArrayList<>(moves.keySet());
        faces.sort((u,v) -> { int size=Integer.compare(Integer.bitCount(u),Integer.bitCount(v)); if(size!=0) return size; for(int i=0;i<labels;i++) if(((u^v)&(1<<i))!=0) return (u&(1<<i))!=0?-1:1; return 0; });
        for(int f : faces) {
            int removed=(1<<(f-1))|(1<<(moves.get(f)-1));
            for(List<Integer> tail : oraclePaths(x&~removed,a&~removed,y,b,labels)) { List<Integer> path=new ArrayList<>(); path.add(f); path.addAll(tail); result.add(path); }
        }
        return result;
    }
    private static List<Integer> signature(SimplicialCollapseSequence s) {
        List<Integer> result=new ArrayList<>(); for(SimplicialCollapse step : s.steps()) { int bits=0; for(BigInteger vertex : step.freeFace().members()) bits|=1<<vertex.intValueExact(); result.add(bits); } return result;
    }
    @Test public void all21904ThreeLabelPairComparisonsMatchReachabilityAndCompleteOrderedPaths() {
        List<int[]> pairs=new ArrayList<>(); for(int x : masks(3)) for(int a : masks(3)) if((x&a)==a) pairs.add(new int[]{x,a}); assertEquals(148,pairs.size());
        for(int[] from : pairs) {
            RelativeSimplicialComplex source=pair(from[0],from[1],3); Set<Long> reach=reachable(from[0],from[1],3);
            for(int[] to : pairs) {
                RelativeSimplicialComplex target=pair(to[0],to[1],3); assertEquals(reach.contains(key(to[0],to[1])),SimplicialCollapseSearch.canCollapseTo(source,target));
                List<List<Integer>> expected=oraclePaths(from[0],from[1],to[0],to[1],3),actual=new ArrayList<>();
                List<SimplicialCollapseSequence> paths=SimplicialCollapseSearch.collapsesTo(source,target);
                for(SimplicialCollapseSequence path : paths) { assertEquals(target,path.target()); actual.add(signature(path)); }
                assertEquals(expected,actual);
                if(!paths.isEmpty()) assertEquals(paths.get(0),SimplicialCollapseSearch.collapseTo(source,target));
            }
        }
    }
    @Test public void all64GraphsCollapseToAPointExactlyWhenTheyAreTrees() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int code=0;code<64;code++) {
            FiniteSimplicialComplex graph=points(0,4); boolean[] reached={true,false,false,false};
            for(int e=0;e<6;e++) if((code&(1<<e))!=0) graph=graph.union(complex(edges[e]));
            for(int round=0;round<4;round++) for(int e=0;e<6;e++) if((code&(1<<e))!=0 && (reached[edges[e][0]] || reached[edges[e][1]])) reached[edges[e][0]]=reached[edges[e][1]]=true;
            boolean tree=Integer.bitCount(code)==3 && reached[1] && reached[2] && reached[3]; assertEquals(tree,SimplicialCollapseSearch.isCollapsible(graph));
            for(int v=0;v<4;v++) assertEquals(tree,SimplicialCollapseSearch.absoluteCanCollapseTo(graph,complex(new int[]{v})));
        }
    }
    @Test public void targetProtectionFindsWitnessesDifferentFromTheUnconstrainedGreedyReduction() {
        FiniteSimplicialComplex x=simplex(3),edge=complex(new int[]{0,1}); SimplicialCollapseSequence s=SimplicialCollapseSearch.absoluteCollapseTo(x,edge);
        assertEquals(face(0,2),s.steps().get(0).freeFace()); assertEquals(face(2),s.steps().get(1).freeFace()); assertEquals(abs(edge),s.target());
        assertFalse(edge.subcomplexOf(SimplicialCollapseSequence.reduceAbsolute(x).target().ambient()));
        assertEquals(2,SimplicialCollapseSearch.absoluteCollapsesTo(x,edge).size()); for(int k=0;k<3;k++) { identities(s,k); inverseMaps(s,k); }
    }
    @Test public void successfulSuffixesAreRetainedForEveryPrefixAndPointEnumerationHasStableOrder() {
        List<SimplicialCollapseSequence> triangle=SimplicialCollapseSearch.collapsesToPoint(simplex(3)); assertEquals(12,triangle.size()); assertEquals(12,new HashSet<>(triangle).size());
        assertEquals(SimplicialCollapseSearch.collapseToPoint(simplex(3)),triangle.get(0));
        for(int v=0;v<3;v++) {
            List<SimplicialCollapseSequence> to=SimplicialCollapseSearch.absoluteCollapsesTo(simplex(3),complex(new int[]{v})); assertEquals(4,to.size());
            assertEquals(oraclePaths(127,0,1<<((1<<v)-1),0,3),signatures(to));
        }
        List<SimplicialCollapseSequence> tetra=SimplicialCollapseSearch.absoluteCollapsesTo(simplex(4),complex(new int[]{0})); assertEquals(480,tetra.size()); assertEquals(480,new HashSet<>(tetra).size());
        assertEquals(oraclePaths(32767,0,1,0,4),signatures(tetra));
        assertThrows(UnsupportedOperationException.class,triangle::clear);
    }
    private static List<List<Integer>> signatures(List<SimplicialCollapseSequence> paths) { List<List<Integer>> result=new ArrayList<>(); for(SimplicialCollapseSequence s : paths) result.add(signature(s)); return result; }
    @Test public void pairSearchPreservesTheExactSubcomplexAndAllowsInternalCancellations() {
        FiniteSimplicialComplex x=simplex(3),point=complex(new int[]{0}); RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(x),end=RelativeSimplicialComplex.diagonal(point);
        SimplicialCollapseSequence s=SimplicialCollapseSearch.collapseTo(diagonal,end); assertEquals(z(3),s.stepCount()); for(int k=0;k<3;k++) identities(s,k);
        assertFalse(SimplicialCollapseSearch.canCollapseTo(diagonal,abs(point))); // same ambient endpoint, wrong subcomplex membership
        RelativeSimplicialComplex disk=new RelativeSimplicialComplex(x,x.skeleton(1)); assertFalse(SimplicialCollapseSearch.canCollapseTo(disk,new RelativeSimplicialComplex(x.skeleton(1),x.skeleton(1))));
        RelativeSimplicialComplex based=new RelativeSimplicialComplex(complex(new int[]{0,1},new int[]{1,2}),point);
        assertEquals(end,SimplicialCollapseSearch.collapseTo(based,end).target()); assertFalse(SimplicialCollapseSearch.canCollapseTo(based,abs(complex(new int[]{2}))));
    }
    // Hachimori's 8-vertex, 17-triangle dunce-hat triangulation, also tabulated by SageMath:
    // https://doc.sagemath.org/html/en/reference/topology/sage/topology/simplicial_complex_examples.html#sage.topology.simplicial_complex_examples.DunceHat
    private static FiniteSimplicialComplex dunceHat() {
        return complex(new int[]{1,3,5},new int[]{2,3,5},new int[]{2,4,5},new int[]{1,2,4},new int[]{1,3,4},new int[]{3,4,8},new int[]{1,2,8},new int[]{1,7,8},new int[]{1,2,7},new int[]{2,3,7},new int[]{3,6,7},new int[]{1,3,6},new int[]{1,5,6},new int[]{4,5,6},new int[]{4,6,8},new int[]{6,7,8},new int[]{2,3,8});
    }
    @Test public void contractibleDunceHatAndBranchesEndingThereReturnMathematicalNo() {
        FiniteSimplicialComplex d=dunceHat(); assertEquals(z(1),d.eulerCharacteristic()); assertEquals(8,d.simplices(0).size()); assertEquals(24,d.simplices(1).size()); assertEquals(17,d.simplices(2).size());
        assertTrue(abs(d).homology(z(1)).group().type().isTrivial()); assertTrue(abs(d).homology(z(2)).group().type().isTrivial());
        assertFalse(SimplicialCollapseSearch.isCollapsible(d)); assertTrue(SimplicialCollapseSearch.collapsesToPoint(d).isEmpty()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialCollapseSearch.collapseToPoint(d));
        FiniteSimplicialComplex withLeaves=d.union(complex(new int[]{1,9},new int[]{1,10},new int[]{1,11}));
        assertFalse(SimplicialCollapseSearch.isCollapsible(withLeaves)); assertEquals(6,SimplicialCollapseSearch.absoluteCollapsesTo(withLeaves,d).size());
    }
    @Test public void emptyPointAndNonNestedInputsDistinguishIdentitiesFromMissingWitnesses() {
        FiniteSimplicialComplex empty=complex(),point=complex(new int[]{0});
        assertEquals(Collections.singletonList(SimplicialCollapseSequence.identity(abs(empty))),SimplicialCollapseSearch.absoluteCollapsesTo(empty,empty));
        assertFalse(SimplicialCollapseSearch.isCollapsible(empty)); assertTrue(SimplicialCollapseSearch.collapsesToPoint(empty).isEmpty()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialCollapseSearch.collapseToPoint(empty));
        assertEquals(Collections.singletonList(SimplicialCollapseSequence.identity(abs(point))),SimplicialCollapseSearch.collapsesToPoint(point));
        assertFalse(SimplicialCollapseSearch.absoluteCanCollapseTo(point,empty)); assertFalse(SimplicialCollapseSearch.absoluteCanCollapseTo(simplex(2),complex(new int[]{8})));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialCollapseSearch.absoluteCollapseTo(point,empty));
        assertTrue(SimplicialCollapseSearch.absoluteCollapsesTo(point,empty).isEmpty());
    }
    @Test public void componentEulerObstructionsAndExtremeLabelsAreHandledBeforeSearch() {
        FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); assertFalse(SimplicialCollapseSearch.isCollapsible(circle)); assertFalse(SimplicialCollapseSearch.isCollapsible(points(0,2)));
        RelativeSimplicialComplex source=new RelativeSimplicialComplex(simplex(3),complex(new int[]{0},new int[]{1})),target=new RelativeSimplicialComplex(complex(new int[]{0,2}),complex(new int[]{0}));
        assertEquals(source.ambient().eulerCharacteristic(),target.ambient().eulerCharacteristic()); assertFalse(SimplicialCollapseSearch.canCollapseTo(source,target));
        FiniteSimplicialComplex a=complex(new int[]{Integer.MIN_VALUE,0,Integer.MAX_VALUE}),b=complex(new int[]{Integer.MAX_VALUE,Integer.MIN_VALUE,0});
        assertEquals(SimplicialCollapseSearch.collapsesToPoint(a),SimplicialCollapseSearch.collapsesToPoint(b));
        assertEquals(abs(complex(new int[]{Integer.MIN_VALUE})),SimplicialCollapseSearch.absoluteCollapseTo(a,complex(new int[]{Integer.MIN_VALUE})).target());
    }
    private static FiniteSimplicialComplex star(int leaves) { int[][] edges=new int[leaves][2]; for(int i=0;i<leaves;i++) edges[i]=new int[]{0,i+1}; return complex(edges); }
    private static FiniteSimplicialComplex interval(int edges) { int[][] facets=new int[edges][2]; for(int i=0;i<edges;i++) facets[i]=new int[]{i,i+1}; return complex(facets); }
    @Test public void enumerationLimitFailsAtomicallyWhileFirstWitnessesRemainAvailable() {
        FiniteSimplicialComplex small=star(6),large=star(7),point=complex(new int[]{0}); assertEquals(720,SimplicialCollapseSearch.absoluteCollapsesTo(small,point).size());
        assertTrue(SimplicialCollapseSearch.absoluteCanCollapseTo(large,point)); assertEquals(z(7),SimplicialCollapseSearch.absoluteCollapseTo(large,point).stepCount());
        MathFailure limit=assertThrows(MathFailure.class,() -> SimplicialCollapseSearch.absoluteCollapsesTo(large,point)); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,limit.kind()); assertTrue(limit.getMessage().contains("1024"));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialCollapseSearch.collapsesToPoint(simplex(4)));
    }
    @Test public void stepAndWorkLimitsNeverBecomeFalseOrMissingWitnesses() {
        assertEquals(z(256),SimplicialCollapseSearch.absoluteCollapseTo(interval(256),complex(new int[]{256})).stepCount());
        for(Runnable op : Arrays.<Runnable>asList(() -> SimplicialCollapseSearch.isCollapsible(interval(257)),() -> SimplicialCollapseSearch.collapseToPoint(interval(257)),() -> SimplicialCollapseSearch.collapsesToPoint(interval(257)))) failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,op);
        FiniteSimplicialComplex hard=dunceHat(); for(int v=9;v<19;v++) hard=hard.union(complex(new int[]{1,v}));
        assertFalse(SimplicialCollapseSearch.isCollapsible(hard)); // failed-state caching avoids revisiting all 10! orders
        final FiniteSimplicialComplex input=hard.union(complex(new int[]{1,19}));
        for(Runnable op : Arrays.<Runnable>asList(() -> SimplicialCollapseSearch.isCollapsible(input),() -> SimplicialCollapseSearch.collapseToPoint(input),() -> SimplicialCollapseSearch.collapsesToPoint(input))) {
            MathFailure exhausted=assertThrows(MathFailure.class,op::run); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,exhausted.kind()); assertTrue(exhausted.getMessage().contains("5000000"));
        }
    }
    @Test public void geometricSearchDoesNotRequireSmallHomologyBases() {
        FiniteSimplicialComplex source=points(0,4095).union(simplex(2)),target=points(1,4095); SimplicialCollapseSequence s=SimplicialCollapseSearch.absoluteCollapseTo(source,target);
        assertEquals(z(1),s.stepCount()); assertEquals(abs(target),s.target()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> s.chainMatrix(z(0)));
        SimplicialCollapseSequence filtered=SimplicialCollapseSearch.collapseTo(new RelativeSimplicialComplex(source,points(2,4095)),new RelativeSimplicialComplex(target,points(2,4095)));
        assertEquals(new IntegerMatrix(new BigInteger[][]{{z(1),z(1)}}),filtered.chainMatrix(z(0))); inverseMaps(filtered,0);
        assertEquals(z(0),SimplicialCollapseSearch.absoluteCollapseTo(points(0,4096),points(0,4096)).stepCount());
    }
    @Test public void searchedWitnessesRetainIntegralTorsionAndTypedFillings() {
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialCollapseSequence s=SimplicialCollapseSearch.absoluteCollapseTo(rp2.union(complex(new int[]{0,1,6},new int[]{1,6,7})),rp2);
        assertEquals(z(4),s.stepCount()); assertEquals(Collections.singletonList(z(2)),s.homologyMap(z(1)).target().type().invariantFactors());
        for(int k=0;k<3;k++) { identities(s,k); inverseMaps(s,k); }
        for(RelativeSimplicialChain cycle : RelativeSimplicialChain.zero(s.source(),z(1)).cycleGenerators()) assertEquals(cycle.subtract(s.onChain(cycle).pushforward(s.inclusion())),s.homotopyOnChain(cycle).boundary());
    }
    @Test public void nativeDecisionWitnessAndFlatSearchFlowsSerializeAndCollectRepeatedly() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex triangle=simplex(3),point=complex(new int[]{0});
        IAlgebraFlow<Boolean> decisions=math.flow(math.complexes,Arrays.asList(triangle,dunceHat())).<Boolean>performAlgebraTransfer("CollapseSequence.is-collapsible");
        IAlgebraFlow<BigInteger> pointCounts=math.flow(math.complexes,Collections.singletonList(triangle)).<SimplicialCollapseSequence>performFlatAlgebraTransfer("CollapseSequence.collapses-to-point").<BigInteger>performAlgebraTransfer("step-count");
        IAlgebraFlow<BigInteger> relativeCounts=math.flow(math.relativeComplexes,Collections.singletonList(abs(triangle))).<SimplicialCollapseSequence>performFlatCustomResultOperation("CollapseSequence.collapses-to",abs(point)).<BigInteger>performAlgebraTransfer("step-count");
        IAlgebraFlow<Boolean> maps=math.flow(math.complexes,Collections.singletonList(triangle)).<SimplicialCollapseSequence>performCustomResultOperation("CollapseSequence.absolute-collapse-to",point)
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism");
        for(IAlgebraFlow<?> original : Arrays.asList(decisions,pointCounts,relativeCounts,maps)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Arrays.asList("true","false"),decisions.collect()); assertEquals(Collections.nCopies(12,"3"),pointCounts.collect()); assertEquals(Collections.nCopies(4,"3"),relativeCounts.collect()); assertEquals(Arrays.asList("true","true"),maps.collect());
        assertSame(math.collapseSequences.algebra(),math.relativeComplexes.algebra().buildAlgebraItem(abs(triangle)).performCustomResultOperation("CollapseSequence.collapse-to",abs(point)).getAlgebra());
    }
}
