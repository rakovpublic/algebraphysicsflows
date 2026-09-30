package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.numbers.*;
import java.math.BigInteger;
import java.util.Arrays;


public final class IntegerRing extends ConcreteAlgebra<BigInteger> {
    public IntegerRing(RationalField rationals,BooleanAlgebra truth) {
        super(carrier("Z",BigInteger.class,"Arbitrary-precision integer ring",x -> true),rationals.unit());
        closed("add",false,BigInteger::add); closed("subtract",false,BigInteger::subtract);
        closed("multiply",false,BigInteger::multiply); closed("gcd",false,BigInteger::gcd);
        closed("lcm",false,NumberTheory::lcm);
        closed("quotient",true,(a,b) -> { nonzero(b); return a.divide(b); });
        closed("remainder",true,(a,b) -> { nonzero(b); return a.remainder(b); });
        unary("negate",algebra(),algebra(),false,BigInteger::negate);
        unary("to-rational",algebra(),rationals.algebra(),false,Rational::of);
        binary("divide-rational",algebra(),algebra(),rationals.algebra(),true,(a,b) -> new Rational(a,b));
        binary("greater",algebra(),algebra(),truth.algebra(),false,(a,b) -> a.compareTo(b)>0);
        binary("equal",algebra(),algebra(),truth.algebra(),false,BigInteger::equals);
        flat("quotient-remainder",algebra(),algebra(),algebra(),true,(a,b) -> { nonzero(b); return Arrays.asList(a.divideAndRemainder(b)); });
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

