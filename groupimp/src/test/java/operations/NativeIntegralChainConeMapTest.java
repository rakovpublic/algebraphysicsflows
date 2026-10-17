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

public class NativeIntegralChainConeMapTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static RelativeSimplicialComplex pair(int[]... facets) {
        List<FiniteSet<Integer>> f=new ArrayList<>(); for(int[] facet : facets) { List<Integer> v=new ArrayList<>(); for(int x : facet) v.add(x); f.add(new FiniteSet<>(v)); }
        return RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(f));
    }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return pair(f); }
    private static RelativeSimplicialComplex circle() { return pair(new int[]{0,1},new int[]{0,2},new int[]{1,2}); }
    private static IntegerMatrix m(long[]... rows) { BigInteger[][] r=new BigInteger[rows.length][]; for(int i=0;i<rows.length;i++) { r[i]=new BigInteger[rows[i].length]; for(int j=0;j<rows[i].length;j++) r[i][j]=z(rows[i][j]); } return new IntegerMatrix(r); }
    private static IntegralChainMappingCone cone(SimplicialChainMap f) { return new IntegralChainMappingCone(f); }
    private static IntegralChainConeMap square(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b,SimplicialChainHomotopy h) {
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),a,b,h));
    }
    private static IntegralChainConeMap strict(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b) { return square(f,g,a,b,SimplicialChainHomotopy.stationary(b.compose(f))); }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static SimplicialChainMap intervalMap(int a,int b,int n) { RelativeSimplicialComplex p=pair(new int[]{0,1}); return new SimplicialChainMap(p,p,Arrays.asList(m(new long[]{a,b},new long[]{n-a,n-b}),m(new long[]{a-b}))); }
    private static SimplicialChainHomotopy intervalHomotopy(int a,int b,int n) {
        return new SimplicialChainHomotopy(intervalMap(a,b,n),intervalMap(b,a,n),Arrays.asList(m(new long[]{a-b,b-a}),IntegerMatrix.zero(0,1)));
    }
    private static void differential(IntegralChainConeMap map) {
        int top=Math.max(map.source().dimension(),map.target().dimension());
        for(int k=1;k<=top+1;k++) assertEquals(map.target().boundaryMatrix(z(k)).multiply(map.chainMatrix(z(k))),map.chainMatrix(z(k-1)).multiply(map.source().boundaryMatrix(z(k))));
    }
    private static void naturality(IntegralChainConeMap map,int degree) {
        BigInteger n=z(degree); List<AbelianGroupHomomorphism> s=map.source().longExactSegment(n),t=map.target().longExactSegment(n),v=map.homologyNaturalityMaps(n);
        assertEquals(4,v.size()); for(int i=0;i<3;i++) assertEquals(t.get(i).compose(v.get(i)),v.get(i+1).compose(s.get(i)));
        s=map.source().longExactCohomologySegment(n); t=map.target().longExactCohomologySegment(n); v=map.cohomologyNaturalityMaps(n);
        assertEquals(4,v.size()); for(int i=0;i<3;i++) assertEquals(s.get(i).compose(v.get(i)),v.get(i+1).compose(t.get(i)));
    }
    @Test public void all125IntervalHomotopiesGiveIndependentSignedConeIsomorphisms() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int n=-2;n<=2;n++) {
            SimplicialChainHomotopy h=intervalHomotopy(a,b,n); IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(h);
            assertEquals(IntegerMatrix.identity(2),map.chainMatrix(z(0)));
            assertEquals(m(new long[]{1,b-a,a-b},new long[]{0,1,0},new long[]{0,0,1}),map.chainMatrix(z(1)));
            assertEquals(IntegerMatrix.identity(1),map.chainMatrix(z(2))); differential(map);
            IntegralChainConeMap reverse=IntegralChainConeMap.fromHomotopy(h.reverse());
            assertEquals(IntegralChainConeMap.identity(map.source()),reverse.compose(map)); assertEquals(IntegralChainConeMap.identity(map.target()),map.compose(reverse));
            for(int k=0;k<=2;k++) { assertTrue(map.homologyMap(z(k)).isIsomorphism()); assertTrue(map.cohomologyMap(z(k)).isIsomorphism()); }
        }
    }
    private static IntegralChainConeMap shear(int a,int b,int loop) {
        SimplicialChainMap zero=SimplicialChainMap.zero(points(1),circle());
        SimplicialChainHomotopy h=new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{loop},new long[]{-loop},new long[]{loop}),IntegerMatrix.zero(0,0)));
        return square(zero,zero,SimplicialChainMap.identity(points(1)).scale(z(a)),SimplicialChainMap.identity(circle()).scale(z(b)),h);
    }
    @Test public void all729CompositesTransportChosenWitnessesWithIndependentCoefficients() {
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int h=-1;h<=1;h++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) for(int k=-1;k<=1;k++) {
            IntegralChainConeMap before=shear(a,b,h),after=shear(c,d,k),composite=after.compose(before);
            assertEquals(shear(c*a,d*b,d*h+k*a),composite);
            int w=d*h+k*a; assertEquals(m(new long[]{d*b,0,0,-w},new long[]{0,d*b,0,w},new long[]{0,0,d*b,-w},new long[]{0,0,0,c*a}),composite.chainMatrix(z(1)));
        }
    }
    @Test public void compositionIsAssociativeUnitalAndRetainsNonzeroLoops() {
        IntegralChainConeMap a=shear(2,-1,3),b=shear(-2,3,-1),c=shear(1,2,2),id=IntegralChainConeMap.identity(a.source());
        assertEquals(c.compose(b).compose(a),c.compose(b.compose(a))); assertEquals(a,id.compose(a)); assertEquals(a,a.compose(id));
        assertNotEquals(shear(1,1,1),id); assertEquals(shear(1,1,2),shear(1,1,1).compose(shear(1,1,1)));
        assertNotEquals(a.compose(b),b.compose(a));
        assertEquals(a,new IntegralChainConeMap(a.data())); assertEquals(a.hashCode(),new IntegralChainConeMap(a.data()).hashCode());
    }
    @Test public void chosenLoopsChangeIntegralConeHomologyEvenWithIdenticalSquareMaps() {
        IntegralChainConeMap map=shear(1,1,1),id=IntegralChainConeMap.identity(map.source()); IntegralHomology h=map.source().homology(z(1));
        IntegerVector shifted=new IntegerVector(z(0),z(0),z(0),z(1)),image=new IntegerVector(z(-1),z(1),z(-1),z(1));
        assertEquals(h.classOf(image),map.homologyMap(z(1)).apply(h.classOf(shifted)));
        assertNotEquals(id.homologyMap(z(1)),map.homologyMap(z(1))); assertNotEquals(id.cohomologyMap(z(1)),map.cohomologyMap(z(1)));
        naturality(map,0); naturality(map,1); naturality(map,2);
    }
    @Test public void projectivePlaneLoopsRetainOrderTwoEffectsAndUnboundedCoefficients() {
        RelativeSimplicialComplex plane=pair(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        IntegerVector cycle=plane.homology(z(1)).generators().get(0); BigInteger huge=BigInteger.ONE.shiftLeft(1024).add(z(1));
        SimplicialChainMap zero=SimplicialChainMap.zero(points(1),plane); List<IntegerMatrix> hs=new ArrayList<>();
        BigInteger[][] entries=new BigInteger[15][1]; for(int i=0;i<15;i++) entries[i][0]=cycle.get(i).multiply(huge);
        hs.add(new IntegerMatrix(entries)); hs.add(IntegerMatrix.zero(10,0)); hs.add(IntegerMatrix.zero(0,0));
        IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(new SimplicialChainHomotopy(zero,zero,hs)),id=IntegralChainConeMap.identity(map.source());
        assertEquals(new AbelianGroupType(z(1),Collections.singletonList(z(2))),map.source().homologyType(z(1)));
        assertNotEquals(id.homologyMap(z(1)),map.homologyMap(z(1))); assertEquals(id.homologyMap(z(1)),map.compose(map).homologyMap(z(1)));
        assertNotEquals(id,map.compose(map)); naturality(map,1); naturality(map,2);
    }
    @Test public void all6561PointMatrixCompositesMatchCoordinateSums() {
        RelativeSimplicialComplex p=points(2); SimplicialChainMap zero=SimplicialChainMap.zero(p,p),id=SimplicialChainMap.identity(p);
        List<IntegerMatrix> matrices=new ArrayList<>(); List<IntegralChainConeMap> maps=new ArrayList<>();
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) { IntegerMatrix v=m(new long[]{a,b},new long[]{c,d}); matrices.add(v); maps.add(strict(zero,zero,id,new SimplicialChainMap(p,p,Collections.singletonList(v)))); }
        for(int i=0;i<81;i++) for(int j=0;j<81;j++) {
            IntegerMatrix a=matrices.get(i),b=matrices.get(j); BigInteger[][] expected=new BigInteger[2][2];
            for(int r=0;r<2;r++) for(int c=0;c<2;c++) expected[r][c]=a.get(r,0).multiply(b.get(0,c)).add(a.get(r,1).multiply(b.get(1,c)));
            IntegralChainConeMap composite=maps.get(i).compose(maps.get(j)); assertEquals(new IntegerMatrix(expected),composite.chainMatrix(z(0))); assertEquals(IntegerMatrix.identity(2),composite.chainMatrix(z(1)));
        }
    }
    @Test public void rectangularSquaresPreserveTorsionAndAllSixNaturalitySquares() {
        RelativeSimplicialComplex p=points(1),q=points(2);
        SimplicialChainMap f=SimplicialChainMap.identity(p).scale(z(2)),g=new SimplicialChainMap(p,q,Collections.singletonList(m(new long[]{2},new long[]{0})));
        SimplicialChainMap a=SimplicialChainMap.identity(p),b=new SimplicialChainMap(p,q,Collections.singletonList(m(new long[]{1},new long[]{0})));
        IntegralChainConeMap map=strict(f,g,a,b); differential(map);
        assertEquals(m(new long[]{1},new long[]{0}),map.chainMatrix(z(0))); assertEquals(IntegerMatrix.identity(1),map.chainMatrix(z(1)));
        assertEquals(AbelianGroupType.cyclic(z(2)),map.homologyMap(z(0)).source().type()); assertEquals(new AbelianGroupType(z(1),Collections.singletonList(z(2))),map.homologyMap(z(0)).target().type());
        for(int n=0;n<=2;n++) naturality(map,n);
    }
    @Test public void homologyCompositionIsCovariantAndCohomologyReversesOrder() {
        IntegralChainConeMap a=shear(2,3,1),b=shear(-1,2,4),c=b.compose(a);
        for(int n=0;n<=2;n++) { assertEquals(b.homologyMap(z(n)).compose(a.homologyMap(z(n))),c.homologyMap(z(n))); assertEquals(a.cohomologyMap(z(n)).compose(b.cohomologyMap(z(n))),c.cohomologyMap(z(n))); }
        assertNotEquals(a.homologyMap(z(1)).compose(b.homologyMap(z(1))),c.homologyMap(z(1)));
    }
    @Test public void suppliedWitnessesMustMatchBothCompositesAndEveryFullPair() {
        SimplicialChainHomotopy h=intervalHomotopy(1,0,1); IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(h);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> square(h.from(),h.to(),map.sourceMap(),map.targetMap(),h.reverse()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> square(h.from(),h.to(),map.sourceMap(),map.targetMap(),SimplicialChainHomotopy.stationary(h.from())));
        SimplicialChainMap relabel=SimplicialChainMap.identity(pair(new int[]{7,8}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> square(h.from(),h.to(),relabel,map.targetMap(),h));
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(h.source().ambient());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> square(h.from(),h.to(),SimplicialChainMap.identity(diagonal),map.targetMap(),h));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map.compose(map));
        assertThrows(NullPointerException.class,() -> new IntegralChainConeMap.Data(null,map.target(),map.sourceMap(),map.targetMap(),h));
    }
    @Test public void emptyChangingAndFilteredDimensionsRetainFullDegreeRanges() {
        for(int n : new int[]{0,1,3}) {
            RelativeSimplicialComplex p=points(n),empty=points(0); SimplicialChainMap f=SimplicialChainMap.zero(empty,p),g=SimplicialChainMap.zero(p,empty);
            IntegralChainConeMap map=strict(f,g,SimplicialChainMap.zero(empty,p),SimplicialChainMap.zero(p,empty)); differential(map);
            assertEquals(n==0?0:2,map.chainMatrices().size()); for(AbelianGroupHomomorphism h : map.homologyMaps()) assertTrue(h.isZero()); naturality(map,0);
        }
        int[] vertices=new int[10]; for(int i=0;i<10;i++) vertices[i]=i;
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(pair(vertices).ambient()); IntegralChainConeMap filtered=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(diagonal)));
        assertEquals(Collections.nCopies(11,IntegerMatrix.zero(0,0)),filtered.chainMatrices()); assertEquals(11,filtered.homologyMaps().size());
        BigInteger huge=BigInteger.ONE.shiftLeft(100); assertEquals(IntegerMatrix.zero(0,0),filtered.chainMatrix(huge)); assertTrue(filtered.homologyMap(huge).isIsomorphism());
    }
    @Test public void degreeZeroNaturalityKeepsNegativePresentationEndpoints() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(2))));
        AbelianGroupHomomorphism last=map.homologyNaturalityMaps(z(0)).get(3);
        assertEquals(IntegerMatrix.zero(0,2),last.source().relations()); assertEquals(last.source(),last.target());
        for(Runnable f : Arrays.<Runnable>asList(() -> map.chainMatrix(z(-1)),() -> map.cochainMatrix(z(-1)),() -> map.homologyMap(z(-1)),() -> map.cohomologyMap(z(-1)),() -> map.homologyNaturalityMaps(z(-1)),() -> map.cohomologyNaturalityMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,f);
        assertThrows(UnsupportedOperationException.class,map.chainMatrices()::clear); assertThrows(UnsupportedOperationException.class,map.homologyMaps()::clear); assertThrows(UnsupportedOperationException.class,() -> map.homologyNaturalityMaps(z(0)).clear());
    }
    @Test public void coneRankLimitsApplyToEvaluationWithoutChangingCarrierMembership() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<256;i++) facets.add(FiniteSet.of(i)); facets.add(FiniteSet.of(0,1));
        RelativeSimplicialComplex p=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(facets)); SimplicialChainMap zero=SimplicialChainMap.zero(p,p);
        // An empty target keeps validation small while the source cone has rank 257.
        SimplicialChainMap empty=SimplicialChainMap.identity(points(0)),vertical=SimplicialChainMap.zero(p,points(0));
        IntegralChainConeMap map=strict(zero,empty,vertical,vertical);
        assertEquals(zero,map.source().map());
        for(Runnable f : Arrays.<Runnable>asList(() -> map.chainMatrix(z(1)),map::chainMatrices,() -> map.homologyMap(z(0)))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("256 total"));
    }
    @Test public void nativeWrappersAndSerializedFlatFlowsRetainChosenSquareData() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainHomotopy h=intervalHomotopy(1,0,1); IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(h);
        IAlgebraItem<IntegralChainConeMap> item=math.chainHomotopies.algebra().buildAlgebraItem(h).performAlgebraTransfer("ChainConeMap.from-homotopy");
        assertSame(math.chainConeMaps.algebra(),item.getAlgebra()); assertEquals(map,item.perform().getResult()); assertSame(math.chainCones.algebra(),item.performAlgebraTransfer("source").getAlgebra());
        for(IAlgebraItem<?> part : item.performUnsafeFlatOperation("cohomology-naturality-maps",z(0))) assertSame(math.abelianHomomorphisms.algebra(),part.getAlgebra());
        List<IAlgebraFlow<?>> flows=Arrays.asList(
                math.flow(math.chainHomotopies,Collections.singletonList(h)).<IntegralChainConeMap>performAlgebraTransfer("ChainConeMap.from-homotopy").performFlatAlgebraTransfer("chain-matrices"),
                math.flow(math.chainConeMaps,Collections.singletonList(map)).performOperation("compose",IntegralChainConeMap.identity(map.source())).<AbelianGroupHomomorphism>performFlatAlgebraTransfer("homology-maps").performAlgebraTransfer("is-isomorphism"),
                math.flow(math.chainConeMaps,Collections.singletonList(map)).<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("cohomology-naturality-maps",z(0)).performAlgebraTransfer("is-isomorphism"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.nCopies(3,"true"),flows.get(1).collect()); assertEquals(Collections.nCopies(4,"true"),flows.get(2).collect());
    }
    @Test public void wholeDegreeMapsShareOneBudgetAcrossSuccessfulScalarDegrees() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(72))));
        for(int n=0;n<=1;n++) { assertTrue(map.homologyMap(z(n)).isIsomorphism()); assertTrue(map.cohomologyMap(z(n)).isIsomorphism()); }
        for(Runnable f : Arrays.<Runnable>asList(map::homologyMaps,map::cohomologyMaps)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("5000000"));
    }
    @Test public void fourNaturalityMapsShareOneBudgetAcrossSuccessfulScalarMaps() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(60))));
        for(int n=0;n<=1;n++) { assertTrue(map.homologyMap(z(n)).isIsomorphism()); assertTrue(map.cohomologyMap(z(n)).isIsomorphism()); }
        assertTrue(map.sourceMap().homologyMap(z(0)).isIsomorphism()); assertTrue(map.targetMap().cohomologyMap(z(0)).isIsomorphism());
        for(Runnable f : Arrays.<Runnable>asList(() -> map.homologyNaturalityMaps(z(0)),() -> map.cohomologyNaturalityMaps(z(0)))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("5000000"));
    }
    @Test public void completeCompositionSharesBudgetAcrossWitnessTransportAndValidation() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(90))));
        assertEquals(map.sourceMap(),map.sourceMap().compose(map.sourceMap())); assertEquals(map.targetMap(),map.targetMap().compose(map.targetMap()));
        assertEquals(map.homotopy(),map.homotopy().precompose(map.sourceMap())); assertEquals(map.homotopy(),map.homotopy().postcompose(map.targetMap()));
        assertEquals(map.homotopy(),map.homotopy().then(map.homotopy()));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> map.compose(map)).getMessage().contains("5000000"));
    }

    @Test public void relativeSquaresUseShiftedWitnessDegreesAndContravariantMatrices() {
        FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient(),triangle=pair(new int[]{0,1,2}).ambient();
        RelativeSimplicialComplex source=new RelativeSimplicialComplex(edge,edge.skeleton(0)),target=new RelativeSimplicialComplex(triangle,triangle.skeleton(1));
        SimplicialChainMap zero=SimplicialChainMap.zero(source,target);
        for(int n=-3;n<=3;n++) {
            SimplicialChainHomotopy h=new SimplicialChainHomotopy(zero,zero,Arrays.asList(IntegerMatrix.zero(0,0),m(new long[]{n}),IntegerMatrix.zero(0,0)));
            IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(h); IntegerMatrix expected=m(new long[]{1,-n},new long[]{0,1});
            assertEquals(expected,map.chainMatrix(z(2))); assertEquals(m(new long[]{1,0},new long[]{-n,1}),map.cochainMatrix(z(2)));
            assertEquals(Arrays.asList(IntegerMatrix.zero(0,0),IntegerMatrix.zero(0,0),expected),map.chainMatrices());
            assertEquals(map.source().homologyType(z(2)),AbelianGroupType.free(z(2))); differential(map); naturality(map,1); naturality(map,2);
        }
    }

}
