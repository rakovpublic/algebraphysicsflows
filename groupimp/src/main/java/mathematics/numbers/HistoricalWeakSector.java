package mathematics.numbers;

import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Exact analysis of the three historical quaternion-adjoint generators and of the
 * alternative left-multiplication action on the same quaternion factor.
 *
 * <p>The adjoint action ad(H) kills the center and acts on the three-dimensional
 * imaginary quaternions. Its Casimir spectrum on H is {0, -8}, the 1+3 decomposition.
 * The left action is the real four-dimensional spinor with Casimir -3. The two
 * spectra are disjoint, so the adjoint action contains no doublet. Neither action
 * preserves the two-dimensional plane span_R{1,j}; both invariant closures have
 * real dimension 32.</p>
 */
public final class HistoricalWeakSector implements Serializable {
    private static final long serialVersionUID=1L;
    private static final HistoricalWeakSector MODEL=compute();
    private final List<RationalMatrix> adjointGenerators;
    private final List<RationalMatrix> leftGenerators;
    private final int commonKernelDimension;
    private final int adjointImageDimension;
    private final RationalMatrix adjointCasimir;
    private final RationalMatrix leftCasimir;
    private final int adjointClosureDimension;
    private final int leftClosureDimension;
    private final int proposedModuleComplexDimension;
    private final Rational adjointClosureCasimirTrace;
    private final Rational leftClosureCasimirTrace;

    private HistoricalWeakSector(List<RationalMatrix> adjointGenerators,List<RationalMatrix> leftGenerators,
            int commonKernelDimension,int adjointImageDimension,RationalMatrix adjointCasimir,RationalMatrix leftCasimir,
            int adjointClosureDimension,int leftClosureDimension,int proposedModuleComplexDimension,
            Rational adjointClosureCasimirTrace,Rational leftClosureCasimirTrace){
        this.adjointGenerators=Collections.unmodifiableList(adjointGenerators);
        this.leftGenerators=Collections.unmodifiableList(leftGenerators);
        this.commonKernelDimension=commonKernelDimension;
        this.adjointImageDimension=adjointImageDimension;
        this.adjointCasimir=adjointCasimir;
        this.leftCasimir=leftCasimir;
        this.adjointClosureDimension=adjointClosureDimension;
        this.leftClosureDimension=leftClosureDimension;
        this.proposedModuleComplexDimension=proposedModuleComplexDimension;
        this.adjointClosureCasimirTrace=adjointClosureCasimirTrace;
        this.leftClosureCasimirTrace=leftClosureCasimirTrace;
    }

    public static HistoricalWeakSector model(){return MODEL;}
    public List<RationalMatrix> adjointGenerators(){return adjointGenerators;}
    public List<RationalMatrix> leftGenerators(){return leftGenerators;}
    public int commonKernelDimension(){return commonKernelDimension;}
    public int adjointImageDimension(){return adjointImageDimension;}
    public RationalMatrix adjointCasimir(){return adjointCasimir;}
    public RationalMatrix leftCasimir(){return leftCasimir;}
    public List<Rational> adjointCasimirEigenvalues(){return adjointCasimir.rationalEigenvalues();}
    public List<Rational> leftCasimirEigenvalues(){return leftCasimir.rationalEigenvalues();}
    public int adjointClosureDimension(){return adjointClosureDimension;}
    public int leftClosureDimension(){return leftClosureDimension;}
    /** Complex dimension of the proposed occupation module under the central i_C. */
    public int proposedModuleComplexDimension(){return proposedModuleComplexDimension;}
    /** Trace of the adjoint Casimir on its 32-real-dimensional invariant closure. */
    public Rational adjointClosureCasimirTrace(){return adjointClosureCasimirTrace;}
    /** Trace of the left-action Casimir on its 32-real-dimensional invariant closure. */
    public Rational leftClosureCasimirTrace(){return leftClosureCasimirTrace;}
    /**
     * Dimension of the singlet part of the adjoint closure. The adjoint Casimir is 0 on
     * singlets and -8 on each triplet, so trace = -8 * (triplet dimension).
     */
    public int adjointClosureSingletDimension(){
        return adjointClosureDimension+adjointClosureCasimirTrace.divide(Rational.of(8)).numerator().intValueExact();
    }
    public int adjointClosureTripletDimension(){
        return adjointClosureCasimirTrace.divide(Rational.of(-8)).numerator().intValueExact();
    }
    /** Dimension of the doublet part of the left closure. The left Casimir is -3 on every real component. */
    public int leftClosureDoubletDimension(){
        return leftClosureCasimirTrace.divide(Rational.of(-3)).numerator().intValueExact();
    }

