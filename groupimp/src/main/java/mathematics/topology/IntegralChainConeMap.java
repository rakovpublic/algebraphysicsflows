package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** A map of integral cones retaining a square and a witness bF -> Ga. */
public final class IntegralChainConeMap implements Serializable {
    private static final long serialVersionUID=1L;
    private final Data data;
    /** Individually valid inputs; exact endpoints and the chosen square witness are checked on construction. */
    public static final class Data implements Serializable {
        private static final long serialVersionUID=1L;
        private final IntegralChainMappingCone source,target;
        private final SimplicialChainMap sourceMap,targetMap;
        private final SimplicialChainHomotopy homotopy;
        public Data(IntegralChainMappingCone source,IntegralChainMappingCone target,SimplicialChainMap sourceMap,SimplicialChainMap targetMap,SimplicialChainHomotopy homotopy) {
            this.source=Objects.requireNonNull(source); this.target=Objects.requireNonNull(target);
            this.sourceMap=Objects.requireNonNull(sourceMap); this.targetMap=Objects.requireNonNull(targetMap); this.homotopy=Objects.requireNonNull(homotopy);
        }
        public IntegralChainMappingCone source() { return source; }
        public IntegralChainMappingCone target() { return target; }
        public SimplicialChainMap sourceMap() { return sourceMap; }
        public SimplicialChainMap targetMap() { return targetMap; }
        public SimplicialChainHomotopy homotopy() { return homotopy; }
        private String contents() { return "source="+source+", target="+target+", source-map="+sourceMap+", target-map="+targetMap+", homotopy="+homotopy; }
        @Override public String toString() { return "ChainConeMapData("+contents()+")"; }
        @Override public boolean equals(Object other) {
            if(!(other instanceof Data)) return false; Data d=(Data)other;
            return source.equals(d.source) && target.equals(d.target) && sourceMap.equals(d.sourceMap) && targetMap.equals(d.targetMap) && homotopy.equals(d.homotopy);
        }
        @Override public int hashCode() { return Objects.hash(source,target,sourceMap,targetMap,homotopy); }
    }
    public IntegralChainConeMap(Data data) { this(data,new Computation()); }
    private IntegralChainConeMap(Data data,Computation work) {
        this.data=Objects.requireNonNull(data);
        if(!sourceMap().source().equals(source().source()) || !sourceMap().target().equals(target().source())
                || !targetMap().source().equals(source().target()) || !targetMap().target().equals(target().target()))
            throw MathFailure.undefined("Cone square maps require all four full labelled endpoint pairs");
        if(!homotopy().from().equals(targetMap().compose(source().map(),work)) || !homotopy().to().equals(target().map().compose(sourceMap(),work)))
            throw MathFailure.undefined("Cone square witness must run from b after F to G after a");
    }
    public static IntegralChainConeMap identity(IntegralChainMappingCone cone) {
        Computation work=new Computation();
        return new IntegralChainConeMap(new Data(cone,cone,SimplicialChainMap.identity(cone.source(),work),SimplicialChainMap.identity(cone.target(),work),SimplicialChainHomotopy.stationary(cone.map(),work)),work);
    }
    public static IntegralChainConeMap fromHomotopy(SimplicialChainHomotopy homotopy) {
        Computation work=new Computation();
        return new IntegralChainConeMap(new Data(new IntegralChainMappingCone(homotopy.from()),new IntegralChainMappingCone(homotopy.to()),
                SimplicialChainMap.identity(homotopy.source(),work),SimplicialChainMap.identity(homotopy.target(),work),homotopy),work);
    }
    public Data data() { return data; }
    public IntegralChainMappingCone source() { return data.source; }
    public IntegralChainMappingCone target() { return data.target; }
    public SimplicialChainMap sourceMap() { return data.sourceMap; }
    public SimplicialChainMap targetMap() { return data.targetMap; }
    public SimplicialChainHomotopy homotopy() { return data.homotopy; }
    /** This after before: transported witness b_after H_before + H_after a_before. */
    public IntegralChainConeMap compose(IntegralChainConeMap before) {
        if(!source().equals(before.target())) throw MathFailure.undefined("Cone-map composition requires the exact defining map at the joining cone");
        Computation work=new Computation();
        SimplicialChainMap a=sourceMap().compose(before.sourceMap(),work),b=targetMap().compose(before.targetMap(),work);
        SimplicialChainHomotopy h=before.homotopy().postcompose(targetMap(),work).then(homotopy().precompose(before.sourceMap(),work),work);
        return new IntegralChainConeMap(new Data(before.source(),target(),a,b,h),work);
    }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Cone-map degrees must be nonnegative"); }
    private int dimension() { return Math.max(source().dimension(),target().dimension()); }
    private static void block(BigInteger[][] into,IntegerMatrix matrix,int row,int column,boolean negate,Computation work) {
        work.use((long)matrix.rows()*matrix.columns());
        for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.columns();c++) into[row+r][column+c]=negate?matrix.get(r,c).negate():matrix.get(r,c);
    }
    private IntegerMatrix matrix(BigInteger degree,Computation work) {
        int rows=target().rank(degree),columns=source().rank(degree),targetOffset=target().target().basis(degree).size(),sourceOffset=source().target().basis(degree).size();
        work.use((long)rows*columns); BigInteger[][] entries=new BigInteger[rows][columns]; for(BigInteger[] row : entries) Arrays.fill(row,BigInteger.ZERO);
        block(entries,targetMap().chainMatrix(degree),0,0,false,work);
        if(degree.signum()>0) {
            BigInteger previous=degree.subtract(BigInteger.ONE);
            block(entries,homotopy().chainMatrix(previous),0,sourceOffset,true,work);
            block(entries,sourceMap().chainMatrix(previous),targetOffset,sourceOffset,false,work);
        }
        return new IntegerMatrix(rows,columns,entries);
    }
    private static IntegerMatrix transpose(IntegerMatrix matrix,Computation work) { work.use((long)matrix.rows()*matrix.columns()); return matrix.transpose(); }
    public IntegerMatrix chainMatrix(BigInteger degree) { requireDegree(degree); return matrix(degree,new Computation()); }
    public IntegerMatrix cochainMatrix(BigInteger degree) { requireDegree(degree); Computation work=new Computation(); return transpose(matrix(degree,work),work); }
    private List<IntegerMatrix> matrices(boolean dual) {
        Computation work=new Computation(); List<IntegerMatrix> result=new ArrayList<>();
        for(int k=0;k<=dimension();k++) { IntegerMatrix m=matrix(BigInteger.valueOf(k),work); result.add(dual?transpose(m,work):m); } return Collections.unmodifiableList(result);
    }
    public List<IntegerMatrix> chainMatrices() { return matrices(false); }
    public List<IntegerMatrix> cochainMatrices() { return matrices(true); }
    private AbelianGroupHomomorphism induced(BigInteger degree,boolean dual,Computation work) {
        IntegerMatrix m=matrix(degree,work);
        IntegralHomology a=source().homology(degree,dual,work),b=target().homology(degree,dual,work);
        return dual?b.inducedMap(a,transpose(m,work),work):a.inducedMap(b,m,work);
    }
    public AbelianGroupHomomorphism homologyMap(BigInteger degree) { requireDegree(degree); return induced(degree,false,new Computation()); }
    public AbelianGroupHomomorphism cohomologyMap(BigInteger degree) { requireDegree(degree); return induced(degree,true,new Computation()); }
    private List<AbelianGroupHomomorphism> maps(boolean dual) {
        Computation work=new Computation(); List<AbelianGroupHomomorphism> result=new ArrayList<>();
        for(int k=0;k<=dimension();k++) result.add(induced(BigInteger.valueOf(k),dual,work)); return Collections.unmodifiableList(result);
    }
    public List<AbelianGroupHomomorphism> homologyMaps() { return maps(false); }
    public List<AbelianGroupHomomorphism> cohomologyMaps() { return maps(true); }
    private static AbelianGroupHomomorphism endpointMap(SimplicialChainMap map,BigInteger degree,boolean dual,Computation work) {
        IntegralHomology a=IntegralChainMappingCone.endpoint(map.source(),degree,dual,work),b=IntegralChainMappingCone.endpoint(map.target(),degree,dual,work);
        IntegerMatrix m=degree.signum()<0?IntegerMatrix.zero(0,0):map.chainMatrix(degree);
        return dual?b.inducedMap(a,transpose(m,work),work):a.inducedMap(b,m,work);
    }
    /** Vertical maps [H_n(a), H_n(b), H_n(cone map), H_(n-1)(a)] between homology segments. */
    public List<AbelianGroupHomomorphism> homologyNaturalityMaps(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        AbelianGroupHomomorphism c=induced(degree,false,work);
        return Collections.unmodifiableList(Arrays.asList(endpointMap(sourceMap(),degree,false,work),endpointMap(targetMap(),degree,false,work),c,endpointMap(sourceMap(),degree.subtract(BigInteger.ONE),false,work)));
    }
    /** Contravariant vertical maps [H^n(cone map), H^n(b), H^n(a), H^(n+1)(cone map)]. */
    public List<AbelianGroupHomomorphism> cohomologyNaturalityMaps(BigInteger degree) {
        requireDegree(degree); Computation work=new Computation();
        AbelianGroupHomomorphism c=induced(degree,true,work),next=induced(degree.add(BigInteger.ONE),true,work);
        return Collections.unmodifiableList(Arrays.asList(c,endpointMap(targetMap(),degree,true,work),endpointMap(sourceMap(),degree,true,work),next));
    }
    @Override public boolean equals(Object other) { return other instanceof IntegralChainConeMap && data.equals(((IntegralChainConeMap)other).data); }
    @Override public int hashCode() { return data.hashCode(); }
    @Override public String toString() { return "ChainConeMap("+data.contents()+")"; }
}
