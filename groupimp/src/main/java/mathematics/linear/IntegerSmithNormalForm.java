package mathematics.linear;

import mathematics.core.MathFailure;
import java.math.BigInteger;
import java.util.*;

/** Positive nonzero Smith invariant factors; no change-of-basis matrices are returned. */
public final class IntegerSmithNormalForm {
    public static final int MAX_DIMENSION=256;
    private IntegerSmithNormalForm() {}
    public static List<BigInteger> invariantFactors(BigInteger[][] matrix) { return new Computation().invariantFactors(matrix); }
    /** A shared arithmetic budget for several boundary matrices in one homology computation. */
    public static final class Computation {
        private long remaining=5000000;
        private void use(long amount) {
            remaining-=amount;
            if(remaining<0) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Smith reduction exceeds 5000000 integer work units");
        }
        public List<BigInteger> invariantFactors(BigInteger[][] input) {
            int rows=input.length,columns=rows==0?0:input[0].length;
            if(rows>MAX_DIMENSION || columns>MAX_DIMENSION)
                throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Smith reduction allows at most 256 rows and columns");
            use((long)rows*columns); BigInteger[][] a=new BigInteger[rows][columns];
            for(int r=0;r<rows;r++) {
                if(input[r].length!=columns) throw MathFailure.invalid("Ragged integer matrix");
                for(int c=0;c<columns;c++) a[r][c]=Objects.requireNonNull(input[r][c]);
            }
            List<BigInteger> result=new ArrayList<>();
            for(int k=0;k<Math.min(rows,columns);k++) {
                use((long)(rows-k)*(columns-k)); int pivotRow=-1,pivotColumn=-1;
                for(int r=k;r<rows;r++) for(int c=k;c<columns;c++) if(a[r][c].signum()!=0
                        && (pivotRow<0 || a[r][c].abs().compareTo(a[pivotRow][pivotColumn].abs())<0)) { pivotRow=r; pivotColumn=c; }
                if(pivotRow<0) break;
                swapRows(a,k,pivotRow); swapColumns(a,k,pivotColumn);
                while(true) {
                    use(rows+columns); boolean restart=false;
                    for(int r=k+1;r<rows;r++) if(a[r][k].signum()!=0) {
                        BigInteger quotient=a[r][k].divide(a[k][k]); use(columns-k);
                        for(int c=k;c<columns;c++) a[r][c]=a[r][c].subtract(quotient.multiply(a[k][c]));
                        if(a[r][k].signum()!=0) { swapRows(a,k,r); restart=true; break; }
                    }
                    if(restart) continue;
                    for(int c=k+1;c<columns;c++) if(a[k][c].signum()!=0) {
                        BigInteger quotient=a[k][c].divide(a[k][k]); use(rows-k);
                        for(int r=k;r<rows;r++) a[r][c]=a[r][c].subtract(quotient.multiply(a[r][k]));
                        if(a[k][c].signum()!=0) { swapColumns(a,k,c); restart=true; break; }
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
                }
                result.add(a[k][k].abs());
            }
            return Collections.unmodifiableList(result);
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