    /** The adjoint Casimir has eigenvalues 0 (center) and -8 (imaginary quaternions): no spin-1/2. */
    public boolean adjointContainsDoublet(){
        return adjointCasimir.eigenvalueMultiplicity(Rational.of(-3))>0;
    }
    /** The left action has the spin-1/2 Casimir -3 on all four real components. */
    public boolean leftContainsDoublet(){
        return leftCasimir.eigenvalueMultiplicity(Rational.of(-3))>0;
    }
    /** The left action is a single real four-dimensional spinor, so it has no trivial summand. */
    public boolean leftContainsSinglet(){
        return leftCasimir.eigenvalueMultiplicity(Rational.ZERO)>0;
    }
    /** Neither action preserves the proposed two-real-dimensional weak plane. */
    public boolean proposedPlaneInvariant(){
        for(HistoricalIdealInvariance.GeneratorResidual residual:HistoricalIdealInvariance.model().residuals())
            if(residual.family().equals("quaternion-adjoint")&&!residual.invariant()) return false;
        return true;
    }

    private static HistoricalWeakSector compute(){
        List<RationalMatrix> adjoint=RationalOctonionDerivations.quaternionAdjointGenerators();
        List<RationalMatrix> left=leftMultiplicationGenerators();
        RationalMatrix stacked=new RationalMatrix(stack(adjoint,4));
        int kernel=stacked.nullspace().size();
        int image=flatten(adjoint).rank();
        RationalMatrix adjointCasimir=casimir(adjoint);
        RationalMatrix leftCasimir=casimir(left);
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        List<RationalVector> adjointBasis=closureBasis(invariance.basis(),adjoint);
        List<RationalVector> leftBasis=closureBasis(invariance.basis(),left);
        int adjointClosure=adjointBasis.size();
        int leftClosure=leftBasis.size();
        Rational adjointTrace=restrictedCasimirTrace(adjointBasis,adjoint);
        Rational leftTrace=restrictedCasimirTrace(leftBasis,left);
        return new HistoricalWeakSector(adjoint,left,kernel,image,adjointCasimir,leftCasimir,
                adjointClosure,leftClosure,invariance.complexDimension(),adjointTrace,leftTrace);
    }

