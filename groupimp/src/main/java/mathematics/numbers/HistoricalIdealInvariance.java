package mathematics.numbers;

import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Exact orthogonal projector onto the 16-dimensional Witt occupation span and the
 * residuals of the historical octonion-stabilizer and quaternion-adjoint matrices.
 * A nonzero residual is an invariance failure. It is not replaced by a projection.
 */
public final class HistoricalIdealInvariance implements Serializable {
    private static final long serialVersionUID=1L;
    private static final HistoricalIdealInvariance MODEL=compute();
    private final RationalMatrix basis;
    private final RationalMatrix gramInverse;
    private final List<GeneratorResidual> residuals;

    public static final class GeneratorResidual implements Serializable {
        private static final long serialVersionUID=1L;
        private final String family;
        private final int index;
        private final RationalMatrix residual;
        private final int rank;
        private final Rational frobeniusSquared;
        private GeneratorResidual(String family,int index,RationalMatrix residual){
            this.family=family;this.index=index;this.residual=residual;this.rank=residual.rank();this.frobeniusSquared=frobenius(residual);
        }
        public String family(){return family;}
        public int index(){return index;}
        public RationalMatrix residual(){return residual;}
        public int rank(){return rank;}
        public Rational frobeniusSquared(){return frobeniusSquared;}
        public boolean invariant(){return frobeniusSquared.equals(Rational.ZERO);}
    }

    public static final class Counterexample implements Serializable {
        private static final long serialVersionUID=1L;
        public final String family;
        public final int generatorIndex;
        public final int stateIndex;
        public final String stateName;
        public final int coordinate;
        public final Rational value;
        public Counterexample(String family,int generatorIndex,int stateIndex,String stateName,int coordinate,Rational value){
            this.family=family;this.generatorIndex=generatorIndex;this.stateIndex=stateIndex;this.stateName=stateName;this.coordinate=coordinate;this.value=value;
        }
        @Override public String toString(){
            return family+"["+generatorIndex+"] on "+stateName+" coordinate "+coordinate+" = "+value;
        }
    }

    private HistoricalIdealInvariance(RationalMatrix basis,RationalMatrix gramInverse,List<GeneratorResidual> residuals){
        this.basis=basis;this.gramInverse=gramInverse;this.residuals=Collections.unmodifiableList(residuals);
    }
    public static HistoricalIdealInvariance model(){return MODEL;}
    public RationalMatrix basis(){return basis;}
    public RationalMatrix gramInverse(){return gramInverse;}
    public List<GeneratorResidual> residuals(){return residuals;}
    public int invariantGeneratorCount(){
        int count=0; for(GeneratorResidual residual:residuals) if(residual.invariant()) count++; return count;
    }
    public Rational totalFrobeniusSquared(){
        Rational sum=Rational.ZERO; for(GeneratorResidual residual:residuals) sum=sum.add(residual.frobeniusSquared()); return sum;
    }

    /** P v = B (B^T B)^{-1} B^T v. */
    public RationalVector project(RationalVector value){
        return basis.multiply(gramInverse.multiply(basis.transpose().multiply(value)));
    }
    public boolean projectorFixesBasis(){
        for(int column=0;column<16;column++) if(!project(basis.column(column)).equals(basis.column(column))) return false;
        return true;
    }
    /** Exact on the standard basis of Q^128, hence P^2=P as an operator. */
    public boolean projectorIsIdempotentOnCoordinates(){
        for(int index=0;index<128;index++){
            RationalVector axis=axis(index);
            if(!project(project(axis)).equals(project(axis))) return false;
        }
        return true;
    }
    public boolean gramInverseIsExact(){
        RationalMatrix gram=basis.transpose().multiply(basis);
        return gram.multiply(gramInverse).equals(RationalMatrix.identity(16));
    }

    public Counterexample firstCounterexample(){
        for(GeneratorResidual residual:residuals) if(!residual.invariant()){
            RationalMatrix matrix=residual.residual();
            for(int column=0;column<matrix.columns();column++) for(int row=0;row<matrix.rows();row++)
                if(matrix.get(row,column).signum()!=0)
                    return new Counterexample(residual.family(),residual.index(),column,
                            DixonMinimalLeftIdeal.model().states().get(column).name(),row,matrix.get(row,column));
        }
        return null;
    }

    private static HistoricalIdealInvariance compute(){
        RationalMatrix basis=columns(DixonMinimalLeftIdeal.model());
        RationalMatrix inverse=basis.transpose().multiply(basis).inverse();
        List<GeneratorResidual> residuals=new ArrayList<GeneratorResidual>();
        List<RationalMatrix> color=RationalOctonionDerivations.su3StabilizerCandidates();
        for(int index=0;index<color.size();index++) residuals.add(new GeneratorResidual("octonion-stabilizer",index,residual(basis,inverse,color.get(index),true)));
        List<RationalMatrix> weak=RationalOctonionDerivations.quaternionAdjointGenerators();
        for(int index=0;index<weak.size();index++) residuals.add(new GeneratorResidual("quaternion-adjoint",index,residual(basis,inverse,weak.get(index),false)));
        return new HistoricalIdealInvariance(basis,inverse,residuals);
    }
    private static RationalMatrix residual(RationalMatrix basis,RationalMatrix inverse,RationalMatrix generator,boolean octonion){
        Rational[][] rows=new Rational[128][16];
        for(int column=0;column<16;column++){
            RationalVector image=octonion?WittGaugeAction.applyOctonion(basis.column(column),generator):WittGaugeAction.applyQuaternion(basis.column(column),generator);
            RationalVector orthogonal=image.add(basis.multiply(inverse.multiply(basis.transpose().multiply(image))).scale(Rational.ONE.negate()));
            for(int row=0;row<128;row++) rows[row][column]=orthogonal.get(row);
        }
        return new RationalMatrix(rows);
    }
    private static RationalMatrix columns(DixonMinimalLeftIdeal ideal){
        Rational[][] rows=new Rational[128][16];
        for(int column=0;column<16;column++) for(int row=0;row<128;row++) rows[row][column]=ideal.states().get(column).vector().toVector().get(row);
        return new RationalMatrix(rows);
    }
    private static RationalVector axis(int index){
        Rational[] values=new Rational[128];
        for(int i=0;i<128;i++) values[i]=i==index?Rational.ONE:Rational.ZERO;
        return new RationalVector(values);
    }
    private static Rational frobenius(RationalMatrix matrix){
        Rational sum=Rational.ZERO;
        for(int row=0;row<matrix.rows();row++) for(int column=0;column<matrix.columns();column++){
            Rational value=matrix.get(row,column); sum=sum.add(value.multiply(value));
        }
        return sum;
    }
}
