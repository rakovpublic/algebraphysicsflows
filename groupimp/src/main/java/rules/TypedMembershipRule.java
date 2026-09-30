package rules;

import operations.OperationBodies.Predicate;
import java.io.Serializable;

/** Serializable membership rule used directly by Algebra.validate and buildAlgebraItem. */
public final class TypedMembershipRule<T> implements IValidationRule<T>,Serializable {
    private static final long serialVersionUID=1L;
    private final Class<T> type;
    private final Predicate<T> predicate;
    private final String description;
    public TypedMembershipRule(Class<T> type,Predicate<T> predicate,String description) {
        this.type=type; this.predicate=predicate; this.description=description;
    }
    public boolean validate(T value) { return type.isInstance(value) && predicate.test(value); }
    public String getDescription() { return description; }
}

