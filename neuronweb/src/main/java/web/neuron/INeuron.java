package web.neuron;

import web.signals.ISignal;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 27.10.2017.
 */
public interface INeuron<A extends IAxon> {
    void addSignals(List<ISignal> signals);
    void addSignal(ISignal signal);
    void processSignals();
    void reconfigure();
    String getId();
    A getAxon();

}
