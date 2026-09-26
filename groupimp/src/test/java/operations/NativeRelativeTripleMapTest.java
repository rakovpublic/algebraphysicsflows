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

public class NativeRelativeTripleMapTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialTriple triple(FiniteSimplicialComplex x,FiniteSimplicialComplex a,FiniteSimplicialComplex b) { return new RelativeSimplicialTriple(new RelativeSimplicialComplex(x,a),b); }
    private static RelativeSimplicialTriple circle() { return triple(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}),complex(new int[]{0},new int[]{1},new int[]{2}),complex()); }
    private static RelativeSimplicialTripleMap map(RelativeSimplicialTriple from,RelativeSimplicialTriple to,int... images) {
        Map<BigInteger,BigInteger> values=new TreeMap<>(); int i=0; for(BigInteger vertex : FiniteSimplicialMap.vertexSet(from.outerPair().ambient()).members()) values.put(vertex,z(images[i++]));
        return new RelativeSimplicialTripleMap(from,to,new FiniteSimplicialMap(from.outerPair().ambient(),to.outerPair().ambient(),values));
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void naturality(RelativeSimplicialTripleMap f,int k) {
        List<AbelianGroupHomomorphism> from=f.source().longExactSegment(z(k)),to=f.target().longExactSegment(z(k)),vertical=f.longExactMaps(z(k));
        for(int i=0;i<3;i++) assertEquals(vertical.get(i+1).compose(from.get(i)),to.get(i).compose(vertical.get(i)));
        from=f.source().longExactCohomologySegment(z(k)); to=f.target().longExactCohomologySegment(z(k)); vertical=f.longExactCohomologyMaps(z(k));
        for(int i=0;i<3;i++) assertEquals(from.get(i).compose(vertical.get(i)),vertical.get(i+1).compose(to.get(i)));
        assertEquals(f.totalMap().compose(f.source().inclusionMap()),f.target().inclusionMap().compose(f.innerMap()));
        assertEquals(f.outerMap().compose(f.source().quotientMap()),f.target().quotientMap().compose(f.totalMap()));
    }
    @Test public void all27CircleMapsAnd729CompositionsMatchWindingNumbersAndBothNaturalityDiagrams() {
        RelativeSimplicialTriple t=circle(); List<RelativeSimplicialTripleMap> maps=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) {
            RelativeSimplicialTripleMap f=map(t,t,a,b,c); maps.add(f); int winding=0; int[] values={a,b,c,a};
            for(int i=0;i<3;i++) { int step=Math.floorMod(values[i+1]-values[i],3); winding+=step==2?-1:step; } winding/=3;
            assertEquals(z(winding),f.totalHomologyMap(z(1)).smithMatrix().get(0,0));
            AbelianGroupHomomorphism cohomology=f.totalCohomologyMap(z(1)); AbelianGroupElement generator=cohomology.source().smithGenerators().get(0);
            assertEquals(generator.scale(z(winding)),cohomology.apply(generator));
            assertEquals(f.outerMap().homologyMap(z(1)),f.outerHomologyMap(z(1))); assertEquals(f.innerMap().homologyMap(z(0)),f.innerHomologyMap(z(0)));
            naturality(f,0); naturality(f,1);
        }
        for(RelativeSimplicialTripleMap after : maps) for(RelativeSimplicialTripleMap before : maps) {
            RelativeSimplicialTripleMap composed=after.compose(before);
            assertEquals(after.ambientMap().compose(before.ambientMap()),composed.ambientMap());
            assertEquals(after.outerMap().chainMatrix(z(1)).multiply(before.outerMap().chainMatrix(z(1))),composed.outerMap().chainMatrix(z(1)));
            assertEquals(after.totalHomologyMap(z(1)).compose(before.totalHomologyMap(z(1))),composed.totalHomologyMap(z(1)));
            assertEquals(before.totalCohomologyMap(z(1)).compose(after.totalCohomologyMap(z(1))),composed.totalCohomologyMap(z(1)));
        }
    }
    @Test public void basedIntervalReflectionRetainsSignsAndTypedConnectingRepresentatives() {
        FiniteSimplicialComplex x=complex(new int[]{0,1}),a=complex(new int[]{0},new int[]{1});
        RelativeSimplicialTriple from=triple(x,a,complex(new int[]{0})),to=triple(x,a,complex(new int[]{1})); RelativeSimplicialTripleMap f=map(from,to,1,0);
        assertEquals(z(-1),f.outerHomologyMap(z(1)).smithMatrix().get(0,0)); assertEquals(z(1),f.innerHomologyMap(z(0)).smithMatrix().get(0,0));
        assertEquals(z(-1),f.outerCohomologyMap(z(1)).smithMatrix().get(0,0)); assertTrue(f.isIsomorphism()); assertEquals(RelativeSimplicialTripleMap.identity(from),f.inverse().compose(f));
        RelativeSimplicialChain c=new RelativeSimplicialChain(from.outerPair(),z(1),v(3));
        assertEquals(to.connectCycle(c.pushforward(f.outerMap())),from.connectCycle(c).pushforward(f.innerMap()));
        RelativeSimplicialCochain phi=new RelativeSimplicialCochain(to.innerPair(),z(0),v(2));
        assertEquals(from.connectCocycle(phi.pullback(f.innerMap())),to.connectCocycle(phi).pullback(f.outerMap())); naturality(f,0); naturality(f,1);
    }
    @Test public void degreeTwoCircleCoverRetainsNontrivialIntegralIndex() {
        FiniteSimplicialComplex six=complex(new int[]{0,1},new int[]{1,2},new int[]{2,3},new int[]{3,4},new int[]{4,5},new int[]{0,5});
        RelativeSimplicialTriple from=triple(six,six.skeleton(0),complex(new int[]{0})),to=triple(circle().outerPair().ambient(),circle().outerPair().subcomplex(),complex(new int[]{0}));
        RelativeSimplicialTripleMap f=map(from,to,0,1,2,0,1,2);
        assertEquals(z(2),f.totalHomologyMap(z(1)).smithMatrix().get(0,0).abs()); assertEquals(AbelianGroupType.cyclic(z(2)),f.totalHomologyMap(z(1)).cokernel().type());
        assertEquals(AbelianGroupType.cyclic(z(2)),f.totalCohomologyMap(z(1)).cokernel().type()); assertFalse(f.isIsomorphism()); naturality(f,0); naturality(f,1);
    }
    @Test public void all256TetrahedronMapsMatchIndependentOrientedQuotientMatrices() {
        RelativeSimplicialTriple from=triple(complex(new int[]{0,1,2,3}),complex(new int[]{0,1,2}),complex(new int[]{0,1}));
        for(int code=0;code<256;code++) {
            int[] images=new int[4]; int value=code; for(int i=0;i<4;i++) { images[i]=value%4; value/=4; }
            RelativeSimplicialTriple to=triple(from.outerPair().ambient(),complex(new int[]{images[0],images[1],images[2]}),complex(new int[]{images[0],images[1]}));
            RelativeSimplicialTripleMap f=map(from,to,images);
            for(RelativeSimplicialMap part : Arrays.asList(f.outerMap(),f.totalMap(),f.innerMap())) for(int k=0;k<=3;k++) {
                List<FiniteSet<BigInteger>> columns=part.source().simplexBasis(z(k)),rows=part.target().simplexBasis(z(k)); BigInteger[][] expected=new BigInteger[rows.size()][columns.size()];
                for(BigInteger[] row : expected) Arrays.fill(row,BigInteger.ZERO);
                for(int col=0;col<columns.size();col++) {
                    List<BigInteger> labels=new ArrayList<>(columns.get(col).members()); Collections.sort(labels); List<BigInteger> image=new ArrayList<>(); for(BigInteger vertex : labels) image.add(z(images[vertex.intValueExact()]));
                    if(new HashSet<>(image).size()!=image.size()) continue; int sign=1; for(int i=0;i<image.size();i++) for(int j=i+1;j<image.size();j++) if(image.get(i).compareTo(image.get(j))>0) sign=-sign;
                    int row=rows.indexOf(new FiniteSet<>(image)); if(row>=0) expected[row][col]=z(sign);
                }
                assertEquals(new IntegerMatrix(rows.size(),columns.size(),expected),part.chainMatrix(z(k)));
                if(k>0) assertEquals(part.target().boundaryMatrix(z(k)).multiply(part.chainMatrix(z(k))),part.chainMatrix(z(k-1)).multiply(part.source().boundaryMatrix(z(k))));
            }
        }
    }
    @Test public void torsionPresentationsSurviveTripleMapNaturality() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        RelativeSimplicialTriple t=triple(x,x.skeleton(1),complex(new int[]{0})); RelativeSimplicialTripleMap f=RelativeSimplicialTripleMap.identity(t);
        assertEquals(AbelianGroupType.cyclic(z(2)),f.totalHomologyMap(z(1)).source().type()); assertEquals(AbelianGroupHomomorphism.identity(t.totalPair().homology(z(1)).group()),f.totalHomologyMap(z(1)));
        assertEquals(AbelianGroupType.cyclic(z(2)),f.totalCohomologyMap(z(2)).source().type()); assertTrue(f.totalCohomologyMap(z(2)).isIsomorphism());
        naturality(f,0); naturality(f,1); naturality(f,2);
    }
    @Test public void contiguityChecksAmbientMiddleAndBaseSeparately() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}),points=complex(new int[]{0},new int[]{1}),point=complex(new int[]{0});
        RelativeSimplicialTriple source=triple(point,point,point),target=triple(edge,edge,edge); RelativeSimplicialTripleMap zero=map(source,target,0),one=map(source,target,1);
        assertTrue(zero.contiguous(one)); assertEquals(zero.longExactMaps(z(0)),one.longExactMaps(z(0))); assertEquals(zero.longExactCohomologyMaps(z(0)),one.longExactCohomologyMaps(z(0)));
        RelativeSimplicialTriple absolutePoint=triple(point,complex(),complex()),absoluteEdge=triple(edge,complex(),complex());
        RelativeSimplicialTripleMap left=map(absolutePoint,absoluteEdge,0),right=map(absolutePoint,absoluteEdge,1);
        assertTrue(left.contiguous(right)); assertEquals(AbelianGroupType.Z,left.totalHomologyMap(z(0)).source().type()); assertEquals(left.totalHomologyMap(z(0)),right.totalHomologyMap(z(0))); assertEquals(left.totalCohomologyMap(z(0)),right.totalCohomologyMap(z(0)));
        RelativeSimplicialTriple discreteMiddle=triple(edge,points,complex()); RelativeSimplicialTriple s=triple(point,point,complex());
        RelativeSimplicialTripleMap a=map(s,discreteMiddle,0),b=map(s,discreteMiddle,1); assertTrue(a.ambientMap().contiguous(b.ambientMap())); assertFalse(a.contiguous(b)); assertNotEquals(a.innerHomologyMap(z(0)),b.innerHomologyMap(z(0)));
        RelativeSimplicialTriple discreteBase=triple(edge,edge,points); a=map(source,discreteBase,0); b=map(source,discreteBase,1);
        assertTrue(a.ambientMap().contiguous(b.ambientMap())); assertTrue(a.outerMap().subcomplexMap().contiguous(b.outerMap().subcomplexMap())); assertFalse(a.contiguous(b));
        RelativeSimplicialTriple disjoint=triple(points,complex(),complex()),absolute=triple(point,complex(),complex()); assertFalse(map(absolute,disjoint,0).contiguous(map(absolute,disjoint,1)));
    }
    @Test public void imagesRestrictionsAndCorestrictionsRetainEveryNestedComponent() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2}); RelativeSimplicialTriple t=triple(x,complex(new int[]{0,1}),complex(new int[]{0})); RelativeSimplicialTripleMap f=map(t,t,0,0,1);
        RelativeSimplicialTriple expected=triple(complex(new int[]{0,1}),complex(new int[]{0}),complex(new int[]{0})); assertEquals(expected,f.image());
        assertEquals(f,RelativeSimplicialTripleMap.inclusion(f.image(),t).compose(f.corestrictImage())); assertTrue(f.corestrictImage().ambientMap().isSurjective());
        RelativeSimplicialTriple sub=triple(complex(new int[]{0,2}),complex(new int[]{0}),complex(new int[]{0}));
        assertEquals(f.restrict(sub),f.compose(RelativeSimplicialTripleMap.inclusion(sub,t))); assertEquals(t,f.restrict(sub).target());
        assertEquals(RelativeSimplicialTripleMap.identity(t),RelativeSimplicialTripleMap.identity(t).corestrictImage());
    }
    @Test public void ambientBijectionAloneDoesNotMakeATripleIsomorphism() {
        FiniteSimplicialComplex x=complex(new int[]{0,1}),p=complex(new int[]{0}); RelativeSimplicialTriple smallA=triple(x,p,complex()),largeA=triple(x,x,complex());
        RelativeSimplicialTripleMap f=RelativeSimplicialTripleMap.inclusion(smallA,largeA); assertTrue(f.ambientMap().isIsomorphism()); assertFalse(f.isIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,f::inverse);
        RelativeSimplicialTriple smallB=triple(x,x,p),largeB=triple(x,x,x); RelativeSimplicialTripleMap g=RelativeSimplicialTripleMap.inclusion(smallB,largeB); assertFalse(g.isIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,g::inverse);
        RelativeSimplicialTriple swapped=triple(x,complex(new int[]{1}),complex()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> RelativeSimplicialTripleMap.identity(swapped).compose(RelativeSimplicialTripleMap.identity(smallA)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> RelativeSimplicialTripleMap.identity(smallB).compose(RelativeSimplicialTripleMap.identity(largeB)));
    }
    @Test public void degreeZeroNaturalityRetainsTheTypedNegativeDegreePresentations() {
        FiniteSimplicialComplex x=complex(new int[]{0,1}),a=complex(new int[]{0},new int[]{1}); RelativeSimplicialTriple from=triple(x,a,complex()),to=triple(x,a,complex(new int[]{0}));
        RelativeSimplicialTripleMap f=RelativeSimplicialTripleMap.inclusion(from,to); AbelianGroupHomomorphism negative=f.longExactMaps(z(0)).get(3);
        assertEquals(from.connectingHomology(z(0)).target(),negative.source()); assertEquals(to.connectingHomology(z(0)).target(),negative.target());
        assertEquals(IntegerMatrix.zero(0,2),negative.source().relations()); assertEquals(IntegerMatrix.zero(0,1),negative.target().relations()); assertTrue(negative.isIsomorphism()); naturality(f,0);
    }
    @Test public void rawConnectingMatricesCanFailNaturalityWhileBothExactDiagramsCommute() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}); RelativeSimplicialTriple t=triple(edge,complex(new int[]{0}),complex()); RelativeSimplicialTripleMap f=map(t,t,0,0);
        assertNotEquals(f.innerMap().chainMatrix(z(0)).multiply(t.connectingChainMatrix(z(1))),t.connectingChainMatrix(z(1)).multiply(f.outerMap().chainMatrix(z(1))));
        naturality(f,0); naturality(f,1);
    }
    @Test public void malformedMapsWrongFullContextsAndNegativeDegreesAreRejected() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}),edge=complex(new int[]{0,1}),p=complex(new int[]{0}); RelativeSimplicialTriple t=triple(triangle,edge,p);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(t,t,0,2,1)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map(t,t,1,0,2));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new RelativeSimplicialTripleMap(t,t,FiniteSimplicialMap.identity(edge)));
        RelativeSimplicialTripleMap f=RelativeSimplicialTripleMap.identity(t); RelativeSimplicialTriple wrongBase=triple(triangle,edge,edge);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.restrict(wrongBase)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.restrict(triple(triangle,triangle,p)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> f.contiguous(RelativeSimplicialTripleMap.identity(wrongBase)));
        for(Runnable action : Arrays.<Runnable>asList(() -> f.outerHomologyMap(z(-1)),() -> f.totalHomologyMap(z(-1)),() -> f.innerHomologyMap(z(-1)),() -> f.outerCohomologyMap(z(-1)),() -> f.totalCohomologyMap(z(-1)),() -> f.innerCohomologyMap(z(-1)),() -> f.longExactMaps(z(-1)),() -> f.longExactCohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
    }
    @Test public void emptyHugeDegreesAndExtremeLabelsPreserveExactContexts() {
        RelativeSimplicialTriple empty=triple(complex(),complex(),complex()); RelativeSimplicialTripleMap e=RelativeSimplicialTripleMap.identity(empty); naturality(e,0); assertEquals(empty,e.image());
        BigInteger huge=BigInteger.TEN.pow(100); assertTrue(e.longExactMaps(huge).stream().allMatch(AbelianGroupHomomorphism::isIsomorphism)); assertTrue(e.longExactCohomologyMaps(huge).stream().allMatch(AbelianGroupHomomorphism::isIsomorphism));
        RelativeSimplicialTriple from=triple(complex(new int[]{0,1}),complex(new int[]{0},new int[]{1}),complex(new int[]{0})),to=triple(complex(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE}),complex(new int[]{Integer.MIN_VALUE},new int[]{Integer.MAX_VALUE}),complex(new int[]{Integer.MAX_VALUE}));
        RelativeSimplicialTripleMap f=map(from,to,Integer.MAX_VALUE,Integer.MIN_VALUE); assertTrue(f.isIsomorphism()); assertEquals(z(-1),f.outerMap().chainMatrix(z(1)).get(0,0)); naturality(f,0); naturality(f,1);
        assertEquals(f,map(from,to,Integer.MAX_VALUE,Integer.MIN_VALUE)); assertEquals(f.hashCode(),map(from,to,Integer.MAX_VALUE,Integer.MIN_VALUE).hashCode()); assertThrows(UnsupportedOperationException.class,() -> f.longExactMaps(z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> f.longExactCohomologyMaps(z(0)).clear());
    }
    @Test public void filteredBasesAvoidFullAmbientLimits() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i));
        RelativeSimplicialTriple t=triple(new FiniteSimplicialComplex(points),new FiniteSimplicialComplex(points.subList(0,299)),new FiniteSimplicialComplex(points.subList(0,298))); RelativeSimplicialTripleMap f=RelativeSimplicialTripleMap.identity(t);
        assertEquals(IntegerMatrix.identity(1),f.outerMap().chainMatrix(z(0))); assertEquals(IntegerMatrix.identity(2),f.totalMap().chainMatrix(z(0))); assertEquals(IntegerMatrix.identity(1),f.innerMap().chainMatrix(z(0))); naturality(f,0);
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> f.ambientMap().chainMatrix(z(0)));
    }
    @Test public void bothFourMapListsShareOneBudgetEvenWhenAllScalarMapsFit() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<70;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex x=new FiniteSimplicialComplex(points);
        RelativeSimplicialTripleMap f=RelativeSimplicialTripleMap.identity(triple(x,x,complex()));
        assertNotNull(f.outerHomologyMap(z(0))); assertNotNull(f.totalHomologyMap(z(0))); assertNotNull(f.innerHomologyMap(z(0)));
        assertNotNull(f.outerCohomologyMap(z(0))); assertNotNull(f.totalCohomologyMap(z(0))); assertNotNull(f.innerCohomologyMap(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> f.longExactMaps(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> f.longExactCohomologyMaps(z(0)));
    }
    @Test public void nativeWrappersAndSerializedNaturalityFlowsUseTheOriginalAlgebras() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialTriple t=circle(); RelativeSimplicialTripleMap f=map(t,t,0,2,1);
        IAlgebraItem<RelativeSimplicialTripleMap> item=math.simplicialMaps.algebra().buildAlgebraItem(f.ambientMap()).performUnsafeOperation("TripleMap.from-map",new Pair<>(t,t)); assertSame(math.tripleMaps.algebra(),item.getAlgebra()); assertEquals(f,item.perform().getResult());
        assertSame(math.relativeTriples.algebra(),item.performAlgebraTransfer("source").getAlgebra()); assertSame(math.relativeMaps.algebra(),item.performAlgebraTransfer("inner-map").getAlgebra()); assertSame(math.tripleMaps.algebra(),item.performCustomMemberOperation("restrict",t).getAlgebra());
        for(IAlgebraItem<AbelianGroupHomomorphism> h : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-cohomology-maps",z(0))) assertSame(math.abelianHomomorphisms.algebra(),h.getAlgebra());
        assertFalse(((algebra.imp.Algebra)math.tripleMaps.boundaries).validate(new Pair<>(t,"wrong")));
        IAlgebraFlow<IntegerMatrix> flow=math.flow(math.simplicialMaps,Collections.singletonList(f.ambientMap()))
                .<RelativeSimplicialTripleMap,Pair<RelativeSimplicialTriple,RelativeSimplicialTriple>>performAlgebraUnsafe("TripleMap.from-map",new Pair<>(t,t))
                .<AbelianGroupHomomorphism,BigInteger>performAlgebraUnsafe("total-cohomology-map",z(1)).<IntegerMatrix>performAlgebraTransfer("smith-matrix");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("ZMatrix(3x3)[[0, 0, 0], [0, 0, 0], [0, 0, -1]]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","true","true","true"),math.flow(math.tripleMaps,Collections.singletonList(f)).<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("long-exact-maps",z(1)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
