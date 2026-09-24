package mathematics.structures;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.linear.IntegerVector;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A relation-respecting map between retained presentations, canonical in full Smith coordinates. */
public final class AbelianGroupHomomorphism implements Serializable {
    private static final long serialVersionUID=1L;
    private final PresentedAbelianGroup source,target;
    private final IntegerMatrix matrix;
    private AbelianGroupHomomorphism(PresentedAbelianGroup source,PresentedAbelianGroup target,IntegerMatrix matrix,Computation work) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
        if(matrix.rows()!=target.generatorCount() || matrix.columns()!=source.generatorCount())
            throw MathFailure.undefined("A homomorphism matrix has target rows and source columns");
        BigInteger[][] entries=zeros(matrix.rows(),matrix.columns(),work);
        for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.columns();c++) {
            BigInteger modulus=target.modulus(r),value=matrix.get(r,c);
            if(modulus.signum()>0) value=value.mod(modulus);
            BigInteger relation=value.multiply(source.modulus(c));
            if(modulus.signum()>0) relation=relation.mod(modulus);
            if(relation.signum()!=0) throw MathFailure.undefined("The matrix does not send source relations to target relations");
            entries[r][c]=value;
        }
        this.matrix=new IntegerMatrix(matrix.rows(),matrix.columns(),entries);
    }
    public static AbelianGroupHomomorphism fromSmith(PresentedAbelianGroup source,PresentedAbelianGroup target,IntegerMatrix matrix) {
        return new AbelianGroupHomomorphism(source,target,matrix,new Computation());
    }
    /** Matrix columns are images of the original presentation generators. */
    public static AbelianGroupHomomorphism fromMatrix(PresentedAbelianGroup source,PresentedAbelianGroup target,IntegerMatrix matrix) {
        return fromMatrix(source,target,matrix,new Computation());
    }
    /** Construct an original-coordinate map within a compound calculation's budget. */
    public static AbelianGroupHomomorphism fromMatrix(PresentedAbelianGroup source,PresentedAbelianGroup target,IntegerMatrix matrix,Computation work) {
        return new AbelianGroupHomomorphism(source,target,work.multiply(work.multiply(target.smithCoordinateMap(),matrix),
                work.inverseUnimodular(source.smithCoordinateMap())),work);
    }
    public static AbelianGroupHomomorphism identity(PresentedAbelianGroup group) { return scaling(group,BigInteger.ONE); }
    public static AbelianGroupHomomorphism scaling(PresentedAbelianGroup group,BigInteger scalar) {
        return fromSmith(group,group,IntegerMatrix.identity(group.generatorCount()).scale(scalar));
    }
    public static AbelianGroupHomomorphism zero(PresentedAbelianGroup source,PresentedAbelianGroup target) {
        return fromSmith(source,target,IntegerMatrix.zero(target.generatorCount(),source.generatorCount()));
    }
    public PresentedAbelianGroup source() { return source; }
    public PresentedAbelianGroup target() { return target; }
    public IntegerMatrix smithMatrix() { return matrix; }
    public IntegerMatrix matrixLift() {
        Computation work=new Computation();
        return work.multiply(work.multiply(work.inverseUnimodular(target.smithCoordinateMap()),matrix),source.smithCoordinateMap());
    }
    public AbelianGroupElement apply(AbelianGroupElement element) {
        requireGroup(source,element); return target.fromSmith(matrix.multiply(element.smithCoordinates()));
    }
    private static void requireGroup(PresentedAbelianGroup group,AbelianGroupElement element) {
        if(!group.equals(element.group())) throw MathFailure.undefined("The element must belong to the declared presentation");
    }
    /** Apply the right operand first; presentation equality is required at the middle boundary. */
    public AbelianGroupHomomorphism compose(AbelianGroupHomomorphism other) { return compose(other,new Computation()); }
    private AbelianGroupHomomorphism compose(AbelianGroupHomomorphism other,Computation work) {
        if(!source.equals(other.target)) throw MathFailure.undefined("Homomorphism composition requires matching middle presentations");
        return new AbelianGroupHomomorphism(other.source,target,work.multiply(matrix,other.matrix),work);
    }
    public AbelianGroupHomomorphism add(AbelianGroupHomomorphism other) {
        if(!source.equals(other.source) || !target.equals(other.target)) throw MathFailure.undefined("Homomorphism addition requires matching presentations");
        return fromSmith(source,target,matrix.add(other.matrix));
    }
    public AbelianGroupHomomorphism scale(BigInteger scalar) { return fromSmith(source,target,matrix.scale(scalar)); }
    public boolean isZero() { return matrix.equals(IntegerMatrix.zero(matrix.rows(),matrix.columns())); }
    public List<AbelianGroupElement> generatorImages() {
        IntegerMatrix images=matrix.multiply(source.smithCoordinateMap()); List<AbelianGroupElement> result=new ArrayList<>();
        for(IntegerVector column : images.columnVectors()) result.add(target.fromSmith(column)); return Collections.unmodifiableList(result);
    }
    private static BigInteger[][] zeros(int rows,int columns,Computation work) {
        if(rows>256 || columns>256) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Homomorphism computations allow at most 256 rows and columns, including auxiliary matrices");
        work.use((long)rows*columns); BigInteger[][] entries=new BigInteger[rows][columns];
        for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO); return entries;
    }
    /** Independent nonzero Smith relation columns, including killed coordinates. */
    private static IntegerMatrix relations(PresentedAbelianGroup group,Computation work) {
        int columns=0; while(columns<group.generatorCount() && group.modulus(columns).signum()>0) columns++;
        BigInteger[][] entries=zeros(group.generatorCount(),columns,work);
        for(int i=0;i<columns;i++) entries[i][i]=group.modulus(i);
        return new IntegerMatrix(group.generatorCount(),columns,entries);
    }
    private static IntegerMatrix join(IntegerMatrix a,IntegerMatrix b,boolean negateB,Computation work) {
        int columns=a.columns()+b.columns(); BigInteger[][] entries=zeros(a.rows(),columns,work);
        for(int r=0;r<a.rows();r++) {
            for(int c=0;c<a.columns();c++) entries[r][c]=a.get(r,c);
            for(int c=0;c<b.columns();c++) entries[r][c+a.columns()]=negateB?b.get(r,c).negate():b.get(r,c);
        }
        return new IntegerMatrix(a.rows(),columns,entries);
    }
    private static IntegerMatrix firstRows(IntegerMatrix a,int rows,Computation work) {
        BigInteger[][] entries=zeros(rows,a.columns(),work);
        for(int r=0;r<rows;r++) for(int c=0;c<a.columns();c++) entries[r][c]=a.get(r,c);
        return new IntegerMatrix(rows,a.columns(),entries);
    }
    private IntegerMatrix liftingSystem(Computation work) { return join(matrix,relations(target,work),true,work); }
    /** Integral basis K of all x for which M*x is a target relation. */
    private IntegerMatrix kernelLattice(Computation work) {
        return firstRows(work.kernelMatrix(liftingSystem(work)),source.generatorCount(),work);
    }
    private PresentedAbelianGroup kernel(IntegerMatrix lattice,Computation work) {
        return new PresentedAbelianGroup(work.solve(lattice,relations(source,work)),work);
    }
    public PresentedAbelianGroup kernel() { Computation work=new Computation(); return kernel(kernelLattice(work),work); }
    public AbelianGroupHomomorphism kernelInclusion() {
        Computation work=new Computation(); IntegerMatrix lattice=kernelLattice(work); PresentedAbelianGroup group=kernel(lattice,work);
        return new AbelianGroupHomomorphism(group,source,work.multiply(lattice,work.inverseUnimodular(group.smithCoordinateMap())),work);
    }
    private PresentedAbelianGroup image(Computation work) { return new PresentedAbelianGroup(kernelLattice(work),work); }
    public PresentedAbelianGroup image() { return image(new Computation()); }
    public AbelianGroupHomomorphism imageInclusion() {
        Computation work=new Computation(); PresentedAbelianGroup group=image(work);
        return new AbelianGroupHomomorphism(group,target,work.multiply(matrix,work.inverseUnimodular(group.smithCoordinateMap())),work);
    }
    public AbelianGroupHomomorphism imageProjection() {
        Computation work=new Computation(); PresentedAbelianGroup group=image(work);
        return new AbelianGroupHomomorphism(source,group,group.smithCoordinateMap(),work);
    }
    private PresentedAbelianGroup cokernel(Computation work) {
        return new PresentedAbelianGroup(join(relations(target,work),matrix,false,work),work);
    }
    public PresentedAbelianGroup cokernel() { return cokernel(new Computation()); }
    public AbelianGroupHomomorphism cokernelProjection() {
        Computation work=new Computation(); PresentedAbelianGroup group=cokernel(work);
        return new AbelianGroupHomomorphism(target,group,group.smithCoordinateMap(),work);
    }
    public boolean isInjective() { return kernel().type().equals(AbelianGroupType.ZERO); }
    public boolean isSurjective() { return cokernel().type().equals(AbelianGroupType.ZERO); }
    public boolean isIsomorphism() {
        Computation work=new Computation();
        return kernel(kernelLattice(work),work).type().equals(AbelianGroupType.ZERO) && cokernel(work).type().equals(AbelianGroupType.ZERO);
    }
    public boolean hasPreimage(AbelianGroupElement element) {
        requireGroup(target,element); Computation work=new Computation(); return work.hasSolution(liftingSystem(work),element.smithCoordinates());
    }
    /** One preimage; the complete fiber is its coset by the kernel. */
    public AbelianGroupElement preimage(AbelianGroupElement element) {
        requireGroup(target,element); Computation work=new Computation();
        BigInteger[][] column=zeros(target.generatorCount(),1,work);
        for(int r=0;r<column.length;r++) column[r][0]=element.smithCoordinates().get(r);
        IntegerMatrix solved=work.solve(liftingSystem(work),new IntegerMatrix(target.generatorCount(),1,column));
        return source.fromSmith(firstRows(solved,source.generatorCount(),work).column(0));
    }
    public AbelianGroupHomomorphism inverse() {
        Computation work=new Computation();
        IntegerMatrix solved=work.solve(liftingSystem(work),IntegerMatrix.identity(target.generatorCount()));
        AbelianGroupHomomorphism candidate=new AbelianGroupHomomorphism(target,source,firstRows(solved,source.generatorCount(),work),work);
        AbelianGroupHomomorphism identity=new AbelianGroupHomomorphism(source,source,IntegerMatrix.identity(source.generatorCount()),work);
        if(!candidate.compose(this,work).equals(identity)) throw MathFailure.undefined("The homomorphism is not injective");
        return candidate;
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof AbelianGroupHomomorphism)) return false; AbelianGroupHomomorphism map=(AbelianGroupHomomorphism)other;
        return source.equals(map.source) && target.equals(map.target) && matrix.equals(map.matrix);
    }
    @Override public int hashCode() { return Objects.hash(source,target,matrix); }
    @Override public String toString() { return "AbelianHom(source="+source+", target="+target+", smith="+matrix+")"; }
}
