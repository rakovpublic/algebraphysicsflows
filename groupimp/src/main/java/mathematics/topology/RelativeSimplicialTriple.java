package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A labelled triple B subset A subset X, retaining all three quotient pairs. */
public final class RelativeSimplicialTriple implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialComplex outer,total,inner;
    public RelativeSimplicialTriple(RelativeSimplicialComplex outer,FiniteSimplicialComplex base) {
        this.outer=Objects.requireNonNull(outer); Objects.requireNonNull(base);
        inner=new RelativeSimplicialComplex(outer.subcomplex(),base);
        total=new RelativeSimplicialComplex(outer.ambient(),base);
    }
    public RelativeSimplicialComplex outerPair() { return outer; }
    public RelativeSimplicialComplex totalPair() { return total; }
    public RelativeSimplicialComplex innerPair() { return inner; }
    public RelativeSimplicialMap inclusionMap() { return RelativeSimplicialMap.inclusion(inner,total); }
    public RelativeSimplicialMap quotientMap() { return RelativeSimplicialMap.inclusion(total,outer); }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Triple matrix and exact-sequence degrees must be nonnegative"); }
    private static IntegralHomology homology(RelativeSimplicialComplex pair,BigInteger degree,Computation work) {
        return new IntegralHomology(pair.boundaryMatrix(degree,work),pair.boundaryMatrix(degree.add(BigInteger.ONE),work),work);
    }
    private IntegerMatrix inclusionMatrix(BigInteger degree,Computation work) {
        return RelativeSimplicialComplex.selector(total.basis(degree),inner.basis(degree),work);
    }
    private IntegerMatrix quotientMatrix(BigInteger degree,Computation work) {
        return RelativeSimplicialComplex.selector(outer.basis(degree),total.basis(degree),work);
    }
    private IntegerMatrix connectingChainMatrix(BigInteger degree,Computation work) {
        return RelativeSimplicialComplex.boundary(inner.basis(degree.subtract(BigInteger.ONE)),outer.basis(degree),work);
    }
    public IntegerMatrix inclusionMatrix(BigInteger degree) { requireDegree(degree); return inclusionMatrix(degree,new Computation()); }
    public IntegerMatrix quotientMatrix(BigInteger degree) { requireDegree(degree); return quotientMatrix(degree,new Computation()); }
    /** The zero-on-A/B section of C(X,B)->C(X,A), generally not a chain map. */
    public IntegerMatrix liftMatrix(BigInteger degree) { return quotientMatrix(degree).transpose(); }
    public IntegerMatrix connectingChainMatrix(BigInteger degree) { requireDegree(degree); return connectingChainMatrix(degree,new Computation()); }
    public AbelianGroupHomomorphism inclusionHomology(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return homology(inner,degree,work).inducedMap(homology(total,degree,work),inclusionMatrix(degree,work),work);
    }
    public AbelianGroupHomomorphism quotientHomology(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return homology(total,degree,work).inducedMap(homology(outer,degree,work),quotientMatrix(degree,work),work);
    }
    public AbelianGroupHomomorphism connectingHomology(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return homology(outer,degree,work).inducedMap(homology(inner,degree.subtract(BigInteger.ONE),work),connectingChainMatrix(degree,work),work);
    }
    /** H_k(A,B)->H_k(X,B)->H_k(X,A)->H_(k-1)(A,B), three maps in order. */
    public List<AbelianGroupHomomorphism> longExactSegment(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        IntegralHomology a=homology(inner,degree,work),x=homology(total,degree,work),q=homology(outer,degree,work),previous=homology(inner,degree.subtract(BigInteger.ONE),work);
        return Collections.unmodifiableList(Arrays.asList(a.inducedMap(x,inclusionMatrix(degree,work),work),
                x.inducedMap(q,quotientMatrix(degree,work),work),q.inducedMap(previous,connectingChainMatrix(degree,work),work)));
    }
    /** Boundary of a lift modulo B; only cycles on the complete outer pair are accepted. */
    public RelativeSimplicialChain connectCycle(RelativeSimplicialChain cycle) {
        if(!outer.equals(cycle.pair())) throw MathFailure.undefined("The connecting cycle requires the full outer pair (X,A)");
        Computation work=new Computation();
        if(!cycle.isCycle(work)) throw MathFailure.undefined("The triple connecting operation requires a relative cycle");
        return new RelativeSimplicialChain(inner,cycle.degree().subtract(BigInteger.ONE),work.apply(connectingChainMatrix(cycle.degree(),work),cycle.coordinates()));
    }
    public IntegerMatrix extensionMatrix(BigInteger degree) { return quotientMatrix(degree).transpose(); }
    public IntegerMatrix restrictionMatrix(BigInteger degree) { return inclusionMatrix(degree).transpose(); }
    public IntegerMatrix connectingCochainMatrix(BigInteger degree) {
        requireDegree(degree); return connectingChainMatrix(degree.add(BigInteger.ONE),new Computation()).transpose();
    }
    public AbelianGroupHomomorphism extensionCohomology(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return RelativeSimplicialCochain.cohomology(outer,degree,work).inducedMap(RelativeSimplicialCochain.cohomology(total,degree,work),quotientMatrix(degree,work).transpose(),work);
    }
    public AbelianGroupHomomorphism restrictionCohomology(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return RelativeSimplicialCochain.cohomology(total,degree,work).inducedMap(RelativeSimplicialCochain.cohomology(inner,degree,work),inclusionMatrix(degree,work).transpose(),work);
    }
    public AbelianGroupHomomorphism connectingCohomology(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); BigInteger next=degree.add(BigInteger.ONE);
        return RelativeSimplicialCochain.cohomology(inner,degree,work).inducedMap(RelativeSimplicialCochain.cohomology(outer,next,work),connectingChainMatrix(next,work).transpose(),work);
    }
    /** H^k(X,A)->H^k(X,B)->H^k(A,B)->H^(k+1)(X,A), three maps in order. */
    public List<AbelianGroupHomomorphism> longExactCohomologySegment(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); BigInteger next=degree.add(BigInteger.ONE);
        IntegralHomology q=RelativeSimplicialCochain.cohomology(outer,degree,work),x=RelativeSimplicialCochain.cohomology(total,degree,work),
                a=RelativeSimplicialCochain.cohomology(inner,degree,work),following=RelativeSimplicialCochain.cohomology(outer,next,work);
        return Collections.unmodifiableList(Arrays.asList(q.inducedMap(x,quotientMatrix(degree,work).transpose(),work),
                x.inducedMap(a,inclusionMatrix(degree,work).transpose(),work),a.inducedMap(following,connectingChainMatrix(next,work).transpose(),work)));
    }
    /** Extend an inner cocycle by zero into C^k(X,B), differentiate, and retain its outer coordinates. */
    public RelativeSimplicialCochain connectCocycle(RelativeSimplicialCochain cocycle) {
        if(!inner.equals(cocycle.pair())) throw MathFailure.undefined("The connecting cocycle requires the full inner pair (A,B)");
        Computation work=new Computation();
        if(!cocycle.isCocycle(work)) throw MathFailure.undefined("The triple connecting operation requires a relative cocycle");
        BigInteger next=cocycle.degree().add(BigInteger.ONE);
        return new RelativeSimplicialCochain(outer,next,work.apply(connectingChainMatrix(next,work).transpose(),cocycle.coordinates()));
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RelativeSimplicialTriple)) return false; RelativeSimplicialTriple t=(RelativeSimplicialTriple)other;
        return outer.equals(t.outer) && inner.equals(t.inner);
    }
    @Override public int hashCode() { return Objects.hash(outer,inner); }
    @Override public String toString() { return "RelativeTriple(outer="+outer+", base="+inner.subcomplex()+")"; }
}
