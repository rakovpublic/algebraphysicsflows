package algebraflow;

import algebra.IAlgebraItem;
import cluster.IPart;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public interface IAlgebraFlow<T> extends Serializable {
    /**
     * perform operation for each element in flow with two elements of type T and return result type T
     * @param operation operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.simple.IOperation
     * */
    public IAlgebraFlow<T> performOperation(String operation, T element);
    /**
     * perform custom result operation for each element in flow with two elements of type T and return result type K
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomResultOperation
     * */
    public <K> IAlgebraFlow<K> performCustomResultOperation(String operationName, T sElement);
    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ITransferOperation
     * */
    public <K > IAlgebraFlow<K> performAlgebraTransfer(String operationName);
    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     * @param operationName operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.IUnsafeOperation
     * */
    public <K,V> IAlgebraFlow<K> performAlgebraUnsafe(String operationName,V element);
    /**
     * perform unsafe operation for each element in flow with two elements of type T and return result type T
     * @param operation operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized T
     * @see operations.flat.IFlatOperation
     * */
    public IAlgebraFlow<T> performFlatOperation(String operation, T element);
    /**
     * perform operation for each element in flow with two elements of type T and return result type K
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomResultFlatOperation
     * */
    public <K> IAlgebraFlow<K> performFlatCustomResultOperation(String operationName, T sElement);
    /**
     * perform transfer operation for each element in flow with  element of type T and return result type K
     * @param operationName operation name
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ITransferFlatOperation
     * */
    public <K > IAlgebraFlow<K> performFlatAlgebraTransfer(String operationName);
    /**
     * perform operation for each element in flow with  elements type T and  element type V return result type K
     * @param operationName operation name
     * @param element second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.IUnsafeFlatOperation
     * */
    public <K,V> IAlgebraFlow<K> performFlatAlgebraUnsafe(String operationName,V element);
    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.simple.ICustomMemberOperation
     * */
    public <K> IAlgebraFlow<T> performCustomMemberOperation(String operationName, K sElement);
    /**
     * perform operation for each element in flow with  elements type T and  element type K return result type T
     * @param operationName operation name
     * @param sElement second element for operation
     * @return IAlgebraFlow parametrized K
     * @see operations.flat.ICustomMemberFlatOperation
     * */
    public <K> IAlgebraFlow<T> performFlatCustomMemberOperation(String operationName, K sElement);
    /**
     *collect items from flow and return it as list of strings, toString method used
     *@return List of items in string view
     * */
    public List<String> collect();
    /**
     *writes items to storage
     * @param writer
     *@see IWriter
     * */
    public<K> void write(IWriter<K> writer);
    /**
     *collects elements from flow as list of IAlgebraItems
     * @return list of IAlgebraItems
     * @see IAlgebraItem
     * */
    public <K>List<IAlgebraItem<K>> collectAlgebraItems();
    /**
     *@return class which used for current AlgebraItem  parametrization
     * */
    public Class getCurrentAlgebraItemClass();
    /**
     *@return current algebra name
     * */
    public String getCurrentAlgebraName();


    /**
     * Set part of input for worker.
     * @param part part of input
     * @see IPart
     * @param startAlgebra name of initial algebra
     *
     * */
    public void setInput(IPart<T> part,String startAlgebra);

}
