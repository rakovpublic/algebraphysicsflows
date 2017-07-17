package distribalgebra;

import algebra.imp.AlgebraItem;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public interface IAlgebraFlow<T > extends Serializable {
    public IAlgebraFlow<T> performOperation(String operation, T element);
    public <K> IAlgebraFlow<K> performCustomOperation(String operationName, T sElement);
    public <K > IAlgebraFlow<K> performAlgebraTransfer(String operationName);
    public List<String> collect();
    public void write();
    public <K>List<AlgebraItem<K>> collectAlgebraItems();
    public Class getCurrentAlgebraItemClass();
    public String getCurrentAlgebraName();
    public<K,V> IAlgebraKeyValueFlow<K,V> splitToKeyValue(String operationName);

}
