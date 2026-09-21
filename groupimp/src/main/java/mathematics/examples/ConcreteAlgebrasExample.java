package mathematics.examples;

import algebra.concrete.ConcreteMathematics;
import algebra.concrete.ConcreteAlgebra;
import algebra.concrete.OperationRegistration;
import mathematics.calculus.Polynomial;
import mathematics.foundations.Pair;
import mathematics.linear.RationalVector;
import mathematics.numbers.Rational;
import java.math.BigInteger;
import java.util.*;

/** Runnable examples using the actual MathTool-registered algebras and legacy flow API. */
public final class ConcreteAlgebrasExample {
    private ConcreteAlgebrasExample() { }
    public static void main(String[] args) {
        ConcreteMathematics math=new ConcreteMathematics();
        if(args.length==1 && args[0].equals("--catalog")) {
            System.out.print(catalogManifest(math));
            return;
        }
        System.out.println("MathTool: "+math.mathTool.getName()+", concrete algebras: "+math.algebras().size()+", operations: "+math.operations().size());
        System.out.println("1/2 + 1/3 = "+math.rationals.algebra().buildAlgebraItem(Rational.of(1,2))
                .performOperation("add",Rational.of(1,3)).perform().getResult());
        System.out.println("N -> Z -> Q: "+math.flow(math.naturals,Arrays.asList(BigInteger.ONE,BigInteger.valueOf(2)))
                .performOperation("add",BigInteger.ONE)
                .<BigInteger>performAlgebraTransfer("to-integer")
                .<Rational>performAlgebraTransfer("to-rational")
                .performOperation("divide",Rational.of(2)).collect());
        System.out.println("Vector x scalar -> vector (flat): "+math.flow(math.vectors,Collections.singletonList(new RationalVector(Rational.ONE,Rational.of(2))))
                .performFlatCustomMemberOperation("scale-flat",Rational.of(3)).collect());
        System.out.println("Integral of x^2 from 0 to 1: "+math.flow(math.polynomials,Collections.singletonList(new Polynomial(Rational.ZERO,Rational.ZERO,Rational.ONE)))
                .<Rational,Pair<Rational,Rational>>performAlgebraUnsafe("integrate",new Pair<>(Rational.ZERO,Rational.ONE)).collect());
        System.out.println("F5: 3 / 2 = "+math.primeFields.get(0).algebra().buildAlgebraItem(math.primeFields.get(0).member(3))
                .performOperation("divide",math.primeFields.get(0).member(2)).perform().getResult());
    }
    /** Stable manifest used to keep the human/JSON coverage registry aligned with real registrations. */
    public static String catalogManifest(ConcreteMathematics math) {
        StringBuilder result=new StringBuilder("id\tclass\talias\tfirst\tsecond\tresult\tsemantics\tpartiality\tinterface\n");
        for(ConcreteAlgebra<?> algebra : math.algebras()) for(OperationRegistration operation : algebra.operations().values()) {
            result.append(operation.id).append('\t').append(algebra.getClass().getSimpleName()).append('\t').append(operation.alias).append('\t')
                    .append(operation.first.getAlgebraName()).append('\t')
                    .append(operation.second==null?"-":operation.second.getAlgebraName()).append('\t')
                    .append(operation.result.getAlgebraName()).append('\t').append(operation.flat?"LIST":"SCALAR").append('\t')
                    .append(operation.partial?"PARTIAL":"TOTAL").append('\t').append(operation.operation.getClass().getInterfaces()[0].getSimpleName()).append('\n');
        }
        return result.toString();
    }
}
