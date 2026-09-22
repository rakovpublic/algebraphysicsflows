package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.*;
import mathematics.structures.FiniteCategory;
import java.math.BigInteger;

/** Explicit finite categories with checked identity, typing and associativity tables. */
public final class FiniteCategoryAlgebra extends ConcreteAlgebra<FiniteCategory> {
    public FiniteCategoryAlgebra(IntegerRing integers,IntegerSetAlgebra sets,
            FiniteIntegerRelationAlgebra relations,BooleanAlgebra truth,NaturalSemiring naturals) {
        super(carrier("FiniteCategory",FiniteCategory.class,"Finite labelled categories with validated tables",c -> true),integers.unit());
        Algebra<BigInteger> z=integers.algebra(); Algebra<Pair<BigInteger,BigInteger>> pairs=pairCarrier(z);
        unary("opposite",algebra(),algebra(),false,FiniteCategory::opposite);
        unary("objects",algebra(),sets.algebra(),false,FiniteCategory::objects);
        unary("arrows",algebra(),sets.algebra(),false,FiniteCategory::arrowLabels);
        unary("object-count",algebra(),naturals.algebra(),false,c -> BigInteger.valueOf(c.objects().size()));
        unary("arrow-count",algebra(),naturals.algebra(),false,c -> BigInteger.valueOf(c.arrows().size()));
        unary("is-groupoid",algebra(),truth.algebra(),false,FiniteCategory::isGroupoid);
        unary("is-thin",algebra(),truth.algebra(),false,FiniteCategory::isThin);
        binary("source",algebra(),z,z,true,FiniteCategory::source);
        binary("target",algebra(),z,z,true,FiniteCategory::target);
        binary("identity",algebra(),z,z,true,FiniteCategory::identity);
        binary("compose",algebra(),pairs,z,true,(c,p) -> c.compose(p.first,p.second));
        flat("hom",algebra(),pairs,z,true,(c,p) -> c.hom(p.first,p.second));
        flat("inverse-of",algebra(),z,z,true,FiniteCategory::inverseOf);
        binary("is-isomorphism",algebra(),z,truth.algebra(),true,FiniteCategory::isIsomorphism);
        flat("endomorphisms",algebra(),z,z,true,(c,p) -> c.hom(p,p));
        unaryFlat("isomorphisms",algebra(),z,false,FiniteCategory::isomorphisms);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteCategory::equals);
        unary("discrete-on",sets.algebra(),algebra(),false,FiniteCategory::discrete);
        unary("from-preorder",relations.algebra(),algebra(),true,FiniteCategory::fromPreorder);
        unary("underlying-relation",algebra(),relations.algebra(),false,c ->
                new FiniteRelation<>(z,z,new FiniteSet<>(c.arrows().values())));
        unaryFlat("initial-objects",algebra(),z,false,FiniteCategory::initialObjects);
        unaryFlat("terminal-objects",algebra(),z,false,FiniteCategory::terminalObjects);
        constant("empty",FiniteCategory.discrete(FiniteSet.of()));
        law("Construction checks source/target typing, complete composition, both identity laws and every composable associativity triple.");
        law("compose takes (f,g) in path order: first f, then g; opposite reverses arrows and composition.");
        law("Equality compares labelled tables, not categorical equivalence or isomorphism of presentations.");
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<BigInteger,BigInteger>> pairCarrier(Algebra<BigInteger> z) {
        Class<Pair<BigInteger,BigInteger>> type=(Class<Pair<BigInteger,BigInteger>>)(Class<?>)Pair.class;
        return carrier("ZxZ.category",type,"Ordered object or arrow labels",p ->
                z.getParamClass().isInstance(p.first) && z.getParamClass().isInstance(p.second)
                && z.validate(p.first) && z.validate(p.second));
    }
}
