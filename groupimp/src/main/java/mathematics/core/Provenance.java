package mathematics.core;

import java.io.Serializable;
import java.util.*;

/** Descriptive provenance is evidence to inspect, not automatic certification. */
public final class Provenance implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<String, String> fields;
    private final List<String> references;

    public Provenance(Map<String, String> fields, List<String> references) {
        this.fields = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(fields, "fields")));
        this.references = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(references, "references")));
    }
    public static Provenance unreviewed(String source) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("definition_source", source);
        fields.put("implemented_by", "Codex");
        fields.put("generated_by_model", "not recorded");
        fields.put("version", "1");
        fields.put("date", "2026-09-21");
        fields.put("human_review_status", "UNREVIEWED");
        fields.put("formal_verification_status", "UNVERIFIED");
        return new Provenance(fields, Collections.singletonList(source));
    }
    public Map<String, String> fields() { return fields; }
    public List<String> references() { return references; }
}
