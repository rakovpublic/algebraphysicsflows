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

public class NativeRelativeHomologyTest {
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
    private static RelativeSimplicialComplex interval() { return new RelativeSimplicialComplex(complex(new int[]{0,1}),complex(new int[]{0},new int[]{1})); }
    private static FiniteSimplicialComplex plane() {
        return complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void exact(AbelianGroupHomomorphism first,AbelianGroupHomomorphism second) {
        assertEquals(first.target(),second.source()); assertTrue(second.compose(first).isZero());
        for(AbelianGroupElement generator : second.kernelInclusion().generatorImages()) assertTrue(first.hasPreimage(generator));
    }
    private static void chainIdentities(RelativeSimplicialComplex pair) {
        for(int k=0;k<=pair.ambient().dimension()+1;k++) {
            BigInteger degree=z(k); IntegerMatrix p=pair.projectionMatrix(degree),j=pair.inclusionMatrix(degree),l=pair.liftMatrix(degree);
            assertEquals(IntegerMatrix.identity(p.rows()),p.multiply(l)); assertEquals(IntegerMatrix.zero(p.rows(),j.columns()),p.multiply(j));
            assertEquals(IntegerMatrix.identity(p.columns()),j.multiply(j.transpose()).add(l.multiply(p)));
            if(k>0) {
                assertEquals(pair.boundaryMatrix(degree).multiply(p),pair.projectionMatrix(z(k-1)).multiply(pair.ambient().integralBoundaryMatrix(degree)));
                assertEquals(pair.ambient().integralBoundaryMatrix(degree).multiply(j),pair.inclusionMatrix(z(k-1)).multiply(pair.subcomplex().integralBoundaryMatrix(degree)));
                assertEquals(IntegerMatrix.zero(pair.boundaryMatrix(z(k-1)).rows(),pair.boundaryMatrix(degree).columns()),pair.boundaryMatrix(z(k-1)).multiply(pair.boundaryMatrix(degree)));
            }
        }
    }
    @Test public void intervalConnectingMapIsTheOrientedDifferenceOfEndpoints() {
        RelativeSimplicialComplex pair=interval(); IntegralHomology h=pair.homology(z(1)); AbelianGroupHomomorphism delta=pair.connectingHomology(z(1));
        assertEquals(AbelianGroupType.ZERO,pair.homologyType(z(0))); assertEquals(AbelianGroupType.Z,h.type());
        assertEquals(IntegerMatrix.zero(0,1),pair.boundaryMatrix(z(1))); assertEquals(m(new long[]{-1},new long[]{1}),pair.connectingChainMatrix(z(1)));
        assertEquals(m(new long[]{-1},new long[]{1}),delta.smithMatrix()); assertTrue(delta.isInjective()); assertFalse(delta.isSurjective());
        assertEquals(AbelianGroupType.Z,delta.cokernel().type());
        IntegerVector cycle=h.generators().get(0),lift=pair.liftMatrix(z(1)).multiply(cycle),boundary=pair.ambient().integralBoundaryMatrix(z(1)).multiply(lift);
        assertEquals(IntegralHomology.atDegree(pair.subcomplex(),z(0)).classOf(boundary),delta.apply(h.classOf(cycle)));
        assertNotEquals(IntegerMatrix.zero(2,1),pair.ambient().integralBoundaryMatrix(z(1)).multiply(pair.liftMatrix(z(1)))); chainIdentities(pair);
    }
    @Test public void disksRelativeToTheirBoundaryHaveOneTopGeneratorAndConnectingIsomorphisms() {
        for(int n=1;n<=4;n++) {
            int[] vertices=new int[n+1]; for(int i=0;i<=n;i++) vertices[i]=i;
            List<int[]> facets=new ArrayList<>(); for(int omit=0;omit<=n;omit++) { int[] face=new int[n]; int j=0; for(int i=0;i<=n;i++) if(i!=omit) face[j++]=i; facets.add(face); }
            RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(vertices),complex(facets.toArray(new int[0][])));
            for(int k=0;k<=n+1;k++) assertEquals(k==n?AbelianGroupType.Z:AbelianGroupType.ZERO,pair.homologyType(z(k)));
            assertEquals(z((n&1)==0?1:-1),pair.eulerCharacteristic()); assertEquals(n,pair.dimension());
            if(n>=2) assertTrue(pair.connectingHomology(z(n)).isIsomorphism());
            IntegralHomology h=pair.homology(z(n)),target=IntegralHomology.atDegree(pair.subcomplex(),z(n-1));
            IntegerVector cycle=h.generators().get(0);
            assertEquals(target.classOf(pair.connectingChainMatrix(z(n)).multiply(cycle)),pair.connectingHomology(z(n)).apply(h.classOf(cycle))); chainIdentities(pair);
        }
    }
    @Test public void quotientingOneVertexPreservesProjectivePlaneTorsion() {
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(plane(),complex(new int[]{0}));
        assertEquals(AbelianGroupType.ZERO,pair.homologyType(z(0))); assertEquals(AbelianGroupType.cyclic(z(2)),pair.homologyType(z(1)));
        assertTrue(pair.quotientHomology(z(1)).isIsomorphism()); IntegralHomology h=pair.homology(z(1)); IntegerVector cycle=h.generators().get(0);
        assertEquals(z(2),h.classOf(cycle).order()); assertFalse(h.isBoundary(cycle)); assertTrue(h.isBoundary(cycle.scale(z(2))));
        assertEquals(cycle.scale(z(2)),h.incomingBoundary().multiply(h.boundingChain(cycle.scale(z(2))))); chainIdentities(pair);
    }
    @Test public void projectivePlaneConnectingImageHasIndexTwoInItsOneSkeleton() {
        FiniteSimplicialComplex plane=plane(); RelativeSimplicialComplex pair=new RelativeSimplicialComplex(plane,new FiniteSimplicialComplex(plane.simplices(1)));
        assertEquals(AbelianGroupType.free(z(10)),pair.homologyType(z(2))); assertEquals(AbelianGroupType.ZERO,pair.homologyType(z(1)));
        AbelianGroupHomomorphism delta=pair.connectingHomology(z(2)); assertTrue(delta.isInjective()); assertFalse(delta.isSurjective());
        assertEquals(AbelianGroupType.cyclic(z(2)),delta.cokernel().type()); exact(delta,pair.inclusionHomology(z(1)));
    }
    @Test public void longExactSegmentsHaveEqualIntegralImagesAndKernels() {
        List<RelativeSimplicialComplex> pairs=Arrays.asList(interval(),new RelativeSimplicialComplex(complex(new int[]{0,1,2}),complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})),
                new RelativeSimplicialComplex(plane(),complex(new int[]{0})),RelativeSimplicialComplex.absolute(plane()),RelativeSimplicialComplex.diagonal(plane()),RelativeSimplicialComplex.absolute(complex()));
        for(RelativeSimplicialComplex pair : pairs) for(int k=0;k<=pair.ambient().dimension()+1;k++) {
            List<AbelianGroupHomomorphism> segment=pair.longExactSegment(z(k)); assertEquals(3,segment.size());
            assertEquals(pair.inclusionHomology(z(k)),segment.get(0)); assertEquals(pair.quotientHomology(z(k)),segment.get(1)); assertEquals(pair.connectingHomology(z(k)),segment.get(2));
            exact(segment.get(0),segment.get(1)); exact(segment.get(1),segment.get(2));
            if(k>0) exact(segment.get(2),pair.inclusionHomology(z(k-1))); else assertTrue(segment.get(2).target().type().isTrivial());
        }
    }
    @Test public void all1024GraphAndVertexPairsMatchIndependentRelativeBettiNumbers() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int mask=0;mask<64;mask++) {
            List<int[]> facets=new ArrayList<>(); for(int i=0;i<4;i++) facets.add(new int[]{i}); boolean[][] connected=new boolean[4][4];
            for(int i=0;i<4;i++) connected[i][i]=true;
            for(int e=0;e<6;e++) if((mask&(1<<e))!=0) { facets.add(edges[e]); connected[edges[e][0]][edges[e][1]]=connected[edges[e][1]][edges[e][0]]=true; }
            for(int k=0;k<4;k++) for(int i=0;i<4;i++) for(int j=0;j<4;j++) connected[i][j]|=connected[i][k]&&connected[k][j];
            FiniteSimplicialComplex graph=complex(facets.toArray(new int[0][]));
            for(int selected=0;selected<16;selected++) {
                List<int[]> points=new ArrayList<>(); for(int i=0;i<4;i++) if((selected&(1<<i))!=0) points.add(new int[]{i}); int components=0,touched=0;
                for(int i=0;i<4;i++) { boolean first=true; for(int j=0;j<i;j++) if(connected[i][j]) first=false; if(!first) continue; components++;
                    for(int j=0;j<4;j++) if(connected[i][j] && (selected&(1<<j))!=0) { touched++; break; }
                }
                RelativeSimplicialComplex pair=new RelativeSimplicialComplex(graph,complex(points.toArray(new int[0][])));
                int b0=components-touched,b1=Integer.bitCount(mask)-4+components+Integer.bitCount(selected)-touched;
                assertEquals(AbelianGroupType.free(z(b0)),pair.homologyType(z(0))); assertEquals(AbelianGroupType.free(z(b1)),pair.homologyType(z(1)));
                assertEquals(z(b0-b1),pair.eulerCharacteristic()); chainIdentities(pair);
            }
        }
    }
    @Test public void relativeBasesAndBoundarySignsUseIncreasingVertexOrder() {
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(new int[]{2,0,1}),complex(new int[]{1,0}));
        assertEquals(Collections.singletonList(FiniteSet.of(z(2))),pair.simplexBasis(z(0)));
        assertEquals(Arrays.asList(FiniteSet.of(z(0),z(2)),FiniteSet.of(z(1),z(2))),pair.simplexBasis(z(1)));
        assertEquals(m(new long[]{1,1}),pair.boundaryMatrix(z(1))); assertEquals(m(new long[]{-1},new long[]{1}),pair.boundaryMatrix(z(2)));
        assertEquals(m(new long[]{-1,0},new long[]{0,-1}),pair.connectingChainMatrix(z(1))); chainIdentities(pair);
    }
    @Test public void absoluteDiagonalEmptyAndHugeDegreesRetainUnreducedConventions() {
        FiniteSimplicialComplex plane=plane(); RelativeSimplicialComplex absolute=RelativeSimplicialComplex.absolute(plane),diagonal=RelativeSimplicialComplex.diagonal(plane),empty=RelativeSimplicialComplex.absolute(complex());
        for(int k=0;k<=3;k++) {
            assertEquals(IntegralHomology.atDegree(plane,z(k)),absolute.homology(z(k))); assertTrue(absolute.quotientHomology(z(k)).isIsomorphism());
            assertEquals(AbelianGroupType.ZERO,diagonal.homologyType(z(k))); assertTrue(diagonal.inclusionHomology(z(k)).isIsomorphism());
        }
        assertEquals(-1,diagonal.dimension()); assertEquals(z(0),diagonal.eulerCharacteristic()); assertTrue(empty.boundaryMatrices().isEmpty()); assertTrue(empty.homologyDegrees().isEmpty());
        assertEquals(AbelianGroupType.ZERO,absolute.homologyType(BigInteger.TEN.pow(100))); assertTrue(absolute.connectingHomology(BigInteger.TEN.pow(100)).isIsomorphism());
        assertEquals(IntegerMatrix.zero(0,6),absolute.connectingChainMatrix(z(0))); assertTrue(absolute.connectingHomology(z(0)).target().type().isTrivial());
    }
    @Test public void invalidPairsNegativeDegreesAndMutationAreRejected() {
        RelativeSimplicialComplex pair=interval();
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new RelativeSimplicialComplex(pair.subcomplex(),pair.ambient()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> pair.boundaryMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> pair.homology(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> pair.simplexBasis(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> pair.longExactSegment(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> pair.connectingHomology(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> pair.projectionMatrix(z(-1)));
        assertThrows(UnsupportedOperationException.class,() -> pair.simplexBasis(z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> pair.boundaryMatrices().clear());
        assertThrows(UnsupportedOperationException.class,() -> pair.homologyDegrees().clear()); assertThrows(UnsupportedOperationException.class,() -> pair.longExactSegment(z(1)).clear());
        assertEquals(interval(),pair); assertEquals(interval().hashCode(),pair.hashCode()); assertNotEquals(RelativeSimplicialComplex.absolute(pair.ambient()),pair);
    }
    @Test public void limitsApplyToRequiredBasesAfterRelativeCancellation() {
        List<int[]> points=new ArrayList<>(); for(int i=0;i<4097;i++) points.add(new int[]{i});
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialComplex.diagonal(complex(points.toArray(new int[0][]))));
        FiniteSimplicialComplex many=complex(points.subList(0,257).toArray(new int[0][])); RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(many);
        assertEquals(AbelianGroupType.ZERO,diagonal.homologyType(z(0))); assertEquals(0,diagonal.simplexCount(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialComplex.absolute(many).homology(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> diagonal.projectionMatrix(z(0)));
        RelativeSimplicialComplex costly=RelativeSimplicialComplex.absolute(complex(points.subList(0,100).toArray(new int[0][])));
        assertEquals(AbelianGroupType.free(z(100)),costly.homologyType(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.quotientHomology(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.longExactSegment(z(0)));
        // All degrees fit separately, but a flat list must share its work budget across degrees.
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int omit=0;omit<8;omit++) {
            List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); facets.add(new FiniteSet<>(face));
        }
        for(int i=0;i<140;i++) facets.add(FiniteSet.of(100+i));
        RelativeSimplicialComplex aggregate=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(facets));
        for(int k=0;k<=6;k++) assertNotNull(aggregate.homology(z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,aggregate::homologyDegrees); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,aggregate::homologyTypes);
    }
    @Test public void nativeScalarAndFlatOperationsUseActualResultWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=interval();
        IAlgebraItem<RelativeSimplicialComplex> item=math.complexes.algebra().buildAlgebraItem(pair.ambient()).performCustomResultOperation("RelativeComplex.from-complexes",pair.subcomplex());
        assertSame(math.relativeComplexes.algebra(),item.getAlgebra()); assertEquals(pair,item.getResult());
        assertSame(math.naturals.algebra(),item.performLeftProjectionOperation("simplex-count",z(1)).getAlgebra());
        assertSame(math.integralHomology.algebra(),item.performUnsafeOperation("homology",z(1)).getAlgebra());
        assertSame(math.simplicialMaps.algebra(),item.performAlgebraTransfer("inclusion").getAlgebra());
        for(IAlgebraItem<AbelianGroupHomomorphism> map : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-segment",z(1))) assertSame(math.abelianHomomorphisms.algebra(),map.getAlgebra());
        for(IAlgebraItem<FiniteSet<BigInteger>> simplex : item.<FiniteSet<BigInteger>,BigInteger>performUnsafeFlatOperation("simplex-basis",z(1))) assertSame(math.integerSets.algebra(),simplex.getAlgebra());
    }
    @Test public void serializedRelativeFlowsExposeConnectingMatricesAndHomologyTypes() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=interval();
        IAlgebraFlow<IntegerMatrix> flow=math.flow(math.complexes,Collections.singletonList(pair.ambient()))
                .<RelativeSimplicialComplex>performCustomResultOperation("RelativeComplex.from-complexes",pair.subcomplex())
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("connecting-homology",z(1)).<IntegerMatrix>performAlgebraTransfer("smith-matrix");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("ZMatrix(2x1)[[-1], [1]]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("0","1"),math.flow(math.relativeComplexes,Collections.singletonList(pair)).<AbelianGroupType>performFlatAlgebraTransfer("homology-types")
                .<BigInteger>performAlgebraTransfer("free-rank").collect());
    }
}
