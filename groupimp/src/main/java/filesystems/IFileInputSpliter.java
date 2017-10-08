package filesystems;

import cluster.IInputSpliter;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public interface IFileInputSpliter<T,P extends IPath,F extends IFileSystem<P>, R extends IFilePart<T,P,F>> extends IInputSpliter {
    List<R> split(List<P> paths, F fileSystem);
}
