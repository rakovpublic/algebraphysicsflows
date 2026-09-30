package mathematics.core;

import java.util.*;

/** First-class cross-domain catalog. Register all domains before their operations. */
public final class OperationCatalog {
    private final Map<String,Domain<?>> domains=new LinkedHashMap<>();
    private final Map<String,DescribedOperation> operations=new LinkedHashMap<>();
    public void addDomain(Domain<?> domain) {
        Domain<?> previous=domains.putIfAbsent(domain.metadata().id,domain);
        if (previous != null && previous != domain) throw new IllegalArgumentException("Conflicting domain definition: " + domain.metadata().id);
    }
    public void addOperation(DescribedOperation operation) {
        List<Domain<?>> involved=new ArrayList<>(operation.signature().operands); involved.add(operation.signature().result);
        for (Domain<?> domain : involved) {
            if (domains.get(domain.metadata().id) != domain) throw new IllegalArgumentException("Unregistered domain: " + domain);
        }
        if (operations.putIfAbsent(operation.metadata().id,operation) != null) throw new IllegalArgumentException("Duplicate operation: " + operation.metadata().id);
    }
    public Map<String,Domain<?>> domains() { return Collections.unmodifiableMap(domains); }
    public Map<String,DescribedOperation> operations() { return Collections.unmodifiableMap(operations); }
    public String dependencyGraph() {
        StringBuilder graph=new StringBuilder("digraph mathematics {\n");
        for (DescribedOperation operation : operations.values()) {
            String node="operation:" + operation.metadata().id;
            for (Domain<?> domain : operation.signature().operands) graph.append(quote(domain.metadata().id)).append(" -> ").append(quote(node)).append(";\n");
            graph.append(quote(node)).append(" -> ").append(quote(operation.signature().result.metadata().id)).append(";\n");
        }
        return graph.append("}\n").toString();
    }
    private static String quote(String value) { return "\"" + value.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n") + "\""; }
}
