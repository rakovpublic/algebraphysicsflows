package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupElement;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Integral cochains on (X,A), stored only on oriented simplices outside A. */
public final class RelativeSimplicialCochain implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialComplex pair;
    private final BigInteger degree;
    private final IntegerVector coordinates;
    public RelativeSimplicialCochain(RelativeSimplicialComplex pair,BigInteger degree,IntegerVector coordinates) {
        this.pair=Objects.requireNonNull(pair); this.degree=Objects.requireNonNull(degree); this.coordinates=Objects.requireNonNull(coordinates);
        requireDegree(degree);
        if(pair.simplexCount(degree)!=coordinates.dimension()) throw MathFailure.undefined("Relative cochain coordinates must match the pair and degree");
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Relative cochain degree must be nonnegative"); }
    public RelativeSimplicialComplex pair() { return pair; }
    public BigInteger degree() { return degree; }
    public IntegerVector coordinates() { return coordinates; }
    public RelativeSimplicialCochain withCoordinates(IntegerVector values) { return new RelativeSimplicialCochain(pair,degree,values); }
    public static RelativeSimplicialCochain zero(RelativeSimplicialComplex pair,BigInteger degree) {
        return new RelativeSimplicialCochain(pair,degree,IntegerVector.zero(pair.simplexCount(degree)));
    }
    public static List<RelativeSimplicialCochain> basisCochains(RelativeSimplicialComplex pair,BigInteger degree) {
        int size=pair.simplexCount(degree); List<RelativeSimplicialCochain> result=new ArrayList<>();
        for(int i=0;i<size;i++) { BigInteger[] entries=new BigInteger[size]; Arrays.fill(entries,BigInteger.ZERO); entries[i]=BigInteger.ONE; result.add(new RelativeSimplicialCochain(pair,degree,new IntegerVector(entries))); }
        return Collections.unmodifiableList(result);
    }
    public static RelativeSimplicialCochain absolute(SimplicialCochain cochain) {
        return new RelativeSimplicialCochain(RelativeSimplicialComplex.absolute(cochain.complex()),cochain.degree(),cochain.coordinates());
    }
    public static RelativeSimplicialCochain fromAbsolute(SimplicialCochain cochain,RelativeSimplicialComplex pair) {
        if(!cochain.complex().equals(pair.ambient())) throw MathFailure.undefined("The absolute cochain must retain the full ambient complex");
        Computation work=new Computation(); IntegerVector restricted=work.apply(pair.inclusionMatrix(cochain.degree(),work).transpose(),cochain.coordinates());
        if(!restricted.equals(IntegerVector.zero(restricted.dimension()))) throw MathFailure.undefined("A relative cochain must vanish on the subcomplex");
        return new RelativeSimplicialCochain(pair,cochain.degree(),work.apply(pair.projectionMatrix(cochain.degree(),work),cochain.coordinates()));
    }
    public SimplicialCochain extendByZero() {
        Computation work=new Computation(); return new SimplicialCochain(pair.ambient(),degree,work.apply(pair.projectionMatrix(degree,work).transpose(),coordinates));
    }
    private void same(RelativeSimplicialCochain other) {
        if(!pair.equals(other.pair) || !degree.equals(other.degree)) throw MathFailure.undefined("Additive relative cochain operations require the same full pair and degree");
    }
    public RelativeSimplicialCochain add(RelativeSimplicialCochain other) { same(other); return withCoordinates(coordinates.add(other.coordinates)); }
    public RelativeSimplicialCochain subtract(RelativeSimplicialCochain other) { return add(other.negate()); }
    public RelativeSimplicialCochain scale(BigInteger scalar) { return withCoordinates(coordinates.scale(scalar)); }
    public RelativeSimplicialCochain negate() { return scale(BigInteger.ONE.negate()); }
    public boolean isZero() { return coordinates.equals(IntegerVector.zero(coordinates.dimension())); }
    public BigInteger evaluate(IntegerVector chain) { return coordinates.dot(chain); }
    private static IntegerMatrix differential(RelativeSimplicialComplex pair,BigInteger degree,Computation work) {
        requireDegree(degree); return pair.boundaryMatrix(degree.add(BigInteger.ONE),work).transpose();
    }
    public RelativeSimplicialCochain coboundary() {
        Computation work=new Computation(); return new RelativeSimplicialCochain(pair,degree.add(BigInteger.ONE),work.apply(differential(pair,degree,work),coordinates));
    }
    boolean isCocycle(Computation work) {
        IntegerVector result=work.apply(differential(pair,degree,work),coordinates); return result.equals(IntegerVector.zero(result.dimension()));
    }
    public boolean isCocycle() { return isCocycle(new Computation()); }
    public boolean isCoboundary() { Computation work=new Computation(); return work.hasSolution(pair.boundaryMatrix(degree,work).transpose(),coordinates); }
    public IntegerVector coboundingCoordinates() { Computation work=new Computation(); return work.solve(pair.boundaryMatrix(degree,work).transpose(),coordinates); }
    public boolean cohomologous(RelativeSimplicialCochain other) {
        same(other); Computation work=new Computation();
        if(!isCocycle(work) || !other.isCocycle(work)) throw MathFailure.undefined("Relative cohomology comparison requires two cocycles");
        return work.hasSolution(pair.boundaryMatrix(degree,work).transpose(),coordinates.add(other.coordinates.scale(BigInteger.ONE.negate())));
    }
    /** C^p(X,A) times C^q(X,B) -> C^(p+q)(X,A union B). */
    public RelativeSimplicialCochain cup(RelativeSimplicialCochain other) { return cup(other,new Computation()); }
    private RelativeSimplicialCochain cup(RelativeSimplicialCochain other,Computation work) {
        if(!pair.ambient().equals(other.pair.ambient())) throw MathFailure.undefined("Relative cup products require the same labelled ambient complex");
        RelativeSimplicialComplex target=new RelativeSimplicialComplex(pair.ambient(),pair.subcomplex().union(other.pair.subcomplex()));
        BigInteger total=degree.add(other.degree);
        return new RelativeSimplicialCochain(target,total,SimplicialCochain.cupCoordinates(pair.basis(degree),coordinates,other.pair.basis(other.degree),other.coordinates,target.basis(total),degree,work));
    }
    public static IntegralHomology cohomology(RelativeSimplicialComplex pair,BigInteger degree) { return cohomology(pair,degree,new Computation()); }
    static IntegralHomology cohomology(RelativeSimplicialComplex pair,BigInteger degree,Computation work) {
        requireDegree(degree); return new IntegralHomology(differential(pair,degree,work),pair.boundaryMatrix(degree,work).transpose(),work);
    }
    public IntegralHomology cohomology() { return cohomology(pair,degree); }
    public static List<IntegralHomology> cohomologyDegrees(RelativeSimplicialComplex pair) {
        Computation work=new Computation(); List<IntegralHomology> result=new ArrayList<>();
        for(int k=0;k<=pair.ambient().dimension();k++) result.add(cohomology(pair,BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupElement classOf() { Computation work=new Computation(); return cohomology(pair,degree,work).classOf(coordinates,work); }
    public RelativeSimplicialCochain representative(AbelianGroupElement element) {
        Computation work=new Computation(); return withCoordinates(cohomology(pair,degree,work).representative(element,work));
    }
    public List<RelativeSimplicialCochain> cocycleGenerators() {
        Computation work=new Computation(); List<RelativeSimplicialCochain> result=new ArrayList<>();
        for(IntegerVector generator : cohomology(pair,degree,work).generators(work)) result.add(withCoordinates(generator)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupElement cupClass(RelativeSimplicialCochain other) {
        Computation work=new Computation();
        if(!isCocycle(work) || !other.isCocycle(work)) throw MathFailure.undefined("Relative cup classes require two cocycles");
        RelativeSimplicialCochain product=cup(other,work); return cohomology(product.pair,product.degree,work).classOf(product.coordinates,work);
    }
    public RelativeSimplicialCochain pullback(RelativeSimplicialMap map) {
        if(!pair.equals(map.target())) throw MathFailure.undefined("Relative pullback requires the complete target pair");
        Computation work=new Computation(); return new RelativeSimplicialCochain(map.source(),degree,work.apply(map.chainMatrix(degree,work).transpose(),coordinates));
    }
    public static IntegerMatrix extensionMatrix(RelativeSimplicialComplex pair,BigInteger degree) {
        requireDegree(degree); return pair.projectionMatrix(degree,new Computation()).transpose();
    }
    public static IntegerMatrix restrictionMatrix(RelativeSimplicialComplex pair,BigInteger degree) {
        requireDegree(degree); return pair.inclusionMatrix(degree,new Computation()).transpose();
    }
    public static IntegerMatrix connectingCochainMatrix(RelativeSimplicialComplex pair,BigInteger degree) {
        requireDegree(degree); return pair.connectingChainMatrix(degree.add(BigInteger.ONE),new Computation()).transpose();
    }
    /** Extend a cocycle on A by zero outside A, differentiate, and retain its relative coordinates. */
    public static RelativeSimplicialCochain connectCocycle(SimplicialCochain cochain,RelativeSimplicialComplex pair) {
        if(!cochain.complex().equals(pair.subcomplex())) throw MathFailure.undefined("The connecting cocycle must be on the full subcomplex");
        Computation work=new Computation();
        if(!cochain.isCocycle(work)) throw MathFailure.undefined("The connecting operation requires a cocycle");
        return new RelativeSimplicialCochain(pair,cochain.degree().add(BigInteger.ONE),work.apply(pair.connectingChainMatrix(cochain.degree().add(BigInteger.ONE),work).transpose(),cochain.coordinates()));
    }
    public static AbelianGroupHomomorphism ambientCohomologyMap(RelativeSimplicialComplex pair,BigInteger degree) {
        Computation work=new Computation(); return cohomology(pair,degree,work).inducedMap(SimplicialCochain.cohomology(pair.ambient(),degree,work),pair.projectionMatrix(degree,work).transpose(),work);
    }
    public static AbelianGroupHomomorphism restrictionCohomologyMap(RelativeSimplicialComplex pair,BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return SimplicialCochain.cohomology(pair.ambient(),degree,work).inducedMap(SimplicialCochain.cohomology(pair.subcomplex(),degree,work),pair.inclusionMatrix(degree,work).transpose(),work);
    }
    public static AbelianGroupHomomorphism connectingCohomologyMap(RelativeSimplicialComplex pair,BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); BigInteger next=degree.add(BigInteger.ONE);
        return SimplicialCochain.cohomology(pair.subcomplex(),degree,work).inducedMap(cohomology(pair,next,work),pair.connectingChainMatrix(next,work).transpose(),work);
    }
    /** H^k(X,A)->H^k(X)->H^k(A)->H^(k+1)(X,A), in order, with a shared work budget. */
    public static List<AbelianGroupHomomorphism> longExactSegment(RelativeSimplicialComplex pair,BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); BigInteger next=degree.add(BigInteger.ONE);
        IntegralHomology relative=cohomology(pair,degree,work),ambient=SimplicialCochain.cohomology(pair.ambient(),degree,work),subcomplex=SimplicialCochain.cohomology(pair.subcomplex(),degree,work),relativeNext=cohomology(pair,next,work);
        return Collections.unmodifiableList(Arrays.asList(relative.inducedMap(ambient,pair.projectionMatrix(degree,work).transpose(),work),
                ambient.inducedMap(subcomplex,pair.inclusionMatrix(degree,work).transpose(),work),
                subcomplex.inducedMap(relativeNext,pair.connectingChainMatrix(next,work).transpose(),work)));
    }
    public static AbelianGroupHomomorphism cohomologyMap(RelativeSimplicialMap map,BigInteger degree) { return cohomologyMap(map,degree,new Computation()); }
    private static AbelianGroupHomomorphism cohomologyMap(RelativeSimplicialMap map,BigInteger degree,Computation work) {
        IntegralHomology from=cohomology(map.target(),degree,work),to=map.source().equals(map.target())?from:cohomology(map.source(),degree,work);
        return from.inducedMap(to,map.chainMatrix(degree,work).transpose(),work);
    }
    public static List<AbelianGroupHomomorphism> cohomologyMaps(RelativeSimplicialMap map) {
        Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        for(int k=0;k<=Math.max(map.source().ambient().dimension(),map.target().ambient().dimension());k++) result.add(cohomologyMap(map,BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    /** Four maps from the target pair's cohomology segment to the source pair's segment. */
    public static List<AbelianGroupHomomorphism> longExactMaps(RelativeSimplicialMap map,BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return Collections.unmodifiableList(Arrays.asList(cohomologyMap(map,degree,work),SimplicialCochain.cohomologyMap(map.ambientMap(),degree,work),
                SimplicialCochain.cohomologyMap(map.subcomplexMap(),degree,work),cohomologyMap(map,degree.add(BigInteger.ONE),work)));
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RelativeSimplicialCochain)) return false; RelativeSimplicialCochain c=(RelativeSimplicialCochain)other;
        return pair.equals(c.pair) && degree.equals(c.degree) && coordinates.equals(c.coordinates);
    }
    @Override public int hashCode() { return Objects.hash(pair,degree,coordinates); }
    @Override public String toString() { return "RelativeCochain(pair="+pair+", degree="+degree+", coordinates="+coordinates+")"; }
}
