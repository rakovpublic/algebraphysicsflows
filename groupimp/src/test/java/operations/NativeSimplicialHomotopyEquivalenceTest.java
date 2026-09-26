package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.*;
import mathematics.linear.*;
import mathematics.structures.*;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialHomotopyEquivalenceTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex simplex(int count) { int[] labels=new int[count]; for(int i=0;i<count;i++) labels[i]=i; return complex(labels); }
    private static FiniteSimplicialComplex line(int edges) {
        if(edges==0) return simplex(1); int[][] facets=new int[edges][2]; for(int i=0;i<edges;i++) facets[i]=new int[]{i,i+1}; return complex(facets);
    }
    private static FiniteSimplicialMap map(FiniteSimplicialComplex source,FiniteSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> values=new TreeMap<>(); int i=0; for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source).members()) values.put(vertex,z(images[i++])); return new FiniteSimplicialMap(source,target,values);
    }
    private static RelativeSimplicialMap absolute(FiniteSimplicialMap map) { return RelativeSimplicialMap.absolute(map); }
    private static SimplicialHomotopyPath stationary(RelativeSimplicialComplex pair) { return SimplicialHomotopyPath.stationary(RelativeSimplicialMap.identity(pair)); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void inverseMaps(SimplicialHomotopyEquivalence e,int degree) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(e.homologyMaps(z(degree)),e.cohomologyMaps(z(degree)))) {
            AbelianGroupHomomorphism f=maps.get(0),g=maps.get(1); assertTrue(f.isIsomorphism()); assertTrue(g.isIsomorphism());
            assertEquals(AbelianGroupHomomorphism.identity(f.source()),g.compose(f)); assertEquals(AbelianGroupHomomorphism.identity(f.target()),f.compose(g)); assertEquals(f.inverse(),g);
        }
        assertEquals(e.forward().homologyMap(z(degree)),e.forwardHomologyMap(z(degree)));
        assertEquals(RelativeSimplicialCochain.cohomologyMap(e.forward(),z(degree)),e.forwardCohomologyMap(z(degree)));
    }
    private static SimplicialHomotopyEquivalence contraction(int edges,boolean relative) {
        FiniteSimplicialComplex x=line(edges),point=simplex(1),a=relative?point:complex(); RelativeSimplicialComplex source=new RelativeSimplicialComplex(x,a),target=new RelativeSimplicialComplex(point,a);
        RelativeSimplicialMap f=new RelativeSimplicialMap(source,target,map(x,point,new int[edges+1])),g=RelativeSimplicialMap.inclusion(target,source);
        List<RelativeSimplicialMap> stages=new ArrayList<>(); for(int cutoff=edges;cutoff>=0;cutoff--) { int[] images=new int[edges+1]; for(int v=0;v<=edges;v++) images[v]=Math.min(v,cutoff); stages.add(new RelativeSimplicialMap(source,source,map(x,x,images))); }
        return new SimplicialHomotopyEquivalence(f,g,new SimplicialHomotopyPath(stages),stationary(target));
    }
    private static SimplicialHomotopyEquivalence retract(RelativeSimplicialComplex source,RelativeSimplicialComplex target,int... images) {
        RelativeSimplicialMap f=new RelativeSimplicialMap(source,target,map(source.ambient(),target.ambient(),images)),g=RelativeSimplicialMap.inclusion(target,source);
        return new SimplicialHomotopyEquivalence(f,g,new SimplicialHomotopyPath(Arrays.asList(RelativeSimplicialMap.identity(source),g.compose(f))),stationary(target));
    }
    private static SimplicialHomotopyEquivalence removeLeaf(int edges) {
        int[] images=new int[edges+1]; for(int i=0;i<=edges;i++) images[i]=Math.min(i,edges-1);
        return retract(RelativeSimplicialComplex.absolute(line(edges)),RelativeSimplicialComplex.absolute(line(edges-1)),images);
    }
    @Test public void absoluteAndBasedIntervalsThroughEightEdgesHaveCheckedHomotopyInverses() {
        for(int edges=1;edges<=8;edges++) for(boolean relative : new boolean[]{false,true}) {
            SimplicialHomotopyEquivalence e=contraction(edges,relative); assertFalse(e.forward().isIsomorphism());
            assertEquals(z(edges),e.sourceHomotopy().stepCount()); assertEquals(z(0),e.targetHomotopy().stepCount());
            if(edges>1) assertFalse(e.sourceHomotopy().from().contiguous(e.sourceHomotopy().to()));
            for(int k=0;k<=2;k++) inverseMaps(e,k);
            assertEquals(relative?z(0):z(1),e.source().homology(z(0)).type().freeRank());
        }
    }
    @Test public void circleWithAnAttachedEdgeRetainsTheIntegralGeneratorAndRelativePairs() {
        FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}),x=circle.union(complex(new int[]{2,3}));
        SimplicialHomotopyEquivalence absolute=retract(RelativeSimplicialComplex.absolute(x),RelativeSimplicialComplex.absolute(circle),0,1,2,2);
        assertEquals(z(1),absolute.source().homology(z(1)).type().freeRank()); for(int k=0;k<=2;k++) inverseMaps(absolute,k);
        RelativeSimplicialComplex source=new RelativeSimplicialComplex(x,complex(new int[]{0},new int[]{2,3})),target=new RelativeSimplicialComplex(circle,complex(new int[]{0},new int[]{2}));
        SimplicialHomotopyEquivalence relative=retract(source,target,0,1,2,2); assertEquals(z(2),relative.source().homology(z(1)).type().freeRank());
        for(int k=0;k<=2;k++) inverseMaps(relative,k);
    }
    @Test public void projectivePlaneRetractionPreservesIntegralHomologyAndCohomologyTorsion() {
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialHomotopyEquivalence e=retract(RelativeSimplicialComplex.absolute(rp2.union(complex(new int[]{5,6}))),RelativeSimplicialComplex.absolute(rp2),0,1,2,3,4,5,5);
        assertEquals(Collections.singletonList(z(2)),e.forwardHomologyMap(z(1)).source().type().invariantFactors());
        assertEquals(Collections.singletonList(z(2)),e.forwardCohomologyMap(z(2)).source().type().invariantFactors());
        for(int k=0;k<=3;k++) inverseMaps(e,k);
    }
    @Test public void compositionTransportsBothWitnessesInTheRightOrderAndIsAssociative() {
        SimplicialHomotopyEquivalence a=removeLeaf(4),b=removeLeaf(3),c=removeLeaf(2),ab=b.compose(a),abc=c.compose(ab);
        assertEquals(c.compose(b).compose(a),abc); assertEquals(b.forward().compose(a.forward()),ab.forward()); assertEquals(a.backward().compose(b.backward()),ab.backward());
        assertEquals(a.sourceHomotopy().then(b.sourceHomotopy().precompose(a.forward()).postcompose(a.backward())),ab.sourceHomotopy());
        assertEquals(b.targetHomotopy().then(a.targetHomotopy().precompose(b.backward()).postcompose(b.forward())),ab.targetHomotopy());
        assertEquals(z(3),abc.sourceHomotopy().stepCount());
        assertEquals(abc,abc.compose(SimplicialHomotopyEquivalence.identity(abc.source()))); assertEquals(abc,SimplicialHomotopyEquivalence.identity(abc.target()).compose(abc));
        for(int k=0;k<=1;k++) inverseMaps(abc,k);
    }
    @Test public void inverseSwapsSuppliedWitnessesWithoutClaimingStrictSimplicialInverses() {
        SimplicialHomotopyEquivalence e=contraction(3,false),inverse=e.inverse(); assertEquals(e,inverse.inverse()); assertEquals(e.hashCode(),inverse.inverse().hashCode());
        assertEquals(e.targetHomotopy(),inverse.sourceHomotopy()); assertEquals(e.sourceHomotopy(),inverse.targetHomotopy());
        assertEquals(e.backward(),inverse.forward()); assertEquals(e.forward(),inverse.backward());
        assertNotEquals(SimplicialHomotopyEquivalence.identity(e.source()),inverse.compose(e)); inverseMaps(inverse,0);
    }
    @Test public void allCircleAutomorphismsGiveStrictWitnessesIncludingOrientationReversal() {
        FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); int checked=0;
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) if(a!=b && a!=c && b!=c) {
            SimplicialHomotopyEquivalence e=SimplicialHomotopyEquivalence.fromIsomorphism(absolute(map(circle,circle,a,b,c))); checked++;
            int parity=((a>b?1:0)+(a>c?1:0)+(b>c?1:0))%2==0?1:-1;
            assertEquals(z(parity),e.forwardHomologyMap(z(1)).smithMatrix().get(0,0)); assertEquals(z(0),e.sourceHomotopy().stepCount()); assertEquals(z(0),e.targetHomotopy().stepCount()); inverseMaps(e,1);
        }
        assertEquals(6,checked); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialHomotopyEquivalence.fromIsomorphism(contraction(1,false).forward()));
    }
    @Test public void bothEndpointEquationsDirectionsAndFullPairsAreRequired() {
        SimplicialHomotopyEquivalence e=contraction(2,false);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopyEquivalence(e.forward(),e.backward(),stationary(e.source()),e.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopyEquivalence(e.forward(),e.backward(),e.sourceHomotopy().reverse(),e.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopyEquivalence(e.forward(),e.backward(),e.sourceHomotopy(),e.sourceHomotopy()));
        // Swapping directions makes the nontrivial witness the target condition, which must still be checked.
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopyEquivalence(e.backward(),e.forward(),e.targetHomotopy(),stationary(e.source())));
        RelativeSimplicialMap wrongInverse=absolute(map(e.target().ambient(),e.source().ambient(),1));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopyEquivalence(e.forward(),wrongInverse,e.sourceHomotopy(),e.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopyEquivalence(e.forward(),RelativeSimplicialMap.diagonal(e.backward().ambientMap()),e.sourceHomotopy(),e.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.compose(e));
    }
    @Test public void equalityRetainsNonzeroClosedWitnessesEvenForIdentityMaps() {
        FiniteSimplicialComplex triangle=simplex(3); RelativeSimplicialMap id=absolute(FiniteSimplicialMap.identity(triangle)),a=absolute(map(triangle,triangle,0,0,0)),b=absolute(map(triangle,triangle,1,1,1)),c=absolute(map(triangle,triangle,2,2,2));
        SimplicialHomotopyPath loop=new SimplicialHomotopyPath(Arrays.asList(id,a,b,c,a,id)); SimplicialHomotopyEquivalence e=new SimplicialHomotopyEquivalence(id,id,loop,SimplicialHomotopyPath.stationary(id));
        assertNotEquals(IntegerMatrix.zero(3,3),e.sourceHomotopy().chainMatrix(z(0))); assertNotEquals(SimplicialHomotopyEquivalence.identity(id.source()),e); inverseMaps(e,0);
    }
    @Test public void witnessActionsFillForwardBackwardCycleAndCocycleDifferences() {
        SimplicialHomotopyEquivalence e=contraction(3,false); RelativeSimplicialChain cycle=new RelativeSimplicialChain(e.source(),z(0),new IntegerVector(z(0),z(0),z(0),z(2)));
        assertEquals(cycle.pushforward(e.forward()).pushforward(e.backward()).subtract(cycle),e.sourceHomotopy().onChain(cycle).boundary());
        RelativeSimplicialCochain cocycle=new RelativeSimplicialCochain(e.source(),z(1),new IntegerVector(z(2),z(3),z(5)));
        assertEquals(cocycle.pullback(e.backward()).pullback(e.forward()).subtract(cocycle),e.sourceHomotopy().onCochain(cocycle).coboundary());
    }
    @Test public void emptyHugeNegativeDegreesAndExtremeLabelsKeepActualPresentations() {
        SimplicialHomotopyEquivalence empty=SimplicialHomotopyEquivalence.identity(RelativeSimplicialComplex.absolute(complex())); inverseMaps(empty,0);
        assertTrue(empty.forwardHomologyMap(BigInteger.TEN.pow(100)).isIsomorphism()); assertTrue(empty.forwardCohomologyMap(BigInteger.TEN.pow(100)).isIsomorphism());
        for(Runnable action : Arrays.<Runnable>asList(() -> empty.forwardHomologyMap(z(-1)),() -> empty.backwardHomologyMap(z(-1)),() -> empty.homologyMaps(z(-1)),() -> empty.forwardCohomologyMap(z(-1)),() -> empty.backwardCohomologyMap(z(-1)),() -> empty.cohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        FiniteSimplicialComplex source=complex(new int[]{Integer.MIN_VALUE}),target=complex(new int[]{Integer.MAX_VALUE}); SimplicialHomotopyEquivalence relabel=SimplicialHomotopyEquivalence.fromIsomorphism(absolute(map(source,target,Integer.MAX_VALUE))); inverseMaps(relabel,0);
        assertThrows(UnsupportedOperationException.class,() -> relabel.homologyMaps(z(0)).clear()); assertThrows(UnsupportedOperationException.class,() -> relabel.cohomologyMaps(z(0)).clear());
    }
    @Test public void filteredBasesAndWholeTwoMapBudgetsAreEnforced() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i));
        SimplicialHomotopyEquivalence filtered=SimplicialHomotopyEquivalence.identity(new RelativeSimplicialComplex(new FiniteSimplicialComplex(points),new FiniteSimplicialComplex(points.subList(0,299)))); inverseMaps(filtered,0);
        SimplicialHomotopyEquivalence e=SimplicialHomotopyEquivalence.identity(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(points.subList(0,70))));
        assertNotNull(e.forwardHomologyMap(z(0))); assertNotNull(e.backwardHomologyMap(z(0))); assertNotNull(e.forwardCohomologyMap(z(0))); assertNotNull(e.backwardCohomologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.homologyMaps(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.cohomologyMaps(z(0)));
    }
    @Test public void compositionSharesWorkAcrossTransportConcatenationAndEndpointValidation() {
        RelativeSimplicialComplex pair=RelativeSimplicialComplex.diagonal(simplex(10)); RelativeSimplicialMap id=RelativeSimplicialMap.identity(pair); SimplicialHomotopyPath path=new SimplicialHomotopyPath(Collections.nCopies(80,id));
        assertNotNull(path.precompose(id)); assertNotNull(path.postcompose(id)); SimplicialHomotopyEquivalence e=new SimplicialHomotopyEquivalence(id,id,path,SimplicialHomotopyPath.stationary(id));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.compose(SimplicialHomotopyEquivalence.identity(pair)));
        RelativeSimplicialMap point=absolute(FiniteSimplicialMap.identity(simplex(1))); SimplicialHomotopyPath many=new SimplicialHomotopyPath(Collections.nCopies(200,point)); SimplicialHomotopyEquivalence longWitness=new SimplicialHomotopyEquivalence(point,point,many,SimplicialHomotopyPath.stationary(point));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> longWitness.compose(longWitness));
    }
    @Test public void originalAlgebrasValidateProductCarriersAndSerializeFlatInverseMapFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialHomotopyEquivalence e=contraction(2,false); Pair<SimplicialHomotopyPath,SimplicialHomotopyPath> witnesses=new Pair<>(e.sourceHomotopy(),e.targetHomotopy());
        IAlgebraItem<Pair<RelativeSimplicialMap,RelativeSimplicialMap>> maps=math.relativeMaps.algebra().buildAlgebraItem(e.forward()).performCustomResultOperation("HomotopyEquivalence.maps",e.backward());
        assertSame(math.homotopyEquivalences.mapPairs,maps.getAlgebra()); IAlgebraItem<SimplicialHomotopyEquivalence> item=maps.performUnsafeOperation("HomotopyEquivalence.from-maps",witnesses);
        assertSame(math.homotopyEquivalences.algebra(),item.getAlgebra()); assertEquals(e,item.perform().getResult());
        assertSame(math.homotopyPaths.algebra(),item.performAlgebraTransfer("source-homotopy").getAlgebra()); assertSame(math.relativeMaps.algebra(),item.performAlgebraTransfer("backward").getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.homotopyEquivalences.mapPairs).validate(new Pair<>(e.forward(),"wrong")));
        assertFalse(((algebra.imp.Algebra)math.homotopyEquivalences.witnessPairs).validate(new Pair<>(e.sourceHomotopy(),e.forward())));
        for(IAlgebraItem<AbelianGroupHomomorphism> map : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("homology-maps",z(0))) assertSame(math.abelianHomomorphisms.algebra(),map.getAlgebra());
        IAlgebraFlow<Boolean> flow=math.flow(math.relativeMaps,Collections.singletonList(e.forward())).<Pair<RelativeSimplicialMap,RelativeSimplicialMap>>performCustomResultOperation("HomotopyEquivalence.maps",e.backward())
                .<SimplicialHomotopyEquivalence,Pair<SimplicialHomotopyPath,SimplicialHomotopyPath>>performAlgebraUnsafe("HomotopyEquivalence.from-maps",witnesses)
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("cohomology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("true","true"),restored.collect()); assertEquals(restored.collect(),restored.collect());
    }
}
