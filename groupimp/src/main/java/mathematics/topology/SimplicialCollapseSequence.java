package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.AbelianGroupHomomorphism;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** An ordered sequence of compatible elementary collapses, including its composite integral witnesses. */
public final class SimplicialCollapseSequence implements Serializable {
    private static final long serialVersionUID=1L;
    public static final int MAX_STEPS=256;
    private final RelativeSimplicialComplex source,target;
    private final List<SimplicialCollapse> steps;

    public SimplicialCollapseSequence(RelativeSimplicialComplex source,List<SimplicialCollapse> steps) {
        this(source,steps,new Computation());
    }
    private SimplicialCollapseSequence(RelativeSimplicialComplex source,List<SimplicialCollapse> steps,Computation work) {
        this.source=Objects.requireNonNull(source); Objects.requireNonNull(steps); requireSize(steps.size());
        List<SimplicialCollapse> copy=new ArrayList<>(steps); RelativeSimplicialComplex current=source;
        for(SimplicialCollapse step : copy) {
            Objects.requireNonNull(step); samePair(current,step.source(),work); current=step.target();
        }
        this.steps=Collections.unmodifiableList(copy); target=current;
    }
    private static void requireSize(int size) {
        if(size>MAX_STEPS) throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Collapse sequences allow at most 256 steps");
    }
    private static void samePair(RelativeSimplicialComplex a,RelativeSimplicialComplex b,Computation work) {
        for(RelativeSimplicialComplex pair : Arrays.asList(a,b)) {
            for(FiniteSet<Integer> face : pair.ambient().faces()) work.use(1L+face.size());
            for(FiniteSet<Integer> face : pair.subcomplex().faces()) work.use(1L+face.size());
        }
        if(!a.equals(b)) throw MathFailure.undefined("Collapse sequences require identical full labelled joining pairs");
    }
    public static SimplicialCollapseSequence identity(RelativeSimplicialComplex pair) { return new SimplicialCollapseSequence(pair,Collections.emptyList()); }
    public static SimplicialCollapseSequence fromCollapse(SimplicialCollapse step) { return new SimplicialCollapseSequence(step.source(),Collections.singletonList(step)); }
    public static SimplicialCollapseSequence reduce(RelativeSimplicialComplex source) {
        Computation work=new Computation(); RelativeSimplicialComplex current=source; List<SimplicialCollapse> steps=new ArrayList<>();
        while(true) {
            List<FiniteSet<BigInteger>> faces=SimplicialCollapse.freeFaces(current,work); if(faces.isEmpty()) break;
            requireSize(steps.size()+1); SimplicialCollapse step=new SimplicialCollapse(current,faces.get(0),work); steps.add(step); current=step.target();
        }
        return new SimplicialCollapseSequence(source,steps,work);
    }
    public static SimplicialCollapseSequence reduceAbsolute(FiniteSimplicialComplex source) { return reduce(RelativeSimplicialComplex.absolute(source)); }
    public SimplicialCollapseSequence append(SimplicialCollapse step) {
        Computation work=new Computation(); samePair(target,step.source(),work); requireSize(steps.size()+1);
        List<SimplicialCollapse> result=new ArrayList<>(steps); result.add(step); return new SimplicialCollapseSequence(source,result,work);
    }
    /** Chronological concatenation: execute this sequence, then after. */
    public SimplicialCollapseSequence then(SimplicialCollapseSequence after) {
        Computation work=new Computation(); samePair(target,after.source,work); requireSize(steps.size()+after.steps.size());
        List<SimplicialCollapse> result=new ArrayList<>(steps); result.addAll(after.steps); return new SimplicialCollapseSequence(source,result,work);
    }
    public RelativeSimplicialComplex source() { return source; }
    public RelativeSimplicialComplex target() { return target; }
    public List<SimplicialCollapse> steps() { return steps; }
    public List<RelativeSimplicialComplex> stages() {
        List<RelativeSimplicialComplex> result=new ArrayList<>(); result.add(source); for(SimplicialCollapse step : steps) result.add(step.target()); return Collections.unmodifiableList(result);
    }
    public BigInteger stepCount() { return BigInteger.valueOf(steps.size()); }
    public boolean isTerminal() { return !SimplicialCollapse.hasFreeFace(target); }
    public RelativeSimplicialMap inclusion() { return RelativeSimplicialMap.inclusion(target,source); }
    private static void requireDegree(BigInteger degree) { if(degree.signum()<0) throw MathFailure.undefined("Collapse-sequence matrix and integral map degrees must be nonnegative"); }
    private static IntegerMatrix identity(int size,Computation work) { work.use((long)size*size); return IntegerMatrix.identity(size); }
    private IntegerMatrix retraction(BigInteger degree,Computation work) {
        if(steps.isEmpty()) return identity(source.basis(degree).size(),work);
        IntegerMatrix result=steps.get(0).retraction(degree,work);
        for(int k=1;k<steps.size();k++) result=work.multiply(steps.get(k).retraction(degree,work),result); return result;
    }
    private IntegerMatrix inclusion(BigInteger degree,Computation work) { return RelativeSimplicialComplex.selector(source.basis(degree),target.basis(degree),work); }
    /** H_total = H_1 + i_1 H_2 R_1 + i_1 i_2 H_3 R_2 R_1 + ... . */
    private IntegerMatrix homotopy(BigInteger degree,Computation work) {
        List<FiniteSet<Integer>> rows=source.basis(degree.add(BigInteger.ONE)); int columns=source.basis(degree).size();
        if(steps.isEmpty()) { work.use((long)rows.size()*columns); return IntegerMatrix.zero(rows.size(),columns); }
        IntegerMatrix result=steps.get(0).homotopy(degree,work),prefix=null;
        for(int k=1;k<steps.size();k++) {
            SimplicialCollapse step=steps.get(k); IntegerMatrix previous=steps.get(k-1).retraction(degree,work);
            prefix=prefix==null?previous:work.multiply(previous,prefix);
            IntegerMatrix intoSource=RelativeSimplicialComplex.selector(rows,step.source().basis(degree.add(BigInteger.ONE)),work);
            IntegerMatrix term=work.multiply(intoSource,work.multiply(step.homotopy(degree,work),prefix));
            work.use((long)rows.size()*columns); result=result.add(term);
        }
        return result;
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
    @Override public boolean equals(Object other) { return other instanceof SimplicialCollapseSequence && source.equals(((SimplicialCollapseSequence)other).source) && steps.equals(((SimplicialCollapseSequence)other).steps); }
    @Override public int hashCode() { return Objects.hash(source,steps); }
    @Override public String toString() { return "CollapseSequence(source="+source+", steps="+steps+")"; }
}
