package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 10.08.2017.
 */
public interface ICustomMemberFlatOperation <K > extends Serializable, IAbsOperation {
    <V>List<IAlgebraItem<K>> performOperation(K  first, V second);
}
