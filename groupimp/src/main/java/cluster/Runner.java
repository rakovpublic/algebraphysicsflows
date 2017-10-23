package cluster;

import cluster.factory.RunnerFactory;
import filesystems.IPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public final class Runner {
    private static final Logger logger = LogManager.getLogger(Runner.class);

    public static <T, O, A extends IPath> void main(String[] args) {

       RunnerFactory<T,O,A> runnerFactory = null;//init configuration here
        if (args[0].equalsIgnoreCase("master")) {
            runnerFactory.build("master").run();
            INodeStarter iNodeStarter = null;
            iNodeStarter.startNodes();
        } else {
            while (true) {
                ILauncher<T,O,A> nodeRunner=runnerFactory.build("node");
                if (ThreadCounter.getInstance().getCounter() < Integer.parseInt(nodeRunner.getPropertyHolder().getProperty("runner.threads"))) {
                    Thread thread = new Thread(nodeRunner);
                    thread.start();
                } else if (nodeRunner.getInputQueue().hasNextInput()) {
                    try {
                        Thread.sleep(5000l);
                    } catch (InterruptedException e) {
                        logger.error(e);
                        break;
                    }
                } else {
                    break;
                }
            }
        }

    }
}
