package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.structures.AbelianGroupHomomorphism;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialChainHomotopyTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static IntegerMatrix matrix(long[]... entries) {
        BigInteger[][] values=new BigInteger[entries.length][]; for(int r=0;r<entries.length;r++) { values[r]=new BigInteger[entries[r].length]; for(int c=0;c<entries[r].length;c++) values[r][c]=z(entries[r][c]); } return new IntegerMatrix(values);
    }
    private static IntegerMatrix difference(IntegerMatrix a,IntegerMatrix b) { return a.add(b.scale(z(-1))); }
    private static void failure(MathFailure.Kind kind,Runnable body) { assertEquals(kind,assertThrows(MathFailure.class,body::run).kind()); }
    private static RelativeSimplicialComplex point() { return abs(complex(new int[]{0})); }
    private static RelativeSimplicialComplex edge() { return abs(complex(new int[]{0,1})); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    private static SimplicialChainHomotopy loop() {
        SimplicialChainMap f=new SimplicialChainMap(point(),circle(),Arrays.asList(matrix(new long[]{1},new long[]{0},new long[]{0}),IntegerMatrix.zero(3,0)));
        return new SimplicialChainHomotopy(f,f,Arrays.asList(matrix(new long[]{1},new long[]{-1},new long[]{1}),IntegerMatrix.zero(0,0)));
    }
    private static void identities(SimplicialChainHomotopy h) {
        int top=Math.max(h.source().ambient().dimension(),h.target().ambient().dimension());
        for(int k=0;k<=top+1;k++) {
            BigInteger d=z(k); IntegerMatrix previous=h.cochainMatrix(d).transpose();
            assertEquals(difference(h.to().chainMatrix(d),h.from().chainMatrix(d)),h.target().boundaryMatrix(z(k+1)).multiply(h.chainMatrix(d)).add(previous.multiply(h.source().boundaryMatrix(d))));
            assertEquals(difference(h.to().cochainMatrix(d),h.from().cochainMatrix(d)),h.source().boundaryMatrix(d).transpose().multiply(h.cochainMatrix(d)).add(h.cochainMatrix(z(k+1)).multiply(h.target().boundaryMatrix(z(k+1)).transpose())));
        }
    }
    private static void equalInduced(SimplicialChainHomotopy h,int degree) {
        for(List<AbelianGroupHomomorphism> maps : Arrays.asList(h.homologyMaps(z(degree)),h.cohomologyMaps(z(degree)))) assertEquals(maps.get(0),maps.get(1));
    }
    @Test public void all2601TernaryIntervalCandidatesAgreeWithIndependentEndpointEquations() {
        List<SimplicialChainMap> maps=new ArrayList<>();
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) for(int e=-1;e<=1;e++)
            if(b-a==-e && d-c==e) maps.add(new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{a,b},new long[]{c,d}),matrix(new long[]{e}))));
        assertEquals(17,maps.size()); int checked=0,accepted=0;
        for(SimplicialChainMap f : maps) for(SimplicialChainMap g : maps) for(int u=-1;u<=1;u++) for(int v=-1;v<=1;v++) {
            IntegerMatrix df=difference(g.chainMatrix(z(0)),f.chainMatrix(z(0))),de=difference(g.chainMatrix(z(1)),f.chainMatrix(z(1)));
            boolean valid=df.equals(matrix(new long[]{-u,-v},new long[]{u,v})) && de.get(0,0).equals(z(v-u));
            List<IntegerMatrix> values=Arrays.asList(matrix(new long[]{u,v}),IntegerMatrix.zero(0,1)); checked++;
            if(valid) { SimplicialChainHomotopy h=new SimplicialChainHomotopy(f,g,values); identities(h); accepted++; }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainHomotopy(f,g,values));
        }
        assertEquals(2601,checked); assertTrue(accepted>17);
    }
    @Test public void all729TwoDegreeTriangleWitnessesRetainBothTermsAndOrientationSigns() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); SimplicialChainMap f=SimplicialChainMap.identity(triangle);
        IntegerMatrix d1=matrix(new long[]{-1,-1,0},new long[]{1,0,-1},new long[]{0,1,1}),d2=matrix(new long[]{1},new long[]{-1},new long[]{1});
        for(int code=0;code<729;code++) {
            int rest=code; long[] v=new long[6]; for(int j=0;j<6;j++) { v[j]=rest%3-1; rest/=3; }
            IntegerMatrix h0=matrix(new long[]{v[0],0,0},new long[]{0,v[1],0},new long[]{0,0,v[2]}),h1=matrix(new long[]{v[3],v[4],v[5]});
            SimplicialChainMap g=new SimplicialChainMap(triangle,triangle,Arrays.asList(IntegerMatrix.identity(3).add(d1.multiply(h0)),IntegerMatrix.identity(3).add(d2.multiply(h1)).add(h0.multiply(d1)),IntegerMatrix.identity(1).add(h1.multiply(d2))));
            SimplicialChainHomotopy h=new SimplicialChainHomotopy(f,g,Arrays.asList(h0,h1,IntegerMatrix.zero(0,1))); identities(h);
            assertEquals(h,h.reverse().reverse()); assertEquals(SimplicialChainHomotopy.stationary(f),h.then(h.reverse()));
            RelativeSimplicialChain c=new RelativeSimplicialChain(triangle,z(1),new IntegerVector(z(2),z(-1),z(3)));
            assertEquals(g.onChain(c).subtract(f.onChain(c)),h.onChain(c).boundary().add(h.onChain(c.boundary())));
        }
    }
    @Test public void relativeWitnessesFilterBothGroupsAndRespectVanishingDegreeZeroGroups() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); List<FiniteSimplicialComplex> subcomplexes=Arrays.asList(complex(),complex(new int[]{0}),complex(new int[]{0,1}),triangle.skeleton(1),triangle);
        for(FiniteSimplicialComplex a : subcomplexes) {
            RelativeSimplicialComplex pair=new RelativeSimplicialComplex(triangle,a); SimplicialChainMap f=SimplicialChainMap.identity(pair);
            List<IntegerMatrix> h=new ArrayList<>(),g=new ArrayList<>();
            for(int k=0;k<3;k++) {
                int rows=pair.simplexCount(z(k+1)),columns=pair.simplexCount(z(k)); BigInteger[][] entries=new BigInteger[rows][columns];
                for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) entries[r][c]=z(r-2*c+1); h.add(new IntegerMatrix(rows,columns,entries));
            }
            for(int k=0;k<3;k++) { IntegerMatrix previous=k==0?IntegerMatrix.zero(pair.simplexCount(z(0)),0):h.get(k-1); g.add(f.chainMatrix(z(k)).add(pair.boundaryMatrix(z(k+1)).multiply(h.get(k))).add(previous.multiply(pair.boundaryMatrix(z(k))))); }
            SimplicialChainHomotopy witness=new SimplicialChainHomotopy(f,new SimplicialChainMap(pair,pair,g),h); identities(witness); for(int k=0;k<3;k++) equalInduced(witness,k);
        }
    }
    @Test public void equalEndpointsRetainNonzeroLoopsAndConcatenationDiffersFromAddition() {
        SimplicialChainHomotopy h=loop(),stationary=SimplicialChainHomotopy.stationary(h.from());
        assertEquals(h.from(),h.to()); assertNotEquals(h,stationary); assertNotEquals(h,h.reverse()); identities(h);
        assertEquals(stationary,h.then(h.reverse())); assertEquals(h,h.then(stationary)); assertEquals(h,stationary.then(h));
        assertEquals(h.scale(z(2)).chainMatrices(),h.then(h).chainMatrices()); assertNotEquals(h.add(h),h.then(h));
        assertEquals(new IntegerVector(z(1),z(-1),z(1)),h.onChain(new RelativeSimplicialChain(point(),z(0),new IntegerVector(z(1)))).coordinates());
        assertEquals(h,h.reverse().reverse()); assertEquals(h.hashCode(),new SimplicialChainHomotopy(h.data()).hashCode());
    }
    @Test public void preAndPostcompositionUseDifferentDegreesAndPreserveWitnessOrder() {
        SimplicialChainHomotopy h=loop(); SimplicialChainMap before=SimplicialChainMap.identity(point()).scale(z(2));
        SimplicialChainMap after=new SimplicialChainMap(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3),matrix(new long[]{3,0,0},new long[]{-2,1,0},new long[]{2,0,1})));
        assertEquals(h.chainMatrix(z(0)).scale(z(3)),h.postcompose(after).chainMatrix(z(0))); assertEquals(h.chainMatrix(z(0)).scale(z(2)),h.precompose(before).chainMatrix(z(0)));
        assertEquals(h.postcompose(after).precompose(before),h.precompose(before).postcompose(after)); identities(h.postcompose(after).precompose(before));
        assertEquals(h.then(h).postcompose(after),h.postcompose(after).then(h.postcompose(after)));
        assertEquals(h.reverse().precompose(before),h.precompose(before).reverse());
        // A two-dimensional target adds a new retained zero degree, rather than truncating by the old endpoints.
        SimplicialChainMap intoDisk=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(circle(),abs(complex(new int[]{0,1,2}))));
        assertEquals(3,h.postcompose(intoDisk).chainMatrices().size()); identities(h.postcompose(intoDisk));
        Map<BigInteger,BigInteger> constant=new TreeMap<>(); for(int v=0;v<3;v++) constant.put(z(v),z(0));
        SimplicialChainMap diskToPoint=SimplicialChainMap.fromAbsoluteSimplicial(new FiniteSimplicialMap(complex(new int[]{0,1,2}),point().ambient(),constant));
        assertEquals(3,h.precompose(diskToPoint).chainMatrices().size()); identities(h.precompose(diskToPoint));
        SimplicialChainMap circleToPoint=SimplicialChainMap.fromAbsoluteSimplicial(new FiniteSimplicialMap(circle().ambient(),point().ambient(),constant));
        assertEquals(SimplicialChainHomotopy.stationary(SimplicialChainMap.identity(point())),h.postcompose(circleToPoint));
    }
    @Test public void all729ContiguousTriangleMapPairsConvertTheActualPrism() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); List<RelativeSimplicialMap> maps=new ArrayList<>();
        for(int code=0;code<27;code++) { int rest=code; Map<BigInteger,BigInteger> vertices=new TreeMap<>(); for(int v=0;v<3;v++) { vertices.put(z(v),z(rest%3)); rest/=3; } maps.add(RelativeSimplicialMap.absolute(new FiniteSimplicialMap(triangle,triangle,vertices))); }
        for(RelativeSimplicialMap f : maps) for(RelativeSimplicialMap g : maps) {
            SimplicialHomotopy prism=new SimplicialHomotopy(f,g); SimplicialChainHomotopy h=SimplicialChainHomotopy.fromPrism(prism);
            assertEquals(prism.chainMatrices(),h.chainMatrices()); identities(h);
            for(int k=0;k<3;k++) assertEquals(h.chainMatrix(z(k)).scale(z(-1)),h.reverse().chainMatrix(z(k)));
        }
        FiniteSimplicialComplex tetra=complex(new int[]{0,1,2,3}); Map<BigInteger,BigInteger> left=new TreeMap<>(),right=new TreeMap<>();
        left.put(z(0),z(0)); left.put(z(1),z(1)); right.put(z(0),z(2)); right.put(z(1),z(3));
        SimplicialHomotopy asymmetric=SimplicialHomotopy.absolute(new FiniteSimplicialMap(edge().ambient(),tetra,left),new FiniteSimplicialMap(edge().ambient(),tetra,right));
        assertNotEquals(SimplicialChainHomotopy.fromPrism(asymmetric).reverse(),SimplicialChainHomotopy.fromPrism(asymmetric.reverse()));
    }
    private static RelativeSimplicialMap pointAt(int vertex) {
        return RelativeSimplicialMap.absolute(new FiniteSimplicialMap(point().ambient(),circle().ambient(),Collections.singletonMap(z(0),z(vertex))));
    }
    @Test public void pathConversionRetainsTheAccumulatedCycleOfAClosedContiguityWalk() {
        SimplicialHomotopyPath path=new SimplicialHomotopyPath(Arrays.asList(pointAt(0),pointAt(1),pointAt(2),pointAt(0)));
        SimplicialChainHomotopy h=SimplicialChainHomotopy.fromPath(path); assertEquals(loop(),h); assertEquals(path.chainMatrices(),h.chainMatrices());
        SimplicialChainHomotopy first=SimplicialChainHomotopy.fromPrism(new SimplicialHomotopy(pointAt(0),pointAt(1))),second=SimplicialChainHomotopy.fromPrism(new SimplicialHomotopy(pointAt(1),pointAt(2))),third=SimplicialChainHomotopy.fromPrism(new SimplicialHomotopy(pointAt(2),pointAt(0)));
        assertEquals(h,first.then(second).then(third)); assertEquals(first.then(second.then(third)),first.then(second).then(third));
    }
    @Test public void collapseSequenceAndSubdivisionConversionsHaveTheCorrectEndpointDirections() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); SimplicialCollapse collapse=new SimplicialCollapse(triangle,FiniteSet.of(z(0),z(1)));
        SimplicialChainHomotopy elementary=SimplicialChainHomotopy.fromCollapse(collapse); assertTrue(elementary.to().isIdentity()); assertEquals(collapse.chainHomotopyMatrices(),elementary.chainMatrices());
        assertEquals(elementary,SimplicialChainHomotopy.fromCollapseSequence(SimplicialCollapseSequence.fromCollapse(collapse)));
        SimplicialCollapseSequence sequence=SimplicialCollapseSequence.reduce(triangle); SimplicialChainHomotopy reduced=SimplicialChainHomotopy.fromCollapseSequence(sequence);
        assertEquals(sequence.chainHomotopyMatrices(),reduced.chainMatrices());
        SimplicialSubdivision sd=new SimplicialSubdivision(triangle); SimplicialChainHomotopy subdivision=SimplicialChainHomotopy.fromSubdivision(sd);
        assertEquals(sd.subdivided(),subdivision.source()); assertTrue(subdivision.to().isIdentity()); assertEquals(sd.chainHomotopyMatrices(),subdivision.chainMatrices());
        for(SimplicialChainHomotopy h : Arrays.asList(elementary,reduced,subdivision)) { identities(h); for(int k=0;k<3;k++) equalInduced(h,k); }
    }
    @Test public void all729TypedChainAndCochainFillingsSatisfyTheFullHomotopyIdentities() {
        SimplicialChainHomotopy h=SimplicialChainHomotopy.fromCollapse(new SimplicialCollapse(abs(complex(new int[]{0,1,2})),FiniteSet.of(z(0),z(1))));
        for(int code=0;code<729;code++) {
            int rest=code; BigInteger[] c=new BigInteger[3],a=new BigInteger[3]; for(int j=0;j<3;j++) { c[j]=z(rest%3-1); rest/=3; a[j]=z(rest%3-1); rest/=3; }
            RelativeSimplicialChain chain=new RelativeSimplicialChain(h.source(),z(1),new IntegerVector(c)); RelativeSimplicialCochain cochain=new RelativeSimplicialCochain(h.target(),z(1),new IntegerVector(a));
            assertEquals(h.to().onChain(chain).subtract(h.from().onChain(chain)),h.onChain(chain).boundary().add(h.onChain(chain.boundary())));
            assertEquals(h.to().onCochain(cochain).subtract(h.from().onCochain(cochain)),h.onCochain(cochain).coboundary().add(h.onCochain(cochain.coboundary())));
        }
    }
    @Test public void projectivePlaneTorsionSurvivesBothEndpointMapsAndHasTypedFillings() {
        FiniteSimplicialComplex rp2=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        SimplicialChainHomotopy h=SimplicialChainHomotopy.fromCollapse(new SimplicialCollapse(abs(rp2.union(complex(new int[]{0,6}))),FiniteSet.of(z(6))));
        assertEquals(Collections.singletonList(z(2)),h.homologyMaps(z(1)).get(0).source().type().invariantFactors());
        assertEquals(Collections.singletonList(z(2)),h.cohomologyMaps(z(2)).get(0).source().type().invariantFactors()); equalInduced(h,1); equalInduced(h,2);
        for(RelativeSimplicialChain cycle : RelativeSimplicialChain.zero(h.source(),z(1)).cycleGenerators()) assertEquals(h.to().onChain(cycle).subtract(h.from().onChain(cycle)),h.onChain(cycle).boundary());
    }
    @Test public void dataListsAreImmutableAndFullMapAndDegreeChecksRejectInvalidWitnesses() {
        SimplicialChainHomotopy h=loop(); List<IntegerMatrix> list=new ArrayList<>(h.chainMatrices()); SimplicialChainHomotopy.Data data=new SimplicialChainHomotopy.Data(h.from(),h.to(),list); list.clear();
        assertEquals(h,new SimplicialChainHomotopy(data)); assertEquals(data,h.data()); assertEquals(data.hashCode(),h.data().hashCode()); assertThrows(UnsupportedOperationException.class,data.matrices()::clear); assertThrows(UnsupportedOperationException.class,h.cochainMatrices()::clear);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.then(h.scale(z(2))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainHomotopy(h.from(),h.to(),Collections.singletonList(h.chainMatrix(z(0)))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainHomotopy(h.from(),h.to(),Arrays.asList(IntegerMatrix.zero(1,3),IntegerMatrix.zero(0,0))));
        // Correct matrix shapes and zero-degree equation cannot excuse a failed top-degree equation.
        RelativeSimplicialComplex disk=new RelativeSimplicialComplex(complex(new int[]{0,1,2}),circle().ambient()); SimplicialChainMap id=SimplicialChainMap.identity(disk);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainHomotopy(id,id.scale(z(2)),Arrays.asList(IntegerMatrix.zero(0,0),IntegerMatrix.zero(1,0),IntegerMatrix.zero(0,1))));
        SimplicialChainMap other=SimplicialChainMap.identity(abs(complex(new int[]{9})));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialChainHomotopy(h.from(),other,h.chainMatrices()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.precompose(other)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.postcompose(other)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.add(SimplicialChainHomotopy.stationary(other)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onChain(RelativeSimplicialChain.zero(circle(),z(0)))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onCochain(RelativeSimplicialCochain.zero(point(),z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onCochain(RelativeSimplicialCochain.zero(circle(),z(0)))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.chainMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.cohomologyMaps(z(-1)));
    }
    @Test public void negativeChainDegreesAndDegreeZeroDualMatricesRetainTheirShapes() {
        SimplicialChainHomotopy h=loop(); assertEquals(IntegerMatrix.zero(0,3),h.cochainMatrix(z(0)));
        assertEquals(RelativeSimplicialChain.zero(circle(),z(0)),h.onChain(RelativeSimplicialChain.zero(point(),z(-1))));
        assertEquals(SimplicialChain.zero(circle().ambient(),z(0)),h.onAbsoluteChain(SimplicialChain.zero(point().ambient(),z(-1))));
        BigInteger huge=BigInteger.ONE.shiftLeft(100); assertEquals(IntegerMatrix.zero(0,0),h.chainMatrix(huge));
        assertEquals(SimplicialCochain.zero(point().ambient(),huge.subtract(z(1))),h.onAbsoluteCochain(SimplicialCochain.zero(circle().ambient(),huge)));
        SimplicialChainHomotopy empty=SimplicialChainHomotopy.stationary(SimplicialChainMap.identity(abs(complex()))); assertTrue(empty.chainMatrices().isEmpty()); assertEquals(Collections.singletonList(IntegerMatrix.zero(0,0)),empty.cochainMatrices()); identities(empty);
    }
    @Test public void absoluteActionsRequireBothSubcomplexesEmptyAndFilteringPrecedesBasisBounds() {
        RelativeSimplicialComplex based=new RelativeSimplicialComplex(edge().ambient(),point().ambient()); SimplicialChainHomotopy h=SimplicialChainHomotopy.stationary(SimplicialChainMap.identity(based));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onAbsoluteChain(SimplicialChain.zero(edge().ambient(),z(0)))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onAbsoluteCochain(SimplicialCochain.zero(edge().ambient(),z(1))));
        List<FiniteSet<Integer>> vertices=new ArrayList<>(); for(int i=0;i<4096;i++) vertices.add(FiniteSet.of(i)); FiniteSimplicialComplex big=new FiniteSimplicialComplex(vertices);
        SimplicialChainHomotopy diagonal=SimplicialChainHomotopy.stationary(SimplicialChainMap.identity(RelativeSimplicialComplex.diagonal(big))); assertEquals(Collections.singletonList(IntegerMatrix.zero(0,0)),diagonal.chainMatrices());
    }
    @Test public void nativeSecondOperandActionsAndScalarFlatFlowsSerializeAndRecollect() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainHomotopy h=loop(); RelativeSimplicialChain vertex=new RelativeSimplicialChain(point(),z(0),new IntegerVector(z(1)));
        IAlgebraItem<SimplicialChainHomotopy> item=math.chainHomotopies.algebra().buildAlgebraItem(h);
        assertSame(math.relativeChains.algebra(),item.performLeftProjectionOperation("on-chain",vertex).getAlgebra());
        assertSame(math.relativeCochains.algebra(),item.performLeftProjectionOperation("on-cochain",RelativeSimplicialCochain.zero(circle(),z(1))).getAlgebra());
        assertSame(math.simplicialChains.algebra(),item.performLeftProjectionOperation("on-absolute-chain",SimplicialChain.zero(point().ambient(),z(0))).getAlgebra());
        assertSame(math.cochains.algebra(),item.performLeftProjectionOperation("on-absolute-cochain",SimplicialCochain.zero(circle().ambient(),z(1))).getAlgebra());
        assertEquals(h,math.chainHomotopies.inputs.buildAlgebraItem(h.data()).<SimplicialChainHomotopy>performAlgebraTransfer("ChainHomotopy.from-data").perform().getResult());
        IAlgebraFlow<IntegerVector> actions=math.flow(math.chainHomotopies,Collections.singletonList(h)).performLeftProjectionOperation("on-chain",vertex).<IntegerVector>performAlgebraTransfer("coordinates");
        IAlgebraFlow<IntegerMatrix> dual=math.flow(math.chainHomotopies,Collections.singletonList(h)).performCustomMemberOperation("scale",z(2)).<IntegerMatrix>performFlatAlgebraTransfer("cochain-matrices");
        IAlgebraFlow<Boolean> maps=math.flow(math.chainHomotopies,Collections.singletonList(h)).<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism");
        for(IAlgebraFlow<?> original : Arrays.asList(actions,dual,maps)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("[1, -1, 1]"),actions.collect()); assertEquals(Arrays.asList("true","true"),maps.collect());
    }
    private static RelativeSimplicialComplex separatedCells(int count) {
        List<FiniteSet<Integer>> ambient=new ArrayList<>(),subcomplex=new ArrayList<>();
        for(int k=0;k<count;k++) {
            int v=5*k; ambient.add(FiniteSet.of(v,v+1)); ambient.add(FiniteSet.of(v+2,v+3,v+4));
            subcomplex.add(FiniteSet.of(v)); subcomplex.add(FiniteSet.of(v+1)); subcomplex.add(FiniteSet.of(v+2,v+3)); subcomplex.add(FiniteSet.of(v+2,v+4)); subcomplex.add(FiniteSet.of(v+3,v+4));
        }
        return new RelativeSimplicialComplex(new FiniteSimplicialComplex(ambient),new FiniteSimplicialComplex(subcomplex));
    }
    @Test public void endpointConstructionWitnessValidationAndBothInducedMapsShareBudgets() {
        SimplicialChainMap large=SimplicialChainMap.identity(separatedCells(100)),medium=SimplicialChainMap.identity(separatedCells(80)),small=SimplicialChainMap.identity(separatedCells(70));
        SimplicialChainHomotopy a=SimplicialChainHomotopy.stationary(large),b=SimplicialChainHomotopy.stationary(medium),c=SimplicialChainHomotopy.stationary(small);
        assertNotNull(large.scale(z(2))); assertNotNull(medium.compose(medium)); assertNotNull(small.homologyMap(z(1))); assertNotNull(small.cohomologyMap(z(1)));
        for(Runnable operation : Arrays.<Runnable>asList(() -> a.scale(z(2)),() -> a.add(a),() -> b.precompose(medium),() -> b.postcompose(medium),() -> c.homologyMaps(z(1)),() -> c.cohomologyMaps(z(1)))) {
            MathFailure limit=assertThrows(MathFailure.class,operation::run); assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,limit.kind()); assertTrue(limit.getMessage().contains("5000000"));
        }
    }
}
