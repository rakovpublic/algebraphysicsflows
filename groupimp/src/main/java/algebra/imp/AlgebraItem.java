package algebra.imp;

import algebra.IAlgebraItem;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public final class AlgebraItem<T> extends AbstractAlgebra<T> implements IAlgebraItem<T>  {
    private final Algebra<T> algebra;
    private final T value;

     AlgebraItem(Algebra<T> algebra, T value) {
        this.algebra = algebra;
        this.value = value;
    }

    @Override
    public T getResult() {
        return value;
    }
    @Override
    public Algebra<T> getAlgebra() {
        return algebra;
    }

    @Override
    public IAlgebraItem<T> getCurrentItem() {
        return this;
    }

    @Override
    public IAlgebraItem<T> perform() {
        return this;
    }


}
