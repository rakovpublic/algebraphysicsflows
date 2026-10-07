package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Retained cone maps modulo unrestricted integral cone homotopies with retained-map boundaries. */
public final class IntegralConeMapSpace implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_TOTAL_RANK=IntegerSmithNormalForm.MAX_DIMENSION;
    private final IntegralChainMappingCone source,target;
    public IntegralConeMapSpace(IntegralChainMappingCone source,IntegralChainMappingCone target) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
    }
    public IntegralChainMappingCone source() { return source; }
    public IntegralChainMappingCone target() { return target; }
    public IntegralChainConeMap zero() { return IntegralChainConeMap.zero(source,target); }

    private final class Layout {
        final Computation work;
        final int degrees=Math.max(source.dimension(),target.dimension())+1;
        final int[] s=new int[degrees],t=new int[degrees];
        // Index shift+1: degree -1 equations, degree 0 maps, degree 1 homotopies.
        final int[][] offsets=new int[3][degrees+1];
        final int[] allowed,forbidden,restricted;
        Layout(Computation work) {
            this.work=work;
            for(int k=0;k<degrees;k++) { s[k]=source.rank(BigInteger.valueOf(k)); t[k]=target.rank(BigInteger.valueOf(k)); }
            for(int shift=-1;shift<=1;shift++) for(int k=0;k<degrees;k++) {
                int rank=offsets[shift+1][k]+targetRank(k+shift)*s[k];
                if(rank>MAX_TOTAL_RANK) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Cone-map spaces allow at most 256 total full Hom coefficients in each degree -1, 0 and 1");
                offsets[shift+1][k+1]=rank;
            }
            List<Integer> yes=new ArrayList<>(),no=new ArrayList<>(); restricted=new int[rank(0)]; Arrays.fill(restricted,-1); work.use(rank(0));
            for(int d=0;d<degrees;d++) {
                int sourceTarget=source.target().basis(BigInteger.valueOf(d)).size(),targetTarget=target.target().basis(BigInteger.valueOf(d)).size();
                for(int r=0;r<t[d];r++) for(int c=0;c<s[d];c++) {
                    int full=offsets[1][d]+r*s[d]+c;
                    if(r<targetTarget || c>=sourceTarget) { restricted[full]=yes.size(); yes.add(full); } else no.add(full);
                }
            }
            allowed=indices(yes); forbidden=indices(no);
        }
        int targetRank(int k) { return k<0 || k>=degrees?0:t[k]; }
        int rank(int shift) { return offsets[shift+1][degrees]; }
        /** delta(T)_k = d_target T_k - (-1)^shift T_(k-1) d_source. */
        IntegerMatrix differential(int shift) {
            int rows=rank(shift-1),columns=rank(shift); work.use((long)rows*columns);
            BigInteger[][] result=new BigInteger[rows][columns]; for(BigInteger[] row : result) Arrays.fill(row,BigInteger.ZERO);
            for(int k=0;k<degrees;k++) {
                IntegerMatrix left=target.boundary(BigInteger.valueOf(k+shift),work),right=source.boundary(BigInteger.valueOf(k),work);
                for(int r=0;r<targetRank(k+shift-1);r++) for(int c=0;c<s[k];c++) {
                    int row=offsets[shift][k]+r*s[k]+c; work.use(targetRank(k+shift)+(k==0?0:s[k-1]));
                    for(int a=0;a<targetRank(k+shift);a++) result[row][offsets[shift+1][k]+a*s[k]+c]=left.get(r,a);
                    if(k>0) for(int b=0;b<s[k-1];b++) result[row][offsets[shift+1][k-1]+r*s[k-1]+b]=shift==0?right.get(b,c).negate():right.get(b,c);
                }
            }
            return new IntegerMatrix(rows,columns,result);
        }
        IntegerVector flatten(IntegralChainConeMap map) {
            work.use(allowed.length); BigInteger[] entries=new BigInteger[allowed.length];
            for(int d=0;d<degrees;d++) {
                IntegerMatrix matrix=map.matrix(BigInteger.valueOf(d),work);
                for(int r=0;r<t[d];r++) for(int c=0;c<s[d];c++) {
                    int coordinate=restricted[offsets[1][d]+r*s[d]+c]; if(coordinate>=0) entries[coordinate]=matrix.get(r,c);
                }
            }
            return new IntegerVector(entries);
        }
        IntegralChainConeMap decode(IntegerVector vector) {
            return IntegralChainConeMap.fromBlocks(source,target,(degree,row,column) -> {
                int coordinate=restricted[offsets[1][degree]+row*s[degree]+column];
                return coordinate<0?BigInteger.ZERO:vector.get(coordinate);
            },work);
        }
    }
    private static int[] indices(List<Integer> list) { int[] result=new int[list.size()]; for(int i=0;i<result.length;i++) result[i]=list.get(i); return result; }
    private static IntegerMatrix select(IntegerMatrix matrix,int[] rows,int[] columns,Computation work) {
        int nr=rows==null?matrix.rows():rows.length,nc=columns==null?matrix.columns():columns.length;
        work.use((long)nr*nc); BigInteger[][] result=new BigInteger[nr][nc];
        for(int r=0;r<nr;r++) for(int c=0;c<nc;c++) result[r][c]=matrix.get(rows==null?r:rows[r],columns==null?c:columns[c]);
        return new IntegerMatrix(nr,nc,result);
    }
    private final class Calculation {
        final Computation work;
        final Layout layout;
        final IntegralHomology homology;
        Calculation() { this(new Computation()); }
        Calculation(Computation work) {
            this.work=work; layout=new Layout(work);
            IntegerMatrix d0=layout.differential(0),d1=layout.differential(1);
            // Only homotopies whose boundaries have zero forbidden blocks may generate relations.
            // Projecting arbitrary boundaries would identify maps which are not homotopic.
            IntegerMatrix admissible=work.kernelMatrix(select(d1,layout.forbidden,null,work));
            homology=new IntegralHomology(select(d0,null,layout.allowed,work),work.multiply(select(d1,layout.allowed,null,work),admissible),work);
        }
    }
    /** Restricted map cycles modulo all homotopy boundaries which lie in the retained-square carrier. */
    public IntegralHomology homology() { return new Calculation().homology; }
    public PresentedAbelianGroup homotopyGroup() { return homology().group(); }
    public AbelianGroupType homotopyType() { return homology().type(); }
    /** A full integral basis for all retained cone maps, not just their homotopy classes. */
    public List<IntegralChainConeMap> mapGenerators() {
        Calculation calculation=new Calculation(); List<IntegralChainConeMap> result=new ArrayList<>();
        for(IntegerVector vector : calculation.homology.cycleBasis()) result.add(calculation.layout.decode(vector));
        return Collections.unmodifiableList(result);
    }
    public AbelianGroupElement classOf(IntegralChainConeMap map) {
        if(!source.equals(map.source()) || !target.equals(map.target())) throw MathFailure.undefined("The cone map must retain this space's full source and target defining cones");
        Calculation calculation=new Calculation(); return calculation.homology.classOf(calculation.layout.flatten(map),calculation.work);
    }
    /** A set-theoretic section of classOf, not an additive choice of representatives. */
    public IntegralChainConeMap representative(AbelianGroupElement element) {
        Calculation calculation=new Calculation(); return calculation.layout.decode(calculation.homology.representative(element,calculation.work));
    }
    /** Representatives of the nonzero Smith generators: torsion first, then free generators. */
    public List<IntegralChainConeMap> representatives() {
        Calculation calculation=new Calculation(); List<IntegralChainConeMap> result=new ArrayList<>();
        for(IntegerVector vector : calculation.homology.generators(calculation.work)) result.add(calculation.layout.decode(vector));
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) { return other instanceof IntegralConeMapSpace && source.equals(((IntegralConeMapSpace)other).source) && target.equals(((IntegralConeMapSpace)other).target); }
    @Override public int hashCode() { return Objects.hash(source,target); }
    @Override public String toString() { return "ConeMapSpace(source="+source+", target="+target+")"; }
}
