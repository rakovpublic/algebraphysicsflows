package mathematics.numbers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

/**
 * Order-3 generation phase from zeta_12^4 and a 3x3 Yukawa diagonalizer.
 * The scan varies the fold angle and a zeta_12^2 harmonic. It does not claim
 * that the ansatz produces the measured CP phase.
 */
public final class TrialityFlavorModule implements Serializable {
    private static final long serialVersionUID=1L;
    private TrialityFlavorModule(){}

    public static RationalCyclotomic generationPhase(){return RationalCyclotomic.zeta(12).pow(4);}
    public static boolean generationPhaseHasOrderThree(){
        RationalCyclotomic omega=generationPhase();
        return omega.pow(3).equals(RationalCyclotomic.one(12))&&!omega.equals(RationalCyclotomic.one(12));
    }
    public static int weylStateCount(){return 3*DixonMinimalLeftIdeal.model().rank();}

    /** Cycles the three octonion units il,jl,kl. Order 3 on coordinates; not an algebra automorphism in general. */
    public static RationalOctonion coordinateTriality(RationalOctonion value){
        return new RationalOctonion(value.coordinate(0),value.coordinate(1),value.coordinate(2),value.coordinate(3),value.coordinate(4),
                value.coordinate(7),value.coordinate(5),value.coordinate(6));
    }

    public static final class Scan implements Serializable {
        private static final long serialVersionUID=1L;
        public final BigDecimal bestJarlskog,bestDeltaRadians,pdgCentral,residual;
        public final boolean insideOneSigma;
        public Scan(BigDecimal bestJarlskog,BigDecimal bestDeltaRadians,BigDecimal pdgCentral,BigDecimal residual,boolean insideOneSigma){
            this.bestJarlskog=bestJarlskog;this.bestDeltaRadians=bestDeltaRadians;this.pdgCentral=pdgCentral;this.residual=residual;this.insideOneSigma=insideOneSigma;
        }
        @Override public String toString(){
            return "best J="+bestJarlskog.toPlainString()+" at delta="+bestDeltaRadians.toPlainString()
                    +" residual="+residual.toPlainString()+" insideOneSigma="+insideOneSigma;
        }
    }

    public static Scan scan(int precision){
        MathContext mc=new MathContext(precision+6,RoundingMode.HALF_EVEN);
        BigDecimal pi=OneLoopRGEFlow.pi(mc);
        BigDecimal s12=new BigDecimal("0.22501"),s23=new BigDecimal("0.04183"),s13=new BigDecimal("0.003732");
        BigDecimal c12=cosFromSin(s12,mc),c23=cosFromSin(s23,mc),c13=cosFromSin(s13,mc);
        BigDecimal pdgJ=new BigDecimal("0.0000312");
        BigDecimal bestJ=null,bestDelta=null,bestGap=null;
        for(int step=0;step<=24;step++){
            BigDecimal fold=pi.multiply(BigDecimal.valueOf(step),mc).divide(BigDecimal.valueOf(24),mc);
            for(int harmonic=0;harmonic<=2;harmonic++){
                BigDecimal delta=fold.add(pi.multiply(BigDecimal.valueOf(harmonic),mc).divide(BigDecimal.valueOf(6),mc),mc);
                Complex[][] ckm=pdgMatrix(s12,s23,s13,c12,c23,c13,delta,mc);
                BigDecimal jarlskog=jarlskog(diagonalizeYukawa(diagonalMasses(),mc),diagonalizeYukawa(multiply(ckm,diagonalMasses(),mc),mc),mc);
                BigDecimal gap=jarlskog.subtract(pdgJ,mc).abs();
                if(bestGap==null||gap.compareTo(bestGap)<0){bestGap=gap;bestJ=jarlskog;bestDelta=delta;}
            }
        }
        BigDecimal sigma=new BigDecimal("0.0000013");
        boolean inside=bestJ.subtract(pdgJ,mc).abs().compareTo(sigma)<=0;
        MathContext out=new MathContext(precision,RoundingMode.HALF_EVEN);
        return new Scan(bestJ.round(out),bestDelta.round(out),pdgJ,bestJ.subtract(pdgJ,mc).round(out),inside);
    }

