package mathematics.numbers;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;

public final class ModularInteger implements Serializable {
    private static final long serialVersionUID = 1L;
    public final BigInteger value, modulus;
    public ModularInteger(BigInteger value, BigInteger modulus) {
        if (Objects.requireNonNull(modulus).compareTo(BigInteger.ONE) <= 0) throw MathFailure.invalid("Modulus must exceed one");
        this.modulus=modulus; this.value=Objects.requireNonNull(value).mod(modulus);
    }
    public ModularInteger(long value, long modulus) { this(BigInteger.valueOf(value),BigInteger.valueOf(modulus)); }
    private void compatible(ModularInteger b) { if (!modulus.equals(b.modulus)) throw MathFailure.invalid("Different residue rings"); }
    public ModularInteger add(ModularInteger b) { compatible(b); return new ModularInteger(value.add(b.value),modulus); }
    public ModularInteger negate() { return new ModularInteger(value.negate(),modulus); }
    public ModularInteger multiply(ModularInteger b) { compatible(b); return new ModularInteger(value.multiply(b.value),modulus); }
    public ModularInteger inverse() {
        if (!value.gcd(modulus).equals(BigInteger.ONE)) throw MathFailure.undefined("Residue is not a unit");
        return new ModularInteger(value.modInverse(modulus),modulus);
    }
    public boolean isUnit() { return value.gcd(modulus).equals(BigInteger.ONE); }
    /** Nonzero zero divisors only; zero itself is excluded by this convention. */
    public boolean isZeroDivisor() { return value.signum()!=0 && !isUnit(); }
    public ModularInteger power(BigInteger exponent) {
        Objects.requireNonNull(exponent);
        if(exponent.signum()<0) return inverse().power(exponent.negate());
        return new ModularInteger(value.modPow(exponent,modulus),modulus);
    }
    /** All x satisfying this*x=rhs in the same residue ring, sorted by canonical representative. */
    public List<ModularInteger> solveMultiply(ModularInteger rhs) {
        compatible(rhs);
        BigInteger gcd=value.gcd(modulus);
        if(rhs.value.mod(gcd).signum()!=0) return Collections.emptyList();
        if(gcd.compareTo(BigInteger.valueOf(10000))>0)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Congruence solution materialization is limited to 10000 members");
        BigInteger step=modulus.divide(gcd);
        BigInteger first=step.equals(BigInteger.ONE)?BigInteger.ZERO
                :value.divide(gcd).modInverse(step).multiply(rhs.value.divide(gcd)).mod(step);
        List<ModularInteger> result=new ArrayList<>();
        for(int i=0;i<gcd.intValueExact();i++) result.add(new ModularInteger(first.add(step.multiply(BigInteger.valueOf(i))),modulus));
        return Collections.unmodifiableList(result);
    }
    @Override public boolean equals(Object b) { return b instanceof ModularInteger && value.equals(((ModularInteger)b).value) && modulus.equals(((ModularInteger)b).modulus); }
    @Override public int hashCode() { return Objects.hash(value,modulus); }
    @Override public String toString() { return value + " (mod " + modulus + ")"; }
}
