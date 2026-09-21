package mathematics.algebras;

import mathematics.core.*;
import mathematics.numbers.*;
import java.math.BigInteger;
import java.util.Arrays;
import static mathematics.core.MathStatus.Membership.MEMBER;

public final class IntegerRing extends ConcreteAlgebra<BigInteger> {
    public IntegerRing(RationalField rationals,BooleanAlgebra truth) {
        super(new Domain<>(Metadata.of("Z","Arbitrary-precision integer ring"),BigInteger.class,x -> MEMBER),rationals.unit());
        closed("add",false,BigInteger::add); closed("subtract",false,BigInteger::subtract);
        closed("multiply",false,BigInteger::multiply); closed("gcd",false,BigInteger::gcd);
        closed("lcm",false,NumberTheory::lcm);
        closed("quotient",true,(a,b) -> { nonzero(b); return a.divide(b); });
        closed("remainder",true,(a,b) -> { nonzero(b); return a.remainder(b); });
        unary("negate",domain(),domain(),false,BigInteger::negate);
        unary("to-rational",domain(),rationals.domain(),false,Rational::of);
        binary("divide-rational",domain(),domain(),rationals.domain(),true,(a,b) -> new Rational(a,b));
        binary("greater",domain(),domain(),truth.domain(),false,(a,b) -> a.compareTo(b)>0);
        binary("equal",domain(),domain(),truth.domain(),false,BigInteger::equals);
        flat("quotient-remainder",domain(),domain(),domain(),true,(a,b) -> { nonzero(b); return Arrays.asList(a.divideAndRemainder(b)); });
        constant("zero",BigInteger.ZERO); constant("one",BigInteger.ONE);
        law("Z is a commutative unital ring with no zero divisors; it is not a field.");
        law("For b != 0, a = b*quotient(a,b)+remainder(a,b); quotient truncates toward zero.");
    }
    private static void nonzero(BigInteger value) {
        if(value.signum()==0) throw MathFailure.undefined("Integer division by zero");
    }
    public BigInteger zero() { return BigInteger.ZERO; }
    public BigInteger one() { return BigInteger.ONE; }
}

