package cluster;

import algebraflow.IWriter;
import algebraflow.imp.AlgebraFlow;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface IDeserializer<I,O,P extends IPath> {
    AlgebraFlow<I> deserializeFlow(P path);
    IWriter<O> deserializeWriter(P path);
}
