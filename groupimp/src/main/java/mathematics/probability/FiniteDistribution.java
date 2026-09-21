package mathematics.probability;

import mathematics.core.*;
import algebra.imp.Algebra;
import static operations.OperationMembers.require;
import mathematics.numbers.Rational;
import java.util.*;

/** Exact finite distribution. Support has set semantics; masses carry probability semantics. */
public final class FiniteDistribution<T> implements java.io.Serializable {
    private static final long serialVersionUID=1L;
    private final Algebra<T> outcomes;
    private final Map<T,Rational> masses;
    public FiniteDistribution(Domain<T> domain,Map<T,Rational> masses) {
        this(domain.algebra(),validateDomain(domain,masses));
    }
    private static <T> Map<T,Rational> validateDomain(Domain<T> domain,Map<T,Rational> masses) {
        for(T value : masses.keySet()) domain.require(value);
        return masses;
    }
    public FiniteDistribution(Algebra<T> outcomes,Map<T,Rational> masses) {
        this.outcomes=Objects.requireNonNull(outcomes); Map<T,Rational> copy=new LinkedHashMap<>(); Rational sum=Rational.ZERO;
        for(Map.Entry<T,Rational> entry : masses.entrySet()) {
            T value=require(outcomes,entry.getKey()); Rational mass=Objects.requireNonNull(entry.getValue());
            if(mass.signum()<0) throw MathFailure.invalid("Negative probability");
            sum=sum.add(mass); if(mass.signum()>0) copy.put(value,mass);
        }
        if(!sum.equals(Rational.ONE)) throw MathFailure.invalid("Probability mass must sum exactly to one");
        this.masses=Collections.unmodifiableMap(copy);
    }
    public Algebra<T> outcomes() { return outcomes; }
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
        return new FiniteDistribution<>(outcomes,normalized);
    }
    public <B> FiniteDistribution<B> pushForward(Domain<B> target, Functions.Unary<T,B> function) {
        return pushForward(target.algebra(),value -> target.require(function.apply(value)));
    }
    public <B> FiniteDistribution<B> pushForward(Algebra<B> target, Functions.Unary<T,B> function) {
        Map<B,Rational> output=new LinkedHashMap<>();
        for(Map.Entry<T,Rational> entry : masses.entrySet()) output.merge(require(target,function.apply(entry.getKey())),entry.getValue(),Rational::add);
        return new FiniteDistribution<>(target,output);
    }
    @Override public boolean equals(Object other) {
        return other instanceof FiniteDistribution && outcomes==((FiniteDistribution<?>)other).outcomes && masses.equals(((FiniteDistribution<?>)other).masses);
    }
    @Override public int hashCode() { return 31*System.identityHashCode(outcomes)+masses.hashCode(); }
    @Override public String toString() { return "Distribution"+masses; }
}
