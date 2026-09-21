package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.foundations.FiniteSet;
import java.math.BigInteger;
import java.util.*;

/** Algebra of finite sets with canonical member equality; no infinite-set enumeration. */
public final class FiniteSetAlgebra<T> extends ConcreteAlgebra<FiniteSet<T>> {
    public final Algebra<T> elements;
    public FiniteSetAlgebra(String name,Algebra<T> elements,BooleanAlgebra truth,NaturalSemiring naturals) {
        super(setCarrier(name,elements),truth.unit());
        this.elements=elements;
        closed("union",false,FiniteSet::union);
        closed("intersection",false,FiniteSet::intersection);
        closed("difference",false,FiniteSet::difference);
        closed("symmetric-difference",false,(a,b) -> a.difference(b).union(b.difference(a)));
        binary("subset",algebra(),algebra(),truth.algebra(),false,FiniteSet::subsetOf);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteSet::equals);
        binary("contains",algebra(),elements,truth.algebra(),false,FiniteSet::contains);
        binary("insert",algebra(),elements,algebra(),false,(set,value) -> set.union(FiniteSet.of(value)));
        binary("remove",algebra(),elements,algebra(),false,(set,value) -> set.difference(FiniteSet.of(value)));
        unary("cardinality",algebra(),naturals.algebra(),false,set -> BigInteger.valueOf(set.size()));
        unaryFlat("elements",algebra(),elements,false,set -> new ArrayList<>(set.members()));
        unaryFlat("subsets",algebra(),algebra(),false,set -> new ArrayList<>(set.powerSet().members()));
        closed("complement-in",true,(set,universe) -> {
            if(!set.subsetOf(universe)) throw mathematics.core.MathFailure.undefined("Relative complement requires a subset of the supplied universe");
            return universe.difference(set);
        });
        constant("empty",FiniteSet.of());
        law("Union and intersection are associative, commutative and idempotent and satisfy absorption and distributivity.");
        law("Complement is relative to the supplied finite universe and requires subset membership.");
        law("Flat enumeration has finite ordered-list semantics; mathematical set equality ignores that enumeration order.");
    }
    @SuppressWarnings("unchecked")
    private static <T> Algebra<FiniteSet<T>> setCarrier(String name,Algebra<T> elements) {
        Class<FiniteSet<T>> type=(Class<FiniteSet<T>>)(Class<?>)FiniteSet.class;
        return carrier(name,type,"Finite sets over "+elements.getAlgebraName(),set -> {
            for(T value : set.members()) if(!elements.getParamClass().isInstance(value) || !elements.validate(value)) return false;
            return true;
        });
    }
}
