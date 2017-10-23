package algebra.imp;

import algebra.IAlgebraItem;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public final class AlgebraItem<T> extends AbstractAlgebra<T> implements IAlgebraItem<T> {
    private final Algebra<T> algebra;
    private final T value;

    AlgebraItem(Algebra<T> algebra, T value) {
        this.algebra = algebra;
        this.value = value;
    }

    /**
     * @return the value of AlgebraItem
     */
    @Override
    public T getResult() {
        return value;
    }

    /**
     * @return algebra object
     */
    @Override
    public Algebra<T> getAlgebra() {
        return algebra;
    }

    /**
     * @return this object
     */
    @Override
    public IAlgebraItem<T> getCurrentItem() {
        return this;
    }

    /**
     * perform stored operations
     *
     * @return IAlgebraItem
     */
    @Override
    public IAlgebraItem<T> perform() {
        return this;
    }


}
