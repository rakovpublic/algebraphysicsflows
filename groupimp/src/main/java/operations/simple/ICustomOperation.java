package operations.simple;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 28.03.2017.
 */
public interface ICustomOperation<K> extends Serializable, IAbsOperation {
    <V>IAlgebraItem<V> performOperation(K  first, K second);

}
