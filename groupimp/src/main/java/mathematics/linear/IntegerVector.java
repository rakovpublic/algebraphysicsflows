package mathematics.linear;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

/** Immutable coordinates in a finite free Z-module, including dimension zero. */
public final class IntegerVector implements Serializable {
    private static final long serialVersionUID=1L;
    private final List<BigInteger> entries;
    public IntegerVector(BigInteger... values) {
        if(values.length>IntegerSmithNormalForm.MAX_DIMENSION) throw IntegerMatrix.limit();
        List<BigInteger> copy=new ArrayList<>(Arrays.asList(values));
        for(BigInteger value : copy) Objects.requireNonNull(value);
        entries=Collections.unmodifiableList(copy);
    }
    public int dimension() { return entries.size(); }
    public BigInteger get(int index) { return entries.get(index); }
    public List<BigInteger> entries() { return entries; }
    public static IntegerVector zero(int dimension) {
        IntegerMatrix.checkDimension(dimension); BigInteger[] values=new BigInteger[dimension];
        Arrays.fill(values,BigInteger.ZERO); return new IntegerVector(values);
    }
    private void same(IntegerVector other) {
        if(dimension()!=other.dimension()) throw MathFailure.undefined("Integer vector dimensions must agree");
    }
    public IntegerVector add(IntegerVector other) {
        same(other); BigInteger[] values=new BigInteger[dimension()];
        for(int i=0;i<values.length;i++) values[i]=get(i).add(other.get(i)); return new IntegerVector(values);
    }
    public IntegerVector scale(BigInteger scalar) {
        BigInteger[] values=new BigInteger[dimension()];
        for(int i=0;i<values.length;i++) values[i]=get(i).multiply(scalar); return new IntegerVector(values);
    }
    public BigInteger dot(IntegerVector other) {
        same(other); BigInteger value=BigInteger.ZERO;
        for(int i=0;i<dimension();i++) value=value.add(get(i).multiply(other.get(i))); return value;
    }
    public RationalVector toRational() {
        Rational[] values=new Rational[dimension()]; for(int i=0;i<values.length;i++) values[i]=Rational.of(get(i));
        return new RationalVector(values);
    }
    public static IntegerVector fromRational(RationalVector vector) {
        IntegerMatrix.checkDimension(vector.dimension()); BigInteger[] values=new BigInteger[vector.dimension()];
        for(int i=0;i<values.length;i++) {
            if(!vector.get(i).denominator().equals(BigInteger.ONE)) throw MathFailure.undefined("Every coordinate must be integral");
            values[i]=vector.get(i).numerator();
        }
        return new IntegerVector(values);
    }
    @Override public boolean equals(Object other) { return other instanceof IntegerVector && entries.equals(((IntegerVector)other).entries); }
    @Override public int hashCode() { return entries.hashCode(); }
    @Override public String toString() { return entries.toString(); }
}
