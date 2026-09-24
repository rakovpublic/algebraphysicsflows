package mathematics.structures;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerVector;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Z^m modulo the columns of a retained integer relation matrix, with explicit Smith coordinates. */
public final class PresentedAbelianGroup implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_ENUMERATION=4096;
    private final IntegerMatrix relations,toSmith;
    private final List<BigInteger> factors;
    private final AbelianGroupType type;
    public PresentedAbelianGroup(IntegerMatrix relations) {
        this(relations,new IntegerSmithNormalForm.Computation());
    }
    PresentedAbelianGroup(IntegerMatrix relations,IntegerSmithNormalForm.Computation work) {
        this.relations=Objects.requireNonNull(relations);
        IntegerSmithNormalForm.Decomposition smith=work.decompose(relations);
        toSmith=smith.left(); factors=smith.invariantFactors();
        type=new AbelianGroupType(BigInteger.valueOf(relations.rows()-factors.size()),factors);
    }
    public static PresentedAbelianGroup fromType(AbelianGroupType type) {
        BigInteger dimension=type.minimalGenerators();
        if(dimension.compareTo(BigInteger.valueOf(IntegerSmithNormalForm.MAX_DIMENSION))>0) throw dimensionLimit();
        int rows=dimension.intValueExact(),columns=type.invariantFactors().size();
        BigInteger[][] matrix=new BigInteger[rows][columns]; for(BigInteger[] row : matrix) Arrays.fill(row,BigInteger.ZERO);
        for(int i=0;i<columns;i++) matrix[i][i]=type.invariantFactors().get(i);
        return new PresentedAbelianGroup(new IntegerMatrix(rows,columns,matrix));
    }
    private static MathFailure dimensionLimit() { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Abelian presentations allow at most 256 generators and relations"); }
    static int enumerationSize(BigInteger size) {
        if(size.compareTo(BigInteger.valueOf(MAX_ENUMERATION))>0)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Abelian element enumeration exceeds 4096 results");
        return size.intValueExact();
    }
    public IntegerMatrix relations() { return relations; }
    public IntegerMatrix smithCoordinateMap() { return toSmith; }
    public AbelianGroupType type() { return type; }
    public int generatorCount() { return relations.rows(); }
    public int relationCount() { return relations.columns(); }
    public boolean isFinite() { return type.isFinite(); }
    public BigInteger order() { return type.order(); }
    /** Zero denotes an infinite cyclic coordinate; one denotes a killed generator. */
    BigInteger modulus(int index) { return index<factors.size()?factors.get(index):BigInteger.ZERO; }
    IntegerVector normalizeSmith(IntegerVector coordinates) {
        if(coordinates.dimension()!=generatorCount()) throw MathFailure.undefined("Coordinates must match the presentation generator count");
        BigInteger[] result=new BigInteger[generatorCount()];
        for(int i=0;i<result.length;i++) result[i]=modulus(i).signum()==0?coordinates.get(i):coordinates.get(i).mod(modulus(i));
        return new IntegerVector(result);
    }
    public IntegerVector reduce(IntegerVector representative) { return normalizeSmith(toSmith.multiply(representative)); }
    public AbelianGroupElement project(IntegerVector representative) { return fromSmith(reduce(representative)); }
    public AbelianGroupElement fromSmith(IntegerVector coordinates) { return new AbelianGroupElement(this,coordinates); }
    public AbelianGroupElement zero() { return fromSmith(IntegerVector.zero(generatorCount())); }
    IntegerVector lift(IntegerVector coordinates) { return toSmith.solveParticular(coordinates); }
    public List<AbelianGroupElement> generators() {
        List<AbelianGroupElement> result=new ArrayList<>();
        for(int i=0;i<generatorCount();i++) result.add(fromSmith(toSmith.column(i))); return Collections.unmodifiableList(result);
    }
    public List<AbelianGroupElement> smithGenerators() {
        List<AbelianGroupElement> result=new ArrayList<>();
        for(int i=0;i<generatorCount();i++) if(!modulus(i).equals(BigInteger.ONE)) {
            BigInteger[] entries=new BigInteger[generatorCount()]; Arrays.fill(entries,BigInteger.ZERO); entries[i]=BigInteger.ONE;
            result.add(fromSmith(new IntegerVector(entries)));
        }
        return Collections.unmodifiableList(result);
    }
    public List<AbelianGroupElement> elements() {
        int count=enumerationSize(order()); List<AbelianGroupElement> result=new ArrayList<>();
        for(int code=0;code<count;code++) {
            BigInteger remaining=BigInteger.valueOf(code); BigInteger[] coordinates=new BigInteger[generatorCount()];
            for(int i=0;i<coordinates.length;i++) {
                BigInteger[] division=remaining.divideAndRemainder(modulus(i)); coordinates[i]=division[1]; remaining=division[0];
            }
            result.add(fromSmith(new IntegerVector(coordinates)));
        }
        return Collections.unmodifiableList(result);
    }
    public PresentedAbelianGroup directSum(PresentedAbelianGroup other) {
        int rows=generatorCount()+other.generatorCount(),columns=relationCount()+other.relationCount();
        if(rows>IntegerSmithNormalForm.MAX_DIMENSION || columns>IntegerSmithNormalForm.MAX_DIMENSION) throw dimensionLimit();
        BigInteger[][] result=new BigInteger[rows][columns]; for(BigInteger[] row : result) Arrays.fill(row,BigInteger.ZERO);
        for(int r=0;r<generatorCount();r++) for(int c=0;c<relationCount();c++) result[r][c]=relations.get(r,c);
        for(int r=0;r<other.generatorCount();r++) for(int c=0;c<other.relationCount();c++) result[r+generatorCount()][c+relationCount()]=other.relations.get(r,c);
        return new PresentedAbelianGroup(new IntegerMatrix(rows,columns,result));
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof PresentedAbelianGroup)) return false; PresentedAbelianGroup group=(PresentedAbelianGroup)other;
        return relations.equals(group.relations) && toSmith.equals(group.toSmith);
    }
    @Override public int hashCode() { return Objects.hash(relations,toSmith); }
    @Override public String toString() { return "PresentedAbelianGroup("+relations+")"; }
}
