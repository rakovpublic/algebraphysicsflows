package mathematics.core;

import java.io.Serializable;
import java.util.*;

/** A computation label is not a proof status. */
public final class Outcome<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final T value;
    private final MathFailure failure;
    private final MathStatus.Computation computation;
    private final List<String> trace;

    private Outcome(T value, MathFailure failure, MathStatus.Computation computation, List<String> trace) {
        this.value=value; this.failure=failure; this.computation=Objects.requireNonNull(computation);
        this.trace=Collections.unmodifiableList(new ArrayList<>(trace));
    }
    public static <T> Outcome<T> success(T value, MathStatus.Computation status, List<String> trace) {
        return new Outcome<>(Objects.requireNonNull(value, "result"), null, status, trace);
    }
    public static <T> Outcome<T> failure(MathFailure failure, MathStatus.Computation status, List<String> trace) {
        return new Outcome<>(null, Objects.requireNonNull(failure), status, trace);
    }
    public boolean succeeded() { return failure == null; }
    public T value() { if (failure != null) throw failure; return value; }
    public Optional<MathFailure> failure() { return Optional.ofNullable(failure); }
    public MathStatus.Computation computation() { return computation; }
    public MathStatus.Epistemic epistemicStatus() { return MathStatus.Epistemic.DEFINED; }
    public List<String> trace() { return trace; }
}
