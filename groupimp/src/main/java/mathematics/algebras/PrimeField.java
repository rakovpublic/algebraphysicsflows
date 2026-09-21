package mathematics.algebras;

import mathematics.core.*;
import mathematics.foundations.Unit;
import mathematics.numbers.*;

/** F_p for an exactly checked prime int p. Each modulus is a distinct carrier. */
public final class PrimeField extends ConcreteAlgebra<ModularInteger> {
    public final int prime;
    public PrimeField(Domain<Unit> unit,int prime) {
        super(NumberDomains.primeField(prime),unit);
        this.prime=prime;
        closed("add",false,ModularInteger::add);
        closed("subtract",false,(a,b) -> a.add(b.negate()));
        closed("multiply",false,ModularInteger::multiply);
        closed("divide",true,(a,b) -> a.multiply(b.inverse()));
        unary("negate",domain(),domain(),false,ModularInteger::negate);
        unary("inverse",domain(),domain(),true,ModularInteger::inverse);
        constant("zero",zero()); constant("one",one());
        law("Residues modulo the specified prime form a field; each nonzero residue is a unit.");
    }
    public ModularInteger member(long value) { return new ModularInteger(value,prime); }
    public ModularInteger zero() { return member(0); }
    public ModularInteger one() { return member(1); }
}

