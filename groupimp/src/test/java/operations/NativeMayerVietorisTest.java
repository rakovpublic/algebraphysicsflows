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

public class NativeMayerVietorisTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int[] facet : facets) { List<Integer> values=new ArrayList<>(); for(int vertex : facet) values.add(vertex); faces.add(new FiniteSet<>(values)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] entries=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { entries[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) entries[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(entries);
    }
    private static IntegerVector v(long... values) { BigInteger[] entries=new BigInteger[values.length]; for(int i=0;i<values.length;i++) entries[i]=z(values[i]); return new IntegerVector(entries); }
    private static SimplicialCover circleCover() { return new SimplicialCover(complex(new int[]{0,1},new int[]{1,2}),complex(new int[]{0,2})); }
    private static FiniteSimplicialComplex plane() {
        return complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void exact(AbelianGroupHomomorphism first,AbelianGroupHomomorphism second) {
        assertEquals(first.target(),second.source()); assertTrue(second.compose(first).isZero());
        for(AbelianGroupElement generator : second.kernelInclusion().generatorImages()) assertTrue(first.hasPreimage(generator));
    }
    private static void exactSegment(SimplicialCover cover,int k) {
        List<AbelianGroupHomomorphism> maps=cover.longExactSegment(z(k)); assertEquals(3,maps.size());
        assertEquals(cover.intersectionHomologyMap(z(k)),maps.get(0)); assertEquals(cover.unionHomologyMap(z(k)),maps.get(1)); assertEquals(cover.connectingHomologyMap(z(k)),maps.get(2));
        exact(maps.get(0),maps.get(1)); exact(maps.get(1),maps.get(2));
        if(k>0) exact(maps.get(2),cover.intersectionHomologyMap(z(k-1))); else assertTrue(maps.get(2).target().type().isTrivial());
    }
    private static void chainIdentities(SimplicialCover cover) {
        for(int k=0;k<=cover.union().dimension()+1;k++) {
            BigInteger degree=z(k); IntegerMatrix alpha=cover.intersectionMatrix(degree),beta=cover.unionMatrix(degree),split=cover.splitMatrix(degree);
            assertEquals(IntegerMatrix.zero(beta.rows(),alpha.columns()),beta.multiply(alpha)); assertEquals(IntegerMatrix.identity(beta.rows()),beta.multiply(split));
            assertEquals(alpha.columns(),alpha.rank());
            for(IntegerVector generator : beta.kernelBasis()) assertTrue(alpha.hasIntegerSolution(generator));
            if(k>0) {
                assertEquals(cover.sumBoundaryMatrix(degree).multiply(alpha),cover.intersectionMatrix(z(k-1)).multiply(cover.intersection().integralBoundaryMatrix(degree)));
                assertEquals(cover.union().integralBoundaryMatrix(degree).multiply(beta),cover.unionMatrix(z(k-1)).multiply(cover.sumBoundaryMatrix(degree)));
            }
        }
    }
    @Test public void twoArcsRecoverTheCircleGeneratorAndItsOrientedEndpointDifference() {
        SimplicialCover cover=circleCover(); IntegralHomology h=cover.unionHomology(z(1)),target=cover.intersectionHomology(z(0));
        assertEquals(AbelianGroupType.Z,h.type()); assertEquals(AbelianGroupType.free(z(2)),cover.sumHomology(z(0)).type());
        assertEquals(AbelianGroupType.ZERO,cover.sumHomology(z(1)).type()); assertEquals(complex(new int[]{0},new int[]{2}),cover.intersection());
        assertEquals(m(new long[]{-1},new long[]{1}),cover.connectingHomologyMap(z(1)).smithMatrix());
        assertEquals(v(-1,1),cover.connectingChainMatrix(z(1)).multiply(v(1,-1,1)));
        assertEquals(target.classOf(v(-1,1)),cover.connectingHomologyMap(z(1)).apply(h.classOf(v(1,-1,1))));
        assertTrue(cover.connectingHomologyMap(z(1)).isInjective()); assertEquals(AbelianGroupType.Z,cover.connectingHomologyMap(z(1)).cokernel().type());
        chainIdentities(cover); exactSegment(cover,0); exactSegment(cover,1);
    }
    @Test public void sphereCoversGiveConnectingIsomorphismsThroughDimensionFour() {
        for(int n=1;n<=4;n++) {
            int[] leftFacet=new int[n+1]; for(int i=0;i<=n;i++) leftFacet[i]=i;
            List<int[]> rightFacets=new ArrayList<>(); for(int omit=0;omit<=n;omit++) { int[] face=new int[n+1]; int p=0; for(int i=0;i<=n+1;i++) if(i!=omit) face[p++]=i; rightFacets.add(face); }
            SimplicialCover cover=new SimplicialCover(complex(leftFacet),complex(rightFacets.toArray(new int[0][])));
            assertEquals(AbelianGroupType.Z,cover.unionHomology(z(n)).type()); assertEquals(AbelianGroupType.ZERO,cover.sumHomology(z(n)).type());
            assertEquals(z(1+((n&1)==0?1:-1)),cover.eulerCharacteristic());
            if(n>=2) assertTrue(cover.connectingHomologyMap(z(n)).isIsomorphism());
            chainIdentities(cover); exactSegment(cover,n);
        }
    }
    @Test public void projectivePlaneCoverRetainsTheIndexTwoIntersectionMap() {
        FiniteSimplicialComplex plane=plane(); List<FiniteSet<Integer>> facets=new ArrayList<>(plane.simplices(2)); facets.remove(FiniteSet.of(0,1,2));
        SimplicialCover cover=new SimplicialCover(new FiniteSimplicialComplex(facets),complex(new int[]{0,1,2}));
        assertEquals(plane,cover.union()); assertEquals(AbelianGroupType.Z,cover.leftHomology(z(1)).type()); assertEquals(AbelianGroupType.ZERO,cover.rightHomology(z(1)).type());
        assertEquals(AbelianGroupType.Z,cover.intersectionHomology(z(1)).type()); assertEquals(AbelianGroupType.cyclic(z(2)),cover.unionHomology(z(1)).type());
        AbelianGroupHomomorphism alpha=cover.intersectionHomologyMap(z(1)),beta=cover.unionHomologyMap(z(1));
        assertTrue(alpha.isInjective()); assertEquals(AbelianGroupType.cyclic(z(2)),alpha.cokernel().type()); assertTrue(beta.isSurjective());
        exactSegment(cover,0); exactSegment(cover,1); exactSegment(cover,2); chainIdentities(cover);
    }
    @Test public void componentMapsExhibitAnActualDirectSumIncludingTorsion() {
        SimplicialCover cover=new SimplicialCover(plane(),circleCover().union());
        assertEquals(AbelianGroupType.Z.directSum(AbelianGroupType.cyclic(z(2))),cover.sumHomology(z(1)).type());
        for(int k=0;k<=2;k++) {
            AbelianGroupHomomorphism i=cover.leftInclusionMap(z(k)),j=cover.rightInclusionMap(z(k)),p=cover.leftProjectionMap(z(k)),q=cover.rightProjectionMap(z(k));
            assertEquals(AbelianGroupHomomorphism.identity(i.source()),p.compose(i)); assertEquals(AbelianGroupHomomorphism.identity(j.source()),q.compose(j));
            assertTrue(p.compose(j).isZero()); assertTrue(q.compose(i).isZero()); assertEquals(AbelianGroupHomomorphism.identity(i.target()),i.compose(p).add(j.compose(q)));
            assertEquals(cover.leftHomology(z(k)).group(),i.source()); assertEquals(cover.rightHomology(z(k)).group(),j.source());
        }
    }
    private static FiniteSimplicialComplex graph(int mask) {
        List<int[]> facets=new ArrayList<>(Arrays.asList(new int[]{0},new int[]{1},new int[]{2})); int[][] edges={{0,1},{0,2},{1,2}};
        for(int i=0;i<3;i++) if((mask&(1<<i))!=0) facets.add(edges[i]); return complex(facets.toArray(new int[0][]));
    }
    private static int components(int mask) { return mask==0?3:Integer.bitCount(mask)==1?2:1; }
    @Test public void all64OrderedThreeVertexGraphCoversMatchIndependentBettiAndExactnessCalculations() {
        for(int a=0;a<8;a++) for(int b=0;b<8;b++) {
            SimplicialCover cover=new SimplicialCover(graph(a),graph(b));
            assertEquals(AbelianGroupType.free(z(components(a)+components(b))),cover.sumHomology(z(0)).type());
            assertEquals(AbelianGroupType.free(z((a==7?1:0)+(b==7?1:0))),cover.sumHomology(z(1)).type());
            assertEquals(AbelianGroupType.free(z(components(a|b))),cover.unionHomology(z(0)).type());
            assertEquals(AbelianGroupType.free(z((a|b)==7?1:0)),cover.unionHomology(z(1)).type());
            assertEquals(AbelianGroupType.free(z(components(a&b))),cover.intersectionHomology(z(0)).type());
            assertEquals(z(3-Integer.bitCount(a|b)),cover.eulerCharacteristic()); chainIdentities(cover); exactSegment(cover,0); exactSegment(cover,1);
            for(int k=0;k<=2;k++) assertTrue(cover.excisionMap().homologyMap(z(k)).isIsomorphism());
        }
    }
    @Test public void simplexSplittingIsASectionButNeedNotCommuteWithBoundaries() {
        SimplicialCover cover=circleCover(); IntegerMatrix split=cover.splitMatrix(z(1));
        assertEquals(m(new long[]{1,0,0},new long[]{0,0,1},new long[]{0,1,0}),split);
        assertNotEquals(cover.sumBoundaryMatrix(z(1)).multiply(split),cover.splitMatrix(z(0)).multiply(cover.union().integralBoundaryMatrix(z(1))));
        // When an edge belongs to both pieces, its entire chain is assigned to the left.
        SimplicialCover doubled=new SimplicialCover(complex(new int[]{0,1}),complex(new int[]{0,1}));
        assertEquals(m(new long[]{1},new long[]{0}),doubled.splitMatrix(z(1))); assertEquals(m(new long[]{1},new long[]{-1}),doubled.intersectionMatrix(z(1)));
        assertEquals(m(new long[]{1,1}),doubled.unionMatrix(z(1))); chainIdentities(doubled);
    }
    @Test public void swappingPiecesNegatesTheConnectingHomomorphism() {
        for(SimplicialCover cover : Arrays.asList(circleCover(),new SimplicialCover(plane(),circleCover().union()))) {
            assertEquals(cover,cover.swap().swap()); assertNotEquals(cover,cover.swap()); assertEquals(cover.union(),cover.swap().union());
            for(int k=0;k<=cover.union().dimension()+1;k++) assertEquals(cover.connectingHomologyMap(z(k)).scale(z(-1)),cover.swap().connectingHomologyMap(z(k)));
        }
    }
    @Test public void excisionIsAnExplicitRelativeChainIsomorphismWithoutAnAmbientInverse() {
        for(SimplicialCover cover : Arrays.asList(circleCover(),new SimplicialCover(plane(),complex(new int[]{0})),new SimplicialCover(plane(),plane()))) {
            RelativeSimplicialMap excision=cover.excisionMap();
            assertEquals(new RelativeSimplicialComplex(cover.left(),cover.intersection()),excision.source());
            assertEquals(new RelativeSimplicialComplex(cover.union(),cover.right()),excision.target());
            for(int k=0;k<=cover.union().dimension()+1;k++) {
                assertEquals(IntegerMatrix.identity(excision.source().simplexCount(z(k))),excision.chainMatrix(z(k)));
                assertEquals(excision.source().boundaryMatrix(z(k)),excision.target().boundaryMatrix(z(k))); assertTrue(excision.homologyMap(z(k)).isIsomorphism());
            }
        }
        assertFalse(circleCover().excisionMap().isIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> circleCover().excisionMap().inverse());
    }
    @Test public void emptyDisjointNestedAndHugeDegreesRespectUnreducedConventions() {
        FiniteSimplicialComplex empty=complex(),circle=circleCover().union();
        for(SimplicialCover cover : Arrays.asList(new SimplicialCover(empty,circle),new SimplicialCover(circle,empty),new SimplicialCover(circle,circle),new SimplicialCover(empty,empty),new SimplicialCover(complex(new int[]{0}),complex(new int[]{9})))) {
            for(int k=0;k<=2;k++) exactSegment(cover,k); chainIdentities(cover);
            assertTrue(cover.unionHomology(BigInteger.TEN.pow(100)).isAcyclic()); assertEquals(IntegerMatrix.zero(0,0),cover.sumBoundaryMatrix(BigInteger.TEN.pow(100)));
        }
        SimplicialCover disjoint=new SimplicialCover(complex(new int[]{0}),complex(new int[]{9})); assertTrue(disjoint.unionHomologyMap(z(0)).isIsomorphism());
        assertEquals(IntegerMatrix.zero(0,2),disjoint.connectingChainMatrix(z(0))); assertTrue(disjoint.connectingHomologyMap(z(0)).target().type().isTrivial());
        assertTrue(new SimplicialCover(empty,empty).sumBoundaryMatrices().isEmpty()); assertTrue(new SimplicialCover(empty,empty).sumHomologyDegrees().isEmpty());
    }
    @Test public void valuesAreImmutableOrderedAndRejectNegativeDegrees() {
        SimplicialCover cover=circleCover(); assertEquals(circleCover(),cover); assertEquals(circleCover().hashCode(),cover.hashCode());
        assertThrows(UnsupportedOperationException.class,() -> cover.sumBoundaryMatrices().clear()); assertThrows(UnsupportedOperationException.class,() -> cover.sumHomologyDegrees().clear());
        assertThrows(UnsupportedOperationException.class,() -> cover.longExactSegment(z(1)).clear());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cover.sumBoundaryMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cover.leftHomology(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cover.sumHomology(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cover.splitMatrix(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cover.connectingHomologyMap(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cover.longExactSegment(z(-1)));
    }
    @Test public void limitsIncludeTheUnionCombinedRanksAndWholeComputationBudgets() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<4102;i++) points.add(FiniteSet.of(i));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new SimplicialCover(new FiniteSimplicialComplex(points.subList(0,2051)),new FiniteSimplicialComplex(points.subList(2051,4102))));
        FiniteSimplicialComplex large=new FiniteSimplicialComplex(points.subList(0,129)); SimplicialCover doubled=new SimplicialCover(large,large);
        assertEquals(IntegerMatrix.zero(0,129),large.integralBoundaryMatrix(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> doubled.sumBoundaryMatrix(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> doubled.unionMatrix(z(0)));
        FiniteSimplicialComplex fifty=new FiniteSimplicialComplex(points.subList(0,50)); SimplicialCover costly=new SimplicialCover(fifty,fifty);
        assertNotNull(costly.intersectionHomologyMap(z(0))); assertNotNull(costly.unionHomologyMap(z(0))); assertNotNull(costly.connectingHomologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.longExactSegment(z(0)));
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int omit=0;omit<8;omit++) { List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); facets.add(new FiniteSet<>(face)); }
        for(int i=0;i<140;i++) facets.add(FiniteSet.of(100+i));
        SimplicialCover aggregate=new SimplicialCover(new FiniteSimplicialComplex(facets),complex());
        for(int k=0;k<=6;k++) assertNotNull(aggregate.sumHomology(z(k))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,aggregate::sumHomologyDegrees);
    }
    @Test public void nativeOperationsUseExistingScalarAndFlatResultWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCover cover=circleCover();
        IAlgebraItem<SimplicialCover> item=math.complexes.algebra().buildAlgebraItem(cover.left()).performCustomResultOperation("SimplicialCover.from-complexes",cover.right());
        assertSame(math.simplicialCovers.algebra(),item.getAlgebra()); assertEquals(cover,item.getResult());
        assertSame(math.complexes.algebra(),item.performAlgebraTransfer("intersection").getAlgebra()); assertSame(math.relativeMaps.algebra(),item.performAlgebraTransfer("excision-map").getAlgebra());
        assertSame(math.integralHomology.algebra(),item.performUnsafeOperation("sum-homology",z(0)).getAlgebra());
        assertSame(math.integerMatrices.algebra(),item.performUnsafeOperation("split-matrix",z(1)).getAlgebra());
        for(IAlgebraItem<AbelianGroupHomomorphism> map : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-segment",z(1))) assertSame(math.abelianHomomorphisms.algebra(),map.getAlgebra());
    }
    @Test public void serializedMayerVietorisAndExcisionFlowsAreRepeatable() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCover cover=circleCover();
        IAlgebraFlow<IntegerMatrix> flow=math.flow(math.complexes,Collections.singletonList(cover.left()))
                .<SimplicialCover>performCustomResultOperation("SimplicialCover.from-complexes",cover.right())
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("connecting-homology-map",z(1)).<IntegerMatrix>performAlgebraTransfer("smith-matrix");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("ZMatrix(2x1)[[-1], [1]]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Collections.singletonList("true"),math.flow(math.simplicialCovers,Collections.singletonList(cover)).<RelativeSimplicialMap>performAlgebraTransfer("excision-map")
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("homology-map",z(1)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
