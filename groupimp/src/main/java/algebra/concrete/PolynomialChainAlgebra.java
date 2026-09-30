package algebra.concrete;

import mathematics.calculus.PolynomialChain;
import java.math.BigInteger;
import java.util.ArrayList;

/** Finite rational cubical chains with exact polynomial form integration. */
public final class PolynomialChainAlgebra extends ConcreteAlgebra<PolynomialChain> {
    public PolynomialChainAlgebra(PolynomialCellAlgebra cells,RationalPolynomialMapAlgebra maps,PolynomialDifferentialFormAlgebra forms,
            RationalField rationals,NaturalSemiring naturals,IntegerRing integers,BooleanAlgebra truth) {
        super(carrier("PolynomialChain(Q)",PolynomialChain.class,"Finite rational chains of canonical polynomial cells with retained ambient dimension and degree",c -> true),rationals.unit());
        closed("add",true,PolynomialChain::add);
        closed("subtract",true,PolynomialChain::subtract);
        unary("negate",algebra(),algebra(),false,PolynomialChain::negate);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,PolynomialChain::scale);
        closed("product",false,PolynomialChain::product);
        unary("boundary",algebra(),algebra(),false,PolynomialChain::boundary);
        binary("pushforward",algebra(),maps.algebra(),algebra(),true,PolynomialChain::pushforward);
        binary("integrate",algebra(),forms.algebra(),rationals.algebra(),true,PolynomialChain::integrate);
        unary("ambient-dimension",algebra(),naturals.algebra(),false,c -> BigInteger.valueOf(c.ambientDimension()));
        unary("degree",algebra(),integers.algebra(),false,PolynomialChain::degree);
        unary("is-zero",algebra(),truth.algebra(),false,PolynomialChain::isZero);
        unary("cell-count",algebra(),naturals.algebra(),false,c -> BigInteger.valueOf(c.coefficients().size()));
        unaryFlat("cells",algebra(),cells.algebra(),false,c -> new ArrayList<>(c.coefficients().keySet()));
        unaryFlat("coefficients",algebra(),rationals.algebra(),false,c -> new ArrayList<>(c.coefficients().values()));
        binary("coefficient",algebra(),cells.algebra(),rationals.algebra(),true,PolynomialChain::coefficient);
        binary("equal",algebra(),algebra(),truth.algebra(),false,PolynomialChain::equals);
        unary("from-cell",cells.algebra(),algebra(),false,PolynomialChain::of);
        unary("boundary-of-cell",cells.algebra(),algebra(),false,c -> c.boundary());
        unary("zero-like",algebra(),algebra(),false,c -> PolynomialChain.zero(c.ambientDimension(),c.degree()));
        law("Each fixed ambient dimension and degree is a rational vector space of finite formal sums of parametrized cells; zero chains retain both parameters.");
        law("Boundary lowers degree by one, squares to zero and commutes with polynomial pushforward. Every negative degree contains only the zero chain.");
        law("Boundary of C product D equals boundary(C) product D plus (-1)^degree(C) C product boundary(D).");
        law("Exact integration is linear and satisfies Stokes: integral_C d(w)=integral_boundary(C) w for compatible polynomial forms.");
    }
}
