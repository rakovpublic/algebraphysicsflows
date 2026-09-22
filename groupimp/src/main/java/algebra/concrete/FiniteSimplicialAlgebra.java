package algebra.concrete;

import mathematics.topology.FiniteSimplicialComplex;
import java.math.BigInteger;
import java.util.*;

/** Finite labelled abstract simplicial complexes; unreduced homology over F2. */
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
}
