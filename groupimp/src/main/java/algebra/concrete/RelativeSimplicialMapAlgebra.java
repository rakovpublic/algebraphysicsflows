package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.Pair;
import mathematics.topology.RelativeSimplicialComplex;
import mathematics.topology.RelativeSimplicialMap;

/** Native maps of pairs, relative homology functoriality and natural long exact sequences. */
public final class RelativeSimplicialMapAlgebra extends ConcreteAlgebra<RelativeSimplicialMap> {
    public final Algebra<Pair<RelativeSimplicialComplex,RelativeSimplicialComplex>> boundaries;
    public RelativeSimplicialMapAlgebra(RelativeSimplicialAlgebra pairs,FiniteSimplicialMapAlgebra simplicialMaps,
                                        IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
                                        IntegerMatrixFamily matrices,IntegralHomologyAlgebra homology,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("RelativeMap",RelativeSimplicialMap.class,"Simplicial maps of labelled pairs with induced integral maps",f -> true),integers.unit());
        boundaries=boundaryCarrier(pairs.algebra());
        binary("from-map",simplicialMaps.algebra(),boundaries,algebra(),true,(f,b) -> new RelativeSimplicialMap(b.first,b.second,f));
        closed("compose",true,RelativeSimplicialMap::compose);
        unary("inverse",algebra(),algebra(),true,RelativeSimplicialMap::inverse);
        unary("source",algebra(),pairs.algebra(),false,RelativeSimplicialMap::source);
        unary("target",algebra(),pairs.algebra(),false,RelativeSimplicialMap::target);
        unary("ambient-map",algebra(),simplicialMaps.algebra(),false,RelativeSimplicialMap::ambientMap);
        unary("subcomplex-map",algebra(),simplicialMaps.algebra(),false,RelativeSimplicialMap::subcomplexMap);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RelativeSimplicialMap::equals);
        unary("is-isomorphism",algebra(),truth.algebra(),false,RelativeSimplicialMap::isIsomorphism);
        unary("identity-on",pairs.algebra(),algebra(),false,RelativeSimplicialMap::identity);
        binary("inclusion",pairs.algebra(),pairs.algebra(),algebra(),true,RelativeSimplicialMap::inclusion);
        unary("absolute",simplicialMaps.algebra(),algebra(),false,RelativeSimplicialMap::absolute);
        unary("diagonal",simplicialMaps.algebra(),algebra(),false,RelativeSimplicialMap::diagonal);
        binary("chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialMap::chainMatrix);
        unaryFlat("chain-matrices",algebra(),matrices.algebra(),false,RelativeSimplicialMap::chainMatrices);
        binary("homology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialMap::homologyMap);
        unaryFlat("homology-maps",algebra(),maps.algebra(),false,RelativeSimplicialMap::homologyMaps);
        binary("source-homology",algebra(),naturals.algebra(),homology.algebra(),false,RelativeSimplicialMap::sourceHomology);
        binary("target-homology",algebra(),naturals.algebra(),homology.algebra(),false,RelativeSimplicialMap::targetHomology);
        binary("ambient-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialMap::ambientHomologyMap);
        binary("subcomplex-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialMap::subcomplexHomologyMap);
        flat("long-exact-maps",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialMap::longExactMaps);
        binary("contiguous",algebra(),algebra(),truth.algebra(),true,RelativeSimplicialMap::contiguous);
        unary("image",algebra(),pairs.algebra(),false,RelativeSimplicialMap::image);
        unary("corestrict-image",algebra(),algebra(),false,RelativeSimplicialMap::corestrictImage);
        binary("restrict",algebra(),pairs.algebra(),algebra(),true,RelativeSimplicialMap::restrict);
        law("A pair map preserves both full ambient complexes and the subcomplexes. Composition requires equality of the whole middle pair.");
        law("Relative chain matrices discard collapsed simplices and images in the target subcomplex; they preserve boundaries, identity and composition.");
        law("Relative homology maps commute with inclusion, quotient and connecting homomorphisms in the long exact sequence of a pair.");
        law("Pair contiguity requires simplex unions inside both the target ambient complex and its subcomplex, and implies equal relative homology maps.");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<RelativeSimplicialComplex,RelativeSimplicialComplex>> boundaryCarrier(Algebra<RelativeSimplicialComplex> pairs) {
        Class<Pair<RelativeSimplicialComplex,RelativeSimplicialComplex>> type=(Class<Pair<RelativeSimplicialComplex,RelativeSimplicialComplex>>)(Class<?>)Pair.class;
        return carrier("RelativeComplex.pair",type,"Source and target pairs for a relative simplicial map",p ->
                pairs.getParamClass().isInstance(p.first) && pairs.getParamClass().isInstance(p.second) && pairs.validate(p.first) && pairs.validate(p.second));
    }
}
