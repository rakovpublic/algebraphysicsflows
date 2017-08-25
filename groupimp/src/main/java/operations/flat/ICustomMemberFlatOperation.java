package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 10.08.2017.
 */
public interface ICustomMemberFlatOperation <K > extends Serializable, IAbsOperation {
    /**
     *
     * Operation between 2 different type params return collection of first type(initial type)
     * @param first
     * @param second
     * @return  List of IAlgebraItem parametrized by type of param first
     * @see IAlgebraItem
     *
     * */
    <V>List<IAlgebraItem<K>> performOperation(K  first, V second);
}
