package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface IUnsafeFlatOperation<T> extends IAbsOperation,Serializable {
    /**
     * Operation between two params with types T,V return collection of IAlgebraItems parametrized K
     * @param t
     * @param v
     * @return collection of IAlgebraItems parametrized K
     * @see IAlgebraItem
     * */
    <K,V> List<IAlgebraItem<K>> performOperation(T t, V v);
}
