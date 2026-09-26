package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import java.math.BigInteger;
import java.util.*;

/** Exhaustive, budgeted vertex-bijection search preserving both components of a simplicial pair. */
public final class SimplicialIsomorphismSearch {
    public static final int MAX_VERTICES=64,MAX_ISOMORPHISMS=1024;
    private SimplicialIsomorphismSearch() {}

    public static boolean isIsomorphicTo(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        return first(source,target,new Computation())!=null;
    }
    public static RelativeSimplicialMap isomorphismTo(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        RelativeSimplicialMap map=first(source,target,new Computation());
        if(map==null) throw MathFailure.undefined("The labelled pairs admit no simplicial isomorphism"); return map;
    }
    public static List<RelativeSimplicialMap> isomorphismsTo(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        return new Search(source,target,new Computation()).run(false);
    }
    static RelativeSimplicialMap first(RelativeSimplicialComplex source,RelativeSimplicialComplex target,Computation work) {
        List<RelativeSimplicialMap> maps=new Search(source,target,work).run(true); return maps.isEmpty()?null:maps.get(0);
    }
    private static final class Search {
        final RelativeSimplicialComplex source,target;
        final Computation work;
        final List<BigInteger> sourceVertices,targetVertices;
        final List<List<int[]>> ambientIncident=new ArrayList<>(),subcomplexIncident=new ArrayList<>();
        final int[] images;
        final boolean[] used;
        final int[][] sourceProfiles,targetProfiles;
        final boolean compatible;
        final List<RelativeSimplicialMap> results=new ArrayList<>();
        Search(RelativeSimplicialComplex source,RelativeSimplicialComplex target,Computation work) {
            this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target); this.work=work;
            sourceVertices=new ArrayList<>(FiniteSimplicialMap.vertexSet(source.ambient()).members());
            targetVertices=new ArrayList<>(FiniteSimplicialMap.vertexSet(target.ambient()).members());
            if(sourceVertices.size()>MAX_VERTICES || targetVertices.size()>MAX_VERTICES)
                throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Simplicial isomorphism search allows at most 64 vertices in each ambient complex");
            int dimensions=Math.max(source.ambient().dimension(),target.ambient().dimension())+1;
            sourceProfiles=new int[sourceVertices.size()][2*dimensions]; targetProfiles=new int[targetVertices.size()][2*dimensions];
            images=new int[sourceVertices.size()]; Arrays.fill(images,-1); used=new boolean[targetVertices.size()];
            for(int v=0;v<sourceVertices.size();v++) { ambientIncident.add(new ArrayList<>()); subcomplexIncident.add(new ArrayList<>()); }
            int[] a=profile(source,sourceVertices,sourceProfiles,dimensions,true),b=profile(target,targetVertices,targetProfiles,dimensions,false);
            compatible=sourceVertices.size()==targetVertices.size() && Arrays.equals(a,b);
        }
        private int[] profile(RelativeSimplicialComplex pair,List<BigInteger> vertices,int[][] profiles,int dimensions,boolean indexSource) {
            Map<Integer,Integer> positions=new HashMap<>(); for(int i=0;i<vertices.size();i++) { work.use(1); positions.put(vertices.get(i).intValueExact(),i); }
            int[] counts=new int[2*dimensions];
            for(int component=0;component<2;component++) for(FiniteSet<Integer> face : (component==0?pair.ambient():pair.subcomplex()).faces()) {
                work.use(1L+face.size()); int column=component*dimensions+face.size()-1; counts[column]++; int[] indices=new int[face.size()]; int next=0;
                for(int vertex : face.members()) { int position=positions.get(vertex); indices[next++]=position; profiles[position][column]++; }
                if(indexSource) for(int position : indices) (component==0?ambientIncident:subcomplexIncident).get(position).add(indices);
            }
            return counts;
        }
        List<RelativeSimplicialMap> run(boolean firstOnly) {
            if(compatible) visit(0,firstOnly); return Collections.unmodifiableList(results);
        }
        private boolean visit(int position,boolean firstOnly) {
            work.use(1);
            if(position==images.length) {
                if(results.size()==MAX_ISOMORPHISMS) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Simplicial isomorphism enumeration exceeds 1024 maps");
                Map<BigInteger,BigInteger> values=new TreeMap<>();
                for(int i=0;i<images.length;i++) { work.use(1); values.put(sourceVertices.get(i),targetVertices.get(images[i])); }
                results.add(new RelativeSimplicialMap(source,target,new FiniteSimplicialMap(source.ambient(),target.ambient(),values,work),work)); return firstOnly;
            }
            for(int candidate=0;candidate<targetVertices.size();candidate++) {
                work.use(1L+sourceProfiles[position].length);
                if(used[candidate] || !Arrays.equals(sourceProfiles[position],targetProfiles[candidate])) continue;
                images[position]=candidate; used[candidate]=true;
                boolean valid=preserves(ambientIncident.get(position),target.ambient()) && preserves(subcomplexIncident.get(position),target.subcomplex());
                if(valid && visit(position+1,firstOnly)) return true;
                images[position]=-1; used[candidate]=false;
            }
            return false;
        }
        private boolean preserves(List<int[]> faces,FiniteSimplicialComplex complex) {
            for(int[] face : faces) {
                work.use(1L+face.length); List<Integer> partialImage=new ArrayList<>();
                for(int vertex : face) if(images[vertex]>=0) partialImage.add(targetVertices.get(images[vertex]).intValueExact());
                if(!complex.faces().contains(new FiniteSet<>(partialImage))) return false;
            }
            return true;
        }
    }
}
