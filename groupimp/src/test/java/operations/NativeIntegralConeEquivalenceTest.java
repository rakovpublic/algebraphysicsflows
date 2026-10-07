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

public class NativeIntegralConeEquivalenceTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static RelativeSimplicialComplex pair(int[]... facets) {
        List<FiniteSet<Integer>> f=new ArrayList<>(); for(int[] facet : facets) { List<Integer> v=new ArrayList<>(); for(int x : facet) v.add(x); f.add(new FiniteSet<>(v)); }
        return RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(f));
    }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return pair(f); }
    private static IntegerMatrix m(long[]... rows) { BigInteger[][] r=new BigInteger[rows.length][]; for(int i=0;i<rows.length;i++) { r[i]=new BigInteger[rows[i].length]; for(int j=0;j<rows[i].length;j++) r[i][j]=z(rows[i][j]); } return new IntegerMatrix(r); }
    private static IntegralChainMappingCone cone(SimplicialChainMap f) { return new IntegralChainMappingCone(f); }
    private static IntegralChainMappingCone pointCone(int k) { return cone(SimplicialChainMap.identity(points(1)).scale(z(k))); }
    private static IntegralChainConeMap strict(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b) {
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),a,b,SimplicialChainHomotopy.stationary(b.compose(f))));
    }
    private static IntegralConeEquivalence loop(int a,int b,int h,int k) {
        SimplicialChainMap zero=SimplicialChainMap.zero(points(1),points(1)),id=SimplicialChainMap.identity(points(1));
        IntegralChainConeMap f=strict(zero,zero,id.scale(z(a)),id.scale(z(b))),identity=IntegralChainConeMap.identity(cone(zero));
        return new IntegralConeEquivalence(f,f,new IntegralConeHomotopy(identity,identity,Arrays.asList(m(new long[]{h}),IntegerMatrix.zero(0,1))),
                new IntegralConeHomotopy(identity,identity,Arrays.asList(m(new long[]{k}),IntegerMatrix.zero(0,1))));
    }
    private static IntegralConeEquivalence zeroEquivalence(IntegralChainMappingCone s,IntegralChainMappingCone t) {
        return new IntegralConeEquivalence(IntegralChainConeMap.zero(s,t),IntegralChainConeMap.zero(t,s),IntegralConeHomotopySolver.contract(s),IntegralConeHomotopySolver.contract(t));
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static void equations(IntegralConeEquivalence e) {
        assertEquals(e.backward().compose(e.forward()),e.sourceHomotopy().from()); assertEquals(e.forward().compose(e.backward()),e.targetHomotopy().from());
        for(IntegralConeHomotopy h : e.homotopies()) {
            assertTrue(h.to().isIdentity());
            for(int n=0;n<=h.source().dimension();n++) {
                IntegerMatrix lhs=h.target().boundaryMatrix(z(n+1)).multiply(h.chainMatrix(z(n)));
                if(n>0) lhs=lhs.add(h.chainMatrix(z(n-1)).multiply(h.source().boundaryMatrix(z(n))));
                assertEquals(IntegerMatrix.identity(h.source().chainRank(z(n)).intValueExact()).add(h.from().chainMatrix(z(n)).scale(z(-1))),lhs);
            }
        }
    }
    @Test public void all1296SignedLoopCompositionsMatchIndependentCoefficients() {
        for(int a : new int[]{-1,1}) for(int b : new int[]{-1,1}) for(int c : new int[]{-1,1}) for(int d : new int[]{-1,1})
        for(int h=-1;h<=1;h++) for(int k=-1;k<=1;k++) for(int j=-1;j<=1;j++) for(int l=-1;l<=1;l++) {
            IntegralConeEquivalence before=loop(a,b,h,k),after=loop(c,d,j,l),composite=after.compose(before);
            assertEquals(loop(a*c,b*d,a*j*b+h,c*k*d+l),composite); equations(composite);
        }
    }
    @Test public void associativityIdentityAndInvolutionPreserveChosenNonzeroLoops() {
        IntegralConeEquivalence a=loop(-1,1,2,3),b=loop(1,-1,-2,4),c=loop(-1,-1,5,-1),id=IntegralConeEquivalence.identity(a.source());
        assertEquals(c.compose(b).compose(a),c.compose(b.compose(a))); assertEquals(a,id.compose(a)); assertEquals(a,a.compose(id));
        assertEquals(a,a.inverse().inverse()); assertSame(a.sourceHomotopy(),a.inverse().targetHomotopy());
        assertNotEquals(id,a.inverse().compose(a)); assertNotEquals(a,IntegralConeEquivalence.fromSquareIsomorphism(a.forward()));
        assertEquals(a,new IntegralConeEquivalence(a.data())); assertEquals(a.hashCode(),new IntegralConeEquivalence(a.data()).hashCode());
        assertThrows(UnsupportedOperationException.class,a.maps()::clear); assertThrows(UnsupportedOperationException.class,a.homotopies()::clear);
    }
    @Test public void noncommutingMatricesTransportBothWitnessesWithCorrectOrder() {
        RelativeSimplicialComplex p=points(2); SimplicialChainMap zero=SimplicialChainMap.zero(p,p);
        IntegerMatrix x=m(new long[]{1,2},new long[]{0,1}),xi=m(new long[]{1,-2},new long[]{0,1}),y=m(new long[]{1,0},new long[]{3,1}),yi=m(new long[]{1,0},new long[]{-3,1}),h=m(new long[]{1,2},new long[]{3,4}),k=m(new long[]{-1,0},new long[]{2,3});
        SimplicialChainMap a=new SimplicialChainMap(p,p,Collections.singletonList(x)),b=new SimplicialChainMap(p,p,Collections.singletonList(y));
        IntegralChainConeMap f=strict(zero,zero,a,a),g=strict(zero,zero,b,b),id=IntegralChainConeMap.identity(cone(zero));
        IntegralConeHomotopy hs=new IntegralConeHomotopy(id,id,Arrays.asList(h,IntegerMatrix.zero(0,2))),ks=new IntegralConeHomotopy(id,id,Arrays.asList(k,IntegerMatrix.zero(0,2)));
        IntegralConeEquivalence before=new IntegralConeEquivalence(f,f.inverseSquare(),hs,ks),after=new IntegralConeEquivalence(g,g.inverseSquare(),ks,hs),e=after.compose(before);
        assertEquals(y.multiply(x),e.forward().chainMatrix(z(0))); assertNotEquals(x.multiply(y),e.forward().chainMatrix(z(0)));
        assertEquals(xi.multiply(k).multiply(x).add(h),e.sourceHomotopy().chainMatrix(z(0)));
        assertEquals(y.multiply(k).multiply(yi).add(h),e.targetHomotopy().chainMatrix(z(0))); equations(e);
    }
    @Test public void suppliedNonStrictEquivalencesComposeAcrossDifferentFormalDimensions() {
        IntegralChainMappingCone s=pointCone(1),t=cone(SimplicialChainMap.identity(pair(new int[]{0,1}))),u=pointCone(-1);
        IntegralConeEquivalence a=zeroEquivalence(s,t),b=zeroEquivalence(t,u),e=b.compose(a);
        assertFalse(e.forward().isChainIsomorphism()); assertEquals(s,e.source()); assertEquals(u,e.target()); equations(a); equations(e);
        assertEquals(a.sourceHomotopy(),e.sourceHomotopy()); assertEquals(b.targetHomotopy(),e.targetHomotopy());
        IntegralConeChain chain=new IntegralConeChain(s,z(0),new IntegerVector(z(7)));
        assertNotEquals(chain,a.inverseOnChain(a.onChain(chain))); assertEquals(chain.classOf(),a.inverseOnChain(a.onChain(chain)).classOf());
        for(int n=0;n<=3;n++) { assertEquals(a.homologyMap(z(n)).inverse(),a.inverseHomologyMap(z(n))); assertEquals(a.cohomologyMap(z(n)).inverse(),a.inverseCohomologyMap(z(n))); }
    }
    @Test public void torsionUnitsNeedNotBeStrictSquareIsomorphismsAndAllowHugeCoefficients() {
        IntegralChainMappingCone c=pointCone(6); IntegralChainConeMap id=IntegralChainConeMap.identity(c);
        BigInteger scalar=BigInteger.ONE.shiftLeft(1024).multiply(z(6)).subtract(z(1)); IntegralChainConeMap f=id.scale(scalar),g=id.scale(z(-1));
        IntegralConeHomotopy h=IntegralConeHomotopySolver.between(g.compose(f),id); IntegralConeEquivalence e=new IntegralConeEquivalence(f,g,h,h);
        assertFalse(f.isSquareIsomorphism()); equations(e); assertEquals(scalar.add(z(1)).divide(z(6)),h.chainMatrix(z(0)).get(0,0));
        assertEquals(AbelianGroupType.cyclic(z(6)),e.homologyMap(z(0)).source().type()); assertEquals(AbelianGroupType.cyclic(z(6)),e.cohomologyMap(z(1)).source().type());
        for(int n=0;n<=2;n++) { assertEquals(e.homologyMap(z(n)).inverse(),e.inverseHomologyMap(z(n))); assertEquals(e.cohomologyMap(z(n)).inverse(),e.inverseCohomologyMap(z(n))); }
        assertEquals(Arrays.asList(e.homologyMap(z(0)),e.inverseHomologyMap(z(0))),e.homologyMaps(z(0)));
        assertThrows(UnsupportedOperationException.class,() -> e.cohomologyMaps(z(1)).clear());
    }
    @Test public void all625StrictMatrixConversionsMatchIndependentDeterminantAndAdjugate() {
        RelativeSimplicialComplex p=points(2); SimplicialChainMap zero=SimplicialChainMap.zero(p,p),id=SimplicialChainMap.identity(p);
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            SimplicialChainMap v=new SimplicialChainMap(p,p,Collections.singletonList(m(new long[]{a,b},new long[]{c,d}))); IntegralChainConeMap f=strict(zero,zero,id,v); int det=a*d-b*c;
            if(Math.abs(det)==1) { IntegralConeEquivalence e=IntegralConeEquivalence.fromSquareIsomorphism(f); assertEquals(m(new long[]{d/det,-b/det},new long[]{-c/det,a/det}),e.backward().chainMatrix(z(0))); equations(e); }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeEquivalence.fromSquareIsomorphism(f));
        }
    }
    @Test public void strictConversionRetainsAndInvertsChosenSquareShears() {
        RelativeSimplicialComplex circle=pair(new int[]{0,1},new int[]{0,2},new int[]{1,2}); SimplicialChainMap zero=SimplicialChainMap.zero(points(1),circle);
        for(int a : new int[]{-1,1}) for(int b : new int[]{-1,1}) for(int n=-3;n<=3;n++) {
            SimplicialChainHomotopy h=new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{n},new long[]{-n},new long[]{n}),IntegerMatrix.zero(0,0)));
            IntegralChainConeMap f=new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(zero),cone(zero),SimplicialChainMap.identity(points(1)).scale(z(a)),SimplicialChainMap.identity(circle).scale(z(b)),h));
            IntegralConeEquivalence e=IntegralConeEquivalence.fromSquareIsomorphism(f); assertEquals(z(-b*n*a),e.backward().homotopy().chainMatrix(z(0)).get(0,0)); equations(e);
            assertEquals(IntegralConeEquivalence.identity(e.source()),e.inverse().compose(e));
        }
    }
    @Test public void totalConeIsomorphismDoesNotAuthorizeAnInverseRetainedSquare() {
        RelativeSimplicialComplex empty=points(0),point=points(1); FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient(); RelativeSimplicialComplex rel=new RelativeSimplicialComplex(edge,edge.skeleton(0));
        SimplicialChainMap f=SimplicialChainMap.zero(point,empty),g=SimplicialChainMap.zero(empty,rel),zero=SimplicialChainMap.zero(point,rel);
        IntegralChainConeMap map=new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),f,g,new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{1}),IntegerMatrix.zero(0,0)))));
        assertTrue(map.isChainIsomorphism()); assertFalse(map.isSquareIsomorphism()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeEquivalence.fromSquareIsomorphism(map));
    }
    @Test public void compatibilityRequiresBothExactCompositesAndCorrectWitnessOrientations() {
        IntegralConeEquivalence e=zeroEquivalence(pointCone(1),pointCone(-1));
        for(IntegralConeHomotopy bad : Arrays.asList(e.sourceHomotopy().reverse(),IntegralConeHomotopy.stationary(e.sourceHomotopy().from()),IntegralConeHomotopy.stationary(e.sourceHomotopy().to())))
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeEquivalence(e.forward(),e.backward(),bad,e.targetHomotopy()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeEquivalence(e.forward(),e.backward(),e.sourceHomotopy(),e.targetHomotopy().reverse()));
        assertThrows(NullPointerException.class,() -> new IntegralConeEquivalence.Data(e.forward(),null,e.sourceHomotopy(),e.targetHomotopy()));
    }
    @Test public void sameRanksDoNotEraseLabelsRelativePairsOrDefiningMaps() {
        IntegralConeEquivalence a=IntegralConeEquivalence.identity(pointCone(1));
        FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient();
        for(IntegralChainMappingCone c : Arrays.asList(pointCone(-1),cone(SimplicialChainMap.identity(pair(new int[]{7}))),cone(SimplicialChainMap.identity(new RelativeSimplicialComplex(edge,pair(new int[]{0}).ambient()))))) {
            IntegralConeEquivalence b=IntegralConeEquivalence.identity(c);
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.compose(b));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeEquivalence(a.forward(),b.backward(),a.sourceHomotopy(),a.targetHomotopy()));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeEquivalence(a.forward(),a.backward(),b.sourceHomotopy(),a.targetHomotopy()));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> new IntegralConeEquivalence(a.forward(),a.backward(),a.sourceHomotopy(),b.targetHomotopy()));
        }
    }
    @Test public void typedActionsAndBothHomotopiesSatisfyIndependentDifferentialIdentities() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(pair(new int[]{0,1}))); IntegralConeEquivalence e=zeroEquivalence(c,c);
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int k=-1;k<=1;k++) {
            IntegralConeChain chain=new IntegralConeChain(c,z(1),new IntegerVector(z(a),z(b),z(k)));
            IntegralConeCochain cochain=new IntegralConeCochain(c,z(1),new IntegerVector(z(k),z(b),z(a)));
            assertEquals(chain,e.sourceHomotopyOnChain(chain).boundary().add(e.sourceHomotopyOnChain(chain.boundary())).add(e.inverseOnChain(e.onChain(chain))));
            assertEquals(chain,e.targetHomotopyOnChain(chain).boundary().add(e.targetHomotopyOnChain(chain.boundary())).add(e.onChain(e.inverseOnChain(chain))));
            assertEquals(cochain,e.sourceHomotopyOnCochain(cochain).coboundary().add(e.sourceHomotopyOnCochain(cochain.coboundary())).add(e.onCochain(e.inverseOnCochain(cochain))));
            assertEquals(cochain,e.targetHomotopyOnCochain(cochain).coboundary().add(e.targetHomotopyOnCochain(cochain.coboundary())).add(e.inverseOnCochain(e.onCochain(cochain))));
        }
        assertEquals(IntegralConeCochain.zero(c,z(0)),e.onCochain(IntegralConeCochain.zero(c,z(0))));
        assertEquals(IntegralConeChain.zero(c,z(-2)),e.inverseOnChain(IntegralConeChain.zero(c,z(-2))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.sourceHomotopyOnCochain(IntegralConeCochain.zero(c,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.targetHomotopyOnCochain(IntegralConeCochain.zero(c,z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.onChain(IntegralConeChain.zero(pointCone(1),z(0))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> e.inverseOnCochain(IntegralConeCochain.zero(pointCone(1),z(0))));
    }
    @Test public void emptyRelativeAndExtremeDegreesPreservePresentations() {
        for(RelativeSimplicialComplex p : Arrays.asList(points(0),RelativeSimplicialComplex.diagonal(pair(new int[]{0,1,2}).ambient()))) {
            IntegralConeEquivalence e=IntegralConeEquivalence.identity(cone(SimplicialChainMap.identity(p))); equations(e); assertEquals(e,e.compose(e)); assertEquals(e,IntegralConeEquivalence.fromSquareIsomorphism(e.forward()));
            assertEquals(2,e.maps().size()); assertEquals(2,e.homotopies().size()); assertEquals(p.ambient().dimension()<0?0:p.ambient().dimension()+2,e.sourceHomotopy().chainMatrices().size());
            for(AbelianGroupHomomorphism h : e.homologyMaps(BigInteger.ONE.shiftLeft(100))) assertTrue(h.isZero());
            for(Runnable action : Arrays.<Runnable>asList(() -> e.homologyMap(z(-1)),() -> e.inverseHomologyMap(z(-1)),() -> e.cohomologyMap(z(-1)),() -> e.inverseCohomologyMap(z(-1)),() -> e.homologyMaps(z(-1)),() -> e.cohomologyMaps(z(-1)))) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        }
    }
    @Test public void suppliedWitnessesAndStrictConversionAvoidSolverAggregateBounds() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(points(12))); IntegralConeEquivalence e=IntegralConeEquivalence.identity(c);
        assertEquals(e,e.compose(e)); assertEquals(e,IntegralConeEquivalence.fromSquareIsomorphism(e.forward()));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeHomotopySolver.between(e.forward(),e.forward()));
    }
    @Test public void wholeCompositionSharesWorkAcrossIndividuallyValidWitnessStages() {
        IntegralConeEquivalence e=IntegralConeEquivalence.identity(cone(SimplicialChainMap.zero(points(40),points(40))));
        assertEquals(e.forward(),e.forward().compose(e.forward()));
        assertEquals(e.sourceHomotopy(),e.sourceHomotopy().precompose(e.forward()).postcompose(e.backward()).then(e.sourceHomotopy()));
        assertEquals(e,new IntegralConeEquivalence(e.data()));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.compose(e)).getMessage().contains("5000000"));
    }
    @Test public void strictConversionSharesInversionEndpointAndValidationWork() {
        IntegralChainMappingCone c=cone(SimplicialChainMap.zero(points(50),points(50))); IntegralConeEquivalence e=IntegralConeEquivalence.identity(c);
        assertEquals(e.backward(),e.forward().inverseSquare()); assertEquals(e,new IntegralConeEquivalence(e.data()));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeEquivalence.fromSquareIsomorphism(e.forward())).getMessage().contains("5000000"));
    }
    @Test public void pairedIntegralMapsShareAllFourReductionsAndRetainFailureKinds() {
        IntegralConeEquivalence e=IntegralConeEquivalence.identity(cone(SimplicialChainMap.zero(points(0),points(70))));
        assertEquals(e.homologyMap(z(0)),e.inverseHomologyMap(z(0))); assertEquals(e.cohomologyMap(z(0)),e.inverseCohomologyMap(z(0)));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.homologyMaps(z(0))).getMessage().contains("5000000"));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> e.cohomologyMaps(z(0))).getMessage().contains("5000000"));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeEquivalence.identity(cone(SimplicialChainMap.zero(points(0),points(257)))));
    }
    @Test public void nativeConstructionAndActionsReturnActualSecondCarrierWrappers() {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralConeEquivalence e=zeroEquivalence(pointCone(1),pointCone(-1));
        IAlgebraItem<IntegralConeEquivalence> item=math.coneEquivalences.inputs.buildAlgebraItem(e.data()).performAlgebraTransfer("ConeEquivalence.from-data");
        assertSame(math.coneEquivalences.algebra(),math.mathTool.getAlgebra("ConeEquivalence")); assertSame(math.coneEquivalences.algebra(),item.getAlgebra()); assertEquals(e,item.perform().getResult());
        assertSame(math.coneEquivalences.algebra(),item.performOneOperandOperation("inverse").getAlgebra());
        for(String name : Arrays.asList("on-chain","source-homotopy-on-chain")) assertSame(math.coneChains.algebra(),item.performLeftProjectionOperation(name,IntegralConeChain.zero(e.source(),z(0))).getAlgebra());
        for(String name : Arrays.asList("inverse-on-chain","target-homotopy-on-chain")) assertSame(math.coneChains.algebra(),item.performLeftProjectionOperation(name,IntegralConeChain.zero(e.target(),z(0))).getAlgebra());
        for(String name : Arrays.asList("inverse-on-cochain","source-homotopy-on-cochain")) assertSame(math.coneCochains.algebra(),item.performLeftProjectionOperation(name,IntegralConeCochain.zero(e.source(),z(1))).getAlgebra());
        for(String name : Arrays.asList("on-cochain","target-homotopy-on-cochain")) assertSame(math.coneCochains.algebra(),item.performLeftProjectionOperation(name,IntegralConeCochain.zero(e.target(),z(1))).getAlgebra());
        for(IAlgebraItem<?> value : item.performAlgebraFlatTransfer("homotopies")) assertSame(math.coneHomotopies.algebra(),value.getAlgebra());
        for(IAlgebraItem<?> value : item.performAlgebraFlatTransfer("maps")) assertSame(math.chainConeMaps.algebra(),value.getAlgebra());
        for(IAlgebraItem<?> value : item.performUnsafeFlatOperation("homology-maps",z(0))) assertSame(math.abelianHomomorphisms.algebra(),value.getAlgebra());
    }
    @Test public void composedScalarAndFlatFlowsSerializeAndCollectRepeatedly() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralConeEquivalence e=loop(-1,1,2,3);
        List<IAlgebraFlow<?>> flows=Arrays.asList(
                math.flow(math.chainConeMaps,Collections.singletonList(e.forward())).<IntegralConeEquivalence>performAlgebraTransfer("ConeEquivalence.from-square-isomorphism").performOneOperandOperation("inverse").performAlgebraTransfer("forward"),
                math.flow(math.coneEquivalences,Collections.singletonList(e)).performOperation("compose",e).<IntegralConeHomotopy>performFlatAlgebraTransfer("homotopies").performAlgebraUnsafe("chain-matrix",z(0)),
                math.flow(math.coneEquivalences,Collections.singletonList(e)).performLeftProjectionOperation("source-homotopy-on-chain",new IntegralConeChain(e.source(),z(0),new IntegerVector(z(3)))).performAlgebraTransfer("coordinates"),
                math.flow(math.coneEquivalences,Collections.singletonList(e)).<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("cohomology-maps",z(1)).performAlgebraTransfer("is-isomorphism"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Arrays.asList("ZMatrix(1x1)[[0]]","ZMatrix(1x1)[[0]]"),flows.get(1).collect()); assertEquals(Collections.singletonList("[6]"),flows.get(2).collect()); assertEquals(Arrays.asList("true","true"),flows.get(3).collect());
    }
}
