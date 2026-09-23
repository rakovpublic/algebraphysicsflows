package algebra.concrete;

import mathematics.numbers.*;

/** Hamilton's noncommutative rational division algebra with exact rational rotation actions. */
public final class RationalQuaternionAlgebra extends ConcreteAlgebra<RationalQuaternion> {
    public RationalQuaternionAlgebra(RationalField rationals,RationalComplexField complex,RationalVectorFamily vectors,
                                     RationalMatrixFamily matrices,BooleanAlgebra truth) {
        super(carrier("H(Q)",RationalQuaternion.class,"Hamilton quaternions with exact rational components",q -> true),rationals.unit());
        closed("add",false,RationalQuaternion::add);
        closed("subtract",false,RationalQuaternion::subtract);
        closed("multiply",false,RationalQuaternion::multiply);
        closed("divide-right",true,RationalQuaternion::divideRight);
        closed("divide-left",true,RationalQuaternion::divideLeft);
        unary("negate",algebra(),algebra(),false,RationalQuaternion::negate);
        unary("conjugate",algebra(),algebra(),false,RationalQuaternion::conjugate);
        unary("inverse",algebra(),algebra(),true,RationalQuaternion::inverse);
        unary("norm-squared",algebra(),rationals.algebra(),false,RationalQuaternion::normSquared);
        unary("real-part",algebra(),rationals.algebra(),false,q -> q.real);
        unary("imaginary-part",algebra(),vectors.algebra(),false,RationalQuaternion::imaginaryPart);
        unaryFlat("components",algebra(),rationals.algebra(),false,RationalQuaternion::components);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalQuaternion::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalQuaternion::equals);
        binary("same-rotation",algebra(),algebra(),truth.algebra(),true,RationalQuaternion::sameRotation);
        unary("embed-rational",rationals.algebra(),algebra(),false,RationalQuaternion::scalar);
        unary("to-rational",algebra(),rationals.algebra(),true,RationalQuaternion::toRational);
        unary("embed-complex",complex.algebra(),algebra(),false,RationalQuaternion::fromComplex);
        unary("to-complex",algebra(),complex.algebra(),true,RationalQuaternion::toComplex);
        unary("from-vector",vectors.algebra(),algebra(),true,RationalQuaternion::fromVector);
        unary("to-vector",algebra(),vectors.algebra(),true,RationalQuaternion::toVector);
        binary("rotate",algebra(),vectors.algebra(),vectors.algebra(),true,RationalQuaternion::rotate);
        unary("to-rotation-matrix",algebra(),matrices.algebra(),true,RationalQuaternion::toRotationMatrix);
        unary("from-rotation-matrix",matrices.algebra(),algebra(),true,RationalQuaternion::fromRotationMatrix);
        constant("zero",RationalQuaternion.ZERO); constant("one",RationalQuaternion.ONE);
        constant("i",RationalQuaternion.I); constant("j",RationalQuaternion.J); constant("k",RationalQuaternion.K);
        law("i*i=j*j=k*k=i*j*k=-1; multiplication is associative and noncommutative, and every nonzero member is invertible.");
        law("Squared norm is a nonnegative rational and multiplicative; conjugation reverses multiplication order.");
        law("Rotation uses q*(0,v)*q^-1 on column vectors; every nonzero rational scalar multiple gives the same rotation.");
        law("Rotation matrix inversion selects a rational projective quaternion with first nonzero component one, not a unit quaternion.");
    }
}
