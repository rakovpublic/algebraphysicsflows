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

    /**
     * @return
     * @see IInputSpliter
     * */
    IInputSpliter<T> getInputSpliter();


    /**
     * @return
     * @see IWriter
     * */
    IWriter<O> getWriter();



    /**
     * @return
     * @see IAlgebraFlow
     * */
    IAlgebraFlow<T> getFlow();


    /**
     * @return
     * @see IInputQueue
     * */
    IInputQueue<T> getInputQueue();

    /**
     * @return
     * @see IPropertyHolder
     * */
    IPropertyHolder getPropertyHolder();
}
