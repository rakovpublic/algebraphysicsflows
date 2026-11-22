package mathematics.numbers;

import mathematics.core.MathFailure;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.io.Serializable;
import java.util.*;

/**
 * Exact crossed product (Q(i) tensor H(Q) tensor O(Q)) semidirect C2.
 * Coordinates are two 32-entry Q(i) blocks, a+b*u, with u*a=alpha(a)*u.
 * The octonion grading involution alpha fixes 1,i,j,k and negates l,il,jl,kl.
 * The coefficient product, and therefore this algebra, is nonassociative.
 */
public final class RationalDixonZ2 implements Serializable {
    private static final long serialVersionUID=1L;
    private static final int BASIS=32;
    private static final int[][] Q_INDEX=new int[BASIS][BASIS];
    private static final int[][] O_INDEX=new int[BASIS][BASIS];
    private static final int[][] SIGN=new int[BASIS][BASIS];
    private static final RationalComplex ONE_COMPLEX=new RationalComplex(Rational.ONE,Rational.ZERO);
    public static final RationalDixonZ2 ZERO=scalar(RationalComplexZero.VALUE);
    public static final RationalDixonZ2 ONE=scalar(new RationalComplex(Rational.ONE,Rational.ZERO));
    public static final RationalDixonZ2 U=groupBasis(0);
    private final List<RationalComplex> coordinates;

    static {
        for(int a=0;a<BASIS;a++) for(int b=0;b<BASIS;b++) {
            int qa=a/8,oa=a%8,qb=b/8,ob=b%8;
            RationalQuaternion q=quaternionBasis(qa).multiply(quaternionBasis(qb));
            RationalOctonion o=octonionBasis(oa).multiply(octonionBasis(ob));
            int qi=-1,oi=-1,s=1;
            for(int n=0;n<4;n++) if(q.components().get(n).signum()!=0) { qi=n; s=q.components().get(n).signum(); }
            for(int n=0;n<8;n++) if(o.coordinate(n).signum()!=0) { oi=n; s*=o.coordinate(n).signum(); }
            if(qi<0 || oi<0) throw new ExceptionInInitializerError("Basis product was not a signed basis element");
            Q_INDEX[a][b]=qi; O_INDEX[a][b]=oi; SIGN[a][b]=s;
        }
    }

