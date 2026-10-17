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

public class NativeIntegralConeElementsTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) { BigInteger[] a=new BigInteger[values.length]; for(int i=0;i<a.length;i++) a[i]=z(values[i]); return new IntegerVector(a); }
    private static IntegerMatrix m(long[]... rows) { BigInteger[][] a=new BigInteger[rows.length][]; for(int i=0;i<a.length;i++) { a[i]=new BigInteger[rows[i].length]; for(int j=0;j<a[i].length;j++) a[i][j]=z(rows[i][j]); } return new IntegerMatrix(a); }
    private static RelativeSimplicialComplex pair(int[]... facets) {
        List<FiniteSet<Integer>> f=new ArrayList<>(); for(int[] face : facets) { List<Integer> labels=new ArrayList<>(); for(int i : face) labels.add(i); f.add(new FiniteSet<>(labels)); } return RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(f));
    }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return pair(f); }
    private static SimplicialChainMap intervalMap(int a,int b,int n) { RelativeSimplicialComplex p=pair(new int[]{0,1}); return new SimplicialChainMap(p,p,Arrays.asList(m(new long[]{a,b},new long[]{n-a,n-b}),m(new long[]{a-b}))); }
    private static IntegralChainMappingCone cone(SimplicialChainMap f) { return new IntegralChainMappingCone(f); }
    private static IntegralChainMappingCone multiple(int n) { return cone(SimplicialChainMap.identity(points(1)).scale(z(n))); }
    private static IntegralChainConeMap swapHomotopy() {
        return IntegralChainConeMap.fromHomotopy(new SimplicialChainHomotopy(intervalMap(1,0,1),intervalMap(0,1,1),Arrays.asList(m(new long[]{1,-1}),IntegerMatrix.zero(0,1))));
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    @Test public void all3375IntervalCoordinatesMatchIndependentSignedDifferentials() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int n=-2;n<=2;n++) {
            IntegralChainMappingCone cone=cone(intervalMap(a,b,n));
            for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++) for(int w=-1;w<=1;w++) {
                IntegralConeChain c=new IntegralConeChain(cone,z(1),v(x,y,w)); IntegralConeCochain u=new IntegralConeCochain(cone,z(1),v(x,y,w));
                assertEquals(v(-x+a*y+b*w,x+(n-a)*y+(n-b)*w),c.boundary().coordinates());
                assertEquals(v((a-b)*x+y-w),u.coboundary().coordinates());
                assertTrue(c.boundary().boundary().isZero()); assertTrue(u.coboundary().coboundary().isZero());
                assertEquals(v(x),c.targetPart().coordinates()); assertEquals(v(y,w),c.sourcePart().coordinates());
            }
        }
    }
    @Test public void all729TypedMapActionsMatchIndependentShearAndTransposeCoordinates() {
        IntegralChainConeMap map=swapHomotopy();
        for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++) for(int w=-1;w<=1;w++) for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) {
            IntegralConeChain chain=new IntegralConeChain(map.source(),z(1),v(x,y,w)); IntegralConeCochain cochain=new IntegralConeCochain(map.target(),z(1),v(a,b,c));
            assertEquals(v(x-y+w,y,w),map.onChain(chain).coordinates()); assertEquals(v(a,b-a,c+a),map.onCochain(cochain).coordinates());
            assertEquals(z(a*(x-y+w)+b*y+c*w),cochain.evaluate(map.onChain(chain)));
            assertEquals(cochain.evaluate(map.onChain(chain)),map.onCochain(cochain).evaluate(chain));
            assertEquals(map.onChain(chain.boundary()),map.onChain(chain).boundary()); assertEquals(map.onCochain(cochain.coboundary()),map.onCochain(cochain).coboundary());
        }
    }
    @Test public void pairingIsAdjointToBoundaryOnAll243SmallCoordinates() {
        IntegralChainMappingCone cone=cone(intervalMap(2,-1,3));
        for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++) for(int w=-1;w<=1;w++) for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) {
            IntegralConeChain chain=new IntegralConeChain(cone,z(1),v(x,y,w)); IntegralConeCochain cochain=new IntegralConeCochain(cone,z(0),v(a,b));
            assertEquals(z(a*(-x+2*y-w)+b*(x+y+4*w)),cochain.evaluate(chain.boundary())); assertEquals(cochain.evaluate(chain.boundary()),cochain.coboundary().evaluate(chain));
        }
    }
    @Test public void typedActionsComposeCovariantlyAndContravariantly() {
        IntegralChainConeMap map=swapHomotopy(),back=IntegralChainConeMap.fromHomotopy(map.homotopy().reverse()),id=back.compose(map);
        for(int k=0;k<=2;k++) {
            for(IntegralConeChain chain : IntegralConeChain.basis(map.source(),z(k))) { assertEquals(chain,id.onChain(chain)); assertEquals(chain,back.onChain(map.onChain(chain))); }
            for(IntegralConeCochain cochain : IntegralConeCochain.basis(map.source(),z(k))) { assertEquals(cochain,id.onCochain(cochain)); assertEquals(cochain,map.onCochain(back.onCochain(cochain))); }
        }
    }
    @Test public void inclusionAndCoordinateSectionsHaveTheRequiredShiftedSigns() {
        IntegralChainMappingCone cone=cone(intervalMap(2,-1,3)); SimplicialChainMap f=cone.map();
        for(int k=0;k<=2;k++) {
            for(RelativeSimplicialChain chain : RelativeSimplicialChain.basisChains(f.source(),z(k))) {
                IntegralConeChain lift=IntegralConeChain.liftSource(cone,chain);
                assertEquals(chain,lift.sourcePart());
                assertEquals(IntegralConeChain.includeTarget(cone,f.onChain(chain)).subtract(IntegralConeChain.liftSource(cone,chain.boundary())),lift.boundary());
            }
            for(RelativeSimplicialChain chain : RelativeSimplicialChain.basisChains(f.target(),z(k))) assertEquals(IntegralConeChain.includeTarget(cone,chain.boundary()),IntegralConeChain.includeTarget(cone,chain).boundary());
            for(RelativeSimplicialCochain u : RelativeSimplicialCochain.basisCochains(f.source(),z(k))) {
                IntegralConeCochain included=IntegralConeCochain.includeSource(cone,u); assertEquals(u,included.sourcePart()); assertEquals(IntegralConeCochain.includeSource(cone,u.coboundary()).negate(),included.coboundary());
            }
            for(RelativeSimplicialCochain u : RelativeSimplicialCochain.basisCochains(f.target(),z(k))) {
                IntegralConeCochain lift=IntegralConeCochain.liftTarget(cone,u); assertEquals(u,lift.targetPart());
                assertEquals(IntegralConeCochain.liftTarget(cone,u.coboundary()).add(IntegralConeCochain.includeSource(cone,f.onCochain(u))),lift.coboundary());
            }
        }
    }
    @Test public void pointMultiplicationRetainsDivisibilityTypedPrimitivesAndTorsion() {
        for(int n=-3;n<=3;n++) {
            IntegralChainMappingCone cone=multiple(n);
            for(int k=-4;k<=4;k++) {
                IntegralConeChain chain=new IntegralConeChain(cone,z(0),v(k)); IntegralConeCochain cochain=new IntegralConeCochain(cone,z(1),v(k));
                boolean divides=n==0?k==0:k%n==0; assertEquals(divides,chain.isBoundary()); assertEquals(divides,cochain.isCoboundary());
                assertTrue(chain.isCycle()); assertTrue(cochain.isCocycle());
                if(divides) { assertEquals(chain,chain.boundingChain().boundary()); assertEquals(cochain,cochain.coboundingCochain().coboundary()); }
                else { failure(MathFailure.Kind.OPERATION_UNDEFINED,chain::boundingChain); failure(MathFailure.Kind.OPERATION_UNDEFINED,cochain::coboundingCochain); }
                assertTrue(chain.homologous(chain.add(new IntegralConeChain(cone,z(0),v(n)))));
                assertTrue(cochain.cohomologous(cochain.add(new IntegralConeCochain(cone,z(1),v(n)))));
                assertTrue(chain.homologous(chain.representative(chain.classOf()))); assertTrue(cochain.cohomologous(cochain.representative(cochain.classOf())));
            }
            if(n!=0) { assertEquals(z(Math.abs(n)),new IntegralConeChain(cone,z(0),v(1)).classOf().order()); assertEquals(z(Math.abs(n)),new IntegralConeCochain(cone,z(1),v(1)).classOf().order()); }
        }
    }
    @Test public void projectivePlaneGeneratorsRetainTypedOrderTwoCyclesAndCocycles() {
        RelativeSimplicialComplex plane=pair(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        IntegralChainMappingCone cone=cone(SimplicialChainMap.zero(points(0),plane));
        List<IntegralConeChain> chains=IntegralConeChain.zero(cone,z(1)).cycleGenerators(); List<IntegralConeCochain> cochains=IntegralConeCochain.zero(cone,z(2)).cocycleGenerators();
        assertEquals(1,chains.size()); assertEquals(1,cochains.size()); IntegralConeChain chain=chains.get(0); IntegralConeCochain cochain=cochains.get(0);
        assertEquals(z(2),chain.classOf().order()); assertEquals(z(2),cochain.classOf().order()); assertFalse(chain.isBoundary()); assertFalse(cochain.isCoboundary());
        assertEquals(chain.scale(z(2)),chain.scale(z(2)).boundingChain().boundary()); assertEquals(cochain.scale(z(2)),cochain.scale(z(2)).coboundingCochain().coboundary());
        assertEquals(cone,chain.cone()); assertEquals(cone,cochain.cone());
    }
    @Test public void classesAndTypedActionsAgreeWithInducedIntegralMaps() {
        for(int n : new int[]{0,2}) {
        IntegralChainConeMap map=IntegralChainConeMap.fromHomotopy(new SimplicialChainHomotopy(intervalMap(1,0,n),intervalMap(0,1,n),Arrays.asList(m(new long[]{1,-1}),IntegerMatrix.zero(0,1))));
        for(int k=0;k<=2;k++) {
            for(IntegralConeChain chain : IntegralConeChain.zero(map.source(),z(k)).cycleGenerators()) assertEquals(map.homologyMap(z(k)).apply(chain.classOf()),map.onChain(chain).classOf());
            for(IntegralConeCochain cochain : IntegralConeCochain.zero(map.target(),z(k)).cocycleGenerators()) assertEquals(map.cohomologyMap(z(k)).apply(cochain.classOf()),map.onCochain(cochain).classOf());
        }
        }
        IntegralChainMappingCone cone=multiple(2); IntegralConeChain chain=new IntegralConeChain(cone,z(0),v(1)); IntegralConeCochain u=IntegralConeCochain.includeSource(cone,new RelativeSimplicialCochain(cone.source(),z(0),v(1)));
        assertEquals(cone.projectionHomologyMap(z(0)).apply(chain.classOf()),chain.sourcePart().classOf());
        assertEquals(cone.projectionCohomologyMap(z(1)).apply(u.sourcePart().classOf()),u.classOf());
        assertEquals(cone.inclusionCohomologyMap(z(1)).apply(u.classOf()),u.targetPart().classOf());
    }
    @Test public void negativeChainDegreesRetainZeroCoordinatesAndAdjacentPresentations() {
        IntegralChainMappingCone cone=multiple(2); IntegralChainConeMap map=IntegralChainConeMap.identity(cone);
        for(int k=-3;k<0;k++) {
            IntegralConeChain c=IntegralConeChain.zero(cone,z(k)); assertTrue(c.isCycle()); assertTrue(c.isBoundary()); assertTrue(c.boundary().isZero()); assertEquals(z(k-1),c.boundary().degree());
            assertEquals(c,c.boundingChain().boundary()); assertEquals(c,map.onChain(c)); assertEquals(Collections.emptyList(),c.cycleGenerators());
        }
        assertEquals(IntegerMatrix.zero(0,1),IntegralConeChain.zero(cone,z(-1)).homology().group().relations());
        assertEquals(IntegerMatrix.zero(0,0),IntegralConeChain.zero(cone,z(-2)).homology().group().relations());
        BigInteger huge=BigInteger.ONE.shiftLeft(100); assertTrue(IntegralConeChain.zero(cone,huge).boundary().isZero()); assertTrue(IntegralConeCochain.zero(cone,huge).coboundary().isZero());
    }
    @Test public void cochainDegreeZeroHasCoordinatePrimitiveButNoNegativeTypedResult() {
        IntegralConeCochain zero=IntegralConeCochain.zero(multiple(2),z(0)); assertTrue(zero.isCoboundary()); assertEquals(v(),zero.coboundingCoordinates());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,zero::coboundingCochain); failure(MathFailure.Kind.OPERATION_UNDEFINED,zero::sourcePart);
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeCochain.zero(zero.cone(),z(-1))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeCochain.basis(zero.cone(),z(-1)));
        IntegralConeCochain nonzero=zero.withCoordinates(v(1)); assertFalse(nonzero.isCocycle()); failure(MathFailure.Kind.OPERATION_UNDEFINED,nonzero::classOf); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> nonzero.cohomologous(zero));
        IntegralConeChain noncycle=new IntegralConeChain(zero.cone(),z(1),v(1)); assertFalse(noncycle.isCycle()); failure(MathFailure.Kind.OPERATION_UNDEFINED,noncycle::classOf); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> noncycle.homologous(noncycle));
    }
    @Test public void arithmeticAndPairingUseUnboundedIntegersAndExactContexts() {
        BigInteger huge=BigInteger.ONE.shiftLeft(1024).add(z(3)); IntegralChainMappingCone cone=multiple(0);
        IntegralConeChain chain=new IntegralConeChain(cone,z(0),new IntegerVector(huge)); IntegralConeCochain cochain=new IntegralConeCochain(cone,z(0),new IntegerVector(huge.negate()));
        assertEquals(huge.multiply(huge).negate(),cochain.evaluate(chain)); assertTrue(chain.add(chain.negate()).isZero()); assertTrue(cochain.subtract(cochain).isZero());
        assertEquals(chain,chain.withCoordinates(chain.coordinates())); assertEquals(chain.hashCode(),chain.withCoordinates(chain.coordinates()).hashCode());
        assertEquals(cochain,cochain.withCoordinates(cochain.coordinates())); assertEquals(cochain.hashCode(),cochain.withCoordinates(cochain.coordinates()).hashCode());
        IntegralConeChain other=IntegralConeChain.zero(multiple(1),z(0)); IntegralConeCochain otherCo=IntegralConeCochain.zero(multiple(1),z(0));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> chain.add(other)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cochain.add(otherCo)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cochain.evaluate(other));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> chain.add(IntegralConeChain.zero(cone,z(1)))); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cochain.evaluate(IntegralConeChain.zero(cone,z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> chain.withCoordinates(v())); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> cochain.withCoordinates(v(1,2)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> chain.representative(new PresentedAbelianGroup(IntegerMatrix.identity(1)).project(v(0))));
    }
    @Test public void mapActionsAndInsertionsRejectFullConeAndPairMismatches() {
        IntegralChainConeMap map=swapHomotopy();
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map.onChain(IntegralConeChain.zero(map.target(),z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map.onCochain(IntegralConeCochain.zero(map.source(),z(1))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> map.onChain(IntegralConeChain.zero(map.target(),z(-1))));
        RelativeSimplicialComplex relabel=pair(new int[]{7,8}),diagonal=RelativeSimplicialComplex.diagonal(map.source().source().ambient());
        for(RelativeSimplicialComplex wrong : Arrays.asList(relabel,diagonal)) {
            RelativeSimplicialChain c=RelativeSimplicialChain.zero(wrong,z(0)); RelativeSimplicialCochain u=RelativeSimplicialCochain.zero(wrong,z(0));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeChain.includeTarget(map.source(),c)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeChain.liftSource(map.source(),c));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeCochain.includeSource(map.source(),u)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeCochain.liftTarget(map.source(),u));
        }
    }
    @Test public void basisListsRespectCombinedRankBoundsAndRelativeFiltering() {
        for(int count : new int[]{255,256}) {
            List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<count;i++) facets.add(FiniteSet.of(i)); facets.add(FiniteSet.of(0,1));
            RelativeSimplicialComplex p=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(facets)); IntegralChainMappingCone c=cone(SimplicialChainMap.zero(p,p));
            if(count==255) { List<IntegralConeChain> basis=IntegralConeChain.basis(c,z(1)); assertEquals(256,basis.size()); for(int i=0;i<256;i++) assertEquals(z(1),basis.get(i).coordinates().get(i)); assertEquals(256,IntegralConeCochain.basis(c,z(1)).size()); }
            else { failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeChain.zero(c,z(1))); failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeCochain.basis(c,z(1))); }
        }
        int[] f=new int[10]; for(int i=0;i<10;i++) f[i]=i; RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(pair(f).ambient()); IntegralChainMappingCone c=cone(SimplicialChainMap.identity(diagonal));
        for(int n=0;n<=10;n++) { assertTrue(IntegralConeChain.zero(c,z(n)).isCycle()); assertTrue(IntegralConeCochain.zero(c,z(n)).isCocycle()); assertEquals(Collections.emptyList(),IntegralConeChain.basis(c,z(n))); }
        IntegralChainMappingCone empty=cone(SimplicialChainMap.identity(points(0))); assertTrue(IntegralConeChain.zero(empty,z(0)).isBoundary()); assertTrue(IntegralConeCochain.zero(empty,z(0)).isCoboundary());
    }
    @Test public void shiftedRelativeCoordinatesRetainTheQuotientAndDegree() {
        RelativeSimplicialComplex interval=new RelativeSimplicialComplex(pair(new int[]{0,1}).ambient(),points(2).ambient()); IntegralChainMappingCone c=cone(SimplicialChainMap.identity(interval));
        IntegralConeChain upper=new IntegralConeChain(c,z(2),v(3)); IntegralConeCochain lower=new IntegralConeCochain(c,z(1),v(4));
        assertEquals(v(3),upper.boundary().coordinates()); assertEquals(v(4),lower.coboundary().coordinates()); assertEquals(z(12),lower.evaluate(upper.boundary()));
        assertEquals(interval,upper.sourcePart().pair()); assertEquals(z(1),upper.sourcePart().degree()); assertEquals(v(3),upper.sourcePart().coordinates());
    }
    @Test public void nativeTypedActionsReturnTheSecondCarrierAndFlowsSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralChainConeMap map=swapHomotopy(); IntegralConeChain c=new IntegralConeChain(map.source(),z(1),v(1,2,3)); IntegralConeCochain u=new IntegralConeCochain(map.target(),z(1),v(4,5,6));
        IAlgebraItem<IntegralChainConeMap> item=math.chainConeMaps.algebra().buildAlgebraItem(map);
        assertSame(math.coneChains.algebra(),item.performLeftProjectionOperation("on-chain",c).getAlgebra()); assertSame(math.coneCochains.algebra(),item.performLeftProjectionOperation("on-cochain",u).getAlgebra());
        assertEquals(v(2,2,3),item.performLeftProjectionOperation("on-chain",c).perform().getResult().coordinates());
        List<IAlgebraFlow<?>> flows=Arrays.asList(
                math.flow(math.chainConeMaps,Collections.singletonList(map)).performLeftProjectionOperation("on-chain",c).performOneOperandOperation("boundary").performAlgebraTransfer("coordinates"),
                math.flow(math.chainConeMaps,Collections.singletonList(map)).performLeftProjectionOperation("on-cochain",u).performAlgebraTransfer("coordinates"),
                math.flow(math.chainCones,Collections.singletonList(multiple(2))).<IntegralConeChain,BigInteger>performAlgebraUnsafe("ConeChain.zero-on",z(0)).performOneOperandFlatOperation("cycle-generators").<AbelianGroupElement>performAlgebraTransfer("class-of").performAlgebraTransfer("order"),
                math.flow(math.chainCones,Collections.singletonList(multiple(2))).<IntegralConeCochain,BigInteger>performAlgebraUnsafe("ConeCochain.zero-on",z(1)).performOneOperandFlatOperation("cocycle-generators").<AbelianGroupElement>performAlgebraTransfer("class-of").performAlgebraTransfer("order"));
        for(IAlgebraFlow<?> original : flows) { ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); } IAlgebraFlow<?> copy; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { copy=(IAlgebraFlow<?>)in.readObject(); } assertEquals(original.collect(),copy.collect()); assertEquals(copy.collect(),copy.collect()); }
        assertEquals(Collections.singletonList("[1, 4]"),flows.get(0).collect()); assertEquals(Collections.singletonList("[4, 1, 10]"),flows.get(1).collect()); assertEquals(Collections.singletonList("2"),flows.get(2).collect()); assertEquals(Collections.singletonList("2"),flows.get(3).collect());
        assertThrows(UnsupportedOperationException.class,() -> IntegralConeChain.basis(map.source(),z(1)).clear()); assertThrows(UnsupportedOperationException.class,() -> IntegralConeCochain.zero(multiple(2),z(1)).cocycleGenerators().clear());
    }
    @Test public void generatorListsShareOneBudgetAcrossHomologyAndGeneratorExtraction() {
        IntegralChainMappingCone cone=cone(SimplicialChainMap.zero(points(110),points(110)));
        IntegralConeChain c=IntegralConeChain.zero(cone,z(0)); IntegralConeCochain u=IntegralConeCochain.zero(cone,z(1));
        assertEquals(110,c.homology().generators().size()); assertEquals(110,u.cohomology().generators().size());
        for(Runnable action : Arrays.<Runnable>asList(c::cycleGenerators,u::cocycleGenerators)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("5000000"));
    }
    @Test public void classProjectionSharesBudgetWithTheWholeReduction() {
        IntegralChainMappingCone cone=cone(SimplicialChainMap.identity(points(118)));
        IntegralConeChain c=IntegralConeChain.zero(cone,z(0)); IntegralConeCochain u=IntegralConeCochain.zero(cone,z(1));
        assertTrue(c.homology().classOf(c.coordinates()).isZero()); assertTrue(u.cohomology().classOf(u.coordinates()).isZero());
        for(Runnable action : Arrays.<Runnable>asList(c::classOf,u::classOf)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("5000000"));
    }

}
