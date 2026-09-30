package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.linear.RationalMatrix;
import mathematics.linear.RationalVector;
import java.math.BigInteger;

/** Shared native registrations for the fixed square carrier and the shape-checked matrix family. */
final class MatrixSpectralOperations {
    private MatrixSpectralOperations() {}
    static void install(ConcreteAlgebra<RationalMatrix> owner,Algebra<RationalVector> vectors,
                        RationalField rationals,RationalPolynomialRing polynomials,NaturalSemiring naturals,
                        BooleanAlgebra truth,boolean partialShape) {
        Algebra<RationalMatrix> matrices=owner.algebra();
        owner.unary("characteristic-polynomial",matrices,polynomials.algebra(),partialShape,RationalMatrix::characteristicPolynomial);
        owner.unary("minimal-polynomial",matrices,polynomials.algebra(),partialShape,RationalMatrix::minimalPolynomial);
        owner.binary("evaluate-polynomial",matrices,polynomials.algebra(),matrices,partialShape,RationalMatrix::evaluatePolynomial);
        owner.binary("evaluate-at-matrix",polynomials.algebra(),matrices,matrices,partialShape,(p,m) -> m.evaluatePolynomial(p));
        owner.unaryFlat("rational-eigenvalues",matrices,rationals.algebra(),partialShape,RationalMatrix::rationalEigenvalues);
        owner.flat("eigenspace-basis",matrices,rationals.algebra(),vectors,partialShape,RationalMatrix::eigenspace);
        owner.flat("generalized-eigenspace-basis",matrices,rationals.algebra(),vectors,partialShape,RationalMatrix::generalizedEigenspace);
        owner.binary("eigenvalue-multiplicity",matrices,rationals.algebra(),naturals.algebra(),partialShape,
                (m,q) -> BigInteger.valueOf(m.eigenvalueMultiplicity(q)));
        owner.unary("is-diagonalizable-over-q",matrices,truth.algebra(),partialShape,RationalMatrix::isDiagonalizableOverRationals);
        owner.unaryFlat("diagonalize-over-q",matrices,matrices,true,RationalMatrix::diagonalizeOverRationals);
        owner.binary("pow",matrices,naturals.algebra(),matrices,partialShape,RationalMatrix::pow);
        owner.law("The characteristic polynomial is det(x*I-A). The monic minimal polynomial divides it, and both annihilate A.");
        owner.law("Rational eigenvalues are distinct and sorted; eigenspace bases use ascending free-column order. A missing rational eigenvalue has multiplicity zero and empty bases.");
        owner.law("Rational diagonalization emits P followed by D, with A*P=P*D and P invertible; exhaustion is never evidence of nonexistence.");
    }
}
