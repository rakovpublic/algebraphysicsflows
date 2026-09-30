package mathematics.core;

import java.io.Serializable;
import java.util.*;

/** A carrier-role/operation/law bundle. Its existence does not assert that its laws hold. */
public final class Structure implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final class Law implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String statement;
        public final MathStatus.Epistemic status;
        public final String evidence;
        public Law(String statement, MathStatus.Epistemic status, String evidence) {
            this.statement=Objects.requireNonNull(statement); this.status=Objects.requireNonNull(status);
            this.evidence=Objects.requireNonNull(evidence);
            if (statement.trim().isEmpty() || evidence.trim().isEmpty()) throw new IllegalArgumentException("A law needs a statement and an evidence description");
        }
    }
    public final Metadata metadata;
    public final Map<String,Domain<?>> carriers;
    public final Map<String,DescribedOperation> operations;
    public final List<Law> laws;
    public Structure(Metadata metadata, Map<String,Domain<?>> carriers, Map<String,DescribedOperation> operations, List<Law> laws) {
        this.metadata=Objects.requireNonNull(metadata);
        this.carriers=Collections.unmodifiableMap(new LinkedHashMap<>(carriers));
        this.operations=Collections.unmodifiableMap(new LinkedHashMap<>(operations));
        this.laws=Collections.unmodifiableList(new ArrayList<>(laws));
        for (DescribedOperation operation : this.operations.values()) {
            List<Domain<?>> domains=new ArrayList<>(operation.signature().operands); domains.add(operation.signature().result);
            if (!this.carriers.values().containsAll(domains)) throw new IllegalArgumentException("An operation refers to an undeclared carrier role");
        }
    }
}
