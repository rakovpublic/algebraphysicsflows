package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.Pair;
import mathematics.linear.IntegerMatrix;
import mathematics.topology.IntegralHomology;
import java.math.BigInteger;

/** Constructive integral homology connected to native matrices, groups, elements and homomorphisms. */
public final class IntegralHomologyAlgebra extends ConcreteAlgebra<IntegralHomology> {
    public final Algebra<Pair<IntegralHomology,IntegerMatrix>> mapInputs;
    public IntegralHomologyAlgebra(FiniteSimplicialAlgebra complexes,IntegerMatrixFamily matrices,IntegerVectorFamily vectors,
                                   PresentedAbelianGroupAlgebra groups,AbelianGroupTypeAlgebra types,AbelianGroupElementAlgebra elements,
                                   AbelianGroupHomomorphismAlgebra maps,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("IntegralHomology",IntegralHomology.class,"Constructive integral homology of consecutive boundary matrices",h -> true),naturals.unit());
        mapInputs=mapInputCarrier(algebra(),matrices.algebra());
        binary("from-boundaries",matrices.algebra(),matrices.algebra(),algebra(),true,IntegralHomology::new);
        binary("at-degree",complexes.algebra(),naturals.algebra(),algebra(),false,IntegralHomology::atDegree);
        unary("outgoing-boundary",algebra(),matrices.algebra(),false,IntegralHomology::outgoingBoundary);
        unary("incoming-boundary",algebra(),matrices.algebra(),false,IntegralHomology::incomingBoundary);
        unary("cycle-matrix",algebra(),matrices.algebra(),false,IntegralHomology::cycleMatrix);
        unary("boundary-coordinates",algebra(),matrices.algebra(),false,IntegralHomology::boundaryCoordinates);
        unary("group",algebra(),groups.algebra(),false,IntegralHomology::group);
        unary("as-type",algebra(),types.algebra(),false,IntegralHomology::type);
        unary("chain-rank",algebra(),naturals.algebra(),false,h -> BigInteger.valueOf(h.chainRank()));
        unary("cycle-rank",algebra(),naturals.algebra(),false,h -> BigInteger.valueOf(h.cycleRank()));
        unary("boundary-rank",algebra(),naturals.algebra(),false,h -> BigInteger.valueOf(h.boundaryRank()));
        unary("betti-number",algebra(),naturals.algebra(),false,h -> h.type().freeRank());
        unary("is-acyclic",algebra(),truth.algebra(),false,IntegralHomology::isAcyclic);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegralHomology::equals);
        unaryFlat("cycle-basis",algebra(),vectors.algebra(),false,IntegralHomology::cycleBasis);
        unaryFlat("boundary-basis",algebra(),vectors.algebra(),false,IntegralHomology::boundaryBasis);
        unaryFlat("generators",algebra(),vectors.algebra(),false,IntegralHomology::generators);
        binary("is-cycle",algebra(),vectors.algebra(),truth.algebra(),true,IntegralHomology::isCycle);
        binary("is-boundary",algebra(),vectors.algebra(),truth.algebra(),true,IntegralHomology::isBoundary);
        binary("class-of",algebra(),vectors.algebra(),elements.algebra(),true,IntegralHomology::classOf);
        binary("representative",algebra(),elements.algebra(),vectors.algebra(),true,IntegralHomology::representative);
        binary("bounding-chain",algebra(),vectors.algebra(),vectors.algebra(),true,IntegralHomology::boundingChain);
        binary("cycle-coordinates",algebra(),vectors.algebra(),vectors.algebra(),true,IntegralHomology::cycleCoordinates);
        binary("from-cycle-coordinates",algebra(),vectors.algebra(),vectors.algebra(),true,IntegralHomology::fromCycleCoordinates);
        unary("projection",algebra(),maps.algebra(),false,IntegralHomology::projection);
        binary("induced-map",algebra(),mapInputs,maps.algebra(),true,(h,p) -> h.inducedMap(p.first,p.second));
        unary("zero-class",algebra(),elements.algebra(),false,h -> h.group().zero());
        law("Consecutive boundaries compose to zero. The cycle matrix K spans the full integral kernel; K times boundary-coordinates equals the incoming differential.");
        law("Classes are cycles modulo integral boundaries; representative followed by class-of is identity, without asserting an additive section.");
        law("Induced maps require preservation of cycles and boundaries and respect composition, identity and homologous cycles.");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<IntegralHomology,IntegerMatrix>> mapInputCarrier(Algebra<IntegralHomology> homology,Algebra<IntegerMatrix> matrices) {
        Class<Pair<IntegralHomology,IntegerMatrix>> type=(Class<Pair<IntegralHomology,IntegerMatrix>>)(Class<?>)Pair.class;
        return carrier("IntegralHomology.map-input",type,"Target homology and a matrix on the chain groups in the chosen degree",p ->
                homology.getParamClass().isInstance(p.first) && matrices.getParamClass().isInstance(p.second)
                && homology.validate(p.first) && matrices.validate(p.second));
    }
}
