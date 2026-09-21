package algebra;

import algebra.imp.Algebra;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 29.03.2017.
 */
public interface IAlgebraItem<T> extends Serializable {
    /**
     * perform operation with two elements of type T and return result IAlgebraItem T
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItem parametrized T
     * @see operations.simple.IOperation
     */
    public IAlgebraItem<T> performOperation(String operationName, T sElement);

    /** Evaluate a registered A -> A operation after any pending binary operations. */
    default IAlgebraItem<T> performOneOperandOperation(String operationName) {
        Algebra<T> algebra=getAlgebra();
        operations.simple.IOneOperandOperation<T> operation=algebra.getOneOperandOperation(operationName);
        if(operation==null) throw new exceptions.UnsupportedOperationException("No one-operand operation " + operationName + " in " + algebra.getAlgebraName());
        T value=operation.performOperation(perform().getResult());
        if(!algebra.getParamClass().isInstance(value)) throw new exceptions.NotMemberException("Invalid unary result representation in " + algebra.getAlgebraName());
        IAlgebraItem<T> result=algebra.buildAlgebraItem(value);
        if(result==null) throw new exceptions.NotMemberException("One-operand operation " + operationName + " returned a nonmember of " + algebra.getAlgebraName());
        return result;
    }

    /** Unary overload of the existing operation entry point. */
    default IAlgebraItem<T> performOperation(String operationName) {
        return performOneOperandOperation(operationName);
    }

    default List<IAlgebraItem<T>> performOneOperandFlatOperation(String operationName) {
        Algebra<T> algebra=getAlgebra();
        operations.flat.IOneOperandFlatOperation<T> operation=algebra.getOneOperandFlatOperation(operationName);
        if(operation==null) throw new exceptions.UnsupportedOperationException("No flat one-operand operation " + operationName);
        List<IAlgebraItem<T>> results=operation.performOperation(perform().getResult());
        if(results==null) throw new exceptions.NotMemberException("Flat unary result list is null");
        List<IAlgebraItem<T>> evaluated=new java.util.ArrayList<>();
        for(IAlgebraItem<T> item : results) {
            if(item==null || item.getAlgebra()!=algebra)
                throw new exceptions.NotMemberException("Flat unary result is outside " + algebra.getAlgebraName());
            IAlgebraItem<T> member=item.perform();
            if(member==null || !algebra.getParamClass().isInstance(member.getResult()) || !algebra.validate(member.getResult()))
                throw new exceptions.NotMemberException("Flat unary result is outside " + algebra.getAlgebraName());
            evaluated.add(member);
        }
        return evaluated;
    }

    default List<IAlgebraItem<T>> performFlatOperation(String operationName) {
        return performOneOperandFlatOperation(operationName);
    }

    /** A x B -> B, with the result in the second operand's algebra. */
    <V> IAlgebraItem<V> performLeftProjectionOperation(String operationName, V second);

    /** A x B -> List(B), preserving ordered duplicates. */
    <V> List<IAlgebraItem<V>> performLeftProjectionFlatOperation(String operationName, V second);

    /**
     * perform custom result operation with two elements of type T and return IAlgebraItem  K
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItem parametrized K
     * @see operations.simple.ICustomResultOperation
     */
    public <K> IAlgebraItem<K> performCustomResultOperation(String operationName, T sElement);

    /**
     * perform transfer operation with  element of type T and return IAlgebraItem  K
     *
     * @param operationName operation name
     * @return IAlgebraItem parametrized K
     * @see operations.simple.ITransferOperation
     */
    public <K> IAlgebraItem<K> performAlgebraTransfer(String operationName);

    /**
     * @return the value of AlgebraItem
     */
    public T getResult();

    /**
     * @return algebra object
     */
    Algebra<T> getAlgebra();

    /**
     * @return this object
     */
    IAlgebraItem<T> getCurrentItem();

    /**
     * perform stored operations
     *
     * @return IAlgebraItem
     */
    public IAlgebraItem<T> perform();

    /**
     * perform operation  with two elements of type T and return IAlgebraItems  parametrized K list
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItems  parametrized K list
     * @see operations.flat.ICustomResultFlatOperation
     */
    public <K> List<IAlgebraItem<K>> performCustomResultFlatOperation(String operationName, T sElement);

    /**
     * perform transfer operation for each element in flow with  element of type T and return IAlgebraItems  parametrized K list
     *
     * @param operationName operation name
     * @return IAlgebraItems  parametrized K list
     * @see operations.flat.ITransferFlatOperation
     */
    public <K> List<IAlgebraItem<K>> performAlgebraFlatTransfer(String operationName);

    /**
     * perform unsafe operation for each element in flow with two elements of type T and IAlgebraItems  parametrized T list
     *
     * @param operationName operation name
     * @param sElement      second element for operation
     * @return IAlgebraItems  parametrized T list
     * @see operations.flat.IFlatOperation
     */
    public List<IAlgebraItem<T>> performFlatOperation(String operationName, T sElement);

    /**
     * perform operation for each element in flow with  elements type T and  element type V return IAlgebraItem K
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItem parametrized K
     * @see operations.simple.IUnsafeOperation
     */
    public <K, V> IAlgebraItem<K> performUnsafeOperation(String operationName, V element);

    /**
     * perform operation for each element in flow with  elements type T and  element type V return IAlgebraItems  parametrized K list
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItems  parametrized K list
     * @see operations.flat.IUnsafeFlatOperation
     * @see IAlgebraItem
     */
    public <K, V> List<IAlgebraItem<K>> performUnsafeFlatOperation(String operationName, V element);

    /**
     * perform operation for each element in flow with  elements type T and  element type K return IAlgebraItem T
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItem parametrized K
     * @see operations.simple.ICustomMemberOperation
     */
    public <K> IAlgebraItem<T> performCustomMemberOperation(String operationName, K element);

    /**
     * perform operation for each element in flow with  elements type T and  element type K return IAlgebraItems  parametrized T list
     *
     * @param operationName operation name
     * @param element       second element for operation
     * @return IAlgebraItems  parametrized T list
     * @see operations.flat.ICustomMemberFlatOperation
     */
    public <K> List<IAlgebraItem<T>> performCustomMemberFlatOperation(String operationName, K element);

}
