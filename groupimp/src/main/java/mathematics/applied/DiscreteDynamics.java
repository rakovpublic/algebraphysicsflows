package mathematics.applied;
import mathematics.core.*;
import java.util.*;
/** Finite prefixes of discrete trajectories. No claim about attractors or infinite-time convergence. */
public final class DiscreteDynamics {
    private DiscreteDynamics() { }
    public static <T> List<T> trajectory(UnaryOperation<T,T> step, T initial, int steps) {
        if(!step.source().compatibleWith(step.target())) throw new IllegalArgumentException("State domain changes");
        if(steps<0) throw new IllegalArgumentException("Negative step count");
        List<T> result=new ArrayList<>(); T value=step.source().require(initial); result.add(value);
        for(int i=0;i<steps;i++) { value=step.apply(value); result.add(value); }
        return Collections.unmodifiableList(result);
    }
}
