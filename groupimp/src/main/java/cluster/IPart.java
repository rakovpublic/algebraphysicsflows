package cluster;

import filesystems.IFileSystem;
import filesystems.IPath;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 26.09.2017.
 */
public interface IPart<T> {
    List<T> getContent();
    String toStr();
    IPart<T> fromStr(String str);
}
