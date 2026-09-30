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

/** Homogeneous integral chains; negative degrees contain only zero (unreduced convention). */
public final class SimplicialChain implements Serializable {
    private static final long serialVersionUID=1L;
    private final FiniteSimplicialComplex complex;
    private final BigInteger degree;
    private final IntegerVector coordinates;
    public SimplicialChain(FiniteSimplicialComplex complex,BigInteger degree,IntegerVector coordinates) {
        this.complex=Objects.requireNonNull(complex); this.degree=Objects.requireNonNull(degree); this.coordinates=Objects.requireNonNull(coordinates);
        if(basis(complex,degree).size()!=coordinates.dimension()) throw MathFailure.undefined("Chain coordinates must match the full complex and degree");
    }
    static List<FiniteSet<Integer>> basis(FiniteSimplicialComplex complex,BigInteger degree) {
        if(complex.faces().size()>FiniteSimplicialMap.MAX_SIMPLICES)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Simplicial chains allow at most 4096 nonempty simplices");
        return degree.signum()<0 || degree.compareTo(BigInteger.valueOf(complex.dimension()))>0?Collections.emptyList():IntegralSimplicialHomology.orderedBasis(complex,null,degree.intValueExact());
    }
    private static void cochainDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("The cochain degree must be nonnegative"); }
    private static IntegerMatrix boundary(FiniteSimplicialComplex complex,BigInteger degree,Computation work) {
        return RelativeSimplicialComplex.boundary(basis(complex,degree.subtract(BigInteger.ONE)),basis(complex,degree),work);
    }
    public FiniteSimplicialComplex complex() { return complex; }
    public BigInteger degree() { return degree; }
    public IntegerVector coordinates() { return coordinates; }
    public SimplicialChain withCoordinates(IntegerVector values) { return new SimplicialChain(complex,degree,values); }
    public static SimplicialChain zero(FiniteSimplicialComplex complex,BigInteger degree) { return new SimplicialChain(complex,degree,IntegerVector.zero(basis(complex,degree).size())); }
    public static List<SimplicialChain> basisChains(FiniteSimplicialComplex complex,BigInteger degree) {
        int size=basis(complex,degree).size(); List<SimplicialChain> result=new ArrayList<>();
        for(int i=0;i<size;i++) { BigInteger[] values=new BigInteger[size]; Arrays.fill(values,BigInteger.ZERO); values[i]=BigInteger.ONE; result.add(new SimplicialChain(complex,degree,new IntegerVector(values))); }
        return Collections.unmodifiableList(result);
    }
    private void same(SimplicialChain other) {
        if(!complex.equals(other.complex) || !degree.equals(other.degree)) throw MathFailure.undefined("Additive chain operations require the same full complex and degree");
    }
    private void sameComplex(SimplicialCochain cochain) {
        if(!complex.equals(cochain.complex())) throw MathFailure.undefined("Chain and cochain must retain the same full labelled complex");
    }
    public SimplicialChain add(SimplicialChain other) { same(other); return withCoordinates(coordinates.add(other.coordinates)); }
    public SimplicialChain subtract(SimplicialChain other) { return add(other.negate()); }
    public SimplicialChain scale(BigInteger scalar) { return withCoordinates(coordinates.scale(scalar)); }
    public SimplicialChain negate() { return scale(BigInteger.ONE.negate()); }
    public boolean isZero() { return coordinates.equals(IntegerVector.zero(coordinates.dimension())); }
    public SimplicialChain boundary() {
        Computation work=new Computation(); return new SimplicialChain(complex,degree.subtract(BigInteger.ONE),work.apply(boundary(complex,degree,work),coordinates));
    }
    private boolean isCycle(Computation work) {
        IntegerVector value=work.apply(boundary(complex,degree,work),coordinates); return value.equals(IntegerVector.zero(value.dimension()));
    }
    public boolean isCycle() { return isCycle(new Computation()); }
    public boolean isBoundary() { Computation work=new Computation(); return work.hasSolution(boundary(complex,degree.add(BigInteger.ONE),work),coordinates); }
    public IntegerVector boundingCoordinates() { Computation work=new Computation(); return work.solve(boundary(complex,degree.add(BigInteger.ONE),work),coordinates); }
    public boolean homologous(SimplicialChain other) {
        same(other); Computation work=new Computation();
        if(!isCycle(work) || !other.isCycle(work)) throw MathFailure.undefined("Homology comparison requires two cycles");
        return work.hasSolution(boundary(complex,degree.add(BigInteger.ONE),work),coordinates.add(other.coordinates.scale(BigInteger.ONE.negate())));
    }
    static IntegralHomology homology(FiniteSimplicialComplex complex,BigInteger degree,Computation work) {
        return new IntegralHomology(boundary(complex,degree,work),boundary(complex,degree.add(BigInteger.ONE),work),work);
    }
    public IntegralHomology homology() { return homology(complex,degree,new Computation()); }
    public AbelianGroupElement classOf() { Computation work=new Computation(); return homology(complex,degree,work).classOf(coordinates,work); }
    public SimplicialChain representative(AbelianGroupElement element) {
        Computation work=new Computation(); return withCoordinates(homology(complex,degree,work).representative(element,work));
    }
    public List<SimplicialChain> cycleGenerators() {
        Computation work=new Computation(); List<SimplicialChain> result=new ArrayList<>();
        for(IntegerVector generator : homology(complex,degree,work).generators(work)) result.add(withCoordinates(generator)); return Collections.unmodifiableList(result);
    }
    public SimplicialChain pushforward(FiniteSimplicialMap map) {
        if(!complex.equals(map.source())) throw MathFailure.undefined("Pushforward requires the chain's full complex to equal the map source");
        if(degree.signum()<0) return zero(map.target(),degree);
        Computation work=new Computation(); return new SimplicialChain(map.target(),degree,work.apply(map.chainMatrix(degree,work),coordinates));
    }
    public BigInteger evaluate(SimplicialCochain cochain) {
        sameComplex(cochain); if(!degree.equals(cochain.degree())) throw MathFailure.undefined("Chain-cochain pairing requires equal degrees");
        return coordinates.dot(cochain.coordinates());
    }
    public BigInteger augmentation() {
        if(degree.signum()!=0) throw MathFailure.undefined("Augmentation is defined on zero-chains only");
        BigInteger sum=BigInteger.ZERO; for(BigInteger value : coordinates.entries()) sum=sum.add(value); return sum;
    }
    private static Map<FiniteSet<Integer>,Integer> positions(List<FiniteSet<Integer>> basis) {
        Map<FiniteSet<Integer>,Integer> result=new HashMap<>(); for(int i=0;i<basis.size();i++) result.put(basis.get(i),i); return result;
    }
    /** Fix either cochain or chain coefficients in c cap phi = phi(front) times back. */
    private static IntegerMatrix capMatrix(FiniteSimplicialComplex complex,BigInteger chainDegree,BigInteger cochainDegree,
                                            IntegerVector coefficients,boolean fixedCochain,Computation work) {
        cochainDegree(cochainDegree);
        List<FiniteSet<Integer>> chains=basis(complex,chainDegree),cochains=basis(complex,cochainDegree),target=basis(complex,chainDegree.subtract(cochainDegree));
        return capMatrix(chains,cochains,target,cochainDegree,coefficients,fixedCochain,work);
    }
    /** Missing front faces have zero cochain value; missing back faces vanish in the target quotient. */
    static IntegerMatrix capMatrix(List<FiniteSet<Integer>> chains,List<FiniteSet<Integer>> cochains,List<FiniteSet<Integer>> target,
                                   BigInteger cochainDegree,IntegerVector coefficients,boolean fixedCochain,Computation work) {
        cochainDegree(cochainDegree);
        int columns=fixedCochain?chains.size():cochains.size(); work.use((long)target.size()*columns);
        BigInteger[][] entries=new BigInteger[target.size()][columns]; for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
        if(!chains.isEmpty() && !target.isEmpty()) {
            int split=cochainDegree.intValueExact(); Map<FiniteSet<Integer>,Integer> front=positions(cochains),back=positions(target);
            for(int c=0;c<chains.size();c++) {
                List<Integer> vertices=IntegralSimplicialHomology.vertices(chains.get(c)); work.use(vertices.size());
                Integer head=front.get(new FiniteSet<>(vertices.subList(0,split+1))),tail=back.get(new FiniteSet<>(vertices.subList(split,vertices.size())));
                if(head==null || tail==null) continue;
                int column=fixedCochain?c:head; BigInteger value=coefficients.get(fixedCochain?head:c);
                entries[tail][column]=entries[tail][column].add(value);
            }
        }
        return new IntegerMatrix(target.size(),columns,entries);
    }
    public static IntegerMatrix capMatrix(SimplicialCochain cochain,BigInteger chainDegree) {
        return capMatrix(cochain.complex(),chainDegree,cochain.degree(),cochain.coordinates(),true,new Computation());
    }
    public IntegerMatrix capCohomologyMatrix(BigInteger cochainDegree) { return capMatrix(complex,degree,cochainDegree,coordinates,false,new Computation()); }
    public SimplicialChain cap(SimplicialCochain cochain) { return cap(cochain,new Computation()); }
    private SimplicialChain cap(SimplicialCochain cochain,Computation work) {
        sameComplex(cochain); return new SimplicialChain(complex,degree.subtract(cochain.degree()),work.apply(capMatrix(complex,degree,cochain.degree(),cochain.coordinates(),true,work),coordinates));
    }
    public AbelianGroupElement capClass(SimplicialCochain cochain) {
        sameComplex(cochain); Computation work=new Computation();
        if(!isCycle(work) || !cochain.isCocycle(work)) throw MathFailure.undefined("Cap classes require a cycle and a cocycle");
        SimplicialChain result=cap(cochain,work); return homology(complex,result.degree,work).classOf(result.coordinates,work);
    }
    /** A fixed p-cocycle induces H_n -> H_(n-p). */
    public static AbelianGroupHomomorphism capHomologyMap(SimplicialCochain cochain,BigInteger chainDegree) {
        Computation work=new Computation();
        if(!cochain.isCocycle(work)) throw MathFailure.undefined("A cap homology map requires a cocycle");
        return homology(cochain.complex(),chainDegree,work).inducedMap(homology(cochain.complex(),chainDegree.subtract(cochain.degree()),work),
                capMatrix(cochain.complex(),chainDegree,cochain.degree(),cochain.coordinates(),true,work),work);
    }
    /** A fixed n-cycle induces H^p -> H_(n-p); no manifold or fundamental-class assumption is made. */
    public AbelianGroupHomomorphism capCohomologyMap(BigInteger cochainDegree) {
        cochainDegree(cochainDegree); Computation work=new Computation();
        if(!isCycle(work)) throw MathFailure.undefined("A cap cohomology map requires a cycle");
        return SimplicialCochain.cohomology(complex,cochainDegree,work).inducedMap(homology(complex,degree.subtract(cochainDegree),work),
                capMatrix(complex,degree,cochainDegree,coordinates,false,work),work);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof SimplicialChain)) return false; SimplicialChain c=(SimplicialChain)other;
        return complex.equals(c.complex) && degree.equals(c.degree) && coordinates.equals(c.coordinates);
    }
    @Override public int hashCode() { return Objects.hash(complex,degree,coordinates); }
    @Override public String toString() { return "SimplicialChain(complex="+complex+", degree="+degree+", coordinates="+coordinates+")"; }
}
