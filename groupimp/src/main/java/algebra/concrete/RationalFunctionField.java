package algebra.concrete;

import mathematics.calculus.RationalFunction;

/** Exact formal rational functions Q(x); evaluation is partial at poles of the canonical fraction. */
public final class RationalFunctionField extends ConcreteAlgebra<RationalFunction> {
    public RationalFunctionField(RationalPolynomialRing polynomials,RationalField rationals,BooleanAlgebra truth) {
        super(carrier("Q(x)",RationalFunction.class,"Univariate rational function field over Q",f -> true),rationals.unit());
        closed("add",false,RationalFunction::add);
        closed("subtract",false,RationalFunction::subtract);
        closed("multiply",false,RationalFunction::multiply);
        closed("divide",true,RationalFunction::divide);
        closed("compose",true,RationalFunction::compose);
        unary("negate",algebra(),algebra(),false,RationalFunction::negate);
        unary("inverse",algebra(),algebra(),true,RationalFunction::inverse);
        unary("derivative",algebra(),algebra(),false,RationalFunction::derivative);
        binary("evaluate",algebra(),rationals.algebra(),rationals.algebra(),true,RationalFunction::evaluate);
        unary("numerator",algebra(),polynomials.algebra(),false,RationalFunction::numerator);
        unary("denominator",algebra(),polynomials.algebra(),false,RationalFunction::denominator);
        unary("embed-polynomial",polynomials.algebra(),algebra(),false,RationalFunction::of);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalFunction::equals);
        constant("zero",RationalFunction.ZERO); constant("one",RationalFunction.ONE);
        law("Q(x) is a field of formal fractions; denominator is nonzero, coprime to the numerator and monic.");
        law("Derivative satisfies the quotient rule; evaluation is undefined at poles of the reduced denominator.");
        law("compose means f(g(x)); substituting a constant pole is undefined. Cancelled factors do not retain domain exclusions.");
    }
}
