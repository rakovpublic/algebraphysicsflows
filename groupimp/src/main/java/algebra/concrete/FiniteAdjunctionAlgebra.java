package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.*;
import mathematics.structures.*;
import java.math.BigInteger;

/** Constructed finite adjoints and hom correspondences through the original operation interfaces. */
public final class FiniteAdjunctionAlgebra extends ConcreteAlgebra<FiniteAdjunction> {
    public FiniteAdjunctionAlgebra(FiniteCategoryAlgebra categories,FiniteFunctorAlgebra functors,
            FiniteNaturalTransformationAlgebra transformations,FiniteEquivalenceAlgebra equivalences,
            FiniteIntegerFunctionAlgebra functions,IntegerRing integers,BooleanAlgebra truth) {
        super(carrier("FiniteAdjunction",FiniteAdjunction.class,"Finite adjunctions with checked unit, counit and triangle identities",a -> true),categories.unit());
        Algebra<Pair<BigInteger,BigInteger>> pairs=categories.labelPairs;
        closed("compose",true,FiniteAdjunction::compose);
        unary("opposite",algebra(),algebra(),false,FiniteAdjunction::opposite);
        unary("source",algebra(),categories.algebra(),false,a -> a.left.source);
        unary("target",algebra(),categories.algebra(),false,a -> a.left.target);
        unary("left",algebra(),functors.algebra(),false,a -> a.left);
        unary("right",algebra(),functors.algebra(),false,a -> a.right);
        unary("unit",algebra(),transformations.algebra(),false,a -> a.unit);
        unary("counit",algebra(),transformations.algebra(),false,a -> a.counit);
        unary("is-equivalence",algebra(),truth.algebra(),false,FiniteAdjunction::isEquivalence);
        unary("to-equivalence",algebra(),equivalences.algebra(),true,FiniteAdjunction::toEquivalence);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteAdjunction::equals);
        unary("identity-on",categories.algebra(),algebra(),false,FiniteAdjunction::identity);
        unary("from-equivalence",equivalences.algebra(),algebra(),false,FiniteAdjunction::fromEquivalence);
        unary("from-left",functors.algebra(),algebra(),true,FiniteAdjunction::fromLeft);
        unary("from-right",functors.algebra(),algebra(),true,FiniteAdjunction::fromRight);
        binary("transpose",algebra(),pairs,integers.algebra(),true,(a,p) -> a.transpose(p.first,p.second));
        binary("untranspose",algebra(),pairs,integers.algebra(),true,(a,p) -> a.untranspose(p.first,p.second));
        binary("hom-map",algebra(),pairs,functions.algebra(),true,(a,p) -> functions.member(
                new FiniteSet<>(a.left.target.hom(a.left.mapObject(p.first),p.second)),
                new FiniteSet<>(a.left.source.hom(p.first,a.right.mapObject(p.second))),a.homMap(p.first,p.second)));
        constant("empty",FiniteAdjunction.identity(FiniteCategory.discrete(FiniteSet.of())));
        law("The unit Id->R.L and counit L.R->Id are natural transformations satisfying both triangle identities; invertibility is not required.");
        law("transpose and untranspose give inverse hom-set correspondences, with an explicit object label to remove ambiguity.");
        law("from-left and from-right construct adjoints by finite universal-arrow search, rejecting mathematical nonexistence.");
        law("compose applies the right operand's left functor first; opposite swaps the adjoint roles.");
    }
}
