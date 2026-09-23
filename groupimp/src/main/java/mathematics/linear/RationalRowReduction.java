package mathematics.linear;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.util.*;

/** Exact Gauss-Jordan reduction, carrying an optional right-hand side through the same row operations. */
final class RationalRowReduction {
    final RationalMatrix matrix;
    final List<Integer> pivots;
    final RationalVector rhs;
    final boolean consistent;

    RationalRowReduction(RationalMatrix source,RationalVector rightHandSide) {
        int rows=source.rows(),columns=source.columns(),width=columns+(rightHandSide==null?0:1);
        if(rightHandSide!=null && rightHandSide.dimension()!=rows) throw MathFailure.undefined("Right-hand side dimension must equal the number of matrix rows");
        Rational[][] entries=new Rational[rows][width];
        for(int r=0;r<rows;r++) {
            for(int c=0;c<columns;c++) entries[r][c]=source.get(r,c);
            if(rightHandSide!=null) entries[r][columns]=rightHandSide.get(r);
        }
        List<Integer> pivotColumns=new ArrayList<>(); int pivotRow=0;
        for(int c=0;c<columns && pivotRow<rows;c++) {
            int selected=pivotRow; while(selected<rows && entries[selected][c].signum()==0) selected++;
            if(selected==rows) continue;
            Rational[] swap=entries[pivotRow]; entries[pivotRow]=entries[selected]; entries[selected]=swap;
            Rational pivot=entries[pivotRow][c];
            for(int j=0;j<width;j++) entries[pivotRow][j]=entries[pivotRow][j].divide(pivot);
            for(int r=0;r<rows;r++) if(r!=pivotRow && entries[r][c].signum()!=0) {
                Rational factor=entries[r][c];
                for(int j=0;j<width;j++) entries[r][j]=entries[r][j].subtract(factor.multiply(entries[pivotRow][j]));
            }
            pivotColumns.add(c); pivotRow++;
        }
        Rational[][] coefficients=new Rational[rows][columns]; Rational[] right=new Rational[rows]; boolean consistent=true;
        for(int r=0;r<rows;r++) {
            System.arraycopy(entries[r],0,coefficients[r],0,columns);
            if(rightHandSide!=null) {
                right[r]=entries[r][columns];
                if(r>=pivotRow && right[r].signum()!=0) consistent=false;
            }
        }
        this.matrix=new RationalMatrix(coefficients); this.pivots=Collections.unmodifiableList(pivotColumns);
        this.rhs=rightHandSide==null?null:new RationalVector(right); this.consistent=consistent;
    }
    List<Integer> freeColumns() {
        List<Integer> result=new ArrayList<>();
        for(int c=0;c<matrix.columns();c++) if(!pivots.contains(c)) result.add(c);
        return Collections.unmodifiableList(result);
    }
    List<RationalVector> nullspace() {
        List<RationalVector> result=new ArrayList<>();
        for(int free : freeColumns()) {
            Rational[] direction=new Rational[matrix.columns()]; Arrays.fill(direction,Rational.ZERO); direction[free]=Rational.ONE;
            for(int r=0;r<pivots.size();r++) direction[pivots.get(r)]=matrix.get(r,free).negate();
            result.add(new RationalVector(direction));
        }
        return Collections.unmodifiableList(result);
    }
    RationalVector particular() {
        if(rhs==null || !consistent) throw MathFailure.undefined("No particular solution is available");
        Rational[] values=new Rational[matrix.columns()]; Arrays.fill(values,Rational.ZERO);
        for(int r=0;r<pivots.size();r++) values[pivots.get(r)]=rhs.get(r);
        return new RationalVector(values);
    }
}
