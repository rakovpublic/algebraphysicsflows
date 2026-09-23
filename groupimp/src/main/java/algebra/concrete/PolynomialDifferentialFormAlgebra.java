package algebra.concrete;

import mathematics.calculus.PolynomialDifferentialForm;
import java.math.BigInteger;
import java.util.*;

/** Polynomial coordinate differential forms with native derivative, pullback and vector field actions. */
public final class PolynomialDifferentialFormAlgebra extends ConcreteAlgebra<PolynomialDifferentialForm> {
    public PolynomialDifferentialFormAlgebra(RationalMultivariatePolynomialAlgebra polynomials,RationalPolynomialMapAlgebra maps,
            RationalExteriorAlgebra exterior,RationalField rationals,RationalVectorFamily vectors,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("PolynomialForm(Q)",PolynomialDifferentialForm.class,"Polynomial differential forms in standard rational coordinate spaces",f -> true),rationals.unit());
        closed("add",true,PolynomialDifferentialForm::add);
        closed("subtract",true,PolynomialDifferentialForm::subtract);
        closed("wedge",true,PolynomialDifferentialForm::wedge);
        unary("negate",algebra(),algebra(),false,PolynomialDifferentialForm::negate);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,PolynomialDifferentialForm::scale);
        binary("multiply-polynomial",algebra(),polynomials.algebra(),algebra(),true,PolynomialDifferentialForm::multiplyPolynomial);
        unary("exterior-derivative",algebra(),algebra(),false,PolynomialDifferentialForm::exteriorDerivative);
        binary("pullback",algebra(),maps.algebra(),algebra(),true,PolynomialDifferentialForm::pullback);
        binary("interior",algebra(),maps.algebra(),algebra(),true,PolynomialDifferentialForm::interior);
        binary("lie-derivative",algebra(),maps.algebra(),algebra(),true,PolynomialDifferentialForm::lieDerivative);
        unary("hodge-star",algebra(),algebra(),false,PolynomialDifferentialForm::hodgeStar);
        unary("grade-involution",algebra(),algebra(),false,PolynomialDifferentialForm::gradeInvolution);
        unary("ambient-dimension",algebra(),naturals.algebra(),false,f -> BigInteger.valueOf(f.dimension()));
        unary("basis-count",algebra(),naturals.algebra(),false,f -> BigInteger.valueOf(f.coefficients().size()));
        unary("coefficient-term-count",algebra(),naturals.algebra(),false,f -> BigInteger.valueOf(f.coefficientTermCount()));
        unaryFlat("degrees",algebra(),naturals.algebra(),false,f -> {
            List<BigInteger> values=new ArrayList<>(); for(int degree : f.degrees()) values.add(BigInteger.valueOf(degree)); return values;
        });
        unaryFlat("terms",algebra(),algebra(),false,PolynomialDifferentialForm::terms);
        unaryFlat("coefficients",algebra(),polynomials.algebra(),false,f -> new ArrayList<>(f.coefficients().values()));
        unaryFlat("basis-masks",algebra(),naturals.algebra(),false,f -> {
            List<BigInteger> values=new ArrayList<>(); for(int mask : f.coefficients().keySet()) values.add(BigInteger.valueOf(mask)); return values;
        });
        binary("grade",algebra(),naturals.algebra(),algebra(),false,(f,k) -> k.compareTo(BigInteger.valueOf(f.dimension()))>0?PolynomialDifferentialForm.zero(f.dimension()):f.grade(k.intValueExact()));
        binary("evaluate",algebra(),vectors.algebra(),exterior.algebra(),true,PolynomialDifferentialForm::evaluate);
        binary("equal",algebra(),algebra(),truth.algebra(),false,PolynomialDifferentialForm::equals);
        unary("is-zero",algebra(),truth.algebra(),false,PolynomialDifferentialForm::isZero);
        unary("scalar-part",algebra(),polynomials.algebra(),false,PolynomialDifferentialForm::scalarPart);
        unary("to-polynomial",algebra(),polynomials.algebra(),true,PolynomialDifferentialForm::toPolynomial);
        unary("from-polynomial",polynomials.algebra(),algebra(),false,PolynomialDifferentialForm::scalar);
        unary("from-exterior",exterior.algebra(),algebra(),true,PolynomialDifferentialForm::fromExterior);
        unary("to-exterior",algebra(),exterior.algebra(),true,PolynomialDifferentialForm::toExterior);
        unary("from-vector-field",maps.algebra(),algebra(),true,PolynomialDifferentialForm::fromVectorField);
        unary("to-vector-field",algebra(),maps.algebra(),true,PolynomialDifferentialForm::toVectorField);
        unary("zero-like",algebra(),algebra(),false,f -> PolynomialDifferentialForm.zero(f.dimension()));
        unary("one-like",algebra(),algebra(),false,f -> PolynomialDifferentialForm.one(f.dimension()));
        unary("volume-like",algebra(),algebra(),false,f -> PolynomialDifferentialForm.volume(f.dimension()));
        unaryFlat("basis",algebra(),algebra(),false,PolynomialDifferentialForm::basis);
        law("Wedge is graded commutative, exterior differentiation squares to zero, and d(a wedge b)=da wedge b+gradeInvolution(a) wedge db, including mixed degrees.");
        law("For a polynomial map F, pullback substitutes coefficients and sends dx_i to dF_i; it preserves wedge and commutes with exterior differentiation.");
        law("Left insertion uses the natural covector-vector pairing; the Lie derivative satisfies Cartan's formula L_X=i_X*d+d*i_X.");
        law("Hodge star, vector-field conversion and identification with rational exterior values use the standard oriented Euclidean coordinates.");
    }
}
