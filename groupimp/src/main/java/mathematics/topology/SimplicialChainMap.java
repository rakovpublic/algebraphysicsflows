package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** An integral degree-zero chain map between the quotient chains of two full labelled pairs. */
public final class SimplicialChainMap implements Serializable {
    private static final long serialVersionUID=1L;
    private final RelativeSimplicialComplex source,target;
    private final List<IntegerMatrix> matrices;

    /** Supplied data, not a claim that the matrices commute with the differentials. */
    public static final class Data implements Serializable {
        private static final long serialVersionUID=1L;
        private final RelativeSimplicialComplex source,target;
        private final List<IntegerMatrix> matrices;
        public Data(RelativeSimplicialComplex source,RelativeSimplicialComplex target,List<IntegerMatrix> matrices) {
            this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target); Objects.requireNonNull(matrices);
            if(matrices.size()!=topDegree(source,target)+1) throw MathFailure.undefined("Supply exactly one chain matrix in every degree from zero through the largest ambient dimension");
            List<IntegerMatrix> copy=new ArrayList<>(matrices); for(IntegerMatrix matrix : copy) Objects.requireNonNull(matrix);
            this.matrices=Collections.unmodifiableList(copy);
        }
        public RelativeSimplicialComplex source() { return source; }
        public RelativeSimplicialComplex target() { return target; }
        public List<IntegerMatrix> matrices() { return matrices; }
        @Override public boolean equals(Object other) { return other instanceof Data && source.equals(((Data)other).source) && target.equals(((Data)other).target) && matrices.equals(((Data)other).matrices); }
        @Override public int hashCode() { return Objects.hash(source,target,matrices); }
        @Override public String toString() { return "ChainMapData(source="+source+", target="+target+", matrices="+matrices+")"; }
    }
    public SimplicialChainMap(RelativeSimplicialComplex source,RelativeSimplicialComplex target,List<IntegerMatrix> matrices) { this(new Data(source,target,matrices)); }
    public SimplicialChainMap(Data data) { this(data,new Computation()); }
    SimplicialChainMap(Data data,Computation work) {
        source=data.source; target=data.target; matrices=data.matrices;
        for(int k=0;k<matrices.size();k++) {
            BigInteger degree=BigInteger.valueOf(k); IntegerMatrix matrix=matrices.get(k);
            if(matrix.rows()!=target.basis(degree).size() || matrix.columns()!=source.basis(degree).size())
                throw MathFailure.undefined("Chain matrix shape must match the target and source quotient bases in degree "+k);
            work.use((long)matrix.rows()*matrix.columns());
        }
        for(int k=1;k<matrices.size();k++) {
            BigInteger degree=BigInteger.valueOf(k);
            IntegerMatrix left=work.multiply(target.boundaryMatrix(degree,work),matrices.get(k));
            IntegerMatrix right=work.multiply(matrices.get(k-1),source.boundaryMatrix(degree,work));
            work.use((long)left.rows()*left.columns());
            if(!left.equals(right)) throw MathFailure.undefined("Chain matrices must satisfy d_target F = F d_source in degree "+k);
        }
    }
    private static int topDegree(RelativeSimplicialComplex source,RelativeSimplicialComplex target) { return Math.max(source.ambient().dimension(),target.ambient().dimension()); }
    private interface MatrixFactory { IntegerMatrix at(BigInteger degree); }
    private static SimplicialChainMap build(RelativeSimplicialComplex source,RelativeSimplicialComplex target,Computation work,MatrixFactory factory) {
        List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=topDegree(source,target);k++) result.add(factory.at(BigInteger.valueOf(k)));
        return new SimplicialChainMap(new Data(source,target,result),work);
    }
    public static SimplicialChainMap identity(RelativeSimplicialComplex pair) { return identity(pair,new Computation()); }
    static SimplicialChainMap identity(RelativeSimplicialComplex pair,Computation work) {
        return build(pair,pair,work,d -> {
            int size=pair.basis(d).size(); work.use((long)size*size); return IntegerMatrix.identity(size);
        });
    }
    public static SimplicialChainMap zero(RelativeSimplicialComplex source,RelativeSimplicialComplex target) { return zero(source,target,new Computation()); }
    static SimplicialChainMap zero(RelativeSimplicialComplex source,RelativeSimplicialComplex target,Computation work) {
        return build(source,target,work,d -> {
            int rows=target.basis(d).size(),columns=source.basis(d).size(); work.use((long)rows*columns); return IntegerMatrix.zero(rows,columns);
        });
    }
    public static SimplicialChainMap fromSimplicial(RelativeSimplicialMap map) { return fromSimplicial(map,new Computation()); }
    static SimplicialChainMap fromSimplicial(RelativeSimplicialMap map,Computation work) {
        return build(map.source(),map.target(),work,d -> map.chainMatrix(d,work));
    }
    public static SimplicialChainMap fromAbsoluteSimplicial(FiniteSimplicialMap map) {
        Computation work=new Computation(); return build(RelativeSimplicialComplex.absolute(map.source()),RelativeSimplicialComplex.absolute(map.target()),work,d -> map.chainMatrix(d,work));
    }
    public static SimplicialChainMap fromCollapse(SimplicialCollapse collapse) { return fromCollapse(collapse,new Computation()); }
    static SimplicialChainMap fromCollapse(SimplicialCollapse collapse,Computation work) {
        return build(collapse.source(),collapse.target(),work,d -> collapse.retraction(d,work));
    }
    public static SimplicialChainMap fromCollapseSequence(SimplicialCollapseSequence sequence) { return fromCollapseSequence(sequence,new Computation()); }
    static SimplicialChainMap fromCollapseSequence(SimplicialCollapseSequence sequence,Computation work) {
        return build(sequence.source(),sequence.target(),work,d -> sequence.retraction(d,work));
    }
    public static SimplicialChainMap fromSubdivision(SimplicialSubdivision subdivision) { return fromSubdivision(subdivision,new Computation()); }
    static SimplicialChainMap fromSubdivision(SimplicialSubdivision subdivision,Computation work) {
        SimplicialSubdivisionChains chains=new SimplicialSubdivisionChains(subdivision,work);
        return build(subdivision.original(),subdivision.subdivided(),work,chains::subdivisionMatrix);
    }
    public RelativeSimplicialComplex source() { return source; }
    public RelativeSimplicialComplex target() { return target; }
    public Data data() { return new Data(source,target,matrices); }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Chain-map matrix and integral map degrees must be nonnegative"); }
    private IntegerMatrix matrixAt(BigInteger degree) {
        return degree.signum()<0 || degree.compareTo(BigInteger.valueOf(matrices.size()))>=0?IntegerMatrix.zero(0,0):matrices.get(degree.intValueExact());
    }
    public IntegerMatrix chainMatrix(BigInteger degree) { requireDegree(degree); return matrixAt(degree); }
    public IntegerMatrix cochainMatrix(BigInteger degree) { return chainMatrix(degree).transpose(); }
    public List<IntegerMatrix> chainMatrices() { return matrices; }
    public List<IntegerMatrix> cochainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(IntegerMatrix matrix : matrices) { work.use((long)matrix.rows()*matrix.columns()); result.add(matrix.transpose()); } return Collections.unmodifiableList(result);
    }
    public boolean isZero() {
        for(IntegerMatrix matrix : matrices) for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.columns();c++) if(matrix.get(r,c).signum()!=0) return false; return true;
    }
    public boolean isIdentity() {
        if(!source.equals(target)) return false;
        for(IntegerMatrix matrix : matrices) for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.columns();c++)
            if(!matrix.get(r,c).equals(r==c?BigInteger.ONE:BigInteger.ZERO)) return false; return true;
    }
    private void requireParallel(SimplicialChainMap other) {
        if(!source.equals(other.source) || !target.equals(other.target)) throw MathFailure.undefined("Chain-map addition requires identical full labelled source and target pairs");
    }
    private SimplicialChainMap combine(SimplicialChainMap other,boolean subtract,Computation work) {
        requireParallel(other); return build(source,target,work,d -> {
            IntegerMatrix a=matrixAt(d),b=other.matrixAt(d); work.use((subtract?2L:1L)*a.rows()*a.columns()); return a.add(subtract?b.scale(BigInteger.ONE.negate()):b);
        });
    }
    public SimplicialChainMap add(SimplicialChainMap other) { return add(other,new Computation()); }
    SimplicialChainMap add(SimplicialChainMap other,Computation work) { return combine(other,false,work); }
    public SimplicialChainMap subtract(SimplicialChainMap other) { return combine(other,true,new Computation()); }
    public SimplicialChainMap scale(BigInteger scalar) { return scale(scalar,new Computation()); }
    SimplicialChainMap scale(BigInteger scalar,Computation work) {
        Objects.requireNonNull(scalar); return build(source,target,work,d -> {
            IntegerMatrix matrix=matrixAt(d); work.use((long)matrix.rows()*matrix.columns()); return matrix.scale(scalar);
        });
    }
    public SimplicialChainMap negate() { return scale(BigInteger.ONE.negate()); }
    /** This after before, requiring equality of the entire joining pair. */
    public SimplicialChainMap compose(SimplicialChainMap before) { return compose(before,new Computation()); }
    SimplicialChainMap compose(SimplicialChainMap before,Computation work) {
        if(!source.equals(before.target)) throw MathFailure.undefined("Chain-map composition requires identical full labelled joining pairs");
        return build(before.source,target,work,d -> work.multiply(matrixAt(d),before.matrixAt(d)));
    }
    public boolean isIsomorphism() {
        Computation work=new Computation();
        for(IntegerMatrix matrix : matrices) {
            if(matrix.rows()!=matrix.columns()) return false;
            List<BigInteger> factors=work.invariantFactors(matrix);
            if(factors.size()!=matrix.rows()) return false;
            for(BigInteger factor : factors) if(!factor.equals(BigInteger.ONE)) return false;
        }
        return true;
    }
    public boolean isQuasiIsomorphism() { return new IntegralChainMappingCone(this).isAcyclic(); }
    public SimplicialChainMap inverse() { return inverse(new Computation()); }
    SimplicialChainMap inverse(Computation work) {
        return build(target,source,work,d -> work.inverseUnimodular(matrixAt(d)));
    }
    public RelativeSimplicialChain onChain(RelativeSimplicialChain chain) {
        if(!source.equals(chain.pair())) throw MathFailure.undefined("Chain-map pushforward requires the full source pair");
        return new RelativeSimplicialChain(target,chain.degree(),new Computation().apply(matrixAt(chain.degree()),chain.coordinates()));
    }
    public RelativeSimplicialCochain onCochain(RelativeSimplicialCochain cochain) {
        if(!target.equals(cochain.pair())) throw MathFailure.undefined("Chain-map pullback requires the full target pair");
        return new RelativeSimplicialCochain(source,cochain.degree(),new Computation().apply(matrixAt(cochain.degree()).transpose(),cochain.coordinates()));
    }
    private void requireAbsolute() {
        if(!source.subcomplex().faces().isEmpty() || !target.subcomplex().faces().isEmpty()) throw MathFailure.undefined("Absolute chain-map actions require empty subcomplexes at both endpoints");
    }
    public SimplicialChain onAbsoluteChain(SimplicialChain chain) {
        requireAbsolute(); RelativeSimplicialChain result=onChain(RelativeSimplicialChain.absolute(chain)); return new SimplicialChain(target.ambient(),result.degree(),result.coordinates());
    }
    public SimplicialCochain onAbsoluteCochain(SimplicialCochain cochain) {
        requireAbsolute(); RelativeSimplicialCochain result=onCochain(RelativeSimplicialCochain.absolute(cochain)); return new SimplicialCochain(source.ambient(),result.degree(),result.coordinates());
    }
    AbelianGroupHomomorphism induced(BigInteger degree,boolean dual,Computation work) {
        requireDegree(degree);
        IntegralHomology a=dual?RelativeSimplicialCochain.cohomology(source,degree,work):source.homology(degree,work);
        IntegralHomology b=source.equals(target)?a:dual?RelativeSimplicialCochain.cohomology(target,degree,work):target.homology(degree,work);
        return dual?b.inducedMap(a,matrixAt(degree).transpose(),work):a.inducedMap(b,matrixAt(degree),work);
    }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { return induced(degree,false,new Computation()); }
    public AbelianGroupHomomorphism cohomologyMap(BigInteger degree) { return induced(degree,true,new Computation()); }
    private List<AbelianGroupHomomorphism> maps(boolean dual) {
        Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        for(int k=0;k<matrices.size();k++) result.add(induced(BigInteger.valueOf(k),dual,work)); return Collections.unmodifiableList(result);
    }
    public List<AbelianGroupHomomorphism> homologyMaps() { return maps(false); }
    public List<AbelianGroupHomomorphism> cohomologyMaps() { return maps(true); }
    @Override public boolean equals(Object other) { return other instanceof SimplicialChainMap && source.equals(((SimplicialChainMap)other).source) && target.equals(((SimplicialChainMap)other).target) && matrices.equals(((SimplicialChainMap)other).matrices); }
    @Override public int hashCode() { return Objects.hash(source,target,matrices); }
    @Override public String toString() { return "ChainMap(source="+source+", target="+target+", matrices="+matrices+")"; }
}
