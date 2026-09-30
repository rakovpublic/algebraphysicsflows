package algebra.concrete;

import mathematics.foundations.FiniteSet;
import mathematics.structures.*;

/** Natural transformations executed through the existing scalar, unary, mixed and flat interfaces. */
public final class FiniteNaturalTransformationAlgebra extends ConcreteAlgebra<FiniteNaturalTransformation> {
    public FiniteNaturalTransformationAlgebra(FiniteFunctorAlgebra functors,FiniteIntegerFunctionAlgebra functions,
                                            IntegerRing integers,BooleanAlgebra truth) {
        super(carrier("FiniteNaturalTransformation",FiniteNaturalTransformation.class,"Natural transformations with all finite naturality squares checked",t -> true),integers.unit());
        closed("compose",true,FiniteNaturalTransformation::compose);
        unary("inverse",algebra(),algebra(),true,FiniteNaturalTransformation::inverse);
        unary("opposite",algebra(),algebra(),false,FiniteNaturalTransformation::opposite);
        unary("source",algebra(),functors.algebra(),false,t -> t.source);
        unary("target",algebra(),functors.algebra(),false,t -> t.target);
        binary("component",algebra(),integers.algebra(),integers.algebra(),true,FiniteNaturalTransformation::component);
        unary("is-isomorphism",algebra(),truth.algebra(),false,FiniteNaturalTransformation::isIsomorphism);
        unary("component-map",algebra(),functions.algebra(),false,t ->
                functions.member(t.source.source.objects(),t.source.target.arrowLabels(),t.componentMap()));
        unaryFlat("components",algebra(),integers.algebra(),false,FiniteNaturalTransformation::componentValues);
        flat("component-fiber",algebra(),integers.algebra(),integers.algebra(),true,FiniteNaturalTransformation::componentFiber);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteNaturalTransformation::equals);
        unary("identity-on",functors.algebra(),algebra(),false,FiniteNaturalTransformation::identity);
        binary("precompose",algebra(),functors.algebra(),algebra(),true,FiniteNaturalTransformation::precompose);
        binary("postcompose",algebra(),functors.algebra(),algebra(),true,FiniteNaturalTransformation::postcompose);
        closed("horizontal",true,FiniteNaturalTransformation::horizontal);
        constant("empty",FiniteNaturalTransformation.identity(FiniteFunctor.identity(FiniteCategory.discrete(FiniteSet.of()))));
        law("Every component is typed F(x)->G(x), and F(f);eta_y = eta_x;G(f) for every source arrow.");
        law("compose is vertical composition with the right operand first; horizontal takes alpha then beta across adjacent categories.");
        law("A natural transformation is invertible exactly when every component is invertible.");
        law("Precomposition pulls components back along object maps; postcomposition maps them along arrow maps.");
    }
}
