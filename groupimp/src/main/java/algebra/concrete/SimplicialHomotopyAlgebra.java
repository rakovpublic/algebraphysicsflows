package algebra.concrete;

import mathematics.topology.SimplicialHomotopy;

/** Explicit integral chain and cochain homotopies through the original operation interfaces. */
public final class SimplicialHomotopyAlgebra extends ConcreteAlgebra<SimplicialHomotopy> {
    public SimplicialHomotopyAlgebra(FiniteSimplicialMapAlgebra maps,RelativeSimplicialMapAlgebra pairMaps,RelativeSimplicialAlgebra pairs,
                                    SimplicialChainAlgebra chains,SimplicialCochainAlgebra cochains,RelativeSimplicialChainAlgebra relativeChains,
                                    RelativeSimplicialCochainAlgebra relativeCochains,IntegerMatrixFamily matrices,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("SimplicialHomotopy",SimplicialHomotopy.class,"Oriented prism witness for contiguous maps of labelled pairs",h -> true),pairs.unit());
        binary("between",pairMaps.algebra(),pairMaps.algebra(),algebra(),true,SimplicialHomotopy::new);
        binary("between-absolute",maps.algebra(),maps.algebra(),algebra(),true,SimplicialHomotopy::absolute);
        unary("from",algebra(),pairMaps.algebra(),false,SimplicialHomotopy::from);
        unary("to",algebra(),pairMaps.algebra(),false,SimplicialHomotopy::to);
        unary("source",algebra(),pairs.algebra(),false,SimplicialHomotopy::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialHomotopy::target);
        unary("reverse",algebra(),algebra(),false,SimplicialHomotopy::reverse);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialHomotopy::equals);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialHomotopy::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialHomotopy::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,SimplicialHomotopy::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,SimplicialHomotopy::cochainMatrices);
        binary("on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialHomotopy::onChain);
        binary("on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialHomotopy::onCochain);
        binary("on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialHomotopy::onAbsoluteChain);
        binary("on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialHomotopy::onAbsoluteCochain);
        law("The integral prism satisfies boundary P + P boundary = to# - from# on full quotient chain groups; repeated vertices vanish with orientation signs retained.");
        law("The dual Q^p = transpose(P_(p-1)) satisfies coboundary Q + Q coboundary = to* - from*. Typed cochains require positive input degree.");
        law("Contiguity in both ambient and subcomplex targets is required. A witness supplies explicit fillings for differences of pushed cycles and pulled cocycles; it is not a homotopy search.");
    }
}
