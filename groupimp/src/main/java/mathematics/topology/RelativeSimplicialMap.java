package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A simplicial map (X,A)->(Y,B), retaining both labelled pairs and the restriction A->B. */
public final class RelativeSimplicialMap implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialComplex source,target;
    private final FiniteSimplicialMap ambient,subcomplex;
    public RelativeSimplicialMap(RelativeSimplicialComplex source,RelativeSimplicialComplex target,FiniteSimplicialMap ambient) {
        this(source,target,ambient,new Computation());
    }
    RelativeSimplicialMap(RelativeSimplicialComplex source,RelativeSimplicialComplex target,FiniteSimplicialMap ambient,Computation work) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target); this.ambient=Objects.requireNonNull(ambient);
        if(!source.ambient().equals(ambient.source()) || !target.ambient().equals(ambient.target()))
            throw MathFailure.undefined("A map of pairs must retain exactly the ambient map's source and target complexes");
        Map<BigInteger,BigInteger> restricted=new TreeMap<>();
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source.subcomplex()).members()) { work.use(1); restricted.put(vertex,ambient.mapVertex(vertex)); }
        subcomplex=new FiniteSimplicialMap(source.subcomplex(),target.subcomplex(),restricted,work);
    }
    public RelativeSimplicialComplex source() { return source; }
    public RelativeSimplicialComplex target() { return target; }
    public FiniteSimplicialMap ambientMap() { return ambient; }
    public FiniteSimplicialMap subcomplexMap() { return subcomplex; }
    public static RelativeSimplicialMap identity(RelativeSimplicialComplex pair) { return inclusion(pair,pair); }
    public static RelativeSimplicialMap inclusion(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        if(!source.subcomplex().subcomplexOf(target.subcomplex())) throw MathFailure.undefined("Pair inclusion requires inclusion of both subcomplexes");
        return new RelativeSimplicialMap(source,target,FiniteSimplicialMap.inclusion(source.ambient(),target.ambient()));
    }
    public static RelativeSimplicialMap absolute(FiniteSimplicialMap map) {
        return new RelativeSimplicialMap(RelativeSimplicialComplex.absolute(map.source()),RelativeSimplicialComplex.absolute(map.target()),map);
    }
    public static RelativeSimplicialMap diagonal(FiniteSimplicialMap map) {
        return new RelativeSimplicialMap(RelativeSimplicialComplex.diagonal(map.source()),RelativeSimplicialComplex.diagonal(map.target()),map);
    }
    /** Apply the right operand first; the whole labelled middle pairs must be equal. */
    public RelativeSimplicialMap compose(RelativeSimplicialMap before) {
        if(!source.equals(before.target)) throw MathFailure.undefined("Composition of pair maps requires equal middle pairs");
        Computation work=new Computation(); return new RelativeSimplicialMap(before.source,target,ambient.compose(before.ambient,work),work);
    }
    public boolean isIsomorphism() { return ambient.isIsomorphism() && subcomplex.isSurjective(); }
    public RelativeSimplicialMap inverse() {
        Computation work=new Computation(); return new RelativeSimplicialMap(target,source,ambient.inverse(work),work);
    }
    /** Pair contiguity also requires each union of images of an A simplex to lie in B. */
    public boolean contiguous(RelativeSimplicialMap other) {
        if(!source.equals(other.source) || !target.equals(other.target)) throw MathFailure.undefined("Pair contiguity requires equal source and target pairs");
        Computation work=new Computation(); return ambient.contiguous(other.ambient,work) && subcomplex.contiguous(other.subcomplex,work);
    }
    private RelativeSimplicialComplex image(Computation work) { return new RelativeSimplicialComplex(ambient.image(work),subcomplex.image(work)); }
    public RelativeSimplicialComplex image() { return image(new Computation()); }
    public RelativeSimplicialMap corestrictImage() {
        Computation work=new Computation(); RelativeSimplicialComplex image=image(work);
        return new RelativeSimplicialMap(source,image,new FiniteSimplicialMap(source.ambient(),image.ambient(),ambient.vertexMap(),work),work);
    }
    public RelativeSimplicialMap restrict(RelativeSimplicialComplex pair) {
        if(!pair.ambient().subcomplexOf(source.ambient()) || !pair.subcomplex().subcomplexOf(source.subcomplex()))
            throw MathFailure.undefined("Restriction requires inclusion of both components of the source pair");
        Computation work=new Computation(); Map<BigInteger,BigInteger> vertices=new TreeMap<>();
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(pair.ambient()).members()) { work.use(1); vertices.put(vertex,ambient.mapVertex(vertex)); }
        return new RelativeSimplicialMap(pair,target,new FiniteSimplicialMap(pair.ambient(),target.ambient(),vertices,work),work);
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Relative map degree must be nonnegative"); }
    public IntegerMatrix chainMatrix(BigInteger degree) { return chainMatrix(degree,new Computation()); }
    IntegerMatrix chainMatrix(BigInteger degree,Computation work) {
        requireDegree(degree); return ambient.chainMatrix(target.basis(degree),source.basis(degree),work);
    }
    private int topDegree() { return Math.max(source.ambient().dimension(),target.ambient().dimension()); }
    public List<IntegerMatrix> chainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=topDegree();k++) result.add(chainMatrix(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public IntegralHomology sourceHomology(BigInteger degree) { return source.homology(degree); }
    public IntegralHomology targetHomology(BigInteger degree) { return target.homology(degree); }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return homologyMap(degree,new Computation()); }
    AbelianGroupHomomorphism homologyMap(BigInteger degree,Computation work) {
        requireDegree(degree); IntegralHomology first=source.homology(degree,work),second=source.equals(target)?first:target.homology(degree,work);
        return first.inducedMap(second,chainMatrix(degree,work),work);
    }
    public List<AbelianGroupHomomorphism> homologyMaps() {
        Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        for(int k=0;k<=topDegree();k++) result.add(homologyMap(BigInteger.valueOf(k),work)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupHomomorphism ambientHomologyMap(BigInteger degree) { return ambient.homologyMap(degree); }
    public AbelianGroupHomomorphism subcomplexHomologyMap(BigInteger degree) { return subcomplex.homologyMap(degree); }
    /** Vertical maps for H_k(A), H_k(X), H_k(X,A), H_(k-1)(A), in that order. */
    public List<AbelianGroupHomomorphism> longExactMaps(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        result.add(subcomplex.homologyMap(degree,work)); result.add(ambient.homologyMap(degree,work)); result.add(homologyMap(degree,work));
        if(degree.signum()>0) result.add(subcomplex.homologyMap(degree.subtract(BigInteger.ONE),work));
        else {
            IntegralHomology zero=new IntegralHomology(IntegerMatrix.zero(0,0),IntegerMatrix.zero(0,0),work);
            result.add(zero.inducedMap(zero,IntegerMatrix.zero(0,0),work));
        }
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RelativeSimplicialMap)) return false; RelativeSimplicialMap map=(RelativeSimplicialMap)other;
        return source.equals(map.source) && target.equals(map.target) && ambient.equals(map.ambient);
    }
    @Override public int hashCode() { return Objects.hash(source,target,ambient); }
    @Override public String toString() { return "RelativeMap(source="+source+", target="+target+", vertices="+ambient.vertexMap()+")"; }
}
