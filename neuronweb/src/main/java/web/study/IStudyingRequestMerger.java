package web.study;

import web.neuron.INeuron;

import java.util.List;

/**
 * Created by Rakovskyi Dmytro on 27.10.2017.
 */
public interface IStudyingRequestMerger<N extends INeuron> {
    IStudyingRequest<N> mergeRequests(List<IStudyingRequest<N>> requests);

}
