package algebra.concrete;

import mathematics.calculus.MultivariatePolynomial;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.ArrayList;

/** Family of rational polynomial rings with an explicit ordered input space per member. */
public final class RationalMultivariatePolynomialAlgebra extends ConcreteAlgebra<MultivariatePolynomial> {
    public RationalMultivariatePolynomialAlgebra(RationalField rationals,NaturalSemiring naturals,IntegerRing integers,
            BooleanAlgebra truth,RationalVectorFamily vectors,RationalMatrixFamily matrices,RationalPolynomialRing univariate) {
        super(carrier("Poly(Q)",MultivariatePolynomial.class,"Sparse rational polynomials in ordered finite positive input dimensions",p -> true),rationals.unit());
        closed("add",true,MultivariatePolynomial::add);
        closed("subtract",true,MultivariatePolynomial::subtract);
        closed("multiply",true,MultivariatePolynomial::multiply);
        unary("negate",algebra(),algebra(),false,MultivariatePolynomial::negate);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,MultivariatePolynomial::scale);
        unary("variable-count",algebra(),naturals.algebra(),false,p -> BigInteger.valueOf(p.variableCount()));
        unary("degree",algebra(),integers.algebra(),false,p -> BigInteger.valueOf(p.degree()));
        unary("term-count",algebra(),naturals.algebra(),false,p -> BigInteger.valueOf(p.coefficients().size()));
        unary("is-zero",algebra(),truth.algebra(),false,MultivariatePolynomial::isZero);
        binary("equal",algebra(),algebra(),truth.algebra(),false,MultivariatePolynomial::equals);
        binary("evaluate",algebra(),vectors.algebra(),rationals.algebra(),true,MultivariatePolynomial::evaluate);
        binary("partial",algebra(),naturals.algebra(),algebra(),true,(p,axis) -> p.partial(p.axis(axis)));
        unaryFlat("partials",algebra(),algebra(),false,MultivariatePolynomial::partials);
        binary("directional",algebra(),vectors.algebra(),algebra(),true,MultivariatePolynomial::directional);
        binary("gradient-at",algebra(),vectors.algebra(),vectors.algebra(),true,MultivariatePolynomial::gradientAt);
        binary("hessian-at",algebra(),vectors.algebra(),matrices.algebra(),true,MultivariatePolynomial::hessianAt);
        unary("laplacian",algebra(),algebra(),false,MultivariatePolynomial::laplacian);
        binary("primitive",algebra(),naturals.algebra(),algebra(),true,(p,axis) -> p.primitive(p.axis(axis)));
        binary("pow",algebra(),naturals.algebra(),algebra(),false,MultivariatePolynomial::pow);
        unaryFlat("terms",algebra(),algebra(),false,MultivariatePolynomial::terms);
        unaryFlat("coefficients",algebra(),rationals.algebra(),false,p -> new ArrayList<>(p.coefficients().values()));
        unary("zero-like",algebra(),algebra(),false,p -> MultivariatePolynomial.constant(p.variableCount(),Rational.ZERO));
        unary("one-like",algebra(),algebra(),false,p -> MultivariatePolynomial.constant(p.variableCount(),Rational.ONE));
        unary("from-univariate",univariate.algebra(),algebra(),false,MultivariatePolynomial::fromUnivariate);
        unary("to-univariate",algebra(),univariate.algebra(),true,MultivariatePolynomial::toUnivariate);
        unary("constant-part",algebra(),rationals.algebra(),false,MultivariatePolynomial::constantPart);
        unary("to-rational",algebra(),rationals.algebra(),true,MultivariatePolynomial::toRational);
        unaryFlat("variables",algebra(),algebra(),false,MultivariatePolynomial::variables);
        unary("integrate-unit-cube",algebra(),rationals.algebra(),false,MultivariatePolynomial::integrateUnitCube);
        law("For each fixed input dimension these polynomials form a commutative Q-algebra; arithmetic requires equal dimensions.");
        law("Partial derivatives commute and satisfy the product rule; directional differentiation is linear in the unnormalized direction.");
        law("Gradient coordinates follow variable order; Hessian rows and columns follow that same order; its trace equals the Laplacian.");
        law("Primitive sets the polynomial independent of the selected variable to zero; differentiating it in that variable recovers the input.");
    }
}
