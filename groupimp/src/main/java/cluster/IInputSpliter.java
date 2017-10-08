package cluster;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface IInputSpliter<T,R extends IPart<T>>{
    List<R> split();
}
