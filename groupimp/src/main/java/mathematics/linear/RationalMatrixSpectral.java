package mathematics.linear;

import mathematics.calculus.Polynomial;
import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;

/** Exact, bounded polynomial calculus on square rational matrices. */
final class RationalMatrixSpectral {
    static final int MAX_DIMENSION=32, MAX_POWER=10000, MAX_POLYNOMIAL_DEGREE=10000;
    private RationalMatrixSpectral() {}
    private static final class Work {
        long remaining=5000000;
        void use(long amount) {
            remaining-=amount;
            if(remaining<0) throw limit("5000000 arithmetic work units");
        }
    }
    private static MathFailure limit(String detail) {
        return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Matrix polynomial calculus exceeds "+detail);
    }
    private static int dimension(RationalMatrix matrix) {
        if(matrix.rows()!=matrix.columns()) throw MathFailure.undefined("Matrix polynomial calculus requires a square matrix");
        if(matrix.rows()>MAX_DIMENSION) throw limit("dimension 32");
        return matrix.rows();
    }
    private static RationalMatrix multiply(RationalMatrix a,RationalMatrix b,Work work) {
        work.use((long)a.rows()*a.columns()*b.columns()); return a.multiply(b);
    }
    private static RationalMatrix shift(RationalMatrix matrix,Rational scalar,Work work) {
        int n=matrix.rows(); work.use((long)n*n);
        Rational[][] result=new Rational[n][n];
        for(int r=0;r<n;r++) for(int c=0;c<n;c++) result[r][c]=r==c?matrix.get(r,c).add(scalar):matrix.get(r,c);
        return new RationalMatrix(result);
    }
    static Polynomial characteristic(RationalMatrix matrix) { dimension(matrix); return characteristic(matrix,new Work()); }
    private static Polynomial characteristic(RationalMatrix matrix,Work work) {
        int n=matrix.rows(); Rational[] coefficients=new Rational[n+1]; coefficients[n]=Rational.ONE;
        RationalMatrix b=RationalMatrix.identity(n);
        // Faddeev-LeVerrier: B0=I, ck=-tr(A*B(k-1))/k, Bk=A*B(k-1)+ck*I.
        for(int k=1;k<=n;k++) {
            b=multiply(matrix,b,work); work.use(n);
            Rational coefficient=b.trace().negate().divide(Rational.of(k)); coefficients[n-k]=coefficient;
            if(k<n) b=shift(b,coefficient,work);
        }
        return new Polynomial(coefficients);
    }
    static Polynomial minimal(RationalMatrix matrix) {
        int n=dimension(matrix),size=n*n; Work work=new Work();
        List<Rational[]> basis=new ArrayList<>(),expressions=new ArrayList<>(); List<Integer> pivots=new ArrayList<>();
        RationalMatrix power=RationalMatrix.identity(n);
        // First dependence among I,A,...,A^n, keeping the same row operations on their polynomials.
        for(int degree=0;degree<=n;degree++) {
            Rational[] coordinates=new Rational[size],expression=new Rational[n+1]; Arrays.fill(expression,Rational.ZERO);
            expression[degree]=Rational.ONE;
            for(int r=0;r<n;r++) for(int c=0;c<n;c++) coordinates[r*n+c]=power.get(r,c);
            for(int j=0;j<basis.size();j++) {
                Rational factor=coordinates[pivots.get(j)];
                if(factor.signum()==0) continue;
                work.use(size+n+1);
                for(int i=0;i<size;i++) coordinates[i]=coordinates[i].subtract(factor.multiply(basis.get(j)[i]));
                for(int i=0;i<=n;i++) expression[i]=expression[i].subtract(factor.multiply(expressions.get(j)[i]));
            }
            int pivot=0; while(pivot<size && coordinates[pivot].signum()==0) pivot++;
            if(pivot==size) return new Polynomial(expression).monic();
            Rational leading=coordinates[pivot]; work.use(size+n+1);
            for(int i=0;i<size;i++) coordinates[i]=coordinates[i].divide(leading);
            for(int i=0;i<=n;i++) expression[i]=expression[i].divide(leading);
            basis.add(coordinates); expressions.add(expression); pivots.add(pivot);
            if(degree<n) power=multiply(power,matrix,work);
        }
        throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"No matrix power dependence found through its dimension");
    }
    static RationalMatrix evaluate(RationalMatrix matrix,Polynomial polynomial) {
        int n=dimension(matrix); Work work=new Work();
        if(polynomial.degree()>MAX_POLYNOMIAL_DEGREE) throw limit("polynomial degree 10000");
        RationalMatrix result=RationalMatrix.identity(n).scale(polynomial.coefficient(Math.max(0,polynomial.degree())));
        for(int k=polynomial.degree()-1;k>=0;k--) result=shift(multiply(result,matrix,work),polynomial.coefficient(k),work);
        return result;
    }
    static RationalMatrix power(RationalMatrix matrix,BigInteger exponent) {
        dimension(matrix);
        if(exponent.signum()<0) throw MathFailure.undefined("Matrix power requires a nonnegative exponent");
        if(exponent.compareTo(BigInteger.valueOf(MAX_POWER))>0) throw limit("exponent 10000");
        return power(matrix,exponent.intValueExact(),new Work());
    }
    private static RationalMatrix power(RationalMatrix matrix,int exponent,Work work) {
        RationalMatrix result=RationalMatrix.identity(matrix.rows()),factor=matrix;
        while(exponent>0) {
            if((exponent&1)!=0) result=multiply(result,factor,work);
            exponent>>=1;
            if(exponent>0) factor=multiply(factor,factor,work);
        }
        return result;
    }
    static RationalMatrix companion(Polynomial polynomial) {
        int n=polynomial.degree();
        if(n<1) throw MathFailure.undefined("A companion matrix requires a polynomial of positive degree");
        if(n>MAX_DIMENSION) throw limit("companion degree 32");
        Rational[][] values=new Rational[n][n]; for(Rational[] row : values) Arrays.fill(row,Rational.ZERO);
        for(int i=1;i<n;i++) values[i][i-1]=Rational.ONE;
        for(int i=0;i<n;i++) values[i][n-1]=polynomial.coefficient(i).negate().divide(polynomial.coefficient(n));
        return new RationalMatrix(values);
    }
    static List<Rational> eigenvalues(RationalMatrix matrix) { return characteristic(matrix).rationalRoots(); }
    static int multiplicity(RationalMatrix matrix,Rational eigenvalue) { return characteristic(matrix).rootMultiplicity(eigenvalue); }
    static List<RationalVector> eigenspace(RationalMatrix matrix,Rational eigenvalue,boolean generalized) {
        dimension(matrix); return eigenspace(matrix,eigenvalue,generalized,new Work());
    }
    private static List<RationalVector> eigenspace(RationalMatrix matrix,Rational eigenvalue,boolean generalized,Work work) {
        RationalMatrix shifted=shift(matrix,eigenvalue.negate(),work);
        if(generalized) shifted=power(shifted,matrix.rows(),work);
        work.use(2L*matrix.rows()*matrix.rows()*matrix.rows());
        return shifted.nullspace();
    }
    /** Null means a proved absence of a rational eigenbasis; resource failures propagate. */
    private static List<RationalMatrix> decomposition(RationalMatrix matrix) {
        int n=dimension(matrix); Work work=new Work(); Polynomial characteristic=characteristic(matrix,work);
        List<RationalVector> vectors=new ArrayList<>(); List<Rational> values=new ArrayList<>();
        for(Rational eigenvalue : characteristic.rationalRoots()) {
            List<RationalVector> space=eigenspace(matrix,eigenvalue,false,work);
            if(space.size()!=characteristic.rootMultiplicity(eigenvalue)) return null;
            vectors.addAll(space); for(int i=0;i<space.size();i++) values.add(eigenvalue);
        }
        if(vectors.size()!=n) return null;
        Rational[][] p=new Rational[n][n],d=new Rational[n][n];
        for(int r=0;r<n;r++) for(int c=0;c<n;c++) {
            p[r][c]=vectors.get(c).get(r); d[r][c]=r==c?values.get(r):Rational.ZERO;
        }
        return Collections.unmodifiableList(Arrays.asList(new RationalMatrix(p),new RationalMatrix(d)));
    }
    static boolean diagonalizable(RationalMatrix matrix) { return decomposition(matrix)!=null; }
    static List<RationalMatrix> diagonalize(RationalMatrix matrix) {
        List<RationalMatrix> result=decomposition(matrix);
        if(result==null) throw MathFailure.undefined("Matrix has no eigenbasis over the rational field");
        return result;
    }
}
