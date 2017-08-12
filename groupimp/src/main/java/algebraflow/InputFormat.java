package algebraflow;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public interface InputFormat<T> extends Serializable {
    Class<T> getInputType();
    List<IAlgebraItem<T>> read(Algebra<T> algebra);
}
