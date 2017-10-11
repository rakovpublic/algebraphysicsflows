package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IFileSystem;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public abstract class NodeRunner<T,O,P extends IPath> implements ILauncher<T,O,P> {

    public static void main(String [] args){}
    abstract IPart<T> getNextPart();
    abstract IDeserializer<T,O,P> getDeserializer();


}
