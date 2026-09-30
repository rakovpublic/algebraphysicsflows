package mathematics;
import mathematics.core.*;
import mathematics.numbers.*;
import mathematics.foundations.*;
import org.junit.Test;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;
public class FoundationsTest {
    @Test public void setOperationsRespectFiniteSetSemantics() {
        FiniteSet<Integer> a=FiniteSet.of(1,1,2),b=FiniteSet.of(2,3);
        assertEquals(2,a.size()); assertEquals(4,a.powerSet().size());
        assertEquals(FiniteSet.of(1,2,3),a.union(b)); assertEquals(FiniteSet.of(2),a.intersection(b));
        assertEquals(FiniteSet.of(1),a.difference(b)); assertEquals(FiniteSet.of(3),a.complementIn(a.union(b)));
        assertEquals(4,a.product(b).size()); assertEquals(FiniteSet.of(1),a.image(n -> 1));
        assertEquals(FiniteSet.of(2),a.preimage(n -> n*2,FiniteSet.of(4)));
        assertTrue(FiniteSet.<Integer>of().every(n -> false)); assertFalse(FiniteSet.<Integer>of().some(n -> true));
        assertThrows(MathFailure.class,() -> a.complementIn(b));
        assertEquals(BigInteger.valueOf(2),new Multiset<>(Arrays.asList(1,1,2)).count(1));
    }
    @Test public void relationsComposeAsObjects() {
        Domain<BigInteger> z=NumberDomains.INTEGERS;
        FiniteRelation<BigInteger,BigInteger> r=new FiniteRelation<>(z,z,FiniteSet.of(new Pair<>(BigInteger.ONE,BigInteger.TEN)));
        FiniteRelation<BigInteger,BigInteger> s=new FiniteRelation<>(z,z,FiniteSet.of(new Pair<>(BigInteger.TEN,BigInteger.ZERO)));
        assertTrue(r.andThen(s).relates(BigInteger.ONE,BigInteger.ZERO));
        assertTrue(r.inverse().relates(BigInteger.TEN,BigInteger.ONE));
        assertTrue(r.isTotalFunctionOn(FiniteSet.of(BigInteger.ONE)));
        assertFalse(r.isTotalFunctionOn(FiniteSet.of(BigInteger.ONE,BigInteger.ZERO)));
        FiniteRelation<BigInteger,BigInteger> wrong=new FiniteRelation<>(NumberDomains.NATURALS,z,FiniteSet.of());
        assertThrows(IllegalArgumentException.class,() -> r.andThen(wrong));
    }
    @Test public void higherOrderFunctionsAndProductDomains() {
        MathFunction<BigInteger,Rational> f=new MathFunction<>(new mathematics.catalog.StandardMathematics().integerToRational);
        Domain<MathFunction<BigInteger,Rational>> functions=FoundationDomains.functions("ZtoQ",NumberDomains.INTEGERS,NumberDomains.RATIONALS);
        assertEquals(MathStatus.Membership.MEMBER,functions.contains(f));
        UnaryOperation<MathFunction<BigInteger,Rational>,Rational> functional=new UnaryOperation<>(Metadata.of("at-ten","Evaluation functional"),functions,NumberDomains.RATIONALS,MathStatus.Computation.EXACT,false,g -> g.apply(BigInteger.TEN));
        assertEquals(Rational.of(10),functional.apply(f));
        Domain<Pair<BigInteger,Rational>> product=FoundationDomains.product("NxQ",NumberDomains.NATURALS,NumberDomains.RATIONALS);
        assertEquals(MathStatus.Membership.MEMBER,product.contains(new Pair<>(BigInteger.ONE,Rational.ONE)));
        assertEquals(MathStatus.Membership.NOT_MEMBER,product.contains(new Pair<>(BigInteger.valueOf(-1),Rational.ONE)));
        assertEquals(MathStatus.Membership.NOT_MEMBER,FoundationDomains.empty("emptyZ",BigInteger.class).contains(BigInteger.ZERO));
    }
    @Test public void infiniteSequencesAreEvaluatedOnlyAtRequestedIndices() {
        AtomicInteger count=new AtomicInteger();
        LazySequence<BigInteger> squares=new LazySequence<>(NumberDomains.NATURALS,n -> { count.incrementAndGet(); return n.multiply(n); });
        assertEquals(0,count.get());
        assertEquals(Arrays.asList(BigInteger.ZERO,BigInteger.ONE,BigInteger.valueOf(4)),squares.prefix(3));
        assertEquals(3,count.get());
        assertEquals(BigInteger.ONE.shiftLeft(200),squares.at(BigInteger.ONE.shiftLeft(100)));
        assertThrows(MathFailure.class,() -> squares.at(BigInteger.valueOf(-1)));
    }
}
