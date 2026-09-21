package operations.simple;

import operations.IAbsOperation;
import algebra.IAlgebraItem;

import java.io.Serializable;

/**
 * Operation A x B -> B. The historical interface name is retained.
 *
 * @param <T> first operand type
 * @param <V> second operand and result type
 */
public interface ILeftProjectionOperation<T, V> extends Serializable, IAbsOperation {
    IAlgebraItem<V> performOperation(T first, V second);
}
