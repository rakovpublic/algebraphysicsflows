package operations;

import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.foundations.*;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeRelationTest {
    private static FiniteRelation<BigInteger,BigInteger> relation(ConcreteMathematics math,long... endpoints) {
        List<Pair<BigInteger,BigInteger>> pairs=new ArrayList<>();
        for(int i=0;i<endpoints.length;i+=2) pairs.add(new Pair<>(BigInteger.valueOf(endpoints[i]),BigInteger.valueOf(endpoints[i+1])));
        return new FiniteRelation<>(math.integers.algebra(),math.integers.algebra(),new FiniteSet<>(pairs));
    }
    @Test public void compositionInverseImageAndPreimageKeepNativeCarriers() {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteRelation<BigInteger,BigInteger> first=relation(m,1,2,1,3,2,3),second=relation(m,2,4,3,4);
        assertEquals(relation(m,1,4,2,4),m.integerRelations.algebra().buildAlgebraItem(first).performOperation("compose",second).perform().getResult());
        assertEquals(relation(m,2,1,3,1,3,2),m.integerRelations.algebra().buildAlgebraItem(first).performOneOperandOperation("inverse").getResult());
        assertEquals(FiniteSet.of(BigInteger.valueOf(2),BigInteger.valueOf(3)),m.integerRelations.algebra().buildAlgebraItem(first)
                .performLeftProjectionOperation("image",FiniteSet.of(BigInteger.ONE)).getResult());
        assertEquals(FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2)),m.integerRelations.algebra().buildAlgebraItem(first)
                .performLeftProjectionOperation("preimage",FiniteSet.of(BigInteger.valueOf(3))).getResult());
        assertEquals(Arrays.asList("2","3"),m.flow(m.integerRelations,Collections.singletonList(first))
                .performLeftProjectionOperation("image",FiniteSet.of(BigInteger.ONE)).<BigInteger>performFlatAlgebraTransfer("elements").collect());
    }
    @Test public void transitiveClosureMatchesIndependentWarshallReachabilityOnAllThreeVertexGraphs() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int mask=0;mask<512;mask++) {
            List<Pair<BigInteger,BigInteger>> pairs=new ArrayList<>(); boolean[][] reachable=new boolean[3][3];
            for(int a=0;a<3;a++) for(int b=0;b<3;b++) if((mask&(1<<(3*a+b)))!=0) {
                reachable[a][b]=true; pairs.add(new Pair<>(BigInteger.valueOf(a),BigInteger.valueOf(b)));
            }
            for(int k=0;k<3;k++) for(int a=0;a<3;a++) for(int b=0;b<3;b++)
                reachable[a][b]|=reachable[a][k] && reachable[k][b];
            FiniteRelation<BigInteger,BigInteger> input=new FiniteRelation<>(m.integers.algebra(),m.integers.algebra(),new FiniteSet<>(pairs));
            FiniteRelation<BigInteger,BigInteger> closure=m.integerRelations.algebra().buildAlgebraItem(input)
                    .performOneOperandOperation("transitive-closure").getResult();
            for(int a=0;a<3;a++) for(int b=0;b<3;b++) assertEquals("graph "+mask+" pair "+a+","+b,
                    reachable[a][b],closure.relates(BigInteger.valueOf(a),BigInteger.valueOf(b)));
            assertEquals(closure,m.integerRelations.algebra().buildAlgebraItem(closure).performOneOperandOperation("transitive-closure").getResult());
        }
    }
    @Test public void finiteFunctionChecksAndForeignAlgebrasAreExplicit() {
        ConcreteMathematics m=new ConcreteMathematics(),other=new ConcreteMathematics();
        FiniteSet<BigInteger> carrier=FiniteSet.of(BigInteger.ONE,BigInteger.valueOf(2));
        FiniteRelation<BigInteger,BigInteger> function=relation(m,1,2,2,2);
        assertTrue(m.integerRelations.algebra().buildAlgebraItem(function)
                .<Boolean,FiniteSet<BigInteger>>performUnsafeOperation("is-function-on",carrier).getResult());
        assertFalse(m.integerRelations.algebra().buildAlgebraItem(relation(m,1,2,1,3))
                .<Boolean,FiniteSet<BigInteger>>performUnsafeOperation("is-function-on",carrier).getResult());
        assertTrue(relation(m).isTotalFunctionOn(FiniteSet.of()));
        assertFalse(function.isTotalFunctionOn(FiniteSet.of(BigInteger.ONE)));
        FiniteRelation<BigInteger,BigInteger> identity=m.integerSets.algebra().buildAlgebraItem(carrier)
                .<FiniteRelation<BigInteger,BigInteger>>performAlgebraTransfer("FiniteRelation(Z,Z).identity-on").getResult();
        assertEquals(function,identity.andThen(function)); assertEquals(identity,identity.inverse());
        assertNull(m.integerRelations.algebra().buildAlgebraItem(relation(other,1,2)));
        assertThrows(IllegalArgumentException.class,() -> function.andThen(relation(other,2,3)));
        assertThrows(IllegalArgumentException.class,() -> function.union(relation(other,1,2)));
        assertThrows(exceptions.NotMemberException.class,() -> m.integerRelations.algebra().buildAlgebraItem(function)
                .performUnsafeOperation("contains",new Pair<>("1","2")));
    }
    @Test public void relationFlowSerializationAndCompositionAssociativity() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics();
        FiniteRelation<BigInteger,BigInteger> a=relation(m,1,2,2,3),b=relation(m,2,4,3,5),c=relation(m,4,6,5,6);
        assertEquals(a.andThen(b).andThen(c),a.andThen(b.andThen(c)));
        IAlgebraFlow<BigInteger> flow=m.flow(m.integerRelations,Collections.singletonList(a))
                .performOperation("compose",b).performOperation("compose",c)
                .<FiniteSet<BigInteger>>performAlgebraTransfer("range").<BigInteger>performFlatAlgebraTransfer("elements");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("6"),restored.collect());
        assertEquals(Collections.singletonList("6"),restored.collect());
    }
}
