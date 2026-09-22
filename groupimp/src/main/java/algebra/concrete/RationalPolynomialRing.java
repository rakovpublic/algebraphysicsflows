package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.calculus.Polynomial;
import mathematics.foundations.*;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;


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
        closed("quotient",true,Polynomial::quotient);
        closed("remainder",true,Polynomial::remainder);
        closed("divide-exact",true,Polynomial::divideExact);
        closed("gcd",false,Polynomial::gcd);
        closed("compose",false,Polynomial::compose);
        unary("monic",algebra(),algebra(),true,Polynomial::monic);
        flat("quotient-remainder",algebra(),algebra(),algebra(),true,(a,b) -> {
            Pair<Polynomial,Polynomial> result=a.divideAndRemainder(b);
            return Arrays.asList(result.first,result.second);
        });
        law("Q[x] is a commutative unital ring; polynomial multiplication distributes over addition.");
        law("The derivative is Q-linear and satisfies the product rule; primitive uses an explicit rational constant.");
        law("Euclidean division has a remainder of smaller degree; gcd is monic unless both operands are zero.");
    }
    public RationalPolynomialRing(RationalField rationals,NaturalSemiring naturals) {
        this(rationals);
        binary("derivative-order",algebra(),naturals.algebra(),algebra(),false,(polynomial,order) ->
                order.compareTo(BigInteger.valueOf(polynomial.degree()))>0
                        ?new Polynomial(Rational.ZERO):polynomial.derivative(order.intValueExact()));
        Algebra<Pair<Rational,BigInteger>> iterationInputs=iterationCarrier(rationals,naturals);
        binary("iterate",algebra(),iterationInputs,rationals.algebra(),false,(polynomial,input) -> {
            int steps=iterationCount(input.second); Rational value=input.first;
            for(int i=0;i<steps;i++) value=polynomial.evaluate(value);
            return value;
        });
        flat("orbit",algebra(),iterationInputs,rationals.algebra(),false,(polynomial,input) -> {
            int steps=iterationCount(input.second); List<Rational> values=new ArrayList<>();
            Rational value=input.first; values.add(value);
            for(int i=0;i<steps;i++) { value=polynomial.evaluate(value); values.add(value); }
            return values;
        });
        law("Iteration applies the polynomial repeatedly; an orbit contains the initial value followed by each iterate.");
    }
    private static int iterationCount(BigInteger steps) {
        if(steps.compareTo(BigInteger.valueOf(10000))>0)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Polynomial iteration is limited to 10000 steps");
        return steps.intValueExact();
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<Rational,BigInteger>> iterationCarrier(RationalField rationals,NaturalSemiring naturals) {
        Class<Pair<Rational,BigInteger>> type=(Class<Pair<Rational,BigInteger>>)(Class<?>)Pair.class;
        return carrier("QxN.iteration",type,"Rational initial value and nonnegative iteration count",pair ->
                rationals.algebra().getParamClass().isInstance(pair.first) && naturals.algebra().getParamClass().isInstance(pair.second)
                && rationals.algebra().validate(pair.first) && naturals.algebra().validate(pair.second));
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<Rational,Rational>> boundsCarrier(RationalField rationals) {
        Class<Pair<Rational,Rational>> type=(Class<Pair<Rational,Rational>>)(Class<?>)Pair.class;
        return carrier("QxQ.bounds",type,"Oriented rational integration bounds",pair ->
                rationals.algebra().getParamClass().isInstance(pair.first) && rationals.algebra().getParamClass().isInstance(pair.second)
                && rationals.algebra().validate(pair.first) && rationals.algebra().validate(pair.second));
    }
}