    private static final class RationalComplexZero { static final RationalComplex VALUE=new RationalComplex(Rational.ZERO,Rational.ZERO); }
    private RationalDixonZ2(List<RationalComplex> values) {
        if(values.size()!=2*BASIS) throw new IllegalArgumentException("Dixon Z2 algebra requires 64 Q(i) coordinates");
        List<RationalComplex> copy=new ArrayList<>(values.size());
        for(RationalComplex value:values) copy.add(Objects.requireNonNull(value));
        coordinates=Collections.unmodifiableList(copy);
    }
    private static RationalQuaternion quaternionBasis(int n) {
        Rational[] c={Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO}; c[n]=Rational.ONE;
        return new RationalQuaternion(c[0],c[1],c[2],c[3]);
    }
    private static RationalOctonion octonionBasis(int n) {
        Rational[] c=new Rational[8]; Arrays.fill(c,Rational.ZERO); c[n]=Rational.ONE; return new RationalOctonion(c);
    }
    /** Basis q_i tensor o_j in the even (identity-group) block. */
    public static RationalDixonZ2 basis(int quaternionIndex,int octonionIndex) {
        if(quaternionIndex<0 || quaternionIndex>3 || octonionIndex<0 || octonionIndex>7) throw new IllegalArgumentException("Basis indices outside H tensor O");
        RationalComplex[] c=zeroCoordinates(); c[quaternionIndex*8+octonionIndex]=ONE_COMPLEX; return of(c);
    }
    private static RationalDixonZ2 groupBasis(int index) {
        RationalComplex[] c=zeroCoordinates(); c[BASIS+index]=ONE_COMPLEX; return of(c);
    }
    private static RationalComplex[] zeroCoordinates() {
        RationalComplex[] values=new RationalComplex[2*BASIS]; Arrays.fill(values,RationalComplexZero.VALUE); return values;
    }
    private static RationalDixonZ2 of(RationalComplex[] values) { return new RationalDixonZ2(Arrays.asList(values)); }
    public static RationalDixonZ2 scalar(RationalComplex value) {
        RationalComplex[] c=zeroCoordinates(); c[0]=Objects.requireNonNull(value); return of(c);
    }
    public static RationalDixonZ2 fromRational(Rational value) { return scalar(new RationalComplex(value,Rational.ZERO)); }
    public static RationalDixonZ2 fromComplex(RationalComplex value) { return scalar(value); }
    public static RationalDixonZ2 fromQuaternion(RationalQuaternion value) {
        RationalComplex[] c=zeroCoordinates();
        c[0]=new RationalComplex(value.real,Rational.ZERO); c[8]=new RationalComplex(value.i,Rational.ZERO);
        c[16]=new RationalComplex(value.j,Rational.ZERO); c[24]=new RationalComplex(value.k,Rational.ZERO); return of(c);
    }
    public static RationalDixonZ2 fromOctonion(RationalOctonion value) {
        RationalComplex[] c=zeroCoordinates(); for(int n=0;n<8;n++) c[n]=new RationalComplex(value.coordinate(n),Rational.ZERO); return of(c);
    }
    public static RationalDixonZ2 fromImaginary(RationalImaginary value) {
        RationalComplex[] c=zeroCoordinates(); c[0]=new RationalComplex(Rational.ZERO,value.coefficient); return of(c);
    }
    public static RationalDixonZ2 fromBoolean(Boolean value) { return fromRational(value?Rational.ONE:Rational.ZERO); }
    public List<RationalComplex> components() { return coordinates; }
    public RationalComplex coordinate(int index) { return coordinates.get(index); }
    /** Flatten as [real(c0),imag(c0),...,real(c63),imag(c63)] in Vec(Q)^128. */
    public RationalVector toVector() {
        Rational[] values=new Rational[128];
        for(int n=0;n<64;n++) { values[2*n]=coordinate(n).real; values[2*n+1]=coordinate(n).imaginary; }
        return new RationalVector(values);
    }
    public static RationalDixonZ2 fromVector(RationalVector vector) {
        if(vector.dimension()!=128) throw MathFailure.undefined("Dixon Z2 coordinate vector must have dimension 128");
        RationalComplex[] values=new RationalComplex[64];
        for(int n=0;n<64;n++) values[n]=new RationalComplex(vector.get(2*n),vector.get(2*n+1));
        return of(values);
    }
    public boolean isZero() { for(RationalComplex c:coordinates) if(!c.equals(RationalComplexZero.VALUE)) return false; return true; }
    public boolean isOne() { return equals(ONE); }
    public RationalDixonZ2 add(RationalDixonZ2 b) {
        RationalComplex[] out=new RationalComplex[64]; for(int n=0;n<64;n++) out[n]=coordinate(n).add(b.coordinate(n)); return of(out);
    }
    public RationalDixonZ2 negate() {
        RationalComplex[] out=new RationalComplex[64]; for(int n=0;n<64;n++) out[n]=coordinate(n).negate(); return of(out);
    }
    public RationalDixonZ2 subtract(RationalDixonZ2 b) { return add(b.negate()); }
    private static RationalComplex[] coefficientProduct(RationalComplex[] a,RationalComplex[] b) {
        RationalComplex[] out=new RationalComplex[BASIS]; Arrays.fill(out,RationalComplexZero.VALUE);
        for(int i=0;i<BASIS;i++) if(!a[i].equals(RationalComplexZero.VALUE))
            for(int j=0;j<BASIS;j++) if(!b[j].equals(RationalComplexZero.VALUE)) {
                int index=Q_INDEX[i][j]*8+O_INDEX[i][j]; RationalComplex term=a[i].multiply(b[j]);
                if(SIGN[i][j]<0) term=term.negate(); out[index]=out[index].add(term);
            }
        return out;
    }
    private static RationalComplex[] applyAlpha(RationalComplex[] a) {
        RationalComplex[] out=a.clone(); for(int n=0;n<BASIS;n++) if(n%8>=4) out[n]=out[n].negate(); return out;
    }
    /** (a+b*u)(c+d*u)=(ac+b*alpha(d))+(ad+b*alpha(c))*u. */
    public RationalDixonZ2 multiply(RationalDixonZ2 other) {
        RationalComplex[] a=coordinates.subList(0,BASIS).toArray(new RationalComplex[0]);
        RationalComplex[] b=coordinates.subList(BASIS,2*BASIS).toArray(new RationalComplex[0]);
        RationalComplex[] c=other.coordinates.subList(0,BASIS).toArray(new RationalComplex[0]);
        RationalComplex[] d=other.coordinates.subList(BASIS,2*BASIS).toArray(new RationalComplex[0]);
        RationalComplex[] even=coefficientProduct(a,c), bd=coefficientProduct(b,applyAlpha(d));
        RationalComplex[] odd=coefficientProduct(a,d), bc=coefficientProduct(b,applyAlpha(c));
        RationalComplex[] out=new RationalComplex[64];
        for(int n=0;n<BASIS;n++) { out[n]=even[n].add(bd[n]); out[BASIS+n]=odd[n].add(bc[n]); }
        return of(out);
    }
    public RationalDixonZ2 gradingInvolution() {
        RationalComplex[] out=zeroCoordinates();
        for(int block=0;block<2;block++) for(int n=0;n<BASIS;n++) out[block*BASIS+n]=(n%8>=4)?coordinate(block*BASIS+n).negate():coordinate(block*BASIS+n);
        return of(out);
    }
    public RationalDixonZ2 evenPart() { RationalComplex[] out=zeroCoordinates(); for(int n=0;n<BASIS;n++) out[n]=coordinate(n); return of(out); }
    public RationalDixonZ2 oddPart() { RationalComplex[] out=zeroCoordinates(); for(int n=BASIS;n<2*BASIS;n++) out[n]=coordinate(n); return of(out); }
    /** The faithful Q-linear regular representation x -> (y -> x*y), not a multiplicative representation when the algebra is nonassociative. */
    public RationalMatrix leftMultiplicationMatrix() {
        Rational[][] rows=new Rational[128][128];
        for(int c=0;c<128;c++) {
            Rational[] basis=new Rational[128]; Arrays.fill(basis,Rational.ZERO); basis[c]=Rational.ONE;
            RationalVector product= multiply(fromVector(new RationalVector(basis))).toVector();
            for(int r=0;r<128;r++) rows[r][c]=product.get(r);
        }
        return new RationalMatrix(rows);
    }
    public static RationalDixonZ2 fromLeftMultiplicationMatrix(RationalMatrix matrix) {
        if(matrix.rows()!=128 || matrix.columns()!=128) throw MathFailure.undefined("Dixon Z2 left multiplication matrix must be 128 by 128");
        RationalVector firstColumn=matrix.column(0);
        RationalDixonZ2 value=fromVector(firstColumn);
        if(!value.leftMultiplicationMatrix().equals(matrix)) throw MathFailure.undefined("Matrix is not left multiplication by a Dixon Z2 element in the declared basis");
        return value;
    }
    public RationalComplex toComplex() {
        for(int n=1;n<64;n++) if(!coordinate(n).equals(RationalComplexZero.VALUE)) throw MathFailure.undefined("Dixon element is outside the scalar Q(i) subalgebra");
        return coordinate(0);
    }
    public Rational toRational() {
        RationalComplex c=toComplex(); if(!c.imaginary.equals(Rational.ZERO)) throw MathFailure.undefined("Dixon scalar is not rational"); return c.real;
    }
    public RationalImaginary toImaginary() { return RationalImaginary.fromComplex(toComplex()); }
    public RationalQuaternion toQuaternion() {
        for(int n=0;n<64;n++) if((n>=32 || n%8!=0) && !coordinate(n).equals(RationalComplexZero.VALUE))
            throw MathFailure.undefined("Dixon element is outside the embedded quaternion algebra");
        Rational[] v=new Rational[4]; for(int q=0;q<4;q++) { RationalComplex c=coordinate(q*8); if(!c.imaginary.equals(Rational.ZERO)) throw MathFailure.undefined("Quaternion projection requires rational coefficients"); v[q]=c.real; }
        return new RationalQuaternion(v[0],v[1],v[2],v[3]);
    }
    public RationalOctonion toOctonion() {
        Rational[] v=new Rational[8];
        for(int n=0;n<8;n++) { RationalComplex c=coordinate(n); if(!c.imaginary.equals(Rational.ZERO)) throw MathFailure.undefined("Octonion projection requires rational coefficients"); v[n]=c.real; }
        for(int n=8;n<64;n++) if(!coordinate(n).equals(RationalComplexZero.VALUE)) throw MathFailure.undefined("Dixon element is outside the embedded octonion algebra");
        return new RationalOctonion(v);
    }
    public boolean toBoolean() {
        if(isZero()) return false;
        if(isOne()) return true;
        throw MathFailure.undefined("Only zero or one in Dixon Z2 algebra converts to Boolean");
    }
    @Override public boolean equals(Object other) { return other instanceof RationalDixonZ2 && coordinates.equals(((RationalDixonZ2)other).coordinates); }
    @Override public int hashCode() { return coordinates.hashCode(); }
    @Override public String toString() { return "DixonZ2"+coordinates; }
}
