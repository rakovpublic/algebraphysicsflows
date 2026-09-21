package operations.simple;

import algebra.imp.Algebra;

import java.util.Objects;

/**
 * Left projection between potentially different operand types: A op B = A.
 */
public final class LeftProjectionOperation<T, V> implements ILeftProjectionOperation<T, V> {
    private static final long serialVersionUID = 1L;

    private final Algebra<T> algebra;
    private final Class<V> secondElementClass;

    public LeftProjectionOperation(Algebra<T> algebra, Class<V> secondElementClass) {
        this.algebra = Objects.requireNonNull(algebra, "algebra");
        this.secondElementClass = Objects.requireNonNull(secondElementClass, "secondElementClass");
    }

    @Override
    public T performOperation(T first, V second) {
        return first;
    }

    @Override
    public String getDescription() {
        return "Left projection operation: A op B = A";
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
