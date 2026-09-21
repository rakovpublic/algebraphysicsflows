package mathematics.applied;
import mathematics.core.*;
import mathematics.foundations.FiniteSet;
import mathematics.numbers.Rational;
import java.util.*;
/** Exhaustive optimization over a declared finite feasible set, preserving ties. */
public final class FiniteOptimization {
    private FiniteOptimization() { }
    public static <T> FiniteSet<T> minimize(FiniteSet<T> feasible, Functions.Unary<T,Rational> objective) {
        if(feasible.size()==0) throw MathFailure.undefined("Empty feasible region has no minimizer");
        Rational best=null; List<T> values=new ArrayList<>();
        for(T value : feasible.members()) {
            Rational score=Objects.requireNonNull(objective.apply(value));
            if(best==null || score.compareTo(best)<0) { best=score; values.clear(); values.add(value); }
            else if(score.equals(best)) values.add(value);
        }
        return new FiniteSet<>(values);
    }
}
