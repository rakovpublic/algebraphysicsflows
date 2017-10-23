package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface ITransferFlatOperation<K> extends Serializable, IAbsOperation {
    /**
     * Operation which perform transfer from one algebra to other as one to many
     *
     * @param input type K - item in flow
     * @return collection of IAlgebraItems parametrized K
     * @see IAlgebraItem
     */
    <V> IAlgebraItem<V> performOperation(K input);

}
