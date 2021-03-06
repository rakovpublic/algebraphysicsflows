package cluster.factory;


import cluster.ILauncher;
import filesystems.IPath;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public interface IRunnerFactory<T,O,P extends IPath> {
    /**
     * build launcher for mode
     * @param mode launcher mode
     * @return
     * @see ILauncher
     * */
    ILauncher<T,O,P> build(String mode);
}
