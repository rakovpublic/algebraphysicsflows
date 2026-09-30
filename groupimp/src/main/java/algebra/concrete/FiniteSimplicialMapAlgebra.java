package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.Pair;
import mathematics.topology.FiniteSimplicialComplex;
import mathematics.topology.FiniteSimplicialMap;

/** Native simplicial maps, oriented chain matrices and integral homology functoriality. */
public final class FiniteSimplicialMapAlgebra extends ConcreteAlgebra<FiniteSimplicialMap> {
    public final Algebra<Pair<FiniteSimplicialComplex,FiniteSimplicialComplex>> boundaries;
    public FiniteSimplicialMapAlgebra(FiniteSimplicialAlgebra complexes,FiniteIntegerFunctionAlgebra functions,
                                     IntegerSetAlgebra sets,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
                                     IntegerMatrixFamily matrices,IntegralHomologyAlgebra homology,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("SimplicialMap",FiniteSimplicialMap.class,"Simplex-preserving maps between labelled finite complexes",f -> true),integers.unit());
        boundaries=boundaryCarrier(complexes.algebra());
        closed("compose",true,FiniteSimplicialMap::compose);
        unary("inverse",algebra(),algebra(),true,FiniteSimplicialMap::inverse);
        unary("source",algebra(),complexes.algebra(),false,FiniteSimplicialMap::source);
        unary("target",algebra(),complexes.algebra(),false,FiniteSimplicialMap::target);
        unary("vertex-map",algebra(),functions.algebra(),false,f -> functions.member(
                FiniteSimplicialMap.vertexSet(f.source()),FiniteSimplicialMap.vertexSet(f.target()),f.vertexMap()));
        binary("map-vertex",algebra(),integers.algebra(),integers.algebra(),true,FiniteSimplicialMap::mapVertex);
        unaryFlat("vertex-images",algebra(),integers.algebra(),false,FiniteSimplicialMap::vertexImages);
        unary("image",algebra(),complexes.algebra(),false,FiniteSimplicialMap::image);
        unary("is-injective",algebra(),truth.algebra(),false,FiniteSimplicialMap::isInjective);
        unary("is-surjective",algebra(),truth.algebra(),false,FiniteSimplicialMap::isSurjective);
        unary("is-isomorphism",algebra(),truth.algebra(),false,FiniteSimplicialMap::isIsomorphism);
        unary("is-vertex-surjective",algebra(),truth.algebra(),false,FiniteSimplicialMap::isVertexSurjective);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteSimplicialMap::equals);
        unary("identity-on",complexes.algebra(),algebra(),false,FiniteSimplicialMap::identity);
        binary("inclusion",complexes.algebra(),complexes.algebra(),algebra(),true,FiniteSimplicialMap::inclusion);
        binary("from-function",functions.algebra(),boundaries,algebra(),true,(f,b) -> FiniteSimplicialMap.fromFunction(f,b.first,b.second));
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,FiniteSimplicialMap::chainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,FiniteSimplicialMap::chainMatrices);
        binary("homology-map",algebra(),naturals.algebra(),maps.algebra(),false,FiniteSimplicialMap::homologyMap);
        unaryFlat("homology-maps",algebra(),maps.algebra(),false,FiniteSimplicialMap::homologyMaps);
        binary("source-homology",algebra(),naturals.algebra(),homology.algebra(),false,FiniteSimplicialMap::sourceHomology);
        binary("target-homology",algebra(),naturals.algebra(),homology.algebra(),false,FiniteSimplicialMap::targetHomology);
        binary("contiguous",algebra(),algebra(),truth.algebra(),true,FiniteSimplicialMap::contiguous);
        flat("vertex-fiber",algebra(),integers.algebra(),integers.algebra(),true,FiniteSimplicialMap::vertexFiber);
        binary("restrict",algebra(),complexes.algebra(),algebra(),true,FiniteSimplicialMap::restrict);
        unary("corestrict-image",algebra(),algebra(),false,FiniteSimplicialMap::corestrictImage);
        binary("map-simplex",algebra(),sets.algebra(),sets.algebra(),true,FiniteSimplicialMap::mapSimplex);
        flat("simplex-basis",complexes.algebra(),naturals.algebra(),sets.algebra(),false,FiniteSimplicialMap::simplexBasis);
        binary("constant-at",algebra(),integers.algebra(),algebra(),true,FiniteSimplicialMap::constantAt);
        unary("empty-to",complexes.algebra(),algebra(),false,FiniteSimplicialMap::emptyTo);
        law("Every nonempty source simplex maps to a target simplex; repeated vertex images are allowed.");
        law("Chain matrices use increasing vertex orientations; collapsed simplices map to zero, and all matrices commute with the integral boundaries.");
        law("Composition applies the right operand first. Chain matrices and induced integral homology maps preserve identities and composition.");
        law("An isomorphism must preserve simplices in both directions; vertex bijectivity alone is insufficient. Contiguous maps induce equal homology maps.");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<FiniteSimplicialComplex,FiniteSimplicialComplex>> boundaryCarrier(Algebra<FiniteSimplicialComplex> complexes) {
        Class<Pair<FiniteSimplicialComplex,FiniteSimplicialComplex>> type=(Class<Pair<FiniteSimplicialComplex,FiniteSimplicialComplex>>)(Class<?>)Pair.class;
        return carrier("FiniteComplex.pair",type,"Source and target complexes for a vertex map",p ->
                complexes.getParamClass().isInstance(p.first) && complexes.getParamClass().isInstance(p.second)
                && complexes.validate(p.first) && complexes.validate(p.second));
    }
}
