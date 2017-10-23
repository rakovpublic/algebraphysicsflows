package operations.simple;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 28.03.2017.
 */
public interface ITransferOperation<K> extends Serializable, IAbsOperation {
    /**
     * transfer element from one algebra to other
     *
     * @param input
     * @return IAlgebraItem parametrized V
     * @see IAlgebraItem
     */
    <V> IAlgebraItem<V> performOperation(K input);

}
