package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.linear.IntegerMatrix;
import mathematics.linear.IntegerVector;
import mathematics.linear.IntegerSmithNormalForm.Computation;
import mathematics.structures.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Constructive ker(outgoing)/im(incoming) for two consecutive differentials over Z. */
public final class IntegralHomology implements Serializable {
    private static final long serialVersionUID=1L;
    private final IntegerMatrix outgoing,incoming,cycles;
    private final PresentedAbelianGroup group;
    public IntegralHomology(IntegerMatrix outgoing,IntegerMatrix incoming) {
        this(outgoing,incoming,new Computation());
    }
    /** Share reductions and products with a larger homology-map calculation. */
    public IntegralHomology(IntegerMatrix outgoing,IntegerMatrix incoming,Computation work) {
        this.outgoing=Objects.requireNonNull(outgoing); this.incoming=Objects.requireNonNull(incoming);
        if(outgoing.columns()!=incoming.rows()) throw MathFailure.undefined("Consecutive boundaries must share the middle chain dimension");
        if(!work.multiply(outgoing,incoming).equals(IntegerMatrix.zero(outgoing.rows(),incoming.columns())))
            throw MathFailure.undefined("Consecutive boundaries must compose to zero");
        cycles=work.kernelMatrix(outgoing);
        group=new PresentedAbelianGroup(work.solve(cycles,incoming),work);
    }
    public static IntegralHomology atDegree(FiniteSimplicialComplex complex,BigInteger degree) {
        return atDegree(complex,degree,new Computation());
    }
    public static IntegralHomology atDegree(FiniteSimplicialComplex complex,BigInteger degree,Computation work) {
        if(degree.signum()<0) throw MathFailure.undefined("Homology degree must be nonnegative");
        return new IntegralHomology(complex.integralBoundaryMatrix(degree),complex.integralBoundaryMatrix(degree.add(BigInteger.ONE)),work);
    }
    public IntegerMatrix outgoingBoundary() { return outgoing; }
    public IntegerMatrix incomingBoundary() { return incoming; }
    /** Columns form an integral basis of the entire cycle lattice in the retained chain coordinates. */
    public IntegerMatrix cycleMatrix() { return cycles; }
    public IntegerMatrix boundaryCoordinates() { return group.relations(); }
    public PresentedAbelianGroup group() { return group; }
    public AbelianGroupType type() { return group.type(); }
    public int chainRank() { return outgoing.columns(); }
    public int cycleRank() { return cycles.columns(); }
    public int boundaryRank() { return cycleRank()-type().freeRank().intValueExact(); }
    public boolean isAcyclic() { return type().equals(AbelianGroupType.ZERO); }
    public List<IntegerVector> cycleBasis() { return cycles.columnVectors(); }
    public List<IntegerVector> boundaryBasis() { return incoming.imageBasis(); }
    /** Representatives of minimal Smith generators: nontrivial torsion first, then free generators. */
    public List<IntegerVector> generators() {
        Computation work=new Computation();
        IntegerMatrix representatives=work.multiply(cycles,work.inverseUnimodular(group.smithCoordinateMap()));
        List<IntegerVector> result=new ArrayList<>();
        for(AbelianGroupElement generator : group.smithGenerators()) {
            for(int i=0;i<cycleRank();i++) if(generator.smithCoordinates().get(i).equals(BigInteger.ONE)) {
                work.use(chainRank()); result.add(representatives.column(i)); break;
            }
        }
        return Collections.unmodifiableList(result);
    }
    public boolean isCycle(IntegerVector chain) { return outgoing.multiply(chain).equals(IntegerVector.zero(outgoing.rows())); }
    public boolean isBoundary(IntegerVector chain) { return new Computation().hasSolution(incoming,chain); }
    public IntegerVector cycleCoordinates(IntegerVector cycle) { return new Computation().solve(cycles,cycle); }
    public IntegerVector fromCycleCoordinates(IntegerVector coordinates) { return cycles.multiply(coordinates); }
    public AbelianGroupElement classOf(IntegerVector cycle) {
        Computation work=new Computation();
        return group.fromSmith(work.apply(group.smithCoordinateMap(),work.solve(cycles,cycle)));
    }
    /** One cycle representative; this is a set-theoretic section, generally not an additive map. */
    public IntegerVector representative(AbelianGroupElement element) {
        if(!group.equals(element.group())) throw MathFailure.undefined("The class must belong to this retained homology presentation");
        Computation work=new Computation();
        return work.apply(cycles,work.solve(group.smithCoordinateMap(),element.smithCoordinates()));
    }
    public IntegerVector boundingChain(IntegerVector boundary) { return new Computation().solve(incoming,boundary); }
    /** Quotient map from the free cycle-coordinate group onto the presented homology group. */
    public AbelianGroupHomomorphism projection() {
        Computation work=new Computation();
        PresentedAbelianGroup freeCycles=new PresentedAbelianGroup(IntegerMatrix.zero(cycleRank(),0),work);
        return AbelianGroupHomomorphism.fromMatrix(freeCycles,group,IntegerMatrix.identity(cycleRank()),work);
    }
    /** The given degree matrix must preserve cycles and boundaries; adjacent chain-map data is not inferred. */
    public AbelianGroupHomomorphism inducedMap(IntegralHomology target,IntegerMatrix degreeMap) {
        return inducedMap(target,degreeMap,new Computation());
    }
    public AbelianGroupHomomorphism inducedMap(IntegralHomology target,IntegerMatrix degreeMap,Computation work) {
        if(degreeMap.columns()!=chainRank() || degreeMap.rows()!=target.chainRank())
            throw MathFailure.undefined("The degree map must have target chain rows and source chain columns");
        // Integral solvability checks cycle preservation; the quotient map constructor checks boundary preservation.
        IntegerMatrix onCycles=work.solve(target.cycles,work.multiply(degreeMap,cycles));
        return AbelianGroupHomomorphism.fromMatrix(group,target.group,onCycles,work);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof IntegralHomology)) return false; IntegralHomology homology=(IntegralHomology)other;
        return outgoing.equals(homology.outgoing) && incoming.equals(homology.incoming);
    }
    @Override public int hashCode() { return Objects.hash(outgoing,incoming); }
    @Override public String toString() { return "IntegralHomology(outgoing="+outgoing+", incoming="+incoming+")"; }
}