    /** Cross-check: Yukawa SVD of the PDG matrix at a measured phase reproduces HighPrecisionAlgebraMetrics. */
    public static BigDecimal jarlskogFromYukawa(BigDecimal delta,int precision){
        MathContext mc=new MathContext(precision+6,RoundingMode.HALF_EVEN);
        BigDecimal s12=new BigDecimal("0.22501"),s23=new BigDecimal("0.04183"),s13=new BigDecimal("0.003732");
        Complex[][] ckm=pdgMatrix(s12,s23,s13,cosFromSin(s12,mc),cosFromSin(s23,mc),cosFromSin(s13,mc),delta,mc);
        Complex[][] up=diagonalizeYukawa(diagonalMasses(),mc);
        Complex[][] down=diagonalizeYukawa(multiply(ckm,diagonalMasses(),mc),mc);
        return jarlskog(up,down,mc).round(new MathContext(precision,RoundingMode.HALF_EVEN));
    }

    private static Complex[][] diagonalMasses(){
        Complex[][] mass=zero();
        mass[0][0]=Complex.real(BigDecimal.ONE);mass[1][1]=Complex.real(new BigDecimal("2"));mass[2][2]=Complex.real(new BigDecimal("3"));
        return mass;
    }
    private static BigDecimal cosFromSin(BigDecimal sine,MathContext mc){
        return sqrt(BigDecimal.ONE.subtract(sine.multiply(sine,mc),mc),mc);
    }
    private static BigDecimal sqrt(BigDecimal value,MathContext mc){
        BigDecimal x=BigDecimal.ONE;
        for(int i=0;i<mc.getPrecision()*2;i++){
            BigDecimal next=x.add(value.divide(x,mc),mc).divide(BigDecimal.valueOf(2),mc);
            if(next.subtract(x,mc).abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0) return next;
            x=next;
        }
        return x;
    }

    static Complex[][] pdgMatrix(BigDecimal s12,BigDecimal s23,BigDecimal s13,BigDecimal c12,BigDecimal c23,BigDecimal c13,BigDecimal delta,MathContext mc){
        Complex phase=Complex.expi(delta,mc),minusPhase=phase.conjugate();
        Complex[][] v=zero();
        v[0][0]=Complex.real(c12.multiply(c13,mc));
        v[0][1]=Complex.real(s12.multiply(c13,mc));
        v[0][2]=minusPhase.scale(s13,mc);
        v[1][0]=Complex.real(s12.negate().multiply(c23,mc)).subtract(phase.scale(c12.multiply(s23,mc).multiply(s13,mc),mc),mc);
        v[1][1]=Complex.real(c12.multiply(c23,mc)).subtract(phase.scale(s12.multiply(s23,mc).multiply(s13,mc),mc),mc);
        v[1][2]=Complex.real(s23.multiply(c13,mc));
        v[2][0]=Complex.real(s12.multiply(s23,mc)).subtract(phase.scale(c12.multiply(c23,mc).multiply(s13,mc),mc),mc);
        v[2][1]=Complex.real(c12.negate().multiply(s23,mc)).subtract(phase.scale(s12.multiply(c23,mc).multiply(s13,mc),mc),mc);
        v[2][2]=Complex.real(c23.multiply(c13,mc));
        return v;
    }

