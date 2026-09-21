package mathematics.algebras;

import mathematics.core.*;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import static mathematics.core.MathStatus.Membership.*;

/** Square rational matrices; multiplication is generally noncommutative. */
public final class RationalMatrixAlgebra extends ConcreteAlgebra<RationalMatrix> {
    public final int dimension;
    public RationalMatrixAlgebra(RationalField rationals,RationalVectorSpace vectors) {
        super(carrier(vectors.dimension),rationals.unit());
        this.dimension=vectors.dimension;
        closed("add",false,RationalMatrix::add); closed("multiply",false,RationalMatrix::multiply);
        closed("subtract",false,(a,b) -> a.add(scale(b,Rational.of(-1))));
        unary("negate",domain(),domain(),false,m -> scale(m,Rational.of(-1)));
        unary("transpose",domain(),domain(),false,RationalMatrix::transpose);
        unary("inverse",domain(),domain(),true,RationalMatrix::inverse);
        unary("determinant",domain(),rationals.domain(),false,RationalMatrix::determinant);
        unary("trace",domain(),rationals.domain(),false,RationalMatrix::trace);
        binary("scale",domain(),rationals.domain(),domain(),false,RationalMatrixAlgebra::scale);
        binary("apply",domain(),vectors.domain(),vectors.domain(),false,RationalMatrix::multiply);
        constant("zero",scale(RationalMatrix.identity(dimension),Rational.ZERO));
        constant("one",RationalMatrix.identity(dimension));
        law("Square matrices form an associative unital Q-algebra; multiplication need not commute.");
        law("Inverse is defined precisely for nonsingular matrices.");
    }
    private static Domain<RationalMatrix> carrier(int n) {
        if(n<=0) throw MathFailure.invalid("Matrix dimension must be positive");
        return new Domain<>(Metadata.of("Mat"+n+"(Q)","Square rational matrix algebra of dimension "+n),RationalMatrix.class,m -> m.rows()==n && m.columns()==n?MEMBER:NOT_MEMBER);
    }
    private static RationalMatrix scale(RationalMatrix m,Rational q) {
        Rational[][] values=new Rational[m.rows()][m.columns()];
        for(int i=0;i<m.rows();i++) for(int j=0;j<m.columns();j++) values[i][j]=m.get(i,j).multiply(q);
        return new RationalMatrix(values);
    }
}

