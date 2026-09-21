package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Flat left projection between potentially different operand types: A op B = [A].
 */
public final class LeftProjectionFlatOperation<T, V> implements ILeftProjectionFlatOperation<T, V> {
    private static final long serialVersionUID = 1L;

    private final Algebra<T> algebra;
    private final Class<V> secondElementClass;

    public LeftProjectionFlatOperation(Algebra<T> algebra, Class<V> secondElementClass) {
        this.algebra = Objects.requireNonNull(algebra, "algebra");
        this.secondElementClass = Objects.requireNonNull(secondElementClass, "secondElementClass");
    }

    @Override
    public List<IAlgebraItem<T>> performOperation(T first, V second) {
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
    public Class<V> getSecondElementClass() {
        return secondElementClass;
    }
}
