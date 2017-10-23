package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IFileSystem;
import filesystems.IPath;
import utils.IInputQueue;
import utils.IPropertyHolder;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public class DevNodeRunner<T, O, P extends IPath>  extends NodeRunner<T, O, P >  {
    private IInputSpliter<T> tiInputSpliter;
    private  IInputQueue<T> tiInputQueue;
    private  IPropertyHolder propertyHolder;
    private  IWriter<O> oiWriter;
    private  IAlgebraFlow<T> tiAlgebraFlow;


    public DevNodeRunner(IInputSpliter<T> tiInputSpliter, IInputQueue<T> tiInputQueue, IPropertyHolder propertyHolder, IWriter<O> oiWriter, IAlgebraFlow<T> tiAlgebraFlow) {
        this.tiInputSpliter = tiInputSpliter;
        this.tiInputQueue = tiInputQueue;
        this.propertyHolder = propertyHolder;
        this.oiWriter = oiWriter;
        this.tiAlgebraFlow = tiAlgebraFlow;
    }

    @Override
    public IInputSpliter<T> getInputSpliter() {
        return tiInputSpliter;
    }

    @Override
    public IWriter<O> getWriter() {
        return oiWriter;
    }

    @Override
    public IAlgebraFlow<T> getFlow() {
        return tiAlgebraFlow;
    }


    @Override
    public IInputQueue<T> getInputQueue() {
        return tiInputQueue;
    }

    @Override
    public IPropertyHolder getPropertyHolder() {
        return propertyHolder;
    }

}
