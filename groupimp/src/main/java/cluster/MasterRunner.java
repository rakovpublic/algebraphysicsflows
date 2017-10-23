package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IPath;
import utils.IInputQueue;
import utils.IPropertyHolder;

/**
 * Created by Rakovskyi Dmytro on 08.10.2017.
 */
public abstract class MasterRunner<T, O, A extends IPath> implements ILauncher<T, O, A>, Runnable {

    @Override
    public void run() {
        IPropertyHolder iPropertyHolder = getPropertyHolder();
        IInputSpliter<T> tiInputSpliter = getInputSpliter();
        IInputQueue<T> tiInputQueue = getInputQueue();
        tiInputQueue.putInput(tiInputSpliter.split());
    }
}
