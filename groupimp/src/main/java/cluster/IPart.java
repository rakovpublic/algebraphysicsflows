package cluster;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 26.09.2017.
 */
public interface IPart<T> {

    /**
     *
     * @return content of part
     * */
    List<T> getContent();

    /**
     *
     * @return string representation of part i.e. path in file system or query for db
     * */
    String toStr();

}
