package cluster;

/**
 * Created by Rakovskyi Dmytro on 23.10.2017.
 */
public interface INodeStarter {

    /**
     *
     * contains commands which starts jar on node with node param
     * should be implemented by hoster
     * */
    void startNodes();
}
