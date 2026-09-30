package mathematics.topology;

import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import java.math.BigInteger;
import java.util.*;

/** Sparse carrier-cone construction; caches and budget belong to one complete public computation. */
final class SimplicialSubdivisionChains {
    private final SimplicialSubdivision context;
    private final Computation work;
    private final Map<FiniteSet<Integer>,Map<FiniteSet<Integer>,BigInteger>> subdivisions=new HashMap<>(),homotopies=new HashMap<>();
    SimplicialSubdivisionChains(SimplicialSubdivision context,Computation work) { this.context=context; this.work=work; }

    private void add(Map<FiniteSet<Integer>,BigInteger> chain,FiniteSet<Integer> simplex,BigInteger value) {
        work.use(1L+simplex.size());
        BigInteger next=chain.getOrDefault(simplex,BigInteger.ZERO).add(value);
        if(next.signum()==0) chain.remove(simplex); else chain.put(simplex,next);
    }
    private void addScaled(Map<FiniteSet<Integer>,BigInteger> target,Map<FiniteSet<Integer>,BigInteger> source,int sign) {
        for(Map.Entry<FiniteSet<Integer>,BigInteger> term : source.entrySet()) add(target,term.getKey(),sign>0?term.getValue():term.getValue().negate());
    }
    /** Prepend the cone vertex and convert to the sorted orientation; repeated vertices vanish. */
    private Map<FiniteSet<Integer>,BigInteger> cone(int apex,Map<FiniteSet<Integer>,BigInteger> chain) {
        Map<FiniteSet<Integer>,BigInteger> result=new HashMap<>();
        for(Map.Entry<FiniteSet<Integer>,BigInteger> term : chain.entrySet()) {
            work.use(1L+term.getKey().size()); if(term.getKey().members().contains(apex)) continue;
            List<Integer> vertices=new ArrayList<>(term.getKey().members()); int sign=1;
            for(int vertex : vertices) if(vertex<apex) sign=-sign;
            vertices.add(apex); add(result,new FiniteSet<>(vertices),sign>0?term.getValue():term.getValue().negate());
        }
        return result;
    }
    /** S(sigma) = cone_b(S(boundary sigma)), with S([v]) = [{v}]. */
    private Map<FiniteSet<Integer>,BigInteger> subdivide(FiniteSet<Integer> simplex) {
        work.use(1L+simplex.size()); Map<FiniteSet<Integer>,BigInteger> cached=subdivisions.get(simplex); if(cached!=null) return cached;
        List<Integer> vertices=IntegralSimplicialHomology.vertices(simplex); Map<FiniteSet<Integer>,BigInteger> boundary=new HashMap<>(),result;
        int apex=context.faceLabel(simplex);
        if(vertices.size()==1) { result=new HashMap<>(); add(result,FiniteSet.of(apex),BigInteger.ONE); }
        else {
            for(int i=0;i<vertices.size();i++) { List<Integer> face=new ArrayList<>(vertices); face.remove(i); addScaled(boundary,subdivide(new FiniteSet<>(face)),(i&1)==0?1:-1); }
            result=cone(apex,boundary);
        }
        subdivisions.put(simplex,result); return result;
    }
    /** P(sigma) = cone_b(sigma - S l# sigma - P(boundary sigma)) in sd(maximum face). */
    private Map<FiniteSet<Integer>,BigInteger> homotopy(FiniteSet<Integer> simplex) {
        work.use(1L+simplex.size()); Map<FiniteSet<Integer>,BigInteger> cached=homotopies.get(simplex); if(cached!=null) return cached;
        List<Integer> vertices=IntegralSimplicialHomology.vertices(simplex),image=new ArrayList<>();
        Map<FiniteSet<Integer>,BigInteger> cycle=new HashMap<>(); add(cycle,simplex,BigInteger.ONE);
        for(int vertex : vertices) { FiniteSet<Integer> face=context.originalFace(vertex); work.use(1L+face.size()); image.add(Collections.max(face.members())); }
        work.use((long)image.size()*image.size());
        if(new HashSet<>(image).size()==image.size()) {
            int sign=-1; for(int i=0;i<image.size();i++) for(int j=i+1;j<image.size();j++) if(image.get(i)>image.get(j)) sign=-sign;
            addScaled(cycle,subdivide(new FiniteSet<>(image)),sign);
        }
        if(vertices.size()>1) for(int i=0;i<vertices.size();i++) {
            List<Integer> face=new ArrayList<>(vertices); face.remove(i);
            // Retain absolute terms until the final projection: coning a discarded A-face can leave A.
            addScaled(cycle,homotopy(new FiniteSet<>(face)),(i&1)==0?-1:1);
        }
        Map<FiniteSet<Integer>,BigInteger> result=cone(vertices.get(vertices.size()-1),cycle); homotopies.put(simplex,result); return result;
    }
    private IntegerMatrix matrix(List<FiniteSet<Integer>> rows,List<FiniteSet<Integer>> columns,boolean homotopy) {
        work.use((long)rows.size()*columns.size()); BigInteger[][] entries=new BigInteger[rows.size()][columns.size()];
        for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
        if(!rows.isEmpty()) {
            Map<FiniteSet<Integer>,Integer> positions=new HashMap<>();
            for(int r=0;r<rows.size();r++) { work.use(1L+rows.get(r).size()); positions.put(rows.get(r),r); }
            for(int c=0;c<columns.size();c++) {
                Map<FiniteSet<Integer>,BigInteger> value=homotopy?homotopy(columns.get(c)):subdivide(columns.get(c));
                for(Map.Entry<FiniteSet<Integer>,BigInteger> term : value.entrySet()) {
                    work.use(1L+term.getKey().size()); Integer row=positions.get(term.getKey()); if(row!=null) entries[row][c]=term.getValue();
                }
            }
        }
        return new IntegerMatrix(rows.size(),columns.size(),entries);
    }
    IntegerMatrix subdivisionMatrix(BigInteger degree) { return matrix(context.subdivided().basis(degree),context.original().basis(degree),false); }
    IntegerMatrix homotopyMatrix(BigInteger degree) { return matrix(context.subdivided().basis(degree.add(BigInteger.ONE)),context.subdivided().basis(degree),true); }
}
