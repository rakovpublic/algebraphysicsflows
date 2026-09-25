package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupElement;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

/** A chain on (X,D) and a chosen target (X,B), for caps by cochains on (X,A) with D=A union B. */
public final class RelativeCapProduct implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialChain chain;
    private final RelativeSimplicialComplex target;
    public RelativeCapProduct(RelativeSimplicialChain chain,RelativeSimplicialComplex target) {
        this.chain=Objects.requireNonNull(chain); this.target=Objects.requireNonNull(target);
        if(!chain.pair().ambient().equals(target.ambient()) || !target.subcomplex().subcomplexOf(chain.pair().subcomplex()))
            throw MathFailure.undefined("A relative cap target must have the same full ambient complex and B contained in the chain subcomplex D");
    }
    public RelativeSimplicialChain chain() { return chain; }
    public RelativeSimplicialComplex targetPair() { return target; }
    /** Replace the chain, allowing another degree but retaining its full source pair. */
    public RelativeCapProduct withChain(RelativeSimplicialChain value) {
        if(!chain.pair().equals(value.pair())) throw MathFailure.undefined("Replacing a cap chain must retain the same full source pair");
        return new RelativeCapProduct(value,target);
    }
    public RelativeCapProduct boundary() { return withChain(chain.boundary()); }
    private void compatible(RelativeSimplicialCochain cochain) {
        if(!target.ambient().equals(cochain.pair().ambient()) ||
                !chain.pair().subcomplex().equals(cochain.pair().subcomplex().union(target.subcomplex())))
            throw MathFailure.undefined("Relative cap requires the same full ambient X and source subcomplex D exactly equal to A union B");
    }
    private static IntegralHomology homology(RelativeSimplicialComplex pair,BigInteger degree,Computation work) {
        return new IntegralHomology(pair.boundaryMatrix(degree,work),pair.boundaryMatrix(degree.add(BigInteger.ONE),work),work);
    }
    private IntegerMatrix matrix(RelativeSimplicialCochain cochain,boolean fixedCochain,Computation work) {
        return SimplicialChain.capMatrix(chain.pair().basis(chain.degree()),cochain.pair().basis(cochain.degree()),
                target.basis(chain.degree().subtract(cochain.degree())),cochain.degree(),
                fixedCochain?cochain.coordinates():chain.coordinates(),fixedCochain,work);
    }
    /** C_n(X,A union B) times C^p(X,A) -> C_(n-p)(X,B). */
    public RelativeSimplicialChain cap(RelativeSimplicialCochain cochain) {
        compatible(cochain); Computation work=new Computation();
        return new RelativeSimplicialChain(target,chain.degree().subtract(cochain.degree()),work.apply(matrix(cochain,true,work),chain.coordinates()));
    }
    public AbelianGroupElement capClass(RelativeSimplicialCochain cochain) {
        compatible(cochain); Computation work=new Computation();
        if(!chain.isCycle(work) || !cochain.isCocycle(work)) throw MathFailure.undefined("Relative cap classes require a cycle and a cocycle");
        return homology(target,chain.degree().subtract(cochain.degree()),work).classOf(work.apply(matrix(cochain,true,work),chain.coordinates()),work);
    }
    /** Fix the cochain; the stored chain supplies only its full pair and degree. */
    public IntegerMatrix capMatrix(RelativeSimplicialCochain cochain) { compatible(cochain); return matrix(cochain,true,new Computation()); }
    public AbelianGroupHomomorphism capHomologyMap(RelativeSimplicialCochain cochain) {
        compatible(cochain); Computation work=new Computation();
        if(!cochain.isCocycle(work)) throw MathFailure.undefined("A cap homology map requires a cocycle");
        return homology(chain.pair(),chain.degree(),work).inducedMap(homology(target,chain.degree().subtract(cochain.degree()),work),matrix(cochain,true,work),work);
    }
    /** Fix the chain; the cochain supplies only its full pair and degree, not its coordinates. */
    public IntegerMatrix capCohomologyMatrix(RelativeSimplicialCochain template) { compatible(template); return matrix(template,false,new Computation()); }
    public AbelianGroupHomomorphism capCohomologyMap(RelativeSimplicialCochain template) {
        compatible(template); Computation work=new Computation();
        if(!chain.isCycle(work)) throw MathFailure.undefined("A cap cohomology map requires a relative cycle");
        return RelativeSimplicialCochain.cohomology(template.pair(),template.degree(),work).inducedMap(
                homology(target,chain.degree().subtract(template.degree()),work),matrix(template,false,work),work);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RelativeCapProduct)) return false; RelativeCapProduct c=(RelativeCapProduct)other;
        return chain.equals(c.chain) && target.equals(c.target);
    }
    @Override public int hashCode() { return Objects.hash(chain,target); }
    @Override public String toString() { return "RelativeCap(chain="+chain+", target="+target+")"; }
}
