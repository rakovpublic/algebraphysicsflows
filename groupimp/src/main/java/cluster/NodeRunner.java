package cluster;

import algebraflow.IAlgebraFlow;
import algebraflow.IWriter;
import filesystems.IFileSystem;
import filesystems.IPath;
import utils.IPropertyHolder;

/**
 * Created by Rakovskyi Dmytro on 09.10.2017.
 */
public abstract class NodeRunner<T, O, P extends IPath> implements ILauncher<T, O, P>, Runnable {

    @Override
    public void run() {
        ThreadCounter.getInstance().increment();
        IInputSpliter<T> tiInputSpliter = getInputSpliter();
        IAlgebraFlow<T> algebraFlow = getFlow();
        IWriter<O> tiWriter = getWriter();
        String part = null;
        while ((part = getInputQueue().getNextPath()) != null) {
            algebraFlow.setInput(tiInputSpliter.partFromString(part));
            algebraFlow.write(tiWriter);
        }
        ThreadCounter.getInstance().decrement();
    }
}
