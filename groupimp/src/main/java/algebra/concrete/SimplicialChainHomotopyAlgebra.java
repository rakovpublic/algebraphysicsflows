package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.topology.SimplicialChainHomotopy;

/** Integral homotopy witnesses using the original scalar and flat operation interfaces. */
public final class SimplicialChainHomotopyAlgebra extends ConcreteAlgebra<SimplicialChainHomotopy> {
    public final Algebra<SimplicialChainHomotopy.Data> inputs;
    public SimplicialChainHomotopyAlgebra(SimplicialChainMapAlgebra maps,SimplicialHomotopyAlgebra prisms,SimplicialHomotopyPathAlgebra paths,
                                         SimplicialCollapseAlgebra collapses,SimplicialCollapseSequenceAlgebra sequences,SimplicialSubdivisionAlgebra subdivisions,
                                         RelativeSimplicialAlgebra pairs,IntegerMatrixFamily matrices,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
                                         RelativeSimplicialChainAlgebra relativeChains,RelativeSimplicialCochainAlgebra relativeCochains,
                                         SimplicialChainAlgebra chains,SimplicialCochainAlgebra cochains,AbelianGroupHomomorphismAlgebra homomorphisms) {
        super(carrier("ChainHomotopy",SimplicialChainHomotopy.class,"Integral witnesses dH + Hd = to - from between full chain maps",h -> true),pairs.unit());
        inputs=carrier("ChainHomotopy.data",SimplicialChainHomotopy.Data.class,"Two chain maps and supplied degree-raising matrices, awaiting homotopy validation",d -> true);
        unary("from-data",inputs,algebra(),true,SimplicialChainHomotopy::new);
        unary("stationary",maps.algebra(),algebra(),false,SimplicialChainHomotopy::stationary);
        unary("from-prism",prisms.algebra(),algebra(),false,SimplicialChainHomotopy::fromPrism);
        unary("from-path",paths.algebra(),algebra(),false,SimplicialChainHomotopy::fromPath);
        unary("from-collapse",collapses.algebra(),algebra(),false,SimplicialChainHomotopy::fromCollapse);
        unary("from-collapse-sequence",sequences.algebra(),algebra(),false,SimplicialChainHomotopy::fromCollapseSequence);
        unary("from-subdivision",subdivisions.algebra(),algebra(),false,SimplicialChainHomotopy::fromSubdivision);
        unary("from",algebra(),maps.algebra(),false,SimplicialChainHomotopy::from);
        unary("to",algebra(),maps.algebra(),false,SimplicialChainHomotopy::to);
        unary("source",algebra(),pairs.algebra(),false,SimplicialChainHomotopy::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialChainHomotopy::target);
        unary("data",algebra(),inputs,false,SimplicialChainHomotopy::data);
        unary("reverse",algebra(),algebra(),false,SimplicialChainHomotopy::reverse);
        closed("then",true,SimplicialChainHomotopy::then);
        closed("add",true,SimplicialChainHomotopy::add);
        binary("scale",algebra(),integers.algebra(),algebra(),false,SimplicialChainHomotopy::scale);
        binary("precompose",algebra(),maps.algebra(),algebra(),true,SimplicialChainHomotopy::precompose);
        binary("postcompose",algebra(),maps.algebra(),algebra(),true,SimplicialChainHomotopy::postcompose);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialChainHomotopy::equals);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialChainHomotopy::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialChainHomotopy::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,SimplicialChainHomotopy::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,SimplicialChainHomotopy::cochainMatrices);
        binary("on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialChainHomotopy::onChain);
        binary("on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialChainHomotopy::onCochain);
        binary("on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialChainHomotopy::onAbsoluteChain);
        binary("on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialChainHomotopy::onAbsoluteCochain);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainHomotopy::homologyMaps);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainHomotopy::cohomologyMaps);
        law("The retained integral matrices satisfy dH + Hd = to - from. Their shifted transposes satisfy the dual cochain identity.");
        law("Chronological concatenation adds witnesses; reversal negates the actual matrices. Endpoint equality alone does not identify homotopies.");
        law("Precomposition uses H_k B_k and postcomposition A_(k+1) H_k; induced integral homology and cohomology maps of both endpoints agree.");
    }
}
