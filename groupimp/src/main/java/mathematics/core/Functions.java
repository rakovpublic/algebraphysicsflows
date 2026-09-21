package mathematics.core;

import java.io.Serializable;

public final class Functions {
    private Functions() { }
    @FunctionalInterface public interface Unary<A,B> extends Serializable { B apply(A value); }
    @FunctionalInterface public interface Binary<A,B,C> extends Serializable { C apply(A first, B second); }
    @FunctionalInterface public interface Membership<T> extends Serializable { MathStatus.Membership test(T value); }
}
