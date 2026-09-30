package algebra.concrete;

import mathematics.calculus.PolynomialCell;
import java.math.BigInteger;

/** Polynomially parametrized oriented cubes and explicit points in positive coordinate spaces. */
public final class PolynomialCellAlgebra extends ConcreteAlgebra<PolynomialCell> {
    public PolynomialCellAlgebra(RationalPolynomialMapAlgebra maps,PolynomialDifferentialFormAlgebra forms,
            RationalVectorFamily vectors,RationalField rationals,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("PolynomialCell(Q)",PolynomialCell.class,"Polynomial parametrizations of oriented unit cubes, including point cells",c -> true),rationals.unit());
        closed("product",false,PolynomialCell::product);
        unary("dimension",algebra(),naturals.algebra(),false,c -> BigInteger.valueOf(c.dimension()));
        unary("ambient-dimension",algebra(),naturals.algebra(),false,c -> BigInteger.valueOf(c.ambientDimension()));
        unary("from-map",maps.algebra(),algebra(),false,PolynomialCell::parameterized);
        unary("to-map",algebra(),maps.algebra(),true,PolynomialCell::parameterization);
        unary("from-point",vectors.algebra(),algebra(),true,PolynomialCell::point);
        unary("to-point",algebra(),vectors.algebra(),true,PolynomialCell::point);
        binary("segment",vectors.algebra(),vectors.algebra(),algebra(),true,PolynomialCell::segment);
        binary("lower-face",algebra(),naturals.algebra(),algebra(),true,(c,i) -> c.face(c.axis(i),false));
        binary("upper-face",algebra(),naturals.algebra(),algebra(),true,(c,i) -> c.face(c.axis(i),true));
        unaryFlat("faces",algebra(),algebra(),false,PolynomialCell::faces);
        unaryFlat("vertices",algebra(),vectors.algebra(),false,PolynomialCell::vertices);
        binary("evaluate",algebra(),vectors.algebra(),vectors.algebra(),true,PolynomialCell::evaluate);
        binary("pushforward",algebra(),maps.algebra(),algebra(),true,PolynomialCell::pushforward);
        binary("integrate",algebra(),forms.algebra(),rationals.algebra(),true,PolynomialCell::integrate);
        binary("equal",algebra(),algebra(),truth.algebra(),false,PolynomialCell::equals);
        law("A k-cell is a polynomial map on the oriented unit k-cube; equality compares parametrizations, not geometric images or homology classes.");
        law("Boundary uses sum_i (-1)^i (upper_i-lower_i); point cells have zero boundary in degree -1.");
        law("Integration pairs a homogeneous k-form with a k-cell by exact pullback and monomial integration; point integration is scalar evaluation.");
        law("Products order the first cell's parameters before the second cell's parameters, fixing product orientation.");
    }
}
