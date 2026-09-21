package mathematics.algebras;

import mathematics.core.*;
import mathematics.calculus.Polynomial;
import mathematics.foundations.*;
import mathematics.numbers.Rational;
import static mathematics.core.MathStatus.Membership.MEMBER;

public final class RationalPolynomialRing extends ConcreteAlgebra<Polynomial> {
    public final Domain<Pair<Rational,Rational>> integrationBounds;
    public RationalPolynomialRing(RationalField rationals) {
        super(new Domain<>(Metadata.of("Q[x]","Univariate rational polynomial ring"),Polynomial.class,p -> MEMBER),rationals.unit());
        integrationBounds=FoundationDomains.product("QxQ.bounds",rationals.domain(),rationals.domain());
        closed("add",false,Polynomial::add); closed("multiply",false,Polynomial::multiply);
        closed("subtract",false,(a,b) -> a.add(b.multiply(new Polynomial(Rational.of(-1)))));
        unary("negate",domain(),domain(),false,p -> p.multiply(new Polynomial(Rational.of(-1))));
        unary("derivative",domain(),domain(),false,Polynomial::derivative);
        binary("evaluate",domain(),rationals.domain(),rationals.domain(),false,Polynomial::evaluate);
        binary("primitive",domain(),rationals.domain(),domain(),false,Polynomial::primitive);
        binary("integrate",domain(),integrationBounds,rationals.domain(),false,(p,bounds) -> p.integrate(bounds.first,bounds.second));
        constant("zero",new Polynomial(Rational.ZERO)); constant("one",new Polynomial(Rational.ONE));
        law("Q[x] is a commutative unital ring; polynomial multiplication distributes over addition.");
        law("The derivative is Q-linear and satisfies the product rule; primitive uses an explicit rational constant.");
    }
}

