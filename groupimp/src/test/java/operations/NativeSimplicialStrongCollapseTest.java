package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.*;
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialStrongCollapseTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex simplex(int first,int count) { int[] vertices=new int[count]; for(int i=0;i<count;i++) vertices[i]=first+i; return complex(vertices); }
    private static FiniteSimplicialComplex line(int edges) { int[][] facets=new int[edges][2]; for(int i=0;i<edges;i++) facets[i]=new int[]{i,i+1}; return complex(facets); }
    private static RelativeSimplicialComplex absolute(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void inverseMaps(SimplicialHomotopyEquivalence e,int degree) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(e.homologyMaps(z(degree)),e.cohomologyMaps(z(degree)))) {
            AbelianGroupHomomorphism f=maps.get(0),g=maps.get(1);
            assertEquals(AbelianGroupHomomorphism.identity(f.source()),g.compose(f)); assertEquals(AbelianGroupHomomorphism.identity(f.target()),f.compose(g));
        }
    }
    private static void retractionWitness(SimplicialHomotopyEquivalence e) {
        assertEquals(RelativeSimplicialMap.identity(e.target()),e.forward().compose(e.backward()));
        assertEquals(z(0),e.targetHomotopy().stepCount()); assertEquals(RelativeSimplicialMap.identity(e.source()),e.sourceHomotopy().from());
        assertEquals(e.backward().compose(e.forward()),e.sourceHomotopy().to());
        for(BigInteger v : FiniteSimplicialMap.vertexSet(e.target().ambient()).members()) {
            assertEquals(v,e.backward().ambientMap().mapVertex(v));
            for(RelativeSimplicialMap stage : e.sourceHomotopy().stages()) assertEquals(v,stage.ambientMap().mapVertex(v));
        }
    }
    private static boolean closed(int mask) {
        for(int face=1;face<16;face++) if((mask&(1<<(face-1)))!=0)
            for(int sub=(face-1)&face;sub>0;sub=(sub-1)&face) if((mask&(1<<(sub-1)))==0) return false;
        return true;
    }
    private static FiniteSimplicialComplex fromMask(int mask) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int face=1;face<16;face++) if((mask&(1<<(face-1)))!=0) { List<Integer> vertices=new ArrayList<>(); for(int v=0;v<4;v++) if((face&(1<<v))!=0) vertices.add(v); faces.add(new FiniteSet<>(vertices)); }
        return new FiniteSimplicialComplex(faces);
    }
    // Independent bitmask oracle: intersect all maximal faces incident to the vertex.
    private static List<BigInteger> maximalFaceDominators(int mask,int vertex) {
        int intersection=15;
        for(int face=1;face<16;face++) if((mask&(1<<(face-1)))!=0 && (face&(1<<vertex))!=0) {
            boolean maximal=true;
            for(int larger=face+1;larger<16;larger++) if((larger&face)==face && (mask&(1<<(larger-1)))!=0) maximal=false;
            if(maximal) intersection&=face;
        }
        List<BigInteger> result=new ArrayList<>(); for(int w=0;w<4;w++) if(w!=vertex && (intersection&(1<<w))!=0) result.add(z(w)); return result;
    }
    @Test public void all167FourLabelComplexesAgreeWithIndependentMaximalFaceOracle() {
        int checked=0;
        for(int mask=0;mask<(1<<15);mask++) if(closed(mask)) {
            checked++; RelativeSimplicialComplex pair=absolute(fromMask(mask)); List<BigInteger> expected=new ArrayList<>();
            for(int vertex=0;vertex<4;vertex++) if((mask&(1<<((1<<vertex)-1)))!=0) {
                List<BigInteger> dominators=maximalFaceDominators(mask,vertex); assertEquals(dominators,SimplicialStrongCollapse.dominators(pair,z(vertex)));
                if(!dominators.isEmpty()) expected.add(z(vertex));
                for(BigInteger survivor : dominators) retractionWitness(SimplicialStrongCollapse.collapseVertex(pair,new Pair<>(z(vertex),survivor)));
            }
            assertEquals(expected,SimplicialStrongCollapse.dominatedVertices(pair)); assertEquals(expected.isEmpty(),SimplicialStrongCollapse.isStrongCore(pair));
            SimplicialHomotopyEquivalence core=SimplicialStrongCollapse.strongCore(pair); retractionWitness(core); assertTrue(SimplicialStrongCollapse.isStrongCore(core.target()));
            assertEquals(SimplicialHomotopyEquivalence.identity(core.target()),SimplicialStrongCollapse.strongCore(core.target())); inverseMaps(core,0); inverseMaps(core,1);
        }
        assertEquals(167,checked);
    }
    @Test public void all1024GraphAndVertexSubcomplexPairsMatchLeafDeletionOracle() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int mask=0;mask<64;mask++) for(int subset=0;subset<16;subset++) {
            List<FiniteSet<Integer>> faces=new ArrayList<>(),points=new ArrayList<>(); int[] degrees=new int[4],neighbor=new int[4];
            for(int v=0;v<4;v++) { faces.add(FiniteSet.of(v)); if((subset&(1<<v))!=0) points.add(FiniteSet.of(v)); }
            for(int e=0;e<6;e++) if((mask&(1<<e))!=0) { int a=edges[e][0],b=edges[e][1]; faces.add(FiniteSet.of(a,b)); degrees[a]++; degrees[b]++; neighbor[a]=b; neighbor[b]=a; }
            RelativeSimplicialComplex pair=new RelativeSimplicialComplex(new FiniteSimplicialComplex(faces),new FiniteSimplicialComplex(points));
            for(int v=0;v<4;v++) assertEquals(degrees[v]==1 && (subset&(1<<v))==0?Collections.singletonList(z(neighbor[v])):Collections.emptyList(),SimplicialStrongCollapse.dominators(pair,z(v)));
            SimplicialHomotopyEquivalence core=SimplicialStrongCollapse.strongCore(pair); retractionWitness(core); assertEquals(pair.subcomplex(),core.target().subcomplex()); inverseMaps(core,1);
        }
    }
    @Test public void intervalsReduceDeterministicallyAndBasedIntervalsKeepTheirBasepoint() {
        for(int edges=1;edges<=12;edges++) {
            SimplicialHomotopyEquivalence ordinary=SimplicialStrongCollapse.strongCoreAbsolute(line(edges));
            assertEquals(simplex(edges,1),ordinary.target().ambient()); assertEquals(z(edges),ordinary.sourceHomotopy().stepCount()); retractionWitness(ordinary);
            RelativeSimplicialComplex based=new RelativeSimplicialComplex(line(edges),simplex(0,1)); SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongCore(based);
            assertEquals(RelativeSimplicialComplex.diagonal(simplex(0,1)),e.target()); retractionWitness(e); inverseMaps(e,0);
        }
    }
    @Test public void relativeDominationChecksTheSubcomplexSeparately() {
        FiniteSimplicialComplex triangle=simplex(0,3),boundary=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2});
        assertEquals(Arrays.asList(z(1),z(2)),SimplicialStrongCollapse.dominators(absolute(triangle),z(0)));
        RelativeSimplicialComplex disk=new RelativeSimplicialComplex(triangle,boundary); assertTrue(SimplicialStrongCollapse.isStrongCore(disk));
        assertEquals(SimplicialHomotopyEquivalence.identity(disk),SimplicialStrongCollapse.strongCore(disk));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialStrongCollapse.collapseVertex(disk,new Pair<>(z(0),z(1))));
        RelativeSimplicialComplex edgePair=new RelativeSimplicialComplex(triangle,complex(new int[]{0,1}));
        assertEquals(Collections.singletonList(z(1)),SimplicialStrongCollapse.dominators(edgePair,z(0))); retractionWitness(SimplicialStrongCollapse.collapseVertex(edgePair,new Pair<>(z(0),z(1))));
        assertEquals(Arrays.asList(z(0),z(1)),SimplicialStrongCollapse.dominators(edgePair,z(2)));
    }
    @Test public void projectivePlaneWithAttachedEdgeKeepsTorsionAndItsCore() {
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        assertTrue(SimplicialStrongCollapse.isStrongCore(absolute(rp2))); SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongCoreAbsolute(rp2.union(complex(new int[]{5,6})));
        assertEquals(rp2,e.target().ambient()); assertEquals(Collections.singletonList(z(2)),e.forwardHomologyMap(z(1)).source().type().invariantFactors());
        assertEquals(Collections.singletonList(z(2)),e.forwardCohomologyMap(z(2)).source().type().invariantFactors()); inverseMaps(e,1); inverseMaps(e,2);
    }
    @Test public void emptyIsolatedAndDisconnectedComplexesRetainComponents() {
        RelativeSimplicialComplex empty=absolute(complex()); assertEquals(SimplicialHomotopyEquivalence.identity(empty),SimplicialStrongCollapse.strongCore(empty));
        assertTrue(SimplicialStrongCollapse.isStrongCore(empty)); assertTrue(SimplicialStrongCollapse.dominatedVertices(empty).isEmpty());
        FiniteSimplicialComplex disconnected=complex(new int[]{0,1},new int[]{2,3},new int[]{4}); SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongCoreAbsolute(disconnected);
        assertEquals(complex(new int[]{1},new int[]{3},new int[]{4}),e.target().ambient()); assertEquals(z(3),e.target().homology(z(0)).type().freeRank()); inverseMaps(e,0);
    }
    @Test public void invalidVerticesAreUndefinedAndExtremeLabelsRemainExact() {
        RelativeSimplicialComplex path=absolute(line(2));
        for(Pair<BigInteger,BigInteger> move : Arrays.asList(new Pair<>(z(0),z(0)),new Pair<>(z(1),z(0)),new Pair<>(z(0),z(2)),new Pair<>(z(3),z(0)),new Pair<>(z(0),BigInteger.TEN.pow(100))))
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialStrongCollapse.collapseVertex(path,move));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialStrongCollapse.dominators(path,z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialStrongCollapse.dominators(path,BigInteger.TEN.pow(100)));
        SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongCoreAbsolute(complex(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE}));
        assertEquals(simplex(Integer.MAX_VALUE,1),e.target().ambient()); assertEquals(z(Integer.MAX_VALUE),e.forward().ambientMap().mapVertex(z(Integer.MIN_VALUE))); retractionWitness(e);
    }
    @Test public void faceInsertionOrderDoesNotChangeMovesOrWitnesses() {
        FiniteSimplicialComplex a=complex(new int[]{0,1,2},new int[]{1,2,3},new int[]{2,3,4}),b=complex(new int[]{4,3,2},new int[]{3,2,1},new int[]{2,1,0});
        assertEquals(a,b); assertEquals(SimplicialStrongCollapse.strongCoreAbsolute(a),SimplicialStrongCollapse.strongCoreAbsolute(b));
        for(int vertex=0;vertex<5;vertex++) assertEquals(SimplicialStrongCollapse.dominators(absolute(a),z(vertex)),SimplicialStrongCollapse.dominators(absolute(b),z(vertex)));
        assertThrows(UnsupportedOperationException.class,() -> SimplicialStrongCollapse.dominatedVertices(absolute(a)).clear());
        assertThrows(UnsupportedOperationException.class,() -> SimplicialStrongCollapse.dominators(absolute(a),z(0)).clear());
    }
    @Test public void generatedWitnessesGiveTypedIntegralFillings() {
        SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongCoreAbsolute(line(3));
        RelativeSimplicialChain cycle=new RelativeSimplicialChain(e.source(),z(0),new mathematics.linear.IntegerVector(z(2),z(0),z(0),z(0)));
        assertEquals(cycle.pushforward(e.forward()).pushforward(e.backward()).subtract(cycle),e.sourceHomotopy().onChain(cycle).boundary());
        RelativeSimplicialCochain cocycle=new RelativeSimplicialCochain(e.source(),z(1),new mathematics.linear.IntegerVector(z(2),z(3),z(5)));
        assertEquals(cocycle.pullback(e.backward()).pullback(e.forward()).subtract(cocycle),e.sourceHomotopy().onCochain(cocycle).coboundary());
    }
    @Test public void stageAndWholeReductionWorkLimitsNeverReturnPartialCores() {
        int[][] edges=new int[256][2]; for(int i=0;i<256;i++) edges[i]=new int[]{2*i,2*i+1};
        MathFailure tooLong=assertThrows(MathFailure.class,() -> SimplicialStrongCollapse.strongCoreAbsolute(complex(edges))); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,tooLong.kind()); assertTrue(tooLong.getMessage().contains("256 stages"));
        FiniteSimplicialComplex first=simplex(1000,11),second=line(180);
        assertNotNull(SimplicialStrongCollapse.strongCoreAbsolute(first)); assertNotNull(SimplicialStrongCollapse.strongCoreAbsolute(second));
        MathFailure tooMuch=assertThrows(MathFailure.class,() -> SimplicialStrongCollapse.strongCoreAbsolute(first.union(second))); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,tooMuch.kind()); assertTrue(tooMuch.getMessage().contains("5000000"));
    }
    @Test public void geometricReductionDoesNotRequireSmallHomologyBases() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<300;i++) facets.add(FiniteSet.of(i)); facets.add(FiniteSet.of(300,301));
        SimplicialHomotopyEquivalence e=SimplicialStrongCollapse.strongCoreAbsolute(new FiniteSimplicialComplex(facets));
        assertEquals(301,e.target().ambient().simplices(0).size()); retractionWitness(e);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.forwardHomologyMap(z(0)));
    }
    @Test public void nativeFlatSecondResultAndSerializedCoreFlowsUseActualWrappers() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=absolute(simplex(0,3)); IAlgebraItem<RelativeSimplicialComplex> item=math.relativeComplexes.algebra().buildAlgebraItem(pair);
        List<IAlgebraItem<BigInteger>> dominators=item.performLeftProjectionFlatOperation("HomotopyEquivalence.dominators",z(0)); assertEquals(2,dominators.size());
        for(IAlgebraItem<BigInteger> dominator : dominators) assertSame(math.integers.algebra(),dominator.getAlgebra());
        IAlgebraItem<SimplicialHomotopyEquivalence> collapsed=item.performUnsafeOperation("HomotopyEquivalence.collapse-vertex",new Pair<>(z(0),z(1))); assertSame(math.homotopyEquivalences.algebra(),collapsed.getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.mathTool.getAlgebra("StrongCollapse.vertices")).validate(new Pair<>(z(0),"bad")));
        IAlgebraFlow<Boolean> flow=math.flow(math.complexes,Collections.singletonList(line(3)))
                .<SimplicialHomotopyEquivalence>performAlgebraTransfer("HomotopyEquivalence.strong-core-absolute")
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("true","true"),restored.collect()); assertEquals(restored.collect(),restored.collect());
    }
}
