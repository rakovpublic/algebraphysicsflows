package mathematics.catalog;

import mathematics.core.*;
import mathematics.numbers.*;
import mathematics.linear.*;
import mathematics.calculus.*;
import mathematics.foundations.*;
import mathematics.topology.FiniteSimplicialComplex;
import java.math.BigInteger;
import java.util.*;
import static mathematics.core.MathStatus.Computation.*;
import static mathematics.core.MathStatus.Membership.*;

/** Executable examples of all unary/binary result-domain forms; exact scopes are named. */
public final class StandardMathematics {
    public final OperationCatalog catalog=new OperationCatalog();
    public final Domain<Polynomial> polynomials=new Domain<>(Metadata.of("Q[x]","Univariate rational polynomials"),Polynomial.class,p -> MEMBER);
    public final Domain<RationalVector> vectors2=new Domain<>(Metadata.of("Q^2","Rational vectors of dimension two"),RationalVector.class,v -> v.dimension()==2?MEMBER:NOT_MEMBER);
    public final Domain<RationalMatrix> matrices2=new Domain<>(Metadata.of("Mat2(Q)","Two by two rational matrices"),RationalMatrix.class,m -> m.rows()==2 && m.columns()==2?MEMBER:NOT_MEMBER);
    public final Domain<FiniteSimplicialComplex> finiteComplexes=new Domain<>(Metadata.of("FiniteComplex","Finite abstract simplicial complexes"),FiniteSimplicialComplex.class,c -> MEMBER);
    public final UnaryOperation<BigInteger,BigInteger> naturalToInteger=new UnaryOperation<>(Metadata.of("N-to-Z","Exact inclusion"),NumberDomains.NATURALS,NumberDomains.INTEGERS,EXACT,false,n -> n);
    public final UnaryOperation<BigInteger,Rational> integerToRational=new UnaryOperation<>(Metadata.of("Z-to-Q","Exact inclusion"),NumberDomains.INTEGERS,NumberDomains.RATIONALS,EXACT,false,Rational::of);
    public final UnaryOperation<Rational,SymbolicReal> rationalToRealExpression=new UnaryOperation<>(Metadata.of("Q-to-RealExpression","Symbolic exact rational embedding"),NumberDomains.RATIONALS,NumberDomains.REAL_EXPRESSIONS,SYMBOLIC,false,SymbolicReal::rational);
    public final UnaryOperation<Rational,RationalComplex> rationalToComplex=new UnaryOperation<>(Metadata.of("Q-to-Qi","Exact inclusion into rational complex numbers"),NumberDomains.RATIONALS,NumberDomains.RATIONAL_COMPLEX,EXACT,false,q -> new RationalComplex(q,Rational.ZERO));
    public final BinaryOperation<Rational,Rational,Rational> add=new BinaryOperation<>(Metadata.of("rational-add","Exact rational addition"),NumberDomains.RATIONALS,NumberDomains.RATIONALS,NumberDomains.RATIONALS,EXACT,false,Rational::add);
    public final BinaryOperation<Rational,Rational,Rational> divide=new BinaryOperation<>(Metadata.of("rational-divide","Partial exact division"),NumberDomains.RATIONALS,NumberDomains.RATIONALS,NumberDomains.RATIONALS,EXACT,true,Rational::divide);
    public final BinaryOperation<BigInteger,BigInteger,Boolean> greater=new BinaryOperation<>(Metadata.of("integer-greater","Integer comparison"),NumberDomains.INTEGERS,NumberDomains.INTEGERS,NumberDomains.BOOLEAN,EXACT,false,(a,b) -> a.compareTo(b)>0);
    public final BinaryOperation<RationalVector,RationalVector,Rational> dot=new BinaryOperation<>(Metadata.of("dot-Q2","Rational dot product"),vectors2,vectors2,NumberDomains.RATIONALS,EXACT,false,RationalVector::dot);
    public final BinaryOperation<Rational,RationalVector,RationalVector> scale=new BinaryOperation<>(Metadata.of("scale-Q2","Scalar times vector returns vector"),NumberDomains.RATIONALS,vectors2,vectors2,EXACT,false,(scalar,vector) -> vector.scale(scalar));
    public final BinaryOperation<RationalVector,Rational,RationalVector> scaleRight=new BinaryOperation<>(Metadata.of("scale-right-Q2","Vector transformed by scalar, not a projection"),vectors2,NumberDomains.RATIONALS,vectors2,EXACT,false,RationalVector::scale);
    public final UnaryOperation<RationalMatrix,Rational> determinant=new UnaryOperation<>(Metadata.of("det-Mat2Q","Exact determinant"),matrices2,NumberDomains.RATIONALS,EXACT,false,RationalMatrix::determinant);
    public final BinaryOperation<RationalMatrix,RationalVector,RationalVector> matrixVector=new BinaryOperation<>(Metadata.of("matrix-vector-Q2","Exact matrix/vector multiplication"),matrices2,vectors2,vectors2,EXACT,false,RationalMatrix::multiply);
    public final BinaryOperation<Polynomial,Rational,Rational> evaluatePolynomial=new BinaryOperation<>(Metadata.of("evaluate-polynomial","Polynomial evaluation"),polynomials,NumberDomains.RATIONALS,NumberDomains.RATIONALS,EXACT,false,Polynomial::evaluate);
    public final UnaryOperation<Polynomial,Polynomial> derivative=new UnaryOperation<>(Metadata.of("polynomial-derivative","Derivative within Q[x]; not arbitrary C1 callbacks"),polynomials,polynomials,EXACT,false,Polynomial::derivative);
    public final UnaryOperation<Unit,Rational> zero=new UnaryOperation<>(Metadata.of("rational-zero","Constant via Unit -> Q"),FoundationDomains.UNIT,NumberDomains.RATIONALS,EXACT,false,u -> Rational.ZERO);
    public final FlatOperation<FiniteSimplicialComplex,Unit,BigInteger> bettiNumbers=new FlatOperation<>(Metadata.of("betti-F2","Unreduced Betti numbers over F2, ordered by degree"),finiteComplexes,FoundationDomains.UNIT,NumberDomains.NATURALS,EXACT,false,(complex,unit) -> {
        List<BigInteger> result=new ArrayList<>();
        for(int k=0;k<=complex.dimension();k++) result.add(BigInteger.valueOf(complex.bettiNumber(k)));
        return result;
    });
    public StandardMathematics() {
        for(Domain<?> domain : Arrays.asList(NumberDomains.NATURALS,NumberDomains.INTEGERS,NumberDomains.RATIONALS,
                NumberDomains.RATIONAL_COMPLEX,NumberDomains.REAL_EXPRESSIONS,NumberDomains.BOOLEAN,
                FoundationDomains.UNIT,polynomials,vectors2,matrices2,finiteComplexes)) catalog.addDomain(domain);
        for(DescribedOperation operation : Arrays.asList(naturalToInteger,integerToRational,rationalToRealExpression,
                rationalToComplex,add,divide,greater,dot,scale,scaleRight,determinant,matrixVector,evaluatePolynomial,
                derivative,zero,bettiNumbers)) catalog.addOperation(operation);
    }
}
