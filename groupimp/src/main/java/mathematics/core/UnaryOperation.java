package mathematics.core;

import java.util.*;

/** Checked A -> B, including same-domain, higher-order and Unit -> B operations. */
public final class UnaryOperation<A,B> implements DescribedOperation {
    private static final long serialVersionUID = 1L;
    private final Domain<A> source;
    private final Domain<B> target;
    private final Metadata metadata;
    private final MathStatus.Computation computation;
    private final boolean partial;
    private final Functions.Unary<A,B> body;
    private final List<String> steps;

    public UnaryOperation(Metadata metadata, Domain<A> source, Domain<B> target,
                          MathStatus.Computation computation, boolean partial, Functions.Unary<A,B> body) {
        this(metadata, source, target, computation, partial, body, Collections.singletonList(metadata.id));
    }
    private UnaryOperation(Metadata metadata, Domain<A> source, Domain<B> target, MathStatus.Computation computation,
                           boolean partial, Functions.Unary<A,B> body, List<String> steps) {
        this.metadata=Objects.requireNonNull(metadata); this.source=Objects.requireNonNull(source);
        this.target=Objects.requireNonNull(target); this.computation=Objects.requireNonNull(computation);
        this.partial=partial; this.body=Objects.requireNonNull(body);
        this.steps=Collections.unmodifiableList(new ArrayList<>(steps));
    }
    public B apply(A value) { return target.require(body.apply(source.require(value))); }
    public Outcome<B> evaluate(A value) {
        try { return Outcome.success(apply(value), computation, steps); }
        catch (MathFailure failure) { return Outcome.failure(failure, computation, steps); }
        catch (RuntimeException failure) { return Outcome.failure(new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE, metadata.id + " failed", failure), computation, steps); }
    }
    public <C> UnaryOperation<A,C> andThen(UnaryOperation<B,C> next) {
        if (!target.compatibleWith(next.source)) throw new IllegalArgumentException("Incompatible domains: " + target + " and " + next.source + "; use an explicit embedding");
        List<String> combined=new ArrayList<>(steps); combined.addAll(next.steps);
        return new UnaryOperation<>(Metadata.of(metadata.id + ">>" + next.metadata.id, "Checked composition"),
                source, next.target, combine(computation, next.computation), partial || next.partial,
                value -> next.apply(apply(value)), combined);
    }
    private static MathStatus.Computation combine(MathStatus.Computation a, MathStatus.Computation b) {
        if (a == b) return a;
        if (a == MathStatus.Computation.EXACT) return b;
        if (b == MathStatus.Computation.EXACT) return a;
        // Mixed numerical/symbolic/bound semantics need an explicit interpretation.
        return MathStatus.Computation.UNKNOWN;
    }
    public Domain<A> source() { return source; }
    public Domain<B> target() { return target; }
    public List<String> steps() { return steps; }
    public Metadata metadata() { return metadata; }
    public Signature signature() { return new Signature(Collections.singletonList(source), target, MathStatus.CollectionSemantics.SCALAR, partial); }
    public MathStatus.Computation computation() { return computation; }
}
