package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.*;
import java.math.BigInteger;
import java.util.*;

/** Finite-support relations on the registered integer Algebra. */
public final class FiniteIntegerRelationAlgebra extends ConcreteAlgebra<FiniteRelation<BigInteger,BigInteger>> {
    public FiniteIntegerRelationAlgebra(IntegerRing integers,IntegerSetAlgebra sets,BooleanAlgebra truth,NaturalSemiring naturals) {
        super(relationCarrier(integers.algebra()),integers.unit());
        Algebra<BigInteger> z=integers.algebra();
        closed("union",false,FiniteRelation::union);
        closed("intersection",false,FiniteRelation::intersection);
        closed("compose",false,FiniteRelation::andThen);
        unary("inverse",algebra(),algebra(),false,FiniteRelation::inverse);
        unary("transitive-closure",algebra(),algebra(),false,FiniteIntegerRelationAlgebra::transitiveClosure);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteRelation::equals);
        binary("subrelation",algebra(),algebra(),truth.algebra(),false,FiniteRelation::subrelationOf);
        unary("domain",algebra(),sets.algebra(),false,FiniteRelation::domain);
        unary("range",algebra(),sets.algebra(),false,FiniteRelation::range);
        unary("cardinality",algebra(),naturals.algebra(),false,r -> BigInteger.valueOf(r.pairs.size()));
        binary("contains",algebra(),pairCarrier(z),truth.algebra(),false,(r,pair) -> r.relates(pair.first,pair.second));
        binary("image",algebra(),sets.algebra(),sets.algebra(),false,FiniteRelation::image);
        binary("preimage",algebra(),sets.algebra(),sets.algebra(),false,FiniteRelation::preimage);
        binary("is-function-on",algebra(),sets.algebra(),truth.algebra(),false,FiniteRelation::isTotalFunctionOn);
        constant("empty",new FiniteRelation<>(z,z,FiniteSet.of()));
        unary("identity-on",sets.algebra(),algebra(),false,set ->
                new FiniteRelation<>(z,z,set.image(value -> new Pair<>(value,value))));
        law("compose applies the first relation and then the second; relational composition is associative.");
        law("Transitive closure includes positive-length paths; reflexivity is not added unless implied by a cycle.");
        law("Function totality quantifies only over the explicit finite carrier, with no relation pairs outside it.");
    }
    private static FiniteRelation<BigInteger,BigInteger> transitiveClosure(FiniteRelation<BigInteger,BigInteger> input) {
        FiniteRelation<BigInteger,BigInteger> result=input;
        while(true) {
            FiniteRelation<BigInteger,BigInteger> expanded=result.union(result.andThen(result));
            if(expanded.equals(result)) return result;
            result=expanded;
        }
    }
    @SuppressWarnings("unchecked")
    private static Algebra<FiniteRelation<BigInteger,BigInteger>> relationCarrier(Algebra<BigInteger> z) {
        Class<FiniteRelation<BigInteger,BigInteger>> type=(Class<FiniteRelation<BigInteger,BigInteger>>)(Class<?>)FiniteRelation.class;
        return carrier("FiniteRelation(Z,Z)",type,"Finite-support integer relations",relation -> relation.source==z && relation.target==z);
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<BigInteger,BigInteger>> pairCarrier(Algebra<BigInteger> z) {
        Class<Pair<BigInteger,BigInteger>> type=(Class<Pair<BigInteger,BigInteger>>)(Class<?>)Pair.class;
        return carrier("ZxZ.relation",type,"Integer relation pair",pair ->
                z.getParamClass().isInstance(pair.first) && z.getParamClass().isInstance(pair.second)
                && z.validate(pair.first) && z.validate(pair.second));
    }
}
