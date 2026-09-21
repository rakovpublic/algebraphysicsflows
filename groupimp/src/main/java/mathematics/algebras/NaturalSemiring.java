package mathematics.algebras;

import mathematics.core.*;
import java.math.BigInteger;
import static mathematics.core.MathStatus.Membership.*;

public final class NaturalSemiring extends ConcreteAlgebra<BigInteger> {
    public NaturalSemiring(IntegerRing integers) {
        super(new Domain<>(Metadata.of("N","Natural-number semiring including zero"),BigInteger.class,n -> n.signum()>=0?MEMBER:NOT_MEMBER),integers.unit());
        closed("add",false,BigInteger::add); closed("multiply",false,BigInteger::multiply);
        unary("successor",domain(),domain(),false,n -> n.add(BigInteger.ONE));
        unary("to-integer",domain(),integers.domain(),false,n -> n);
        constant("zero",BigInteger.ZERO); constant("one",BigInteger.ONE);
        law("Addition and multiplication form a commutative semiring with zero and one.");
    }
}

