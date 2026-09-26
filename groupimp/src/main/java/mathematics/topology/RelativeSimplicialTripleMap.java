package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A simplicial map (X,A,B)->(Y,C,D), preserving both nested subcomplexes. */
public final class RelativeSimplicialTripleMap implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialTriple source,target;
    private final RelativeSimplicialMap outer,total,inner;
    public RelativeSimplicialTripleMap(RelativeSimplicialTriple source,RelativeSimplicialTriple target,FiniteSimplicialMap ambient) {
        this(source,target,ambient,new Computation());
    }
    private RelativeSimplicialTripleMap(RelativeSimplicialTriple source,RelativeSimplicialTriple target,FiniteSimplicialMap ambient,Computation work) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
        outer=new RelativeSimplicialMap(source.outerPair(),target.outerPair(),ambient,work);
        total=new RelativeSimplicialMap(source.totalPair(),target.totalPair(),ambient,work);
        inner=new RelativeSimplicialMap(source.innerPair(),target.innerPair(),outer.subcomplexMap(),work);
    }
    public RelativeSimplicialTriple source() { return source; }
    public RelativeSimplicialTriple target() { return target; }
    public FiniteSimplicialMap ambientMap() { return outer.ambientMap(); }
    public RelativeSimplicialMap outerMap() { return outer; }
    public RelativeSimplicialMap totalMap() { return total; }
    public RelativeSimplicialMap innerMap() { return inner; }
    public static RelativeSimplicialTripleMap identity(RelativeSimplicialTriple triple) { return inclusion(triple,triple); }
    private static void requireSubtriple(RelativeSimplicialTriple smaller,RelativeSimplicialTriple larger) {
        if(!smaller.outerPair().ambient().subcomplexOf(larger.outerPair().ambient()) ||
                !smaller.outerPair().subcomplex().subcomplexOf(larger.outerPair().subcomplex()) ||
                !smaller.innerPair().subcomplex().subcomplexOf(larger.innerPair().subcomplex()))
            throw MathFailure.undefined("A subtriple must include all three full labelled components");
    }
    public static RelativeSimplicialTripleMap inclusion(RelativeSimplicialTriple source,RelativeSimplicialTriple target) {
        requireSubtriple(source,target); return new RelativeSimplicialTripleMap(source,target,FiniteSimplicialMap.inclusion(source.outerPair().ambient(),target.outerPair().ambient()));
    }
    /** Apply the right operand first; X, A and B must all match at the middle triple. */
    public RelativeSimplicialTripleMap compose(RelativeSimplicialTripleMap before) {
        if(!source.equals(before.target)) throw MathFailure.undefined("Triple-map composition requires the same full middle triple");
        Computation work=new Computation(); return new RelativeSimplicialTripleMap(before.source,target,ambientMap().compose(before.ambientMap(),work),work);
    }
    public boolean isIsomorphism() { return ambientMap().isIsomorphism() && outer.subcomplexMap().isSurjective() && total.subcomplexMap().isSurjective(); }
    public RelativeSimplicialTripleMap inverse() {
        Computation work=new Computation(); return new RelativeSimplicialTripleMap(target,source,ambientMap().inverse(work),work);
    }
    /** Contiguity must hold in the target X, A and B separately. */
    public boolean contiguous(RelativeSimplicialTripleMap other) {
        if(!source.equals(other.source) || !target.equals(other.target)) throw MathFailure.undefined("Triple contiguity requires equal full source and target triples");
        Computation work=new Computation(); return ambientMap().contiguous(other.ambientMap(),work) &&
                outer.subcomplexMap().contiguous(other.outer.subcomplexMap(),work) && total.subcomplexMap().contiguous(other.total.subcomplexMap(),work);
    }
    private RelativeSimplicialTriple image(Computation work) {
        return new RelativeSimplicialTriple(new RelativeSimplicialComplex(ambientMap().image(work),outer.subcomplexMap().image(work)),total.subcomplexMap().image(work));
    }
    public RelativeSimplicialTriple image() { return image(new Computation()); }
    public RelativeSimplicialTripleMap corestrictImage() {
        Computation work=new Computation(); RelativeSimplicialTriple image=image(work);
        return new RelativeSimplicialTripleMap(source,image,new FiniteSimplicialMap(source.outerPair().ambient(),image.outerPair().ambient(),ambientMap().vertexMap(),work),work);
    }
    public RelativeSimplicialTripleMap restrict(RelativeSimplicialTriple triple) {
        requireSubtriple(triple,source); Computation work=new Computation(); Map<BigInteger,BigInteger> vertices=new TreeMap<>();
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(triple.outerPair().ambient()).members()) { work.use(1); vertices.put(vertex,ambientMap().mapVertex(vertex)); }
        return new RelativeSimplicialTripleMap(triple,target,new FiniteSimplicialMap(triple.outerPair().ambient(),target.outerPair().ambient(),vertices,work),work);
    }
    public AbelianGroupHomomorphism outerHomologyMap(BigInteger degree) { return outer.homologyMap(degree); }
    public AbelianGroupHomomorphism totalHomologyMap(BigInteger degree) { return total.homologyMap(degree); }
    public AbelianGroupHomomorphism innerHomologyMap(BigInteger degree) { return inner.homologyMap(degree); }
    public AbelianGroupHomomorphism outerCohomologyMap(BigInteger degree) { return RelativeSimplicialCochain.cohomologyMap(outer,degree); }
    public AbelianGroupHomomorphism totalCohomologyMap(BigInteger degree) { return RelativeSimplicialCochain.cohomologyMap(total,degree); }
    public AbelianGroupHomomorphism innerCohomologyMap(BigInteger degree) { return RelativeSimplicialCochain.cohomologyMap(inner,degree); }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Triple-map degree must be nonnegative"); }
    /** Four source-to-target maps on H_k(A,B), H_k(X,B), H_k(X,A), H_(k-1)(A,B). */
    public List<AbelianGroupHomomorphism> longExactMaps(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        result.add(inner.homologyMap(degree,work)); result.add(total.homologyMap(degree,work)); result.add(outer.homologyMap(degree,work));
        if(degree.signum()>0) result.add(inner.homologyMap(degree.subtract(BigInteger.ONE),work));
        else {
            IntegralHomology from=RelativeSimplicialTriple.homology(source.innerPair(),BigInteger.ONE.negate(),work),
                    to=source.innerPair().equals(target.innerPair())?from:RelativeSimplicialTriple.homology(target.innerPair(),BigInteger.ONE.negate(),work);
            result.add(from.inducedMap(to,IntegerMatrix.zero(0,0),work));
        }
        return Collections.unmodifiableList(result);
    }
    /** Four target-to-source maps on H^k(X,A), H^k(X,B), H^k(A,B), H^(k+1)(X,A). */
    public List<AbelianGroupHomomorphism> longExactCohomologyMaps(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        return Collections.unmodifiableList(Arrays.asList(RelativeSimplicialCochain.cohomologyMap(outer,degree,work),
                RelativeSimplicialCochain.cohomologyMap(total,degree,work),RelativeSimplicialCochain.cohomologyMap(inner,degree,work),
                RelativeSimplicialCochain.cohomologyMap(outer,degree.add(BigInteger.ONE),work)));
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RelativeSimplicialTripleMap)) return false; RelativeSimplicialTripleMap f=(RelativeSimplicialTripleMap)other;
        return source.equals(f.source) && target.equals(f.target) && ambientMap().equals(f.ambientMap());
    }
    @Override public int hashCode() { return Objects.hash(source,target,ambientMap()); }
    @Override public String toString() { return "TripleMap(source="+source+", target="+target+", vertices="+ambientMap().vertexMap()+")"; }
}
