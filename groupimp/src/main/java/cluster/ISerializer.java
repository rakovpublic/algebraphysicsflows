package cluster;

import algebraflow.IWriter;
import algebraflow.imp.AlgebraFlow;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface ISerializer<I,O,P extends IPath> {
    P serializeFlow(AlgebraFlow<I> flow);
    P serializeWriter(IWriter<O> writer);
}
