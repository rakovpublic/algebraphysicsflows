package algebra.concrete;

import mathematics.topology.SimplicialCover;

/** Two-subcomplex covers with constructive integral Mayer-Vietoris and simplicial excision. */
public final class SimplicialCoverAlgebra extends ConcreteAlgebra<SimplicialCover> {
    public SimplicialCoverAlgebra(FiniteSimplicialAlgebra complexes,IntegerRing integers,NaturalSemiring naturals,
                                  BooleanAlgebra truth,IntegerMatrixFamily matrices,IntegralHomologyAlgebra homology,
                                  AbelianGroupHomomorphismAlgebra maps,RelativeSimplicialMapAlgebra relativeMaps) {
        super(carrier("SimplicialCover",SimplicialCover.class,"Ordered two-subcomplex covers of finite unions",c -> true),integers.unit());
        binary("from-complexes",complexes.algebra(),complexes.algebra(),algebra(),false,SimplicialCover::new);
        unary("left",algebra(),complexes.algebra(),false,SimplicialCover::left);
        unary("right",algebra(),complexes.algebra(),false,SimplicialCover::right);
        unary("union",algebra(),complexes.algebra(),false,SimplicialCover::union);
        unary("intersection",algebra(),complexes.algebra(),false,SimplicialCover::intersection);
        unary("swap",algebra(),algebra(),false,SimplicialCover::swap);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialCover::equals);
        unary("euler-characteristic",algebra(),integers.algebra(),false,SimplicialCover::eulerCharacteristic);
        binary("sum-boundary-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCover::sumBoundaryMatrix);
        unaryFlat("sum-boundary-matrices",algebra(),matrices.algebra(),false,SimplicialCover::sumBoundaryMatrices);
        binary("sum-homology",algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCover::sumHomology);
        unaryFlat("sum-homology-degrees",algebra(),homology.algebra(),false,SimplicialCover::sumHomologyDegrees);
        binary("left-homology",algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCover::leftHomology);
        binary("right-homology",algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCover::rightHomology);
        binary("intersection-homology",algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCover::intersectionHomology);
        binary("union-homology",algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCover::unionHomology);
        binary("intersection-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCover::intersectionMatrix);
        binary("union-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCover::unionMatrix);
        binary("split-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCover::splitMatrix);
        binary("connecting-chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialCover::connectingChainMatrix);
        binary("intersection-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::intersectionHomologyMap);
        binary("union-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::unionHomologyMap);
        binary("connecting-homology-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::connectingHomologyMap);
        flat("long-exact-segment",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::longExactSegment);
        binary("left-inclusion-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::leftInclusionMap);
        binary("right-inclusion-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::rightInclusionMap);
        binary("left-projection-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::leftProjectionMap);
        binary("right-projection-map",algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCover::rightProjectionMap);
        unary("excision-map",algebra(),relativeMaps.algebra(),false,SimplicialCover::excisionMap);
        law("The ordered pieces cover their union simplex by simplex. Sum chains use left coordinates followed by right coordinates.");
        law("The intersection chain map is (i,-j), the union map is addition, and their sequence is short exact over Z.");
        law("The connecting map sends a union cycle to the boundary of its left part. Splitting assigns shared simplices left and need not be a chain map.");
        law("The Mayer-Vietoris homology sequence is exact. Component inclusions and projections exhibit the sum homology as the direct sum of the two homologies.");
        law("The inclusion (A,A intersection B)->(A union B,B) induces an isomorphism on integral relative chains and homology.");
    }
}
