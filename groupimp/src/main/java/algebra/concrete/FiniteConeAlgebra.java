package algebra.concrete;

import mathematics.structures.*;

/** Finite diagram cones and checked limits registered in the existing Algebra/MathTool runtime. */
public final class FiniteConeAlgebra extends ConcreteAlgebra<FiniteCone> {
    public FiniteConeAlgebra(FiniteFunctorAlgebra functors,FiniteNaturalTransformationAlgebra transformations,
            FiniteIntegerFunctionAlgebra functions,IntegerRing integers,BooleanAlgebra truth) {
        super(carrier("FiniteCone",FiniteCone.class,"Typed commuting cones over finite diagrams",c -> true),integers.unit());
        unary("diagram",algebra(),functors.algebra(),false,c -> c.diagram);
        unary("vertex",algebra(),integers.algebra(),false,c -> c.vertex);
        binary("leg",algebra(),integers.algebra(),integers.algebra(),true,FiniteCone::leg);
        unary("leg-map",algebra(),functions.algebra(),false,c -> functions.member(c.diagram.source.objects(),c.diagram.target.arrowLabels(),c.legMap()));
        unaryFlat("legs",algebra(),integers.algebra(),false,FiniteCone::legValues);
        unary("natural-transformation",algebra(),transformations.algebra(),false,FiniteCone::asTransformation);
        unary("is-limit",algebra(),truth.algebra(),false,FiniteCone::isLimit);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteCone::equals);
        binary("lift",algebra(),algebra(),integers.algebra(),true,FiniteCone::lift);
        flat("mediators",algebra(),algebra(),integers.algebra(),true,FiniteCone::mediatorsFrom);
        binary("reindex",algebra(),functors.algebra(),algebra(),true,FiniteCone::reindex);
        binary("map",algebra(),functors.algebra(),algebra(),true,FiniteCone::map);
        unary("limit",functors.algebra(),algebra(),true,FiniteCone::limit);
        flat("cones-at",functors.algebra(),integers.algebra(),algebra(),true,FiniteCone::at);
        binary("from-transformation",transformations.algebra(),integers.algebra(),algebra(),true,FiniteCone::fromTransformation);
        law("Every leg has source vertex and target F(j); leg_j followed by F(f) equals leg_k for each f:j->k.");
        law("A limit cone has exactly one commuting mediator from every cone over the same labelled diagram.");
        law("Search is bounded at 10000 cones and 1000000 steps; resource exhaustion is distinct from mathematical nonexistence.");
        law("Mapping or reindexing preserves cone equations but need not preserve the limit universal property.");
    }
}
