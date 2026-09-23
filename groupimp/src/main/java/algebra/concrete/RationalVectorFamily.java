package algebra.concrete;

import mathematics.core.MathFailure;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;

/** Finite rational vectors of varying dimensions; dimension-sensitive operations are partial. */
public final class RationalVectorFamily extends ConcreteAlgebra<RationalVector> {
    public RationalVectorFamily(RationalField rationals,NaturalSemiring naturals,RationalVectorSpace fixed) {
        super(carrier("Vec(Q)",RationalVector.class,"Finite rational vectors with explicit varying dimensions",v -> true),rationals.unit());
        closed("add",true,(a,b) -> same(a,b).add(b));
        closed("subtract",true,(a,b) -> same(a,b).add(b.scale(Rational.of(-1))));
        unary("negate",algebra(),algebra(),false,v -> v.scale(Rational.of(-1)));
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalVector::scale);
        binary("dot",algebra(),algebra(),rationals.algebra(),true,(a,b) -> same(a,b).dot(b));
        unary("dimension",algebra(),naturals.algebra(),false,v -> BigInteger.valueOf(v.dimension()));
        unaryFlat("entries",algebra(),rationals.algebra(),false,v -> {
            List<Rational> entries=new ArrayList<>(); for(int i=0;i<v.dimension();i++) entries.add(v.get(i)); return entries;
        });
        unary("zero-like",algebra(),algebra(),false,v -> v.scale(Rational.ZERO));
        unary("from-fixed",fixed.algebra(),algebra(),false,v -> v);
        unary("to-fixed",algebra(),fixed.algebra(),true,v -> {
            if(v.dimension()!=fixed.dimension) throw MathFailure.undefined("Vector dimension does not match the fixed target carrier"); return v;
        });
        constant("empty",new RationalVector());
        law("This carrier is a family of finite-dimensional vector spaces; addition and dot product require equal dimensions.");
        law("Scalar multiplication and zero-like preserve dimension; the empty vector has dimension zero.");
    }
    private static RationalVector same(RationalVector a,RationalVector b) {
        if(a.dimension()!=b.dimension()) throw MathFailure.undefined("Vector dimensions must agree"); return a;
    }
}
