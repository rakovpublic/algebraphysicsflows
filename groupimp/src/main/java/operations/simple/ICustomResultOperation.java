package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 28.03.2017.
 */
public interface ICustomResultOperation<K> extends Serializable, IAbsOperation {
    /**
     *
     *
     * perform operation with 2 same type K params return new type V
     * @param first
     * @param second
     * @return IAlgebraItem parametrized V
     * @see IAlgebraItem
     *
     * */
    <V>IAlgebraItem<V> performOperation(K  first, K second);

}
