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

public class NativeConeTest {
    private static IAlgebraItem<FiniteCone> item(ConcreteMathematics m,FiniteCone cone) { return m.cones.algebra().buildAlgebraItem(cone); }
    private static void undefined(Runnable action) {
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind());
    }
    private static FiniteFunctor pairDiagram(FiniteCategory category,int first,int second) {
        return new FiniteFunctor(discrete(0,1),category,map(0,first,1,second),
                map(0,category.identity(n(first)).intValue(),1,category.identity(n(second)).intValue()));
    }
    @Test public void constructorChecksEveryConeEquationAndRetainsTheVertexForEmptyShapes() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory interval=relationCategory(m,false,0,1);
        FiniteFunctor diagram=FiniteFunctor.identity(interval); Map<BigInteger,BigInteger> legs=map(0,0,1,1);
        FiniteCone cone=new FiniteCone(diagram,n(0),legs); legs.put(n(1),n(2));
        assertEquals(map(0,0,1,1),cone.legMap()); assertThrows(UnsupportedOperationException.class,() -> cone.legMap().clear());
        assertEquals(cone,FiniteCone.fromTransformation(cone.asTransformation(),n(0)));
        assertEquals(cone.hashCode(),new FiniteCone(diagram,n(0),map(0,0,1,1)).hashCode());
        for(Map<BigInteger,BigInteger> invalid : Arrays.asList(map(0,0),map(0,0,1,1,2,2),map(0,0,1,2)))
            assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,() -> new FiniteCone(diagram,n(0),invalid)).kind());
        assertThrows(MathFailure.class,() -> new FiniteCone(diagram,n(7),map(0,0,1,1)));
        assertThrows(MathFailure.class,() -> new FiniteCone(FiniteFunctor.identity(cyclic(2)),n(0),map(0,0)));
        undefined(() -> cone.leg(n(2)));
        undefined(() -> FiniteCone.fromTransformation(FiniteNaturalTransformation.identity(diagram),n(0)));
        FiniteFunctor empty=FiniteFunctor.emptyDiagram(interval);
        FiniteCone a=new FiniteCone(empty,n(0),map()),b=new FiniteCone(empty,n(1),map());
        assertNotEquals(a,b); assertEquals(a.asTransformation(),b.asTransformation());
        assertEquals(a,FiniteCone.fromTransformation(a.asTransformation(),n(0)));
        assertEquals(b,FiniteCone.fromTransformation(a.asTransformation(),n(1)));
        undefined(() -> FiniteFunctor.constant(discrete(),interval,n(9)));
        assertEquals(FiniteFunctor.emptyDiagram(interval),FiniteFunctor.constant(discrete(),interval,n(0)));
    }
    @Test public void everyBinaryDiagramInSmallChainsHasTheExpectedMinimumAsLimit() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int size=1;size<=4;size++) {
            int[] labels=new int[size]; for(int i=0;i<size;i++) labels[i]=i;
            FiniteCategory chain=relationCategory(m,false,labels);
            for(int a=0;a<size;a++) for(int b=0;b<size;b++) {
                FiniteFunctor diagram=pairDiagram(chain,a,b); int minimum=Math.min(a,b);
                IAlgebraItem<FiniteCone> wrapped=m.functors.algebra().buildAlgebraItem(diagram).performAlgebraTransfer("FiniteCone.limit");
                assertSame(m.cones.algebra(),wrapped.getAlgebra()); FiniteCone limit=wrapped.getResult();
                assertEquals(n(minimum),limit.vertex); assertTrue(limit.isLimit());
                assertEquals(chain.hom(n(minimum),n(a)).get(0),limit.leg(n(0)));
                assertEquals(chain.hom(n(minimum),n(b)).get(0),limit.leg(n(1)));
                for(int vertex=0;vertex<size;vertex++) {
                    List<FiniteCone> cones=FiniteCone.at(diagram,n(vertex));
                    assertEquals(vertex<=minimum?1:0,cones.size());
                    if(!cones.isEmpty()) {
                        assertEquals(vertex==minimum,cones.get(0).isLimit());
                        assertEquals(chain.hom(n(vertex),n(minimum)).get(0),limit.lift(cones.get(0)));
                    }
                }
            }
        }
    }
    @Test public void finiteSetProductsCheckExistenceAndUniquenessWithParallelArrows() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory sets=smallSets();
        for(int a=0;a<=2;a++) for(int b=0;b<=2;b++) {
            FiniteFunctor diagram=pairDiagram(sets,a,b);
            for(int vertex=0;vertex<=2;vertex++) assertEquals((int)Math.pow(a*b,vertex),FiniteCone.at(diagram,n(vertex)).size());
            if(a*b<=2) assertEquals(n(a*b),FiniteCone.limit(diagram).vertex);
            else undefined(() -> FiniteCone.limit(diagram));
        }
        FiniteFunctor diagram=pairDiagram(sets,1,2); FiniteCone limit=FiniteCone.limit(diagram);
        assertEquals(map(0,6,1,8),limit.legMap()); // The least bijection is the swap, before identity label 9.
        FiniteCone pointAtZero=new FiniteCone(diagram,n(1),map(0,3,1,4));
        IAlgebraItem<BigInteger> lift=item(m,limit).performCustomResultOperation("lift",pointAtZero);
        assertSame(m.integers.algebra(),lift.getAlgebra()); assertEquals(n(5),lift.getResult());
        FiniteCone bad=new FiniteCone(diagram,n(2),map(0,6,1,7));
        assertFalse(bad.isLimit()); assertEquals(Arrays.asList(n(4),n(5)),bad.mediatorsFrom(pointAtZero));
        assertEquals(Collections.emptyList(),bad.mediatorsFrom(new FiniteCone(diagram,n(1),map(0,3,1,5))));
        List<IAlgebraItem<BigInteger>> factors=item(m,bad).performCustomResultFlatOperation("mediators",pointAtZero);
        assertEquals(2,factors.size()); assertSame(m.integers.algebra(),factors.get(0).getAlgebra());
        assertEquals(n(4),factors.get(0).getResult()); assertEquals(n(5),factors.get(1).getResult());
        // A unique arrow from one cone alone does not make bad a limit.
        FiniteCone empty=new FiniteCone(diagram,n(0),map(0,1,1,2));
        assertEquals(Collections.singletonList(n(2)),bad.mediatorsFrom(empty)); undefined(() -> bad.lift(empty));
        FiniteCone different=new FiniteCone(pairDiagram(sets,2,1),n(0),map(0,2,1,1));
        undefined(() -> limit.lift(different)); undefined(() -> limit.mediatorsFrom(different));
    }
    @Test public void parallelPairAndGroupActionDiagramsComputeEqualizersAndFixedPoints() {
        FiniteCategory sets=smallSets();
        FiniteFunctor equalizer=new FiniteFunctor(parallelPair(),sets,map(0,2,1,2),map(0,9,1,9,2,9,3,7));
        FiniteCone one=FiniteCone.limit(equalizer); assertEquals(n(1),one.vertex); assertEquals(map(0,4,1,4),one.legMap());
        assertEquals(1,FiniteCone.at(equalizer,n(2)).size());
        assertThrows(MathFailure.class,() -> new FiniteCone(equalizer,n(1),map(0,5,1,5)));
        FiniteFunctor noEqualPoints=new FiniteFunctor(parallelPair(),sets,map(0,2,1,2),map(0,9,1,9,2,9,3,8));
        FiniteCone zero=FiniteCone.limit(noEqualPoints); assertEquals(n(0),zero.vertex); assertEquals(map(0,2,1,2),zero.legMap());
        FiniteFunctor action=new FiniteFunctor(cyclic(2),sets,map(0,2),map(0,9,1,8));
        assertEquals(n(0),FiniteCone.limit(action).vertex); assertTrue(FiniteCone.at(action,n(1)).isEmpty());
        FiniteFunctor freeAction=FiniteFunctor.identity(cyclic(2));
        assertTrue(FiniteCone.at(freeAction,n(0)).isEmpty()); undefined(() -> FiniteCone.limit(freeAction));
    }
    @Test public void emptyDiagramLimitsAreTerminalObjectsAndStillRequireUniqueArrows() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory interval=relationCategory(m,false,0,1);
        FiniteFunctor empty=FiniteFunctor.emptyDiagram(interval); FiniteCone terminal=FiniteCone.limit(empty);
        assertEquals(n(1),terminal.vertex); assertTrue(terminal.legMap().isEmpty());
        assertEquals(n(1),terminal.lift(new FiniteCone(empty,n(0),map())));
        FiniteFunctor nontrivialGroup=FiniteFunctor.emptyDiagram(cyclic(2)); FiniteCone cone=new FiniteCone(nontrivialGroup,n(0),map());
        assertEquals(Arrays.asList(n(0),n(1)),cone.mediatorsFrom(cone)); assertFalse(cone.isLimit());
        undefined(() -> FiniteCone.limit(nontrivialGroup)); undefined(() -> FiniteCone.limit(FiniteFunctor.emptyDiagram(discrete(0,1))));
        undefined(() -> FiniteCone.limit(FiniteFunctor.emptyDiagram(discrete())));
        undefined(() -> FiniteCone.at(empty,n(9)));
    }
    @Test public void mappingAndReindexingPreserveConesButCanLoseTheLimitProperty() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory point=discrete(0),interval=relationCategory(m,false,0,1);
        FiniteCone terminal=FiniteCone.limit(FiniteFunctor.emptyDiagram(point));
        FiniteFunctor bottom=new FiniteFunctor(point,interval,map(0,0),map(0,0));
        FiniteCone mapped=item(m,terminal).performCustomMemberOperation("map",bottom).getResult();
        assertEquals(FiniteFunctor.emptyDiagram(interval),mapped.diagram); assertEquals(n(0),mapped.vertex); assertFalse(mapped.isLimit());
        FiniteCone initial=new FiniteCone(FiniteFunctor.identity(interval),n(0),map(0,0,1,1)); assertTrue(initial.isLimit());
        FiniteFunctor top=new FiniteFunctor(point,interval,map(0,1),map(0,2));
        FiniteCone reindexed=item(m,initial).performCustomMemberOperation("reindex",top).getResult();
        assertEquals(map(0,1),reindexed.legMap()); assertFalse(reindexed.isLimit());
        assertEquals(n(1),FiniteCone.limit(reindexed.diagram).vertex);
        assertEquals(initial,initial.map(FiniteFunctor.identity(interval))); assertEquals(initial,initial.reindex(FiniteFunctor.identity(interval)));
        undefined(() -> initial.map(FiniteFunctor.identity(point))); undefined(() -> initial.reindex(FiniteFunctor.identity(point)));
    }
    @Test public void mixedFlatTransfersAndSerializedFlowsRetainWrappersAndDuplicateLegs() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteFunctor diagram=pairDiagram(smallSets(),1,2);
        IAlgebraFlow<BigInteger> flow=m.flow(m.functors,Collections.singletonList(diagram))
                .<FiniteCone>performAlgebraTransfer("FiniteCone.limit")
                .<FiniteFunction<BigInteger,BigInteger>>performAlgebraTransfer("leg-map")
                .<BigInteger>performFlatAlgebraTransfer("values").performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("-6","-8"),restored.collect()); assertEquals(Arrays.asList("-6","-8"),restored.collect());
        assertEquals(Arrays.asList("1","1"),m.flow(m.functors,Collections.singletonList(diagram))
                .<FiniteCone,BigInteger>performFlatAlgebraUnsafe("FiniteCone.cones-at",n(1))
                .<BigInteger>performAlgebraTransfer("vertex").collect());
        FiniteFunctor constant=FiniteFunctor.constant(discrete(0,1),discrete(10),n(10));
        FiniteCone cone=FiniteCone.limit(constant);
        assertEquals(Arrays.asList("10","10"),m.flow(m.cones,Collections.singletonList(cone)).<BigInteger>performFlatAlgebraTransfer("legs").collect());
        IAlgebraItem<BigInteger> leg=item(m,cone).performLeftProjectionOperation("leg",n(0));
        assertSame(m.integers.algebra(),leg.getAlgebra()); assertEquals(n(10),leg.getResult());
        IAlgebraItem<FiniteCone> reconstructed=m.naturalTransformations.algebra().buildAlgebraItem(cone.asTransformation())
                .performUnsafeOperation("FiniteCone.from-transformation",n(10));
        assertSame(m.cones.algebra(),reconstructed.getAlgebra()); assertEquals(cone,reconstructed.getResult());
    }
    @Test public void resourceExhaustionIsNeverReportedAsNonexistenceOrFalse() {
        List<BigInteger> labels=new ArrayList<>(); for(int i=0;i<14;i++) labels.add(n(i));
        FiniteFunctor many=FiniteFunctor.constant(FiniteCategory.discrete(new FiniteSet<>(labels)),cyclic(2),n(0));
        MathFailure tooMany=assertThrows(MathFailure.class,() -> FiniteCone.at(many,n(0)));
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,tooMany.kind()); assertTrue(tooMany.getMessage().contains("10000 cones"));
        Map<BigInteger,BigInteger> legs=new TreeMap<>(); for(BigInteger label : labels) legs.put(label,n(0));
        FiniteCone cone=new FiniteCone(many,n(0),legs);
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,cone::isLimit).kind());
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,() -> FiniteCone.limit(many)).kind());
        for(int i=14;i<128;i++) labels.add(n(i));
        FiniteFunctor wide=FiniteFunctor.constant(FiniteCategory.discrete(new FiniteSet<>(labels)),cyclic(2),n(0));
        MathFailure tooMuch=assertThrows(MathFailure.class,() -> FiniteCone.at(wide,n(0)));
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,tooMuch.kind()); assertTrue(tooMuch.getMessage().contains("1000000 search steps"));
    }
}
