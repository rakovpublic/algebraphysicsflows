package algebra.concrete;

import mathematics.numbers.*;

/** The one-dimensional rational pure-imaginary subspace iQ, with exact scalar relations. */
public final class RationalImaginaryAlgebra extends ConcreteAlgebra<RationalImaginary> {
    public RationalImaginaryAlgebra(RationalField rationals,RationalComplexField complex,
                                    RationalQuaternionAlgebra quaternions,BooleanAlgebra truth) {
        super(carrier("iQ",RationalImaginary.class,"Pure imaginary rational numbers coefficient*i",x -> true),rationals.unit());
        closed("add",false,RationalImaginary::add);
        closed("subtract",false,RationalImaginary::subtract);
        unary("negate",algebra(),algebra(),false,RationalImaginary::negate);
        unary("conjugate",algebra(),algebra(),false,RationalImaginary::conjugate);
        binary("multiply",algebra(),algebra(),rationals.algebra(),false,RationalImaginary::multiply);
        binary("divide",algebra(),algebra(),rationals.algebra(),true,RationalImaginary::divide);
        unary("norm-squared",algebra(),rationals.algebra(),false,RationalImaginary::normSquared);
        unary("square",algebra(),rationals.algebra(),false,RationalImaginary::square);
        unary("coefficient",algebra(),rationals.algebra(),false,value -> value.coefficient);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalImaginary::scale);
        binary("scale-left",rationals.algebra(),algebra(),algebra(),false,(scalar,value) -> value.scale(scalar));
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalImaginary::equals);
        unary("to-boolean",algebra(),truth.algebra(),true,value -> BooleanAlgebra.decode(value.equals(RationalImaginary.ZERO),value.equals(RationalImaginary.I),"iQ"));
        unary("from-rational",rationals.algebra(),algebra(),false,RationalImaginary::new);
        unary("to-complex",algebra(),complex.algebra(),false,RationalImaginary::toComplex);
        unary("from-complex",complex.algebra(),algebra(),true,RationalImaginary::fromComplex);
        unary("to-quaternion",algebra(),quaternions.algebra(),false,RationalImaginary::toQuaternion);
        unary("from-quaternion",quaternions.algebra(),algebra(),true,RationalImaginary::fromQuaternion);
        constant("zero",RationalImaginary.ZERO);
        constant("i",RationalImaginary.I);
        law("iQ is the additive one-dimensional Q-vector space of pure imaginary numbers. Its product and quotient of two members are rational, because (a*i)*(b*i)=-a*b and (a*i)/(b*i)=a/b.");
        law("Complex and quaternion transfers retain the selected i axis exactly. Boolean decoding accepts only zero and the generator i.");
    }
}
