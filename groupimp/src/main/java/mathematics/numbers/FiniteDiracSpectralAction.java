package mathematics.numbers;

import mathematics.linear.RationalMatrix;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Finite spectral triple data on the 128 rational coordinates of {@link RationalDixonZ2}.
 * N_g=1. The grading is the octonion grading involution. The real structure swaps the
 * two octonion grades and conjugates Q(i). The Dirac operator pairs those grades across
 * the C2 fold. Inner fluctuations use A + J A J^{-1}.
 */
public final class FiniteDiracSpectralAction implements Serializable {
    private static final long serialVersionUID=1L;
    private static final int N=128;
    private static final FiniteDiracSpectralAction MODEL=build();
    private final RationalMatrix gamma,realStructure,dirac,higgsDirection,sampleFluctuation;
    private final Rational quarticNumerator,quadraticNumerator;

    private FiniteDiracSpectralAction(RationalMatrix gamma,RationalMatrix realStructure,RationalMatrix dirac,RationalMatrix higgsDirection,RationalMatrix sampleFluctuation,Rational quarticNumerator,Rational quadraticNumerator){
        this.gamma=gamma;this.realStructure=realStructure;this.dirac=dirac;this.higgsDirection=higgsDirection;this.sampleFluctuation=sampleFluctuation;this.quarticNumerator=quarticNumerator;this.quadraticNumerator=quadraticNumerator;
    }
    public static FiniteDiracSpectralAction model(){return MODEL;}
    public RationalMatrix gamma(){return gamma;}
    public RationalMatrix realStructure(){return realStructure;}
    public RationalMatrix dirac(){return dirac;}
    public RationalMatrix higgsDirection(){return higgsDirection;}
    public int dimension(){return N;}
    /** Coefficient of phi^4 in Tr(D_A^4), with D_A = D + phi H and H the symmetrized Higgs direction. */
    public Rational quarticNumerator(){return quarticNumerator;}
    /** Coefficient of phi^2 in Tr(D_A^4). Negative means a tachyonic mass term before the quartic stabilizes it. */
    public Rational quadraticNumerator(){return quadraticNumerator;}
    public boolean quarticIsPositive(){return quarticNumerator.signum()>0;}

    public RationalMatrix fluctuated(Rational phi){
        return dirac.add(sampleFluctuation.scale(phi));
    }

    private static FiniteDiracSpectralAction build(){
        RationalMatrix gamma=gradingMatrix();
        RationalMatrix real=realStructureMatrix();
        RationalMatrix dirac=diracMatrix();
        // Block-odd sign makes the fold-odd direction square to -I after composition with D, giving -mu^2.
        RationalMatrix rawHiggs=blockSignature().multiply(dirac);
        RationalMatrix higgs=rawHiggs.add(real.multiply(rawHiggs).multiply(real)).scale(Rational.of(1,2));
        Rational constant=tracePolynomial(dirac,higgs,0);
        Rational atOne=tracePolynomial(dirac,higgs,1);
        Rational atTwo=tracePolynomial(dirac,higgs,2);
        // Tr = a + b phi^2 + c phi^4. Samples 0,1,2.
        Rational a=constant;
        Rational c=atTwo.subtract(a).subtract(atOne.subtract(a).multiply(Rational.of(4))).divide(Rational.of(12));
        Rational b=atOne.subtract(a).subtract(c);
        return new FiniteDiracSpectralAction(gamma,real,dirac,higgs,higgs,c,b);
    }
    private static Rational tracePolynomial(RationalMatrix dirac,RationalMatrix higgs,int phi){
        RationalMatrix fluctuated=dirac.add(higgs.scale(Rational.of(phi)));
        RationalMatrix square=fluctuated.multiply(fluctuated);
        return square.multiply(square).trace();
    }

    static RationalMatrix gradingMatrix(){
        int[] image=new int[N]; int[] sign=new int[N];
        for(int i=0;i<N;i++){image[i]=i;sign[i]=octonionIndex(i)<4?1:-1;}
        return permutation(image,sign);
    }
    static RationalMatrix realStructureMatrix(){
        int[] image=new int[N]; int[] sign=new int[N];
        for(int i=0;i<N;i++){image[i]=pack(block(i),quaternion(i),octonionIndex(i)^4,part(i));sign[i]=part(i)==0?1:-1;}
        return permutation(image,sign);
    }
    static RationalMatrix blockSignature(){
        int[] image=new int[N]; int[] sign=new int[N];
        for(int i=0;i<N;i++){image[i]=i;sign[i]=block(i)==0?1:-1;}
        return permutation(image,sign);
    }
    static RationalMatrix diracMatrix(){
        int[] image=new int[N]; int[] sign=new int[N];
        for(int i=0;i<N;i++){image[i]=pack(block(i)^1,quaternion(i),octonionIndex(i)^4,part(i));sign[i]=1;}
        return permutation(image,sign);
    }
    private static RationalMatrix permutation(int[] image,int[] sign){
        Rational[][] rows=new Rational[N][N];
        for(int r=0;r<N;r++) Arrays.fill(rows[r],Rational.ZERO);
        for(int c=0;c<N;c++) rows[image[c]][c]=sign[c]>0?Rational.ONE:Rational.ONE.negate();
        return new RationalMatrix(rows);
    }
    private static int complexCoordinate(int index){return index/2;}
    private static int block(int index){return complexCoordinate(index)/32;}
    private static int quaternion(int index){return (complexCoordinate(index)%32)/8;}
    private static int octonionIndex(int index){return complexCoordinate(index)%8;}
    private static int part(int index){return index%2;}
    private static int pack(int block,int quaternion,int octonion,int part){return 2*(block*32+quaternion*8+octonion)+part;}

    /** Left multiplication by a complex scalar, which is central in the coordinate representation. */
    public static RationalMatrix complexScalarMatrix(RationalComplex scalar){
        Rational[][] rows=new Rational[N][N];
        for(int r=0;r<N;r++) Arrays.fill(rows[r],Rational.ZERO);
        for(int c=0;c<64;c++){
            rows[2*c][2*c]=scalar.real; rows[2*c][2*c+1]=scalar.imaginary.negate();
            rows[2*c+1][2*c]=scalar.imaginary; rows[2*c+1][2*c+1]=scalar.real;
        }
        return new RationalMatrix(rows);
    }
    /** First-order defect of one associative-slice element against complex i, as a squared Frobenius norm. */
    public static Rational associativeSliceDefect(RationalDixonZ2 element){
        RationalMatrix dirac=model().dirac();
        RationalMatrix real=model().realStructure();
        RationalMatrix left=element.leftMultiplicationMatrix();
        RationalMatrix scalar=complexScalarMatrix(new RationalComplex(Rational.ZERO,Rational.ONE));
        RationalMatrix opposite=real.multiply(left).multiply(real);
        RationalMatrix defect=commutator(commutator(dirac,scalar),opposite);
        return frobenius(defect);
    }
    private static Rational frobenius(RationalMatrix matrix){
        Rational sum=Rational.ZERO;
        for(int r=0;r<matrix.rows();r++) for(int c=0;c<matrix.columns();c++) sum=sum.add(matrix.get(r,c).multiply(matrix.get(r,c)));
        return sum;
    }
    public static RationalMatrix commutator(RationalMatrix a,RationalMatrix b){
        return a.multiply(b).add(b.multiply(a).scale(Rational.ONE.negate()));
    }
}
