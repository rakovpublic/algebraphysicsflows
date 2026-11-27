package mathematics.numbers;

import mathematics.core.MathFailure;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.io.Serializable;
import java.util.*;

/** Exact rational octonions in the Cayley-Dickson basis 1,i,j,k,l,il,jl,kl. */
public final class RationalOctonion implements Serializable {
    private static final long serialVersionUID=1L;
    public static final RationalOctonion ZERO=scalar(Rational.ZERO);
    public static final RationalOctonion ONE=scalar(Rational.ONE);
    public static final RationalOctonion I=basis(1), J=basis(2), K=basis(3), L=basis(4);
    public static final RationalOctonion IL=basis(5), JL=basis(6), KL=basis(7);
    private final List<Rational> coordinates;

    public RationalOctonion(Rational... coordinates) {
        if(coordinates.length!=8) throw new IllegalArgumentException("An octonion has exactly eight rational coordinates");
        List<Rational> copy=new ArrayList<>(Arrays.asList(coordinates.clone()));
        for(Rational coordinate:copy) Objects.requireNonNull(coordinate);
        this.coordinates=Collections.unmodifiableList(copy);
    }
    private static RationalOctonion basis(int index) {
        Rational[] values=new Rational[8]; Arrays.fill(values,Rational.ZERO); values[index]=Rational.ONE;
        return new RationalOctonion(values);
    }
    public static RationalOctonion scalar(Rational value) {
        Rational[] values=new Rational[8]; Arrays.fill(values,Rational.ZERO); values[0]=Objects.requireNonNull(value);
        return new RationalOctonion(values);
    }
    public Rational coordinate(int index) { return coordinates.get(index); }
    public List<Rational> components() { return coordinates; }
    public Rational realPart() { return coordinate(0); }
    public RationalOctonion add(RationalOctonion other) {
        Rational[] out=new Rational[8]; for(int n=0;n<8;n++) out[n]=coordinate(n).add(other.coordinate(n));
        return new RationalOctonion(out);
    }
    public RationalOctonion negate() { return scale(Rational.of(-1)); }
    public RationalOctonion subtract(RationalOctonion other) { return add(other.negate()); }
    public RationalOctonion scale(Rational scalar) {
        Rational[] out=new Rational[8]; for(int n=0;n<8;n++) out[n]=coordinate(n).multiply(scalar);
        return new RationalOctonion(out);
    }
    public RationalOctonion conjugate() {
        Rational[] out=new Rational[8]; out[0]=coordinate(0);
        for(int n=1;n<8;n++) out[n]=coordinate(n).negate();
        return new RationalOctonion(out);
    }
    public Rational normSquared() {
        Rational norm=Rational.ZERO;
        for(Rational value:coordinates) norm=norm.add(value.multiply(value));
        return norm;
    }
    /** Cayley-Dickson product (a,b)(c,d)=(ac-conj(d)b, da+b conj(c)). */
    public RationalOctonion multiply(RationalOctonion other) {
        RationalQuaternion a=quaternion(0), b=quaternion(4);
        RationalQuaternion c=other.quaternion(0), d=other.quaternion(4);
        RationalQuaternion first=a.multiply(c).subtract(d.conjugate().multiply(b));
        RationalQuaternion second=d.multiply(a).add(b.multiply(c.conjugate()));
        return pair(first,second);
    }
    private RationalQuaternion quaternion(int offset) {
        return new RationalQuaternion(coordinate(offset),coordinate(offset+1),coordinate(offset+2),coordinate(offset+3));
    }
    private static RationalOctonion pair(RationalQuaternion first,RationalQuaternion second) {
        return new RationalOctonion(first.real,first.i,first.j,first.k,second.real,second.i,second.j,second.k);
    }
    public RationalOctonion inverse() {
        Rational norm=normSquared();
        if(norm.signum()==0) throw MathFailure.undefined("Octonion zero has no inverse");
        return conjugate().scale(Rational.ONE.divide(norm));
    }
    /** Right quotient x/y=x*inverse(y), valid by the inverse property of octonions. */
    public RationalOctonion divideRight(RationalOctonion other) { return multiply(other.inverse()); }
    /** Left quotient y\\x=inverse(y)*x, valid by the inverse property of octonions. */
    public RationalOctonion divideLeft(RationalOctonion other) { return other.inverse().multiply(this); }
    public RationalOctonion imaginaryPartAsOctonion() {
        Rational[] out=coordinates.toArray(new Rational[0]); out[0]=Rational.ZERO; return new RationalOctonion(out);
    }
    public RationalVector imaginaryPart() { return new RationalVector(coordinates.subList(1,8).toArray(new Rational[0])); }
    public RationalVector toVector() {
        if(realPart().signum()!=0) throw MathFailure.undefined("Only a pure imaginary octonion converts to a seven-dimensional vector");
        return imaginaryPart();
    }
    public static RationalOctonion fromVector(RationalVector vector) {
        if(vector.dimension()!=7) throw MathFailure.undefined("A pure imaginary octonion requires a seven-dimensional vector");
        Rational[] out=new Rational[8]; out[0]=Rational.ZERO;
        for(int n=0;n<7;n++) out[n+1]=vector.get(n);
        return new RationalOctonion(out);
    }
    public Rational toRational() {
        for(int n=1;n<8;n++) if(coordinate(n).signum()!=0) throw MathFailure.undefined("Octonion has non-real components");
        return realPart();
    }
    public static RationalOctonion fromComplex(RationalComplex value) {
        return new RationalOctonion(value.real,value.imaginary,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO);
    }
    public RationalComplex toComplex() {
        for(int n=2;n<8;n++) if(coordinate(n).signum()!=0) throw MathFailure.undefined("Octonion is outside the chosen Q(i) subfield");
        return new RationalComplex(coordinate(0),coordinate(1));
    }
    public static RationalOctonion fromImaginary(RationalImaginary value) {
        return new RationalOctonion(Rational.ZERO,value.coefficient,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO);
    }
    public RationalImaginary toImaginary() {
        if(coordinate(0).signum()!=0) throw MathFailure.undefined("Only a pure imaginary octonion can map to iQ");
        for(int n=2;n<8;n++) if(coordinate(n).signum()!=0) throw MathFailure.undefined("Octonion is outside the chosen i axis");
        return new RationalImaginary(coordinate(1));
    }
    /**
     * Order-3 automorphism of the Cayley-Dickson product: i→j→k→i and il→jl→kl→il, fixing 1 and l.
     * It extends the quaternion automorphism and is not the coordinate cycle of il,jl,kl alone.
     */
    public RationalOctonion orderThreeAutomorphism() {
        return new RationalOctonion(coordinate(0),coordinate(3),coordinate(1),coordinate(2),coordinate(4),coordinate(7),coordinate(5),coordinate(6));
    }
    public static RationalOctonion fromQuaternion(RationalQuaternion value) {
        return new RationalOctonion(value.real,value.i,value.j,value.k,Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO);
    }
    public RationalQuaternion toQuaternion() {
        for(int n=4;n<8;n++) if(coordinate(n).signum()!=0) throw MathFailure.undefined("Octonion is outside the chosen H(Q) subalgebra");
        return quaternion(0);
    }
    /** The regular representation x -> (y -> x*y), with columns in the declared basis order. */
    public RationalMatrix leftMultiplicationMatrix() {
        Rational[][] columns=new Rational[8][8];
        for(int c=0;c<8;c++) {
            RationalOctonion basis=basis(c), product=multiply(basis);
            for(int r=0;r<8;r++) columns[r][c]=product.coordinate(r);
        }
        return new RationalMatrix(columns);
    }
    public static RationalOctonion fromLeftMultiplicationMatrix(RationalMatrix matrix) {
        if(matrix.rows()!=8 || matrix.columns()!=8) throw MathFailure.undefined("An octonion regular representation must be an 8 by 8 matrix");
        RationalOctonion value=new RationalOctonion(matrix.get(0,0),matrix.get(1,0),matrix.get(2,0),matrix.get(3,0),
                matrix.get(4,0),matrix.get(5,0),matrix.get(6,0),matrix.get(7,0));
        if(!value.leftMultiplicationMatrix().equals(matrix)) throw MathFailure.undefined("Matrix is not left multiplication by an octonion in the declared basis");
        return value;
    }
    public boolean isZero() { return equals(ZERO); }
    public boolean isOne() { return equals(ONE); }
    @Override public boolean equals(Object other) { return other instanceof RationalOctonion && coordinates.equals(((RationalOctonion)other).coordinates); }
    @Override public int hashCode() { return coordinates.hashCode(); }
    @Override public String toString() { return "Octonion"+coordinates; }
}
