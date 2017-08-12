package distribalgebra;

import algebra.IAlgebraItem;

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
    public<K> void write(IWriter<K> writer);
    public <K>List<IAlgebraItem<K>> collectAlgebraItems();
    public Class getCurrentAlgebraItemClass();
    public String getCurrentAlgebraName();

}
