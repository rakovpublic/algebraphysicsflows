package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.util.Arrays;


/** Exact Q with ordinary legacy add/subtract/multiply/divide operations. */
public final class RationalField extends ConcreteAlgebra<Rational> {
    public RationalField(BooleanAlgebra truth) {
        super(carrier("Q",Rational.class,"Exact rational field",x -> true),truth.unit());
        closed("add",false,Rational::add); closed("subtract",false,Rational::subtract);
        closed("multiply",false,Rational::multiply); closed("divide",true,Rational::divide);
        unary("negate",algebra(),algebra(),false,Rational::negate);
        unary("inverse",algebra(),algebra(),true,q -> Rational.ONE.divide(q));
        binary("greater",algebra(),algebra(),truth.algebra(),false,(a,b) -> a.compareTo(b)>0);
        binary("equal",algebra(),algebra(),truth.algebra(),false,Rational::equals);
        flat("add-subtract",algebra(),algebra(),algebra(),false,(a,b) -> Arrays.asList(a.add(b),a.subtract(b)));
        constant("zero",Rational.ZERO); constant("one",Rational.ONE);
        law("Addition is an abelian group with zero and negation.");
        law("Multiplication is commutative, associative and distributive over addition, with identity one.");
        law("Every nonzero rational has a multiplicative inverse; division by zero is undefined.");
    }
    public Rational zero() { return Rational.ZERO; }
    public Rational one() { return Rational.ONE; }
}

