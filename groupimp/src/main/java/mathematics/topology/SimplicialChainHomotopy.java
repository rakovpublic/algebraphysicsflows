package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Supplied integral H with dH + Hd = to - from, retaining the actual witness matrices. */
public final class SimplicialChainHomotopy implements Serializable {
    private static final long serialVersionUID=1L;
    private final SimplicialChainMap from,to;
    private final List<IntegerMatrix> matrices;

    /** Immutable input; membership alone does not assert the homotopy equations. */
    public static final class Data implements Serializable {
        private static final long serialVersionUID=1L;
        private final SimplicialChainMap from,to;
        private final List<IntegerMatrix> matrices;
        public Data(SimplicialChainMap from,SimplicialChainMap to,List<IntegerMatrix> matrices) {
            this.from=Objects.requireNonNull(from); this.to=Objects.requireNonNull(to); Objects.requireNonNull(matrices);
            if(matrices.size()!=from.chainMatrices().size()) throw MathFailure.undefined("Supply one homotopy matrix per degree through the largest ambient dimension");
            List<IntegerMatrix> copy=new ArrayList<>(matrices); for(IntegerMatrix matrix : copy) Objects.requireNonNull(matrix);
            this.matrices=Collections.unmodifiableList(copy);
        }
        public SimplicialChainMap from() { return from; }
        public SimplicialChainMap to() { return to; }
        public List<IntegerMatrix> matrices() { return matrices; }
        @Override public boolean equals(Object other) { return other instanceof Data && from.equals(((Data)other).from) && to.equals(((Data)other).to) && matrices.equals(((Data)other).matrices); }
        @Override public int hashCode() { return Objects.hash(from,to,matrices); }
        @Override public String toString() { return "ChainHomotopyData(from="+from+", to="+to+", matrices="+matrices+")"; }
    }
    public SimplicialChainHomotopy(SimplicialChainMap from,SimplicialChainMap to,List<IntegerMatrix> matrices) { this(new Data(from,to,matrices)); }
    public SimplicialChainHomotopy(Data data) { this(data,new Computation()); }
    private SimplicialChainHomotopy(Data data,Computation work) {
        from=data.from; to=data.to; matrices=data.matrices; requireParallel(from,to);
        for(int k=0;k<matrices.size();k++) {
            BigInteger degree=BigInteger.valueOf(k); IntegerMatrix h=matrices.get(k);
            if(h.rows()!=target().basis(degree.add(BigInteger.ONE)).size() || h.columns()!=source().basis(degree).size())
                throw MathFailure.undefined("Homotopy matrix shape must be target degree k+1 by source degree k");
            work.use((long)h.rows()*h.columns());
        }
        for(int k=0;k<matrices.size();k++) {
            BigInteger degree=BigInteger.valueOf(k);
            IntegerMatrix left=work.multiply(target().boundaryMatrix(degree.add(BigInteger.ONE),work),matrices.get(k)),
                    right=work.multiply(matrixAt(degree.subtract(BigInteger.ONE)),source().boundaryMatrix(degree,work));
            work.use(3L*left.rows()*left.columns());
            if(!left.add(right).equals(to.chainMatrix(degree).add(from.chainMatrix(degree).scale(BigInteger.ONE.negate()))))
                throw MathFailure.undefined("Homotopy matrices must satisfy dH + Hd = to - from in degree "+k);
        }
    }
    private static void requireParallel(SimplicialChainMap a,SimplicialChainMap b) {
        if(!a.source().equals(b.source()) || !a.target().equals(b.target())) throw MathFailure.undefined("Chain homotopies require identical full labelled source and target pairs");
    }
    private interface MatrixFactory { IntegerMatrix at(BigInteger degree); }
    private static SimplicialChainHomotopy build(SimplicialChainMap from,SimplicialChainMap to,Computation work,MatrixFactory factory) {
        List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<from.chainMatrices().size();k++) result.add(factory.at(BigInteger.valueOf(k)));
        return new SimplicialChainHomotopy(new Data(from,to,result),work);
    }
    public static SimplicialChainHomotopy stationary(SimplicialChainMap map) {
        Computation work=new Computation(); return build(map,map,work,d -> {
            int rows=map.target().basis(d.add(BigInteger.ONE)).size(),columns=map.source().basis(d).size(); work.use((long)rows*columns); return IntegerMatrix.zero(rows,columns);
        });
    }
    public static SimplicialChainHomotopy fromPrism(SimplicialHomotopy prism) {
        Computation work=new Computation(); return build(SimplicialChainMap.fromSimplicial(prism.from(),work),SimplicialChainMap.fromSimplicial(prism.to(),work),work,d -> prism.prism(d,work));
    }
    public static SimplicialChainHomotopy fromPath(SimplicialHomotopyPath path) {
        Computation work=new Computation(); return build(SimplicialChainMap.fromSimplicial(path.from(),work),SimplicialChainMap.fromSimplicial(path.to(),work),work,d -> path.prism(d,work));
    }
    private static SimplicialChainHomotopy contraction(SimplicialChainMap retraction,Computation work,MatrixFactory factory) {
        SimplicialChainMap inclusion=SimplicialChainMap.fromSimplicial(RelativeSimplicialMap.inclusion(retraction.target(),retraction.source(),work),work);
        return build(inclusion.compose(retraction,work),SimplicialChainMap.identity(retraction.source(),work),work,factory);
    }
    public static SimplicialChainHomotopy fromCollapse(SimplicialCollapse collapse) {
        Computation work=new Computation(); return contraction(SimplicialChainMap.fromCollapse(collapse,work),work,d -> collapse.homotopy(d,work));
    }
    public static SimplicialChainHomotopy fromCollapseSequence(SimplicialCollapseSequence sequence) {
        Computation work=new Computation(); return contraction(SimplicialChainMap.fromCollapseSequence(sequence,work),work,d -> sequence.homotopy(d,work));
    }
    public static SimplicialChainHomotopy fromSubdivision(SimplicialSubdivision subdivision) {
        Computation work=new Computation(); SimplicialChainMap s=SimplicialChainMap.fromSubdivision(subdivision,work),last=SimplicialChainMap.fromSimplicial(subdivision.lastVertexMap(work),work);
        SimplicialSubdivisionChains chains=new SimplicialSubdivisionChains(subdivision,work);
        return build(s.compose(last,work),SimplicialChainMap.identity(subdivision.subdivided(),work),work,chains::homotopyMatrix);
    }
    public SimplicialChainMap from() { return from; }
    public SimplicialChainMap to() { return to; }
    public RelativeSimplicialComplex source() { return from.source(); }
    public RelativeSimplicialComplex target() { return from.target(); }
    public Data data() { return new Data(from,to,matrices); }
    private IntegerMatrix matrixAt(BigInteger degree) {
        if(degree.signum()>=0 && degree.compareTo(BigInteger.valueOf(matrices.size()))<0) return matrices.get(degree.intValueExact());
        return IntegerMatrix.zero(target().basis(degree.add(BigInteger.ONE)).size(),source().basis(degree).size());
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Chain-homotopy matrix and integral map degrees must be nonnegative"); }
    public IntegerMatrix chainMatrix(BigInteger degree) { requireDegree(degree); return matrixAt(degree); }
    public IntegerMatrix cochainMatrix(BigInteger degree) { requireDegree(degree); return matrixAt(degree.subtract(BigInteger.ONE)).transpose(); }
    public List<IntegerMatrix> chainMatrices() { return matrices; }
    public List<IntegerMatrix> cochainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=matrices.size();k++) { IntegerMatrix h=matrixAt(BigInteger.valueOf(k-1)); work.use((long)h.rows()*h.columns()); result.add(h.transpose()); }
        return Collections.unmodifiableList(result);
    }
    public SimplicialChainHomotopy reverse() {
        Computation work=new Computation(); return build(to,from,work,d -> { IntegerMatrix h=matrixAt(d); work.use((long)h.rows()*h.columns()); return h.scale(BigInteger.ONE.negate()); });
    }
    /** Chronological concatenation with equality of the full joining chain map. */
    public SimplicialChainHomotopy then(SimplicialChainHomotopy next) {
        if(!to.equals(next.from)) throw MathFailure.undefined("Homotopy concatenation requires the exact full joining chain map");
        Computation work=new Computation(); return build(from,next.to,work,d -> sum(matrixAt(d),next.matrixAt(d),work));
    }
    private static IntegerMatrix sum(IntegerMatrix a,IntegerMatrix b,Computation work) { work.use((long)a.rows()*a.columns()); return a.add(b); }
    public SimplicialChainHomotopy add(SimplicialChainHomotopy other) {
        requireParallel(from,other.from); Computation work=new Computation();
        return build(from.add(other.from,work),to.add(other.to,work),work,d -> sum(matrixAt(d),other.matrixAt(d),work));
    }
    public SimplicialChainHomotopy scale(BigInteger scalar) {
        Computation work=new Computation(); return build(from.scale(scalar,work),to.scale(scalar,work),work,d -> {
            IntegerMatrix h=matrixAt(d); work.use((long)h.rows()*h.columns()); return h.scale(scalar);
        });
    }
    public SimplicialChainHomotopy precompose(SimplicialChainMap before) {
        Computation work=new Computation(); return build(from.compose(before,work),to.compose(before,work),work,d -> work.multiply(matrixAt(d),before.chainMatrix(d)));
    }
    public SimplicialChainHomotopy postcompose(SimplicialChainMap after) {
        Computation work=new Computation(); return build(after.compose(from,work),after.compose(to,work),work,d -> work.multiply(after.chainMatrix(d.add(BigInteger.ONE)),matrixAt(d)));
    }
    public RelativeSimplicialChain onChain(RelativeSimplicialChain chain) {
        if(!source().equals(chain.pair())) throw MathFailure.undefined("Chain homotopy requires the full source pair");
        return new RelativeSimplicialChain(target(),chain.degree().add(BigInteger.ONE),new Computation().apply(matrixAt(chain.degree()),chain.coordinates()));
    }
    public RelativeSimplicialCochain onCochain(RelativeSimplicialCochain cochain) {
        if(!target().equals(cochain.pair())) throw MathFailure.undefined("Cochain homotopy requires the full target pair");
        if(cochain.degree().signum()==0) throw MathFailure.undefined("Typed cochain homotopy requires positive degree; use cochain-matrix for degree zero");
        BigInteger degree=cochain.degree().subtract(BigInteger.ONE);
        return new RelativeSimplicialCochain(source(),degree,new Computation().apply(matrixAt(degree).transpose(),cochain.coordinates()));
    }
    private void requireAbsolute() {
        if(!source().subcomplex().faces().isEmpty() || !target().subcomplex().faces().isEmpty()) throw MathFailure.undefined("Absolute homotopy actions require both subcomplexes empty");
    }
    public SimplicialChain onAbsoluteChain(SimplicialChain chain) {
        requireAbsolute(); RelativeSimplicialChain result=onChain(RelativeSimplicialChain.absolute(chain)); return new SimplicialChain(target().ambient(),result.degree(),result.coordinates());
    }
    public SimplicialCochain onAbsoluteCochain(SimplicialCochain cochain) {
        requireAbsolute(); RelativeSimplicialCochain result=onCochain(RelativeSimplicialCochain.absolute(cochain)); return new SimplicialCochain(source().ambient(),result.degree(),result.coordinates());
    }
    private List<AbelianGroupHomomorphism> maps(BigInteger degree,boolean dual) {
        requireDegree(degree); Computation work=new Computation();
        return Collections.unmodifiableList(Arrays.asList(from.induced(degree,dual,work),to.induced(degree,dual,work)));
    }
    public List<AbelianGroupHomomorphism> homologyMaps(BigInteger degree) { return maps(degree,false); }
    public List<AbelianGroupHomomorphism> cohomologyMaps(BigInteger degree) { return maps(degree,true); }
    @Override public boolean equals(Object other) { return other instanceof SimplicialChainHomotopy && from.equals(((SimplicialChainHomotopy)other).from) && to.equals(((SimplicialChainHomotopy)other).to) && matrices.equals(((SimplicialChainHomotopy)other).matrices); }
    @Override public int hashCode() { return Objects.hash(from,to,matrices); }
    @Override public String toString() { return "ChainHomotopy(from="+from+", to="+to+", matrices="+matrices+")"; }
}
