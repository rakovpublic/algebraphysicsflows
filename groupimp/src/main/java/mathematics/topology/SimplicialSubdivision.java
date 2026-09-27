package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Barycentric subdivision of a labelled pair, retaining a shared ambient face-to-vertex dictionary. */
public final class SimplicialSubdivision implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialComplex original,subdivided;
    private final List<FiniteSet<Integer>> faces;
    private final Map<FiniteSet<Integer>,Integer> labels;

    public SimplicialSubdivision(RelativeSimplicialComplex original) { this(original,new Computation()); }
    private SimplicialSubdivision(RelativeSimplicialComplex original,Computation work) {
        this.original=Objects.requireNonNull(original);
        List<FiniteSet<Integer>> ordered=new ArrayList<>(original.ambient().faces());
        ordered.sort((a,b) -> {
            work.use(1L+a.size()+b.size()); int size=Integer.compare(a.size(),b.size()); if(size!=0) return size;
            List<Integer> x=IntegralSimplicialHomology.vertices(a),y=IntegralSimplicialHomology.vertices(b);
            for(int i=0;i<x.size();i++) { int order=Integer.compare(x.get(i),y.get(i)); if(order!=0) return order; } return 0;
        });
        faces=Collections.unmodifiableList(ordered); Map<FiniteSet<Integer>,Integer> dictionary=new HashMap<>();
        for(int i=0;i<faces.size();i++) { work.use(1L+faces.get(i).size()); dictionary.put(faces.get(i),i); } labels=Collections.unmodifiableMap(dictionary);
        int[] firstLarger=new int[original.ambient().dimension()+2];
        int next=0; for(int size=0;size<firstLarger.length;size++) { while(next<faces.size() && faces.get(next).size()<=size) { work.use(1); next++; } firstLarger[size]=next; }
        Set<FiniteSet<Integer>> ambient=new LinkedHashSet<>(),subcomplex=new LinkedHashSet<>();
        for(int root=0;root<faces.size();root++) extend(new ArrayList<>(),root,firstLarger,ambient,subcomplex,work);
        subdivided=new RelativeSimplicialComplex(FiniteSimplicialComplex.fromClosedFaces(ambient),FiniteSimplicialComplex.fromClosedFaces(subcomplex));
    }
    private void extend(List<Integer> chain,int vertex,int[] firstLarger,Set<FiniteSet<Integer>> ambient,Set<FiniteSet<Integer>> subcomplex,Computation work) {
        work.use(1L+chain.size());
        if(ambient.size()==RelativeSimplicialComplex.MAX_SIMPLICES) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Barycentric subdivision exceeds 4096 nonempty simplices");
        chain.add(vertex); FiniteSet<Integer> simplex=new FiniteSet<>(chain); ambient.add(simplex);
        if(original.subcomplex().faces().contains(faces.get(vertex))) subcomplex.add(simplex);
        for(int next=firstLarger[faces.get(vertex).size()];next<faces.size();next++) {
            work.use(1L+faces.get(vertex).size());
            if(faces.get(vertex).subsetOf(faces.get(next))) extend(chain,next,firstLarger,ambient,subcomplex,work);
        }
        chain.remove(chain.size()-1);
    }
    public static SimplicialSubdivision absolute(FiniteSimplicialComplex complex) { return new SimplicialSubdivision(RelativeSimplicialComplex.absolute(complex)); }
    public RelativeSimplicialComplex original() { return original; }
    public RelativeSimplicialComplex subdivided() { return subdivided; }
    public BigInteger vertexCount() { return BigInteger.valueOf(faces.size()); }
    public FiniteSet<BigInteger> vertexFace(BigInteger vertex) {
        if(vertex.signum()<0 || vertex.compareTo(vertexCount())>=0) throw MathFailure.undefined("Subdivision vertex outside the retained face dictionary");
        List<BigInteger> values=new ArrayList<>(); for(int label : IntegralSimplicialHomology.vertices(faces.get(vertex.intValueExact()))) values.add(BigInteger.valueOf(label)); return new FiniteSet<>(values);
    }
    public List<FiniteSet<BigInteger>> vertexFaces() {
        List<FiniteSet<BigInteger>> result=new ArrayList<>(); for(int i=0;i<faces.size();i++) result.add(vertexFace(BigInteger.valueOf(i))); return Collections.unmodifiableList(result);
    }
    public BigInteger faceVertex(FiniteSet<BigInteger> face) {
        if(face.size()==0 || face.size()>original.ambient().dimension()+1) throw MathFailure.undefined("A subdivision vertex represents a nonempty original simplex");
        List<Integer> vertices=new ArrayList<>();
        for(BigInteger vertex : face.members()) {
            try { vertices.add(vertex.intValueExact()); } catch(ArithmeticException e) { throw MathFailure.undefined("Original simplex label outside the signed int range"); }
        }
        Integer result=labels.get(new FiniteSet<>(vertices)); if(result==null) throw MathFailure.undefined("The supplied set is not an original simplex"); return BigInteger.valueOf(result);
    }
    private RelativeSimplicialMap lastVertexMap(Computation work) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>();
        for(int i=0;i<faces.size();i++) { work.use(1L+faces.get(i).size()); vertices.put(BigInteger.valueOf(i),BigInteger.valueOf(Collections.max(faces.get(i).members()))); }
        return new RelativeSimplicialMap(subdivided,original,new FiniteSimplicialMap(subdivided.ambient(),original.ambient(),vertices,work),work);
    }
    public RelativeSimplicialMap lastVertexMap() { return lastVertexMap(new Computation()); }
    private void requireSource(RelativeSimplicialMap map) { if(!original.equals(map.source())) throw MathFailure.undefined("Subdivision map requires the full retained source pair"); }
    private RelativeSimplicialMap map(RelativeSimplicialMap map,SimplicialSubdivision target,Computation work) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>();
        for(int i=0;i<faces.size();i++) {
            work.use(1L+faces.get(i).size()); Set<Integer> image=new TreeSet<>();
            for(int vertex : faces.get(i).members()) image.add(map.ambientMap().mapVertex(BigInteger.valueOf(vertex)).intValueExact());
            vertices.put(BigInteger.valueOf(i),BigInteger.valueOf(target.labels.get(new FiniteSet<>(image))));
        }
        return new RelativeSimplicialMap(subdivided,target.subdivided,new FiniteSimplicialMap(subdivided.ambient(),target.subdivided.ambient(),vertices,work),work);
    }
    public RelativeSimplicialMap map(RelativeSimplicialMap map) {
        requireSource(map); Computation work=new Computation(); return map(map,new SimplicialSubdivision(map.target(),work),work);
    }
    /** From last_Y after sd(f) to f after last_X; these can differ for nonmonotone vertex maps. */
    public SimplicialHomotopy naturalityHomotopy(RelativeSimplicialMap map) {
        requireSource(map); Computation work=new Computation(); SimplicialSubdivision target=new SimplicialSubdivision(map.target(),work);
        return new SimplicialHomotopy(target.lastVertexMap(work).compose(map(map,target,work),work),map.compose(lastVertexMap(work),work),work);
    }
    private AbelianGroupHomomorphism homologyMap(BigInteger degree,Computation work) { return lastVertexMap(work).homologyMap(degree,work); }
    private AbelianGroupHomomorphism cohomologyMap(BigInteger degree,Computation work) { return RelativeSimplicialCochain.cohomologyMap(lastVertexMap(work),degree,work); }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return homologyMap(degree,new Computation()); }
    public AbelianGroupHomomorphism inverseHomologyMap(BigInteger degree) { Computation work=new Computation(); return homologyMap(degree,work).inverse(work); }
    public List<AbelianGroupHomomorphism> homologyMaps(BigInteger degree) {
        Computation work=new Computation(); AbelianGroupHomomorphism forward=homologyMap(degree,work); return Collections.unmodifiableList(Arrays.asList(forward,forward.inverse(work)));
    }
    public AbelianGroupHomomorphism cohomologyMap(BigInteger degree) { return cohomologyMap(degree,new Computation()); }
    public AbelianGroupHomomorphism inverseCohomologyMap(BigInteger degree) { Computation work=new Computation(); return cohomologyMap(degree,work).inverse(work); }
    public List<AbelianGroupHomomorphism> cohomologyMaps(BigInteger degree) {
        Computation work=new Computation(); AbelianGroupHomomorphism pullback=cohomologyMap(degree,work); return Collections.unmodifiableList(Arrays.asList(pullback,pullback.inverse(work)));
    }
    @Override public boolean equals(Object other) { return other instanceof SimplicialSubdivision && original.equals(((SimplicialSubdivision)other).original); }
    @Override public int hashCode() { return original.hashCode(); }
    @Override public String toString() { return "Subdivision(original="+original+", subdivided="+subdivided+")"; }
}
