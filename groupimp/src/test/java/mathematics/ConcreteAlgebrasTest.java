package mathematics;

import algebra.IAlgebraItem;
import algebra.imp.Algebra;
import algebra.imp.MathTool;
import algebraflow.IAlgebraFlow;
import algebra.concrete.*;
import mathematics.calculus.Polynomial;
import mathematics.core.MathFailure;
import operations.simple.*;
import operations.flat.*;
import mathematics.foundations.*;
import mathematics.linear.*;
import mathematics.numbers.*;
import mathematics.examples.ConcreteAlgebrasExample;
import org.junit.Test;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;

public class ConcreteAlgebrasTest {
    @Test public void all214RegisteredOperationsReturnIndependentExpectedValues() {
        ConcreteMathematics math=new ConcreteMathematics();
        Map<String,String> expected=new HashMap<>();
        expected.put("BooleanAlgebra","false|true|true|false|false|false|false|true");
        expected.put("NaturalSemiring","8|12|7|6|0|1");
        expected.put("IntegerRing","8|4|12|2|6|3|0|-6|6|3|true|false|[3, 0]|0|1");
        expected.put("RationalField","8|4|12|3|-6|1/6|true|false|[8, 4]|0|1");
        expected.put("RationalComplexField","(4)+(3)i|(-2)+(1)i|(1)+(7)i|(1/2)+(1/2)i|(-1)+(-2)i|(1)+(-2)i|5|(6)+(0)i|(0)+(0)i|(1)+(0)i");
        expected.put("RationalVectorSpace","[4, 6]|[-2, -2]|[-1, -2]|[2, 4]|[18, 24]|[[18, 24]]|11|[[2, 4]]|[[2, 4], [-2, -4]]|[0, 0]");
        expected.put("RationalMatrixAlgebra","[[3, 2], [3, 6]]|[[2, 4], [6, 8]]|[[-1, 2], [3, 2]]|[[-1, -2], [-3, -4]]|[[1, 3], [2, 4]]|[[-2, 1], [3/2, -1/2]]|-2|5|[[2, 4], [6, 8]]|[11, 25]|[-2, 5/2]|[[0, 0], [0, 0]]|[[1, 0], [0, 1]]|2");
        expected.put("RationalPolynomialRing","Q[x][3, 3, 1]|Q[x][2, 5, 4, 1]|Q[x][-1, 1, 1]|Q[x][-1, -2, -1]|Q[x][2, 2]|9|Q[x][2, 1, 1, 1/3]|7/3|Q[x][0]|Q[x][1]|Q[x][0, 1]|Q[x][1]|Q[x][1, 1]|Q[x][1]|Q[x][9, 6, 1]|Q[x][1, 2, 1]|[Q[x][0, 1], Q[x][1]]|Q[x][2]|4|[0, 1, 4]");
        expected.put("PrimeField","0 (mod 5)|1 (mod 5)|1 (mod 5)|4 (mod 5)|2 (mod 5)|2 (mod 5)|0 (mod 5)|1 (mod 5)");
        expected.put("IntegerSetAlgebra","[1, 2, 3]|[1, 2]|[]|[3]|true|false|true|[1, 2]|[1]|2|[1, 2]|[[], [1], [2], [1, 2]]|[3]|[]|[1]|[2]|3|4|[1]|[2]");
        expected.put("RationalSampleAlgebra","Sample[1, 2, 3, 2, 4, 6]|3|2|2/3|1|Sample[-1, 0, 1]|4/3|2|Sample[2, 4, 6]|[1, 2, 3]|Sample[]");
        expected.put("FiniteProbabilityAlgebra","1|0|Distribution{1=1/4, 3=3/4}|[1, 3]|2|[1, 3]|false|Distribution{6=1}|5/2|3/4");
        expected.put("FiniteSimplicialAlgebra","Complex[[0], [1], [2], [0, 1], [0, 2], [1, 2], [0, 1, 2]]|Complex[[0], [1], [2], [0, 1], [0, 2], [1, 2]]|false|true|1|0|3|0|0|Complex[[0], [1], [2], [0, 1], [0, 2], [1, 2]]|[1, 1]|Complex[]");
        expected.put("FiniteIntegerRelationAlgebra","Relation[(1,2), (2,3), (2,4), (3,5)]|Relation[]|Relation[(1,4), (2,5)]|Relation[(2,1), (3,2)]|Relation[(1,2), (2,3), (1,3)]|false|false|[1, 2]|[2, 3]|2|true|[2, 3]|[1, 2]|false|Relation[]|Relation[(1,1), (2,2)]");
        expected.put("RationalFunctionField","Q(x)(Q[x][1, 1, 1])/(Q[x][0, 1])|Q(x)(Q[x][1, -1, -1])/(Q[x][0, 1])|Q(x)(Q[x][1, 1])/(Q[x][0, 1])|Q(x)(Q[x][1])/(Q[x][0, 1, 1])|Q(x)(Q[x][1])/(Q[x][1, 1])|Q(x)(Q[x][-1])/(Q[x][0, 1])|Q(x)(Q[x][0, 1])/(Q[x][1])|Q(x)(Q[x][-1])/(Q[x][0, 0, 1])|1/2|Q[x][1]|Q[x][0, 1]|Q(x)(Q[x][1, 2, 1])/(Q[x][1])|false|Q(x)(Q[x][0])/(Q[x][1])|Q(x)(Q[x][1])/(Q[x][1])");
        expected.put("SymmetricGroup","Perm[2, 1, 0]|Perm[2, 0, 1]|false|3|1|0|0|Perm[2, 0, 1]|[2, 0, 1]|[Perm[1, 2, 0]]|Perm[0, 1, 2]|[Perm[0, 1, 2], Perm[0, 2, 1], Perm[1, 0, 2], Perm[1, 2, 0], Perm[2, 0, 1], Perm[2, 1, 0]]");
        expected.put("ResidueRing","0 (mod 6)|4 (mod 6)|5 (mod 6)|5 (mod 6)|1 (mod 6)|5 (mod 6)|true|false|5|0 (mod 6)|[5 (mod 6)]|1 (mod 6)|false|0 (mod 6)|1 (mod 6)|[0 (mod 6), 1 (mod 6), 2 (mod 6), 3 (mod 6), 4 (mod 6), 5 (mod 6)]");
        int count=0;
        for(ConcreteAlgebra<?> algebra : math.algebras()) {
            String[] values=expected.get(algebra.getClass().getSimpleName()).split("\\|",-1);
            assertEquals(algebra.operations().size(),values.length);
            int i=0;
            for(Map.Entry<String,OperationRegistration> entry : algebra.operations().entrySet())
                assertEquals(entry.getValue().id,values[i++],invokeRegistered(math,algebra,entry.getKey(),entry.getValue()));
            count+=i;
        }
        assertEquals(214,count);
    }
    @SuppressWarnings({"unchecked","rawtypes"})
    private String invokeRegistered(ConcreteMathematics math,ConcreteAlgebra<?> owner,String name,OperationRegistration entry) {
        Algebra source=math.mathTool.getAlgebra(entry.first.getAlgebraName());
        IAlgebraItem item=source.buildAlgebraItem(sample(math,source.getAlgebraName(),0));
        String alias=entry.alias;
        Object operation=entry.operation;
        if(operation instanceof IOneOperandOperation) return item.performOneOperandOperation(alias).perform().getResult().toString();
        if(operation instanceof ITransferOperation) return item.performAlgebraTransfer(alias).perform().getResult().toString();
        Object second=entry.second==null?null:sample(math,entry.second.getAlgebraName(),1);
        if(entry.id.equals("Q[x].divide-exact")) second=new Polynomial(Rational.ONE,Rational.ONE);
        if(entry.flat) {
            List<IAlgebraItem> results;
            if(operation instanceof IOneOperandFlatOperation) results=item.performOneOperandFlatOperation(alias);
            else if(operation instanceof ITransferFlatOperation) results=item.performAlgebraFlatTransfer(alias);
            else if(operation instanceof IFlatOperation) results=item.performFlatOperation(alias,second);
            else if(operation instanceof ICustomMemberFlatOperation) results=item.performCustomMemberFlatOperation(alias,second);
            else if(operation instanceof ILeftProjectionFlatOperation) results=item.performLeftProjectionFlatOperation(alias,second);
            else if(operation instanceof ICustomResultFlatOperation) results=item.performCustomResultFlatOperation(alias,second);
            else results=item.performUnsafeFlatOperation(alias,second);
            List<Object> values=new ArrayList<>();
            for(IAlgebraItem result : results) values.add(result.perform().getResult());
            return values.toString();
        }
        IAlgebraItem result;
        if(operation instanceof IOperation) result=item.performOperation(alias,second);
        else if(operation instanceof ICustomResultOperation) result=item.performCustomResultOperation(alias,second);
        else if(operation instanceof ICustomMemberOperation) result=item.performCustomMemberOperation(alias,second);
        else if(operation instanceof ILeftProjectionOperation) result=item.performLeftProjectionOperation(alias,second);
        else result=item.performUnsafeOperation(alias,second);
        return result.perform().getResult().toString();
    }
    private Object sample(ConcreteMathematics math,String domain,int index) {
        switch(domain) {
            case "Unit": return Unit.INSTANCE;
            case "FiniteComplex": return new mathematics.topology.FiniteSimplicialComplex(index==0
                    ?Arrays.asList(FiniteSet.of(0,1),FiniteSet.of(1,2),FiniteSet.of(0,2))
                    :Collections.singletonList(FiniteSet.of(0,1,2)));
            case "Boolean": return index==0;
            case "N": case "Z": return BigInteger.valueOf(index==0?6:2);
            case "Q": return Rational.of(index==0?6:2);
            case "Q(i)": return index==0?new RationalComplex(Rational.ONE,Rational.of(2)):new RationalComplex(Rational.of(3),Rational.ONE);
            case "Q^2": return index==0?new RationalVector(Rational.ONE,Rational.of(2)):new RationalVector(Rational.of(3),Rational.of(4));
            case "Mat2(Q)": return new RationalMatrix(index==0
                    ?new Rational[][] {{Rational.ONE,Rational.of(2)},{Rational.of(3),Rational.of(4)}}
                    :new Rational[][] {{Rational.of(2),Rational.ZERO},{Rational.ZERO,Rational.of(2)}});
            case "Q[x]": return index==0?new Polynomial(Rational.ONE,Rational.of(2),Rational.ONE):new Polynomial(Rational.of(2),Rational.ONE);
            case "FiniteSet(Z)": return index==0?FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)):FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2),BigInteger.valueOf(3));
            case "Sample(Q)": return index==0?mathematics.statistics.RationalSample.of(Rational.ONE,Rational.of(2),Rational.of(3)):mathematics.statistics.RationalSample.of(Rational.of(2),Rational.of(4),Rational.of(6));
            case "FiniteDistribution(Z)":
                Map<BigInteger,Rational> masses=new LinkedHashMap<>();
                if(index==0) { masses.put(BigInteger.ONE,Rational.of(1,4)); masses.put(BigInteger.valueOf(3),Rational.of(3,4)); }
                else masses.put(BigInteger.ONE,Rational.ONE);
                return new mathematics.probability.FiniteDistribution<>(math.integers.algebra(),masses);
            case "Q(x)": return index==0
                    ?new mathematics.calculus.RationalFunction(Polynomial.ONE,new Polynomial(Rational.ZERO,Rational.ONE))
                    :mathematics.calculus.RationalFunction.of(new Polynomial(Rational.ONE,Rational.ONE));
            case "S3": return index==0?new mathematics.structures.Permutation(1,2,0):new mathematics.structures.Permutation(1,0,2);
            case "QxQ.bounds": return new Pair<>(Rational.ZERO,Rational.ONE);
            case "QxN.iteration": return new Pair<>(Rational.ZERO,BigInteger.valueOf(2));
            case "ZxZ.relation": return new Pair<>(BigInteger.ONE,BigInteger.valueOf(2));
            case "FiniteRelation(Z,Z)": return new FiniteRelation<>(math.integers.algebra(),math.integers.algebra(),index==0
                    ?FiniteSet.of(new Pair<>(BigInteger.ONE,BigInteger.valueOf(2)),new Pair<>(BigInteger.valueOf(2),BigInteger.valueOf(3)))
                    :FiniteSet.of(new Pair<>(BigInteger.valueOf(2),BigInteger.valueOf(4)),new Pair<>(BigInteger.valueOf(3),BigInteger.valueOf(5))));
            case "Z/6Z": return new ModularInteger(index==0?5:1,6);
            case "Z/5Z": return new ModularInteger(index==0?3:2,5);
            default: throw new AssertionError("Missing test operand for "+domain);
        }
    }
    @Test public void runtimeRegistrationsMatchTheCoverageManifest() throws Exception {
        StringBuilder expected=new StringBuilder();
        try(BufferedReader input=new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/mathematics/concrete-catalog.tsv")),"UTF-8"))) {
            String line; while((line=input.readLine())!=null) expected.append(line).append('\n');
        }
        assertEquals(expected.toString(),ConcreteAlgebrasExample.catalogManifest(new ConcreteMathematics()));
    }
    @Test public void registersConcreteLegacyAlgebrasAndKeepsInstancesIsolated() throws Exception {
        ConcreteMathematics first=new ConcreteMathematics(),second=new ConcreteMathematics();
        for(ConcreteAlgebra<?> algebra : first.algebras()) {
            assertSame(algebra.algebra(),first.mathTool.getAlgebra(algebra.algebra().getAlgebraName()));
            assertFalse(algebra.operations().isEmpty());
            assertFalse(algebra.laws().isEmpty());
            assertNotSame(algebra.algebra(),second.mathTool.getAlgebra(algebra.algebra().getAlgebraName()));
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
                .performOneOperandOperation("not");
        assertEquals(Arrays.asList("true","false"),flow.collect());
        assertEquals(Arrays.asList("true","false"),flow.collect());
        assertNull(m.naturals.algebra().buildAlgebraItem(BigInteger.valueOf(-1)));
        assertThrows(exceptions.NotMemberException.class,() -> m.flow(m.naturals,Collections.singletonList(BigInteger.valueOf(-1))));
        assertEquals(Rational.of(3,2),m.integers.algebra().buildAlgebraItem(BigInteger.valueOf(3))
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
                IAlgebraItem<ModularInteger> member=field.algebra().buildAlgebraItem(field.member(a));
                assertEquals(field.member(a+b),member.performOperation("add",field.member(b)).perform().getResult());
                assertEquals(field.member(a*b),member.performOperation("multiply",field.member(b)).perform().getResult());
                if(b!=0) assertEquals(field.member(a),member.performOperation("divide",field.member(b)).performOperation("multiply",field.member(b)).perform().getResult());
            }
            assertThrows(MathFailure.class,() -> field.algebra().buildAlgebraItem(field.one()).performOperation("divide",field.zero()).perform());
        }
        assertNull(m.primeFields.get(0).algebra().buildAlgebraItem(new ModularInteger(1,7)));
        assertThrows(exceptions.NotMemberException.class,() -> m.primeFields.get(0).algebra().buildAlgebraItem(new ModularInteger(1,5)).performOperation("add",new ModularInteger(1,7)));
        assertThrows(MathFailure.class,() -> new PrimeField(m.unit,9));
        assertThrows(IllegalArgumentException.class,() -> new ConcreteMathematics(2,5,5));
    }
    @Test public void vectorActionsUseDifferentOperandClassesAndFlatResultsStayInTheVectorDomain() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalVector v=new RationalVector(Rational.ONE,Rational.of(2));
        assertEquals(Arrays.asList("[6, 12]"),m.flow(m.vectors,Collections.singletonList(v))
                .performCustomMemberOperation("scale",Rational.of(2))
                .performFlatCustomMemberOperation("scale-flat",Rational.of(3)).collect());
        assertEquals(Arrays.asList("5","-5"),m.flow(m.vectors,Collections.singletonList(v))
                .performFlatCustomMemberOperation("scale-signs",Rational.ONE)
                .<Rational>performCustomResultOperation("dot",v)
                .collect());
        // Test actual opposite-order scalar action through a Q -> Q^2 flow.
        assertEquals(Collections.singletonList("[3, 6]"),m.flow(m.rationals,Collections.singletonList(Rational.of(3)))
                .performLeftProjectionOperation("Q^2.scale-left",v).collect());
        assertNull(m.vectors.algebra().buildAlgebraItem(new RationalVector(Rational.ONE)));
    }
    @Test public void matrixAndPolynomialOperationsCrossDomainsWithExactResults() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalMatrix a=new RationalMatrix(new Rational[][] {{Rational.ONE,Rational.of(2)},{Rational.of(3),Rational.of(4)}});
        assertEquals(Collections.singletonList("-2"),m.flow(m.matrices,Collections.singletonList(a)).<Rational>performAlgebraTransfer("determinant").collect());
        assertEquals(RationalMatrix.identity(2),m.matrices.algebra().buildAlgebraItem(a).performOneOperandOperation("inverse").performOperation("multiply",a).perform().getResult());
        RationalVector v=new RationalVector(Rational.ONE,Rational.of(2));
        assertEquals(Collections.singletonList("[5, 11]"),m.flow(m.matrices,Collections.singletonList(a)).performLeftProjectionOperation("apply",v).collect());
        RationalMatrix b=new RationalMatrix(new Rational[][] {{Rational.ZERO,Rational.ONE},{Rational.ONE,Rational.ZERO}});
        assertNotEquals(m.matrices.algebra().buildAlgebraItem(a).performOperation("multiply",b).perform().getResult(),m.matrices.algebra().buildAlgebraItem(b).performOperation("multiply",a).perform().getResult());
        Polynomial square=new Polynomial(Rational.ZERO,Rational.ZERO,Rational.ONE);
        assertEquals(Collections.singletonList("6"),m.flow(m.polynomials,Collections.singletonList(square))
                .performOneOperandOperation("derivative").performLeftProjectionOperation("evaluate",Rational.of(3)).collect());
        assertEquals(Collections.singletonList("1/3"),m.flow(m.polynomials,Collections.singletonList(square))
                .<Rational,Pair<Rational,Rational>>performAlgebraUnsafe("integrate",new Pair<>(Rational.ZERO,Rational.ONE)).collect());
        assertEquals(square,m.polynomials.algebra().buildAlgebraItem(square).performCustomMemberOperation("primitive",Rational.of(7))
                .performOneOperandOperation("derivative").perform().getResult());
    }
    @Test public void constantsBooleanLawsAndComplexArithmeticAreRegistered() {
        ConcreteMathematics m=new ConcreteMathematics();
        assertEquals(Rational.ZERO,m.unit.buildAlgebraItem(Unit.INSTANCE).<Rational>performAlgebraTransfer("Q.zero").perform().getResult());
        for(boolean a : new boolean[]{false,true}) for(boolean b : new boolean[]{false,true}) {
            Boolean notAnd=m.booleans.algebra().buildAlgebraItem(a).performOperation("and",b).performOneOperandOperation("not").perform().getResult();
            Boolean deMorgan=m.booleans.algebra().buildAlgebraItem(!a).performOperation("or",!b).perform().getResult();
            assertEquals(notAnd,deMorgan);
        }
        RationalComplex i=new RationalComplex(Rational.ZERO,Rational.ONE);
        assertEquals(new RationalComplex(Rational.of(-1),Rational.ZERO),m.complexRationals.algebra().buildAlgebraItem(i).performOperation("multiply",i).perform().getResult());
        assertEquals(Rational.ONE,m.complexRationals.algebra().buildAlgebraItem(i).<Rational>performAlgebraTransfer("norm-squared").perform().getResult());
        assertEquals(Collections.singletonList("(2)+(0)i"),m.flow(m.rationals,Collections.singletonList(Rational.of(2)))
                .<RationalComplex>performAlgebraTransfer("Q(i).embed-rational").collect());
    }
    @Test public void rankSolveAndHigherDerivativesUseNativeOperationsAndRespectTheirDomains() {
        ConcreteMathematics m=new ConcreteMathematics();
        RationalMatrix a=new RationalMatrix(new Rational[][] {{Rational.ZERO,Rational.of(2)},{Rational.of(3),Rational.ONE}});
        RationalVector rhs=new RationalVector(Rational.of(4),Rational.of(5));
        IAlgebraItem<RationalVector> solution=m.matrices.algebra().buildAlgebraItem(a).performLeftProjectionOperation("solve",rhs);
        assertSame(m.vectors.algebra(),solution.getAlgebra());
        assertEquals(new RationalVector(Rational.ONE,Rational.of(2)),solution.perform().getResult());
        assertEquals(rhs,a.multiply(solution.getResult()));
        RationalMatrix singular=new RationalMatrix(new Rational[][] {{Rational.ONE,Rational.of(2)},{Rational.of(2),Rational.of(4)}});
        assertEquals(Arrays.asList("2","1","0"),m.flow(m.matrices,Arrays.asList(a,singular,
                new RationalMatrix(new Rational[][] {{Rational.ZERO,Rational.ZERO},{Rational.ZERO,Rational.ZERO}})))
                .<BigInteger>performAlgebraTransfer("rank").collect());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                () -> m.matrices.algebra().buildAlgebraItem(singular).performLeftProjectionOperation("solve",rhs)).kind());
        Polynomial p=new Polynomial(Rational.ONE,Rational.of(2),Rational.of(3));
        assertEquals(p,m.polynomials.algebra().buildAlgebraItem(p).performCustomMemberOperation("derivative-order",BigInteger.ZERO).getResult());
        assertEquals(Collections.singletonList("Q[x][6]"),m.flow(m.polynomials,Collections.singletonList(p))
                .performCustomMemberOperation("derivative-order",BigInteger.valueOf(2)).collect());
        assertEquals(new Polynomial(Rational.ZERO),m.polynomials.algebra().buildAlgebraItem(p)
                .performCustomMemberOperation("derivative-order",BigInteger.ONE.shiftLeft(100)).getResult());
        assertThrows(exceptions.NotMemberException.class,() -> m.polynomials.algebra().buildAlgebraItem(p)
                .performCustomMemberOperation("derivative-order",BigInteger.valueOf(-1)));
    }
}
