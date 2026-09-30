package algebra.concrete;

import algebra.imp.Algebra;
import mathematics.core.MathFailure;
import mathematics.foundations.Unit;
import mathematics.numbers.*;

/** F_p for an exactly checked prime int p. Each modulus is a distinct carrier. */
public final class PrimeField extends ConcreteAlgebra<ModularInteger> {
    public final int prime;
    public PrimeField(Algebra<Unit> unit,int prime) {
        super(primeCarrier(prime),unit);
        this.prime=prime;
        closed("add",false,ModularInteger::add);
        closed("subtract",false,(a,b) -> a.add(b.negate()));
        closed("multiply",false,ModularInteger::multiply);
        closed("divide",true,(a,b) -> a.multiply(b.inverse()));
        unary("negate",algebra(),algebra(),false,ModularInteger::negate);
        unary("inverse",algebra(),algebra(),true,ModularInteger::inverse);
        constant("zero",zero()); constant("one",one());
        law("Residues modulo the specified prime form a field; each nonzero residue is a unit.");
    }
    private static Algebra<ModularInteger> primeCarrier(int prime) {
        if(!NumberTheory.isPrime(prime)) throw MathFailure.invalid("Field modulus must be prime");
        return carrier("Z/"+prime+"Z",ModularInteger.class,"Prime residue field",n -> n.modulus.equals(java.math.BigInteger.valueOf(prime)));
    }
    public ModularInteger member(long value) { return new ModularInteger(value,prime); }
    public ModularInteger zero() { return member(0); }
    public ModularInteger one() { return member(1); }
}

