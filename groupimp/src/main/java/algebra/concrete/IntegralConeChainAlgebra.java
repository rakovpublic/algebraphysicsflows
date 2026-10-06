package algebra.concrete;

import mathematics.topology.IntegralConeChain;

/** Typed integral cone chains through existing native scalar and flat operations. */
public final class IntegralConeChainAlgebra extends ConcreteAlgebra<IntegralConeChain> {
    public IntegralConeChainAlgebra(IntegralChainMappingConeAlgebra cones,RelativeSimplicialChainAlgebra relative,
            IntegerVectorFamily vectors,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
            IntegralHomologyAlgebra homology,AbelianGroupElementAlgebra elements) {
        super(carrier("ConeChain",IntegralConeChain.class,"Integral chains retaining the exact cone, degree and coordinates",c -> true),integers.unit());
        binary("zero-on",cones.algebra(),integers.algebra(),algebra(),false,IntegralConeChain::zero);
        flat("basis-on",cones.algebra(),integers.algebra(),algebra(),false,IntegralConeChain::basis);
        closed("add",true,IntegralConeChain::add);
        closed("subtract",true,IntegralConeChain::subtract);
        unary("negate",algebra(),algebra(),false,IntegralConeChain::negate);
        binary("scale",algebra(),integers.algebra(),algebra(),false,IntegralConeChain::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralConeChain::equals);
        unary("cone",algebra(),cones.algebra(),false,IntegralConeChain::cone);
        unary("degree",algebra(),integers.algebra(),false,IntegralConeChain::degree);
        unary("coordinates",algebra(),vectors.algebra(),false,IntegralConeChain::coordinates);
        binary("with-coordinates",algebra(),vectors.algebra(),algebra(),true,IntegralConeChain::withCoordinates);
        unary("is-zero",algebra(),truth.algebra(),false,IntegralConeChain::isZero);
        unary("boundary",algebra(),algebra(),false,IntegralConeChain::boundary);
        unary("is-cycle",algebra(),truth.algebra(),false,IntegralConeChain::isCycle);
        unary("is-boundary",algebra(),truth.algebra(),false,IntegralConeChain::isBoundary);
        unary("bounding-coordinates",algebra(),vectors.algebra(),true,IntegralConeChain::boundingCoordinates);
        unary("bounding-chain",algebra(),algebra(),true,IntegralConeChain::boundingChain);
        binary("homologous",algebra(),algebra(),truth.algebra(),true,IntegralConeChain::homologous);
        unary("homology",algebra(),homology.algebra(),false,IntegralConeChain::homology);
        unary("class-of",algebra(),elements.algebra(),true,IntegralConeChain::classOf);
        binary("representative",algebra(),elements.algebra(),algebra(),true,IntegralConeChain::representative);
        unaryFlat("cycle-generators",algebra(),algebra(),false,IntegralConeChain::cycleGenerators);
        unary("target-part",algebra(),relative.algebra(),false,IntegralConeChain::targetPart);
        unary("source-part",algebra(),relative.algebra(),false,IntegralConeChain::sourcePart);
        binary("include-target",cones.algebra(),relative.algebra(),algebra(),true,IntegralConeChain::includeTarget);
        binary("lift-source",cones.algebra(),relative.algebra(),algebra(),true,IntegralConeChain::liftSource);
        law("Coordinates are ordered target then shifted source; equality and additive operations retain the exact defining map and degree.");
        law("Negative chain degrees contain only zero. Boundary squares to zero. Target inclusion commutes with boundary; shifted source projection anticommutes, and its coordinate lift includes the defining map term.");
        law("Integral cycle classes, representatives and typed bounding chains retain torsion. Complete homology and generator calculations share work budgets.");
    }
}
