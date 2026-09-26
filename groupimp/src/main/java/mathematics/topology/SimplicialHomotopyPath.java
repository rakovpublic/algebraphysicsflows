package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A finite supplied contiguity path, retaining every stage and summing its oriented prisms. */
public final class SimplicialHomotopyPath implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_STAGES=256;
    private final List<RelativeSimplicialMap> stages;
    private final List<SimplicialHomotopy> steps;

    public SimplicialHomotopyPath(List<RelativeSimplicialMap> stages) { this(stages,new Computation()); }
    private SimplicialHomotopyPath(List<RelativeSimplicialMap> stages,Computation work) {
        Objects.requireNonNull(stages);
        if(stages.isEmpty()) throw MathFailure.undefined("A homotopy path needs at least one retained map");
        if(stages.size()>MAX_STAGES) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"A homotopy path allows at most 256 stages");
        List<RelativeSimplicialMap> copy=new ArrayList<>(stages.size());
        for(RelativeSimplicialMap stage : stages) { work.use(1); copy.add(Objects.requireNonNull(stage)); }
        List<SimplicialHomotopy> witnesses=new ArrayList<>();
        for(int i=1;i<copy.size();i++) witnesses.add(new SimplicialHomotopy(copy.get(i-1),copy.get(i),work));
        this.stages=Collections.unmodifiableList(copy); steps=Collections.unmodifiableList(witnesses);
    }
    public static SimplicialHomotopyPath fromHomotopy(SimplicialHomotopy witness) { return new SimplicialHomotopyPath(Arrays.asList(witness.from(),witness.to())); }
    public static SimplicialHomotopyPath stationary(RelativeSimplicialMap map) { return new SimplicialHomotopyPath(Collections.singletonList(map)); }
    public RelativeSimplicialMap from() { return stages.get(0); }
    public RelativeSimplicialMap to() { return stages.get(stages.size()-1); }
    public RelativeSimplicialComplex source() { return from().source(); }
    public RelativeSimplicialComplex target() { return from().target(); }
    public BigInteger stepCount() { return BigInteger.valueOf(steps.size()); }
    public List<RelativeSimplicialMap> stages() { return stages; }
    public List<SimplicialHomotopy> steps() { return steps; }
    public SimplicialHomotopyPath append(RelativeSimplicialMap next) {
        List<RelativeSimplicialMap> result=new ArrayList<>(stages); result.add(next); return new SimplicialHomotopyPath(result);
    }
    /** Chronological concatenation: this path first, then the supplied path. */
    public SimplicialHomotopyPath then(SimplicialHomotopyPath next) {
        if(!to().equals(next.from())) throw MathFailure.undefined("Concatenation requires the same full joining map, not just matching pairs");
        List<RelativeSimplicialMap> result=new ArrayList<>(stages); result.addAll(next.stages.subList(1,next.stages.size())); return new SimplicialHomotopyPath(result);
    }
    /** Reverse the stages and recompute their prisms, rather than negating the accumulated matrix. */
    public SimplicialHomotopyPath reverse() {
        List<RelativeSimplicialMap> result=new ArrayList<>(stages); Collections.reverse(result); return new SimplicialHomotopyPath(result);
    }
    public SimplicialHomotopyPath precompose(RelativeSimplicialMap before) {
        Computation work=new Computation(); List<RelativeSimplicialMap> result=new ArrayList<>();
        for(RelativeSimplicialMap stage : stages) result.add(stage.compose(before,work)); return new SimplicialHomotopyPath(result,work);
    }
    public SimplicialHomotopyPath postcompose(RelativeSimplicialMap after) {
        Computation work=new Computation(); List<RelativeSimplicialMap> result=new ArrayList<>();
        for(RelativeSimplicialMap stage : stages) result.add(after.compose(stage,work)); return new SimplicialHomotopyPath(result,work);
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Path matrix degree must be nonnegative"); }
    private IntegerMatrix prism(BigInteger degree,Computation work) {
        int rows=target().basis(degree.add(BigInteger.ONE)).size(),columns=source().basis(degree).size();
        long size=(long)rows*columns; work.use(size); IntegerMatrix result=IntegerMatrix.zero(rows,columns);
        for(SimplicialHomotopy step : steps) {
            IntegerMatrix term=step.prism(degree,work); work.use(size); result=result.add(term);
        }
        return result;
    }
    public IntegerMatrix chainMatrix(BigInteger degree) { requireDegree(degree); return prism(degree,new Computation()); }
    public IntegerMatrix cochainMatrix(BigInteger degree) { requireDegree(degree); return prism(degree.subtract(BigInteger.ONE),new Computation()).transpose(); }
    public List<IntegerMatrix> chainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>(); int top=Math.max(source().dimension(),target().dimension());
        for(int k=0;k<=top;k++) result.add(prism(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public List<IntegerMatrix> cochainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>(); int top=Math.max(source().dimension(),target().dimension())+1;
        for(int k=0;k<=top;k++) result.add(prism(BigInteger.valueOf(k-1),work).transpose()); return Collections.unmodifiableList(result);
    }
    public RelativeSimplicialChain onChain(RelativeSimplicialChain chain) {
        if(!source().equals(chain.pair())) throw MathFailure.undefined("Path chain action requires the full source pair");
        Computation work=new Computation(); return new RelativeSimplicialChain(target(),chain.degree().add(BigInteger.ONE),work.apply(prism(chain.degree(),work),chain.coordinates()));
    }
    public RelativeSimplicialCochain onCochain(RelativeSimplicialCochain cochain) {
        if(!target().equals(cochain.pair())) throw MathFailure.undefined("Path cochain action requires the full target pair");
        if(cochain.degree().signum()==0) throw MathFailure.undefined("Typed path cochain actions require positive degree; use cochain-matrix for degree zero");
        Computation work=new Computation(); BigInteger degree=cochain.degree().subtract(BigInteger.ONE);
        return new RelativeSimplicialCochain(source(),degree,work.apply(prism(degree,work).transpose(),cochain.coordinates()));
    }
    private void requireAbsolute() {
        if(!source().subcomplex().faces().isEmpty() || !target().subcomplex().faces().isEmpty()) throw MathFailure.undefined("Absolute path actions require empty source and target subcomplexes");
    }
    public SimplicialChain onAbsoluteChain(SimplicialChain chain) {
        requireAbsolute(); RelativeSimplicialChain result=onChain(RelativeSimplicialChain.absolute(chain));
        return new SimplicialChain(target().ambient(),result.degree(),result.coordinates());
    }
    public SimplicialCochain onAbsoluteCochain(SimplicialCochain cochain) {
        requireAbsolute(); RelativeSimplicialCochain result=onCochain(RelativeSimplicialCochain.absolute(cochain));
        return new SimplicialCochain(source().ambient(),result.degree(),result.coordinates());
    }
    @Override public boolean equals(Object other) { return other instanceof SimplicialHomotopyPath && stages.equals(((SimplicialHomotopyPath)other).stages); }
    @Override public int hashCode() { return stages.hashCode(); }
    @Override public String toString() { return "HomotopyPath(stages="+stages+")"; }
}
