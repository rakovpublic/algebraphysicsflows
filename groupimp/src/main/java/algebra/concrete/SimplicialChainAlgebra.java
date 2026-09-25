package algebra.concrete;

import mathematics.topology.SimplicialChain;

/** Native integral chains, Kronecker pairing and degree-lowering cap products. */
public final class SimplicialChainAlgebra extends ConcreteAlgebra<SimplicialChain> {
    public SimplicialChainAlgebra(FiniteSimplicialAlgebra complexes,FiniteSimplicialMapAlgebra simplicialMaps,SimplicialCochainAlgebra cochains,
                                 IntegerVectorFamily vectors,IntegerMatrixFamily matrices,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
                                 IntegralHomologyAlgebra homology,AbelianGroupElementAlgebra elements,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("SimplicialChain",SimplicialChain.class,"Homogeneous integral chains on finite labelled complexes",c -> true),integers.unit());
        binary("zero-on",complexes.algebra(),integers.algebra(),algebra(),false,SimplicialChain::zero);
        flat("basis-on",complexes.algebra(),integers.algebra(),algebra(),false,SimplicialChain::basisChains);
        closed("add",true,SimplicialChain::add);
        closed("subtract",true,SimplicialChain::subtract);
        unary("negate",algebra(),algebra(),false,SimplicialChain::negate);
        binary("scale",algebra(),integers.algebra(),algebra(),false,SimplicialChain::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialChain::equals);
        unary("complex",algebra(),complexes.algebra(),false,SimplicialChain::complex);
        unary("degree",algebra(),integers.algebra(),false,SimplicialChain::degree);
        unary("coordinates",algebra(),vectors.algebra(),false,SimplicialChain::coordinates);
        binary("with-coordinates",algebra(),vectors.algebra(),algebra(),true,SimplicialChain::withCoordinates);
        unary("is-zero",algebra(),truth.algebra(),false,SimplicialChain::isZero);
        unary("boundary",algebra(),algebra(),false,SimplicialChain::boundary);
        unary("is-cycle",algebra(),truth.algebra(),false,SimplicialChain::isCycle);
        unary("is-boundary",algebra(),truth.algebra(),false,SimplicialChain::isBoundary);
        unary("bounding-coordinates",algebra(),vectors.algebra(),true,SimplicialChain::boundingCoordinates);
        binary("homologous",algebra(),algebra(),truth.algebra(),true,SimplicialChain::homologous);
        unary("homology",algebra(),homology.algebra(),false,SimplicialChain::homology);
        unary("class-of",algebra(),elements.algebra(),true,SimplicialChain::classOf);
        binary("representative",algebra(),elements.algebra(),algebra(),true,SimplicialChain::representative);
        unaryFlat("cycle-generators",algebra(),algebra(),false,SimplicialChain::cycleGenerators);
        binary("pushforward",algebra(),simplicialMaps.algebra(),algebra(),true,SimplicialChain::pushforward);
        binary("evaluate",algebra(),cochains.algebra(),integers.algebra(),true,SimplicialChain::evaluate);
        binary("cap",algebra(),cochains.algebra(),algebra(),true,SimplicialChain::cap);
        binary("cap-class",algebra(),cochains.algebra(),elements.algebra(),true,SimplicialChain::capClass);
        binary("cap-matrix",cochains.algebra(),integers.algebra(),matrices.algebra(),false,SimplicialChain::capMatrix);
        binary("cap-homology-map",cochains.algebra(),integers.algebra(),maps.algebra(),true,SimplicialChain::capHomologyMap);
        binary("cap-cohomology-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,SimplicialChain::capCohomologyMatrix);
        binary("cap-cohomology-map",algebra(),naturals.algebra(),maps.algebra(),true,SimplicialChain::capCohomologyMap);
        unary("augmentation",algebra(),integers.algebra(),true,SimplicialChain::augmentation);
        law("Chains retain their full labelled complex and integer degree. Negative degrees contain only zero; the unreduced boundary lowers degree and squares to zero.");
        law("Cap evaluates the cochain on the front face and retains the back face. For a p-cochain phi, boundary(c cap phi)=(-1)^p(boundary(c) cap phi - c cap coboundary(phi)).");
        law("Cycle-cocycle cap products descend to integral homology. (c cap a) cap b = c cap (a cup b); the cochain unit acts identically.");
        law("Pushforward is covariant and adjoint to cochain pullback under pairing. Cap naturality under arbitrary vertex maps is asserted on classes, not on sorted cochain coordinates.");
    }
}
