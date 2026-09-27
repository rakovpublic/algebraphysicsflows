package algebra.concrete;

import mathematics.topology.SimplicialSubdivision;

/** Barycentric subdivision contexts and their original scalar, second-result and flat operations. */
public final class SimplicialSubdivisionAlgebra extends ConcreteAlgebra<SimplicialSubdivision> {
    public SimplicialSubdivisionAlgebra(RelativeSimplicialAlgebra pairs,FiniteSimplicialAlgebra complexes,RelativeSimplicialMapAlgebra maps,
                                       SimplicialHomotopyAlgebra homotopies,AbelianGroupHomomorphismAlgebra homomorphisms,
                                       IntegerSetAlgebra sets,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth) {
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
        law("Subdivision vertices encode nonempty original faces, ordered by size then lexicographic labels. Simplices are strict inclusion chains, with the subcomplex using the same ambient labels.");
        law("Subdivision preserves identities and composition of pair maps, including collapsed faces. The last-vertex map induces integral homology and cohomology isomorphisms.");
        law("Last-vertex maps commute with arbitrary vertex maps up to the exposed contiguous-pair homotopy; equality of the two raw maps is not required.");
    }
}
