package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.numbers.ModularInteger;
import java.math.BigInteger;
import java.util.*;

/** Z/nZ for a fixed arbitrary-precision modulus n > 1; primality is not assumed. */
public final class ResidueRing extends ConcreteAlgebra<ModularInteger> {
    public final BigInteger modulus;
    public ResidueRing(BigInteger modulus,IntegerRing integers,BooleanAlgebra truth) {
        super(residueCarrier(modulus),integers.unit()); this.modulus=modulus;
        closed("add",false,ModularInteger::add);
        closed("subtract",false,(a,b) -> a.add(b.negate()));
        closed("multiply",false,ModularInteger::multiply);
        closed("divide",true,(a,b) -> a.multiply(b.inverse()));
        unary("negate",algebra(),algebra(),false,ModularInteger::negate);
        unary("inverse",algebra(),algebra(),true,ModularInteger::inverse);
        unary("is-unit",algebra(),truth.algebra(),false,ModularInteger::isUnit);
        unary("is-zero-divisor",algebra(),truth.algebra(),false,ModularInteger::isZeroDivisor);
        unary("lift",algebra(),integers.algebra(),false,a -> a.value);
        unary("reduce",integers.algebra(),algebra(),false,n -> new ModularInteger(n,modulus));
        flat("solve-multiply",algebra(),algebra(),algebra(),false,ModularInteger::solveMultiply);
        binary("power",algebra(),integers.algebra(),algebra(),true,ModularInteger::power);
        binary("equal",algebra(),algebra(),truth.algebra(),false,ModularInteger::equals);
        constant("zero",member(BigInteger.ZERO)); constant("one",member(BigInteger.ONE));
        unaryFlat("elements",unit(),algebra(),false,ignored -> {
            if(modulus.compareTo(BigInteger.valueOf(10000))>0)
                throw new MathFailure(MathFailure.Kind.IMPLEMENTATION_FAILURE,"Residue enumeration is limited to 10000 members");
            List<ModularInteger> values=new ArrayList<>();
            for(int i=0;i<modulus.intValueExact();i++) values.add(member(BigInteger.valueOf(i)));
            return values;
        });
        law("Z/nZ is a commutative unital ring; nonzero elements need not be units for composite n.");
        law("Inverses and division require gcd(value,n)=1; negative powers require a unit base.");
        law("a*x=b has gcd(a,n) solutions exactly when that gcd divides b; all solutions have canonical representatives.");
    }
    public ModularInteger member(BigInteger value) { return new ModularInteger(value,modulus); }
    public ModularInteger member(long value) { return member(BigInteger.valueOf(value)); }
    private static Algebra<ModularInteger> residueCarrier(BigInteger modulus) {
        if(Objects.requireNonNull(modulus).compareTo(BigInteger.ONE)<=0) throw MathFailure.invalid("Modulus must exceed one");
        return carrier("Z/"+modulus+"Z",ModularInteger.class,"Residue ring modulo "+modulus,n -> n.modulus.equals(modulus));
    }
}
