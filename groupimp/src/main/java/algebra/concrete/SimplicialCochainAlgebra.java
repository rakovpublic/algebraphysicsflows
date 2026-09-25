package algebra.concrete;

import mathematics.topology.SimplicialCochain;

/** Native homogeneous integer cochains, cup products and contravariant cohomology. */
public final class SimplicialCochainAlgebra extends ConcreteAlgebra<SimplicialCochain> {
    public SimplicialCochainAlgebra(FiniteSimplicialAlgebra complexes,FiniteSimplicialMapAlgebra simplicialMaps,
                                  IntegerVectorFamily vectors,IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,
                                  IntegralHomologyAlgebra homology,AbelianGroupElementAlgebra elements,AbelianGroupHomomorphismAlgebra maps) {
        super(carrier("SimplicialCochain",SimplicialCochain.class,"Homogeneous integral cochains on labelled finite complexes",c -> true),integers.unit());
        binary("zero-on",complexes.algebra(),naturals.algebra(),algebra(),false,SimplicialCochain::zero);
        unary("unit-on",complexes.algebra(),algebra(),false,SimplicialCochain::unit);
        flat("basis-on",complexes.algebra(),naturals.algebra(),algebra(),false,SimplicialCochain::basisCochains);
        closed("add",true,SimplicialCochain::add);
        closed("subtract",true,SimplicialCochain::subtract);
        closed("cup",true,SimplicialCochain::cup);
        unary("negate",algebra(),algebra(),false,SimplicialCochain::negate);
        binary("scale",algebra(),integers.algebra(),algebra(),false,SimplicialCochain::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialCochain::equals);
        unary("complex",algebra(),complexes.algebra(),false,SimplicialCochain::complex);
        unary("degree",algebra(),naturals.algebra(),false,SimplicialCochain::degree);
        unary("coordinates",algebra(),vectors.algebra(),false,SimplicialCochain::coordinates);
        binary("with-coordinates",algebra(),vectors.algebra(),algebra(),true,SimplicialCochain::withCoordinates);
        unary("coboundary",algebra(),algebra(),false,SimplicialCochain::coboundary);
        unary("is-zero",algebra(),truth.algebra(),false,SimplicialCochain::isZero);
        unary("is-cocycle",algebra(),truth.algebra(),false,SimplicialCochain::isCocycle);
        unary("is-coboundary",algebra(),truth.algebra(),false,SimplicialCochain::isCoboundary);
        binary("cohomologous",algebra(),algebra(),truth.algebra(),true,SimplicialCochain::cohomologous);
        unary("cohomology",algebra(),homology.algebra(),false,SimplicialCochain::cohomology);
        unary("class-of",algebra(),elements.algebra(),true,SimplicialCochain::classOf);
        binary("representative",algebra(),elements.algebra(),algebra(),true,SimplicialCochain::representative);
        unary("cobounding-coordinates",algebra(),vectors.algebra(),true,SimplicialCochain::coboundingCoordinates);
        unaryFlat("cocycle-generators",algebra(),algebra(),false,SimplicialCochain::cocycleGenerators);
        binary("cup-class",algebra(),algebra(),elements.algebra(),true,SimplicialCochain::cupClass);
        binary("evaluate",algebra(),vectors.algebra(),integers.algebra(),true,SimplicialCochain::evaluate);
        binary("pullback",algebra(),simplicialMaps.algebra(),algebra(),true,SimplicialCochain::pullback);
        binary("cohomology-on",complexes.algebra(),naturals.algebra(),homology.algebra(),false,SimplicialCochain::cohomology);
        unaryFlat("cohomology-degrees",complexes.algebra(),homology.algebra(),false,SimplicialCochain::cohomologyDegrees);
        binary("cohomology-map",simplicialMaps.algebra(),naturals.algebra(),maps.algebra(),false,SimplicialCochain::cohomologyMap);
        unaryFlat("cohomology-maps",simplicialMaps.algebra(),maps.algebra(),false,SimplicialCochain::cohomologyMaps);
        law("Cochains retain their complete labelled complex and nonnegative degree; addition requires both to agree.");
        law("Coboundary is the transpose of the oriented boundary and squares to zero. The cup product is associative, unital and satisfies the signed Leibniz rule.");
        law("Cup products are graded commutative on cohomology, not generally on cochains. Classes use the existing integral quotient presentations.");
        law("Simplicial pullback commutes with coboundary and reverses composition. Cup naturality for arbitrary vertex maps is asserted on cohomology.");
    }
}
