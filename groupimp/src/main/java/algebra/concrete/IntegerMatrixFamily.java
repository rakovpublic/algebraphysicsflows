package algebra.concrete;

import mathematics.linear.IntegerMatrix;
import java.math.BigInteger;

/** Matrices between finite free Z-modules and exact integral linear-system operations. */
public final class IntegerMatrixFamily extends ConcreteAlgebra<IntegerMatrix> {
    public IntegerMatrixFamily(IntegerRing integers,IntegerVectorFamily vectors,NaturalSemiring naturals,
                               BooleanAlgebra truth,RationalMatrixFamily rationals,AbelianGroupTypeAlgebra groups) {
        super(carrier("Mat(Z)",IntegerMatrix.class,"Integer matrices with explicit nonnegative row and column dimensions",m -> true),integers.unit());
        closed("add",true,IntegerMatrix::add);
        closed("subtract",true,(a,b) -> a.add(b.scale(BigInteger.ONE.negate())));
        closed("multiply",true,IntegerMatrix::multiply);
        unary("negate",algebra(),algebra(),false,m -> m.scale(BigInteger.ONE.negate()));
        unary("transpose",algebra(),algebra(),false,IntegerMatrix::transpose);
        binary("scale",algebra(),integers.algebra(),algebra(),false,IntegerMatrix::scale);
        binary("apply",algebra(),vectors.algebra(),vectors.algebra(),true,IntegerMatrix::multiply);
        binary("equal",algebra(),algebra(),truth.algebra(),false,IntegerMatrix::equals);
        unary("row-count",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.rows()));
        unary("column-count",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.columns()));
        unaryFlat("rows",algebra(),vectors.algebra(),false,IntegerMatrix::rowVectors);
        unaryFlat("columns",algebra(),vectors.algebra(),false,IntegerMatrix::columnVectors);
        unary("zero-like",algebra(),algebra(),false,m -> IntegerMatrix.zero(m.rows(),m.columns()));
        unary("identity-on-domain",algebra(),algebra(),false,m -> IntegerMatrix.identity(m.columns()));
        unary("identity-on-codomain",algebra(),algebra(),false,m -> IntegerMatrix.identity(m.rows()));
        unaryFlat("smith-invariant-factors",algebra(),naturals.algebra(),false,IntegerMatrix::smithInvariantFactors);
        unary("smith-form",algebra(),algebra(),false,IntegerMatrix::smithForm);
        unaryFlat("smith-decomposition",algebra(),algebra(),false,IntegerMatrix::smithDecomposition);
        unaryFlat("kernel-basis",algebra(),vectors.algebra(),false,IntegerMatrix::kernelBasis);
        unaryFlat("image-basis",algebra(),vectors.algebra(),false,IntegerMatrix::imageBasis);
        unary("cokernel",algebra(),groups.algebra(),false,IntegerMatrix::cokernel);
        unary("rank",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.rank()));
        unary("nullity",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.columns()-m.rank()));
        binary("has-integer-solution",algebra(),vectors.algebra(),truth.algebra(),true,IntegerMatrix::hasIntegerSolution);
        binary("solve-particular",algebra(),vectors.algebra(),vectors.algebra(),true,IntegerMatrix::solveParticular);
        flat("solve-generators",algebra(),vectors.algebra(),vectors.algebra(),true,IntegerMatrix::solveGenerators);
        unary("inverse-unimodular",algebra(),algebra(),true,IntegerMatrix::inverseUnimodular);
        unary("to-rational",algebra(),rationals.algebra(),true,IntegerMatrix::toRational);
        unary("from-rational",rationals.algebra(),algebra(),true,IntegerMatrix::fromRational);
        law("Smith decomposition emits [U,D,V] with U*A*V=D and U,V unimodular; it retains the input shape, including empty dimensions.");
        law("Kernel and image lists are integral lattice bases, not rational bases or enumeration of their elements.");
        law("Solve-generators emits a particular solution followed by a kernel basis; all solutions are x0 plus arbitrary integer combinations of that basis.");
        law("Cokernel means Z^rows modulo the image of A, classified by free rank and torsion invariant factors.");
    }
}
