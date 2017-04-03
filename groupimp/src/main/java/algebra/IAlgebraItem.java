package algebra;

import algebra.imp.Algebra;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 29.03.2017.
 */
public interface IAlgebraItem<T> extends Serializable {
    public IAlgebraItem<T> performOperation(String operationName, T sElement);
    public IAlgebraItem<T> performOperations(List<IOperationInvoke<T>> operationInvokes);
    public <K>IAlgebraItem<K> performCustomOperation(String operationName, T sElement);
    public <K>IAlgebraItem<K> performAlgebraTransfer(String operationName);
    public T getResult();
    Algebra<T> getAlgebra();
    IAlgebraItem<T> getCurrentItem();
    public IAlgebraItem<T> perform();
    public <K>List<IAlgebraItem<K>> performCustomFlatOperation(String operationName, T sElement);
    public <K>List<IAlgebraItem<K>> performAlgebraFlatTransfer(String operationName);
    public List<IAlgebraItem<T>> performFlatOperation(String operationName, T sElement);

}
