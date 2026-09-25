package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A simplicial map preserving the two ordered pieces of a finite cover. */
public final class SimplicialCoverMap implements Serializable {
    private static final long serialVersionUID=1L;
    private final SimplicialCover source,target;
    private final FiniteSimplicialMap union,left,right,intersection;
    public SimplicialCoverMap(SimplicialCover source,SimplicialCover target,FiniteSimplicialMap union) {
        this(source,target,union,new Computation());
    }
    private SimplicialCoverMap(SimplicialCover source,SimplicialCover target,FiniteSimplicialMap union,Computation work) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target); this.union=Objects.requireNonNull(union);
        if(!source.union().equals(union.source()) || !target.union().equals(union.target()))
            throw MathFailure.undefined("A cover map requires exactly the declared source and target unions");
        left=restriction(source.left(),target.left(),work); right=restriction(source.right(),target.right(),work);
        intersection=restriction(source.intersection(),target.intersection(),work);
    }
    private FiniteSimplicialMap restriction(FiniteSimplicialComplex from,FiniteSimplicialComplex to,Computation work) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>();
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(from).members()) { work.use(1); vertices.put(vertex,union.mapVertex(vertex)); }
        return new FiniteSimplicialMap(from,to,vertices,work);
    }
    public SimplicialCover source() { return source; }
    public SimplicialCover target() { return target; }
    public FiniteSimplicialMap unionMap() { return union; }
    public FiniteSimplicialMap leftMap() { return left; }
    public FiniteSimplicialMap rightMap() { return right; }
    public FiniteSimplicialMap intersectionMap() { return intersection; }
    public static SimplicialCoverMap identity(SimplicialCover cover) { return inclusion(cover,cover); }
    public static SimplicialCoverMap inclusion(SimplicialCover source,SimplicialCover target) {
        if(!source.left().subcomplexOf(target.left()) || !source.right().subcomplexOf(target.right()))
            throw MathFailure.undefined("Cover inclusion requires inclusion of each ordered piece");
        return new SimplicialCoverMap(source,target,FiniteSimplicialMap.inclusion(source.union(),target.union()));
    }
    /** Apply the right operand first; both ordered pieces of the middle cover must agree. */
    public SimplicialCoverMap compose(SimplicialCoverMap before) {
        if(!source.equals(before.target)) throw MathFailure.undefined("Cover composition requires equal ordered middle covers");
        Computation work=new Computation(); return new SimplicialCoverMap(before.source,target,union.compose(before.union,work),work);
    }
    public boolean isIsomorphism() { return union.isIsomorphism() && left.isSurjective() && right.isSurjective(); }
    public SimplicialCoverMap inverse() {
        Computation work=new Computation(); return new SimplicialCoverMap(target,source,union.inverse(work),work);
    }
    /** Swap both covers simultaneously, retaining the union vertex map. */
    public SimplicialCoverMap swap() { return new SimplicialCoverMap(source.swap(),target.swap(),union); }
    public boolean contiguous(SimplicialCoverMap other) {
        if(!source.equals(other.source) || !target.equals(other.target)) throw MathFailure.undefined("Cover contiguity requires equal ordered source and target covers");
        Computation work=new Computation(); return left.contiguous(other.left,work) && right.contiguous(other.right,work);
    }
    private SimplicialCover image(Computation work) { return new SimplicialCover(left.image(work),right.image(work)); }
    public SimplicialCover image() { return image(new Computation()); }
    public SimplicialCoverMap corestrictImage() {
        Computation work=new Computation(); SimplicialCover image=image(work);
        return new SimplicialCoverMap(source,image,new FiniteSimplicialMap(source.union(),image.union(),union.vertexMap(),work),work);
    }
    public SimplicialCoverMap restrict(SimplicialCover cover) {
        if(!cover.left().subcomplexOf(source.left()) || !cover.right().subcomplexOf(source.right()))
            throw MathFailure.undefined("Restriction requires inclusion of each ordered source piece");
        Computation work=new Computation(); return new SimplicialCoverMap(cover,target,restriction(cover.union(),target.union(),work),work);
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Cover map degree must be nonnegative"); }
    private int topDegree() { return Math.max(source.union().dimension(),target.union().dimension()); }
    public IntegerMatrix sumChainMatrix(BigInteger degree) { return sumChainMatrix(degree,new Computation()); }
    private IntegerMatrix sumChainMatrix(BigInteger degree,Computation work) {
        requireDegree(degree); return SimplicialCover.blockDiagonal(left.chainMatrix(degree,work),right.chainMatrix(degree,work),work);
    }
    public List<IntegerMatrix> sumChainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=topDegree();k++) result.add(sumChainMatrix(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public IntegralHomology sourceSumHomology(BigInteger degree) { return source.sumHomology(degree); }
    public IntegralHomology targetSumHomology(BigInteger degree) { return target.sumHomology(degree); }
    public AbelianGroupHomomorphism sumHomologyMap(BigInteger degree) { return sumHomologyMap(degree,new Computation()); }
    private AbelianGroupHomomorphism sumHomologyMap(BigInteger degree,Computation work) {
        requireDegree(degree); IntegralHomology first=source.sumHomology(degree,work),second=source.equals(target)?first:target.sumHomology(degree,work);
        return first.inducedMap(second,sumChainMatrix(degree,work),work);
    }
    public List<AbelianGroupHomomorphism> sumHomologyMaps() {
        Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        for(int k=0;k<=topDegree();k++) result.add(sumHomologyMap(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupHomomorphism unionHomologyMap(BigInteger degree) { return union.homologyMap(degree); }
    public AbelianGroupHomomorphism intersectionHomologyMap(BigInteger degree) { return intersection.homologyMap(degree); }
    public AbelianGroupHomomorphism leftHomologyMap(BigInteger degree) { return left.homologyMap(degree); }
    public AbelianGroupHomomorphism rightHomologyMap(BigInteger degree) { return right.homologyMap(degree); }
    /** Vertical maps on H_k(I), sum homology, H_k(U), H_(k-1)(I), in that order. */
    public List<AbelianGroupHomomorphism> longExactMaps(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        result.add(intersection.homologyMap(degree,work)); result.add(sumHomologyMap(degree,work)); result.add(union.homologyMap(degree,work));
        if(degree.signum()>0) result.add(intersection.homologyMap(degree.subtract(BigInteger.ONE),work));
        else {
            IntegralHomology zero=new IntegralHomology(IntegerMatrix.zero(0,0),IntegerMatrix.zero(0,0),work);
            result.add(zero.inducedMap(zero,IntegerMatrix.zero(0,0),work));
        }
        return Collections.unmodifiableList(result);
    }
    private RelativeSimplicialMap leftRelativeMap(Computation work) {
        return new RelativeSimplicialMap(new RelativeSimplicialComplex(source.left(),source.intersection()),
                new RelativeSimplicialComplex(target.left(),target.intersection()),left,work);
    }
    private RelativeSimplicialMap unionRelativeMap(Computation work) {
        return new RelativeSimplicialMap(new RelativeSimplicialComplex(source.union(),source.right()),
                new RelativeSimplicialComplex(target.union(),target.right()),union,work);
    }
    public RelativeSimplicialMap leftRelativeMap() { return leftRelativeMap(new Computation()); }
    public RelativeSimplicialMap unionRelativeMap() { return unionRelativeMap(new Computation()); }
    /** Vertical relative maps for (A,I)->(U,B), the excision square. */
    public List<RelativeSimplicialMap> excisionMaps() {
        Computation work=new Computation(); return Collections.unmodifiableList(Arrays.asList(leftRelativeMap(work),unionRelativeMap(work)));
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof SimplicialCoverMap)) return false; SimplicialCoverMap map=(SimplicialCoverMap)other;
        return source.equals(map.source) && target.equals(map.target) && union.equals(map.union);
    }
    @Override public int hashCode() { return Objects.hash(source,target,union); }
    @Override public String toString() { return "CoverMap(source="+source+", target="+target+", vertices="+union.vertexMap()+")"; }
}
