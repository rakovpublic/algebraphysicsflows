package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.foundations.Pair;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import java.math.BigInteger;
import java.util.*;

/** Constructive dominated-vertex reductions, preserving both components of a labelled pair. */
public final class SimplicialStrongCollapse {
    private SimplicialStrongCollapse() {}

    private static final class Index {
        final RelativeSimplicialComplex pair;
        final SortedMap<Integer,List<FiniteSet<Integer>>> ambient,subcomplex;
        Index(RelativeSimplicialComplex pair,Computation work) {
            this.pair=Objects.requireNonNull(pair); ambient=incident(pair.ambient(),work); subcomplex=incident(pair.subcomplex(),work);
        }
        private static SortedMap<Integer,List<FiniteSet<Integer>>> incident(FiniteSimplicialComplex complex,Computation work) {
            SortedMap<Integer,List<FiniteSet<Integer>>> result=new TreeMap<>();
            for(FiniteSet<Integer> face : complex.faces()) {
                work.use(1L+face.size());
                for(int vertex : face.members()) result.computeIfAbsent(vertex,v -> new ArrayList<>()).add(face);
            }
            return result;
        }
        List<Integer> candidates(int vertex,Computation work) {
            List<FiniteSet<Integer>> faces=ambient.get(vertex);
            if(faces==null) throw MathFailure.undefined("The removed vertex must belong to the ambient complex");
            // A face of largest size incident to v is maximal. Every dominator must occur in it.
            FiniteSet<Integer> largest=faces.get(0);
            for(FiniteSet<Integer> face : faces) { work.use(1); if(face.size()>largest.size()) largest=face; }
            List<Integer> result=new ArrayList<>(largest.members()); work.use(result.size()); Collections.sort(result); result.remove(Integer.valueOf(vertex)); return result;
        }
        boolean dominates(int vertex,int survivor,Computation work) {
            if(vertex==survivor || !ambient.containsKey(vertex) || !ambient.containsKey(survivor)) return false;
            if(!cone(ambient.get(vertex),pair.ambient(),survivor,work)) return false;
            return !subcomplex.containsKey(vertex) || cone(subcomplex.get(vertex),pair.subcomplex(),survivor,work);
        }
        private static boolean cone(List<FiniteSet<Integer>> faces,FiniteSimplicialComplex complex,int survivor,Computation work) {
            for(FiniteSet<Integer> face : faces) {
                work.use(1L+face.size());
                if(!complex.faces().contains(face.union(FiniteSet.of(survivor)))) return false;
            }
            return true;
        }
        Integer firstDominator(int vertex,Computation work) {
            for(int survivor : candidates(vertex,work)) if(dominates(vertex,survivor,work)) return survivor; return null;
        }
        Pair<Integer,Integer> first(Computation work) {
            for(int vertex : ambient.keySet()) { Integer survivor=firstDominator(vertex,work); if(survivor!=null) return new Pair<>(vertex,survivor); }
            return null;
        }
    }
    private static int vertexLabel(BigInteger vertex) {
        Objects.requireNonNull(vertex);
        try { return vertex.intValueExact(); } catch(ArithmeticException e) { throw MathFailure.undefined("Vertex label is outside the signed integer label range"); }
    }
    /** All compatible dominators of one ambient vertex, in ascending label order. */
    public static List<BigInteger> dominators(RelativeSimplicialComplex pair,BigInteger vertex) {
        Computation work=new Computation(); Index index=new Index(pair,work); int removed=vertexLabel(vertex); List<BigInteger> result=new ArrayList<>();
        for(int survivor : index.candidates(removed,work)) if(index.dominates(removed,survivor,work)) result.add(BigInteger.valueOf(survivor));
        return Collections.unmodifiableList(result);
    }
    public static List<BigInteger> dominatedVertices(RelativeSimplicialComplex pair) {
        Computation work=new Computation(); Index index=new Index(pair,work); List<BigInteger> result=new ArrayList<>();
        for(int vertex : index.ambient.keySet()) if(index.firstDominator(vertex,work)!=null) result.add(BigInteger.valueOf(vertex));
        return Collections.unmodifiableList(result);
    }
    public static boolean isStrongCore(RelativeSimplicialComplex pair) { Computation work=new Computation(); return new Index(pair,work).first(work)==null; }
    private static FiniteSimplicialComplex delete(FiniteSimplicialComplex complex,int vertex,Computation work) {
        Set<FiniteSet<Integer>> faces=new LinkedHashSet<>();
        for(FiniteSet<Integer> face : complex.faces()) { work.use(1); if(!face.contains(vertex)) faces.add(face); }
        return FiniteSimplicialComplex.fromClosedFaces(faces);
    }
    private static RelativeSimplicialMap inclusion(RelativeSimplicialComplex source,RelativeSimplicialComplex target,Computation work) {
        Map<BigInteger,BigInteger> vertices=new TreeMap<>();
        for(BigInteger vertex : FiniteSimplicialMap.vertexSet(source.ambient()).members()) { work.use(1); vertices.put(vertex,vertex); }
        return new RelativeSimplicialMap(source,target,new FiniteSimplicialMap(source.ambient(),target.ambient(),vertices,work),work);
    }
    private static RelativeSimplicialMap retraction(RelativeSimplicialComplex source,int vertex,int survivor,Computation work) {
        RelativeSimplicialComplex target=new RelativeSimplicialComplex(delete(source.ambient(),vertex,work),delete(source.subcomplex(),vertex,work));
        Map<BigInteger,BigInteger> vertices=new TreeMap<>();
        for(BigInteger label : FiniteSimplicialMap.vertexSet(source.ambient()).members()) { work.use(1); vertices.put(label,label.intValueExact()==vertex?BigInteger.valueOf(survivor):label); }
        return new RelativeSimplicialMap(source,target,new FiniteSimplicialMap(source.ambient(),target.ambient(),vertices,work),work);
    }
    private static SimplicialHomotopyEquivalence witness(RelativeSimplicialMap forward,List<RelativeSimplicialMap> stages,Computation work) {
        RelativeSimplicialMap backward=inclusion(forward.target(),forward.source(),work);
        return new SimplicialHomotopyEquivalence(forward,backward,new SimplicialHomotopyPath(stages,work),
                new SimplicialHomotopyPath(Collections.singletonList(RelativeSimplicialMap.identity(forward.target(),work)),work),work);
    }
    /** Delete the first label by mapping it to the second; all remaining vertices are fixed. */
    public static SimplicialHomotopyEquivalence collapseVertex(RelativeSimplicialComplex pair,Pair<BigInteger,BigInteger> vertices) {
        Computation work=new Computation(); int vertex=vertexLabel(vertices.first),survivor=vertexLabel(vertices.second);
        if(!new Index(pair,work).dominates(vertex,survivor,work)) throw MathFailure.undefined("Collapse requires distinct ambient vertices and domination in both pair components containing the removed vertex");
        RelativeSimplicialMap forward=retraction(pair,vertex,survivor,work),backward=inclusion(forward.target(),pair,work);
        return witness(forward,Arrays.asList(RelativeSimplicialMap.identity(pair,work),backward.compose(forward,work)),work);
    }
    /** Repeatedly remove the least compatible dominated vertex using its least dominator. */
    public static SimplicialHomotopyEquivalence strongCore(RelativeSimplicialComplex pair) {
        Computation work=new Computation(); RelativeSimplicialMap forward=RelativeSimplicialMap.identity(pair,work);
        List<RelativeSimplicialMap> stages=new ArrayList<>(); stages.add(forward);
        while(true) {
            Pair<Integer,Integer> move=new Index(forward.target(),work).first(work);
            if(move==null) return witness(forward,stages,work);
            if(stages.size()==SimplicialHomotopyPath.MAX_STAGES) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Strong-core witness exceeds 256 stages");
            forward=retraction(forward.target(),move.first,move.second,work).compose(forward,work);
            stages.add(inclusion(forward.target(),pair,work).compose(forward,work));
        }
    }
    public static SimplicialHomotopyEquivalence strongCoreAbsolute(FiniteSimplicialComplex complex) { return strongCore(RelativeSimplicialComplex.absolute(complex)); }
}
