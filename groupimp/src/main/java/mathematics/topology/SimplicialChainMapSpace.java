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

/** Full labelled endpoints for Z-linear chain maps and their additive homotopy classes. */
public final class SimplicialChainMapSpace implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_TOTAL_RANK=IntegerSmithNormalForm.MAX_DIMENSION;
    private final RelativeSimplicialComplex source,target;
    public SimplicialChainMapSpace(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
    }
    public RelativeSimplicialComplex source() { return source; }
    public RelativeSimplicialComplex target() { return target; }
    public SimplicialChainMap zero() { return SimplicialChainMap.zero(source,target); }

    private final class Calculation {
        final Computation work=new Computation();
        final int degrees=Math.max(source.ambient().dimension(),target.ambient().dimension())+1;
        final int[] s=new int[degrees],t=new int[degrees];
        // Index shift+1: degree -1 equations, degree 0 maps, degree 1 homotopies.
        final int[][] offsets=new int[3][degrees+1];
        final IntegralHomology homology;
        Calculation() {
            for(int k=0;k<degrees;k++) { s[k]=source.basis(BigInteger.valueOf(k)).size(); t[k]=target.basis(BigInteger.valueOf(k)).size(); }
            for(int shift=-1;shift<=1;shift++) for(int k=0;k<degrees;k++) {
                int rank=offsets[shift+1][k]+targetRank(k+shift)*s[k];
                if(rank>MAX_TOTAL_RANK) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Chain-map spaces allow at most 256 total Hom coefficients in each degree -1, 0 and 1");
                offsets[shift+1][k+1]=rank;
            }
            homology=new IntegralHomology(differential(0),differential(1),work);
        }
        int targetRank(int k) { return k<0 || k>=degrees?0:t[k]; }
        int rank(int shift) { return offsets[shift+1][degrees]; }
        /** delta(T)_k = d_target T_k - (-1)^shift T_(k-1) d_source. */
        IntegerMatrix differential(int shift) {
            int rows=rank(shift-1),columns=rank(shift); work.use((long)rows*columns);
            BigInteger[][] result=new BigInteger[rows][columns]; for(BigInteger[] row : result) Arrays.fill(row,BigInteger.ZERO);
            for(int k=0;k<degrees;k++) {
                IntegerMatrix left=target.boundaryMatrix(BigInteger.valueOf(k+shift),work),right=source.boundaryMatrix(BigInteger.valueOf(k),work);
                for(int r=0;r<targetRank(k+shift-1);r++) for(int c=0;c<s[k];c++) {
                    int row=offsets[shift][k]+r*s[k]+c; work.use(targetRank(k+shift)+(k==0?0:s[k-1]));
                    for(int a=0;a<targetRank(k+shift);a++) result[row][offsets[shift+1][k]+a*s[k]+c]=left.get(r,a);
                    if(k>0) for(int b=0;b<s[k-1];b++) result[row][offsets[shift+1][k-1]+r*s[k-1]+b]=shift==0?right.get(b,c).negate():right.get(b,c);
                }
            }
            return new IntegerMatrix(rows,columns,result);
        }
        IntegerVector flatten(SimplicialChainMap map) {
            work.use(rank(0)); BigInteger[] entries=new BigInteger[rank(0)];
            for(int k=0;k<degrees;k++) for(int r=0;r<t[k];r++) for(int c=0;c<s[k];c++) entries[offsets[1][k]+r*s[k]+c]=map.chainMatrices().get(k).get(r,c);
            return new IntegerVector(entries);
        }
        SimplicialChainMap decode(IntegerVector vector) {
            work.use(rank(0)); List<IntegerMatrix> matrices=new ArrayList<>();
            for(int k=0;k<degrees;k++) {
                BigInteger[][] entries=new BigInteger[t[k]][s[k]];
                for(int r=0;r<t[k];r++) for(int c=0;c<s[k];c++) entries[r][c]=vector.get(offsets[1][k]+r*s[k]+c);
                matrices.add(new IntegerMatrix(t[k],s[k],entries));
            }
            return new SimplicialChainMap(new SimplicialChainMap.Data(source,target,matrices),work);
        }
    }
    /** H_0 of Hom, with flattened degree/row/column coordinates retained. */
    public IntegralHomology homology() { return new Calculation().homology; }
    public PresentedAbelianGroup homotopyGroup() { return homology().group(); }
    public AbelianGroupType homotopyType() { return homology().type(); }
    /** A full integral basis for all chain maps, not just their homotopy classes. */
    public List<SimplicialChainMap> mapGenerators() {
        Calculation calculation=new Calculation(); List<SimplicialChainMap> result=new ArrayList<>();
        for(IntegerVector vector : calculation.homology.cycleBasis()) result.add(calculation.decode(vector));
        return Collections.unmodifiableList(result);
    }
    public AbelianGroupElement classOf(SimplicialChainMap map) {
        if(!source.equals(map.source()) || !target.equals(map.target())) throw MathFailure.undefined("The chain map must retain this space's full labelled source and target pairs");
        Calculation calculation=new Calculation(); return calculation.homology.classOf(calculation.flatten(map),calculation.work);
    }
    /** A set-theoretic section of classOf, not an additive choice of representatives. */
    public SimplicialChainMap representative(AbelianGroupElement element) {
        Calculation calculation=new Calculation(); return calculation.decode(calculation.homology.representative(element,calculation.work));
    }
    /** Representatives of the nonzero Smith generators: torsion first, then free generators. */
    public List<SimplicialChainMap> representatives() {
        Calculation calculation=new Calculation(); List<SimplicialChainMap> result=new ArrayList<>();
        for(IntegerVector vector : calculation.homology.generators(calculation.work)) result.add(calculation.decode(vector));
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) { return other instanceof SimplicialChainMapSpace && source.equals(((SimplicialChainMapSpace)other).source) && target.equals(((SimplicialChainMapSpace)other).target); }
    @Override public int hashCode() { return Objects.hash(source,target); }
    @Override public String toString() { return "ChainMapSpace(source="+source+", target="+target+")"; }
}
