package mathematics.algebras;

import mathematics.core.*;
import mathematics.numbers.Rational;
import java.util.Arrays;
import static mathematics.core.MathStatus.Membership.MEMBER;

/** Exact Q with ordinary legacy add/subtract/multiply/divide operations. */
public final class RationalField extends ConcreteAlgebra<Rational> {
    public RationalField(BooleanAlgebra truth) {
        super(new Domain<>(Metadata.of("Q","Exact rational field"),Rational.class,x -> MEMBER),truth.unit());
        closed("add",false,Rational::add); closed("subtract",false,Rational::subtract);
        closed("multiply",false,Rational::multiply); closed("divide",true,Rational::divide);
        unary("negate",domain(),domain(),false,Rational::negate);
        unary("inverse",domain(),domain(),true,q -> Rational.ONE.divide(q));
        binary("greater",domain(),domain(),truth.domain(),false,(a,b) -> a.compareTo(b)>0);
        binary("equal",domain(),domain(),truth.domain(),false,Rational::equals);
        flat("add-subtract",domain(),domain(),domain(),false,(a,b) -> Arrays.asList(a.add(b),a.subtract(b)));
        constant("zero",Rational.ZERO); constant("one",Rational.ONE);
        law("Addition is an abelian group with zero and negation.");
        law("Multiplication is commutative, associative and distributive over addition, with identity one.");
        law("Every nonzero rational has a multiplicative inverse; division by zero is undefined.");
    }
    public Rational zero() { return Rational.ZERO; }
    public Rational one() { return Rational.ONE; }
}

