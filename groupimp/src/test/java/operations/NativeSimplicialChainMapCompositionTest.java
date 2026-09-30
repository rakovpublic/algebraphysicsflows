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

public class NativeSimplicialChainMapCompositionTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return complex(f); }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static RelativeSimplicialComplex point() { return abs(points(1)); }
    private static RelativeSimplicialComplex edge() { return abs(complex(new int[]{0,1})); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    private static IntegerMatrix matrix(long[]... entries) {
        BigInteger[][] values=new BigInteger[entries.length][]; for(int r=0;r<entries.length;r++) { values[r]=new BigInteger[entries[r].length]; for(int c=0;c<entries[r].length;c++) values[r][c]=z(entries[r][c]); } return new IntegerMatrix(values);
    }
    private static SimplicialChainMap pointMap(IntegerMatrix m) { return new SimplicialChainMap(abs(points(m.columns())),abs(points(m.rows())),Collections.singletonList(m)); }
    private static MathFailure failure(MathFailure.Kind kind,Runnable body) { MathFailure e=assertThrows(MathFailure.class,body::run); assertEquals(kind,e.kind()); return e; }
    private static void squares(SimplicialChainMapSpace from,SimplicialChainMapSpace to,List<IntegerMatrix> m) {
        assertEquals(3,m.size()); IntegralHomology a=from.homology(),b=to.homology();
        assertEquals(b.outgoingBoundary().multiply(m.get(1)),m.get(0).multiply(a.outgoingBoundary()));
        assertEquals(b.incomingBoundary().multiply(m.get(2)),m.get(1).multiply(a.incomingBoundary()));
    }
    @Test public void all6561TernaryPointMapPairsAgreeWithIndependentLeftAndRightCoordinateSums() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(points(2)),abs(points(2))); List<SimplicialChainMap> maps=new ArrayList<>(); List<AbelianGroupElement> classes=new ArrayList<>();
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) { SimplicialChainMap f=pointMap(matrix(new long[]{a,b},new long[]{c,d})); maps.add(f); classes.add(space.classOf(f)); }
        for(SimplicialChainMap fixed : maps) {
            AbelianGroupHomomorphism pre=space.precomposeMap(fixed),post=space.postcomposeMap(fixed); IntegerMatrix b=fixed.chainMatrix(z(0));
            for(int i=0;i<maps.size();i++) {
                IntegerMatrix f=maps.get(i).chainMatrix(z(0)); BigInteger[] right=new BigInteger[4],left=new BigInteger[4];
                for(int r=0;r<2;r++) for(int c=0;c<2;c++) { right[2*r+c]=BigInteger.ZERO; left[2*r+c]=BigInteger.ZERO; for(int a=0;a<2;a++) { right[2*r+c]=right[2*r+c].add(f.get(r,a).multiply(b.get(a,c))); left[2*r+c]=left[2*r+c].add(b.get(r,a).multiply(f.get(a,c))); } }
                assertEquals(new IntegerVector(right),pre.apply(classes.get(i)).smithCoordinates()); assertEquals(new IntegerVector(left),post.apply(classes.get(i)).smithCoordinates());
            }
        }
    }
    @Test public void rectangularPointActionsHaveExplicitRowMajorKroneckerMatrices() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(points(2)),abs(points(2)));
        SimplicialChainMap before=pointMap(matrix(new long[]{2},new long[]{3})),after=pointMap(matrix(new long[]{1,2},new long[]{3,4},new long[]{5,6}));
        assertEquals(Arrays.asList(IntegerMatrix.zero(0,0),matrix(new long[]{2,3,0,0},new long[]{0,0,2,3}),IntegerMatrix.zero(0,0)),space.precomposeMatrices(before));
        assertEquals(Arrays.asList(IntegerMatrix.zero(0,0),matrix(new long[]{1,0,2,0},new long[]{0,1,0,2},new long[]{3,0,4,0},new long[]{0,3,0,4},new long[]{5,0,6,0},new long[]{0,5,0,6}),IntegerMatrix.zero(0,0)),space.postcomposeMatrices(after));
        assertEquals(space.precomposeMatrices(before).get(1),space.precomposeMap(before).smithMatrix()); assertEquals(space.postcomposeMatrices(after).get(1),space.postcomposeMap(after).smithMatrix());
        assertEquals(new SimplicialChainMapSpace(point(),abs(points(2))),space.precomposeSpace(before)); assertEquals(new SimplicialChainMapSpace(abs(points(2)),abs(points(3))),space.postcomposeSpace(after));
    }
    @Test public void noncommutingCompositionRespectsVarianceAndTheTwoActionsCommute() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(points(2)),abs(points(2)));
        SimplicialChainMap a=pointMap(matrix(new long[]{1,2},new long[]{0,1})),b=pointMap(matrix(new long[]{1,0},new long[]{3,1})); assertNotEquals(a.compose(b),b.compose(a));
        assertEquals(space.precomposeMap(a.compose(b)),space.precomposeSpace(a).precomposeMap(b).compose(space.precomposeMap(a)));
        assertEquals(space.postcomposeMap(b.compose(a)),space.postcomposeSpace(a).postcomposeMap(b).compose(space.postcomposeMap(a)));
        assertNotEquals(space.precomposeMap(a.compose(b)),space.precomposeMap(b.compose(a))); assertNotEquals(space.postcomposeMap(a.compose(b)),space.postcomposeMap(b.compose(a)));
        assertEquals(space.precomposeSpace(b).postcomposeMap(a).compose(space.precomposeMap(b)),space.postcomposeSpace(a).precomposeMap(b).compose(space.postcomposeMap(a)));
        assertEquals(space.precomposeMap(a).add(space.precomposeMap(b)),space.precomposeMap(a.add(b))); assertEquals(space.postcomposeMap(a).add(space.postcomposeMap(b)),space.postcomposeMap(a.add(b)));
    }
    private static SimplicialChainMap circleDegree(int degree) { return new SimplicialChainMap(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3),matrix(new long[]{degree,0,0},new long[]{1-degree,1,0},new long[]{degree-1,0,1}))); }
    @Test public void circleActionsPreserveTheIndependentH0AndH1ClassesAndSignedDifferentialSquares() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(circle(),circle());
        for(int n=-3;n<=3;n++) {
            SimplicialChainMap fixed=circleDegree(n); squares(space,space,space.precomposeMatrices(fixed)); squares(space,space,space.postcomposeMatrices(fixed));
            AbelianGroupHomomorphism pre=space.precomposeMap(fixed),post=space.postcomposeMap(fixed);
            for(int m=-3;m<=3;m++) { AbelianGroupElement value=space.classOf(circleDegree(m)); assertEquals(space.classOf(circleDegree(n*m)),pre.apply(value)); assertEquals(space.classOf(circleDegree(n*m)),post.apply(value)); }
            for(SimplicialChainMap generator : space.mapGenerators()) { assertEquals(space.classOf(generator.compose(fixed)),pre.apply(space.classOf(generator))); assertEquals(space.classOf(fixed.compose(generator)),post.apply(space.classOf(generator))); }
        }
        List<IntegerMatrix> post=space.postcomposeMatrices(circleDegree(3));
        // H0 sends vertex zero to the circle cycle, in row-major order. Postcomposition acts by degree three on this Hom_1 cycle.
        IntegerVector loop=new IntegerVector(z(1),z(0),z(0),z(-1),z(0),z(0),z(1),z(0),z(0)); assertEquals(loop.scale(z(3)),post.get(2).multiply(loop));
        assertEquals(IntegerMatrix.identity(9),post.get(0));
    }
    private static SimplicialChainMap constantInterval(int vertex) { return new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{vertex==0?1:0,vertex==0?1:0},new long[]{vertex==1?1:0,vertex==1?1:0}),IntegerMatrix.zero(1,1))); }
    @Test public void homotopicFixedMapsInduceEqualClassMapsDespiteDifferentHomMatrices() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(edge(),edge()); SimplicialChainMap a=constantInterval(0),b=constantInterval(1),id=SimplicialChainMap.identity(edge());
        assertTrue(SimplicialChainHomotopySolver.areHomotopic(a,b)); assertNotEquals(space.precomposeMatrices(a),space.precomposeMatrices(b)); assertNotEquals(space.postcomposeMatrices(a),space.postcomposeMatrices(b));
        assertEquals(space.precomposeMap(a),space.precomposeMap(b)); assertEquals(space.postcomposeMap(a),space.postcomposeMap(b));
        assertEquals(space.precomposeMap(id),space.precomposeMap(a)); assertEquals(space.postcomposeMap(id),space.postcomposeMap(a));
    }
    private static RelativeSimplicialComplex projectivePlane() { return abs(complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5})); }
    private static RelativeSimplicialComplex relativeDisk() { FiniteSimplicialComplex disk=complex(new int[]{0,1,2}); return new RelativeSimplicialComplex(disk,disk.skeleton(1)); }
    private static SimplicialChainMap torsionMap(int coefficient) {
        BigInteger[][] top=new BigInteger[1][10]; Arrays.fill(top[0],BigInteger.ZERO); top[0][0]=z(coefficient);
        return new SimplicialChainMap(projectivePlane(),relativeDisk(),Arrays.asList(IntegerMatrix.zero(0,6),IntegerMatrix.zero(0,15),new IntegerMatrix(top)));
    }
    @Test public void precompositionDetectsATorsionActionEvenWhenTheFixedMapHasZeroHomologyMaps() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(relativeDisk(),relativeDisk()); SimplicialChainMap odd=torsionMap(1); AbelianGroupElement identity=space.classOf(SimplicialChainMap.identity(relativeDisk()));
        SimplicialChainMapSpace destination=space.precomposeSpace(odd); assertEquals(AbelianGroupType.cyclic(z(2)),destination.homotopyType());
        for(int k=0;k<3;k++) assertEquals(odd.homologyMap(z(k)),torsionMap(0).homologyMap(z(k)));
        for(int n=-4;n<=4;n++) {
            AbelianGroupHomomorphism action=space.precomposeMap(torsionMap(n)); assertEquals(n%2==0,action.isZero()); assertEquals(destination.classOf(torsionMap(n)),action.apply(identity));
        }
        assertEquals(space.precomposeMap(torsionMap(1)),space.precomposeMap(torsionMap(3))); assertNotEquals(space.precomposeMap(torsionMap(0)),space.precomposeMap(odd));
        squares(space,destination,space.precomposeMatrices(odd));
    }
    @Test public void bothActionsRetainTorsionAndUnboundedIntegerScaling() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(projectivePlane(),relativeDisk()); PresentedAbelianGroup group=space.homotopyGroup();
        List<BigInteger> coefficients=new ArrayList<>(); for(int n=-4;n<=4;n++) coefficients.add(z(n)); coefficients.add(BigInteger.ONE.shiftLeft(1024)); coefficients.add(BigInteger.ONE.shiftLeft(1024).add(BigInteger.ONE));
        for(BigInteger n : coefficients) { AbelianGroupHomomorphism expected=AbelianGroupHomomorphism.scaling(group,n); assertEquals(expected,space.precomposeMap(SimplicialChainMap.identity(projectivePlane()).scale(n))); assertEquals(expected,space.postcomposeMap(SimplicialChainMap.identity(relativeDisk()).scale(n))); }
    }
    @Test public void differentAmbientDimensionsRetainZeroBlocksAndProjectionNaturality() {
        RelativeSimplicialComplex triangle=abs(complex(new int[]{0,1,2})); SimplicialChainMap inclusion=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(edge(),triangle));
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); vertices.put(z(0),z(0)); vertices.put(z(1),z(1)); vertices.put(z(2),z(0));
        SimplicialChainMap retraction=SimplicialChainMap.fromAbsoluteSimplicial(new FiniteSimplicialMap(triangle.ambient(),edge().ambient(),vertices));
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(edge(),edge()),post=space.postcomposeSpace(inclusion),pre=space.precomposeSpace(retraction);
        squares(space,post,space.postcomposeMatrices(inclusion)); squares(space,pre,space.precomposeMatrices(retraction));
        assertEquals(7,space.postcomposeMatrices(inclusion).get(2).rows()); assertEquals(7,space.precomposeMatrices(retraction).get(0).rows());
        for(SimplicialChainMap f : space.mapGenerators()) { assertEquals(post.classOf(inclusion.compose(f)),space.postcomposeMap(inclusion).apply(space.classOf(f))); assertEquals(pre.classOf(f.compose(retraction)),space.precomposeMap(retraction).apply(space.classOf(f))); }
        // Both directions also discard degrees absent from the destination without losing the group context.
        SimplicialChainMapSpace reverse=new SimplicialChainMapSpace(triangle,triangle); squares(reverse,reverse.precomposeSpace(inclusion),reverse.precomposeMatrices(inclusion)); squares(reverse,reverse.postcomposeSpace(retraction),reverse.postcomposeMatrices(retraction));
    }
    @Test public void identitiesAndEmptyHomGroupsRetainAllThreeMatrixShapes() {
        RelativeSimplicialComplex empty=abs(complex());
        for(RelativeSimplicialComplex source : Arrays.asList(empty,point(),edge())) for(RelativeSimplicialComplex target : Arrays.asList(empty,point(),edge())) {
            SimplicialChainMapSpace space=new SimplicialChainMapSpace(source,target); IntegralHomology h=space.homology(); List<IntegerMatrix> expected=Arrays.asList(IntegerMatrix.identity(h.outgoingBoundary().rows()),IntegerMatrix.identity(h.chainRank()),IntegerMatrix.identity(h.incomingBoundary().columns()));
            assertEquals(space,space.precomposeSpace(SimplicialChainMap.identity(source))); assertEquals(space,space.postcomposeSpace(SimplicialChainMap.identity(target)));
            assertEquals(expected,space.precomposeMatrices(SimplicialChainMap.identity(source))); assertEquals(expected,space.postcomposeMatrices(SimplicialChainMap.identity(target)));
            assertEquals(AbelianGroupHomomorphism.identity(h.group()),space.precomposeMap(SimplicialChainMap.identity(source))); assertEquals(AbelianGroupHomomorphism.identity(h.group()),space.postcomposeMap(SimplicialChainMap.identity(target)));
        }
        SimplicialChainMapSpace singleton=new SimplicialChainMapSpace(point(),point()); SimplicialChainMap toEmpty=SimplicialChainMap.zero(point(),empty),fromEmpty=SimplicialChainMap.zero(empty,point());
        assertEquals(IntegerMatrix.zero(0,1),singleton.postcomposeMatrices(toEmpty).get(1)); assertEquals(IntegerMatrix.zero(0,1),singleton.precomposeMatrices(fromEmpty).get(1));
    }
    private static List<Runnable> operations(SimplicialChainMapSpace space,SimplicialChainMap map) { return Arrays.asList(() -> space.precomposeSpace(map),() -> space.postcomposeSpace(map),() -> space.precomposeMap(map),() -> space.postcomposeMap(map),() -> space.precomposeMatrices(map),() -> space.postcomposeMatrices(map)); }
    @Test public void compositionChecksExactLabelledMiddlePairsBeforeAnyReductionOrShapeLimit() {
        RelativeSimplicialComplex based0=new RelativeSimplicialComplex(edge().ambient(),points(1)),based1=new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{1}));
        for(Runnable operation : operations(new SimplicialChainMapSpace(based0,based0),SimplicialChainMap.identity(based1))) failure(MathFailure.Kind.OPERATION_UNDEFINED,operation);
        for(Runnable operation : operations(new SimplicialChainMapSpace(abs(points(17)),abs(points(17))),SimplicialChainMap.identity(abs(points(16))))) failure(MathFailure.Kind.OPERATION_UNDEFINED,operation);
    }
    @Test public void bothSpacesRespectAggregateBoundsButContextChangesDoNotRunReductions() {
        SimplicialChainMapSpace small=new SimplicialChainMapSpace(point(),abs(points(16))); SimplicialChainMap before=pointMap(IntegerMatrix.zero(1,17));
        assertEquals(abs(points(17)),small.precomposeSpace(before).source()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> small.precomposeMatrices(before)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> small.precomposeMap(before));
        SimplicialChainMapSpace transposed=new SimplicialChainMapSpace(abs(points(16)),point()); SimplicialChainMap after=pointMap(IntegerMatrix.zero(17,1));
        assertEquals(abs(points(17)),transposed.postcomposeSpace(after).target()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> transposed.postcomposeMatrices(after)); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> transposed.postcomposeMap(after));
        SimplicialChainMapSpace large=new SimplicialChainMapSpace(abs(points(16)),abs(points(16))); SimplicialChainMap id=SimplicialChainMap.identity(abs(points(16)));
        assertEquals(IntegerMatrix.identity(256),large.precomposeMatrices(id).get(1)); assertEquals(IntegerMatrix.identity(256),large.postcomposeMatrices(id).get(1));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.precomposeMap(id)).getMessage().contains("5000000"));
        assertThrows(UnsupportedOperationException.class,large.postcomposeMatrices(id)::clear);
    }
    @Test public void bothHomologyReductionsAndTheInducedMapShareOneBudget() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<8;i++) for(int j=i+1;j<8;j++) if(facets.size()<22) facets.add(FiniteSet.of(i,j));
        RelativeSimplicialComplex target=abs(new FiniteSimplicialComplex(facets)); SimplicialChainMapSpace space=new SimplicialChainMapSpace(circle(),target); IntegralHomology h=space.homology();
        assertEquals(AbelianGroupType.free(z(16)),h.type()); assertEquals(AbelianGroupHomomorphism.identity(h.group()),h.inducedMap(h,IntegerMatrix.identity(h.chainRank())));
        assertEquals(IntegerMatrix.identity(h.chainRank()),space.precomposeMatrices(SimplicialChainMap.identity(circle())).get(1));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> space.precomposeMap(SimplicialChainMap.identity(circle()))).getMessage().contains("5000000"));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> space.postcomposeMap(SimplicialChainMap.identity(target))).getMessage().contains("5000000"));
    }
    @Test public void nativeContextClassActionAndFlatMatrixFlowsUseActualWrappersAndSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainMapSpace space=new SimplicialChainMapSpace(relativeDisk(),relativeDisk()); SimplicialChainMap fixed=torsionMap(1); IAlgebraItem<SimplicialChainMapSpace> item=math.chainMapSpaces.algebra().buildAlgebraItem(space);
        assertSame(math.chainMapSpaces.algebra(),item.performCustomMemberOperation("precompose-space",fixed).getAlgebra()); assertSame(math.abelianHomomorphisms.algebra(),item.performUnsafeOperation("precompose-map",fixed).getAlgebra());
        List<IAlgebraItem<IntegerMatrix>> values=item.performUnsafeFlatOperation("precompose-matrices",fixed); assertEquals(3,values.size()); for(IAlgebraItem<IntegerMatrix> value : values) assertSame(math.integerMatrices.algebra(),value.getAlgebra());
        AbelianGroupElement identity=space.classOf(SimplicialChainMap.identity(relativeDisk()));
        IAlgebraFlow<BigInteger> action=math.flow(math.chainMapSpaces,Collections.singletonList(space)).<AbelianGroupHomomorphism,SimplicialChainMap>performAlgebraUnsafe("precompose-map",fixed).performLeftProjectionOperation("apply",identity).performAlgebraTransfer("order");
        IAlgebraFlow<AbelianGroupType> context=math.flow(math.chainMapSpaces,Collections.singletonList(space)).performCustomMemberOperation("precompose-space",fixed).performAlgebraTransfer("homotopy-type");
        IAlgebraFlow<IntegerMatrix> matrices=math.flow(math.chainMapSpaces,Collections.singletonList(space)).performFlatAlgebraUnsafe("precompose-matrices",fixed);
        for(IAlgebraFlow<?> original : Arrays.asList(action,context,matrices)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("2"),action.collect()); assertEquals(Collections.singletonList("AbelianGroup(rank=0, torsion=[2])"),context.collect()); assertEquals(3,matrices.collect().size());
    }
}
