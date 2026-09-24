package algebra.concrete;

import mathematics.linear.IntegerVector;
import java.math.BigInteger;

/** Finite free Z-modules, with checked dimension-sensitive operations. */
public final class IntegerVectorFamily extends ConcreteAlgebra<IntegerVector> {
    public IntegerVectorFamily(IntegerRing integers,NaturalSemiring naturals,BooleanAlgebra truth,RationalVectorFamily rationals) {
        super(carrier("Vec(Z)",IntegerVector.class,"Integer coordinate vectors with retained dimension",v -> true),integers.unit());
        closed("add",true,IntegerVector::add);
        closed("subtract",true,(a,b) -> a.add(b.scale(BigInteger.ONE.negate())));
        unary("negate",algebra(),algebra(),false,v -> v.scale(BigInteger.ONE.negate()));
        binary("scale",algebra(),integers.algebra(),algebra(),false,IntegerVector::scale);
        binary("dot",algebra(),algebra(),integers.algebra(),true,IntegerVector::dot);
        unary("dimension",algebra(),naturals.algebra(),false,v -> BigInteger.valueOf(v.dimension()));
        unaryFlat("entries",algebra(),integers.algebra(),false,IntegerVector::entries);
        unary("zero-like",algebra(),algebra(),false,v -> IntegerVector.zero(v.dimension()));
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegerVector::equals);
        unary("to-rational",algebra(),rationals.algebra(),false,IntegerVector::toRational);
        unary("from-rational",rationals.algebra(),algebra(),true,IntegerVector::fromRational);
        constant("empty",new IntegerVector());
        law("Each fixed dimension is a free Z-module; addition and dot product require matching dimensions.");
    }
}
