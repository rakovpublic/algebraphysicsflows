package web;

/**
 * Created by Rakovskyi Dmytro on 27.10.2017.
 */

import web.neuron.INeuron;
import web.signals.ISignal;

/**
 *
 * listener for neurons
 * */
public interface ILayer {
    void register(INeuron neuron);
    void storeResult(ISignal signal);
}
