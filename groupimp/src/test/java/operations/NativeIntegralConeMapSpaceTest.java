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

public class NativeIntegralConeMapSpaceTest {
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
    private static IntegralChainConeMap strict(IntegralChainMappingCone s,IntegralChainMappingCone t,SimplicialChainMap a,SimplicialChainMap b) {
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(s,t,a,b,SimplicialChainHomotopy.stationary(b.compose(s.map()))));
    }
    private static IntegralChainConeMap lift(SimplicialChainMap f) { return strict(carrier(f.source()),carrier(f.target()),SimplicialChainMap.identity(points(0)),f); }
    private static MathFailure failure(MathFailure.Kind kind,Runnable action) { MathFailure e=assertThrows(MathFailure.class,action::run); assertEquals(kind,e.kind()); return e; }
    private static void roundTrip(IntegralConeMapSpace space,IntegralChainConeMap map) {
        AbelianGroupElement value=space.classOf(map); IntegralChainConeMap representative=space.representative(value);
        assertEquals(value,space.classOf(representative)); assertEquals(space.source(),representative.source()); assertEquals(space.target(),representative.target());
        assertTrue(IntegralConeHomotopySolver.areHomotopic(map,representative));
    }
    @Test public void all625PointMatricesRetainFourIndependentIntegralCoordinates() {
        RelativeSimplicialComplex p=points(2); IntegralConeMapSpace space=new IntegralConeMapSpace(carrier(p),carrier(p));
        assertEquals(AbelianGroupType.free(z(4)),space.homotopyType()); List<IntegralChainConeMap> basis=space.mapGenerators(); assertEquals(4,basis.size()); assertEquals(basis,space.representatives());
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            IntegralChainConeMap f=lift(new SimplicialChainMap(p,p,Collections.singletonList(m(new long[]{a,b},new long[]{c,d}))));
            assertEquals(new IntegerVector(z(a),z(b),z(c),z(d)),space.classOf(f).smithCoordinates()); assertEquals(f,space.representative(space.classOf(f)));
            IntegralChainConeMap sum=space.zero(); int[] coefficients={a,b,c,d}; for(int i=0;i<4;i++) sum=sum.add(basis.get(i).scale(z(coefficients[i]))); assertEquals(f,sum);
        }
    }
    @Test public void all81TwoTermSpacesMatchIndependentGcdGroupsAndPrimitiveMapLattices() {
        SimplicialChainMap id=SimplicialChainMap.identity(points(1));
        for(int n=-4;n<=4;n++) for(int k=-4;k<=4;k++) {
            IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(n),pointCone(k)); BigInteger gcd=z(n).gcd(z(k));
            if(gcd.signum()==0) { assertEquals(AbelianGroupType.free(z(2)),space.homotopyType()); assertEquals(2,space.mapGenerators().size()); }
            else {
                assertEquals(AbelianGroupType.cyclic(gcd),space.homotopyType()); assertEquals(1,space.mapGenerators().size());
                IntegralChainConeMap primitive=strict(space.source(),space.target(),id.scale(z(n).divide(gcd)),id.scale(z(k).divide(gcd)));
                assertEquals(gcd,space.classOf(primitive).order()); roundTrip(space,primitive);
                IntegralChainConeMap generator=space.mapGenerators().get(0); assertTrue(generator.equals(primitive) || generator.equals(primitive.negate()));
                assertEquals(gcd.equals(z(1))?0:1,space.representatives().size());
            }
        }
    }
    @Test public void pointConeIdentitiesAreKilledByHomotopiesMixingTheConeBlocks() {
        IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(1),pointCone(1)); IntegralChainConeMap id=IntegralChainConeMap.identity(space.source());
        assertEquals(AbelianGroupType.ZERO,space.homotopyType()); assertTrue(space.classOf(id).isZero()); assertEquals(Collections.singletonList(id),space.mapGenerators());
        IntegralHomology h=space.homology(); assertEquals(m(new long[]{-1,1}),h.outgoingBoundary()); assertEquals(m(new long[]{1},new long[]{1}),h.incomingBoundary());
        assertEquals(space.zero(),space.representative(space.classOf(id))); assertTrue(space.representatives().isEmpty());
        IntegralConeHomotopy contraction=IntegralConeHomotopySolver.between(space.zero(),id); assertEquals(m(new long[]{1}),contraction.chainMatrix(z(0)));
    }
    @Test public void all567ScalarComparisonsAgreeWithIndependentCongruencesAndHomotopySolving() {
        for(int n=-3;n<=3;n++) {
            IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(n),pointCone(n)); IntegralChainConeMap id=IntegralChainConeMap.identity(space.source());
            for(int a=-4;a<=4;a++) for(int b=-4;b<=4;b++) {
                IntegralChainConeMap f=id.scale(z(a)),g=id.scale(z(b)); boolean same=n==0?a==b:(a-b)%n==0;
                assertEquals(same,space.classOf(f).equals(space.classOf(g))); assertEquals(same,IntegralConeHomotopySolver.areHomotopic(f,g));
                assertEquals(space.classOf(f).add(space.classOf(g)),space.classOf(f.add(g)));
            }
        }
    }
    private static RelativeSimplicialComplex relativeEdge() { FiniteSimplicialComplex edge=pair(new int[]{0,1}).ambient(); return new RelativeSimplicialComplex(edge,edge.skeleton(0)); }
    private static IntegralChainConeMap cross(IntegralConeMapSpace space,int coefficient) {
        SimplicialChainMap zero=SimplicialChainMap.zero(space.source().source(),space.target().target());
        SimplicialChainHomotopy h=new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{-coefficient}),IntegerMatrix.zero(0,0)));
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(space.source(),space.target(),SimplicialChainMap.zero(space.source().source(),space.target().source()),SimplicialChainMap.zero(space.source().target(),space.target().target()),h));
    }
    @Test public void crossDegreeClassesDetectTorsionInvisibleToBothInducedIntegralMaps() {
        IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(2),cone(SimplicialChainMap.identity(relativeEdge()).scale(z(2))));
        assertEquals(AbelianGroupType.cyclic(z(2)),space.homotopyType()); IntegralChainConeMap odd=cross(space,1),zero=space.zero();
        for(int degree=0;degree<=2;degree++) { assertEquals(zero.homologyMap(z(degree)),odd.homologyMap(z(degree))); assertEquals(zero.cohomologyMap(z(degree)),odd.cohomologyMap(z(degree))); }
        for(int n=-5;n<=5;n++) { assertEquals(n%2==0,space.classOf(cross(space,n)).isZero()); roundTrip(space,cross(space,n)); }
        assertEquals(z(2),space.classOf(odd).order()); assertEquals(1,space.mapGenerators().size()); assertEquals(1,space.representatives().size());
    }
    @Test public void bothAdjacentWitnessDegreesContributeToTheGcdOfCrossRelations() {
        for(int a=-3;a<=3;a++) for(int b=-3;b<=3;b++) {
            IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(a),cone(SimplicialChainMap.identity(relativeEdge()).scale(z(b)))); BigInteger gcd=z(a).gcd(z(b));
            assertEquals(gcd.signum()==0?AbelianGroupType.Z:AbelianGroupType.cyclic(gcd),space.homotopyType());
            assertEquals(1,space.mapGenerators().size()); roundTrip(space,cross(space,1));
        }
    }
    @Test public void forbiddenBoundaryComponentsAreIntersectedBeforeTakingTheQuotient() {
        RelativeSimplicialComplex rp=pair(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
        FiniteSimplicialComplex triangle=pair(new int[]{0,1,2}).ambient(); RelativeSimplicialComplex disk=new RelativeSimplicialComplex(triangle,triangle.skeleton(1));
        BigInteger[][] top=new BigInteger[1][10]; Arrays.fill(top[0],z(0)); top[0][0]=z(1);
        SimplicialChainMap defining=new SimplicialChainMap(rp,disk,Arrays.asList(IntegerMatrix.zero(0,6),IntegerMatrix.zero(0,15),new IntegerMatrix(top)));
        IntegralConeMapSpace space=new IntegralConeMapSpace(carrier(disk),cone(defining));
        // Projecting D_3's first column onto the allowed target block would falsely make its unit a boundary.
        IntegerVector boundary=space.target().boundaryMatrix(z(3)).column(0); assertEquals(z(1),boundary.get(0)); assertFalse(boundary.equals(new IntegerVector(z(1),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0),z(0))));
        IntegralChainConeMap inclusion=strict(space.source(),space.target(),SimplicialChainMap.zero(points(0),rp),SimplicialChainMap.identity(disk));
        assertEquals(AbelianGroupType.Z,space.homotopyType()); assertEquals(IntegerMatrix.zero(1,0),space.homology().incomingBoundary());
        assertFalse(space.classOf(inclusion).isZero()); assertFalse(IntegralConeHomotopySolver.areHomotopic(space.zero(),inclusion)); roundTrip(space,inclusion);
    }
    private static IntegralChainConeMap shear(int a,int b,int h) {
        RelativeSimplicialComplex point=points(1),edge=relativeEdge(); SimplicialChainMap zero=SimplicialChainMap.zero(point,edge);
        return new IntegralChainConeMap(new IntegralChainConeMap.Data(cone(zero),cone(zero),SimplicialChainMap.identity(point).scale(z(a)),SimplicialChainMap.identity(edge).scale(z(b)),new SimplicialChainHomotopy(zero,zero,Arrays.asList(m(new long[]{h}),IntegerMatrix.zero(0,0)))));
    }
    @Test public void all125ShearsRetainOnlyThreeAllowedCoordinatesAndDecodeTheSquareSign() {
        IntegralConeMapSpace space=new IntegralConeMapSpace(shear(0,0,0).source(),shear(0,0,0).target()); assertEquals(AbelianGroupType.free(z(3)),space.homotopyType()); assertEquals(3,space.mapGenerators().size());
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int h=-2;h<=2;h++) {
            IntegralChainConeMap f=shear(a,b,h); assertEquals(new IntegerVector(z(b),z(-h),z(a)),space.classOf(f).smithCoordinates()); assertEquals(f,space.representative(space.classOf(f)));
        }
        assertEquals(IntegerMatrix.zero(0,3),space.homology().outgoingBoundary()); assertEquals(IntegerMatrix.zero(3,0),space.homology().incomingBoundary());
    }
    @Test public void nonadditiveRepresentativesPreserveClassesAndUnboundedCoefficients() {
        IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(6),pointCone(6)); IntegralChainConeMap id=IntegralChainConeMap.identity(space.source());
        AbelianGroupElement one=space.classOf(id); assertEquals(z(6),one.order()); assertFalse(space.representative(one).scale(z(6)).isZero()); assertTrue(space.representative(one.scale(z(6))).isZero());
        BigInteger huge=z(1).shiftLeft(1024).multiply(z(6)); assertTrue(space.classOf(id.scale(huge)).isZero()); assertEquals(one,space.classOf(id.scale(huge.add(z(1)))));
        assertEquals(one.scale(z(-5)),space.classOf(id.scale(z(-5))));
    }
    @Test public void all25RelativeIntervalContextsAgreeWithKnownFreeHomologyDegrees() {
        RelativeSimplicialComplex edge=pair(new int[]{0,1}); List<RelativeSimplicialComplex> pairs=new ArrayList<>();
        for(RelativeSimplicialComplex sub : Arrays.asList(points(0),points(1),pair(new int[]{1}),points(2),edge)) pairs.add(new RelativeSimplicialComplex(edge.ambient(),sub.ambient()));
        for(int a=0;a<5;a++) for(int b=0;b<5;b++) {
            IntegralConeMapSpace space=new IntegralConeMapSpace(carrier(pairs.get(a)),carrier(pairs.get(b))); boolean nonzero=(a==0 && b==0)||(a==3 && b==3);
            assertEquals(nonzero?AbelianGroupType.Z:AbelianGroupType.ZERO,space.homotopyType());
            for(IntegralChainConeMap generator : space.mapGenerators()) roundTrip(space,generator);
        }
    }
    @Test public void emptyAndFilteredContextsRetainFormalSlotsAndRejectDifferentDefiningMaps() {
        IntegralChainMappingCone empty=carrier(points(0)),filtered=cone(SimplicialChainMap.identity(RelativeSimplicialComplex.diagonal(pair(new int[]{0,1,2}).ambient())));
        for(IntegralChainMappingCone s : Arrays.asList(empty,filtered)) for(IntegralChainMappingCone t : Arrays.asList(empty,filtered)) {
            IntegralConeMapSpace space=new IntegralConeMapSpace(s,t); assertEquals(AbelianGroupType.ZERO,space.homotopyType()); assertTrue(space.mapGenerators().isEmpty()); assertTrue(space.representatives().isEmpty()); roundTrip(space,space.zero());
            assertEquals(Math.max(s.dimension(),t.dimension())+1,space.zero().chainMatrices().size());
        }
        IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(2),pointCone(2));
        for(IntegralChainMappingCone other : Arrays.asList(pointCone(-2),cone(SimplicialChainMap.identity(pair(new int[]{9})).scale(z(2))))) failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> space.classOf(IntegralChainConeMap.identity(other)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> space.representative(PresentedAbelianGroup.fromType(AbelianGroupType.cyclic(z(3))).zero()));
        assertEquals(space,new IntegralConeMapSpace(space.source(),space.target())); assertEquals(space.hashCode(),new IntegralConeMapSpace(space.source(),space.target()).hashCode());
        assertThrows(UnsupportedOperationException.class,space.mapGenerators()::clear); assertThrows(UnsupportedOperationException.class,space.representatives()::clear);
    }
    private static RelativeSimplicialComplex relativeStar(int n) {
        int[][] edges=new int[n][2]; for(int i=0;i<n;i++) { edges[i][0]=0; edges[i][1]=i+1; } FiniteSimplicialComplex graph=pair(edges).ambient(); return new RelativeSimplicialComplex(graph,graph.skeleton(0));
    }
    @Test public void allThreeFullHomRanksHaveSeparateBoundsEvenForForbiddenCoordinates() {
        for(IntegralConeMapSpace small : Arrays.asList(new IntegralConeMapSpace(carrier(points(16)),carrier(relativeStar(16))),new IntegralConeMapSpace(carrier(relativeStar(16)),carrier(points(16))))) assertEquals(AbelianGroupType.ZERO,small.homotopyType());
        for(IntegralConeMapSpace large : Arrays.asList(new IntegralConeMapSpace(carrier(points(17)),carrier(relativeStar(16))),new IntegralConeMapSpace(carrier(relativeStar(16)),carrier(points(17))),new IntegralConeMapSpace(carrier(points(17)),carrier(points(17))),new IntegralConeMapSpace(carrier(relativeStar(17)),cone(SimplicialChainMap.zero(points(16),points(0)))))) {
            assertEquals(large.source(),large.zero().source());
            for(Runnable f : Arrays.<Runnable>asList(large::homology,large::homotopyGroup,large::mapGenerators,large::representatives)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("256 total full Hom"));
        }
    }
    @Test public void denseWorkExhaustionDoesNotReturnATrivialGroupOrPartialBasis() {
        IntegralConeMapSpace space=new IntegralConeMapSpace(carrier(points(16)),carrier(points(16)));
        for(Runnable f : Arrays.<Runnable>asList(space::homotopyType,space::mapGenerators,space::representatives,() -> space.classOf(space.zero()))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,f).getMessage().contains("5000000"));
    }
    @Test public void wholeRepresentativeListsShareOneBudgetAcrossSuccessfulIndividualLifts() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<9;i++) for(int j=i+1;j<9;j++) if(facets.size()<29) facets.add(FiniteSet.of(i,j));
        IntegralConeMapSpace space=new IntegralConeMapSpace(carrier(pair(new int[]{0,1},new int[]{0,2},new int[]{1,2})),carrier(RelativeSimplicialComplex.absolute(new FiniteSimplicialComplex(facets))));
        assertEquals(AbelianGroupType.free(z(22)),space.homotopyType()); assertFalse(space.mapGenerators().isEmpty());
        for(AbelianGroupElement element : space.homotopyGroup().smithGenerators()) assertEquals(space.source(),space.representative(element).source());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,space::representatives).getMessage().contains("5000000"));
    }
    @Test public void combinedConeRankBoundsRemainDistinctFromTheFullHomBounds() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<24 && facets.size()<256;i++) for(int j=i+1;j<24 && facets.size()<256;j++) facets.add(FiniteSet.of(i,j));
        FiniteSimplicialComplex graph=new FiniteSimplicialComplex(facets); RelativeSimplicialComplex relative=new RelativeSimplicialComplex(graph,graph.skeleton(0));
        IntegralConeMapSpace space=new IntegralConeMapSpace(cone(SimplicialChainMap.zero(points(1),relative)),carrier(points(0)));
        assertEquals(space.source(),space.zero().source());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,space::homology).getMessage().contains("256 total target"));
    }
    @Test public void nativeClassAndRepresentativeOperationsUseActualWrappersAndSerializedFlatFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegralConeMapSpace space=new IntegralConeMapSpace(pointCone(6),pointCone(6)); IntegralChainConeMap id=IntegralChainConeMap.identity(space.source());
        IAlgebraItem<IntegralConeMapSpace> item=math.chainCones.algebra().buildAlgebraItem(space.source()).performCustomResultOperation("ConeMapSpace.from-cones",space.target());
        assertSame(math.coneMapSpaces.algebra(),item.getAlgebra()); assertSame(math.coneMapSpaces.algebra(),math.mathTool.getAlgebra("ConeMapSpace")); assertEquals(space,item.perform().getResult());
        assertSame(math.abelianGroupElements.algebra(),item.performUnsafeOperation("class-of",id).getAlgebra()); assertSame(math.chainConeMaps.algebra(),item.performUnsafeOperation("representative",space.classOf(id)).getAlgebra());
        for(IAlgebraItem<?> map : item.performAlgebraFlatTransfer("map-generators")) assertSame(math.chainConeMaps.algebra(),map.getAlgebra());
        List<IAlgebraFlow<?>> flows=Arrays.asList(
                math.flow(math.chainCones,Collections.singletonList(space.source())).performCustomResultOperation("ConeMapSpace.from-cones",space.target()),
                math.flow(math.coneMapSpaces,Collections.singletonList(space)).<AbelianGroupElement,IntegralChainConeMap>performAlgebraUnsafe("class-of",id).performAlgebraTransfer("order"),
                math.flow(math.coneMapSpaces,Collections.singletonList(space)).<IntegralChainConeMap>performFlatAlgebraTransfer("representatives").performAlgebraTransfer("is-zero"),
                math.flow(math.coneMapSpaces,Collections.singletonList(space)).<IntegralChainConeMap>performFlatAlgebraTransfer("map-generators").performAlgebraUnsafe("chain-matrix",z(1)));
        for(IAlgebraFlow<?> original : flows) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("6"),flows.get(1).collect()); assertEquals(Collections.singletonList("false"),flows.get(2).collect()); assertEquals(Collections.singletonList("ZMatrix(1x1)[[1]]"),flows.get(3).collect());
    }
}
