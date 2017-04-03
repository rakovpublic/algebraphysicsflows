package operations.simple;


import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 23.03.2017.
 */
public interface IOperation<T> extends Serializable, IAbsOperation {
   T performOperation(T  first, T second);
}
