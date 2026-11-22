package algebra.concrete;

import mathematics.numbers.*;

/** The rational Cayley division algebra, with its nonassociative product and exact subalgebras. */
public final class RationalOctonionAlgebra extends ConcreteAlgebra<RationalOctonion> {
    public RationalOctonionAlgebra(RationalField rationals,RationalComplexField complex,
                                   RationalImaginaryAlgebra imaginaries,RationalQuaternionAlgebra quaternions,
                                   RationalVectorFamily vectors,RationalMatrixFamily matrices,BooleanAlgebra truth) {
        super(carrier("O(Q)",RationalOctonion.class,"Rational octonions in the Cayley-Dickson basis 1,i,j,k,l,il,jl,kl",x -> true),rationals.unit());
        closed("add",false,RationalOctonion::add);
        closed("subtract",false,RationalOctonion::subtract);
        closed("multiply",false,RationalOctonion::multiply);
        closed("divide-right",true,RationalOctonion::divideRight);
        closed("divide-left",true,RationalOctonion::divideLeft);
        unary("negate",algebra(),algebra(),false,RationalOctonion::negate);
        unary("conjugate",algebra(),algebra(),false,RationalOctonion::conjugate);
        unary("inverse",algebra(),algebra(),true,RationalOctonion::inverse);
        unary("norm-squared",algebra(),rationals.algebra(),false,RationalOctonion::normSquared);
        unary("real-part",algebra(),rationals.algebra(),false,RationalOctonion::realPart);
        unary("to-rational",algebra(),rationals.algebra(),true,RationalOctonion::toRational);
        unary("to-boolean",algebra(),truth.algebra(),true,value -> BooleanAlgebra.decode(value.isZero(),value.isOne(),"O(Q)"));
        unaryFlat("components",algebra(),rationals.algebra(),false,RationalOctonion::components);
        unary("imaginary-part",algebra(),vectors.algebra(),false,RationalOctonion::imaginaryPart);
        unary("to-vector",algebra(),vectors.algebra(),true,RationalOctonion::toVector);
        unary("from-vector",vectors.algebra(),algebra(),true,RationalOctonion::fromVector);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalOctonion::scale);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalOctonion::equals);
        unary("embed-rational",rationals.algebra(),algebra(),false,RationalOctonion::scalar);
        unary("embed-complex",complex.algebra(),algebra(),false,RationalOctonion::fromComplex);
        unary("to-complex",algebra(),complex.algebra(),true,RationalOctonion::toComplex);
        unary("embed-imaginary",imaginaries.algebra(),algebra(),false,RationalOctonion::fromImaginary);
        unary("to-imaginary",algebra(),imaginaries.algebra(),true,RationalOctonion::toImaginary);
        unary("embed-quaternion",quaternions.algebra(),algebra(),false,RationalOctonion::fromQuaternion);
        unary("to-quaternion",algebra(),quaternions.algebra(),true,RationalOctonion::toQuaternion);
        unary("left-multiplication-matrix",algebra(),matrices.algebra(),false,RationalOctonion::leftMultiplicationMatrix);
        unaryFlat("su3-stabilizer-derivations",algebra(),matrices.algebra(),false,
                ignored -> mathematics.numbers.RationalOctonionDerivations.su3StabilizerCandidates());
        unary("from-left-multiplication-matrix",matrices.algebra(),algebra(),true,RationalOctonion::fromLeftMultiplicationMatrix);
        constant("zero",RationalOctonion.ZERO); constant("one",RationalOctonion.ONE);
        constant("i",RationalOctonion.I); constant("j",RationalOctonion.J); constant("k",RationalOctonion.K);
        constant("l",RationalOctonion.L); constant("il",RationalOctonion.IL);
        constant("jl",RationalOctonion.JL); constant("kl",RationalOctonion.KL);
        law("The Cayley-Dickson basis is 1,i,j,k,l,il,jl,kl with (a,b)(c,d)=(ac-conj(d)b, da+b conj(c)); multiplication is noncommutative and nonassociative but alternative.");
        law("Squared norm is the sum of the eight rational coordinate squares and is multiplicative; every nonzero rational octonion has inverse conjugate(x)/norm-squared(x).");
        law("Left and right quotients use the octonion inverse property. Products of three factors must retain parentheses because multiplication is nonassociative.");
        law("H(Q), Q(i), iQ and Q embed as the declared coordinate subalgebras; projections are partial and require all omitted coordinates to vanish.");
        law("Boolean decoding accepts only octonion zero and one. The regular representation is the 8 by 8 rational matrix of left multiplication in the declared basis.");
    }
}
