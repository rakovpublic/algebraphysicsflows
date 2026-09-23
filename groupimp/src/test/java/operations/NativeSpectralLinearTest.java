package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.calculus.Polynomial;
import mathematics.core.MathFailure;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeSpectralLinearTest {
    private static RationalMatrix m(long[]... rows) {
        Rational[][] result=new Rational[rows.length][];
        for(int r=0;r<rows.length;r++) {
            result[r]=new Rational[rows[r].length];
            for(int c=0;c<rows[r].length;c++) result[r][c]=Rational.of(rows[r][c]);
        }
        return new RationalMatrix(result);
    }
    private static Polynomial p(long... values) {
        Rational[] coefficients=new Rational[values.length];
        for(int i=0;i<values.length;i++) coefficients[i]=Rational.of(values[i]);
        return new Polynomial(coefficients);
    }
    private static RationalVector v(long... values) {
        Rational[] entries=new Rational[values.length]; for(int i=0;i<values.length;i++) entries[i]=Rational.of(values[i]);
        return new RationalVector(entries);
    }
    private static RationalMatrix zero(int n) { return RationalMatrix.identity(n).scale(Rational.ZERO); }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static void undefined(Runnable action) { failure(MathFailure.Kind.OPERATION_UNDEFINED,action); }
    private static void limit(Runnable action) { failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,action); }

    @Test public void rationalRootsAreSortedDistinctAndKeepMultiplicitiesSeparate() {
        Polynomial a=p(-1,2),b=p(2,3);
        Polynomial polynomial=a.multiply(a).multiply(a).multiply(b).multiply(b).multiply(p(0,0,1)).multiply(p(1,0,1));
        assertEquals(Arrays.asList(Rational.of(-2,3),Rational.ZERO,Rational.of(1,2)),polynomial.rationalRoots());
        assertEquals(2,polynomial.rootMultiplicity(Rational.of(-2,3)));
        assertEquals(2,polynomial.rootMultiplicity(Rational.ZERO));
        assertEquals(3,polynomial.rootMultiplicity(Rational.of(1,2)));
        assertEquals(0,polynomial.rootMultiplicity(Rational.ONE));
        assertEquals(polynomial.rationalRoots(),polynomial.scale(Rational.of(BigInteger.TEN.pow(60))).rationalRoots());
        assertEquals(polynomial.rationalRoots(),polynomial.scale(Rational.of(-7,11)).rationalRoots());
        assertEquals(Collections.emptyList(),p(1,0,1).rationalRoots());
        assertEquals(Collections.emptyList(),p(-2,0,1).rationalRoots());
        assertEquals(Collections.emptyList(),p(9).rationalRoots()); assertEquals(0,p(9).rootMultiplicity(Rational.ZERO));
        undefined(Polynomial.ZERO::rationalRoots); undefined(() -> Polynomial.ZERO.rootMultiplicity(Rational.ONE));
        assertThrows(UnsupportedOperationException.class,() -> polynomial.rationalRoots().add(Rational.ONE));
    }

    private static Rational derivativeValue(long[] coefficients,int order,Rational point) {
        Rational result=Rational.ZERO;
        for(int i=order;i<coefficients.length;i++) {
            long multiplier=coefficients[i]; for(int j=0;j<order;j++) multiplier*=i-j;
            result=result.add(Rational.of(multiplier).multiply(point.pow(i-order)));
        }
        return result;
    }
    @Test public void allSmallCubicsMatchAnIndependentCandidateAndDerivativeOracle() {
        // Rational-root theorem: coefficients in [-2,2] permit only these rational candidates.
        List<Rational> candidates=Arrays.asList(Rational.of(-2),Rational.of(-1),Rational.of(-1,2),Rational.ZERO,
                Rational.of(1,2),Rational.ONE,Rational.of(2));
        for(int code=0;code<625;code++) {
            int digits=code; long[] coefficients=new long[4];
            for(int i=0;i<4;i++) { coefficients[i]=digits%5-2; digits/=5; }
            Polynomial polynomial=p(coefficients); if(polynomial.degree()<0) continue;
            List<Rational> roots=new ArrayList<>();
            for(Rational candidate : candidates) {
                int multiplicity=0;
                while(derivativeValue(coefficients,multiplicity,candidate).signum()==0) multiplicity++;
                if(multiplicity>0) roots.add(candidate);
                assertEquals(multiplicity,polynomial.rootMultiplicity(candidate));
            }
            assertEquals(roots,polynomial.rationalRoots());
        }
    }

    /** Independent determinant expansion over all six permutations of three coordinates. */
    private static Polynomial determinantPolynomial3(RationalMatrix matrix) {
        Polynomial result=Polynomial.ZERO;
        for(int a=0;a<3;a++) for(int b=0;b<3;b++) if(a!=b) for(int c=0;c<3;c++) if(c!=a && c!=b) {
            int[] columns={a,b,c}; int inversions=(a>b?1:0)+(a>c?1:0)+(b>c?1:0);
            Polynomial term=new Polynomial(Rational.of((inversions&1)==0?1:-1));
            for(int r=0;r<3;r++) term=term.multiply(new Polynomial(matrix.get(r,columns[r]).negate(),r==columns[r]?Rational.ONE:Rational.ZERO));
            result=result.add(term);
        }
        return result;
    }
    private static void checkMinimality(RationalMatrix matrix,Polynomial minimal) {
        assertEquals(Rational.ONE,minimal.coefficient(minimal.degree()));
        assertEquals(zero(matrix.rows()),matrix.evaluatePolynomial(minimal));
        assertEquals(Polynomial.ZERO,matrix.characteristicPolynomial().remainder(minimal));
        // Full rank of vec(I),...,vec(A^(d-1)) independently proves no smaller annihilator exists.
        int n=matrix.rows(),degree=minimal.degree(); Rational[][] columns=new Rational[n*n][degree];
        RationalMatrix power=RationalMatrix.identity(n);
        for(int k=0;k<degree;k++) {
            for(int r=0;r<n;r++) for(int c=0;c<n;c++) columns[r*n+c][k]=power.get(r,c);
            power=power.multiply(matrix);
        }
        assertEquals(degree,new RationalMatrix(columns).rank());
    }
    @Test public void allBinaryThreeByThreeMatricesMatchDeterminantExpansionAndMinimality() {
        for(int code=0;code<512;code++) {
            long[][] values=new long[3][3]; int bits=code;
            for(int r=0;r<3;r++) for(int c=0;c<3;c++) { values[r][c]=bits&1; bits>>=1; }
            RationalMatrix matrix=m(values); Polynomial characteristic=matrix.characteristicPolynomial();
            assertEquals(determinantPolynomial3(matrix),characteristic);
            assertEquals(matrix.trace().negate(),characteristic.coefficient(2));
            assertEquals(matrix.determinant().negate(),characteristic.coefficient(0));
            assertEquals(zero(3),matrix.evaluatePolynomial(characteristic));
            checkMinimality(matrix,matrix.minimalPolynomial());
        }
    }

    @Test public void allTernaryTwoByTwoSpectraMatchTheQuadraticFormula() {
        for(int code=0;code<81;code++) {
            int digits=code; long[] x=new long[4]; for(int i=0;i<4;i++) { x[i]=digits%3-1; digits/=3; }
            RationalMatrix matrix=m(new long[]{x[0],x[1]},new long[]{x[2],x[3]});
            long trace=x[0]+x[3],det=x[0]*x[3]-x[1]*x[2],discriminant=trace*trace-4*det;
            assertEquals(p(det,-trace,1),matrix.characteristicPolynomial());
            SortedSet<Rational> roots=new TreeSet<>();
            for(long root=0;root<=6;root++) if(root*root==discriminant) {
                roots.add(Rational.of(trace-root,2)); roots.add(Rational.of(trace+root,2));
            }
            assertEquals(new ArrayList<>(roots),matrix.rationalEigenvalues());
            boolean scalar=x[1]==0 && x[2]==0 && x[0]==x[3];
            boolean diagonalizable=roots.size()==2 || scalar;
            assertEquals(diagonalizable,matrix.isDiagonalizableOverRationals());
            for(Rational eigenvalue : roots) {
                int multiplicity=discriminant==0?2:1;
                assertEquals(multiplicity,matrix.eigenvalueMultiplicity(eigenvalue));
                List<RationalVector> space=matrix.eigenspace(eigenvalue),generalized=matrix.generalizedEigenspace(eigenvalue);
                assertEquals(scalar?2:1,space.size()); assertEquals(multiplicity,generalized.size());
                for(RationalVector vector : space) assertEquals(vector.scale(eigenvalue),matrix.multiply(vector));
                RationalMatrix shifted=matrix.add(RationalMatrix.identity(2).scale(eigenvalue.negate()));
                for(RationalVector vector : generalized) assertEquals(v(0,0),shifted.multiply(shifted).multiply(vector));
            }
            assertEquals(0,matrix.eigenvalueMultiplicity(Rational.of(9)));
            assertTrue(matrix.eigenspace(Rational.of(9)).isEmpty()); assertTrue(matrix.generalizedEigenspace(Rational.of(9)).isEmpty());
            if(diagonalizable) checkDecomposition(matrix); else undefined(matrix::diagonalizeOverRationals);
        }
    }
    private static List<RationalMatrix> checkDecomposition(RationalMatrix matrix) {
        List<RationalMatrix> result=matrix.diagonalizeOverRationals(); assertEquals(2,result.size());
        RationalMatrix change=result.get(0),diagonal=result.get(1);
        assertEquals(matrix.rows(),change.rank()); assertEquals(matrix.multiply(change),change.multiply(diagonal));
        assertEquals(diagonal,change.inverse().multiply(matrix).multiply(change));
        for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.rows();c++) if(r!=c) assertEquals(Rational.ZERO,diagonal.get(r,c));
        for(int i=1;i<matrix.rows();i++) assertTrue(diagonal.get(i-1,i-1).compareTo(diagonal.get(i,i))<=0);
        return result;
    }

    @Test public void jordanBlocksSeparateMinimalPolynomialAndGeometricMultiplicity() {
        RationalMatrix jordan=m(new long[]{2,1,0,0},new long[]{0,2,1,0},new long[]{0,0,2,0},new long[]{0,0,0,2});
        Polynomial factor=p(-2,1);
        assertEquals(factor.multiply(factor).multiply(factor).multiply(factor),jordan.characteristicPolynomial());
        assertEquals(factor.multiply(factor).multiply(factor),jordan.minimalPolynomial());
        assertEquals(Arrays.asList(v(1,0,0,0),v(0,0,0,1)),jordan.eigenspace(Rational.of(2)));
        assertEquals(4,jordan.generalizedEigenspace(Rational.of(2)).size());
        assertEquals(4,jordan.eigenvalueMultiplicity(Rational.of(2))); assertFalse(jordan.isDiagonalizableOverRationals());
        undefined(jordan::diagonalizeOverRationals);
        RationalMatrix nilpotent=jordan.add(RationalMatrix.identity(4).scale(Rational.of(-2)));
        assertEquals(p(0,0,0,1),nilpotent.minimalPolynomial()); assertEquals(zero(4),nilpotent.pow(BigInteger.valueOf(3)));
        assertEquals(p(-2,1),RationalMatrix.identity(4).scale(Rational.of(2)).minimalPolynomial());
    }

    @Test public void irrationalAndComplexEigenvaluesRemainOutsideTheRationalCarrier() {
        RationalMatrix rotation=m(new long[]{0,-1},new long[]{1,0}),irrational=m(new long[]{0,2},new long[]{1,0});
        assertEquals(p(1,0,1),rotation.minimalPolynomial()); assertEquals(p(-2,0,1),irrational.minimalPolynomial());
        assertTrue(rotation.rationalEigenvalues().isEmpty()); assertTrue(irrational.rationalEigenvalues().isEmpty());
        assertFalse(rotation.isDiagonalizableOverRationals()); assertFalse(irrational.isDiagonalizableOverRationals());
        undefined(rotation::diagonalizeOverRationals); undefined(irrational::diagonalizeOverRationals);
        RationalMatrix mixed=m(new long[]{0,-1,0},new long[]{1,0,0},new long[]{0,0,3});
        assertEquals(Collections.singletonList(Rational.of(3)),mixed.rationalEigenvalues());
        assertEquals(Collections.singletonList(v(0,0,1)),mixed.eigenspace(Rational.of(3)));
        assertFalse(mixed.isDiagonalizableOverRationals()); undefined(mixed::diagonalizeOverRationals);
    }

    @Test public void exactSimilarityAndRepeatedRationalEigenvaluesGiveAnInvertibleEigenbasis() {
        RationalMatrix change=m(new long[]{1,1,0},new long[]{0,1,1},new long[]{1,0,1});
        RationalMatrix diagonal=new RationalMatrix(new Rational[][]{{Rational.of(-2),Rational.ZERO,Rational.ZERO},
                {Rational.ZERO,Rational.of(1,2),Rational.ZERO},{Rational.ZERO,Rational.ZERO,Rational.of(3)}});
        RationalMatrix matrix=change.multiply(diagonal).multiply(change.inverse());
        assertEquals(diagonal,checkDecomposition(matrix).get(1));
        assertEquals(diagonal.characteristicPolynomial(),matrix.characteristicPolynomial());
        assertEquals(diagonal.minimalPolynomial(),matrix.minimalPolynomial());
        RationalMatrix repeated=m(new long[]{-2,0,0},new long[]{0,3,0},new long[]{0,0,3});
        RationalMatrix similar=change.multiply(repeated).multiply(change.inverse());
        assertEquals(repeated,checkDecomposition(similar).get(1));
        assertEquals(p(-6,-1,1),similar.minimalPolynomial());
        assertEquals(2,similar.eigenspace(Rational.of(3)).size());
        assertThrows(UnsupportedOperationException.class,() -> matrix.diagonalizeOverRationals().clear());
    }

    @Test public void companionMatricesNormalizeNonmonicPolynomialsAndAreCyclic() {
        Polynomial polynomial=p(6,-5,-2,1).scale(Rational.of(2,7));
        RationalMatrix companion=RationalMatrix.companion(polynomial);
        assertEquals(m(new long[]{0,0,-6},new long[]{1,0,5},new long[]{0,1,2}),companion);
        assertEquals(polynomial.monic(),companion.characteristicPolynomial());
        assertEquals(polynomial.monic(),companion.minimalPolynomial()); checkMinimality(companion,companion.minimalPolynomial());
        assertEquals(m(new long[]{2}),RationalMatrix.companion(p(-6,3)));
        assertEquals(p(-2,1),m(new long[]{2}).characteristicPolynomial());
        assertEquals(Arrays.asList(m(new long[]{1}),m(new long[]{2})),m(new long[]{2}).diagonalizeOverRationals());
        assertEquals(p(0,1),zero(3).minimalPolynomial()); checkDecomposition(zero(3));
        undefined(() -> RationalMatrix.companion(Polynomial.ZERO)); undefined(() -> RationalMatrix.companion(Polynomial.ONE));
    }

    @Test public void matrixEvaluationIsARingHomomorphismAndPowersIncludeZero() {
        RationalMatrix matrix=m(new long[]{1,2},new long[]{3,4}); Polynomial first=p(2,-3,4),second=p(1,2,1);
        assertEquals(matrix.evaluatePolynomial(first).add(matrix.evaluatePolynomial(second)),matrix.evaluatePolynomial(first.add(second)));
        assertEquals(matrix.evaluatePolynomial(first).multiply(matrix.evaluatePolynomial(second)),matrix.evaluatePolynomial(first.multiply(second)));
        assertEquals(matrix.evaluatePolynomial(first).multiply(matrix),matrix.multiply(matrix.evaluatePolynomial(first)));
        assertEquals(zero(2),matrix.evaluatePolynomial(Polynomial.ZERO));
        assertEquals(RationalMatrix.identity(2).scale(Rational.of(7,13)),matrix.evaluatePolynomial(new Polynomial(Rational.of(7,13))));
        RationalMatrix power=RationalMatrix.identity(2);
        for(int k=0;k<12;k++) { assertEquals(power,matrix.pow(BigInteger.valueOf(k))); power=power.multiply(matrix); }
        assertEquals(RationalMatrix.identity(2),zero(2).pow(BigInteger.ZERO));
        undefined(() -> matrix.pow(BigInteger.valueOf(-1)));
    }

    @Test public void shapesAndResourceCapsFailExplicitlyWithoutFalseSpectralResults() {
        RationalMatrix rectangle=m(new long[]{1,2,3},new long[]{4,5,6});
        undefined(rectangle::characteristicPolynomial); undefined(rectangle::minimalPolynomial);
        undefined(() -> rectangle.evaluatePolynomial(Polynomial.ZERO)); undefined(() -> rectangle.pow(BigInteger.ZERO));
        undefined(rectangle::rationalEigenvalues); undefined(() -> rectangle.eigenspace(Rational.ONE));
        undefined(() -> rectangle.generalizedEigenspace(Rational.ONE)); undefined(() -> rectangle.eigenvalueMultiplicity(Rational.ONE));
        undefined(rectangle::isDiagonalizableOverRationals); undefined(rectangle::diagonalizeOverRationals);
        RationalMatrix oversized=RationalMatrix.identity(33);
        limit(oversized::characteristicPolynomial); limit(oversized::minimalPolynomial); limit(oversized::isDiagonalizableOverRationals);
        limit(() -> oversized.evaluatePolynomial(Polynomial.ONE)); limit(() -> oversized.pow(BigInteger.ZERO));
        Rational[] coefficients=new Rational[10002]; Arrays.fill(coefficients,Rational.ZERO); coefficients[10001]=Rational.ONE;
        Polynomial large=new Polynomial(coefficients); limit(() -> RationalMatrix.identity(1).evaluatePolynomial(large));
        limit(() -> RationalMatrix.companion(large)); limit(large::rationalRoots); limit(() -> large.rootMultiplicity(Rational.ZERO));
        limit(() -> RationalMatrix.identity(1).pow(BigInteger.TEN.pow(100)));
        Rational[] longCoefficients=new Rational[201]; Arrays.fill(longCoefficients,Rational.ONE);
        limit(() -> RationalMatrix.identity(32).evaluatePolynomial(new Polynomial(longCoefficients)));
        // The prime 2^127-1 cannot be certified by this bounded trial-division search.
        Polynomial costly=new Polynomial(Rational.of(BigInteger.ONE.shiftLeft(127).subtract(BigInteger.ONE)),Rational.ZERO,Rational.ONE);
        limit(costly::rationalRoots); limit(RationalMatrix.companion(costly)::rationalEigenvalues);
        limit(RationalMatrix.companion(costly)::isDiagonalizableOverRationals);
        limit(RationalMatrix.companion(costly)::diagonalizeOverRationals);
        // Coprime coefficient factors force distinct rational candidates; no deduplication loophole.
        BigInteger numerator=BigInteger.valueOf(6).pow(15),denominator=BigInteger.valueOf(35).pow(15);
        Polynomial tooManyCandidates=new Polynomial(Rational.of(numerator),Rational.ZERO,Rational.of(denominator));
        limit(tooManyCandidates::rationalRoots);
        BigInteger tooManyDivisors=BigInteger.valueOf(210).pow(16);
        limit(() -> new Polynomial(Rational.of(tooManyDivisors),Rational.ZERO,Rational.ONE).rationalRoots());
        Rational[] denseScan=new Rational[65]; Arrays.fill(denseScan,Rational.ZERO);
        denseScan[0]=Rational.of(BigInteger.valueOf(6).pow(10)); denseScan[64]=Rational.of(BigInteger.valueOf(35).pow(10));
        limit(() -> new Polynomial(denseScan).rationalRoots());
    }

    @Test public void largeExactCoefficientsRemainRationalWithoutRounding() {
        Rational huge=Rational.of(BigInteger.TEN.pow(80)),fraction=Rational.of(7,13);
        RationalMatrix matrix=new RationalMatrix(new Rational[][]{{huge,fraction},{Rational.ZERO,huge}});
        Polynomial factor=new Polynomial(huge.negate(),Rational.ONE);
        assertEquals(factor.multiply(factor),matrix.characteristicPolynomial()); assertEquals(factor.multiply(factor),matrix.minimalPolynomial());
        assertEquals(2,matrix.eigenvalueMultiplicity(huge)); assertEquals(Collections.singletonList(v(1,0)),matrix.eigenspace(huge));
        assertEquals(2,matrix.generalizedEigenspace(huge).size());
        assertEquals(Collections.singletonList(huge),factor.rationalRoots());
    }

    @Test public void nativeOperationsUseOriginalInterfacesAndActualResultCarriers() {
        ConcreteMathematics math=new ConcreteMathematics(); RationalMatrix matrix=m(new long[]{2,1},new long[]{0,3});
        IAlgebraItem<RationalMatrix> fixed=math.matrices.algebra().buildAlgebraItem(matrix),family=math.rectangularMatrices.algebra().buildAlgebraItem(matrix);
        IAlgebraItem<Polynomial> characteristic=family.performAlgebraTransfer("characteristic-polynomial");
        assertSame(math.polynomials.algebra(),characteristic.getAlgebra()); assertEquals(p(6,-5,1),characteristic.getResult());
        assertSame(math.mathTool.getAlgebra("Q[x]"),characteristic.getAlgebra());
        Polynomial polynomial=p(1,2,1); RationalMatrix expected=m(new long[]{9,7},new long[]{0,16});
        assertEquals(expected,fixed.performCustomMemberOperation("evaluate-polynomial",polynomial).getResult());
        IAlgebraItem<RationalMatrix> evaluated=math.polynomials.algebra().buildAlgebraItem(polynomial)
                .performLeftProjectionOperation("Mat(Q).evaluate-at-matrix",matrix);
        assertSame(math.rectangularMatrices.algebra(),evaluated.getAlgebra()); assertEquals(expected,evaluated.getResult());
        IAlgebraItem<RationalMatrix> evaluatedFixed=math.polynomials.algebra().buildAlgebraItem(polynomial)
                .performLeftProjectionOperation("Mat2(Q).evaluate-at-matrix",matrix);
        assertSame(math.matrices.algebra(),evaluatedFixed.getAlgebra()); assertEquals(expected,evaluatedFixed.getResult());
        List<IAlgebraItem<RationalVector>> basis=family.performUnsafeFlatOperation("eigenspace-basis",Rational.of(3));
        assertEquals(1,basis.size()); assertSame(math.finiteVectors.algebra(),basis.get(0).getAlgebra()); assertEquals(v(1,1),basis.get(0).getResult());
        List<IAlgebraItem<RationalVector>> fixedBasis=fixed.performUnsafeFlatOperation("eigenspace-basis",Rational.of(3));
        assertSame(math.vectors.algebra(),fixedBasis.get(0).getAlgebra());
        IAlgebraItem<BigInteger> multiplicity=family.performUnsafeOperation("eigenvalue-multiplicity",Rational.of(3));
        assertSame(math.naturals.algebra(),multiplicity.getAlgebra()); assertEquals(BigInteger.ONE,multiplicity.getResult());
        IAlgebraItem<RationalMatrix> companion=characteristic.performAlgebraTransfer("Mat(Q).companion");
        assertSame(math.rectangularMatrices.algebra(),companion.getAlgebra()); assertEquals(p(6,-5,1),companion.getResult().minimalPolynomial());
        assertEquals(2,family.performOneOperandFlatOperation("diagonalize-over-q").size());
        assertEquals(Collections.emptyList(),family.performUnsafeFlatOperation("eigenspace-basis",Rational.ZERO));
        undefined(() -> math.rectangularMatrices.algebra().buildAlgebraItem(m(new long[]{1,2})).performAlgebraTransfer("characteristic-polynomial"));
        assertEquals(Collections.singletonList(Rational.of(-1)),values(math.polynomials.algebra().buildAlgebraItem(polynomial).performAlgebraFlatTransfer("rational-roots")));
    }
    private static <T> List<T> values(List<IAlgebraItem<T>> items) {
        List<T> values=new ArrayList<>(); for(IAlgebraItem<T> item : items) values.add(item.getResult()); return values;
    }
    private static IAlgebraFlow<?> serialized(IAlgebraFlow<?> flow) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { return (IAlgebraFlow<?>)in.readObject(); }
    }
    @Test public void serializedScalarAndFlatFlowsComposeThroughMathTool() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics(); RationalMatrix matrix=m(new long[]{2,1},new long[]{0,3});
        IAlgebraFlow<Rational> roots=math.flow(math.rectangularMatrices,Collections.singletonList(matrix))
                .<Polynomial>performAlgebraTransfer("characteristic-polynomial").<Rational>performFlatAlgebraTransfer("rational-roots");
        IAlgebraFlow<?> restored=serialized(roots); assertEquals(Arrays.asList("2","3"),restored.collect()); assertEquals(restored.collect(),restored.collect());
        IAlgebraFlow<Rational> eigenvector=math.flow(math.matrices,Collections.singletonList(matrix))
                .<RationalVector,Rational>performFlatAlgebraUnsafe("eigenspace-basis",Rational.of(3))
                .<Rational>performCustomResultOperation("dot",v(1,2));
        assertEquals(Collections.singletonList("3"),serialized(eigenvector).collect());
        IAlgebraFlow<RationalMatrix> evaluate=math.flow(math.polynomials,Collections.singletonList(p(1,2,1)))
                .performLeftProjectionOperation("Mat(Q).evaluate-at-matrix",matrix);
        assertEquals(Collections.singletonList("[[9, 7], [0, 16]]"),serialized(evaluate).collect());
        IAlgebraFlow<RationalVector> decomposition=math.flow(math.rectangularMatrices,Collections.singletonList(matrix))
                .performOneOperandFlatOperation("diagonalize-over-q").performLeftProjectionOperation("apply",v(1,1));
        assertEquals(Arrays.asList("[2, 1]","[2, 3]"),serialized(decomposition).collect());
    }
}
