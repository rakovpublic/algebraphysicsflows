package algebra.concrete;

import mathematics.topology.RelativeSimplicialComplex;
import java.math.BigInteger;

/** Relative integral chains, homology and the maps in the long exact sequence of a pair. */
public final class RelativeSimplicialAlgebra extends ConcreteAlgebra<RelativeSimplicialComplex> {
    public RelativeSimplicialAlgebra(FiniteSimplicialAlgebra complexes,IntegerSetAlgebra sets,IntegerRing integers,
                                    NaturalSemiring naturals,BooleanAlgebra truth,IntegerMatrixFamily matrices,
                                    IntegralHomologyAlgebra homology,AbelianGroupTypeAlgebra types,
                                    AbelianGroupHomomorphismAlgebra maps,FiniteSimplicialMapAlgebra simplicialMaps) {
        super(carrier("RelativeComplex",RelativeSimplicialComplex.class,"Finite labelled simplicial pairs with integral relative homology",p -> true),integers.unit());
        binary("from-complexes",complexes.algebra(),complexes.algebra(),algebra(),true,RelativeSimplicialComplex::new);
        unary("ambient",algebra(),complexes.algebra(),false,RelativeSimplicialComplex::ambient);
        unary("subcomplex",algebra(),complexes.algebra(),false,RelativeSimplicialComplex::subcomplex);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RelativeSimplicialComplex::equals);
        unary("dimension",algebra(),integers.algebra(),false,p -> BigInteger.valueOf(p.dimension()));
        unary("euler-characteristic",algebra(),integers.algebra(),false,RelativeSimplicialComplex::eulerCharacteristic);
        binary("simplex-count",algebra(),naturals.algebra(),naturals.algebra(),false,(p,k) -> BigInteger.valueOf(p.simplexCount(k)));
        flat("simplex-basis",algebra(),naturals.algebra(),sets.algebra(),false,RelativeSimplicialComplex::simplexBasis);
        binary("boundary-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialComplex::boundaryMatrix);
        unaryFlat("boundary-matrices",algebra(),matrices.algebra(),false,RelativeSimplicialComplex::boundaryMatrices);
        binary("homology",algebra(),naturals.algebra(),homology.algebra(),false,RelativeSimplicialComplex::homology);
        unaryFlat("homology-degrees",algebra(),homology.algebra(),false,RelativeSimplicialComplex::homologyDegrees);
        binary("homology-type",algebra(),naturals.algebra(),types.algebra(),false,RelativeSimplicialComplex::homologyType);
        unaryFlat("homology-types",algebra(),types.algebra(),false,RelativeSimplicialComplex::homologyTypes);
        binary("betti-number",algebra(),naturals.algebra(),naturals.algebra(),false,(p,k) -> p.homologyType(k).freeRank());
        binary("is-acyclic-degree",algebra(),naturals.algebra(),truth.algebra(),false,(p,k) -> p.homology(k).isAcyclic());
        binary("projection-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialComplex::projectionMatrix);
        binary("lift-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialComplex::liftMatrix);
        binary("inclusion-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialComplex::inclusionMatrix);
        binary("connecting-chain-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialComplex::connectingChainMatrix);
        binary("inclusion-homology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialComplex::inclusionHomology);
        binary("quotient-homology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialComplex::quotientHomology);
        binary("connecting-homology",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialComplex::connectingHomology);
        flat("long-exact-segment",algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialComplex::longExactSegment);
        unary("absolute",complexes.algebra(),algebra(),false,RelativeSimplicialComplex::absolute);
        unary("diagonal",complexes.algebra(),algebra(),false,RelativeSimplicialComplex::diagonal);
        unary("inclusion",algebra(),simplicialMaps.algebra(),false,RelativeSimplicialComplex::inclusion);
        law("Relative chains use the ordered simplices of X outside A; quotient boundaries square to zero and Euler characteristic is chi(X)-chi(A).");
        law("Inclusion and quotient are chain maps. The zero-on-A lift is a chain-group section and need not commute with boundaries.");
        law("Connecting homology sends a relative cycle to the class of its lifted boundary in A; H_-1(A) is zero in the unreduced convention.");
        law("The emitted long-exact segment is inclusion, quotient, connecting in that order; consecutive maps compose to zero and their images equal the next kernels.");
    }
}
