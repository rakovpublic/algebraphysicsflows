package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.topology.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSimplicialHomotopyTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialMap map(FiniteSimplicialComplex source,FiniteSimplicialComplex target,int... images) {
        Map<BigInteger,BigInteger> values=new TreeMap<>(); int i=0; for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source).members()) values.put(vertex,z(images[i++]));
        return new FiniteSimplicialMap(source,target,values);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void identities(SimplicialHomotopy h,int degree) {
        BigInteger k=z(degree); IntegerMatrix p=h.chainMatrix(k);
        IntegerMatrix sides=degree==0?IntegerMatrix.zero(h.target().simplexCount(z(0)),0):h.chainMatrix(k.subtract(BigInteger.ONE));
        IntegerMatrix difference=h.to().chainMatrix(k).add(h.from().chainMatrix(k).scale(z(-1)));
        assertEquals(difference,h.target().boundaryMatrix(k.add(BigInteger.ONE)).multiply(p).add(sides.multiply(h.source().boundaryMatrix(k))));
        IntegerMatrix lower=degree==0?IntegerMatrix.zero(h.source().simplexCount(k),0):h.source().boundaryMatrix(k).transpose();
        assertEquals(difference.transpose(),lower.multiply(h.cochainMatrix(k)).add(h.cochainMatrix(k.add(BigInteger.ONE)).multiply(h.target().boundaryMatrix(k.add(BigInteger.ONE)).transpose())));
    }
    // Laplace expansion of the vertex-incidence determinant, independent of inversion counting.
    private static int determinant(int[][] a) {
        if(a.length==0) return 1; int result=0;
        for(int c=0;c<a.length;c++) if(a[0][c]!=0) {
            int[][] minor=new int[a.length-1][a.length-1];
            for(int r=1;r<a.length;r++) for(int j=0,t=0;j<a.length;j++) if(j!=c) minor[r-1][t++]=a[r][j];
            result+=(c%2==0?1:-1)*a[0][c]*determinant(minor);
        }
        return result;
    }
    private static BigInteger oracle(FiniteSimplicialMap f,FiniteSimplicialMap g,FiniteSet<BigInteger> source,FiniteSet<BigInteger> target) {
        List<BigInteger> s=new ArrayList<>(source.members()),t=new ArrayList<>(target.members()); Collections.sort(s); Collections.sort(t); int result=0;
        for(int cut=0;cut<s.size();cut++) {
            List<BigInteger> prism=new ArrayList<>(); for(int i=0;i<=cut;i++) prism.add(f.mapVertex(s.get(i))); for(int i=cut;i<s.size();i++) prism.add(g.mapVertex(s.get(i)));
            int[][] incidence=new int[t.size()][t.size()]; for(int r=0;r<t.size();r++) for(int c=0;c<t.size();c++) incidence[r][c]=t.get(r).equals(prism.get(c))?1:0;
            result+=(cut%2==0?1:-1)*determinant(incidence);
        }
        return z(result);
    }
    @Test public void all729TriangleMapPairsMatchIndependentPrismDeterminantsAndBothIdentities() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); List<FiniteSimplicialMap> maps=new ArrayList<>();
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) maps.add(map(triangle,triangle,a,b,c));
        for(FiniteSimplicialMap f : maps) for(FiniteSimplicialMap g : maps) {
            SimplicialHomotopy h=SimplicialHomotopy.absolute(f,g);
            for(int k=0;k<=3;k++) {
                identities(h,k); IntegerMatrix p=h.chainMatrix(z(k)); List<FiniteSet<BigInteger>> rows=h.target().simplexBasis(z(k+1)),columns=h.source().simplexBasis(z(k));
                for(int r=0;r<rows.size();r++) for(int c=0;c<columns.size();c++) assertEquals(oracle(f,g,columns.get(c),rows.get(r)),p.get(r,c));
            }
        }
    }
    @Test public void all324RelativeTriangleMapPairsDescendFromTheAbsolutePrism() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); RelativeSimplicialComplex source=new RelativeSimplicialComplex(triangle,complex(new int[]{0})),target=new RelativeSimplicialComplex(triangle,complex(new int[]{0,1}));
        List<RelativeSimplicialMap> maps=new ArrayList<>();
        for(int a=0;a<2;a++) for(int b=0;b<3;b++) for(int c=0;c<3;c++) maps.add(new RelativeSimplicialMap(source,target,map(triangle,triangle,a,b,c)));
        for(RelativeSimplicialMap f : maps) for(RelativeSimplicialMap g : maps) {
            SimplicialHomotopy h=new SimplicialHomotopy(f,g),absolute=SimplicialHomotopy.absolute(f.ambientMap(),g.ambientMap());
            for(int k=0;k<=2;k++) {
                identities(h,k);
                assertEquals(h.chainMatrix(z(k)),target.projectionMatrix(z(k+1)).multiply(absolute.chainMatrix(z(k))).multiply(source.projectionMatrix(z(k)).transpose()));
            }
        }
    }
    @Test public void pointToIntervalWitnessesFillCycleAndCocycleDifferencesWithTheCorrectSign() {
        FiniteSimplicialComplex point=complex(new int[]{0}),edge=complex(new int[]{0,1}); SimplicialHomotopy h=SimplicialHomotopy.absolute(map(point,edge,0),map(point,edge,1));
        assertEquals(new IntegerMatrix(new BigInteger[][]{{z(1)}}),h.chainMatrix(z(0)));
        SimplicialChain cycle=new SimplicialChain(point,z(0),v(7)),filling=h.onAbsoluteChain(cycle);
        assertEquals(v(7),filling.coordinates()); assertEquals(v(-7,7),filling.boundary().coordinates());
        assertEquals(cycle.pushforward(h.to().ambientMap()).subtract(cycle.pushforward(h.from().ambientMap())),filling.boundary());
        SimplicialCochain cochain=new SimplicialCochain(edge,z(1),v(5)); assertTrue(cochain.isCocycle()); assertEquals(v(5),h.onAbsoluteCochain(cochain).coordinates());
        assertEquals(h.to().homologyMap(z(0)),h.from().homologyMap(z(0)));
    }
    @Test public void noncyclesRequireBothTermsAndCochainActionsAreAdjointToPrisms() {
        FiniteSimplicialComplex triangle=complex(new int[]{0,1,2}); SimplicialHomotopy h=SimplicialHomotopy.absolute(FiniteSimplicialMap.identity(triangle),map(triangle,triangle,0,0,0));
        SimplicialChain chain=new SimplicialChain(triangle,z(1),v(2,3,4)); assertFalse(chain.isCycle());
        assertEquals(chain.pushforward(h.to().ambientMap()).subtract(chain.pushforward(h.from().ambientMap())),h.onAbsoluteChain(chain).boundary().add(h.onAbsoluteChain(chain.boundary())));
        SimplicialCochain cochain=new SimplicialCochain(triangle,z(2),v(7));
        assertEquals(h.onAbsoluteChain(chain).coordinates().dot(cochain.coordinates()),chain.coordinates().dot(h.onAbsoluteCochain(cochain).coordinates()));
        SimplicialCochain one=new SimplicialCochain(triangle,z(1),v(2,4,7)); assertFalse(one.isCocycle());
        assertEquals(one.pullback(h.to().ambientMap()).subtract(one.pullback(h.from().ambientMap())),h.onAbsoluteCochain(one).coboundary().add(h.onAbsoluteCochain(one.coboundary())));
    }
    @Test public void relativeCyclesAndCocyclesHaveTypedFillingsOnDifferentPairs() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}),triangle=complex(new int[]{0,1,2});
        RelativeSimplicialComplex source=new RelativeSimplicialComplex(edge,complex(new int[]{0})),target=new RelativeSimplicialComplex(triangle,edge);
        RelativeSimplicialMap f=new RelativeSimplicialMap(source,target,map(edge,triangle,0,2)),g=new RelativeSimplicialMap(source,target,map(edge,triangle,1,1)); SimplicialHomotopy h=new SimplicialHomotopy(f,g);
        RelativeSimplicialChain cycle=new RelativeSimplicialChain(source,z(0),v(3)); assertTrue(cycle.isCycle());
        assertEquals(cycle.pushforward(g).subtract(cycle.pushforward(f)),h.onChain(cycle).boundary()); assertEquals(v(0,-3),h.onChain(cycle).coordinates());
        RelativeSimplicialCochain cocycle=new RelativeSimplicialCochain(target,z(1),v(5,5)); assertTrue(cocycle.isCocycle());
        assertEquals(cocycle.pullback(g).subtract(cocycle.pullback(f)),h.onCochain(cocycle).coboundary());
        assertEquals(source,h.onCochain(cocycle).pair()); assertEquals(target,h.onChain(cycle).pair());
    }
    @Test public void simplexContractionsThroughDimensionSixAndProjectivePlaneConeFillIntegralCycles() {
        for(int dimension=1;dimension<=6;dimension++) {
            int[] labels=new int[dimension+1]; for(int i=0;i<labels.length;i++) labels[i]=i;
            FiniteSimplicialComplex simplex=complex(labels); SimplicialHomotopy h=SimplicialHomotopy.absolute(FiniteSimplicialMap.identity(simplex),map(simplex,simplex,new int[labels.length]));
            for(int k=0;k<=dimension;k++) identities(h,k);
        }
        int[][] facets={{0,1,2},{0,1,3},{0,2,4},{0,3,5},{0,4,5},{1,2,5},{1,3,4},{1,4,5},{2,3,4},{2,3,5}};
        FiniteSimplicialComplex rp2=complex(facets); int[][] cones=new int[facets.length][4];
        for(int i=0;i<facets.length;i++) { System.arraycopy(facets[i],0,cones[i],0,3); cones[i][3]=6; }
        FiniteSimplicialComplex cone=complex(cones); SimplicialHomotopy h=SimplicialHomotopy.absolute(FiniteSimplicialMap.inclusion(rp2,cone),map(rp2,cone,6,6,6,6,6,6));
        assertEquals(Collections.singletonList(z(2)),IntegralHomology.atDegree(rp2,z(1)).group().type().invariantFactors());
        for(SimplicialChain cycle : SimplicialChain.zero(rp2,z(1)).cycleGenerators()) assertEquals(cycle.pushforward(h.to().ambientMap()).subtract(cycle.pushforward(h.from().ambientMap())),h.onAbsoluteChain(cycle).boundary());
    }
    @Test public void contiguousCircleConstantsInduceEqualIntegralMapsButHomotopicRotationsNeedNotBeContiguous() {
        FiniteSimplicialComplex circle=complex(new int[]{0,1},new int[]{0,2},new int[]{1,2}); SimplicialHomotopy h=SimplicialHomotopy.absolute(map(circle,circle,0,0,0),map(circle,circle,1,1,1));
        for(int k=0;k<=2;k++) { identities(h,k); assertEquals(h.from().homologyMap(z(k)),h.to().homologyMap(z(k))); assertEquals(RelativeSimplicialCochain.cohomologyMap(h.from(),z(k)),RelativeSimplicialCochain.cohomologyMap(h.to(),z(k))); }
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialHomotopy.absolute(FiniteSimplicialMap.identity(circle),map(circle,circle,1,2,0)));
    }
    @Test public void wrongContextsAndSubcomplexContiguityAreRejectedBeforeQuotientActions() {
        FiniteSimplicialComplex edge=complex(new int[]{0,1}),point=complex(new int[]{0}),discrete=complex(new int[]{0},new int[]{1});
        RelativeSimplicialComplex source=RelativeSimplicialComplex.diagonal(point),target=new RelativeSimplicialComplex(edge,discrete);
        RelativeSimplicialMap f=new RelativeSimplicialMap(source,target,map(point,edge,0)),g=new RelativeSimplicialMap(source,target,map(point,edge,1)); assertTrue(f.ambientMap().contiguous(g.ambientMap()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new SimplicialHomotopy(f,g));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> SimplicialHomotopy.absolute(FiniteSimplicialMap.identity(point),map(point,edge,0)));
        SimplicialHomotopy h=SimplicialHomotopy.absolute(map(point,edge,0),map(point,edge,1));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onChain(RelativeSimplicialChain.zero(source,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onCochain(RelativeSimplicialCochain.zero(RelativeSimplicialComplex.absolute(point),z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onAbsoluteCochain(SimplicialCochain.zero(edge,z(0))));
        SimplicialHomotopy relative=new SimplicialHomotopy(f,f);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> relative.onAbsoluteChain(SimplicialChain.zero(point,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> relative.onAbsoluteCochain(SimplicialCochain.zero(edge,z(1))));
    }
    @Test public void emptyNegativeAndHugeDegreesRetainUnreducedZeroShapes() {
        FiniteSimplicialComplex empty=complex(),point=complex(new int[]{0}); SimplicialHomotopy h=SimplicialHomotopy.absolute(FiniteSimplicialMap.identity(point),FiniteSimplicialMap.identity(point));
        assertEquals(IntegerMatrix.zero(0,1),h.cochainMatrix(z(0))); assertEquals(IntegerMatrix.zero(1,0),h.cochainMatrix(z(1)));
        assertEquals(SimplicialChain.zero(point,z(0)),h.onAbsoluteChain(SimplicialChain.zero(point,z(-1))));
        assertEquals(SimplicialChain.zero(point,z(-1)),h.onAbsoluteChain(SimplicialChain.zero(point,z(-2))));
        BigInteger huge=BigInteger.TEN.pow(100); assertEquals(IntegerMatrix.zero(0,0),h.chainMatrix(huge)); assertEquals(IntegerMatrix.zero(0,0),h.cochainMatrix(huge));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.chainMatrix(z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.cochainMatrix(z(-1)));
        SimplicialHomotopy e=SimplicialHomotopy.absolute(FiniteSimplicialMap.identity(empty),FiniteSimplicialMap.identity(empty));
        assertTrue(e.chainMatrices().isEmpty()); assertEquals(Collections.singletonList(IntegerMatrix.zero(0,0)),e.cochainMatrices()); identities(e,0);
    }
    @Test public void filteredBasesWorkBeyondAmbientLimitsAndOversizedRequiredBasesFail() {
        List<FiniteSet<Integer>> points=new ArrayList<>(); for(int i=0;i<300;i++) points.add(FiniteSet.of(i)); FiniteSimplicialComplex x=new FiniteSimplicialComplex(points);
        RelativeSimplicialComplex pair=new RelativeSimplicialComplex(x,new FiniteSimplicialComplex(points.subList(0,299))); RelativeSimplicialMap identity=RelativeSimplicialMap.identity(pair); SimplicialHomotopy h=new SimplicialHomotopy(identity,identity);
        assertEquals(IntegerMatrix.zero(0,1),h.chainMatrix(z(0))); assertEquals(IntegerMatrix.zero(0,1),h.cochainMatrix(z(0))); identities(h,0);
        SimplicialHomotopy full=SimplicialHomotopy.absolute(identity.ambientMap(),identity.ambientMap()); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> full.chainMatrix(z(0))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,full::chainMatrices);
    }
    @Test public void reversingEndpointsRecomputesThePrismAndExtremeLabelsPreserveOrientation() {
        FiniteSimplicialComplex point=complex(new int[]{Integer.MIN_VALUE}),edge=complex(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE});
        SimplicialHomotopy h=SimplicialHomotopy.absolute(map(point,edge,Integer.MAX_VALUE),map(point,edge,Integer.MIN_VALUE));
        assertEquals(z(-1),h.chainMatrix(z(0)).get(0,0)); assertEquals(h,h.reverse().reverse()); assertEquals(h.hashCode(),h.reverse().reverse().hashCode());
        assertThrows(UnsupportedOperationException.class,() -> h.chainMatrices().clear()); assertThrows(UnsupportedOperationException.class,() -> h.cochainMatrices().clear());
        FiniteSimplicialComplex interval=complex(new int[]{0,1}),tetrahedron=complex(new int[]{0,1,2,3});
        SimplicialHomotopy asymmetric=SimplicialHomotopy.absolute(map(interval,tetrahedron,0,1),map(interval,tetrahedron,2,3));
        assertNotEquals(asymmetric.chainMatrix(z(1)).scale(z(-1)),asymmetric.reverse().chainMatrix(z(1))); identities(asymmetric.reverse(),1);
    }
    @Test public void originalSecondOperandWrappersFlatUnaryAndSerializedFlowsExecutePrisms() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); FiniteSimplicialComplex point=complex(new int[]{0}),edge=complex(new int[]{0,1}); FiniteSimplicialMap f=map(point,edge,0),g=map(point,edge,1);
        IAlgebraItem<SimplicialHomotopy> item=math.simplicialMaps.algebra().buildAlgebraItem(f).performCustomResultOperation("SimplicialHomotopy.between-absolute",g);
        assertSame(math.simplicialHomotopies.algebra(),item.getAlgebra());
        IAlgebraItem<SimplicialChain> result=item.performLeftProjectionOperation("on-absolute-chain",new SimplicialChain(point,z(0),v(3)));
        assertSame(math.simplicialChains.algebra(),result.getAlgebra()); assertEquals(v(3),result.perform().getResult().coordinates());
        IAlgebraItem<RelativeSimplicialChain> relative=item.performLeftProjectionOperation("on-chain",RelativeSimplicialChain.absolute(new SimplicialChain(point,z(0),v(3)))); assertSame(math.relativeChains.algebra(),relative.getAlgebra());
        IAlgebraItem<RelativeSimplicialCochain> cochain=item.performLeftProjectionOperation("on-cochain",RelativeSimplicialCochain.absolute(new SimplicialCochain(edge,z(1),v(5)))); assertSame(math.relativeCochains.algebra(),cochain.getAlgebra()); assertEquals(v(5),cochain.perform().getResult().coordinates());
        for(IAlgebraItem<IntegerMatrix> matrix : item.<IntegerMatrix>performAlgebraFlatTransfer("chain-matrices")) assertSame(math.integerMatrices.algebra(),matrix.getAlgebra());
        assertEquals(Arrays.asList("1","0"),math.flow(math.simplicialHomotopies,Collections.singletonList(item.perform().getResult()))
                .<IntegerMatrix>performFlatAlgebraTransfer("chain-matrices").<BigInteger>performAlgebraTransfer("row-count").collect());
        assertEquals(Arrays.asList("0","1","0"),math.flow(math.simplicialHomotopies,Collections.singletonList(item.perform().getResult()))
                .<IntegerMatrix>performFlatAlgebraTransfer("cochain-matrices").<BigInteger>performAlgebraTransfer("row-count").collect());
        IAlgebraFlow<IntegerVector> flow=math.flow(math.simplicialMaps,Collections.singletonList(f)).<SimplicialHomotopy>performCustomResultOperation("SimplicialHomotopy.between-absolute",g)
                .performLeftProjectionOperation("on-absolute-chain",new SimplicialChain(point,z(0),v(3))).performOneOperandOperation("boundary").<IntegerVector>performAlgebraTransfer("coordinates");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("[-3, 3]"),restored.collect()); assertEquals(restored.collect(),restored.collect());
    }
}
