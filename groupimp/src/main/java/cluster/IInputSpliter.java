package cluster;


import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface IInputSpliter<T> {
    List<IPart<T>> split();

    IPart<T> partFromString(String part);
}
