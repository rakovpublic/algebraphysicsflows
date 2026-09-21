package mathematics.adapters;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebraflow.InputFormat;
import mathematics.core.Domain;
import java.util.*;

/** Explicit finite input for a checked mathematical carrier. */
public final class DomainInput<T> implements InputFormat<T> {
    private final Domain<T> domain;
    private final List<T> values;
    public DomainInput(Domain<T> domain,List<T> values) {
        this.domain=Objects.requireNonNull(domain);
        this.values=Collections.unmodifiableList(new ArrayList<>(values));
        for(T value : this.values) domain.require(value);
    }
    public Class<T> getInputType() { return domain.representation(); }
    public Algebra<T> getAlgebra() { return domain.algebra(); }
    public List<IAlgebraItem<T>> read(Algebra<T> algebra) {
        if(algebra!=domain.algebra()) throw new IllegalArgumentException("Input and MathTool must share the same domain definition");
        List<IAlgebraItem<T>> result=new ArrayList<>();
        for(T value : values) result.add(domain.item(value));
        return result;
    }
}

