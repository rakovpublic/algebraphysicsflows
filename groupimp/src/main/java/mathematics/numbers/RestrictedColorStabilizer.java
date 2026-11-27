package mathematics.numbers;

import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The eight historical octonion-stabilizer matrices, restricted to the occupation ideal.
 * Restriction is defined only because those matrices preserve the column span.
 * This is not a substitution of Gell-Mann matrices.
 *
 * <p>The class derives, from the restricted matrices themselves: linear independence,
 * closure and exact structure constants, the Jacobi identity, the trace form and its
 * compactness, the Casimir eigenvalues and multiplicities, the two-copy isomorphism
 * with the octonion module, the commutant dimension, the physical complex dimension,
 * and whether the occupation-dictionary hypercharge is color invariant.</p>
 */
public final class RestrictedColorStabilizer implements Serializable {
    private static final long serialVersionUID=1L;
    private static final RestrictedColorStabilizer MODEL=compute();
    private final List<RationalMatrix> generators;
    private final int independentCount;
    private final int commutatorsOutsideSpan;
    private final boolean jacobi;
    private final RationalMatrix casimir;
    private final RationalMatrix octonionCasimir;
    private final Rational[][][] structureConstants;
    private final RationalMatrix traceForm;
    private final boolean compact;
    private final boolean twoCopiesOfOctonionModule;
    private final int commutantDimension;
    private final int octonionCommutantDimension;
    private final int fixedSubspaceDimension;
    private final int centerDimension;
    private final int rank;
    private final int complexCentralizerDimension;
    private final int complexStructureRank;
    private final int hyperchargeNonCommutingCount;
    private final boolean cubicAnomalyVanishes;

    private RestrictedColorStabilizer(List<RationalMatrix> generators,int independentCount,int commutatorsOutsideSpan,
            boolean jacobi,RationalMatrix casimir,RationalMatrix octonionCasimir,Rational[][][] structureConstants,RationalMatrix traceForm,
            boolean compact,boolean twoCopiesOfOctonionModule,int commutantDimension,int octonionCommutantDimension,
            int fixedSubspaceDimension,int centerDimension,int rank,int complexCentralizerDimension,
            int complexStructureRank,int hyperchargeNonCommutingCount,boolean cubicAnomalyVanishes){
        this.generators=Collections.unmodifiableList(generators);
        this.independentCount=independentCount;
        this.commutatorsOutsideSpan=commutatorsOutsideSpan;
        this.jacobi=jacobi;
        this.casimir=casimir;
        this.octonionCasimir=octonionCasimir;
        this.structureConstants=structureConstants;
        this.traceForm=traceForm;
        this.compact=compact;
        this.twoCopiesOfOctonionModule=twoCopiesOfOctonionModule;
        this.commutantDimension=commutantDimension;
        this.octonionCommutantDimension=octonionCommutantDimension;
        this.fixedSubspaceDimension=fixedSubspaceDimension;
        this.centerDimension=centerDimension;
        this.rank=rank;
        this.complexCentralizerDimension=complexCentralizerDimension;
        this.complexStructureRank=complexStructureRank;
        this.hyperchargeNonCommutingCount=hyperchargeNonCommutingCount;
        this.cubicAnomalyVanishes=cubicAnomalyVanishes;
    }

