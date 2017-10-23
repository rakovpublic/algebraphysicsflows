package cluster.factory;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import cluster.*;
import filesystems.IFileSystem;
import filesystems.IPath;
import utils.IInputQueue;
import utils.IPropertyHolder;

/**
 * Created by Rakovskyi Dmytro on 24.10.2017.
 */
public class RunnerFactory<T,O,P extends IPath> implements IRunnerFactory<T,O,P>{

    protected IInputSpliter<T> tiInputSpliter;
    protected IWriter<O> oiWriter;
    protected IAlgebraFlow<T> tiAlgebraFlow;
    protected IInputQueue<T> tiInputQueue;
    protected IPropertyHolder propertyHolder;


    public RunnerFactory(IInputSpliter<T> tiInputSpliter, IWriter<O> oiWriter, IAlgebraFlow<T> tiAlgebraFlow) {
        this.tiInputSpliter = tiInputSpliter;
        this.oiWriter = oiWriter;
        this.tiAlgebraFlow = tiAlgebraFlow;
        /**
         *
         * for host holders. please add initiating of holders part here or inherit this class
         * */
    }

    @Override
    public ILauncher<T,O,P> build(String mode) {
        if("master".equalsIgnoreCase(mode)){
            return new DevMasterRunner<T,O,P>(tiInputSpliter,oiWriter,tiAlgebraFlow,tiInputQueue,propertyHolder);
        }
        return new DevNodeRunner<T, O, P>(tiInputSpliter,tiInputQueue,propertyHolder,oiWriter,tiAlgebraFlow);
    }
}
