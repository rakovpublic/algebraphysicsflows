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

public class NativeRelativeTripleTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static RelativeSimplicialTriple triple(FiniteSimplicialComplex x,FiniteSimplicialComplex a,FiniteSimplicialComplex b) { return new RelativeSimplicialTriple(new RelativeSimplicialComplex(x,a),b); }
    private static RelativeSimplicialTriple interval() { return triple(complex(new int[]{0,1}),complex(new int[]{0},new int[]{1}),complex(new int[]{0})); }
    private static FiniteSimplicialComplex plane() { return complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5}); }
    private static RelativeSimplicialChain chain(RelativeSimplicialComplex pair,int degree,long... values) { return new RelativeSimplicialChain(pair,z(degree),v(values)); }
    private static RelativeSimplicialCochain cochain(RelativeSimplicialComplex pair,int degree,long... values) { return new RelativeSimplicialCochain(pair,z(degree),v(values)); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void exact(AbelianGroupHomomorphism first,AbelianGroupHomomorphism second) {
        assertEquals(first.target(),second.source()); assertTrue(second.compose(first).isZero());
        for(AbelianGroupElement generator : second.kernelInclusion().generatorImages()) assertTrue(first.hasPreimage(generator));
    }
    private static void exactSegments(RelativeSimplicialTriple t,int degree) {
        List<AbelianGroupHomomorphism> h=t.longExactSegment(z(degree)),c=t.longExactCohomologySegment(z(degree));
        assertEquals(Arrays.asList(t.inclusionHomology(z(degree)),t.quotientHomology(z(degree)),t.connectingHomology(z(degree))),h);
        assertEquals(Arrays.asList(t.extensionCohomology(z(degree)),t.restrictionCohomology(z(degree)),t.connectingCohomology(z(degree))),c);
        exact(h.get(0),h.get(1)); exact(h.get(1),h.get(2)); if(degree>0) exact(h.get(2),t.inclusionHomology(z(degree-1)));
        exact(c.get(0),c.get(1)); exact(c.get(1),c.get(2)); exact(c.get(2),t.extensionCohomology(z(degree+1)));
    }
    @Test public void intervalBasedAtOneEndpointHasConnectingIsomorphismsAndTypedRepresentatives() {
        RelativeSimplicialTriple t=interval(); RelativeSimplicialChain c=chain(t.outerPair(),1,3); RelativeSimplicialCochain phi=cochain(t.innerPair(),0,2);
        assertEquals(v(3),t.connectCycle(c).coordinates()); assertEquals(t.innerPair(),t.connectCycle(c).pair()); assertTrue(t.connectCycle(c).isCycle());
        assertEquals(v(2),t.connectCocycle(phi).coordinates()); assertEquals(t.outerPair(),t.connectCocycle(phi).pair()); assertTrue(t.connectCocycle(phi).isCocycle());
        assertEquals(t.connectCycle(c).classOf(),t.connectingHomology(z(1)).apply(c.classOf())); assertEquals(t.connectCocycle(phi).classOf(),t.connectingCohomology(z(0)).apply(phi.classOf()));
        assertEquals(z(6),t.connectCycle(c).evaluate(phi)); assertEquals(c.evaluate(t.connectCocycle(phi)),t.connectCycle(c).evaluate(phi));
        assertTrue(t.connectingHomology(z(1)).isIsomorphism()); assertTrue(t.connectingCohomology(z(0)).isIsomorphism());
        assertEquals(IntegerMatrix.identity(1),t.connectingChainMatrix(z(1))); assertEquals(IntegerMatrix.identity(1),t.connectingCochainMatrix(z(0)));
        exactSegments(t,0); exactSegments(t,1);
    }
    @Test public void all216ThreeVertexGraphsAndNestedVertexSubsetsMatchIndependentRanksAndExactness() {
        int[][] edges={{0,1},{0,2},{1,2}};
        for(int mask=0;mask<8;mask++) {
            List<int[]> facets=new ArrayList<>(); boolean[][] connected=new boolean[3][3];
            for(int i=0;i<3;i++) { facets.add(new int[]{i}); connected[i][i]=true; }
            for(int e=0;e<3;e++) if((mask&(1<<e))!=0) { facets.add(edges[e]); connected[edges[e][0]][edges[e][1]]=connected[edges[e][1]][edges[e][0]]=true; }
            for(int k=0;k<3;k++) for(int i=0;i<3;i++) for(int j=0;j<3;j++) connected[i][j]|=connected[i][k]&&connected[k][j];
            for(int selection=0;selection<27;selection++) {
                List<int[]> a=new ArrayList<>(),b=new ArrayList<>(); int[] state=new int[3]; int code=selection;
                for(int i=0;i<3;i++) { state[i]=code%3; code/=3; if(state[i]>0) a.add(new int[]{i}); if(state[i]==2) b.add(new int[]{i}); }
                int components=0,touchesA=0,touchesB=0;
                for(int i=0;i<3;i++) { boolean first=true; for(int j=0;j<i;j++) if(connected[i][j]) first=false; if(!first) continue;
                    components++; boolean inA=false,inB=false; for(int j=0;j<3;j++) if(connected[i][j]) { inA|=state[j]>0; inB|=state[j]==2; } if(inA) touchesA++; if(inB) touchesB++;
                }
                RelativeSimplicialTriple t=triple(complex(facets.toArray(new int[0][])),complex(a.toArray(new int[0][])),complex(b.toArray(new int[0][])));
                int cycles=Integer.bitCount(mask)-3+components,deltaRank=a.size()-b.size()-touchesA+touchesB;
                assertEquals(AbelianGroupType.free(z(cycles+a.size()-touchesA)),t.outerPair().homologyType(z(1)));
                assertEquals(AbelianGroupType.free(z(cycles+b.size()-touchesB)),t.totalPair().homologyType(z(1)));
                assertEquals(AbelianGroupType.free(z(components-touchesB)),t.totalPair().homologyType(z(0)));
                assertEquals(AbelianGroupType.free(z(a.size()-b.size())),t.innerPair().homologyType(z(0)));
                assertEquals(AbelianGroupType.free(z(deltaRank)),t.connectingHomology(z(1)).image().type());
                assertEquals(AbelianGroupType.free(z(deltaRank)),t.connectingCohomology(z(0)).image().type());
                exactSegments(t,0); exactSegments(t,1);
            }
        }
    }
    @Test public void everyNestedTriangleSubcomplexHasSplitChainGroupsAndCorrectBoundaryIdentities() {
        FiniteSimplicialComplex x=complex(new int[]{0,1,2}); List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int k=0;k<=2;k++) faces.addAll(x.simplices(k));
        Set<FiniteSimplicialComplex> subcomplexes=new HashSet<>();
        for(int mask=0;mask<128;mask++) { List<FiniteSet<Integer>> selected=new ArrayList<>(); for(int i=0;i<7;i++) if((mask&(1<<i))!=0) selected.add(faces.get(i)); subcomplexes.add(new FiniteSimplicialComplex(selected)); }
        assertEquals(19,subcomplexes.size());
        for(FiniteSimplicialComplex a : subcomplexes) for(FiniteSimplicialComplex b : subcomplexes) if(b.subcomplexOf(a)) {
            RelativeSimplicialTriple t=triple(x,a,b);
            for(int k=0;k<=3;k++) {
                BigInteger n=z(k); IntegerMatrix i=t.inclusionMatrix(n),q=t.quotientMatrix(n),l=t.liftMatrix(n);
                assertEquals(IntegerMatrix.identity(q.rows()),q.multiply(l)); assertEquals(IntegerMatrix.zero(q.rows(),i.columns()),q.multiply(i));
                assertEquals(IntegerMatrix.identity(q.columns()),i.multiply(i.transpose()).add(l.multiply(q)));
                assertEquals(i,t.inclusionMap().chainMatrix(n)); assertEquals(q,t.quotientMap().chainMatrix(n));
                assertEquals(q.transpose(),t.extensionMatrix(n)); assertEquals(i.transpose(),t.restrictionMatrix(n)); assertEquals(t.connectingChainMatrix(z(k+1)).transpose(),t.connectingCochainMatrix(n));
                if(k>0) {
                    assertEquals(t.totalPair().boundaryMatrix(n).multiply(i),t.inclusionMatrix(z(k-1)).multiply(t.innerPair().boundaryMatrix(n)));
                    assertEquals(t.outerPair().boundaryMatrix(n).multiply(q),t.quotientMatrix(z(k-1)).multiply(t.totalPair().boundaryMatrix(n)));
                    assertEquals(t.totalPair().boundaryMatrix(n).multiply(l),t.liftMatrix(z(k-1)).multiply(t.outerPair().boundaryMatrix(n)).add(t.inclusionMatrix(z(k-1)).multiply(t.connectingChainMatrix(n))));
                }
                if(k>1) assertEquals(IntegerMatrix.zero(t.innerPair().simplexCount(z(k-2)),t.outerPair().simplexCount(n)),t.innerPair().boundaryMatrix(z(k-1)).multiply(t.connectingChainMatrix(n)).add(t.connectingChainMatrix(z(k-1)).multiply(t.outerPair().boundaryMatrix(n))));
            }
        }
        RelativeSimplicialTriple t=interval(); assertNotEquals(IntegerMatrix.zero(1,1),t.totalPair().boundaryMatrix(z(1)).multiply(t.liftMatrix(z(1))));
        RelativeSimplicialTriple pointed=triple(complex(new int[]{0,1}),complex(new int[]{0}),complex());
        Map<BigInteger,BigInteger> collapse=new TreeMap<>(); collapse.put(z(0),z(0)); collapse.put(z(1),z(0));
        RelativeSimplicialMap f=new RelativeSimplicialMap(pointed.outerPair(),pointed.outerPair(),new FiniteSimplicialMap(pointed.outerPair().ambient(),pointed.outerPair().ambient(),collapse));
        assertNotEquals(pointed.connectingChainMatrix(z(1)),pointed.connectingChainMatrix(z(1)).multiply(f.chainMatrix(z(1)))); // The inner map is identity; raw connecting matrices need not be natural.
    }
    @Test public void disksThroughDimensionFourHaveIntegralAndDualConnectingIsomorphisms() {
        for(int n=1;n<=4;n++) {
            int[] simplex=new int[n+1]; for(int i=0;i<=n;i++) simplex[i]=i; List<int[]> boundary=new ArrayList<>();
            for(int omit=0;omit<=n;omit++) { int[] face=new int[n]; int j=0; for(int i=0;i<=n;i++) if(i!=omit) face[j++]=i; boundary.add(face); }
            RelativeSimplicialTriple t=triple(complex(simplex),complex(boundary.toArray(new int[0][])),complex(new int[]{0}));
            assertTrue(t.connectingHomology(z(n)).isIsomorphism()); assertTrue(t.connectingCohomology(z(n-1)).isIsomorphism());
            RelativeSimplicialChain fundamental=chain(t.outerPair(),n,1),connected=t.connectCycle(fundamental);
            assertEquals(AbelianGroupType.Z,connected.homology().type()); assertFalse(connected.classOf().isZero());
            assertEquals(connected.classOf(),t.connectingHomology(z(n)).apply(fundamental.classOf())); exactSegments(t,n-1); exactSegments(t,n);
        }
    }
    @Test public void projectivePlaneTripleRetainsIndexTwoConnectingImageAndCohomologicalTorsion() {
        FiniteSimplicialComplex x=plane(); RelativeSimplicialTriple t=triple(x,x.skeleton(1),complex(new int[]{0}));
        assertEquals(AbelianGroupType.cyclic(z(2)),t.totalPair().homologyType(z(1))); assertEquals(AbelianGroupType.free(z(10)),t.outerPair().homologyType(z(2)));
        assertTrue(t.connectingHomology(z(2)).isInjective()); assertEquals(AbelianGroupType.cyclic(z(2)),t.connectingHomology(z(2)).cokernel().type());
        assertTrue(t.connectingCohomology(z(1)).isInjective()); assertEquals(AbelianGroupType.cyclic(z(2)),t.connectingCohomology(z(1)).cokernel().type());
        assertEquals(AbelianGroupType.cyclic(z(2)),t.extensionCohomology(z(2)).target().type());
        exactSegments(t,0); exactSegments(t,1); exactSegments(t,2);
    }
    @Test public void emptyBaseRecoversExistingPairSequencesAndDegenerateTriplesGiveIdentityMaps() {
        RelativeSimplicialTriple t=triple(complex(new int[]{0,1,2}),complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}),complex()); RelativeSimplicialComplex p=t.outerPair();
        for(int k=0;k<=3;k++) {
            assertEquals(p.inclusionMatrix(z(k)),t.inclusionMatrix(z(k))); assertEquals(p.projectionMatrix(z(k)),t.quotientMatrix(z(k)));
            assertEquals(p.inclusionHomology(z(k)),t.inclusionHomology(z(k))); assertEquals(p.quotientHomology(z(k)),t.quotientHomology(z(k))); if(k>0) assertEquals(p.connectingHomology(z(k)),t.connectingHomology(z(k)));
            assertEquals(RelativeSimplicialCochain.longExactSegment(p,z(k)),t.longExactCohomologySegment(z(k)));
        }
        RelativeSimplicialTriple equalBase=new RelativeSimplicialTriple(p,p.subcomplex()),equalAmbient=triple(p.ambient(),p.ambient(),p.subcomplex());
        for(int k=0;k<=2;k++) {
            assertEquals(AbelianGroupHomomorphism.identity(p.homology(z(k)).group()),equalBase.quotientHomology(z(k)));
            assertEquals(AbelianGroupHomomorphism.identity(p.homology(z(k)).group()),equalAmbient.inclusionHomology(z(k)));
            exactSegments(equalBase,k); exactSegments(equalAmbient,k);
        }
    }
    @Test public void connectingClassesAreIndependentOfRelativeRepresentatives() {
        RelativeSimplicialTriple t=triple(complex(new int[]{0,1,2}),complex(new int[]{0},new int[]{1}),complex(new int[]{0}));
        RelativeSimplicialChain c=chain(t.outerPair(),1,1,0,0),changed=c.add(chain(t.outerPair(),2,7).boundary());
        assertNotEquals(c,changed); assertEquals(t.connectCycle(c).classOf(),t.connectCycle(changed).classOf());
        RelativeSimplicialTriple disk=triple(complex(new int[]{0,1,2}),complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}),complex(new int[]{0}));
        RelativeSimplicialCochain phi=cochain(disk.innerPair(),1,1,0,0),other=phi.add(cochain(disk.innerPair(),0,2,5).coboundary());
        assertNotEquals(phi,other); assertEquals(disk.connectCocycle(phi).classOf(),disk.connectCocycle(other).classOf());
        assertEquals(disk.connectingCohomology(z(1)).apply(phi.classOf()),disk.connectCocycle(phi).classOf());
    }
    @Test public void all27TriangleVertexMapsCommuteWithBothTripleExactSequences() {
        FiniteSimplicialComplex x=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}),a=complex(new int[]{0},new int[]{1},new int[]{2}),b=complex(new int[]{0}); RelativeSimplicialTriple t=triple(x,a,b);
        for(int u=0;u<3;u++) for(int v=0;v<3;v++) for(int w=0;w<3;w++) {
            Map<BigInteger,BigInteger> values=new TreeMap<>(); values.put(z(0),z(u)); values.put(z(1),z(v)); values.put(z(2),z(w)); FiniteSimplicialMap f=new FiniteSimplicialMap(x,x,values);
            RelativeSimplicialTriple target=triple(x,complex(new int[]{u},new int[]{v},new int[]{w}),complex(new int[]{u}));
            RelativeSimplicialMap outer=new RelativeSimplicialMap(t.outerPair(),target.outerPair(),f),total=new RelativeSimplicialMap(t.totalPair(),target.totalPair(),f),inner=new RelativeSimplicialMap(t.innerPair(),target.innerPair(),outer.subcomplexMap());
            for(int k=0;k<=2;k++) {
                assertEquals(total.homologyMap(z(k)).compose(t.inclusionHomology(z(k))),target.inclusionHomology(z(k)).compose(inner.homologyMap(z(k))));
                assertEquals(outer.homologyMap(z(k)).compose(t.quotientHomology(z(k))),target.quotientHomology(z(k)).compose(total.homologyMap(z(k))));
                if(k>0) assertEquals(inner.homologyMap(z(k-1)).compose(t.connectingHomology(z(k))),target.connectingHomology(z(k)).compose(outer.homologyMap(z(k))));
                assertEquals(RelativeSimplicialCochain.cohomologyMap(total,z(k)).compose(target.extensionCohomology(z(k))),t.extensionCohomology(z(k)).compose(RelativeSimplicialCochain.cohomologyMap(outer,z(k))));
                assertEquals(RelativeSimplicialCochain.cohomologyMap(inner,z(k)).compose(target.restrictionCohomology(z(k))),t.restrictionCohomology(z(k)).compose(RelativeSimplicialCochain.cohomologyMap(total,z(k))));
                assertEquals(RelativeSimplicialCochain.cohomologyMap(outer,z(k+1)).compose(target.connectingCohomology(z(k))),t.connectingCohomology(z(k)).compose(RelativeSimplicialCochain.cohomologyMap(inner,z(k))));
            }
        }
    }
    @Test public void invalidInclusionsContextsCyclesCocyclesAndNegativeDegreesFailExplicitly() {
        RelativeSimplicialTriple t=interval(); RelativeSimplicialTriple disk=triple(complex(new int[]{0,1,2}),complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}),complex(new int[]{0}));
        for(Runnable action : Arrays.<Runnable>asList(() -> new RelativeSimplicialTriple(t.outerPair(),complex(new int[]{0,1})),
                () -> t.connectCycle(chain(t.totalPair(),1,1)),() -> t.connectCocycle(cochain(t.outerPair(),1,1)),
                () -> disk.connectCocycle(cochain(disk.innerPair(),0,1,0)),
                () -> t.inclusionMatrix(z(-1)),() -> t.quotientMatrix(z(-1)),() -> t.liftMatrix(z(-1)),() -> t.connectingChainMatrix(z(-1)),
                () -> t.inclusionHomology(z(-1)),() -> t.quotientHomology(z(-1)),() -> t.connectingHomology(z(-1)),() -> t.longExactSegment(z(-1)),
                () -> t.extensionMatrix(z(-1)),() -> t.restrictionMatrix(z(-1)),() -> t.connectingCochainMatrix(z(-1)),
                () -> t.extensionCohomology(z(-1)),() -> t.restrictionCohomology(z(-1)),() -> t.connectingCohomology(z(-1)),() -> t.longExactCohomologySegment(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        RelativeSimplicialTriple absolute=triple(complex(new int[]{0,1}),complex(),complex()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> absolute.connectCycle(chain(absolute.outerPair(),1,1)));
    }
    @Test public void emptyHugeAndNegativeTypedDegreesPreserveZeroGroupsAndPresentations() {
        RelativeSimplicialTriple t=interval(); BigInteger huge=BigInteger.TEN.pow(100); exactSegments(triple(complex(),complex(),complex()),0);
        assertEquals(IntegerMatrix.zero(0,0),t.connectingChainMatrix(huge)); assertTrue(t.connectingHomology(huge).isZero()); assertTrue(t.connectingCohomology(huge).isZero());
        assertTrue(t.longExactSegment(huge).stream().allMatch(AbelianGroupHomomorphism::isZero)); assertTrue(t.longExactCohomologySegment(huge).stream().allMatch(AbelianGroupHomomorphism::isZero));
        RelativeSimplicialChain c=chain(t.outerPair(),0),connected=t.connectCycle(c); assertEquals(z(-1),connected.degree()); assertEquals(t.innerPair(),connected.pair()); assertEquals(connected.classOf().group(),t.connectingHomology(z(0)).target());
        assertTrue(t.connectCycle(RelativeSimplicialChain.zero(t.outerPair(),huge.negate())).isZero()); assertTrue(t.connectCocycle(RelativeSimplicialCochain.zero(t.innerPair(),huge)).isZero());
        assertThrows(UnsupportedOperationException.class,() -> t.longExactSegment(z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> t.longExactCohomologySegment(z(0)).clear());
        assertEquals(t,interval()); assertEquals(t.hashCode(),interval().hashCode()); assertNotEquals(t,new RelativeSimplicialTriple(t.outerPair(),complex()));
    }
    @Test public void quotientFilteringPrecedesBasisLimitsAndCompoundCalculationsShareBudgets() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i));
        RelativeSimplicialTriple small=triple(new FiniteSimplicialComplex(points),new FiniteSimplicialComplex(points.subList(0,299)),new FiniteSimplicialComplex(points.subList(0,298)));
        assertEquals(AbelianGroupType.Z,small.innerPair().homologyType(z(0))); assertEquals(AbelianGroupType.free(z(2)),small.totalPair().homologyType(z(0))); exactSegments(small,0);
        RelativeSimplicialTriple large=new RelativeSimplicialTriple(small.outerPair(),complex()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.inclusionMatrix(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> large.longExactSegment(z(0)));
        FiniteSimplicialComplex discrete=new FiniteSimplicialComplex(points.subList(0,100)); RelativeSimplicialTriple costly=triple(discrete,complex(),complex());
        assertEquals(AbelianGroupType.free(z(100)),costly.totalPair().homologyType(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.quotientHomology(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.extensionCohomology(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.longExactSegment(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> costly.longExactCohomologySegment(z(0)));
    }
    @Test public void exactSegmentsShareOneBudgetEvenWhenAllSixScalarMapsFit() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<95;i++) points.add(FiniteSet.of(i));
        RelativeSimplicialTriple t=triple(new FiniteSimplicialComplex(points),new FiniteSimplicialComplex(points.subList(0,47)),complex());
        assertNotNull(t.inclusionHomology(z(0))); assertNotNull(t.quotientHomology(z(0))); assertNotNull(t.connectingHomology(z(0)));
        assertNotNull(t.extensionCohomology(z(0))); assertNotNull(t.restrictionCohomology(z(0))); assertNotNull(t.connectingCohomology(z(0)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> t.longExactSegment(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> t.longExactCohomologySegment(z(0)));
    }
    @Test public void nativeSecondOperandWrappersAndSerializedFlatFlowsUseTheExistingArchitecture() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RelativeSimplicialTriple t=interval(); RelativeSimplicialChain c=chain(t.outerPair(),1,3); RelativeSimplicialCochain phi=cochain(t.innerPair(),0,2);
        IAlgebraItem<RelativeSimplicialTriple> item=math.relativeComplexes.algebra().buildAlgebraItem(t.outerPair()).performUnsafeOperation("RelativeTriple.from-pair",t.innerPair().subcomplex()); assertSame(math.relativeTriples.algebra(),item.getAlgebra()); assertEquals(t,item.perform().getResult());
        IAlgebraItem<RelativeSimplicialChain> cycle=item.performLeftProjectionOperation("connect-cycle",c); assertSame(math.relativeChains.algebra(),cycle.getAlgebra()); assertEquals(v(3),cycle.perform().getResult().coordinates());
        IAlgebraItem<RelativeSimplicialCochain> cocycle=item.performLeftProjectionOperation("connect-cocycle",phi); assertSame(math.relativeCochains.algebra(),cocycle.getAlgebra()); assertEquals(v(2),cocycle.perform().getResult().coordinates());
        for(IAlgebraItem<AbelianGroupHomomorphism> map : item.<AbelianGroupHomomorphism,BigInteger>performUnsafeFlatOperation("long-exact-segment",z(1))) assertSame(math.abelianHomomorphisms.algebra(),map.getAlgebra());
        IAlgebraFlow<IntegerVector> flow=math.flow(math.relativeTriples,Collections.singletonList(t)).performLeftProjectionOperation("connect-cycle",c).<IntegerVector>performAlgebraTransfer("coordinates");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("[3]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("true","false","true"),math.flow(math.relativeTriples,Collections.singletonList(t)).<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("long-exact-cohomology-segment",z(0)).<Boolean>performAlgebraTransfer("is-isomorphism").collect());
    }
}
