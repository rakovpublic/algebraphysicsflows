package operations.simple;


import operations.IAbsOperation;

import java.io.Serializable;

//TODO: add implementation to flow
public interface IOneOperandOperation<K> extends Serializable, IAbsOperation {
     K performOperation(K input);

}
