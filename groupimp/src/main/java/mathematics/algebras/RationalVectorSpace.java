package mathematics.algebras;

import mathematics.core.*;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.util.*;
import static mathematics.core.MathStatus.Membership.*;

public final class RationalVectorSpace extends ConcreteAlgebra<RationalVector> {
    public final int dimension;
    public RationalVectorSpace(RationalField rationals,int dimension) {
        super(carrier(dimension),rationals.unit());
        this.dimension=dimension;
        closed("add",false,RationalVector::add);
        closed("subtract",false,(a,b) -> a.add(b.scale(Rational.of(-1))));
        unary("negate",domain(),domain(),false,v -> v.scale(Rational.of(-1)));
        binary("scale",domain(),rationals.domain(),domain(),false,RationalVector::scale);
        binary("scale-left",rationals.domain(),domain(),domain(),false,(q,v) -> v.scale(q));
        binary("dot",domain(),domain(),rationals.domain(),false,RationalVector::dot);
        flat("scale-flat",domain(),rationals.domain(),domain(),false,(v,q) -> Collections.singletonList(v.scale(q)));
        flat("scale-signs",domain(),rationals.domain(),domain(),false,(v,q) -> Arrays.asList(v.scale(q),v.scale(q.negate())));
        constant("zero",zero());
        law("Vector addition is an abelian group; scalar multiplication satisfies the vector-space laws over Q.");
        law("Dot product is symmetric and bilinear; dimension is fixed for this carrier.");
    }
    private static Domain<RationalVector> carrier(int dimension) {
        if(dimension<0) throw MathFailure.invalid("Vector dimension must be nonnegative");
        return new Domain<>(Metadata.of("Q^"+dimension,"Rational vector space of dimension "+dimension),RationalVector.class,v -> v.dimension()==dimension?MEMBER:NOT_MEMBER);
    }
    public RationalVector zero() {
        Rational[] values=new Rational[dimension]; Arrays.fill(values,Rational.ZERO);
        return new RationalVector(values);
    }
}

