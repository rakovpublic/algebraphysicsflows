package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.FiniteSet;
import mathematics.numbers.Rational;
import mathematics.probability.FiniteDistribution;
import operations.OperationBodies.Unary;
import java.math.BigInteger;
import java.util.*;

/** Exact finite probability measures with outcomes validated by an existing Algebra. */
public final class FiniteProbabilityAlgebra<T> extends ConcreteAlgebra<FiniteDistribution<T>> {
    public final Algebra<T> outcomes;
    public FiniteProbabilityAlgebra(String name,Algebra<T> outcomes,FiniteSetAlgebra<T> events,
                                   RationalField rationals,BooleanAlgebra truth,NaturalSemiring naturals,Unary<T,Rational> observable) {
        super(probabilityCarrier(name,outcomes),rationals.unit());
        if(events.elements!=outcomes) throw new IllegalArgumentException("Events and probability measures must use the same outcome algebra");
        Objects.requireNonNull(observable,"observable");
        this.outcomes=outcomes;
        binary("event-probability",algebra(),events.algebra(),rationals.algebra(),false,(distribution,event) -> distribution.probability(event::contains));
        binary("point-probability",algebra(),outcomes,rationals.algebra(),false,(distribution,point) -> distribution.masses().getOrDefault(point,Rational.ZERO));
        binary("condition",algebra(),events.algebra(),algebra(),true,(distribution,event) -> distribution.condition(event::contains));
        unary("support",algebra(),events.algebra(),false,distribution -> new FiniteSet<>(distribution.masses().keySet()));
        unary("support-size",algebra(),naturals.algebra(),false,distribution -> BigInteger.valueOf(distribution.masses().size()));
        unaryFlat("outcomes",algebra(),outcomes,false,distribution -> new ArrayList<>(distribution.masses().keySet()));
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteDistribution::equals);
        unary("point-mass",outcomes,algebra(),false,point -> new FiniteDistribution<>(outcomes,Collections.singletonMap(point,Rational.ONE)));
        unary("expectation",algebra(),rationals.algebra(),false,distribution -> distribution.expectation(observable::apply));
        unary("variance",algebra(),rationals.algebra(),false,distribution -> {
            Rational mean=distribution.expectation(observable::apply);
            return distribution.expectation(point -> {
                Rational delta=observable.apply(point).subtract(mean);
                return delta.multiply(delta);
            });
        });
        law("Masses are exact nonnegative rationals summing to one, with zero-mass points excluded from support.");
        law("Conditioning is defined only when the supplied event has positive probability.");
        law("Expectation and variance refer to the observable supplied to this concrete algebra, not an implicit arbitrary metric.");
    }
    @SuppressWarnings("unchecked")
    private static <T> Algebra<FiniteDistribution<T>> probabilityCarrier(String name,Algebra<T> outcomes) {
        Class<FiniteDistribution<T>> type=(Class<FiniteDistribution<T>>)(Class<?>)FiniteDistribution.class;
        return carrier(name,type,"Finite rational probability measures on "+outcomes.getAlgebraName(),distribution -> distribution.outcomes()==outcomes);
    }
}
