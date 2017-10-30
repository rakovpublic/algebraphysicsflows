package cluster;


import filesystems.IPath;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */

/**
 * Describes spliting input on parts for nodes
 *
 * */
public interface IInputSpliter<T> {

    /**
     *
     * splits input on parts
     * @return list of IPart
     * @see IPart
     * */
    List<IPart<T>> split();



    /**
     * Parse part from string
     * @param  part - string representation of part
     * @return
     * @see IPart
     * */
    IPart<T> partFromString(String part);
}
