package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Cone(F)_n = target_n + source_(n-1), retaining the defining integral chain map. */
public final class IntegralChainMappingCone implements Serializable {
    private static final long serialVersionUID=1L;
    private final SimplicialChainMap map;
    public IntegralChainMappingCone(SimplicialChainMap map) { this.map=Objects.requireNonNull(map); }
    public SimplicialChainMap map() { return map; }
    public RelativeSimplicialComplex source() { return map.source(); }
    public RelativeSimplicialComplex target() { return map.target(); }
    /** Formal ambient degree range; diagonal relative pairs keep their zero degree slots. */
    public int dimension() {
        int sourceTop=source().ambient().dimension(); return Math.max(target().ambient().dimension(),sourceTop<0?-1:sourceTop+1);
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Mapping-cone degrees must be nonnegative"); }
    int rank(BigInteger degree) {
        int size=target().basis(degree).size()+source().basis(degree.subtract(BigInteger.ONE)).size();
        if(size>256) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Mapping-cone groups allow at most 256 total target and shifted source coordinates per degree");
        return size;
    }
    public BigInteger chainRank(BigInteger degree) { requireDegree(degree); return BigInteger.valueOf(rank(degree)); }
    private static BigInteger[][] zeros(int rows,int columns,Computation work) {
        work.use((long)rows*columns); BigInteger[][] entries=new BigInteger[rows][columns]; for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO); return entries;
    }
    private static void block(BigInteger[][] into,IntegerMatrix matrix,int row,int column,boolean negate,Computation work) {
        work.use((long)matrix.rows()*matrix.columns());
        for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.columns();c++) into[row+r][column+c]=negate?matrix.get(r,c).negate():matrix.get(r,c);
    }
    IntegerMatrix boundary(BigInteger degree,Computation work) {
        BigInteger previous=degree.subtract(BigInteger.ONE); int rows=rank(previous),columns=rank(degree),t=target().basis(degree).size(),previousT=target().basis(previous).size();
        BigInteger[][] entries=zeros(rows,columns,work);
        block(entries,target().boundaryMatrix(degree,work),0,0,false,work);
        if(degree.signum()>0) block(entries,map.chainMatrix(previous),0,t,false,work);
        block(entries,source().boundaryMatrix(previous,work),previousT,t,true,work);
        return new IntegerMatrix(rows,columns,entries);
    }
    public IntegerMatrix boundaryMatrix(BigInteger degree) { requireDegree(degree); return boundary(degree,new Computation()); }
    public List<IntegerMatrix> boundaryMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=dimension();k++) result.add(boundary(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    private IntegerMatrix inclusion(BigInteger degree,Computation work) {
        int rows=rank(degree),columns=target().basis(degree).size(); BigInteger[][] entries=zeros(rows,columns,work); work.use(columns);
        for(int i=0;i<columns;i++) entries[i][i]=BigInteger.ONE; return new IntegerMatrix(rows,columns,entries);
    }
    private IntegerMatrix projection(BigInteger degree,Computation work) {
        int rows=source().basis(degree.subtract(BigInteger.ONE)).size(),columns=rank(degree),offset=target().basis(degree).size(); BigInteger[][] entries=zeros(rows,columns,work); work.use(rows);
        for(int i=0;i<rows;i++) entries[i][offset+i]=BigInteger.ONE; return new IntegerMatrix(rows,columns,entries);
    }
    public IntegerMatrix inclusionMatrix(BigInteger degree) { requireDegree(degree); return inclusion(degree,new Computation()); }
    public IntegerMatrix projectionMatrix(BigInteger degree) { requireDegree(degree); return projection(degree,new Computation()); }
    private static IntegerMatrix transpose(IntegerMatrix matrix,Computation work) { work.use((long)matrix.rows()*matrix.columns()); return matrix.transpose(); }
    IntegralHomology homology(BigInteger degree,boolean dual,Computation work) {
        IntegerMatrix outgoing=boundary(degree,work),incoming=boundary(degree.add(BigInteger.ONE),work);
        return dual?new IntegralHomology(transpose(incoming,work),transpose(outgoing,work),work):new IntegralHomology(outgoing,incoming,work);
    }
    static IntegralHomology endpoint(RelativeSimplicialComplex pair,BigInteger degree,boolean dual,Computation work) {
        // Retain adjacent zero shapes even at degree -1, as in the existing relative-triple algebra.
        IntegerMatrix outgoing=pair.boundaryMatrix(degree,work),incoming=pair.boundaryMatrix(degree.add(BigInteger.ONE),work);
        return dual?new IntegralHomology(transpose(incoming,work),transpose(outgoing,work),work):new IntegralHomology(outgoing,incoming,work);
    }
    public IntegralHomology homology(BigInteger degree) { requireDegree(degree); return homology(degree,false,new Computation()); }
    public IntegralHomology cohomology(BigInteger degree) { requireDegree(degree); return homology(degree,true,new Computation()); }
    public AbelianGroupType homologyType(BigInteger degree) { return homology(degree).type(); }
    public AbelianGroupType cohomologyType(BigInteger degree) { return cohomology(degree).type(); }
    private List<AbelianGroupType> types(boolean dual) {
        Computation work=new Computation(); List<AbelianGroupType> result=new ArrayList<>();
        for(int k=0;k<=dimension();k++) result.add(homology(BigInteger.valueOf(k),dual,work).type()); return Collections.unmodifiableList(result);
    }
    public List<AbelianGroupType> homologyTypes() { return types(false); }
    public List<AbelianGroupType> cohomologyTypes() { return types(true); }
    public boolean isAcyclic() {
        Computation work=new Computation(); for(int k=0;k<=dimension();k++) rank(BigInteger.valueOf(k));
        for(int k=0;k<=dimension();k++) if(!homology(BigInteger.valueOf(k),false,work).isAcyclic()) return false; return true;
    }
    private AbelianGroupHomomorphism structuralMap(BigInteger degree,boolean dual,boolean project) {
        requireDegree(degree); Computation work=new Computation(); IntegralHomology cone=homology(degree,dual,work),other=endpoint(project?source():target(),project?degree.subtract(BigInteger.ONE):degree,dual,work);
        IntegerMatrix matrix=project?projection(degree,work):inclusion(degree,work); if(dual) matrix=transpose(matrix,work);
        return project==dual?other.inducedMap(cone,matrix,work):cone.inducedMap(other,matrix,work);
    }
    public AbelianGroupHomomorphism inclusionHomologyMap(BigInteger degree) { return structuralMap(degree,false,false); }
    public AbelianGroupHomomorphism projectionHomologyMap(BigInteger degree) { return structuralMap(degree,false,true); }
    public AbelianGroupHomomorphism inclusionCohomologyMap(BigInteger degree) { return structuralMap(degree,true,false); }
    public AbelianGroupHomomorphism projectionCohomologyMap(BigInteger degree) { return structuralMap(degree,true,true); }
    /** H_n(source) -> H_n(target) -> H_n(cone) -> H_(n-1)(source). */
    public List<AbelianGroupHomomorphism> longExactSegment(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        IntegralHomology c=homology(degree,false,work),a=endpoint(source(),degree,false,work),b=endpoint(target(),degree,false,work),previous=endpoint(source(),degree.subtract(BigInteger.ONE),false,work);
        return Collections.unmodifiableList(Arrays.asList(a.inducedMap(b,map.chainMatrix(degree),work),b.inducedMap(c,inclusion(degree,work),work),c.inducedMap(previous,projection(degree,work),work)));
    }
    /** H^n(cone) -> H^n(target) -> H^n(source) -> H^(n+1)(cone). */
    public List<AbelianGroupHomomorphism> longExactCohomologySegment(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); BigInteger next=degree.add(BigInteger.ONE);
        IntegralHomology c=homology(degree,true,work),following=homology(next,true,work),a=endpoint(source(),degree,true,work),b=endpoint(target(),degree,true,work);
        return Collections.unmodifiableList(Arrays.asList(c.inducedMap(b,transpose(inclusion(degree,work),work),work),b.inducedMap(a,transpose(map.chainMatrix(degree),work),work),a.inducedMap(following,transpose(projection(next,work),work),work)));
    }
    @Override public boolean equals(Object other) { return other instanceof IntegralChainMappingCone && map.equals(((IntegralChainMappingCone)other).map); }
    @Override public int hashCode() { return map.hashCode(); }
    @Override public String toString() { return "ChainCone(map="+map+")"; }
}
