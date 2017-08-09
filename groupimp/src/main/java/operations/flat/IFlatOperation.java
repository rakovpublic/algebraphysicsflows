package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface IFlatOperation<T extends Object> extends Serializable, IAbsOperation {
    List<IAlgebraItem<T>> performOperation(T  first, T second);
}
