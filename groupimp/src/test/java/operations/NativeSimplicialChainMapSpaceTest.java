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

public class NativeSimplicialChainMapSpaceTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> faces=new ArrayList<>(); for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int v : facet) labels.add(v); faces.add(new FiniteSet<>(labels)); } return new FiniteSimplicialComplex(faces);
    }
    private static FiniteSimplicialComplex points(int n) { int[][] f=new int[n][1]; for(int i=0;i<n;i++) f[i][0]=i; return complex(f); }
    private static RelativeSimplicialComplex abs(FiniteSimplicialComplex c) { return RelativeSimplicialComplex.absolute(c); }
    private static RelativeSimplicialComplex point() { return abs(points(1)); }
    private static RelativeSimplicialComplex edge() { return abs(complex(new int[]{0,1})); }
    private static RelativeSimplicialComplex circle() { return abs(complex(new int[]{0,1},new int[]{0,2},new int[]{1,2})); }
    private static IntegerMatrix matrix(long[]... entries) {
        BigInteger[][] values=new BigInteger[entries.length][]; for(int r=0;r<entries.length;r++) { values[r]=new BigInteger[entries[r].length]; for(int c=0;c<entries[r].length;c++) values[r][c]=z(entries[r][c]); } return new IntegerMatrix(values);
    }
    private static MathFailure failure(MathFailure.Kind kind,Runnable body) { MathFailure e=assertThrows(MathFailure.class,body::run); assertEquals(kind,e.kind()); return e; }
    private static void roundTrip(SimplicialChainMapSpace space,SimplicialChainMap map) {
        AbelianGroupElement value=space.classOf(map); SimplicialChainMap representative=space.representative(value);
        assertEquals(value,space.classOf(representative)); assertTrue(SimplicialChainHomotopySolver.areHomotopic(map,representative));
    }
    @Test public void all625TwoPointMapsRetainTheirFourIndependentIntegralCoordinates() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(points(2)),abs(points(2)));
        assertEquals(AbelianGroupType.free(z(4)),space.homotopyType()); List<SimplicialChainMap> basis=space.mapGenerators(); assertEquals(4,basis.size()); assertEquals(basis,space.representatives());
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) for(int c=-2;c<=2;c++) for(int d=-2;d<=2;d++) {
            SimplicialChainMap f=new SimplicialChainMap(space.source(),space.target(),Collections.singletonList(matrix(new long[]{a,b},new long[]{c,d})));
            SimplicialChainMap sum=space.zero(); long[] entries={a,b,c,d}; for(int i=0;i<4;i++) sum=sum.add(basis.get(i).scale(z(entries[i])));
            assertEquals(f,sum); assertEquals(new IntegerVector(z(a),z(b),z(c),z(d)),space.classOf(f).smithCoordinates()); assertEquals(f,space.representative(space.classOf(f)));
        }
    }
    @Test public void all289IntervalComparisonsHaveEqualClassesExactlyForEqualAugmentations() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(edge(),edge()); List<SimplicialChainMap> maps=new ArrayList<>(); List<BigInteger> augmentations=new ArrayList<>();
        for(int a=-1;a<=1;a++) for(int b=-1;b<=1;b++) for(int c=-1;c<=1;c++) for(int d=-1;d<=1;d++) for(int e=-1;e<=1;e++) if(b-a==-e && d-c==e) {
            maps.add(new SimplicialChainMap(edge(),edge(),Arrays.asList(matrix(new long[]{a,b},new long[]{c,d}),matrix(new long[]{e})))); augmentations.add(z(a+c));
        }
        assertEquals(17,maps.size()); assertEquals(AbelianGroupType.Z,space.homotopyType()); assertEquals(3,space.mapGenerators().size()); assertEquals(1,space.representatives().size());
        // Every interval map has the unique integer parameters (F0(0,0),F0(1,0),F1(0,0)).
        BigInteger[][] parameters=new BigInteger[3][3]; List<SimplicialChainMap> basis=space.mapGenerators();
        for(int c=0;c<3;c++) { parameters[0][c]=basis.get(c).chainMatrix(z(0)).get(0,0); parameters[1][c]=basis.get(c).chainMatrix(z(0)).get(1,0); parameters[2][c]=basis.get(c).chainMatrix(z(1)).get(0,0); }
        BigInteger determinant=parameters[0][0].multiply(parameters[1][1].multiply(parameters[2][2]).subtract(parameters[1][2].multiply(parameters[2][1])))
                .subtract(parameters[0][1].multiply(parameters[1][0].multiply(parameters[2][2]).subtract(parameters[1][2].multiply(parameters[2][0]))))
                .add(parameters[0][2].multiply(parameters[1][0].multiply(parameters[2][1]).subtract(parameters[1][1].multiply(parameters[2][0]))));
        assertEquals(BigInteger.ONE,determinant.abs());
        List<AbelianGroupElement> classes=new ArrayList<>(); for(SimplicialChainMap f : maps) { classes.add(space.classOf(f)); roundTrip(space,f); }
        for(int i=0;i<17;i++) for(int j=0;j<17;j++) { assertEquals(augmentations.get(i).equals(augmentations.get(j)),classes.get(i).equals(classes.get(j))); assertEquals(classes.get(i).add(classes.get(j)),space.classOf(maps.get(i).add(maps.get(j)))); }
    }
    private static FiniteSimplicialComplex intervalSubcomplex(int mask) {
        if(mask==7) return complex(new int[]{0,1}); if(mask==3) return points(2); if(mask==1) return points(1); if(mask==2) return complex(new int[]{1}); return complex();
    }
    @Test public void all25RelativeIntervalSpacesRetainOnlyTheirCommonNonzeroHomologyDegree() {
        int[] masks={0,1,2,3,7};
        for(int a : masks) for(int b : masks) {
            SimplicialChainMapSpace space=new SimplicialChainMapSpace(new RelativeSimplicialComplex(edge().ambient(),intervalSubcomplex(a)),new RelativeSimplicialComplex(edge().ambient(),intervalSubcomplex(b)));
            boolean nonzero=(a==0 && b==0)||(a==3 && b==3); assertEquals(nonzero?AbelianGroupType.Z:AbelianGroupType.ZERO,space.homotopyType()); assertEquals(nonzero?1:0,space.representatives().size());
            for(SimplicialChainMap f : space.mapGenerators()) roundTrip(space,f);
        }
    }
    @Test public void circleMapClassesHaveIndependentAugmentationAndWindingCoordinates() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(circle(),circle()); assertEquals(AbelianGroupType.free(z(2)),space.homotopyType()); assertEquals(10,space.mapGenerators().size()); assertEquals(2,space.representatives().size());
        Set<AbelianGroupElement> classes=new HashSet<>();
        for(int n=-4;n<=4;n++) for(int m=-4;m<=4;m++) {
            SimplicialChainMap f=new SimplicialChainMap(circle(),circle(),Arrays.asList(IntegerMatrix.identity(3).scale(z(n)),matrix(new long[]{m,0,0},new long[]{n-m,n,0},new long[]{m-n,0,n})));
            assertTrue(classes.add(space.classOf(f))); roundTrip(space,f);
            assertEquals(space.classOf(f).scale(z(-3)),space.classOf(f.scale(z(-3))));
        }
        assertEquals(81,classes.size());
    }
    private static FiniteSimplicialComplex projectivePlane() { return complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5}); }
    private static RelativeSimplicialComplex relativeDisk() { FiniteSimplicialComplex disk=complex(new int[]{0,1,2}); return new RelativeSimplicialComplex(disk,disk.skeleton(1)); }
    private static SimplicialChainMap topMap(SimplicialChainMapSpace space,BigInteger a,BigInteger b) {
        BigInteger[][] top=new BigInteger[1][space.source().simplexCount(z(2))]; Arrays.fill(top[0],BigInteger.ZERO); top[0][0]=a; top[0][1]=b;
        return new SimplicialChainMap(space.source(),space.target(),Arrays.asList(IntegerMatrix.zero(0,space.source().simplexCount(z(0))),IntegerMatrix.zero(0,space.source().simplexCount(z(1))),new IntegerMatrix(top)));
    }
    @Test public void projectivePlaneClassesDetectParityTorsionInvisibleToInducedHomologyMaps() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(projectivePlane()),relativeDisk()); assertEquals(AbelianGroupType.cyclic(z(2)),space.homotopyType());
        assertEquals(10,space.mapGenerators().size()); assertEquals(1,space.representatives().size()); SimplicialChainMap odd=topMap(space,z(1),z(0));
        for(int k=0;k<3;k++) assertEquals(space.zero().homologyMap(z(k)),odd.homologyMap(z(k)));
        for(int a=-4;a<=4;a++) for(int b=-4;b<=4;b++) {
            SimplicialChainMap f=topMap(space,z(a),z(b)); AbelianGroupElement value=space.classOf(f); assertEquals((a+b)%2==0,value.isZero()); assertEquals(z((a+b)%2==0?1:2),value.order()); roundTrip(space,f);
        }
        BigInteger huge=BigInteger.ONE.shiftLeft(1024); assertTrue(space.classOf(topMap(space,huge,z(0))).isZero()); assertFalse(space.classOf(topMap(space,huge.add(BigInteger.ONE),z(0))).isZero());
    }
    @Test public void smithRepresentativesRetainMixedFreeAndTorsionClassesWithoutAnAdditiveSection() {
        FiniteSimplicialComplex sphere=complex(new int[]{6,7,8},new int[]{6,7,9},new int[]{6,8,9},new int[]{7,8,9});
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(projectivePlane().union(sphere)),relativeDisk()); assertEquals(new AbelianGroupType(z(1),Collections.singletonList(z(2))),space.homotopyType());
        List<SimplicialChainMap> representatives=space.representatives(); assertEquals(2,representatives.size()); AbelianGroupElement torsion=space.classOf(representatives.get(0)),free=space.classOf(representatives.get(1));
        assertEquals(z(2),torsion.order()); assertFalse(free.isTorsion());
        SimplicialChainMap twice=representatives.get(0).scale(z(2)); assertFalse(twice.isZero()); assertTrue(space.classOf(twice).isZero()); assertTrue(space.representative(torsion.scale(z(2))).isZero());
        for(int a=-2;a<=2;a++) for(int b=-2;b<=2;b++) { AbelianGroupElement value=torsion.scale(z(a)).add(free.scale(z(b))); assertEquals(value,space.classOf(space.representative(value))); }
    }
    @Test public void homDifferentialsUseOppositeSourceSignsAndRetainDegreeRowColumnOrdering() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(edge(),edge()); IntegralHomology h=space.homology();
        // Flatten [F0(0,0),F0(0,1),F0(1,0),F0(1,1),F1(0,0)] and [H0(0,0),H0(0,1)].
        assertEquals(matrix(new long[]{1,-1,0,0,-1},new long[]{0,0,1,-1,1}),h.outgoingBoundary());
        assertEquals(matrix(new long[]{-1,0},new long[]{0,-1},new long[]{1,0},new long[]{0,1},new long[]{-1,1}),h.incomingBoundary());
        assertEquals(IntegerMatrix.zero(2,2),h.outgoingBoundary().multiply(h.incomingBoundary()));
        assertEquals(h.incomingBoundary(),h.cycleMatrix().multiply(h.boundaryCoordinates()));
        assertEquals(h.group(),space.homotopyGroup());
    }
    @Test public void fullEndpointPairsAndRetainedPresentationsAreRequired() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(edge(),edge()),same=new SimplicialChainMapSpace(edge(),edge()); assertEquals(space,same); assertEquals(space.hashCode(),same.hashCode());
        RelativeSimplicialComplex based0=new RelativeSimplicialComplex(edge().ambient(),points(1)),based1=new RelativeSimplicialComplex(edge().ambient(),complex(new int[]{1}));
        SimplicialChainMapSpace based=new SimplicialChainMapSpace(based0,based0); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> based.classOf(SimplicialChainMap.identity(based1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> space.classOf(SimplicialChainMap.zero(edge(),abs(complex(new int[]{4,5})))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> space.representative(PresentedAbelianGroup.fromType(AbelianGroupType.cyclic(z(3))).zero()));
        assertNotEquals(space,new SimplicialChainMapSpace(edge(),point())); assertThrows(UnsupportedOperationException.class,space.mapGenerators()::clear); assertThrows(UnsupportedOperationException.class,space.representatives()::clear);
    }
    @Test public void emptyAndDiagonalPairsKeepGeometricContextsWithZeroHomotopyGroups() {
        RelativeSimplicialComplex empty=abs(complex());
        for(RelativeSimplicialComplex s : Arrays.asList(empty,point())) for(RelativeSimplicialComplex t : Arrays.asList(empty,point())) {
            SimplicialChainMapSpace space=new SimplicialChainMapSpace(s,t); boolean nonzero=s.equals(point()) && t.equals(point()); assertEquals(nonzero?AbelianGroupType.Z:AbelianGroupType.ZERO,space.homotopyType());
            assertEquals(nonzero?1:0,space.mapGenerators().size()); roundTrip(space,space.zero());
        }
        RelativeSimplicialComplex diagonal=RelativeSimplicialComplex.diagonal(abs(points(4096)).ambient()); SimplicialChainMapSpace space=new SimplicialChainMapSpace(diagonal,diagonal);
        assertEquals(AbelianGroupType.ZERO,space.homotopyType()); assertEquals(Collections.singletonList(IntegerMatrix.zero(0,0)),space.zero().chainMatrices());
    }
    private static RelativeSimplicialComplex relativeStar(int n) {
        int[][] edges=new int[n][2]; for(int i=0;i<n;i++) { edges[i][0]=0; edges[i][1]=i+1; } return new RelativeSimplicialComplex(complex(edges),points(n+1));
    }
    @Test public void allThreeHomRanksHaveIndependent256BoundsAfterQuotientFiltering() {
        for(SimplicialChainMapSpace small : Arrays.asList(new SimplicialChainMapSpace(abs(points(16)),relativeStar(16)),new SimplicialChainMapSpace(relativeStar(16),abs(points(16))))) assertEquals(AbelianGroupType.ZERO,small.homotopyType());
        for(SimplicialChainMapSpace large : Arrays.asList(new SimplicialChainMapSpace(abs(points(17)),relativeStar(16)),new SimplicialChainMapSpace(relativeStar(16),abs(points(17))),new SimplicialChainMapSpace(abs(points(17)),abs(points(17))))) {
            assertEquals(large.source(),large.zero().source()); assertEquals(large.target(),large.zero().target());
            for(Runnable operation : Arrays.<Runnable>asList(large::homology,large::homotopyGroup,large::mapGenerators,large::representatives)) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,operation).getMessage().contains("256 total Hom coefficients"));
        }
        SimplicialChainMapSpace big=new SimplicialChainMapSpace(abs(points(257)),point()); assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,big::homology).getMessage().contains("256"));
    }
    @Test public void boundedDenseWorkExhaustionIsNotAnEmptyOrTrivialMapSpace() {
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(points(16)),abs(points(16)));
        for(Runnable operation : Arrays.<Runnable>asList(space::homotopyType,space::mapGenerators,space::representatives,() -> space.classOf(space.zero()))) assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,operation).getMessage().contains("5000000"));
    }
    @Test public void representativeListsShareTheirHomologyReductionAndValidationBudget() {
        List<FiniteSet<Integer>> facets=new ArrayList<>(); for(int i=0;i<9;i++) for(int j=i+1;j<9;j++) if(facets.size()<29) facets.add(FiniteSet.of(i,j));
        SimplicialChainMapSpace space=new SimplicialChainMapSpace(circle(),abs(new FiniteSimplicialComplex(facets)));
        assertEquals(AbelianGroupType.free(z(22)),space.homotopyType()); assertFalse(space.mapGenerators().isEmpty());
        assertTrue(failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,space::representatives).getMessage().contains("5000000"));
    }
    @Test public void nativeConstructionClassesRepresentativesAndFlatFlowsSerializeWithActualWrappers() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); SimplicialChainMapSpace space=new SimplicialChainMapSpace(abs(projectivePlane()),relativeDisk()); SimplicialChainMap odd=topMap(space,z(1),z(0));
        IAlgebraItem<SimplicialChainMapSpace> item=math.relativeComplexes.algebra().buildAlgebraItem(space.source()).performCustomResultOperation("ChainMapSpace.from-pairs",space.target()); assertSame(math.chainMapSpaces.algebra(),item.getAlgebra()); assertEquals(space,item.perform().getResult());
        assertSame(math.abelianGroupElements.algebra(),item.performUnsafeOperation("class-of",odd).getAlgebra()); assertSame(math.chainMaps.algebra(),item.performUnsafeOperation("representative",space.classOf(odd)).getAlgebra());
        IAlgebraFlow<BigInteger> orders=math.flow(math.chainMapSpaces,Collections.singletonList(space)).<AbelianGroupElement,SimplicialChainMap>performAlgebraUnsafe("class-of",odd).performAlgebraTransfer("order");
        IAlgebraFlow<Boolean> representatives=math.flow(math.chainMapSpaces,Collections.singletonList(space)).<SimplicialChainMap>performFlatAlgebraTransfer("representatives").performAlgebraTransfer("is-zero");
        IAlgebraFlow<IntegerMatrix> basis=math.flow(math.chainMapSpaces,Collections.singletonList(space)).<SimplicialChainMap>performFlatAlgebraTransfer("map-generators").<IntegerMatrix,BigInteger>performAlgebraUnsafe("chain-matrix",z(2));
        for(IAlgebraFlow<?> original : Arrays.asList(orders,representatives,basis)) {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(original); }
            IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
            assertEquals(original.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        }
        assertEquals(Collections.singletonList("2"),orders.collect()); assertEquals(Collections.singletonList("false"),representatives.collect()); assertEquals(10,basis.collect().size());
    }
}
