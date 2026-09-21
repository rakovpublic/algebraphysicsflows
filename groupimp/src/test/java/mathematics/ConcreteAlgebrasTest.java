package mathematics;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import mathematics.algebras.*;
import mathematics.calculus.Polynomial;
import mathematics.core.*;
import mathematics.foundations.*;
import mathematics.linear.*;
import mathematics.numbers.*;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class ConcreteAlgebrasTest {
    @Test public void registersConcreteLegacyAlgebrasAndKeepsInstancesIsolated() throws Exception {
        ConcreteMathematics first=new ConcreteMathematics(),second=new ConcreteMathematics();
        for(ConcreteAlgebra<?> algebra : first.algebras()) {
            assertSame(algebra.algebra(),first.mathTool.getAlgebra(algebra.domain().metadata().id));
            assertFalse(algebra.operations().isEmpty());
            assertFalse(algebra.structure().laws.isEmpty());
            assertNotSame(algebra.algebra(),second.mathTool.getAlgebra(algebra.domain().metadata().id));
        }
        assertTrue(first.integers.algebra().hasOperation("add"));
        assertTrue(first.rationals.algebra().hasFlatOperation("add-subtract"));
        assertThrows(IllegalArgumentException.class,() -> second.integers.register(first.mathTool));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream output=new ObjectOutputStream(bytes)) { output.writeObject(first.mathTool); }
        MathTool restored;
        try(ObjectInputStream input=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(MathTool)input.readObject(); }
        @SuppressWarnings("unchecked") Algebra<Rational> q=(Algebra<Rational>)restored.getAlgebra("Q");
        assertEquals(Rational.of(5,6),q.buildAlgebraItem(Rational.of(1,2)).performOperation("add",Rational.of(1,3)).perform().getResult());
    }
    @Test public void naturalIntegerRationalBooleanFlowUsesMathToolRegistrations() {
        ConcreteMathematics m=new ConcreteMathematics();
        IAlgebraFlow<Boolean> flow=m.flow(m.naturals,Arrays.asList(BigInteger.ONE,BigInteger.valueOf(2)))
                .performOperation("add",BigInteger.ONE)
                .<BigInteger>performAlgebraTransfer("to-integer")
                .performOperation("subtract",BigInteger.valueOf(4))
                .<Rational>performAlgebraTransfer("to-rational")
                .performOperation("divide",Rational.of(2))
                .<Boolean>performCustomResultOperation("greater",Rational.of(-1))
                .<Boolean>performAlgebraTransfer("not");
        assertEquals(Arrays.asList("true","false"),flow.collect());
        assertEquals(Arrays.asList("true","false"),flow.collect());
        assertNull(m.naturals.algebra().buildAlgebraItem(BigInteger.valueOf(-1)));
        assertThrows(MathFailure.class,() -> m.flow(m.naturals,Collections.singletonList(BigInteger.valueOf(-1))));
        assertEquals(Rational.of(3,2),m.integers.domain().item(BigInteger.valueOf(3))
                .<Rational>performCustomResultOperation("divide-rational",BigInteger.valueOf(2)).perform().getResult());
        assertEquals(Arrays.asList("-2","-1"),m.flow(m.integers,Collections.singletonList(BigInteger.valueOf(-7)))
                .performFlatOperation("quotient-remainder",BigInteger.valueOf(3)).collect());
    }
    @Test public void rationalFieldLawsHoldForDeterministicSamplesThroughRegisteredOperations() {
        ConcreteMathematics m=new ConcreteMathematics();
        Algebra<Rational> q=m.rationals.algebra();
        Random random=new Random(1921);
        for(int i=0;i<120;i++) {
            Rational a=Rational.of(random.nextInt(101)-50,random.nextInt(12)+1);
            Rational b=Rational.of(random.nextInt(101)-50,random.nextInt(12)+1);
            Rational c=Rational.of(random.nextInt(101)-50,random.nextInt(12)+1);
            Rational sum=q.buildAlgebraItem(b).performOperation("add",c).perform().getResult();
            Rational left=q.buildAlgebraItem(a).performOperation("multiply",sum).perform().getResult();
            Rational right=q.buildAlgebraItem(a).performOperation("multiply",b)
                    .performOperation("add",q.buildAlgebraItem(a).performOperation("multiply",c).perform().getResult()).perform().getResult();
            assertEquals(left,right);
            if(a.signum()!=0) assertEquals(Rational.ONE,q.buildAlgebraItem(a).performOperation("divide",a).perform().getResult());
        }
        assertEquals(Arrays.asList("2","2"),m.flow(m.rationals,Collections.singletonList(Rational.of(2)))
                .performFlatOperation("add-subtract",Rational.ZERO).collect());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                () -> q.buildAlgebraItem(Rational.ONE).performOperation("divide",Rational.ZERO).perform()).kind());
    }
    @Test public void primeFieldsAreClosedAndRejectWrongModuliAndCompositeParameters() {
        ConcreteMathematics m=new ConcreteMathematics(2,5,7);
        for(PrimeField field : m.primeFields) {
            for(int a=0;a<field.prime;a++) for(int b=0;b<field.prime;b++) {
                IAlgebraItem<ModularInteger> member=field.domain().item(field.member(a));
                assertEquals(field.member(a+b),member.performOperation("add",field.member(b)).perform().getResult());
                assertEquals(field.member(a*b),member.performOperation("multiply",field.member(b)).perform().getResult());
                if(b!=0) assertEquals(field.member(a),member.performOperation("divide",field.member(b)).performOperation("multiply",field.member(b)).perform().getResult());
            }
            assertThrows(MathFailure.class,() -> field.domain().item(field.one()).performOperation("divide",field.zero()).perform());
        }
        assertThrows(MathFailure.class,() -> m.primeFields.get(0).domain().item(new ModularInteger(1,7)));
        assertThrows(exceptions.NotMemberException.class,() -> m.primeFields.get(0).domain().item(new ModularInteger(1,5)).performOperation("add",new ModularInteger(1,7)));
        assertThrows(MathFailure.class,() -> new PrimeField(m.unit,9));
        assertThrows(IllegalArgumentException.class,() -> new ConcreteMathematics(2,5,5));
    }
    @Test public void vectorActionsUseDifferentOperandClassesAndFlatResultsStayInTheVectorDomain() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalVector v=new RationalVector(Rational.ONE,Rational.of(2));
        assertEquals(Arrays.asList("[6, 12]"),m.flow(m.vectors,Collections.singletonList(v))
                .<RationalVector,Rational>performAlgebraUnsafe("scale",Rational.of(2))
                .<RationalVector,Rational>performFlatAlgebraUnsafe("scale-flat",Rational.of(3)).collect());
        assertEquals(Arrays.asList("5","-5"),m.flow(m.vectors,Collections.singletonList(v))
                .<RationalVector,Rational>performFlatAlgebraUnsafe("scale-signs",Rational.ONE)
                .<Rational>performCustomResultOperation("dot",v)
                .collect());
        // Test actual opposite-order scalar action through a Q -> Q^2 flow.
        assertEquals(Collections.singletonList("[3, 6]"),m.flow(m.rationals,Collections.singletonList(Rational.of(3)))
                .<RationalVector,RationalVector>performAlgebraUnsafe("Q^2.scale-left",v).collect());
        assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,
                () -> m.vectors.domain().item(new RationalVector(Rational.ONE))).kind());
    }
    @Test public void matrixAndPolynomialOperationsCrossDomainsWithExactResults() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalMatrix a=new RationalMatrix(new Rational[][] {{Rational.ONE,Rational.of(2)},{Rational.of(3),Rational.of(4)}});
        assertEquals(Collections.singletonList("-2"),m.flow(m.matrices,Collections.singletonList(a)).<Rational>performAlgebraTransfer("determinant").collect());
        assertEquals(RationalMatrix.identity(2),m.matrices.domain().item(a).<RationalMatrix>performAlgebraTransfer("inverse").performOperation("multiply",a).perform().getResult());
        RationalVector v=new RationalVector(Rational.ONE,Rational.of(2));
        assertEquals(Collections.singletonList("[5, 11]"),m.flow(m.matrices,Collections.singletonList(a)).<RationalVector,RationalVector>performAlgebraUnsafe("apply",v).collect());
        RationalMatrix b=new RationalMatrix(new Rational[][] {{Rational.ZERO,Rational.ONE},{Rational.ONE,Rational.ZERO}});
        assertNotEquals(m.matrices.domain().item(a).performOperation("multiply",b).perform().getResult(),m.matrices.domain().item(b).performOperation("multiply",a).perform().getResult());
        Polynomial square=new Polynomial(Rational.ZERO,Rational.ZERO,Rational.ONE);
        assertEquals(Collections.singletonList("6"),m.flow(m.polynomials,Collections.singletonList(square))
                .<Polynomial>performAlgebraTransfer("derivative").<Rational,Rational>performAlgebraUnsafe("evaluate",Rational.of(3)).collect());
        assertEquals(Collections.singletonList("1/3"),m.flow(m.polynomials,Collections.singletonList(square))
                .<Rational,Pair<Rational,Rational>>performAlgebraUnsafe("integrate",new Pair<>(Rational.ZERO,Rational.ONE)).collect());
        assertEquals(square,m.polynomials.domain().item(square).<Polynomial,Rational>performUnsafeOperation("primitive",Rational.of(7))
                .<Polynomial>performAlgebraTransfer("derivative").perform().getResult());
    }
    @Test public void constantsBooleanLawsAndComplexArithmeticAreRegistered() {
        ConcreteMathematics m=new ConcreteMathematics();
        assertEquals(Rational.ZERO,m.unit.item(Unit.INSTANCE).<Rational>performAlgebraTransfer("Q.zero").perform().getResult());
        for(boolean a : new boolean[]{false,true}) for(boolean b : new boolean[]{false,true}) {
            Boolean notAnd=m.booleans.domain().item(a).performOperation("and",b).<Boolean>performAlgebraTransfer("not").perform().getResult();
            Boolean deMorgan=m.booleans.domain().item(!a).performOperation("or",!b).perform().getResult();
            assertEquals(notAnd,deMorgan);
        }
        RationalComplex i=new RationalComplex(Rational.ZERO,Rational.ONE);
        assertEquals(new RationalComplex(Rational.of(-1),Rational.ZERO),m.complexRationals.domain().item(i).performOperation("multiply",i).perform().getResult());
        assertEquals(Rational.ONE,m.complexRationals.domain().item(i).<Rational>performAlgebraTransfer("norm-squared").perform().getResult());
        assertEquals(Collections.singletonList("(2)+(0)i"),m.flow(m.rationals,Collections.singletonList(Rational.of(2)))
                .<RationalComplex>performAlgebraTransfer("Q(i).embed-rational").collect());
    }
}
