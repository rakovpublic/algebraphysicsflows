package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IFileSystem;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface ILauncher<T,O,P extends IPath> {
    IFileSystem<P> getFileSystem();
    IWriter<O> getWriter();
    IAlgebraFlow<T> getFlow();
}
