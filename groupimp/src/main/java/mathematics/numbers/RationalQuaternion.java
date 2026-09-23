package mathematics.numbers;

import mathematics.core.MathFailure;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.io.Serializable;
import java.util.*;

/** Hamilton quaternions with exact rational coefficients; multiplication is noncommutative. */
public final class RationalQuaternion implements Serializable {
    private static final long serialVersionUID=1L;
    public static final RationalQuaternion ZERO=new RationalQuaternion(Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ZERO);
    public static final RationalQuaternion ONE=new RationalQuaternion(Rational.ONE,Rational.ZERO,Rational.ZERO,Rational.ZERO);
    public static final RationalQuaternion I=new RationalQuaternion(Rational.ZERO,Rational.ONE,Rational.ZERO,Rational.ZERO);
    public static final RationalQuaternion J=new RationalQuaternion(Rational.ZERO,Rational.ZERO,Rational.ONE,Rational.ZERO);
    public static final RationalQuaternion K=new RationalQuaternion(Rational.ZERO,Rational.ZERO,Rational.ZERO,Rational.ONE);
    public final Rational real,i,j,k;
    public RationalQuaternion(Rational real,Rational i,Rational j,Rational k) {
        this.real=Objects.requireNonNull(real); this.i=Objects.requireNonNull(i); this.j=Objects.requireNonNull(j); this.k=Objects.requireNonNull(k);
    }
    public static RationalQuaternion scalar(Rational value) { return new RationalQuaternion(value,Rational.ZERO,Rational.ZERO,Rational.ZERO); }
    public RationalQuaternion add(RationalQuaternion b) { return new RationalQuaternion(real.add(b.real),i.add(b.i),j.add(b.j),k.add(b.k)); }
    public RationalQuaternion negate() { return scale(Rational.of(-1)); }
    public RationalQuaternion subtract(RationalQuaternion b) { return add(b.negate()); }
    public RationalQuaternion scale(Rational value) { return new RationalQuaternion(real.multiply(value),i.multiply(value),j.multiply(value),k.multiply(value)); }
    public RationalQuaternion conjugate() { return new RationalQuaternion(real,i.negate(),j.negate(),k.negate()); }
    public Rational normSquared() { return real.multiply(real).add(i.multiply(i)).add(j.multiply(j)).add(k.multiply(k)); }
    public RationalQuaternion multiply(RationalQuaternion b) {
        return new RationalQuaternion(
                real.multiply(b.real).subtract(i.multiply(b.i)).subtract(j.multiply(b.j)).subtract(k.multiply(b.k)),
                real.multiply(b.i).add(i.multiply(b.real)).add(j.multiply(b.k)).subtract(k.multiply(b.j)),
                real.multiply(b.j).subtract(i.multiply(b.k)).add(j.multiply(b.real)).add(k.multiply(b.i)),
                real.multiply(b.k).add(i.multiply(b.j)).subtract(j.multiply(b.i)).add(k.multiply(b.real)));
    }
    public RationalQuaternion inverse() {
        Rational norm=normSquared(); if(norm.signum()==0) throw MathFailure.undefined("Quaternion zero has no inverse");
        return conjugate().scale(Rational.ONE.divide(norm));
    }
    /** a.divideRight(b) = a*b^-1, the solution x to x*b=a. */
    public RationalQuaternion divideRight(RationalQuaternion b) { return multiply(b.inverse()); }
    /** a.divideLeft(b) = b^-1*a, the solution x to b*x=a. */
    public RationalQuaternion divideLeft(RationalQuaternion b) { return b.inverse().multiply(this); }
    public List<Rational> components() { return Collections.unmodifiableList(Arrays.asList(real,i,j,k)); }
    public RationalVector imaginaryPart() { return new RationalVector(i,j,k); }
    public static RationalQuaternion fromVector(RationalVector vector) {
        if(vector.dimension()!=3) throw MathFailure.undefined("A pure quaternion requires a three-dimensional vector");
        return new RationalQuaternion(Rational.ZERO,vector.get(0),vector.get(1),vector.get(2));
    }
    public RationalVector toVector() {
        if(real.signum()!=0) throw MathFailure.undefined("Only a pure quaternion converts to a vector"); return imaginaryPart();
    }
    public Rational toRational() {
        if(i.signum()!=0 || j.signum()!=0 || k.signum()!=0) throw MathFailure.undefined("Quaternion has non-real components"); return real;
    }
    public static RationalQuaternion fromComplex(RationalComplex value) { return new RationalQuaternion(value.real,value.imaginary,Rational.ZERO,Rational.ZERO); }
    public RationalComplex toComplex() {
        if(j.signum()!=0 || k.signum()!=0) throw MathFailure.undefined("Quaternion is outside the chosen Q(i) subfield");
        return new RationalComplex(real,i);
    }
    /** Active right-handed rotation of a column vector by q*(0,v)*q^-1; no unit normalization is required. */
    public RationalVector rotate(RationalVector vector) { return multiply(fromVector(vector)).multiply(inverse()).toVector(); }
    public RationalMatrix toRotationMatrix() {
        Rational norm=normSquared(); if(norm.signum()==0) throw MathFailure.undefined("Quaternion zero does not represent a rotation");
        Rational w2=real.multiply(real),x2=i.multiply(i),y2=j.multiply(j),z2=k.multiply(k),two=Rational.of(2);
        Rational[][] entries={
                {w2.add(x2).subtract(y2).subtract(z2),i.multiply(j).subtract(real.multiply(k)).multiply(two),i.multiply(k).add(real.multiply(j)).multiply(two)},
                {i.multiply(j).add(real.multiply(k)).multiply(two),w2.subtract(x2).add(y2).subtract(z2),j.multiply(k).subtract(real.multiply(i)).multiply(two)},
                {i.multiply(k).subtract(real.multiply(j)).multiply(two),j.multiply(k).add(real.multiply(i)).multiply(two),w2.subtract(x2).subtract(y2).add(z2)}};
        for(int r=0;r<3;r++) for(int c=0;c<3;c++) entries[r][c]=entries[r][c].divide(norm);
        return new RationalMatrix(entries);
    }
    /** Canonical representative of a nonzero rational projective quaternion: first nonzero component is one. */
    private RationalQuaternion projective() {
        for(Rational value : components()) if(value.signum()!=0) return scale(Rational.ONE.divide(value));
        throw MathFailure.undefined("Quaternion zero does not represent a rotation");
    }
    public boolean sameRotation(RationalQuaternion other) { return projective().equals(other.projective()); }
    /** Exact inverse on rational SO(3), choosing a projective representative, not necessarily a unit quaternion. */
    public static RationalQuaternion fromRotationMatrix(RationalMatrix matrix) {
        if(matrix.rows()!=3 || matrix.columns()!=3 || !matrix.transpose().multiply(matrix).equals(RationalMatrix.identity(3))
                || !matrix.determinant().equals(Rational.ONE)) throw MathFailure.undefined("A rational orthogonal 3 by 3 matrix with determinant one is required");
        Rational w=matrix.trace().add(Rational.ONE);
        if(w.signum()!=0) return new RationalQuaternion(w,matrix.get(2,1).subtract(matrix.get(1,2)),
                matrix.get(0,2).subtract(matrix.get(2,0)),matrix.get(1,0).subtract(matrix.get(0,1))).projective();
        // A half-turn has trace -1; any nonzero column of R+I supplies its rational rotation axis.
        RationalMatrix axis=matrix.add(RationalMatrix.identity(3));
        for(int c=0;c<3;c++) {
            RationalQuaternion candidate=fromVector(axis.column(c));
            if(candidate.normSquared().signum()!=0) return candidate.projective();
        }
        throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Validated half-turn has no rotation axis");
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RationalQuaternion)) return false; RationalQuaternion b=(RationalQuaternion)other;
        return real.equals(b.real) && i.equals(b.i) && j.equals(b.j) && k.equals(b.k);
    }
    @Override public int hashCode() { return Objects.hash(real,i,j,k); }
    @Override public String toString() { return "Quaternion"+components(); }
}
