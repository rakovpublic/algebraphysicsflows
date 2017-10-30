package filesystems;

import cluster.IPart;

import java.io.InputStream;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public interface IFilePart<T,P extends IPath,K extends IFilePointer> extends IPart<T> {
    InputStream getStream();
    K getPointer();
    P getPath();
    IFileSystem<P,K> getFileSystem();
    IFileFormat<T> getFileFormat();

}
