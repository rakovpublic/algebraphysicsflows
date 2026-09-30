package operations.flat;

import algebra.IAlgebraItem;
import operations.IAbsOperation;
import java.io.Serializable;
import java.util.List;

/** One operand produces a finite ordered list of members in the same algebra. */
public interface IOneOperandFlatOperation<T> extends Serializable,IAbsOperation {
    List<IAlgebraItem<T>> performOperation(T input);
}

