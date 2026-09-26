package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.Pair;
import mathematics.topology.RelativeSimplicialTriple;
import mathematics.topology.RelativeSimplicialTripleMap;

/** Native triple maps and the naturality maps of both integral exact sequences. */
public final class RelativeSimplicialTripleMapAlgebra extends ConcreteAlgebra<RelativeSimplicialTripleMap> {
    public final Algebra<Pair<RelativeSimplicialTriple,RelativeSimplicialTriple>> boundaries;
    public RelativeSimplicialTripleMapAlgebra(RelativeSimplicialTripleAlgebra triples,FiniteSimplicialMapAlgebra simplicialMaps,
                                            RelativeSimplicialMapAlgebra pairMaps,NaturalSemiring naturals,
                                            BooleanAlgebra truth,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("TripleMap",RelativeSimplicialTripleMap.class,"Simplicial maps preserving both nested subcomplexes",f -> true),triples.unit());
        boundaries=boundaryCarrier(triples.algebra());
        binary("from-map",simplicialMaps.algebra(),boundaries,algebra(),true,(f,b) -> new RelativeSimplicialTripleMap(b.first,b.second,f));
        closed("compose",true,RelativeSimplicialTripleMap::compose);
        unary("inverse",algebra(),algebra(),true,RelativeSimplicialTripleMap::inverse);
        unary("source",algebra(),triples.algebra(),false,RelativeSimplicialTripleMap::source);
        unary("target",algebra(),triples.algebra(),false,RelativeSimplicialTripleMap::target);
        unary("ambient-map",algebra(),simplicialMaps.algebra(),false,RelativeSimplicialTripleMap::ambientMap);
        unary("outer-map",algebra(),pairMaps.algebra(),false,RelativeSimplicialTripleMap::outerMap);
        unary("total-map",algebra(),pairMaps.algebra(),false,RelativeSimplicialTripleMap::totalMap);
        unary("inner-map",algebra(),pairMaps.algebra(),false,RelativeSimplicialTripleMap::innerMap);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RelativeSimplicialTripleMap::equals);
        unary("is-isomorphism",algebra(),truth.algebra(),false,RelativeSimplicialTripleMap::isIsomorphism);
        unary("identity-on",triples.algebra(),algebra(),false,RelativeSimplicialTripleMap::identity);
        binary("inclusion",triples.algebra(),triples.algebra(),algebra(),true,RelativeSimplicialTripleMap::inclusion);
        binary("contiguous",algebra(),algebra(),truth.algebra(),true,RelativeSimplicialTripleMap::contiguous);
        unary("image",algebra(),triples.algebra(),false,RelativeSimplicialTripleMap::image);
        unary("corestrict-image",algebra(),algebra(),false,RelativeSimplicialTripleMap::corestrictImage);
        binary("restrict",algebra(),triples.algebra(),algebra(),true,RelativeSimplicialTripleMap::restrict);
        binary("outer-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::outerHomologyMap);
        binary("total-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::totalHomologyMap);
        binary("inner-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::innerHomologyMap);
        flat("long-exact-maps",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::longExactMaps);
        binary("outer-cohomology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::outerCohomologyMap);
        binary("total-cohomology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::totalCohomologyMap);
        binary("inner-cohomology-map",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::innerCohomologyMap);
        flat("long-exact-cohomology-maps",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialTripleMap::longExactCohomologyMaps);
        law("Maps preserve all three full labelled components; composition applies the right map first and requires the full middle triple to match.");
        law("The three pair maps commute with inclusion and quotient. Covariant homology and contravariant cohomology maps commute with all three exact-sequence maps, including connecting maps.");
        law("Triple contiguity requires contiguity inside the target ambient, middle and base complexes, and implies equal induced maps.");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<RelativeSimplicialTriple,RelativeSimplicialTriple>> boundaryCarrier(Algebra<RelativeSimplicialTriple> triples) {
        Class<Pair<RelativeSimplicialTriple,RelativeSimplicialTriple>> type=(Class<Pair<RelativeSimplicialTriple,RelativeSimplicialTriple>>)(Class<?>)Pair.class;
        return carrier("RelativeTriple.pair",type,"Ordered source and target triples for a simplicial triple map",p ->
                triples.getParamClass().isInstance(p.first) && triples.getParamClass().isInstance(p.second) && triples.validate(p.first) && triples.validate(p.second));
    }
}
