package mathematics.numbers;

import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standard Model gauge action on the 16 occupation states, plus the defect of the
 * historical octonion-stabilizer and quaternion-adjoint matrices on that span.
 */
public final class WittGaugeAction implements Serializable {
    private static final long serialVersionUID=1L;
    private static final WittGaugeAction MODEL=build();
    private final RationalMatrix isospin;
    private final RationalMatrix hypercharge;
    private final RationalMatrix weakSwap;
    private final RationalMatrix colorCycle;
    private final Rational historicalResidual;
    private final boolean historicallyInvariant;

    private WittGaugeAction(RationalMatrix isospin,RationalMatrix hypercharge,RationalMatrix weakSwap,RationalMatrix colorCycle,Rational historicalResidual){
        this.isospin=isospin;this.hypercharge=hypercharge;this.weakSwap=weakSwap;this.colorCycle=colorCycle;this.historicalResidual=historicalResidual;
        this.historicallyInvariant=historicalResidual.equals(Rational.ZERO);
    }
    public static WittGaugeAction model(){return MODEL;}
    public RationalMatrix isospin(){return isospin;}
    public RationalMatrix hypercharge(){return hypercharge;}
    public RationalMatrix weakSwap(){return weakSwap;}
    public RationalMatrix colorCycle(){return colorCycle;}
    public Rational historicalResidual(){return historicalResidual;}
    public boolean historicallyInvariant(){return historicallyInvariant;}

    public boolean hyperchargeCommutesWithGauge(){
        return commutator(hypercharge,isospin).equals(zero16())
                &&commutator(hypercharge,weakSwap).equals(zero16())
                &&commutator(hypercharge,colorCycle).equals(zero16());
    }
    public boolean colorCommutesWithIsospin(){return commutator(colorCycle,isospin).equals(zero16());}

    /** su(2) Casimir 3/4 on doublets and 0 on singlets, using Ty^2=Tx^2. */
    public Rational su2Casimir(int state){
        RationalVector basis=unit(state);
        RationalVector t3=isospin.multiply(basis);
        RationalVector tx=weakSwap.multiply(basis);
        return t3.dot(t3).add(tx.dot(tx)).add(tx.dot(tx));
    }
    /** 4/3 on the four color triplets and 0 on the four leptons. */
    public Rational su3Casimir(int state){
        return colorIndex(state)<0?Rational.ZERO:Rational.of(4,3);
    }
    public String representationLabel(int state){
        DixonMinimalLeftIdeal.State value=DixonMinimalLeftIdeal.model().states().get(state);
        String color=colorIndex(state)<0?"1":(state>=10?"3bar":"3");
        String weak=value.t3().signum()==0?"1":"2";
        return "("+color+", "+weak+")_"+value.hypercharge();
    }

    /** Real 4x4 form of (sigma_x^2+sigma_y^2+sigma_z^2)/4 = (3/4)I on one doublet. */
    public static RationalMatrix realDoubletCasimir(){
        RationalMatrix sx=pauli(new int[][]{{0,0,1,0},{0,0,0,1},{1,0,0,0},{0,1,0,0}});
        RationalMatrix sy=pauli(new int[][]{{0,0,0,-1},{0,0,1,0},{0,1,0,0},{-1,0,0,0}});
        RationalMatrix sz=pauli(new int[][]{{1,0,0,0},{0,1,0,0},{0,0,-1,0},{0,0,0,-1}});
        return sx.multiply(sx).add(sy.multiply(sy)).add(sz.multiply(sz)).scale(Rational.of(1,4));
    }

    private static WittGaugeAction build(){
        RationalMatrix t3=diagonal(isospinValues());
        RationalMatrix y=diagonal(hyperchargeValues());
        RationalMatrix swap=weakSwapMatrix();
        RationalMatrix color=colorCycleMatrix();
        Rational residual=historicalResidual(DixonMinimalLeftIdeal.model());
        return new WittGaugeAction(t3,y,swap,color,residual);
    }
    private static Rational[] isospinValues(){
        List<DixonMinimalLeftIdeal.State> states=DixonMinimalLeftIdeal.model().states();
        Rational[] values=new Rational[16];
        for(int i=0;i<16;i++) values[i]=states.get(i).t3();
        return values;
    }
    private static Rational[] hyperchargeValues(){
        List<DixonMinimalLeftIdeal.State> states=DixonMinimalLeftIdeal.model().states();
        Rational[] values=new Rational[16];
        for(int i=0;i<16;i++) values[i]=states.get(i).hypercharge();
        return values;
    }
    private static RationalMatrix weakSwapMatrix(){
        Rational[][] m=zero();
        int[][] pairs={{0,1},{2,5},{3,6},{4,7}};
        for(int[] pair:pairs){
            m[pair[0]][pair[1]]=Rational.of(1,2);
            m[pair[1]][pair[0]]=Rational.of(1,2);
        }
        return new RationalMatrix(m);
    }
    private static RationalMatrix colorCycleMatrix(){
        Rational[][] m=zero();
        int[][] cycles={{2,3,4},{5,6,7},{10,11,12},{13,14,15}};
        for(int[] cycle:cycles) for(int i=0;i<3;i++) m[cycle[(i+1)%3]][cycle[i]]=Rational.ONE;
        return new RationalMatrix(m);
    }
    private static int colorIndex(int state){
        if(state>=2&&state<=7) return state;
        if(state>=10) return state;
        return -1;
    }
    private static RationalVector unit(int state){
        Rational[] values=new Rational[16];
        Arrays.fill(values,Rational.ZERO);
        values[state]=Rational.ONE;
        return new RationalVector(values);
    }
    private static RationalMatrix diagonal(Rational[] values){
        Rational[][] m=zero();
        for(int i=0;i<16;i++) m[i][i]=values[i];
        return new RationalMatrix(m);
    }
    private static Rational[][] zero(){
        Rational[][] m=new Rational[16][16];
        for(int r=0;r<16;r++) Arrays.fill(m[r],Rational.ZERO);
        return m;
    }
    private static RationalMatrix zero16(){return diagonal(new Rational[]{Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO});}
    private static RationalMatrix commutator(RationalMatrix a,RationalMatrix b){
        return a.multiply(b).add(b.multiply(a).scale(Rational.ONE.negate()));
    }
    private static RationalMatrix pauli(int[][] pattern){
        Rational[][] m=new Rational[4][4];
        for(int r=0;r<4;r++) for(int c=0;c<4;c++) m[r][c]=Rational.of(pattern[r][c]);
        return new RationalMatrix(m);
    }

