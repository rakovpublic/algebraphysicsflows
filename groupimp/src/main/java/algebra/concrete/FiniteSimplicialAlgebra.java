package algebra.concrete;

import mathematics.topology.FiniteSimplicialComplex;
import mathematics.structures.AbelianGroupType;
import java.math.BigInteger;
import java.util.*;

/** Finite labelled abstract simplicial complexes with unreduced coefficient-specific homology. */
public final class FiniteSimplicialAlgebra extends ConcreteAlgebra<FiniteSimplicialComplex> {
    public FiniteSimplicialAlgebra(BooleanAlgebra truth,NaturalSemiring naturals,IntegerRing integers) {
        super(carrier("FiniteComplex",FiniteSimplicialComplex.class,
                "Finite simplicial complexes with integer vertex labels",complex -> true),truth.unit());
        closed("union",false,FiniteSimplicialComplex::union);
        closed("intersection",false,FiniteSimplicialComplex::intersection);
        binary("equal",algebra(),algebra(),truth.algebra(),false,FiniteSimplicialComplex::equals);
        binary("subcomplex",algebra(),algebra(),truth.algebra(),false,FiniteSimplicialComplex::subcomplexOf);
        unary("dimension",algebra(),integers.algebra(),false,c -> BigInteger.valueOf(c.dimension()));
        unary("euler-characteristic",algebra(),integers.algebra(),false,FiniteSimplicialComplex::eulerCharacteristic);
        unary("vertex-count",algebra(),naturals.algebra(),false,c -> BigInteger.valueOf(c.simplices(0).size()));
        binary("simplex-count",algebra(),naturals.algebra(),naturals.algebra(),false,(c,degree) ->
                aboveDimension(c,degree)?BigInteger.ZERO:BigInteger.valueOf(c.simplices(degree.intValueExact()).size()));
        binary("betti-number",algebra(),naturals.algebra(),naturals.algebra(),false,(c,degree) ->
                aboveDimension(c,degree)?BigInteger.ZERO:BigInteger.valueOf(c.bettiNumber(degree.intValueExact())));
        binary("skeleton",algebra(),naturals.algebra(),algebra(),false,(c,degree) ->
                aboveDimension(c,degree)?c:c.skeleton(degree.intValueExact()));
        unaryFlat("betti-numbers",algebra(),naturals.algebra(),false,c -> {
            List<BigInteger> result=new ArrayList<>();
            for(int degree=0;degree<=c.dimension();degree++) result.add(BigInteger.valueOf(c.bettiNumber(degree)));
            return result;
        });
        constant("empty",new FiniteSimplicialComplex(Collections.emptyList()));
        law("Complexes are closed under taking nonempty faces; union and intersection preserve closure.");
        law("Euler characteristic equals the alternating sum of unreduced F2 Betti numbers.");
        law("Equality compares labelled simplex sets, not homeomorphism or homotopy equivalence.");
    }
    private static boolean aboveDimension(FiniteSimplicialComplex complex,BigInteger degree) {
        return degree.compareTo(BigInteger.valueOf(complex.dimension()))>0;
    }
    public FiniteSimplicialAlgebra(BooleanAlgebra truth,NaturalSemiring naturals,IntegerRing integers,AbelianGroupTypeAlgebra groups) {
        this(truth,naturals,integers);
        binary("integral-homology",algebra(),naturals.algebra(),groups.algebra(),false,FiniteSimplicialComplex::integralHomology);
        unaryFlat("integral-homology-groups",algebra(),groups.algebra(),false,FiniteSimplicialComplex::integralHomologyGroups);
        binary("rational-betti-number",algebra(),naturals.algebra(),naturals.algebra(),false,(c,k) -> c.integralHomology(k).freeRank());
        unaryFlat("rational-betti-numbers",algebra(),naturals.algebra(),false,c -> {
            List<BigInteger> result=new ArrayList<>();
            for(AbelianGroupType group : c.integralHomologyGroups()) result.add(group.freeRank()); return result;
        });
        flat("boundary-invariant-factors",algebra(),naturals.algebra(),naturals.algebra(),false,FiniteSimplicialComplex::integralBoundaryInvariants);
        law("Integral homology is unreduced, with ascending vertex orientation and Smith invariant factors; rational Betti numbers are the integral free ranks.");
        law("The existing betti-number and betti-numbers operations retain their F2 coefficients; torsion can distinguish them from rational Betti numbers.");
    }
    public FiniteSimplicialAlgebra(BooleanAlgebra truth,NaturalSemiring naturals,IntegerRing integers,
                                   AbelianGroupTypeAlgebra groups,IntegerMatrixFamily matrices) {
        this(truth,naturals,integers,groups);
        binary("boundary-matrix",algebra(),naturals.algebra(),matrices.algebra(),false,FiniteSimplicialComplex::integralBoundaryMatrix);
    }
}
