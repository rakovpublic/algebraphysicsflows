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

public class NativeIntegralChainConeTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return abs(complex(f)); }
    private static RelativeSimplicialComplex edge() { return abs(complex(new int[]{0,1})); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    private static IntegerMatrix matrix(long[]... rows) {
        BigInteger[][] result=new BigInteger[rows.length][]; for(int r=0;r<rows.length;r++) { result[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) result[r][c]=z(rows[r][c]); } return new IntegerMatrix(result);
    }
    private static IntegralChainMappingCone cone(SimplicialChainMap map) { return new IntegralChainMappingCone(map); }
    private static SimplicialChainMap pointMap(IntegerMatrix m) { return new SimplicialChainMap(points(m.columns()),points(m.rows()),Collections.singletonList(m)); }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static void exact(AbelianGroupHomomorphism first,AbelianGroupHomomorphism second) {
        assertEquals(first.target(),second.source()); assertTrue(second.compose(first).isZero());
        for(AbelianGroupElement generator : second.kernelInclusion().generatorImages()) assertTrue(first.hasPreimage(generator));
    }
    private static void segments(IntegralChainMappingCone c,int degree) {
        BigInteger n=z(degree); List<AbelianGroupHomomorphism> h=c.longExactSegment(n),q=c.longExactCohomologySegment(n);
        assertEquals(Arrays.asList(c.map().homologyMap(n),c.inclusionHomologyMap(n),c.projectionHomologyMap(n)),h);
        assertEquals(Arrays.asList(c.inclusionCohomologyMap(n),c.map().cohomologyMap(n),c.projectionCohomologyMap(n.add(BigInteger.ONE))),q);
        exact(h.get(0),h.get(1)); exact(h.get(1),h.get(2)); if(degree>0) exact(h.get(2),c.map().homologyMap(z(degree-1)));
        exact(q.get(0),q.get(1)); exact(q.get(1),q.get(2)); exact(q.get(2),c.inclusionCohomologyMap(z(degree+1)));
    }
    private static SimplicialChainMap intervalMap(int a,int b,int n) {
        return new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{a,b},new long[]{n-a,n-b}),matrix(new long[]{a-b})));
    }
    @Test public void all125IntervalConesHaveIndependentSignedBoundariesAndHomology() {
        for(int n=-2;n<=2;n++) for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) {
            IntegralChainMappingCone c=cone(intervalMap(a,b,n)); assertEquals(2,c.dimension());
            assertEquals(matrix(new long[]{-1,a,b},new long[]{1,n-a,n-b}),c.boundaryMatrix(z(1)));
            assertEquals(matrix(new long[]{a-b},new long[]{1},new long[]{-1}),c.boundaryMatrix(z(2)));
            assertEquals(IntegerMatrix.zero(2,1),c.boundaryMatrix(z(1)).multiply(c.boundaryMatrix(z(2))));
            assertEquals(AbelianGroupType.cyclic(z(Math.abs(n))),c.homologyType(z(0))); assertEquals(n==0?AbelianGroupType.Z:AbelianGroupType.ZERO,c.homologyType(z(1)));
            assertEquals(n==0?AbelianGroupType.Z:AbelianGroupType.ZERO,c.cohomologyType(z(0))); assertEquals(AbelianGroupType.cyclic(z(Math.abs(n))),c.cohomologyType(z(1)));
            assertEquals(Math.abs(n)==1,c.map().isQuasiIsomorphism());
        }
    }
    @Test public void all625PointMatricesMatchIndependentDeterminantalDivisors() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            BigInteger gcd=z(a).gcd(z(b)).gcd(z(c)).gcd(z(d)); int det=a*d-b*c,rank=det!=0?2:gcd.signum()==0?0:1;
            List<BigInteger> factors=new ArrayList<>(); if(rank>0) factors.add(gcd); if(rank==2) factors.add(z(Math.abs(det)).divide(gcd));
            AbelianGroupType cokernel=new AbelianGroupType(z(2-rank),factors),kernel=AbelianGroupType.free(z(2-rank));
            IntegralChainMappingCone cone=cone(pointMap(matrix(new long[]{a,b},new long[]{c,d})));
            assertEquals(cokernel,cone.homologyType(z(0))); assertEquals(kernel,cone.homologyType(z(1)));
            assertEquals(kernel,cone.cohomologyType(z(0))); assertEquals(cokernel,cone.cohomologyType(z(1)));
            assertEquals(Math.abs(det)==1,cone.isAcyclic());
        }
    }
    @Test public void multiplicationConesKeepExactTorsionClassesAndBoundingChains() {
        BigInteger huge=BigInteger.ONE.shiftLeft(1024).add(z(3)); IntegralChainMappingCone c=cone(pointMap(new IntegerMatrix(new BigInteger[][]{{huge}})));
        assertEquals(AbelianGroupType.cyclic(huge),c.homologyType(z(0))); assertEquals(AbelianGroupType.cyclic(huge),c.cohomologyType(z(1)));
        IntegralHomology h=c.homology(z(0)); assertEquals(huge,h.classOf(new IntegerVector(z(1))).order());
        assertEquals(new IntegerVector(z(1)),h.boundingChain(new IntegerVector(huge)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.boundingChain(new IntegerVector(z(1))));
        segments(c,0); segments(c,1);
    }
    @Test public void rectangularMapsRetainExactHomologicalAndCohomologicalSequences() {
        for(IntegerMatrix m : Arrays.asList(matrix(new long[]{2,4}),matrix(new long[]{2},new long[]{4}),matrix(new long[]{0,0}),IntegerMatrix.zero(0,2),IntegerMatrix.zero(2,0))) {
            IntegralChainMappingCone c=cone(pointMap(m)); segments(c,0); segments(c,1); segments(c,2);
        }
        for(int n=-2;n<=2;n++) { IntegralChainMappingCone c=cone(intervalMap(1,-1,n)); segments(c,0); segments(c,1); segments(c,2); }
    }
    private static RelativeSimplicialComplex plane() { return abs(complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5})); }
    private static RelativeSimplicialComplex disk() { FiniteSimplicialComplex d=complex(new int[]{0,1,2}); return new RelativeSimplicialComplex(d,d.skeleton(1)); }
    private static SimplicialChainMap ghost(int n) {
        BigInteger[][] top=new BigInteger[1][10]; Arrays.fill(top[0],z(0)); top[0][0]=z(n);
        return new SimplicialChainMap(plane(),disk(),Arrays.asList(IntegerMatrix.zero(0,6),IntegerMatrix.zero(0,15),new IntegerMatrix(top)));
    }
    @Test public void conesDetectNonsplitIntegralExtensionsInvisibleToInducedHomologyMaps() {
        IntegralChainMappingCone zero=cone(ghost(0));
        for(int n=-3;n<=3;n++) {
            IntegralChainMappingCone c=cone(ghost(n)); assertEquals(zero.map().homologyMaps(),c.map().homologyMaps());
            assertEquals(n%2==0?new AbelianGroupType(z(1),Collections.singletonList(z(2))):AbelianGroupType.Z,c.homologyType(z(2)));
            assertEquals(n%2==0?AbelianGroupType.cyclic(z(2)):AbelianGroupType.ZERO,c.cohomologyType(z(3))); segments(c,1); segments(c,2);
        }
    }
    @Test public void zeroMapsSplitHomologyIntoTargetAndShiftedSourceIncludingTorsion() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.zero(plane(),circle()));
        for(int k=0;k<=3;k++) {
            AbelianGroupType shifted=k==0?AbelianGroupType.ZERO:plane().homologyType(z(k-1));
            assertEquals(circle().homologyType(z(k)).directSum(shifted),c.homologyType(z(k))); segments(c,k);
        }
    }
    @Test public void boundaryInclusionAndProjectionObeyChainAndShiftedChainIdentities() {
        IntegralChainMappingCone c=cone(intervalMap(2,-1,3));
        for(int n=1;n<=3;n++) {
            assertEquals(c.inclusionMatrix(z(n-1)).multiply(c.target().boundaryMatrix(z(n))),c.boundaryMatrix(z(n)).multiply(c.inclusionMatrix(z(n))));
            assertEquals(c.source().boundaryMatrix(z(n-1)).multiply(c.projectionMatrix(z(n))).scale(z(-1)),c.projectionMatrix(z(n-1)).multiply(c.boundaryMatrix(z(n))));
            assertEquals(IntegerMatrix.zero(c.projectionMatrix(z(n)).rows(),c.inclusionMatrix(z(n)).columns()),c.projectionMatrix(z(n)).multiply(c.inclusionMatrix(z(n))));
        }
    }
    @Test public void inclusionConeMatchesRelativeHomologyOfThePair() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); IntegralChainMappingCone c=cone(SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(circle(),triangle)));
        for(int n=0;n<=3;n++) { assertEquals(disk().homologyType(z(n)),c.homologyType(z(n))); assertEquals(RelativeSimplicialCochain.cohomology(disk(),z(n)).type(),c.cohomologyType(z(n))); segments(c,n); }
    }
    @Test public void quasiIsomorphismDetectsIntegralUnitsAndExceedsInverseSearchBounds() {
        SimplicialChainMap id=SimplicialChainMap.identity(points(12)); assertTrue(id.isQuasiIsomorphism());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainInverseSolver.isHomotopyEquivalence(id));
        SimplicialChainMap contraction=intervalMap(1,1,1); assertFalse(contraction.isIsomorphism()); assertTrue(contraction.isQuasiIsomorphism());
        for(int n=-3;n<=3;n++) assertEquals(Math.abs(n)==1,SimplicialChainMap.identity(circle()).scale(z(n)).isQuasiIsomorphism());
    }
    @Test public void equalityKeepsDefiningMapsAndFullPairsEvenForEqualConeHomology() {
        IntegralChainMappingCone a=cone(intervalMap(1,1,1)),b=cone(SimplicialChainMap.identity(edge())); assertNotEquals(a,b); assertEquals(a.homologyTypes(),b.homologyTypes());
        assertEquals(a,cone(a.map())); assertEquals(a.hashCode(),cone(a.map()).hashCode());
        IntegralChainMappingCone relabel=cone(SimplicialChainMap.identity(abs(complex(new int[]{7})))); assertNotEquals(cone(SimplicialChainMap.identity(points(1))),relabel);
        RelativeSimplicialComplex based=new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{0})); assertNotEquals(b,cone(SimplicialChainMap.identity(based)));
    }
    @Test public void emptyEndpointsAndFilteredPairsRetainZeroShapesAndFormalDegreeSlots() {
        IntegralChainMappingCone empty=cone(SimplicialChainMap.identity(points(0))); assertEquals(-1,empty.dimension()); assertTrue(empty.isAcyclic()); assertEquals(Collections.emptyList(),empty.homologyTypes()); assertEquals(Collections.emptyList(),empty.boundaryMatrices());
        IntegralChainMappingCone shifted=cone(SimplicialChainMap.zero(points(1),points(0))); assertEquals(1,shifted.dimension()); assertEquals(IntegerMatrix.zero(0,1),shifted.boundaryMatrix(z(1))); assertEquals(IntegerMatrix.zero(1,0),shifted.boundaryMatrix(z(2))); assertEquals(AbelianGroupType.Z,shifted.homologyType(z(1))); segments(shifted,0);
        IntegralChainMappingCone unshifted=cone(SimplicialChainMap.zero(points(0),points(1))); assertEquals(0,unshifted.dimension()); assertEquals(AbelianGroupType.Z,unshifted.homologyType(z(0)));
        int[] labels=new int[10]; for(int i=0;i<10;i++) labels[i]=i; RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(complex(labels)); IntegralChainMappingCone filtered=cone(SimplicialChainMap.identity(diagonal));
        assertEquals(10,filtered.dimension()); assertEquals(11,filtered.boundaryMatrices().size()); assertTrue(filtered.isAcyclic()); assertEquals(Collections.nCopies(11,AbelianGroupType.ZERO),filtered.homologyTypes());
    }
    @Test public void degreesRejectNegativeAndPreserveRetainedNegativeEndpointPresentations() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(points(1)));
        for(Runnable action : Arrays.<Runnable>asList(() -> c.chainRank(z(-1)),() -> c.boundaryMatrix(z(-1)),() -> c.homology(z(-1)),() -> c.cohomology(z(-1)),() -> c.inclusionMatrix(z(-1)),() -> c.projectionMatrix(z(-1)),() -> c.inclusionHomologyMap(z(-1)),() -> c.projectionCohomologyMap(z(-1)),() -> c.longExactSegment(z(-1)),() -> c.longExactCohomologySegment(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        BigInteger huge=BigInteger.ONE.shiftLeft(100); assertEquals(AbelianGroupType.ZERO,c.homologyType(huge)); assertEquals(IntegerMatrix.zero(0,0),c.boundaryMatrix(huge));
        assertEquals(IntegerMatrix.zero(0,1),c.projectionHomologyMap(z(0)).target().relations());
        assertEquals(IntegerMatrix.zero(0,0),c.projectionCohomologyMap(z(0)).source().relations());
    }
    @Test public void combinedRankBoundsApplyAfterQuotientFiltering() {
        for(int count : new int[]{255,256}) {
            RelativeSimplicialComplex pair=abs(points(count).ambient().union(complex(new int[]{0,1}))); IntegralChainMappingCone c=cone(SimplicialChainMap.identity(pair));
            if(count==255) { assertEquals(z(256),c.chainRank(z(1))); assertEquals(256,c.boundaryMatrix(z(1)).columns()); }
            else for(Runnable action : Arrays.<Runnable>asList(() -> c.chainRank(z(1)),() -> c.boundaryMatrices(),() -> c.isAcyclic(),() -> c.longExactSegment(z(0)))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total"));
        }
    }
    @Test public void nativeConesReturnActualWrappersAndSerializedScalarAndFlatFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainMap f=pointMap(matrix(new long[]{2})); IntegralChainMappingCone c=cone(f);
        IAlgebraItem<IntegralChainMappingCone> item=math.chainMaps.algebra().buildAlgebraItem(f).performAlgebraTransfer("ChainCone.from-map"); assertSame(math.chainCones.algebra(),item.getAlgebra()); assertEquals(c,item.perform().getResult());
        assertSame(math.integralHomology.algebra(),item.performUnsafeOperation("homology",z(0)).getAlgebra());
        assertSame(math.naturals.algebra(),item.performLeftProjectionOperation("chain-rank",z(0)).getAlgebra());
        for(IAlgebraItem<?> m : item.performUnsafeFlatOperation("long-exact-segment",z(0))) assertSame(math.abelianHomomorphisms.algebra(),m.getAlgebra());
        List<IAlgebraFlow<?>> flows=Arrays.asList(
                math.flow(math.chainMaps,Collections.singletonList(f)).<IntegralChainMappingCone>performAlgebraTransfer("ChainCone.from-map").performFlatAlgebraTransfer("homology-types"),
                math.flow(math.chainCones,Collections.singletonList(c)).performFlatAlgebraUnsafe("long-exact-cohomology-segment",z(0)),
                math.flow(math.chainCones,Collections.singletonList(c)).performFlatAlgebraTransfer("boundary-matrices"),
                math.flow(math.chainCones,Collections.singletonList(c)).<IntegralHomology,BigInteger>performAlgebraUnsafe("homology",z(0)).<IntegerVector>performFlatAlgebraTransfer("generators"),
                math.flow(math.chainMaps,Collections.singletonList(f)).performAlgebraTransfer("is-quasi-isomorphism"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("[1]"),flows.get(3).collect()); assertEquals(Collections.singletonList("false"),flows.get(4).collect());
        assertThrows(UnsupportedOperationException.class,c.boundaryMatrices()::clear); assertThrows(UnsupportedOperationException.class,c.homologyTypes()::clear); assertThrows(UnsupportedOperationException.class,() -> c.longExactSegment(z(0)).clear());
    }
    @Test public void fullDegreeListsAndAcyclicityShareOneBudgetAcrossSuccessfulScalarDegrees() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(points(120)));
        for(int n=0;n<=1;n++) { assertTrue(c.homology(z(n)).isAcyclic()); assertTrue(c.cohomology(z(n)).isAcyclic()); }
        for(Runnable action : Arrays.<Runnable>asList(c::homologyTypes,c::cohomologyTypes,c::isAcyclic,() -> c.map().isQuasiIsomorphism()))
            assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("5000000"));
    }
    @Test public void exactSegmentsShareBudgetsAcrossIndividuallySuccessfulMaps() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(points(70)));
        assertNotNull(c.map().homologyMap(z(0))); assertNotNull(c.inclusionHomologyMap(z(0))); assertNotNull(c.projectionHomologyMap(z(0)));
        assertNotNull(c.inclusionCohomologyMap(z(0))); assertNotNull(c.map().cohomologyMap(z(0))); assertNotNull(c.projectionCohomologyMap(z(1)));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> c.longExactSegment(z(0))).getMessage().contains("5000000"));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> c.longExactCohomologySegment(z(0))).getMessage().contains("5000000"));
    }
}
