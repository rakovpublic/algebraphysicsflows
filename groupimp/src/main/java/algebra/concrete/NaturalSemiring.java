package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import java.math.BigInteger;


public final class NaturalSemiring extends ConcreteAlgebra<BigInteger> {
    public NaturalSemiring(IntegerRing integers) {
        super(carrier("N",BigInteger.class,"Natural-number semiring including zero",n -> n.signum()>=0),integers.unit());
        closed("add",false,BigInteger::add); closed("multiply",false,BigInteger::multiply);
        unary("successor",algebra(),algebra(),false,n -> n.add(BigInteger.ONE));
        unary("to-integer",algebra(),integers.algebra(),false,n -> n);
        constant("zero",BigInteger.ZERO); constant("one",BigInteger.ONE);
        law("Addition and multiplication form a commutative semiring with zero and one.");
    }
}

