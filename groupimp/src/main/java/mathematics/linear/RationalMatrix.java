package mathematics.linear;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import mathematics.calculus.Polynomial;
import java.math.BigInteger;
import java.io.Serializable;
import java.util.*;

/** Exact dense matrices with positive dimensions. */
public final class RationalMatrix implements Serializable {
    private static final long serialVersionUID=1L;
    private final Rational[][] entries;
    private final int rows, columns;
    public RationalMatrix(Rational[][] input) {
        if (input.length==0 || input[0].length==0) throw MathFailure.invalid("This matrix representation requires positive dimensions");
        rows=input.length; columns=input[0].length; entries=new Rational[rows][columns];
        for (int r=0;r<rows;r++) {
            if (input[r].length!=columns) throw MathFailure.invalid("Ragged matrix");
            for (int c=0;c<columns;c++) entries[r][c]=Objects.requireNonNull(input[r][c]);
        }
    }
    public int rows() { return rows; }
    public int columns() { return columns; }
    /** Monic det(x*I-A), for a square matrix. */
    public Polynomial characteristicPolynomial() { return RationalMatrixSpectral.characteristic(this); }
    public Polynomial minimalPolynomial() { return RationalMatrixSpectral.minimal(this); }
    public RationalMatrix evaluatePolynomial(Polynomial polynomial) { return RationalMatrixSpectral.evaluate(this,polynomial); }
    public RationalMatrix pow(BigInteger exponent) { return RationalMatrixSpectral.power(this,exponent); }
    public static RationalMatrix companion(Polynomial polynomial) { return RationalMatrixSpectral.companion(polynomial); }
    /** Distinct rational eigenvalues, sorted; the list need not contain the entire complex spectrum. */
    public List<Rational> rationalEigenvalues() { return RationalMatrixSpectral.eigenvalues(this); }
    public int eigenvalueMultiplicity(Rational eigenvalue) { return RationalMatrixSpectral.multiplicity(this,eigenvalue); }
    public List<RationalVector> eigenspace(Rational eigenvalue) { return RationalMatrixSpectral.eigenspace(this,eigenvalue,false); }
    /** Kernel of (A-lambda*I)^n, including the zero subspace for a non-eigenvalue. */
    public List<RationalVector> generalizedEigenspace(Rational eigenvalue) { return RationalMatrixSpectral.eigenspace(this,eigenvalue,true); }
    public boolean isDiagonalizableOverRationals() { return RationalMatrixSpectral.diagonalizable(this); }
    /** Emit [P,D], with eigenvectors in P's columns and A*P=P*D; requires a rational eigenbasis. */
    public List<RationalMatrix> diagonalizeOverRationals() { return RationalMatrixSpectral.diagonalize(this); }
    public Rational get(int r,int c) { return entries[r][c]; }
    public RationalVector row(int index) { return new RationalVector(entries[index]); }
    public RationalVector column(int index) {
        Rational[] values=new Rational[rows]; for(int r=0;r<rows;r++) values[r]=get(r,index);
        return new RationalVector(values);
    }
    public List<RationalVector> rowVectors() {
        List<RationalVector> result=new ArrayList<>(); for(int r=0;r<rows;r++) result.add(row(r));
        return Collections.unmodifiableList(result);
    }
    public List<RationalVector> columnVectors() {
        List<RationalVector> result=new ArrayList<>(); for(int c=0;c<columns;c++) result.add(column(c));
        return Collections.unmodifiableList(result);
    }
    public RationalMatrix scale(Rational scalar) {
        Rational[][] values=copy();
        for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) values[r][c]=values[r][c].multiply(scalar);
        return new RationalMatrix(values);
    }
    public RationalMatrix rref() { return new RationalRowReduction(this,null).matrix; }
    public List<Integer> pivotColumns() { return new RationalRowReduction(this,null).pivots; }
    public List<RationalVector> nullspace() { return new RationalRowReduction(this,null).nullspace(); }
    public List<RationalVector> rowSpace() {
        RationalRowReduction reduction=new RationalRowReduction(this,null); List<RationalVector> result=new ArrayList<>();
        for(int r=0;r<reduction.pivots.size();r++) result.add(reduction.matrix.row(r));
        return Collections.unmodifiableList(result);
    }
    public List<RationalVector> columnSpace() {
        List<RationalVector> result=new ArrayList<>(); for(int c : pivotColumns()) result.add(column(c));
        return Collections.unmodifiableList(result);
    }
    public RationalAffineSpace solve(RationalVector rhs) { return RationalAffineSpace.solve(this,rhs); }
    /** Moore-Penrose inverse in the standard rational Euclidean inner products. */
    public RationalMatrix pseudoinverse() {
        RationalRowReduction reduction=new RationalRowReduction(this,null); int rank=reduction.pivots.size();
        if(rank==0) return transpose(); // The zero matrix's pseudoinverse is the transposed zero matrix.
        Rational[][] columns=new Rational[rows][rank],rowCoordinates=new Rational[rank][this.columns];
        for(int i=0;i<rank;i++) {
            for(int r=0;r<rows;r++) columns[r][i]=get(r,reduction.pivots.get(i));
            for(int c=0;c<this.columns;c++) rowCoordinates[i][c]=reduction.matrix.get(i,c);
        }
        // A=C*R with C of full column rank and R of full row rank; A+=R+*C+.
        RationalMatrix c=new RationalMatrix(columns),r=new RationalMatrix(rowCoordinates);
        RationalMatrix ct=c.transpose(),rt=r.transpose();
        return rt.multiply(r.multiply(rt).inverse()).multiply(ct.multiply(c).inverse()).multiply(ct);
    }
    public RationalMatrix columnProjector() { return multiply(pseudoinverse()); }
    public RationalMatrix rowProjector() { return pseudoinverse().multiply(this); }
    private void rightHandSide(RationalVector rhs) {
        if(rhs.dimension()!=rows) throw MathFailure.undefined("Right-hand side dimension must equal the number of matrix rows");
    }
    /** All minimizers of ||A*x-b||^2, represented as an affine solution set of the normal equations. */
    public RationalAffineSpace leastSquares(RationalVector rhs) {
        rightHandSide(rhs); RationalMatrix transpose=transpose();
        return transpose.multiply(this).solve(transpose.multiply(rhs));
    }
    /** The unique least-squares minimizer with smallest squared Euclidean norm. */
    public RationalVector minimumNormLeastSquares(RationalVector rhs) {
        rightHandSide(rhs); return pseudoinverse().multiply(rhs);
    }
    public RationalVector projectColumn(RationalVector point) { return multiply(minimumNormLeastSquares(point)); }
    /** Residual convention: b minus its fitted value A*x. */
    public RationalVector leastSquaresResidual(RationalVector rhs) { return rhs.add(projectColumn(rhs).scale(Rational.of(-1))); }
    public Rational leastSquaresError(RationalVector rhs) {
        RationalVector residual=leastSquaresResidual(rhs); return residual.dot(residual);
    }
    private Rational[][] copy() { Rational[][] copy=new Rational[rows][]; for(int r=0;r<rows;r++) copy[r]=entries[r].clone(); return copy; }
    private void square() { if (rows!=columns) throw MathFailure.invalid("A square matrix is required"); }
    public static RationalMatrix identity(int n) {
        if (n<=0) throw MathFailure.invalid("Positive dimension required");
        Rational[][] values=new Rational[n][n];
        for(int r=0;r<n;r++) for(int c=0;c<n;c++) values[r][c]=r==c?Rational.ONE:Rational.ZERO;
        return new RationalMatrix(values);
    }
    public RationalMatrix add(RationalMatrix b) {
        if(rows!=b.rows || columns!=b.columns) throw MathFailure.invalid("Matrix shapes differ");
        Rational[][] result=copy();
        for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) result[r][c]=get(r,c).add(b.get(r,c));
        return new RationalMatrix(result);
    }
    public RationalMatrix multiply(RationalMatrix b) {
        if(columns!=b.rows) throw MathFailure.invalid("Incompatible matrix product");
        Rational[][] result=new Rational[rows][b.columns];
        for(int r=0;r<rows;r++) for(int c=0;c<b.columns;c++) {
            Rational sum=Rational.ZERO;
            for(int k=0;k<columns;k++) sum=sum.add(get(r,k).multiply(b.get(k,c)));
            result[r][c]=sum;
        }
        return new RationalMatrix(result);
    }
    public RationalVector multiply(RationalVector b) {
        if(columns!=b.dimension()) throw MathFailure.invalid("Incompatible matrix/vector product");
        Rational[] result=new Rational[rows];
        for(int r=0;r<rows;r++) {
            result[r]=Rational.ZERO;
            for(int c=0;c<columns;c++) result[r]=result[r].add(get(r,c).multiply(b.get(c)));
        }
        return new RationalVector(result);
    }
    public RationalMatrix transpose() {
        Rational[][] result=new Rational[columns][rows];
        for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) result[c][r]=get(r,c);
        return new RationalMatrix(result);
    }
    public Rational trace() { square(); Rational result=Rational.ZERO; for(int i=0;i<rows;i++) result=result.add(get(i,i)); return result; }
    public Rational determinant() {
        square(); Rational[][] a=copy(); Rational determinant=Rational.ONE;
        for(int c=0;c<columns;c++) {
            int pivot=c; while(pivot<rows && a[pivot][c].signum()==0) pivot++;
            if(pivot==rows) return Rational.ZERO;
            if(pivot!=c) { Rational[] row=a[c]; a[c]=a[pivot]; a[pivot]=row; determinant=determinant.negate(); }
            Rational value=a[c][c]; determinant=determinant.multiply(value);
            for(int r=c+1;r<rows;r++) {
                Rational factor=a[r][c].divide(value);
                for(int j=c+1;j<columns;j++) a[r][j]=a[r][j].subtract(factor.multiply(a[c][j]));
                a[r][c]=Rational.ZERO;
            }
        }
        return determinant;
    }
    public int rank() {
        Rational[][] a=copy(); int rank=0;
        for(int c=0;c<columns && rank<rows;c++) {
            int pivot=rank; while(pivot<rows && a[pivot][c].signum()==0) pivot++;
            if(pivot==rows) continue;
            Rational[] row=a[rank]; a[rank]=a[pivot]; a[pivot]=row;
            for(int r=rank+1;r<rows;r++) {
                Rational factor=a[r][c].divide(a[rank][c]);
                for(int j=c;j<columns;j++) a[r][j]=a[r][j].subtract(factor.multiply(a[rank][j]));
            }
            rank++;
        }
        return rank;
    }
    public RationalMatrix inverse() {
        square(); Rational[][] a=copy(), inverse=identity(rows).copy();
        for(int c=0;c<columns;c++) {
            int pivot=c; while(pivot<rows && a[pivot][c].signum()==0) pivot++;
            if(pivot==rows) throw MathFailure.undefined("Singular matrix has no inverse");
            Rational[] row=a[c]; a[c]=a[pivot]; a[pivot]=row;
            row=inverse[c]; inverse[c]=inverse[pivot]; inverse[pivot]=row;
            Rational value=a[c][c];
            for(int j=0;j<columns;j++) { a[c][j]=a[c][j].divide(value); inverse[c][j]=inverse[c][j].divide(value); }
            for(int r=0;r<rows;r++) if(r!=c) {
                Rational factor=a[r][c];
                for(int j=0;j<columns;j++) {
                    a[r][j]=a[r][j].subtract(factor.multiply(a[c][j]));
                    inverse[r][j]=inverse[r][j].subtract(factor.multiply(inverse[c][j]));
                }
            }
        }
        return new RationalMatrix(inverse);
    }
    @Override public boolean equals(Object b) { return b instanceof RationalMatrix && Arrays.deepEquals(entries,((RationalMatrix)b).entries); }
    @Override public int hashCode() { return Arrays.deepHashCode(entries); }
    @Override public String toString() { return Arrays.deepToString(entries); }
}
