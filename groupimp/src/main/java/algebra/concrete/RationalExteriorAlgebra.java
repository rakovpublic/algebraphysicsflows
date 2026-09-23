package algebra.concrete;

import mathematics.core.MathFailure;
import mathematics.linear.RationalExterior;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;

/** Exact exterior algebras of standard oriented rational coordinate spaces, retaining ambient dimension. */
public final class RationalExteriorAlgebra extends ConcreteAlgebra<RationalExterior> {
    public RationalExteriorAlgebra(RationalField rationals,RationalVectorFamily vectors,RationalMatrixFamily matrices,
                                   NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("Exterior(Q)",RationalExterior.class,"Sparse exterior elements in standard oriented rational coordinate spaces",a -> true),rationals.unit());
        closed("add",true,RationalExterior::add);
        closed("subtract",true,(a,b) -> a.add(b.scale(Rational.of(-1))));
        closed("wedge",true,RationalExterior::wedge);
        unary("negate",algebra(),algebra(),false,a -> a.scale(Rational.of(-1)));
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalExterior::scale);
        unary("grade-involution",algebra(),algebra(),false,RationalExterior::gradeInvolution);
        unary("reverse",algebra(),algebra(),false,RationalExterior::reverse);
        unary("hodge-star",algebra(),algebra(),false,RationalExterior::hodgeStar);
        unary("ambient-dimension",algebra(),naturals.algebra(),false,a -> BigInteger.valueOf(a.dimension()));
        unary("term-count",algebra(),naturals.algebra(),false,a -> BigInteger.valueOf(a.coefficients().size()));
        unaryFlat("degrees",algebra(),naturals.algebra(),false,a -> {
            List<BigInteger> result=new ArrayList<>(); for(int degree : a.degrees()) result.add(BigInteger.valueOf(degree)); return result;
        });
        unaryFlat("terms",algebra(),algebra(),false,RationalExterior::terms);
        unaryFlat("coefficients",algebra(),rationals.algebra(),false,a -> new ArrayList<>(a.coefficients().values()));
        unaryFlat("basis-masks",algebra(),naturals.algebra(),false,a -> {
            List<BigInteger> result=new ArrayList<>(); for(int mask : a.coefficients().keySet()) result.add(BigInteger.valueOf(mask)); return result;
        });
        binary("grade",algebra(),naturals.algebra(),algebra(),false,(a,k) -> k.compareTo(BigInteger.valueOf(a.dimension()))>0?RationalExterior.zero(a.dimension()):a.grade(k.intValue()));
        binary("interior",algebra(),vectors.algebra(),algebra(),true,RationalExterior::interior);
        binary("dot",algebra(),algebra(),rationals.algebra(),true,RationalExterior::dot);
        unary("norm-squared",algebra(),rationals.algebra(),false,a -> a.dot(a));
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalExterior::equals);
        unary("is-zero",algebra(),truth.algebra(),false,RationalExterior::isZero);
        unary("scalar-part",algebra(),rationals.algebra(),false,RationalExterior::scalarPart);
        unary("to-scalar",algebra(),rationals.algebra(),true,RationalExterior::toScalar);
        unary("to-vector",algebra(),vectors.algebra(),true,RationalExterior::toVector);
        unary("from-vector",vectors.algebra(),algebra(),false,RationalExterior::fromVector);
        binary("apply",matrices.algebra(),algebra(),algebra(),true,(m,a) -> a.map(m));
        unary("zero-in",naturals.algebra(),algebra(),false,n -> RationalExterior.zero(dimension(n)));
        unary("one-in",naturals.algebra(),algebra(),false,n -> RationalExterior.scalar(dimension(n),Rational.ONE));
        unary("volume-in",naturals.algebra(),algebra(),false,n -> RationalExterior.volume(dimension(n)));
        law("Within a fixed ambient dimension, wedge is associative and homogeneous elements of degrees p,q commute with sign (-1)^(p*q).");
        law("Interior insertion is a degree-minus-one antiderivation using the standard coordinate dot product.");
        law("Hodge star uses the positive ordered orthonormal basis; star squared on degree k is (-1)^(k*(n-k)).");
        law("A matrix induces a covariant algebra map on multivectors, preserving scalars and wedge products; this is not pullback of differential forms.");
    }
    private static int dimension(BigInteger value) {
        if(value.compareTo(BigInteger.valueOf(RationalExterior.MAX_DIMENSION))>0)
            throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Exterior dimension exceeds the coordinate representation limit");
        return value.intValue();
    }
}
