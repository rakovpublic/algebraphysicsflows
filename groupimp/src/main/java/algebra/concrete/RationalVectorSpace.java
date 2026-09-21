package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.util.*;


public final class RationalVectorSpace extends ConcreteAlgebra<RationalVector> {
    public final int dimension;
    public RationalVectorSpace(RationalField rationals,int dimension) {
        super(carrier(dimension),rationals.unit());
        this.dimension=dimension;
        closed("add",false,RationalVector::add);
        closed("subtract",false,(a,b) -> a.add(b.scale(Rational.of(-1))));
        unary("negate",algebra(),algebra(),false,v -> v.scale(Rational.of(-1)));
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalVector::scale);
        binary("scale-left",rationals.algebra(),algebra(),algebra(),false,(q,v) -> v.scale(q));
        flat("scale-left-flat",rationals.algebra(),algebra(),algebra(),false,(q,v) -> Collections.singletonList(v.scale(q)));
        binary("dot",algebra(),algebra(),rationals.algebra(),false,RationalVector::dot);
        flat("scale-flat",algebra(),rationals.algebra(),algebra(),false,(v,q) -> Collections.singletonList(v.scale(q)));
        flat("scale-signs",algebra(),rationals.algebra(),algebra(),false,(v,q) -> Arrays.asList(v.scale(q),v.scale(q.negate())));
        constant("zero",zero());
        law("Vector addition is an abelian group; scalar multiplication satisfies the vector-space laws over Q.");
        law("Dot product is symmetric and bilinear; dimension is fixed for this carrier.");
    }
    private static Algebra<RationalVector> carrier(int dimension) {
        if(dimension<0) throw MathFailure.invalid("Vector dimension must be nonnegative");
        return carrier("Q^"+dimension,RationalVector.class,"Rational vector space of dimension "+dimension,v -> v.dimension()==dimension);
    }
    public RationalVector zero() {
        Rational[] values=new Rational[dimension]; Arrays.fill(values,Rational.ZERO);
        return new RationalVector(values);
    }
}

