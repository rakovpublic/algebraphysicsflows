package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.structures.Permutation;
import java.math.BigInteger;

/** The full symmetric group on a fixed number of zero-based labels. */
public final class SymmetricGroup extends ConcreteAlgebra<Permutation> {
    public final int degree;
    public SymmetricGroup(int degree,NaturalSemiring naturals,IntegerRing integers,BooleanAlgebra truth) {
        super(groupCarrier(degree),truth.unit()); this.degree=degree;
        closed("compose",false,Permutation::compose);
        unary("inverse",algebra(),algebra(),false,Permutation::inverse);
        binary("equal",algebra(),algebra(),truth.algebra(),false,Permutation::equals);
        unary("order",algebra(),naturals.algebra(),false,Permutation::order);
        unary("sign",algebra(),integers.algebra(),false,p -> BigInteger.valueOf(p.sign()));
        unary("fixed-point-count",algebra(),naturals.algebra(),false,p -> BigInteger.valueOf(p.fixedPointCount()));
        binary("apply",algebra(),naturals.algebra(),naturals.algebra(),true,Permutation::image);
        binary("power",algebra(),integers.algebra(),algebra(),false,Permutation::power);
        flat("orbit",algebra(),naturals.algebra(),naturals.algebra(),true,Permutation::orbit);
        unaryFlat("cycles",algebra(),algebra(),false,Permutation::disjointCycles);
        constant("identity",Permutation.identity(degree));
        unaryFlat("elements",unit(),algebra(),false,ignored -> Permutation.all(degree));
        law("Composition is associative with identity and inverse; S_n need not commute for n >= 3.");
        law("The order is the lcm of disjoint cycle lengths; sign is multiplicative under composition.");
        law("The action uses labels 0..n-1. Integer powers use cycle lengths, including negative exponents.");
    }
    private static Algebra<Permutation> groupCarrier(int degree) {
        if(degree<0) throw MathFailure.invalid("Negative symmetric group degree");
        return carrier("S"+degree,Permutation.class,"Symmetric group on "+degree+" labels",p -> p.degree()==degree);
    }
}
