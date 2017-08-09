package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.MathTool;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface ITransferFlatOperation<K > extends Serializable, IAbsOperation {
    <V>IAlgebraItem<V> performOperation(K input);

}
