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

public class NativeIntegralConeHomotopySolverTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static RelativeSimplicialComplex pair(int[]... facets) {
        List<FiniteSet<Integer>> f=new ArrayList<>(); for(int[] facet : facets) { List<Integer> v=new ArrayList<>(); for(int x : facet) v.add(x); f.add(new FiniteSet<>(v)); }
        return RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(f));
    }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return pair(f); }
    private static IntegerMatrix m(long[]... rows) { BigInteger[][] r=new BigInteger[rows.length][]; for(int i=0;i<rows.length;i++) { r[i]=new BigInteger[rows[i].length]; for(int j=0;j<rows[i].length;j++) r[i][j]=z(rows[i][j]); } return new IntegerMatrix(r); }
    private static IntegralChainMappingCone cone(SimplicialChainMap f) { return new IntegralChainMappingCone(f); }
    private static IntegralChainMappingCone carrier(RelativeSimplicialComplex p) { return cone(SimplicialChainMap.zero(points(0),p)); }
    private static IntegralChainMappingCone pointCone(int n) { return cone(SimplicialChainMap.identity(points(1)).scale(z(n))); }
    private static IntegralChainConeMap strict(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b) {
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),a,b,SimplicialChainHomotopy.stationary(b.compose(f))));
    }
    private static IntegralChainConeMap lift(SimplicialChainMap map) {
        return strict(carrier(map.source()).map(),carrier(map.target()).map(),SimplicialChainMap.identity(points(0)),map);
    }
    // X is [Z --a--> Z] in degrees 1,0; Y is [Z --b--> Z] in degrees 2,1.
    // Its only nonzero degree matrix is [coefficient] in degree 1.
    private static IntegralChainConeMap cross(int a,int b,BigInteger coefficient) {
        RelativeSimplicialComplex p=points(1); FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient();
        RelativeSimplicialComplex shifted=new RelativeSimplicialComplex(edge,edge.skeleton(0));
        SimplicialChainMap f=SimplicialChainMap.identity(p).scale(z(a)),g=SimplicialChainMap.identity(shifted).scale(z(b)),zero=SimplicialChainMap.zero(p,shifted);
        SimplicialChainHomotopy h=new SimplicialChainHomotopy(zero,zero,Arrays.asList(new IntegerMatrix(new BigInteger[][]{{coefficient.negate()}}),IntegerMatrix.zero(0,0)));
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),zero,zero,h));
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static List<Runnable> operations(IntegralChainConeMap from,IntegralChainConeMap to) {
        return Arrays.asList(() -> IntegralConeHomotopySolver.areHomotopic(from,to),() -> IntegralConeHomotopySolver.between(from,to),() -> IntegralConeHomotopySolver.solutionGenerators(from,to));
    }
    private static void equations(IntegralConeHomotopy h) {
        for(int n=0;n<=Math.max(h.source().dimension(),h.target().dimension());n++) {
            IntegerMatrix lhs=h.target().boundaryMatrix(z(n+1)).multiply(h.chainMatrix(z(n)));
            if(n>0) lhs=lhs.add(h.chainMatrix(z(n-1)).multiply(h.source().boundaryMatrix(z(n))));
            assertEquals(h.to().chainMatrix(z(n)).add(h.from().chainMatrix(z(n)).scale(z(-1))),lhs);
        }
    }
    private static List<IntegralConeHomotopy> check(IntegralChainConeMap from,IntegralChainConeMap to,boolean exists) {
        assertEquals(exists,IntegralConeHomotopySolver.areHomotopic(from,to));
        List<IntegralConeHomotopy> family=IntegralConeHomotopySolver.solutionGenerators(from,to);
        if(!exists) { assertTrue(family.isEmpty()); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeHomotopySolver.between(from,to)); return family; }
        IntegralConeHomotopy witness=IntegralConeHomotopySolver.between(from,to); assertEquals(from,witness.from()); assertEquals(to,witness.to()); assertEquals(witness,family.get(0)); equations(witness);
        for(int i=1;i<family.size();i++) { assertTrue(family.get(i).from().isZero()); assertTrue(family.get(i).to().isZero()); equations(family.get(i)); }
        return family;
    }
    @Test public void all729PointMapPairsAgreeWithIndependentDivisibility() {
        for(int n=-4;n<=4;n++) for(int a=-4;a<=4;a++) for(int b=-4;b<=4;b++) {
            IntegralChainConeMap id=IntegralChainConeMap.identity(pointCone(n)); boolean exists=n==0?a==b:(b-a)%n==0;
            List<IntegralConeHomotopy> family=check(id.scale(z(a)),id.scale(z(b)),exists);
            if(exists) { assertEquals(n==0?2:1,family.size()); if(n!=0) assertEquals(m(new long[]{(b-a)/n}),family.get(0).chainMatrix(z(0))); else assertEquals(z(1),family.get(1).chainMatrix(z(0)).get(0,0).abs()); }
        }
    }
    @Test public void all441CrossDegreeSystemsAgreeWithGcdAndCoupleAdjacentWitnessDegrees() {
        for(int a=-3;a<=3;a++) for(int b=-3;b<=3;b++) for(int c=-4;c<=4;c++) {
            BigInteger gcd=z(a).gcd(z(b)); boolean exists=gcd.signum()==0?c==0:z(c).remainder(gcd).signum()==0;
            List<IntegralConeHomotopy> family=check(cross(a,b,z(0)),cross(a,b,z(c)),exists);
            if(exists) {
                BigInteger x=family.get(0).chainMatrix(z(0)).get(0,0),y=family.get(0).chainMatrix(z(1)).get(0,0);
                assertEquals(z(c),z(a).multiply(x).add(z(b).multiply(y))); assertEquals(gcd.signum()==0?3:2,family.size());
                if(gcd.signum()!=0) {
                    BigInteger u=family.get(1).chainMatrix(z(0)).get(0,0),v=family.get(1).chainMatrix(z(1)).get(0,0);
                    assertEquals(z(b).divide(gcd).abs(),u.abs()); assertEquals(z(a).divide(gcd).abs(),v.abs()); assertEquals(z(1),u.gcd(v));
                }
            }
        }
    }
    @Test public void affineGeneratorsParametrizeEveryBoundedSolutionWithoutLosingEndpointData() {
        IntegralChainConeMap from=cross(6,4,z(-3)),to=cross(6,4,z(-1)); List<IntegralConeHomotopy> family=check(from,to,true);
        IntegralConeHomotopy base=family.get(0),kernel=family.get(1); BigInteger p=base.chainMatrix(z(0)).get(0,0),u=kernel.chainMatrix(z(0)).get(0,0);
        int count=0;
        for(int x=-10;x<=10;x++) for(int y=-10;y<=10;y++) if(6*x+4*y==2) {
            BigInteger[] qr=z(x).subtract(p).divideAndRemainder(u); assertEquals(z(0),qr[1]); IntegralConeHomotopy actual=base.add(kernel.scale(qr[0]));
            assertEquals(m(new long[]{x}),actual.chainMatrix(z(0))); assertEquals(m(new long[]{y}),actual.chainMatrix(z(1))); assertEquals(from,actual.from()); assertEquals(to,actual.to()); count++;
        }
        assertTrue(count>4); assertThrows(UnsupportedOperationException.class,family::clear);
    }
    @Test public void equalHomologyAndCohomologyMapsDoNotRemoveIntegralParityObstructions() {
        IntegralChainConeMap zero=cross(2,2,z(0)),odd=cross(2,2,z(1));
        for(int n=0;n<=2;n++) { assertEquals(zero.homologyMap(z(n)),odd.homologyMap(z(n))); assertEquals(zero.cohomologyMap(z(n)),odd.cohomologyMap(z(n))); }
        check(zero,odd,false); check(zero,cross(2,2,z(2)),true);
    }
    @Test public void coefficientsHaveNoBitLengthCapAndRationalOnlyWitnessesAreRejected() {
        BigInteger huge=BigInteger.ONE.shiftLeft(1024).add(z(1)); IntegralChainConeMap zero=cross(6,4,z(0));
        List<IntegralConeHomotopy> family=check(zero,cross(6,4,huge.multiply(z(2))),true);
        BigInteger x=family.get(0).chainMatrix(z(0)).get(0,0),y=family.get(0).chainMatrix(z(1)).get(0,0);
        assertEquals(huge.multiply(z(2)),x.multiply(z(6)).add(y.multiply(z(4)))); check(zero,cross(6,4,huge),false); check(zero,cross(6,4,huge.multiply(z(-2))),true);
    }
    private static SimplicialChainMap intervalMap(int a,int b,int augmentation) {
        RelativeSimplicialComplex p=pair(new int[]{0,1}); return new SimplicialChainMap(p,p,Arrays.asList(m(new long[]{a,b},new long[]{augmentation-a,augmentation-b}),m(new long[]{a-b})));
    }
    @Test public void all125IntervalContractionsAgreeWithIndependentAugmentationUnits() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int n=-2;n<=2;n++) {
            IntegralChainMappingCone c=cone(intervalMap(a,b,n)); boolean contractible=Math.abs(n)==1;
            assertEquals(contractible,IntegralConeHomotopySolver.isContractible(c));
            if(contractible) { IntegralConeHomotopy h=IntegralConeHomotopySolver.contract(c); assertTrue(h.from().isZero()); assertTrue(h.to().isIdentity()); equations(h); }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeHomotopySolver.contract(c));
        }
    }
    @Test public void solvedContractionFillsCyclesWhenStrictIsomorphismContractionIsUndefined() {
        IntegralChainMappingCone c=cone(intervalMap(1,1,1)); assertFalse(c.map().isIsomorphism()); assertTrue(c.isAcyclic());
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeHomotopy.contractIsomorphism(c)); IntegralConeHomotopy h=IntegralConeHomotopySolver.contract(c);
        IntegralConeChain cycle=new IntegralConeChain(c,z(0),new IntegerVector(z(3),z(-7))); assertEquals(cycle,h.onChain(cycle).boundary());
        IntegralConeCochain cocycle=new IntegralConeCochain(c,z(2),new IntegerVector(z(5))); assertEquals(cocycle,h.onCochain(cocycle).coboundary());
    }
    private static IntegralChainConeMap loop(int n) {
        RelativeSimplicialComplex circle=pair(new int[]{0,1},new int[]{0,2},new int[]{1,2}); SimplicialChainMap zero=SimplicialChainMap.zero(points(1),circle);
        return IntegralChainConeMap.fromHomotopy(new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{n},new long[]{-n},new long[]{n}),IntegerMatrix.zero(0,0))));
    }
    @Test public void all25SquareLoopPairsUseTheActualChosenWitnessNotOnlyVerticalMaps() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) {
            IntegralChainConeMap f=loop(a),g=loop(b); assertEquals(f.sourceMap(),g.sourceMap()); assertEquals(f.targetMap(),g.targetMap()); check(f,g,a==b);
        }
    }
    @Test public void emptyFilteredAndChangingDegreesRetainEveryFormalZeroShape() {
        IntegralChainMappingCone empty=carrier(points(0)),point=carrier(points(1));
        for(IntegralChainMappingCone s : Arrays.asList(empty,point)) for(IntegralChainMappingCone t : Arrays.asList(empty,point)) {
            IntegralChainConeMap zero=IntegralChainConeMap.zero(s,t); List<IntegralConeHomotopy> family=check(zero,zero,true); assertEquals(Collections.singletonList(IntegralConeHomotopy.stationary(zero)),family);
        }
        assertTrue(IntegralConeHomotopySolver.isContractible(empty)); assertEquals(0,IntegralConeHomotopySolver.contract(empty).chainMatrices().size());
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(pair(new int[]{0,1,2}).ambient()); IntegralChainMappingCone filtered=cone(SimplicialChainMap.identity(diagonal));
        IntegralConeHomotopy h=IntegralConeHomotopySolver.contract(filtered); assertEquals(Collections.nCopies(4,IntegerMatrix.zero(0,0)),h.chainMatrices());
        assertEquals(IntegralConeChain.zero(filtered,z(0)),h.onChain(IntegralConeChain.zero(filtered,z(-1))));
    }
    @Test public void allBinarySolversRejectDifferentDefiningMapsAndLabelledPairs() {
        IntegralChainConeMap id=IntegralChainConeMap.identity(pointCone(2)),different=IntegralChainConeMap.identity(pointCone(-2)),relabelled=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(pair(new int[]{9})).scale(z(2))));
        for(IntegralChainConeMap other : Arrays.asList(different,relabelled)) for(Runnable action : operations(id,other)) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
        FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient(); RelativeSimplicialComplex left=new RelativeSimplicialComplex(edge,points(1).ambient()),right=new RelativeSimplicialComplex(edge,pair(new int[]{1}).ambient());
        IntegralChainConeMap a=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(left))),b=IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(right)));
        for(Runnable action : operations(a,b)) failure(MathFailure.Kind.OPERATION_UNDEFINED,action);
    }
    @Test public void aggregateEquationBoundsAccept256AndReject289WithoutFalseDecisions() {
        IntegralChainConeMap small=IntegralChainConeMap.identity(carrier(points(16))),large=IntegralChainConeMap.identity(carrier(points(17)));
        check(small,small,true); check(small,small.zeroLike(),false);
        for(Runnable action : operations(large,large)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total equations"));
        IntegralChainMappingCone c=cone(SimplicialChainMap.identity(points(12))); equations(IntegralConeHomotopy.contractIsomorphism(c));
        for(Runnable action : Arrays.<Runnable>asList(() -> IntegralConeHomotopySolver.isContractible(c),() -> IntegralConeHomotopySolver.contract(c))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total equations"));
    }
    @Test public void maximalKernelReturnsAll257WitnessesAndUnknownBoundsAreIndependent() {
        IntegralChainMappingCone target=cone(SimplicialChainMap.zero(points(16),points(0)));
        IntegralChainConeMap small=IntegralChainConeMap.zero(carrier(points(16)),target),large=IntegralChainConeMap.zero(carrier(points(17)),target);
        List<IntegralConeHomotopy> family=check(small,small,true); assertEquals(257,family.size());
        for(int i=0;i<256;i++) {
            IntegerMatrix h=family.get(i+1).chainMatrix(z(0)); assertEquals(16,h.rows()); assertEquals(16,h.columns());
            for(int r=0;r<16;r++) for(int c=0;c<16;c++) assertEquals(z(r*16+c==i?1:0),h.get(r,c));
        }
        for(Runnable action : operations(large,large)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total unknown coefficients"));
    }
    @Test public void denseSmithWorkExhaustionIsNotReportedAsMathematicalAbsence() {
        int[][] facets=new int[16][2]; for(int i=0;i<16;i++) { facets[i][0]=0; facets[i][1]=i+1; }
        RelativeSimplicialComplex source=points(16),target=new RelativeSimplicialComplex(pair(facets).ambient(),points(1).ambient());
        SimplicialChainMap f=SimplicialChainMap.zero(source,target),g=new SimplicialChainMap(source,target,Arrays.asList(IntegerMatrix.identity(16),IntegerMatrix.zero(16,0)));
        IntegralChainConeMap from=lift(f),to=lift(g); equations(new IntegralConeHomotopy(from,to,Arrays.asList(IntegerMatrix.identity(16),IntegerMatrix.zero(0,0))));
        for(Runnable action : operations(from,to)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("5000000"));
    }
    @Test public void completeGeneratorFamiliesShareSolveAndValidationWorkWithoutTruncation() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<12;i++) for(int j=i+1;j<12;j++) if(facets.size()<63) facets.add(FiniteSet.of(i,j));
        IntegralChainConeMap zero=IntegralChainConeMap.zero(carrier(pair(new int[]{0,1},new int[]{0,2},new int[]{1,2})),carrier(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(facets))));
        assertTrue(IntegralConeHomotopySolver.areHomotopic(zero,zero)); equations(IntegralConeHomotopySolver.between(zero,zero));
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegralConeHomotopySolver.solutionGenerators(zero,zero)).getMessage().contains("5000000"));
    }
    @Test public void combinedConeRankLimitsApplyBeforeClaimsAboutSolvability() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<24 && facets.size()<256;i++) for(int j=i+1;j<24 && facets.size()<256;j++) facets.add(FiniteSet.of(i,j));
        FiniteSimplicialComplex graph=new FiniteSimplicialComplex(facets); RelativeSimplicialComplex relative=new RelativeSimplicialComplex(graph,graph.skeleton(0));
        IntegralChainMappingCone large=cone(SimplicialChainMap.zero(points(1),relative)); IntegralChainConeMap zero=IntegralChainConeMap.zero(large,carrier(points(0)));
        for(Runnable action : operations(zero,zero)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total target"));
    }
    @Test public void nativeDecisionsWitnessesFamiliesAndContractionsUseActualSerializableFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralChainConeMap from=IntegralChainConeMap.identity(pointCone(2)),to=from.scale(z(3));
        IAlgebraItem<IntegralChainConeMap> item=math.chainConeMaps.algebra().buildAlgebraItem(from);
        IAlgebraItem<IntegralConeHomotopy> witness=item.performCustomResultOperation("ConeHomotopy.between",to); assertSame(math.coneHomotopies.algebra(),witness.getAlgebra()); assertEquals(m(new long[]{1}),witness.perform().getResult().chainMatrix(z(0)));
        for(IAlgebraItem<IntegralConeHomotopy> h : item.<IntegralConeHomotopy>performCustomResultFlatOperation("ConeHomotopy.solution-generators",to)) assertSame(math.coneHomotopies.algebra(),h.getAlgebra());
        IntegralConeChain cycle=new IntegralConeChain(from.source(),z(0),new IntegerVector(z(3)));
        assertSame(math.coneChains.algebra(),witness.performLeftProjectionOperation("on-chain",cycle).getAlgebra());
        IntegralChainMappingCone contractible=cone(intervalMap(1,1,1));
        List<IAlgebraFlow<?>> flows=Arrays.asList(
            math.flow(math.chainConeMaps,Collections.singletonList(from)).performCustomResultOperation("ConeHomotopy.are-homotopic",to),
            math.flow(math.chainConeMaps,Collections.singletonList(from)).<IntegralConeHomotopy>performCustomResultOperation("ConeHomotopy.between",to).performLeftProjectionOperation("on-chain",cycle).performAlgebraTransfer("coordinates"),
            math.flow(math.chainConeMaps,Collections.singletonList(from)).<IntegralConeHomotopy>performFlatCustomResultOperation("ConeHomotopy.solution-generators",to).performFlatAlgebraTransfer("chain-matrices"),
            math.flow(math.chainCones,Collections.singletonList(contractible)).performAlgebraTransfer("ConeHomotopy.is-contractible"),
            math.flow(math.chainCones,Collections.singletonList(contractible)).<IntegralConeHomotopy>performAlgebraTransfer("ConeHomotopy.contract").performFlatAlgebraTransfer("chain-matrices"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("true"),flows.get(0).collect()); assertEquals(Collections.singletonList("[3]"),flows.get(1).collect());
        assertEquals(Arrays.asList("ZMatrix(1x1)[[1]]","ZMatrix(0x1)[]"),flows.get(2).collect()); assertEquals(Collections.singletonList("true"),flows.get(3).collect()); assertEquals(3,flows.get(4).collect().size());
        assertEquals(Collections.emptyList(),math.flow(math.chainConeMaps,Collections.singletonList(from)).performFlatCustomResultOperation("ConeHomotopy.solution-generators",from.scale(z(2))).collect());
    }
}
