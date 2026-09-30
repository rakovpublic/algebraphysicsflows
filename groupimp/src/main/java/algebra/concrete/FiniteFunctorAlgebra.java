package algebra.concrete;

import mathematics.foundations.FiniteSet;
import mathematics.structures.*;

/** Native functor operations over the validated finite category representation. */
public final class FiniteFunctorAlgebra extends ConcreteAlgebra<FiniteFunctor> {
    public FiniteFunctorAlgebra(FiniteCategoryAlgebra categories,FiniteIntegerFunctionAlgebra functions,
                               IntegerRing integers,BooleanAlgebra truth) {
        super(carrier("FiniteFunctor",FiniteFunctor.class,"Covariant functors between finite category tables",f -> true),integers.unit());
        closed("compose",true,FiniteFunctor::compose);
        unary("inverse",algebra(),algebra(),true,FiniteFunctor::inverse);
        unary("opposite",algebra(),algebra(),false,FiniteFunctor::opposite);
        unary("source",algebra(),categories.algebra(),false,f -> f.source);
        unary("target",algebra(),categories.algebra(),false,f -> f.target);
        binary("map-object",algebra(),integers.algebra(),integers.algebra(),true,FiniteFunctor::mapObject);
        binary("map-arrow",algebra(),integers.algebra(),integers.algebra(),true,FiniteFunctor::mapArrow);
        unary("is-faithful",algebra(),truth.algebra(),false,FiniteFunctor::isFaithful);
        unary("is-full",algebra(),truth.algebra(),false,FiniteFunctor::isFull);
        unary("is-essentially-surjective",algebra(),truth.algebra(),false,FiniteFunctor::isEssentiallySurjective);
        unary("is-equivalence",algebra(),truth.algebra(),false,FiniteFunctor::isEquivalence);
        unary("is-isomorphism",algebra(),truth.algebra(),false,FiniteFunctor::isIsomorphism);
        unary("object-map",algebra(),functions.algebra(),false,f -> functions.member(f.source.objects(),f.target.objects(),f.objectMap()));
        unary("arrow-map",algebra(),functions.algebra(),false,f -> functions.member(f.source.arrowLabels(),f.target.arrowLabels(),f.arrowMap()));
        unaryFlat("object-images",algebra(),integers.algebra(),false,FiniteFunctor::objectImages);
        unaryFlat("arrow-images",algebra(),integers.algebra(),false,FiniteFunctor::arrowImages);
        flat("object-fiber",algebra(),integers.algebra(),integers.algebra(),true,FiniteFunctor::objectFiber);
        flat("arrow-fiber",algebra(),integers.algebra(),integers.algebra(),true,FiniteFunctor::arrowFiber);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteFunctor::equals);
        unary("identity-on",categories.algebra(),algebra(),false,FiniteFunctor::identity);
        unary("from-discrete-map",functions.algebra(),algebra(),false,FiniteFunctor::fromDiscreteMap);
        constant("empty",FiniteFunctor.identity(FiniteCategory.discrete(FiniteSet.of())));
        binary("constant-at",algebra(),integers.algebra(),algebra(),true,(f,c) -> FiniteFunctor.constant(f.source,f.target,c));
        unary("empty-diagram",categories.algebra(),algebra(),false,FiniteFunctor::emptyDiagram);
        law("Object and arrow maps preserve endpoints, identities and every composition in the finite source table.");
        law("compose is F(G(-)); the labelled middle categories must match exactly.");
        law("Faithfulness and fullness are checked separately on every hom set; equivalence also requires essential surjectivity.");
        law("Strict inverse requires bijective object and arrow maps; categorical equivalence alone is insufficient.");
    }
}
