package utils;

import cluster.IPart;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 22.10.2017.
 */
public interface IInputQueue<T> {
    String getNextPath();

    void putInput(List<IPart<T>> in);

    boolean hasNextInput();
}
