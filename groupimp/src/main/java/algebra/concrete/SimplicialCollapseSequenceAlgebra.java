package algebra.concrete;

import mathematics.topology.SimplicialCollapseSequence;
import mathematics.topology.SimplicialCollapseSearch;

/** Checked collapse sequences and deterministic reduction in the original algebra/flow interfaces. */
public final class SimplicialCollapseSequenceAlgebra extends ConcreteAlgebra<SimplicialCollapseSequence> {
    public SimplicialCollapseSequenceAlgebra(SimplicialCollapseAlgebra collapses,RelativeSimplicialAlgebra pairs,FiniteSimplicialAlgebra complexes,RelativeSimplicialMapAlgebra maps,
                                            NaturalSemiring naturals,BooleanAlgebra truth,IntegerMatrixFamily matrices,
                                            RelativeSimplicialChainAlgebra relativeChains,RelativeSimplicialCochainAlgebra relativeCochains,
                                            SimplicialChainAlgebra chains,SimplicialCochainAlgebra cochains,AbelianGroupHomomorphismAlgebra homomorphisms) {
        super(carrier("CollapseSequence",SimplicialCollapseSequence.class,"An ordered elementary-collapse sequence with composite integral witnesses",c -> true),pairs.unit());
        unary("identity-on",pairs.algebra(),algebra(),false,SimplicialCollapseSequence::identity);
        unary("from-collapse",collapses.algebra(),algebra(),false,SimplicialCollapseSequence::fromCollapse);
        unary("reduce",pairs.algebra(),algebra(),false,SimplicialCollapseSequence::reduce);
        unary("reduce-absolute",complexes.algebra(),algebra(),false,SimplicialCollapseSequence::reduceAbsolute);
        binary("append",algebra(),collapses.algebra(),algebra(),true,SimplicialCollapseSequence::append);
        closed("then",true,SimplicialCollapseSequence::then);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialCollapseSequence::equals);
        unary("source",algebra(),pairs.algebra(),false,SimplicialCollapseSequence::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialCollapseSequence::target);
        unaryFlat("steps",algebra(),collapses.algebra(),false,SimplicialCollapseSequence::steps);
        unaryFlat("stages",algebra(),pairs.algebra(),false,SimplicialCollapseSequence::stages);
        unary("step-count",algebra(),naturals.algebra(),false,SimplicialCollapseSequence::stepCount);
        unary("is-terminal",algebra(),truth.algebra(),false,SimplicialCollapseSequence::isTerminal);
        unary("inclusion",algebra(),maps.algebra(),false,SimplicialCollapseSequence::inclusion);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapseSequence::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapseSequence::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,SimplicialCollapseSequence::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,SimplicialCollapseSequence::cochainMatrices);
        binary("chain-homotopy-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapseSequence::chainHomotopyMatrix);
        binary("cochain-homotopy-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapseSequence::cochainHomotopyMatrix);
        unaryFlat("chain-homotopy-matrices",algebra(),matrices.algebra(),false,SimplicialCollapseSequence::chainHomotopyMatrices);
        unaryFlat("cochain-homotopy-matrices",algebra(),matrices.algebra(),false,SimplicialCollapseSequence::cochainHomotopyMatrices);
        binary("on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialCollapseSequence::onChain);
        binary("on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialCollapseSequence::onCochain);
        binary("on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialCollapseSequence::onAbsoluteChain);
        binary("on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialCollapseSequence::onAbsoluteCochain);
        binary("homotopy-on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialCollapseSequence::homotopyOnChain);
        binary("homotopy-on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialCollapseSequence::homotopyOnCochain);
        binary("homotopy-on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialCollapseSequence::homotopyOnAbsoluteChain);
        binary("homotopy-on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialCollapseSequence::homotopyOnAbsoluteCochain);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapseSequence::homologyMap);
        binary("inverse-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapseSequence::inverseHomologyMap);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapseSequence::homologyMaps);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapseSequence::cohomologyMap);
        binary("inverse-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapseSequence::inverseCohomologyMap);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapseSequence::cohomologyMaps);
        binary("can-collapse-to",pairs.algebra(),pairs.algebra(),truth.algebra(),false,SimplicialCollapseSearch::canCollapseTo);
        binary("collapse-to",pairs.algebra(),pairs.algebra(),algebra(),true,SimplicialCollapseSearch::collapseTo);
        flat("collapses-to",pairs.algebra(),pairs.algebra(),algebra(),false,SimplicialCollapseSearch::collapsesTo);
        binary("absolute-can-collapse-to",complexes.algebra(),complexes.algebra(),truth.algebra(),false,SimplicialCollapseSearch::absoluteCanCollapseTo);
        binary("absolute-collapse-to",complexes.algebra(),complexes.algebra(),algebra(),true,SimplicialCollapseSearch::absoluteCollapseTo);
        flat("absolute-collapses-to",complexes.algebra(),complexes.algebra(),algebra(),false,SimplicialCollapseSearch::absoluteCollapsesTo);
        unary("is-collapsible",complexes.algebra(),truth.algebra(),false,SimplicialCollapseSearch::isCollapsible);
        unary("collapse-to-point",complexes.algebra(),algebra(),true,SimplicialCollapseSearch::collapseToPoint);
        unaryFlat("collapses-to-point",complexes.algebra(),algebra(),false,SimplicialCollapseSearch::collapsesToPoint);
        law("An empty sequence is a chronological concatenation unit; steps retain full matching labelled pairs and order.");
        law("Composite retraction R splits inclusion strictly; boundary H + H boundary = identity - i R, with dual cochain witnesses and inverse integral maps.");
        law("Greedy reduction chooses the first compatible free face by size and lexicographic labels. Its terminal pair is not a canonical homotopy invariant or a decision of collapsibility.");
        law("Exhaustive search returns false only after a necessary obstruction or complete search. Resource limits raise implementation failures; enumeration retains every ordered sequence or fails atomically.");
    }
}
