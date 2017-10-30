package filesystems;


import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 15.09.2017.
 */
public interface IFileSystem<T extends IPath,K extends IFilePointer> extends Serializable {
    InputStream getStream(T path);
    InputStream getStream(T path,K pointer);

    T getPathFromString(String path);
    K getPointerFromString(String pointer);

}
