package algebra.concrete;

import mathematics.core.MathFailure;
import mathematics.linear.*;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;

/** Exact positive rectangular matrices with checked shape-sensitive operations. */
public final class RationalMatrixFamily extends ConcreteAlgebra<RationalMatrix> {
    public RationalMatrixFamily(RationalField rationals,RationalVectorFamily vectors,NaturalSemiring naturals,
                                BooleanAlgebra truth,RationalMatrixAlgebra fixed) {
        super(carrier("Mat(Q)",RationalMatrix.class,"Positive rectangular rational matrices with explicit shapes",m -> true),rationals.unit());
        closed("add",true,(a,b) -> same(a,b).add(b));
        closed("subtract",true,(a,b) -> same(a,b).add(b.scale(Rational.of(-1))));
        closed("multiply",true,(a,b) -> {
            if(a.columns()!=b.rows()) throw MathFailure.undefined("Matrix product requires matching inner dimensions"); return a.multiply(b);
        });
        unary("negate",algebra(),algebra(),false,m -> m.scale(Rational.of(-1)));
        unary("transpose",algebra(),algebra(),false,RationalMatrix::transpose);
        binary("scale",algebra(),rationals.algebra(),algebra(),false,RationalMatrix::scale);
        binary("apply",algebra(),vectors.algebra(),vectors.algebra(),true,(m,v) -> {
            if(m.columns()!=v.dimension()) throw MathFailure.undefined("Vector dimension must equal the number of matrix columns"); return m.multiply(v);
        });
        unary("rref",algebra(),algebra(),false,RationalMatrix::rref);
        unary("rank",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.rank()));
        unary("nullity",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.columns()-m.rank()));
        unaryFlat("pivot-columns",algebra(),naturals.algebra(),false,m -> {
            List<BigInteger> result=new ArrayList<>(); for(int pivot : m.pivotColumns()) result.add(BigInteger.valueOf(pivot)); return result;
        });
        unaryFlat("nullspace-basis",algebra(),vectors.algebra(),false,RationalMatrix::nullspace);
        unaryFlat("row-space-basis",algebra(),vectors.algebra(),false,RationalMatrix::rowSpace);
        unaryFlat("column-space-basis",algebra(),vectors.algebra(),false,RationalMatrix::columnSpace);
        unaryFlat("rows",algebra(),vectors.algebra(),false,RationalMatrix::rowVectors);
        unaryFlat("columns",algebra(),vectors.algebra(),false,RationalMatrix::columnVectors);
        unary("row-count",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.rows()));
        unary("column-count",algebra(),naturals.algebra(),false,m -> BigInteger.valueOf(m.columns()));
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalMatrix::equals);
        unary("from-fixed",fixed.algebra(),algebra(),false,m -> m);
        unary("to-fixed",algebra(),fixed.algebra(),true,m -> {
            if(m.rows()!=fixed.dimension || m.columns()!=fixed.dimension) throw MathFailure.undefined("Matrix shape does not match the fixed target carrier"); return m;
        });
        unary("zero-like",algebra(),algebra(),false,m -> m.scale(Rational.ZERO));
        unary("inverse",algebra(),algebra(),true,m -> square(m).inverse());
        unary("determinant",algebra(),rationals.algebra(),true,m -> square(m).determinant());
        unary("trace",algebra(),rationals.algebra(),true,m -> square(m).trace());
        unary("pseudoinverse",algebra(),algebra(),false,RationalMatrix::pseudoinverse);
        unary("column-projector",algebra(),algebra(),false,RationalMatrix::columnProjector);
        unary("row-projector",algebra(),algebra(),false,RationalMatrix::rowProjector);
        binary("project-column",algebra(),vectors.algebra(),vectors.algebra(),true,RationalMatrix::projectColumn);
        binary("least-squares-minimum-norm",algebra(),vectors.algebra(),vectors.algebra(),true,RationalMatrix::minimumNormLeastSquares);
        binary("least-squares-residual",algebra(),vectors.algebra(),vectors.algebra(),true,RationalMatrix::leastSquaresResidual);
        binary("least-squares-error",algebra(),vectors.algebra(),rationals.algebra(),true,RationalMatrix::leastSquaresError);
        law("This is a family of matrix spaces, with shape-checked addition and composition; it is not one ring across all shapes.");
        law("RREF and bases use exact rational arithmetic, with pivot and free columns in ascending zero-based order.");
        law("Rank plus nullity equals the number of columns; column-space bases retain original pivot columns.");
        law("The rational pseudoinverse satisfies all four Moore-Penrose equations in the standard Euclidean inner products, including rank-zero matrices.");
        law("Least-squares residual means b minus A*x; it is perpendicular to every column, and the minimum-norm minimizer is perpendicular to the kernel.");
    }
    private static RationalMatrix same(RationalMatrix a,RationalMatrix b) {
        if(a.rows()!=b.rows() || a.columns()!=b.columns()) throw MathFailure.undefined("Matrix shapes must agree"); return a;
    }
    private static RationalMatrix square(RationalMatrix m) {
        if(m.rows()!=m.columns()) throw MathFailure.undefined("This operation requires a square matrix"); return m;
    }
}
