package algebra.concrete;

import mathematics.calculus.Polynomial;
import mathematics.calculus.RationalFunction;
import mathematics.numbers.*;
import java.math.BigInteger;

/** Exact division operations on the rational-function subring of a twisted HMN completion. */
public final class RationalTwistedHahnMalcevNeumannAlgebra extends ConcreteAlgebra<RationalTwistedHahnSeries> {
    public RationalTwistedHahnMalcevNeumannAlgebra(RationalField rationals,RationalPolynomialRing polynomials,
            RationalFunctionField functions,IntegerRing integers,RationalComplexField complex,
            RationalImaginaryAlgebra imaginaries,RationalQuaternionAlgebra quaternions,
            RationalOctonionAlgebra octonions,RationalDixonZ2Algebra dixon,BooleanAlgebra truth) {
        super(carrier("HMN(H(Q);Z,sigma,1)",RationalTwistedHahnSeries.class,
                "Rational-function division subring of H(Q)((Z;sigma,1)); sigma(q)=i*q*i^-1 and the group variable has valuation one",x -> true),rationals.unit());
        closed("add",false,RationalTwistedHahnSeries::add);
        closed("subtract",false,RationalTwistedHahnSeries::subtract);
        closed("multiply",false,RationalTwistedHahnSeries::multiply);
        closed("divide-right",true,RationalTwistedHahnSeries::divideRight);
        closed("divide-left",true,RationalTwistedHahnSeries::divideLeft);
        unary("negate",algebra(),algebra(),false,RationalTwistedHahnSeries::negate);
        unary("conjugate",algebra(),algebra(),false,RationalTwistedHahnSeries::conjugate);
        unary("inverse",algebra(),algebra(),true,RationalTwistedHahnSeries::inverse);
        unary("sigma",algebra(),algebra(),false,RationalTwistedHahnSeries::applySigma);
        unary("norm-squared",algebra(),functions.algebra(),false,RationalTwistedHahnSeries::normSquared);
        unary("group-valuation",algebra(),integers.algebra(),true,RationalTwistedHahnSeries::groupValuation);
        unary("leading-coefficient",algebra(),quaternions.algebra(),true,RationalTwistedHahnSeries::leadingCoefficient);
        unary("to-boolean",algebra(),truth.algebra(),true,RationalTwistedHahnSeries::toBoolean);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalTwistedHahnSeries::equals);

        unary("group-basis",integers.algebra(),algebra(),false,RationalTwistedHahnSeries::groupElement);
        unary("embed-rational",rationals.algebra(),algebra(),false,RationalTwistedHahnSeries::fromRational);
        unary("to-rational",algebra(),rationals.algebra(),true,RationalTwistedHahnSeries::toRational);
        unary("embed-polynomial",polynomials.algebra(),algebra(),false,RationalTwistedHahnSeries::fromPolynomial);
        unary("to-polynomial",algebra(),polynomials.algebra(),true,RationalTwistedHahnSeries::toPolynomial);
        unary("embed-rational-function",functions.algebra(),algebra(),false,RationalTwistedHahnSeries::fromRationalFunction);
        unary("to-rational-function",algebra(),functions.algebra(),true,RationalTwistedHahnSeries::toRationalFunction);
        unary("embed-complex",complex.algebra(),algebra(),false,RationalTwistedHahnSeries::fromComplex);
        unary("to-complex",algebra(),complex.algebra(),true,RationalTwistedHahnSeries::toComplex);
        unary("embed-imaginary",imaginaries.algebra(),algebra(),false,RationalTwistedHahnSeries::fromImaginary);
        unary("to-imaginary",algebra(),imaginaries.algebra(),true,RationalTwistedHahnSeries::toImaginary);
        unary("embed-quaternion",quaternions.algebra(),algebra(),false,RationalTwistedHahnSeries::fromQuaternion);
        unary("to-quaternion",algebra(),quaternions.algebra(),true,RationalTwistedHahnSeries::toQuaternion);
        unary("embed-octonion",octonions.algebra(),algebra(),true,RationalTwistedHahnSeries::fromOctonion);
        unary("to-octonion",algebra(),octonions.algebra(),false,RationalTwistedHahnSeries::toOctonion);
        unary("embed-dixon-z2",dixon.algebra(),algebra(),true,RationalTwistedHahnSeries::fromDixonZ2);
        unary("to-dixon-z2",algebra(),dixon.algebra(),true,RationalTwistedHahnSeries::toDixonZ2);
        constant("zero",RationalTwistedHahnSeries.ZERO_VALUE);
        constant("one",RationalTwistedHahnSeries.ONE_VALUE);
        constant("group-generator",RationalTwistedHahnSeries.GROUP_GENERATOR);
        constant("tau-one",RationalTwistedHahnSeries.ONE_VALUE);
        law("The exact base division ring is K=H(Q), G=(Z,+) with its usual order, sigma_n=Ad(i)^n, and normalized tau(m,n)=1.");
        law("The group basis X satisfies X*q=sigma(q)*X. Set t=i^-1*X; t is central and the rational subring is the quaternion division algebra H(Q(t)).");
        law("A represented value a+b*i+(c+d*i)X has four exact Q(t) coordinates. It embeds into the full formal Hahn-Mal'cev-Neumann division ring, whose arbitrary well-ordered series are not all represented by this rational-function carrier.");
        law("H(Q) embeds as constants; Q, Q(i), iQ and polynomial/rational-function scalars have checked constant projections. O(Q) and DixonZ2(Q) connect only through their selected H(Q) subalgebra; their full nonassociative carriers cannot embed as subrings of an associative division ring.");
        law("Group valuation is the least exponent in the induced formal Laurent expansion; the leading coefficient is returned as an actual H(Q) Algebra item. Zero has no valuation or leading coefficient.");
    }
}
