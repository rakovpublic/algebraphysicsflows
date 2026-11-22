package algebra.concrete;

import mathematics.numbers.*;

/** Registered exact cyclotomic field Q(zeta_12), containing the selected Q(i) subfield. */
public final class RationalCyclotomicAlgebra extends ConcreteAlgebra<RationalCyclotomic> {
    public static final int ORDER=12;
    public RationalCyclotomicAlgebra(RationalField rationals,RationalComplexField complex,BooleanAlgebra truth){
        super(carrier("Q(zeta_12)",RationalCyclotomic.class,"Exact cyclotomic field with power basis reduced modulo Phi_12(x)=x^4-x^2+1",x -> x.order()==ORDER),rationals.unit());
        closed("add",false,RationalCyclotomic::add);
        closed("subtract",false,RationalCyclotomic::subtract);
        closed("multiply",false,RationalCyclotomic::multiply);
        closed("divide",true,RationalCyclotomic::divide);
        unary("negate",algebra(),algebra(),false,RationalCyclotomic::negate);
        unary("inverse",algebra(),algebra(),true,RationalCyclotomic::inverse);
        unary("to-rational",algebra(),rationals.algebra(),true,RationalCyclotomic::toRational);
        unary("to-complex",algebra(),complex.algebra(),true,RationalCyclotomic::toComplex);
        unary("to-boolean",algebra(),truth.algebra(),true,x -> BooleanAlgebra.decode(x.equals(RationalCyclotomic.zero(ORDER)),x.equals(RationalCyclotomic.one(ORDER)),"Q(zeta_12)"));
        binary("equal",algebra(),algebra(),truth.algebra(),false,RationalCyclotomic::equals);
        unary("embed-rational",rationals.algebra(),algebra(),false,q -> RationalCyclotomic.fromRational(ORDER,q));
        unary("embed-complex",complex.algebra(),algebra(),false,z -> RationalCyclotomic.fromComplex(ORDER,z));
        constant("zero",RationalCyclotomic.zero(ORDER));constant("one",RationalCyclotomic.one(ORDER));constant("zeta",RationalCyclotomic.zeta(ORDER));
        law("Arithmetic is exact in Q[x]/(Phi_12), and inverses use polynomial extended Euclid.");
        law("Q and the selected Q(i) subfield have exact inclusion and partial projection maps. Boolean projection is defined only on zero and one.");
        law("Decimal complex evaluation is approximate and precision-controlled; it is distinct from exact field equality.");
    }
}
