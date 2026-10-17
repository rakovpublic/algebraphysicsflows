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

public class NativeIntegralConeInverseTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static RelativeSimplicialComplex pair(int[]... facets) {
        List<FiniteSet<Integer>> f=new ArrayList<>(); for(int[] facet : facets) { List<Integer> v=new ArrayList<>(); for(int x : facet) v.add(x); f.add(new FiniteSet<>(v)); }
        return RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(f));
    }
    private static RelativeSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return pair(f); }
    private static IntegerMatrix m(long[]... rows) { BigInteger[][] r=new BigInteger[rows.length][]; for(int i=0;i<rows.length;i++) { r[i]=new BigInteger[rows[i].length]; for(int j=0;j<rows[i].length;j++) r[i][j]=z(rows[i][j]); } return new IntegerMatrix(r); }
    private static IntegralChainMappingCone cone(SimplicialChainMap f) { return new IntegralChainMappingCone(f); }
    private static IntegralChainMappingCone pointCone(int n) { return cone(SimplicialChainMap.identity(points(1)).scale(z(n))); }
    private static IntegralChainMappingCone carrier(RelativeSimplicialComplex pair) { return cone(SimplicialChainMap.zero(points(0),pair)); }
    private static IntegralChainConeMap strict(SimplicialChainMap f,SimplicialChainMap g,SimplicialChainMap a,SimplicialChainMap b) {
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),a,b,SimplicialChainHomotopy.stationary(b.compose(f))));
    }
    private static IntegralChainConeMap lift(SimplicialChainMap f) { return strict(carrier(f.source()).map(),carrier(f.target()).map(),SimplicialChainMap.identity(points(0)),f); }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static List<Runnable> operations(IntegralChainConeMap f) { return Arrays.asList(() -> IntegralConeInverseSolver.hasRetainedInverse(f),() -> IntegralConeInverseSolver.inverse(f),() -> IntegralConeInverseSolver.inverseHomotopies(f),() -> IntegralConeEquivalence.fromMap(f)); }
    private static void equations(IntegralConeHomotopy h) {
        for(int n=0;n<=h.source().dimension();n++) {
            IntegerMatrix lhs=h.target().boundaryMatrix(z(n+1)).multiply(h.chainMatrix(z(n)));
            if(n>0) lhs=lhs.add(h.chainMatrix(z(n-1)).multiply(h.source().boundaryMatrix(z(n))));
            assertEquals(IntegerMatrix.identity(h.source().chainRank(z(n)).intValueExact()).add(h.from().chainMatrix(z(n)).scale(z(-1))),lhs);
        }
    }
    private static IntegralConeEquivalence check(IntegralChainConeMap f,boolean expected) {
        assertEquals(expected,IntegralConeInverseSolver.hasRetainedInverse(f));
        if(!expected) {
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeInverseSolver.inverse(f));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeInverseSolver.inverseHomotopies(f));
            failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegralConeEquivalence.fromMap(f)); return null;
        }
        IntegralConeEquivalence e=IntegralConeEquivalence.fromMap(f); IntegralChainConeMap g=IntegralConeInverseSolver.inverse(f);
        assertEquals(f,e.forward()); assertEquals(g,e.backward()); assertEquals(f.source(),g.target()); assertEquals(f.target(),g.source());
        assertEquals(e.homotopies(),IntegralConeInverseSolver.inverseHomotopies(f));
        assertEquals(g.compose(f),e.sourceHomotopy().from()); assertEquals(f.compose(g),e.targetHomotopy().from());
        assertTrue(e.sourceHomotopy().to().isIdentity()); assertTrue(e.targetHomotopy().to().isIdentity());
        equations(e.sourceHomotopy()); equations(e.targetHomotopy());
        return e;
    }
    @Test public void all625PointMatricesMatchIndependentDeterminantAndAdjugate() {
        RelativeSimplicialComplex p=points(2);
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            IntegralChainConeMap f=lift(new SimplicialChainMap(p,p,Collections.singletonList(m(new long[]{a,b},new long[]{c,d})))); int det=a*d-b*c;
            IntegralConeEquivalence e=check(f,Math.abs(det)==1);
            if(e!=null) assertEquals(m(new long[]{d/det,-b/det},new long[]{-c/det,a/det}),e.backward().chainMatrix(z(0)));
        }
    }
    @Test public void all169TwoTermScalarsMatchIntegerGcdIncludingFreeAndContractibleCases() {
        for(int n=-6;n<=6;n++) for(int scalar=-6;scalar<=6;scalar++) {
            IntegralChainConeMap f=IntegralChainConeMap.identity(pointCone(n)).scale(z(scalar)); IntegralConeEquivalence e=check(f,z(n).gcd(z(scalar)).equals(z(1)));
            if(e!=null) {
                for(int degree=0;degree<=1;degree++) { assertEquals(e.homologyMap(z(degree)).inverse(),e.inverseHomologyMap(z(degree))); assertEquals(e.cohomologyMap(z(degree)).inverse(),e.inverseCohomologyMap(z(degree))); }
                if(n!=0) { BigInteger v=e.backward().chainMatrix(z(0)).get(0,0); assertEquals(z(1).subtract(z(scalar).multiply(v)).divide(z(n)),e.sourceHomotopy().chainMatrix(z(0)).get(0,0)); }
            }
        }
    }
    @Test public void compatibleMapsBetweenTwoTermConesMatchCyclicGroupAndFreeRankOracles() {
        SimplicialChainMap id=SimplicialChainMap.identity(points(1));
        for(int n=-3;n<=3;n++) for(int m=-3;m<=3;m++) for(int a=-3;a<=3;a++) for(int b=-3;b<=3;b++) if(b*n==m*a) {
            IntegralChainConeMap f=strict(id.scale(z(n)),id.scale(z(m)),id.scale(z(a)),id.scale(z(b)));
            boolean expected=n==0 && m==0?Math.abs(a)==1 && Math.abs(b)==1:n!=0 && m!=0 && Math.abs(n)==Math.abs(m) && z(b).gcd(z(m)).equals(z(1));
            check(f,expected);
        }
    }
    private static RelativeSimplicialComplex relativeEdge() { FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient(); return new RelativeSimplicialComplex(edge,edge.skeleton(0)); }
    private static IntegralChainConeMap shear(int a,int b,int h) {
        RelativeSimplicialComplex point=points(1),edge=relativeEdge(); SimplicialChainMap zero=SimplicialChainMap.zero(point,edge);
        SimplicialChainHomotopy witness=new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{h}),IntegerMatrix.zero(0,0)));
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(zero),cone(zero),SimplicialChainMap.identity(point).scale(z(a)),SimplicialChainMap.identity(edge).scale(z(b)),witness));
    }
    @Test public void all125OverlappingDegreeShearsDecodeTheNegativeUpperRightWitness() {
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int h=-2;h<=2;h++) {
            IntegralConeEquivalence e=check(shear(a,b,h),Math.abs(a*b)==1);
            if(e!=null) { assertEquals(shear(a,b,-b*h*a),e.backward()); assertEquals(e.backward(),e.forward().inverseSquare()); }
        }
    }
    @Test public void noncommutingBlocksRetainInverseMultiplicationOrder() {
        RelativeSimplicialComplex p=points(2); FiniteSimplicialComplex graph=pair(new int[]{0,1},new int[]{0,2}).ambient(); RelativeSimplicialComplex edges=new RelativeSimplicialComplex(graph,graph.skeleton(0));
        IntegerMatrix a=m(new long[]{1,2},new long[]{0,1}),b=m(new long[]{1,0},new long[]{3,1}),h=m(new long[]{1,2},new long[]{3,4}),ai=m(new long[]{1,-2},new long[]{0,1}),bi=m(new long[]{1,0},new long[]{-3,1});
        SimplicialChainMap zero=SimplicialChainMap.zero(p,edges),am=new SimplicialChainMap(p,p,Collections.singletonList(a)),bm=new SimplicialChainMap(edges,edges,Arrays.asList(IntegerMatrix.zero(0,0),b));
        IntegralChainConeMap f=new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(zero),cone(zero),am,bm,new SimplicialChainHomotopy(zero,zero,Arrays.asList(h,IntegerMatrix.zero(0,0)))));
        IntegralConeEquivalence e=check(f,true); assertEquals(ai,e.backward().sourceMap().chainMatrix(z(0))); assertEquals(bi,e.backward().targetMap().chainMatrix(z(1)));
        assertEquals(bi.multiply(h).multiply(ai).scale(z(-1)),e.backward().homotopy().chainMatrix(z(0)));
    }
    @Test public void totalConeIsomorphismCanHaveNoInverseInTheRetainedCarrier() {
        RelativeSimplicialComplex point=points(1),empty=points(0),edge=relativeEdge(); SimplicialChainMap f=SimplicialChainMap.zero(point,empty),g=SimplicialChainMap.zero(empty,edge),zero=SimplicialChainMap.zero(point,edge);
        IntegralChainConeMap map=new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(f),cone(g),f,g,new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{1}),IntegerMatrix.zero(0,0)))));
        assertTrue(map.isChainIsomorphism()); assertTrue(map.homologyMap(z(1)).isIsomorphism()); assertTrue(map.cohomologyMap(z(1)).isIsomorphism()); check(map,false);
    }
    @Test public void aOneSidedInverseDoesNotSatisfyBothConeIdentities() {
        RelativeSimplicialComplex p=points(1),q=points(2);
        IntegralChainConeMap f=lift(new SimplicialChainMap(p,q,Collections.singletonList(m(new long[]{1},new long[]{0})))),g=lift(new SimplicialChainMap(q,p,Collections.singletonList(m(new long[]{1,0}))));
        assertTrue(g.compose(f).isIdentity()); assertFalse(f.compose(g).isIdentity()); check(f,false); check(g,false);
    }
    @Test public void noninvertibleIntervalMapsHaveSolvedConeInversesAndNonzeroWitnesses() {
        RelativeSimplicialComplex edge=pair(new int[]{0,1}); SimplicialChainMap id=SimplicialChainMap.identity(edge);
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) {
            SimplicialChainMap constant=new SimplicialChainMap(edge,edge,Arrays.asList(m(new long[]{a,b},new long[]{1-a,1-b}),m(new long[]{a-b})));
            IntegralConeEquivalence e=check(IntegralChainConeMap.zero(cone(constant),cone(id)),true); assertTrue(e.backward().isZero());
            assertFalse(e.sourceHomotopy().equals(IntegralConeHomotopy.stationary(e.sourceHomotopy().from())));
        }
    }
    @Test public void differentLabelsDimensionsAndRelativeSubcomplexesAreRetainedExactly() {
        RelativeSimplicialComplex point=pair(new int[]{9}),edge=pair(new int[]{0,1}),triangle=pair(new int[]{0,1,2});
        IntegralConeEquivalence e=check(IntegralChainConeMap.zero(cone(SimplicialChainMap.identity(point)),cone(SimplicialChainMap.identity(triangle))),true);
        assertEquals(2,e.sourceHomotopy().chainMatrices().size()); assertEquals(4,e.targetHomotopy().chainMatrices().size());
        IntegralConeEquivalence inclusion=check(lift(SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(edge,triangle))),true);
        assertEquals(edge,inclusion.backward().target().target()); assertEquals(triangle,inclusion.backward().source().target());
        for(int vertex=0;vertex<=1;vertex++) {
            RelativeSimplicialComplex relative=new RelativeSimplicialComplex(edge.ambient(),pair(new int[]{vertex}).ambient());
            IntegralConeEquivalence relativeInverse=check(IntegralChainConeMap.zero(carrier(relative),carrier(points(0))),true); assertEquals(relative,relativeInverse.backward().target().target());
        }
    }
    @Test public void emptyAndFullyFilteredContextsRetainTerminalZeroSlots() {
        IntegralChainMappingCone empty=carrier(points(0)),diagonal=cone(SimplicialChainMap.identity(RelativeSimplicialComplex.diagonal(pair(new int[]{0,1,2}).ambient())));
        for(IntegralChainMappingCone s : Arrays.asList(empty,diagonal)) for(IntegralChainMappingCone t : Arrays.asList(empty,diagonal)) {
            IntegralConeEquivalence e=check(IntegralChainConeMap.zero(s,t),true); assertEquals(s.dimension()+1,e.sourceHomotopy().chainMatrices().size()); assertEquals(t.dimension()+1,e.targetHomotopy().chainMatrices().size());
            assertEquals(2,e.homotopies().size());
        }
        assertThrows(UnsupportedOperationException.class,() -> IntegralConeInverseSolver.inverseHomotopies(IntegralChainConeMap.identity(empty)).clear());
    }
    @Test public void largeIntegerCoefficientsKeepTorsionObstructionsExact() {
        BigInteger huge=z(1).shiftLeft(1024),unit=huge.multiply(z(6)).add(z(1)); IntegralChainConeMap id=IntegralChainConeMap.identity(pointCone(6));
        IntegralConeEquivalence e=check(id.scale(unit),true); check(id.scale(huge.multiply(z(6))),false);
        assertEquals(AbelianGroupType.cyclic(z(6)),e.homologyMap(z(0)).source().type()); assertEquals(e.homologyMap(z(0)).inverse(),e.inverseHomologyMap(z(0)));
        assertEquals(e.cohomologyMap(z(1)).inverse(),e.inverseCohomologyMap(z(1)));
    }
    @Test public void solvedWitnessActionsFillBothChainAndCochainInverseDifferences() {
        IntegralConeEquivalence e=check(IntegralChainConeMap.identity(pointCone(6)).scale(z(5)),true); IntegralChainMappingCone c=e.source();
        for(int n=-4;n<=4;n++) {
            IntegralConeChain chain=new IntegralConeChain(c,z(0),new IntegerVector(z(n))); IntegralConeCochain cochain=new IntegralConeCochain(c,z(1),new IntegerVector(z(n)));
            assertEquals(chain,e.sourceHomotopyOnChain(chain).boundary().add(e.inverseOnChain(e.onChain(chain))));
            assertEquals(chain,e.targetHomotopyOnChain(chain).boundary().add(e.onChain(e.inverseOnChain(chain))));
            assertEquals(cochain,e.sourceHomotopyOnCochain(cochain).coboundary().add(e.onCochain(e.inverseOnCochain(cochain))));
            assertEquals(cochain,e.targetHomotopyOnCochain(cochain).coboundary().add(e.inverseOnCochain(e.onCochain(cochain))));
        }
    }
    @Test public void aggregateEquationBoundsCountBothIdentitiesAndAllChainEquations() {
        check(IntegralChainConeMap.identity(carrier(points(11))),true);
        IntegralChainConeMap large=IntegralChainConeMap.identity(carrier(points(12)));
        assertEquals(large.inverseSquare(),IntegralConeEquivalence.fromSquareIsomorphism(large).backward());
        for(Runnable action : operations(large)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total equations"));
        check(IntegralChainConeMap.zero(carrier(points(16)),carrier(points(0))),false);
        for(Runnable action : operations(IntegralChainConeMap.zero(carrier(points(17)),carrier(points(0))))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total equations"));
        check(IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(7)))),true);
        for(Runnable action : operations(IntegralChainConeMap.identity(cone(SimplicialChainMap.identity(points(8)))))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total equations"));
    }
    @Test public void smithWorkLimitsAreFailuresEvenForAnIdentityHomotopyClass() {
        List<FiniteSet<Integer>> fs=new ArrayList<>(); for(int i=0;i<6;i++) for(int j=i+1;j<6;j++) if(fs.size()<8) fs.add(FiniteSet.of(i,j));
        RelativeSimplicialComplex pair=RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(fs));
        IntegralChainConeMap f=lift(SimplicialChainMapClass.identityOn(pair).representative());
        assertTrue(f.homologyMap(z(0)).isIsomorphism()); assertTrue(f.homologyMap(z(1)).isIsomorphism());
        for(Runnable action : operations(f)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("5000000"));
    }
    @Test public void combinedConeRankBoundsPrecedeDecisionsEvenWithFilteredEndpointBases() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<24 && facets.size()<256;i++) for(int j=i+1;j<24 && facets.size()<256;j++) facets.add(FiniteSet.of(i,j));
        FiniteSimplicialComplex graph=new FiniteSimplicialComplex(facets); RelativeSimplicialComplex relative=new RelativeSimplicialComplex(graph,graph.skeleton(0));
        IntegralChainConeMap f=IntegralChainConeMap.zero(cone(SimplicialChainMap.zero(points(1),relative)),carrier(points(0)));
        for(Runnable action : operations(f)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action).getMessage().contains("256 total target"));
    }
    @Test public void nativeDecisionInverseWitnessAndEquivalenceWrappersSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralChainConeMap f=IntegralChainConeMap.identity(pointCone(6)).scale(z(5)); IntegralConeEquivalence e=IntegralConeEquivalence.fromMap(f);
        IAlgebraItem<IntegralChainConeMap> item=math.chainConeMaps.algebra().buildAlgebraItem(f);
        assertSame(math.booleans.algebra(),item.performAlgebraTransfer("has-retained-homotopy-inverse").getAlgebra());
        assertSame(math.chainConeMaps.algebra(),item.performOneOperandOperation("retained-homotopy-inverse").getAlgebra());
        IAlgebraItem<IntegralConeEquivalence> equivalence=item.performAlgebraTransfer("ConeEquivalence.from-map"); assertSame(math.coneEquivalences.algebra(),equivalence.getAlgebra()); assertEquals(e,equivalence.perform().getResult());
        List<IAlgebraItem<IntegralConeHomotopy>> witnesses=item.performAlgebraFlatTransfer("ConeHomotopy.inverse-homotopies"); assertEquals(2,witnesses.size()); for(IAlgebraItem<?> witness : witnesses) assertSame(math.coneHomotopies.algebra(),witness.getAlgebra());
        List<IAlgebraFlow<?>> flows=Arrays.asList(
                math.flow(math.chainConeMaps,Collections.singletonList(f)).performAlgebraTransfer("has-retained-homotopy-inverse"),
                math.flow(math.chainConeMaps,Collections.singletonList(f)).performOneOperandOperation("retained-homotopy-inverse").performAlgebraTransfer("source"),
                math.flow(math.chainConeMaps,Collections.singletonList(f)).<IntegralConeHomotopy>performFlatAlgebraTransfer("ConeHomotopy.inverse-homotopies").<IntegralChainConeMap>performAlgebraTransfer("to").performAlgebraTransfer("is-identity"),
                math.flow(math.chainConeMaps,Collections.singletonList(f)).<IntegralConeEquivalence>performAlgebraTransfer("ConeEquivalence.from-map").<AbelianGroupHomomorphism,BigInteger>performFlatAlgebraUnsafe("homology-maps",z(0)).performAlgebraTransfer("is-isomorphism"));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("true"),flows.get(0).collect()); assertEquals(Arrays.asList("true","true"),flows.get(2).collect()); assertEquals(Arrays.asList("true","true"),flows.get(3).collect());
    }
}
