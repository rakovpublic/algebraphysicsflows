package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupElement;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Integral quotient chains on (X,A); negative-degree groups are zero. */
public final class RelativeSimplicialChain implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialComplex pair;
    private final BigInteger degree;
    private final IntegerVector coordinates;
    public RelativeSimplicialChain(RelativeSimplicialComplex pair,BigInteger degree,IntegerVector coordinates) {
        this.pair=Objects.requireNonNull(pair); this.degree=Objects.requireNonNull(degree); this.coordinates=Objects.requireNonNull(coordinates);
        if(pair.basis(degree).size()!=coordinates.dimension()) throw MathFailure.undefined("Relative chain coordinates must match the full pair and degree");
    }
    public RelativeSimplicialComplex pair() { return pair; }
    public BigInteger degree() { return degree; }
    public IntegerVector coordinates() { return coordinates; }
    public RelativeSimplicialChain withCoordinates(IntegerVector values) { return new RelativeSimplicialChain(pair,degree,values); }
    public static RelativeSimplicialChain zero(RelativeSimplicialComplex pair,BigInteger degree) { return new RelativeSimplicialChain(pair,degree,IntegerVector.zero(pair.basis(degree).size())); }
    public static List<RelativeSimplicialChain> basisChains(RelativeSimplicialComplex pair,BigInteger degree) {
        int size=pair.basis(degree).size(); List<RelativeSimplicialChain> result=new ArrayList<>();
        for(int i=0;i<size;i++) { BigInteger[] values=new BigInteger[size]; Arrays.fill(values,BigInteger.ZERO); values[i]=BigInteger.ONE; result.add(new RelativeSimplicialChain(pair,degree,new IntegerVector(values))); }
        return Collections.unmodifiableList(result);
    }
    public static RelativeSimplicialChain absolute(SimplicialChain chain) {
        return new RelativeSimplicialChain(RelativeSimplicialComplex.absolute(chain.complex()),chain.degree(),chain.coordinates());
    }
    public static RelativeSimplicialChain fromAbsolute(SimplicialChain chain,RelativeSimplicialComplex pair) {
        if(!chain.complex().equals(pair.ambient())) throw MathFailure.undefined("Projection requires the chain's full complex to equal the ambient complex");
        Computation work=new Computation(); return new RelativeSimplicialChain(pair,chain.degree(),work.apply(pair.projectionMatrix(chain.degree(),work),chain.coordinates()));
    }
    /** Zero-on-A section of quotient chain groups, generally not a chain map. */
    public SimplicialChain liftAbsolute() {
        Computation work=new Computation(); return new SimplicialChain(pair.ambient(),degree,work.apply(pair.projectionMatrix(degree,work).transpose(),coordinates));
    }
    /** Boundary of a lifted relative cycle, represented on the full subcomplex A. */
    public SimplicialChain connectCycle() {
        Computation work=new Computation();
        if(!isCycle(work)) throw MathFailure.undefined("A connecting chain requires a relative cycle");
        return new SimplicialChain(pair.subcomplex(),degree.subtract(BigInteger.ONE),work.apply(pair.connectingChainMatrix(degree,work),coordinates));
    }
    private void same(RelativeSimplicialChain other) {
        if(!pair.equals(other.pair) || !degree.equals(other.degree)) throw MathFailure.undefined("Additive relative chain operations require the same full pair and degree");
    }
    private void sameAmbient(SimplicialCochain cochain) {
        if(!pair.ambient().equals(cochain.complex())) throw MathFailure.undefined("The cochain must retain the full ambient complex");
    }
    private void samePair(RelativeSimplicialCochain cochain) {
        if(!pair.equals(cochain.pair())) throw MathFailure.undefined("The relative cochain must retain the same full pair");
    }
    public RelativeSimplicialChain add(RelativeSimplicialChain other) { same(other); return withCoordinates(coordinates.add(other.coordinates)); }
    public RelativeSimplicialChain subtract(RelativeSimplicialChain other) { return add(other.negate()); }
    public RelativeSimplicialChain scale(BigInteger scalar) { return withCoordinates(coordinates.scale(scalar)); }
    public RelativeSimplicialChain negate() { return scale(BigInteger.ONE.negate()); }
    public boolean isZero() { return coordinates.equals(IntegerVector.zero(coordinates.dimension())); }
    public RelativeSimplicialChain boundary() {
        Computation work=new Computation(); return new RelativeSimplicialChain(pair,degree.subtract(BigInteger.ONE),work.apply(pair.boundaryMatrix(degree,work),coordinates));
    }
    boolean isCycle(Computation work) {
        IntegerVector value=work.apply(pair.boundaryMatrix(degree,work),coordinates); return value.equals(IntegerVector.zero(value.dimension()));
    }
    public boolean isCycle() { return isCycle(new Computation()); }
    public boolean isBoundary() { Computation work=new Computation(); return work.hasSolution(pair.boundaryMatrix(degree.add(BigInteger.ONE),work),coordinates); }
    public IntegerVector boundingCoordinates() { Computation work=new Computation(); return work.solve(pair.boundaryMatrix(degree.add(BigInteger.ONE),work),coordinates); }
    public boolean homologous(RelativeSimplicialChain other) {
        same(other); Computation work=new Computation();
        if(!isCycle(work) || !other.isCycle(work)) throw MathFailure.undefined("Relative homology comparison requires two cycles");
        return work.hasSolution(pair.boundaryMatrix(degree.add(BigInteger.ONE),work),coordinates.add(other.coordinates.scale(BigInteger.ONE.negate())));
    }
    private IntegralHomology homology(BigInteger degree,Computation work) {
        return new IntegralHomology(pair.boundaryMatrix(degree,work),pair.boundaryMatrix(degree.add(BigInteger.ONE),work),work);
    }
    public IntegralHomology homology() { return homology(degree,new Computation()); }
    public AbelianGroupElement classOf() { Computation work=new Computation(); return homology(degree,work).classOf(coordinates,work); }
    public RelativeSimplicialChain representative(AbelianGroupElement element) {
        Computation work=new Computation(); return withCoordinates(homology(degree,work).representative(element,work));
    }
    public List<RelativeSimplicialChain> cycleGenerators() {
        Computation work=new Computation(); List<RelativeSimplicialChain> result=new ArrayList<>();
        for(IntegerVector generator : homology(degree,work).generators(work)) result.add(withCoordinates(generator)); return Collections.unmodifiableList(result);
    }
    public RelativeSimplicialChain pushforward(RelativeSimplicialMap map) {
        if(!pair.equals(map.source())) throw MathFailure.undefined("Relative pushforward requires the full source pair");
        if(degree.signum()<0) return zero(map.target(),degree);
        Computation work=new Computation(); return new RelativeSimplicialChain(map.target(),degree,work.apply(map.chainMatrix(degree,work),coordinates));
    }
    public BigInteger evaluate(RelativeSimplicialCochain cochain) {
        samePair(cochain); if(!degree.equals(cochain.degree())) throw MathFailure.undefined("Relative pairing requires equal degrees");
        return coordinates.dot(cochain.coordinates());
    }
    private static void requireCochainDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("The cochain degree must be nonnegative"); }
    private IntegerMatrix capMatrix(BigInteger p,IntegerVector coefficients,boolean fixedCochain,boolean relativeCochain,Computation work) {
        requireCochainDegree(p);
        List<FiniteSet<Integer>> cochains=relativeCochain?pair.basis(p):SimplicialChain.basis(pair.ambient(),p);
        List<FiniteSet<Integer>> target=relativeCochain?SimplicialChain.basis(pair.ambient(),degree.subtract(p)):pair.basis(degree.subtract(p));
        return SimplicialChain.capMatrix(pair.basis(degree),cochains,target,p,coefficients,fixedCochain,work);
    }
    public IntegerMatrix capMatrix(SimplicialCochain cochain) { sameAmbient(cochain); return capMatrix(cochain.degree(),cochain.coordinates(),true,false,new Computation()); }
    public IntegerMatrix relativeCapMatrix(RelativeSimplicialCochain cochain) { samePair(cochain); return capMatrix(cochain.degree(),cochain.coordinates(),true,true,new Computation()); }
    public IntegerMatrix capCohomologyMatrix(BigInteger p) { return capMatrix(p,coordinates,false,false,new Computation()); }
    public IntegerMatrix relativeCapCohomologyMatrix(BigInteger p) { return capMatrix(p,coordinates,false,true,new Computation()); }
    public RelativeSimplicialChain cap(SimplicialCochain cochain) {
        sameAmbient(cochain); Computation work=new Computation();
        return new RelativeSimplicialChain(pair,degree.subtract(cochain.degree()),work.apply(capMatrix(cochain.degree(),cochain.coordinates(),true,false,work),coordinates));
    }
    /** C_n(X,A) times C^p(X,A) -> C_(n-p)(X), since cochains vanish on A. */
    public SimplicialChain relativeCap(RelativeSimplicialCochain cochain) {
        samePair(cochain); Computation work=new Computation();
        return new SimplicialChain(pair.ambient(),degree.subtract(cochain.degree()),work.apply(capMatrix(cochain.degree(),cochain.coordinates(),true,true,work),coordinates));
    }
    public AbelianGroupElement capClass(SimplicialCochain cochain) {
        sameAmbient(cochain); Computation work=new Computation();
        if(!isCycle(work) || !cochain.isCocycle(work)) throw MathFailure.undefined("Relative cap classes require a cycle and a cocycle");
        return homology(degree.subtract(cochain.degree()),work).classOf(work.apply(capMatrix(cochain.degree(),cochain.coordinates(),true,false,work),coordinates),work);
    }
    public AbelianGroupElement relativeCapClass(RelativeSimplicialCochain cochain) {
        samePair(cochain); Computation work=new Computation();
        if(!isCycle(work) || !cochain.isCocycle(work)) throw MathFailure.undefined("Relative cap classes require a cycle and a cocycle");
        return SimplicialChain.homology(pair.ambient(),degree.subtract(cochain.degree()),work).classOf(work.apply(capMatrix(cochain.degree(),cochain.coordinates(),true,true,work),coordinates),work);
    }
    /** The chain value supplies only the pair and source degree; its coefficients are not used. */
    public AbelianGroupHomomorphism capHomologyMap(SimplicialCochain cochain) {
        sameAmbient(cochain); Computation work=new Computation();
        if(!cochain.isCocycle(work)) throw MathFailure.undefined("A cap homology map requires a cocycle");
        return homology(degree,work).inducedMap(homology(degree.subtract(cochain.degree()),work),capMatrix(cochain.degree(),cochain.coordinates(),true,false,work),work);
    }
    public AbelianGroupHomomorphism relativeCapHomologyMap(RelativeSimplicialCochain cochain) {
        samePair(cochain); Computation work=new Computation();
        if(!cochain.isCocycle(work)) throw MathFailure.undefined("A cap homology map requires a cocycle");
        return homology(degree,work).inducedMap(SimplicialChain.homology(pair.ambient(),degree.subtract(cochain.degree()),work),capMatrix(cochain.degree(),cochain.coordinates(),true,true,work),work);
    }
    public AbelianGroupHomomorphism capCohomologyMap(BigInteger p) {
        requireCochainDegree(p); Computation work=new Computation();
        if(!isCycle(work)) throw MathFailure.undefined("A cap cohomology map requires a relative cycle");
        return SimplicialCochain.cohomology(pair.ambient(),p,work).inducedMap(homology(degree.subtract(p),work),capMatrix(p,coordinates,false,false,work),work);
    }
    public AbelianGroupHomomorphism relativeCapCohomologyMap(BigInteger p) {
        requireCochainDegree(p); Computation work=new Computation();
        if(!isCycle(work)) throw MathFailure.undefined("A cap cohomology map requires a relative cycle");
        return RelativeSimplicialCochain.cohomology(pair,p,work).inducedMap(SimplicialChain.homology(pair.ambient(),degree.subtract(p),work),capMatrix(p,coordinates,false,true,work),work);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RelativeSimplicialChain)) return false; RelativeSimplicialChain c=(RelativeSimplicialChain)other;
        return pair.equals(c.pair) && degree.equals(c.degree) && coordinates.equals(c.coordinates);
    }
    @Override public int hashCode() { return Objects.hash(pair,degree,coordinates); }
    @Override public String toString() { return "RelativeChain(pair="+pair+", degree="+degree+", coordinates="+coordinates+")"; }
}
