package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.structures.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static operations.FiniteCategoryFixtures.*;

public class NativeEquivalenceTest {
    private static IAlgebraItem<FiniteEquivalence> item(ConcreteMathematics m,FiniteEquivalence e) { return m.equivalences.algebra().buildAlgebraItem(e); }
    private static FiniteFunctor inclusion(ConcreteMathematics m) {
        return new FiniteFunctor(discrete(0),relationCategory(m,true,0,1),map(0,0),map(0,0));
    }
    private static FiniteEquivalence cyclicWitness(int multiplier,int unitComponent) {
        FiniteCategory c=cyclic(3); FiniteFunctor id=FiniteFunctor.identity(c);
        FiniteFunctor f=new FiniteFunctor(c,c,map(0,0),map(0,0,1,multiplier,2,(2*multiplier)%3));
        FiniteNaturalTransformation unit=new FiniteNaturalTransformation(id,id,map(0,unitComponent));
        FiniteNaturalTransformation counit=new FiniteNaturalTransformation(id,id,map(0,(3-(multiplier*unitComponent)%3)%3));
        return new FiniteEquivalence(f,f,unit,counit);
    }
    @Test public void witnessConstructionHandlesEquivalenceWithoutStrictInverse() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteFunctor f=inclusion(m);
        assertFalse(f.isIsomorphism()); assertThrows(MathFailure.class,f::inverse);
        IAlgebraItem<FiniteEquivalence> wrapped=m.functors.algebra().buildAlgebraItem(f).performAlgebraTransfer("FiniteEquivalence.from-functor");
        assertSame(m.equivalences.algebra(),wrapped.getAlgebra()); FiniteEquivalence e=wrapped.getResult();
        assertEquals(map(0,0,1,0),e.backward.objectMap()); assertEquals(map(0,0,1,0,2,0,3,0),e.backward.arrowMap());
        assertEquals(map(0,0),e.unit.componentMap()); assertEquals(map(0,0,1,1),e.counit.componentMap());
        assertEquals(FiniteFunctor.identity(f.source),e.backward.compose(f));
        assertNotEquals(FiniteFunctor.identity(f.target),f.compose(e.backward));
        assertTrue(e.counit.isIsomorphism()); assertEquals(FiniteNaturalTransformation.identity(FiniteFunctor.identity(f.target)),e.counit.compose(e.counit.inverse()));
        IAlgebraItem<FiniteNaturalTransformation> counit=item(m,e).performAlgebraTransfer("counit");
        assertSame(m.naturalTransformations.algebra(),counit.getAlgebra());
        assertEquals(n(1),counit.performLeftProjectionOperation("component",n(1)).getResult());
        assertEquals(e,FiniteEquivalence.fromFunctor(f));
        assertEquals(FiniteEquivalence.identity(discrete()),FiniteEquivalence.fromFunctor(FiniteFunctor.identity(discrete())));
    }
    @Test public void everySmallIndiscreteFunctorHasTheExpectedRepresentativeAndWitnessTables() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int sourceSize=1;sourceSize<=3;sourceSize++) for(int targetSize=1;targetSize<=3;targetSize++) {
            int[] sourceLabels=new int[sourceSize],targetLabels=new int[targetSize];
            for(int i=0;i<sourceSize;i++) sourceLabels[i]=i;
            for(int i=0;i<targetSize;i++) targetLabels[i]=i;
            FiniteCategory source=relationCategory(m,true,sourceLabels),target=relationCategory(m,true,targetLabels);
            int count=1; for(int i=0;i<sourceSize;i++) count*=targetSize;
            for(int code=0;code<count;code++) {
                int[] images=new int[sourceSize]; int digits=code;
                Map<BigInteger,BigInteger> objects=new TreeMap<>(),arrows=new TreeMap<>();
                for(int i=0;i<sourceSize;i++) { images[i]=digits%targetSize; digits/=targetSize; objects.put(n(i),n(images[i])); }
                for(int i=0;i<sourceSize;i++) for(int j=0;j<sourceSize;j++) arrows.put(n(i*sourceSize+j),n(images[i]*targetSize+images[j]));
                FiniteFunctor f=new FiniteFunctor(source,target,objects,arrows);
                FiniteEquivalence e=m.functors.algebra().buildAlgebraItem(f).<FiniteEquivalence>performAlgebraTransfer("FiniteEquivalence.from-functor").getResult();
                int[] representative=new int[targetSize];
                for(int i=0;i<targetSize;i++) {
                    representative[i]=0;
                    for(int j=0;j<sourceSize;j++) if(images[j]==i) { representative[i]=j; break; }
                    assertEquals(n(representative[i]),e.backward.mapObject(n(i)));
                    assertEquals(n(images[representative[i]]*targetSize+i),e.counit.component(n(i)));
                }
                for(int i=0;i<sourceSize;i++) assertEquals(n(i*sourceSize+representative[images[i]]),e.unit.component(n(i)));
                for(int i=0;i<targetSize;i++) for(int j=0;j<targetSize;j++)
                    assertEquals(n(representative[i]*sourceSize+representative[j]),e.backward.mapArrow(n(i*targetSize+j)));
                assertEquals(e,e.inverse().inverse()); assertEquals(e,e.opposite().opposite());
            }
        }
    }
    @Test public void invalidCoherenceAndNonEquivalencesAreRejected() {
        FiniteCategory c=cyclic(3); FiniteFunctor id=FiniteFunctor.identity(c);
        FiniteNaturalTransformation one=new FiniteNaturalTransformation(id,id,map(0,1));
        assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,() -> new FiniteEquivalence(id,id,one,one)).kind());
        FiniteFunctor doubling=new FiniteFunctor(c,c,map(0,0),map(0,0,1,2,2,1));
        assertThrows(MathFailure.class,() -> new FiniteEquivalence(id,id,FiniteNaturalTransformation.identity(doubling),one));
        assertThrows(MathFailure.class,() -> new FiniteEquivalence(id,FiniteFunctor.identity(discrete(0)),one,one));
        FiniteFunctor idempotent=FiniteFunctor.identity(monoid(new int[][]{{0,1},{1,1}},0));
        FiniteNaturalTransformation noninvertible=new FiniteNaturalTransformation(idempotent,idempotent,map(0,1));
        assertThrows(MathFailure.class,() -> new FiniteEquivalence(idempotent,idempotent,noninvertible,FiniteNaturalTransformation.identity(idempotent)));
        List<FiniteFunctor> failures=Arrays.asList(
                new FiniteFunctor(cyclic(2),discrete(0),map(0,0),map(0,0,1,0)),
                new FiniteFunctor(discrete(0,1),discrete(0),map(0,0,1,0),map(0,0,1,0)),
                new FiniteFunctor(discrete(),discrete(0),map(),map()));
        ConcreteMathematics m=new ConcreteMathematics();
        for(FiniteFunctor f : failures) assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,
                () -> m.functors.algebra().buildAlgebraItem(f).performAlgebraTransfer("FiniteEquivalence.from-functor")).kind());
    }
    @Test public void compositionUsesSuppliedWitnessesAndAgreesWithIndependentCyclicArithmetic() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteEquivalence[][] witnesses=new FiniteEquivalence[3][3];
        for(int k=1;k<=2;k++) for(int r=0;r<3;r++) witnesses[k][r]=cyclicWitness(k,r);
        for(int k=1;k<=2;k++) for(int r=0;r<3;r++) {
            FiniteEquivalence e=witnesses[k][r];
            assertEquals(witnesses[k][(k*r)%3],item(m,e).performOneOperandOperation("inverse").getResult());
            assertEquals(witnesses[k][(3-r)%3],item(m,e).performOneOperandOperation("opposite").getResult());
            assertEquals(e,e.compose(witnesses[1][0])); assertEquals(e,witnesses[1][0].compose(e));
            for(int l=1;l<=2;l++) for(int s=0;s<3;s++) {
                FiniteEquivalence before=witnesses[l][s];
                assertEquals(witnesses[(k*l)%3][(s+l*r)%3],item(m,e).performOperation("compose",before).perform().getResult());
                for(int p=1;p<=2;p++) for(int t=0;t<3;t++)
                    assertEquals(e.compose(before).compose(witnesses[p][t]),e.compose(before.compose(witnesses[p][t])));
            }
        }
        assertNotEquals(witnesses[1][0],witnesses[1][1]);
        assertNotEquals(witnesses[1][1],FiniteEquivalence.fromFunctor(witnesses[1][1].forward));
        assertThrows(MathFailure.class,() -> witnesses[1][0].compose(FiniteEquivalence.identity(discrete(10))));
    }
    @Test public void witnessesComposeThroughNativeTransfersAndSerializedFlows() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteFunctor f=inclusion(m);
        IAlgebraFlow<BigInteger> flow=m.flow(m.functors,Collections.singletonList(f))
                .<FiniteEquivalence>performAlgebraTransfer("FiniteEquivalence.from-functor")
                .<FiniteNaturalTransformation>performAlgebraTransfer("counit")
                .performOneOperandOperation("inverse").performLeftProjectionOperation("component",n(1)).performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("-2"),restored.collect()); assertEquals(Collections.singletonList("-2"),restored.collect());
        assertEquals(Collections.singletonList("2"),m.flow(m.categories,Collections.singletonList(discrete(2)))
                .<FiniteEquivalence>performAlgebraTransfer("FiniteEquivalence.identity-on")
                .<FiniteFunctor>performAlgebraTransfer("backward").performLeftProjectionOperation("map-object",n(2)).collect());
    }
}
