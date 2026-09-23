package algebra.concrete;

import mathematics.calculus.PolynomialMap;
import mathematics.calculus.MultivariatePolynomial;
import java.math.BigInteger;

/** Polynomial maps and coordinate vector calculus through the existing native operation interfaces. */
public final class RationalPolynomialMapAlgebra extends ConcreteAlgebra<PolynomialMap> {
    public RationalPolynomialMapAlgebra(RationalMultivariatePolynomialAlgebra polynomials,RationalField rationals,
            NaturalSemiring naturals,BooleanAlgebra truth,RationalVectorFamily vectors,RationalMatrixFamily matrices) {
        super(carrier("PolynomialMap(Q)",PolynomialMap.class,"Exact rational polynomial maps between explicit positive coordinate dimensions",p -> true),rationals.unit());
        closed("add",true,PolynomialMap::add);
        closed("subtract",true,PolynomialMap::subtract);
        unary("negate",algebra(),algebra(),false,PolynomialMap::negate);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,PolynomialMap::scale);
        closed("compose",true,PolynomialMap::compose);
        binary("evaluate",algebra(),vectors.algebra(),vectors.algebra(),true,PolynomialMap::evaluate);
        binary("partial",algebra(),naturals.algebra(),algebra(),true,(map,axis) -> map.partial(map.axis(axis)));
        unaryFlat("components",algebra(),polynomials.algebra(),false,PolynomialMap::components);
        binary("component",algebra(),naturals.algebra(),polynomials.algebra(),true,PolynomialMap::component);
        unary("input-dimension",algebra(),naturals.algebra(),false,p -> BigInteger.valueOf(p.inputDimension()));
        unary("output-dimension",algebra(),naturals.algebra(),false,p -> BigInteger.valueOf(p.outputDimension()));
        binary("jacobian-at",algebra(),vectors.algebra(),matrices.algebra(),true,PolynomialMap::jacobianAt);
        unary("divergence",algebra(),polynomials.algebra(),true,PolynomialMap::divergence);
        unary("curl",algebra(),algebra(),true,PolynomialMap::curl);
        unary("from-polynomial",polynomials.algebra(),algebra(),false,p -> new PolynomialMap(p));
        unary("to-polynomial",algebra(),polynomials.algebra(),true,PolynomialMap::toPolynomial);
        unary("gradient",polynomials.algebra(),algebra(),false,PolynomialMap::gradient);
        unary("identity-on-input",algebra(),algebra(),false,p -> PolynomialMap.identity(p.inputDimension()));
        unary("from-matrix",matrices.algebra(),algebra(),false,PolynomialMap::fromMatrix);
        unary("linear-part",algebra(),matrices.algebra(),false,PolynomialMap::linearPart);
        unary("constant-part",algebra(),vectors.algebra(),false,PolynomialMap::constantPart);
        binary("equal",algebra(),algebra(),truth.algebra(),false,PolynomialMap::equals);
        binary("substitute",polynomials.algebra(),algebra(),polynomials.algebra(),true,MultivariatePolynomial::substitute);
        law("Composition applies the right operand first; its Jacobian obeys J(F o G)(x)=J(F)(G(x))*J(G)(x).");
        law("Jacobian rows index output components and columns index input variables; linear-part is the Jacobian at zero.");
        law("Divergence is the Jacobian trace for square vector fields; curl uses the standard right-handed three-dimensional orientation.");
        law("Curl of a polynomial gradient and divergence of a polynomial curl vanish; divergence of the gradient is the Laplacian.");
    }
}