    public static RestrictedColorStabilizer model(){return MODEL;}
    public List<RationalMatrix> generators(){return generators;}
    public int independentCount(){return independentCount;}
    public int commutatorsOutsideSpan(){return commutatorsOutsideSpan;}
    public boolean closed(){return commutatorsOutsideSpan==0;}
    public boolean jacobi(){return jacobi;}
    public RationalMatrix casimir(){return casimir;}
    public List<Rational> casimirEigenvalues(){return casimir.rationalEigenvalues();}
    /** Casimir computed on the 8-dimensional octonion module, where the fundamental has 4/3. */
    public RationalMatrix octonionCasimir(){return octonionCasimir;}
    public List<Rational> octonionCasimirEigenvalues(){return octonionCasimir.rationalEigenvalues();}
    public int casimirMultiplicity(Rational eigenvalue){return casimir.eigenvalueMultiplicity(eigenvalue);}
    public Rational[][][] structureConstants(){return structureConstants;}
    public RationalMatrix traceForm(){return traceForm;}
    /** Negative definiteness of the trace form is the compact real form condition. */
    public boolean compact(){return compact;}
    /** True when, in the adapted basis, every restricted matrix is block diagonal with two equal octonion blocks. */
    public boolean twoCopiesOfOctonionModule(){return twoCopiesOfOctonionModule;}
    /** Dimension of the real commutant of the restricted representation. */
    public int commutantDimension(){return commutantDimension;}
    /** Dimension of the real commutant of the eight-dimensional octonion representation. */
    public int octonionCommutantDimension(){return octonionCommutantDimension;}
    /** Common fixed subspace of the eight derivations on the octonion module. */
    public int fixedSubspaceDimension(){return fixedSubspaceDimension;}
    /** Center of the derived Lie algebra. */
    public int centerDimension(){return centerDimension;}
    /** Rank of the derived Lie algebra, computed from the centralizer of a generic element. */
    public int rank(){return rank;}
    /** Dimension of the real centralizer of the physical complex structure inside the commutant. */
    public int complexCentralizerDimension(){return complexCentralizerDimension;}
    /** Rank of the coordinate matrix of the central complex structure on the ideal. */
    public int complexStructureRank(){return complexStructureRank;}
    /** Number of the eight generators that fail to commute with the occupation-dictionary hypercharge. */
    public int hyperchargeNonCommutingCount(){return hyperchargeNonCommutingCount;}
    /** The symmetrized cubic color anomaly Tr(T_a{T_b,T_c}) vanishes identically. */
    public boolean cubicAnomalyVanishes(){return cubicAnomalyVanishes;}

    private static RestrictedColorStabilizer compute(){
        HistoricalIdealInvariance invariance=HistoricalIdealInvariance.model();
        RationalMatrix basis=invariance.basis();
        RationalMatrix inverse=invariance.gramInverse();
        List<RationalMatrix> historical=RationalOctonionDerivations.su3StabilizerCandidates();
        List<RationalMatrix> restricted=new ArrayList<RationalMatrix>();
        for(RationalMatrix generator:historical) restricted.add(restrict(basis,inverse,generator));
        int outside=0;
        RationalMatrix span=flatten(restricted);
        for(int i=0;i<restricted.size();i++) for(int j=i+1;j<restricted.size();j++){
            RationalMatrix bracket=commutator(restricted.get(i),restricted.get(j));
            if(spanWith(span,bracket).rank()!=span.rank()) outside++;
        }
        boolean jacobi=true;
        for(int i=0;i<restricted.size()&&jacobi;i++) for(int j=i+1;j<restricted.size()&&jacobi;j++) for(int k=j+1;k<restricted.size();k++){
            RationalMatrix left=commutator(commutator(restricted.get(i),restricted.get(j)),restricted.get(k));
            RationalMatrix middle=commutator(commutator(restricted.get(j),restricted.get(k)),restricted.get(i));
            RationalMatrix right=commutator(commutator(restricted.get(k),restricted.get(i)),restricted.get(j));
            if(!left.add(middle).add(right).equals(zero(16))) { jacobi=false; break; }
        }
        RationalMatrix trace=traceForm(restricted);
        RationalMatrix traceInverse=trace.inverse();
        RationalMatrix casimir=zero(16);
        for(int a=0;a<restricted.size();a++) for(int b=0;b<restricted.size();b++)
            casimir=casimir.add(restricted.get(a).multiply(restricted.get(b)).scale(traceInverse.get(a,b)));
        RationalMatrix octonionCasimir=casimirOf(historical);
        Rational[][][] f=structureConstants(restricted);
        boolean twoCopies=twoCopiesOfOctonionModule(historical);
        int commutant=commutantDimension(restricted,16);
        int octonionCommutant=commutantDimension(historical,8);
        int fixed=fixedSubspaceDimension(historical);
        int center=centerDimension(f);
        int rank=rank(f);
        RationalMatrix complexCoordinates=invariance.complexStructureCoordinates();
        int complexRank=complexCoordinates.rank();
        int complexCentralizer=commutantWith(restricted,complexCoordinates,16);
        RationalMatrix hypercharge=WittGaugeAction.model().hypercharge();
        int hyperchargeFailures=0;
        for(RationalMatrix generator:restricted) if(!commutator(hypercharge,generator).equals(zero(16))) hyperchargeFailures++;
        boolean anomaly=cubicAnomalyVanishes(restricted);
        return new RestrictedColorStabilizer(restricted,span.rank(),outside,jacobi,casimir,octonionCasimir,f,trace,
                negativeDefinite(trace),twoCopies,commutant,octonionCommutant,fixed,center,rank,complexCentralizer,complexRank,hyperchargeFailures,anomaly);
    }

