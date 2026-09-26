package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.Pair;
import mathematics.topology.*;
import java.math.BigInteger;

/** Supplied homotopy equivalences of simplicial pairs, with inverse integral (co)homology maps. */
public final class SimplicialHomotopyEquivalenceAlgebra extends ConcreteAlgebra<SimplicialHomotopyEquivalence> {
    public final Algebra<Pair<RelativeSimplicialMap,RelativeSimplicialMap>> mapPairs;
    public final Algebra<Pair<SimplicialHomotopyPath,SimplicialHomotopyPath>> witnessPairs;
    public SimplicialHomotopyEquivalenceAlgebra(RelativeSimplicialMapAlgebra maps,RelativeSimplicialAlgebra pairs,SimplicialHomotopyPathAlgebra paths,
                                              AbelianGroupHomomorphismAlgebra homomorphisms,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("HomotopyEquivalence",SimplicialHomotopyEquivalence.class,"Opposite simplicial pair maps with checked contiguity-path inverse witnesses",e -> true),pairs.unit());
        mapPairs=pairCarrier("RelativeMap.pair",maps.algebra(),"Ordered forward and backward pair maps; witness construction checks opposite boundaries");
        witnessPairs=pairCarrier("HomotopyPath.pair",paths.algebra(),"Ordered source and target homotopies from identities to composites");
        binary("maps",maps.algebra(),maps.algebra(),mapPairs,false,Pair::new);
        binary("from-maps",mapPairs,witnessPairs,algebra(),true,(m,h) -> new SimplicialHomotopyEquivalence(m.first,m.second,h.first,h.second));
        unary("identity-on",pairs.algebra(),algebra(),false,SimplicialHomotopyEquivalence::identity);
        unary("from-isomorphism",maps.algebra(),algebra(),true,SimplicialHomotopyEquivalence::fromIsomorphism);
        closed("compose",true,SimplicialHomotopyEquivalence::compose);
        unary("inverse",algebra(),algebra(),false,SimplicialHomotopyEquivalence::inverse);
        unary("forward",algebra(),maps.algebra(),false,SimplicialHomotopyEquivalence::forward);
        unary("backward",algebra(),maps.algebra(),false,SimplicialHomotopyEquivalence::backward);
        unary("source",algebra(),pairs.algebra(),false,SimplicialHomotopyEquivalence::source);
        unary("target",algebra(),pairs.algebra(),false,SimplicialHomotopyEquivalence::target);
        unary("source-homotopy",algebra(),paths.algebra(),false,SimplicialHomotopyEquivalence::sourceHomotopy);
        unary("target-homotopy",algebra(),paths.algebra(),false,SimplicialHomotopyEquivalence::targetHomotopy);
        binary("equal",algebra(),algebra(),truth.algebra(),false,SimplicialHomotopyEquivalence::equals);
        binary("forward-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialHomotopyEquivalence::forwardHomologyMap);
        binary("backward-homology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialHomotopyEquivalence::backwardHomologyMap);
        flat("homology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialHomotopyEquivalence::homologyMaps);
        binary("forward-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialHomotopyEquivalence::forwardCohomologyMap);
        binary("backward-cohomology-map",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialHomotopyEquivalence::backwardCohomologyMap);
        flat("cohomology-maps",algebra(),naturals.algebra(),homomorphisms.algebra(),false,SimplicialHomotopyEquivalence::cohomologyMaps);
        law("The source path runs from Id_X to backward after forward, and the target path from Id_Y to forward after backward, through maps of the full labelled pairs.");
        law("The induced forward/backward integral homology maps are mutual inverses. Cohomology pullbacks are also mutual inverses and reverse the geometric directions.");
        law("Composition transports and concatenates the supplied paths. Equality retains both maps and both witnesses; swapping them gives a homotopy inverse, not necessarily a strict simplicial inverse.");
    }
    @SuppressWarnings("unchecked")
    private static <T> Algebra<Pair<T,T>> pairCarrier(String name,Algebra<T> component,String description) {
        Class<Pair<T,T>> type=(Class<Pair<T,T>>)(Class<?>)Pair.class;
        return carrier(name,type,description,p -> component.getParamClass().isInstance(p.first) && component.getParamClass().isInstance(p.second)
                && component.validate(p.first) && component.validate(p.second));
    }
    public SimplicialHomotopyEquivalenceAlgebra(RelativeSimplicialMapAlgebra maps,RelativeSimplicialAlgebra pairs,SimplicialHomotopyPathAlgebra paths,
                                              AbelianGroupHomomorphismAlgebra homomorphisms,NaturalSemiring naturals,BooleanAlgebra truth,
                                              FiniteSimplicialAlgebra complexes,IntegerRing integers) {
        this(maps,pairs,paths,homomorphisms,naturals,truth);
        Algebra<Pair<BigInteger,BigInteger>> vertices=pairCarrier("StrongCollapse.vertices",integers.algebra(),"Ordered removed vertex and surviving dominator labels");
        flat("dominators",pairs.algebra(),integers.algebra(),integers.algebra(),true,SimplicialStrongCollapse::dominators);
        unaryFlat("dominated-vertices",pairs.algebra(),integers.algebra(),false,SimplicialStrongCollapse::dominatedVertices);
        unary("is-strong-core",pairs.algebra(),truth.algebra(),false,SimplicialStrongCollapse::isStrongCore);
        binary("collapse-vertex",pairs.algebra(),vertices,algebra(),true,SimplicialStrongCollapse::collapseVertex);
        unary("strong-core",pairs.algebra(),algebra(),false,SimplicialStrongCollapse::strongCore);
        unary("strong-core-absolute",complexes.algebra(),algebra(),false,SimplicialStrongCollapse::strongCoreAbsolute);
        law("A compatible strong collapse deletes v only when every incident simplex extends by its distinct dominator w in each pair component containing v.");
        law("Strong-core reduction chooses the least removable label and least compatible dominator, retains a stationary target witness, and fixes every final target vertex at every source stage.");
    }
}
