package mathematics.core;

import java.util.*;

public final class BinaryOperation<A,B,C> implements DescribedOperation {
    private static final long serialVersionUID = 1L;
    private final Metadata metadata;
    private final Domain<A> first;
    private final Domain<B> second;
    private final Domain<C> target;
    private final MathStatus.Computation computation;
    private final boolean partial;
    private final Functions.Binary<A,B,C> body;
    public BinaryOperation(Metadata metadata, Domain<A> first, Domain<B> second, Domain<C> target,
                           MathStatus.Computation computation, boolean partial, Functions.Binary<A,B,C> body) {
        this.metadata=Objects.requireNonNull(metadata); this.first=Objects.requireNonNull(first);
        this.second=Objects.requireNonNull(second); this.target=Objects.requireNonNull(target);
        this.computation=Objects.requireNonNull(computation); this.partial=partial; this.body=Objects.requireNonNull(body);
    }
    public C apply(A a, B b) { return target.require(body.apply(first.require(a), second.require(b))); }
    public Outcome<C> evaluate(A a, B b) {
        List<String> trace=Collections.singletonList(metadata.id);
        try { return Outcome.success(apply(a,b), computation, trace); }
        catch (MathFailure failure) { return Outcome.failure(failure, computation, trace); }
        catch (RuntimeException failure) { return Outcome.failure(new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE, metadata.id + " failed", failure), computation, trace); }
    }
    public UnaryOperation<A,C> bindRight(B value) {
        second.require(value);
        return new UnaryOperation<>(metadata, first, target, computation, partial, a -> apply(a,value));
    }
    public Domain<A> first() { return first; }
    public Domain<B> second() { return second; }
    public Domain<C> target() { return target; }
    public Metadata metadata() { return metadata; }
    public Signature signature() { return new Signature(Arrays.asList(first,second), target, MathStatus.CollectionSemantics.SCALAR, partial); }
    public MathStatus.Computation computation() { return computation; }
}