    private static List<RationalMatrix> leftMultiplicationGenerators(){
        List<RationalMatrix> result=new ArrayList<RationalMatrix>();
        RationalQuaternion[] units={RationalQuaternion.I,RationalQuaternion.J,RationalQuaternion.K};
        for(RationalQuaternion unit:units){
            Rational[][] entries=new Rational[4][4];
            for(int column=0;column<4;column++){
                Rational[] values={Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO};
                values[column]=Rational.ONE;
                RationalQuaternion product=unit.multiply(new RationalQuaternion(values[0],values[1],values[2],values[3]));
                Rational[] image={product.real,product.i,product.j,product.k};
                for(int row=0;row<4;row++) entries[row][column]=image[row];
            }
            result.add(new RationalMatrix(entries));
        }
        return result;
    }
    private static RationalMatrix casimir(List<RationalMatrix> generators){
        int size=generators.get(0).rows();
        RationalMatrix result=zero(size);
        for(RationalMatrix generator:generators) result=result.add(generator.multiply(generator));
        return result;
    }
    private static Rational[][] stack(List<RationalMatrix> generators,int size){
        Rational[][] rows=new Rational[generators.size()*size][size];
        for(int index=0;index<generators.size();index++) for(int row=0;row<size;row++) for(int column=0;column<size;column++)
            rows[index*size+row][column]=generators.get(index).get(row,column);
        return rows;
    }
    private static RationalMatrix flatten(List<RationalMatrix> generators){
        int size=generators.get(0).rows();
        Rational[][] rows=new Rational[size*size][generators.size()];
        for(int index=0;index<generators.size();index++) for(int row=0;row<size;row++) for(int column=0;column<size;column++)
            rows[row*size+column][index]=generators.get(index).get(row,column);
        return new RationalMatrix(rows);
    }
    private static int closureDimension(RationalMatrix basis,List<RationalMatrix> generators){
        return closureBasis(basis,generators).size();
    }
    /** An exact independent spanning set of the smallest invariant subspace containing the ideal. */
    private static List<RationalVector> closureBasis(RationalMatrix basis,List<RationalMatrix> generators){
        List<RationalVector> vectors=new ArrayList<RationalVector>();
        for(int column=0;column<basis.columns();column++) vectors.add(basis.column(column));
        vectors=independent(vectors);
        int rank=vectors.size();
        boolean changed=true;
        while(changed){
            changed=false;
            List<RationalVector> all=new ArrayList<RationalVector>(vectors);
            for(RationalVector vector:vectors) for(RationalMatrix generator:generators)
                all.add(WittGaugeAction.applyQuaternion(vector,generator));
            List<RationalVector> reduced=independent(all);
            if(reduced.size()>rank){vectors=reduced;rank=reduced.size();changed=true;}
        }
        return vectors;
    }
    /**
     * Trace of the Casimir sum_g D_g^2 on the invariant subspace, where D_g is the exact
     * restriction of the lift of g to the supplied basis. The trace fixes the multiplicity
     * decomposition because every irreducible summand has a known Casimir eigenvalue.
     */
    private static Rational restrictedCasimirTrace(List<RationalVector> basisVectors,List<RationalMatrix> generators){
        int size=basisVectors.size();
        Rational[][] columns=new Rational[128][size];
        for(int column=0;column<size;column++) for(int row=0;row<128;row++)
            columns[row][column]=basisVectors.get(column).get(row);
        RationalMatrix basis=new RationalMatrix(columns);
        RationalMatrix dual=basis.transpose().multiply(basis).inverse().multiply(basis.transpose());
        Rational trace=Rational.ZERO;
        for(RationalMatrix generator:generators){
            Rational[][] restricted=new Rational[size][size];
            for(int column=0;column<size;column++){
                RationalVector image=WittGaugeAction.applyQuaternion(basisVectors.get(column),generator);
                RationalVector coordinates=dual.multiply(image);
                for(int row=0;row<size;row++) restricted[row][column]=coordinates.get(row);
            }
            RationalMatrix matrix=new RationalMatrix(restricted);
            trace=trace.add(matrix.multiply(matrix).trace());
        }
        return trace;
    }
    private static List<RationalVector> independent(List<RationalVector> vectors){
        List<RationalVector> result=new ArrayList<RationalVector>();
        int rank=0;
        for(RationalVector vector:vectors){
            List<RationalVector> trial=new ArrayList<RationalVector>(result);
            trial.add(vector);
            int next=spanRank(trial);
            if(next>rank){result=trial;rank=next;}
        }
        return result;
    }
    private static int spanRank(List<RationalVector> vectors){
        Rational[][] rows=new Rational[128][vectors.size()];
        for(int column=0;column<vectors.size();column++) for(int row=0;row<128;row++)
            rows[row][column]=vectors.get(column).get(row);
        return new RationalMatrix(rows).rank();
    }
    private static RationalMatrix zero(int size){
        Rational[][] rows=new Rational[size][size];
        for(int row=0;row<size;row++) Arrays.fill(rows[row],Rational.ZERO);
        return new RationalMatrix(rows);
    }
}
