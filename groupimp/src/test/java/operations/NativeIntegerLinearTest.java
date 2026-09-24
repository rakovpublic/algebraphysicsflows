package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import mathematics.structures.AbelianGroupType;
import mathematics.topology.FiniteSimplicialComplex;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeIntegerLinearTest {
    private static BigInteger z(long n) { return BigInteger.valueOf(n); }
    private static IntegerVector v(long... values) {
        BigInteger[] result=new BigInteger[values.length]; for(int i=0;i<values.length;i++) result[i]=z(values[i]); return new IntegerVector(result);
    }
    private static IntegerMatrix m(long[]... rows) {
        BigInteger[][] values=new BigInteger[rows.length][];
        for(int r=0;r<rows.length;r++) { values[r]=new BigInteger[rows[r].length]; for(int c=0;c<rows[r].length;c++) values[r][c]=z(rows[r][c]); }
        return new IntegerMatrix(values);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static BigInteger determinant(IntegerMatrix matrix) {
        if(matrix.rows()==0) return BigInteger.ONE;
        BigInteger result=BigInteger.ZERO;
        for(int c=0;c<matrix.columns();c++) {
            BigInteger[][] minor=new BigInteger[matrix.rows()-1][matrix.columns()-1];
            for(int r=1;r<matrix.rows();r++) for(int k=0,j=0;k<matrix.columns();k++) if(k!=c) minor[r-1][j++]=matrix.get(r,k);
            BigInteger term=matrix.get(0,c).multiply(determinant(new IntegerMatrix(minor)));
            result=(c&1)==0?result.add(term):result.subtract(term);
        }
        return result;
    }
    private static List<BigInteger> divisors(long[][] a) {
        BigInteger gcd1=BigInteger.ZERO,gcd2=BigInteger.ZERO;
        for(long[] row : a) for(long value : row) gcd1=gcd1.gcd(z(value));
        for(int i=0;i<a[0].length;i++) for(int j=i+1;j<a[0].length;j++) gcd2=gcd2.gcd(z(a[0][i]*a[1][j]-a[0][j]*a[1][i]));
        List<BigInteger> result=new ArrayList<>(); if(gcd1.signum()>0) result.add(gcd1); if(gcd2.signum()>0) result.add(gcd2.divide(gcd1)); return result;
    }
    private static long[][] ternary(int code) {
        long[][] result=new long[2][3];
        for(int r=0;r<2;r++) for(int c=0;c<3;c++) { result[r][c]=code%3-1; code/=3; } return result;
    }
    private static void witnesses(IntegerMatrix matrix) {
        List<IntegerMatrix> decomposition=matrix.smithDecomposition(); IntegerMatrix u=decomposition.get(0),d=decomposition.get(1),v=decomposition.get(2);
        assertEquals(d,u.multiply(matrix).multiply(v));
        assertEquals(BigInteger.ONE,determinant(u).abs()); assertEquals(BigInteger.ONE,determinant(v).abs());
        assertEquals(matrix.smithForm(),d); assertEquals(matrix.rows(),d.rows()); assertEquals(matrix.columns(),d.columns());
        for(IntegerVector vector : matrix.kernelBasis()) assertEquals(IntegerVector.zero(matrix.rows()),matrix.multiply(vector));
        assertEquals(matrix.columns()-matrix.rank(),matrix.kernelBasis().size());
        List<IntegerVector> image=matrix.imageBasis(); assertEquals(matrix.rank(),image.size());
        for(int i=0;i<image.size();i++) assertEquals(matrix.multiply(v.column(i)),image.get(i));
    }
    @Test public void all729TernaryRectangularMatricesHaveExactUnimodularSmithWitnesses() {
        for(int code=0;code<729;code++) {
            long[][] entries=ternary(code); IntegerMatrix matrix=m(entries);
            assertEquals(divisors(entries),matrix.smithInvariantFactors()); witnesses(matrix); witnesses(matrix.transpose());
        }
    }
    @Test public void allSmallRectangularIntegerSystemsMatchIndependentLatticeIndexOracle() {
        // Adding b as a column preserves the image lattice iff its rank and determinantal divisors stay equal.
        for(int code=0;code<729;code++) {
            long[][] entries=ternary(code); IntegerMatrix matrix=m(entries); List<BigInteger> original=divisors(entries);
            for(int b0=-1;b0<=1;b0++) for(int b1=-1;b1<=1;b1++) {
                long[][] augmented={Arrays.copyOf(entries[0],4),Arrays.copyOf(entries[1],4)}; augmented[0][3]=b0; augmented[1][3]=b1;
                IntegerVector b=v(b0,b1); boolean exists=original.equals(divisors(augmented));
                assertEquals(exists,matrix.hasIntegerSolution(b));
                if(exists) {
                    List<IntegerVector> generators=matrix.solveGenerators(b); assertEquals(b,matrix.multiply(generators.get(0)));
                    assertEquals(4-original.size(),generators.size());
                    IntegerVector shifted=generators.get(0);
                    for(int i=1;i<generators.size();i++) shifted=shifted.add(generators.get(i).scale(z(i+1)));
                    assertEquals(b,matrix.multiply(shifted));
                } else failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> matrix.solveParticular(b));
            }
        }
    }
    @Test public void kernelIsTheWholeIntegerLatticeAndImageDoesNotDiscardIndices() {
        IntegerMatrix row=m(new long[]{2,1,1}); List<IntegerVector> kernel=row.kernelBasis(); boolean found=false;
        for(int a=-4;a<=4;a++) for(int b=-4;b<=4;b++) if(kernel.get(0).scale(z(a)).add(kernel.get(1).scale(z(b))).equals(v(-1,1,1))) found=true;
        assertTrue(found); // Scaling each rational free-column basis separately would miss this vector.
        IntegerMatrix doubled=IntegerMatrix.identity(2).scale(z(2));
        assertEquals(Arrays.asList(v(2,0),v(0,2)),doubled.imageBasis());
        assertFalse(doubled.hasIntegerSolution(v(1,0))); assertEquals(v(1,2),doubled.solveParticular(v(2,4)));
        assertEquals(Arrays.asList(z(2),z(2)),doubled.cokernel().invariantFactors());
    }
    @Test public void cokernelsClassifyIntegerPresentationsIncludingFreeSummands() {
        IntegerMatrix presentation=m(new long[]{2,0},new long[]{0,3},new long[]{0,0});
        assertEquals(new AbelianGroupType(BigInteger.ONE,Collections.singletonList(z(6))),presentation.cokernel());
        assertEquals(presentation.cokernel(),m(new long[]{1,2,0},new long[]{0,1,0},new long[]{0,0,-1}).multiply(presentation).cokernel());
        assertEquals(AbelianGroupType.ZERO,m(new long[]{2,3}).cokernel());
        assertEquals(AbelianGroupType.free(z(3)),IntegerMatrix.zero(3,0).cokernel());
    }
    @Test public void unimodularInversesExistExactlyForUnitDeterminants() {
        for(int code=0;code<625;code++) {
            int digits=code; long[][] entries=new long[2][2];
            for(int r=0;r<2;r++) for(int c=0;c<2;c++) { entries[r][c]=digits%5-2; digits/=5; }
            IntegerMatrix matrix=m(entries); boolean unit=determinant(matrix).abs().equals(BigInteger.ONE);
            if(unit) { IntegerMatrix inverse=matrix.inverseUnimodular(); assertEquals(IntegerMatrix.identity(2),matrix.multiply(inverse)); assertEquals(IntegerMatrix.identity(2),inverse.multiply(matrix)); }
            else failure(MathFailure.Kind.OPERATION_UNDEFINED,matrix::inverseUnimodular);
        }
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegerMatrix.zero(2,3).inverseUnimodular());
    }
    @Test public void emptyShapesRetainDomainsCodomainsAndAllFreeSolutions() {
        IntegerMatrix a=IntegerMatrix.zero(0,3),b=IntegerMatrix.zero(2,0);
        assertEquals(IntegerMatrix.zero(2,3),b.multiply(a)); assertEquals(IntegerMatrix.zero(0,0),a.multiply(a.transpose()));
        assertEquals(IntegerMatrix.zero(3,0),a.transpose()); assertNotEquals(a,IntegerMatrix.zero(0,2));
        assertEquals(Arrays.asList(v(),v(),v()),a.columnVectors()); assertTrue(a.rowVectors().isEmpty());
        assertEquals(Arrays.asList(v(),v()),b.rowVectors()); assertTrue(b.columnVectors().isEmpty());
        assertEquals(Arrays.asList(v(0,0,0),v(1,0,0),v(0,1,0),v(0,0,1)),a.solveGenerators(v()));
        assertEquals(Collections.singletonList(v()),b.solveGenerators(v(0,0))); assertFalse(b.hasIntegerSolution(v(1,0)));
        assertEquals(IntegerMatrix.zero(0,0),IntegerMatrix.zero(0,0).inverseUnimodular()); witnesses(a); witnesses(b);
        assertEquals(v(0,0),b.multiply(v())); assertEquals(v(),a.multiply(v(2,3,4)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,a::toRational);
    }
    @Test public void arithmeticAndConversionsAreExactAndShapeChecked() {
        IntegerMatrix a=m(new long[]{2,3},new long[]{4,5}),b=m(new long[]{1,0},new long[]{-1,2});
        assertEquals(m(new long[]{-1,6},new long[]{-1,10}),a.multiply(b)); assertEquals(v(13,23),a.multiply(v(2,3)));
        assertEquals(m(new long[]{3,3},new long[]{3,7}),a.add(b)); assertEquals(a,IntegerMatrix.fromRational(a.toRational()));
        assertEquals(v(4,6),v(1,2).add(v(3,4))); assertEquals(z(11),v(1,2).dot(v(3,4)));
        assertEquals(v(-2,-4),v(1,2).scale(z(-2))); assertEquals(v(1,2),IntegerVector.fromRational(v(1,2).toRational()));
        assertEquals(v(),IntegerVector.fromRational(new RationalVector()));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegerVector.fromRational(new RationalVector(Rational.of(1,2))));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> IntegerMatrix.fromRational(new RationalMatrix(new Rational[][]{{Rational.of(1,2)}})));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.add(IntegerMatrix.zero(2,3)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.multiply(IntegerMatrix.zero(3,2)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.multiply(v(1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> a.hasIntegerSolution(v(1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> v(1).dot(v(1,2)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> v(1).add(v(1,2)));
    }
    @Test public void constructionIsImmutableAndResourceFailuresAreSeparateFromNoSolution() {
        BigInteger[][] entries={{z(2),z(3)}}; IntegerMatrix matrix=new IntegerMatrix(entries); entries[0][0]=z(100);
        assertEquals(z(2),matrix.get(0,0)); assertThrows(UnsupportedOperationException.class,() -> matrix.rowVectors().clear());
        BigInteger[] vectorEntries={z(7)}; IntegerVector vector=new IntegerVector(vectorEntries); vectorEntries[0]=z(8); assertEquals(z(7),vector.get(0));
        assertThrows(UnsupportedOperationException.class,() -> vector.entries().clear());
        failure(MathFailure.Kind.INVALID_MEMBER,() -> IntegerMatrix.zero(-1,2));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> new IntegerMatrix(new BigInteger[][]{{z(1)},{}}));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> new IntegerMatrix(0,2,new BigInteger[][]{{z(1),z(2)}}));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegerMatrix.zero(257,0));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegerVector.zero(257));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegerMatrix.identity(256).multiply(IntegerMatrix.identity(256)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> IntegerMatrix.identity(256).hasIntegerSolution(IntegerVector.zero(256)));
        BigInteger huge=BigInteger.TEN.pow(100); IntegerMatrix large=new IntegerMatrix(new BigInteger[][]{{huge,BigInteger.ZERO},{BigInteger.ZERO,huge.add(BigInteger.ONE)}});
        List<IntegerMatrix> decomposition=large.smithDecomposition(); assertEquals(decomposition.get(1),decomposition.get(0).multiply(large).multiply(decomposition.get(2)));
        assertEquals(Arrays.asList(BigInteger.ONE,huge.multiply(huge.add(BigInteger.ONE))),large.smithInvariantFactors());
    }
    @Test public void orientedBoundaryMatricesComposeToZeroWithRetainedEmptyShapes() {
        FiniteSimplicialComplex tetrahedron=new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(3,1,0,2)));
        for(int k=0;k<=tetrahedron.dimension();k++) {
            IntegerMatrix current=tetrahedron.integralBoundaryMatrix(z(k)),next=tetrahedron.integralBoundaryMatrix(z(k+1));
            assertEquals(IntegerMatrix.zero(current.rows(),next.columns()),current.multiply(next));
            assertEquals(tetrahedron.integralBoundaryInvariants(z(k)),current.smithInvariantFactors());
        }
        assertEquals(IntegerMatrix.zero(0,4),tetrahedron.integralBoundaryMatrix(z(0)));
        assertEquals(IntegerMatrix.zero(1,0),tetrahedron.integralBoundaryMatrix(z(4)));
        assertEquals(IntegerMatrix.zero(0,0),tetrahedron.integralBoundaryMatrix(BigInteger.TEN.pow(100)));
        FiniteSimplicialComplex triangle=new FiniteSimplicialComplex(Collections.singletonList(FiniteSet.of(2,0,1)));
        assertEquals(m(new long[]{1},new long[]{-1},new long[]{1}),triangle.integralBoundaryMatrix(z(2)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> triangle.integralBoundaryMatrix(z(-1)));
    }
    @Test public void nativeSolversAndSmithResultsUseActualWrappersAndSerializedFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IntegerMatrix matrix=m(new long[]{2,1,1});
        IAlgebraItem<IntegerMatrix> item=math.integerMatrices.algebra().buildAlgebraItem(matrix);
        IAlgebraItem<IntegerVector> solution=item.performLeftProjectionOperation("solve-particular",v(1));
        assertSame(math.integerVectors.algebra(),solution.getAlgebra()); assertEquals(v(1),matrix.multiply(solution.getResult()));
        List<IAlgebraItem<IntegerVector>> generators=item.performLeftProjectionFlatOperation("solve-generators",v(1));
        assertEquals(3,generators.size()); for(IAlgebraItem<IntegerVector> result : generators) assertSame(math.integerVectors.algebra(),result.getAlgebra());
        List<IAlgebraItem<IntegerMatrix>> smith=item.performOneOperandFlatOperation("smith-decomposition");
        assertEquals(3,smith.size()); for(IAlgebraItem<IntegerMatrix> result : smith) assertSame(math.integerMatrices.algebra(),result.getAlgebra());
        assertSame(math.abelianGroups.algebra(),item.performAlgebraTransfer("cokernel").getAlgebra());
        assertSame(math.integerMatrices.algebra(),math.rectangularMatrices.algebra().buildAlgebraItem(matrix.toRational()).performAlgebraTransfer("Mat(Z).from-rational").getAlgebra());
        IAlgebraFlow<IntegerVector> flow=math.flow(math.integerMatrices,Collections.singletonList(matrix)).performLeftProjectionFlatOperation("solve-generators",v(1));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored; try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(flow.collect(),restored.collect()); assertEquals(restored.collect(),restored.collect());
        FiniteSimplicialComplex circle=new FiniteSimplicialComplex(Arrays.asList(FiniteSet.of(0,1),FiniteSet.of(1,2),FiniteSet.of(0,2)));
        assertEquals(Collections.singletonList("AbelianGroup(rank=1, torsion=[])"),math.flow(math.complexes,Collections.singletonList(circle))
                .<IntegerMatrix,BigInteger>performAlgebraUnsafe("boundary-matrix",z(1)).<AbelianGroupType>performAlgebraTransfer("cokernel").collect());
    }
}
