package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.structures.*;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeCohomologicalMayerVietorisTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int[] facet : facets) { List<Integer> values=new ArrayList<>(); for(int vertex : facet) values.add(vertex); faces.add(new FiniteSet<>(values)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static IntegerVector v(long... values) { BigInteger[] entries=new BigInteger[values.length]; for(int i=0;i<values.length;i++) entries[i]=z(values[i]); return new IntegerVector(entries); }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] entries=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { entries[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) entries[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(entries);
    }
    private static SimplicialCover circle() { return new SimplicialCover(complex(new int[]{0,1},new int[]{1,2}),complex(new int[]{0,2})); }
    private static SimplicialCover plane() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        List<FiniteSet<Integer>> facets=new ArrayList<>(plane.simplices(2)); facets.remove(FiniteSet.of(0,1,2));
        return new SimplicialCover(new FiniteSimplicialComplex(facets),complex(new int[]{0,1,2}));
    }
    private static SimplicialCoverMap map(SimplicialCover source,SimplicialCover target,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0;
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source.union()).members()) vertices.put(vertex,z(images[i++]));
        return new SimplicialCoverMap(source,target,new FiniteSimplicialMap(source.union(),target.union(),vertices));
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void exact(AbelianGroupHomomorphism first,AbelianGroupHomomorphism second) {
        assertEquals(first.target(),second.source()); assertTrue(second.compose(first).isZero());
        for(AbelianGroupElement generator : second.kernelInclusion().generatorImages()) assertTrue(first.hasPreimage(generator));
    }
    private static void exactSegment(SimplicialCover cover,int degree) {
        BigInteger k=z(degree); List<AbelianGroupHomomorphism> maps=cover.longExactCohomologySegment(k);
        assertEquals(Arrays.asList(cover.restrictionCohomologyMap(k),cover.differenceCohomologyMap(k),cover.connectingCohomologyMap(k)),maps);
        exact(maps.get(0),maps.get(1)); exact(maps.get(1),maps.get(2)); exact(maps.get(2),cover.restrictionCohomologyMap(k.add(BigInteger.ONE)));
    }
    private static void cochainExactness(SimplicialCover cover) {
        for(int degree=0;degree<=cover.union().dimension()+1;degree++) {
            BigInteger k=z(degree); IntegerMatrix r=cover.restrictionMatrix(k),d=cover.differenceMatrix(k);
            assertEquals(r.columns(),r.rank()); assertEquals(d.rows(),d.rank()); assertEquals(IntegerMatrix.zero(d.rows(),r.columns()),d.multiply(r));
            for(IntegerVector generator : d.kernelBasis()) assertTrue(r.hasIntegerSolution(generator));
            for(int i=0;i<d.rows();i++) { BigInteger[] e=new BigInteger[d.rows()]; Arrays.fill(e,BigInteger.ZERO); e[i]=BigInteger.ONE; assertTrue(d.hasIntegerSolution(new IntegerVector(e))); }
            IntegerMatrix unionDifferential=cover.union().integralBoundaryMatrix(k.add(BigInteger.ONE)).transpose();
            IntegerMatrix intersectionDifferential=cover.intersection().integralBoundaryMatrix(k.add(BigInteger.ONE)).transpose();
            assertEquals(cover.sumCoboundaryMatrix(k).multiply(r),cover.restrictionMatrix(k.add(BigInteger.ONE)).multiply(unionDifferential));
            assertEquals(intersectionDifferential.multiply(d),cover.differenceMatrix(k.add(BigInteger.ONE)).multiply(cover.sumCoboundaryMatrix(k)));
        }
    }
    private static void naturality(SimplicialCoverMap f,int degree) {
        BigInteger k=z(degree); List<AbelianGroupHomomorphism> source=f.source().longExactCohomologySegment(k),target=f.target().longExactCohomologySegment(k),vertical=f.longExactCohomologyMaps(k);
        assertEquals(Arrays.asList(f.unionCohomologyMap(k),f.sumCohomologyMap(k),f.intersectionCohomologyMap(k),f.unionCohomologyMap(k.add(BigInteger.ONE))),vertical);
        for(int i=0;i<3;i++) assertEquals(source.get(i).compose(vertical.get(i)),vertical.get(i+1).compose(target.get(i)));
        assertEquals(f.source().leftCohomologyProjectionMap(k).compose(f.sumCohomologyMap(k)),f.leftCohomologyMap(k).compose(f.target().leftCohomologyProjectionMap(k)));
        assertEquals(f.sumCohomologyMap(k).compose(f.target().rightCohomologyInclusionMap(k)),f.source().rightCohomologyInclusionMap(k).compose(f.rightCohomologyMap(k)));
        assertEquals(f.source().restrictionMatrix(k).multiply(f.unionMap().chainMatrix(k).transpose()),f.sumCochainMatrix(k).multiply(f.target().restrictionMatrix(k)));
        assertEquals(f.source().differenceMatrix(k).multiply(f.sumCochainMatrix(k)),f.intersectionMap().chainMatrix(k).transpose().multiply(f.target().differenceMatrix(k)));
        assertEquals(f.source().sumCoboundaryMatrix(k).multiply(f.sumCochainMatrix(k)),f.sumCochainMatrix(k.add(BigInteger.ONE)).multiply(f.target().sumCoboundaryMatrix(k)));
    }
    @Test public void circleConnectingCocycleHasTheExpectedOrientedPeriod() {
        SimplicialCover cover=circle(); IntegerVector endpoint=v(1,0),result=cover.connectingCochainMatrix(z(0)).multiply(endpoint);
        assertEquals(v(-1,0,0),result); assertEquals(z(-1),result.dot(v(1,-1,1)));
        SimplicialCochain cocycle=new SimplicialCochain(cover.union(),z(1),result); assertTrue(cocycle.isCocycle()); assertFalse(cocycle.isCoboundary());
        assertEquals(cocycle.classOf(),cover.connectingCohomologyMap(z(0)).apply(cover.intersectionCohomology(z(0)).classOf(endpoint)));
        assertTrue(cover.connectingCohomologyMap(z(0)).isSurjective());
        assertEquals(AbelianGroupType.Z,cover.unionCohomology(z(1)).type()); assertEquals(AbelianGroupType.ZERO,cover.sumCohomology(z(1)).type());
        cochainExactness(cover); exactSegment(cover,0); exactSegment(cover,1);
    }
    @Test public void spheresGiveDegreeRaisingConnectingIsomorphismsThroughDimensionFour() {
        for(int n=1;n<=4;n++) {
            int[] left=new int[n+1]; for(int i=0;i<=n;i++) left[i]=i;
            List<int[]> right=new ArrayList<>(); for(int omit=0;omit<=n;omit++) { int[] face=new int[n+1]; int p=0; for(int i=0;i<=n+1;i++) if(i!=omit) face[p++]=i; right.add(face); }
            SimplicialCover cover=new SimplicialCover(complex(left),complex(right.toArray(new int[0][])));
            assertEquals(AbelianGroupType.Z,cover.unionCohomology(z(n)).type()); assertEquals(AbelianGroupType.ZERO,cover.sumCohomology(z(n)).type());
            if(n>1) assertTrue(cover.connectingCohomologyMap(z(n-1)).isIsomorphism()); else assertTrue(cover.connectingCohomologyMap(z(0)).isSurjective());
            exactSegment(cover,n-1); cochainExactness(cover);
        }
    }
    @Test public void projectivePlaneHasIndexTwoDifferenceAndDegreeTwoTorsion() {
        SimplicialCover cover=plane(); AbelianGroupHomomorphism difference=cover.differenceCohomologyMap(z(1)),connecting=cover.connectingCohomologyMap(z(1));
        assertEquals(AbelianGroupType.ZERO,cover.unionCohomology(z(1)).type()); assertEquals(AbelianGroupType.cyclic(z(2)),cover.unionCohomology(z(2)).type());
        assertEquals(AbelianGroupType.Z,cover.sumCohomology(z(1)).type()); assertEquals(AbelianGroupType.Z,cover.intersectionCohomology(z(1)).type());
        assertTrue(difference.isInjective()); assertEquals(AbelianGroupType.cyclic(z(2)),difference.cokernel().type()); assertTrue(connecting.isSurjective()); assertFalse(connecting.isZero());
        for(int k=0;k<=2;k++) { exactSegment(cover,k); naturality(SimplicialCoverMap.identity(cover),k); naturality(map(cover,cover,0,0,0,0,0,0),k); }
    }
    @Test public void componentCohomologyMapsExhibitDirectSumsIncludingTorsion() {
        SimplicialCover cover=new SimplicialCover(plane().union(),plane().union());
        assertEquals(AbelianGroupType.cyclic(z(2)).directSum(AbelianGroupType.cyclic(z(2))),cover.sumCohomology(z(2)).type());
        for(int k=0;k<=2;k++) {
            AbelianGroupHomomorphism i=cover.leftCohomologyInclusionMap(z(k)),j=cover.rightCohomologyInclusionMap(z(k)),p=cover.leftCohomologyProjectionMap(z(k)),q=cover.rightCohomologyProjectionMap(z(k));
            assertEquals(AbelianGroupHomomorphism.identity(i.source()),p.compose(i)); assertEquals(AbelianGroupHomomorphism.identity(j.source()),q.compose(j));
            assertTrue(p.compose(j).isZero()); assertTrue(q.compose(i).isZero()); assertEquals(AbelianGroupHomomorphism.identity(i.target()),i.compose(p).add(j.compose(q)));
            assertEquals(cover.leftCohomology(z(k)).group(),i.source()); assertEquals(cover.rightCohomology(z(k)).group(),j.source());
        }
    }
    private static FiniteSimplicialComplex graph(int mask) {
        List<int[]> facets=new ArrayList<>(Arrays.asList(new int[]{0},new int[]{1},new int[]{2})); int[][] edges={{0,1},{0,2},{1,2}};
        for(int i=0;i<3;i++) if((mask&(1<<i))!=0) facets.add(edges[i]); return complex(facets.toArray(new int[0][]));
    }
    private static int components(int mask) { return mask==0?3:Integer.bitCount(mask)==1?2:1; }
    @Test public void all64GraphCoversMatchIndependentRanksAndIntegralExactness() {
        for(int a=0;a<8;a++) for(int b=0;b<8;b++) {
            SimplicialCover cover=new SimplicialCover(graph(a),graph(b));
            assertEquals(AbelianGroupType.free(z(components(a)+components(b))),cover.sumCohomology(z(0)).type());
            assertEquals(AbelianGroupType.free(z((a==7?1:0)+(b==7?1:0))),cover.sumCohomology(z(1)).type());
            assertEquals(AbelianGroupType.free(z(components(a|b))),cover.unionCohomology(z(0)).type());
            assertEquals(AbelianGroupType.free(z((a|b)==7?1:0)),cover.unionCohomology(z(1)).type());
            assertEquals(AbelianGroupType.free(z(components(a&b))),cover.intersectionCohomology(z(0)).type());
            exactSegment(cover,0); exactSegment(cover,1); cochainExactness(cover);
        }
    }
    @Test public void all27DiscreteMapsAnd729CompositionsHaveContravariantCoordinatePullbacks() {
        SimplicialCover cover=new SimplicialCover(graph(0),graph(0)); List<SimplicialCoverMap> maps=new ArrayList<>(); List<AbelianGroupHomomorphism> cohomology=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) {
            SimplicialCoverMap f=map(cover,cover,a,b,c); maps.add(f); cohomology.add(f.sumCohomologyMap(z(0)));
            long[][] entries=new long[6][6]; int[] images={a,b,c}; for(int i=0;i<3;i++) { entries[i][images[i]]=1; entries[i+3][images[i]+3]=1; }
            assertEquals(m(entries),f.sumCochainMatrix(z(0))); assertEquals(m(entries),f.sumCohomologyMap(z(0)).smithMatrix()); naturality(f,0);
        }
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) {
            SimplicialCoverMap composition=maps.get(i).compose(maps.get(j));
            assertEquals(maps.get(j).sumCochainMatrix(z(0)).multiply(maps.get(i).sumCochainMatrix(z(0))),composition.sumCochainMatrix(z(0)));
            assertEquals(cohomology.get(j).compose(cohomology.get(i)),composition.sumCohomologyMap(z(0)));
        }
    }
    @Test public void reflectionsAndCollapsesCommuteWithAllThreeCohomologySquares() {
        SimplicialCover cover=circle(); SimplicialCoverMap reflection=map(cover,cover,2,1,0);
        assertEquals(AbelianGroupHomomorphism.identity(cover.unionCohomology(z(1)).group()).scale(z(-1)),reflection.unionCohomologyMap(z(1))); naturality(reflection,0); naturality(reflection,1);
        SimplicialCover sphere=new SimplicialCover(complex(new int[]{0,1,2}),complex(new int[]{0,1,3},new int[]{0,2,3},new int[]{1,2,3}));
        SimplicialCoverMap reverse=map(sphere,sphere,1,0,2,3); assertEquals(AbelianGroupHomomorphism.identity(sphere.unionCohomology(z(2)).group()).scale(z(-1)),reverse.unionCohomologyMap(z(2))); naturality(reverse,1);
        SimplicialCover points=new SimplicialCover(complex(new int[]{0}),complex(new int[]{1})),point=new SimplicialCover(complex(new int[]{7}),complex(new int[]{7}));
        SimplicialCoverMap collapse=map(points,point,7,7); assertEquals(m(new long[]{1},new long[]{1}),collapse.unionCohomologyMap(z(0)).smithMatrix());
        assertEquals(point.sumCohomology(z(0)),collapse.targetSumCohomology(z(0))); assertEquals(points.sumCohomology(z(0)),collapse.sourceSumCohomology(z(0))); naturality(collapse,0);
    }
    @Test public void connectingNaturalityHoldsOnClassesWhenRawMatricesDoNotCommute() {
        SimplicialCover source=circle(),target=new SimplicialCover(source.union(),source.right()); SimplicialCoverMap f=SimplicialCoverMap.inclusion(source,target);
        assertNotEquals(source.connectingCochainMatrix(z(0)).multiply(f.intersectionMap().chainMatrix(z(0)).transpose()),
                f.unionMap().chainMatrix(z(1)).transpose().multiply(target.connectingCochainMatrix(z(0))));
        naturality(f,0); naturality(f,1);
        assertTrue(target.connectingCohomologyMap(z(0)).isZero()); assertFalse(source.connectingCohomologyMap(z(0)).isZero());
    }
    @Test public void swappingPiecesNegatesConnectingClassesAndExcisionAgreesWithRelativeCohomology() {
        for(SimplicialCover cover : Arrays.asList(circle(),plane())) for(int k=0;k<=2;k++) {
            assertEquals(cover.connectingCohomologyMap(z(k)).scale(z(-1)),cover.swap().connectingCohomologyMap(z(k)));
            RelativeSimplicialMap excision=cover.excisionMap(); AbelianGroupHomomorphism e=RelativeSimplicialCochain.cohomologyMap(excision,z(k+1));
            assertTrue(e.isIsomorphism());
            AbelianGroupHomomorphism relative=RelativeSimplicialCochain.connectingCohomologyMap(excision.source(),z(k));
            assertEquals(cover.connectingCohomologyMap(z(k)),RelativeSimplicialCochain.ambientCohomologyMap(excision.target(),z(k+1)).compose(e.inverse()).compose(relative));
        }
    }
    @Test public void emptyNestedDisjointAndHugeDegreesRetainUnreducedZeroShapes() {
        FiniteSimplicialComplex empty=complex(),circle=circle().union(); BigInteger huge=BigInteger.TEN.pow(100);
        for(SimplicialCover cover : Arrays.asList(new SimplicialCover(empty,circle),new SimplicialCover(circle,empty),new SimplicialCover(circle,circle),new SimplicialCover(empty,empty),new SimplicialCover(complex(new int[]{0}),complex(new int[]{9})))) {
            for(int k=0;k<=2;k++) { exactSegment(cover,k); naturality(SimplicialCoverMap.identity(cover),k); } cochainExactness(cover);
            assertTrue(cover.sumCohomology(huge).isAcyclic()); assertEquals(IntegerMatrix.zero(0,0),cover.connectingCochainMatrix(huge));
            assertTrue(SimplicialCoverMap.identity(cover).longExactCohomologyMaps(huge).get(3).isIsomorphism());
        }
        SimplicialCover noCover=new SimplicialCover(empty,empty); SimplicialCoverMap noMap=SimplicialCoverMap.identity(noCover);
        assertTrue(noCover.sumCoboundaryMatrices().isEmpty()); assertTrue(noCover.sumCohomologyDegrees().isEmpty()); assertTrue(noMap.sumCochainMatrices().isEmpty()); assertTrue(noMap.sumCohomologyMaps().isEmpty());
        SimplicialCover disjoint=new SimplicialCover(complex(new int[]{0}),complex(new int[]{9})); assertTrue(disjoint.restrictionCohomologyMap(z(0)).isIsomorphism());
    }
    @Test public void immutableListsAndNegativeDegreesRespectExistingFailureKinds() {
        SimplicialCover cover=circle(); SimplicialCoverMap map=SimplicialCoverMap.identity(cover);
        assertThrows(UnsupportedOperationException.class,() -> cover.sumCoboundaryMatrices().clear()); assertThrows(UnsupportedOperationException.class,() -> cover.sumCohomologyDegrees().clear());
        assertThrows(UnsupportedOperationException.class,() -> cover.longExactCohomologySegment(z(0)).clear()); assertThrows(UnsupportedOperationException.class,() -> map.sumCochainMatrices().clear());
        assertThrows(UnsupportedOperationException.class,() -> map.sumCohomologyMaps().clear()); assertThrows(UnsupportedOperationException.class,() -> map.longExactCohomologyMaps(z(0)).clear());
        for(Runnable action : Arrays.<Runnable>asList(() -> cover.sumCohomology(z(-1)),() -> cover.sumCoboundaryMatrix(z(-1)),() -> cover.restrictionMatrix(z(-1)),() -> cover.differenceMatrix(z(-1)),
                () -> cover.connectingCochainMatrix(z(-1)),() -> cover.connectingCohomologyMap(z(-1)),() -> cover.longExactCohomologySegment(z(-1)),() -> map.sumCochainMatrix(z(-1)),
                () -> map.sumCohomologyMap(z(-1)),() -> map.longExactCohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
    }
    @Test public void combinedCochainRanksAndWholeSegmentBudgetsAreBounded() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<129;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex large=new FiniteSimplicialComplex(points);
        SimplicialCover doubled=new SimplicialCover(large,large); SimplicialCoverMap largeMap=SimplicialCoverMap.identity(doubled);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> doubled.sumCoboundaryMatrix(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> doubled.sumCohomology(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> doubled.restrictionMatrix(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> largeMap.sumCochainMatrix(z(0)));
        FiniteSimplicialComplex fifty=new FiniteSimplicialComplex(points.subList(0,50)); SimplicialCover costly=new SimplicialCover(fifty,fifty);
        assertNotNull(costly.restrictionCohomologyMap(z(0))); assertNotNull(costly.differenceCohomologyMap(z(0))); assertNotNull(costly.connectingCohomologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.longExactCohomologySegment(z(0)));
        FiniteSimplicialComplex fortyTwo=new FiniteSimplicialComplex(points.subList(0,42)); SimplicialCoverMap costlyMap=SimplicialCoverMap.identity(new SimplicialCover(fortyTwo,fortyTwo));
        assertNotNull(costlyMap.sumCohomologyMap(z(0))); assertNotNull(costlyMap.unionCohomologyMap(z(0))); assertNotNull(costlyMap.intersectionCohomologyMap(z(0))); assertNotNull(costlyMap.unionCohomologyMap(z(1)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costlyMap.longExactCohomologyMaps(z(0)));
    }
    @Test public void wholeDegreeListsShareOneBudgetAcrossIndividuallyFeasibleOutputs() {
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int omit=0;omit<8;omit++) { List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); facets.add(new FiniteSet<>(face)); }
        for(int i=0;i<70;i++) facets.add(FiniteSet.of(100+i));
        SimplicialCoverMap map=SimplicialCoverMap.identity(new SimplicialCover(new FiniteSimplicialComplex(facets),complex()));
        for(int k=0;k<=6;k++) assertNotNull(map.sumCohomologyMap(z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,map::sumCohomologyMaps);
        for(int i=70;i<180;i++) facets.add(FiniteSet.of(100+i));
        SimplicialCover cover=new SimplicialCover(new FiniteSimplicialComplex(facets),complex());
        for(int k=0;k<=6;k++) assertNotNull(cover.sumCohomology(z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,cover::sumCohomologyDegrees);
    }
    @Test public void nativeWrappersAndSerializedFlowsUseTheExistingMathTool() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCover cover=circle(); IAlgebraItem<SimplicialCover> item=math.simplicialCovers.algebra().buildAlgebraItem(cover);
        assertSame(math.integralHomology.algebra(),item.performUnsafeOperation("sum-cohomology",z(0)).getAlgebra());
        assertSame(math.integerMatrices.algebra(),item.performUnsafeOperation("connecting-cochain-matrix",z(0)).getAlgebra());
        for(IAlgebraItem<AbelianGroupHomomorphism> value : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-cohomology-segment",z(0))) assertSame(math.abelianHomomorphisms.algebra(),value.getAlgebra());
        IAlgebraItem<SimplicialCoverMap> map=math.coverMaps.algebra().buildAlgebraItem(SimplicialCoverMap.identity(cover));
        for(IAlgebraItem<AbelianGroupHomomorphism> value : map.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-cohomology-maps",z(0))) assertSame(math.abelianHomomorphisms.algebra(),value.getAlgebra());
        for(IAlgebraItem<IntegerMatrix> value : map.<IntegerMatrix>performAlgebraFlatTransfer("sum-cochain-matrices")) assertSame(math.integerMatrices.algebra(),value.getAlgebra());
        IAlgebraFlow<Boolean> flow=math.flow(math.simplicialCovers,Collections.singletonList(cover))
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("connecting-cohomology-map",z(0)).<Boolean>performAlgebraTransfer("is-surjective");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("true"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","true","true","true"),math.flow(math.coverMaps,Collections.singletonList(SimplicialCoverMap.identity(cover)))
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("long-exact-cohomology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
