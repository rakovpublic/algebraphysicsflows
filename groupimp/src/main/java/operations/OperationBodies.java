package operations;

import java.io.Serializable;

/** Serializable mathematical bodies used by implementations of the existing operation interfaces. */
public final class OperationBodies {
    private OperationBodies() { }
    public interface Unary<A,B> extends Serializable { B apply(A value); }
    public interface Binary<A,B,C> extends Serializable { C apply(A first,B second); }
    public interface Predicate<T> extends Serializable { boolean test(T value); }
}

