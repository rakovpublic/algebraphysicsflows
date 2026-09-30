package mathematics.linear;

import mathematics.core.MathFailure;
import java.math.BigInteger;
import java.util.*;

/** Exact Smith reduction, optionally retaining unimodular witnesses U*A*V=D. */
public final class IntegerSmithNormalForm {
    public static final int MAX_DIMENSION=256;
    private IntegerSmithNormalForm() {}
    public static List<BigInteger> invariantFactors(BigInteger[][] matrix) { return new Computation().invariantFactors(matrix); }
    /** Immutable witnesses; diagonal entries are positive nonzero factors followed by zeros. */
    public static final class Decomposition {
        private final IntegerMatrix left,diagonal,right;
        private final List<BigInteger> factors;
        private Decomposition(IntegerMatrix left,IntegerMatrix diagonal,IntegerMatrix right,List<BigInteger> factors) {
            this.left=left; this.diagonal=diagonal; this.right=right; this.factors=Collections.unmodifiableList(factors);
        }
        public IntegerMatrix left() { return left; }
        public IntegerMatrix diagonal() { return diagonal; }
        public IntegerMatrix right() { return right; }
        public int rank() { return factors.size(); }
        public List<BigInteger> invariantFactors() { return factors; }
    }
    /** A shared arithmetic budget for a compound integer linear computation. */
    public static final class Computation {
        private long remaining=5000000;
        public void use(long amount) {
            if(amount<0) throw new IllegalArgumentException("Work charges must be nonnegative");
            if(amount>remaining) {
                remaining=0;
                throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Integer linear computation exceeds 5000000 work units");
            }
            remaining-=amount;
        }
        public List<BigInteger> invariantFactors(BigInteger[][] input) {
            return reduce(input,input.length==0?0:input[0].length,false).invariantFactors();
        }
        public Decomposition decompose(IntegerMatrix matrix) { return reduce(matrix.copyEntries(),matrix.columns(),true); }
        public List<BigInteger> invariantFactors(IntegerMatrix matrix) { return reduce(matrix.copyEntries(),matrix.columns(),false).invariantFactors(); }
        public IntegerMatrix multiply(IntegerMatrix a,IntegerMatrix b) { return a.multiply(b,this); }
        public IntegerVector apply(IntegerMatrix a,IntegerVector b) { return a.multiply(b,this); }
        public IntegerMatrix inverseUnimodular(IntegerMatrix a) { return a.inverseUnimodular(this); }
        public IntegerMatrix kernelMatrix(IntegerMatrix a) {
            Decomposition smith=decompose(a); int columns=a.columns()-smith.rank();
            use((long)a.columns()*columns); BigInteger[][] result=IntegerMatrix.zeros(a.columns(),columns);
            for(int r=0;r<a.columns();r++) for(int c=0;c<columns;c++) result[r][c]=smith.right().get(r,c+smith.rank());
            return new IntegerMatrix(a.columns(),columns,result);
        }
        public boolean hasSolution(IntegerMatrix a,IntegerVector b) {
            if(b.dimension()!=a.rows()) throw MathFailure.undefined("Right-hand side dimension must match matrix rows");
            return IntegerMatrix.solveCoordinates(decompose(a),b,this)!=null;
        }
        public IntegerVector solve(IntegerMatrix a,IntegerVector b) {
            if(b.dimension()!=a.rows()) throw MathFailure.undefined("Right-hand side dimension must match matrix rows");
            Decomposition smith=decompose(a); IntegerVector y=IntegerMatrix.solveCoordinates(smith,b,this);
            if(y==null) throw MathFailure.undefined("This linear system has no integer solution");
            return apply(smith.right(),y);
        }
        /** Solve several right-hand sides with one decomposition and one shared work budget. */
        public IntegerMatrix solve(IntegerMatrix a,IntegerMatrix b) {
            if(b.rows()!=a.rows()) throw MathFailure.undefined("Right-hand side dimension must match matrix rows");
            Decomposition smith=decompose(a); use((long)a.columns()*b.columns());
            BigInteger[][] result=IntegerMatrix.zeros(a.columns(),b.columns());
            for(int c=0;c<b.columns();c++) {
                IntegerVector y=IntegerMatrix.solveCoordinates(smith,b.column(c),this);
                if(y==null) throw MathFailure.undefined("This linear system has no integer solution");
                IntegerVector x=apply(smith.right(),y);
                for(int r=0;r<a.columns();r++) result[r][c]=x.get(r);
            }
            return new IntegerMatrix(a.columns(),b.columns(),result);
        }
        private Decomposition reduce(BigInteger[][] input,int columns,boolean transform) {
            int rows=input.length;
            if(rows>MAX_DIMENSION || columns>MAX_DIMENSION)
                throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Smith reduction allows at most 256 rows and columns");
            use((long)rows*columns); BigInteger[][] a=new BigInteger[rows][columns];
            for(int r=0;r<rows;r++) {
                if(input[r].length!=columns) throw MathFailure.invalid("Ragged integer matrix");
                for(int c=0;c<columns;c++) a[r][c]=Objects.requireNonNull(input[r][c]);
            }
            BigInteger[][] left=null,right=null;
            if(transform) {
                use((long)rows*rows+(long)columns*columns);
                left=IntegerMatrix.identity(rows).copyEntries(); right=IntegerMatrix.identity(columns).copyEntries();
            }
            List<BigInteger> result=new ArrayList<>();
            for(int k=0;k<Math.min(rows,columns);k++) {
                use((long)(rows-k)*(columns-k)); int pivotRow=-1,pivotColumn=-1;
                for(int r=k;r<rows;r++) for(int c=k;c<columns;c++) if(a[r][c].signum()!=0
                        && (pivotRow<0 || a[r][c].abs().compareTo(a[pivotRow][pivotColumn].abs())<0)) { pivotRow=r; pivotColumn=c; }
                if(pivotRow<0) break;
                swapRows(a,k,pivotRow); swapColumns(a,k,pivotColumn);
                if(transform) { swapRows(left,k,pivotRow); swapColumns(right,k,pivotColumn); }
                while(true) {
                    use(rows+columns); boolean restart=false;
                    for(int r=k+1;r<rows;r++) if(a[r][k].signum()!=0) {
                        BigInteger quotient=a[r][k].divide(a[k][k]); use(columns-k);
                        for(int c=k;c<columns;c++) a[r][c]=a[r][c].subtract(quotient.multiply(a[k][c]));
                        if(transform) subtractRow(left,r,k,quotient);
                        if(a[r][k].signum()!=0) { swapRows(a,k,r); if(transform) swapRows(left,k,r); restart=true; break; }
                    }
                    if(restart) continue;
                    for(int c=k+1;c<columns;c++) if(a[k][c].signum()!=0) {
                        BigInteger quotient=a[k][c].divide(a[k][k]); use(rows-k);
                        for(int r=k;r<rows;r++) a[r][c]=a[r][c].subtract(quotient.multiply(a[r][k]));
                        if(transform) subtractColumn(right,c,k,quotient);
                        if(a[k][c].signum()!=0) { swapColumns(a,k,c); if(transform) swapColumns(right,k,c); restart=true; break; }
                    }
                    if(restart) continue;
                    // A diagonal pivot must divide the remaining block, not just its own row/column.
                    int offending=-1; BigInteger divisor=a[k][k].abs();
                    if(!divisor.equals(BigInteger.ONE)) {
                        use((long)(rows-k-1)*(columns-k-1));
                        for(int r=k+1;r<rows && offending<0;r++) for(int c=k+1;c<columns;c++)
                            if(a[r][c].mod(divisor).signum()!=0) { offending=r; break; }
                    }
                    if(offending<0) break;
                    use(columns-k);
                    for(int c=k;c<columns;c++) a[k][c]=a[k][c].add(a[offending][c]);
                    if(transform) subtractRow(left,k,offending,BigInteger.ONE.negate());
                }
                if(transform && a[k][k].signum()<0) {
                    use(columns-k); for(int c=k;c<columns;c++) a[k][c]=a[k][c].negate();
                    subtractRow(left,k,k,BigInteger.valueOf(2));
                }
                result.add(a[k][k].abs());
            }
            return transform?new Decomposition(new IntegerMatrix(left),new IntegerMatrix(rows,columns,a),new IntegerMatrix(right),result)
                    :new Decomposition(null,null,null,result);
        }
        private void subtractRow(BigInteger[][] matrix,int target,int source,BigInteger factor) {
            use(matrix[target].length);
            for(int c=0;c<matrix[target].length;c++) matrix[target][c]=matrix[target][c].subtract(factor.multiply(matrix[source][c]));
        }
        private void subtractColumn(BigInteger[][] matrix,int target,int source,BigInteger factor) {
            use(matrix.length);
            for(BigInteger[] row : matrix) row[target]=row[target].subtract(factor.multiply(row[source]));
        }
        private void swapRows(BigInteger[][] matrix,int a,int b) {
            BigInteger[] row=matrix[a]; matrix[a]=matrix[b]; matrix[b]=row;
        }
        private void swapColumns(BigInteger[][] matrix,int a,int b) {
            use(matrix.length);
            for(BigInteger[] row : matrix) { BigInteger value=row[a]; row[a]=row[b]; row[b]=value; }
        }
    }
}
