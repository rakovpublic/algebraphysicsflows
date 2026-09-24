package mathematics.topology;

import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.linear.IntegerSmithNormalForm;
import mathematics.linear.IntegerMatrix;
import mathematics.structures.AbelianGroupType;
import java.math.BigInteger;
import java.util.*;

/** Oriented integral boundary matrices and unreduced homology isomorphism types. */
final class IntegralSimplicialHomology {
    private final FiniteSimplicialComplex complex;
    private final IntegerSmithNormalForm.Computation smith=new IntegerSmithNormalForm.Computation();
    private final Map<Integer,List<FiniteSet<Integer>>> bases=new HashMap<>();
    private final Map<Integer,List<BigInteger>> boundaries=new HashMap<>();
    IntegralSimplicialHomology(FiniteSimplicialComplex complex) { this.complex=complex; }
    static List<Integer> vertices(FiniteSet<Integer> simplex) {
        List<Integer> result=new ArrayList<>(simplex.members()); Collections.sort(result); return result;
    }
    List<FiniteSet<Integer>> basis(int degree) {
        if(degree<0 || degree>complex.dimension()) return Collections.emptyList();
        if(!bases.containsKey(degree)) {
            List<FiniteSet<Integer>> result=new ArrayList<>(complex.simplices(degree));
            if(result.size()>IntegerSmithNormalForm.MAX_DIMENSION)
                throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Integral homology allows at most 256 simplices in each required degree");
            result.sort((a,b) -> {
                List<Integer> first=vertices(a),second=vertices(b);
                for(int i=0;i<first.size();i++) { int order=first.get(i).compareTo(second.get(i)); if(order!=0) return order; }
                return 0;
            });
            bases.put(degree,result);
        }
        return bases.get(degree);
    }
    List<BigInteger> boundary(int degree) {
        if(degree<=0 || degree>complex.dimension()) return Collections.emptyList();
        if(!boundaries.containsKey(degree)) boundaries.put(degree,smith.invariantFactors(boundaryMatrix(degree)));
        return boundaries.get(degree);
    }
    IntegerMatrix boundaryMatrix(int degree) {
        List<FiniteSet<Integer>> rows=basis(degree-1),columns=basis(degree);
        BigInteger[][] matrix=new BigInteger[rows.size()][columns.size()];
        for(BigInteger[] row : matrix) Arrays.fill(row,BigInteger.ZERO);
        Map<FiniteSet<Integer>,Integer> positions=new HashMap<>(); for(int i=0;i<rows.size();i++) positions.put(rows.get(i),i);
        for(int c=0;degree>0 && c<columns.size();c++) {
            List<Integer> simplex=vertices(columns.get(c));
            for(int i=0;i<simplex.size();i++) {
                List<Integer> face=new ArrayList<>(simplex); face.remove(i);
                matrix[positions.get(new FiniteSet<>(face))][c]=(i&1)==0?BigInteger.ONE:BigInteger.ONE.negate();
            }
        }
        return new IntegerMatrix(rows.size(),columns.size(),matrix);
    }
    AbelianGroupType group(int degree) {
        if(degree<0) throw MathFailure.undefined("Homology degree must be nonnegative");
        if(degree>complex.dimension()) return AbelianGroupType.ZERO;
        List<BigInteger> incoming=boundary(degree+1),outgoing=boundary(degree);
        int freeRank=basis(degree).size()-incoming.size()-outgoing.size();
        // ker(d_k) is saturated in the free group C_k. Therefore torsion(coker d_(k+1))
        // equals torsion(H_k); the complementary quotient im(d_k) is free.
        return new AbelianGroupType(BigInteger.valueOf(freeRank),incoming);
    }
    List<AbelianGroupType> groups() {
        List<AbelianGroupType> result=new ArrayList<>();
        for(int degree=0;degree<=complex.dimension();degree++) result.add(group(degree));
        return Collections.unmodifiableList(result);
    }
}
