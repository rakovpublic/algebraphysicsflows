package operations.simple;


import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 23.03.2017.
 */
public interface IOperation<T> extends Serializable, IAbsOperation {
    /**
     * perform operation with 2 params T return T
     *
     * @param first
     * @param second
     * @return T
     */
    T performOperation(T first, T second);
}
