package algebra.concrete;

import mathematics.applied.FiniteOptimization;
import mathematics.calculus.Polynomial;
import mathematics.foundations.FiniteSet;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.ArrayList;

/** Finite integer sets, including exact polynomial optimization over the explicit feasible set. */
public final class IntegerSetAlgebra extends FiniteSetAlgebra<BigInteger> {
    public IntegerSetAlgebra(IntegerRing integers,BooleanAlgebra truth,NaturalSemiring naturals,
                             RationalField rationals,RationalPolynomialRing polynomials) {
        super("FiniteSet(Z)",integers.algebra(),truth,naturals);
        binary("argmin",algebra(),polynomials.algebra(),algebra(),true,(set,objective) -> optimizers(set,objective,false));
        binary("argmax",algebra(),polynomials.algebra(),algebra(),true,(set,objective) -> optimizers(set,objective,true));
        binary("minimum",algebra(),polynomials.algebra(),rationals.algebra(),true,(set,objective) -> optimalValue(set,objective,false));
        binary("maximum",algebra(),polynomials.algebra(),rationals.algebra(),true,(set,objective) -> optimalValue(set,objective,true));
        flat("minimizers",algebra(),polynomials.algebra(),integers.algebra(),true,
                (set,objective) -> new ArrayList<>(optimizers(set,objective,false).members()));
        flat("maximizers",algebra(),polynomials.algebra(),integers.algebra(),true,
                (set,objective) -> new ArrayList<>(optimizers(set,objective,true).members()));
        law("Every finite nonempty feasible set has a polynomial minimum and maximum; all tied optimizers are retained.");
        law("An optimizer is certified only relative to the explicitly supplied finite feasible set, not all integers or reals.");
    }
    private static FiniteSet<BigInteger> optimizers(FiniteSet<BigInteger> feasible,Polynomial objective,boolean maximum) {
        return FiniteOptimization.minimize(feasible,value -> {
            Rational score=objective.evaluate(Rational.of(value));
            return maximum?score.negate():score;
        });
    }
    private static Rational optimalValue(FiniteSet<BigInteger> feasible,Polynomial objective,boolean maximum) {
        BigInteger optimizer=optimizers(feasible,objective,maximum).members().iterator().next();
        return objective.evaluate(Rational.of(optimizer));
    }
}
