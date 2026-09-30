package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Flat operation A x B -> List(B). The historical interface name is retained.
 *
 * @param <T> first operand type
 * @param <V> second operand and result type
 */
public interface ILeftProjectionFlatOperation<T, V> extends Serializable, IAbsOperation {
    List<IAlgebraItem<V>> performOperation(T first, V second);
}