    private static RationalMatrix restrict(RationalMatrix basis,RationalMatrix inverse,RationalMatrix generator){
        Rational[][] entries=new Rational[16][16];
        for(int column=0;column<16;column++){
            RationalVector image=WittGaugeAction.applyOctonion(basis.column(column),generator);
            RationalVector coordinates=inverse.multiply(basis.transpose().multiply(image));
            for(int row=0;row<16;row++) entries[row][column]=coordinates.get(row);
        }
        return new RationalMatrix(entries);
    }
    private static RationalMatrix flatten(List<RationalMatrix> matrices){
        int size=matrices.get(0).rows();
        Rational[][] rows=new Rational[size*size][matrices.size()];
        for(int index=0;index<matrices.size();index++) for(int row=0;row<size;row++) for(int column=0;column<size;column++)
            rows[row*size+column][index]=matrices.get(index).get(row,column);
        return new RationalMatrix(rows);
    }
    private static RationalMatrix spanWith(RationalMatrix span,RationalMatrix extra){
        int size=16;
        Rational[][] rows=new Rational[size*size][span.columns()+1];
        for(int column=0;column<span.columns();column++) for(int row=0;row<size*size;row++) rows[row][column]=span.get(row,column);
        for(int row=0;row<size;row++) for(int column=0;column<size;column++) rows[row*size+column][span.columns()]=extra.get(row,column);
        return new RationalMatrix(rows);
    }
    private static RationalMatrix commutator(RationalMatrix left,RationalMatrix right){
        return left.multiply(right).add(right.multiply(left).scale(Rational.ONE.negate()));
    }
    private static RationalMatrix zero(int size){
        Rational[][] rows=new Rational[size][size];
        for(int row=0;row<size;row++) for(int column=0;column<size;column++) rows[row][column]=Rational.ZERO;
        return new RationalMatrix(rows);
    }

