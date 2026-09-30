package algebra.concrete;

import mathematics.foundations.FiniteSet;
import mathematics.structures.*;

/** Native finite adjoint equivalences with constructed and checked coherence witnesses. */
public final class FiniteEquivalenceAlgebra extends ConcreteAlgebra<FiniteEquivalence> {
    public FiniteEquivalenceAlgebra(FiniteCategoryAlgebra categories,FiniteFunctorAlgebra functors,
            FiniteNaturalTransformationAlgebra transformations,BooleanAlgebra truth) {
        super(carrier("FiniteEquivalence",FiniteEquivalence.class,"Finite equivalences with unit, counit and both triangle identities",e -> true),categories.unit());
        closed("compose",true,FiniteEquivalence::compose);
        unary("inverse",algebra(),algebra(),false,FiniteEquivalence::inverse);
        unary("opposite",algebra(),algebra(),false,FiniteEquivalence::opposite);
        unary("source",algebra(),categories.algebra(),false,e -> e.forward.source);
        unary("target",algebra(),categories.algebra(),false,e -> e.forward.target);
        unary("forward",algebra(),functors.algebra(),false,e -> e.forward);
        unary("backward",algebra(),functors.algebra(),false,e -> e.backward);
        unary("unit",algebra(),transformations.algebra(),false,e -> e.unit);
        unary("counit",algebra(),transformations.algebra(),false,e -> e.counit);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteEquivalence::equals);
        unary("identity-on",categories.algebra(),algebra(),false,FiniteEquivalence::identity);
        unary("from-functor",functors.algebra(),algebra(),true,FiniteEquivalence::fromFunctor);
        constant("empty",FiniteEquivalence.identity(FiniteCategory.discrete(FiniteSet.of())));
        law("The unit Id->G.F and counit F.G->Id are natural isomorphisms satisfying both triangle identities.");
        law("Witness construction uses finite fullness, faithfulness and essential surjectivity, with deterministic representative choices.");
        law("compose combines the supplied witnesses; equality includes the chosen quasi-inverse, unit and counit.");
    }
}
