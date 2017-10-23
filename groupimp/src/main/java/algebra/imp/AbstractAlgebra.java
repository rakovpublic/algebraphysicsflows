package algebra.imp;

import algebra.IAlgebraItem;
import exceptions.NotMemberException;
import operations.flat.*;
import operations.simple.ICustomMemberOperation;
import operations.simple.ICustomResultOperation;
import operations.simple.ITransferOperation;
import operations.simple.IUnsafeOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 31.03.2017.
 */
public abstract class AbstractAlgebra<T> implements IAlgebraItem<T> {
    private static final Logger logger = LogManager.getLogger(AbstractAlgebra.class);

    /**
     * perform operation with two elements of type T and return result IAlgebraItem T
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItem parametrized T
     * @see operations.simple.IOperation
     */
    @Override
    public IAlgebraItem<T> performOperation(String operationName, T sElement) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasOperation(operationName)) {
            if (algebra.validate(sElement)) {
                return new AlgebraItemDecorator<T>(algebra, this.getCurrentItem(), sElement, algebra.getOperation(operationName));
            }
            NotMemberException ex = new NotMemberException("Element" + sElement.toString() + " is not member of this algebra.Check validation rules of algebra " + algebra.toString());
            logger.error("Invalid element", ex);
            throw ex;
        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }


    /**
     * perform custom result operation with two elements of type T and return IAlgebraItem  K
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItem parametrized K
     * @see operations.simple.ICustomResultOperation
     */
    @Override
    public <K> IAlgebraItem<K> performCustomResultOperation(String operationName, T sElement) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasCustomResultOperation(operationName)) {
            ICustomResultOperation<T> op = algebra.getCustomResultOperation(operationName);
            if (algebra.validate(sElement)) {
                return op.performOperation(perform().getResult(), sElement);
            }
            NotMemberException ex = new NotMemberException("Element" + sElement.toString() + " is not member of this algebra.Check validation rules of algebra " + algebra.toString());
            logger.error("Invalid element", ex);
            throw ex;
        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform transfer operation with  element of type T and return IAlgebraItem  K
     *
     * @param operationName operation name
     * @return IAlgebraItem parametrized K
     * @see operations.simple.ITransferOperation
     */
    @Override
    public <K> IAlgebraItem<K> performAlgebraTransfer(String operationName) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasAlgebraTransfer(operationName)) {
            ITransferOperation<T> op = algebra.getTransferOperation(operationName);
            return op.performOperation(perform().getResult());

        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform operation  with two elements of type T and return IAlgebraItems  parametrized K list
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItems  parametrized K list
     * @see operations.flat.ICustomResultFlatOperation
     */
    @Override
    public <K> List<IAlgebraItem<K>> performCustomResultFlatOperation(String operationName, T sElement) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasCustomResultFlatOperation(operationName)) {
            ICustomResultFlatOperation<T> op = algebra.getCustomResultFlatOperation(operationName);
            if (algebra.validate(sElement)) {
                return op.performOperation(perform().getResult(), sElement);
            }
            NotMemberException ex = new NotMemberException("Element" + sElement.toString() + " is not member of this algebra.Check validation rules of algebra " + algebra.toString());
            logger.error("Invalid element", ex);
            throw ex;
        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform transfer operation for each element in flow with  element of type T and return IAlgebraItems  parametrized K list
     *
     * @param operationName operation name
     * @return IAlgebraItems  parametrized K list
     * @see operations.flat.ITransferFlatOperation
     */
    @Override
    public <V> List<IAlgebraItem<V>> performAlgebraFlatTransfer(String operationName) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasAlgebraFlatTransfer(operationName)) {
            ITransferFlatOperation<T> op = algebra.getTransferFlatOperation(operationName);
            return (List<IAlgebraItem<V>>) op.performOperation(perform().getResult());

        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform unsafe operation for each element in flow with two elements of type T and IAlgebraItems  parametrized T list
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItems  parametrized T list
     * @see operations.flat.IFlatOperation
     */
    @Override
    public List<IAlgebraItem<T>> performFlatOperation(String operationName, T sElement) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasFlatOperation(operationName)) {
            IFlatOperation<T> op = algebra.getFlatOperation(operationName);
            if (algebra.validate(sElement)) {
                return op.performOperation(perform().getResult(), sElement);
            }
            NotMemberException ex = new NotMemberException("Element" + sElement.toString() + " is not member of this algebra.Check validation rules of algebra " + algebra.toString());
            logger.error("Invalid element", ex);
            throw ex;
        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type V return IAlgebraItem K
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItem parametrized K
     * @see operations.simple.IUnsafeOperation
     */
    @Override
    public <K, V> IAlgebraItem<K> performUnsafeOperation(String operationName, V element) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasUnsafeOperation(operationName)) {
            IUnsafeOperation<T> op = algebra.getUnsafeOperation(operationName);
            return op.<K, V>performOperation(perform().getResult(), element);
        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type V return IAlgebraItems  parametrized K list
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItems  parametrized K list
     * @see operations.flat.IUnsafeFlatOperation
     * @see IAlgebraItem
     */
    @Override
    public <K, V> List<IAlgebraItem<K>> performUnsafeFlatOperation(String operationName, V element) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasUnsafeFlatOperation(operationName)) {
            IUnsafeFlatOperation<T> op = algebra.getUnsafeFlatOperation(operationName);
            return op.<K, V>performOperation(perform().getResult(), element);
        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type K return IAlgebraItem T
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItem parametrized K
     * @see operations.simple.ICustomMemberOperation
     */
    @Override
    public <K> IAlgebraItem<T> performCustomMemberOperation(String operationName, K element) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasCustomMemberOperation(operationName)) {
            ICustomMemberOperation<T> op = algebra.getCustomMemberOperation(operationName);

            return op.performOperation(perform().getResult(), element);

        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }

    /**
     * perform operation for each element in flow with  elements type T and  element type K return IAlgebraItems  parametrized T list
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItems  parametrized T list
     * @see operations.flat.ICustomMemberFlatOperation
     */
    @Override
    public <K> List<IAlgebraItem<T>> performCustomMemberFlatOperation(String operationName, K element) {
        Algebra<T> algebra = this.getAlgebra();
        if (this.getAlgebra().hasCustomMemberFlatOperation(operationName)) {
            ICustomMemberFlatOperation<T> op = algebra.getCustomMemberFlatOperation(operationName);
            return op.performOperation(perform().getResult(), element);

        }
        UnsupportedOperationException ex = new UnsupportedOperationException("Operation" + operationName + " is not exists in this algebra.");
        logger.error("Invalid operation name.", ex);
        throw ex;
    }
}
