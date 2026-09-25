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

public class NativeSimplicialChainTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static SimplicialChain chain(FiniteSimplicialComplex complex,int degree,long... values) { return new SimplicialChain(complex,z(degree),v(values)); }
    private static SimplicialCochain cochain(FiniteSimplicialComplex complex,int degree,long... values) { return new SimplicialCochain(complex,z(degree),v(values)); }
    private static FiniteSimplicialComplex circle() { return complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); }
    private static FiniteSimplicialMap map(FiniteSimplicialComplex from,FiniteSimplicialComplex to,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0; for(BigInteger vertex : FiniteSimplicialMap.vertexSet(from).members()) vertices.put(vertex,z(images[i++]));
        return new FiniteSimplicialMap(from,to,vertices);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    @Test public void boundariesPairingsAndIntegralFillingsRetainTheFullComplex() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); SimplicialChain face=chain(triangle,2,3),boundary=face.boundary();
        assertEquals(v(3,-3,3),boundary.coordinates()); assertTrue(boundary.isCycle()); assertTrue(boundary.isBoundary()); assertTrue(boundary.boundary().isZero());
        assertEquals(boundary,chain(triangle,2,0).withCoordinates(boundary.boundingCoordinates()).boundary());
        SimplicialCochain phi=cochain(triangle,1,2,7,11); assertEquals(z(18),boundary.evaluate(phi)); assertEquals(boundary.evaluate(phi),face.evaluate(phi.coboundary()));
        assertEquals(z(0),boundary.boundary().augmentation()); assertEquals(z(5),chain(triangle,0,2,-1,4).augmentation());
    }
    @Test public void all729TernaryCochainsGiveIndependentCapCoefficientsAndSignedDifferentials() {
        FiniteSimplicialComplex tetra=complex(new int[]{0,1,2,3}); SimplicialChain top=chain(tetra,3,2),faces=chain(tetra,2,1,-2,3,4);
        SimplicialCochain b=cochain(tetra,1,2,3,-1,4,5,-2),f=cochain(tetra,0,1,-1,2,3);
        for(int code=0;code<729;code++) {
            long[] entries=new long[6]; int digits=code; for(int i=0;i<6;i++) { entries[i]=digits%3-1; digits/=3; }
            SimplicialCochain a=cochain(tetra,1,entries);
            assertEquals(v(0,0,0,2*entries[0]),top.cap(a).coordinates());
            assertEquals(v(0,0,0,entries[0],-2*entries[0],3*entries[1]+4*entries[3]),faces.cap(a).coordinates());
            assertEquals(top.cap(a).coordinates(),SimplicialChain.capMatrix(a,z(3)).multiply(top.coordinates()));
            assertEquals(faces.cap(a).coordinates(),faces.capCohomologyMatrix(z(1)).multiply(a.coordinates()));
            assertEquals(top.cap(a).boundary(),top.boundary().cap(a).subtract(top.cap(a.coboundary())).negate());
            assertEquals(top.cap(f).boundary(),top.boundary().cap(f).subtract(top.cap(f.coboundary())));
            assertEquals(top.cap(a).cap(b),top.cap(a.cup(b))); assertEquals(top,top.cap(SimplicialCochain.unit(tetra)));
            assertEquals(top.cap(a.cup(b)).boundary(),top.boundary().cap(a.cup(b)).subtract(top.cap(a.cup(b).coboundary())));
        }
    }
    private static int[][] torusTriangles() {
        List<int[]> faces=new ArrayList<>();
        for(int i=0;i<3;i++) for(int j=0;j<3;j++) { int a=3*i+j,b=3*((i+1)%3)+j,c=3*((i+1)%3)+(j+1)%3,d=3*i+(j+1)%3; faces.add(new int[]{a,b,c}); faces.add(new int[]{a,c,d}); }
        return faces.toArray(new int[0][]);
    }
    private static SimplicialChain torusFundamental(FiniteSimplicialComplex torus) {
        List<FiniteSet<BigInteger>> basis=FiniteSimplicialMap.simplexBasis(torus,z(2)); long[] values=new long[basis.size()];
        for(int[] triangle : torusTriangles()) {
            int sign=1; for(int i=0;i<3;i++) for(int j=i+1;j<3;j++) if(triangle[i]>triangle[j]) sign=-sign;
            values[basis.indexOf(FiniteSet.of(z(triangle[0]),z(triangle[1]),z(triangle[2])))]=sign;
        }
        return chain(torus,2,values);
    }
    private static SimplicialCochain torusCocycle(FiniteSimplicialComplex torus,boolean horizontal) {
        List<FiniteSet<BigInteger>> edges=FiniteSimplicialMap.simplexBasis(torus,z(1)); long[] values=new long[edges.size()];
        for(int i=0;i<values.length;i++) {
            List<BigInteger> edge=new ArrayList<>(edges.get(i).members()); Collections.sort(edge); int a=edge.get(0).intValue(),b=edge.get(1).intValue();
            a=horizontal?a/3:a%3; b=horizontal?b/3:b%3; values[i]=a==2 && b==0?1:a==0 && b==2?-1:0;
        }
        return cochain(torus,1,values);
    }
    @Test public void torusFundamentalCycleGivesIntegralDualityWithTheExpectedIntersectionSigns() {
        FiniteSimplicialComplex torus=complex(torusTriangles()); SimplicialChain fundamental=torusFundamental(torus);
        SimplicialCochain a=torusCocycle(torus,true),b=torusCocycle(torus,false);
        assertTrue(fundamental.isCycle()); assertEquals(z(1),fundamental.cap(a).evaluate(b)); assertEquals(z(-1),fundamental.cap(b).evaluate(a));
        assertEquals(z(0),fundamental.cap(a).evaluate(a)); assertEquals(z(0),fundamental.cap(b).evaluate(b));
        assertEquals(fundamental.evaluate(a.cup(b)),fundamental.cap(a).cap(b).augmentation());
        for(int k=0;k<=2;k++) assertTrue(fundamental.capCohomologyMap(z(k)).isIsomorphism());
        assertEquals(fundamental.capClass(a),fundamental.capCohomologyMap(z(1)).apply(a.classOf()));
        assertEquals(fundamental.capClass(a),SimplicialChain.capHomologyMap(a,z(2)).apply(fundamental.classOf()));
        assertFalse(fundamental.scale(z(2)).capCohomologyMap(z(1)).isSurjective());
        assertEquals(AbelianGroupType.cyclic(z(2)).directSum(AbelianGroupType.cyclic(z(2))),fundamental.scale(z(2)).capCohomologyMap(z(1)).cokernel().type());
    }
    @Test public void sphereFundamentalCyclesGiveDualityThroughDimensionFour() {
        for(int n=1;n<=4;n++) {
            List<int[]> facets=new ArrayList<>(); for(int omit=0;omit<n+2;omit++) { int[] face=new int[n+1]; int p=0; for(int i=0;i<n+2;i++) if(i!=omit) face[p++]=i; facets.add(face); }
            FiniteSimplicialComplex sphere=complex(facets.toArray(new int[0][])); List<FiniteSet<BigInteger>> basis=FiniteSimplicialMap.simplexBasis(sphere,z(n)); long[] values=new long[basis.size()];
            for(int i=0;i<basis.size();i++) for(int vertex=0;vertex<n+2;vertex++) if(!basis.get(i).contains(z(vertex))) values[i]=(vertex&1)==0?1:-1;
            SimplicialChain fundamental=chain(sphere,n,values); assertTrue(fundamental.isCycle());
            for(int k=0;k<=n;k++) assertTrue(fundamental.capCohomologyMap(z(k)).isIsomorphism());
        }
    }
    @Test public void capClassesAndBothInducedMapsIgnoreBoundaryRepresentatives() {
        FiniteSimplicialComplex torus=complex(torusTriangles()); SimplicialChain fundamental=torusFundamental(torus); SimplicialCochain a=torusCocycle(torus,true);
        SimplicialCochain changed=a.add(cochain(torus,0,2,-1,0,3,1,4,-2,5,7).coboundary());
        assertEquals(fundamental.capClass(a),fundamental.capClass(changed)); assertTrue(fundamental.cap(a).homologous(fundamental.cap(changed)));
        assertEquals(SimplicialChain.capHomologyMap(a,z(2)),SimplicialChain.capHomologyMap(changed,z(2)));
        FiniteSimplicialComplex withFace=complex(new int[]{0,1,2},new int[]{2,3},new int[]{0,3});
        SimplicialChain cycle=chain(withFace,1,1,0,-1,1,1),shifted=cycle.add(chain(withFace,2,3).boundary());
        assertTrue(cycle.homologous(shifted)); assertEquals(cycle.classOf(),shifted.classOf());
        assertEquals(cycle.capCohomologyMap(z(1)),shifted.capCohomologyMap(z(1)));
    }
    @Test public void torsionCyclesAndTheCochainUnitRetainIntegralPresentations() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialChain torsion=SimplicialChain.zero(plane,z(1)).cycleGenerators().get(0); SimplicialCochain unit=SimplicialCochain.unit(plane);
        assertFalse(torsion.isBoundary()); assertTrue(torsion.scale(z(2)).isBoundary()); assertEquals(AbelianGroupType.cyclic(z(2)),torsion.homology().type());
        assertEquals(torsion,torsion.cap(unit)); assertEquals(torsion.classOf(),torsion.capClass(unit));
        assertEquals(AbelianGroupHomomorphism.identity(torsion.homology().group()),SimplicialChain.capHomologyMap(unit,z(1)));
        assertEquals(torsion.classOf(),torsion.capCohomologyMap(z(0)).apply(unit.classOf())); assertTrue(torsion.capCohomologyMap(z(0)).isSurjective());
        assertEquals(torsion,torsion.representative(torsion.classOf()));
    }
    @Test public void all27CircleMapsAnd729CompositionsPreservePushforwardAndCapNaturalityOnClasses() {
        FiniteSimplicialComplex circle=circle(); SimplicialChain cycle=chain(circle,1,1,-1,1); SimplicialCochain phi=cochain(circle,1,1,0,0);
        List<FiniteSimplicialMap> maps=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) {
            FiniteSimplicialMap f=map(circle,circle,a,b,c); maps.add(f);
            assertEquals(cycle.pushforward(f).capClass(phi),cycle.cap(phi.pullback(f)).pushforward(f).classOf());
            assertEquals(cycle.pushforward(f).evaluate(phi),cycle.evaluate(phi.pullback(f)));
            assertEquals(cycle.pushforward(f).classOf(),f.homologyMap(z(1)).apply(cycle.classOf()));
        }
        SimplicialChain vertices=chain(circle,0,2,-1,3);
        for(FiniteSimplicialMap f : maps) for(FiniteSimplicialMap g : maps) {
            assertEquals(vertices.pushforward(f.compose(g)),vertices.pushforward(g).pushforward(f));
            assertEquals(cycle.pushforward(f.compose(g)),cycle.pushforward(g).pushforward(f));
        }
    }
    @Test public void all256TetrahedronMapsCommuteWithBoundaryAndAreAdjointToPullback() {
        FiniteSimplicialComplex tetra=complex(new int[]{0,1,2,3}); SimplicialChain chain=chain(tetra,2,1,-2,3,4); SimplicialCochain phi=cochain(tetra,2,5,-1,7,2);
        for(int a=0;a<4;a++) for(int b=0;b<4;b++) for(int c=0;c<4;c++) for(int d=0;d<4;d++) {
            FiniteSimplicialMap f=map(tetra,tetra,a,b,c,d);
            assertEquals(chain.pushforward(f).boundary(),chain.boundary().pushforward(f));
            assertEquals(chain.pushforward(f).evaluate(phi),chain.evaluate(phi.pullback(f)));
        }
    }
    @Test public void capNaturalityForUnorderedVertexMapsRequiresPassingToHomology() {
        FiniteSimplicialComplex circle=circle(); SimplicialChain cycle=chain(circle,1,1,-1,1); SimplicialCochain phi=cochain(circle,1,1,0,0); FiniteSimplicialMap flip=map(circle,circle,1,0,2);
        SimplicialChain left=cycle.pushforward(flip).cap(phi),right=cycle.cap(phi.pullback(flip)).pushforward(flip);
        assertNotEquals(left,right); assertTrue(left.homologous(right));
    }
    @Test public void emptyNegativeAndHugeDegreesFollowTheUnreducedConvention() {
        FiniteSimplicialComplex point=complex(new int[]{0}),empty=complex(); BigInteger huge=BigInteger.TEN.pow(100);
        SimplicialChain vertex=chain(point,0,1),negative=vertex.boundary();
        assertEquals(z(-1),negative.degree()); assertEquals(IntegerVector.zero(0),negative.coordinates()); assertTrue(negative.isCycle()); assertTrue(negative.isBoundary());
        assertTrue(negative.homology().isAcyclic()); assertTrue(negative.classOf().isZero()); assertTrue(negative.boundary().isZero());
        assertEquals(negative,negative.pushforward(FiniteSimplicialMap.identity(point))); assertEquals(negative,negative.representative(negative.classOf()));
        for(BigInteger degree : Arrays.asList(huge,huge.negate(),z(-1))) {
            SimplicialChain zero=SimplicialChain.zero(point,degree); assertTrue(zero.isZero()); assertTrue(zero.homology().isAcyclic()); assertTrue(zero.cap(SimplicialCochain.unit(point)).isZero());
            assertTrue(SimplicialChain.basisChains(point,degree).isEmpty());
        }
        assertTrue(vertex.cap(SimplicialCochain.zero(point,huge)).isZero()); assertTrue(vertex.capCohomologyMap(huge).isZero());
        assertTrue(SimplicialChain.zero(empty,z(0)).cycleGenerators().isEmpty()); assertEquals(z(0),SimplicialChain.zero(empty,z(0)).augmentation());
    }
    @Test public void wrongContextsDegreesAndNoncyclesAreMathematicallyUndefined() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}),other=complex(new int[]{1,2}); SimplicialChain c=chain(edge,1,1); SimplicialCochain noncocycle=cochain(edge,0,0,1);
        for(Runnable action : Arrays.<Runnable>asList(() -> c.add(chain(other,1,1)),() -> c.add(chain(edge,0,1,0)),() -> c.cap(cochain(other,1,1)),
                () -> c.evaluate(noncocycle),() -> c.capClass(cochain(edge,1,1)),() -> c.capCohomologyMap(z(0)),() -> SimplicialChain.capHomologyMap(noncocycle,z(1)),
                () -> c.capCohomologyMatrix(z(-1)),() -> c.pushforward(FiniteSimplicialMap.identity(other)),() -> c.classOf(),() -> c.homologous(c),
                () -> c.augmentation(),() -> new SimplicialChain(edge,z(-1),v(1)),() -> c.withCoordinates(v(1,2)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> chain(edge,0,1,0).boundingCoordinates());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> chain(edge,0,1,0).capClass(noncocycle));
    }
    @Test public void simplexAndRequiredBasisLimitsFailWithoutFalsePredicates() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<4097;i++) points.add(FiniteSet.of(i));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChain.zero(new FiniteSimplicialComplex(points),z(2)));
        FiniteSimplicialComplex large=new FiniteSimplicialComplex(points.subList(0,257));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChain.zero(large,z(0)));
        SimplicialChain zero=SimplicialChain.zero(large,z(1)); assertTrue(zero.isZero());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,zero::boundary); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,zero::isCycle);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> zero.capCohomologyMatrix(z(0)));
    }
    @Test public void wrappersRetainTheChainAlgebraAndSerializedCapFlowsAreRepeatable() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex circle=circle(); SimplicialChain cycle=chain(circle,1,1,-1,1); SimplicialCochain phi=cochain(circle,1,1,0,0);
        IAlgebraItem<SimplicialChain> item=math.simplicialChains.algebra().buildAlgebraItem(cycle);
        IAlgebraItem<SimplicialChain> capped=item.performCustomMemberOperation("cap",phi); assertSame(math.simplicialChains.algebra(),capped.getAlgebra()); assertEquals(cycle.cap(phi),capped.perform().getResult());
        assertSame(math.abelianGroupElements.algebra(),math.simplicialChains.algebra().buildAlgebraItem(cycle).performUnsafeOperation("cap-class",phi).getAlgebra());
        assertSame(math.abelianHomomorphisms.algebra(),math.cochains.algebra().buildAlgebraItem(phi).performUnsafeOperation("SimplicialChain.cap-homology-map",z(1)).getAlgebra());
        for(IAlgebraItem<SimplicialChain> generator : math.simplicialChains.algebra().buildAlgebraItem(cycle).<SimplicialChain>performOneOperandFlatOperation("cycle-generators")) assertSame(math.simplicialChains.algebra(),generator.getAlgebra());
        IAlgebraFlow<BigInteger> flow=math.flow(math.simplicialChains,Collections.singletonList(cycle)).performCustomMemberOperation("cap",phi).<BigInteger>performAlgebraTransfer("augmentation");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("1"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Collections.singletonList("[1, -1, 1]"),math.flow(math.simplicialChains,Collections.singletonList(cycle)).performOneOperandFlatOperation("cycle-generators").<IntegerVector>performAlgebraTransfer("coordinates").collect());
    }
    @Test public void inducedCapMapsShareOneBudgetAcrossBothModelsAndInduction() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<100;i++) points.add(FiniteSet.of(i));
        FiniteSimplicialComplex complex=new FiniteSimplicialComplex(points); SimplicialChain zero=SimplicialChain.zero(complex,z(0)); SimplicialCochain unit=SimplicialCochain.unit(complex);
        assertEquals(AbelianGroupType.free(z(100)),zero.homology().type()); assertEquals(AbelianGroupType.free(z(100)),unit.cohomology().type());
        assertEquals(IntegerMatrix.identity(100),SimplicialChain.capMatrix(unit,z(0))); assertEquals(IntegerMatrix.zero(100,100),zero.capCohomologyMatrix(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChain.capHomologyMap(unit,z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> zero.capCohomologyMap(z(0)));
    }
    @Test public void basisAndGeneratorListsAreImmutableAndClassRepresentativesStayCycles() {
        FiniteSimplicialComplex circle=circle(); SimplicialChain zero=SimplicialChain.zero(circle,z(1));
        assertThrows(UnsupportedOperationException.class,() -> SimplicialChain.basisChains(circle,z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> zero.cycleGenerators().clear());
        assertEquals(3,SimplicialChain.basisChains(circle,z(1)).size()); assertEquals(1,zero.cycleGenerators().size());
        SimplicialChain cycle=zero.cycleGenerators().get(0); assertTrue(cycle.representative(cycle.classOf()).isCycle()); assertEquals(cycle.classOf(),cycle.representative(cycle.classOf()).classOf());
        assertEquals(zero,SimplicialChain.zero(circle,z(1))); assertEquals(zero.hashCode(),SimplicialChain.zero(circle,z(1)).hashCode());
    }
}
