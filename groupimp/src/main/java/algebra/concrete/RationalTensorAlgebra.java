package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.foundations.Pair;
import mathematics.linear.RationalTensor;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;

/** Finite rational coordinate tensors with explicit shapes and the standard coordinate contraction. */
public final class RationalTensorAlgebra extends ConcreteAlgebra<RationalTensor> {
    public final Algebra<Pair<BigInteger,BigInteger>> axisPairs;
    public RationalTensorAlgebra(RationalField rationals,RationalVectorFamily vectors,RationalMatrixFamily matrices,
                                 NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("Tensor(Q)",RationalTensor.class,"Exact dense rational coordinate tensors with explicit ordered axes",t -> true),rationals.unit());
        axisPairs=pairCarrier(naturals.algebra());
        closed("add",true,RationalTensor::add);
        closed("subtract",true,(a,b) -> a.add(b.scale(Rational.of(-1))));
        closed("hadamard",true,RationalTensor::hadamard);
        closed("tensor-product",false,RationalTensor::tensorProduct);
        unary("negate",algebra(),algebra(),false,t -> t.scale(Rational.of(-1)));
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalTensor::scale);
        unary("order",algebra(),naturals.algebra(),false,t -> BigInteger.valueOf(t.order()));
        unary("size",algebra(),naturals.algebra(),false,t -> BigInteger.valueOf(t.size()));
        unaryFlat("shape",algebra(),naturals.algebra(),false,t -> {
            List<BigInteger> result=new ArrayList<>(); for(int dimension : t.shape()) result.add(BigInteger.valueOf(dimension)); return result;
        });
        unaryFlat("entries",algebra(),rationals.algebra(),false,RationalTensor::entries);
        unary("norm-squared",algebra(),rationals.algebra(),false,t -> t.dot(t));
        binary("dot",algebra(),algebra(),rationals.algebra(),true,RationalTensor::dot);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalTensor::equals);
        binary("swap-axes",algebra(),axisPairs,algebra(),true,(t,p) -> t.swapAxes(axis(t,p.first),axis(t,p.second)));
        binary("contract",algebra(),axisPairs,algebra(),true,(t,p) -> t.contract(axis(t,p.first),axis(t,p.second)));
        unary("from-scalar",rationals.algebra(),algebra(),false,RationalTensor::scalar);
        unary("to-scalar",algebra(),rationals.algebra(),true,RationalTensor::scalar);
        unary("from-vector",vectors.algebra(),algebra(),false,RationalTensor::fromVector);
        unary("to-vector",algebra(),vectors.algebra(),true,RationalTensor::toVector);
        unary("from-matrix",matrices.algebra(),algebra(),false,RationalTensor::fromMatrix);
        unary("to-matrix",algebra(),matrices.algebra(),true,RationalTensor::toMatrix);
        unary("zero-like",algebra(),algebra(),false,t -> t.scale(Rational.ZERO));
        constant("one",RationalTensor.scalar(Rational.ONE));
        law("Tensor product concatenates shapes and is associative; the scalar tensor one is its identity.");
        law("Contraction sums along matching coordinates of two distinct equal-sized axes in standard coordinate bases, retaining other axes in order.");
        law("Scalars have order zero and one entry. Zero-sized axes are retained, and their contraction is an empty sum of zero.");
        law("This is a family of coordinate tensor spaces, not an implementation of arbitrary tensor bundles or variance-aware abstract tensors.");
    }
    private static int axis(RationalTensor tensor,BigInteger value) {
        if(value.signum()<0 || value.compareTo(BigInteger.valueOf(tensor.order()))>=0) throw MathFailure.undefined("Tensor axis is outside its order");
        return value.intValue();
    }
    @SuppressWarnings("unchecked")
    private static Algebra<Pair<BigInteger,BigInteger>> pairCarrier(Algebra<BigInteger> naturals) {
        Class<Pair<BigInteger,BigInteger>> type=(Class<Pair<BigInteger,BigInteger>>)(Class<?>)Pair.class;
        return carrier("NxN.tensor-axes",type,"Ordered pair of nonnegative tensor axis indices",p ->
                naturals.getParamClass().isInstance(p.first) && naturals.getParamClass().isInstance(p.second)
                && naturals.validate(p.first) && naturals.validate(p.second));
    }
}
