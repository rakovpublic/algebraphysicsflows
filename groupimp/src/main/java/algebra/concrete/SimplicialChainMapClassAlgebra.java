package algebra.concrete;

import mathematics.topology.SimplicialChainMapClass;

/** Contextual chain-homotopy classes through the original Algebra and operation interfaces. */
public final class SimplicialChainMapClassAlgebra extends ConcreteAlgebra<SimplicialChainMapClass> {
    public SimplicialChainMapClassAlgebra(SimplicialChainMapSpaceAlgebra spaces,SimplicialChainMapAlgebra maps,
                                         RelativeSimplicialAlgebra pairs,AbelianGroupElementAlgebra elements,
                                         IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
                                         AbelianGroupHomomorphismAlgebra homomorphisms) {
        super(carrier("ChainMapClass",SimplicialChainMapClass.class,"Integral homotopy classes retaining full labelled source and target pairs",c -> true),spaces.unit());
        unary("from-map",maps.algebra(),algebra(),false,SimplicialChainMapClass::fromMap);
        binary("from-element",spaces.algebra(),elements.algebra(),algebra(),true,SimplicialChainMapClass::fromElement);
        unary("zero-in",spaces.algebra(),algebra(),false,SimplicialChainMapClass::zeroIn);
        unary("identity-on",pairs.algebra(),algebra(),false,SimplicialChainMapClass::identityOn);
        unary("source",algebra(),pairs.algebra(),false,SimplicialChainMapClass::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialChainMapClass::target);
        unary("space",algebra(),spaces.algebra(),false,SimplicialChainMapClass::space);
        unary("element",algebra(),elements.algebra(),false,SimplicialChainMapClass::element);
        unary("representative",algebra(),maps.algebra(),false,SimplicialChainMapClass::representative);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialChainMapClass::equals);
        unary("is-zero",algebra(),truth.algebra(),false,SimplicialChainMapClass::isZero);
        unary("is-identity",algebra(),truth.algebra(),false,SimplicialChainMapClass::isIdentity);
        closed("add",true,SimplicialChainMapClass::add);
        closed("subtract",true,SimplicialChainMapClass::subtract);
        unary("negate",algebra(),algebra(),false,SimplicialChainMapClass::negate);
        binary("scale",algebra(),integers.algebra(),algebra(),false,SimplicialChainMapClass::scale);
        closed("compose",true,SimplicialChainMapClass::compose);
        unary("has-finite-order",algebra(),truth.algebra(),false,SimplicialChainMapClass::hasFiniteOrder);
        unary("order",algebra(),naturals.algebra(),true,SimplicialChainMapClass::order);
        binary("homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainMapClass::homologyMap);
        binary("cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialChainMapClass::cohomologyMap);
        unaryFlat("homology-maps",algebra(),homomorphisms.algebra(),false,SimplicialChainMapClass::homologyMaps);
        unaryFlat("cohomology-maps",algebra(),homomorphisms.algebra(),false,SimplicialChainMapClass::cohomologyMaps);
        unaryFlat("generators-in",spaces.algebra(),algebra(),false,SimplicialChainMapClass::generatorsIn);
        unary("is-isomorphism",algebra(),truth.algebra(),false,SimplicialChainMapClass::isIsomorphism);
        unary("inverse",algebra(),algebra(),true,SimplicialChainMapClass::inverse);
        law("Each full source/target context is an abelian group of integral chain-homotopy classes. Equality retains the full context and detects torsion invisible on homology.");
        law("Composition is associative, additive in each argument and unital on matching full pairs; homology is covariant and cohomology contravariant.");
        law("The representative is a deterministic section on sets, not generally additive or functorial. Its exact matrices and actions on noncycles are not invariants of the class.");
    }
}
