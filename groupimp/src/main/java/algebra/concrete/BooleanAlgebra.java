package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.calculus.Polynomial;
import mathematics.calculus.RationalFunction;
import mathematics.foundations.Unit;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalTensor;
import mathematics.numbers.Rational;
import mathematics.numbers.RationalComplex;
import mathematics.numbers.RationalQuaternion;
import java.math.BigInteger;
import java.util.*;


/** The two-element Boolean algebra, with exact truth-table operations. */
public final class BooleanAlgebra extends ConcreteAlgebra<Boolean> {
    public BooleanAlgebra(Algebra<Unit> unit) {
        super(carrier("Boolean",Boolean.class,"Two-element Boolean algebra",x -> true),unit);
        closed("and",false,(a,b) -> a && b);
        closed("or",false,(a,b) -> a || b);
        closed("xor",false,(a,b) -> a ^ b);
        closed("implies",false,(a,b) -> !a || b);
        closed("equal",false,(a,b) -> a.equals(b));
        closed("nand",false,(a,b) -> !(a && b));
        closed("nor",false,(a,b) -> !(a || b));
        closed("equivalent",false,(a,b) -> a.equals(b));
        closed("difference",false,(a,b) -> a && !b);
        closed("less-or-equal",false,(a,b) -> !a || b);
        closed("greater-or-equal",false,(a,b) -> !b || a);
        unary("not",algebra(),algebra(),false,a -> !a);
        constant("zero",false); constant("one",true);
        law("Meet and join form a bounded distributive lattice; not supplies complements. Xor is symmetric difference, and implication is the Boolean order.");
    }

    /**
     * Install exact 0/1 encodings after the scalar carriers have been constructed.
     * The conversion back to Boolean is deliberately partial: values outside {0,1}
     * are not silently assigned an arbitrary truth value.
     */
    public void connectScalars(NaturalSemiring naturals,IntegerRing integers,RationalField rationals,
                               RationalComplexField complex,RationalQuaternionAlgebra quaternions,
                               RationalPolynomialRing polynomials,RationalFunctionField functions,
                               RationalTensorAlgebra tensors,RationalMatrixAlgebra matrices,
                               ResidueRing residues,List<PrimeField> primeFields) {
        Objects.requireNonNull(primeFields);
        unary("to-natural",algebra(),naturals.algebra(),false,value -> value?BigInteger.ONE:BigInteger.ZERO);
        unary("to-integer",algebra(),integers.algebra(),false,value -> value?BigInteger.ONE:BigInteger.ZERO);
        unary("to-rational",algebra(),rationals.algebra(),false,value -> value?Rational.ONE:Rational.ZERO);
        unary("to-complex",algebra(),complex.algebra(),false,value -> new RationalComplex(value?Rational.ONE:Rational.ZERO,Rational.ZERO));
        unary("to-quaternion",algebra(),quaternions.algebra(),false,value -> RationalQuaternion.scalar(value?Rational.ONE:Rational.ZERO));
        unary("to-polynomial",algebra(),polynomials.algebra(),false,value -> value?Polynomial.ONE:Polynomial.ZERO);
        unary("to-rational-function",algebra(),functions.algebra(),false,value -> value?RationalFunction.ONE:RationalFunction.ZERO);
        unary("to-tensor",algebra(),tensors.algebra(),false,value -> RationalTensor.scalar(value?Rational.ONE:Rational.ZERO));
        unary("to-matrix",algebra(),matrices.algebra(),false,value -> RationalMatrix.identity(matrices.dimension).scale(value?Rational.ONE:Rational.ZERO));
        unary("to-residue-"+residues.modulus,algebra(),residues.algebra(),false,value -> residues.member(value?BigInteger.ONE:BigInteger.ZERO));
        for(PrimeField field : primeFields)
            unary("to-prime-"+field.prime,algebra(),field.algebra(),false,value -> field.member(value?1:0));
        law("The scalar transitions encode false as 0 and true as 1. Their inverse decoders accept only those two distinguished values; these encodings are not asserted to preserve ordinary addition in characteristic zero.");
    }

    /** Decode the chosen 0 and 1, rejecting every other value instead of using truthiness. */
    public static boolean decode(boolean zero,boolean one,String carrier) {
        if(zero) return false;
        if(one) return true;
        throw MathFailure.undefined("Only 0 or 1 in "+carrier+" converts to Boolean");
    }
}

