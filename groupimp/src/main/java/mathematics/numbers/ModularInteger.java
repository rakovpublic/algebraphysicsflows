package mathematics.numbers;

import mathematics.core.MathFailure;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;

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
    @Override public boolean equals(Object b) { return b instanceof ModularInteger && value.equals(((ModularInteger)b).value) && modulus.equals(((ModularInteger)b).modulus); }
    @Override public int hashCode() { return Objects.hash(value,modulus); }
    @Override public String toString() { return value + " (mod " + modulus + ")"; }
}
