package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Actual integral witnesses D K + K D = to - from between retained cone maps. */
public final class IntegralConeHomotopy implements Serializable {
    private static final long serialVersionUID=1L;
    private final Data data;
    /** Immutable supplied matrices; compatibility, shifted shapes and equations are checked separately. */
    public static final class Data implements Serializable {
        private static final long serialVersionUID=1L;
        private final IntegralChainConeMap from,to;
        private final List<IntegerMatrix> matrices;
        public Data(IntegralChainConeMap from,IntegralChainConeMap to,List<IntegerMatrix> matrices) {
            this.from=Objects.requireNonNull(from); this.to=Objects.requireNonNull(to); Objects.requireNonNull(matrices);
            if(matrices.size()!=Math.max(from.source().dimension(),from.target().dimension())+1)
                throw MathFailure.undefined("Supply one cone homotopy matrix per degree through the maximum formal cone dimension");
            List<IntegerMatrix> copy=new ArrayList<>(matrices); for(IntegerMatrix matrix : copy) Objects.requireNonNull(matrix);
            this.matrices=Collections.unmodifiableList(copy);
        }
        public IntegralChainConeMap from() { return from; }
        public IntegralChainConeMap to() { return to; }
        public List<IntegerMatrix> matrices() { return matrices; }
        private String contents() { return "from="+from+", to="+to+", matrices="+matrices; }
        @Override public boolean equals(Object other) { return other instanceof Data && from.equals(((Data)other).from) && to.equals(((Data)other).to) && matrices.equals(((Data)other).matrices); }
        @Override public int hashCode() { return Objects.hash(from,to,matrices); }
        @Override public String toString() { return "ConeHomotopyData("+contents()+")"; }
    }
    public IntegralConeHomotopy(IntegralChainConeMap from,IntegralChainConeMap to,List<IntegerMatrix> matrices) { this(new Data(from,to,matrices)); }
    public IntegralConeHomotopy(Data data) { this(data,new Computation()); }
    IntegralConeHomotopy(Data data,Computation work) {
        this.data=Objects.requireNonNull(data); requireParallel(from(),to());
        for(int k=0;k<data.matrices.size();k++) {
            BigInteger degree=BigInteger.valueOf(k); IntegerMatrix h=data.matrices.get(k);
            if(h.rows()!=target().rank(degree.add(BigInteger.ONE)) || h.columns()!=source().rank(degree))
                throw MathFailure.undefined("Cone homotopy matrix shape must be target degree n+1 by source degree n");
            work.use((long)h.rows()*h.columns());
        }
        for(int k=0;k<data.matrices.size();k++) {
            BigInteger degree=BigInteger.valueOf(k);
            IntegerMatrix left=work.multiply(target().boundary(degree.add(BigInteger.ONE),work),data.matrices.get(k)),
                    right=work.multiply(matrixAt(degree.subtract(BigInteger.ONE),work),source().boundary(degree,work)),
                    a=from().matrix(degree,work),b=to().matrix(degree,work);
            work.use(3L*left.rows()*left.columns());
            if(!left.add(right).equals(b.add(a.scale(BigInteger.ONE.negate()))))
                throw MathFailure.undefined("Cone homotopy matrices must satisfy D K + K D = to - from in degree "+k);
        }
    }
    private static void requireParallel(IntegralChainConeMap a,IntegralChainConeMap b) {
        if(!a.source().equals(b.source()) || !a.target().equals(b.target())) throw MathFailure.undefined("Cone homotopies require identical full source and target defining maps");
    }
    public IntegralChainConeMap from() { return data.from; }
    public IntegralChainConeMap to() { return data.to; }
    public IntegralChainMappingCone source() { return from().source(); }
    public IntegralChainMappingCone target() { return from().target(); }
    public Data data() { return data; }
    private interface MatrixFactory { IntegerMatrix at(BigInteger degree); }
    private static IntegralConeHomotopy build(IntegralChainConeMap from,IntegralChainConeMap to,Computation work,MatrixFactory factory) {
        requireParallel(from,to); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=Math.max(from.source().dimension(),from.target().dimension());k++) result.add(factory.at(BigInteger.valueOf(k)));
        return new IntegralConeHomotopy(new Data(from,to,result),work);
    }
    private static IntegerMatrix zero(int rows,int columns,Computation work) { work.use((long)rows*columns); return IntegerMatrix.zero(rows,columns); }
    private IntegerMatrix matrixAt(BigInteger degree,Computation work) {
        if(degree.signum()>=0 && degree.compareTo(BigInteger.valueOf(data.matrices.size()))<0) return data.matrices.get(degree.intValueExact());
        return zero(target().rank(degree.add(BigInteger.ONE)),source().rank(degree),work);
    }
    private static IntegerMatrix sum(IntegerMatrix a,IntegerMatrix b,Computation work) { work.use((long)a.rows()*a.columns()); return a.add(b); }
    private static IntegerMatrix times(IntegerMatrix a,BigInteger scalar,Computation work) { work.use((long)a.rows()*a.columns()); return a.scale(scalar); }
    private static IntegerMatrix transpose(IntegerMatrix a,Computation work) { work.use((long)a.rows()*a.columns()); return a.transpose(); }
    public static IntegralConeHomotopy stationary(IntegralChainConeMap map) {
        Computation work=new Computation(); return build(map,map,work,d -> zero(map.target().rank(d.add(BigInteger.ONE)),map.source().rank(d),work));
    }
    /** Contract Cone(F) from zero to identity when F is an integral chain isomorphism: K(t,s)=(0,F^-1 t). */
    public static IntegralConeHomotopy contractIsomorphism(IntegralChainMappingCone cone) {
        Computation work=new Computation(); SimplicialChainMap inverse=cone.map().inverse(work);
        return build(IntegralChainConeMap.zero(cone,cone,work),IntegralChainConeMap.identity(cone,work),work,d -> {
            int rows=cone.rank(d.add(BigInteger.ONE)),columns=cone.rank(d),offset=cone.target().basis(d.add(BigInteger.ONE)).size();
            work.use((long)rows*columns); BigInteger[][] entries=new BigInteger[rows][columns]; for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
            IntegerMatrix block=inverse.chainMatrix(d); work.use((long)block.rows()*block.columns());
            for(int r=0;r<block.rows();r++) for(int c=0;c<block.columns();c++) entries[offset+r][c]=block.get(r,c);
            return new IntegerMatrix(rows,columns,entries);
        });
    }
    public IntegralConeHomotopy reverse() {
        Computation work=new Computation(); return build(to(),from(),work,d -> times(matrixAt(d,work),BigInteger.ONE.negate(),work));
    }
    /** Chronological concatenation, retaining the exact common cone map and both witnesses. */
    public IntegralConeHomotopy then(IntegralConeHomotopy next) {
        if(!to().equals(next.from())) throw MathFailure.undefined("Cone homotopy concatenation requires the exact joining cone map including its square witness");
        Computation work=new Computation(); return build(from(),next.to(),work,d -> sum(matrixAt(d,work),next.matrixAt(d,work),work));
    }
    public IntegralConeHomotopy add(IntegralConeHomotopy other) {
        requireParallel(from(),other.from()); Computation work=new Computation();
        return build(from().add(other.from(),work),to().add(other.to(),work),work,d -> sum(matrixAt(d,work),other.matrixAt(d,work),work));
    }
    public IntegralConeHomotopy scale(BigInteger scalar) {
        Computation work=new Computation(); return build(from().scale(scalar,work),to().scale(scalar,work),work,d -> times(matrixAt(d,work),scalar,work));
    }
    public IntegralConeHomotopy precompose(IntegralChainConeMap before) {
        Computation work=new Computation(); return build(from().compose(before,work),to().compose(before,work),work,d -> work.multiply(matrixAt(d,work),before.matrix(d,work)));
    }
    public IntegralConeHomotopy postcompose(IntegralChainConeMap after) {
        Computation work=new Computation(); return build(after.compose(from(),work),after.compose(to(),work),work,d -> work.multiply(after.matrix(d.add(BigInteger.ONE),work),matrixAt(d,work)));
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Cone homotopy matrix and integral-map degrees must be nonnegative"); }
    public IntegerMatrix chainMatrix(BigInteger degree) { requireDegree(degree); return matrixAt(degree,new Computation()); }
    public IntegerMatrix cochainMatrix(BigInteger degree) { requireDegree(degree); Computation work=new Computation(); return transpose(matrixAt(degree.subtract(BigInteger.ONE),work),work); }
    public List<IntegerMatrix> chainMatrices() { return data.matrices; }
    public List<IntegerMatrix> cochainMatrices() {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=data.matrices.size();k++) result.add(transpose(matrixAt(BigInteger.valueOf(k-1),work),work));
        return Collections.unmodifiableList(result);
    }
    public IntegralConeChain onChain(IntegralConeChain chain) {
        if(!source().equals(chain.cone())) throw MathFailure.undefined("Cone homotopy action requires the exact source cone");
        Computation work=new Computation(); return new IntegralConeChain(target(),chain.degree().add(BigInteger.ONE),work.apply(matrixAt(chain.degree(),work),chain.coordinates()));
    }
    public IntegralConeCochain onCochain(IntegralConeCochain cochain) {
        if(!target().equals(cochain.cone())) throw MathFailure.undefined("Cone cochain homotopy action requires the exact target cone");
        if(cochain.degree().signum()==0) throw MathFailure.undefined("Typed cone cochain homotopy requires positive degree; use cochain-matrix for degree zero");
        Computation work=new Computation(); BigInteger degree=cochain.degree().subtract(BigInteger.ONE);
        return new IntegralConeCochain(source(),degree,work.apply(transpose(matrixAt(degree,work),work),cochain.coordinates()));
    }
    private List<AbelianGroupHomomorphism> maps(BigInteger degree,boolean dual) {
        requireDegree(degree); Computation work=new Computation();
        return Collections.unmodifiableList(Arrays.asList(from().induced(degree,dual,work),to().induced(degree,dual,work)));
    }
    public List<AbelianGroupHomomorphism> homologyMaps(BigInteger degree) { return maps(degree,false); }
    public List<AbelianGroupHomomorphism> cohomologyMaps(BigInteger degree) { return maps(degree,true); }
    @Override public boolean equals(Object other) { return other instanceof IntegralConeHomotopy && data.equals(((IntegralConeHomotopy)other).data); }
    @Override public int hashCode() { return data.hashCode(); }
    @Override public String toString() { return "ConeHomotopy("+data.contents()+")"; }
}
