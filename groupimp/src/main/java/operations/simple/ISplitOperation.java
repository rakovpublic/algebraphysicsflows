package operations.simple;

import algebra.IAlgebraItem;
import distribalgebra.imp.KeyValuePair;
import operations.IAbsOperation;

import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 31.03.2017.
 */
public interface ISplitOperation< I > extends Serializable, IAbsOperation {

    public <K,V>KeyValuePair<K,V> performSplit(IAlgebraItem<I> input);
    public String getKeyAlgebraName();
    public String getValueAlgebraName();
}
