package mathematics.core;

import java.util.*;

/** Finite ordered list semantics only. Sets and distributions are separate members. */
public final class FlatOperation<A,B,C> implements DescribedOperation {
    private static final long serialVersionUID = 1L;
    private final Metadata metadata;
    private final Domain<A> first;
    private final Domain<B> second;
    private final Domain<C> target;
    private final MathStatus.Computation computation;
    private final boolean partial;
    private final Functions.Binary<A,B,List<C>> body;
    public FlatOperation(Metadata metadata, Domain<A> first, Domain<B> second, Domain<C> target,
                         MathStatus.Computation computation, boolean partial, Functions.Binary<A,B,List<C>> body) {
        this.metadata=Objects.requireNonNull(metadata); this.first=Objects.requireNonNull(first);
        this.second=Objects.requireNonNull(second); this.target=Objects.requireNonNull(target);
        this.computation=Objects.requireNonNull(computation); this.partial=partial; this.body=Objects.requireNonNull(body);
    }
    public List<C> apply(A a, B b) {
        List<C> result=new ArrayList<>();
        for (C value : Objects.requireNonNull(body.apply(first.require(a),second.require(b)), "flat result")) result.add(target.require(value));
        return Collections.unmodifiableList(result);
    }
    public Outcome<List<C>> evaluate(A a, B b) {
        List<String> trace=Collections.singletonList(metadata.id);
        try { return Outcome.success(apply(a,b), computation, trace); }
        catch (MathFailure failure) { return Outcome.failure(failure, computation, trace); }
        catch (RuntimeException failure) { return Outcome.failure(new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE, metadata.id + " failed", failure), computation, trace); }
    }
    public Domain<A> first() { return first; }
    public Domain<B> second() { return second; }
    public Domain<C> target() { return target; }
    public Metadata metadata() { return metadata; }
    public Signature signature() { return new Signature(Arrays.asList(first,second), target, MathStatus.CollectionSemantics.LIST, partial); }
    public MathStatus.Computation computation() { return computation; }
}
