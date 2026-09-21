package mathematics.algebras;

import mathematics.core.*;
import mathematics.numbers.*;
import static mathematics.core.MathStatus.Membership.MEMBER;

public final class RationalComplexField extends ConcreteAlgebra<RationalComplex> {
    public RationalComplexField(RationalField rationals) {
        super(new Domain<>(Metadata.of("Q(i)","Rational complex field, a proper subfield of C"),RationalComplex.class,x -> MEMBER),rationals.unit());
        closed("add",false,RationalComplex::add); closed("subtract",false,RationalComplex::subtract);
        closed("multiply",false,RationalComplex::multiply); closed("divide",true,RationalComplex::divide);
        unary("negate",domain(),domain(),false,RationalComplex::negate);
        unary("conjugate",domain(),domain(),false,RationalComplex::conjugate);
        unary("norm-squared",domain(),rationals.domain(),false,RationalComplex::normSquared);
        unary("embed-rational",rationals.domain(),domain(),false,q -> new RationalComplex(q,Rational.ZERO));
        constant("zero",new RationalComplex(Rational.ZERO,Rational.ZERO));
        constant("one",new RationalComplex(Rational.ONE,Rational.ZERO));
        law("Q(i) is a commutative field with i*i=-1; no representation of arbitrary complex numbers is claimed.");
    }
}

