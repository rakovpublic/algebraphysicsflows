package mathematics.adapters;

import mathematics.core.*;
import java.util.List;

/**
 * Optional boundary for CAS/numerical/proof systems. No external engine is bundled.
 * Implementations must check Signature domains, expose version/provenance, and
 * preserve failure/accuracy/evidence distinctions. No shell execution is implied.
 */
public interface ExternalMathAdapter {
    String systemName();
    String systemVersion();
    Provenance provenance();
    boolean supports(Signature signature);
    Outcome<?> evaluate(Signature signature,List<?> operands);
}
