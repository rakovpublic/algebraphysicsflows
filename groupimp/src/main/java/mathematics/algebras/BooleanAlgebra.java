package mathematics.algebras;

import mathematics.core.*;
import mathematics.foundations.Unit;
import static mathematics.core.MathStatus.Membership.MEMBER;

/** The two-element Boolean algebra, with exact truth-table operations. */
public final class BooleanAlgebra extends ConcreteAlgebra<Boolean> {
    public BooleanAlgebra(Domain<Unit> unit) {
        super(new Domain<>(Metadata.of("Boolean","Two-element Boolean algebra"),Boolean.class,x -> MEMBER),unit);
        closed("and",false,(a,b) -> a && b);
        closed("or",false,(a,b) -> a || b);
        closed("xor",false,(a,b) -> a ^ b);
        closed("implies",false,(a,b) -> !a || b);
        closed("equal",false,(a,b) -> a.equals(b));
        unary("not",domain(),domain(),false,a -> !a);
        constant("zero",false); constant("one",true);
        law("Meet and join form a bounded distributive lattice; not supplies complements.");
    }
}

