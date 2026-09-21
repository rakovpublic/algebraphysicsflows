package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Flat left projection: A op B = [A], wrapped in the supplied algebra.
 */
public final class LeftProjectionFlatOperation<T> implements IFlatOperation<T> {
    private static final long serialVersionUID = 1L;

    private final Algebra<T> algebra;

    public LeftProjectionFlatOperation(Algebra<T> algebra) {
        this.algebra = Objects.requireNonNull(algebra, "algebra");
    }

    @Override
    public List<IAlgebraItem<T>> performOperation(T first, T second) {
        return Collections.singletonList(algebra.buildAlgebraItem(first));
    }

    @Override
    public String getDescription() {
        return "Flat left projection operation: A op B = [A]";
    }

    @Override
    public String getAlgebraName() {
        return algebra.getAlgebraName();
    }

    @Override
    public Class<?> getResultBaseClass() {
        return algebra.getParamClass();
    }

    @Override
    public Class<?> getSecondElementClass() {
        return algebra.getParamClass();
    }
}
