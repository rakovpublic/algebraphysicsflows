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

public class NativeNaturalTransformationTest {
    private static IAlgebraItem<FiniteNaturalTransformation> item(ConcreteMathematics m,FiniteNaturalTransformation t) {
        return m.naturalTransformations.algebra().buildAlgebraItem(t);
    }
    private static FiniteNaturalTransformation arrowBetweenConstants(ConcreteMathematics m) {
        FiniteCategory point=discrete(9),chain=relationCategory(m,false,0,1);
        FiniteFunctor f=new FiniteFunctor(point,chain,map(9,0),map(9,0));
        FiniteFunctor g=new FiniteFunctor(point,chain,map(9,1),map(9,2));
        return new FiniteNaturalTransformation(f,g,map(9,1));
    }
    @Test public void componentsAreTotalTypedImmutableAndBoundToParallelFunctors() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteNaturalTransformation eta=arrowBetweenConstants(m);
        Map<BigInteger,BigInteger> components=map(9,1);
        FiniteNaturalTransformation copy=new FiniteNaturalTransformation(eta.source,eta.target,components);
        components.clear(); assertEquals(eta,copy); assertEquals(n(1),copy.component(n(9)));
        assertThrows(UnsupportedOperationException.class,() -> eta.componentMap().clear());
        assertThrows(MathFailure.class,() -> new FiniteNaturalTransformation(eta.source,eta.target,map()));
        assertThrows(MathFailure.class,() -> new FiniteNaturalTransformation(eta.source,eta.target,map(9,1,8,1)));
        assertThrows(MathFailure.class,() -> new FiniteNaturalTransformation(eta.source,eta.target,map(9,99)));
        assertThrows(MathFailure.class,() -> new FiniteNaturalTransformation(eta.source,eta.target,map(9,0)));
        assertThrows(MathFailure.class,() -> new FiniteNaturalTransformation(eta.source,FiniteFunctor.identity(discrete(9)),map(9,1)));
        assertThrows(MathFailure.class,() -> eta.component(n(99)));
        assertFalse(eta.isIsomorphism()); assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,eta::inverse).kind());
        assertEquals(eta.target.opposite(),eta.opposite().source); assertEquals(eta.source.opposite(),eta.opposite().target);
        assertEquals(eta,eta.opposite().opposite());
    }
    @Test public void naturalityIsCheckedAgainstIndependentNoncommutativeMapComposition() {
        ConcreteMathematics m=new ConcreteMathematics(); int[][] multiplication=new int[4][4];
        for(int a=0;a<4;a++) for(int b=0;b<4;b++) {
            int[] f={a%2,a/2},g={b%2,b/2}; multiplication[a][b]=g[f[0]]+2*g[f[1]];
        }
        FiniteCategory c2=cyclic(2),maps=monoid(multiplication,2);
        for(int fGenerator : new int[]{1,2}) for(int gGenerator : new int[]{1,2}) for(int component=0;component<4;component++) {
            FiniteFunctor f=new FiniteFunctor(c2,maps,map(0,0),map(0,2,1,fGenerator));
            FiniteFunctor g=new FiniteFunctor(c2,maps,map(0,0),map(0,2,1,gGenerator));
            int[] first={fGenerator%2,fGenerator/2},second={gGenerator%2,gGenerator/2},eta={component%2,component/2};
            boolean natural=true; for(int point=0;point<2;point++) if(eta[first[point]]!=second[eta[point]]) natural=false;
            final Map<BigInteger,BigInteger> components=map(0,component);
            if(!natural) { assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,
                    () -> new FiniteNaturalTransformation(f,g,components)).kind()); continue; }
            FiniteNaturalTransformation t=new FiniteNaturalTransformation(f,g,components);
            assertEquals(component==1 || component==2,item(m,t).<Boolean>performAlgebraTransfer("is-isomorphism").getResult());
            assertEquals(n(component),item(m,t).performLeftProjectionOperation("component",n(0)).getResult());
            if(t.isIsomorphism()) assertEquals(FiniteNaturalTransformation.identity(f),t.inverse().compose(t));
        }
        FiniteFunctor trivial=new FiniteFunctor(c2,maps,map(0,0),map(0,2,1,2));
        FiniteNaturalTransformation constant=new FiniteNaturalTransformation(trivial,trivial,map(0,3));
        FiniteNaturalTransformation swap=new FiniteNaturalTransformation(trivial,trivial,map(0,1));
        assertEquals(n(0),item(m,swap).performOperation("compose",constant).perform().getResult().component(n(0)));
        assertEquals(n(3),item(m,constant).performOperation("compose",swap).perform().getResult().component(n(0)));
    }
    @Test public void horizontalVerticalCompositionAndInterchangeAgreeWithCyclicArithmetic() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory c=cyclic(3);
        FiniteNaturalTransformation[][] transformations=new FiniteNaturalTransformation[3][3];
        for(int multiplier=0;multiplier<3;multiplier++) {
            FiniteFunctor f=new FiniteFunctor(c,c,map(0,0),map(0,0,1,multiplier,2,(2*multiplier)%3));
            for(int component=0;component<3;component++) transformations[multiplier][component]=new FiniteNaturalTransformation(f,f,map(0,component));
        }
        for(int k=0;k<3;k++) for(int a=0;a<3;a++) for(int b=0;b<3;b++) {
            FiniteNaturalTransformation first=transformations[k][a],second=transformations[k][b];
            assertEquals(transformations[k][(a+b)%3],item(m,second).performOperation("compose",first).perform().getResult());
            assertEquals(transformations[k][(3-a)%3],first.inverse());
            for(int d=0;d<3;d++) assertEquals(first.andThen(second).andThen(transformations[k][d]),first.andThen(second.andThen(transformations[k][d])));
        }
        for(int k=0;k<3;k++) for(int l=0;l<3;l++) for(int a=0;a<3;a++) for(int b=0;b<3;b++) {
            FiniteNaturalTransformation alpha=transformations[k][a],beta=transformations[l][b];
            assertEquals(transformations[(k*l)%3][(l*a+b)%3],item(m,alpha).performOperation("horizontal",beta).perform().getResult());
            for(int a2=0;a2<3;a2++) for(int b2=0;b2<3;b2++) {
                FiniteNaturalTransformation alpha2=transformations[k][a2],beta2=transformations[l][b2];
                assertEquals(alpha.andThen(alpha2).horizontal(beta.andThen(beta2)),alpha.horizontal(beta).andThen(alpha2.horizontal(beta2)));
            }
        }
    }
    @Test public void precompositionPostcompositionAndOppositesRetainTheRequiredBoundaries() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteNaturalTransformation eta=arrowBetweenConstants(m);
        FiniteFunctor before=new FiniteFunctor(discrete(7,8),discrete(9),map(7,9,8,9),map(7,9,8,9));
        Map<BigInteger,Pair<BigInteger,BigInteger>> arrows=new LinkedHashMap<>();
        arrows.put(n(10),new Pair<>(n(10),n(10))); arrows.put(n(11),new Pair<>(n(10),n(20))); arrows.put(n(12),new Pair<>(n(20),n(20)));
        Map<Pair<BigInteger,BigInteger>,BigInteger> table=new LinkedHashMap<>();
        table.put(new Pair<>(n(10),n(10)),n(10)); table.put(new Pair<>(n(10),n(11)),n(11));
        table.put(new Pair<>(n(11),n(12)),n(11)); table.put(new Pair<>(n(12),n(12)),n(12));
        FiniteCategory relabelled=new FiniteCategory(set(10,20),arrows,map(10,10,20,12),table);
        FiniteFunctor after=new FiniteFunctor(eta.source.target,relabelled,map(0,10,1,20),map(0,10,1,11,2,12));
        FiniteNaturalTransformation result=item(m,eta).performCustomMemberOperation("precompose",before).performCustomMemberOperation("postcompose",after).getResult();
        assertEquals(map(7,11,8,11),result.componentMap());
        assertEquals(after.compose(eta.source).compose(before),result.source);
        assertEquals(after.compose(eta.target).compose(before),result.target);
        assertEquals(result,eta.postcompose(after).precompose(before));
        assertEquals(eta.precompose(before),FiniteNaturalTransformation.identity(before).horizontal(eta));
        assertEquals(eta.postcompose(after),eta.horizontal(FiniteNaturalTransformation.identity(after)));
        assertThrows(MathFailure.class,() -> eta.precompose(FiniteFunctor.identity(eta.source.target)));
        assertThrows(MathFailure.class,() -> eta.postcompose(FiniteFunctor.identity(eta.source.source)));
        assertThrows(MathFailure.class,() -> eta.compose(eta));
        assertThrows(MathFailure.class,() -> eta.horizontal(eta));
        assertEquals(eta,eta.compose(FiniteNaturalTransformation.identity(eta.source)));
        assertEquals(eta,FiniteNaturalTransformation.identity(eta.target).compose(eta));
    }
    @Test public void wrappersFlatComponentsFibersAndFunctionTransfersUseTheNativeCarriers() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteNaturalTransformation eta=arrowBetweenConstants(m);
        FiniteFunctor before=new FiniteFunctor(discrete(7,8),discrete(9),map(7,9,8,9),map(7,9,8,9));
        FiniteNaturalTransformation repeated=eta.precompose(before);
        IAlgebraItem<BigInteger> component=item(m,repeated).performLeftProjectionOperation("component",n(7));
        assertSame(m.integers.algebra(),component.getAlgebra()); assertEquals(n(1),component.getResult());
        IAlgebraItem<FiniteFunction<BigInteger,BigInteger>> function=item(m,repeated).performAlgebraTransfer("component-map");
        assertSame(m.integerFunctions.algebra(),function.getAlgebra()); assertSame(m.integers.algebra(),function.getResult().source);
        assertEquals(set(7,8),function.getResult().domain); assertEquals(set(0,1,2),function.getResult().codomain);
        assertEquals(Arrays.asList("-1","-1"),m.flow(m.naturalTransformations,Collections.singletonList(repeated))
                .<BigInteger>performFlatAlgebraTransfer("components").performOneOperandOperation("negate").collect());
        assertEquals(Arrays.asList("7","8"),m.flow(m.naturalTransformations,Collections.singletonList(repeated))
                .performLeftProjectionFlatOperation("component-fiber",n(1)).collect());
        assertTrue(item(m,repeated).performLeftProjectionFlatOperation("component-fiber",n(0)).isEmpty());
        assertThrows(MathFailure.class,() -> item(m,repeated).performLeftProjectionFlatOperation("component-fiber",n(99)));
        assertEquals(Collections.singletonList("2"),m.flow(m.functors,Collections.singletonList(eta.target))
                .<FiniteNaturalTransformation>performAlgebraTransfer("FiniteNaturalTransformation.identity-on")
                .performLeftProjectionOperation("component",n(9)).collect());
        FiniteNaturalTransformation empty=FiniteNaturalTransformation.identity(FiniteFunctor.identity(discrete()));
        assertTrue(empty.isIsomorphism()); assertTrue(empty.componentValues().isEmpty()); assertEquals(empty,empty.inverse());
    }
    @Test public void naturalTransformationFlowSurvivesSerializationAndRepeatedCollection() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteNaturalTransformation eta=arrowBetweenConstants(m);
        FiniteFunctor before=new FiniteFunctor(discrete(7,8),discrete(9),map(7,9,8,9),map(7,9,8,9));
        IAlgebraFlow<BigInteger> flow=m.flow(m.naturalTransformations,Collections.singletonList(eta))
                .performCustomMemberOperation("precompose",before).performOneOperandOperation("opposite")
                .<FiniteFunctor>performAlgebraTransfer("source").performLeftProjectionOperation("map-object",n(7)).performOperation("multiply",n(3));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("3"),restored.collect()); assertEquals(Collections.singletonList("3"),restored.collect());
    }
}
