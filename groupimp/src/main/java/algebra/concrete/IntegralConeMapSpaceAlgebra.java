package algebra.concrete;

import mathematics.topology.IntegralConeMapSpace;

/** Additive retained cone-map spaces and homotopy classes through existing native operation interfaces. */
public final class IntegralConeMapSpaceAlgebra extends ConcreteAlgebra<IntegralConeMapSpace> {
    public IntegralConeMapSpaceAlgebra(IntegralChainMappingConeAlgebra cones,IntegralChainConeMapAlgebra maps,
                                         IntegralHomologyAlgebra homology,PresentedAbelianGroupAlgebra groups,
                                         AbelianGroupTypeAlgebra types,AbelianGroupElementAlgebra elements,BooleanAlgebra truth) {
        super(carrier("ConeMapSpace",IntegralConeMapSpace.class,"Full defining cones for retained cone maps modulo unrestricted integral homotopy",s -> true),cones.unit());
        binary("from-cones",cones.algebra(),cones.algebra(),algebra(),false,IntegralConeMapSpace::new);
        unary("source",algebra(),cones.algebra(),false,IntegralConeMapSpace::source);
        unary("target",algebra(),cones.algebra(),false,IntegralConeMapSpace::target);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralConeMapSpace::equals);
        unary("zero",algebra(),maps.algebra(),false,IntegralConeMapSpace::zero);
        unary("homology",algebra(),homology.algebra(),false,IntegralConeMapSpace::homology);
        unary("homotopy-group",algebra(),groups.algebra(),false,IntegralConeMapSpace::homotopyGroup);
        unary("homotopy-type",algebra(),types.algebra(),false,IntegralConeMapSpace::homotopyType);
        unaryFlat("map-generators",algebra(),maps.algebra(),false,IntegralConeMapSpace::mapGenerators);
        binary("class-of",algebra(),maps.algebra(),elements.algebra(),true,IntegralConeMapSpace::classOf);
        binary("representative",algebra(),elements.algebra(),maps.algebra(),true,IntegralConeMapSpace::representative);
        unaryFlat("representatives",algebra(),maps.algebra(),false,IntegralConeMapSpace::representatives);
        law("The allowed degree-zero matrices have zero lower-left cone blocks. Chain-map cycles are quotiented only by boundaries of unrestricted homotopies whose forbidden blocks vanish.");
        law("class-of is additive and detects exactly integral cone homotopy between retained maps, including torsion invisible to induced integral maps. Representative selection is a section on sets and need not be additive.");
        law("Flat map-generators span every retained integral map, while representatives lift the nonzero minimal Smith generators of its homotopy quotient. Full defining cones and chosen square data survive decoding.");
    }
}
