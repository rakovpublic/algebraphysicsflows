package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.topology.IntegralConeEquivalence;

/** Retained integral equivalence witnesses registered through existing scalar and flat interfaces. */
public final class IntegralConeEquivalenceAlgebra extends ConcreteAlgebra<IntegralConeEquivalence> {
    public final Algebra<IntegralConeEquivalence.Data> inputs;
    public IntegralConeEquivalenceAlgebra(IntegralChainConeMapAlgebra maps,IntegralConeHomotopyAlgebra homotopies,
                                             IntegralChainMappingConeAlgebra pairs,
                                             IntegralConeChainAlgebra chains,IntegralConeCochainAlgebra cochains,
                                             AbelianGroupHomomorphismAlgebra homomorphisms,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("ConeEquivalence",IntegralConeEquivalence.class,"Opposite retained cone maps and both retained inverse homotopies",e -> true),pairs.unit());
        inputs=carrier("ConeEquivalence.data",IntegralConeEquivalence.Data.class,"Individually valid maps and homotopies awaiting mutual compatibility checks",d -> true);
        unary("from-data",inputs,algebra(),true,IntegralConeEquivalence::new);
        unary("identity-on",pairs.algebra(),algebra(),false,IntegralConeEquivalence::identity);
        unary("source",algebra(),pairs.algebra(),false,IntegralConeEquivalence::source);
        unary("target",algebra(),pairs.algebra(),false,IntegralConeEquivalence::target);
        unary("forward",algebra(),maps.algebra(),false,IntegralConeEquivalence::forward);
        unary("backward",algebra(),maps.algebra(),false,IntegralConeEquivalence::backward);
        unary("source-homotopy",algebra(),homotopies.algebra(),false,IntegralConeEquivalence::sourceHomotopy);
        unary("target-homotopy",algebra(),homotopies.algebra(),false,IntegralConeEquivalence::targetHomotopy);
        unary("data",algebra(),inputs,false,IntegralConeEquivalence::data);
        unary("inverse",algebra(),algebra(),false,IntegralConeEquivalence::inverse);
        closed("compose",true,IntegralConeEquivalence::compose);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralConeEquivalence::equals);
        unaryFlat("homotopies",algebra(),homotopies.algebra(),false,IntegralConeEquivalence::homotopies);
        unaryFlat("maps",algebra(),maps.algebra(),false,IntegralConeEquivalence::maps);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeEquivalence::homologyMap);
        binary("inverse-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeEquivalence::inverseHomologyMap);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeEquivalence::homologyMaps);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeEquivalence::cohomologyMap);
        binary("inverse-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeEquivalence::inverseCohomologyMap);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralConeEquivalence::cohomologyMaps);
        binary("on-chain",algebra(),chains.algebra(),chains.algebra(),true,IntegralConeEquivalence::onChain);
        binary("inverse-on-chain",algebra(),chains.algebra(),chains.algebra(),true,IntegralConeEquivalence::inverseOnChain);
        binary("on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,IntegralConeEquivalence::onCochain);
        binary("inverse-on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,IntegralConeEquivalence::inverseOnCochain);
        unary("from-square-isomorphism",maps.algebra(),algebra(),true,IntegralConeEquivalence::fromSquareIsomorphism);
        binary("source-homotopy-on-chain",algebra(),chains.algebra(),chains.algebra(),true,IntegralConeEquivalence::sourceHomotopyOnChain);
        binary("target-homotopy-on-chain",algebra(),chains.algebra(),chains.algebra(),true,IntegralConeEquivalence::targetHomotopyOnChain);
        binary("source-homotopy-on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,IntegralConeEquivalence::sourceHomotopyOnCochain);
        binary("target-homotopy-on-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,IntegralConeEquivalence::targetHomotopyOnCochain);
        unary("from-map",maps.algebra(),algebra(),true,IntegralConeEquivalence::fromMap);
        law("from-square-isomorphism requires both vertical chain maps to be degreewise unimodular. Total cone-matrix invertibility alone does not produce an inverse retained square.");
        law("Both full defining cone maps, both retained squares and both homotopies are retained. Equality includes witness matrices, even nonzero loops.");
        law("For before=(F,G,H,K) and after=(A,B,J,L), composition has maps AF, GB and witnesses GJF+H, AKB+L. Composition is associative and unital on the retained data; inversion swaps maps and witnesses.");
        law("Forward and backward induced integral maps are mutual inverses. Composing an equivalence with its inverse need not equal the identity witness data or act identically on raw chains.");
    }
}
