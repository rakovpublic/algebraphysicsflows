package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** The oriented prism witness between two contiguous maps of labelled pairs. */
public final class SimplicialHomotopy implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialMap from,to;

    public SimplicialHomotopy(RelativeSimplicialMap from,RelativeSimplicialMap to) {
        this(from,to,new Computation());
    }
    SimplicialHomotopy(RelativeSimplicialMap from,RelativeSimplicialMap to,Computation work) {
        this.from=Objects.requireNonNull(from); this.to=Objects.requireNonNull(to);
        if(!from.contiguous(to,work)) throw MathFailure.undefined("A prism homotopy requires contiguity in both the ambient and subcomplex targets");
    }
    public static SimplicialHomotopy absolute(FiniteSimplicialMap from,FiniteSimplicialMap to) {
        return new SimplicialHomotopy(RelativeSimplicialMap.absolute(from),RelativeSimplicialMap.absolute(to));
    }
    public RelativeSimplicialMap from() { return from; }
    public RelativeSimplicialMap to() { return to; }
    public RelativeSimplicialComplex source() { return from.source(); }
    public RelativeSimplicialComplex target() { return from.target(); }
    /** Recompute the prism with the endpoints exchanged; it need not be -P. */
    public SimplicialHomotopy reverse() { return new SimplicialHomotopy(to,from); }
    private static void requireDegree(BigInteger degree) {
        if(degree.signum()<0) throw MathFailure.undefined("Prism matrix degree must be nonnegative");
    }
    /** P_n[v0,...,vn] = sum_i (-1)^i [f(v0),...,f(vi),g(vi),...,g(vn)]. */
    IntegerMatrix prism(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> rows=target().basis(degree.add(BigInteger.ONE)),columns=source().basis(degree);
        work.use((long)rows.size()*columns.size());
        BigInteger[][] entries=new BigInteger[rows.size()][columns.size()];
        for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
        Map<FiniteSet<Integer>,Integer> positions=new HashMap<>();
        for(int r=0;r<rows.size();r++) positions.put(rows.get(r),r);
        for(int c=0;c<columns.size();c++) {
            List<Integer> simplex=IntegralSimplicialHomology.vertices(columns.get(c));
            for(int cut=0;cut<simplex.size();cut++) {
                int length=simplex.size()+1; work.use((long)length*length);
                List<Integer> vertices=new ArrayList<>(length);
                for(int i=0;i<=cut;i++) vertices.add(from.ambientMap().mapVertex(BigInteger.valueOf(simplex.get(i))).intValueExact());
                for(int i=cut;i<simplex.size();i++) vertices.add(to.ambientMap().mapVertex(BigInteger.valueOf(simplex.get(i))).intValueExact());
                Set<Integer> distinct=new HashSet<>(vertices);
                if(distinct.size()!=length) continue;
                int sign=(cut&1)==0?1:-1;
                for(int i=0;i<length;i++) for(int j=i+1;j<length;j++) if(vertices.get(i)>vertices.get(j)) sign=-sign;
                Integer row=positions.get(new FiniteSet<>(distinct));
                if(row!=null) entries[row][c]=entries[row][c].add(BigInteger.valueOf(sign));
            }
        }
        return new IntegerMatrix(rows.size(),columns.size(),entries);
    }
    public IntegerMatrix chainMatrix(BigInteger degree) { requireDegree(degree); return prism(degree,new Computation()); }
    /** Q^p = transpose(P_(p-1)); Q^0 has zero rows, retaining its target C^0 columns. */
    public IntegerMatrix cochainMatrix(BigInteger degree) { requireDegree(degree); return prism(degree.subtract(BigInteger.ONE),new Computation()).transpose(); }
    public List<IntegerMatrix> chainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=Math.max(source().dimension(),target().dimension());k++) result.add(prism(BigInteger.valueOf(k),work));
        return Collections.unmodifiableList(result);
    }
    public List<IntegerMatrix> cochainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=Math.max(source().dimension(),target().dimension())+1;k++) result.add(prism(BigInteger.valueOf(k-1),work).transpose());
        return Collections.unmodifiableList(result);
    }
    public RelativeSimplicialChain onChain(RelativeSimplicialChain chain) {
        if(!source().equals(chain.pair())) throw MathFailure.undefined("Prism input must retain the full source pair");
        Computation work=new Computation();
        return new RelativeSimplicialChain(target(),chain.degree().add(BigInteger.ONE),work.apply(prism(chain.degree(),work),chain.coordinates()));
    }
    public RelativeSimplicialCochain onCochain(RelativeSimplicialCochain cochain) {
        if(!target().equals(cochain.pair())) throw MathFailure.undefined("Cochain prism input must retain the full target pair");
        if(cochain.degree().signum()==0) throw MathFailure.undefined("Typed cochain prisms require positive degree; use cochain-matrix for the zero degree-minus-one group");
        Computation work=new Computation(); BigInteger degree=cochain.degree().subtract(BigInteger.ONE);
        return new RelativeSimplicialCochain(source(),degree,work.apply(prism(degree,work).transpose(),cochain.coordinates()));
    }
    private void requireAbsolute() {
        if(!source().subcomplex().faces().isEmpty() || !target().subcomplex().faces().isEmpty())
            throw MathFailure.undefined("Absolute prism actions require empty subcomplexes at both boundaries");
    }
    public SimplicialChain onAbsoluteChain(SimplicialChain chain) {
        requireAbsolute(); RelativeSimplicialChain result=onChain(RelativeSimplicialChain.absolute(chain));
        return new SimplicialChain(result.pair().ambient(),result.degree(),result.coordinates());
    }
    public SimplicialCochain onAbsoluteCochain(SimplicialCochain cochain) {
        requireAbsolute(); RelativeSimplicialCochain result=onCochain(RelativeSimplicialCochain.absolute(cochain));
        return new SimplicialCochain(result.pair().ambient(),result.degree(),result.coordinates());
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof SimplicialHomotopy)) return false; SimplicialHomotopy h=(SimplicialHomotopy)other;
        return from.equals(h.from) && to.equals(h.to);
    }
    @Override public int hashCode() { return Objects.hash(from,to); }
    @Override public String toString() { return "SimplicialHomotopy(from="+from+", to="+to+")"; }
}
