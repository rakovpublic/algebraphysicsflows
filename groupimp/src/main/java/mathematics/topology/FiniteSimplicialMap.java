package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.*;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A total vertex map preserving every simplex of its declared source complex. */
public final class FiniteSimplicialMap implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_SIMPLICES=4096;
    private final FiniteSimplicialComplex source,target;
    private final Map<BigInteger,BigInteger> vertices;
    public FiniteSimplicialMap(FiniteSimplicialComplex source,FiniteSimplicialComplex target,Map<BigInteger,BigInteger> vertices) {
        this(source,target,vertices,new Computation());
    }
    private FiniteSimplicialMap(FiniteSimplicialComplex source,FiniteSimplicialComplex target,Map<BigInteger,BigInteger> vertices,Computation work) {
        this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target); checkSize(source); checkSize(target);
        if(!Objects.requireNonNull(vertices).keySet().equals(vertexSet(source).members()))
            throw MathFailure.undefined("A simplicial vertex map must cover exactly the source vertices");
        FiniteSet<BigInteger> targets=vertexSet(target); Map<BigInteger,BigInteger> copy=new TreeMap<>();
        for(Map.Entry<BigInteger,BigInteger> entry : vertices.entrySet()) {
            work.use(1);
            if(!targets.contains(entry.getValue())) throw MathFailure.undefined("Vertex image outside the declared target complex");
            copy.put(entry.getKey(),entry.getValue());
        }
        this.vertices=Collections.unmodifiableMap(copy);
        for(FiniteSet<Integer> simplex : source.faces()) if(!target.faces().contains(imageOf(simplex,work)))
            throw MathFailure.undefined("A source simplex does not map to a target simplex");
    }
    private static void checkSize(FiniteSimplicialComplex complex) {
        if(complex.faces().size()>MAX_SIMPLICES) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Simplicial maps allow at most 4096 nonempty simplices per boundary complex");
    }
    public static FiniteSet<BigInteger> vertexSet(FiniteSimplicialComplex complex) {
        checkSize(complex); Set<BigInteger> values=new TreeSet<>();
        for(FiniteSet<Integer> face : complex.simplices(0)) values.add(BigInteger.valueOf(face.members().iterator().next()));
        return new FiniteSet<>(values);
    }
    public FiniteSimplicialComplex source() { return source; }
    public FiniteSimplicialComplex target() { return target; }
    public Map<BigInteger,BigInteger> vertexMap() { return vertices; }
    public BigInteger mapVertex(BigInteger vertex) {
        BigInteger result=vertices.get(vertex); if(result==null) throw MathFailure.undefined("Vertex outside the source complex"); return result;
    }
    private FiniteSet<Integer> imageOf(FiniteSet<Integer> simplex,Computation work) {
        work.use(simplex.size()); Set<Integer> values=new TreeSet<>();
        for(int vertex : simplex.members()) values.add(vertices.get(BigInteger.valueOf(vertex)).intValueExact()); return new FiniteSet<>(values);
    }
    public FiniteSet<BigInteger> mapSimplex(FiniteSet<BigInteger> simplex) {
        List<Integer> labels=new ArrayList<>();
        for(BigInteger vertex : simplex.members()) { mapVertex(vertex); labels.add(vertex.intValueExact()); }
        if(!source.faces().contains(new FiniteSet<>(labels))) throw MathFailure.undefined("The input must be a nonempty source simplex");
        Set<BigInteger> values=new TreeSet<>(); for(BigInteger label : simplex.members()) values.add(mapVertex(label)); return new FiniteSet<>(values);
    }
    public List<BigInteger> vertexImages() { return Collections.unmodifiableList(new ArrayList<>(vertices.values())); }
    public List<BigInteger> vertexFiber(BigInteger vertex) {
        if(!vertexSet(target).contains(vertex)) throw MathFailure.undefined("Fiber vertex outside the target complex");
        List<BigInteger> result=new ArrayList<>(); for(Map.Entry<BigInteger,BigInteger> entry : vertices.entrySet()) if(entry.getValue().equals(vertex)) result.add(entry.getKey());
        return Collections.unmodifiableList(result);
    }
    private FiniteSimplicialComplex image(Computation work) {
        Set<FiniteSet<Integer>> faces=new HashSet<>(); for(FiniteSet<Integer> face : source.faces()) faces.add(imageOf(face,work));
        return FiniteSimplicialComplex.fromClosedFaces(faces);
    }
    public FiniteSimplicialComplex image() { return image(new Computation()); }
    public boolean isInjective() { return new HashSet<>(vertices.values()).size()==vertices.size(); }
    public boolean isVertexSurjective() { return new HashSet<>(vertices.values()).equals(vertexSet(target).members()); }
    public boolean isSurjective() { return image().equals(target); }
    public boolean isIsomorphism() { return isInjective() && isSurjective(); }
    public FiniteSimplicialMap inverse() {
        if(!isInjective()) throw MathFailure.undefined("A simplicial inverse requires a vertex bijection and simplex preservation in both directions");
        Computation work=new Computation();
        if(!image(work).equals(target)) throw MathFailure.undefined("The image must equal the target complex");
        Map<BigInteger,BigInteger> inverse=new TreeMap<>(); for(Map.Entry<BigInteger,BigInteger> entry : vertices.entrySet()) inverse.put(entry.getValue(),entry.getKey());
        return new FiniteSimplicialMap(target,source,inverse,work);
    }
    /** Apply the right operand first, with equality of the full labelled middle complexes. */
    public FiniteSimplicialMap compose(FiniteSimplicialMap before) {
        if(!source.equals(before.target)) throw MathFailure.undefined("Simplicial composition requires equal middle complexes");
        Computation work=new Computation(); work.use(before.vertices.size()); Map<BigInteger,BigInteger> values=new TreeMap<>();
        for(Map.Entry<BigInteger,BigInteger> entry : before.vertices.entrySet()) values.put(entry.getKey(),mapVertex(entry.getValue()));
        return new FiniteSimplicialMap(before.source,target,values,work);
    }
    public static FiniteSimplicialMap identity(FiniteSimplicialComplex complex) { return inclusion(complex,complex); }
    public static FiniteSimplicialMap inclusion(FiniteSimplicialComplex source,FiniteSimplicialComplex target) {
        checkSize(source); checkSize(target);
        if(!source.subcomplexOf(target)) throw MathFailure.undefined("Inclusion requires a labelled subcomplex");
        Map<BigInteger,BigInteger> values=new TreeMap<>(); for(BigInteger vertex : vertexSet(source).members()) values.put(vertex,vertex);
        return new FiniteSimplicialMap(source,target,values);
    }
    public static FiniteSimplicialMap fromFunction(FiniteFunction<BigInteger,BigInteger> function,FiniteSimplicialComplex source,FiniteSimplicialComplex target) {
        if(!function.domain.equals(vertexSet(source)) || !function.codomain.equals(vertexSet(target)))
            throw MathFailure.undefined("Declared function boundaries must equal the complex vertex sets");
        return new FiniteSimplicialMap(source,target,function.mapping());
    }
    public FiniteSimplicialMap restrict(FiniteSimplicialComplex subcomplex) {
        checkSize(subcomplex);
        if(!subcomplex.subcomplexOf(source)) throw MathFailure.undefined("Restriction requires a source subcomplex");
        Map<BigInteger,BigInteger> values=new TreeMap<>(); for(BigInteger vertex : vertexSet(subcomplex).members()) values.put(vertex,mapVertex(vertex));
        return new FiniteSimplicialMap(subcomplex,target,values);
    }
    public FiniteSimplicialMap corestrictImage() {
        Computation work=new Computation(); return new FiniteSimplicialMap(source,image(work),vertices,work);
    }
    public FiniteSimplicialMap constantAt(BigInteger vertex) {
        if(!vertexSet(target).contains(vertex)) throw MathFailure.undefined("A constant map requires a target vertex");
        Map<BigInteger,BigInteger> values=new TreeMap<>(); for(BigInteger label : vertices.keySet()) values.put(label,vertex);
        return new FiniteSimplicialMap(source,target,values);
    }
    public static FiniteSimplicialMap emptyTo(FiniteSimplicialComplex target) {
        return new FiniteSimplicialMap(new FiniteSimplicialComplex(Collections.emptyList()),target,Collections.emptyMap());
    }
    public boolean contiguous(FiniteSimplicialMap other) {
        if(!source.equals(other.source) || !target.equals(other.target)) throw MathFailure.undefined("Contiguity requires the same source and target complexes");
        Computation work=new Computation();
        for(FiniteSet<Integer> simplex : source.faces()) if(!target.faces().contains(imageOf(simplex,work).union(other.imageOf(simplex,work)))) return false;
        return true;
    }
    private static List<FiniteSet<Integer>> basis(FiniteSimplicialComplex complex,BigInteger degree) {
        if(degree.signum()<0) throw MathFailure.undefined("Chain degree must be nonnegative");
        return degree.compareTo(BigInteger.valueOf(complex.dimension()))>0?Collections.emptyList()
                :new IntegralSimplicialHomology(complex).basis(degree.intValueExact());
    }
    public static List<FiniteSet<BigInteger>> simplexBasis(FiniteSimplicialComplex complex,BigInteger degree) {
        List<FiniteSet<BigInteger>> result=new ArrayList<>();
        for(FiniteSet<Integer> face : basis(complex,degree)) {
            List<BigInteger> labels=new ArrayList<>(); for(int vertex : IntegralSimplicialHomology.vertices(face)) labels.add(BigInteger.valueOf(vertex));
            result.add(new FiniteSet<>(labels));
        }
        return Collections.unmodifiableList(result);
    }
    public IntegerMatrix chainMatrix(BigInteger degree) { return chainMatrix(degree,new Computation()); }
    private IntegerMatrix chainMatrix(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> rows=basis(target,degree),columns=basis(source,degree);
        work.use((long)rows.size()*columns.size()); BigInteger[][] entries=new BigInteger[rows.size()][columns.size()];
        for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
        Map<FiniteSet<Integer>,Integer> positions=new HashMap<>(); for(int r=0;r<rows.size();r++) positions.put(rows.get(r),r);
        for(int c=0;c<columns.size();c++) {
            List<Integer> labels=IntegralSimplicialHomology.vertices(columns.get(c)),images=new ArrayList<>();
            work.use((long)labels.size()*labels.size()); for(int label : labels) images.add(mapVertex(BigInteger.valueOf(label)).intValueExact());
            Set<Integer> distinct=new HashSet<>(images); if(distinct.size()!=labels.size()) continue;
            int sign=1; for(int i=0;i<images.size();i++) for(int j=i+1;j<images.size();j++) if(images.get(i)>images.get(j)) sign=-sign;
            entries[positions.get(new FiniteSet<>(distinct))][c]=BigInteger.valueOf(sign);
        }
        return new IntegerMatrix(rows.size(),columns.size(),entries);
    }
    public List<IntegerMatrix> chainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=Math.max(source.dimension(),target.dimension());k++) result.add(chainMatrix(BigInteger.valueOf(k),work));
        return Collections.unmodifiableList(result);
    }
    public IntegralHomology sourceHomology(BigInteger degree) { return IntegralHomology.atDegree(source,degree); }
    public IntegralHomology targetHomology(BigInteger degree) { return IntegralHomology.atDegree(target,degree); }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return homologyMap(degree,new Computation()); }
    private AbelianGroupHomomorphism homologyMap(BigInteger degree,Computation work) {
        IntegralHomology first=IntegralHomology.atDegree(source,degree,work);
        IntegralHomology second=source.equals(target)?first:IntegralHomology.atDegree(target,degree,work);
        return first.inducedMap(second,chainMatrix(degree,work),work);
    }
    public List<AbelianGroupHomomorphism> homologyMaps() {
        Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        for(int k=0;k<=Math.max(source.dimension(),target.dimension());k++) result.add(homologyMap(BigInteger.valueOf(k),work));
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof FiniteSimplicialMap)) return false; FiniteSimplicialMap map=(FiniteSimplicialMap)other;
        return source.equals(map.source) && target.equals(map.target) && vertices.equals(map.vertices);
    }
    @Override public int hashCode() { return Objects.hash(source,target,vertices); }
    @Override public String toString() { return "SimplicialMap(source="+source+", target="+target+", vertices="+vertices+")"; }
}