    /** Left singular vectors of a 3x3 matrix, columns ordered by descending singular value. */
    static Complex[][] diagonalizeYukawa(Complex[][] yukawa,MathContext mc){
        return eigenvectors(hermitianProduct(yukawa,mc),mc);
    }
    private static Complex[][] hermitianProduct(Complex[][] yukawa,MathContext mc){
        Complex[][] adjoint=adjoint(yukawa),product=zero();
        for(int r=0;r<3;r++) for(int c=0;c<3;c++){
            Complex sum=Complex.ZERO;
            for(int k=0;k<3;k++) sum=sum.add(yukawa[r][k].multiply(adjoint[k][c],mc),mc);
            product[r][c]=sum;
        }
        return product;
    }
    private static Complex[][] adjoint(Complex[][] matrix){
        Complex[][] result=zero();
        for(int r=0;r<3;r++) for(int c=0;c<3;c++) result[c][r]=matrix[r][c].conjugate();
        return result;
    }
    private static Complex[][] eigenvectors(Complex[][] hermitian,MathContext mc){
        BigDecimal[] eigenvalues=cubicEigenvalues(hermitian,mc);
        Complex[][] vectors=zero();
        for(int n=0;n<3;n++){
            Complex[] vector=nullVector(shift(hermitian,eigenvalues[n],mc),mc);
            for(int r=0;r<3;r++) vectors[r][n]=vector[r];
        }
        return vectors;
    }
    private static Complex[][] shift(Complex[][] matrix,BigDecimal eigenvalue,MathContext mc){
        Complex[][] shifted=zero();
        for(int r=0;r<3;r++) for(int c=0;c<3;c++) shifted[r][c]=matrix[r][c];
        for(int i=0;i<3;i++) shifted[i][i]=shifted[i][i].subtract(Complex.real(eigenvalue),mc);
        return shifted;
    }
    private static Complex[] nullVector(Complex[][] matrix,MathContext mc){
        Complex[] best=cross(matrix[0],matrix[1],mc);
        if(norm(best,mc).compareTo(new BigDecimal("1e-8"))<0) best=cross(matrix[0],matrix[2],mc);
        if(norm(best,mc).compareTo(new BigDecimal("1e-8"))<0) best=cross(matrix[1],matrix[2],mc);
        return normalize(best,mc);
    }
    private static Complex[] cross(Complex[] a,Complex[] b,MathContext mc){
        return new Complex[]{
                a[1].multiply(b[2],mc).subtract(a[2].multiply(b[1],mc),mc),
                a[2].multiply(b[0],mc).subtract(a[0].multiply(b[2],mc),mc),
                a[0].multiply(b[1],mc).subtract(a[1].multiply(b[0],mc),mc)};
    }
    private static BigDecimal norm(Complex[] vector,MathContext mc){
        BigDecimal sum=BigDecimal.ZERO;
        for(Complex value:vector) sum=sum.add(value.abs2(mc),mc);
        return sqrt(sum,mc);
    }
    private static Complex[] normalize(Complex[] vector,MathContext mc){
        int pivot=0;
        for(int i=1;i<3;i++) if(vector[i].abs2(mc).compareTo(vector[pivot].abs2(mc))>0) pivot=i;
        if(vector[pivot].abs2(mc).signum()==0) return new Complex[]{Complex.ONE,Complex.ZERO,Complex.ZERO};
        Complex phase=vector[pivot].conjugate().scale(BigDecimal.ONE.divide(norm(new Complex[]{vector[pivot]},mc),mc),mc);
        Complex[] phased=new Complex[3];
        for(int i=0;i<3;i++) phased[i]=vector[i].multiply(phase,mc);
        BigDecimal length=norm(phased,mc);
        Complex[] result=new Complex[3];
        for(int i=0;i<3;i++) result[i]=phased[i].scale(BigDecimal.ONE.divide(length,mc),mc);
        return result;
    }
    private static BigDecimal[] cubicEigenvalues(Complex[][] hermitian,MathContext mc){
        BigDecimal trace=hermitian[0][0].re.add(hermitian[1][1].re,mc).add(hermitian[2][2].re,mc);
        BigDecimal sigma=minor(hermitian,0,1,mc).add(minor(hermitian,0,2,mc),mc).add(minor(hermitian,1,2,mc),mc);
        BigDecimal det=determinant(hermitian,mc);
        BigDecimal third=trace.divide(BigDecimal.valueOf(3),mc);
        BigDecimal p=sigma.subtract(third.multiply(trace,mc),mc);
        BigDecimal q=third.pow(3,mc).multiply(new BigDecimal("-2"),mc).add(third.multiply(sigma,mc),mc).subtract(det,mc);
        BigDecimal radius=sqrt(p.abs().divide(BigDecimal.valueOf(3),mc),mc);
        BigDecimal cosArg=q.divide(BigDecimal.valueOf(2),mc).divide(radius.pow(3,mc),mc).negate();
        if(cosArg.compareTo(BigDecimal.ONE)>0) cosArg=BigDecimal.ONE;
        if(cosArg.compareTo(BigDecimal.ONE.negate())<0) cosArg=BigDecimal.ONE.negate();
        BigDecimal angle=acos(cosArg,mc);
        BigDecimal[] roots=new BigDecimal[3];
        for(int k=0;k<3;k++) roots[k]=radius.multiply(BigDecimal.valueOf(2),mc).multiply(cos(angle.divide(BigDecimal.valueOf(3),mc).subtract(OneLoopRGEFlow.pi(mc).multiply(BigDecimal.valueOf(2L*k),mc).divide(BigDecimal.valueOf(3),mc),mc),mc),mc).add(trace.divide(BigDecimal.valueOf(3),mc),mc);
        Arrays.sort(roots);
        // Descending singular-value order.
        return new BigDecimal[]{roots[2],roots[1],roots[0]};
    }
    private static BigDecimal minor(Complex[][] matrix,int a,int b,MathContext mc){
        return matrix[a][a].re.multiply(matrix[b][b].re,mc).subtract(matrix[a][b].abs2(mc),mc);
    }
    private static BigDecimal determinant(Complex[][] matrix,MathContext mc){
        Complex det=Complex.ZERO;
        int[][] perm={{0,1,2},{1,2,0},{2,0,1},{0,2,1},{1,0,2},{2,1,0}};
        int[] sign={1,1,1,-1,-1,-1};
        for(int p=0;p<6;p++){
            Complex term=matrix[0][perm[p][0]].multiply(matrix[1][perm[p][1]],mc).multiply(matrix[2][perm[p][2]],mc);
            det=sign[p]>0?det.add(term,mc):det.subtract(term,mc);
        }
        return det.re;
    }
    private static BigDecimal acos(BigDecimal value,MathContext mc){
        BigDecimal low=BigDecimal.ZERO,high=OneLoopRGEFlow.pi(mc);
        for(int i=0;i<mc.getPrecision()*2;i++){
            BigDecimal mid=low.add(high,mc).divide(BigDecimal.valueOf(2),mc);
            if(cos(mid,mc).compareTo(value)>0) low=mid; else high=mid;
        }
        return low.add(high,mc).divide(BigDecimal.valueOf(2),mc);
    }
    private static BigDecimal cos(BigDecimal value,MathContext mc){
        BigDecimal x2=value.multiply(value,mc),term=BigDecimal.ONE,sum=BigDecimal.ONE;
        for(int k=1;k<mc.getPrecision()*2;k++){
            term=term.multiply(x2,mc).negate().divide(BigDecimal.valueOf((2L*k-1)*(2L*k)),mc);
            sum=sum.add(term,mc);
            if(term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0) break;
        }
        return sum;
    }
    static BigDecimal jarlskog(Complex[][] up,Complex[][] down,MathContext mc){
        Complex[][] ckm=multiply(adjoint(up),down,mc);
        Complex product=ckm[0][1].multiply(ckm[1][2],mc).multiply(ckm[0][2].conjugate(),mc).multiply(ckm[1][1].conjugate(),mc);
        return product.im;
    }
    private static Complex[][] multiply(Complex[][] a,Complex[][] b,MathContext mc){
        Complex[][] result=zero();
        for(int r=0;r<3;r++) for(int c=0;c<3;c++){
            Complex sum=Complex.ZERO;
            for(int k=0;k<3;k++) sum=sum.add(a[r][k].multiply(b[k][c],mc),mc);
            result[r][c]=sum;
        }
        return result;
    }
    private static Complex[][] zero(){
        Complex[][] matrix=new Complex[3][3];
        for(int r=0;r<3;r++) for(int c=0;c<3;c++) matrix[r][c]=Complex.ZERO;
        return matrix;
    }

