package mathematics.foundations;

import mathematics.core.*;
import java.io.Serializable;
import java.util.Objects;

/** Functions are members with declared source/target domains; extensional equality is not assumed decidable. */
public final class MathFunction<A,B> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final UnaryOperation<A,B> operation;
    public MathFunction(UnaryOperation<A,B> operation) { this.operation=Objects.requireNonNull(operation); }
    public B apply(A value) { return operation.apply(value); }
    public Domain<A> source() { return operation.source(); }
    public Domain<B> target() { return operation.target(); }
    public UnaryOperation<A,B> operation() { return operation; }
    public <C> MathFunction<A,C> andThen(MathFunction<B,C> next) { return new MathFunction<>(operation.andThen(next.operation)); }
}
