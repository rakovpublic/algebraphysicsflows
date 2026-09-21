package operations.simple;

import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Operation between independent operand types that returns the first operand.
 *
 * @param <T> first operand and result type
 * @param <V> second operand type
 */
public interface ILeftProjectionOperation<T, V> extends Serializable, IAbsOperation {
    T performOperation(T first, V second);
}
