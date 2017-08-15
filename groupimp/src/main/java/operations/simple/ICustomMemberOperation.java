package operations.simple;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 10.08.2017.
 */
public interface ICustomMemberOperation<K > extends Serializable, IAbsOperation {
    /**
     *
     * Operation between 2 different type return first type(initial type) example:  matrix multiply integer.
     * @param first
     * @param second
     * @return  IAlgebraItem parametrized by type of param first
     * @see IAlgebraItem
     *
     * */
    <V> IAlgebraItem<K> performOperation(K  first, V second);
}
