package algebra.concrete;

import mathematics.topology.RelativeSimplicialCochain;

/** Relative integral cochains, cup products, natural long exact cohomology sequences. */
public final class RelativeSimplicialCochainAlgebra extends ConcreteAlgebra<RelativeSimplicialCochain> {
    public RelativeSimplicialCochainAlgebra(RelativeSimplicialAlgebra pairs,RelativeSimplicialMapAlgebra pairMaps,SimplicialCochainAlgebra cochains,
                                          IntegerVectorFamily vectors,IntegerMatrixFamily matrices,IntegerRing integers,NaturalSemiring naturals,
                                          BooleanAlgebra truth,IntegralHomologyAlgebra homology,AbelianGroupElementAlgebra elements,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("RelativeCochain",RelativeSimplicialCochain.class,"Integral cochains vanishing on a retained subcomplex",c -> true),integers.unit());
        binary("zero-on",pairs.algebra(),naturals.algebra(),algebra(),false,RelativeSimplicialCochain::zero);
        flat("basis-on",pairs.algebra(),naturals.algebra(),algebra(),false,RelativeSimplicialCochain::basisCochains);
        unary("absolute",cochains.algebra(),algebra(),false,RelativeSimplicialCochain::absolute);
        binary("from-absolute",cochains.algebra(),pairs.algebra(),algebra(),true,RelativeSimplicialCochain::fromAbsolute);
        closed("add",true,RelativeSimplicialCochain::add);
        closed("subtract",true,RelativeSimplicialCochain::subtract);
        closed("cup",true,RelativeSimplicialCochain::cup);
        unary("negate",algebra(),algebra(),false,RelativeSimplicialCochain::negate);
        binary("scale",algebra(),integers.algebra(),algebra(),false,RelativeSimplicialCochain::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RelativeSimplicialCochain::equals);
        unary("pair",algebra(),pairs.algebra(),false,RelativeSimplicialCochain::pair);
        unary("degree",algebra(),naturals.algebra(),false,RelativeSimplicialCochain::degree);
        unary("coordinates",algebra(),vectors.algebra(),false,RelativeSimplicialCochain::coordinates);
        binary("with-coordinates",algebra(),vectors.algebra(),algebra(),true,RelativeSimplicialCochain::withCoordinates);
        unary("coboundary",algebra(),algebra(),false,RelativeSimplicialCochain::coboundary);
        unary("is-zero",algebra(),truth.algebra(),false,RelativeSimplicialCochain::isZero);
        unary("is-cocycle",algebra(),truth.algebra(),false,RelativeSimplicialCochain::isCocycle);
        unary("is-coboundary",algebra(),truth.algebra(),false,RelativeSimplicialCochain::isCoboundary);
        binary("cohomologous",algebra(),algebra(),truth.algebra(),true,RelativeSimplicialCochain::cohomologous);
        unary("cohomology",algebra(),homology.algebra(),false,RelativeSimplicialCochain::cohomology);
        unary("class-of",algebra(),elements.algebra(),true,RelativeSimplicialCochain::classOf);
        binary("representative",algebra(),elements.algebra(),algebra(),true,RelativeSimplicialCochain::representative);
        unary("cobounding-coordinates",algebra(),vectors.algebra(),true,RelativeSimplicialCochain::coboundingCoordinates);
        unaryFlat("cocycle-generators",algebra(),algebra(),false,RelativeSimplicialCochain::cocycleGenerators);
        binary("cup-class",algebra(),algebra(),elements.algebra(),true,RelativeSimplicialCochain::cupClass);
        binary("evaluate",algebra(),vectors.algebra(),integers.algebra(),true,RelativeSimplicialCochain::evaluate);
        binary("pullback",algebra(),pairMaps.algebra(),algebra(),true,RelativeSimplicialCochain::pullback);
        unary("extend-by-zero",algebra(),cochains.algebra(),false,RelativeSimplicialCochain::extendByZero);
        binary("connect-cocycle",cochains.algebra(),pairs.algebra(),algebra(),true,RelativeSimplicialCochain::connectCocycle);
        binary("cohomology-on",pairs.algebra(),naturals.algebra(),homology.algebra(),false,RelativeSimplicialCochain::cohomology);
        unaryFlat("cohomology-degrees",pairs.algebra(),homology.algebra(),false,RelativeSimplicialCochain::cohomologyDegrees);
        binary("extension-matrix",pairs.algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialCochain::extensionMatrix);
        binary("restriction-matrix",pairs.algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialCochain::restrictionMatrix);
        binary("connecting-cochain-matrix",pairs.algebra(),naturals.algebra(),matrices.algebra(),false,RelativeSimplicialCochain::connectingCochainMatrix);
        binary("ambient-cohomology-map",pairs.algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialCochain::ambientCohomologyMap);
        binary("restriction-cohomology-map",pairs.algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialCochain::restrictionCohomologyMap);
        binary("connecting-cohomology-map",pairs.algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialCochain::connectingCohomologyMap);
        flat("long-exact-segment",pairs.algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialCochain::longExactSegment);
        binary("cohomology-map",pairMaps.algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialCochain::cohomologyMap);
        unaryFlat("cohomology-maps",pairMaps.algebra(),maps.algebra(),false,RelativeSimplicialCochain::cohomologyMaps);
        flat("long-exact-maps",pairMaps.algebra(),naturals.algebra(),maps.algebra(),false,RelativeSimplicialCochain::longExactMaps);
        law("Relative cochains vanish on A and use the dual quotient-chain basis. Their coboundary squares to zero, and extension by zero into C*(X) is a cochain map.");
        law("The cup product of cochains on (X,A) and (X,B) lands on (X,A union B), satisfies Leibniz and is graded commutative on cohomology.");
        law("The connecting homomorphism raises degree: H^k(A)->H^(k+1)(X,A). The three-map long exact segment uses extension, restriction, connecting in that order.");
        law("Pair maps induce contravariant relative cohomology maps. All three long exact sequence squares commute on cohomology.");
    }
}
