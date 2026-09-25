package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** An ordered two-subcomplex cover of its union, with integral Mayer-Vietoris maps. */
public final class SimplicialCover implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_SIMPLICES=4096;
    private final FiniteSimplicialComplex left,right,union,intersection;
    public SimplicialCover(FiniteSimplicialComplex left,FiniteSimplicialComplex right) {
        this.left=Objects.requireNonNull(left); this.right=Objects.requireNonNull(right);
        if(left.faces().size()>MAX_SIMPLICES || right.faces().size()>MAX_SIMPLICES) throw sizeLimit();
        union=left.union(right);
        if(union.faces().size()>MAX_SIMPLICES) throw sizeLimit();
        intersection=left.intersection(right);
    }
    private static MathFailure sizeLimit() { return new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"A simplicial cover allows at most 4096 nonempty simplices in its union"); }
    public FiniteSimplicialComplex left() { return left; }
    public FiniteSimplicialComplex right() { return right; }
    public FiniteSimplicialComplex union() { return union; }
    public FiniteSimplicialComplex intersection() { return intersection; }
    public SimplicialCover swap() { return new SimplicialCover(right,left); }
    public BigInteger eulerCharacteristic() { return left.eulerCharacteristic().add(right.eulerCharacteristic()).subtract(intersection.eulerCharacteristic()); }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Mayer-Vietoris degree must be nonnegative"); }
    private static List<FiniteSet<Integer>> basis(FiniteSimplicialComplex complex,BigInteger degree) {
        if(degree.signum()<0 || degree.compareTo(BigInteger.valueOf(complex.dimension()))>0) return Collections.emptyList();
        return IntegralSimplicialHomology.orderedBasis(complex,null,degree.intValueExact());
    }
    private static BigInteger[][] zeros(int rows,int columns,Computation work) {
        if(rows>IntegerSmithNormalForm.MAX_DIMENSION || columns>IntegerSmithNormalForm.MAX_DIMENSION)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Every Mayer-Vietoris matrix dimension, including a sum of chain ranks, must be at most 256");
        work.use((long)rows*columns); BigInteger[][] entries=new BigInteger[rows][columns]; for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO); return entries;
    }
    private static Map<FiniteSet<Integer>,Integer> positions(List<FiniteSet<Integer>> basis) {
        Map<FiniteSet<Integer>,Integer> result=new HashMap<>(); for(int i=0;i<basis.size();i++) result.put(basis.get(i),i); return result;
    }
    private static IntegerMatrix boundary(FiniteSimplicialComplex complex,BigInteger degree,Computation work) {
        return RelativeSimplicialComplex.boundary(basis(complex,degree.subtract(BigInteger.ONE)),basis(complex,degree),work);
    }
    private static IntegralHomology homology(FiniteSimplicialComplex complex,BigInteger degree,Computation work) {
        requireDegree(degree); return new IntegralHomology(boundary(complex,degree,work),boundary(complex,degree.add(BigInteger.ONE),work),work);
    }
    public IntegerMatrix sumBoundaryMatrix(BigInteger degree) { requireDegree(degree); return sumBoundaryMatrix(degree,new Computation()); }
    private IntegerMatrix sumBoundaryMatrix(BigInteger degree,Computation work) {
        IntegerMatrix a=boundary(left,degree,work),b=boundary(right,degree,work);
        int rows=a.rows()+b.rows(),columns=a.columns()+b.columns(); BigInteger[][] entries=zeros(rows,columns,work);
        for(int r=0;r<a.rows();r++) for(int c=0;c<a.columns();c++) entries[r][c]=a.get(r,c);
        for(int r=0;r<b.rows();r++) for(int c=0;c<b.columns();c++) entries[a.rows()+r][a.columns()+c]=b.get(r,c);
        return new IntegerMatrix(rows,columns,entries);
    }
    public List<IntegerMatrix> sumBoundaryMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=union.dimension();k++) result.add(sumBoundaryMatrix(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public IntegralHomology sumHomology(BigInteger degree) { requireDegree(degree); return sumHomology(degree,new Computation()); }
    private IntegralHomology sumHomology(BigInteger degree,Computation work) {
        return new IntegralHomology(sumBoundaryMatrix(degree,work),sumBoundaryMatrix(degree.add(BigInteger.ONE),work),work);
    }
    public List<IntegralHomology> sumHomologyDegrees() {
        Computation work=new Computation(); List<IntegralHomology> result=new ArrayList<>();
        for(int k=0;k<=union.dimension();k++) result.add(sumHomology(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public IntegralHomology leftHomology(BigInteger degree) { return homology(left,degree,new Computation()); }
    public IntegralHomology rightHomology(BigInteger degree) { return homology(right,degree,new Computation()); }
    public IntegralHomology intersectionHomology(BigInteger degree) { return homology(intersection,degree,new Computation()); }
    public IntegralHomology unionHomology(BigInteger degree) { return homology(union,degree,new Computation()); }
    /** c maps to (i(c),-j(c)), in left-then-right chain coordinates. */
    public IntegerMatrix intersectionMatrix(BigInteger degree) { requireDegree(degree); return intersectionMatrix(degree,new Computation()); }
    private IntegerMatrix intersectionMatrix(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> a=basis(left,degree),b=basis(right,degree),i=basis(intersection,degree);
        BigInteger[][] entries=zeros(a.size()+b.size(),i.size(),work); Map<FiniteSet<Integer>,Integer> pa=positions(a),pb=positions(b);
        for(int c=0;c<i.size();c++) { entries[pa.get(i.get(c))][c]=BigInteger.ONE; entries[a.size()+pb.get(i.get(c))][c]=BigInteger.ONE.negate(); }
        return new IntegerMatrix(a.size()+b.size(),i.size(),entries);
    }
    /** (a,b) maps to a+b in the union. */
    public IntegerMatrix unionMatrix(BigInteger degree) { requireDegree(degree); return unionMatrix(degree,new Computation()); }
    private IntegerMatrix unionMatrix(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> a=basis(left,degree),b=basis(right,degree),u=basis(union,degree);
        BigInteger[][] entries=zeros(u.size(),a.size()+b.size(),work); Map<FiniteSet<Integer>,Integer> positions=positions(u);
        for(int c=0;c<a.size();c++) entries[positions.get(a.get(c))][c]=BigInteger.ONE;
        for(int c=0;c<b.size();c++) entries[positions.get(b.get(c))][a.size()+c]=BigInteger.ONE;
        return new IntegerMatrix(u.size(),a.size()+b.size(),entries);
    }
    /** Chain-group section assigning every shared simplex to the left; generally not a chain map. */
    public IntegerMatrix splitMatrix(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        List<FiniteSet<Integer>> a=basis(left,degree),b=basis(right,degree),u=basis(union,degree);
        BigInteger[][] entries=zeros(a.size()+b.size(),u.size(),work); Map<FiniteSet<Integer>,Integer> pa=positions(a),pb=positions(b);
        for(int c=0;c<u.size();c++) { Integer row=pa.get(u.get(c)); entries[row==null?a.size()+pb.get(u.get(c)):row][c]=BigInteger.ONE; }
        return new IntegerMatrix(a.size()+b.size(),u.size(),entries);
    }
    /** I-component of the boundary of the left part. On union cycles the whole boundary lies in I. */
    public IntegerMatrix connectingChainMatrix(BigInteger degree) { requireDegree(degree); return connectingChainMatrix(degree,new Computation()); }
    private IntegerMatrix connectingChainMatrix(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> rows=basis(intersection,degree.subtract(BigInteger.ONE)),columns=basis(union,degree);
        IntegerMatrix faces=RelativeSimplicialComplex.boundary(rows,columns,work); BigInteger[][] entries=zeros(rows.size(),columns.size(),work);
        for(int c=0;c<columns.size();c++) if(left.faces().contains(columns.get(c))) for(int r=0;r<rows.size();r++) entries[r][c]=faces.get(r,c);
        return new IntegerMatrix(rows.size(),columns.size(),entries);
    }
    private IntegralHomology connectingTarget(BigInteger degree,Computation work) {
        return degree.signum()==0?new IntegralHomology(IntegerMatrix.zero(0,0),IntegerMatrix.zero(0,0),work):homology(intersection,degree.subtract(BigInteger.ONE),work);
    }
    public AbelianGroupHomomorphism intersectionHomologyMap(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return homology(intersection,degree,work).inducedMap(sumHomology(degree,work),intersectionMatrix(degree,work),work);
    }
    public AbelianGroupHomomorphism unionHomologyMap(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return sumHomology(degree,work).inducedMap(homology(union,degree,work),unionMatrix(degree,work),work);
    }
    public AbelianGroupHomomorphism connectingHomologyMap(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return homology(union,degree,work).inducedMap(connectingTarget(degree,work),connectingChainMatrix(degree,work),work);
    }
    /** H_k(I)->H_k(A) direct-sum H_k(B)->H_k(U)->H_(k-1)(I), three maps in order. */
    public List<AbelianGroupHomomorphism> longExactSegment(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        IntegralHomology i=homology(intersection,degree,work),sum=sumHomology(degree,work),u=homology(union,degree,work);
        return Collections.unmodifiableList(Arrays.asList(i.inducedMap(sum,intersectionMatrix(degree,work),work),
                sum.inducedMap(u,unionMatrix(degree,work),work),u.inducedMap(connectingTarget(degree,work),connectingChainMatrix(degree,work),work)));
    }
    private AbelianGroupHomomorphism componentMap(BigInteger degree,boolean second,boolean projection) {
        requireDegree(degree); Computation work=new Computation(); IntegralHomology component=homology(second?right:left,degree,work),sum=sumHomology(degree,work);
        int n=component.chainRank(),total=sum.chainRank(),offset=second?basis(left,degree).size():0;
        int rows=projection?n:total,columns=projection?total:n; BigInteger[][] entries=zeros(rows,columns,work);
        for(int i=0;i<n;i++) entries[projection?i:offset+i][projection?offset+i:i]=BigInteger.ONE;
        return projection?sum.inducedMap(component,new IntegerMatrix(rows,columns,entries),work):component.inducedMap(sum,new IntegerMatrix(rows,columns,entries),work);
    }
    public AbelianGroupHomomorphism leftInclusionMap(BigInteger degree) { return componentMap(degree,false,false); }
    public AbelianGroupHomomorphism rightInclusionMap(BigInteger degree) { return componentMap(degree,true,false); }
    public AbelianGroupHomomorphism leftProjectionMap(BigInteger degree) { return componentMap(degree,false,true); }
    public AbelianGroupHomomorphism rightProjectionMap(BigInteger degree) { return componentMap(degree,true,true); }
    /** Simplicial excision: (A,A intersection B)->(A union B,B) induces an integral chain isomorphism. */
    public RelativeSimplicialMap excisionMap() {
        return RelativeSimplicialMap.inclusion(new RelativeSimplicialComplex(left,intersection),new RelativeSimplicialComplex(union,right));
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof SimplicialCover)) return false; SimplicialCover cover=(SimplicialCover)other; return left.equals(cover.left) && right.equals(cover.right);
    }
    @Override public int hashCode() { return Objects.hash(left,right); }
    @Override public String toString() { return "SimplicialCover(left="+left+", right="+right+")"; }
}
