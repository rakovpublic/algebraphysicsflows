package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.structures.AbelianGroupType;
import mathematics.topology.FiniteSimplicialComplex;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeIntegralHomologyTest {
    private static BigInteger z(long value) { return BigInteger.valueOf(value); }
    private static BigInteger[][] matrix(long[]... rows) {
        BigInteger[][] result=new BigInteger[rows.length][];
        for(int i=0;i<rows.length;i++) { result[i]=new BigInteger[rows[i].length]; for(int j=0;j<rows[i].length;j++) result[i][j]=z(rows[i][j]); }
        return result;
    }
    private static FiniteSimplicialComplex complex(int[]... facets) {
        List<FiniteSet<Integer>> result=new ArrayList<>();
        for(int[] facet : facets) { List<Integer> labels=new ArrayList<>(); for(int vertex : facet) labels.add(vertex); result.add(new FiniteSet<>(labels)); }
        return new FiniteSimplicialComplex(result);
    }
    private static AbelianGroupType free(long rank) { return AbelianGroupType.free(z(rank)); }
    private static List<BigInteger> smith(BigInteger[][] matrix) { return IntegerSmithNormalForm.invariantFactors(matrix); }
    private static FiniteSimplicialComplex projectivePlane() {
        return complex(new int[]{0,1,2},new int[]{0,1,3},new int[]{0,2,4},new int[]{0,3,5},new int[]{0,4,5},
                new int[]{1,2,5},new int[]{1,3,4},new int[]{1,4,5},new int[]{2,3,4},new int[]{2,3,5});
    }
    /** Polygon construction described by Sage's MooreSpace documentation, for q >= 3. */
    private static FiniteSimplicialComplex moore(int q) {
        List<int[]> facets=new ArrayList<>();
        for(int i=0;i<q;i++) {
            int a=10+i,b=100+i,next=10+(i+1)%q;
            facets.add(new int[]{1,2,a}); facets.add(new int[]{2,3,a}); facets.add(new int[]{3,1,b});
            facets.add(new int[]{3,b,a}); facets.add(new int[]{1,b,next}); facets.add(new int[]{b,a,next});
        }
        for(int i=1;i<q-1;i++) facets.add(new int[]{10,10+i,10+i+1});
        return complex(facets.toArray(new int[0][]));
    }
    private static FiniteSimplicialComplex suspend(FiniteSimplicialComplex source) {
        List<FiniteSet<Integer>> facets=new ArrayList<>();
        for(int degree=0;degree<=source.dimension();degree++) for(FiniteSet<Integer> face : source.simplices(degree)) {
            facets.add(face.union(FiniteSet.of(-1000))); facets.add(face.union(FiniteSet.of(-1001)));
        }
        return new FiniteSimplicialComplex(facets);
    }
    private static void limit(Runnable action) { assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,action::run).kind()); }

    @Test public void allSmallTwoByTwoSmithFormsMatchGcdAndDeterminantDivisors() {
        for(int code=0;code<625;code++) {
            int digits=code; long[] entries=new long[4]; BigInteger gcd=BigInteger.ZERO;
            for(int i=0;i<4;i++) { entries[i]=digits%5-2; digits/=5; gcd=gcd.gcd(z(entries[i])); }
            BigInteger determinant=z(entries[0]*entries[3]-entries[1]*entries[2]).abs();
            List<BigInteger> expected=new ArrayList<>();
            if(gcd.signum()>0) expected.add(gcd); if(determinant.signum()>0) expected.add(determinant.divide(gcd));
            assertEquals(expected,smith(matrix(new long[]{entries[0],entries[1]},new long[]{entries[2],entries[3]})));
        }
        assertEquals(Arrays.asList(z(1),z(6)),smith(matrix(new long[]{2,0},new long[]{0,3})));
        assertEquals(Arrays.asList(z(2),z(30)),smith(matrix(new long[]{-6,0,0},new long[]{0,10,0})));
    }
    @Test public void allBinaryThreeByThreeSmithFormsMatchIndependentMinorGcds() {
        for(int code=0;code<512;code++) {
            int bits=code; long[][] a=new long[3][3]; BigInteger minors1=BigInteger.ZERO,minors2=BigInteger.ZERO;
            for(int r=0;r<3;r++) for(int c=0;c<3;c++) { a[r][c]=bits&1; bits>>=1; minors1=minors1.gcd(z(a[r][c])); }
            for(int r=0;r<3;r++) for(int s=r+1;s<3;s++) for(int c=0;c<3;c++) for(int d=c+1;d<3;d++)
                minors2=minors2.gcd(z(a[r][c]*a[s][d]-a[r][d]*a[s][c]));
            long determinant=a[0][0]*(a[1][1]*a[2][2]-a[1][2]*a[2][1])-a[0][1]*(a[1][0]*a[2][2]-a[1][2]*a[2][0])+a[0][2]*(a[1][0]*a[2][1]-a[1][1]*a[2][0]);
            List<BigInteger> expected=new ArrayList<>();
            if(minors1.signum()>0) expected.add(minors1); if(minors2.signum()>0) expected.add(minors2.divide(minors1));
            if(determinant!=0) expected.add(z(determinant).abs().divide(minors2));
            assertEquals(expected,smith(matrix(a)));
        }
    }
    @Test public void smithHandlesEmptyRectangularAndLargeExactMatricesWithoutMutation() {
        assertTrue(smith(new BigInteger[0][3]).isEmpty()); assertTrue(smith(new BigInteger[4][0]).isEmpty());
        assertEquals(Collections.singletonList(z(6)),smith(matrix(new long[]{12,-18,24})));
        BigInteger huge=BigInteger.TEN.pow(100),next=huge.add(BigInteger.ONE);
        BigInteger[][] input={{huge,BigInteger.ZERO},{BigInteger.ZERO,next}};
        assertEquals(Arrays.asList(BigInteger.ONE,huge.multiply(next)),smith(input)); assertEquals(huge,input[0][0]); assertEquals(BigInteger.ZERO,input[0][1]);
        assertThrows(UnsupportedOperationException.class,() -> smith(input).clear());
        assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,() -> smith(new BigInteger[][]{{BigInteger.ONE},{}})).kind());
    }
    @Test public void projectivePlaneDistinguishesIntegralRationalAndModTwoHomology() {
        FiniteSimplicialComplex plane=projectivePlane();
        assertEquals(Arrays.asList(AbelianGroupType.Z,AbelianGroupType.cyclic(z(2)),AbelianGroupType.ZERO),plane.integralHomologyGroups());
        assertEquals(1,plane.bettiNumber(0)); assertEquals(1,plane.bettiNumber(1)); assertEquals(1,plane.bettiNumber(2));
        assertEquals(BigInteger.ZERO,plane.integralHomology(z(1)).freeRank()); assertEquals(BigInteger.ZERO,plane.integralHomology(z(2)).freeRank());
        List<BigInteger> invariants=plane.integralBoundaryInvariants(z(2));
        assertEquals(10,invariants.size()); assertEquals(z(2),invariants.get(9));
        for(int i=0;i<9;i++) assertEquals(BigInteger.ONE,invariants.get(i));
        assertEquals(BigInteger.ONE,plane.eulerCharacteristic());
    }
    @Test public void mooreSpacesDetectOddCompositeAndSuspendedTorsion() {
        for(int q=3;q<=10;q++) {
            FiniteSimplicialComplex space=moore(q);
            assertEquals(Arrays.asList(AbelianGroupType.Z,AbelianGroupType.cyclic(z(q)),AbelianGroupType.ZERO),space.integralHomologyGroups());
            assertEquals(q%2==0?1:0,space.bettiNumber(1)); assertEquals(q%2==0?1:0,space.bettiNumber(2));
            assertEquals(BigInteger.ONE,space.eulerCharacteristic());
        }
        assertEquals(Arrays.asList(AbelianGroupType.Z,AbelianGroupType.ZERO,AbelianGroupType.cyclic(z(4)),AbelianGroupType.ZERO),suspend(moore(4)).integralHomologyGroups());
        assertEquals(AbelianGroupType.cyclic(z(2)),suspend(projectivePlane()).integralHomology(z(2)));
    }
    @Test public void spheresDisksAndPeriodicGridTorusHaveKnownFreeHomology() {
        for(int n=1;n<=4;n++) {
            List<int[]> facets=new ArrayList<>();
            for(int missing=0;missing<n+2;missing++) {
                int[] face=new int[n+1]; int index=0;
                for(int i=0;i<n+2;i++) if(i!=missing) face[index++]=i; facets.add(face);
            }
            FiniteSimplicialComplex sphere=complex(facets.toArray(new int[0][]));
            for(int degree=0;degree<=n;degree++) assertEquals(degree==0 || degree==n?AbelianGroupType.Z:AbelianGroupType.ZERO,sphere.integralHomology(z(degree)));
            int[] full=new int[n+2]; for(int i=0;i<full.length;i++) full[i]=i;
            FiniteSimplicialComplex disk=complex(full);
            for(int degree=0;degree<=n+1;degree++) assertEquals(degree==0?AbelianGroupType.Z:AbelianGroupType.ZERO,disk.integralHomology(z(degree)));
        }
        List<int[]> triangles=new ArrayList<>();
        for(int i=0;i<3;i++) for(int j=0;j<3;j++) {
            int a=3*i+j,b=3*((i+1)%3)+j,c=3*((i+1)%3)+(j+1)%3,d=3*i+(j+1)%3;
            triangles.add(new int[]{a,b,c}); triangles.add(new int[]{a,c,d});
        }
        assertEquals(Arrays.asList(AbelianGroupType.Z,free(2),AbelianGroupType.Z),complex(triangles.toArray(new int[0][])).integralHomologyGroups());
    }
    @Test public void disjointUnionRelabellingAndVertexOrderRetainGroupTypes() {
        FiniteSimplicialComplex first=moore(3),second=moore(4); List<FiniteSet<Integer>> renamed=new ArrayList<>();
        for(FiniteSet<Integer> face : second.simplices(2)) {
            List<Integer> vertices=new ArrayList<>(); for(int vertex : face.members()) vertices.add(10000-vertex); Collections.reverse(vertices);
            renamed.add(new FiniteSet<>(vertices));
        }
        FiniteSimplicialComplex copy=new FiniteSimplicialComplex(renamed);
        assertEquals(second.integralHomologyGroups(),copy.integralHomologyGroups());
        assertEquals(Arrays.asList(free(2),AbelianGroupType.cyclic(z(12)),AbelianGroupType.ZERO),first.union(copy).integralHomologyGroups());
        List<FiniteSet<Integer>> mixed=new ArrayList<>(projectivePlane().simplices(2)); mixed.add(FiniteSet.of(100,101)); mixed.add(FiniteSet.of(101,102)); mixed.add(FiniteSet.of(100,102));
        assertEquals(new AbelianGroupType(BigInteger.ONE,Collections.singletonList(z(2))),new FiniteSimplicialComplex(mixed).integralHomology(z(1)));
    }
    @Test public void allFourVertexGraphsMatchIndependentComponentAndCycleCountsOverZ() {
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int mask=0;mask<64;mask++) {
            List<int[]> facets=new ArrayList<>(); for(int i=0;i<4;i++) facets.add(new int[]{i});
            int[] parent={0,1,2,3}; int edgeCount=0;
            for(int i=0;i<6;i++) if((mask&(1<<i))!=0) {
                int a=edges[i][0],b=edges[i][1]; facets.add(edges[i]); edgeCount++;
                while(parent[a]!=a) a=parent[a]; while(parent[b]!=b) b=parent[b]; parent[a]=b;
            }
            int components=0; for(int i=0;i<4;i++) if(parent[i]==i) components++;
            FiniteSimplicialComplex graph=complex(facets.toArray(new int[0][]));
            assertEquals(free(components),graph.integralHomology(z(0))); assertEquals(free(edgeCount-4+components),graph.integralHomology(z(1)));
            assertEquals(z(4-edgeCount),graph.eulerCharacteristic());
        }
    }
    @Test public void emptyDegreesAndSharedResourceExhaustionAreExplicit() {
        FiniteSimplicialComplex empty=complex();
        assertTrue(empty.integralHomologyGroups().isEmpty()); assertEquals(AbelianGroupType.ZERO,empty.integralHomology(z(0)));
        assertEquals(AbelianGroupType.ZERO,projectivePlane().integralHomology(BigInteger.TEN.pow(100)));
        assertTrue(projectivePlane().integralBoundaryInvariants(BigInteger.TEN.pow(100)).isEmpty());
        assertTrue(projectivePlane().integralBoundaryInvariants(z(0)).isEmpty());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,() -> empty.integralHomology(z(-1))).kind());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,() -> empty.integralBoundaryInvariants(z(-1))).kind());
        List<int[]> vertices=new ArrayList<>(); for(int i=0;i<257;i++) vertices.add(new int[]{i});
        FiniteSimplicialComplex oversized=complex(vertices.toArray(new int[0][])); limit(() -> oversized.integralHomology(z(0))); limit(oversized::integralHomologyGroups);
        assertEquals(AbelianGroupType.ZERO,oversized.integralHomology(z(1)));
        limit(() -> smith(new BigInteger[257][0]));
        BigInteger[][] identity=new BigInteger[128][128];
        for(int r=0;r<128;r++) for(int c=0;c<128;c++) identity[r][c]=r==c?BigInteger.ONE:BigInteger.ZERO;
        assertEquals(128,smith(identity).size());
        IntegerSmithNormalForm.Computation shared=new IntegerSmithNormalForm.Computation();
        limit(() -> { for(int i=0;i<10;i++) shared.invariantFactors(identity); });
    }
    @Test public void nativeScalarFlatAndSecondTypeResultsUseActualCarriersAndSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IAlgebraItem<FiniteSimplicialComplex> item=math.complexes.algebra().buildAlgebraItem(projectivePlane());
        IAlgebraItem<AbelianGroupType> homology=item.performUnsafeOperation("integral-homology",z(1));
        assertSame(math.abelianGroups.algebra(),homology.getAlgebra()); assertEquals(AbelianGroupType.cyclic(z(2)),homology.getResult());
        assertEquals(z(2),homology.<BigInteger>performAlgebraTransfer("order").getResult());
        IAlgebraItem<BigInteger> rational=item.performLeftProjectionOperation("rational-betti-number",z(1));
        assertSame(math.naturals.algebra(),rational.getAlgebra()); assertEquals(BigInteger.ZERO,rational.getResult());
        assertEquals(BigInteger.ONE,item.performLeftProjectionOperation("betti-number",z(1)).getResult());
        List<IAlgebraItem<BigInteger>> invariants=item.performLeftProjectionFlatOperation("boundary-invariant-factors",z(2));
        assertEquals(10,invariants.size()); assertSame(math.naturals.algebra(),invariants.get(9).getAlgebra()); assertEquals(z(2),invariants.get(9).getResult());
        IAlgebraFlow<BigInteger> flow=math.flow(math.complexes,Collections.singletonList(projectivePlane()))
                .<AbelianGroupType>performFlatAlgebraTransfer("integral-homology-groups")
                .<BigInteger>performFlatAlgebraTransfer("invariant-factors");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("2"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Arrays.asList("1","0","0"),math.flow(math.complexes,Collections.singletonList(projectivePlane())).performFlatAlgebraTransfer("rational-betti-numbers").collect());
    }
}
