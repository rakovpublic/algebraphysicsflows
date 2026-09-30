package algebra.concrete;

import mathematics.topology.SimplicialHomotopyPath;

/** Finite supplied contiguity paths and accumulated integral prism actions. */
public final class SimplicialHomotopyPathAlgebra extends ConcreteAlgebra<SimplicialHomotopyPath> {
    public SimplicialHomotopyPathAlgebra(SimplicialHomotopyAlgebra homotopies,RelativeSimplicialMapAlgebra maps,RelativeSimplicialAlgebra pairs,
                                        SimplicialChainAlgebra chains,SimplicialCochainAlgebra cochains,RelativeSimplicialChainAlgebra relativeChains,
                                        RelativeSimplicialCochainAlgebra relativeCochains,IntegerMatrixFamily matrices,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("HomotopyPath",SimplicialHomotopyPath.class,"Finite supplied paths of contiguous maps with accumulated integral prisms",p -> true),pairs.unit());
        unary("from-homotopy",homotopies.algebra(),algebra(),false,SimplicialHomotopyPath::fromHomotopy);
        unary("stationary-on",maps.algebra(),algebra(),false,SimplicialHomotopyPath::stationary);
        binary("append",algebra(),maps.algebra(),algebra(),true,SimplicialHomotopyPath::append);
        binary("then",algebra(),algebra(),algebra(),true,SimplicialHomotopyPath::then);
        unary("reverse",algebra(),algebra(),false,SimplicialHomotopyPath::reverse);
        unary("from",algebra(),maps.algebra(),false,SimplicialHomotopyPath::from);
        unary("to",algebra(),maps.algebra(),false,SimplicialHomotopyPath::to);
        unary("source",algebra(),pairs.algebra(),false,SimplicialHomotopyPath::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialHomotopyPath::target);
        unary("step-count",algebra(),naturals.algebra(),false,SimplicialHomotopyPath::stepCount);
        unaryFlat("stages",algebra(),maps.algebra(),false,SimplicialHomotopyPath::stages);
        unaryFlat("steps",algebra(),homotopies.algebra(),false,SimplicialHomotopyPath::steps);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialHomotopyPath::equals);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialHomotopyPath::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialHomotopyPath::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,SimplicialHomotopyPath::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,SimplicialHomotopyPath::cochainMatrices);
        binary("on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialHomotopyPath::onChain);
        binary("on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialHomotopyPath::onCochain);
        binary("on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialHomotopyPath::onAbsoluteChain);
        binary("on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialHomotopyPath::onAbsoluteCochain);
        binary("precompose",algebra(),maps.algebra(),algebra(),true,SimplicialHomotopyPath::precompose);
        binary("postcompose",algebra(),maps.algebra(),algebra(),true,SimplicialHomotopyPath::postcompose);
        law("The sum D of consecutive prisms satisfies boundary D + D boundary = to# - from#, even when the endpoint maps are not directly contiguous. Transposes give the dual identity.");
        law("Concatenation is chronological and requires equal full joining maps. Stationary paths have zero steps; equality retains every stage, so a loop can have a nonzero prism.");
        law("Precomposition, postcomposition and reversal recompute the prisms of the resulting stages. In particular precomposition and reversal need not agree with multiplying or negating the previous prism matrix.");
    }
}
