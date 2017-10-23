package algebraflow;

import algebra.IAlgebraItem;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 02.04.2017.
 */
public interface IFlowInvoke<T> {
    /**
     * encapsulate operation invoke in flow
     *
     * @return IAlgebraItems list
     */
    List<IAlgebraItem<T>> perform();

    String getAlgebraName();

}
