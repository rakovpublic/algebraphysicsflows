package operations.simple;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 01.04.2017.
 */
public interface IUnsafeOperation<T> extends Serializable, IAbsOperation {
    /**
     *
     * perform operation with 2 params types T and V return the result K.
     * @param t
     * @param v
     * @return IAlgebraItem parameterized K
     * @see IAlgebraItem
     * **/
    <K,V>IAlgebraItem<K> performOperation(T t,V v);

}
