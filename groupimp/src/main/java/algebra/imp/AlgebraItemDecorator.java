package algebra.imp;

import algebra.IAlgebraItem;
import operations.simple.IOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public final class AlgebraItemDecorator<T> extends AbstractAlgebra<T> implements IAlgebraItem<T> {
    private static final Logger logger = LogManager.getLogger(AlgebraItemDecorator.class);

    private final Algebra<T> algebra;
    private final IAlgebraItem<T> algebraOperation;
    private final T value;
    private final IOperation<T> operation;

    AlgebraItemDecorator(Algebra<T> algebra, IAlgebraItem<T> algebraOperation, T value, IOperation<T> operation) {

        this.algebra = algebra;
        this.algebraOperation = algebraOperation;
        this.value = value;
        this.operation = operation;
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

        return algebra.buildAlgebraItem(operation.performOperation(algebraOperation.perform().getResult(), value));

    }
}
