package distribalgebra;

import algebra.IAlgebraItem;
import algebra.imp.AlgebraItem;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 12.08.2017.
 */
public interface IWriter<T> {
    public void write(List<IAlgebraItem<T>> input);
}
