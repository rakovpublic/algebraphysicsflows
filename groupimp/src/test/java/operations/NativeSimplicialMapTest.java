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

public class NativeSimplicialMapTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int[] facet : facets) { List<Integer> values=new ArrayList<>(); for(int vertex : facet) values.add(vertex); faces.add(new FiniteSet<>(values)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex circle() { return complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); }
    private static FiniteSimplicialMap map(FiniteSimplicialComplex source,FiniteSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0;
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source).members()) vertices.put(vertex,z(images[i++]));
        return new FiniteSimplicialMap(source,target,vertices);
    }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] entries=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { entries[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) entries[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(entries);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void chainIdentities(FiniteSimplicialMap f) {
        for(int k=1;k<=Math.max(f.source().dimension(),f.target().dimension())+1;k++)
            assertEquals(f.target().integralBoundaryMatrix(z(k)).multiply(f.chainMatrix(z(k))),f.chainMatrix(z(k-1)).multiply(f.source().integralBoundaryMatrix(z(k))));
    }
    private static BigInteger determinant(IntegerMatrix a) {
        if(a.rows()==0) return BigInteger.ONE; BigInteger result=BigInteger.ZERO;
        for(int column=0;column<a.columns();column++) {
            BigInteger[][] minor=new BigInteger[a.rows()-1][a.columns()-1];
            for(int r=1;r<a.rows();r++) { int c1=0; for(int c=0;c<a.columns();c++) if(c!=column) minor[r-1][c1++]=a.get(r,c); }
            BigInteger term=a.get(0,column).multiply(determinant(new IntegerMatrix(minor))); result=(column&1)==0?result.add(term):result.subtract(term);
        }
        return result;
    }
    @Test public void all256TetrahedronVertexMapsRespectBoundariesAndIndependentOrientationSigns() {
        FiniteSimplicialComplex tetrahedron=complex(new int[]{0,1,2,3});
        for(int code=0;code<256;code++) {
            int digits=code; int[] values=new int[4]; for(int i=0;i<4;i++) { values[i]=digits%4; digits/=4; }
            FiniteSimplicialMap f=map(tetrahedron,tetrahedron,values); chainIdentities(f);
            assertEquals(determinant(f.chainMatrix(z(0))),f.chainMatrix(z(3)).get(0,0));
            for(int k=0;k<=3;k++) {
                IntegerMatrix matrix=f.chainMatrix(z(k)); List<FiniteSet<BigInteger>> source=FiniteSimplicialMap.simplexBasis(tetrahedron,z(k)),target=FiniteSimplicialMap.simplexBasis(tetrahedron,z(k));
                for(int c=0;c<matrix.columns();c++) {
                    FiniteSet<BigInteger> image=f.mapSimplex(source.get(c)); int nonzero=0;
                    for(int r=0;r<matrix.rows();r++) if(matrix.get(r,c).signum()!=0) { nonzero++; assertEquals(image,target.get(r)); assertEquals(BigInteger.ONE,matrix.get(r,c).abs()); }
                    assertEquals(image.size()==k+1?1:0,nonzero);
                }
            }
        }
    }
    private static int direction(int a,int b) { return a==b?0:(b-a+3)%3==1?1:-1; }
    @Test public void all27CircleMapsAnd729CompositionsMatchIndependentWindingNumbers() {
        FiniteSimplicialComplex circle=circle(); List<FiniteSimplicialMap> maps=new ArrayList<>(); List<AbelianGroupHomomorphism> homology=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) {
            FiniteSimplicialMap f=map(circle,circle,a,b,c); maps.add(f); AbelianGroupHomomorphism h=f.homologyMap(z(1)); homology.add(h);
            int degree=(direction(a,b)+direction(b,c)+direction(c,a))/3;
            assertEquals(m(new long[]{degree}),h.smithMatrix()); assertEquals(h.source(),h.target());
            assertEquals(AbelianGroupHomomorphism.identity(f.sourceHomology(z(0)).group()),f.homologyMap(z(0))); chainIdentities(f);
        }
        for(int i=0;i<maps.size();i++) for(int j=0;j<maps.size();j++) {
            FiniteSimplicialMap a=maps.get(i),b=maps.get(j),composed=a.compose(b);
            assertEquals(a.chainMatrix(z(1)).multiply(b.chainMatrix(z(1))),composed.chainMatrix(z(1)));
            assertEquals(homology.get(i).compose(homology.get(j)),composed.homologyMap(z(1)));
            boolean contiguous=true;
            for(int[] edge : new int[][]{{0,1},{0,2},{1,2}}) {
                Set<BigInteger> union=new HashSet<>(); for(int vertex : edge) { union.add(a.mapVertex(z(vertex))); union.add(b.mapVertex(z(vertex))); }
                if(union.size()>2) contiguous=false;
            }
            assertEquals(contiguous,a.contiguous(b)); if(contiguous) assertEquals(homology.get(i),homology.get(j));
        }
    }
    @Test public void degreeTwoCoveringProducesAnIndexTwoHomologyImage() {
        FiniteSimplicialComplex hexagon=complex(new int[]{0,1},new int[]{1,2},new int[]{2,3},new int[]{3,4},new int[]{4,5},new int[]{0,5});
        FiniteSimplicialMap covering=map(hexagon,circle(),0,1,2,0,1,2); AbelianGroupHomomorphism h=covering.homologyMap(z(1));
        assertEquals(z(2),h.smithMatrix().get(0,0).abs()); assertTrue(h.isInjective()); assertFalse(h.isSurjective());
        assertEquals(AbelianGroupType.cyclic(z(2)),h.cokernel().type()); assertTrue(covering.isSurjective()); assertFalse(covering.isInjective());
        for(IntegerVector cycle : covering.sourceHomology(z(1)).generators())
            assertEquals(covering.targetHomology(z(1)).classOf(covering.chainMatrix(z(1)).multiply(cycle)),h.apply(covering.sourceHomology(z(1)).classOf(cycle)));
        chainIdentities(covering);
    }
    @Test public void collapsedEdgesAndTrianglesAreZeroChainsWithCorrectLowerDegreeImages() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}),edge=complex(new int[]{7,9});
        FiniteSimplicialMap collapse=map(triangle,edge,7,7,9);
        assertEquals(m(new long[]{1,1,0},new long[]{0,0,1}),collapse.chainMatrix(z(0)));
        assertEquals(m(new long[]{0,1,1}),collapse.chainMatrix(z(1))); assertEquals(IntegerMatrix.zero(0,1),collapse.chainMatrix(z(2)));
        assertEquals(FiniteSet.of(z(7)),collapse.mapSimplex(FiniteSet.of(z(0),z(1))));
        assertEquals(Arrays.asList(z(7),z(7),z(9)),collapse.vertexImages()); assertEquals(Arrays.asList(z(0),z(1)),collapse.vertexFiber(z(7)));
        chainIdentities(collapse); assertTrue(collapse.homologyMap(z(0)).isIsomorphism());
    }
    @Test public void vertexBijectionsDoNotImplySimplicialIsomorphisms() {
        FiniteSimplicialComplex discrete=complex(new int[]{0},new int[]{1},new int[]{2}),circle=circle(),disk=complex(new int[]{0,1,2});
        FiniteSimplicialMap edgeInclusion=FiniteSimplicialMap.inclusion(discrete,circle),diskInclusion=FiniteSimplicialMap.inclusion(circle,disk);
        for(FiniteSimplicialMap f : Arrays.asList(edgeInclusion,diskInclusion)) {
            assertTrue(f.isInjective()); assertTrue(f.isVertexSurjective()); assertFalse(f.isSurjective()); assertFalse(f.isIsomorphism());
            assertEquals(f.source(),f.image()); failure(MathFailure.Kind.OPERATION_UNDEFINED,f::inverse);
            assertTrue(f.corestrictImage().isIsomorphism()); chainIdentities(f);
        }
        assertTrue(diskInclusion.homologyMap(z(1)).isZero()); assertEquals(AbelianGroupType.Z,diskInclusion.homologyMap(z(1)).kernel().type());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> FiniteSimplicialMap.inclusion(disk,circle));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(circle,discrete,0,1,2));
    }
    @Test public void arbitraryVertexLabelsRoundTripThroughNativeFunctionsAndInverses() {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex source=complex(new int[]{Integer.MIN_VALUE,7,Integer.MAX_VALUE}),target=complex(new int[]{-5,2,8});
        FiniteSimplicialMap f=map(source,target,8,-5,2); assertTrue(f.isIsomorphism());
        assertEquals(FiniteSimplicialMap.identity(source),f.inverse().compose(f)); assertEquals(FiniteSimplicialMap.identity(target),f.compose(f.inverse()));
        FiniteFunction<BigInteger,BigInteger> function=math.integerFunctions.member(FiniteSimplicialMap.vertexSet(source),FiniteSimplicialMap.vertexSet(target),f.vertexMap());
        assertEquals(f,FiniteSimplicialMap.fromFunction(function,source,target)); assertEquals(z(8),f.mapVertex(z(Integer.MIN_VALUE)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.mapVertex(BigInteger.TEN.pow(100)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.mapSimplex(FiniteSet.of(BigInteger.TEN.pow(100))));
        chainIdentities(f);
    }
    @Test public void projectivePlaneIdentityAndConstantsActCorrectlyOnIntegralTorsion() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        FiniteSimplicialMap identity=FiniteSimplicialMap.identity(plane),constant=identity.constantAt(z(0));
        AbelianGroupHomomorphism h=identity.homologyMap(z(1)); assertEquals(AbelianGroupType.cyclic(z(2)),h.source().type());
        assertEquals(AbelianGroupHomomorphism.identity(h.source()),h); assertTrue(constant.homologyMap(z(1)).isZero());
        assertTrue(constant.homologyMap(z(0)).isIsomorphism()); chainIdentities(constant);
    }
    @Test public void restrictionsImagesConstantsAndEmptyMapsRetainDeclaredBoundaries() {
        FiniteSimplicialComplex circle=circle(),edge=complex(new int[]{0,1}),empty=complex(); FiniteSimplicialMap reflection=map(circle,circle,1,0,2);
        FiniteSimplicialMap restricted=reflection.restrict(edge); assertEquals(edge,restricted.source()); assertEquals(circle,restricted.target()); assertEquals(edge,restricted.image());
        assertEquals(FiniteSimplicialMap.inclusion(edge,circle).compose(restricted.corestrictImage()),restricted);
        assertEquals(FiniteSimplicialMap.emptyTo(circle),reflection.restrict(empty));
        assertEquals(FiniteSimplicialMap.identity(empty),FiniteSimplicialMap.emptyTo(empty));
        assertTrue(FiniteSimplicialMap.emptyTo(empty).chainMatrices().isEmpty()); assertTrue(FiniteSimplicialMap.emptyTo(empty).homologyMaps().isEmpty());
        FiniteSimplicialMap emptyMap=FiniteSimplicialMap.emptyTo(circle);
        assertEquals(IntegerMatrix.zero(3,0),emptyMap.chainMatrix(z(0))); assertEquals(IntegerMatrix.zero(3,0),emptyMap.chainMatrix(z(1)));
        assertTrue(emptyMap.homologyMap(z(1)).isInjective()); assertFalse(emptyMap.homologyMap(z(1)).isSurjective());
        assertEquals(IntegerMatrix.zero(0,0),reflection.chainMatrix(BigInteger.TEN.pow(100))); assertTrue(reflection.homologyMap(BigInteger.TEN.pow(100)).isIsomorphism());
        assertEquals(2,emptyMap.homologyMaps().size()); chainIdentities(emptyMap);
    }
    @Test public void malformedMapsAndMismatchedOperationsAreRejectedWithoutPartialResults() {
        FiniteSimplicialComplex circle=circle(),edge=complex(new int[]{0,1}); FiniteSimplicialMap identity=FiniteSimplicialMap.identity(circle);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new FiniteSimplicialMap(circle,circle,Collections.emptyMap()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(circle,circle,0,1,10));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.mapSimplex(FiniteSet.of()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.mapSimplex(FiniteSet.of(z(0),z(1),z(2))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.vertexFiber(z(7)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.constantAt(z(7)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.chainMatrix(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.homologyMap(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.compose(FiniteSimplicialMap.identity(edge)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.contiguous(FiniteSimplicialMap.identity(edge)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> identity.restrict(complex(new int[]{9})));
        ConcreteMathematics math=new ConcreteMathematics(); FiniteFunction<BigInteger,BigInteger> wrong=math.integerFunctions.member(FiniteSet.of(z(0)),FiniteSet.of(z(0),z(1)),Collections.singletonMap(z(0),z(0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> FiniteSimplicialMap.fromFunction(wrong,complex(new int[]{0}),complex(new int[]{0})));
    }
    @Test public void immutableValuesAndSharedHomologyBudgetsAreEnforced() {
        FiniteSimplicialComplex circle=circle(); Map<BigInteger,BigInteger> input=new HashMap<>(); input.put(z(0),z(0)); input.put(z(1),z(1)); input.put(z(2),z(2));
        FiniteSimplicialMap identity=new FiniteSimplicialMap(circle,circle,input); input.put(z(0),z(1));
        assertEquals(FiniteSimplicialMap.identity(circle),identity); assertEquals(FiniteSimplicialMap.identity(circle).hashCode(),identity.hashCode());
        assertThrows(UnsupportedOperationException.class,() -> identity.vertexMap().clear()); assertThrows(UnsupportedOperationException.class,() -> identity.chainMatrices().clear());
        assertThrows(UnsupportedOperationException.class,() -> identity.homologyMaps().clear()); assertThrows(UnsupportedOperationException.class,() -> identity.vertexImages().clear());
        List<int[]> points=new ArrayList<>(); for(int i=0;i<4097;i++) points.add(new int[]{i});
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> FiniteSimplicialMap.identity(complex(points.toArray(new int[0][]))));
        FiniteSimplicialMap many=FiniteSimplicialMap.identity(complex(points.subList(0,257).toArray(new int[0][])));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> many.chainMatrix(z(0)));
        FiniteSimplicialMap costly=FiniteSimplicialMap.identity(complex(points.subList(0,100).toArray(new int[0][])));
        assertEquals(AbelianGroupType.free(z(100)),costly.sourceHomology(z(0)).type());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.homologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,costly::homologyMaps);
        // Every degree fits separately; the flat operation must not reset its budget between degrees.
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int omit=0;omit<8;omit++) {
            List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); facets.add(new FiniteSet<>(face));
        }
        for(int i=0;i<70;i++) facets.add(FiniteSet.of(100+i));
        FiniteSimplicialMap aggregate=FiniteSimplicialMap.identity(new FiniteSimplicialComplex(facets));
        for(int k=0;k<=6;k++) assertNotNull(aggregate.homologyMap(z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,aggregate::homologyMaps);
    }
    @Test public void nativeScalarAndFlatOperationsUseExistingWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex circle=circle(); FiniteSimplicialMap f=map(circle,circle,1,0,2);
        FiniteFunction<BigInteger,BigInteger> function=math.integerFunctions.member(FiniteSimplicialMap.vertexSet(circle),FiniteSimplicialMap.vertexSet(circle),f.vertexMap());
        IAlgebraItem<FiniteSimplicialMap> item=math.integerFunctions.algebra().buildAlgebraItem(function)
                .performUnsafeOperation("SimplicialMap.from-function",new Pair<>(circle,circle));
        assertSame(math.simplicialMaps.algebra(),item.getAlgebra()); assertEquals(f,item.getResult());
        assertSame(math.integers.algebra(),item.performLeftProjectionOperation("map-vertex",z(0)).getAlgebra());
        assertSame(math.integerSets.algebra(),item.performLeftProjectionOperation("map-simplex",FiniteSet.of(z(0),z(1))).getAlgebra());
        for(IAlgebraItem<BigInteger> vertex : item.<BigInteger>performLeftProjectionFlatOperation("vertex-fiber",z(1))) assertSame(math.integers.algebra(),vertex.getAlgebra());
        assertSame(math.integerMatrices.algebra(),item.performUnsafeOperation("chain-matrix",z(1)).getAlgebra());
        assertSame(math.abelianHomomorphisms.algebra(),item.performUnsafeOperation("homology-map",z(1)).getAlgebra());
        assertSame(math.integerFunctions.algebra(),item.performAlgebraTransfer("vertex-map").getAlgebra());
        List<IAlgebraItem<FiniteSet<BigInteger>>> basis=math.complexes.algebra().buildAlgebraItem(circle).performUnsafeFlatOperation("SimplicialMap.simplex-basis",z(1));
        assertEquals(3,basis.size()); for(IAlgebraItem<FiniteSet<BigInteger>> simplex : basis) assertSame(math.integerSets.algebra(),simplex.getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.simplicialMaps.boundaries).validate(new Pair<>(circle,"wrong")));
    }
    @Test public void serializedVertexToHomologyFlowsStayRepeatable() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialMap reflection=map(circle(),circle(),1,0,2);
        IAlgebraFlow<IntegerMatrix> flow=math.flow(math.simplicialMaps,Collections.singletonList(reflection))
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("homology-map",z(1))
                .<IntegerMatrix>performAlgebraTransfer("smith-matrix");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("ZMatrix(1x1)[[-1]]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Collections.singletonList("true"),math.flow(math.complexes,Collections.singletonList(circle()))
                .<FiniteSimplicialMap>performAlgebraTransfer("SimplicialMap.identity-on")
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("homology-map",z(1))
                .<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
