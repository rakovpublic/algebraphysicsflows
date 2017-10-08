package filesystems;

import cluster.IPart;
import filesystems.IFileSystem;
import filesystems.IPath;

import java.io.InputStream;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public interface IFilePart<T,P extends IPath,F extends IFileSystem<P>> extends IPart<T> {
    InputStream getStream();

}
