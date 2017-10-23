package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IPath;
import utils.IInputQueue;
import utils.IPropertyHolder;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public interface ILauncher<T, O, P extends IPath> extends Runnable {
    IInputSpliter<T> getInputSpliter();

    IWriter<O> getWriter();

    IAlgebraFlow<T> getFlow();

    IInputQueue<T> getInputQueue();

    IPropertyHolder getPropertyHolder();
}
