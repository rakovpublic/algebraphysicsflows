package algebra.concrete;

import mathematics.numbers.*;
import mathematics.linear.RationalVector;
import mathematics.linear.RationalMatrix;

/** Native operations and exact structural maps for the Z2-graded Dixon crossed product. */
public final class RationalDixonZ2Algebra extends ConcreteAlgebra<RationalDixonZ2> {
    public RationalDixonZ2Algebra(RationalField rationals,RationalComplexField complex,
                                  RationalImaginaryAlgebra imaginaries,RationalQuaternionAlgebra quaternions,
                                  RationalOctonionAlgebra octonions,RationalVectorFamily vectors,
                                  RationalMatrixFamily matrices,BooleanAlgebra truth) {
        super(carrier("DixonZ2(Q)",RationalDixonZ2.class,
                "(Q(i) tensor H(Q) tensor O(Q)) semidirect C2 in exact 64-coordinate crossed-product form",x -> true),rationals.unit());
        closed("add",false,RationalDixonZ2::add);
        closed("subtract",false,RationalDixonZ2::subtract);
        closed("multiply",false,RationalDixonZ2::multiply);
        binary("commutator",algebra(),algebra(),algebra(),false,RationalDixonZ2::commutator);
        unary("negate",algebra(),algebra(),false,RationalDixonZ2::negate);
        unary("grading-involution",algebra(),algebra(),false,RationalDixonZ2::gradingInvolution);
        unary("even-part",algebra(),algebra(),false,RationalDixonZ2::evenPart);
        unary("odd-part",algebra(),algebra(),false,RationalDixonZ2::oddPart);
        unary("idempotent",algebra(),truth.algebra(),false,RationalDixonZ2::isIdempotent);
        constant("group-idempotent-plus",RationalDixonZ2.groupIdempotent(true));
        constant("group-idempotent-minus",RationalDixonZ2.groupIdempotent(false));
        constant("witt-element",RationalDixonZ2.wittElement());
        constant("witt-idempotent-plus",RationalDixonZ2.wittIdempotent(true));
        constant("witt-idempotent-minus",RationalDixonZ2.wittIdempotent(false));
        unary("group-idempotent-plus-left-projector",rationals.unit(),matrices.algebra(),false,
                ignored -> RationalDixonZ2.groupIdempotent(true).leftMultiplicationMatrix());
        unary("group-idempotent-minus-left-projector",rationals.unit(),matrices.algebra(),false,
                ignored -> RationalDixonZ2.groupIdempotent(false).leftMultiplicationMatrix());
        unary("witt-idempotent-plus-left-projector",rationals.unit(),matrices.algebra(),false,
                ignored -> RationalDixonZ2.wittIdempotent(true).leftMultiplicationMatrix());
        unary("witt-idempotent-minus-left-projector",rationals.unit(),matrices.algebra(),false,
                ignored -> RationalDixonZ2.wittIdempotent(false).leftMultiplicationMatrix());
        unary("u1-complex-phase-generator",rationals.unit(),matrices.algebra(),false,ignored -> RationalDixonZ2.complexPhaseGeneratorMatrix());
        unaryFlat("components",algebra(),complex.algebra(),false,RationalDixonZ2::components);
        unary("to-vector",algebra(),vectors.algebra(),false,RationalDixonZ2::toVector);
        unary("from-vector",vectors.algebra(),algebra(),true,RationalDixonZ2::fromVector);
        unary("left-multiplication-matrix",algebra(),matrices.algebra(),false,RationalDixonZ2::leftMultiplicationMatrix);
        unary("from-left-multiplication-matrix",matrices.algebra(),algebra(),true,RationalDixonZ2::fromLeftMultiplicationMatrix);
        unary("equal-zero",algebra(),truth.algebra(),false,RationalDixonZ2::isZero);
        unary("to-boolean",algebra(),truth.algebra(),true,RationalDixonZ2::toBoolean);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalDixonZ2::equals);
        binary("not-equal",algebra(),algebra(),truth.algebra(),false,(a,b) -> !a.equals(b));

        unary("embed-rational",rationals.algebra(),algebra(),false,RationalDixonZ2::fromRational);
        unary("to-rational",algebra(),rationals.algebra(),true,RationalDixonZ2::toRational);
        unary("embed-complex",complex.algebra(),algebra(),false,RationalDixonZ2::fromComplex);
        unary("to-complex",algebra(),complex.algebra(),true,RationalDixonZ2::toComplex);
        unary("embed-imaginary",imaginaries.algebra(),algebra(),false,RationalDixonZ2::fromImaginary);
        unary("to-imaginary",algebra(),imaginaries.algebra(),true,RationalDixonZ2::toImaginary);
        unary("embed-quaternion",quaternions.algebra(),algebra(),false,RationalDixonZ2::fromQuaternion);
        unary("to-quaternion",algebra(),quaternions.algebra(),true,RationalDixonZ2::toQuaternion);
        unary("embed-octonion",octonions.algebra(),algebra(),false,RationalDixonZ2::fromOctonion);
        unary("to-octonion",algebra(),octonions.algebra(),true,RationalDixonZ2::toOctonion);
        constant("zero",RationalDixonZ2.ZERO); constant("one",RationalDixonZ2.ONE);
        constant("group-generator",RationalDixonZ2.U);
        law("The coefficient algebra is Q(i) tensor_Q H(Q) tensor_Q O(Q), with 32 coordinates over Q(i), using the ordered factor basis.");
        law("The C2 action is alpha(q tensor o)=q tensor (o0+o1*i+o2*j+o3*k-o4*l-o5*il-o6*jl-o7*kl); alpha is an involutive automorphism.");
        law("Elements are a+b*u with u^2=1 and u*a=alpha(a)*u; multiplication is (a+b*u)(c+d*u)=(ac+b*alpha(d))+(ad+b*alpha(c))*u.");
        law("The crossed product retains octonion nonassociativity. No associative-division-ring or division-algebra claim is made.");
        law("The group and Witt idempotents are exact complementary idempotents. They are candidate projectors; primitive/minimal left ideals are not asserted for this nonassociative carrier.");
        law("Commutators are native bilinear operations. A computed bracket alone does not certify a Lie subalgebra or identify physical gauge generators.");
        law("The registered U(1)-type phase generator is left multiplication by the central Q(i) unit on Vec(Q)^128; it squares to minus the identity matrix.");
        law("Q, Q(i), iQ, H(Q), O(Q), and Boolean 0/1 have exact coordinate embeddings. Reverse maps are partial coordinate projections.");
        law("There is no canonical order on this noncommutative nonassociative algebra, so order comparisons are intentionally not registered.");
    }
}
