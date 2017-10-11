package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IFileSystem;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public abstract class Runner <T,O,A extends IPath> implements ILauncher<T,O,A>{
    abstract IInputSpliter<T> getInputSpliter();
    abstract ISerializer<T,O,A> getSerializer();
    public static void main(String [] args){

    }

}
