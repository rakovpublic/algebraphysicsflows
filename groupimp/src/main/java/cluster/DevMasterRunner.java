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
public class DevMasterRunner<T, O, A extends IPath> extends MasterRunner<T,O,A> {
    private IInputSpliter<T> tiInputSpliter;
    private  IWriter<O> oiWriter;
    private  IAlgebraFlow<T> tiAlgebraFlow;
    private  IInputQueue<T> tiInputQueue;
    private  IPropertyHolder propertyHolder;


    public DevMasterRunner(IInputSpliter<T> tiInputSpliter, IWriter<O> oiWriter, IAlgebraFlow<T> tiAlgebraFlow, IInputQueue<T> tiInputQueue, IPropertyHolder propertyHolder) {
        this.tiInputSpliter = tiInputSpliter;
        this.oiWriter = oiWriter;
        this.tiAlgebraFlow = tiAlgebraFlow;
        this.tiInputQueue = tiInputQueue;
        this.propertyHolder = propertyHolder;
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
    public IInputSpliter<T> getInputSpliter() {
        return tiInputSpliter;
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