    private static Rational[][][] structureConstants(List<RationalMatrix> generators){
        int count=generators.size(),size=generators.get(0).rows();
        RationalMatrix span=flatten(generators);
        RationalMatrix solve=span.transpose().multiply(span).inverse().multiply(span.transpose());
        Rational[][][] result=new Rational[count][count][count];
        for(int a=0;a<count;a++) for(int b=0;b<count;b++){
            RationalMatrix bracket=commutator(generators.get(a),generators.get(b));
            Rational[] rhs=new Rational[size*size];
            for(int row=0;row<size;row++) for(int column=0;column<size;column++) rhs[row*size+column]=bracket.get(row,column);
            RationalVector coefficients=solve.multiply(new RationalVector(rhs));
            for(int c=0;c<count;c++) result[a][b][c]=coefficients.get(c);
        }
        return result;
    }
    private static RationalMatrix casimirOf(List<RationalMatrix> generators){
        RationalMatrix trace=traceForm(generators);
        RationalMatrix inverse=trace.inverse();
        RationalMatrix result=zero(generators.get(0).rows());
        for(int a=0;a<generators.size();a++) for(int b=0;b<generators.size();b++)
            result=result.add(generators.get(a).multiply(generators.get(b)).scale(inverse.get(a,b)));
        return result;
    }
    private static RationalMatrix traceForm(List<RationalMatrix> generators){
        int count=generators.size();
        Rational[][] entries=new Rational[count][count];
        for(int a=0;a<count;a++) for(int b=0;b<count;b++) entries[a][b]=generators.get(a).multiply(generators.get(b)).trace();
        return new RationalMatrix(entries);
    }
    private static boolean negativeDefinite(RationalMatrix matrix){
        for(int size=1;size<=matrix.rows();size++){
            Rational[][] block=new Rational[size][size];
            for(int row=0;row<size;row++) for(int column=0;column<size;column++) block[row][column]=matrix.get(row,column);
            int sign=new RationalMatrix(block).determinant().signum();
            int expected=(size%2==0)?1:-1;
            if(sign!=expected) return false;
        }
        return true;
    }
    private static boolean twoCopiesOfOctonionModule(List<RationalMatrix> historical){
        RationalMatrix adapted=adaptedBasis();
        if(adapted.rank()!=16) return false;
        RationalMatrix dual=adapted.transpose().multiply(adapted).inverse().multiply(adapted.transpose());
        for(int index=0;index<historical.size();index++){
            RationalMatrix expected=blockDiagonal(historical.get(index),historical.get(index));
            if(!dual.multiply(WittGaugeAction.liftOctonion(historical.get(index))).multiply(adapted).equals(expected)) return false;
        }
        return true;
    }
    /** Columns are e_c v and j e_c v; the two octonion copies are separated. */
    private static RationalMatrix adaptedBasis(){
        RationalDixonZ2 vacuum=DixonMinimalLeftIdeal.wittVacuum();
        RationalOctonion[] units={RationalOctonion.ONE,RationalOctonion.I,RationalOctonion.J,RationalOctonion.K,
                RationalOctonion.L,RationalOctonion.IL,RationalOctonion.JL,RationalOctonion.KL};
        Rational[][] rows=new Rational[128][16];
        for(int column=0;column<8;column++){
            RationalVector left=RationalDixonZ2.fromOctonion(units[column]).multiply(vacuum).toVector();
            RationalVector right=RationalDixonZ2.fromQuaternion(RationalQuaternion.J)
                    .multiply(RationalDixonZ2.fromOctonion(units[column])).multiply(vacuum).toVector();
            for(int row=0;row<128;row++){rows[row][column]=left.get(row);rows[row][8+column]=right.get(row);}
        }
        return new RationalMatrix(rows);
    }
    private static RationalMatrix blockDiagonal(RationalMatrix left,RationalMatrix right){
        int first=left.rows(),second=right.rows();
        Rational[][] entries=new Rational[first+second][first+second];
        for(int row=0;row<first+second;row++) for(int column=0;column<first+second;column++) entries[row][column]=Rational.ZERO;
        for(int row=0;row<first;row++) for(int column=0;column<first;column++) entries[row][column]=left.get(row,column);
        for(int row=0;row<second;row++) for(int column=0;column<second;column++) entries[first+row][first+column]=right.get(row,column);
        return new RationalMatrix(entries);
    }
    private static int fixedSubspaceDimension(List<RationalMatrix> generators){
        int size=generators.get(0).rows();
        Rational[][] rows=new Rational[generators.size()*size][size];
        for(int index=0;index<generators.size();index++) for(int row=0;row<size;row++) for(int column=0;column<size;column++)
            rows[index*size+row][column]=generators.get(index).get(row,column);
        return new RationalMatrix(rows).nullspace().size();
    }
    private static int centerDimension(Rational[][][] f){
        int count=f.length;
        Rational[][] rows=new Rational[count*count][count];
        for(int a=0;a<count;a++) for(int b=0;b<count;b++) for(int c=0;c<count;c++)
            rows[a*count+b][c]=f[a][b][c];
        return new RationalMatrix(rows).nullspace().size();
    }
    private static int rank(Rational[][][] f){
        int count=f.length;
        Rational[] generic=new Rational[count];
        for(int a=0;a<count;a++) generic[a]=Rational.of(a+1);
        Rational[][] rows=new Rational[count][count];
        for(int c=0;c<count;c++) for(int b=0;b<count;b++){
            Rational sum=Rational.ZERO;
            for(int a=0;a<count;a++) sum=sum.add(generic[a].multiply(f[a][b][c]));
            rows[c][b]=sum;
        }
        return new RationalMatrix(rows).nullspace().size();
    }
    private static int commutantDimension(List<RationalMatrix> generators,int size){
        return commutantWith(generators,null,size);
    }
    private static int commutantWith(List<RationalMatrix> generators,RationalMatrix extra,int size){
        int variables=size*size;
        List<Rational[]> rows=new ArrayList<Rational[]>();
        List<RationalMatrix> all=new ArrayList<RationalMatrix>(generators);
        if(extra!=null) all.add(extra);
        for(RationalMatrix generator:all) for(int row=0;row<size;row++) for(int column=0;column<size;column++){
            Rational[] equation=new Rational[variables];
            Arrays.fill(equation,Rational.ZERO);
            for(int k=0;k<size;k++){
                equation[row*size+k]=equation[row*size+k].add(generator.get(k,column));
                equation[k*size+column]=equation[k*size+column].subtract(generator.get(row,k));
            }
            rows.add(equation);
        }
        return variables-new RationalMatrix(rows.toArray(new Rational[0][])).rank();
    }
    private static boolean cubicAnomalyVanishes(List<RationalMatrix> generators){
        for(int a=0;a<generators.size();a++) for(int b=0;b<generators.size();b++) for(int c=0;c<generators.size();c++){
            RationalMatrix symmetric=generators.get(b).multiply(generators.get(c)).add(generators.get(c).multiply(generators.get(b)));
            if(generators.get(a).multiply(symmetric).trace().signum()!=0) return false;
        }
        return true;
    }
}
