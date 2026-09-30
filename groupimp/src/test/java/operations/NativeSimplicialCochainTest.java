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

public class NativeSimplicialCochainTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... entries) { BigInteger[] values=new BigInteger[entries.length]; for(int i=0;i<values.length;i++) values[i]=z(entries[i]); return new IntegerVector(values); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static SimplicialCochain c(FiniteSimplicialComplex complex,int k,long... values) { return new SimplicialCochain(complex,z(k),v(values)); }
    private static FiniteSimplicialComplex circle() { return complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static FiniteSimplicialMap map(FiniteSimplicialComplex from,FiniteSimplicialComplex to,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0; for(BigInteger vertex : FiniteSimplicialMap.vertexSet(from).members()) vertices.put(vertex,z(images[i++]));
        return new FiniteSimplicialMap(from,to,vertices);
    }
    @Test public void coboundaryUsesSignedFacesAndItsPairingIsDualToBoundary() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); SimplicialCochain a=c(triangle,0,2,5,-1),b=c(triangle,1,7,-3,4);
        assertEquals(v(3,-3,-6),a.coboundary().coordinates()); assertEquals(v(14),b.coboundary().coordinates());
        assertTrue(a.coboundary().coboundary().isZero());
        IntegerVector chain=v(11,-2,3); assertEquals(a.coboundary().evaluate(chain),a.evaluate(triangle.integralBoundaryMatrix(z(1)).multiply(chain)));
        assertEquals(z(28),b.coboundary().evaluate(v(2))); assertEquals(b.coboundary().evaluate(v(2)),b.evaluate(triangle.integralBoundaryMatrix(z(2)).multiply(v(2))));
        assertFalse(a.isCocycle()); assertTrue(a.coboundary().isCoboundary());
        assertEquals(a.coboundary(),c(triangle,0,0,0,0).withCoordinates(a.coboundary().coboundingCoordinates()).coboundary());
    }
    @Test public void all729TernaryEdgeCochainsSatisfyCupLeibnizAssociativityAndUnits() {
        FiniteSimplicialComplex tetra=complex(new int[]{0,1,2,3}); SimplicialCochain b=c(tetra,1,2,-1,3,0,-2,1),f=c(tetra,0,1,2,3,4),unit=SimplicialCochain.unit(tetra);
        List<FiniteSet<BigInteger>> edges=FiniteSimplicialMap.simplexBasis(tetra,z(1)),triangles=FiniteSimplicialMap.simplexBasis(tetra,z(2));
        for(int code=0;code<729;code++) {
            int digits=code; long[] entries=new long[6]; for(int i=0;i<6;i++) { entries[i]=digits%3-1; digits/=3; } SimplicialCochain a=c(tetra,1,entries),product=a.cup(b);
            for(int i=0;i<triangles.size();i++) {
                List<BigInteger> face=new ArrayList<>(triangles.get(i).members()); Collections.sort(face);
                int front=edges.indexOf(FiniteSet.of(face.get(0),face.get(1))),back=edges.indexOf(FiniteSet.of(face.get(1),face.get(2)));
                assertEquals(z(entries[front]).multiply(b.coordinates().get(back)),product.coordinates().get(i));
            }
            assertEquals(product.coboundary(),a.coboundary().cup(b).subtract(a.cup(b.coboundary())));
            assertEquals(f.cup(a).coboundary(),f.coboundary().cup(a).add(f.cup(a.coboundary())));
            assertEquals(f.cup(a).cup(b),f.cup(a.cup(b))); assertEquals(a,unit.cup(a)); assertEquals(a,a.cup(unit));
            assertTrue(a.coboundary().coboundary().isZero());
        }
        FiniteSimplicialComplex edge=complex(new int[]{0,1}); assertNotEquals(c(edge,0,1,2).cup(c(edge,1,1)),c(edge,1,1).cup(c(edge,0,1,2)));
    }
    private static int[][] torusTriangles() {
        List<int[]> triangles=new ArrayList<>();
        for(int i=0;i<3;i++) for(int j=0;j<3;j++) {
            int a=3*i+j,b=3*((i+1)%3)+j,cc=3*((i+1)%3)+(j+1)%3,d=3*i+(j+1)%3;
            triangles.add(new int[]{a,b,cc}); triangles.add(new int[]{a,cc,d});
        }
        return triangles.toArray(new int[0][]);
    }
    private static SimplicialCochain torusClass(FiniteSimplicialComplex torus,boolean horizontal) {
        List<FiniteSet<BigInteger>> edges=FiniteSimplicialMap.simplexBasis(torus,z(1)); long[] values=new long[edges.size()];
        for(int i=0;i<values.length;i++) {
            List<BigInteger> edge=new ArrayList<>(edges.get(i).members()); Collections.sort(edge); int a=edge.get(0).intValue(),b=edge.get(1).intValue();
            a=horizontal?a/3:a%3; b=horizontal?b/3:b%3; values[i]=a==2 && b==0?1:a==0 && b==2?-1:0;
        }
        return c(torus,1,values);
    }
    private static IntegerVector torusCycle(FiniteSimplicialComplex torus) {
        List<FiniteSet<BigInteger>> basis=FiniteSimplicialMap.simplexBasis(torus,z(2)); long[] values=new long[basis.size()];
        for(int[] triangle : torusTriangles()) {
            int sign=1; for(int i=0;i<3;i++) for(int j=i+1;j<3;j++) if(triangle[i]>triangle[j]) sign=-sign;
            values[basis.indexOf(FiniteSet.of(z(triangle[0]),z(triangle[1]),z(triangle[2])))]=sign;
        }
        return v(values);
    }
    @Test public void torusCupProductsRecoverTheIntegralExteriorRingAndDescendToClasses() {
        FiniteSimplicialComplex torus=complex(torusTriangles()); SimplicialCochain a=torusClass(torus,true),b=torusClass(torus,false); IntegerVector fundamental=torusCycle(torus);
        assertEquals(IntegerVector.zero(torus.simplices(1).size()),torus.integralBoundaryMatrix(z(2)).multiply(fundamental));
        assertTrue(a.isCocycle()); assertTrue(b.isCocycle()); assertEquals(z(1),a.cup(b).evaluate(fundamental)); assertEquals(z(-1),b.cup(a).evaluate(fundamental));
        assertTrue(a.cup(a).isCoboundary()); assertTrue(b.cup(b).isCoboundary()); assertTrue(a.cup(b).cohomologous(b.cup(a).negate()));
        assertFalse(a.cupClass(b).isZero()); assertEquals(a.cupClass(b).scale(z(-1)),b.cupClass(a));
        SimplicialCochain changed=a.add(c(torus,0,2,-1,0,3,1,4,-2,5,7).coboundary());
        assertTrue(changed.cohomologous(a)); assertEquals(a.cupClass(b),changed.cupClass(b));
        assertEquals(AbelianGroupType.free(z(2)),a.cohomology().type()); assertEquals(AbelianGroupType.Z,a.cup(b).cohomology().type());
        assertEquals(a.classOf(),a.representative(a.classOf()).classOf()); assertTrue(a.representative(a.classOf()).cohomologous(a));
    }
    @Test public void arbitraryVertexPullbackIsCupNaturalOnCohomologyButNotOnCochains() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}); FiniteSimplicialMap flip=map(edge,edge,1,0);
        SimplicialCochain f=c(edge,0,1,2),a=c(edge,1,1);
        assertNotEquals(f.cup(a).pullback(flip),f.pullback(flip).cup(a.pullback(flip)));
        FiniteSimplicialComplex torus=complex(torusTriangles()); int[] transpose=new int[9]; for(int i=0;i<9;i++) transpose[i]=3*(i%3)+i/3;
        FiniteSimplicialMap swap=map(torus,torus,transpose); SimplicialCochain x=torusClass(torus,true),y=torusClass(torus,false);
        assertEquals(y,x.pullback(swap)); assertEquals(x,y.pullback(swap));
        assertTrue(x.cup(y).pullback(swap).cohomologous(x.pullback(swap).cup(y.pullback(swap))));
        assertEquals(x.pullback(swap).cupClass(y.pullback(swap)),SimplicialCochain.cohomologyMap(swap,z(2)).apply(x.cupClass(y)));
        assertEquals(x.cupClass(y).scale(z(-1)),SimplicialCochain.cohomologyMap(swap,z(2)).apply(x.cupClass(y)));
    }
    @Test public void all256TetrahedronVertexMapsHaveSignedContravariantPullbacks() {
        FiniteSimplicialComplex tetra=complex(new int[]{0,1,2,3});
        for(int code=0;code<256;code++) {
            int digits=code; int[] images=new int[4]; for(int i=0;i<4;i++) { images[i]=digits%4; digits/=4; } FiniteSimplicialMap f=map(tetra,tetra,images);
            for(int k=0;k<=3;k++) {
                List<FiniteSet<BigInteger>> basis=FiniteSimplicialMap.simplexBasis(tetra,z(k)); long[] coefficients=new long[basis.size()]; for(int i=0;i<coefficients.length;i++) coefficients[i]=i+1;
                SimplicialCochain cochain=c(tetra,k,coefficients),pulled=cochain.pullback(f);
                for(int i=0;i<basis.size();i++) {
                    List<BigInteger> face=new ArrayList<>(basis.get(i).members()); Collections.sort(face); List<BigInteger> image=new ArrayList<>();
                    for(BigInteger vertex : face) image.add(z(images[vertex.intValue()])); int sign=1;
                    for(int r=0;r<image.size();r++) for(int s=r+1;s<image.size();s++) if(image.get(r).compareTo(image.get(s))>0) sign=-sign;
                    long expected=new HashSet<>(image).size()!=image.size()?0:sign*(basis.indexOf(new FiniteSet<>(image))+1);
                    assertEquals(z(expected),pulled.coordinates().get(i));
                }
                assertEquals(cochain.coboundary().pullback(f),pulled.coboundary());
            }
        }
    }
    private static int direction(int a,int b) { return a==b?0:(b-a+3)%3==1?1:-1; }
    @Test public void all27CircleMapsAnd729CompositionsReverseCohomologyComposition() {
        FiniteSimplicialComplex circle=circle(); SimplicialCochain generator=c(circle,1,1,0,0); List<FiniteSimplicialMap> maps=new ArrayList<>(); List<AbelianGroupHomomorphism> induced=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int cc=0;cc<3;cc++) {
            FiniteSimplicialMap f=map(circle,circle,a,b,cc); maps.add(f); AbelianGroupHomomorphism h=SimplicialCochain.cohomologyMap(f,z(1)); induced.add(h);
            int winding=(direction(a,b)+direction(b,cc)+direction(cc,a))/3;
            assertEquals(generator.classOf().scale(z(winding)),h.apply(generator.classOf())); assertEquals(generator.pullback(f).classOf(),h.apply(generator.classOf()));
        }
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) {
            FiniteSimplicialMap f=maps.get(i),g=maps.get(j);
            assertEquals(induced.get(j).compose(induced.get(i)),SimplicialCochain.cohomologyMap(f.compose(g),z(1)));
            assertEquals(generator.pullback(f).pullback(g),generator.pullback(f.compose(g)));
            if(f.contiguous(g)) assertEquals(induced.get(i),induced.get(j));
        }
        FiniteSimplicialComplex six=complex(new int[]{0,1},new int[]{1,2},new int[]{2,3},new int[]{3,4},new int[]{4,5},new int[]{0,5});
        FiniteSimplicialMap doubleCover=map(six,circle,0,1,2,0,1,2); AbelianGroupHomomorphism pullback=SimplicialCochain.cohomologyMap(doubleCover,z(1));
        assertEquals(z(2),generator.pullback(doubleCover).evaluate(v(1,-1,1,1,1,1)));
        assertTrue(pullback.isInjective()); assertFalse(pullback.isSurjective()); assertEquals(AbelianGroupType.cyclic(z(2)),pullback.cokernel().type());
        assertEquals(generator.pullback(doubleCover).classOf(),pullback.apply(generator.classOf()));
        AbelianGroupHomomorphism restriction=SimplicialCochain.cohomologyMap(FiniteSimplicialMap.inclusion(circle,complex(new int[]{0,1,2})),z(1));
        assertEquals(AbelianGroupType.ZERO,restriction.source().type()); assertEquals(AbelianGroupType.Z,restriction.target().type()); assertTrue(restriction.isZero());
    }
    @Test public void integralCohomologyMovesProjectivePlaneTorsionIntoDegreeTwo() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        List<IntegralHomology> models=SimplicialCochain.cohomologyDegrees(plane);
        assertEquals(AbelianGroupType.Z,models.get(0).type()); assertEquals(AbelianGroupType.ZERO,models.get(1).type()); assertEquals(AbelianGroupType.cyclic(z(2)),models.get(2).type());
        SimplicialCochain torsion=SimplicialCochain.zero(plane,z(2)).cocycleGenerators().get(0);
        assertEquals(z(2),torsion.classOf().order()); assertFalse(torsion.isCoboundary()); assertTrue(torsion.scale(z(2)).isCoboundary());
        assertEquals(torsion.scale(z(2)),new SimplicialCochain(plane,z(1),torsion.scale(z(2)).coboundingCoordinates()).coboundary());
        assertEquals(torsion.classOf(),SimplicialCochain.cohomologyMap(FiniteSimplicialMap.identity(plane),z(2)).apply(torsion.classOf()));
        assertTrue(SimplicialCochain.cohomologyMap(map(plane,plane,0,0,0,0,0,0),z(2)).apply(torsion.classOf()).isZero());
    }
    @Test public void all64GraphsMatchIndependentConnectivityAndCycleCounts() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int mask=0;mask<64;mask++) {
            List<int[]> faces=new ArrayList<>(); int[] parent={0,1,2,3}; int components=4,count=0; for(int i=0;i<4;i++) faces.add(new int[]{i});
            for(int e=0;e<6;e++) if((mask&(1<<e))!=0) {
                faces.add(edges[e]); count++; int a=edges[e][0],b=edges[e][1]; while(a!=parent[a]) a=parent[a]; while(b!=parent[b]) b=parent[b]; if(a!=b) { parent[a]=b; components--; }
            }
            FiniteSimplicialComplex graph=complex(faces.toArray(new int[0][]));
            assertEquals(AbelianGroupType.free(z(components)),SimplicialCochain.cohomology(graph,z(0)).type());
            assertEquals(AbelianGroupType.free(z(count-4+components)),SimplicialCochain.cohomology(graph,z(1)).type());
            assertEquals(AbelianGroupType.ZERO,SimplicialCochain.cohomology(graph,z(2)).type());
        }
    }
    @Test public void emptyHugeDegreesAndExtremeLabelsRetainExactCoordinateConventions() {
        FiniteSimplicialComplex empty=complex(),point=complex(new int[]{7}); BigInteger huge=BigInteger.TEN.pow(100);
        assertTrue(SimplicialCochain.unit(empty).isZero()); assertTrue(SimplicialCochain.cohomologyDegrees(empty).isEmpty()); assertTrue(SimplicialCochain.basisCochains(empty,z(0)).isEmpty());
        SimplicialCochain high=SimplicialCochain.zero(point,huge); assertEquals(huge.add(z(1)),high.coboundary().degree()); assertEquals(huge.add(huge),high.cup(high).degree()); assertTrue(high.isCoboundary());
        assertTrue(SimplicialCochain.cohomologyMap(FiniteSimplicialMap.emptyTo(point),huge).isIsomorphism());
        AbelianGroupHomomorphism emptyPullback=SimplicialCochain.cohomologyMap(FiniteSimplicialMap.emptyTo(point),z(0)); assertEquals(AbelianGroupType.Z,emptyPullback.source().type()); assertEquals(AbelianGroupType.ZERO,emptyPullback.target().type());
        FiniteSimplicialComplex extreme=complex(new int[]{Integer.MAX_VALUE,Integer.MIN_VALUE}); assertEquals(v(7),c(extreme,0,-2,5).coboundary().coordinates());
        assertEquals(v(),SimplicialCochain.zero(point,z(0)).coboundingCoordinates()); assertFalse(SimplicialCochain.unit(point).isCoboundary());
    }
    @Test public void invalidBoundariesDegreesAndNoncocyclesAreRejectedWithoutConflatingEquality() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}),other=complex(new int[]{0},new int[]{1}); SimplicialCochain a=c(edge,0,0,1),b=c(edge,1,1);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c(edge,-1)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c(edge,1,1,2));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.add(b)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.cup(c(other,0,0,1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,a::classOf); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.cupClass(b));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.cohomologous(a)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.pullback(FiniteSimplicialMap.identity(other)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.evaluate(v(0))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialCochain.unit(edge).coboundingCoordinates());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.representative(SimplicialCochain.cohomology(other,z(0)).group().zero()));
        assertFalse(a.equals(c(other,0,0,1))); assertFalse(a.equals(b));
    }
    @Test public void flatOutputsAreImmutableAndLargeBasesReportImplementationFailure() {
        FiniteSimplicialComplex circle=circle(); SimplicialCochain zero=SimplicialCochain.zero(circle,z(1));
        assertEquals(zero,SimplicialCochain.zero(circle,z(1))); assertEquals(zero.hashCode(),SimplicialCochain.zero(circle,z(1)).hashCode());
        assertThrows(UnsupportedOperationException.class,() -> zero.coordinates().entries().clear()); assertThrows(UnsupportedOperationException.class,() -> zero.cocycleGenerators().clear());
        assertThrows(UnsupportedOperationException.class,() -> SimplicialCochain.basisCochains(circle,z(1)).clear());
        assertThrows(UnsupportedOperationException.class,() -> SimplicialCochain.cohomologyDegrees(circle).clear());
        assertThrows(UnsupportedOperationException.class,() -> SimplicialCochain.cohomologyMaps(FiniteSimplicialMap.identity(circle)).clear());
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<257;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex large=new FiniteSimplicialComplex(points);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialCochain.zero(large,z(0))); assertTrue(SimplicialCochain.zero(large,z(2)).isZero());
        for(int i=257;i<4097;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex tooLarge=new FiniteSimplicialComplex(points);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialCochain.zero(tooLarge,z(2)));
    }
    @Test public void nativeOperationsRetainActualFirstAndForeignWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex circle=circle();
        IAlgebraItem<SimplicialCochain> item=math.complexes.algebra().buildAlgebraItem(circle).performUnsafeOperation("SimplicialCochain.zero-on",z(1));
        item=item.performCustomMemberOperation("with-coordinates",v(1,0,0)).perform(); assertSame(math.cochains.algebra(),item.getAlgebra());
        assertSame(math.abelianGroupElements.algebra(),item.performAlgebraTransfer("class-of").getAlgebra()); assertSame(math.integralHomology.algebra(),item.performAlgebraTransfer("cohomology").getAlgebra());
        assertSame(math.cochains.algebra(),item.performCustomMemberOperation("pullback",FiniteSimplicialMap.identity(circle)).getAlgebra());
        for(IAlgebraItem<SimplicialCochain> generator : item.performOneOperandFlatOperation("cocycle-generators")) assertSame(math.cochains.algebra(),generator.getAlgebra());
        for(IAlgebraItem<SimplicialCochain> generator : math.complexes.algebra().buildAlgebraItem(circle).<SimplicialCochain,BigInteger>performUnsafeFlatOperation("SimplicialCochain.basis-on",z(1))) assertSame(math.cochains.algebra(),generator.getAlgebra());
    }
    @Test public void entireDegreeListsShareBudgetsEvenWhenEveryDegreeFitsIndividually() {
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int omit=0;omit<8;omit++) { List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); facets.add(new FiniteSet<>(face)); }
        for(int i=0;i<70;i++) facets.add(FiniteSet.of(100+i));
        FiniteSimplicialMap identity=FiniteSimplicialMap.identity(new FiniteSimplicialComplex(facets));
        for(int k=0;k<=6;k++) assertNotNull(SimplicialCochain.cohomologyMap(identity,z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialCochain.cohomologyMaps(identity));
        for(int i=70;i<180;i++) facets.add(FiniteSet.of(100+i)); FiniteSimplicialComplex larger=new FiniteSimplicialComplex(facets);
        for(int k=0;k<=6;k++) assertNotNull(SimplicialCochain.cohomology(larger,z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialCochain.cohomologyDegrees(larger));
    }
    @Test public void serializedCochainAndContravariantFlowsRemainRepeatable() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex circle=circle();
        IAlgebraFlow<IntegerVector> flow=math.flow(math.complexes,Collections.singletonList(circle))
                .<SimplicialCochain,BigInteger>performAlgebraUnsafe("SimplicialCochain.zero-on",z(1))
                .performCustomMemberOperation("with-coordinates",v(1,0,0)).performCustomMemberOperation("pullback",map(circle,circle,1,0,2))
                .<IntegerVector>performAlgebraTransfer("coordinates");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("[-1, 0, 0]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","true"),math.flow(math.simplicialMaps,Collections.singletonList(map(circle,circle,1,0,2)))
                .<AbelianGroupHomomorphism>performFlatAlgebraTransfer("SimplicialCochain.cohomology-maps").<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
