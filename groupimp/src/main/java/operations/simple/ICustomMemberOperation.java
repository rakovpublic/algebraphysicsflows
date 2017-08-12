package operations.simple;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 10.08.2017.
 */
public interface ICustomMemberOperation<K > extends Serializable, IAbsOperation {
    <V> IAlgebraItem<K> performOperation(K  first, V second);
}
