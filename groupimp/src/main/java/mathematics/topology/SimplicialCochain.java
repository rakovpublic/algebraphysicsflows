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

/** Homogeneous integral cochains in the lexicographic increasing-vertex simplex basis. */
public final class SimplicialCochain implements Serializable {
    private static final long serialVersionUID=1L;
    private final FiniteSimplicialComplex complex;
    private final BigInteger degree;
    private final IntegerVector coordinates;
    public SimplicialCochain(FiniteSimplicialComplex complex,BigInteger degree,IntegerVector coordinates) {
        this.complex=Objects.requireNonNull(complex); this.degree=Objects.requireNonNull(degree); this.coordinates=Objects.requireNonNull(coordinates);
        if(basis(complex,degree).size()!=coordinates.dimension()) throw MathFailure.undefined("Cochain coordinates must match the retained complex and degree");
    }
    private static void check(FiniteSimplicialComplex complex,BigInteger degree) {
        if(degree.signum()<0) throw MathFailure.undefined("Cochain degree must be nonnegative");
        if(complex.faces().size()>FiniteSimplicialMap.MAX_SIMPLICES)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Simplicial cochains allow at most 4096 nonempty simplices");
    }
    private static List<FiniteSet<Integer>> basis(FiniteSimplicialComplex complex,BigInteger degree) {
        check(complex,degree);
        return degree.compareTo(BigInteger.valueOf(complex.dimension()))>0?Collections.emptyList():IntegralSimplicialHomology.orderedBasis(complex,null,degree.intValueExact());
    }
    public FiniteSimplicialComplex complex() { return complex; }
    public BigInteger degree() { return degree; }
    public IntegerVector coordinates() { return coordinates; }
    public SimplicialCochain withCoordinates(IntegerVector values) { return new SimplicialCochain(complex,degree,values); }
    public static SimplicialCochain zero(FiniteSimplicialComplex complex,BigInteger degree) {
        return new SimplicialCochain(complex,degree,IntegerVector.zero(basis(complex,degree).size()));
    }
    public static SimplicialCochain unit(FiniteSimplicialComplex complex) {
        BigInteger[] values=new BigInteger[basis(complex,BigInteger.ZERO).size()]; Arrays.fill(values,BigInteger.ONE);
        return new SimplicialCochain(complex,BigInteger.ZERO,new IntegerVector(values));
    }
    public static List<SimplicialCochain> basisCochains(FiniteSimplicialComplex complex,BigInteger degree) {
        int size=basis(complex,degree).size(); List<SimplicialCochain> result=new ArrayList<>();
        for(int i=0;i<size;i++) { BigInteger[] values=new BigInteger[size]; Arrays.fill(values,BigInteger.ZERO); values[i]=BigInteger.ONE; result.add(new SimplicialCochain(complex,degree,new IntegerVector(values))); }
        return Collections.unmodifiableList(result);
    }
    private void sameComplex(SimplicialCochain other) {
        if(!complex.equals(other.complex)) throw MathFailure.undefined("Cochains must retain the same labelled complex");
    }
    private void sameDegree(SimplicialCochain other) {
        sameComplex(other); if(!degree.equals(other.degree)) throw MathFailure.undefined("Additive cochain operations require the same degree");
    }
    public SimplicialCochain add(SimplicialCochain other) { sameDegree(other); return withCoordinates(coordinates.add(other.coordinates)); }
    public SimplicialCochain subtract(SimplicialCochain other) { return add(other.negate()); }
    public SimplicialCochain scale(BigInteger scalar) { return withCoordinates(coordinates.scale(scalar)); }
    public SimplicialCochain negate() { return scale(BigInteger.ONE.negate()); }
    public boolean isZero() { return coordinates.equals(IntegerVector.zero(coordinates.dimension())); }
    public BigInteger evaluate(IntegerVector chain) { return coordinates.dot(chain); }
    private static IntegerMatrix dualBoundary(FiniteSimplicialComplex complex,BigInteger boundaryDegree,Computation work) {
        check(complex,boundaryDegree); IntegerMatrix boundary=complex.integralBoundaryMatrix(boundaryDegree);
        work.use((long)boundary.rows()*boundary.columns()); return boundary.transpose();
    }
    private IntegerVector differential(Computation work) { return work.apply(dualBoundary(complex,degree.add(BigInteger.ONE),work),coordinates); }
    public SimplicialCochain coboundary() { return new SimplicialCochain(complex,degree.add(BigInteger.ONE),differential(new Computation())); }
    private boolean isCocycle(Computation work) { IntegerVector value=differential(work); return value.equals(IntegerVector.zero(value.dimension())); }
    public boolean isCocycle() { return isCocycle(new Computation()); }
    public boolean isCoboundary() { Computation work=new Computation(); return work.hasSolution(dualBoundary(complex,degree,work),coordinates); }
    /** Coordinates of one primitive in C^(degree-1); at degree zero only the zero cochain has the empty primitive. */
    public IntegerVector coboundingCoordinates() { Computation work=new Computation(); return work.solve(dualBoundary(complex,degree,work),coordinates); }
    public boolean cohomologous(SimplicialCochain other) {
        sameDegree(other); Computation work=new Computation();
        if(!isCocycle(work) || !other.isCocycle(work)) throw MathFailure.undefined("Cohomology comparison requires two cocycles");
        return work.hasSolution(dualBoundary(complex,degree,work),coordinates.add(other.coordinates.scale(BigInteger.ONE.negate())));
    }
    /** Alexander-Whitney front/back product, with the shared vertex at the left degree. */
    public SimplicialCochain cup(SimplicialCochain other) { return cup(other,new Computation()); }
    private SimplicialCochain cup(SimplicialCochain other,Computation work) {
        sameComplex(other); BigInteger total=degree.add(other.degree); List<FiniteSet<Integer>> resultBasis=basis(complex,total);
        if(resultBasis.isEmpty()) return zero(complex,total);
        Map<FiniteSet<Integer>,BigInteger> first=values(),second=other.values(); int split=degree.intValueExact(); BigInteger[] result=new BigInteger[resultBasis.size()];
        for(int i=0;i<result.length;i++) {
            List<Integer> labels=IntegralSimplicialHomology.vertices(resultBasis.get(i)); work.use(labels.size());
            result[i]=first.get(new FiniteSet<>(labels.subList(0,split+1))).multiply(second.get(new FiniteSet<>(labels.subList(split,labels.size()))));
        }
        return new SimplicialCochain(complex,total,new IntegerVector(result));
    }
    private Map<FiniteSet<Integer>,BigInteger> values() {
        List<FiniteSet<Integer>> faces=basis(complex,degree); Map<FiniteSet<Integer>,BigInteger> result=new HashMap<>();
        for(int i=0;i<faces.size();i++) result.put(faces.get(i),coordinates.get(i)); return result;
    }
    public static IntegralHomology cohomology(FiniteSimplicialComplex complex,BigInteger degree) { return cohomology(complex,degree,new Computation()); }
    private static IntegralHomology cohomology(FiniteSimplicialComplex complex,BigInteger degree,Computation work) {
        check(complex,degree);
        return new IntegralHomology(dualBoundary(complex,degree.add(BigInteger.ONE),work),dualBoundary(complex,degree,work),work);
    }
    public IntegralHomology cohomology() { return cohomology(complex,degree); }
    public static List<IntegralHomology> cohomologyDegrees(FiniteSimplicialComplex complex) {
        check(complex,BigInteger.ZERO); Computation work=new Computation(); List<IntegralHomology> result=new ArrayList<>();
        for(int k=0;k<=complex.dimension();k++) result.add(cohomology(complex,BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupElement classOf() { Computation work=new Computation(); return cohomology(complex,degree,work).classOf(coordinates,work); }
    public SimplicialCochain representative(AbelianGroupElement element) {
        Computation work=new Computation(); return withCoordinates(cohomology(complex,degree,work).representative(element,work));
    }
    public List<SimplicialCochain> cocycleGenerators() {
        Computation work=new Computation(); List<SimplicialCochain> result=new ArrayList<>();
        for(IntegerVector generator : cohomology(complex,degree,work).generators(work)) result.add(withCoordinates(generator)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupElement cupClass(SimplicialCochain other) {
        sameComplex(other); Computation work=new Computation();
        if(!isCocycle(work) || !other.isCocycle(work)) throw MathFailure.undefined("Cup product of classes requires two cocycles");
        SimplicialCochain product=cup(other,work); return cohomology(complex,product.degree,work).classOf(product.coordinates,work);
    }
    public SimplicialCochain pullback(FiniteSimplicialMap map) {
        if(!complex.equals(map.target())) throw MathFailure.undefined("Pullback requires the cochain's full complex to equal the map target");
        Computation work=new Computation(); return new SimplicialCochain(map.source(),degree,work.apply(map.chainMatrix(degree,work).transpose(),coordinates));
    }
    /** Contravariant H^k(target)->H^k(source), retaining both cohomology presentations. */
    public static AbelianGroupHomomorphism cohomologyMap(FiniteSimplicialMap map,BigInteger degree) { return cohomologyMap(map,degree,new Computation()); }
    private static AbelianGroupHomomorphism cohomologyMap(FiniteSimplicialMap map,BigInteger degree,Computation work) {
        IntegralHomology from=cohomology(map.target(),degree,work),to=map.source().equals(map.target())?from:cohomology(map.source(),degree,work);
        return from.inducedMap(to,map.chainMatrix(degree,work).transpose(),work);
    }
    public static List<AbelianGroupHomomorphism> cohomologyMaps(FiniteSimplicialMap map) {
        Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        for(int k=0;k<=Math.max(map.source().dimension(),map.target().dimension());k++) result.add(cohomologyMap(map,BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof SimplicialCochain)) return false; SimplicialCochain c=(SimplicialCochain)other;
        return complex.equals(c.complex) && degree.equals(c.degree) && coordinates.equals(c.coordinates);
    }
    @Override public int hashCode() { return Objects.hash(complex,degree,coordinates); }
    @Override public String toString() { return "SimplicialCochain(complex="+complex+", degree="+degree+", coordinates="+coordinates+")"; }
}
