package utils;

import cluster.IPart;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 22.10.2017.
 */
public interface IInputQueue<T> {
    /**
     * @return  string representation of next part in queue
     * */
    String getNextPath();
/**
 * @param in - list of parts for processing
 * @see IPart
 * */
    void putInput(List<IPart<T>> in);

    /**
     * @return
     * */
    boolean hasNextInput();
}
