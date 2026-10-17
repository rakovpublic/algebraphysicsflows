package algebra.concrete;

import mathematics.topology.IntegralChainMappingCone;
import java.math.BigInteger;

/** Integral mapping cones using the existing native scalar and flat interfaces. */
public final class IntegralChainMappingConeAlgebra extends ConcreteAlgebra<IntegralChainMappingCone> {
    public IntegralChainMappingConeAlgebra(SimplicialChainMapAlgebra maps,RelativeSimplicialAlgebra pairs,IntegerMatrixFamily matrices,
                                           IntegralHomologyAlgebra homology,AbelianGroupTypeAlgebra types,AbelianGroupHomomorphismAlgebra homomorphisms,
                                           IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("ChainCone",IntegralChainMappingCone.class,"Integral cone of a retained full chain map; target coordinates precede shifted source coordinates",c -> true),pairs.unit());
        unary("from-map",maps.algebra(),algebra(),false,IntegralChainMappingCone::new);
        unary("map",algebra(),maps.algebra(),false,IntegralChainMappingCone::map);
        unary("source",algebra(),pairs.algebra(),false,IntegralChainMappingCone::source);
        unary("target",algebra(),pairs.algebra(),false,IntegralChainMappingCone::target);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralChainMappingCone::equals);
        unary("dimension",algebra(),integers.algebra(),false,c -> BigInteger.valueOf(c.dimension()));
        binary("chain-rank",algebra(),naturals.algebra(),naturals.algebra(),false,IntegralChainMappingCone::chainRank);
        binary("boundary-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,IntegralChainMappingCone::boundaryMatrix);
        unaryFlat("boundary-matrices",algebra(),matrices.algebra(),false,IntegralChainMappingCone::boundaryMatrices);
        binary("homology",algebra(),naturals.algebra(),homology.algebra(),false,IntegralChainMappingCone::homology);
        binary("homology-type",algebra(),naturals.algebra(),types.algebra(),false,IntegralChainMappingCone::homologyType);
        unaryFlat("homology-types",algebra(),types.algebra(),false,IntegralChainMappingCone::homologyTypes);
        binary("cohomology",algebra(),naturals.algebra(),homology.algebra(),false,IntegralChainMappingCone::cohomology);
        binary("cohomology-type",algebra(),naturals.algebra(),types.algebra(),false,IntegralChainMappingCone::cohomologyType);
        unaryFlat("cohomology-types",algebra(),types.algebra(),false,IntegralChainMappingCone::cohomologyTypes);
        binary("inclusion-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,IntegralChainMappingCone::inclusionMatrix);
        binary("projection-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,IntegralChainMappingCone::projectionMatrix);
        binary("inclusion-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainMappingCone::inclusionHomologyMap);
        binary("projection-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainMappingCone::projectionHomologyMap);
        binary("inclusion-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainMappingCone::inclusionCohomologyMap);
        binary("projection-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainMappingCone::projectionCohomologyMap);
        flat("long-exact-segment",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainMappingCone::longExactSegment);
        flat("long-exact-cohomology-segment",algebra(),naturals.algebra(),homomorphisms.algebra(),false,IntegralChainMappingCone::longExactCohomologySegment);
        unary("is-acyclic",algebra(),truth.algebra(),false,IntegralChainMappingCone::isAcyclic);
        law("Cone(F)_n = target_n + source_(n-1), with boundary [[d_target,F],[0,-d_source]]. The negative shifted boundary makes consecutive differentials compose to zero.");
        law("Inclusion and shifted projection give exact integral homology and contravariant cohomology sequences, retaining actual presentations and torsion. An acyclic cone detects integral quasi-isomorphism.");
        law("Equality retains the defining map and full labelled pairs. The algebraic cone does not assert a simplicial or topological cone realization.");
    }
}
