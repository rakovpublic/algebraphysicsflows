package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IFileSystem;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public abstract class Runner <T,O,P extends IPart<T>,A extends IPath,F extends IFileSystem<A>>{
    abstract IInputSpliter<T,P> getInputSpliter();
    abstract IAlgebraFlow<T> getFlow();
    abstract IWriter<O> getWriter();
    public static void main(String [] args){}

}
