package cluster;

import algebraflow.IWriter;
import algebraflow.imp.AlgebraFlow;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface ISerializer<I,O,F extends AlgebraFlow<I>,W extends IWriter<O>,P extends IPath> {
    P serializeFlow(F flow);
    P serializeWriter(W writer);
}
