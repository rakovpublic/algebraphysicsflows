package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** One compatible codimension-one free-face collapse of a labelled pair, with integral chain witnesses. */
public final class SimplicialCollapse implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialComplex source,target;
    private final FiniteSet<Integer> face,coface;
    private final int incidence;

    private static final class Index {
        final RelativeSimplicialComplex pair;
        final Map<FiniteSet<Integer>,List<FiniteSet<Integer>>> cofaces=new HashMap<>();
        Index(RelativeSimplicialComplex pair,Computation work) {
            this.pair=pair;
            for(FiniteSet<Integer> simplex : pair.ambient().faces()) { work.use(1L+simplex.size()); cofaces.put(simplex,new ArrayList<>()); }
            for(FiniteSet<Integer> simplex : pair.ambient().faces()) if(simplex.size()>1) {
                List<Integer> vertices=IntegralSimplicialHomology.vertices(simplex); work.use((long)vertices.size()*vertices.size());
                for(int i=0;i<vertices.size();i++) { List<Integer> facet=new ArrayList<>(vertices); facet.remove(i); cofaces.get(new FiniteSet<>(facet)).add(simplex); }
            }
        }
        FiniteSet<Integer> compatibleCoface(FiniteSet<Integer> face,Computation work) {
            work.use(1L+face.size()); List<FiniteSet<Integer>> higher=cofaces.get(face);
            if(higher==null || higher.size()!=1) return null;
            FiniteSet<Integer> coface=higher.get(0);
            if(!cofaces.get(coface).isEmpty() || pair.subcomplex().faces().contains(face)!=pair.subcomplex().faces().contains(coface)) return null;
            return coface;
        }
    }
    public SimplicialCollapse(RelativeSimplicialComplex source,FiniteSet<BigInteger> face) {
        this(source,face,new Computation());
    }
    SimplicialCollapse(RelativeSimplicialComplex source,FiniteSet<BigInteger> face,Computation work) {
        this.source=Objects.requireNonNull(source); Objects.requireNonNull(face);
        if(face.size()==0 || face.size()>source.ambient().dimension()+1) throw MathFailure.undefined("A collapse requires a nonempty original free face");
        List<Integer> labels=new ArrayList<>(); for(BigInteger vertex : face.members()) {
            try { labels.add(vertex.intValueExact()); } catch(ArithmeticException e) { throw MathFailure.undefined("Free-face labels must fit signed ints"); }
        }
        this.face=new FiniteSet<>(labels); coface=new Index(source,work).compatibleCoface(this.face,work);
        if(coface==null) throw MathFailure.undefined("A collapse requires a unique codimension-one maximal coface, with both removed simplices inside or both outside the subcomplex");
        List<Integer> vertices=IntegralSimplicialHomology.vertices(coface); int omitted=0; while(this.face.contains(vertices.get(omitted))) omitted++;
        incidence=(omitted&1)==0?1:-1;
        target=new RelativeSimplicialComplex(remove(source.ambient(),work),remove(source.subcomplex(),work));
    }
    private FiniteSimplicialComplex remove(FiniteSimplicialComplex complex,Computation work) {
        Set<FiniteSet<Integer>> result=new LinkedHashSet<>();
        for(FiniteSet<Integer> simplex : complex.faces()) { work.use(1L+simplex.size()); if(!simplex.equals(face) && !simplex.equals(coface)) result.add(simplex); }
        return FiniteSimplicialComplex.fromClosedFaces(result);
    }
    public static SimplicialCollapse absolute(FiniteSimplicialComplex complex,FiniteSet<BigInteger> face) { return new SimplicialCollapse(RelativeSimplicialComplex.absolute(complex),face); }
    private static FiniteSet<BigInteger> labels(FiniteSet<Integer> simplex) {
        List<BigInteger> result=new ArrayList<>(); for(int label : IntegralSimplicialHomology.vertices(simplex)) result.add(BigInteger.valueOf(label)); return new FiniteSet<>(result);
    }
    public static List<FiniteSet<BigInteger>> freeFaces(RelativeSimplicialComplex pair) {
        return freeFaces(pair,new Computation());
    }
    static List<FiniteSet<BigInteger>> freeFaces(RelativeSimplicialComplex pair,Computation work) {
        Index index=new Index(pair,work); List<FiniteSet<Integer>> faces=new ArrayList<>();
        for(FiniteSet<Integer> face : pair.ambient().faces()) if(index.compatibleCoface(face,work)!=null) faces.add(face);
        faces.sort((a,b) -> {
            work.use(1L+a.size()+b.size()); int compare=Integer.compare(a.size(),b.size()); if(compare!=0) return compare;
            List<Integer> x=IntegralSimplicialHomology.vertices(a),y=IntegralSimplicialHomology.vertices(b);
            for(int i=0;i<x.size();i++) { compare=Integer.compare(x.get(i),y.get(i)); if(compare!=0) return compare; } return 0;
        });
        List<FiniteSet<BigInteger>> result=new ArrayList<>(); for(FiniteSet<Integer> face : faces) result.add(labels(face)); return Collections.unmodifiableList(result);
    }
    public static boolean hasFreeFace(RelativeSimplicialComplex pair) {
        Computation work=new Computation(); Index index=new Index(pair,work);
        for(FiniteSet<Integer> face : pair.ambient().faces()) if(index.compatibleCoface(face,work)!=null) return true; return false;
    }
    public RelativeSimplicialComplex source() { return source; }
    public RelativeSimplicialComplex target() { return target; }
    public FiniteSet<BigInteger> freeFace() { return labels(face); }
    public FiniteSet<BigInteger> coface() { return labels(coface); }
    public RelativeSimplicialMap inclusion() { return RelativeSimplicialMap.inclusion(target,source); }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Collapse matrix and integral map degrees must be nonnegative"); }
    private static BigInteger[][] zeros(int rows,int columns,Computation work) {
        work.use((long)rows*columns); BigInteger[][] result=new BigInteger[rows][columns]; for(BigInteger[] row : result) Arrays.fill(row,BigInteger.ZERO); return result;
    }
    /** R(tau) = -epsilon*(boundary sigma - epsilon*tau), R(sigma)=0; all other basis simplices are fixed. */
    IntegerMatrix retraction(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> rows=target.basis(degree),columns=source.basis(degree); BigInteger[][] entries=zeros(rows.size(),columns.size(),work);
        Map<FiniteSet<Integer>,Integer> positions=new HashMap<>(); for(int r=0;r<rows.size();r++) { work.use(1L+rows.get(r).size()); positions.put(rows.get(r),r); }
        for(int c=0;c<columns.size();c++) {
            FiniteSet<Integer> simplex=columns.get(c); work.use(1L+simplex.size()); Integer row=positions.get(simplex); if(row!=null) entries[row][c]=BigInteger.ONE;
            if(simplex.equals(face)) {
                List<Integer> vertices=IntegralSimplicialHomology.vertices(coface); work.use((long)vertices.size()*vertices.size());
                for(int i=0;i<vertices.size();i++) { List<Integer> facet=new ArrayList<>(vertices); facet.remove(i); row=positions.get(new FiniteSet<>(facet)); if(row!=null) entries[row][c]=BigInteger.valueOf((i&1)==0?-incidence:incidence); }
            }
        }
        return new IntegerMatrix(rows.size(),columns.size(),entries);
    }
    private IntegerMatrix inclusion(BigInteger degree,Computation work) { return RelativeSimplicialComplex.selector(source.basis(degree),target.basis(degree),work); }
    /** H(tau)=epsilon*sigma; boundary H + H boundary = identity - inclusion R. */
    IntegerMatrix homotopy(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> rows=source.basis(degree.add(BigInteger.ONE)),columns=source.basis(degree); BigInteger[][] entries=zeros(rows.size(),columns.size(),work);
        work.use((long)(rows.size()+columns.size())*(coface.size()+1)); int row=rows.indexOf(coface),column=columns.indexOf(face);
        if(row>=0 && column>=0) entries[row][column]=BigInteger.valueOf(incidence); return new IntegerMatrix(rows.size(),columns.size(),entries);
    }
    public IntegerMatrix chainMatrix(BigInteger degree) { requireDegree(degree); return retraction(degree,new Computation()); }
    public IntegerMatrix cochainMatrix(BigInteger degree) { return chainMatrix(degree).transpose(); }
    public IntegerMatrix chainHomotopyMatrix(BigInteger degree) { requireDegree(degree); return homotopy(degree,new Computation()); }
    public IntegerMatrix cochainHomotopyMatrix(BigInteger degree) { requireDegree(degree); return homotopy(degree.subtract(BigInteger.ONE),new Computation()).transpose(); }
    private List<IntegerMatrix> matrices(boolean homotopy,boolean dual) {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>(); int shift=homotopy && dual?1:0;
        for(int k=0;k<=source.ambient().dimension()+shift;k++) { BigInteger degree=BigInteger.valueOf(k-shift); IntegerMatrix matrix=homotopy?homotopy(degree,work):retraction(degree,work); result.add(dual?matrix.transpose():matrix); }
        return Collections.unmodifiableList(result);
    }
    public List<IntegerMatrix> chainMatrices() { return matrices(false,false); }
    public List<IntegerMatrix> cochainMatrices() { return matrices(false,true); }
    public List<IntegerMatrix> chainHomotopyMatrices() { return matrices(true,false); }
    public List<IntegerMatrix> cochainHomotopyMatrices() { return matrices(true,true); }
    public RelativeSimplicialChain onChain(RelativeSimplicialChain chain) {
        if(!source.equals(chain.pair())) throw MathFailure.undefined("Collapse retraction requires a chain on the full source pair");
        Computation work=new Computation(); return new RelativeSimplicialChain(target,chain.degree(),work.apply(retraction(chain.degree(),work),chain.coordinates()));
    }
    public RelativeSimplicialCochain onCochain(RelativeSimplicialCochain cochain) {
        if(!target.equals(cochain.pair())) throw MathFailure.undefined("Collapse pullback requires a cochain on the full target pair");
        Computation work=new Computation(); return new RelativeSimplicialCochain(source,cochain.degree(),work.apply(retraction(cochain.degree(),work).transpose(),cochain.coordinates()));
    }
    public RelativeSimplicialChain homotopyOnChain(RelativeSimplicialChain chain) {
        if(!source.equals(chain.pair())) throw MathFailure.undefined("Collapse homotopy requires a chain on the full source pair");
        Computation work=new Computation(); return new RelativeSimplicialChain(source,chain.degree().add(BigInteger.ONE),work.apply(homotopy(chain.degree(),work),chain.coordinates()));
    }
    public RelativeSimplicialCochain homotopyOnCochain(RelativeSimplicialCochain cochain) {
        if(!source.equals(cochain.pair())) throw MathFailure.undefined("Collapse cochain homotopy requires the full source pair");
        if(cochain.degree().signum()==0) throw MathFailure.undefined("Typed cochain homotopies require positive degree; use cochain-homotopy-matrix for degree zero");
        BigInteger degree=cochain.degree().subtract(BigInteger.ONE); Computation work=new Computation();
        return new RelativeSimplicialCochain(source,degree,work.apply(homotopy(degree,work).transpose(),cochain.coordinates()));
    }
    private void requireAbsolute() { if(!source.subcomplex().faces().isEmpty()) throw MathFailure.undefined("Absolute collapse actions require an empty source subcomplex"); }
    private static SimplicialChain absolute(RelativeSimplicialChain c) { return new SimplicialChain(c.pair().ambient(),c.degree(),c.coordinates()); }
    private static SimplicialCochain absolute(RelativeSimplicialCochain c) { return new SimplicialCochain(c.pair().ambient(),c.degree(),c.coordinates()); }
    public SimplicialChain onAbsoluteChain(SimplicialChain c) { requireAbsolute(); return absolute(onChain(RelativeSimplicialChain.absolute(c))); }
    public SimplicialCochain onAbsoluteCochain(SimplicialCochain c) { requireAbsolute(); return absolute(onCochain(RelativeSimplicialCochain.absolute(c))); }
    public SimplicialChain homotopyOnAbsoluteChain(SimplicialChain c) { requireAbsolute(); return absolute(homotopyOnChain(RelativeSimplicialChain.absolute(c))); }
    public SimplicialCochain homotopyOnAbsoluteCochain(SimplicialCochain c) { requireAbsolute(); return absolute(homotopyOnCochain(RelativeSimplicialCochain.absolute(c))); }
    private AbelianGroupHomomorphism induced(BigInteger degree,boolean cohomology,boolean inverse,IntegralHomology original,IntegralHomology reduced,Computation work) {
        IntegerMatrix matrix=inverse?inclusion(degree,work):retraction(degree,work); if(cohomology) matrix=matrix.transpose();
        return cohomology!=inverse?reduced.inducedMap(original,matrix,work):original.inducedMap(reduced,matrix,work);
    }
    private List<AbelianGroupHomomorphism> maps(BigInteger degree,boolean cohomology,boolean inverse,boolean both) {
        requireDegree(degree); Computation work=new Computation();
        IntegralHomology original=cohomology?RelativeSimplicialCochain.cohomology(source,degree,work):source.homology(degree,work),
                reduced=cohomology?RelativeSimplicialCochain.cohomology(target,degree,work):target.homology(degree,work);
        List<AbelianGroupHomomorphism> result=new ArrayList<>(); result.add(induced(degree,cohomology,inverse,original,reduced,work));
        if(both) result.add(induced(degree,cohomology,!inverse,original,reduced,work)); return Collections.unmodifiableList(result);
    }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return maps(degree,false,false,false).get(0); }
    public AbelianGroupHomomorphism inverseHomologyMap(BigInteger degree) { return maps(degree,false,true,false).get(0); }
    public List<AbelianGroupHomomorphism> homologyMaps(BigInteger degree) { return maps(degree,false,false,true); }
    public AbelianGroupHomomorphism cohomologyMap(BigInteger degree) { return maps(degree,true,false,false).get(0); }
    public AbelianGroupHomomorphism inverseCohomologyMap(BigInteger degree) { return maps(degree,true,true,false).get(0); }
    public List<AbelianGroupHomomorphism> cohomologyMaps(BigInteger degree) { return maps(degree,true,false,true); }
    @Override public boolean equals(Object other) { return other instanceof SimplicialCollapse && source.equals(((SimplicialCollapse)other).source) && face.equals(((SimplicialCollapse)other).face); }
    @Override public int hashCode() { return Objects.hash(source,face); }
    @Override public String toString() { return "SimplicialCollapse(source="+source+", free-face="+freeFace()+", coface="+coface()+", target="+target+")"; }
}
