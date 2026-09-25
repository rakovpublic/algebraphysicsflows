package algebra.concrete;

import mathematics.topology.RelativeSimplicialChain;

/** Native quotient chains, connecting cycles and the two standard relative cap products. */
public final class RelativeSimplicialChainAlgebra extends ConcreteAlgebra<RelativeSimplicialChain> {
    public RelativeSimplicialChainAlgebra(RelativeSimplicialAlgebra pairs,RelativeSimplicialMapAlgebra pairMaps,SimplicialChainAlgebra chains,
                                         SimplicialCochainAlgebra cochains,RelativeSimplicialCochainAlgebra relativeCochains,
                                         IntegerVectorFamily vectors,IntegerMatrixFamily matrices,IntegerRing integers,NaturalSemiring naturals,
                                         BooleanAlgebra truth,IntegralHomologyAlgebra homology,AbelianGroupElementAlgebra elements,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("RelativeChain",RelativeSimplicialChain.class,"Integral quotient chains on retained labelled pairs",c -> true),integers.unit());
        binary("zero-on",pairs.algebra(),integers.algebra(),algebra(),false,RelativeSimplicialChain::zero);
        flat("basis-on",pairs.algebra(),integers.algebra(),algebra(),false,RelativeSimplicialChain::basisChains);
        unary("absolute",chains.algebra(),algebra(),false,RelativeSimplicialChain::absolute);
        binary("from-absolute",chains.algebra(),pairs.algebra(),algebra(),true,RelativeSimplicialChain::fromAbsolute);
        closed("add",true,RelativeSimplicialChain::add);
        closed("subtract",true,RelativeSimplicialChain::subtract);
        unary("negate",algebra(),algebra(),false,RelativeSimplicialChain::negate);
        binary("scale",algebra(),integers.algebra(),algebra(),false,RelativeSimplicialChain::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RelativeSimplicialChain::equals);
        unary("pair",algebra(),pairs.algebra(),false,RelativeSimplicialChain::pair);
        unary("degree",algebra(),integers.algebra(),false,RelativeSimplicialChain::degree);
        unary("coordinates",algebra(),vectors.algebra(),false,RelativeSimplicialChain::coordinates);
        binary("with-coordinates",algebra(),vectors.algebra(),algebra(),true,RelativeSimplicialChain::withCoordinates);
        unary("is-zero",algebra(),truth.algebra(),false,RelativeSimplicialChain::isZero);
        unary("boundary",algebra(),algebra(),false,RelativeSimplicialChain::boundary);
        unary("is-cycle",algebra(),truth.algebra(),false,RelativeSimplicialChain::isCycle);
        unary("is-boundary",algebra(),truth.algebra(),false,RelativeSimplicialChain::isBoundary);
        unary("bounding-coordinates",algebra(),vectors.algebra(),true,RelativeSimplicialChain::boundingCoordinates);
        binary("homologous",algebra(),algebra(),truth.algebra(),true,RelativeSimplicialChain::homologous);
        unary("homology",algebra(),homology.algebra(),false,RelativeSimplicialChain::homology);
        unary("class-of",algebra(),elements.algebra(),true,RelativeSimplicialChain::classOf);
        binary("representative",algebra(),elements.algebra(),algebra(),true,RelativeSimplicialChain::representative);
        unaryFlat("cycle-generators",algebra(),algebra(),false,RelativeSimplicialChain::cycleGenerators);
        binary("pushforward",algebra(),pairMaps.algebra(),algebra(),true,RelativeSimplicialChain::pushforward);
        binary("evaluate",algebra(),relativeCochains.algebra(),integers.algebra(),true,RelativeSimplicialChain::evaluate);
        unary("lift-absolute",algebra(),chains.algebra(),false,RelativeSimplicialChain::liftAbsolute);
        unary("connect-cycle",algebra(),chains.algebra(),true,RelativeSimplicialChain::connectCycle);
        binary("cap",algebra(),cochains.algebra(),algebra(),true,RelativeSimplicialChain::cap);
        binary("cap-class",algebra(),cochains.algebra(),elements.algebra(),true,RelativeSimplicialChain::capClass);
        binary("relative-cap",algebra(),relativeCochains.algebra(),chains.algebra(),true,RelativeSimplicialChain::relativeCap);
        binary("relative-cap-class",algebra(),relativeCochains.algebra(),elements.algebra(),true,RelativeSimplicialChain::relativeCapClass);
        binary("cap-matrix",algebra(),cochains.algebra(),matrices.algebra(),true,RelativeSimplicialChain::capMatrix);
        binary("cap-homology-map",algebra(),cochains.algebra(),maps.algebra(),true,RelativeSimplicialChain::capHomologyMap);
        binary("cap-cohomology-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialChain::capCohomologyMatrix);
        binary("cap-cohomology-map",algebra(),naturals.algebra(),maps.algebra(),true,RelativeSimplicialChain::capCohomologyMap);
        binary("relative-cap-matrix",algebra(),relativeCochains.algebra(),matrices.algebra(),true,RelativeSimplicialChain::relativeCapMatrix);
        binary("relative-cap-homology-map",algebra(),relativeCochains.algebra(),maps.algebra(),true,RelativeSimplicialChain::relativeCapHomologyMap);
        binary("relative-cap-cohomology-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialChain::relativeCapCohomologyMatrix);
        binary("relative-cap-cohomology-map",algebra(),naturals.algebra(),maps.algebra(),true,RelativeSimplicialChain::relativeCapCohomologyMap);
        law("Quotient chains use only simplices outside A; negative degrees contain only zero. Projection commutes with boundary, but the zero-on-A lift generally does not.");
        law("A relative cycle has a lifted boundary in A, giving the connecting cycle of the long exact sequence.");
        law("Absolute cochains cap relative chains to relative chains. Cochains vanishing on the same A cap relative chains to absolute chains.");
        law("Both cap products obey the signed boundary identity, descend to integral homology and are natural on classes under maps of pairs.");
    }
}
