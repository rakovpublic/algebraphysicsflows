package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.calculus.Polynomial;
import mathematics.foundations.*;
import mathematics.numbers.Rational;


public final class RationalPolynomialRing extends ConcreteAlgebra<Polynomial> {
    public final Algebra<Pair<Rational,Rational>> integrationBounds;
    public RationalPolynomialRing(RationalField rationals) {
        super(carrier("Q[x]",Polynomial.class,"Univariate rational polynomial ring",p -> true),rationals.unit());
        integrationBounds=boundsCarrier(rationals);
        closed("add",false,Polynomial::add); closed("multiply",false,Polynomial::multiply);
        closed("subtract",false,(a,b) -> a.add(b.multiply(new Polynomial(Rational.of(-1)))));
        unary("negate",algebra(),algebra(),false,p -> p.multiply(new Polynomial(Rational.of(-1))));
        unary("derivative",algebra(),algebra(),false,Polynomial::derivative);
        binary("evaluate",algebra(),rationals.algebra(),rationals.algebra(),false,Polynomial::evaluate);
        binary("primitive",algebra(),rationals.algebra(),algebra(),false,Polynomial::primitive);
        binary("integrate",algebra(),integrationBounds,rationals.algebra(),false,(p,bounds) -> p.integrate(bounds.first,bounds.second));
        constant("zero",new Polynomial(Rational.ZERO)); constant("one",new Polynomial(Rational.ONE));
        law("Q[x] is a commutative unital ring; polynomial multiplication distributes over addition.");
        law("The derivative is Q-linear and satisfies the product rule; primitive uses an explicit rational constant.");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<Rational,Rational>> boundsCarrier(RationalField rationals) {
        Class<Pair<Rational,Rational>> type=(Class<Pair<Rational,Rational>>)(Class<?>)Pair.class;
        return carrier("QxQ.bounds",type,"Oriented rational integration bounds",pair ->
                rationals.algebra().getParamClass().isInstance(pair.first) && rationals.algebra().getParamClass().isInstance(pair.second)
                && rationals.algebra().validate(pair.first) && rationals.algebra().validate(pair.second));
    }
}
