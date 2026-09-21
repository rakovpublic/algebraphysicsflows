package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import java.util.*;

/** Finite abstract simplicial complexes; homology dimensions over F_2 only, unreduced. */
public final class FiniteSimplicialComplex {
    private final Set<FiniteSet<Integer>> simplices;
    public FiniteSimplicialComplex(Collection<FiniteSet<Integer>> facets) {
        Set<FiniteSet<Integer>> result=new LinkedHashSet<>();
        for(FiniteSet<Integer> facet : facets) {
            for(FiniteSet<Integer> face : facet.powerSet().members()) if(face.size()>0) result.add(face);
        }
        simplices=Collections.unmodifiableSet(result);
    }
    public List<FiniteSet<Integer>> simplices(int dimension) {
        if(dimension<0) throw MathFailure.invalid("Negative simplex dimension");
        List<FiniteSet<Integer>> result=new ArrayList<>();
        for(FiniteSet<Integer> simplex : simplices) if(simplex.size()==dimension+1) result.add(simplex);
        return Collections.unmodifiableList(result);
    }
    public int dimension() { int result=-1; for(FiniteSet<Integer> simplex : simplices) result=Math.max(result,simplex.size()-1); return result; }
    public int bettiNumber(int dimension) {
        if(dimension<0) throw MathFailure.invalid("Negative homology degree");
        return simplices(dimension).size()-boundaryRank(dimension)-boundaryRank(dimension+1);
    }
    private int boundaryRank(int degree) {
        if(degree==0) return 0;
        List<FiniteSet<Integer>> rows=simplices(degree-1), columns=simplices(degree);
        boolean[][] matrix=new boolean[rows.size()][columns.size()];
        for(int r=0;r<rows.size();r++) for(int c=0;c<columns.size();c++) matrix[r][c]=rows.get(r).subsetOf(columns.get(c));
        int rank=0;
        for(int c=0;c<columns.size() && rank<rows.size();c++) {
            int pivot=rank; while(pivot<rows.size() && !matrix[pivot][c]) pivot++;
            if(pivot==rows.size()) continue;
            boolean[] row=matrix[rank]; matrix[rank]=matrix[pivot]; matrix[pivot]=row;
            for(int r=rank+1;r<rows.size();r++) if(matrix[r][c]) for(int j=c;j<columns.size();j++) matrix[r][j]^=matrix[rank][j];
            rank++;
        }
        return rank;
    }
}
