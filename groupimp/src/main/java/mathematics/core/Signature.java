package mathematics.core;

import java.io.Serializable;
import java.util.*;

public final class Signature implements Serializable {
    private static final long serialVersionUID = 1L;
    public final List<Domain<?>> operands;
    public final Domain<?> result;
    public final MathStatus.CollectionSemantics semantics;
    public final boolean partial;
    public Signature(List<Domain<?>> operands, Domain<?> result, MathStatus.CollectionSemantics semantics, boolean partial) {
        if (operands.isEmpty() || operands.size() > 2) throw new IllegalArgumentException("Use Unit or a product domain; native n-ary operations are deferred");
        this.operands=Collections.unmodifiableList(new ArrayList<>(operands));
        for (Domain<?> domain : this.operands) Objects.requireNonNull(domain);
        this.result=Objects.requireNonNull(result);
        this.semantics=Objects.requireNonNull(semantics);
        this.partial=partial;
    }
    public String display() {
        StringJoiner joiner=new StringJoiner(" x ");
        for (Domain<?> domain : operands) joiner.add(domain.metadata().id);
        return joiner + (partial ? " ~> " : " -> ") + result.metadata().id
                + (semantics == MathStatus.CollectionSemantics.SCALAR ? "" : " [" + semantics + "]");
    }
}
