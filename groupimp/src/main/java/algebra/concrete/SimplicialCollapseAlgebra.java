package algebra.concrete;

import mathematics.topology.SimplicialCollapse;

/** Elementary free-face collapses through the original scalar, unary, flat and second-result interfaces. */
public final class SimplicialCollapseAlgebra extends ConcreteAlgebra<SimplicialCollapse> {
    public SimplicialCollapseAlgebra(RelativeSimplicialAlgebra pairs,FiniteSimplicialAlgebra complexes,RelativeSimplicialMapAlgebra maps,
                                    IntegerSetAlgebra sets,NaturalSemiring naturals,BooleanAlgebra truth,IntegerMatrixFamily matrices,
                                    RelativeSimplicialChainAlgebra relativeChains,RelativeSimplicialCochainAlgebra relativeCochains,
                                    SimplicialChainAlgebra chains,SimplicialCochainAlgebra cochains,AbelianGroupHomomorphismAlgebra homomorphisms) {
        super(carrier("SimplicialCollapse",SimplicialCollapse.class,"A compatible codimension-one free-face collapse with integral chain witnesses",c -> true),pairs.unit());
        unaryFlat("free-faces",pairs.algebra(),sets.algebra(),false,SimplicialCollapse::freeFaces);
        unary("has-free-face",pairs.algebra(),truth.algebra(),false,SimplicialCollapse::hasFreeFace);
        binary("from-face",pairs.algebra(),sets.algebra(),algebra(),true,SimplicialCollapse::new);
        binary("from-absolute-face",complexes.algebra(),sets.algebra(),algebra(),true,SimplicialCollapse::absolute);
        unary("source",algebra(),pairs.algebra(),false,SimplicialCollapse::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialCollapse::target);
        unary("free-face",algebra(),sets.algebra(),false,SimplicialCollapse::freeFace);
        unary("coface",algebra(),sets.algebra(),false,SimplicialCollapse::coface);
        unary("inclusion",algebra(),maps.algebra(),false,SimplicialCollapse::inclusion);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialCollapse::equals);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapse::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapse::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,SimplicialCollapse::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,SimplicialCollapse::cochainMatrices);
        binary("chain-homotopy-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapse::chainHomotopyMatrix);
        binary("cochain-homotopy-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCollapse::cochainHomotopyMatrix);
        unaryFlat("chain-homotopy-matrices",algebra(),matrices.algebra(),false,SimplicialCollapse::chainHomotopyMatrices);
        unaryFlat("cochain-homotopy-matrices",algebra(),matrices.algebra(),false,SimplicialCollapse::cochainHomotopyMatrices);
        binary("on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialCollapse::onChain);
        binary("on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialCollapse::onCochain);
        binary("on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialCollapse::onAbsoluteChain);
        binary("on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialCollapse::onAbsoluteCochain);
        binary("homotopy-on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialCollapse::homotopyOnChain);
        binary("homotopy-on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialCollapse::homotopyOnCochain);
        binary("homotopy-on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialCollapse::homotopyOnAbsoluteChain);
        binary("homotopy-on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialCollapse::homotopyOnAbsoluteCochain);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapse::homologyMap);
        binary("inverse-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapse::inverseHomologyMap);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapse::homologyMaps);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapse::cohomologyMap);
        binary("inverse-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapse::inverseCohomologyMap);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialCollapse::cohomologyMaps);
        law("The nonempty free face has a unique maximal codimension-one coface; both removed simplices lie inside the subcomplex or both outside it.");
        law("The integral retraction R and inclusion i are chain maps with R i = identity and boundary H + H boundary = identity - i R. Transposes give the dual cochain identities.");
        law("Retraction and inclusion induce inverse integral homology and cohomology maps. A chain retraction need not come from a simplicial vertex map.");
    }
}
