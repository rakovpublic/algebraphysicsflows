package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import java.math.BigInteger;
import java.util.*;

/** Exhaustive search for compatible elementary collapses, with atomic, bounded witness enumeration. */
public final class SimplicialCollapseSearch {
    public static final int MAX_STATES=4096,MAX_SEQUENCES=1024;
    private SimplicialCollapseSearch() {}

    public static boolean canCollapseTo(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        return !new Search(source,Objects.requireNonNull(target)).run(true).isEmpty();
    }
    public static SimplicialCollapseSequence collapseTo(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        return witness(new Search(source,Objects.requireNonNull(target)).run(true));
    }
    public static List<SimplicialCollapseSequence> collapsesTo(RelativeSimplicialComplex source,RelativeSimplicialComplex target) {
        return new Search(source,Objects.requireNonNull(target)).run(false);
    }
    public static boolean absoluteCanCollapseTo(FiniteSimplicialComplex source,FiniteSimplicialComplex target) {
        return canCollapseTo(RelativeSimplicialComplex.absolute(source),RelativeSimplicialComplex.absolute(target));
    }
    public static SimplicialCollapseSequence absoluteCollapseTo(FiniteSimplicialComplex source,FiniteSimplicialComplex target) {
        return collapseTo(RelativeSimplicialComplex.absolute(source),RelativeSimplicialComplex.absolute(target));
    }
    public static List<SimplicialCollapseSequence> absoluteCollapsesTo(FiniteSimplicialComplex source,FiniteSimplicialComplex target) {
        return collapsesTo(RelativeSimplicialComplex.absolute(source),RelativeSimplicialComplex.absolute(target));
    }
    public static boolean isCollapsible(FiniteSimplicialComplex source) {
        return !new Search(RelativeSimplicialComplex.absolute(source),null).run(true).isEmpty();
    }
    public static SimplicialCollapseSequence collapseToPoint(FiniteSimplicialComplex source) {
        return witness(new Search(RelativeSimplicialComplex.absolute(source),null).run(true));
    }
    public static List<SimplicialCollapseSequence> collapsesToPoint(FiniteSimplicialComplex source) {
        return new Search(RelativeSimplicialComplex.absolute(source),null).run(false);
    }
    private static SimplicialCollapseSequence witness(List<SimplicialCollapseSequence> results) {
        if(results.isEmpty()) throw MathFailure.undefined("There is no compatible elementary collapse sequence to the requested target");
        return results.get(0);
    }
    private static final class Search {
        final RelativeSimplicialComplex source,target; // null target means any single vertex of an absolute input
        final Computation work=new Computation();
        final Set<RelativeSimplicialComplex> visited=new HashSet<>(),failed=new HashSet<>();
        final List<SimplicialCollapse> path=new ArrayList<>();
        final List<SimplicialCollapseSequence> results=new ArrayList<>();
        Search(RelativeSimplicialComplex source,RelativeSimplicialComplex target) { this.source=Objects.requireNonNull(source); this.target=target; }
        private void charge(RelativeSimplicialComplex pair) {
            for(FiniteSet<Integer> face : pair.ambient().faces()) work.use(1L+face.size());
            for(FiniteSet<Integer> face : pair.subcomplex().faces()) work.use(1L+face.size());
        }
        private boolean compatible() {
            charge(source); int targetSize;
            if(target==null) {
                if(!source.ambient().eulerCharacteristic().equals(BigInteger.ONE)) return false;
                targetSize=1;
            } else {
                charge(target); targetSize=target.ambient().faces().size();
                if(!target.ambient().subcomplexOf(source.ambient()) || !source.subcomplex().intersection(target.ambient()).equals(target.subcomplex())) return false;
                if(!source.ambient().eulerCharacteristic().equals(target.ambient().eulerCharacteristic())
                        || !source.subcomplex().eulerCharacteristic().equals(target.subcomplex().eulerCharacteristic())) return false;
            }
            int removed=source.ambient().faces().size()-targetSize;
            if(removed<0 || (removed&1)!=0) return false;
            if(removed/2>SimplicialCollapseSequence.MAX_STEPS)
                throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Collapse search would require more than 256 steps per sequence");
            return true;
        }
        List<SimplicialCollapseSequence> run(boolean firstOnly) {
            if(compatible()) visit(source,firstOnly); return Collections.unmodifiableList(results);
        }
        private boolean visit(RelativeSimplicialComplex current,boolean firstOnly) {
            charge(current); work.use(1);
            if(failed.contains(current)) return false;
            if(!visited.contains(current)) {
                if(visited.size()==MAX_STATES) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Collapse search exceeds 4096 distinct intermediate pairs");
                visited.add(current);
            }
            if(target==null?current.ambient().faces().size()==1:current.equals(target)) {
                if(results.size()==MAX_SEQUENCES) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Collapse enumeration exceeds 1024 sequences");
                results.add(new SimplicialCollapseSequence(source,path,work)); return true;
            }
            boolean found=false;
            for(FiniteSet<BigInteger> face : SimplicialCollapse.freeFaces(current,work)) {
                if(target!=null) {
                    List<Integer> labels=new ArrayList<>(); for(BigInteger v : face.members()) { work.use(1); labels.add(v.intValueExact()); }
                    // If the target contains the coface, closure also makes it contain this face.
                    if(target.ambient().faces().contains(new FiniteSet<>(labels))) continue;
                }
                SimplicialCollapse step=new SimplicialCollapse(current,face,work); path.add(step);
                boolean reached=visit(step.target(),firstOnly); path.remove(path.size()-1);
                if(reached) { found=true; if(firstOnly) return true; }
            }
            if(!found) failed.add(current); // Successful suffixes must be revisited for each distinct prefix when enumerating.
            return found;
        }
    }
}
