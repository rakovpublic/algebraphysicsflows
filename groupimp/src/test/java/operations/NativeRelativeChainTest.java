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

public class NativeRelativeChainTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialChain chain(RelativeSimplicialComplex pair,int degree,long... values) { return new RelativeSimplicialChain(pair,z(degree),v(values)); }
    private static RelativeSimplicialCochain cochain(RelativeSimplicialComplex pair,int degree,long... values) { return new RelativeSimplicialCochain(pair,z(degree),v(values)); }
    private static RelativeSimplicialComplex interval() { return new RelativeSimplicialComplex(complex(new int[]{0,1}),complex(new int[]{0},new int[]{1})); }
    private static RelativeSimplicialMap map(RelativeSimplicialComplex from,RelativeSimplicialComplex to,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0; for(BigInteger vertex : FiniteSimplicialMap.vertexSet(from.ambient()).members()) vertices.put(vertex,z(images[i++]));
        return new RelativeSimplicialMap(from,to,new FiniteSimplicialMap(from.ambient(),to.ambient(),vertices));
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    @Test public void intervalCapLandsInAbsoluteChainsAndConnectingCycleIsEndpointDifference() {
        RelativeSimplicialComplex pair=interval(); RelativeSimplicialChain c=chain(pair,1,3); RelativeSimplicialCochain phi=cochain(pair,1,2);
        assertTrue(c.isCycle()); assertFalse(c.isBoundary()); assertEquals(v(-3,3),c.connectCycle().coordinates()); assertTrue(c.connectCycle().isCycle());
        assertEquals(pair.subcomplex(),c.connectCycle().complex()); assertEquals(c.connectCycle().classOf(),pair.connectingHomology(z(1)).apply(c.classOf()));
        assertEquals(v(0,6),c.relativeCap(phi).coordinates()); assertEquals(z(6),c.evaluate(phi)); assertEquals(c.evaluate(phi),c.relativeCap(phi).augmentation());
        assertEquals(c.relativeCapClass(phi),c.relativeCapHomologyMap(phi).apply(c.classOf()));
        assertEquals(c.relativeCapClass(phi),c.relativeCapCohomologyMap(z(1)).apply(phi.classOf()));
        assertEquals(c,c.cap(SimplicialCochain.unit(pair.ambient()))); assertEquals(AbelianGroupHomomorphism.identity(c.homology().group()),c.capHomologyMap(SimplicialCochain.unit(pair.ambient())));
    }
    @Test public void disksRelativeToTheirBoundariesGiveBothLefschetzMapsThroughDimensionFour() {
        for(int n=1;n<=4;n++) {
            int[] simplex=new int[n+1]; for(int i=0;i<=n;i++) simplex[i]=i; List<int[]> boundary=new ArrayList<>();
            for(int omit=0;omit<=n;omit++) { int[] face=new int[n]; int p=0; for(int i=0;i<=n;i++) if(i!=omit) face[p++]=i; boundary.add(face); }
            RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(simplex),complex(boundary.toArray(new int[0][])));
            RelativeSimplicialChain fundamental=chain(pair,n,1); RelativeSimplicialCochain top=cochain(pair,n,1);
            for(int p=0;p<=n;p++) { assertTrue(fundamental.capCohomologyMap(z(p)).isIsomorphism()); assertTrue(fundamental.relativeCapCohomologyMap(z(p)).isIsomorphism()); }
            assertTrue(fundamental.relativeCapHomologyMap(top).isIsomorphism()); assertEquals(z(1),fundamental.relativeCap(top).augmentation());
            assertTrue(fundamental.connectCycle().isCycle()); assertFalse(fundamental.connectCycle().classOf().isZero());
        }
    }
    @Test public void annulusRelativeFundamentalCycleGivesNonzeroDegreeOneDuality() {
        List<int[]> triangles=new ArrayList<>(),edges=new ArrayList<>();
        for(int i=0;i<3;i++) { int j=(i+1)%3; triangles.add(new int[]{i,j,j+3}); triangles.add(new int[]{i,j+3,i+3}); edges.add(new int[]{i,j}); edges.add(new int[]{i+3,j+3}); }
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(triangles.toArray(new int[0][])),complex(edges.toArray(new int[0][])));
        List<FiniteSet<BigInteger>> basis=pair.simplexBasis(z(2)); long[] values=new long[basis.size()];
        for(int[] triangle : triangles) { int sign=1; for(int i=0;i<3;i++) for(int j=i+1;j<3;j++) if(triangle[i]>triangle[j]) sign=-sign; values[basis.indexOf(FiniteSet.of(z(triangle[0]),z(triangle[1]),z(triangle[2])))]=sign; }
        RelativeSimplicialChain fundamental=chain(pair,2,values); assertTrue(fundamental.isCycle());
        SimplicialCochain radial=new SimplicialCochain(pair.ambient(),z(0),v(0,0,0,1,1,1));
        RelativeSimplicialCochain phi=RelativeSimplicialCochain.fromAbsolute(radial.coboundary(),pair);
        assertFalse(phi.classOf().isZero()); assertTrue(phi.isCocycle()); assertEquals(AbelianGroupType.Z,phi.cohomology().type());
        assertFalse(fundamental.relativeCapClass(phi).isZero()); assertEquals(AbelianGroupType.Z,fundamental.relativeCap(phi).homology().type());
        for(int p=0;p<=2;p++) { assertTrue(fundamental.capCohomologyMap(z(p)).isIsomorphism()); assertTrue(fundamental.relativeCapCohomologyMap(z(p)).isIsomorphism()); }
        assertEquals(AbelianGroupType.cyclic(z(2)),fundamental.scale(z(2)).relativeCapCohomologyMap(z(1)).cokernel().type());
        RelativeSimplicialMap flip=map(pair,pair,3,5,4,0,2,1);
        assertEquals(fundamental.pushforward(flip).relativeCapClass(phi),fundamental.relativeCap(phi.pullback(flip)).pushforward(flip.ambientMap()).classOf());
    }
    @Test public void all729RelativeChainCochainPairsMatchIndependentCapCoefficients() {
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(new int[]{0,1,2}),complex(new int[]{2}));
        for(int a=0;a<27;a++) for(int b=0;b<27;b++) {
            long[] x=new long[3],y=new long[3]; int aa=a,bb=b; for(int i=0;i<3;i++) { x[i]=aa%3-1; aa/=3; y[i]=bb%3-1; bb/=3; }
            RelativeSimplicialChain c=chain(pair,1,x),face=chain(pair,2,x[0]); RelativeSimplicialCochain phi=cochain(pair,1,y);
            assertEquals(v(0,x[0]*y[0],x[1]*y[1]+x[2]*y[2]),c.relativeCap(phi).coordinates());
            assertEquals(v(0,x[0]*y[0]),c.cap(phi.extendByZero()).coordinates());
            assertEquals(c.relativeCap(phi).coordinates(),c.relativeCapMatrix(phi).multiply(c.coordinates()));
            assertEquals(c.relativeCap(phi).coordinates(),c.relativeCapCohomologyMatrix(z(1)).multiply(phi.coordinates()));
            assertEquals(c.cap(phi.extendByZero()).coordinates(),c.capMatrix(phi.extendByZero()).multiply(c.coordinates()));
            assertEquals(c.cap(phi.extendByZero()).coordinates(),c.capCohomologyMatrix(z(1)).multiply(phi.extendByZero().coordinates()));
            assertEquals(face.relativeCap(phi).boundary(),face.boundary().relativeCap(phi).subtract(face.relativeCap(phi.coboundary())).negate());
            assertEquals(face.cap(phi.extendByZero()).boundary(),face.boundary().cap(phi.extendByZero()).subtract(face.cap(phi.extendByZero().coboundary())).negate());
            assertEquals(c.evaluate(phi),c.relativeCap(phi).augmentation()); assertEquals(c.boundary().evaluate(cochain(pair,0,2,-1)),c.evaluate(cochain(pair,0,2,-1).coboundary()));
        }
    }
    @Test public void relativeFrontFacesVanishButAbsoluteBackFacesMustBeRetained() {
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(new int[]{0,1,2}),complex(new int[]{1,2})); RelativeSimplicialChain c=chain(pair,2,3);
        RelativeSimplicialCochain phi=cochain(pair,1,2,5); assertEquals(v(0,0,6),c.relativeCap(phi).coordinates());
        assertTrue(c.cap(phi.extendByZero()).isZero()); // The same back edge lies in A and vanishes only in the relative target.
        assertEquals(c.relativeCap(phi).boundary(),c.boundary().relativeCap(phi).subtract(c.relativeCap(phi.coboundary())).negate());
        RelativeSimplicialComplex other=new RelativeSimplicialComplex(pair.ambient(),complex(new int[]{0,1}));
        assertTrue(chain(other,2,3).relativeCap(cochain(other,1,2,5)).isZero()); // Its front edge lies in A.
    }
    @Test public void quotientProjectionIsAChainMapButItsZeroLiftIsOnlyASection() {
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(new int[]{0,1,2}),complex(new int[]{1,2}));
        SimplicialChain first=new SimplicialChain(pair.ambient(),z(1),v(1,-1,0)),second=first.add(new SimplicialChain(pair.ambient(),z(1),v(0,0,7)));
        RelativeSimplicialChain c=RelativeSimplicialChain.fromAbsolute(first,pair); assertEquals(c,RelativeSimplicialChain.fromAbsolute(second,pair));
        assertEquals(c.boundary(),RelativeSimplicialChain.fromAbsolute(first.boundary(),pair)); assertEquals(c,RelativeSimplicialChain.fromAbsolute(c.liftAbsolute(),pair));
        assertTrue(c.isCycle()); assertFalse(c.liftAbsolute().isCycle()); assertNotEquals(c.liftAbsolute().boundary(),c.boundary().liftAbsolute());
        assertEquals(v(1,-1),c.connectCycle().coordinates()); assertTrue(c.connectCycle().isBoundary()); assertTrue(c.isBoundary());
        assertEquals(c,chain(pair,2,0).withCoordinates(c.boundingCoordinates()).boundary());
        RelativeSimplicialCochain phi=cochain(pair,1,2,5); assertEquals(first.cap(phi.extendByZero()),second.cap(phi.extendByZero()));
    }
    @Test public void all27PairMapsAnd729CompositionsPreservePairingConnectingClassesAndCapNaturality() {
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(new int[]{0,1,2}),complex(new int[]{0},new int[]{1},new int[]{2}));
        RelativeSimplicialChain c=chain(pair,1,2,-1,3); RelativeSimplicialCochain phi=cochain(pair,1,1,3,2); List<RelativeSimplicialMap> maps=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int d=0;d<3;d++) {
            RelativeSimplicialMap f=map(pair,pair,a,b,d); maps.add(f); RelativeSimplicialChain pushed=c.pushforward(f);
            assertEquals(c.evaluate(phi.pullback(f)),pushed.evaluate(phi)); assertEquals(c.boundary().pushforward(f),pushed.boundary());
            assertEquals(pushed.classOf(),f.homologyMap(z(1)).apply(c.classOf()));
            assertEquals(pushed.connectCycle().classOf(),c.connectCycle().pushforward(f.subcomplexMap()).classOf());
            assertEquals(pushed.relativeCapClass(phi),c.relativeCap(phi.pullback(f)).pushforward(f.ambientMap()).classOf());
            SimplicialCochain unit=SimplicialCochain.unit(pair.ambient()); assertEquals(pushed.capClass(unit),c.cap(unit.pullback(f.ambientMap())).pushforward(f).classOf());
        }
        for(RelativeSimplicialMap f : maps) for(RelativeSimplicialMap g : maps) assertEquals(c.pushforward(f.compose(g)),c.pushforward(g).pushforward(f));
    }
    @Test public void capProductsAndInducedMapsAreIndependentOfBothRepresentatives() {
        RelativeSimplicialComplex square=new RelativeSimplicialComplex(complex(new int[]{0,1,2},new int[]{0,2,3}),complex(new int[]{0,1},new int[]{1,2},new int[]{2,3},new int[]{0,3}));
        RelativeSimplicialChain fundamental=chain(square,2,1,1); RelativeSimplicialCochain phi=cochain(square,2,1,0),shifted=phi.add(cochain(square,1,7).coboundary());
        assertEquals(fundamental.relativeCapClass(phi),fundamental.relativeCapClass(shifted)); assertEquals(fundamental.relativeCapHomologyMap(phi),fundamental.relativeCapHomologyMap(shifted));
        assertTrue(fundamental.relativeCap(phi).homologous(fundamental.relativeCap(shifted)));
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(complex(new int[]{0,1,2}),complex(new int[]{0},new int[]{1},new int[]{2}));
        RelativeSimplicialChain c=chain(pair,1,2,-1,3),changed=c.add(chain(pair,2,5).boundary());
        assertTrue(c.homologous(changed)); assertEquals(c.classOf(),changed.classOf()); assertEquals(c.relativeCapCohomologyMap(z(1)),changed.relativeCapCohomologyMap(z(1)));
        assertEquals(c.capCohomologyMap(z(0)),changed.capCohomologyMap(z(0)));
    }
    @Test public void capModuleAssociativityAndAbsoluteEmbeddingAgreeWithExistingOperations() {
        FiniteSimplicialComplex tetra=complex(new int[]{0,1,2,3}); RelativeSimplicialComplex pair=new RelativeSimplicialComplex(tetra,complex(new int[]{3}));
        RelativeSimplicialChain c=chain(pair,3,2); SimplicialCochain a=new SimplicialCochain(tetra,z(1),v(1,2,-1,3,4,-2)),b=new SimplicialCochain(tetra,z(1),v(-1,2,3,1,-2,4));
        RelativeSimplicialCochain phi=RelativeSimplicialCochain.fromAbsolute(a,pair);
        assertEquals(c.cap(a).cap(b),c.cap(a.cup(b))); assertEquals(c.relativeCap(phi).cap(b),c.relativeCap(phi.cup(RelativeSimplicialCochain.absolute(b))));
        SimplicialChain absolute=new SimplicialChain(tetra,z(3),v(2)); RelativeSimplicialChain embedded=RelativeSimplicialChain.absolute(absolute);
        assertEquals(absolute,embedded.liftAbsolute()); assertEquals(RelativeSimplicialChain.absolute(absolute.cap(a)),embedded.cap(a));
        assertEquals(absolute.cap(a),embedded.relativeCap(RelativeSimplicialCochain.absolute(a)));
    }
    @Test public void projectivePlaneRelativeChainsRetainTorsionAndIntegralFillings() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(plane,complex(new int[]{0})); RelativeSimplicialChain torsion=RelativeSimplicialChain.zero(pair,z(1)).cycleGenerators().get(0);
        assertEquals(AbelianGroupType.cyclic(z(2)),torsion.homology().type()); assertFalse(torsion.isBoundary()); assertTrue(torsion.scale(z(2)).isBoundary());
        assertEquals(torsion.scale(z(2)),RelativeSimplicialChain.zero(pair,z(2)).withCoordinates(torsion.scale(z(2)).boundingCoordinates()).boundary());
        assertEquals(torsion.classOf(),torsion.capClass(SimplicialCochain.unit(plane))); assertEquals(torsion.classOf(),torsion.representative(torsion.classOf()).classOf());
        assertTrue(torsion.connectCycle().isZero());
    }
    @Test public void filteredRelativeBasesAvoidUnnecessaryAmbientChainLimits() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex ambient=new FiniteSimplicialComplex(points);
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(ambient,new FiniteSimplicialComplex(points.subList(0,299))); RelativeSimplicialChain c=chain(pair,0,1);
        assertEquals(AbelianGroupType.Z,c.homology().type()); assertTrue(c.isCycle()); assertEquals(c,c.pushforward(RelativeSimplicialMap.identity(pair)));
        assertTrue(c.cap(SimplicialCochain.zero(ambient,z(1))).isZero()); assertTrue(c.relativeCap(cochain(pair,1)).isZero());
        assertEquals(IntegerMatrix.zero(0,0),c.relativeCapCohomologyMatrix(z(1))); assertTrue(c.connectCycle().isZero());
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,c::liftAbsolute); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> c.relativeCap(cochain(pair,0,1)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> c.capCohomologyMatrix(z(0)));
        RelativeSimplicialChain zero=chain(RelativeSimplicialComplex.diagonal(ambient),0); assertTrue(zero.homology().isAcyclic()); assertTrue(zero.cycleGenerators().isEmpty());
    }
    @Test public void wrongPairNoncyclesAndInvalidCoordinatesAreRejected() {
        RelativeSimplicialComplex pair=interval(),based=new RelativeSimplicialComplex(pair.ambient(),complex(new int[]{0})); RelativeSimplicialChain c=chain(pair,1,1),noncycle=chain(based,1,1);
        RelativeSimplicialCochain phi=cochain(pair,1,1); SimplicialCochain noncocycle=new SimplicialCochain(pair.ambient(),z(0),v(0,1));
        for(Runnable action : Arrays.<Runnable>asList(() -> c.add(noncycle),() -> c.relativeCap(cochain(based,1,1)),() -> c.pushforward(RelativeSimplicialMap.identity(based)),
                () -> c.evaluate(cochain(pair,0)),() -> noncycle.connectCycle(),() -> noncycle.classOf(),() -> noncycle.relativeCapClass(cochain(based,1,1)),
                () -> c.capHomologyMap(noncocycle),() -> c.capClass(noncocycle),() -> noncycle.capCohomologyMap(z(0)),() -> c.relativeCapCohomologyMap(z(-1)),
                () -> new RelativeSimplicialChain(pair,z(-1),v(1)),() -> c.withCoordinates(v()),() -> c.cap(SimplicialCochain.unit(complex(new int[]{7}))),
                () -> RelativeSimplicialChain.fromAbsolute(SimplicialChain.zero(complex(new int[]{7}),z(1)),pair))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,c::boundingCoordinates); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> noncycle.homologous(noncycle));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> noncycle.relativeCapHomologyMap(cochain(based,0,1)));
        assertEquals(c.relativeCapHomologyMap(phi),c.scale(z(7)).relativeCapHomologyMap(phi));
    }
    @Test public void emptyNegativeHugeDegreesAndImmutableOutputsRetainTheirContext() {
        RelativeSimplicialComplex pair=interval(); BigInteger huge=BigInteger.TEN.pow(100); RelativeSimplicialChain c=chain(pair,0);
        assertEquals(z(-1),c.boundary().degree()); assertTrue(c.boundary().classOf().isZero()); assertTrue(c.connectCycle().isZero());
        for(BigInteger degree : Arrays.asList(huge,huge.negate(),z(-1))) { RelativeSimplicialChain zero=RelativeSimplicialChain.zero(pair,degree); assertTrue(zero.homology().isAcyclic()); assertTrue(zero.isBoundary()); assertTrue(zero.connectCycle().isZero()); assertEquals(zero,zero.pushforward(RelativeSimplicialMap.identity(pair))); }
        assertTrue(chain(pair,1,1).relativeCap(RelativeSimplicialCochain.zero(pair,huge)).isZero()); assertTrue(chain(pair,1,1).relativeCapCohomologyMap(huge).isZero());
        assertThrows(UnsupportedOperationException.class,() -> RelativeSimplicialChain.basisChains(pair,z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> chain(pair,1,1).cycleGenerators().clear());
        assertEquals(chain(pair,1,1),chain(pair,1,1)); assertEquals(chain(pair,1,1).hashCode(),chain(pair,1,1).hashCode());
        assertTrue(RelativeSimplicialChain.zero(RelativeSimplicialComplex.absolute(complex()),z(0)).cycleGenerators().isEmpty());
    }
    @Test public void inducedCapMapsShareBudgetsAcrossModelsAndPreserveFailureKinds() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<257;i++) points.add(FiniteSet.of(i));
        RelativeSimplicialComplex large=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(points)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialChain.zero(large,z(0)));
        RelativeSimplicialComplex pair=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(points.subList(0,100))); RelativeSimplicialChain zero=RelativeSimplicialChain.zero(pair,z(0));
        SimplicialCochain unit=SimplicialCochain.unit(pair.ambient()); RelativeSimplicialCochain relative=RelativeSimplicialCochain.absolute(unit);
        assertEquals(AbelianGroupType.free(z(100)),zero.homology().type()); assertEquals(AbelianGroupType.free(z(100)),relative.cohomology().type());
        assertEquals(IntegerMatrix.identity(100),zero.capMatrix(unit)); assertEquals(IntegerMatrix.identity(100),zero.relativeCapMatrix(relative));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> zero.capHomologyMap(unit)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> zero.relativeCapHomologyMap(relative));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> zero.capCohomologyMap(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> zero.relativeCapCohomologyMap(z(0)));
    }
    @Test public void nativeWrappersAndSerializedRelativeCapFlowsUseTheOriginalArchitecture() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=interval(); RelativeSimplicialChain c=chain(pair,1,1); RelativeSimplicialCochain phi=cochain(pair,1,3);
        IAlgebraItem<RelativeSimplicialChain> item=math.relativeChains.algebra().buildAlgebraItem(c).performCustomMemberOperation("cap",SimplicialCochain.unit(pair.ambient()));
        assertSame(math.relativeChains.algebra(),item.getAlgebra()); assertEquals(c,item.perform().getResult());
        assertSame(math.simplicialChains.algebra(),math.relativeChains.algebra().buildAlgebraItem(c).performUnsafeOperation("relative-cap",phi).getAlgebra());
        assertSame(math.simplicialChains.algebra(),math.relativeChains.algebra().buildAlgebraItem(c).performAlgebraTransfer("connect-cycle").getAlgebra());
        for(IAlgebraItem<RelativeSimplicialChain> generator : math.relativeChains.algebra().buildAlgebraItem(c).performOneOperandFlatOperation("cycle-generators")) assertSame(math.relativeChains.algebra(),generator.getAlgebra());
        IAlgebraFlow<BigInteger> flow=math.flow(math.relativeChains,Collections.singletonList(c)).<SimplicialChain,RelativeSimplicialCochain>performAlgebraUnsafe("relative-cap",phi).<BigInteger>performAlgebraTransfer("augmentation");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("3"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Collections.singletonList("[-1, 1]"),math.flow(math.relativeChains,Collections.singletonList(c)).<SimplicialChain>performAlgebraTransfer("connect-cycle").<IntegerVector>performAlgebraTransfer("coordinates").collect());
    }
}
