package mathematics.core;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import rules.IValidationRule;
import java.io.Serializable;
import java.util.Objects;

/** A carrier definition, not an enumeration. Different domain instances require explicit embeddings. */
public final class Domain<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Metadata metadata;
    private final Class<T> representation;
    private final Functions.Membership<T> membership;
    private final Algebra<T> algebra;

    public Domain(Metadata metadata, Class<T> representation, Functions.Membership<T> membership) {
        this.metadata=Objects.requireNonNull(metadata);
        this.representation=Objects.requireNonNull(representation);
        this.membership=Objects.requireNonNull(membership);
        this.algebra=new Algebra<>(metadata.id, representation, metadata.description);
        this.algebra.addValidationRule(new MembershipRule());
    }
    private final class MembershipRule implements IValidationRule<T>, Serializable {
        private static final long serialVersionUID = 1L;
        public boolean validate(T value) { return contains(value) == MathStatus.Membership.MEMBER; }
        public String getDescription() { return metadata.description; }
    }
    public MathStatus.Membership contains(Object value) {
        if (!representation.isInstance(value)) return MathStatus.Membership.NOT_MEMBER;
        return Objects.requireNonNull(membership.test(representation.cast(value)), "membership decision");
    }
    public T require(Object value) {
        MathStatus.Membership decision=contains(value);
        if (decision == MathStatus.Membership.NOT_MEMBER) throw MathFailure.invalid("Not a member of " + metadata.id);
        if (decision == MathStatus.Membership.UNKNOWN) throw MathFailure.undefined("Membership in " + metadata.id + " is unknown; a witness or stronger rule is required");
        return representation.cast(value);
    }
    public IAlgebraItem<T> item(T value) { return algebra.buildAlgebraItem(require(value)); }
    public Algebra<T> algebra() { return algebra; }
    public Metadata metadata() { return metadata; }
    public Class<T> representation() { return representation; }
    public boolean compatibleWith(Domain<?> other) { return this == other; }
    @Override public String toString() { return metadata.id; }
}
