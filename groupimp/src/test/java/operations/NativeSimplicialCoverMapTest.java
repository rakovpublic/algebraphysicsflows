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

public class NativeSimplicialCoverMapTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(int[] facet : facets) { List<Integer> values=new ArrayList<>(); for(int vertex : facet) values.add(vertex); faces.add(new FiniteSet<>(values)); }
        return new FiniteSimplicialComplex(faces);
    }
    private static SimplicialCover circleCover() { return new SimplicialCover(complex(new int[]{0,1},new int[]{1,2}),complex(new int[]{0,2})); }
    private static SimplicialCoverMap map(SimplicialCover source,SimplicialCover target,int... images) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>(); int i=0;
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source.union()).members()) vertices.put(vertex,z(images[i++]));
        return new SimplicialCoverMap(source,target,new FiniteSimplicialMap(source.union(),target.union(),vertices));
    }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] entries=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { entries[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) entries[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(entries);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void chainIdentities(SimplicialCoverMap f) {
        for(int k=0;k<=Math.max(f.source().union().dimension(),f.target().union().dimension())+1;k++) {
            BigInteger degree=z(k); IntegerMatrix sum=f.sumChainMatrix(degree);
            assertEquals(sum.multiply(f.source().intersectionMatrix(degree)),f.target().intersectionMatrix(degree).multiply(f.intersectionMap().chainMatrix(degree)));
            assertEquals(f.unionMap().chainMatrix(degree).multiply(f.source().unionMatrix(degree)),f.target().unionMatrix(degree).multiply(sum));
            if(k>0) assertEquals(f.target().sumBoundaryMatrix(degree).multiply(sum),f.sumChainMatrix(z(k-1)).multiply(f.source().sumBoundaryMatrix(degree)));
        }
    }
    private static void naturality(SimplicialCoverMap f,int degree) {
        BigInteger k=z(degree); List<AbelianGroupHomomorphism> from=f.source().longExactSegment(k),to=f.target().longExactSegment(k),vertical=f.longExactMaps(k);
        assertEquals(4,vertical.size());
        for(int i=0;i<3;i++) assertEquals(vertical.get(i+1).compose(from.get(i)),to.get(i).compose(vertical.get(i)));
        assertEquals(f.intersectionHomologyMap(k),vertical.get(0)); assertEquals(f.sumHomologyMap(k),vertical.get(1)); assertEquals(f.unionHomologyMap(k),vertical.get(2));
        assertEquals(f.sumHomologyMap(k).compose(f.source().leftInclusionMap(k)),f.target().leftInclusionMap(k).compose(f.leftHomologyMap(k)));
        assertEquals(f.target().rightProjectionMap(k).compose(f.sumHomologyMap(k)),f.rightHomologyMap(k).compose(f.source().rightProjectionMap(k)));
        List<RelativeSimplicialMap> relative=f.excisionMaps(); assertEquals(Arrays.asList(f.leftRelativeMap(),f.unionRelativeMap()),relative);
        assertEquals(relative.get(1).compose(f.source().excisionMap()),f.target().excisionMap().compose(relative.get(0)));
        assertEquals(relative.get(1).homologyMap(k).compose(f.source().excisionMap().homologyMap(k)),f.target().excisionMap().homologyMap(k).compose(relative.get(0).homologyMap(k)));
    }
    @Test public void circleAndSphereReflectionsCommuteWithMayerVietorisAndExcision() {
        SimplicialCover circle=circleCover(); SimplicialCoverMap reflection=map(circle,circle,2,1,0);
        assertEquals(m(new long[]{-1}),reflection.unionHomologyMap(z(1)).smithMatrix()); assertEquals(m(new long[]{0,1},new long[]{1,0}),reflection.intersectionHomologyMap(z(0)).smithMatrix());
        assertEquals(AbelianGroupHomomorphism.identity(circle.sumHomology(z(0)).group()),reflection.sumHomologyMap(z(0)));
        chainIdentities(reflection); naturality(reflection,0); naturality(reflection,1);
        SimplicialCover sphere=new SimplicialCover(complex(new int[]{0,1,2}),complex(new int[]{0,1,3},new int[]{0,2,3},new int[]{1,2,3}));
        SimplicialCoverMap reverseSphere=map(sphere,sphere,1,0,2,3);
        assertEquals(m(new long[]{-1}),reverseSphere.unionHomologyMap(z(2)).smithMatrix()); assertEquals(m(new long[]{-1}),reverseSphere.intersectionHomologyMap(z(1)).smithMatrix());
        chainIdentities(reverseSphere); naturality(reverseSphere,2);
    }
    private static int direction(int a,int b) { return a==b?0:(b-a+3)%3==1?1:-1; }
    @Test public void all27DoubleCircleMapsAnd729CompositionsMatchIndependentWindingNumbers() {
        FiniteSimplicialComplex circle=circleCover().union(); SimplicialCover cover=new SimplicialCover(circle,circle);
        List<SimplicialCoverMap> maps=new ArrayList<>(); List<AbelianGroupHomomorphism> homology=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) {
            SimplicialCoverMap f=map(cover,cover,a,b,c); maps.add(f); AbelianGroupHomomorphism h=f.sumHomologyMap(z(1)); homology.add(h);
            int winding=(direction(a,b)+direction(b,c)+direction(c,a))/3; assertEquals(m(new long[]{winding,0},new long[]{0,winding}),h.smithMatrix());
            chainIdentities(f); naturality(f,1);
        }
        for(int i=0;i<27;i++) for(int j=0;j<27;j++) {
            SimplicialCoverMap f=maps.get(i),g=maps.get(j),composition=f.compose(g);
            assertEquals(f.sumChainMatrix(z(1)).multiply(g.sumChainMatrix(z(1))),composition.sumChainMatrix(z(1)));
            assertEquals(homology.get(i).compose(homology.get(j)),composition.sumHomologyMap(z(1)));
            boolean contiguous=true;
            for(int[] edge : new int[][]{{0,1},{0,2},{1,2}}) { Set<BigInteger> images=new HashSet<>(); for(int v : edge) { images.add(f.unionMap().mapVertex(z(v))); images.add(g.unionMap().mapVertex(z(v))); } if(images.size()>2) contiguous=false; }
            assertEquals(contiguous,f.contiguous(g)); if(contiguous) assertEquals(homology.get(i),homology.get(j));
        }
    }
    @Test public void connectingNaturalityHoldsEvenWhenTheChosenChainSplittingsDoNotCommute() {
        SimplicialCover source=circleCover(),target=new SimplicialCover(source.union(),source.right()); SimplicialCoverMap f=SimplicialCoverMap.inclusion(source,target);
        assertNotEquals(f.target().connectingChainMatrix(z(1)).multiply(f.unionMap().chainMatrix(z(1))),f.intersectionMap().chainMatrix(z(0)).multiply(f.source().connectingChainMatrix(z(1))));
        assertTrue(target.connectingHomologyMap(z(1)).isZero()); assertFalse(source.connectingHomologyMap(z(1)).isZero());
        naturality(f,0); naturality(f,1); chainIdentities(f);
    }
    @Test public void projectivePlaneMapsRetainTorsionAndTheIndexTwoNaturalitySquare() {
        FiniteSimplicialComplex plane=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        List<FiniteSet<Integer>> facets=new ArrayList<>(plane.simplices(2)); facets.remove(FiniteSet.of(0,1,2));
        SimplicialCover cover=new SimplicialCover(new FiniteSimplicialComplex(facets),complex(new int[]{0,1,2}));
        SimplicialCoverMap identity=SimplicialCoverMap.identity(cover),constant=map(cover,cover,0,0,0,0,0,0);
        assertEquals(AbelianGroupType.cyclic(z(2)),identity.unionHomologyMap(z(1)).source().type()); assertTrue(identity.unionHomologyMap(z(1)).isIsomorphism());
        assertTrue(constant.unionHomologyMap(z(1)).isZero()); assertTrue(constant.sumHomologyMap(z(1)).isZero());
        assertEquals(AbelianGroupType.cyclic(z(2)),cover.intersectionHomologyMap(z(1)).cokernel().type());
        for(int k=0;k<=2;k++) { naturality(identity,k); naturality(constant,k); }
    }
    @Test public void imageCoverIntersectionCanBeLargerThanTheImageOfTheSourceIntersection() {
        SimplicialCover source=new SimplicialCover(complex(new int[]{0}),complex(new int[]{1})),target=new SimplicialCover(complex(new int[]{7}),complex(new int[]{7}));
        SimplicialCoverMap f=map(source,target,7,7); assertEquals(complex(),f.intersectionMap().image()); assertEquals(complex(new int[]{7}),f.image().intersection());
        assertEquals(target,f.image()); assertFalse(f.isIsomorphism()); assertEquals(f,SimplicialCoverMap.inclusion(f.image(),target).compose(f.corestrictImage()));
        assertEquals(IntegerMatrix.identity(2),f.sumChainMatrix(z(0))); assertEquals(m(new long[]{1,1}),f.unionMap().chainMatrix(z(0)));
        assertEquals(IntegerMatrix.zero(1,0),f.intersectionMap().chainMatrix(z(0))); chainIdentities(f); naturality(f,0); naturality(f,1);
    }
    @Test public void inversesAndCompositionRequireCompleteOrderedCovers() {
        SimplicialCover cover=circleCover(); SimplicialCoverMap reflection=map(cover,cover,2,1,0);
        assertTrue(reflection.isIsomorphism()); assertEquals(SimplicialCoverMap.identity(cover),reflection.inverse().compose(reflection));
        SimplicialCover enlarged=new SimplicialCover(cover.union(),cover.right()); SimplicialCoverMap inclusion=SimplicialCoverMap.inclusion(cover,enlarged);
        assertTrue(inclusion.unionMap().isIsomorphism()); assertFalse(inclusion.isIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,inclusion::inverse);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> reflection.compose(SimplicialCoverMap.identity(enlarged)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> reflection.contiguous(SimplicialCoverMap.identity(enlarged)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialCoverMap.inclusion(enlarged,cover));
    }
    @Test public void contiguityMustHoldWithinEachPieceNotOnlyTheUnion() {
        SimplicialCover cover=new SimplicialCover(complex(new int[]{0,1}),complex(new int[]{0},new int[]{1}));
        SimplicialCoverMap identity=SimplicialCoverMap.identity(cover),reflection=map(cover,cover,1,0);
        assertTrue(identity.unionMap().contiguous(reflection.unionMap())); assertFalse(identity.contiguous(reflection));
        assertNotEquals(identity.rightHomologyMap(z(0)),reflection.rightHomologyMap(z(0)));
        SimplicialCover based=new SimplicialCover(cover.left(),complex(new int[]{0})); SimplicialCoverMap constant=map(based,based,0,0),basedIdentity=SimplicialCoverMap.identity(based);
        assertTrue(constant.contiguous(basedIdentity)); assertEquals(constant.sumHomologyMap(z(0)),basedIdentity.sumHomologyMap(z(0))); naturality(constant,0);
    }
    @Test public void simultaneousSwapAndComponentwiseRestrictionPreserveDeclaredBoundaries() {
        SimplicialCover cover=circleCover(); SimplicialCoverMap f=map(cover,cover,2,1,0),g=map(cover,cover,0,0,0);
        assertEquals(f,f.swap().swap()); assertEquals(f.rightMap(),f.swap().leftMap()); assertEquals(f.unionMap(),f.swap().unionMap());
        assertEquals(f.compose(g).swap(),f.swap().compose(g.swap())); naturality(f.swap(),1); chainIdentities(f.swap());
        SimplicialCover smaller=new SimplicialCover(complex(new int[]{0}),complex(new int[]{2})); SimplicialCoverMap restricted=f.restrict(smaller);
        assertEquals(smaller,restricted.source()); assertEquals(cover,restricted.target()); assertEquals(f.compose(SimplicialCoverMap.inclusion(smaller,cover)),restricted);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.restrict(new SimplicialCover(cover.union(),complex())));
    }
    @Test public void emptyHugeDegreeAndExtremeLabelsRetainBlockAndUnionOrders() {
        SimplicialCover empty=new SimplicialCover(complex(),complex()); SimplicialCoverMap emptyIdentity=SimplicialCoverMap.identity(empty),emptyTo=SimplicialCoverMap.inclusion(empty,circleCover());
        assertTrue(emptyIdentity.sumChainMatrices().isEmpty()); assertTrue(emptyIdentity.sumHomologyMaps().isEmpty()); naturality(emptyIdentity,0);
        assertEquals(IntegerMatrix.zero(5,0),emptyTo.sumChainMatrix(z(0))); assertEquals(IntegerMatrix.zero(3,0),emptyTo.sumChainMatrix(z(1))); naturality(emptyTo,1);
        assertTrue(emptyTo.sumHomologyMap(BigInteger.TEN.pow(100)).isIsomorphism()); assertEquals(IntegerMatrix.zero(0,0),emptyTo.sumChainMatrix(BigInteger.TEN.pow(100)));
        SimplicialCover source=new SimplicialCover(complex(new int[]{0}),complex(new int[]{1})),target=new SimplicialCover(complex(new int[]{Integer.MAX_VALUE}),complex(new int[]{Integer.MIN_VALUE}));
        SimplicialCoverMap relabel=map(source,target,Integer.MAX_VALUE,Integer.MIN_VALUE);
        assertEquals(IntegerMatrix.identity(2),relabel.sumChainMatrix(z(0))); assertEquals(m(new long[]{0,1},new long[]{1,0}),relabel.unionMap().chainMatrix(z(0)));
        assertTrue(relabel.isIsomorphism()); naturality(relabel,0);
    }
    @Test public void malformedPieceMapsWrongUnionsAndNegativeDegreesAreRejected() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}),circle=circleCover().union();
        SimplicialCover full=new SimplicialCover(triangle,triangle),boundaryLeft=new SimplicialCover(circle,triangle);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialCoverMap(full,boundaryLeft,FiniteSimplicialMap.identity(triangle)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialCoverMap(full,full,FiniteSimplicialMap.identity(circle)));
        SimplicialCover asymmetric=new SimplicialCover(complex(new int[]{0,1}),complex(new int[]{0}));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(asymmetric,asymmetric,1,0));
        SimplicialCoverMap f=SimplicialCoverMap.identity(circleCover());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.sumChainMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.sumHomologyMap(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.longExactMaps(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.sourceSumHomology(z(-1)));
    }
    @Test public void immutableValuesRespectCombinedDimensionsAndSharedListBudgets() {
        SimplicialCoverMap f=SimplicialCoverMap.identity(circleCover()); assertEquals(f,SimplicialCoverMap.identity(circleCover())); assertEquals(f.hashCode(),SimplicialCoverMap.identity(circleCover()).hashCode());
        assertThrows(UnsupportedOperationException.class,() -> f.sumChainMatrices().clear()); assertThrows(UnsupportedOperationException.class,() -> f.sumHomologyMaps().clear());
        assertThrows(UnsupportedOperationException.class,() -> f.longExactMaps(z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> f.excisionMaps().clear());
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<129;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex many=new FiniteSimplicialComplex(points);
        SimplicialCoverMap large=SimplicialCoverMap.identity(new SimplicialCover(many,many)); assertEquals(IntegerMatrix.identity(129),large.unionMap().chainMatrix(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.sumChainMatrix(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.sumHomologyMap(z(0)));
        FiniteSimplicialComplex fortyTwo=new FiniteSimplicialComplex(points.subList(0,42)); SimplicialCoverMap costly=SimplicialCoverMap.identity(new SimplicialCover(fortyTwo,fortyTwo));
        assertNotNull(costly.sumHomologyMap(z(0))); assertNotNull(costly.unionHomologyMap(z(0))); assertNotNull(costly.intersectionHomologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.longExactMaps(z(0)));
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int omit=0;omit<8;omit++) { List<Integer> face=new ArrayList<>(); for(int i=0;i<8;i++) if(i!=omit) face.add(i); facets.add(new FiniteSet<>(face)); }
        for(int i=0;i<70;i++) facets.add(FiniteSet.of(100+i));
        SimplicialCoverMap aggregate=SimplicialCoverMap.identity(new SimplicialCover(new FiniteSimplicialComplex(facets),complex()));
        for(int k=0;k<=6;k++) assertNotNull(aggregate.sumHomologyMap(z(k))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,aggregate::sumHomologyMaps);
    }
    @Test public void nativeScalarAndFlatMapsUseTheActualCarrierWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCover cover=circleCover(); SimplicialCoverMap f=map(cover,cover,2,1,0);
        IAlgebraItem<SimplicialCoverMap> item=math.simplicialMaps.algebra().buildAlgebraItem(f.unionMap()).performUnsafeOperation("CoverMap.from-map",new Pair<>(cover,cover));
        assertSame(math.coverMaps.algebra(),item.getAlgebra()); assertEquals(f,item.getResult()); assertSame(math.simplicialCovers.algebra(),item.performAlgebraTransfer("source").getAlgebra());
        assertSame(math.simplicialMaps.algebra(),item.performAlgebraTransfer("intersection-map").getAlgebra()); assertSame(math.coverMaps.algebra(),item.performCustomMemberOperation("restrict",cover).getAlgebra());
        for(IAlgebraItem<AbelianGroupHomomorphism> map : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-maps",z(1))) assertSame(math.abelianHomomorphisms.algebra(),map.getAlgebra());
        for(IAlgebraItem<RelativeSimplicialMap> map : item.<RelativeSimplicialMap>performAlgebraFlatTransfer("excision-maps")) assertSame(math.relativeMaps.algebra(),map.getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.coverMaps.boundaries).validate(new Pair<>(cover,"wrong")));
    }
    @Test public void serializedNaturalityFlowsRemainRepeatable() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialCover cover=circleCover(); SimplicialCoverMap f=map(cover,cover,2,1,0);
        IAlgebraFlow<IntegerMatrix> flow=math.flow(math.simplicialMaps,Collections.singletonList(f.unionMap()))
                .<SimplicialCoverMap,Pair<SimplicialCover,SimplicialCover>>performAlgebraUnsafe("CoverMap.from-map",new Pair<>(cover,cover))
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("union-homology-map",z(1)).<IntegerMatrix>performAlgebraTransfer("smith-matrix");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("ZMatrix(1x1)[[-1]]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","true","true","true"),math.flow(math.coverMaps,Collections.singletonList(f))
                .<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("long-exact-maps",z(1)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
        assertEquals(Arrays.asList("true","true"),math.flow(math.coverMaps,Collections.singletonList(f)).<RelativeSimplicialMap>performFlatAlgebraTransfer("excision-maps")
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("homology-map",z(1)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
