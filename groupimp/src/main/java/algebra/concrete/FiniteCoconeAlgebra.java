package algebra.concrete;

import mathematics.structures.*;

/** Native cocone operations and finite colimits, using the checked opposite-cone implementation. */
public final class FiniteCoconeAlgebra extends ConcreteAlgebra<FiniteCocone> {
    public FiniteCoconeAlgebra(FiniteConeAlgebra cones,FiniteFunctorAlgebra functors,FiniteNaturalTransformationAlgebra transformations,
            FiniteIntegerFunctionAlgebra functions,IntegerRing integers,BooleanAlgebra truth) {
        super(carrier("FiniteCocone",FiniteCocone.class,"Typed commuting cocones over finite diagrams",c -> true),integers.unit());
        unary("diagram",algebra(),functors.algebra(),false,c -> c.diagram);
        unary("vertex",algebra(),integers.algebra(),false,c -> c.vertex);
        binary("leg",algebra(),integers.algebra(),integers.algebra(),true,FiniteCocone::leg);
        unary("leg-map",algebra(),functions.algebra(),false,c -> functions.member(c.diagram.source.objects(),c.diagram.target.arrowLabels(),c.legMap()));
        unaryFlat("legs",algebra(),integers.algebra(),false,FiniteCocone::legValues);
        unary("natural-transformation",algebra(),transformations.algebra(),false,FiniteCocone::asTransformation);
        unary("is-colimit",algebra(),truth.algebra(),false,FiniteCocone::isColimit);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteCocone::equals);
        binary("descend",algebra(),algebra(),integers.algebra(),true,FiniteCocone::descend);
        flat("mediators",algebra(),algebra(),integers.algebra(),true,FiniteCocone::mediatorsTo);
        binary("reindex",algebra(),functors.algebra(),algebra(),true,FiniteCocone::reindex);
        binary("map",algebra(),functors.algebra(),algebra(),true,FiniteCocone::map);
        unary("colimit",functors.algebra(),algebra(),true,FiniteCocone::colimit);
        flat("cocones-at",functors.algebra(),integers.algebra(),algebra(),true,FiniteCocone::at);
        binary("from-transformation",transformations.algebra(),integers.algebra(),algebra(),true,FiniteCocone::fromTransformation);
        unary("opposite",algebra(),cones.algebra(),false,FiniteCocone::opposite);
        unary("opposite-cone",cones.algebra(),algebra(),false,FiniteCocone::fromOpposite);
        law("Every leg has source F(j) and target vertex; F(f) followed by leg_k equals leg_j for each f:j->k.");
        law("A colimit cocone has exactly one commuting mediator to every cocone over the same labelled diagram.");
        law("Colimit search is limit search on opposite categories, with the same 10000-cone and 1000000-step bounds.");
        law("Mapping or reindexing preserves cocone equations but need not preserve the colimit universal property.");
    }
}
