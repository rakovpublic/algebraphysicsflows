package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.*;
import mathematics.structures.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static operations.FiniteCategoryFixtures.*;

public class NativeFunctorTest {
    private static IAlgebraItem<FiniteFunctor> item(ConcreteMathematics m,FiniteFunctor f) { return m.functors.algebra().buildAlgebraItem(f); }
    @Test public void constructionChecksEveryFunctorObligationAndCopiesInputMaps() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory d=discrete(0,1),c2=cyclic(2),c3=cyclic(3);
        Map<BigInteger,BigInteger> objects=map(0,1,1,0),arrows=map(0,1,1,0);
        FiniteFunctor swap=new FiniteFunctor(d,d,objects,arrows);
        objects.clear(); arrows.clear(); assertEquals(n(1),swap.mapObject(n(0))); assertEquals(n(0),swap.mapArrow(n(1)));
        assertThrows(UnsupportedOperationException.class,() -> swap.objectMap().clear());
        assertThrows(UnsupportedOperationException.class,() -> swap.arrowMap().clear());
        assertThrows(MathFailure.class,() -> new FiniteFunctor(d,d,map(0,0),map(0,0,1,1)));
        assertThrows(MathFailure.class,() -> new FiniteFunctor(d,d,map(0,0,1,1),map(0,0,1,1,2,1)));
        assertThrows(MathFailure.class,() -> new FiniteFunctor(d,d,map(0,0,1,99),map(0,0,1,1)));
        assertThrows(MathFailure.class,() -> new FiniteFunctor(d,d,map(0,0,1,1),map(0,0,1,99)));
        FiniteCategory chain=relationCategory(m,false,0,1);
        assertThrows(MathFailure.class,() -> new FiniteFunctor(chain,chain,map(0,0,1,1),map(0,0,1,0,2,2)));
        assertThrows(MathFailure.class,() -> new FiniteFunctor(c2,c2,map(0,0),map(0,1,1,0)));
        assertThrows(MathFailure.class,() -> new FiniteFunctor(c2,c3,map(0,0),map(0,0,1,1)));
        FiniteCategory idem=monoid(new int[][]{{0,1},{1,1}},0);
        assertNotEquals(new FiniteFunctor(c2,c2,map(0,0),map(0,0,1,0)),new FiniteFunctor(c2,idem,map(0,0),map(0,0,1,0)));
        assertEquals(swap,new FiniteFunctor(discrete(1,0),discrete(1,0),map(1,0,0,1),map(1,0,0,1)));
    }
    @Test public void allSmallCyclicGroupMapsAreAcceptedExactlyWhenTheyPreserveAddition() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int from=2;from<=4;from++) for(int to=2;to<=4;to++) {
            FiniteCategory source=cyclic(from),target=cyclic(to); int count=1;
            for(int i=1;i<from;i++) count*=to;
            for(int code=0;code<count;code++) {
                int[] images=new int[from]; int digits=code; Map<BigInteger,BigInteger> mapping=map(0,0);
                for(int i=1;i<from;i++) { images[i]=digits%to; digits/=to; mapping.put(n(i),n(images[i])); }
                boolean preserves=true;
                for(int a=0;a<from;a++) for(int b=0;b<from;b++) if(images[(a+b)%from]!=(images[a]+images[b])%to) preserves=false;
                if(!preserves) { assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,
                        () -> new FiniteFunctor(source,target,map(0,0),mapping)).kind()); continue; }
                FiniteFunctor f=new FiniteFunctor(source,target,map(0,0),mapping);
                Set<Integer> distinct=new HashSet<>(); for(int image : images) distinct.add(image);
                assertEquals(distinct.size()==from,item(m,f).<Boolean>performAlgebraTransfer("is-faithful").getResult());
                assertEquals(distinct.size()==to,item(m,f).<Boolean>performAlgebraTransfer("is-full").getResult());
                assertTrue(item(m,f).<Boolean>performAlgebraTransfer("is-essentially-surjective").getResult());
                assertEquals(from==to && distinct.size()==to,f.isEquivalence());
                for(int a=0;a<from;a++) assertEquals(n(images[a]),item(m,f).performLeftProjectionOperation("map-arrow",n(a)).getResult());
                if(f.isIsomorphism()) assertEquals(FiniteFunctor.identity(source),item(m,f).performOneOperandOperation("inverse").performOperation("compose",f).perform().getResult());
                else assertThrows(MathFailure.class,() -> item(m,f).performOneOperandOperation("inverse"));
            }
        }
    }
    @Test public void faithfulnessIsLocalAndEquivalenceDoesNotRequireStrictBijections() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory point=discrete(0);
        FiniteFunctor collapsedDiscrete=new FiniteFunctor(discrete(0,1),point,map(0,0,1,0),map(0,0,1,0));
        assertTrue(collapsedDiscrete.isFaithful()); assertFalse(collapsedDiscrete.isFull()); assertTrue(collapsedDiscrete.isEssentiallySurjective());
        FiniteCategory connected=relationCategory(m,true,0,1);
        FiniteFunctor collapse=new FiniteFunctor(connected,point,map(0,0,1,0),map(0,0,1,0,2,0,3,0));
        assertTrue(item(m,collapse).<Boolean>performAlgebraTransfer("is-equivalence").getResult()); assertFalse(collapse.isIsomorphism());
        assertThrows(MathFailure.class,collapse::inverse);
        FiniteFunctor inclusion=new FiniteFunctor(point,connected,map(0,0),map(0,0));
        assertTrue(inclusion.isEquivalence()); assertFalse(inclusion.isIsomorphism());
        assertTrue(inclusion.objectFiber(n(1)).isEmpty());
        FiniteFunctor missesComponent=new FiniteFunctor(point,discrete(0,1),map(0,0),map(0,0));
        assertTrue(missesComponent.isFull()); assertTrue(missesComponent.isFaithful()); assertFalse(missesComponent.isEssentiallySurjective());
        FiniteCategory empty=discrete();
        assertTrue(FiniteFunctor.identity(empty).isEquivalence()); assertTrue(FiniteFunctor.identity(empty).isIsomorphism());
        FiniteFunctor fromEmpty=new FiniteFunctor(empty,point,map(),map());
        assertTrue(fromEmpty.isFull()); assertTrue(fromEmpty.isFaithful()); assertFalse(fromEmpty.isEssentiallySurjective());
    }
    @Test public void allTwoObjectDiscreteEndofunctorsComposeInTheDeclaredOrder() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory d=discrete(0,1);
        List<FiniteFunctor> functions=new ArrayList<>();
        for(int code=0;code<4;code++) functions.add(new FiniteFunctor(d,d,map(0,code%2,1,code/2),map(0,code%2,1,code/2)));
        int[][] products=new int[4][4];
        for(int a=0;a<4;a++) for(int b=0;b<4;b++) {
            int[] first={a%2,a/2},second={b%2,b/2}; int result=first[second[0]]+2*first[second[1]];
            products[a][b]=result;
            assertEquals(functions.get(result),item(m,functions.get(a)).performOperation("compose",functions.get(b)).perform().getResult());
            assertEquals(functions.get(a),functions.get(a).opposite().opposite());
        }
        for(int a=0;a<4;a++) for(int b=0;b<4;b++) for(int c=0;c<4;c++) assertEquals(products[products[a][b]][c],products[a][products[b][c]]);
        assertThrows(MathFailure.class,() -> functions.get(0).compose(FiniteFunctor.identity(discrete(10,20))));
        assertEquals(functions.get(1),functions.get(1).compose(FiniteFunctor.identity(discrete(1,0))));
    }
    @Test public void nativeWrappersTransfersAndFlatFibersRetainTheirActualAlgebras() {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteFunctor f=new FiniteFunctor(discrete(0,1),discrete(10,20),map(0,10,1,10),map(0,10,1,10));
        IAlgebraItem<BigInteger> mapped=item(m,f).performLeftProjectionOperation("map-object",n(1));
        assertSame(m.integers.algebra(),mapped.getAlgebra()); assertEquals(n(10),mapped.getResult());
        IAlgebraItem<FiniteFunction<BigInteger,BigInteger>> objectMap=item(m,f).performAlgebraTransfer("object-map");
        assertSame(m.integerFunctions.algebra(),objectMap.getAlgebra()); assertSame(m.integers.algebra(),objectMap.getResult().source);
        assertEquals(Arrays.asList("-10","-10"),m.flow(m.functors,Collections.singletonList(f)).<BigInteger>performFlatAlgebraTransfer("object-images").performOneOperandOperation("negate").collect());
        assertEquals(Arrays.asList("1","2"),m.flow(m.functors,Collections.singletonList(f)).performLeftProjectionFlatOperation("arrow-fiber",n(10)).performOperation("add",n(1)).collect());
        assertTrue(m.flow(m.functors,Collections.singletonList(f)).performLeftProjectionFlatOperation("object-fiber",n(20)).collect().isEmpty());
        assertThrows(MathFailure.class,() -> item(m,f).performLeftProjectionOperation("map-object",n(99)));
        assertThrows(MathFailure.class,() -> item(m,f).performLeftProjectionOperation("map-arrow",n(99)));
        assertThrows(MathFailure.class,() -> item(m,f).performLeftProjectionFlatOperation("object-fiber",n(99)));
        assertEquals(Collections.singletonList("10"),m.flow(m.integerFunctions,Collections.singletonList(objectMap.getResult()))
                .<FiniteFunctor>performAlgebraTransfer("FiniteFunctor.from-discrete-map").performLeftProjectionOperation("map-arrow",n(0)).collect());
        assertEquals(Collections.singletonList("2"),m.flow(m.functors,Collections.singletonList(f)).<FiniteCategory>performAlgebraTransfer("target")
                .<BigInteger>performAlgebraTransfer("object-count").collect());
    }
    @Test public void serializedFunctorFlowsAndCategoryResourceLimitsRemainExplicit() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteFunctor f=new FiniteFunctor(discrete(0,1),discrete(10,20),map(0,10,1,20),map(0,10,1,20));
        IAlgebraFlow<BigInteger> flow=m.flow(m.functors,Collections.singletonList(f)).performOneOperandOperation("inverse")
                .performOneOperandOperation("opposite").performLeftProjectionOperation("map-arrow",n(20)).performOperation("add",n(2));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("3"),restored.collect()); assertEquals(Collections.singletonList("3"),restored.collect());
        Map<BigInteger,BigInteger> mapping=new LinkedHashMap<>(); for(int i=0;i<=128;i++) mapping.put(n(i),n(0));
        FiniteFunction<BigInteger,BigInteger> large=m.integerFunctions.member(new FiniteSet<>(mapping.keySet()),set(0),mapping);
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,() ->
                m.integerFunctions.algebra().buildAlgebraItem(large).performAlgebraTransfer("FiniteFunctor.from-discrete-map")).kind());
    }
}
