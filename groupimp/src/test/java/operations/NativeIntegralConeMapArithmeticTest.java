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

public class NativeIntegralConeMapArithmeticTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static RelativeSimplicialComplex pair(int[]... facets) {
        List<FiniteSet<Integer>> f=new ArrayList<>(); for(int[] facet : facets) { List<Integer> v=new ArrayList<>(); for(int x : facet) v.add(x); f.add(new FiniteSet<>(v)); }
        return RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(f));
    }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return pair(f); }
    private static RelativeSimplicialComplex circle() { return pair(new int[]{0,1},new int[]{0,2},new int[]{1,2}); }
    private static IntegerMatrix m(long[]... rows) { BigInteger[][] r=new BigInteger[rows.length][]; for(int i=0;i<rows.length;i++) { r[i]=new BigInteger[rows[i].length]; for(int j=0;j<rows[i].length;j++) r[i][j]=z(rows[i][j]); } return new IntegerMatrix(r); }
    private static IntegralChainMappingCone cone(SimplicialChainMap f) { return new IntegralChainMappingCone(f); }
    private static IntegralChainConeMap square(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b,SimplicialChainHomotopy h) {
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),a,b,h));
    }
    private static IntegralChainConeMap strict(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b) { return square(f,g,a,b,SimplicialChainHomotopy.stationary(b.compose(f))); }
    private static IntegralChainConeMap shear(int a,int b,int loop) {
        SimplicialChainMap zero=SimplicialChainMap.zero(points(1),circle());
        SimplicialChainHomotopy h=new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{loop},new long[]{-loop},new long[]{loop}),IntegerMatrix.zero(0,0)));
        return square(zero,zero,SimplicialChainMap.identity(points(1)).scale(z(a)),SimplicialChainMap.identity(circle()).scale(z(b)),h);
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static void inverseLaws(IntegralChainConeMap map) {
        IntegralChainConeMap inverse=map.inverseSquare(); assertEquals(map.source(),inverse.target()); assertEquals(map.target(),inverse.source());
        assertEquals(IntegralChainConeMap.identity(map.source()),inverse.compose(map)); assertEquals(IntegralChainConeMap.identity(map.target()),map.compose(inverse));
        assertEquals(map,inverse.inverseSquare()); assertTrue(map.isSquareIsomorphism()); assertTrue(map.isChainIsomorphism());
    }
    @Test public void zeroMapsRetainArbitraryExactConesAndBothWitnessEndpoints() {
        IntegralChainMappingCone s=cone(SimplicialChainMap.identity(points(1)).scale(z(2))),t=cone(SimplicialChainMap.zero(points(2),circle()));
        IntegralChainConeMap zero=IntegralChainConeMap.zero(s,t);
        assertEquals(s,zero.source()); assertEquals(t,zero.target()); assertTrue(zero.isZero()); assertFalse(zero.isIdentity());
        assertEquals(SimplicialChainMap.zero(s.source(),t.target()),zero.homotopy().from()); assertEquals(zero.homotopy().from(),zero.homotopy().to());
        assertEquals(zero,zero.zeroLike()); assertFalse(zero.isSquareIsomorphism()); assertFalse(zero.isChainIsomorphism());
        for(int n=0;n<=2;n++) assertEquals(IntegerMatrix.zero(t.chainRank(z(n)).intValueExact(),s.chainRank(z(n)).intValueExact()),zero.chainMatrix(z(n)));
    }
    @Test public void identityAndZeroPredicatesIncludeActualNonzeroLoopWitnesses() {
        assertTrue(shear(0,0,0).isZero()); assertFalse(shear(0,0,1).isZero()); assertTrue(shear(1,1,0).isIdentity()); assertFalse(shear(1,1,1).isIdentity());
        assertFalse(shear(1,1,0).isZero()); assertFalse(shear(0,0,0).isIdentity());
        IntegralChainConeMap empty=IntegralChainConeMap.zero(cone(SimplicialChainMap.identity(points(0))),cone(SimplicialChainMap.identity(points(0))));
        assertTrue(empty.isZero()); assertTrue(empty.isIdentity()); inverseLaws(empty); assertTrue(empty.chainMatrices().isEmpty());
    }
    @Test public void all729SumsAndDifferencesMatchIndependentWitnessAndBlockCoefficients() {
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int h=-1;h<=1;h++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) for(int k=-1;k<=1;k++) {
            IntegralChainConeMap x=shear(a,b,h),y=shear(c,d,k),sum=x.add(y);
            assertEquals(shear(a+c,b+d,h+k),sum); assertEquals(shear(a-c,b-d,h-k),x.subtract(y));
            int w=h+k; assertEquals(m(new long[]{b+d,0,0,-w},new long[]{0,b+d,0,w},new long[]{0,0,b+d,-w},new long[]{0,0,0,a+c}),sum.chainMatrix(z(1)));
        }
    }
    @Test public void parallelMapsFormAbelianGroupsAndCompositionIsBilinear() {
        IntegralChainConeMap a=shear(2,-1,3),b=shear(-2,3,-1),c=shear(1,2,2),zero=a.zeroLike();
        assertEquals(a,a.add(zero)); assertEquals(zero,a.add(a.negate())); assertEquals(a.add(b),b.add(a)); assertEquals(a.add(b).add(c),a.add(b.add(c)));
        assertEquals(c.compose(a).add(c.compose(b)),c.compose(a.add(b))); assertEquals(a.compose(c).add(b.compose(c)),a.add(b).compose(c));
        assertEquals(zero,zero.compose(a)); assertEquals(zero,a.compose(zero)); assertNotEquals(a.compose(b),b.compose(a));
    }
    @Test public void scalingRetains1025BitCoefficientsAndDistributesOverWitnesses() {
        BigInteger huge=BigInteger.ONE.shiftLeft(1024).add(z(1)); IntegralChainConeMap a=shear(2,-1,3),b=shear(-2,3,-1),scaled=a.scale(huge);
        assertEquals(huge.multiply(z(2)),scaled.sourceMap().chainMatrix(z(0)).get(0,0));
        assertEquals(huge.multiply(z(-3)),scaled.chainMatrix(z(1)).get(0,3));
        assertEquals(a.scale(huge).add(b.scale(huge)),a.add(b).scale(huge)); assertEquals(a,a.scale(z(1))); assertEquals(a.zeroLike(),a.scale(z(0)));
        assertEquals(a.negate(),a.scale(z(-1))); assertEquals(a.scale(huge.multiply(z(-2))),a.scale(huge).scale(z(-2)));
    }
    @Test public void arithmeticChecksFullDefiningMapsAndLabelsRatherThanMatrixShapes() {
        IntegralChainConeMap a=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(1)))),b=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(1)).scale(z(2)))),c=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(pair(new int[]{7}))));
        for(IntegralChainConeMap other : Arrays.asList(b,c)) for(Runnable f : Arrays.<Runnable>asList(() -> a.add(other),() -> a.subtract(other))) failure(MathFailure.Kind.OPERATION_UNDEFINED,f);
        IntegralChainConeMap reverse=IntegralChainConeMap.zero(b.source(),a.source()),forward=IntegralChainConeMap.zero(a.source(),b.source());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> forward.add(reverse));
    }
    @Test public void all625PointMatricesUseIndependentDeterminantsAndAdjugateInverses() {
        RelativeSimplicialComplex p=points(2); SimplicialChainMap zero=SimplicialChainMap.zero(p,p),id=SimplicialChainMap.identity(p);
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            SimplicialChainMap v=new SimplicialChainMap(p,p,Collections.singletonList(m(new long[]{a,b},new long[]{c,d})));
            IntegralChainConeMap map=strict(zero,zero,id,v); int det=a*d-b*c; boolean invertible=Math.abs(det)==1;
            assertEquals(invertible,map.isSquareIsomorphism()); assertEquals(invertible,map.isChainIsomorphism());
            if(invertible) { assertEquals(m(new long[]{d/det,-b/det},new long[]{-c/det,a/det}),map.inverseSquare().chainMatrix(z(0))); inverseLaws(map); }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,map::inverseSquare);
        }
    }
    @Test public void shearInversesTransportNonzeroWitnessWithTheCorrectSign() {
        for(int a : new int[]{-1,1}) for(int b : new int[]{-1,1}) for(int h=-4;h<=4;h++) {
            IntegralChainConeMap map=shear(a,b,h); assertEquals(shear(a,b,-b*h*a),map.inverseSquare()); inverseLaws(map);
        }
        IntegralChainConeMap a=shear(-1,1,2),b=shear(1,-1,3);
        assertEquals(a.inverseSquare().compose(b.inverseSquare()),b.compose(a).inverseSquare());
    }
    @Test public void inverseSquareSwapsDifferentDefiningMapsAndTypedActionsRoundTrip() {
        RelativeSimplicialComplex edge=pair(new int[]{0,1}); SimplicialChainMap f=SimplicialChainMap.identity(edge),g=new SimplicialChainMap(edge,edge,Arrays.asList(m(new long[]{1,1},new long[]{0,0}),m(new long[]{0})));
        SimplicialChainHomotopy h=new SimplicialChainHomotopy(f,g,Arrays.asList(m(new long[]{0,-1}),IntegerMatrix.zero(0,1)));
        IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(h),inverse=map.inverseSquare();
        assertEquals(IntegralChainConeMap.fromHomotopy(h.reverse()),inverse); inverseLaws(map);
        IntegralConeChain chain=new IntegralConeChain(map.source(),z(1),new IntegerVector(z(2),z(3),z(-1)));
        IntegralConeCochain cochain=new IntegralConeCochain(map.target(),z(1),new IntegerVector(z(-3),z(1),z(4)));
        assertEquals(chain,inverse.onChain(map.onChain(chain))); assertEquals(cochain,inverse.onCochain(map.onCochain(cochain)));
    }
    @Test public void totalConeIsomorphismCanHaveNoninvertibleRectangularVerticalMaps() {
        RelativeSimplicialComplex empty=points(0),point=points(1); FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient();
        RelativeSimplicialComplex relativeEdge=new RelativeSimplicialComplex(edge,edge.skeleton(0));
        SimplicialChainMap f=SimplicialChainMap.zero(point,empty),g=SimplicialChainMap.zero(empty,relativeEdge),a=SimplicialChainMap.zero(point,empty),b=SimplicialChainMap.zero(empty,relativeEdge),zero=SimplicialChainMap.zero(point,relativeEdge);
        SimplicialChainHomotopy h=new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{1}),IntegerMatrix.zero(0,0)));
        IntegralChainConeMap map=square(f,g,a,b,h);
        assertEquals(Arrays.asList(IntegerMatrix.zero(0,0),m(new long[]{-1})),map.chainMatrices());
        assertTrue(map.isChainIsomorphism()); assertFalse(map.isSquareIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,map::inverseSquare);
        assertTrue(map.homologyMap(z(1)).isIsomorphism()); assertTrue(map.cohomologyMap(z(1)).isIsomorphism());
    }
    @Test public void inversesPreserveTorsionMapsAndTheContravariantOrder() {
        RelativeSimplicialComplex p=points(1); SimplicialChainMap f=SimplicialChainMap.identity(p).scale(z(6)),negative=SimplicialChainMap.identity(p).negate();
        IntegralChainConeMap map=strict(f,f,negative,negative),inverse=map.inverseSquare(),id=IntegralChainConeMap.identity(map.source());
        for(int n=0;n<=1;n++) {
            assertEquals(id.homologyMap(z(n)),inverse.homologyMap(z(n)).compose(map.homologyMap(z(n))));
            assertEquals(id.cohomologyMap(z(n)),map.cohomologyMap(z(n)).compose(inverse.cohomologyMap(z(n))));
        }
        assertEquals(Collections.singletonList(z(6)),map.homologyMap(z(0)).source().type().invariantFactors()); inverseLaws(map);
    }
    @Test public void relativeAndFilteredZeroGroupsKeepShiftedWitnessDegrees() {
        FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient(),triangle=pair(new int[]{0,1,2}).ambient();
        RelativeSimplicialComplex s=new RelativeSimplicialComplex(edge,edge.skeleton(0)),t=new RelativeSimplicialComplex(triangle,triangle.skeleton(1));
        SimplicialChainMap zero=SimplicialChainMap.zero(s,t); IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(new SimplicialChainHomotopy(zero,zero,Arrays.asList(IntegerMatrix.zero(0,0),m(new long[]{5}),IntegerMatrix.zero(0,0))));
        assertEquals(m(new long[]{1,5},new long[]{0,1}),map.inverseSquare().chainMatrix(z(2))); inverseLaws(map);
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(triangle);
        IntegralChainConeMap filtered=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(diagonal)));
        assertTrue(filtered.isZero()); assertTrue(filtered.isIdentity()); assertEquals(4,filtered.chainMatrices().size()); inverseLaws(filtered);
    }
    @Test public void totalConePredicateReportsCombinedRankLimitsInsteadOfFalseDecisions() {
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int i=0;i<24 && facets.size()<256;i++) for(int j=i+1;j<24 && facets.size()<256;j++) facets.add(FiniteSet.of(i,j));
        FiniteSimplicialComplex graph=new FiniteSimplicialComplex(facets);
        RelativeSimplicialComplex relative=new RelativeSimplicialComplex(graph,graph.skeleton(0));
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.zero(points(1),relative)));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,map::isSquareIsomorphism).getMessage().contains("5000000"));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,map::isChainIsomorphism).getMessage().contains("256 total"));
    }
    @Test public void nativeArithmeticUsesFirstCarrierWrappersAndSquareInverseFeedsFlatFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralChainConeMap map=shear(-1,1,2);
        IAlgebraItem<IntegralChainConeMap> item=math.chainConeMaps.algebra().buildAlgebraItem(map),scaled=item.performCustomMemberOperation("scale",z(3));
        assertSame(math.chainConeMaps.algebra(),scaled.getAlgebra()); assertEquals(map.scale(z(3)),scaled.perform().getResult());
        IAlgebraItem<IntegralChainConeMap> zero=math.chainCones.algebra().buildAlgebraItem(map.source()).performCustomResultOperation("ChainConeMap.zero-between",map.target());
        assertSame(math.chainConeMaps.algebra(),zero.getAlgebra()); assertTrue(zero.perform().getResult().isZero());
        assertSame(math.chainConeMaps.algebra(),math.mathTool.getAlgebra("ChainConeMap"));
        List<IAlgebraFlow<?>> flows=Arrays.asList(
            math.flow(math.chainConeMaps,Collections.singletonList(map)).performOneOperandOperation("inverse-square").performOperation("compose",map).performAlgebraTransfer("is-identity"),
            math.flow(math.chainConeMaps,Collections.singletonList(map)).performOneOperandOperation("inverse-square").performFlatAlgebraTransfer("chain-matrices"),
            math.flow(math.chainConeMaps,Collections.singletonList(map)).performOneOperandOperation("inverse-square").<AbelianGroupHomomorphism>performFlatAlgebraTransfer("homology-maps").performAlgebraTransfer("is-isomorphism"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("true"),flows.get(0).collect()); assertEquals(Arrays.asList("true","true"),flows.get(2).collect());
    }
    @Test public void subtractionSharesOneBudgetAcrossNegationAdditionAndValidation() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(110))));
        IntegralChainConeMap negative=map.negate(); assertTrue(map.add(negative).isZero());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> map.subtract(map)).getMessage().contains("5000000"));
    }
    @Test public void inversionSharesOneBudgetAcrossBothInversesWitnessTransportAndValidation() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(85))));
        assertEquals(map.sourceMap(),map.sourceMap().inverse()); assertEquals(map.targetMap(),map.targetMap().inverse());
        assertEquals(map.homotopy(),map.homotopy().precompose(map.sourceMap()).postcompose(map.targetMap()).reverse());
        assertTrue(map.isSquareIsomorphism()); assertTrue(map.isChainIsomorphism());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,map::inverseSquare).getMessage().contains("5000000"));
    }
}
