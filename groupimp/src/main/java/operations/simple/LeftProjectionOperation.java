package operations.simple;

import algebra.imp.Algebra;

import java.util.Objects;

/**
 * Left projection: A op B = A.
 */
public final class LeftProjectionOperation<T> implements IOperation<T> {
    private static final long serialVersionUID = 1L;

    private final Algebra<T> algebra;

    public LeftProjectionOperation(Algebra<T> algebra) {
        this.algebra = Objects.requireNonNull(algebra, "algebra");
    }

    @Override
    public T performOperation(T first, T second) {
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
    public Class<?> getSecondElementClass() {
        return algebra.getParamClass();
    }
}
