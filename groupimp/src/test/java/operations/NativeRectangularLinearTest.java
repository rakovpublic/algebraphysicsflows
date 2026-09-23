package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeRectangularLinearTest {
    private static RationalVector v(long... values) {
        Rational[] result=new Rational[values.length]; for(int i=0;i<values.length;i++) result[i]=Rational.of(values[i]);
        return new RationalVector(result);
    }
    private static RationalMatrix m(long[]... rows) {
        Rational[][] result=new Rational[rows.length][];
        for(int r=0;r<rows.length;r++) { result[r]=new Rational[rows[r].length]; for(int c=0;c<rows[r].length;c++) result[r][c]=Rational.of(rows[r][c]); }
        return new RationalMatrix(result);
    }
    private static RationalMatrix plane() { return m(new long[]{1,2,3},new long[]{2,4,6}); }
    private static void undefined(Runnable action) { assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind()); }
    @Test public void rectangularOperationsUseActualCarriersAndCheckEveryShapeBoundary() {
        ConcreteMathematics math=new ConcreteMathematics(); RationalMatrix a=plane(); IAlgebraItem<RationalMatrix> item=math.rectangularMatrices.algebra().buildAlgebraItem(a);
        IAlgebraItem<RationalVector> applied=item.performLeftProjectionOperation("apply",v(3,2,1));
        assertSame(math.finiteVectors.algebra(),applied.getAlgebra()); assertEquals(v(10,20),applied.getResult());
        assertSame(math.vectors.algebra(),applied.performAlgebraTransfer("to-fixed").getAlgebra());
        assertEquals(m(new long[]{1,2},new long[]{2,4},new long[]{3,6}),item.performOneOperandOperation("transpose").getResult());
        assertEquals(m(new long[]{10},new long[]{20}),item.performOperation("multiply",m(new long[]{3},new long[]{2},new long[]{1})).perform().getResult());
        undefined(() -> item.performOperation("add",RationalMatrix.identity(2)).perform());
        undefined(() -> item.performOperation("multiply",a).perform()); undefined(() -> item.performLeftProjectionOperation("apply",v(1,2)));
        undefined(() -> item.performAlgebraTransfer("to-fixed")); undefined(() -> item.performAlgebraTransfer("determinant"));
        undefined(() -> item.performOneOperandOperation("inverse")); undefined(() -> item.performAlgebraTransfer("trace"));
        assertEquals(a,math.rectangularMatrices.algebra().buildAlgebraItem(a).performCustomMemberOperation("scale",Rational.ONE).getResult());
        RationalMatrix swap=m(new long[]{0,1},new long[]{1,0});
        assertEquals(swap,math.rectangularMatrices.algebra().buildAlgebraItem(swap).performOneOperandOperation("inverse").getResult());
        assertEquals(Rational.of(-1),math.rectangularMatrices.algebra().buildAlgebraItem(swap).performAlgebraTransfer("determinant").getResult());
        undefined(() -> math.rectangularMatrices.algebra().buildAlgebraItem(m(new long[]{1,2},new long[]{2,4})).performOneOperandOperation("inverse"));
    }
    @Test public void rrefHandlesSkippedColumnsRowSwapsFractionsAndCanonicalBases() {
        RationalMatrix a=m(new long[]{0,2,4,2},new long[]{0,4,8,4},new long[]{0,0,2,2});
        assertEquals(m(new long[]{0,1,0,-1},new long[]{0,0,1,1},new long[]{0,0,0,0}),a.rref());
        assertEquals(Arrays.asList(1,2),a.pivotColumns());
        assertEquals(Arrays.asList(v(1,0,0,0),v(0,1,-1,1)),a.nullspace());
        assertEquals(Arrays.asList(v(0,1,0,-1),v(0,0,1,1)),a.rowSpace());
        assertEquals(Arrays.asList(v(2,4,0),v(4,8,2)),a.columnSpace());
        assertEquals(v(0,-1,1,0),a.solve(v(2,4,2)).particular());
        RationalMatrix fractions=new RationalMatrix(new Rational[][]{{Rational.of(1,2),Rational.of(1,3)},{Rational.of(1,4),Rational.of(1,6)}});
        assertEquals(new RationalVector(Rational.of(-2,3),Rational.ONE),fractions.nullspace().get(0));
        assertEquals(v(2,0),fractions.solve(new RationalVector(Rational.ONE,Rational.of(1,2))).particular());
        assertEquals(RationalMatrix.identity(2),m(new long[]{0,2},new long[]{3,4}).rref());
        assertEquals(a.rref(),a.rref().rref());
    }
    @Test public void solutionSetsDistinguishEmptySingletonAndInfiniteAffineSpaces() {
        RationalMatrix tall=m(new long[]{1,0},new long[]{0,1},new long[]{1,1});
        RationalAffineSpace unique=tall.solve(v(2,3,5));
        assertFalse(unique.isEmpty()); assertTrue(unique.isUnique()); assertEquals(0,unique.dimension());
        assertEquals(v(2,3),unique.at(v())); assertEquals(Collections.emptyList(),unique.directions()); assertTrue(unique.contains(v(2,3)));
        RationalAffineSpace empty=tall.solve(v(2,3,6)); assertTrue(empty.isEmpty()); assertFalse(empty.isUnique());
        assertEquals(2,empty.ambientDimension()); assertFalse(empty.contains(v(2,3))); assertFalse(empty.contains(v()));
        undefined(empty::particular); undefined(empty::directions); undefined(empty::dimension); undefined(() -> empty.at(v()));
        RationalAffineSpace infinite=plane().solve(v(1,2));
        assertEquals(2,infinite.dimension()); assertEquals(3,infinite.ambientDimension()); assertFalse(infinite.isUnique());
        assertEquals(v(1,0,0),infinite.particular()); assertEquals(Arrays.asList(v(-2,1,0),v(-3,0,1)),infinite.directions());
        assertEquals(v(-3,-1,2),infinite.at(v(-1,2))); assertTrue(infinite.contains(v(-3,-1,2))); assertFalse(infinite.contains(v(1,2)));
        undefined(() -> infinite.at(v(1))); undefined(() -> unique.at(v(1)));
        RationalMatrix zero=m(new long[]{0,0,0},new long[]{0,0,0}); RationalAffineSpace all=zero.solve(v(0,0));
        assertEquals(3,all.dimension()); assertEquals(Arrays.asList(v(1,0,0),v(0,1,0),v(0,0,1)),all.directions());
        assertEquals(v(-2,4,9),all.at(v(-2,4,9))); assertTrue(all.contains(v(-100,7,2)));
        assertTrue(zero.solve(v(1,0)).isEmpty()); assertNotEquals(empty,zero.solve(v(1,0)));
        undefined(() -> plane().solve(v(1)));
    }
    @Test public void equalityDependsOnTheAffineSetInsteadOfEquationPresentation() {
        RationalAffineSpace a=plane().solve(v(1,2));
        RationalAffineSpace b=m(new long[]{3,6,9}).solve(v(3));
        RationalAffineSpace c=m(new long[]{0,0,0},new long[]{-2,-4,-6},new long[]{1,2,3}).solve(v(0,-2,1));
        assertEquals(a,b); assertEquals(a,c); assertEquals(a.hashCode(),c.hashCode());
        assertNotEquals(a,plane().solve(v(2,4)));
        assertEquals(m(new long[]{0,0}).solve(v(1)),m(new long[]{1,0},new long[]{1,0}).solve(v(0,1)));
        RationalAffineSpace line=m(new long[]{0,1,2},new long[]{1,0,3}).solve(v(4,5));
        RationalAffineSpace reordered=m(new long[]{2,0,6},new long[]{1,1,5},new long[]{0,0,0}).solve(v(10,9,0));
        assertEquals(line,reordered); assertEquals(line.hashCode(),reordered.hashCode());
    }
    /** Independent rank oracle for two rows: inspect all entries and all 2x2 minors. */
    private static int rankByMinors(long[][] matrix) {
        boolean nonzero=false;
        for(long[] row : matrix) for(long value : row) if(value!=0) nonzero=true;
        for(int a=0;a<matrix[0].length;a++) for(int b=a+1;b<matrix[0].length;b++)
            if(matrix[0][a]*matrix[1][b]-matrix[0][b]*matrix[1][a]!=0) return 2;
        return nonzero?1:0;
    }
    private static void reducedForm(RationalMatrix reduced) {
        int previous=-1; boolean zeroSeen=false;
        for(int r=0;r<reduced.rows();r++) {
            int first=0; while(first<reduced.columns() && reduced.get(r,first).signum()==0) first++;
            if(first==reduced.columns()) { zeroSeen=true; continue; }
            assertFalse(zeroSeen); assertTrue(first>previous); assertEquals(Rational.ONE,reduced.get(r,first)); previous=first;
            for(int i=0;i<reduced.rows();i++) if(i!=r) assertEquals(Rational.ZERO,reduced.get(i,first));
        }
    }
    @Test public void everyTernaryTwoByThreeSystemAgreesWithIndependentMinorAndSubstitutionChecks() {
        for(int code=0;code<729;code++) {
            long[][] values=new long[2][3]; int digits=code;
            for(int r=0;r<2;r++) for(int c=0;c<3;c++) { values[r][c]=digits%3-1; digits/=3; }
            RationalMatrix a=m(values); int rank=rankByMinors(values);
            assertEquals(rank,a.rank()); reducedForm(a.rref()); assertEquals(rank,a.pivotColumns().size());
            List<RationalVector> kernel=a.nullspace(); assertEquals(3-rank,kernel.size());
            for(RationalVector direction : kernel) assertEquals(v(0,0),a.multiply(direction));
            // A nonzero Gram determinant checks linear independence of the returned directions.
            Rational[][] gram=new Rational[kernel.size()][kernel.size()];
            for(int i=0;i<kernel.size();i++) for(int j=0;j<kernel.size();j++) gram[i][j]=kernel.get(i).dot(kernel.get(j));
            assertTrue(new RationalMatrix(gram).determinant().signum()!=0);
            for(int b0=-1;b0<=1;b0++) for(int b1=-1;b1<=1;b1++) {
                long[][] augmented={{values[0][0],values[0][1],values[0][2],b0},{values[1][0],values[1][1],values[1][2],b1}};
                boolean consistent=rankByMinors(augmented)==rank; RationalVector rhs=v(b0,b1); RationalAffineSpace solution=a.solve(rhs);
                assertEquals(!consistent,solution.isEmpty());
                if(consistent) {
                    assertEquals(3-rank,solution.dimension()); assertEquals(rhs,a.multiply(solution.particular()));
                    Rational[] params=new Rational[solution.dimension()]; Arrays.fill(params,Rational.of(-2));
                    RationalVector point=solution.at(new RationalVector(params)); assertEquals(rhs,a.multiply(point)); assertTrue(solution.contains(point));
                }
                RationalVector candidate=v(1,-1,2);
                boolean direct=values[0][0]-values[0][1]+2*values[0][2]==b0 && values[1][0]-values[1][1]+2*values[1][2]==b1;
                assertEquals(direct,solution.contains(candidate));
            }
        }
    }
    @Test public void varyingVectorDimensionsAndFixedCarriersInteroperateWithoutAliasingTools() {
        ConcreteMathematics math=new ConcreteMathematics(),other=new ConcreteMathematics(3,7);
        IAlgebraItem<RationalVector> vector=math.finiteVectors.algebra().buildAlgebraItem(v(1,2,3));
        assertEquals(v(0,0,0),vector.performOneOperandOperation("zero-like").getResult());
        assertEquals(Rational.of(14),vector.performCustomResultOperation("dot",v(1,2,3)).getResult());
        undefined(() -> vector.performOperation("add",v(1,2)).perform()); undefined(() -> vector.performCustomResultOperation("dot",v()));
        undefined(() -> vector.performAlgebraTransfer("to-fixed"));
        IAlgebraItem<RationalVector> empty=math.finiteVectors.algebra().buildAlgebraItem(v());
        assertEquals(v(),empty.performOperation("add",v()).perform().getResult());
        assertEquals(Rational.ZERO,empty.performCustomResultOperation("dot",v()).getResult());
        assertEquals(Collections.emptyList(),empty.performAlgebraFlatTransfer("entries"));
        IAlgebraItem<RationalVector> embedded=other.vectors.algebra().buildAlgebraItem(v(1,2,3)).performAlgebraTransfer("Vec(Q).from-fixed");
        assertSame(other.finiteVectors.algebra(),embedded.getAlgebra());
        assertSame(other.vectors.algebra(),embedded.performAlgebraTransfer("to-fixed").getAlgebra());
        IAlgebraItem<RationalMatrix> matrix=other.matrices.algebra().buildAlgebraItem(RationalMatrix.identity(3)).performAlgebraTransfer("Mat(Q).from-fixed");
        assertSame(other.rectangularMatrices.algebra(),matrix.getAlgebra());
        assertSame(other.matrices.algebra(),matrix.performAlgebraTransfer("to-fixed").getAlgebra());
        assertThrows(IllegalArgumentException.class,() -> math.flow(other.finiteVectors,Collections.singletonList(v(1))));
    }
    @Test public void affineParametrizationsAndBasesExecuteThroughSerializedNativeFlows() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraItem<RationalAffineSpace> solved=math.rectangularMatrices.algebra().buildAlgebraItem(plane()).performUnsafeOperation("Affine(Q).solve",v(1,2));
        assertSame(math.affineSpaces.algebra(),solved.getAlgebra());
        IAlgebraItem<RationalVector> point=solved.performLeftProjectionOperation("at",v(-1,2));
        assertSame(math.finiteVectors.algebra(),point.getAlgebra()); assertEquals(v(-3,-1,2),point.getResult());
        IAlgebraFlow<Rational> flow=math.flow(math.rectangularMatrices,Collections.singletonList(plane()))
                .<RationalAffineSpace,RationalVector>performAlgebraUnsafe("Affine(Q).solve",v(1,2))
                .performLeftProjectionOperation("at",v(-1,2)).<Rational>performFlatAlgebraTransfer("entries").performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("3","1","-2"),restored.collect()); assertEquals(Arrays.asList("3","1","-2"),restored.collect());
        assertEquals(Arrays.asList("0","0"),math.flow(math.rectangularMatrices,Collections.singletonList(plane()))
                .<RationalVector>performFlatAlgebraTransfer("nullspace-basis").<Rational>performCustomResultOperation("dot",v(1,2,3)).collect());
        assertTrue(math.rectangularMatrices.algebra().buildAlgebraItem(plane()).<RationalAffineSpace,RationalVector>performUnsafeOperation("Affine(Q).solve",v(1,3)).getResult().isEmpty());
    }
    @Test public void exactArithmeticAndImmutableOutputsSurviveLargeCoefficients() {
        Rational huge=Rational.of(BigInteger.TEN.pow(80)),next=huge.add(Rational.ONE),last=next.add(Rational.ONE);
        Rational[][] entries={{huge,next},{next,last}}; RationalMatrix a=new RationalMatrix(entries); entries[0][0]=Rational.ZERO;
        assertEquals(huge,a.get(0,0)); assertEquals(Rational.of(-1),a.determinant());
        assertEquals(RationalMatrix.identity(2),a.rref()); assertEquals(v(1,-1),a.solve(v(-1,-1)).particular());
        assertThrows(UnsupportedOperationException.class,() -> plane().nullspace().clear());
        assertThrows(UnsupportedOperationException.class,() -> plane().solve(v(1,2)).directions().clear());
        assertThrows(UnsupportedOperationException.class,() -> plane().pivotColumns().clear());
        assertThrows(UnsupportedOperationException.class,() -> plane().rowVectors().clear());
        assertThrows(MathFailure.class,() -> new RationalMatrix(new Rational[0][]));
        assertThrows(MathFailure.class,() -> new RationalMatrix(new Rational[][]{{}}));
    }
}
