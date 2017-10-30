package filesystems;

import cluster.IInputSpliter;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public interface IFileInputSpliter<T, P extends IPath,K extends IFilePointer> extends IInputSpliter {
    List<IFilePart<T,P,K>> split(List<P> paths,IFileSystem<P,K> fileSystem);
    List<P> getPaths();
    IFileSystem<P,K> getFileSystem();
    K pointerFromStr(String pointer);
    IFileFormat<T> getFileFormat();
}
