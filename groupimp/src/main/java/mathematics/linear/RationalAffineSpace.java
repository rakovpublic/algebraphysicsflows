package mathematics.linear;

import mathematics.core.MathFailure;
import mathematics.numbers.Rational;
import java.io.Serializable;
import java.util.*;

/** A canonical exact solution set to a finite rational linear system, including empty sets. */
public final class RationalAffineSpace implements Serializable {
    private static final long serialVersionUID=1L;
    private final int ambientDimension;
    private final RationalVector particular;
    private final List<RationalVector> directions;
    private final List<Integer> freeColumns;

    private RationalAffineSpace(int dimension,RationalRowReduction reduction) {
        ambientDimension=dimension;
        particular=reduction.consistent?reduction.particular():null;
        directions=reduction.consistent?reduction.nullspace():Collections.emptyList();
        freeColumns=reduction.consistent?reduction.freeColumns():Collections.emptyList();
    }
    public static RationalAffineSpace solve(RationalMatrix matrix,RationalVector rhs) {
        Objects.requireNonNull(matrix); Objects.requireNonNull(rhs);
        return new RationalAffineSpace(matrix.columns(),new RationalRowReduction(matrix,rhs));
    }
    public int ambientDimension() { return ambientDimension; }
    public boolean isEmpty() { return particular==null; }
    public boolean isUnique() { return !isEmpty() && directions.isEmpty(); }
    private void nonempty() { if(isEmpty()) throw MathFailure.undefined("The affine solution set is empty"); }
    public int dimension() { nonempty(); return directions.size(); }
    public RationalVector particular() { nonempty(); return particular; }
    public List<RationalVector> directions() { nonempty(); return directions; }
    public RationalVector at(RationalVector parameters) {
        nonempty();
        if(parameters.dimension()!=directions.size()) throw MathFailure.undefined("One rational parameter is required per free variable");
        RationalVector result=particular;
        for(int i=0;i<directions.size();i++) result=result.add(directions.get(i).scale(parameters.get(i)));
        return result;
    }
    public boolean contains(RationalVector point) {
        if(isEmpty() || point.dimension()!=ambientDimension) return false;
        Rational[] parameters=new Rational[freeColumns.size()];
        for(int i=0;i<parameters.length;i++) parameters[i]=point.get(freeColumns.get(i));
        return at(new RationalVector(parameters)).equals(point);
    }
    @Override public boolean equals(Object other) {
        if(!(other instanceof RationalAffineSpace)) return false;
        RationalAffineSpace a=(RationalAffineSpace)other;
        return ambientDimension==a.ambientDimension && Objects.equals(particular,a.particular) && directions.equals(a.directions);
    }
    @Override public int hashCode() { return Objects.hash(ambientDimension,particular,directions); }
    @Override public String toString() { return isEmpty()?"EmptyAffine(Q^"+ambientDimension+")":"Affine(particular="+particular+", directions="+directions+")"; }
}
