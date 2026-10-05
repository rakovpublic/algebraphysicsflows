package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.topology.SimplicialChainEquivalence;

/** Retained integral equivalence witnesses registered through existing scalar and flat interfaces. */
public final class SimplicialChainEquivalenceAlgebra extends ConcreteAlgebra<SimplicialChainEquivalence> {
    public final Algebra<SimplicialChainEquivalence.Data> inputs;
    public SimplicialChainEquivalenceAlgebra(SimplicialChainMapAlgebra maps,SimplicialChainHomotopyAlgebra homotopies,
                                             SimplicialChainMapClassAlgebra classes,RelativeSimplicialAlgebra pairs,
                                             RelativeSimplicialChainAlgebra chains,RelativeSimplicialCochainAlgebra cochains,
                                             AbelianGroupHomomorphismAlgebra homomorphisms,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("ChainEquivalence",SimplicialChainEquivalence.class,"Opposite integral chain maps and both retained inverse homotopies",e -> true),pairs.unit());
        inputs=carrier("ChainEquivalence.data",SimplicialChainEquivalence.Data.class,"Individually valid maps and homotopies awaiting mutual compatibility checks",d -> true);
        unary("from-data",inputs,algebra(),true,SimplicialChainEquivalence::new);
        unary("from-map",maps.algebra(),algebra(),true,SimplicialChainEquivalence::fromMap);
        unary("identity-on",pairs.algebra(),algebra(),false,SimplicialChainEquivalence::identity);
        unary("source",algebra(),pairs.algebra(),false,SimplicialChainEquivalence::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialChainEquivalence::target);
        unary("forward",algebra(),maps.algebra(),false,SimplicialChainEquivalence::forward);
        unary("backward",algebra(),maps.algebra(),false,SimplicialChainEquivalence::backward);
        unary("source-homotopy",algebra(),homotopies.algebra(),false,SimplicialChainEquivalence::sourceHomotopy);
        unary("target-homotopy",algebra(),homotopies.algebra(),false,SimplicialChainEquivalence::targetHomotopy);
        unary("data",algebra(),inputs,false,SimplicialChainEquivalence::data);
        unary("inverse",algebra(),algebra(),false,SimplicialChainEquivalence::inverse);
        closed("compose",true,SimplicialChainEquivalence::compose);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialChainEquivalence::equals);
        unaryFlat("homotopies",algebra(),homotopies.algebra(),false,SimplicialChainEquivalence::homotopies);
        unaryFlat("maps",algebra(),maps.algebra(),false,SimplicialChainEquivalence::maps);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainEquivalence::homologyMap);
        binary("inverse-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainEquivalence::inverseHomologyMap);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainEquivalence::homologyMaps);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainEquivalence::cohomologyMap);
        binary("inverse-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainEquivalence::inverseCohomologyMap);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainEquivalence::cohomologyMaps);
        binary("on-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialChainEquivalence::onChain);
        binary("inverse-on-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialChainEquivalence::inverseOnChain);
        binary("on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialChainEquivalence::onCochain);
        binary("inverse-on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialChainEquivalence::inverseOnCochain);
        unary("forward-class",algebra(),classes.algebra(),false,SimplicialChainEquivalence::forwardClass);
        unary("backward-class",algebra(),classes.algebra(),false,SimplicialChainEquivalence::backwardClass);
        law("Both full labelled endpoints, both chain maps and both homotopies are retained. Equality includes witness matrices, even nonzero loops.");
        law("For before=(F,G,H,K) and after=(A,B,J,L), composition has maps AF, GB and witnesses GJF+H, AKB+L. Composition is associative and unital on the retained data; inversion swaps maps and witnesses.");
        law("Forward and backward classes and induced integral maps are mutual inverses. Composing an equivalence with its inverse need not equal the identity witness data or act identically on raw chains.");
    }
}
