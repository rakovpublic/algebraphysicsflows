package mathematics.linear;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import mathematics.structures.AbelianGroupType;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Exact matrices Z^columns -> Z^rows, retaining zero-sized dimensions. */
public final class IntegerMatrix implements Serializable {
    private static final long serialVersionUID=1L;
    private final int rows,columns;
    private final BigInteger[][] entries;
    static MathFailure limit() { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Integer linear algebra allows dimensions at most 256"); }
    static void checkDimension(int dimension) {
        if(dimension<0) throw MathFailure.invalid("A coordinate dimension cannot be negative");
        if(dimension>IntegerSmithNormalForm.MAX_DIMENSION) throw limit();
    }
    public IntegerMatrix(BigInteger[][] values) { this(values.length,values.length==0?0:values[0].length,values); }
    public IntegerMatrix(int rows,int columns,BigInteger[][] values) {
        checkDimension(rows); checkDimension(columns);
        if(values.length!=rows) throw MathFailure.invalid("Integer matrix row count does not match its shape");
        this.rows=rows; this.columns=columns; entries=new BigInteger[rows][columns];
        for(int r=0;r<rows;r++) {
            if(values[r].length!=columns) throw MathFailure.invalid("Ragged integer matrix");
            for(int c=0;c<columns;c++) entries[r][c]=Objects.requireNonNull(values[r][c]);
        }
    }
    public int rows() { return rows; }
    public int columns() { return columns; }
    public BigInteger get(int row,int column) { return entries[row][column]; }
    BigInteger[][] copyEntries() { BigInteger[][] result=new BigInteger[rows][]; for(int r=0;r<rows;r++) result[r]=entries[r].clone(); return result; }
    static BigInteger[][] zeros(int rows,int columns) {
        checkDimension(rows); checkDimension(columns); BigInteger[][] result=new BigInteger[rows][columns];
        for(BigInteger[] row : result) Arrays.fill(row,BigInteger.ZERO); return result;
    }
    public static IntegerMatrix zero(int rows,int columns) { return new IntegerMatrix(rows,columns,zeros(rows,columns)); }
    public static IntegerMatrix identity(int dimension) {
        BigInteger[][] values=zeros(dimension,dimension); for(int i=0;i<dimension;i++) values[i][i]=BigInteger.ONE;
        return new IntegerMatrix(dimension,dimension,values);
    }
    public IntegerMatrix add(IntegerMatrix other) {
        if(rows!=other.rows || columns!=other.columns) throw MathFailure.undefined("Integer matrix shapes must agree");
        BigInteger[][] result=zeros(rows,columns);
        for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) result[r][c]=get(r,c).add(other.get(r,c));
        return new IntegerMatrix(rows,columns,result);
    }
    public IntegerMatrix scale(BigInteger scalar) {
        BigInteger[][] result=zeros(rows,columns);
        for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) result[r][c]=get(r,c).multiply(scalar);
        return new IntegerMatrix(rows,columns,result);
    }
    public IntegerMatrix transpose() {
        BigInteger[][] result=zeros(columns,rows); for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) result[c][r]=get(r,c);
        return new IntegerMatrix(columns,rows,result);
    }
    public IntegerMatrix multiply(IntegerMatrix other) { return multiply(other,new IntegerSmithNormalForm.Computation()); }
    IntegerMatrix multiply(IntegerMatrix other,IntegerSmithNormalForm.Computation work) {
        if(columns!=other.rows) throw MathFailure.undefined("Integer matrix multiplication requires matching inner dimensions");
        work.use((long)rows*columns*other.columns); BigInteger[][] result=zeros(rows,other.columns);
        for(int r=0;r<rows;r++) for(int c=0;c<other.columns;c++) for(int k=0;k<columns;k++)
            result[r][c]=result[r][c].add(get(r,k).multiply(other.get(k,c)));
        return new IntegerMatrix(rows,other.columns,result);
    }
    public IntegerVector multiply(IntegerVector vector) { return multiply(vector,new IntegerSmithNormalForm.Computation()); }
    IntegerVector multiply(IntegerVector vector,IntegerSmithNormalForm.Computation work) {
        if(columns!=vector.dimension()) throw MathFailure.undefined("Integer matrix columns must match the vector dimension");
        work.use((long)rows*columns); BigInteger[] result=new BigInteger[rows]; Arrays.fill(result,BigInteger.ZERO);
        for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) result[r]=result[r].add(get(r,c).multiply(vector.get(c)));
        return new IntegerVector(result);
    }
    public IntegerVector column(int index) {
        if(index<0 || index>=columns) throw MathFailure.undefined("Column index outside the matrix");
        BigInteger[] result=new BigInteger[rows]; for(int r=0;r<rows;r++) result[r]=get(r,index); return new IntegerVector(result);
    }
    public List<IntegerVector> columnVectors() {
        List<IntegerVector> result=new ArrayList<>(); for(int c=0;c<columns;c++) result.add(column(c)); return Collections.unmodifiableList(result);
    }
    public List<IntegerVector> rowVectors() {
        List<IntegerVector> result=new ArrayList<>(); for(BigInteger[] row : entries) result.add(new IntegerVector(row)); return Collections.unmodifiableList(result);
    }
    public List<BigInteger> smithInvariantFactors() { return IntegerSmithNormalForm.invariantFactors(entries); }
    public int rank() { return smithInvariantFactors().size(); }
    public IntegerMatrix smithForm() {
        BigInteger[][] result=zeros(rows,columns); List<BigInteger> factors=smithInvariantFactors();
        for(int i=0;i<factors.size();i++) result[i][i]=factors.get(i); return new IntegerMatrix(rows,columns,result);
    }
    public List<IntegerMatrix> smithDecomposition() {
        IntegerSmithNormalForm.Decomposition smith=new IntegerSmithNormalForm.Computation().decompose(this);
        return Collections.unmodifiableList(Arrays.asList(smith.left(),smith.diagonal(),smith.right()));
    }
    private static List<IntegerVector> kernel(IntegerSmithNormalForm.Decomposition smith) {
        List<IntegerVector> result=new ArrayList<>();
        for(int i=smith.rank();i<smith.right().columns();i++) result.add(smith.right().column(i)); return Collections.unmodifiableList(result);
    }
    public List<IntegerVector> kernelBasis() { return kernel(new IntegerSmithNormalForm.Computation().decompose(this)); }
    public List<IntegerVector> imageBasis() {
        IntegerSmithNormalForm.Computation work=new IntegerSmithNormalForm.Computation();
        IntegerSmithNormalForm.Decomposition smith=work.decompose(this); List<IntegerVector> result=new ArrayList<>();
        for(int i=0;i<smith.rank();i++) result.add(multiply(smith.right().column(i),work)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupType cokernel() {
        List<BigInteger> factors=smithInvariantFactors(); return new AbelianGroupType(BigInteger.valueOf(rows-factors.size()),factors);
    }
    private IntegerSmithNormalForm.Decomposition forRightHandSide(IntegerVector b,IntegerSmithNormalForm.Computation work) {
        if(b.dimension()!=rows) throw MathFailure.undefined("Right-hand side dimension must match matrix rows"); return work.decompose(this);
    }
    /** Coordinates y with D*y=U*b and free coordinates zero, or null when inconsistent over Z. */
    static IntegerVector solveCoordinates(IntegerSmithNormalForm.Decomposition smith,IntegerVector b,IntegerSmithNormalForm.Computation work) {
        IntegerVector transformed=smith.left().multiply(b,work); BigInteger[] y=new BigInteger[smith.right().rows()]; Arrays.fill(y,BigInteger.ZERO);
        for(int i=0;i<transformed.dimension();i++) {
            if(i<smith.rank()) {
                BigInteger[] quotient=transformed.get(i).divideAndRemainder(smith.diagonal().get(i,i));
                if(quotient[1].signum()!=0) return null; y[i]=quotient[0];
            } else if(transformed.get(i).signum()!=0) return null;
        }
        return new IntegerVector(y);
    }
    public boolean hasIntegerSolution(IntegerVector b) {
        IntegerSmithNormalForm.Computation work=new IntegerSmithNormalForm.Computation();
        return solveCoordinates(forRightHandSide(b,work),b,work)!=null;
    }
    public IntegerVector solveParticular(IntegerVector b) { return solveGenerators(b).get(0); }
    /** Emit a particular solution followed by an integral basis of all homogeneous solutions. */
    public List<IntegerVector> solveGenerators(IntegerVector b) {
        IntegerSmithNormalForm.Computation work=new IntegerSmithNormalForm.Computation();
        IntegerSmithNormalForm.Decomposition smith=forRightHandSide(b,work); IntegerVector y=solveCoordinates(smith,b,work);
        if(y==null) throw MathFailure.undefined("This linear system has no integer solution");
        List<IntegerVector> result=new ArrayList<>(); result.add(smith.right().multiply(y,work)); result.addAll(kernel(smith));
        return Collections.unmodifiableList(result);
    }
    public IntegerMatrix inverseUnimodular() {
        return inverseUnimodular(new IntegerSmithNormalForm.Computation());
    }
    IntegerMatrix inverseUnimodular(IntegerSmithNormalForm.Computation work) {
        if(rows!=columns) throw MathFailure.undefined("An integer inverse requires a square matrix");
        IntegerSmithNormalForm.Decomposition smith=work.decompose(this);
        if(smith.rank()!=rows) throw MathFailure.undefined("The matrix is not unimodular");
        for(int i=0;i<rows;i++) if(!smith.diagonal().get(i,i).equals(BigInteger.ONE)) throw MathFailure.undefined("The matrix is not unimodular");
        return smith.right().multiply(smith.left(),work);
    }
    public RationalMatrix toRational() {
        if(rows==0 || columns==0) throw MathFailure.undefined("The rational matrix carrier currently requires positive dimensions");
        Rational[][] values=new Rational[rows][columns]; for(int r=0;r<rows;r++) for(int c=0;c<columns;c++) values[r][c]=Rational.of(get(r,c));
        return new RationalMatrix(values);
    }
    public static IntegerMatrix fromRational(RationalMatrix matrix) {
        BigInteger[][] values=zeros(matrix.rows(),matrix.columns());
        for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.columns();c++) {
            if(!matrix.get(r,c).denominator().equals(BigInteger.ONE)) throw MathFailure.undefined("Every matrix entry must be integral");
            values[r][c]=matrix.get(r,c).numerator();
        }
        return new IntegerMatrix(values);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof IntegerMatrix)) return false; IntegerMatrix matrix=(IntegerMatrix)other;
        return rows==matrix.rows && columns==matrix.columns && Arrays.deepEquals(entries,matrix.entries);
    }
    @Override public int hashCode() { return Objects.hash(rows,columns,Arrays.deepHashCode(entries)); }
    @Override public String toString() { return "ZMatrix("+rows+"x"+columns+")"+Arrays.deepToString(entries); }
}
