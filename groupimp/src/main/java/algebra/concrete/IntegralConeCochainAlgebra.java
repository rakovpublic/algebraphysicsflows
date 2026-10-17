package algebra.concrete;

import mathematics.topology.IntegralConeCochain;

/** Typed integral cone cochains through existing native scalar and flat operations. */
public final class IntegralConeCochainAlgebra extends ConcreteAlgebra<IntegralConeCochain> {
    public IntegralConeCochainAlgebra(IntegralChainMappingConeAlgebra cones,RelativeSimplicialCochainAlgebra relative,
            IntegerVectorFamily vectors,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
            IntegralHomologyAlgebra homology,AbelianGroupElementAlgebra elements,IntegralConeChainAlgebra chains) {
        super(carrier("ConeCochain",IntegralConeCochain.class,"Integral cochains retaining the exact cone, degree and coordinates",c -> true),integers.unit());
        binary("zero-on",cones.algebra(),naturals.algebra(),algebra(),false,IntegralConeCochain::zero);
        flat("basis-on",cones.algebra(),naturals.algebra(),algebra(),false,IntegralConeCochain::basis);
        closed("add",true,IntegralConeCochain::add);
        closed("subtract",true,IntegralConeCochain::subtract);
        unary("negate",algebra(),algebra(),false,IntegralConeCochain::negate);
        binary("scale",algebra(),integers.algebra(),algebra(),false,IntegralConeCochain::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralConeCochain::equals);
        unary("cone",algebra(),cones.algebra(),false,IntegralConeCochain::cone);
        unary("degree",algebra(),naturals.algebra(),false,IntegralConeCochain::degree);
        unary("coordinates",algebra(),vectors.algebra(),false,IntegralConeCochain::coordinates);
        binary("with-coordinates",algebra(),vectors.algebra(),algebra(),true,IntegralConeCochain::withCoordinates);
        unary("is-zero",algebra(),truth.algebra(),false,IntegralConeCochain::isZero);
        unary("coboundary",algebra(),algebra(),false,IntegralConeCochain::coboundary);
        unary("is-cocycle",algebra(),truth.algebra(),false,IntegralConeCochain::isCocycle);
        unary("is-coboundary",algebra(),truth.algebra(),false,IntegralConeCochain::isCoboundary);
        unary("cobounding-coordinates",algebra(),vectors.algebra(),true,IntegralConeCochain::coboundingCoordinates);
        unary("cobounding-cochain",algebra(),algebra(),true,IntegralConeCochain::coboundingCochain);
        binary("cohomologous",algebra(),algebra(),truth.algebra(),true,IntegralConeCochain::cohomologous);
        unary("cohomology",algebra(),homology.algebra(),false,IntegralConeCochain::cohomology);
        unary("class-of",algebra(),elements.algebra(),true,IntegralConeCochain::classOf);
        binary("representative",algebra(),elements.algebra(),algebra(),true,IntegralConeCochain::representative);
        unaryFlat("cocycle-generators",algebra(),algebra(),false,IntegralConeCochain::cocycleGenerators);
        unary("target-part",algebra(),relative.algebra(),false,IntegralConeCochain::targetPart);
        unary("source-part",algebra(),relative.algebra(),true,IntegralConeCochain::sourcePart);
        binary("include-source",cones.algebra(),relative.algebra(),algebra(),true,IntegralConeCochain::includeSource);
        binary("lift-target",cones.algebra(),relative.algebra(),algebra(),true,IntegralConeCochain::liftTarget);
        binary("evaluate",algebra(),chains.algebra(),integers.algebra(),true,IntegralConeCochain::evaluate);
        law("Coordinates are ordered target then shifted source; equality and additive operations retain the exact defining map and degree.");
        law("Cochain degrees are nonnegative. Coboundary squares to zero, and evaluation satisfies <delta u,c>=<u,dc>. Source inclusion anticommutes with coboundary; the target coordinate lift is generally not a cochain map.");
        law("Integral cohomology retains torsion and exact presentations. Primitive and generator calculations share one complete work budget. No cup product on arbitrary algebraic cones is assumed.");
    }
}
