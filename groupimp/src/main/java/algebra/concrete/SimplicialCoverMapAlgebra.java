package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.Pair;
import mathematics.topology.SimplicialCover;
import mathematics.topology.SimplicialCoverMap;

/** Native maps of ordered covers, Mayer-Vietoris naturality and excision diagrams. */
public final class SimplicialCoverMapAlgebra extends ConcreteAlgebra<SimplicialCoverMap> {
    public final Algebra<Pair<SimplicialCover,SimplicialCover>> boundaries;
    public SimplicialCoverMapAlgebra(SimplicialCoverAlgebra covers,FiniteSimplicialMapAlgebra simplicialMaps,
                                     RelativeSimplicialMapAlgebra relativeMaps,IntegerRing integers,NaturalSemiring naturals,
                                     BooleanAlgebra truth,IntegerMatrixFamily matrices,IntegralHomologyAlgebra homology,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("CoverMap",SimplicialCoverMap.class,"Simplicial maps preserving two ordered cover pieces",f -> true),integers.unit());
        boundaries=boundaryCarrier(covers.algebra());
        binary("from-map",simplicialMaps.algebra(),boundaries,algebra(),true,(f,b) -> new SimplicialCoverMap(b.first,b.second,f));
        closed("compose",true,SimplicialCoverMap::compose);
        unary("inverse",algebra(),algebra(),true,SimplicialCoverMap::inverse);
        unary("source",algebra(),covers.algebra(),false,SimplicialCoverMap::source);
        unary("target",algebra(),covers.algebra(),false,SimplicialCoverMap::target);
        unary("union-map",algebra(),simplicialMaps.algebra(),false,SimplicialCoverMap::unionMap);
        unary("left-map",algebra(),simplicialMaps.algebra(),false,SimplicialCoverMap::leftMap);
        unary("right-map",algebra(),simplicialMaps.algebra(),false,SimplicialCoverMap::rightMap);
        unary("intersection-map",algebra(),simplicialMaps.algebra(),false,SimplicialCoverMap::intersectionMap);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialCoverMap::equals);
        unary("is-isomorphism",algebra(),truth.algebra(),false,SimplicialCoverMap::isIsomorphism);
        unary("identity-on",covers.algebra(),algebra(),false,SimplicialCoverMap::identity);
        binary("inclusion",covers.algebra(),covers.algebra(),algebra(),true,SimplicialCoverMap::inclusion);
        unary("swap",algebra(),algebra(),false,SimplicialCoverMap::swap);
        binary("sum-chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCoverMap::sumChainMatrix);
        unaryFlat("sum-chain-matrices",algebra(),matrices.algebra(),false,SimplicialCoverMap::sumChainMatrices);
        binary("sum-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCoverMap::sumHomologyMap);
        unaryFlat("sum-homology-maps",algebra(),maps.algebra(),false,SimplicialCoverMap::sumHomologyMaps);
        binary("source-sum-homology",algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCoverMap::sourceSumHomology);
        binary("target-sum-homology",algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCoverMap::targetSumHomology);
        binary("union-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCoverMap::unionHomologyMap);
        binary("intersection-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCoverMap::intersectionHomologyMap);
        binary("left-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCoverMap::leftHomologyMap);
        binary("right-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCoverMap::rightHomologyMap);
        flat("long-exact-maps",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCoverMap::longExactMaps);
        unary("left-relative-map",algebra(),relativeMaps.algebra(),false,SimplicialCoverMap::leftRelativeMap);
        unary("union-relative-map",algebra(),relativeMaps.algebra(),false,SimplicialCoverMap::unionRelativeMap);
        unaryFlat("excision-maps",algebra(),relativeMaps.algebra(),false,SimplicialCoverMap::excisionMaps);
        binary("contiguous",algebra(),algebra(),truth.algebra(),true,SimplicialCoverMap::contiguous);
        unary("image",algebra(),covers.algebra(),false,SimplicialCoverMap::image);
        unary("corestrict-image",algebra(),algebra(),false,SimplicialCoverMap::corestrictImage);
        binary("restrict",algebra(),covers.algebra(),algebra(),true,SimplicialCoverMap::restrict);
        law("A cover map preserves each ordered piece simplex by simplex; composition requires equality of the complete middle cover.");
        law("Sum-chain matrices are block diagonal and preserve boundaries, identities and composition. Their integral homology maps retain the sum presentations.");
        law("The four homology maps commute with all three Mayer-Vietoris maps, including the connecting homomorphism.");
        law("The relative maps on (A,I) and (U,B) commute with the excision inclusions as simplicial maps and hence on chains and homology.");
        law("Contiguity is checked in each target piece. The image cover uses (f(A),f(B)); its intersection need not equal f(I).");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<SimplicialCover,SimplicialCover>> boundaryCarrier(Algebra<SimplicialCover> covers) {
        Class<Pair<SimplicialCover,SimplicialCover>> type=(Class<Pair<SimplicialCover,SimplicialCover>>)(Class<?>)Pair.class;
        return carrier("SimplicialCover.pair",type,"Ordered source and target covers for a cover map",p ->
                covers.getParamClass().isInstance(p.first) && covers.getParamClass().isInstance(p.second) && covers.validate(p.first) && covers.validate(p.second));
    }
}
