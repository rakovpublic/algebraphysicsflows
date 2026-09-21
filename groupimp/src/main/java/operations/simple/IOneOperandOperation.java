package operations.simple;


import operations.IAbsOperation;

import java.io.Serializable;

/** One operand in an algebra produces a member of the same algebra (A -> A). */
public interface IOneOperandOperation<K> extends Serializable, IAbsOperation {
     K performOperation(K input);

}
