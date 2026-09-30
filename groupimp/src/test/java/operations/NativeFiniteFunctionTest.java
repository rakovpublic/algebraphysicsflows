package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeFiniteFunctionTest {
    private static BigInteger n(int value) { return BigInteger.valueOf(value); }
    private static FiniteSet<BigInteger> set(int... values) {
        List<BigInteger> result=new ArrayList<>();
        for(int value : values) result.add(n(value));
        return new FiniteSet<>(result);
    }
    private static FiniteFunction<BigInteger,BigInteger> function(ConcreteMathematics m,int[] source,int[] target,int... images) {
        Map<BigInteger,BigInteger> result=new LinkedHashMap<>();
        for(int i=0;i<source.length;i++) result.put(n(source[i]),n(images[i]));
        return m.integerFunctions.member(set(source),set(target),result);
    }
    private static IAlgebraItem<FiniteFunction<BigInteger,BigInteger>> item(ConcreteMathematics m,FiniteFunction<BigInteger,BigInteger> f) {
        return m.integerFunctions.algebra().buildAlgebraItem(f);
    }
    @Test public void declaredDomainsAndCodomainsAreValidatedAndPartOfEquality() {
        ConcreteMathematics m=new ConcreteMathematics();
        Map<BigInteger,BigInteger> values=new LinkedHashMap<>(); values.put(n(1),n(2));
        FiniteFunction<BigInteger,BigInteger> f=m.integerFunctions.member(set(1),set(2,3),values);
        values.put(n(1),n(3)); assertEquals(n(2),f.apply(n(1)));
        assertThrows(UnsupportedOperationException.class,() -> f.mapping().put(n(1),n(3)));
        assertTrue(f.isInjective()); assertFalse(f.isSurjective());
        FiniteFunction<BigInteger,BigInteger> onto=function(m,new int[]{1},new int[]{2},2);
        assertEquals(f.graph(),onto.graph()); assertNotEquals(f,onto);
        assertThrows(MathFailure.class,() -> m.integerFunctions.member(set(1,2),set(2,3),values));
        assertThrows(MathFailure.class,() -> m.integerFunctions.member(set(),set(2,3),values));
        assertThrows(MathFailure.class,() -> m.integerFunctions.member(set(1),set(2),values));
        assertThrows(MathFailure.class,() -> m.integerFunctions.member(set(1),set(),values));
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,() -> f.apply(n(2))).kind());
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,f::inverse).kind());
        FiniteFunction<BigInteger,BigInteger> empty=function(m,new int[]{},new int[]{});
        assertTrue(empty.isBijective()); assertEquals(empty,empty.inverse());
        assertTrue(function(m,new int[]{},new int[]{1}).isInjective());
        assertFalse(function(m,new int[]{},new int[]{1}).isSurjective());
        ConcreteMathematics foreign=new ConcreteMathematics();
        assertNull(m.integerFunctions.algebra().buildAlgebraItem(FiniteFunction.identity(foreign.integers.algebra(),set(1))));
    }
    @Test public void everyMapBetweenSetsOfAtMostThreePointsMatchesIndependentValuesAndFibers() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int sourceSize=0;sourceSize<=3;sourceSize++) for(int targetSize=0;targetSize<=3;targetSize++) {
            int[] source=new int[sourceSize],target=new int[targetSize];
            for(int i=0;i<sourceSize;i++) source[i]=i;
            for(int i=0;i<targetSize;i++) target[i]=10+i;
            int count=1; for(int i=0;i<sourceSize;i++) count*=targetSize;
            for(int code=0;code<count;code++) {
                int[] images=new int[sourceSize]; int digits=code;
                Set<Integer> reached=new HashSet<>(); List<BigInteger> expectedValues=new ArrayList<>();
                for(int i=0;i<sourceSize;i++) { images[i]=10+digits%targetSize; digits/=targetSize; reached.add(images[i]); expectedValues.add(n(images[i])); }
                FiniteFunction<BigInteger,BigInteger> f=function(m,source,target,images);
                assertEquals(sourceSize==reached.size(),item(m,f).<Boolean>performAlgebraTransfer("is-injective").getResult());
                assertEquals(targetSize==reached.size(),item(m,f).<Boolean>performAlgebraTransfer("is-surjective").getResult());
                List<BigInteger> actualValues=new ArrayList<>();
                for(IAlgebraItem<BigInteger> value : item(m,f).<BigInteger>performAlgebraFlatTransfer("values")) {
                    assertSame(m.integers.algebra(),value.getAlgebra()); actualValues.add(value.getResult());
                }
                assertEquals(expectedValues,actualValues);
                for(int i=0;i<sourceSize;i++) {
                    IAlgebraItem<BigInteger> value=item(m,f).performLeftProjectionOperation("apply",n(i));
                    assertSame(m.integers.algebra(),value.getAlgebra()); assertEquals(n(images[i]),value.getResult());
                }
                for(int value : target) {
                    List<BigInteger> expected=new ArrayList<>(),actual=new ArrayList<>();
                    for(int i=0;i<sourceSize;i++) if(images[i]==value) expected.add(n(i));
                    for(IAlgebraItem<BigInteger> point : item(m,f).performLeftProjectionFlatOperation("preimage-of",n(value))) {
                        assertSame(m.integers.algebra(),point.getAlgebra()); actual.add(point.getResult());
                    }
                    assertEquals(expected,actual);
                    IAlgebraItem<FiniteSet<BigInteger>> preimage=item(m,f).performLeftProjectionOperation("preimage",set(value));
                    assertSame(m.integerSets.algebra(),preimage.getAlgebra()); assertEquals(new FiniteSet<>(expected),preimage.getResult());
                }
                if(f.isBijective()) assertEquals(FiniteFunction.identity(m.integers.algebra(),set(source)),
                        item(m,f).performOneOperandOperation("inverse").performOperation("compose",f).perform().getResult());
                else assertThrows(MathFailure.class,() -> item(m,f).performOneOperandOperation("inverse"));
            }
        }
    }
    @Test public void allThreePointEndomapsHaveTheExpectedCompositionAndAssociativity() {
        ConcreteMathematics m=new ConcreteMathematics();
        List<FiniteFunction<BigInteger,BigInteger>> maps=new ArrayList<>();
        for(int code=0;code<27;code++) maps.add(function(m,new int[]{0,1,2},new int[]{0,1,2},code%3,(code/3)%3,code/9));
        int[][] products=new int[27][27];
        for(int a=0;a<27;a++) for(int b=0;b<27;b++) {
            FiniteFunction<BigInteger,BigInteger> first=maps.get(a),second=maps.get(b);
            int expected=0,factor=1;
            int[] firstImages={a%3,(a/3)%3,a/9},secondImages={b%3,(b/3)%3,b/9};
            for(int x=0;x<3;x++) { expected+=factor*firstImages[secondImages[x]]; factor*=3; }
            FiniteFunction<BigInteger,BigInteger> result=item(m,first).performOperation("compose",second).perform().getResult();
            assertEquals(maps.get(expected),result); products[a][b]=expected;
            assertEquals(second.graph().andThen(first.graph()),result.graph());
        }
        for(int a=0;a<27;a++) for(int b=0;b<27;b++) for(int c=0;c<27;c++)
            assertEquals(products[products[a][b]][c],products[a][products[b][c]]);
    }
    @Test public void compositionAndRelationConversionRespectExplicitFiniteBoundaries() {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteFunction<BigInteger,BigInteger> f=function(m,new int[]{1,2},new int[]{10,20},10,20);
        FiniteFunction<BigInteger,BigInteger> g=function(m,new int[]{10,20},new int[]{30},30,30);
        assertEquals(function(m,new int[]{1,2},new int[]{30},30,30),item(m,g).performOperation("compose",f).perform().getResult());
        FiniteFunction<BigInteger,BigInteger> wider=function(m,new int[]{1,2},new int[]{10,20,99},10,20);
        assertThrows(MathFailure.class,() -> item(m,g).performOperation("compose",wider).perform());
        assertThrows(MathFailure.class,() -> item(m,f).performOperation("compose",g).perform());
        Pair<FiniteSet<BigInteger>,FiniteSet<BigInteger>> ends=new Pair<>(set(1,2),set(10,20));
        IAlgebraItem<FiniteFunction<BigInteger,BigInteger>> converted=m.integerRelations.algebra().buildAlgebraItem(f.graph())
                .performUnsafeOperation("FiniteFunction(Z,Z).from-relation",ends);
        assertSame(m.integerFunctions.algebra(),converted.getAlgebra()); assertEquals(f,converted.getResult());
        assertThrows(MathFailure.class,() -> FiniteFunction.fromRelation(f.graph(),set(1),set(10,20)));
        assertThrows(MathFailure.class,() -> FiniteFunction.fromRelation(f.graph(),set(1,2,3),set(10,20)));
        assertThrows(MathFailure.class,() -> FiniteFunction.fromRelation(f.graph(),set(1,2),set(10)));
        FiniteRelation<BigInteger,BigInteger> multivalued=new FiniteRelation<>(m.integers.algebra(),m.integers.algebra(),
                FiniteSet.of(new Pair<>(n(1),n(10)),new Pair<>(n(1),n(20))));
        assertThrows(MathFailure.class,() -> m.integerRelations.algebra().buildAlgebraItem(multivalued)
                .performUnsafeOperation("FiniteFunction(Z,Z).from-relation",new Pair<>(set(1),set(10,20))));
        assertThrows(MathFailure.class,() -> item(m,f).performLeftProjectionOperation("image",set(99)));
        assertThrows(MathFailure.class,() -> item(m,f).performLeftProjectionOperation("preimage",set(99)));
        assertThrows(MathFailure.class,() -> item(m,f).performLeftProjectionFlatOperation("preimage-of",n(99)));
        assertThrows(MathFailure.class,() -> item(m,f).performCustomMemberOperation("restrict",set(99)));
        FiniteFunction<BigInteger,BigInteger> restricted=item(m,f).performCustomMemberOperation("restrict",set(1)).getResult();
        assertEquals(set(10,20),restricted.codomain); assertFalse(restricted.isSurjective());
    }
    @Test public void nativeFlowsComposeEvaluateRestrictAndRetainRepeatedValues() {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteFunction<BigInteger,BigInteger> f=function(m,new int[]{1,2,3},new int[]{10,20,30},10,10,20);
        assertEquals(Arrays.asList("-10","-10","-20"),m.flow(m.integerFunctions,Collections.singletonList(f))
                .<BigInteger>performFlatAlgebraTransfer("values").performOneOperandOperation("negate").collect());
        assertEquals(Arrays.asList("2","3"),m.flow(m.integerFunctions,Collections.singletonList(f))
                .performLeftProjectionFlatOperation("preimage-of",n(10)).performOperation("add",n(1)).collect());
        assertTrue(m.flow(m.integerFunctions,Collections.singletonList(f)).performLeftProjectionFlatOperation("preimage-of",n(30)).collect().isEmpty());
        assertEquals(Collections.singletonList("20"),m.flow(m.integerFunctions,Collections.singletonList(f))
                .performCustomMemberOperation("restrict",set(3)).performLeftProjectionOperation("apply",n(3)).collect());
        assertEquals(Collections.singletonList("1"),m.flow(m.integerSets,Collections.singletonList(set(1,2)))
                .<FiniteFunction<BigInteger,BigInteger>>performAlgebraTransfer("FiniteFunction(Z,Z).identity-on")
                .performLeftProjectionOperation("apply",n(1)).collect());
    }
    @Test public void serializedFunctionFlowRetainsCarrierIdentityAndCanBeCollectedAgain() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteFunction<BigInteger,BigInteger> f=function(m,new int[]{1,2},new int[]{10,20},10,20);
        IAlgebraFlow<BigInteger> flow=m.flow(m.integerFunctions,Collections.singletonList(f))
                .<FiniteRelation<BigInteger,BigInteger>>performAlgebraTransfer("graph")
                .<FiniteFunction<BigInteger,BigInteger>,Pair<FiniteSet<BigInteger>,FiniteSet<BigInteger>>>performAlgebraUnsafe(
                        "FiniteFunction(Z,Z).from-relation",new Pair<>(set(1,2),set(10,20)))
                .performOneOperandOperation("inverse").performLeftProjectionOperation("apply",n(20));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("2"),restored.collect()); assertEquals(Collections.singletonList("2"),restored.collect());
    }
    @Test public void finiteFunctionValuesAlsoSupportDifferentSourceAndTargetMemberClasses() {
        ConcreteMathematics m=new ConcreteMathematics();
        Map<Boolean,BigInteger> values=new LinkedHashMap<>(); values.put(false,n(0)); values.put(true,n(1));
        FiniteFunction<Boolean,BigInteger> encode=new FiniteFunction<>(m.booleans.algebra(),m.integers.algebra(),
                FiniteSet.of(false,true),set(0,1),values);
        FiniteFunction<BigInteger,Boolean> decode=encode.inverse();
        assertSame(m.booleans.algebra(),decode.target);
        assertEquals(Boolean.TRUE,decode.apply(n(1)));
        assertEquals(FiniteFunction.identity(m.booleans.algebra(),FiniteSet.of(false,true)),decode.compose(encode));
        assertEquals(encode,FiniteFunction.fromRelation(encode.graph(),encode.domain,encode.codomain));
        ConcreteMathematics foreign=new ConcreteMathematics();
        assertThrows(MathFailure.class,() -> encode.andThen(FiniteFunction.identity(foreign.integers.algebra(),set(0,1))));
    }
}
