package mathematics;

import mathematics.core.*;
import mathematics.catalog.StandardMathematics;
import mathematics.numbers.*;
import org.junit.Test;
import java.math.BigInteger;
import java.util.*;
import static org.junit.Assert.*;
import static mathematics.core.MathStatus.Computation.*;
import static mathematics.core.MathStatus.Membership.*;

public class CoreTest {
    @Test public void domainCompatibilityUsesDefinitionsNotJavaClassesOrNames() {
        StandardMathematics math=new StandardMathematics();
        assertEquals(Rational.of(7),math.naturalToInteger.andThen(math.integerToRational).apply(BigInteger.valueOf(7)));
        UnaryOperation<BigInteger,BigInteger> wrong=new UnaryOperation<>(Metadata.of("idN","identity"),NumberDomains.NATURALS,NumberDomains.NATURALS,EXACT,false,n -> n);
        assertThrows(IllegalArgumentException.class,() -> math.naturalToInteger.andThen(wrong));
        Domain<BigInteger> imposter=new Domain<>(Metadata.of("Z","Different definition with same ID"),BigInteger.class,n -> NOT_MEMBER);
        assertFalse(imposter.compatibleWith(NumberDomains.INTEGERS));
        assertThrows(IllegalArgumentException.class,() -> math.catalog.addDomain(imposter));
    }
    @Test public void outcomesDistinguishFailureKindsAndDoNotInventProofs() {
        StandardMathematics math=new StandardMathematics();
        Outcome<Rational> undefined=math.divide.evaluate(Rational.ONE,Rational.ZERO);
        assertFalse(undefined.succeeded());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,undefined.failure().get().kind());
        assertEquals(MathFailure.Kind.INVALID_MEMBER,math.naturalToInteger.evaluate(BigInteger.valueOf(-1)).failure().get().kind());
        UnaryOperation<Rational,Rational> broken=new UnaryOperation<>(Metadata.of("broken","failure"),NumberDomains.RATIONALS,NumberDomains.RATIONALS,EXACT,false,x -> { throw new IllegalStateException("implementation bug"); });
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,broken.evaluate(Rational.ONE).failure().get().kind());
        UnaryOperation<Rational,Rational> numerical=new UnaryOperation<>(Metadata.of("numerical","failure"),NumberDomains.RATIONALS,NumberDomains.RATIONALS,NUMERICAL,true,x -> { throw new MathFailure(MathFailure.Kind.NUMERICAL_FAILURE,"solver failed"); });
        assertEquals(MathFailure.Kind.NUMERICAL_FAILURE,numerical.evaluate(Rational.ONE).failure().get().kind());
        assertEquals(MathStatus.Epistemic.DEFINED,math.add.evaluate(Rational.ONE,Rational.ONE).epistemicStatus());
    }
    @Test public void closureAndUnknownMembershipAreChecked() {
        Domain<String> undecided=new Domain<>(Metadata.of("unknown","Undecidable membership without witness"),String.class,v -> MathStatus.Membership.UNKNOWN);
        assertEquals(MathStatus.Membership.UNKNOWN,undecided.contains("object"));
        MathFailure failure=assertThrows(MathFailure.class,() -> undecided.require("object"));
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,failure.kind());
        UnaryOperation<BigInteger,BigInteger> nonclosed=new UnaryOperation<>(Metadata.of("bad","negative result"),NumberDomains.NATURALS,NumberDomains.NATURALS,EXACT,false,n -> BigInteger.valueOf(-1));
        assertEquals(MathFailure.Kind.INVALID_MEMBER,nonclosed.evaluate(BigInteger.ONE).failure().get().kind());
        assertNull(NumberDomains.NATURALS.algebra().buildAlgebraItem(BigInteger.valueOf(-1)));
    }
    @Test public void compositionKeepsAccuracyAndInspectableDependencies() {
        StandardMathematics math=new StandardMathematics();
        Outcome<SymbolicReal> result=math.naturalToInteger.andThen(math.integerToRational).andThen(math.rationalToRealExpression).evaluate(BigInteger.TEN);
        assertEquals(SYMBOLIC,result.computation());
        assertEquals(Arrays.asList("N-to-Z","Z-to-Q","Q-to-RealExpression"),result.trace());
        assertTrue(math.catalog.dependencyGraph().contains("\"Q^2\" -> \"operation:dot-Q2\""));
        assertThrows(IllegalArgumentException.class,() -> math.catalog.addOperation(math.add));
    }
    @Test public void flatListsPreserveDuplicatesAndValidateEveryMember() {
        FlatOperation<BigInteger,BigInteger,BigInteger> repeat=new FlatOperation<>(Metadata.of("repeat","list"),NumberDomains.NATURALS,NumberDomains.NATURALS,NumberDomains.NATURALS,EXACT,false,(a,b) -> Arrays.asList(a,a,b));
        assertEquals(Arrays.asList(BigInteger.ONE,BigInteger.ONE,BigInteger.TEN),repeat.apply(BigInteger.ONE,BigInteger.TEN));
        FlatOperation<BigInteger,BigInteger,BigInteger> bad=new FlatOperation<>(Metadata.of("badFlat","nonclosed"),NumberDomains.NATURALS,NumberDomains.NATURALS,NumberDomains.NATURALS,EXACT,false,(a,b) -> Arrays.asList(a,BigInteger.valueOf(-1)));
        assertEquals(MathFailure.Kind.INVALID_MEMBER,bad.evaluate(BigInteger.ONE,BigInteger.ONE).failure().get().kind());
    }
    @Test public void metadataAndLawDeclarationsAreInspectableAndImmutable() {
        StandardMathematics math=new StandardMathematics();
        Map<String,Domain<?>> carriers=new LinkedHashMap<>(); carriers.put("carrier",NumberDomains.RATIONALS);
        Map<String,DescribedOperation> operations=new LinkedHashMap<>(); operations.put("+",math.add);
        Structure.Law law=new Structure.Law("(a+b)+c=a+(b+c)",MathStatus.Epistemic.DEFINED,"Definition/law obligation, not a proof");
        Structure structure=new Structure(Metadata.of("additiveQ","Additive structure"),carriers,operations,Collections.singletonList(law));
        carriers.clear(); assertEquals(1,structure.carriers.size());
        assertEquals("UNREVIEWED",structure.metadata.provenance.fields().get("human_review_status"));
        assertEquals(MathStatus.Epistemic.DEFINED,structure.laws.get(0).status);
        assertThrows(java.lang.UnsupportedOperationException.class,() -> structure.carriers.clear());
        assertThrows(IllegalArgumentException.class,() -> new Structure(structure.metadata,Collections.emptyMap(),operations,Collections.singletonList(law)));
    }
}
