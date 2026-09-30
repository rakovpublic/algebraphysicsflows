package operations;

import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.FiniteSet;
import mathematics.topology.FiniteSimplicialComplex;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeTopologyTest {
    @SafeVarargs private static FiniteSimplicialComplex complex(FiniteSet<Integer>... facets) {
        return new FiniteSimplicialComplex(Arrays.asList(facets));
    }
    @Test public void knownHomologyRunsThroughNativeFlatTransfers() {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteSimplicialComplex circle=complex(FiniteSet.of(0,1),FiniteSet.of(1,2),FiniteSet.of(0,2));
        FiniteSimplicialComplex disk=complex(FiniteSet.of(0,1,2));
        FiniteSimplicialComplex sphere=complex(FiniteSet.of(0,1,2),FiniteSet.of(0,1,3),FiniteSet.of(0,2,3),FiniteSet.of(1,2,3));
        assertEquals(Arrays.asList("1","1"),m.flow(m.complexes,Collections.singletonList(circle)).<BigInteger>performFlatAlgebraTransfer("betti-numbers").collect());
        assertEquals(Arrays.asList("1","0","0"),m.flow(m.complexes,Collections.singletonList(disk)).<BigInteger>performFlatAlgebraTransfer("betti-numbers").collect());
        assertEquals(Arrays.asList("1","0","1"),m.flow(m.complexes,Collections.singletonList(sphere)).<BigInteger>performFlatAlgebraTransfer("betti-numbers").collect());
        assertEquals(Collections.singletonList("2"),m.flow(m.complexes,Collections.singletonList(sphere)).<BigInteger>performAlgebraTransfer("euler-characteristic").collect());
        assertEquals(Collections.singletonList("-1"),m.flow(m.complexes,Collections.singletonList(complex())).<BigInteger>performAlgebraTransfer("dimension").collect());
        assertTrue(m.flow(m.complexes,Collections.singletonList(complex())).performFlatAlgebraTransfer("betti-numbers").collect().isEmpty());
        assertEquals(BigInteger.valueOf(2),m.complexes.algebra().buildAlgebraItem(complex(FiniteSet.of(0),FiniteSet.of(1)))
                .performLeftProjectionOperation("betti-number",BigInteger.ZERO).getResult());
    }
    @Test public void closureSkeletonEqualityAndUnboundedDegreeAreExplicit() {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteSimplicialComplex triangle=complex(FiniteSet.of(0,1,2));
        FiniteSimplicialComplex edge=complex(FiniteSet.of(2,1));
        assertEquals(triangle,triangle.union(edge)); assertEquals(edge,triangle.intersection(edge));
        assertTrue(edge.subcomplexOf(triangle)); assertFalse(triangle.subcomplexOf(edge));
        assertEquals(triangle,complex(FiniteSet.of(2,0,1))); assertEquals(triangle.hashCode(),complex(FiniteSet.of(1,2,0)).hashCode());
        assertNotEquals(triangle,complex(FiniteSet.of(3,4,5)));
        assertEquals(Collections.singletonList("1"),m.flow(m.complexes,Collections.singletonList(triangle))
                .performCustomMemberOperation("skeleton",BigInteger.ONE).performLeftProjectionOperation("betti-number",BigInteger.ONE).collect());
        BigInteger huge=BigInteger.ONE.shiftLeft(80);
        assertEquals(BigInteger.ZERO,m.complexes.algebra().buildAlgebraItem(triangle).performLeftProjectionOperation("betti-number",huge).getResult());
        assertEquals(triangle,m.complexes.algebra().buildAlgebraItem(triangle).performCustomMemberOperation("skeleton",huge).getResult());
        assertEquals(0,triangle.bettiNumber(Integer.MAX_VALUE));
        assertThrows(exceptions.NotMemberException.class,() -> m.complexes.algebra().buildAlgebraItem(triangle).performLeftProjectionOperation("betti-number",BigInteger.valueOf(-1)));
        List<Integer> labels=new ArrayList<>(); for(int i=0;i<21;i++) labels.add(i);
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,() -> complex(new FiniteSet<>(labels))).kind());
    }
    @Test public void allGraphsOnFourVerticesAgreeWithIndependentComponentAndCycleCounts() {
        ConcreteMathematics m=new ConcreteMathematics();
        int[][] edges={{0,1},{0,2},{0,3},{1,2},{1,3},{2,3}};
        for(int mask=0;mask<64;mask++) {
            List<FiniteSet<Integer>> facets=new ArrayList<>();
            for(int i=0;i<4;i++) facets.add(FiniteSet.of(i));
            boolean[][] adjacent=new boolean[4][4]; int edgeCount=0;
            for(int i=0;i<6;i++) if((mask&(1<<i))!=0) {
                int a=edges[i][0],b=edges[i][1]; adjacent[a][b]=adjacent[b][a]=true;
                facets.add(FiniteSet.of(a,b)); edgeCount++;
            }
            boolean[] visited=new boolean[4]; int components=0;
            for(int start=0;start<4;start++) if(!visited[start]) {
                components++; Queue<Integer> queue=new ArrayDeque<>(); queue.add(start); visited[start]=true;
                while(!queue.isEmpty()) {
                    int a=queue.remove();
                    for(int b=0;b<4;b++) if(adjacent[a][b] && !visited[b]) { visited[b]=true; queue.add(b); }
                }
            }
            FiniteSimplicialComplex c=new FiniteSimplicialComplex(facets);
            assertEquals(BigInteger.valueOf(components),m.complexes.algebra().buildAlgebraItem(c).performLeftProjectionOperation("betti-number",BigInteger.ZERO).getResult());
            assertEquals(edgeCount-4+components,c.bettiNumber(1));
            assertEquals(BigInteger.valueOf(4-edgeCount),c.eulerCharacteristic());
        }
    }
    @Test public void topologyFlowSurvivesSerializationAndRepeatedCollection() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics();
        IAlgebraFlow<BigInteger> flow=m.flow(m.complexes,Collections.singletonList(complex(FiniteSet.of(0,1))))
                .<BigInteger>performFlatAlgebraTransfer("betti-numbers").<BigInteger>performAlgebraTransfer("to-integer").performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("-1","0"),restored.collect());
        assertEquals(Arrays.asList("-1","0"),restored.collect());
    }
}
