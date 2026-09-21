package mathematics.core;

import java.io.Serializable;
import java.util.*;

/** Shared metadata for domains, operations and structures; mathematical content stays explicit. */
public final class Metadata implements Serializable {
    private static final long serialVersionUID = 1L;
    public final String id;
    public final String name;
    public final String description;
    public final Map<String, String> details;
    public final List<String> invariants;
    public final List<String> relatedConcepts;
    public final MathStatus.Implementation implementation;
    public final Provenance provenance;

    private Metadata(Builder b) {
        id = requireText(b.id); name = requireText(b.name); description = requireText(b.description);
        details = Collections.unmodifiableMap(new LinkedHashMap<>(b.details));
        invariants = Collections.unmodifiableList(new ArrayList<>(b.invariants));
        relatedConcepts = Collections.unmodifiableList(new ArrayList<>(b.related));
        implementation = Objects.requireNonNull(b.implementation);
        provenance = Objects.requireNonNull(b.provenance, "provenance");
    }
    private static String requireText(String value) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("Metadata text must be nonempty");
        return value;
    }
    public static Builder builder(String id, String name, String description) { return new Builder(id, name, description); }
    public static Metadata of(String id, String description) {
        return builder(id, id, description).provenance(Provenance.unreviewed("docs/MATHEMATICAL_OPERATION_MODEL.md")).build();
    }
    public static final class Builder {
        private final String id, name, description;
        private final Map<String, String> details = new LinkedHashMap<>();
        private final List<String> invariants = new ArrayList<>(), related = new ArrayList<>();
        private MathStatus.Implementation implementation = MathStatus.Implementation.IMPLEMENTED;
        private Provenance provenance;
        private Builder(String id, String name, String description) { this.id=id; this.name=name; this.description=description; }
        public Builder detail(String key, String value) { details.put(requireText(key), requireText(value)); return this; }
        public Builder invariant(String value) { invariants.add(requireText(value)); return this; }
        public Builder related(String id) { related.add(requireText(id)); return this; }
        public Builder implementation(MathStatus.Implementation value) { implementation=value; return this; }
        public Builder provenance(Provenance value) { provenance=value; return this; }
        public Metadata build() { return new Metadata(this); }
    }
}
