package operations;

import algebra.concrete.*;
import algebra.IAlgebraItem;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.structures.*;
import mathematics.topology.*;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class NativeSimplicialChainMapClassTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> sets=new ArrayList<>(); for(int[] f : facets) { List<Integer> values=new ArrayList<>(); for(int v : f) values.add(v); sets.add(new FiniteSet<>(values)); } return new FiniteSimplicialComplex(sets);
    }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return abs(complex(f)); }
    private static RelativeSimplicialComplex edge() { return abs(complex(new int[]{0,1})); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    private static IntegerMatrix matrix(long[]... rows) {
        BigInteger[][] values=new BigInteger[rows.length][]; for(int r=0;r<rows.length;r++) { values[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) values[r][c]=z(rows[r][c]); } return new IntegerMatrix(values);
    }
    private static SimplicialChainMapClass pointClass(IntegerMatrix m) { return SimplicialChainMapClass.fromMap(new SimplicialChainMap(points(m.columns()),points(m.rows()),Collections.singletonList(m))); }
    private static MathFailure failure(MathFailure.Kind kind,Runnable body) { MathFailure e=assertThrows(MathFailure.class,body::run); assertEquals(kind,e.kind()); return e; }
    private static SimplicialChainMapClass roundTrip(SimplicialChainMap map) {
        SimplicialChainMapClass value=SimplicialChainMapClass.fromMap(map);
        assertEquals(map.source(),value.source()); assertEquals(map.target(),value.target());
        assertEquals(value.space().classOf(map),value.element()); assertEquals(value,SimplicialChainMapClass.fromElement(value.space(),value.element()));
        assertEquals(value,SimplicialChainMapClass.fromMap(value.representative())); assertEquals(value.hashCode(),SimplicialChainMapClass.fromMap(value.representative()).hashCode());
        assertEquals(value.space().representative(value.element()),value.representative()); return value;
    }
    @Test public void all6561TernaryPointCompositionsUseIndependentCoordinateSums() {
        List<SimplicialChainMapClass> classes=new ArrayList<>();
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) classes.add(pointClass(matrix(new long[]{a,b},new long[]{c,d})));
        for(SimplicialChainMapClass left : classes) for(SimplicialChainMapClass right : classes) {
            IntegerMatrix a=left.representative().chainMatrix(z(0)),b=right.representative().chainMatrix(z(0)); BigInteger[][] expected=new BigInteger[2][2];
            for(int r=0;r<2;r++) for(int c=0;c<2;c++) { expected[r][c]=BigInteger.ZERO; for(int k=0;k<2;k++) expected[r][c]=expected[r][c].add(a.get(r,k).multiply(b.get(k,c))); }
            assertEquals(new IntegerMatrix(expected),left.compose(right).representative().chainMatrix(z(0)));
        }
    }
    @Test public void compositionIsAssociativeBilinearAndUnitalButNotCommutative() {
        SimplicialChainMapClass a=pointClass(matrix(new long[]{1,2},new long[]{0,1})),b=pointClass(matrix(new long[]{1,0},new long[]{3,1})),c=pointClass(matrix(new long[]{2,1},new long[]{1,-1}));
        assertNotEquals(a.compose(b),b.compose(a)); assertEquals(a.compose(b).compose(c),a.compose(b.compose(c)));
        assertEquals(a.homologyMap(z(0)).compose(b.homologyMap(z(0))),a.compose(b).homologyMap(z(0)));
        assertEquals(b.cohomologyMap(z(0)).compose(a.cohomologyMap(z(0))),a.compose(b).cohomologyMap(z(0)));
        assertNotEquals(a.cohomologyMap(z(0)).compose(b.cohomologyMap(z(0))),a.compose(b).cohomologyMap(z(0)));
        assertEquals(a.add(b).compose(c),a.compose(c).add(b.compose(c))); assertEquals(a.compose(b.add(c)),a.compose(b).add(a.compose(c)));
        SimplicialChainMapClass id=SimplicialChainMapClass.identityOn(points(2)),zero=SimplicialChainMapClass.zeroIn(a.space());
        assertEquals(a,id.compose(a)); assertEquals(a,a.compose(id)); assertEquals(zero,a.compose(zero)); assertEquals(zero,zero.compose(a));
        assertEquals(zero,a.add(a.negate())); assertEquals(zero,a.subtract(a)); assertEquals(a.add(a),a.scale(z(2)));
        BigInteger huge=BigInteger.ONE.shiftLeft(1024).add(z(1)); assertEquals(a.scale(huge).compose(b),a.compose(b).scale(huge));
        assertFalse(a.hasFiniteOrder()); failure(MathFailure.Kind.OPERATION_UNDEFINED,a::order); assertTrue(zero.hasFiniteOrder()); assertEquals(z(1),zero.order());
    }
    @Test public void rectangularCompositionAndTheExistingSpaceActionsAgree() {
        SimplicialChainMapClass a=pointClass(matrix(new long[]{1,2},new long[]{3,4},new long[]{5,6})),b=pointClass(matrix(new long[]{2},new long[]{3}));
        SimplicialChainMapClass product=a.compose(b); assertEquals(points(1),product.source()); assertEquals(points(3),product.target());
        assertEquals(matrix(new long[]{8},new long[]{18},new long[]{28}),product.representative().chainMatrix(z(0)));
        assertEquals(product.element(),a.space().precomposeMap(b.representative()).apply(a.element())); assertEquals(product.element(),b.space().postcomposeMap(a.representative()).apply(b.element()));
    }
    private static SimplicialChainMap intervalMap(int x,int y,int augmentation) {
        return new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{x,y},new long[]{augmentation-x,augmentation-y}),matrix(new long[]{x-y})));
    }
    @Test public void intervalHomotopiesGiveEqualHashKeysAndDeterministicRepresentatives() {
        Map<Integer,SimplicialChainMapClass> byAugmentation=new HashMap<>(); Set<SimplicialChainMapClass> distinct=new HashSet<>();
        for(int n=-2;n<=2;n++) for(int x=-2;x<=2;x++) for(int y=-2;y<=2;y++) {
            SimplicialChainMapClass value=roundTrip(intervalMap(x,y,n)); distinct.add(value);
            if(byAugmentation.containsKey(n)) { assertEquals(byAugmentation.get(n),value); assertEquals(byAugmentation.get(n).representative(),value.representative()); } else byAugmentation.put(n,value);
            assertEquals(n==0,value.isZero()); assertEquals(n==1,value.isIdentity());
        }
        assertEquals(5,distinct.size());
        SimplicialChainMap original=SimplicialChainMap.identity(edge()); SimplicialChainMapClass id=SimplicialChainMapClass.fromMap(original);
        assertNotEquals(original,id.representative()); assertFalse(id.representative().isIdentity()); assertTrue(id.isIdentity());
    }
    private static SimplicialChainMap circleMap(int h0,int h1) {
        return new SimplicialChainMap(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3).scale(z(h0)),matrix(new long[]{h1,0,0},new long[]{h0-h1,h0,0},new long[]{h1-h0,0,h0})));
    }
    @Test public void circleClassesComposeBothIndependentHomologyDegrees() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) {
            SimplicialChainMapClass x=roundTrip(circleMap(a,b)),y=SimplicialChainMapClass.fromMap(circleMap(2,-3));
            assertEquals(SimplicialChainMapClass.fromMap(circleMap(2*a,-3*b)),x.compose(y));
            for(int k=0;k<2;k++) {
                assertEquals(circleMap(a,b).homologyMap(z(k)),x.homologyMap(z(k))); assertEquals(circleMap(a,b).cohomologyMap(z(k)),x.cohomologyMap(z(k)));
                assertEquals(x.homologyMap(z(k)).compose(y.homologyMap(z(k))),x.compose(y).homologyMap(z(k)));
                assertEquals(y.cohomologyMap(z(k)).compose(x.cohomologyMap(z(k))),x.compose(y).cohomologyMap(z(k)));
            }
        }
    }
    private static RelativeSimplicialComplex projectivePlane() { return abs(complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5})); }
    private static RelativeSimplicialComplex disk() { FiniteSimplicialComplex d=complex(new int[]{0,1,2}); return new RelativeSimplicialComplex(d,d.skeleton(1)); }
    private static SimplicialChainMap torsionMap(int n) {
        BigInteger[][] top=new BigInteger[1][10]; Arrays.fill(top[0],BigInteger.ZERO); top[0][0]=z(n);
        return new SimplicialChainMap(projectivePlane(),disk(),Arrays.asList(IntegerMatrix.zero(0,6),IntegerMatrix.zero(0,15),new IntegerMatrix(top)));
    }
    @Test public void torsionClassesRemainDistinctDespiteEqualHomologyMapsAndNonadditiveLifts() {
        SimplicialChainMapClass odd=roundTrip(torsionMap(1)),zero=SimplicialChainMapClass.zeroIn(odd.space());
        assertFalse(odd.isZero()); assertEquals(z(2),odd.order()); assertTrue(odd.hasFiniteOrder()); assertFalse(odd.isIdentity());
        assertEquals(odd,roundTrip(torsionMap(3))); assertEquals(zero,roundTrip(torsionMap(2))); assertEquals(zero,odd.add(odd)); assertEquals(odd,odd.negate());
        assertFalse(odd.representative().add(odd.representative()).isZero()); assertTrue(odd.add(odd).representative().isZero());
        assertEquals(zero.homologyMaps(),odd.homologyMaps()); assertNotEquals(zero,odd);
        assertNotEquals(zero.cohomologyMap(z(2)),odd.cohomologyMap(z(2)));
        SimplicialChainMapClass targetIdentity=SimplicialChainMapClass.identityOn(disk());
        for(int n=-4;n<=4;n++) assertEquals(n%2==0?zero:odd,targetIdentity.scale(z(n)).compose(odd));
        assertEquals(odd,odd.scale(BigInteger.ONE.shiftLeft(1024).add(BigInteger.ONE)));
    }
    @Test public void generatorListsRetainMixedTorsionAndFreeContexts() {
        FiniteSimplicialComplex sphere=complex(new int[]{6,7,8},new int[]{6,7,9},new int[]{6,8,9},new int[]{7,8,9});
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(projectivePlane().ambient().union(sphere)),disk()); List<SimplicialChainMapClass> generators=SimplicialChainMapClass.generatorsIn(space);
        assertEquals(2,generators.size()); assertEquals(z(2),generators.get(0).order()); assertFalse(generators.get(1).hasFiniteOrder());
        for(SimplicialChainMapClass value : generators) { assertEquals(space,value.space()); assertEquals(value,roundTrip(value.representative())); }
        Set<SimplicialChainMapClass> values=new HashSet<>(); for(int a=0;a<2;a++) for(int b=-2;b<=2;b++) assertTrue(values.add(generators.get(0).scale(z(a)).add(generators.get(1).scale(z(b)))));
        assertThrows(UnsupportedOperationException.class,generators::clear);
    }
    @Test public void fullEndpointPairsGuardEqualityAdditionAndCompositionEvenForZeroGroups() {
        RelativeSimplicialComplex based0=new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{0})),based1=new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{1}));
        SimplicialChainMapClass a=SimplicialChainMapClass.identityOn(based0),b=SimplicialChainMapClass.identityOn(based1);
        assertTrue(a.isZero()); assertTrue(b.isZero()); assertTrue(a.isIdentity()); assertTrue(b.isIdentity()); assertNotEquals(a,b);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.add(b)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.subtract(b)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.compose(b));
        SimplicialChainMapClass otherLabel=SimplicialChainMapClass.identityOn(abs(complex(new int[]{7}))); assertNotEquals(SimplicialChainMapClass.identityOn(points(1)),otherLabel);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> otherLabel.compose(SimplicialChainMapClass.identityOn(points(1))));
    }
    @Test public void elementConstructionRequiresTheExactPresentationAndDegreesAreNonnegative() {
        SimplicialChainMapClass id=SimplicialChainMapClass.identityOn(edge());
        PresentedAbelianGroup wrong=PresentedAbelianGroup.fromType(id.element().group().type()); assertNotEquals(wrong,id.element().group());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialChainMapClass.fromElement(id.space(),wrong.zero()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> id.homologyMap(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> id.cohomologyMap(z(-1)));
        assertTrue(id.homologyMap(BigInteger.ONE.shiftLeft(64)).isZero()); assertTrue(id.cohomologyMap(BigInteger.ONE.shiftLeft(64)).isZero());
    }
    @Test public void emptyAndFilteredContextsKeepZeroShapesAndZeroIdentityClasses() {
        for(RelativeSimplicialComplex source : Arrays.asList(points(0),points(1),RelativeSimplicialComplex.diagonal(edge().ambient()))) for(RelativeSimplicialComplex target : Arrays.asList(points(0),points(1))) {
            SimplicialChainMapSpace space=new SimplicialChainMapSpace(source,target); SimplicialChainMapClass zero=SimplicialChainMapClass.zeroIn(space);
            assertTrue(zero.isZero()); assertEquals(space.zero(),zero.representative()); assertEquals(zero,roundTrip(zero.representative()));
            assertEquals(zero,SimplicialChainMapClass.identityOn(target).compose(zero)); assertEquals(zero,zero.compose(SimplicialChainMapClass.identityOn(source)));
            assertEquals(source.equals(points(1))&&target.equals(points(1))?1:0,SimplicialChainMapClass.generatorsIn(space).size());
        }
        assertTrue(SimplicialChainMapClass.identityOn(points(0)).isZero()); assertTrue(SimplicialChainMapClass.identityOn(points(0)).isIdentity());
        assertEquals(Collections.emptyList(),SimplicialChainMapClass.identityOn(points(0)).homologyMaps());
    }
    @Test public void differingDimensionsKeepFunctorialIntegralHomologyAndCohomology() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); SimplicialChainMap inclusion=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(edge(),triangle));
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); vertices.put(z(0),z(0)); vertices.put(z(1),z(1)); vertices.put(z(2),z(0));
        SimplicialChainMap retraction=SimplicialChainMap.fromAbsoluteSimplicial(new FiniteSimplicialMap(triangle.ambient(),edge().ambient(),vertices));
        SimplicialChainMapClass a=roundTrip(inclusion),b=roundTrip(retraction);
        assertEquals(SimplicialChainMapClass.identityOn(edge()),b.compose(a)); assertEquals(SimplicialChainMapClass.identityOn(triangle),a.compose(b));
        for(int k=0;k<=3;k++) { assertEquals(b.homologyMap(z(k)).compose(a.homologyMap(z(k))),b.compose(a).homologyMap(z(k))); assertEquals(a.cohomologyMap(z(k)).compose(b.cohomologyMap(z(k))),b.compose(a).cohomologyMap(z(k))); }
        assertEquals(3,a.homologyMaps().size()); assertEquals(3,b.cohomologyMaps().size());
    }
    @Test public void outputShapeAndWorkLimitsFailWithoutReturningFalseOrPartialLists() {
        SimplicialChainMapSpace large=new SimplicialChainMapSpace(points(17),points(17));
        for(Runnable operation : Arrays.<Runnable>asList(() -> SimplicialChainMapClass.zeroIn(large),() -> SimplicialChainMapClass.generatorsIn(large),() -> SimplicialChainMapClass.fromMap(large.zero())))
            assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,operation).getMessage().contains("256"));
        SimplicialChainMapClass left=pointClass(IntegerMatrix.zero(17,1)),right=pointClass(IntegerMatrix.zero(1,17));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> left.compose(right)).getMessage().contains("256"));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> left.compose(left));
        SimplicialChainMapSpace dense=new SimplicialChainMapSpace(points(16),points(16));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainMapClass.zeroIn(dense)).getMessage().contains("5000000"));
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<9;i++) for(int j=i+1;j<9;j++) if(facets.size()<29) facets.add(FiniteSet.of(i,j));
        SimplicialChainMapSpace many=new SimplicialChainMapSpace(circle(),abs(new FiniteSimplicialComplex(facets))); assertEquals(AbelianGroupType.free(z(22)),many.homotopyType());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> SimplicialChainMapClass.generatorsIn(many)).getMessage().contains("5000000"));
    }
    @Test public void nativeClassesScalarCompositionAndFlatMapsUseActualWrappersAndSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainMapClass odd=SimplicialChainMapClass.fromMap(torsionMap(1)),id=SimplicialChainMapClass.identityOn(disk());
        IAlgebraItem<SimplicialChainMapClass> item=math.chainMaps.algebra().buildAlgebraItem(torsionMap(1)).performAlgebraTransfer("ChainMapClass.from-map");
        assertSame(math.chainMapClasses.algebra(),item.getAlgebra()); assertEquals(odd,item.perform().getResult()); assertSame(math.chainMapClasses.algebra(),item.performCustomMemberOperation("scale",z(3)).getAlgebra());
        assertSame(math.chainMapClasses.algebra(),math.chainMapSpaces.algebra().buildAlgebraItem(odd.space()).performUnsafeOperation("ChainMapClass.from-element",odd.element()).getAlgebra());
        assertSame(math.chainMaps.algebra(),item.performAlgebraTransfer("representative").getAlgebra()); assertSame(math.abelianHomomorphisms.algebra(),item.performUnsafeOperation("homology-map",z(0)).getAlgebra());
        IAlgebraFlow<BigInteger> orders=math.flow(math.chainMapClasses,Collections.singletonList(id)).performOperation("compose",odd).performAlgebraTransfer("order");
        IAlgebraFlow<SimplicialChainMapClass> generators=math.flow(math.chainMapSpaces,Collections.singletonList(odd.space())).performFlatAlgebraTransfer("ChainMapClass.generators-in");
        IAlgebraFlow<AbelianGroupHomomorphism> maps=math.flow(math.chainMapClasses,Collections.singletonList(odd)).performFlatAlgebraTransfer("homology-maps");
        for(IAlgebraFlow<?> original : Arrays.asList(orders,generators,maps)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("2"),orders.collect()); assertEquals(1,generators.collect().size()); assertEquals(3,maps.collect().size());
    }
}
