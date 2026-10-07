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

public class NativeIntegralConeHomotopyTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static RelativeSimplicialComplex pair(int[]... facets) {
        List<FiniteSet<Integer>> f=new ArrayList<>(); for(int[] facet : facets) { List<Integer> v=new ArrayList<>(); for(int x : facet) v.add(x); f.add(new FiniteSet<>(v)); }
        return RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(f));
    }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return pair(f); }
    private static IntegerMatrix m(long[]... rows) { BigInteger[][] r=new BigInteger[rows.length][]; for(int i=0;i<rows.length;i++) { r[i]=new BigInteger[rows[i].length]; for(int j=0;j<rows[i].length;j++) r[i][j]=z(rows[i][j]); } return new IntegerMatrix(r); }
    private static IntegralChainMappingCone cone(SimplicialChainMap f) { return new IntegralChainMappingCone(f); }
    private static IntegralChainMappingCone pointCone(int k) { return cone(SimplicialChainMap.identity(points(1)).scale(z(k))); }
    private static IntegralChainConeMap pointMap(int k,int scalar) { return IntegralChainConeMap.identity(pointCone(k)).scale(z(scalar)); }
    private static IntegralConeHomotopy pointWitness(int k,int start,int h) {
        return new IntegralConeHomotopy(pointMap(k,start),pointMap(k,start+k*h),Arrays.asList(m(new long[]{h}),IntegerMatrix.zero(0,1)));
    }
    private static IntegralChainConeMap strict(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b) {
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),a,b,SimplicialChainHomotopy.stationary(b.compose(f))));
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static void equations(IntegralConeHomotopy h) {
        int dimension=Math.max(h.source().dimension(),h.target().dimension());
        for(int n=0;n<=dimension;n++) {
            IntegerMatrix lhs=h.target().boundaryMatrix(z(n+1)).multiply(h.chainMatrix(z(n)));
            if(n>0) lhs=lhs.add(h.chainMatrix(z(n-1)).multiply(h.source().boundaryMatrix(z(n))));
            assertEquals(h.to().chainMatrix(z(n)).add(h.from().chainMatrix(z(n)).scale(z(-1))),lhs);
        }
    }
    @Test public void all125PointWitnessesCheckIntegerDivisibilityAndRejectOffByOneEndpoints() {
        for(int k=-2;k<=2;k++) for(int start=-2;start<=2;start++) for(int h=-2;h<=2;h++) {
            IntegralConeHomotopy value=pointWitness(k,start,h); assertEquals(m(new long[]{h}),value.chainMatrix(z(0))); equations(value);
            assertEquals(pointMap(k,start+k*h),value.to());
            final int kk=k,ss=start,hh=h;
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeHomotopy(pointMap(kk,ss),pointMap(kk,ss+kk*hh+1),Arrays.asList(m(new long[]{hh}),IntegerMatrix.zero(0,1))));
            for(int n=0;n<=1;n++) { List<AbelianGroupHomomorphism> maps=value.homologyMaps(z(n)); assertEquals(maps.get(0),maps.get(1)); maps=value.cohomologyMaps(z(n)); assertEquals(maps.get(0),maps.get(1)); }
        }
    }
    @Test public void intervalIdentityContractsWithIndependentLowerLeftBlockMatrices() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(pair(new int[]{0,1}))); IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(c);
        assertTrue(h.from().isZero()); assertTrue(h.to().isIdentity());
        assertEquals(Arrays.asList(m(new long[]{0,0},new long[]{1,0},new long[]{0,1}),m(new long[]{1,0,0}),IntegerMatrix.zero(0,1)),h.chainMatrices());
        equations(h);
        for(int n=0;n<=2;n++) assertEquals(IntegerMatrix.zero(c.chainRank(z(n+2)).intValueExact(),c.chainRank(z(n)).intValueExact()),h.chainMatrix(z(n+1)).multiply(h.chainMatrix(z(n))));
    }
    @Test public void all625PointMatricesContractExactlyWhenTheirIntegerDeterminantIsAUnit() {
        RelativeSimplicialComplex p=points(2);
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            SimplicialChainMap f=new SimplicialChainMap(p,p,Collections.singletonList(m(new long[]{a,b},new long[]{c,d}))); IntegralChainMappingCone cone=cone(f); int det=a*d-b*c;
            if(Math.abs(det)==1) { IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(cone); assertEquals(m(new long[]{d/det,-b/det},new long[]{-c/det,a/det}),h.chainMatrix(z(0))); equations(h); }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeHomotopy.contractIsomorphism(cone));
        }
    }
    @Test public void contractionRequiresStrictIsomorphismEvenForOtherAcyclicCones() {
        RelativeSimplicialComplex p=pair(new int[]{0,1}); SimplicialChainMap constant=new SimplicialChainMap(p,p,Arrays.asList(m(new long[]{1,1},new long[]{0,0}),m(new long[]{0})));
        IntegralChainMappingCone c=cone(constant); assertTrue(c.isAcyclic()); assertFalse(constant.isIsomorphism());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeHomotopy.contractIsomorphism(c));
    }
    @Test public void chosenLoopsRemainDistinctAndChronologicalCompositionRetainsEndpoints() {
        IntegralConeHomotopy loop=pointWitness(0,1,3),stationary=IntegralConeHomotopy.stationary(loop.from());
        assertNotEquals(loop,stationary); assertEquals(pointWitness(0,1,6),loop.then(loop)); assertEquals(stationary,loop.then(loop.reverse())); assertEquals(loop,loop.reverse().reverse());
        IntegralConeHomotopy a=pointWitness(2,1,1),b=pointWitness(2,3,2),c=pointWitness(2,7,-3);
        assertEquals(pointWitness(2,1,3),a.then(b)); assertEquals(a.then(b).then(c),a.then(b.then(c))); assertEquals(a,IntegralConeHomotopy.stationary(a.from()).then(a));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.then(a));
    }
    @Test public void additionAnd1025BitScalingActOnBothMapsAndActualWitnesses() {
        IntegralConeHomotopy a=pointWitness(2,1,3),b=pointWitness(2,-2,1);
        assertEquals(pointWitness(2,-1,4),a.add(b)); assertEquals(a.add(b),b.add(a));
        BigInteger huge=BigInteger.ONE.shiftLeft(1024).add(z(1)); IntegralConeHomotopy scaled=a.scale(huge);
        assertEquals(huge.multiply(z(3)),scaled.chainMatrix(z(0)).get(0,0)); assertEquals(huge.multiply(z(7)),scaled.to().chainMatrix(z(1)).get(0,0));
        assertEquals(a.scale(huge).add(b.scale(huge)),a.add(b).scale(huge)); equations(scaled);
        assertEquals(IntegralConeHomotopy.stationary(a.from().zeroLike()),a.scale(z(0))); assertNotEquals(a.reverse(),a.scale(z(-1)));
    }
    @Test public void all81MatrixWitnessesDistinguishPrecompositionFromPostcomposition() {
        RelativeSimplicialComplex p=points(2); SimplicialChainMap f=SimplicialChainMap.identity(p),b=new SimplicialChainMap(p,p,Collections.singletonList(m(new long[]{1,2},new long[]{0,1})));
        IntegralChainConeMap before=strict(f,f,b,b),zero=IntegralChainConeMap.zero(cone(f),cone(f));
        for(int a=-1;a<=1;a++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) for(int e=-1;e<=1;e++) {
            SimplicialChainMap aMap=new SimplicialChainMap(p,p,Collections.singletonList(m(new long[]{a,c},new long[]{d,e})));
            IntegralConeHomotopy h=new IntegralConeHomotopy(zero,strict(f,f,aMap,aMap),Arrays.asList(aMap.chainMatrix(z(0)),IntegerMatrix.zero(0,2)));
            assertEquals(m(new long[]{a,2*a+c},new long[]{d,2*d+e}),h.precompose(before).chainMatrix(z(0)));
            assertEquals(m(new long[]{a+2*d,c+2*e},new long[]{d,e}),h.postcompose(before).chainMatrix(z(0)));
            equations(h.precompose(before)); equations(h.postcompose(before));
        }
    }
    @Test public void compositionTransportsWitnessesBetweenDifferentConeContexts() {
        SimplicialChainMap f=SimplicialChainMap.identity(points(1)),g=SimplicialChainMap.identity(pair(new int[]{9})),a=new SimplicialChainMap(f.source(),g.source(),Collections.singletonList(m(new long[]{-1})));
        IntegralChainConeMap map=strict(f,g,a,a); IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(cone(f));
        IntegralConeHomotopy transported=h.postcompose(map).precompose(map.inverseSquare());
        assertEquals(IntegralConeHomotopy.contractIsomorphism(cone(g)),transported); equations(transported);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.precompose(map)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.postcompose(map.inverseSquare()));
    }
    @Test public void all27IntervalChainAndCochainActionsSatisfyHomotopyAndPairingIdentities() {
        IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(cone(SimplicialChainMap.identity(pair(new int[]{0,1}))));
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) {
            IntegralConeChain chain=new IntegralConeChain(h.source(),z(1),new IntegerVector(z(a),z(b),z(c)));
            assertEquals(chain,h.onChain(chain).boundary().add(h.onChain(chain.boundary())));
            IntegralConeCochain cochain=new IntegralConeCochain(h.target(),z(1),new IntegerVector(z(c),z(a),z(b)));
            assertEquals(cochain,h.onCochain(cochain).coboundary().add(h.onCochain(cochain.coboundary())));
            IntegralConeCochain upper=new IntegralConeCochain(h.target(),z(2),new IntegerVector(z(a+b-c)));
            assertEquals(upper.evaluate(h.onChain(chain)),h.onCochain(upper).evaluate(chain));
        }
    }
    @Test public void typedCycleFillingsRetainSignsContextsAndDegreeZeroRestrictions() {
        IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(pointCone(-1));
        IntegralConeChain cycle=new IntegralConeChain(h.source(),z(0),new IntegerVector(z(7)));
        assertEquals(new IntegerVector(z(-7)),h.onChain(cycle).coordinates()); assertEquals(cycle,h.onChain(cycle).boundary());
        IntegralConeCochain cocycle=new IntegralConeCochain(h.target(),z(1),new IntegerVector(z(5)));
        assertEquals(cocycle,h.onCochain(cocycle).coboundary());
        assertEquals(IntegralConeChain.zero(h.target(),z(0)),h.onChain(IntegralConeChain.zero(h.source(),z(-1))));
        assertEquals(IntegralConeChain.zero(h.target(),z(-4)),h.onChain(IntegralConeChain.zero(h.source(),z(-5))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onCochain(IntegralConeCochain.zero(h.target(),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onChain(IntegralConeChain.zero(pointCone(1),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.onCochain(IntegralConeCochain.zero(pointCone(1),z(1))));
    }
    @Test public void witnessValidationChecksFullContextsShapesTerminalDegreesAndDefensiveCopies() {
        IntegralConeHomotopy h=pointWitness(2,1,3);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeHomotopy(h.from(),h.to(),Collections.singletonList(m(new long[]{3}))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeHomotopy(h.from(),h.to(),Arrays.asList(IntegerMatrix.zero(0,1),IntegerMatrix.zero(0,1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeHomotopy(h.from(),h.to(),Arrays.asList(m(new long[]{3}),m(new long[]{0}))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeHomotopy(h.from(),pointMap(-2,7),h.chainMatrices()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> h.add(pointWitness(-2,1,3)));
        List<IntegerMatrix> supplied=new ArrayList<>(h.chainMatrices()); IntegralConeHomotopy.Data data=new IntegralConeHomotopy.Data(h.from(),h.to(),supplied); supplied.clear();
        assertEquals(h,new IntegralConeHomotopy(data)); assertEquals(h.hashCode(),new IntegralConeHomotopy(data).hashCode()); assertEquals(h.data(),data);
        assertThrows(UnsupportedOperationException.class,data.matrices()::clear); assertThrows(UnsupportedOperationException.class,h.cochainMatrices()::clear);
        assertThrows(NullPointerException.class,() -> new IntegralConeHomotopy.Data(h.from(),h.to(),Arrays.asList(null,IntegerMatrix.zero(0,1))));
    }
    @Test public void integralEndpointPairsPreserveTorsionAndContravariantPresentations() {
        IntegralConeHomotopy h=pointWitness(6,1,1);
        List<AbelianGroupHomomorphism> hom=h.homologyMaps(z(0)),co=h.cohomologyMaps(z(1));
        assertEquals(hom.get(0),hom.get(1)); assertEquals(AbelianGroupType.cyclic(z(6)),hom.get(0).source().type());
        assertEquals(co.get(0),co.get(1)); assertEquals(AbelianGroupType.cyclic(z(6)),co.get(0).source().type());
        assertThrows(UnsupportedOperationException.class,hom::clear);
    }
    @Test public void emptyFilteredAndHighDegreesPreserveFormalRangesAndZeroShapes() {
        IntegralChainMappingCone empty=cone(SimplicialChainMap.identity(points(0))); IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(empty);
        assertEquals(Collections.emptyList(),h.chainMatrices()); assertEquals(Collections.singletonList(IntegerMatrix.zero(0,0)),h.cochainMatrices()); assertEquals(h,IntegralConeHomotopy.stationary(h.from()));
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(pair(new int[]{0,1,2}).ambient());
        IntegralConeHomotopy filtered=IntegralConeHomotopy.contractIsomorphism(cone(SimplicialChainMap.identity(diagonal)));
        assertEquals(Collections.nCopies(4,IntegerMatrix.zero(0,0)),filtered.chainMatrices()); assertEquals(5,filtered.cochainMatrices().size());
        BigInteger huge=BigInteger.ONE.shiftLeft(100); assertEquals(IntegerMatrix.zero(0,0),filtered.chainMatrix(huge)); assertEquals(IntegerMatrix.zero(0,0),filtered.cochainMatrix(huge));
        assertEquals(IntegerMatrix.zero(0,1),pointWitness(2,1,3).cochainMatrix(z(0)));
        for(Runnable f : Arrays.<Runnable>asList(() -> h.chainMatrix(z(-1)),() -> h.cochainMatrix(z(-1)),() -> h.homologyMaps(z(-1)),() -> h.cohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,f);
    }
    @Test public void nativeConversionsAndTypedActionsUseActualWrappersInSerializedFlatFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralChainMappingCone cone=pointCone(-1); IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(cone);
        IAlgebraItem<IntegralConeHomotopy> item=math.chainCones.algebra().buildAlgebraItem(cone).performAlgebraTransfer("ConeHomotopy.contract-isomorphism");
        assertSame(math.coneHomotopies.algebra(),item.getAlgebra()); assertEquals(h,item.perform().getResult()); assertSame(math.coneHomotopies.algebra(),math.mathTool.getAlgebra("ConeHomotopy"));
        IAlgebraItem<IntegralConeChain> chain=item.performLeftProjectionOperation("on-chain",new IntegralConeChain(cone,z(0),new IntegerVector(z(4))));
        assertSame(math.coneChains.algebra(),chain.getAlgebra()); assertEquals(new IntegerVector(z(-4)),chain.perform().getResult().coordinates());
        IAlgebraItem<IntegralConeCochain> cochain=item.performLeftProjectionOperation("on-cochain",new IntegralConeCochain(cone,z(1),new IntegerVector(z(3))));
        assertSame(math.coneCochains.algebra(),cochain.getAlgebra()); assertEquals(new IntegerVector(z(-3)),cochain.perform().getResult().coordinates());
        assertSame(math.coneHomotopies.algebra(),item.performCustomMemberOperation("scale",z(2)).getAlgebra());
        List<IAlgebraFlow<?>> flows=Arrays.asList(
            math.flow(math.chainCones,Collections.singletonList(cone)).<IntegralConeHomotopy>performAlgebraTransfer("ConeHomotopy.contract-isomorphism").performOneOperandOperation("reverse").performFlatAlgebraTransfer("chain-matrices"),
            math.flow(math.coneHomotopies,Collections.singletonList(h)).<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).performAlgebraTransfer("is-isomorphism"),
            math.flow(math.coneHomotopies,Collections.singletonList(h)).performLeftProjectionOperation("on-chain",new IntegralConeChain(cone,z(0),new IntegerVector(z(4)))).performOneOperandOperation("boundary").performAlgebraTransfer("coordinates"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Arrays.asList("ZMatrix(1x1)[[1]]","ZMatrix(0x1)[]"),flows.get(0).collect()); assertEquals(Arrays.asList("true","true"),flows.get(1).collect()); assertEquals(Collections.singletonList("[4]"),flows.get(2).collect());
    }
    @Test public void relativeAndChangingDimensionsKeepEveryShiftedAndTerminalShape() {
        FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient(); RelativeSimplicialComplex relative=new RelativeSimplicialComplex(edge,edge.skeleton(0));
        IntegralConeHomotopy h=IntegralConeHomotopy.contractIsomorphism(cone(SimplicialChainMap.identity(relative)));
        assertEquals(Arrays.asList(IntegerMatrix.zero(1,0),m(new long[]{1}),IntegerMatrix.zero(0,1)),h.chainMatrices()); equations(h);
        IntegralChainConeMap zero=IntegralChainConeMap.zero(pointCone(2),cone(SimplicialChainMap.identity(pair(new int[]{0,1}))));
        IntegralConeHomotopy stationary=IntegralConeHomotopy.stationary(zero);
        assertEquals(Arrays.asList(IntegerMatrix.zero(3,1),IntegerMatrix.zero(1,1),IntegerMatrix.zero(0,0)),stationary.chainMatrices()); equations(stationary);
    }
    @Test public void contractionSharesOneBudgetAcrossInverseEndpointsAndValidation() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(points(90)));
        assertEquals(c.map(),c.map().inverse()); assertTrue(IntegralChainConeMap.zero(c,c).isZero()); assertTrue(IntegralChainConeMap.identity(c).isIdentity());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeHomotopy.contractIsomorphism(c)).getMessage().contains("5000000"));
    }
    @Test public void arithmeticSharesOneBudgetAcrossBothSuccessfulEndpointOperations() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(95)))); IntegralConeHomotopy h=IntegralConeHomotopy.stationary(map);
        IntegralChainConeMap doubled=map.add(map); assertEquals(doubled,map.scale(z(2)));
        assertEquals(doubled,new IntegralConeHomotopy(doubled,doubled,h.chainMatrices()).from());
        for(Runnable f : Arrays.<Runnable>asList(() -> h.add(h),() -> h.scale(z(2)))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("5000000"));
    }
    @Test public void preAndPostcompositionShareOneBudgetAcrossEndpointsTransportAndValidation() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(65)))); IntegralConeHomotopy h=IntegralConeHomotopy.stationary(map);
        assertEquals(map,map.compose(map)); assertEquals(h,new IntegralConeHomotopy(map,map,h.chainMatrices()));
        for(Runnable f : Arrays.<Runnable>asList(() -> h.precompose(map),() -> h.postcompose(map))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("5000000"));
    }
    @Test public void pairedIntegralMapsShareOneBudgetAcrossIndividuallySuccessfulEndpoints() {
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(60)))); IntegralConeHomotopy h=IntegralConeHomotopy.stationary(map);
        assertTrue(map.homologyMap(z(0)).isIsomorphism()); assertTrue(map.cohomologyMap(z(1)).isIsomorphism());
        for(Runnable f : Arrays.<Runnable>asList(() -> h.homologyMaps(z(0)),() -> h.cohomologyMaps(z(1)))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("5000000"));
    }
    @Test public void suppliedDataDoesNotPromiseTheCombinedConeRanksFitEvaluationLimits() {
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int i=0;i<24 && facets.size()<256;i++) for(int j=i+1;j<24 && facets.size()<256;j++) facets.add(FiniteSet.of(i,j));
        FiniteSimplicialComplex graph=new FiniteSimplicialComplex(facets); RelativeSimplicialComplex relative=new RelativeSimplicialComplex(graph,graph.skeleton(0));
        IntegralChainConeMap map=IntegralChainConeMap.identity(cone(SimplicialChainMap.zero(points(1),relative)));
        IntegralConeHomotopy.Data data=new IntegralConeHomotopy.Data(map,map,Arrays.asList(IntegerMatrix.zero(0,0),IntegerMatrix.zero(0,0)));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new IntegralConeHomotopy(data)).getMessage().contains("256 total"));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeHomotopy.stationary(map)).getMessage().contains("256 total"));
    }

}
