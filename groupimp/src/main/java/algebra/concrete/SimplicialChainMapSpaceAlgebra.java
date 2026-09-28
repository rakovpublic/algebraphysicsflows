package algebra.concrete;

import mathematics.topology.SimplicialChainMapSpace;

/** Additive chain-map spaces and homotopy classes through existing native operation interfaces. */
public final class SimplicialChainMapSpaceAlgebra extends ConcreteAlgebra<SimplicialChainMapSpace> {
    public SimplicialChainMapSpaceAlgebra(RelativeSimplicialAlgebra pairs,SimplicialChainMapAlgebra maps,
                                         IntegralHomologyAlgebra homology,PresentedAbelianGroupAlgebra groups,
                                         AbelianGroupTypeAlgebra types,AbelianGroupElementAlgebra elements,BooleanAlgebra truth,
                                         AbelianGroupHomomorphismAlgebra homomorphisms,IntegerMatrixFamily matrices) {
        super(carrier("ChainMapSpace",SimplicialChainMapSpace.class,"Full labelled endpoints for integral chain maps modulo chain homotopy",s -> true),pairs.unit());
        binary("from-pairs",pairs.algebra(),pairs.algebra(),algebra(),false,SimplicialChainMapSpace::new);
        unary("source",algebra(),pairs.algebra(),false,SimplicialChainMapSpace::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialChainMapSpace::target);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialChainMapSpace::equals);
        unary("zero",algebra(),maps.algebra(),false,SimplicialChainMapSpace::zero);
        unary("homology",algebra(),homology.algebra(),false,SimplicialChainMapSpace::homology);
        unary("homotopy-group",algebra(),groups.algebra(),false,SimplicialChainMapSpace::homotopyGroup);
        unary("homotopy-type",algebra(),types.algebra(),false,SimplicialChainMapSpace::homotopyType);
        unaryFlat("map-generators",algebra(),maps.algebra(),false,SimplicialChainMapSpace::mapGenerators);
        binary("class-of",algebra(),maps.algebra(),elements.algebra(),true,SimplicialChainMapSpace::classOf);
        binary("representative",algebra(),elements.algebra(),maps.algebra(),true,SimplicialChainMapSpace::representative);
        unaryFlat("representatives",algebra(),maps.algebra(),false,SimplicialChainMapSpace::representatives);
        binary("precompose-space",algebra(),maps.algebra(),algebra(),true,SimplicialChainMapSpace::precomposeSpace);
        binary("postcompose-space",algebra(),maps.algebra(),algebra(),true,SimplicialChainMapSpace::postcomposeSpace);
        binary("precompose-map",algebra(),maps.algebra(),homomorphisms.algebra(),true,SimplicialChainMapSpace::precomposeMap);
        binary("postcompose-map",algebra(),maps.algebra(),homomorphisms.algebra(),true,SimplicialChainMapSpace::postcomposeMap);
        flat("precompose-matrices",algebra(),maps.algebra(),matrices.algebra(),true,SimplicialChainMapSpace::precomposeMatrices);
        flat("postcompose-matrices",algebra(),maps.algebra(),matrices.algebra(),true,SimplicialChainMapSpace::postcomposeMatrices);
        law("The degree-zero cycles of the integral Hom complex are exactly chain maps; boundaries are dH + Hd, so H_0(Hom) is their additive group modulo integral chain homotopy.");
        law("class-of is additive and detects integral homotopy classes, including torsion invisible to induced homology maps. A chosen representative is a section on sets, not generally an additive section.");
        law("Flat map-generators span the full integral chain-map lattice; representatives lift only the nonzero minimal Smith generators of its homotopy quotient.");
        law("Precomposition sends [F] to [F B], and postcomposition sends [F] to [A F]. They are additive, respect chain homotopy of the fixed map, preserve identities, and commute with each other; the first argument of Hom is contravariant.");
    }
}
