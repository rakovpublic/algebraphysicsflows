package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.*;
import java.math.BigInteger;
import java.util.*;

/** Total maps between declared finite integer sets, with partial typed composition. */
public final class FiniteIntegerFunctionAlgebra extends ConcreteAlgebra<FiniteFunction<BigInteger,BigInteger>> {
    private final Algebra<BigInteger> integers;
    public final Algebra<Pair<FiniteSet<BigInteger>,FiniteSet<BigInteger>>> boundaries;
    public FiniteIntegerFunctionAlgebra(IntegerRing integers,IntegerSetAlgebra sets,
            FiniteIntegerRelationAlgebra relations,BooleanAlgebra truth,NaturalSemiring naturals) {
        super(functionCarrier(integers.algebra()),integers.unit());
        this.integers=integers.algebra(); boundaries=boundaryCarrier(sets.algebra());
        closed("compose",true,FiniteFunction::compose);
        unary("inverse",algebra(),algebra(),true,FiniteFunction::inverse);
        unary("domain",algebra(),sets.algebra(),false,f -> f.domain);
        unary("codomain",algebra(),sets.algebra(),false,f -> f.codomain);
        unary("range",algebra(),sets.algebra(),false,FiniteFunction::range);
        unary("graph",algebra(),relations.algebra(),false,FiniteFunction::graph);
        unary("size",algebra(),naturals.algebra(),false,f -> BigInteger.valueOf(f.domain.size()));
        unary("is-injective",algebra(),truth.algebra(),false,FiniteFunction::isInjective);
        unary("is-surjective",algebra(),truth.algebra(),false,FiniteFunction::isSurjective);
        unary("is-bijective",algebra(),truth.algebra(),false,FiniteFunction::isBijective);
        binary("apply",algebra(),this.integers,this.integers,true,FiniteFunction::apply);
        binary("image",algebra(),sets.algebra(),sets.algebra(),true,FiniteFunction::image);
        binary("preimage",algebra(),sets.algebra(),sets.algebra(),true,FiniteFunction::preimage);
        binary("restrict",algebra(),sets.algebra(),algebra(),true,FiniteFunction::restrict);
        unaryFlat("values",algebra(),this.integers,false,FiniteFunction::values);
        flat("preimage-of",algebra(),this.integers,this.integers,true,FiniteFunction::preimageOf);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteFunction::equals);
        unary("identity-on",sets.algebra(),algebra(),false,set -> FiniteFunction.identity(this.integers,set));
        binary("from-relation",relations.algebra(),boundaries,algebra(),true,
                (relation,ends) -> FiniteFunction.fromRelation(relation,ends.first,ends.second));
        constant("empty",FiniteFunction.identity(this.integers,FiniteSet.of()));
        law("compose means f(g(x)); the declared middle finite sets and Algebra instances must match.");
        law("Identity and associativity hold for compatible maps; inverse is defined exactly for bijections.");
        law("Domain and codomain are part of equality; restriction retains the declared codomain.");
    }
    public FiniteFunction<BigInteger,BigInteger> member(FiniteSet<BigInteger> domain,
            FiniteSet<BigInteger> codomain,Map<BigInteger,BigInteger> values) {
        return new FiniteFunction<>(integers,integers,domain,codomain,values);
    }
    @SuppressWarnings("unchecked")
    private static Algebra<FiniteFunction<BigInteger,BigInteger>> functionCarrier(Algebra<BigInteger> z) {
        Class<FiniteFunction<BigInteger,BigInteger>> type=(Class<FiniteFunction<BigInteger,BigInteger>>)(Class<?>)FiniteFunction.class;
        return carrier("FiniteFunction(Z,Z)",type,"Total functions between finite integer sets",f -> f.source==z && f.target==z);
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<FiniteSet<BigInteger>,FiniteSet<BigInteger>>> boundaryCarrier(Algebra<FiniteSet<BigInteger>> sets) {
        Class<Pair<FiniteSet<BigInteger>,FiniteSet<BigInteger>>> type=(Class<Pair<FiniteSet<BigInteger>,FiniteSet<BigInteger>>>)(Class<?>)Pair.class;
        return carrier("FiniteSet(Z)xFiniteSet(Z).function",type,"Declared domain and codomain for a finite function",p ->
                sets.getParamClass().isInstance(p.first) && sets.getParamClass().isInstance(p.second)
                && sets.validate(p.first) && sets.validate(p.second));
    }
}
