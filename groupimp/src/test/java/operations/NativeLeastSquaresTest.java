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

public class NativeLeastSquaresTest {
    private static RationalVector v(long... values) {
        Rational[] result=new Rational[values.length]; for(int i=0;i<values.length;i++) result[i]=Rational.of(values[i]);
        return new RationalVector(result);
    }
    private static RationalVector fractions(long denominator,long... numerators) { return v(numerators).scale(Rational.of(1,denominator)); }
    private static RationalMatrix m(long[]... rows) {
        Rational[][] result=new Rational[rows.length][];
        for(int r=0;r<rows.length;r++) { result[r]=new Rational[rows[r].length]; for(int c=0;c<rows[r].length;c++) result[r][c]=Rational.of(rows[r][c]); }
        return new RationalMatrix(result);
    }
    private static RationalMatrix plane() { return m(new long[]{1,2,3},new long[]{2,4,6}); }
    private static RationalVector minus(RationalVector a,RationalVector b) { return a.add(b.scale(Rational.of(-1))); }
    private static Rational normSquared(RationalVector v) { return v.dot(v); }
    private static void undefined(Runnable action) { assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind()); }

    @Test public void rankDeficientInconsistentSystemsHaveAnAffineFamilyAndOneMinimumNormMinimizer() {
        RationalMatrix a=plane(); RationalVector rhs=v(1,3);
        assertTrue(a.solve(rhs).isEmpty());
        assertEquals(m(new long[]{1,2},new long[]{2,4},new long[]{3,6}).scale(Rational.of(1,70)),a.pseudoinverse());
        assertEquals(m(new long[]{1,2},new long[]{2,4}).scale(Rational.of(1,5)),a.columnProjector());
        assertEquals(m(new long[]{1,2,3},new long[]{2,4,6},new long[]{3,6,9}).scale(Rational.of(1,14)),a.rowProjector());
        RationalAffineSpace minimizers=a.leastSquares(rhs);
        assertFalse(minimizers.isEmpty()); assertFalse(minimizers.isUnique()); assertEquals(2,minimizers.dimension());
        assertEquals(fractions(5,7,0,0),minimizers.particular()); assertEquals(a.nullspace(),minimizers.directions());
        assertEquals(fractions(10,1,2,3),a.minimumNormLeastSquares(rhs));
        assertEquals(a.minimumNormLeastSquares(rhs),minimizers.minimumNorm());
        assertEquals(fractions(5,7,14),a.projectColumn(rhs)); assertEquals(fractions(5,-2,1),a.leastSquaresResidual(rhs));
        assertEquals(Rational.of(1,5),a.leastSquaresError(rhs));
        assertNotEquals(minimizers.particular(),minimizers.minimumNorm());
        assertTrue(minimizers.contains(minimizers.at(v(4,-7))));
        assertEquals(Rational.of(1,5),normSquared(minus(rhs,a.multiply(minimizers.at(v(4,-7))))));
    }

    @Test public void fullRankTallSystemsAndInvertibleSquareMatricesAgreeWithKnownExactResults() {
        RationalMatrix tall=m(new long[]{1,0},new long[]{0,1},new long[]{1,1}); RationalVector rhs=v(1,2,4);
        assertEquals(m(new long[]{2,-1,1},new long[]{-1,2,1}).scale(Rational.of(1,3)),tall.pseudoinverse());
        assertEquals(fractions(3,4,7),tall.minimumNormLeastSquares(rhs));
        assertEquals(fractions(3,-1,-1,1),tall.leastSquaresResidual(rhs)); assertEquals(Rational.of(1,3),tall.leastSquaresError(rhs));
        assertTrue(tall.leastSquares(rhs).isUnique()); assertEquals(fractions(3,4,7),tall.leastSquares(rhs).particular());
        assertEquals(tall.solve(v(1,2,3)),tall.leastSquares(v(1,2,3)));
        RationalMatrix square=m(new long[]{0,2},new long[]{3,4});
        assertEquals(square.inverse(),square.pseudoinverse()); assertEquals(RationalMatrix.identity(2),square.columnProjector());
        assertEquals(RationalMatrix.identity(2),square.rowProjector()); assertEquals(v(0,0),square.leastSquaresResidual(v(3,-7)));
        RationalMatrix rankThree=m(new long[]{1,0,0,1,0},new long[]{0,2,0,0,2},new long[]{0,0,3,0,0},new long[]{0,0,0,0,0});
        assertEquals(m(new long[]{6,0,0,0},new long[]{0,3,0,0},new long[]{0,0,4,0},new long[]{6,0,0,0},new long[]{0,3,0,0})
                .scale(Rational.of(1,12)),rankThree.pseudoinverse());
        assertEquals(fractions(2,1,1,2,1,1),rankThree.minimumNormLeastSquares(v(1,2,3,4)));
        penrose(rankThree,rankThree.pseudoinverse());
        RationalMatrix dense=m(new long[]{1,2,0},new long[]{0,1,3},new long[]{4,0,1});
        assertEquals(m(new long[]{1,-2,6},new long[]{12,1,-3},new long[]{-4,8,1}).scale(Rational.of(1,25)),dense.pseudoinverse());
    }

    @Test public void zeroMatricesRetainTheirShapesAndEveryPointMinimizesTheResidual() {
        RationalMatrix zero=m(new long[]{0,0,0},new long[]{0,0,0}); RationalVector rhs=v(3,4);
        assertEquals(m(new long[]{0,0},new long[]{0,0},new long[]{0,0}),zero.pseudoinverse());
        assertEquals(RationalMatrix.identity(2).scale(Rational.ZERO),zero.columnProjector());
        assertEquals(RationalMatrix.identity(3).scale(Rational.ZERO),zero.rowProjector());
        RationalAffineSpace all=zero.leastSquares(rhs); assertEquals(3,all.dimension()); assertTrue(all.contains(v(100,-9,2)));
        assertEquals(v(0,0,0),zero.minimumNormLeastSquares(rhs)); assertEquals(v(0,0),zero.projectColumn(rhs));
        assertEquals(rhs,zero.leastSquaresResidual(rhs)); assertEquals(Rational.of(25),zero.leastSquaresError(rhs));
        assertEquals(v(1,2,3),all.closestPoint(v(1,2,3))); assertEquals(v(0,0,0),all.minimumNorm());
    }

    /** These four identities characterize the pseudoinverse without reproducing its factorization algorithm. */
    private static void penrose(RationalMatrix a,RationalMatrix inverse) {
        RationalMatrix column=a.multiply(inverse),row=inverse.multiply(a);
        assertEquals(a,column.multiply(a)); assertEquals(inverse,row.multiply(inverse));
        assertEquals(column,column.transpose()); assertEquals(row,row.transpose());
        assertEquals(column,column.multiply(column)); assertEquals(row,row.multiply(row));
        assertEquals(Rational.of(a.rank()),column.trace()); assertEquals(Rational.of(a.rank()),row.trace());
    }
    private static void leastSquaresIdentities(RationalMatrix a,RationalMatrix inverse) {
        Rational[] values=new Rational[a.rows()]; for(int i=0;i<values.length;i++) values[i]=Rational.of(i+1);
        RationalVector rhs=new RationalVector(values),x=inverse.multiply(rhs),residual=minus(rhs,a.multiply(x));
        for(RationalVector column : a.columnVectors()) assertEquals(Rational.ZERO,column.dot(residual));
        RationalAffineSpace family=a.leastSquares(rhs); assertTrue(family.contains(x)); assertEquals(a.columns()-a.rank(),family.dimension());
        assertEquals(normSquared(residual),normSquared(minus(rhs,a.multiply(family.particular()))));
        for(RationalVector kernel : a.nullspace()) {
            assertEquals(Rational.ZERO,kernel.dot(x));
            assertEquals(normSquared(x).add(normSquared(kernel)),normSquared(x.add(kernel)));
            assertTrue(family.contains(x.add(kernel)));
        }
        Rational[] delta=new Rational[a.columns()]; for(int i=0;i<delta.length;i++) delta[i]=Rational.of(2-i);
        RationalVector displacement=new RationalVector(delta);
        // Pythagoras: every other candidate has at least this residual energy.
        assertEquals(normSquared(residual).add(normSquared(a.multiply(displacement))),normSquared(minus(rhs,a.multiply(x.add(displacement)))));
    }
    @Test public void allTernaryTwoByThreeMatricesAndTheirTransposesSatisfyPenroseAndLeastSquaresIdentities() {
        for(int code=0;code<729;code++) {
            long[][] entries=new long[2][3]; int digits=code;
            for(int r=0;r<2;r++) for(int c=0;c<3;c++) { entries[r][c]=digits%3-1; digits/=3; }
            RationalMatrix a=m(entries),inverse=a.pseudoinverse();
            penrose(a,inverse); leastSquaresIdentities(a,inverse);
            assertEquals(a,inverse.pseudoinverse());
            RationalMatrix transposed=a.transpose(),transposedInverse=transposed.pseudoinverse();
            assertEquals(inverse.transpose(),transposedInverse); penrose(transposed,transposedInverse);
            leastSquaresIdentities(transposed,transposedInverse);
        }
    }

    @Test public void affineProjectionIsNearestAndIdempotentAndRejectsEmptySets() {
        RationalAffineSpace line=m(new long[]{1,1}).solve(v(1)); RationalVector point=v(2,0);
        assertEquals(fractions(2,3,-1),line.closestPoint(point)); assertEquals(fractions(2,1,1),line.minimumNorm());
        RationalVector nearest=line.closestPoint(point),displacement=minus(point,nearest);
        assertTrue(line.contains(nearest)); assertEquals(nearest,line.closestPoint(nearest));
        for(int t=-5;t<=5;t++) {
            RationalVector alternative=line.at(v(t));
            assertEquals(normSquared(displacement).add(normSquared(minus(alternative,nearest))),normSquared(minus(point,alternative)));
        }
        RationalAffineSpace singleton=RationalMatrix.identity(2).solve(v(4,-2));
        assertEquals(v(4,-2),singleton.closestPoint(v(999,-999))); assertEquals(v(4,-2),singleton.minimumNorm());
        RationalAffineSpace empty=m(new long[]{0,0}).solve(v(1));
        undefined(() -> empty.closestPoint(v(0,0))); undefined(empty::minimumNorm); undefined(() -> line.closestPoint(v(1)));
        RationalMatrix a=plane();
        undefined(() -> a.leastSquares(v(1))); undefined(() -> a.minimumNormLeastSquares(v(1)));
        undefined(() -> a.projectColumn(v(1))); undefined(() -> a.leastSquaresResidual(v(1))); undefined(() -> a.leastSquaresError(v(1)));
    }

    @Test public void nativeWrappersAndSerializedFlowsPreserveMinimumNormAndAffineSemantics() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); IAlgebraItem<RationalMatrix> matrix=math.rectangularMatrices.algebra().buildAlgebraItem(plane());
        IAlgebraItem<RationalVector> minimum=matrix.performLeftProjectionOperation("least-squares-minimum-norm",v(1,3));
        assertSame(math.finiteVectors.algebra(),minimum.getAlgebra()); assertEquals(fractions(10,1,2,3),minimum.getResult());
        IAlgebraItem<Rational> error=matrix.performUnsafeOperation("least-squares-error",v(1,3));
        assertSame(math.rationals.algebra(),error.getAlgebra()); assertEquals(Rational.of(1,5),error.getResult());
        IAlgebraItem<RationalAffineSpace> family=matrix.performUnsafeOperation("Affine(Q).least-squares",v(1,3));
        assertSame(math.affineSpaces.algebra(),family.getAlgebra());
        assertSame(math.finiteVectors.algebra(),family.performAlgebraTransfer("minimum-norm").getAlgebra());
        assertEquals(minimum.getResult(),family.performLeftProjectionOperation("closest-point",v(0,0,0)).getResult());
        undefined(() -> matrix.performLeftProjectionOperation("least-squares-minimum-norm",v(1)));
        undefined(() -> matrix.performUnsafeOperation("Affine(Q).least-squares",v(1)));
        IAlgebraFlow<Rational> flow=math.flow(math.rectangularMatrices,Collections.singletonList(plane()))
                .<RationalAffineSpace,RationalVector>performAlgebraUnsafe("Affine(Q).least-squares",v(1,3))
                .<RationalVector>performAlgebraTransfer("minimum-norm").<Rational>performFlatAlgebraTransfer("entries");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("1/10","1/5","3/10"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        assertEquals(Collections.singletonList("[7/5, 14/5]"),math.flow(math.rectangularMatrices,Collections.singletonList(plane()))
                .performOneOperandOperation("column-projector").performLeftProjectionOperation("apply",v(1,3)).collect());
    }

    @Test public void largeExactRankOneCoefficientsNeedNoFloatingPointTolerance() {
        Rational huge=Rational.of(BigInteger.TEN.pow(80)),next=huge.add(Rational.ONE);
        RationalMatrix a=new RationalMatrix(new Rational[][]{{huge,next},{huge.multiply(Rational.of(2)),next.multiply(Rational.of(2))}});
        RationalVector rhs=v(1,3),solution=a.minimumNormLeastSquares(rhs);
        Rational denominator=huge.multiply(huge).add(next.multiply(next)).multiply(Rational.of(5));
        assertEquals(new RationalVector(huge.multiply(Rational.of(7)).divide(denominator),next.multiply(Rational.of(7)).divide(denominator)),solution);
        assertEquals(Rational.of(1,5),a.leastSquaresError(rhs)); penrose(a,a.pseudoinverse());
    }
}
