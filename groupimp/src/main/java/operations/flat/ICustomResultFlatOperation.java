package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface ICustomResultFlatOperation<K > extends Serializable, IAbsOperation {
    /**
     * Operation between 2 same params K type return collection of elements V type
     *@param first
     *@param second
     *@return collection of IAlgebraItems parametrized V
     *@see IAlgebraItem
     * */
    <V>List<IAlgebraItem<V>> performOperation(K  first, K second);

}
