package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.numbers.*;


public final class RationalComplexField extends ConcreteAlgebra<RationalComplex> {
    public RationalComplexField(RationalField rationals) {
        super(carrier("Q(i)",RationalComplex.class,"Rational complex field, a proper subfield of C",x -> true),rationals.unit());
        closed("add",false,RationalComplex::add); closed("subtract",false,RationalComplex::subtract);
        closed("multiply",false,RationalComplex::multiply); closed("divide",true,RationalComplex::divide);
        unary("negate",algebra(),algebra(),false,RationalComplex::negate);
        unary("conjugate",algebra(),algebra(),false,RationalComplex::conjugate);
        unary("norm-squared",algebra(),rationals.algebra(),false,RationalComplex::normSquared);
        unary("embed-rational",rationals.algebra(),algebra(),false,q -> new RationalComplex(q,Rational.ZERO));
        constant("zero",new RationalComplex(Rational.ZERO,Rational.ZERO));
        constant("one",new RationalComplex(Rational.ONE,Rational.ZERO));
        law("Q(i) is a commutative field with i*i=-1; no representation of arbitrary complex numbers is claimed.");
    }
}