    private static Rational historicalResidual(DixonMinimalLeftIdeal ideal){
        RationalMatrix basis=columns(ideal);
        RationalMatrix dual=basis.transpose().multiply(basis).inverse().multiply(basis.transpose());
        Rational sum=Rational.ZERO;
        List<RationalMatrix> color=RationalOctonionDerivations.su3StabilizerCandidates();
        List<RationalMatrix> weak=RationalOctonionDerivations.quaternionAdjointGenerators();
        for(DixonMinimalLeftIdeal.State state:ideal.states()){
            for(RationalMatrix generator:color) sum=sum.add(defect(basis,dual,applyOctonion(state.vector().toVector(),generator)));
            for(RationalMatrix generator:weak) sum=sum.add(defect(basis,dual,applyQuaternion(state.vector().toVector(),generator)));
        }
        return sum;
    }
    private static Rational defect(RationalMatrix basis,RationalMatrix dual,RationalVector image){
        RationalVector projection=basis.multiply(dual.multiply(image));
        RationalVector orthogonal=image.add(projection.scale(Rational.ONE.negate()));
        return orthogonal.dot(orthogonal);
    }
    private static RationalMatrix columns(DixonMinimalLeftIdeal ideal){
        Rational[][] rows=new Rational[128][16];
        for(int c=0;c<16;c++) for(int r=0;r<128;r++) rows[r][c]=ideal.states().get(c).vector().toVector().get(r);
        return new RationalMatrix(rows);
    }
    /** Lift of an octonion-index generator to the 128 rational coordinates. */
    public static RationalVector applyOctonion(RationalVector value,RationalMatrix generator){
        Rational[] out=new Rational[128];
        Arrays.fill(out,Rational.ZERO);
        for(int index=0;index<128;index++) if(value.get(index).signum()!=0){
            int octonion=octonionIndex(index);
            for(int image=0;image<8;image++){
                Rational weight=generator.get(image,octonion);
                if(weight.signum()==0) continue;
                int dest=pack(block(index),quaternion(index),image,part(index));
                out[dest]=out[dest].add(weight.multiply(value.get(index)));
            }
        }
        return new RationalVector(out);
    }
    /** Full 128x128 lift of an octonion-index generator. */
    public static RationalMatrix liftOctonion(RationalMatrix generator){
        return lift(generator,true);
    }
    /** Full 128x128 lift of a quaternion-index generator. */
    public static RationalMatrix liftQuaternion(RationalMatrix generator){
        return lift(generator,false);
    }
    private static RationalMatrix lift(RationalMatrix generator,boolean octonion){
        Rational[][] rows=new Rational[128][128];
        for(int column=0;column<128;column++){
            Rational[] values=new Rational[128];
            Arrays.fill(values,Rational.ZERO);
            values[column]=Rational.ONE;
            RationalVector image=octonion?applyOctonion(new RationalVector(values),generator)
                    :applyQuaternion(new RationalVector(values),generator);
            for(int row=0;row<128;row++) rows[row][column]=image.get(row);
        }
        return new RationalMatrix(rows);
    }
    /** Lift of a quaternion-index generator to the 128 rational coordinates. */
    public static RationalVector applyQuaternion(RationalVector value,RationalMatrix generator){
        Rational[] out=new Rational[128];
        Arrays.fill(out,Rational.ZERO);
        for(int index=0;index<128;index++) if(value.get(index).signum()!=0){
            int q=quaternion(index);
            for(int image=0;image<4;image++){
                Rational weight=generator.get(image,q);
                if(weight.signum()==0) continue;
                int dest=pack(block(index),image,octonionIndex(index),part(index));
                out[dest]=out[dest].add(weight.multiply(value.get(index)));
            }
        }
        return new RationalVector(out);
    }
    private static int block(int index){return (index/2)/32;}
    private static int quaternion(int index){return ((index/2)%32)/8;}
    private static int octonionIndex(int index){return (index/2)%8;}
    private static int part(int index){return index%2;}
    private static int pack(int block,int quaternion,int octonion,int part){return 2*(block*32+quaternion*8+octonion)+part;}
}
