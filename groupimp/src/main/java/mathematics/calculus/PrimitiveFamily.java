package mathematics.calculus;
import mathematics.core.MathStatus;
import mathematics.numbers.Rational;
import java.util.Objects;
/** Non-enumerated family {F+C | C in Q}; real constants require a richer coefficient domain. */
public final class PrimitiveFamily {
    private final Polynomial integrand;
    public PrimitiveFamily(Polynomial integrand) { this.integrand=Objects.requireNonNull(integrand); }
    public Polynomial at(Rational constant) { return integrand.primitive(constant); }
    public MathStatus.Solution status() { return MathStatus.Solution.EXACT_SOLUTION; }
    public String parameterDomain() { return "Q"; }
}
