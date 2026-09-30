package algebra.concrete;

import mathematics.linear.*;
import java.math.BigInteger;

/** Affine parametrizations of rational linear-system solutions, including the empty solution set. */
public final class RationalAffineSpaceAlgebra extends ConcreteAlgebra<RationalAffineSpace> {
    public RationalAffineSpaceAlgebra(RationalMatrixFamily matrices,RationalVectorFamily vectors,NaturalSemiring naturals,BooleanAlgebra truth) {
        super(carrier("Affine(Q)",RationalAffineSpace.class,"Canonical rational affine solution sets in explicit finite ambient dimensions",a -> true),matrices.unit());
        binary("solve",matrices.algebra(),vectors.algebra(),algebra(),true,RationalAffineSpace::solve);
        unary("particular",algebra(),vectors.algebra(),true,RationalAffineSpace::particular);
        unaryFlat("directions",algebra(),vectors.algebra(),true,RationalAffineSpace::directions);
        unary("dimension",algebra(),naturals.algebra(),true,a -> BigInteger.valueOf(a.dimension()));
        unary("ambient-dimension",algebra(),naturals.algebra(),false,a -> BigInteger.valueOf(a.ambientDimension()));
        unary("is-empty",algebra(),truth.algebra(),false,RationalAffineSpace::isEmpty);
        unary("is-unique",algebra(),truth.algebra(),false,RationalAffineSpace::isUnique);
        binary("contains",algebra(),vectors.algebra(),truth.algebra(),false,RationalAffineSpace::contains);
        binary("at",algebra(),vectors.algebra(),vectors.algebra(),true,RationalAffineSpace::at);
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalAffineSpace::equals);
        binary("least-squares",matrices.algebra(),vectors.algebra(),algebra(),true,RationalMatrix::leastSquares);
        binary("closest-point",algebra(),vectors.algebra(),vectors.algebra(),true,RationalAffineSpace::closestPoint);
        unary("minimum-norm",algebra(),vectors.algebra(),true,RationalAffineSpace::minimumNorm);
        law("A consistent system is represented by the free-zero particular solution and one canonical nullspace direction per free variable.");
        law("An inconsistent system yields an empty solution set with a retained ambient dimension, not a fabricated vector.");
        law("Every rational parameter tuple determines a solution; directions are a finite basis, not an enumeration of infinitely many points.");
        law("Equality compares canonical solution sets in the same ambient dimension, not the input equation presentation.");
        law("Least-squares returns all minimizers of the squared Euclidean residual and is nonempty for every dimension-compatible right-hand side.");
        law("Closest-point and minimum-norm are unique orthogonal projections onto nonempty affine sets in the standard Euclidean inner product.");
    }
}
