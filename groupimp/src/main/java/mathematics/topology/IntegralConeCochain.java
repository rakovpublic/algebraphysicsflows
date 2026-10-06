package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupElement;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Integral cochains with the exact defining cone and homogeneous degree retained. */
public final class IntegralConeCochain implements Serializable {
    private static final long serialVersionUID=1L;
    private final IntegralChainMappingCone cone;
    private final BigInteger degree;
    private final IntegerVector coordinates;
    public IntegralConeCochain(IntegralChainMappingCone cone,BigInteger degree,IntegerVector coordinates) {
        this.cone=Objects.requireNonNull(cone); this.degree=Objects.requireNonNull(degree); this.coordinates=Objects.requireNonNull(coordinates);
        if(degree.signum()<0) throw MathFailure.undefined("Cone cochain degree must be nonnegative");
        if(cone.rank(degree)!=coordinates.dimension()) throw MathFailure.undefined("Cone cochains coordinates must match the exact cone and degree");
    }
    public IntegralChainMappingCone cone() { return cone; }
    public BigInteger degree() { return degree; }
    public IntegerVector coordinates() { return coordinates; }
    public IntegralConeCochain withCoordinates(IntegerVector values) { return new IntegralConeCochain(cone,degree,values); }
    public static IntegralConeCochain zero(IntegralChainMappingCone cone,BigInteger degree) { return new IntegralConeCochain(cone,degree,IntegerVector.zero(cone.rank(degree))); }
    public static List<IntegralConeCochain> basis(IntegralChainMappingCone cone,BigInteger degree) {
        IntegralConeCochain zero=zero(cone,degree); int size=zero.coordinates.dimension(); List<IntegralConeCochain> result=new ArrayList<>();
        for(int i=0;i<size;i++) { BigInteger[] values=new BigInteger[size]; Arrays.fill(values,BigInteger.ZERO); values[i]=BigInteger.ONE; result.add(zero.withCoordinates(new IntegerVector(values))); }
        return Collections.unmodifiableList(result);
    }
    private void same(IntegralConeCochain other) {
        if(!cone.equals(other.cone) || !degree.equals(other.degree)) throw MathFailure.undefined("Additive cone cochains operations require the exact cone and degree");
    }
    public IntegralConeCochain add(IntegralConeCochain other) { same(other); return withCoordinates(coordinates.add(other.coordinates)); }
    public IntegralConeCochain subtract(IntegralConeCochain other) { same(other); return withCoordinates(coordinates.add(other.coordinates.scale(BigInteger.ONE.negate()))); }
    public IntegralConeCochain scale(BigInteger scalar) { return withCoordinates(coordinates.scale(scalar)); }
    public IntegralConeCochain negate() { return scale(BigInteger.ONE.negate()); }
    public boolean isZero() { return coordinates.equals(IntegerVector.zero(coordinates.dimension())); }
    private IntegerMatrix outgoing(Computation work) { return transpose(cone.boundary(degree.add(BigInteger.ONE),work),work); }
    private IntegerMatrix incoming(Computation work) { return transpose(cone.boundary(degree,work),work); }
    public IntegralConeCochain coboundary() {
        Computation work=new Computation(); return new IntegralConeCochain(cone,degree.add(BigInteger.ONE),work.apply(outgoing(work),coordinates));
    }
    private boolean closed(Computation work) { IntegerVector image=work.apply(outgoing(work),coordinates); return image.equals(IntegerVector.zero(image.dimension())); }
    public boolean isCocycle() { return closed(new Computation()); }
    public boolean isCoboundary() { Computation work=new Computation(); return work.hasSolution(incoming(work),coordinates); }
    public IntegerVector coboundingCoordinates() { Computation work=new Computation(); return work.solve(incoming(work),coordinates); }
    public IntegralConeCochain coboundingCochain() {
        if(degree.signum()==0) throw MathFailure.undefined("A typed cobounding cochain requires positive degree; use cobounding-coordinates for degree zero");
        Computation work=new Computation(); return new IntegralConeCochain(cone,degree.subtract(BigInteger.ONE),work.solve(incoming(work),coordinates));
    }
    public boolean cohomologous(IntegralConeCochain other) {
        same(other); Computation work=new Computation();
        if(!closed(work) || !other.closed(work)) throw MathFailure.undefined("Cone cohomology comparison requires two cocycles");
        return work.hasSolution(incoming(work),coordinates.add(other.coordinates.scale(BigInteger.ONE.negate())));
    }
    public IntegralHomology cohomology() { return cone.homology(degree,true,new Computation()); }
    public AbelianGroupElement classOf() { Computation work=new Computation(); return cone.homology(degree,true,work).classOf(coordinates,work); }
    public IntegralConeCochain representative(AbelianGroupElement element) {
        Computation work=new Computation(); return withCoordinates(cone.homology(degree,true,work).representative(element,work));
    }
    public List<IntegralConeCochain> cocycleGenerators() {
        Computation work=new Computation(); List<IntegralConeCochain> result=new ArrayList<>();
        for(IntegerVector vector : cone.homology(degree,true,work).generators(work)) result.add(withCoordinates(vector)); return Collections.unmodifiableList(result);
    }
    private static IntegerMatrix transpose(IntegerMatrix matrix,Computation work) { work.use((long)matrix.rows()*matrix.columns()); return matrix.transpose(); }
    public RelativeSimplicialCochain targetPart() {
        int size=cone.target().basis(degree).size(); BigInteger[] values=new BigInteger[size]; for(int i=0;i<size;i++) values[i]=coordinates.get(i);
        return new RelativeSimplicialCochain(cone.target(),degree,new IntegerVector(values));
    }
    public RelativeSimplicialCochain sourcePart() {
        if(degree.signum()==0) throw MathFailure.undefined("A typed shifted source cochain requires positive cone degree");
        int offset=cone.target().basis(degree).size(); BigInteger[] values=new BigInteger[coordinates.dimension()-offset]; for(int i=0;i<values.length;i++) values[i]=coordinates.get(offset+i);
        return new RelativeSimplicialCochain(cone.source(),degree.subtract(BigInteger.ONE),new IntegerVector(values));
    }
    private static IntegralConeCochain insert(IntegralChainMappingCone cone,RelativeSimplicialCochain cochain,boolean shifted) {
        if(!cochain.pair().equals(shifted?cone.source():cone.target())) throw MathFailure.undefined("Cone insertion requires the exact full source or target pair");
        BigInteger degree=shifted?cochain.degree().add(BigInteger.ONE):cochain.degree(); int size=cone.rank(degree),offset=shifted?cone.target().basis(degree).size():0;
        BigInteger[] values=new BigInteger[size]; Arrays.fill(values,BigInteger.ZERO); for(int i=0;i<cochain.coordinates().dimension();i++) values[offset+i]=cochain.coordinates().get(i);
        return new IntegralConeCochain(cone,degree,new IntegerVector(values));
    }
    /** Dual shifted projection; its coboundary is the negative of the shifted source coboundary. */
    public static IntegralConeCochain includeSource(IntegralChainMappingCone cone,RelativeSimplicialCochain cochain) { return insert(cone,cochain,true); }
    public static IntegralConeCochain liftTarget(IntegralChainMappingCone cone,RelativeSimplicialCochain cochain) { return insert(cone,cochain,false); }
    public BigInteger evaluate(IntegralConeChain chain) {
        if(!cone.equals(chain.cone()) || !degree.equals(chain.degree())) throw MathFailure.undefined("Cone pairing requires the exact defining map and equal degrees");
        return coordinates.dot(chain.coordinates());
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof IntegralConeCochain)) return false; IntegralConeCochain c=(IntegralConeCochain)other;
        return cone.equals(c.cone) && degree.equals(c.degree) && coordinates.equals(c.coordinates);
    }
    @Override public int hashCode() { return Objects.hash(cone,degree,coordinates); }
    @Override public String toString() { return "ConeCochain(cone="+cone+", degree="+degree+", coordinates="+coordinates+")"; }
}
