package mathematics.probability;

import mathematics.core.*;
import mathematics.numbers.Rational;
import java.util.*;

/** Exact finite distribution. Support has set semantics; masses carry probability semantics. */
public final class FiniteDistribution<T> {
    private final Domain<T> domain;
    private final Map<T,Rational> masses;
    public FiniteDistribution(Domain<T> domain,Map<T,Rational> masses) {
        this.domain=Objects.requireNonNull(domain); Map<T,Rational> copy=new LinkedHashMap<>(); Rational sum=Rational.ZERO;
        for(Map.Entry<T,Rational> entry : masses.entrySet()) {
            T value=domain.require(entry.getKey()); Rational mass=Objects.requireNonNull(entry.getValue());
            if(mass.signum()<0) throw MathFailure.invalid("Negative probability");
            sum=sum.add(mass); if(mass.signum()>0) copy.put(value,mass);
        }
        if(!sum.equals(Rational.ONE)) throw MathFailure.invalid("Probability mass must sum exactly to one");
        this.masses=Collections.unmodifiableMap(copy);
    }
    public Map<T,Rational> masses() { return masses; }
    public Rational probability(Functions.Unary<T,Boolean> event) {
        Rational result=Rational.ZERO;
        for(Map.Entry<T,Rational> entry : masses.entrySet()) if(event.apply(entry.getKey())) result=result.add(entry.getValue());
        return result;
    }
    public Rational expectation(Functions.Unary<T,Rational> observable) {
        Rational result=Rational.ZERO;
        for(Map.Entry<T,Rational> entry : masses.entrySet()) result=result.add(entry.getValue().multiply(observable.apply(entry.getKey())));
        return result;
    }
    public FiniteDistribution<T> condition(Functions.Unary<T,Boolean> event) {
        // Evaluate the event once per support point; do not require a stateful callback to repeat itself.
        Map<T,Rational> selected=new LinkedHashMap<>(); Rational total=Rational.ZERO;
        for(Map.Entry<T,Rational> entry : masses.entrySet()) if(event.apply(entry.getKey())) { selected.put(entry.getKey(),entry.getValue()); total=total.add(entry.getValue()); }
        if(total.signum()==0) throw MathFailure.undefined("Conditioning on a zero-probability event");
        Map<T,Rational> normalized=new LinkedHashMap<>();
        for(Map.Entry<T,Rational> entry : selected.entrySet()) normalized.put(entry.getKey(),entry.getValue().divide(total));
        return new FiniteDistribution<>(domain,normalized);
    }
    public <B> FiniteDistribution<B> pushForward(Domain<B> target, Functions.Unary<T,B> function) {
        Map<B,Rational> output=new LinkedHashMap<>();
        for(Map.Entry<T,Rational> entry : masses.entrySet()) output.merge(target.require(function.apply(entry.getKey())),entry.getValue(),Rational::add);
        return new FiniteDistribution<>(target,output);
    }
}
