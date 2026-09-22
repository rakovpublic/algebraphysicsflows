package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Finite abstract simplicial complexes; homology dimensions over F_2 only, unreduced. */
public final class FiniteSimplicialComplex implements Serializable {
    private static final long serialVersionUID=1L;
    private final Set<FiniteSet<Integer>> simplices;
    public FiniteSimplicialComplex(Collection<FiniteSet<Integer>> facets) {
        Set<FiniteSet<Integer>> result=new LinkedHashSet<>();
        for(FiniteSet<Integer> facet : facets) {
            for(FiniteSet<Integer> face : facet.powerSet().members()) if(face.size()>0) result.add(face);
        }
        simplices=Collections.unmodifiableSet(result);
    }
    private FiniteSimplicialComplex(Set<FiniteSet<Integer>> closedFaces,boolean alreadyClosed) {
        simplices=Collections.unmodifiableSet(new LinkedHashSet<>(closedFaces));
    }
    public FiniteSimplicialComplex union(FiniteSimplicialComplex other) {
        Set<FiniteSet<Integer>> result=new LinkedHashSet<>(simplices); result.addAll(other.simplices);
        return new FiniteSimplicialComplex(result,true);
    }
    public FiniteSimplicialComplex intersection(FiniteSimplicialComplex other) {
        Set<FiniteSet<Integer>> result=new LinkedHashSet<>(simplices); result.retainAll(other.simplices);
        return new FiniteSimplicialComplex(result,true);
    }
    public boolean subcomplexOf(FiniteSimplicialComplex other) { return other.simplices.containsAll(simplices); }
    public FiniteSimplicialComplex skeleton(int degree) {
        if(degree<0) throw MathFailure.invalid("Negative skeleton degree");
        Set<FiniteSet<Integer>> result=new LinkedHashSet<>();
        for(FiniteSet<Integer> simplex : simplices) if(simplex.size()-1<=degree) result.add(simplex);
        return new FiniteSimplicialComplex(result,true);
    }
    public BigInteger eulerCharacteristic() {
        BigInteger result=BigInteger.ZERO;
        for(FiniteSet<Integer> simplex : simplices)
            result=result.add(simplex.size()%2==1?BigInteger.ONE:BigInteger.ONE.negate());
        return result;
    }
    public List<FiniteSet<Integer>> simplices(int dimension) {
        if(dimension<0) throw MathFailure.invalid("Negative simplex dimension");
        if(dimension>dimension()) return Collections.emptyList();
        List<FiniteSet<Integer>> result=new ArrayList<>();
        for(FiniteSet<Integer> simplex : simplices) if(simplex.size()==dimension+1) result.add(simplex);
        return Collections.unmodifiableList(result);
    }
    public int dimension() { int result=-1; for(FiniteSet<Integer> simplex : simplices) result=Math.max(result,simplex.size()-1); return result; }
    public int bettiNumber(int dimension) {
        if(dimension<0) throw MathFailure.invalid("Negative homology degree");
        if(dimension>dimension()) return 0;
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
    @Override public boolean equals(Object other) {
        return other instanceof FiniteSimplicialComplex && simplices.equals(((FiniteSimplicialComplex)other).simplices);
    }
    @Override public int hashCode() { return simplices.hashCode(); }
    @Override public String toString() {
        List<List<Integer>> faces=new ArrayList<>();
        for(FiniteSet<Integer> simplex : simplices) {
            List<Integer> vertices=new ArrayList<>(simplex.members()); Collections.sort(vertices); faces.add(vertices);
        }
        faces.sort((a,b) -> {
            int bySize=Integer.compare(a.size(),b.size()); if(bySize!=0) return bySize;
            for(int i=0;i<a.size();i++) { int order=Integer.compare(a.get(i),b.get(i)); if(order!=0) return order; }
            return 0;
        });
        return "Complex"+faces;
    }
}
