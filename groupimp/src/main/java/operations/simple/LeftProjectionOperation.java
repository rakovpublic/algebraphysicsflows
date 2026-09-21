package operations.simple;

import algebra.imp.Algebra;
import algebra.IAlgebraItem;

import java.util.Objects;

/**
 * Projection A x B -> B returning the second operand. The historical class name is retained.
 */
public final class LeftProjectionOperation<T, V> implements ILeftProjectionOperation<T, V> {
    private static final long serialVersionUID = 1L;

    private final Algebra<V> algebra;
    private final Class<V> secondElementClass;

    public LeftProjectionOperation(Algebra<V> algebra, Class<V> secondElementClass) {
        this.algebra = Objects.requireNonNull(algebra, "algebra");
        this.secondElementClass = Objects.requireNonNull(secondElementClass, "secondElementClass");
    }

    @Override
    public IAlgebraItem<V> performOperation(T first, V second) {
        return algebra.buildAlgebraItem(operations.OperationMembers.require(algebra,second));
    }

    @Override
    public String getDescription() {
        return "Left projection operation: A op B = B";
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
