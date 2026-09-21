package mathematics.numbers;

import mathematics.core.*;
import java.math.BigInteger;
import static mathematics.core.MathStatus.Membership.*;

public final class NumberDomains {
    private NumberDomains() { }
    public static final Domain<BigInteger> NATURALS=new Domain<>(Metadata.of("N","Nonnegative arbitrary precision integers; includes zero"),BigInteger.class,n -> n.signum()>=0 ? MEMBER : NOT_MEMBER);
    public static final Domain<BigInteger> INTEGERS=new Domain<>(Metadata.of("Z","Arbitrary precision signed integers"),BigInteger.class,n -> MEMBER);
    public static final Domain<Rational> RATIONALS=new Domain<>(Metadata.of("Q","Canonical exact rational numbers"),Rational.class,n -> MEMBER);
    public static final Domain<RationalComplex> RATIONAL_COMPLEX=new Domain<>(Metadata.of("Q(i)","Exact rational complex subfield, not all C"),RationalComplex.class,n -> MEMBER);
    public static final Domain<SymbolicReal> REAL_EXPRESSIONS=new Domain<>(Metadata.of("RealExpression","Finite rational/pi/e/radical expressions, not all R; structural equality only"),SymbolicReal.class,n -> MEMBER);
    public static final Domain<Boolean> BOOLEAN=new Domain<>(Metadata.of("Boolean","Two truth values"),Boolean.class,n -> MEMBER);
    public static Domain<ModularInteger> residues(int modulus) {
        if (modulus <= 1) throw MathFailure.invalid("Modulus must exceed one");
        return new Domain<>(Metadata.of("Z/" + modulus + "Z","Residues with fixed modulus " + modulus),ModularInteger.class,n -> n.modulus.equals(BigInteger.valueOf(modulus)) ? MEMBER : NOT_MEMBER);
    }
    public static Domain<ModularInteger> primeField(int prime) {
        if (!NumberTheory.isPrime(prime)) throw MathFailure.invalid("Field modulus must be prime");
        return residues(prime);
    }
}
