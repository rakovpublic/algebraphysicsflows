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

public class NativeCoconeTest {
    private static IAlgebraItem<FiniteCocone> item(ConcreteMathematics m,FiniteCocone c) { return m.cocones.algebra().buildAlgebraItem(c); }
    private static void undefined(Runnable action) { assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind()); }
    private static FiniteFunctor pairDiagram(FiniteCategory category,int a,int b) {
        return new FiniteFunctor(discrete(0,1),category,map(0,a,1,b),map(0,category.identity(n(a)).intValue(),1,category.identity(n(b)).intValue()));
    }
    @Test public void constructorChecksCoconeDirectionsNaturalityAndEmptyShapeVertices() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory interval=relationCategory(m,false,0,1);
        FiniteFunctor diagram=FiniteFunctor.identity(interval); Map<BigInteger,BigInteger> legs=map(0,1,1,2);
        FiniteCocone cocone=new FiniteCocone(diagram,n(1),legs); legs.clear();
        assertEquals(map(0,1,1,2),cocone.legMap()); assertThrows(UnsupportedOperationException.class,() -> cocone.legMap().clear());
        assertEquals(diagram,cocone.asTransformation().source);
        assertEquals(FiniteFunctor.constant(interval,interval,n(1)),cocone.asTransformation().target);
        assertEquals(cocone,FiniteCocone.fromTransformation(cocone.asTransformation(),n(1)));
        assertEquals(cocone,FiniteCocone.fromOpposite(cocone.opposite()));
        assertEquals(cocone.hashCode(),new FiniteCocone(diagram,n(1),map(0,1,1,2)).hashCode());
        assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,() -> new FiniteCocone(diagram,n(0),map(0,0,1,1))).kind());
        assertThrows(MathFailure.class,() -> new FiniteCocone(diagram,n(1),map(0,1)));
        assertThrows(MathFailure.class,() -> new FiniteCocone(diagram,n(5),map(0,1,1,2)));
        assertThrows(MathFailure.class,() -> new FiniteCocone(FiniteFunctor.identity(cyclic(2)),n(0),map(0,0)));
        undefined(() -> cocone.leg(n(7)));
        undefined(() -> FiniteCocone.fromTransformation(FiniteNaturalTransformation.identity(diagram),n(1)));
        FiniteFunctor empty=FiniteFunctor.emptyDiagram(interval);
        FiniteCocone a=new FiniteCocone(empty,n(0),map()),b=new FiniteCocone(empty,n(1),map());
        assertNotEquals(a,b); assertEquals(a.asTransformation(),b.asTransformation());
        assertEquals(b,FiniteCocone.fromTransformation(a.asTransformation(),n(1)));
    }
    @Test public void everyBinaryDiagramInSmallChainsHasMaximumAsColimit() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int size=1;size<=4;size++) {
            int[] labels=new int[size]; for(int i=0;i<size;i++) labels[i]=i;
            FiniteCategory chain=relationCategory(m,false,labels);
            for(int a=0;a<size;a++) for(int b=0;b<size;b++) {
                FiniteFunctor diagram=pairDiagram(chain,a,b); int maximum=Math.max(a,b);
                IAlgebraItem<FiniteCocone> wrapped=m.functors.algebra().buildAlgebraItem(diagram).performAlgebraTransfer("FiniteCocone.colimit");
                assertSame(m.cocones.algebra(),wrapped.getAlgebra()); FiniteCocone colimit=wrapped.getResult();
                assertEquals(n(maximum),colimit.vertex); assertTrue(colimit.isColimit());
                assertEquals(chain.hom(n(a),n(maximum)).get(0),colimit.leg(n(0)));
                assertEquals(chain.hom(n(b),n(maximum)).get(0),colimit.leg(n(1)));
                for(int vertex=0;vertex<size;vertex++) {
                    List<FiniteCocone> cocones=FiniteCocone.at(diagram,n(vertex));
                    assertEquals(vertex>=maximum?1:0,cocones.size());
                    if(!cocones.isEmpty()) {
                        assertEquals(vertex==maximum,cocones.get(0).isColimit());
                        assertEquals(chain.hom(n(maximum),n(vertex)).get(0),colimit.descend(cocones.get(0)));
                    }
                }
            }
        }
    }
    @Test public void finiteSetCoproductsRequireAllOutgoingFactorizationsToBeUnique() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory sets=smallSets();
        for(int a=0;a<=2;a++) for(int b=0;b<=2;b++) {
            FiniteFunctor diagram=pairDiagram(sets,a,b);
            for(int vertex=0;vertex<=2;vertex++) assertEquals((int)Math.pow(vertex,a+b),FiniteCocone.at(diagram,n(vertex)).size());
            if(a+b<=2) assertEquals(n(a+b),FiniteCocone.colimit(diagram).vertex);
            else undefined(() -> FiniteCocone.colimit(diagram));
        }
        FiniteFunctor diagram=pairDiagram(sets,1,1); FiniteCocone colimit=FiniteCocone.colimit(diagram);
        assertEquals(n(2),colimit.vertex); assertEquals(map(0,4,1,5),colimit.legMap());
        FiniteCocone reversed=new FiniteCocone(diagram,n(2),map(0,5,1,4));
        IAlgebraItem<BigInteger> descent=item(m,colimit).performCustomResultOperation("descend",reversed);
        assertSame(m.integers.algebra(),descent.getAlgebra()); assertEquals(n(8),descent.getResult());
        FiniteCocone bad=new FiniteCocone(diagram,n(2),map(0,4,1,4));
        assertFalse(bad.isColimit()); assertEquals(Arrays.asList(n(7),n(9)),bad.mediatorsTo(bad));
        assertEquals(Collections.emptyList(),bad.mediatorsTo(colimit));
        List<IAlgebraItem<BigInteger>> mediators=item(m,bad).performCustomResultFlatOperation("mediators",bad);
        assertEquals(2,mediators.size()); assertSame(m.integers.algebra(),mediators.get(0).getAlgebra());
        assertEquals(n(7),mediators.get(0).getResult()); assertEquals(n(9),mediators.get(1).getResult());
        FiniteCocone point=new FiniteCocone(diagram,n(1),map(0,3,1,3));
        assertEquals(Collections.singletonList(n(6)),bad.mediatorsTo(point)); undefined(() -> bad.descend(point));
        FiniteCocone otherDiagram=new FiniteCocone(pairDiagram(sets,1,2),n(1),map(0,3,1,6));
        undefined(() -> colimit.descend(otherDiagram)); undefined(() -> colimit.mediatorsTo(otherDiagram));
    }
    @Test public void parallelPairAndActionDiagramsComputeCoequalizersAndOrbits() {
        FiniteCategory sets=smallSets();
        FiniteFunctor coequalizer=new FiniteFunctor(parallelPair(),sets,map(0,2,1,2),map(0,9,1,9,2,9,3,7));
        FiniteCocone quotient=FiniteCocone.colimit(coequalizer);
        assertEquals(n(1),quotient.vertex); assertEquals(map(0,6,1,6),quotient.legMap());
        assertThrows(MathFailure.class,() -> new FiniteCocone(coequalizer,n(2),map(0,9,1,9)));
        FiniteFunctor sameMaps=new FiniteFunctor(parallelPair(),sets,map(0,2,1,2),map(0,9,1,9,2,9,3,9));
        assertEquals(n(2),FiniteCocone.colimit(sameMaps).vertex);
        FiniteFunctor action=new FiniteFunctor(cyclic(2),sets,map(0,2),map(0,9,1,8));
        FiniteCocone orbit=FiniteCocone.colimit(action); assertEquals(n(1),orbit.vertex); assertEquals(map(0,6),orbit.legMap());
        assertEquals(2,FiniteCocone.at(action,n(2)).size());
        FiniteFunctor group=FiniteFunctor.identity(cyclic(2));
        assertTrue(FiniteCocone.at(group,n(0)).isEmpty()); undefined(() -> FiniteCocone.colimit(group));
    }
    @Test public void initialObjectsAndFunctorialOperationsRespectColimitBoundaries() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory point=discrete(0),interval=relationCategory(m,false,0,1);
        FiniteFunctor empty=FiniteFunctor.emptyDiagram(interval); FiniteCocone initial=FiniteCocone.colimit(empty);
        assertEquals(n(0),initial.vertex); assertEquals(n(1),initial.descend(new FiniteCocone(empty,n(1),map())));
        FiniteFunctor group=FiniteFunctor.emptyDiagram(cyclic(2)); FiniteCocone noninitial=new FiniteCocone(group,n(0),map());
        assertEquals(Arrays.asList(n(0),n(1)),noninitial.mediatorsTo(noninitial)); assertFalse(noninitial.isColimit());
        undefined(() -> FiniteCocone.colimit(group)); undefined(() -> FiniteCocone.colimit(FiniteFunctor.emptyDiagram(discrete())));
        undefined(() -> FiniteCocone.colimit(FiniteFunctor.emptyDiagram(discrete(0,1)))); undefined(() -> FiniteCocone.at(empty,n(9)));
        FiniteCocone pointInitial=FiniteCocone.colimit(FiniteFunctor.emptyDiagram(point));
        FiniteFunctor top=new FiniteFunctor(point,interval,map(0,1),map(0,2));
        FiniteCocone mapped=item(m,pointInitial).performCustomMemberOperation("map",top).getResult();
        assertEquals(n(1),mapped.vertex); assertFalse(mapped.isColimit());
        FiniteCocone terminal=new FiniteCocone(FiniteFunctor.identity(interval),n(1),map(0,1,1,2)); assertTrue(terminal.isColimit());
        FiniteFunctor bottom=new FiniteFunctor(point,interval,map(0,0),map(0,0));
        FiniteCocone reindexed=item(m,terminal).performCustomMemberOperation("reindex",bottom).getResult();
        assertEquals(map(0,1),reindexed.legMap()); assertFalse(reindexed.isColimit());
        assertEquals(n(0),FiniteCocone.colimit(reindexed.diagram).vertex);
        assertEquals(terminal,terminal.map(FiniteFunctor.identity(interval))); assertEquals(terminal,terminal.reindex(FiniteFunctor.identity(interval)));
        undefined(() -> terminal.map(FiniteFunctor.identity(point))); undefined(() -> terminal.reindex(FiniteFunctor.identity(point)));
    }
    @Test public void oppositeConversionFlatResultsAndSerializedNativeFlowsKeepActualCarriers() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteFunctor diagram=pairDiagram(smallSets(),1,1);
        IAlgebraFlow<BigInteger> flow=m.flow(m.functors,Collections.singletonList(diagram))
                .<FiniteCocone>performAlgebraTransfer("FiniteCocone.colimit")
                .<FiniteFunction<BigInteger,BigInteger>>performAlgebraTransfer("leg-map")
                .<BigInteger>performFlatAlgebraTransfer("values").performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("-4","-5"),restored.collect()); assertEquals(Arrays.asList("-4","-5"),restored.collect());
        assertEquals(Arrays.asList("2","2","2","2"),m.flow(m.functors,Collections.singletonList(diagram))
                .<FiniteCocone,BigInteger>performFlatAlgebraUnsafe("FiniteCocone.cocones-at",n(2))
                .<BigInteger>performAlgebraTransfer("vertex").collect());
        FiniteCocone colimit=FiniteCocone.colimit(diagram);
        IAlgebraItem<FiniteCone> dual=item(m,colimit).performAlgebraTransfer("opposite");
        assertSame(m.cones.algebra(),dual.getAlgebra()); assertEquals(diagram.opposite(),dual.getResult().diagram); assertTrue(dual.getResult().isLimit());
        IAlgebraItem<FiniteCocone> back=dual.performAlgebraTransfer("FiniteCocone.opposite-cone");
        assertSame(m.cocones.algebra(),back.getAlgebra()); assertEquals(colimit,back.getResult());
        IAlgebraItem<BigInteger> leg=item(m,colimit).performLeftProjectionOperation("leg",n(0));
        assertSame(m.integers.algebra(),leg.getAlgebra()); assertEquals(n(4),leg.getResult());
        IAlgebraItem<FiniteCocone> reconstructed=m.naturalTransformations.algebra().buildAlgebraItem(colimit.asTransformation())
                .performUnsafeOperation("FiniteCocone.from-transformation",n(2));
        assertSame(m.cocones.algebra(),reconstructed.getAlgebra()); assertEquals(colimit,reconstructed.getResult());
        FiniteCocone duplicateLegs=new FiniteCocone(diagram,n(1),map(0,3,1,3));
        assertEquals(Arrays.asList("3","3"),m.flow(m.cocones,Collections.singletonList(duplicateLegs)).<BigInteger>performFlatAlgebraTransfer("legs").collect());
    }
    @Test public void dualSearchPropagatesResourceExhaustionWithoutTruncation() {
        List<BigInteger> labels=new ArrayList<>(); for(int i=0;i<14;i++) labels.add(n(i));
        FiniteFunctor many=FiniteFunctor.constant(FiniteCategory.discrete(new FiniteSet<>(labels)),cyclic(2),n(0));
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,() -> FiniteCocone.at(many,n(0))).kind());
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,() -> FiniteCocone.colimit(many)).kind());
    }
}
