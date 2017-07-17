package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface ICustomFlatOperation <K > extends Serializable, IAbsOperation {
    <V>List<IAlgebraItem<V>> performOperation(K  first, K second);

}
