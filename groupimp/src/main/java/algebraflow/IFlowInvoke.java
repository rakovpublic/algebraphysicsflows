package algebraflow;

import algebra.IAlgebraItem;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface IFlowInvoke<T> {
    List<IAlgebraItem<T>> perform();
}
