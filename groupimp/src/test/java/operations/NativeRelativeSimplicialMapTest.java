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

public class NativeRelativeSimplicialMapTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int[] facet : facets) { List<Integer> values=new ArrayList<>(); for(int vertex : facet) values.add(vertex); faces.add(new FiniteSet<>(values)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialMap map(FiniteSimplicialComplex source,FiniteSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0;
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source).members()) vertices.put(vertex,z(images[i++]));
        return new FiniteSimplicialMap(source,target,vertices);
    }
    private static RelativeSimplicialMap map(RelativeSimplicialComplex source,RelativeSimplicialComplex target,int... images) {
        return new RelativeSimplicialMap(source,target,map(source.ambient(),target.ambient(),images));
    }
    private static RelativeSimplicialComplex interval() { return new RelativeSimplicialComplex(complex(new int[]{0,1}),complex(new int[]{0},new int[]{1})); }
    private static FiniteSimplicialComplex circle() { return complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] entries=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { entries[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) entries[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(entries);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void chainIdentities(RelativeSimplicialMap f) {
        for(int k=0;k<=Math.max(f.source().ambient().dimension(),f.target().ambient().dimension())+1;k++) {
            BigInteger degree=z(k); IntegerMatrix relative=f.chainMatrix(degree),ambient=f.ambientMap().chainMatrix(degree),sub=f.subcomplexMap().chainMatrix(degree);
            assertEquals(f.target().projectionMatrix(degree).multiply(ambient),relative.multiply(f.source().projectionMatrix(degree)));
            assertEquals(ambient.multiply(f.source().inclusionMatrix(degree)),f.target().inclusionMatrix(degree).multiply(sub));
            assertEquals(relative,f.target().projectionMatrix(degree).multiply(ambient).multiply(f.source().liftMatrix(degree)));
            if(k>0) assertEquals(f.target().boundaryMatrix(degree).multiply(relative),f.chainMatrix(z(k-1)).multiply(f.source().boundaryMatrix(degree)));
        }
    }
    private static void naturality(RelativeSimplicialMap f,int degree) {
        BigInteger k=z(degree); List<AbelianGroupHomomorphism> source=f.source().longExactSegment(k),target=f.target().longExactSegment(k),vertical=f.longExactMaps(k);
        assertEquals(4,vertical.size());
        for(int i=0;i<3;i++) assertEquals(vertical.get(i+1).compose(source.get(i)),target.get(i).compose(vertical.get(i)));
        assertEquals(f.subcomplexHomologyMap(k),vertical.get(0)); assertEquals(f.ambientHomologyMap(k),vertical.get(1)); assertEquals(f.homologyMap(k),vertical.get(2));
        if(degree==0) assertTrue(vertical.get(3).source().type().isTrivial());
    }
    private static BigInteger determinant(IntegerMatrix a) {
        if(a.rows()==0) return BigInteger.ONE; BigInteger result=BigInteger.ZERO;
        for(int c=0;c<a.columns();c++) {
            BigInteger[][] minor=new BigInteger[a.rows()-1][a.columns()-1];
            for(int r=1;r<a.rows();r++) { int j=0; for(int k=0;k<a.columns();k++) if(k!=c) minor[r-1][j++]=a.get(r,k); }
            BigInteger term=a.get(0,c).multiply(determinant(new IntegerMatrix(minor))); result=(c&1)==0?result.add(term):result.subtract(term);
        }
        return result;
    }
    @Test public void all256TetrahedronPairMapsMatchIndependentTopOrientationDeterminants() {
        FiniteSimplicialComplex tetrahedron=complex(new int[]{0,1,2,3}); RelativeSimplicialComplex pair=new RelativeSimplicialComplex(tetrahedron,tetrahedron.skeleton(2));
        for(int code=0;code<256;code++) {
            int digits=code; int[] images=new int[4]; for(int i=0;i<4;i++) { images[i]=digits%4; digits/=4; }
            RelativeSimplicialMap f=map(pair,pair,images); BigInteger degree=determinant(f.ambientMap().chainMatrix(z(0)));
            assertEquals(new IntegerMatrix(new BigInteger[][]{{degree}}),f.chainMatrix(z(3)));
            assertEquals(new IntegerMatrix(new BigInteger[][]{{degree}}),f.homologyMap(z(3)).smithMatrix()); chainIdentities(f); naturality(f,3);
        }
    }
    @Test public void all27TrianglePairMapsAnd729CompositionsPreserveRelativeChainsAndHomology() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); RelativeSimplicialComplex pair=new RelativeSimplicialComplex(triangle,triangle.skeleton(0));
        List<RelativeSimplicialMap> maps=new ArrayList<>(); List<AbelianGroupHomomorphism> homology=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) {
            RelativeSimplicialMap f=map(pair,pair,a,b,c); maps.add(f); homology.add(f.homologyMap(z(1))); chainIdentities(f); naturality(f,0); naturality(f,1);
        }
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) {
            RelativeSimplicialMap first=maps.get(i),second=maps.get(j),composed=first.compose(second);
            assertEquals(first.chainMatrix(z(1)).multiply(second.chainMatrix(z(1))),composed.chainMatrix(z(1)));
            assertEquals(homology.get(i).compose(homology.get(j)),composed.homologyMap(z(1)));
            assertTrue(first.ambientMap().contiguous(second.ambientMap())); assertEquals(i==j,first.contiguous(second));
        }
    }
    @Test public void pairContiguityRequiresTheSubcomplexCondition() {
        RelativeSimplicialComplex pair=interval(); RelativeSimplicialMap identity=RelativeSimplicialMap.identity(pair),reflection=map(pair,pair,1,0);
        assertTrue(identity.ambientMap().contiguous(reflection.ambientMap())); assertFalse(identity.contiguous(reflection));
        assertEquals(m(new long[]{1}),identity.homologyMap(z(1)).smithMatrix()); assertEquals(m(new long[]{-1}),reflection.homologyMap(z(1)).smithMatrix());
        RelativeSimplicialComplex based=new RelativeSimplicialComplex(pair.ambient(),complex(new int[]{0}));
        RelativeSimplicialMap constant=map(based,based,0,0),basedIdentity=RelativeSimplicialMap.identity(based);
        assertTrue(constant.contiguous(basedIdentity)); assertEquals(constant.homologyMap(z(1)),basedIdentity.homologyMap(z(1))); naturality(reflection,1);
    }
    @Test public void noncollapsedImagesInsideTheTargetSubcomplexBecomeZeroRelativeChains() {
        RelativeSimplicialComplex source=interval(),target=new RelativeSimplicialComplex(complex(new int[]{0,1,2}),complex(new int[]{0,1}));
        RelativeSimplicialMap f=map(source,target,0,1);
        assertEquals(IntegerMatrix.zero(2,1),f.chainMatrix(z(1))); assertNotEquals(IntegerMatrix.zero(3,1),f.ambientMap().chainMatrix(z(1)));
        assertTrue(f.homologyMap(z(1)).isZero()); chainIdentities(f); naturality(f,1);
        RelativeSimplicialMap collapsed=map(source,source,0,0); assertEquals(m(new long[]{0}),collapsed.chainMatrix(z(1))); assertTrue(collapsed.homologyMap(z(1)).isZero());
    }
    @Test public void degreeTwoCircleCoverHasSurjectiveRelativeMapAndAbsoluteIndexTwo() {
        FiniteSimplicialComplex hexagon=complex(new int[]{0,1},new int[]{1,2},new int[]{2,3},new int[]{3,4},new int[]{4,5},new int[]{0,5});
        RelativeSimplicialComplex source=new RelativeSimplicialComplex(hexagon,complex(new int[]{0},new int[]{3})),target=new RelativeSimplicialComplex(circle(),complex(new int[]{0}));
        RelativeSimplicialMap f=map(source,target,0,1,2,0,1,2); AbelianGroupHomomorphism h=f.homologyMap(z(1));
        assertEquals(AbelianGroupType.free(z(2)),h.source().type()); assertEquals(AbelianGroupType.Z,h.target().type());
        assertTrue(h.isSurjective()); assertEquals(AbelianGroupType.Z,h.kernel().type());
        assertEquals(z(2),f.ambientHomologyMap(z(1)).smithMatrix().get(0,0).abs());
        for(IntegerVector cycle : f.sourceHomology(z(1)).generators())
            assertEquals(f.targetHomology(z(1)).classOf(f.chainMatrix(z(1)).multiply(cycle)),h.apply(f.sourceHomology(z(1)).classOf(cycle)));
        chainIdentities(f); naturality(f,0); naturality(f,1); naturality(f,2);
    }
    @Test public void projectivePlaneMapsPreserveIntegralTorsionAndConnectingNaturality() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        RelativeSimplicialComplex source=new RelativeSimplicialComplex(plane,plane.skeleton(1)),target=new RelativeSimplicialComplex(plane,complex(new int[]{0}));
        RelativeSimplicialMap identity=RelativeSimplicialMap.identity(target),constant=map(source,target,0,0,0,0,0,0);
        assertEquals(AbelianGroupType.cyclic(z(2)),identity.homologyMap(z(1)).source().type()); assertTrue(identity.homologyMap(z(1)).isIsomorphism());
        assertTrue(constant.homologyMap(z(2)).isZero());
        RelativeSimplicialMap enlargement=RelativeSimplicialMap.inclusion(target,source);
        for(int k=0;k<=2;k++) { naturality(identity,k); naturality(constant,k); naturality(enlargement,k); }
        assertEquals(AbelianGroupType.cyclic(z(2)),source.connectingHomology(z(2)).cokernel().type());
    }
    @Test public void isomorphismsMustIdentifyBothPairsAndCompositionChecksBothMiddleComponents() {
        RelativeSimplicialComplex pair=interval(); RelativeSimplicialMap reflection=map(pair,pair,1,0);
        assertTrue(reflection.isIsomorphism()); assertEquals(RelativeSimplicialMap.identity(pair),reflection.inverse().compose(reflection));
        RelativeSimplicialComplex absolute=RelativeSimplicialComplex.absolute(pair.ambient()); RelativeSimplicialMap enlargement=RelativeSimplicialMap.inclusion(absolute,pair);
        assertTrue(enlargement.ambientMap().isIsomorphism()); assertFalse(enlargement.isIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,enlargement::inverse);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> reflection.compose(RelativeSimplicialMap.identity(absolute)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> reflection.contiguous(RelativeSimplicialMap.identity(absolute)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> RelativeSimplicialMap.inclusion(pair,absolute));
    }
    @Test public void imageCorestrictionAndRestrictionRetainDeclaredPairs() {
        RelativeSimplicialComplex pair=interval(); RelativeSimplicialMap constant=map(pair,pair,0,0);
        RelativeSimplicialComplex point=RelativeSimplicialComplex.diagonal(complex(new int[]{0})); assertEquals(point,constant.image());
        assertEquals(constant,RelativeSimplicialMap.inclusion(point,pair).compose(constant.corestrictImage()));
        RelativeSimplicialMap restricted=RelativeSimplicialMap.identity(pair).restrict(point); assertEquals(point,restricted.source()); assertEquals(pair,restricted.target());
        assertEquals(RelativeSimplicialMap.inclusion(point,pair),restricted); chainIdentities(restricted);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> constant.restrict(RelativeSimplicialComplex.diagonal(pair.ambient())));
    }
    @Test public void invalidSubcomplexMapsAndWrongFullAmbientBoundariesAreRejected() {
        RelativeSimplicialComplex pair=interval(); FiniteSimplicialMap identity=FiniteSimplicialMap.identity(pair.ambient());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new RelativeSimplicialMap(RelativeSimplicialComplex.diagonal(pair.ambient()),pair,identity));
        RelativeSimplicialComplex based=new RelativeSimplicialComplex(pair.ambient(),complex(new int[]{0}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(based,based,1,0));
        FiniteSimplicialMap wrong=map(pair.subcomplex(),pair.ambient(),0,1);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new RelativeSimplicialMap(pair,pair,wrong));
        RelativeSimplicialMap f=RelativeSimplicialMap.identity(pair);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.chainMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.homologyMap(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.longExactMaps(z(-1)));
    }
    @Test public void absoluteDiagonalEmptyAndRelabelledMapsRespectRetainedCoordinates() {
        FiniteSimplicialComplex circle=circle(); FiniteSimplicialMap reflection=map(circle,circle,1,0,2); RelativeSimplicialMap absolute=RelativeSimplicialMap.absolute(reflection),diagonal=RelativeSimplicialMap.diagonal(reflection);
        for(int k=0;k<=2;k++) {
            assertEquals(reflection.chainMatrix(z(k)),absolute.chainMatrix(z(k))); assertEquals(reflection.homologyMap(z(k)),absolute.homologyMap(z(k)));
            assertEquals(IntegerMatrix.zero(0,0),diagonal.chainMatrix(z(k))); assertTrue(diagonal.homologyMap(z(k)).source().type().isTrivial());
        }
        RelativeSimplicialMap empty=RelativeSimplicialMap.identity(RelativeSimplicialComplex.absolute(complex()));
        assertTrue(empty.chainMatrices().isEmpty()); assertTrue(empty.homologyMaps().isEmpty()); naturality(empty,0);
        RelativeSimplicialMap emptyTo=RelativeSimplicialMap.inclusion(empty.source(),interval()); assertEquals(IntegerMatrix.zero(1,0),emptyTo.chainMatrix(z(1))); naturality(emptyTo,1);
        assertTrue(absolute.homologyMap(BigInteger.TEN.pow(100)).isIsomorphism()); assertEquals(IntegerMatrix.zero(0,0),absolute.chainMatrix(BigInteger.TEN.pow(100)));
        RelativeSimplicialComplex labels=new RelativeSimplicialComplex(complex(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE}),complex(new int[]{Integer.MIN_VALUE},new int[]{Integer.MAX_VALUE}));
        RelativeSimplicialMap relabel=map(interval(),labels,Integer.MAX_VALUE,Integer.MIN_VALUE);
        assertEquals(m(new long[]{-1}),relabel.homologyMap(z(1)).smithMatrix()); assertTrue(relabel.isIsomorphism()); naturality(relabel,1);
    }
    @Test public void immutableMapsEnforceRelativeBasisAndSharedComputationLimits() {
        RelativeSimplicialMap f=RelativeSimplicialMap.identity(interval()); assertEquals(f,RelativeSimplicialMap.identity(interval())); assertEquals(f.hashCode(),RelativeSimplicialMap.identity(interval()).hashCode());
        assertThrows(UnsupportedOperationException.class,() -> f.chainMatrices().clear()); assertThrows(UnsupportedOperationException.class,() -> f.homologyMaps().clear()); assertThrows(UnsupportedOperationException.class,() -> f.longExactMaps(z(1)).clear());
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<257;i++) points.add(FiniteSet.of(i));
        RelativeSimplicialMap diagonal=RelativeSimplicialMap.identity(RelativeSimplicialComplex.diagonal(new FiniteSimplicialComplex(points)));
        assertEquals(IntegerMatrix.zero(0,0),diagonal.chainMatrix(z(0))); assertTrue(diagonal.homologyMap(z(0)).isIsomorphism());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> diagonal.longExactMaps(z(0)));
        RelativeSimplicialMap costly=RelativeSimplicialMap.identity(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(points.subList(0,100))));
        assertEquals(AbelianGroupType.free(z(100)),costly.sourceHomology(z(0)).type()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.homologyMap(z(0)));
        RelativeSimplicialMap naturalityBudget=RelativeSimplicialMap.identity(RelativeSimplicialComplex.diagonal(new FiniteSimplicialComplex(points.subList(0,70))));
        assertNotNull(naturalityBudget.ambientHomologyMap(z(0))); assertNotNull(naturalityBudget.subcomplexHomologyMap(z(0))); assertNotNull(naturalityBudget.homologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> naturalityBudget.longExactMaps(z(0)));
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int omit=0;omit<8;omit++) { List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); facets.add(new FiniteSet<>(face)); }
        for(int i=0;i<70;i++) facets.add(FiniteSet.of(100+i));
        RelativeSimplicialMap aggregate=RelativeSimplicialMap.identity(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(facets)));
        for(int k=0;k<=6;k++) assertNotNull(aggregate.homologyMap(z(k))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,aggregate::homologyMaps);
    }
    @Test public void nativeMapConstructionAndScalarFlatResultsUseExistingWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=interval(); RelativeSimplicialMap reflection=map(pair,pair,1,0);
        IAlgebraItem<RelativeSimplicialMap> item=math.simplicialMaps.algebra().buildAlgebraItem(reflection.ambientMap()).performUnsafeOperation("RelativeMap.from-map",new Pair<>(pair,pair));
        assertSame(math.relativeMaps.algebra(),item.getAlgebra()); assertEquals(reflection,item.getResult());
        assertSame(math.relativeComplexes.algebra(),item.performAlgebraTransfer("source").getAlgebra()); assertSame(math.simplicialMaps.algebra(),item.performAlgebraTransfer("subcomplex-map").getAlgebra());
        assertSame(math.integralHomology.algebra(),item.performUnsafeOperation("source-homology",z(1)).getAlgebra());
        assertSame(math.relativeMaps.algebra(),item.performCustomMemberOperation("restrict",pair).getAlgebra());
        for(IAlgebraItem<AbelianGroupHomomorphism> h : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-maps",z(1))) assertSame(math.abelianHomomorphisms.algebra(),h.getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.relativeMaps.boundaries).validate(new Pair<>(pair,"wrong")));
    }
    @Test public void serializedPairMapFlowsRemainRepeatableAndExposeNaturalityMaps() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=interval(); RelativeSimplicialMap reflection=map(pair,pair,1,0);
        IAlgebraFlow<IntegerMatrix> flow=math.flow(math.simplicialMaps,Collections.singletonList(reflection.ambientMap()))
                .<RelativeSimplicialMap,Pair<RelativeSimplicialComplex,RelativeSimplicialComplex>>performAlgebraUnsafe("RelativeMap.from-map",new Pair<>(pair,pair))
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("homology-map",z(1)).<IntegerMatrix>performAlgebraTransfer("smith-matrix");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("ZMatrix(1x1)[[-1]]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","true","true","true"),math.flow(math.relativeMaps,Collections.singletonList(reflection))
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("long-exact-maps",z(1)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
