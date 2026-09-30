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

public class NativeRelativeCochainTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... entries) { BigInteger[] values=new BigInteger[entries.length]; for(int i=0;i<values.length;i++) values[i]=z(entries[i]); return new IntegerVector(values); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> values=new ArrayList<>(); for(int x : facet) values.add(x); faces.add(new FiniteSet<>(values)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialCochain c(RelativeSimplicialComplex pair,int degree,long... values) { return new RelativeSimplicialCochain(pair,z(degree),v(values)); }
    private static RelativeSimplicialComplex interval() { return new RelativeSimplicialComplex(complex(new int[]{0,1}),complex(new int[]{0},new int[]{1})); }
    private static FiniteSimplicialComplex plane() {
        return complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
    }
    private static RelativeSimplicialMap map(RelativeSimplicialComplex source,RelativeSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0;
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source.ambient()).members()) vertices.put(vertex,z(images[i++]));
        return new RelativeSimplicialMap(source,target,new FiniteSimplicialMap(source.ambient(),target.ambient(),vertices));
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void exact(AbelianGroupHomomorphism first,AbelianGroupHomomorphism second) {
        assertEquals(first.target(),second.source()); assertTrue(second.compose(first).isZero());
        for(AbelianGroupElement generator : second.kernelInclusion().generatorImages()) assertTrue(first.hasPreimage(generator));
    }
    private static void naturality(RelativeSimplicialMap map,int k) {
        List<AbelianGroupHomomorphism> from=RelativeSimplicialCochain.longExactSegment(map.target(),z(k)),to=RelativeSimplicialCochain.longExactSegment(map.source(),z(k)),vertical=RelativeSimplicialCochain.longExactMaps(map,z(k));
        assertEquals(4,vertical.size()); for(int i=0;i<3;i++) assertEquals(vertical.get(i+1).compose(from.get(i)),to.get(i).compose(vertical.get(i)));
        assertEquals(RelativeSimplicialCochain.cohomologyMap(map,z(k)),vertical.get(0));
        assertEquals(SimplicialCochain.cohomologyMap(map.ambientMap(),z(k)),vertical.get(1));
        assertEquals(SimplicialCochain.cohomologyMap(map.subcomplexMap(),z(k)),vertical.get(2));
        assertEquals(RelativeSimplicialCochain.cohomologyMap(map,z(k+1)),vertical.get(3));
    }
    @Test public void intervalConnectingCohomologyIsEndpointDifferenceAndRaisesDegree() {
        RelativeSimplicialComplex pair=interval(); SimplicialCochain values=new SimplicialCochain(pair.subcomplex(),z(0),v(2,7));
        RelativeSimplicialCochain connected=RelativeSimplicialCochain.connectCocycle(values,pair);
        assertEquals(c(pair,1,5),connected); assertEquals(z(1),connected.degree());
        AbelianGroupHomomorphism delta=RelativeSimplicialCochain.connectingCohomologyMap(pair,z(0));
        assertEquals(new IntegerMatrix(new BigInteger[][]{{z(-1),z(1)}}),delta.smithMatrix()); assertTrue(delta.isSurjective()); assertFalse(delta.isInjective());
        assertEquals(connected.classOf(),delta.apply(values.classOf()));
        assertEquals(AbelianGroupType.ZERO,RelativeSimplicialCochain.cohomology(pair,z(0)).type()); assertEquals(AbelianGroupType.Z,connected.cohomology().type());
        assertFalse(connected.isCoboundary()); assertEquals(connected.classOf(),connected.representative(connected.classOf()).classOf());
        assertEquals(z(15),connected.evaluate(v(3))); naturality(map(pair,pair,1,0),0);
    }
    @Test public void relativeDisksHaveOneTopCohomologyClassAndConnectingIsomorphisms() {
        for(int n=1;n<=4;n++) {
            int[] vertices=new int[n+1]; for(int i=0;i<=n;i++) vertices[i]=i; FiniteSimplicialComplex disk=complex(vertices);
            RelativeSimplicialComplex pair=new RelativeSimplicialComplex(disk,disk.skeleton(n-1));
            for(int k=0;k<=n+1;k++) assertEquals(k==n?AbelianGroupType.Z:AbelianGroupType.ZERO,RelativeSimplicialCochain.cohomology(pair,z(k)).type());
            if(n>1) assertTrue(RelativeSimplicialCochain.connectingCohomologyMap(pair,z(n-1)).isIsomorphism());
            for(SimplicialCochain generator : SimplicialCochain.zero(pair.subcomplex(),z(n-1)).cocycleGenerators())
                assertEquals(RelativeSimplicialCochain.connectCocycle(generator,pair).classOf(),RelativeSimplicialCochain.connectingCohomologyMap(pair,z(n-1)).apply(generator.classOf()));
        }
    }
    @Test public void longExactSequencesHaveEqualIntegralImagesAndKernelsIncludingTorsion() {
        FiniteSimplicialComplex plane=plane(); List<RelativeSimplicialComplex> pairs=Arrays.asList(interval(),new RelativeSimplicialComplex(plane,complex(new int[]{0})),
                new RelativeSimplicialComplex(plane,plane.skeleton(1)),RelativeSimplicialComplex.absolute(plane),RelativeSimplicialComplex.diagonal(plane),RelativeSimplicialComplex.absolute(complex()));
        for(RelativeSimplicialComplex pair : pairs) for(int k=0;k<=pair.ambient().dimension()+1;k++) {
            List<AbelianGroupHomomorphism> sequence=RelativeSimplicialCochain.longExactSegment(pair,z(k));
            assertEquals(RelativeSimplicialCochain.ambientCohomologyMap(pair,z(k)),sequence.get(0)); assertEquals(RelativeSimplicialCochain.restrictionCohomologyMap(pair,z(k)),sequence.get(1));
            assertEquals(RelativeSimplicialCochain.connectingCohomologyMap(pair,z(k)),sequence.get(2));
            exact(sequence.get(0),sequence.get(1)); exact(sequence.get(1),sequence.get(2)); exact(sequence.get(2),RelativeSimplicialCochain.ambientCohomologyMap(pair,z(k+1)));
        }
        RelativeSimplicialComplex skeleton=new RelativeSimplicialComplex(plane,plane.skeleton(1)); AbelianGroupHomomorphism delta=RelativeSimplicialCochain.connectingCohomologyMap(skeleton,z(1));
        assertTrue(delta.isInjective()); assertEquals(AbelianGroupType.cyclic(z(2)),delta.cokernel().type());
        RelativeSimplicialComplex pointed=new RelativeSimplicialComplex(plane,complex(new int[]{0})); RelativeSimplicialCochain torsion=RelativeSimplicialCochain.zero(pointed,z(2)).cocycleGenerators().get(0);
        assertEquals(z(2),torsion.classOf().order()); assertFalse(torsion.isCoboundary()); assertTrue(torsion.scale(z(2)).isCoboundary());
        assertEquals(torsion.classOf(),RelativeSimplicialCochain.absolute(SimplicialCochain.unit(plane)).cupClass(torsion.scale(z(3))));
        assertEquals(torsion.scale(z(2)),new RelativeSimplicialCochain(pointed,z(1),torsion.scale(z(2)).coboundingCoordinates()).coboundary());
        for(int k=0;k<=2;k++) { naturality(RelativeSimplicialMap.identity(pointed),k); naturality(map(pointed,pointed,0,0,0,0,0,0),k); }
    }
    @Test public void all1024GraphAndVertexSubcomplexPairsMatchIndependentRelativeBettiCounts() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int mask=0;mask<64;mask++) {
            List<int[]> faces=new ArrayList<>(); int[] parent={0,1,2,3}; int count=0,components=4; for(int i=0;i<4;i++) faces.add(new int[]{i});
            for(int e=0;e<6;e++) if((mask&(1<<e))!=0) {
                faces.add(edges[e]); count++; int a=edges[e][0],b=edges[e][1]; while(a!=parent[a]) a=parent[a]; while(b!=parent[b]) b=parent[b]; if(a!=b) { parent[a]=b; components--; }
            }
            FiniteSimplicialComplex graph=complex(faces.toArray(new int[0][]));
            for(int subset=0;subset<16;subset++) {
                List<int[]> points=new ArrayList<>(); Set<Integer> touched=new HashSet<>();
                for(int v=0;v<4;v++) if((subset&(1<<v))!=0) { points.add(new int[]{v}); int p=v; while(p!=parent[p]) p=parent[p]; touched.add(p); }
                RelativeSimplicialComplex pair=new RelativeSimplicialComplex(graph,complex(points.toArray(new int[0][])));
                assertEquals(AbelianGroupType.free(z(components-touched.size())),RelativeSimplicialCochain.cohomology(pair,z(0)).type());
                assertEquals(AbelianGroupType.free(z(count-4+components+points.size()-touched.size())),RelativeSimplicialCochain.cohomology(pair,z(1)).type());
            }
        }
    }
    @Test public void all27TrianglePairMapsAnd729CompositionsAreContravariant() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); RelativeSimplicialComplex pair=new RelativeSimplicialComplex(triangle,triangle.skeleton(0));
        RelativeSimplicialCochain first=c(pair,1,1,0,-1),second=c(pair,1,0,1,1); List<RelativeSimplicialMap> maps=new ArrayList<>(); List<AbelianGroupHomomorphism> induced=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int cc=0;cc<3;cc++) {
            RelativeSimplicialMap f=map(pair,pair,a,b,cc); maps.add(f); AbelianGroupHomomorphism h=RelativeSimplicialCochain.cohomologyMap(f,z(1)); induced.add(h);
            for(int vertex=1;vertex<=2;vertex++) {
                RelativeSimplicialCochain cochain=vertex==1?first:second;
                assertEquals(v((b==vertex?1:0)-(a==vertex?1:0),(cc==vertex?1:0)-(a==vertex?1:0),(cc==vertex?1:0)-(b==vertex?1:0)),cochain.pullback(f).coordinates());
                assertEquals(cochain.pullback(f).classOf(),h.apply(cochain.classOf()));
            }
            naturality(f,0); naturality(f,1);
        }
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) {
            assertEquals(induced.get(j).compose(induced.get(i)),RelativeSimplicialCochain.cohomologyMap(maps.get(i).compose(maps.get(j)),z(1)));
            assertEquals(first.pullback(maps.get(i)).pullback(maps.get(j)),first.pullback(maps.get(i).compose(maps.get(j))));
        }
    }
    @Test public void relativeCupOnTheSquareUsesTheUnionOfBothVanishingSubcomplexes() {
        FiniteSimplicialComplex square=complex(new int[]{0,1,2},new int[]{0,2,3});
        RelativeSimplicialComplex vertical=new RelativeSimplicialComplex(square,complex(new int[]{0,3},new int[]{1,2})),horizontal=new RelativeSimplicialComplex(square,complex(new int[]{0,1},new int[]{2,3}));
        RelativeSimplicialCochain x=c(vertical,1,1,1,-1),y=c(horizontal,1,1,1,1),product=x.cup(y);
        assertTrue(x.isCocycle()); assertTrue(y.isCocycle()); assertEquals(new RelativeSimplicialComplex(square,square.skeleton(1).intersection(complex(new int[]{0,1},new int[]{1,2},new int[]{2,3},new int[]{0,3}))),product.pair());
        assertEquals(v(1,0),product.coordinates()); assertEquals(v(0,-1),y.cup(x).coordinates()); assertEquals(z(1),product.evaluate(v(1,1)));
        assertTrue(product.cohomologous(y.cup(x).negate())); assertEquals(product.classOf(),y.cupClass(x).scale(z(-1))); assertFalse(product.classOf().isZero());
        assertEquals(AbelianGroupType.Z,product.cohomology().type()); assertEquals(x.extendByZero().cup(y.extendByZero()),product.extendByZero());
        assertEquals(x,RelativeSimplicialCochain.fromAbsolute(x.extendByZero(),vertical));
        assertEquals(x,RelativeSimplicialCochain.absolute(SimplicialCochain.unit(square)).cup(x));
        assertEquals(y,y.cup(RelativeSimplicialCochain.absolute(SimplicialCochain.unit(square))));
        int[] swap={0,3,2,1}; RelativeSimplicialMap fv=map(horizontal,vertical,swap),fh=map(vertical,horizontal,swap),boundary=map(product.pair(),product.pair(),swap);
        assertEquals(product.pullback(boundary).classOf(),x.pullback(fv).cupClass(y.pullback(fh)));
        assertEquals(product.classOf().scale(z(-1)),RelativeSimplicialCochain.cohomologyMap(boundary,z(2)).apply(product.classOf()));
    }
    @Test public void all729RelativeCochainPairsSatisfyLeibnizAndExtensionCompatibility() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); RelativeSimplicialComplex pair=new RelativeSimplicialComplex(triangle,complex(new int[]{0}));
        for(int a=0;a<27;a++) for(int b=0;b<27;b++) {
            long[] x=new long[3],y=new long[3]; int aa=a,bb=b; for(int i=0;i<3;i++) { x[i]=aa%3-1; aa/=3; y[i]=bb%3-1; bb/=3; }
            RelativeSimplicialCochain first=c(pair,1,x),second=c(pair,1,y),product=first.cup(second);
            assertEquals(v(x[0]*y[2]),product.coordinates()); assertEquals(first.extendByZero().cup(second.extendByZero()),product.extendByZero());
            assertEquals(first.coboundary().extendByZero(),first.extendByZero().coboundary()); assertTrue(first.coboundary().coboundary().isZero());
            RelativeSimplicialCochain zero=c(pair,0,x[0],x[1]);
            assertEquals(zero.cup(second).coboundary(),zero.coboundary().cup(second).add(zero.cup(second.coboundary())));
            assertEquals(zero.cup(first).cup(second),zero.cup(first.cup(second)));
        }
        RelativeSimplicialComplex tetra=new RelativeSimplicialComplex(complex(new int[]{0,1,2,3}),complex(new int[]{0}));
        RelativeSimplicialCochain a=c(tetra,1,1,-2,3,4,-5,6),b=c(tetra,1,2,1,-1,0,3,-2);
        assertEquals(a.cup(b).coboundary(),a.coboundary().cup(b).subtract(a.cup(b.coboundary())));
    }
    @Test public void differentPairsWithTheSameAmbientStillHaveDifferentCochainsAndMapBoundaries() {
        RelativeSimplicialComplex endpoints=interval(),based=new RelativeSimplicialComplex(endpoints.ambient(),complex(new int[]{0}));
        RelativeSimplicialCochain a=c(endpoints,1,1),b=c(based,1,1);
        assertNotEquals(a,b); assertTrue(b.isCoboundary()); assertFalse(a.isCoboundary());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.add(b)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.cohomologous(b));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.pullback(RelativeSimplicialMap.identity(based)));
        RelativeSimplicialMap inclusion=RelativeSimplicialMap.inclusion(based,endpoints); assertEquals(b,a.pullback(inclusion));
        assertTrue(RelativeSimplicialCochain.cohomologyMap(inclusion,z(1)).apply(a.classOf()).isZero()); naturality(inclusion,0); naturality(inclusion,1);
        assertNotEquals(inclusion.chainMatrix(z(1)).transpose().multiply(RelativeSimplicialCochain.connectingCochainMatrix(endpoints,z(0))),
                RelativeSimplicialCochain.connectingCochainMatrix(based,z(0)).multiply(inclusion.subcomplexMap().chainMatrix(z(0)).transpose()));
    }
    @Test public void excisionInducesContravariantCohomologyIsomorphismsAndCompatibleCochains() {
        SimplicialCover cover=new SimplicialCover(complex(new int[]{0,1},new int[]{1,2}),complex(new int[]{0,2})); RelativeSimplicialMap excision=cover.excisionMap();
        for(int k=0;k<=2;k++) {
            AbelianGroupHomomorphism f=RelativeSimplicialCochain.cohomologyMap(excision,z(k)); assertTrue(f.isIsomorphism());
            for(RelativeSimplicialCochain c : RelativeSimplicialCochain.basisCochains(excision.target(),z(k))) assertEquals(c.coordinates(),c.pullback(excision).coordinates());
        }
        assertFalse(excision.isIsomorphism());
    }
    @Test public void relativeBasesAreFilteredBeforeLimitsAndEmptyHighDegreesRemainTyped() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex ambient=new FiniteSimplicialComplex(points);
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(ambient),oneLeft=new RelativeSimplicialComplex(ambient,new FiniteSimplicialComplex(points.subList(0,299)));
        RelativeSimplicialCochain zero=c(diagonal,0); assertTrue(zero.cohomology().isAcyclic()); assertTrue(zero.cup(zero).isZero());
        assertTrue(RelativeSimplicialCochain.cohomologyMap(RelativeSimplicialMap.identity(diagonal),z(0)).isIsomorphism());
        assertEquals(AbelianGroupType.Z,c(oneLeft,0,1).cohomology().type()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,zero::extendByZero);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialCochain.longExactSegment(diagonal,z(0)));
        BigInteger huge=BigInteger.TEN.pow(100); RelativeSimplicialCochain high=RelativeSimplicialCochain.zero(oneLeft,huge);
        assertEquals(huge.add(z(1)),high.coboundary().degree()); assertEquals(huge.add(huge),high.cup(high).degree()); assertTrue(high.isCoboundary());
        RelativeSimplicialComplex empty=RelativeSimplicialComplex.absolute(complex()); assertTrue(RelativeSimplicialCochain.cohomologyDegrees(empty).isEmpty());
        assertTrue(RelativeSimplicialCochain.cohomologyMaps(RelativeSimplicialMap.identity(empty)).isEmpty()); assertEquals(4,RelativeSimplicialCochain.longExactMaps(RelativeSimplicialMap.identity(empty),huge).size());
    }
    @Test public void malformedCoordinatesNonvanishingRestrictionsAndNoncocyclesAreRejected() {
        RelativeSimplicialComplex pair=interval();
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c(pair,-1)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c(pair,1,1,2));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> RelativeSimplicialCochain.fromAbsolute(SimplicialCochain.unit(pair.ambient()),pair));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> RelativeSimplicialCochain.fromAbsolute(SimplicialCochain.unit(pair.subcomplex()),pair));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> RelativeSimplicialCochain.connectCocycle(SimplicialCochain.unit(pair.ambient()),pair));
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(pair.ambient()); SimplicialCochain noncocycle=new SimplicialCochain(pair.ambient(),z(0),v(1,2));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> RelativeSimplicialCochain.connectCocycle(noncocycle,diagonal));
        RelativeSimplicialCochain relative=RelativeSimplicialCochain.absolute(noncocycle);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,relative::classOf); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> relative.cohomologous(relative));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> relative.cupClass(relative)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c(pair,1,1).coboundingCoordinates());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> c(pair,1,1).evaluate(v()));
    }
    @Test public void nativeWrappersAndSerializedConnectingFlowsUseTheExistingAlgebras() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialComplex pair=interval();
        IAlgebraItem<RelativeSimplicialCochain> item=math.relativeComplexes.algebra().buildAlgebraItem(pair).performUnsafeOperation("RelativeCochain.zero-on",z(1));
        item=item.performCustomMemberOperation("with-coordinates",v(1)).perform(); assertSame(math.relativeCochains.algebra(),item.getAlgebra());
        assertSame(math.abelianGroupElements.algebra(),item.performAlgebraTransfer("class-of").getAlgebra()); assertSame(math.cochains.algebra(),item.performAlgebraTransfer("extend-by-zero").getAlgebra());
        for(IAlgebraItem<RelativeSimplicialCochain> generator : item.performOneOperandFlatOperation("cocycle-generators")) assertSame(math.relativeCochains.algebra(),generator.getAlgebra());
        for(IAlgebraItem<AbelianGroupHomomorphism> f : math.relativeComplexes.algebra().buildAlgebraItem(pair).<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("RelativeCochain.long-exact-segment",z(0))) assertSame(math.abelianHomomorphisms.algebra(),f.getAlgebra());
        IAlgebraFlow<IntegerVector> flow=math.flow(math.cochains,Collections.singletonList(new SimplicialCochain(pair.subcomplex(),z(0),v(0,1))))
                .<RelativeSimplicialCochain,RelativeSimplicialComplex>performAlgebraUnsafe("RelativeCochain.connect-cocycle",pair)
                .performCustomMemberOperation("pullback",map(pair,pair,1,0)).<IntegerVector>performAlgebraTransfer("coordinates");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("[-1]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","true","true","true"),math.flow(math.relativeMaps,Collections.singletonList(map(pair,pair,1,0)))
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("RelativeCochain.long-exact-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
    @Test public void flatResultsAreImmutableAndValuesRetainPairDegreeAndCoordinates() {
        RelativeSimplicialComplex pair=interval(); RelativeSimplicialCochain a=c(pair,1,1); assertEquals(a,c(pair,1,1)); assertEquals(a.hashCode(),c(pair,1,1).hashCode());
        assertThrows(UnsupportedOperationException.class,() -> a.coordinates().entries().clear()); assertThrows(UnsupportedOperationException.class,() -> a.cocycleGenerators().clear());
        assertThrows(UnsupportedOperationException.class,() -> RelativeSimplicialCochain.basisCochains(pair,z(1)).clear());
        assertThrows(UnsupportedOperationException.class,() -> RelativeSimplicialCochain.cohomologyDegrees(pair).clear());
        assertThrows(UnsupportedOperationException.class,() -> RelativeSimplicialCochain.cohomologyMaps(RelativeSimplicialMap.identity(pair)).clear());
        assertThrows(UnsupportedOperationException.class,() -> RelativeSimplicialCochain.longExactSegment(pair,z(0)).clear());
        assertThrows(UnsupportedOperationException.class,() -> RelativeSimplicialCochain.longExactMaps(RelativeSimplicialMap.identity(pair),z(0)).clear());
    }
    @Test public void degreeListsExactSegmentsAndNaturalityListsShareTheirWholeWorkBudget() {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int omit=0;omit<8;omit++) { List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); faces.add(new FiniteSet<>(face)); }
        for(int i=0;i<70;i++) faces.add(FiniteSet.of(100+i)); RelativeSimplicialMap identity=RelativeSimplicialMap.identity(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(faces)));
        for(int k=0;k<=6;k++) assertNotNull(RelativeSimplicialCochain.cohomologyMap(identity,z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialCochain.cohomologyMaps(identity));
        for(int i=70;i<180;i++) faces.add(FiniteSet.of(100+i)); RelativeSimplicialComplex larger=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(faces));
        for(int k=0;k<=6;k++) assertNotNull(RelativeSimplicialCochain.cohomology(larger,z(k)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialCochain.cohomologyDegrees(larger));
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<110;i++) points.add(FiniteSet.of(i));
        RelativeSimplicialComplex split=new RelativeSimplicialComplex(new FiniteSimplicialComplex(points),new FiniteSimplicialComplex(points.subList(0,55)));
        assertNotNull(RelativeSimplicialCochain.ambientCohomologyMap(split,z(0))); assertNotNull(RelativeSimplicialCochain.restrictionCohomologyMap(split,z(0))); assertNotNull(RelativeSimplicialCochain.connectingCohomologyMap(split,z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialCochain.longExactSegment(split,z(0)));
        RelativeSimplicialMap costly=RelativeSimplicialMap.identity(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(points.subList(0,80))));
        assertNotNull(RelativeSimplicialCochain.cohomologyMap(costly,z(0))); assertNotNull(SimplicialCochain.cohomologyMap(costly.ambientMap(),z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> RelativeSimplicialCochain.longExactMaps(costly,z(0)));
    }
}
