package algebraflow;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import filesystems.IFileSystem;
import filesystems.IPath;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 30.03.2017.
 */
public interface InputFormat<T> extends Serializable {
    // P extends IPath,F extends IFileSystem<P>
    /**
     *
     * @return type of desired input
     * */
    Class<T> getInputType();
    /**
     * @param algebra which will be used to build items from input
     * @return IAlgebraItems list
     * */
    List<IAlgebraItem<T>> read(Algebra<T> algebra);
    Algebra<T> getAlgebra();
}
