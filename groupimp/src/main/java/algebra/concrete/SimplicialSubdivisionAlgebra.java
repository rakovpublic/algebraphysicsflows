package algebra.concrete;

import mathematics.topology.SimplicialSubdivision;

/** Barycentric subdivision contexts and their original scalar, second-result and flat operations. */
public final class SimplicialSubdivisionAlgebra extends ConcreteAlgebra<SimplicialSubdivision> {
    public SimplicialSubdivisionAlgebra(RelativeSimplicialAlgebra pairs,FiniteSimplicialAlgebra complexes,RelativeSimplicialMapAlgebra maps,
                                       SimplicialHomotopyAlgebra homotopies,AbelianGroupHomomorphismAlgebra homomorphisms,
                                       IntegerSetAlgebra sets,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
                                       IntegerMatrixFamily matrices,RelativeSimplicialChainAlgebra relativeChains,RelativeSimplicialCochainAlgebra relativeCochains,
                                       SimplicialChainAlgebra chains,SimplicialCochainAlgebra cochains) {
        super(carrier("SimplicialSubdivision",SimplicialSubdivision.class,"Barycentric subdivision of a pair with a retained ambient face dictionary",s -> true),pairs.unit());
        unary("from-pair",pairs.algebra(),algebra(),false,SimplicialSubdivision::new);
        unary("from-complex",complexes.algebra(),algebra(),false,SimplicialSubdivision::absolute);
        unary("original",algebra(),pairs.algebra(),false,SimplicialSubdivision::original);
        unary("subdivided",algebra(),pairs.algebra(),false,SimplicialSubdivision::subdivided);
        unary("vertex-count",algebra(),naturals.algebra(),false,SimplicialSubdivision::vertexCount);
        binary("vertex-face",algebra(),integers.algebra(),sets.algebra(),true,SimplicialSubdivision::vertexFace);
        unaryFlat("vertex-faces",algebra(),sets.algebra(),false,SimplicialSubdivision::vertexFaces);
        binary("face-vertex",algebra(),sets.algebra(),integers.algebra(),true,SimplicialSubdivision::faceVertex);
        unary("last-vertex-map",algebra(),maps.algebra(),false,SimplicialSubdivision::lastVertexMap);
        binary("map",algebra(),maps.algebra(),maps.algebra(),true,SimplicialSubdivision::map);
        binary("naturality-homotopy",algebra(),maps.algebra(),homotopies.algebra(),true,SimplicialSubdivision::naturalityHomotopy);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialSubdivision::homologyMap);
        binary("inverse-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialSubdivision::inverseHomologyMap);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialSubdivision::homologyMaps);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialSubdivision::cohomologyMap);
        binary("inverse-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialSubdivision::inverseCohomologyMap);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialSubdivision::cohomologyMaps);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialSubdivision::equals);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialSubdivision::chainMatrix);
        binary("cochain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialSubdivision::cochainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,SimplicialSubdivision::chainMatrices);
        unaryFlat("cochain-matrices",algebra(),matrices.algebra(),false,SimplicialSubdivision::cochainMatrices);
        binary("on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialSubdivision::onChain);
        binary("on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialSubdivision::onCochain);
        binary("on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialSubdivision::onAbsoluteChain);
        binary("on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialSubdivision::onAbsoluteCochain);
        binary("chain-homotopy-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialSubdivision::chainHomotopyMatrix);
        binary("cochain-homotopy-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialSubdivision::cochainHomotopyMatrix);
        unaryFlat("chain-homotopy-matrices",algebra(),matrices.algebra(),false,SimplicialSubdivision::chainHomotopyMatrices);
        unaryFlat("cochain-homotopy-matrices",algebra(),matrices.algebra(),false,SimplicialSubdivision::cochainHomotopyMatrices);
        binary("homotopy-on-chain",algebra(),relativeChains.algebra(),relativeChains.algebra(),true,SimplicialSubdivision::homotopyOnChain);
        binary("homotopy-on-cochain",algebra(),relativeCochains.algebra(),relativeCochains.algebra(),true,SimplicialSubdivision::homotopyOnCochain);
        binary("homotopy-on-absolute-chain",algebra(),chains.algebra(),chains.algebra(),true,SimplicialSubdivision::homotopyOnAbsoluteChain);
        binary("homotopy-on-absolute-cochain",algebra(),cochains.algebra(),cochains.algebra(),true,SimplicialSubdivision::homotopyOnAbsoluteCochain);
        law("Subdivision vertices encode nonempty original faces, ordered by size then lexicographic labels. Simplices are strict inclusion chains, with the subcomplex using the same ambient labels.");
        law("Subdivision preserves identities and composition of pair maps, including collapsed faces. The last-vertex map induces integral homology and cohomology isomorphisms.");
        law("Last-vertex maps commute with arbitrary vertex maps up to the exposed contiguous-pair homotopy; equality of the two raw maps is not required.");
        law("The signed subdivision chain map S commutes with boundaries and satisfies last# S = identity. On subdivided chains, boundary P + P boundary = identity - S last#; transposes give dual cochain identities.");
    }
}
