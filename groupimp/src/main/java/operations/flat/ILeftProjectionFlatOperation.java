package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Flat operation between independent operand types that returns the first
 * operand as a single item in its algebra.
 *
 * @param <T> first operand and result type
 * @param <V> second operand type
 */
public interface ILeftProjectionFlatOperation<T, V> extends Serializable, IAbsOperation {
    List<IAlgebraItem<T>> performOperation(T first, V second);
}