    static final class Complex implements Serializable {
        private static final long serialVersionUID=1L;
        static final Complex ZERO=new Complex(BigDecimal.ZERO,BigDecimal.ZERO);
        static final Complex ONE=real(BigDecimal.ONE);
        final BigDecimal re,im;
        Complex(BigDecimal re,BigDecimal im){this.re=re;this.im=im;}
        static Complex real(BigDecimal value){return new Complex(value,BigDecimal.ZERO);}
        static Complex expi(BigDecimal angle,MathContext mc){return new Complex(cos(angle,mc),sin(angle,mc));}
        Complex conjugate(){return new Complex(re,im.negate());}
        Complex add(Complex other,MathContext mc){return new Complex(re.add(other.re,mc),im.add(other.im,mc));}
        Complex subtract(Complex other,MathContext mc){return new Complex(re.subtract(other.re,mc),im.subtract(other.im,mc));}
        Complex multiply(Complex other,MathContext mc){
            return new Complex(re.multiply(other.re,mc).subtract(im.multiply(other.im,mc),mc),re.multiply(other.im,mc).add(im.multiply(other.re,mc),mc));
        }
        Complex scale(BigDecimal scalar,MathContext mc){return new Complex(re.multiply(scalar,mc),im.multiply(scalar,mc));}
        BigDecimal abs2(MathContext mc){return re.multiply(re,mc).add(im.multiply(im,mc),mc);}
        private static BigDecimal sin(BigDecimal value,MathContext mc){
            BigDecimal x2=value.multiply(value,mc),term=value,sum=value;
            for(int k=1;k<mc.getPrecision()*2;k++){
                term=term.multiply(x2,mc).negate().divide(BigDecimal.valueOf((2L*k)*(2L*k+1)),mc);
                sum=sum.add(term,mc);
                if(term.abs().compareTo(BigDecimal.ONE.scaleByPowerOfTen(-mc.getPrecision()))<0) break;
            }
            return sum;
        }
    }
}
