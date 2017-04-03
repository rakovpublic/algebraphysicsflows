package operations.flat;

import algebra.IAlgebraItem;
import algebra.imp.SuperAlgebra;
import distribalgebra.imp.KeyValuePair;
import operations.IAbsOperation;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface ISplitFlatOperation <I> extends Serializable, IAbsOperation {
    public <K,V>List<KeyValuePair<K,V>> performSplit(IAlgebraItem<I> input);
    public String getKeyAlgebraName();
    public String getValueAlgebraName();

}
