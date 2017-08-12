package distribalgebra;

import algebra.imp.AlgebraItem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public interface IAlgebraFlow<T > extends Serializable {
    public IAlgebraFlow<T> performOperation(String operation, T element);
    public <K> IAlgebraFlow<K> performCustomResultOperation(String operationName, T sElement);
    public <K > IAlgebraFlow<K> performAlgebraTransfer(String operationName);
    public <K,V> IAlgebraFlow<K> performAlgebraUnsafe(String operationName,V element);
    public IAlgebraFlow<T> performFlatOperation(String operation, T element);
    public <K> IAlgebraFlow<K> performFlatCustomResultOperation(String operationName, T sElement);
    public <K > IAlgebraFlow<K> performFlatAlgebraTransfer(String operationName);
    public <K,V> IAlgebraFlow<K> performFlatAlgebraUnsafe(String operationName,V element);
    public <K> IAlgebraFlow<T> performCustomMemberOperation(String operationName, K sElement);
    public <K> IAlgebraFlow<T> performFlatCustomMemberOperation(String operationName, K sElement);
    public List<String> collect();
    public void write();
    public <K>List<AlgebraItem<K>> collectAlgebraItems();
    public Class getCurrentAlgebraItemClass();
    public String getCurrentAlgebraName();

}
