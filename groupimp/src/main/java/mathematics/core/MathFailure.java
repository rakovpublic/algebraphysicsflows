package mathematics.core;

/** These failures must never be replaced by a plausible-looking mathematical value. */
public final class MathFailure extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public enum Kind { INVALID_MEMBER, OPERATION_UNDEFINED, NUMERICAL_FAILURE, IMPLEMENTATION_FAILURE }
    private final Kind kind;
    public MathFailure(Kind kind, String message) { super(message); this.kind=java.util.Objects.requireNonNull(kind); }
    public MathFailure(Kind kind, String message, Throwable cause) { super(message, cause); this.kind=java.util.Objects.requireNonNull(kind); }
    public Kind kind() { return kind; }
    public static MathFailure undefined(String message) { return new MathFailure(Kind.OPERATION_UNDEFINED, message); }
    public static MathFailure invalid(String message) { return new MathFailure(Kind.INVALID_MEMBER, message); }
}
