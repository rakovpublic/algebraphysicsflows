package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.linear.*;
import mathematics.numbers.Rational;


/** Square rational matrices; multiplication is generally noncommutative. */
public final class RationalMatrixAlgebra extends ConcreteAlgebra<RationalMatrix> {
    public final int dimension;
    public RationalMatrixAlgebra(RationalField rationals,RationalVectorSpace vectors) {
        super(carrier(vectors.dimension),rationals.unit());
        this.dimension=vectors.dimension;
        closed("add",false,RationalMatrix::add); closed("multiply",false,RationalMatrix::multiply);
        closed("subtract",false,(a,b) -> a.add(scale(b,Rational.of(-1))));
        unary("negate",algebra(),algebra(),false,m -> scale(m,Rational.of(-1)));
        unary("transpose",algebra(),algebra(),false,RationalMatrix::transpose);
        unary("inverse",algebra(),algebra(),true,RationalMatrix::inverse);
        unary("determinant",algebra(),rationals.algebra(),false,RationalMatrix::determinant);
        unary("trace",algebra(),rationals.algebra(),false,RationalMatrix::trace);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalMatrixAlgebra::scale);
        binary("apply",algebra(),vectors.algebra(),vectors.algebra(),false,RationalMatrix::multiply);
        binary("solve",algebra(),vectors.algebra(),vectors.algebra(),true,(matrix,rhs) -> matrix.inverse().multiply(rhs));
        constant("zero",scale(RationalMatrix.identity(dimension),Rational.ZERO));
        constant("one",RationalMatrix.identity(dimension));
        law("Square matrices form an associative unital Q-algebra; multiplication need not commute.");
        law("Inverse is defined precisely for nonsingular matrices.");
    }
    public RationalMatrixAlgebra(RationalField rationals,RationalVectorSpace vectors,NaturalSemiring naturals) {
        this(rationals,vectors);
        unary("rank",algebra(),naturals.algebra(),false,m -> java.math.BigInteger.valueOf(m.rank()));
    }
    private static Algebra<RationalMatrix> carrier(int n) {
        if(n<=0) throw MathFailure.invalid("Matrix dimension must be positive");
        return carrier("Mat"+n+"(Q)",RationalMatrix.class,"Square rational matrix algebra of dimension "+n,m -> m.rows()==n && m.columns()==n);
    }
    private static RationalMatrix scale(RationalMatrix m,Rational q) {
        Rational[][] values=new Rational[m.rows()][m.columns()];
        for(int i=0;i<m.rows();i++) for(int j=0;j<m.columns();j++) values[i][j]=m.get(i,j).multiply(q);
        return new RationalMatrix(values);
    }
}

