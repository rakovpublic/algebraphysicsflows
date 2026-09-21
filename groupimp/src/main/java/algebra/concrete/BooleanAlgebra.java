package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.foundations.Unit;


/** The two-element Boolean algebra, with exact truth-table operations. */
public final class BooleanAlgebra extends ConcreteAlgebra<Boolean> {
    public BooleanAlgebra(Algebra<Unit> unit) {
        super(carrier("Boolean",Boolean.class,"Two-element Boolean algebra",x -> true),unit);
        closed("and",false,(a,b) -> a && b);
        closed("or",false,(a,b) -> a || b);
        closed("xor",false,(a,b) -> a ^ b);
        closed("implies",false,(a,b) -> !a || b);
        closed("equal",false,(a,b) -> a.equals(b));
        unary("not",algebra(),algebra(),false,a -> !a);
        constant("zero",false); constant("one",true);
        law("Meet and join form a bounded distributive lattice; not supplies complements.");
    }
}

