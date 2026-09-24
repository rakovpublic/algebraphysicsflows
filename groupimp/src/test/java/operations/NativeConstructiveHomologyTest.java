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

public class NativeConstructiveHomologyTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) {
        BigInteger[] entries=new BigInteger[values.length]; for(int i=0;i<values.length;i++) entries[i]=z(values[i]); return new IntegerVector(entries);
    }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] entries=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { entries[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) entries[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(entries);
    }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int[] facet : facets) { List<Integer> vertices=new ArrayList<>(); for(int vertex : facet) vertices.add(vertex); faces.add(new FiniteSet<>(vertices)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static IntegralHomology cyclic(long order) { return new IntegralHomology(IntegerMatrix.zero(0,1),m(new long[]{order})); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void witnesses(IntegralHomology h) {
        assertEquals(h.incomingBoundary(),h.cycleMatrix().multiply(h.boundaryCoordinates()));
        assertEquals(IntegerMatrix.zero(h.outgoingBoundary().rows(),h.cycleRank()),h.outgoingBoundary().multiply(h.cycleMatrix()));
        for(IntegerVector cycle : h.cycleBasis()) { assertTrue(h.isCycle(cycle)); assertEquals(cycle,h.fromCycleCoordinates(h.cycleCoordinates(cycle))); }
        for(IntegerVector boundary : h.boundaryBasis()) {
            assertTrue(h.isCycle(boundary)); assertTrue(h.isBoundary(boundary)); assertTrue(h.classOf(boundary).isZero());
            assertEquals(boundary,h.incomingBoundary().multiply(h.boundingChain(boundary)));
        }
        List<IntegerVector> generators=h.generators(); List<AbelianGroupElement> classes=h.group().smithGenerators();
        assertEquals(classes.size(),generators.size());
        for(int i=0;i<generators.size();i++) {
            assertTrue(h.isCycle(generators.get(i))); assertEquals(classes.get(i),h.classOf(generators.get(i)));
            assertEquals(classes.get(i),h.classOf(h.representative(classes.get(i))));
        }
    }
    @Test public void all336SmallRankOneComplexesMatchPrimitiveLatticeArithmetic() {
        for(int a=-3;a<=3;a++) for(int b=-3;b<=3;b++) if(a!=0 || b!=0) {
            int gcd=z(a).gcd(z(b)).intValueExact(); IntegerVector primitive=v(-b/gcd,a/gcd);
            for(int q=-3;q<=3;q++) {
                IntegralHomology h=new IntegralHomology(m(new long[]{a,b}),m(new long[]{-b/gcd*q},new long[]{a/gcd*q}));
                assertEquals(q==0?AbelianGroupType.Z:AbelianGroupType.cyclic(z(q).abs()),h.type());
                assertEquals(1,h.cycleRank()); assertEquals(q==0?0:1,h.boundaryRank());
                for(int n=-4;n<=4;n++) {
                    IntegerVector chain=primitive.scale(z(n)); boolean boundary=q==0?n==0:n%q==0;
                    assertTrue(h.isCycle(chain)); assertEquals(boundary,h.isBoundary(chain)); assertEquals(boundary,h.classOf(chain).isZero());
                    assertEquals(h.classOf(chain),h.classOf(h.representative(h.classOf(chain))));
                    if(boundary) assertEquals(chain,h.incomingBoundary().multiply(h.boundingChain(chain)));
                    else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.boundingChain(chain));
                }
                if(q!=0) assertEquals(z(q).abs(),h.classOf(primitive).order()); witnesses(h);
            }
        }
    }
    @Test public void nonPrimitiveBoundariesProduceTorsionAndBoundingWitnesses() {
        IntegralHomology h=new IntegralHomology(m(new long[]{1,1,0}),m(new long[]{2,0},new long[]{-2,0},new long[]{0,3}));
        assertEquals(AbelianGroupType.cyclic(z(6)),h.type()); assertEquals(2,h.cycleRank()); assertEquals(2,h.boundaryRank());
        IntegerVector cycle=v(1,-1,1); AbelianGroupElement value=h.classOf(cycle);
        assertEquals(z(6),value.order()); assertFalse(h.isBoundary(cycle));
        assertEquals(cycle.scale(z(6)),h.incomingBoundary().multiply(h.boundingChain(cycle.scale(z(6)))));
        assertEquals(value,h.classOf(cycle.add(h.incomingBoundary().multiply(v(7,-5))))); witnesses(h);
        IntegralHomology mixed=new IntegralHomology(IntegerMatrix.zero(0,3),m(new long[]{2,0},new long[]{0,6},new long[]{0,0}));
        assertEquals(new AbelianGroupType(z(1),Arrays.asList(z(2),z(6))),mixed.type()); witnesses(mixed);
    }
    @Test public void allFourVertexGraphsHaveIndependentCycleAndComponentWitnesses() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int mask=0;mask<64;mask++) {
            List<int[]> facets=new ArrayList<>(); for(int i=0;i<4;i++) facets.add(new int[]{i}); int count=0;
            boolean[][] reach=new boolean[4][4]; for(int i=0;i<4;i++) reach[i][i]=true;
            for(int i=0;i<6;i++) if((mask&(1<<i))!=0) { facets.add(edges[i]); count++; reach[edges[i][0]][edges[i][1]]=true; reach[edges[i][1]][edges[i][0]]=true; }
            for(int k=0;k<4;k++) for(int i=0;i<4;i++) for(int j=0;j<4;j++) reach[i][j]|=reach[i][k]&&reach[k][j];
            int components=0; for(int i=0;i<4;i++) { boolean first=true; for(int j=0;j<i;j++) if(reach[i][j]) first=false; if(first) components++; }
            FiniteSimplicialComplex graph=complex(facets.toArray(new int[0][])); IntegralHomology h0=IntegralHomology.atDegree(graph,z(0)),h1=IntegralHomology.atDegree(graph,z(1));
            assertEquals(AbelianGroupType.free(z(components)),h0.type()); assertEquals(AbelianGroupType.free(z(count-4+components)),h1.type());
            for(int i=0;i<4;i++) for(int j=0;j<4;j++) {
                BigInteger[] entries={z(0),z(0),z(0),z(0)}; entries[i]=entries[i].add(z(1)); entries[j]=entries[j].subtract(z(1));
                assertEquals(reach[i][j],h0.isBoundary(new IntegerVector(entries)));
            }
            witnesses(h0); witnesses(h1);
        }
    }
    @Test public void projectivePlaneHasAnExplicitOrderTwoCycleAndIntegralFilling() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        IntegralHomology h=IntegralHomology.atDegree(plane,z(1)); assertEquals(AbelianGroupType.cyclic(z(2)),h.type());
        assertEquals(1,h.generators().size()); IntegerVector generator=h.generators().get(0);
        assertFalse(h.isBoundary(generator)); assertEquals(z(2),h.classOf(generator).order());
        assertEquals(generator.scale(z(2)),h.incomingBoundary().multiply(h.boundingChain(generator.scale(z(2)))));
        for(int k=0;k<=3;k++) {
            IntegralHomology degree=IntegralHomology.atDegree(plane,z(k)); assertEquals(plane.integralHomology(z(k)),degree.type()); witnesses(degree);
        }
    }
    @Test public void circleReflectionAndDiskInclusionInduceTheExpectedHomomorphisms() {
        FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}),disk=complex(new int[]{0,1,2});
        IntegralHomology h=IntegralHomology.atDegree(circle,z(1)),filled=IntegralHomology.atDegree(disk,z(1));
        // Swap vertices 0 and 1; columns are images of oriented edges 01,02,12.
        IntegerMatrix reflection=m(new long[]{-1,0,0},new long[]{0,0,1},new long[]{0,1,0});
        AbelianGroupHomomorphism reversal=h.inducedMap(h,reflection);
        assertEquals(AbelianGroupHomomorphism.scaling(h.group(),z(-1)),reversal);
        assertEquals(AbelianGroupHomomorphism.identity(h.group()),reversal.compose(reversal));
        AbelianGroupHomomorphism inclusion=h.inducedMap(filled,IntegerMatrix.identity(3)); assertTrue(inclusion.isZero());
        assertTrue(inclusion.isSurjective()); assertEquals(AbelianGroupType.Z,inclusion.kernel().type());
        for(IntegerVector cycle : h.generators()) assertEquals(filled.classOf(cycle),inclusion.apply(h.classOf(cycle)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> filled.inducedMap(h,IntegerMatrix.identity(3)));
    }
    @Test public void inducedMapsRespectCompositionAndHomologousChanges() {
        IntegralHomology a=cyclic(6),b=cyclic(4),c=cyclic(2);
        IntegerMatrix f=m(new long[]{2}),g=m(new long[]{1});
        AbelianGroupHomomorphism first=a.inducedMap(b,f),second=b.inducedMap(c,g);
        assertEquals(second.compose(first),a.inducedMap(c,g.multiply(f)));
        assertEquals(first,a.inducedMap(b,m(new long[]{6})));
        for(int x=-10;x<=10;x++) assertEquals(b.classOf(f.multiply(v(x))),first.apply(a.classOf(v(x))));
        assertEquals(AbelianGroupHomomorphism.identity(a.group()),a.inducedMap(a,IntegerMatrix.identity(1)));
        assertTrue(a.inducedMap(b,IntegerMatrix.zero(1,1)).isZero());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.inducedMap(b,m(new long[]{1})));
    }
    @Test public void projectionUsesCycleCoordinatesAndRepresentativeIsNotAnAdditiveSection() {
        IntegralHomology h=cyclic(2); AbelianGroupHomomorphism projection=h.projection();
        assertTrue(projection.isSurjective()); assertEquals(AbelianGroupType.Z,projection.source().type()); assertEquals(h.group(),projection.target());
        for(int i=-4;i<=4;i++) assertEquals(h.classOf(v(i)),projection.apply(projection.source().project(v(i))));
        AbelianGroupElement one=h.classOf(v(1));
        assertNotEquals(h.representative(one).add(h.representative(one)),h.representative(one.add(one)));
        assertTrue(h.isBoundary(h.representative(one).scale(z(2))));
    }
    @Test public void invalidDifferentialsCyclesDimensionsAndForeignClassesAreRejected() {
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralHomology(m(new long[]{1}),m(new long[]{1})));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralHomology(IntegerMatrix.zero(0,2),IntegerMatrix.zero(1,0)));
        IntegralHomology h=new IntegralHomology(m(new long[]{1,1}),IntegerMatrix.zero(2,0));
        assertFalse(h.isCycle(v(1,0))); assertFalse(h.isBoundary(v(1,0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.classOf(v(1,0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.cycleCoordinates(v(1,0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.boundingChain(v(1,0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.isCycle(v(0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.isBoundary(v(0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.fromCycleCoordinates(v(0,0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.representative(cyclic(2).group().zero()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.inducedMap(h,m(new long[]{1,0},new long[]{0,0})));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.inducedMap(h,IntegerMatrix.identity(1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralHomology.atDegree(complex(),z(-1)));
    }
    @Test public void emptyShapesAndDegreesRetainChainCoordinatesAndImmutability() {
        IntegralHomology empty=new IntegralHomology(IntegerMatrix.zero(3,0),IntegerMatrix.zero(0,2));
        assertEquals(AbelianGroupType.ZERO,empty.type()); assertTrue(empty.isAcyclic()); assertTrue(empty.generators().isEmpty());
        assertEquals(v(0,0),empty.boundingChain(v())); assertEquals(v(),empty.representative(empty.group().zero())); witnesses(empty);
        IntegralHomology same=new IntegralHomology(IntegerMatrix.zero(3,0),IntegerMatrix.zero(0,2));
        assertEquals(empty,same); assertEquals(empty.hashCode(),same.hashCode());
        assertNotEquals(empty,new IntegralHomology(IntegerMatrix.zero(0,0),IntegerMatrix.zero(0,0)));
        assertTrue(IntegralHomology.atDegree(complex(new int[]{1}),BigInteger.TEN.pow(100)).isAcyclic());
        IntegralHomology above=IntegralHomology.atDegree(complex(new int[]{1}),z(1)); assertEquals(IntegerMatrix.zero(1,0),above.outgoingBoundary());
        assertThrows(UnsupportedOperationException.class,() -> cyclic(3).generators().clear());
        assertThrows(UnsupportedOperationException.class,() -> cyclic(3).cycleBasis().clear());
    }
    @Test public void sharedLimitsFailExplicitlyInsteadOfReturningFalseOrPartialWitnesses() {
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new IntegralHomology(IntegerMatrix.identity(180),IntegerMatrix.zero(180,180)));
        List<int[]> points=new ArrayList<>(); for(int i=0;i<257;i++) points.add(new int[]{i});
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralHomology.atDegree(complex(points.toArray(new int[0][])),z(0)));
        IntegralHomology large=new IntegralHomology(IntegerMatrix.zero(0,180),IntegerMatrix.zero(180,0));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.inducedMap(large,IntegerMatrix.identity(180)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,large::generators);
    }
    @Test public void nativeWitnessOperationsRetainTheActualCarrierWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralHomology h=cyclic(6);
        IAlgebraItem<IntegralHomology> item=math.integerMatrices.algebra().buildAlgebraItem(h.outgoingBoundary())
                .performCustomResultOperation("IntegralHomology.from-boundaries",h.incomingBoundary());
        assertSame(math.integralHomology.algebra(),item.getAlgebra());
        IAlgebraItem<AbelianGroupElement> value=item.performUnsafeOperation("class-of",v(2));
        assertSame(math.abelianGroupElements.algebra(),value.getAlgebra());
        IAlgebraItem<IntegerVector> representative=item.performUnsafeOperation("representative",value.getResult());
        assertSame(math.integerVectors.algebra(),representative.getAlgebra()); assertEquals(v(2),representative.getResult());
        IAlgebraItem<IntegerVector> filling=item.performLeftProjectionOperation("bounding-chain",v(12));
        assertSame(math.integerVectors.algebra(),filling.getAlgebra()); assertEquals(v(2),filling.getResult());
        for(IAlgebraItem<IntegerVector> cycle : item.<IntegerVector>performAlgebraFlatTransfer("generators")) assertSame(math.integerVectors.algebra(),cycle.getAlgebra());
        IAlgebraItem<AbelianGroupHomomorphism> map=item.performUnsafeOperation("induced-map",new Pair<>(h,m(new long[]{2})));
        assertSame(math.abelianHomomorphisms.algebra(),map.getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.integralHomology.mapInputs).validate(new Pair<>(h,"wrong")));
    }
    @Test public void serializedSimplicialFlowsProduceRepresentativesAndComposableMaps() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2});
        IAlgebraFlow<IntegerVector> flow=math.flow(math.complexes,Collections.singletonList(circle))
                .<IntegralHomology,BigInteger>performAlgebraUnsafe("IntegralHomology.at-degree",z(1))
                .<IntegerVector>performFlatAlgebraTransfer("generators");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("[1, -1, 1]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        IntegralHomology h=cyclic(6);
        assertEquals(Collections.singletonList("[4]"),math.flow(math.integralHomology,Collections.singletonList(h))
                .<AbelianGroupHomomorphism,Pair<IntegralHomology,IntegerMatrix>>performAlgebraUnsafe("induced-map",new Pair<>(h,m(new long[]{2})))
                .<AbelianGroupElement>performLeftProjectionOperation("apply",h.classOf(v(2)))
                .<IntegerVector>performAlgebraTransfer("smith-coordinates").collect());
    }
}
