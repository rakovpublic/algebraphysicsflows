package synchronizer;

import web.signals.ISignal;

/**
 * Created by Rakovskyi Dmytro on 29.10.2017.
 */
public interface INeuronResultHolder {
    void putSignal(String layer, String neuronId, ISignal signal);
    <K>ISignal<K> getSignal(String layer,String neuronId);
}
