package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupElement;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Integral chains with the exact defining cone and homogeneous degree retained. */
public final class IntegralConeChain implements Serializable {
    private static final long serialVersionUID=1L;
    private final IntegralChainMappingCone cone;
    private final BigInteger degree;
    private final IntegerVector coordinates;
    public IntegralConeChain(IntegralChainMappingCone cone,BigInteger degree,IntegerVector coordinates) {
        this.cone=Objects.requireNonNull(cone); this.degree=Objects.requireNonNull(degree); this.coordinates=Objects.requireNonNull(coordinates);
        if(cone.rank(degree)!=coordinates.dimension()) throw MathFailure.undefined("Cone chains coordinates must match the exact cone and degree");
    }
    public IntegralChainMappingCone cone() { return cone; }
    public BigInteger degree() { return degree; }
    public IntegerVector coordinates() { return coordinates; }
    public IntegralConeChain withCoordinates(IntegerVector values) { return new IntegralConeChain(cone,degree,values); }
    public static IntegralConeChain zero(IntegralChainMappingCone cone,BigInteger degree) { return new IntegralConeChain(cone,degree,IntegerVector.zero(cone.rank(degree))); }
    public static List<IntegralConeChain> basis(IntegralChainMappingCone cone,BigInteger degree) {
        IntegralConeChain zero=zero(cone,degree); int size=zero.coordinates.dimension(); List<IntegralConeChain> result=new ArrayList<>();
        for(int i=0;i<size;i++) { BigInteger[] values=new BigInteger[size]; Arrays.fill(values,BigInteger.ZERO); values[i]=BigInteger.ONE; result.add(zero.withCoordinates(new IntegerVector(values))); }
        return Collections.unmodifiableList(result);
    }
    private void same(IntegralConeChain other) {
        if(!cone.equals(other.cone) || !degree.equals(other.degree)) throw MathFailure.undefined("Additive cone chains operations require the exact cone and degree");
    }
    public IntegralConeChain add(IntegralConeChain other) { same(other); return withCoordinates(coordinates.add(other.coordinates)); }
    public IntegralConeChain subtract(IntegralConeChain other) { same(other); return withCoordinates(coordinates.add(other.coordinates.scale(BigInteger.ONE.negate()))); }
    public IntegralConeChain scale(BigInteger scalar) { return withCoordinates(coordinates.scale(scalar)); }
    public IntegralConeChain negate() { return scale(BigInteger.ONE.negate()); }
    public boolean isZero() { return coordinates.equals(IntegerVector.zero(coordinates.dimension())); }
    private IntegerMatrix outgoing(Computation work) { return cone.boundary(degree,work); }
    private IntegerMatrix incoming(Computation work) { return cone.boundary(degree.add(BigInteger.ONE),work); }
    public IntegralConeChain boundary() {
        Computation work=new Computation(); return new IntegralConeChain(cone,degree.subtract(BigInteger.ONE),work.apply(outgoing(work),coordinates));
    }
    private boolean closed(Computation work) { IntegerVector image=work.apply(outgoing(work),coordinates); return image.equals(IntegerVector.zero(image.dimension())); }
    public boolean isCycle() { return closed(new Computation()); }
    public boolean isBoundary() { Computation work=new Computation(); return work.hasSolution(incoming(work),coordinates); }
    public IntegerVector boundingCoordinates() { Computation work=new Computation(); return work.solve(incoming(work),coordinates); }
    public IntegralConeChain boundingChain() {
        Computation work=new Computation(); return new IntegralConeChain(cone,degree.add(BigInteger.ONE),work.solve(incoming(work),coordinates));
    }
    public boolean homologous(IntegralConeChain other) {
        same(other); Computation work=new Computation();
        if(!closed(work) || !other.closed(work)) throw MathFailure.undefined("Cone homology comparison requires two cycles");
        return work.hasSolution(incoming(work),coordinates.add(other.coordinates.scale(BigInteger.ONE.negate())));
    }
    public IntegralHomology homology() { return cone.homology(degree,false,new Computation()); }
    public AbelianGroupElement classOf() { Computation work=new Computation(); return cone.homology(degree,false,work).classOf(coordinates,work); }
    public IntegralConeChain representative(AbelianGroupElement element) {
        Computation work=new Computation(); return withCoordinates(cone.homology(degree,false,work).representative(element,work));
    }
    public List<IntegralConeChain> cycleGenerators() {
        Computation work=new Computation(); List<IntegralConeChain> result=new ArrayList<>();
        for(IntegerVector vector : cone.homology(degree,false,work).generators(work)) result.add(withCoordinates(vector)); return Collections.unmodifiableList(result);
    }
    public RelativeSimplicialChain targetPart() {
        int size=cone.target().basis(degree).size(); BigInteger[] values=new BigInteger[size]; for(int i=0;i<size;i++) values[i]=coordinates.get(i);
        return new RelativeSimplicialChain(cone.target(),degree,new IntegerVector(values));
    }
    public RelativeSimplicialChain sourcePart() {
        int offset=cone.target().basis(degree).size(); BigInteger[] values=new BigInteger[coordinates.dimension()-offset]; for(int i=0;i<values.length;i++) values[i]=coordinates.get(offset+i);
        return new RelativeSimplicialChain(cone.source(),degree.subtract(BigInteger.ONE),new IntegerVector(values));
    }
    private static IntegralConeChain insert(IntegralChainMappingCone cone,RelativeSimplicialChain chain,boolean shifted) {
        if(!chain.pair().equals(shifted?cone.source():cone.target())) throw MathFailure.undefined("Cone insertion requires the exact full source or target pair");
        BigInteger degree=shifted?chain.degree().add(BigInteger.ONE):chain.degree(); int size=cone.rank(degree),offset=shifted?cone.target().basis(degree).size():0;
        BigInteger[] values=new BigInteger[size]; Arrays.fill(values,BigInteger.ZERO); for(int i=0;i<chain.coordinates().dimension();i++) values[offset+i]=chain.coordinates().get(i);
        return new IntegralConeChain(cone,degree,new IntegerVector(values));
    }
    public static IntegralConeChain includeTarget(IntegralChainMappingCone cone,RelativeSimplicialChain chain) { return insert(cone,chain,false); }
    /** Coordinate section of shifted projection; its boundary includes F applied to the source component. */
    public static IntegralConeChain liftSource(IntegralChainMappingCone cone,RelativeSimplicialChain chain) { return insert(cone,chain,true); }
    @Override public boolean equals(Object other) {
        if(!(other instanceof IntegralConeChain)) return false; IntegralConeChain c=(IntegralConeChain)other;
        return cone.equals(c.cone) && degree.equals(c.degree) && coordinates.equals(c.coordinates);
    }
    @Override public int hashCode() { return Objects.hash(cone,degree,coordinates); }
    @Override public String toString() { return "ConeChain(cone="+cone+", degree="+degree+", coordinates="+coordinates+")"; }
}
