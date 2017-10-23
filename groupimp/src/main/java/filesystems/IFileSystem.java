package filesystems;


import java.io.InputStream;
import java.io.Serializable;

/**
 * Created by Rakovskyi Dmytro on 15.09.2017.
 */
public interface IFileSystem<T extends IPath> extends Serializable {
    InputStream getStream(T path);

    T getPathFromString(String path);

}
