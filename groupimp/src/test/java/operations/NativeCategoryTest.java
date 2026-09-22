package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.foundations.*;
import mathematics.structures.FiniteCategory;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeCategoryTest {
    private static BigInteger n(int value) { return BigInteger.valueOf(value); }
    private static Pair<BigInteger,BigInteger> pair(int a,int b) { return new Pair<>(n(a),n(b)); }
    private static FiniteCategory monoid(int[][] product,int identity) {
        Map<BigInteger,Pair<BigInteger,BigInteger>> arrows=new LinkedHashMap<>();
        Map<Pair<BigInteger,BigInteger>,BigInteger> table=new LinkedHashMap<>();
        for(int i=0;i<product.length;i++) {
            arrows.put(n(i),pair(0,0));
            for(int j=0;j<product.length;j++) table.put(pair(i,j),n(product[i][j]));
        }
        return new FiniteCategory(FiniteSet.of(n(0)),arrows,Collections.singletonMap(n(0),n(identity)),table);
    }
    private static IAlgebraItem<FiniteCategory> item(ConcreteMathematics m,FiniteCategory category) {
        return m.categories.algebra().buildAlgebraItem(category);
    }
    private static FiniteRelation<BigInteger,BigInteger> chain(ConcreteMathematics m) {
        return new FiniteRelation<>(m.integers.algebra(),m.integers.algebra(),FiniteSet.of(pair(0,0),pair(0,1),pair(1,1)));
    }
    @Test public void allThreeArrowUnitalTablesAreAcceptedExactlyWhenAssociative() {
        ConcreteMathematics m=new ConcreteMathematics(); int accepted=0,rejected=0;
        for(int code=0;code<81;code++) {
            int[][] table={{0,1,2},{1,0,0},{2,0,0}}; int digits=code;
            for(int i=1;i<3;i++) for(int j=1;j<3;j++) { table[i][j]=digits%3; digits/=3; }
            boolean associative=true;
            for(int i=0;i<3;i++) for(int j=0;j<3;j++) for(int k=0;k<3;k++)
                if(table[table[i][j]][k]!=table[i][table[j][k]]) associative=false;
            if(!associative) {
                assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,() -> monoid(table,0)).kind());
                rejected++; continue;
            }
            accepted++; FiniteCategory category=monoid(table,0);
            assertEquals(category,item(m,category).performOneOperandOperation("opposite").performOneOperandOperation("opposite").perform().getResult());
            boolean group=true;
            for(int i=0;i<3;i++) {
                List<BigInteger> expected=new ArrayList<>(),actual=new ArrayList<>();
                for(int j=0;j<3;j++) if(table[i][j]==0 && table[j][i]==0) expected.add(n(j));
                if(expected.isEmpty()) group=false;
                for(IAlgebraItem<BigInteger> value : item(m,category).performLeftProjectionFlatOperation("inverse-of",n(i))) {
                    assertSame(m.integers.algebra(),value.getAlgebra()); actual.add(value.getResult());
                }
                assertEquals(expected,actual);
            }
            assertEquals(group,item(m,category).<Boolean>performAlgebraTransfer("is-groupoid").getResult());
            assertFalse(category.isThin()); assertTrue(category.initialObjects().isEmpty()); assertTrue(category.terminalObjects().isEmpty());
        }
        assertTrue(accepted>0); assertTrue(rejected>0); assertEquals(81,accepted+rejected);
    }
    @Test public void invalidTypingMissingCompositionsAndIdentityFailuresAreRejected() {
        FiniteCategory c=FiniteCategory.discrete(FiniteSet.of(n(1),n(2)));
        Map<BigInteger,Pair<BigInteger,BigInteger>> arrows=new LinkedHashMap<>(c.arrows());
        Map<BigInteger,BigInteger> ids=new LinkedHashMap<>(c.identities());
        Map<Pair<BigInteger,BigInteger>,BigInteger> table=new LinkedHashMap<>(c.composition());
        FiniteCategory copy=new FiniteCategory(c.objects(),arrows,ids,table);
        arrows.clear(); ids.clear(); table.clear(); assertEquals(c,copy);
        assertThrows(UnsupportedOperationException.class,() -> c.arrows().clear());
        assertThrows(UnsupportedOperationException.class,() -> c.identities().clear());
        assertThrows(UnsupportedOperationException.class,() -> c.composition().clear());
        assertThrows(MathFailure.class,() -> new FiniteCategory(c.objects(),c.arrows(),Collections.singletonMap(n(1),n(1)),c.composition()));
        Map<BigInteger,BigInteger> wrongIdentity=new LinkedHashMap<>(c.identities()); wrongIdentity.put(n(1),n(2));
        assertThrows(MathFailure.class,() -> new FiniteCategory(c.objects(),c.arrows(),wrongIdentity,c.composition()));
        Map<BigInteger,Pair<BigInteger,BigInteger>> wrongEnds=new LinkedHashMap<>(c.arrows()); wrongEnds.put(n(1),pair(1,99));
        assertThrows(MathFailure.class,() -> new FiniteCategory(c.objects(),wrongEnds,c.identities(),c.composition()));
        assertThrows(MathFailure.class,() -> new FiniteCategory(c.objects(),c.arrows(),c.identities(),Collections.singletonMap(pair(1,1),n(1))));
        Map<Pair<BigInteger,BigInteger>,BigInteger> extra=new LinkedHashMap<>(c.composition()); extra.put(pair(1,2),n(1));
        assertThrows(MathFailure.class,() -> new FiniteCategory(c.objects(),c.arrows(),c.identities(),extra));
        Map<Pair<BigInteger,BigInteger>,BigInteger> badResult=new LinkedHashMap<>(c.composition()); badResult.put(pair(1,1),n(2));
        assertThrows(MathFailure.class,() -> new FiniteCategory(c.objects(),c.arrows(),c.identities(),badResult));
        assertThrows(MathFailure.class,() -> monoid(new int[][]{{0,0},{1,0}},0));
    }
    @Test public void everyThreePointRelationIsConvertedExactlyWhenItIsAPreorderOnItsSupport() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int mask=0;mask<512;mask++) {
            boolean[][] edges=new boolean[3][3]; boolean[] support=new boolean[3];
            List<Pair<BigInteger,BigInteger>> pairs=new ArrayList<>();
            for(int i=0;i<3;i++) for(int j=0;j<3;j++) if((mask&(1<<(3*i+j)))!=0) {
                edges[i][j]=true; support[i]=true; support[j]=true; pairs.add(pair(i,j));
            }
            boolean preorder=true,symmetric=true;
            for(int i=0;i<3;i++) {
                if(support[i] && !edges[i][i]) preorder=false;
                for(int j=0;j<3;j++) {
                    if(edges[i][j]!=edges[j][i]) symmetric=false;
                    for(int k=0;k<3;k++) if(edges[i][j] && edges[j][k] && !edges[i][k]) preorder=false;
                }
            }
            FiniteRelation<BigInteger,BigInteger> relation=new FiniteRelation<>(m.integers.algebra(),m.integers.algebra(),new FiniteSet<>(pairs));
            if(!preorder) {
                assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,() -> m.integerRelations.algebra()
                        .buildAlgebraItem(relation).performAlgebraTransfer("FiniteCategory.from-preorder")).kind());
                continue;
            }
            IAlgebraItem<FiniteCategory> converted=m.integerRelations.algebra().buildAlgebraItem(relation).performAlgebraTransfer("FiniteCategory.from-preorder");
            assertSame(m.categories.algebra(),converted.getAlgebra()); FiniteCategory category=converted.getResult();
            assertTrue(category.isThin()); assertEquals(symmetric,category.isGroupoid());
            assertEquals(relation,item(m,category).<FiniteRelation<BigInteger,BigInteger>>performAlgebraTransfer("underlying-relation").getResult());
            assertEquals(relation.inverse(),item(m,category.opposite()).<FiniteRelation<BigInteger,BigInteger>>performAlgebraTransfer("underlying-relation").getResult());
            List<BigInteger> initial=new ArrayList<>(),terminal=new ArrayList<>();
            for(int i=0;i<3;i++) if(support[i]) {
                boolean isInitial=true,isTerminal=true;
                for(int j=0;j<3;j++) if(support[j]) {
                    assertEquals(edges[i][j]?1:0,category.hom(n(i),n(j)).size());
                    if(!edges[i][j]) isInitial=false; if(!edges[j][i]) isTerminal=false;
                }
                if(isInitial) initial.add(n(i)); if(isTerminal) terminal.add(n(i));
            }
            assertEquals(initial,category.initialObjects()); assertEquals(terminal,category.terminalObjects());
            Collections.reverse(pairs);
            assertEquals(category,FiniteCategory.fromPreorder(new FiniteRelation<>(m.integers.algebra(),m.integers.algebra(),new FiniteSet<>(pairs))));
        }
    }
    @Test public void noncommutativeMonoidCategoryUsesPathOrderAndHasParallelArrows() {
        ConcreteMathematics m=new ConcreteMathematics(); int[][] product=new int[4][4];
        // All maps on {0,1}; code is image(0)+2*image(1), identity code 2.
        for(int a=0;a<4;a++) for(int b=0;b<4;b++) {
            int[] first={a%2,a/2},second={b%2,b/2}; product[a][b]=second[first[0]]+2*second[first[1]];
        }
        FiniteCategory c=monoid(product,2);
        assertEquals(n(3),item(m,c).<BigInteger,Pair<BigInteger,BigInteger>>performUnsafeOperation("compose",pair(1,3)).getResult());
        assertEquals(n(0),item(m,c).<BigInteger,Pair<BigInteger,BigInteger>>performUnsafeOperation("compose",pair(3,1)).getResult());
        assertEquals(n(0),c.opposite().compose(n(1),n(3)));
        assertEquals(Arrays.asList(n(0),n(1),n(2),n(3)),c.hom(n(0),n(0)));
        assertEquals(Arrays.asList(n(1),n(2)),c.isomorphisms()); assertFalse(c.isThin()); assertFalse(c.isGroupoid());
        assertTrue(c.inverseOf(n(0)).isEmpty());
        FiniteCategory twoObjects=FiniteCategory.fromPreorder(chain(m));
        IAlgebraItem<BigInteger> source=item(m,twoObjects).performLeftProjectionOperation("source",n(1));
        assertSame(m.integers.algebra(),source.getAlgebra()); assertEquals(n(0),source.getResult());
        assertEquals(n(1),item(m,twoObjects).performLeftProjectionOperation("target",n(1)).getResult());
        assertEquals(n(2),item(m,twoObjects).performLeftProjectionOperation("identity",n(1)).getResult());
        assertEquals(n(1),twoObjects.compose(n(1),n(2)));
    }
    @Test public void homAndUniversalObjectFlowsComposeWithExistingIntegerOperations() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory c=FiniteCategory.fromPreorder(chain(m));
        assertEquals(Collections.singletonList("-1"),m.flow(m.categories,Collections.singletonList(c))
                .<BigInteger,Pair<BigInteger,BigInteger>>performFlatAlgebraUnsafe("hom",pair(0,1)).performOneOperandOperation("negate").collect());
        assertTrue(m.flow(m.categories,Collections.singletonList(c)).performLeftProjectionFlatOperation("inverse-of",n(1)).collect().isEmpty());
        assertEquals(Collections.singletonList("0"),m.flow(m.categories,Collections.singletonList(c)).<BigInteger>performFlatAlgebraTransfer("initial-objects").collect());
        assertEquals(Collections.singletonList("1"),m.flow(m.categories,Collections.singletonList(c)).<BigInteger>performFlatAlgebraTransfer("terminal-objects").collect());
        assertEquals(Collections.singletonList("1"),m.flow(m.categories,Collections.singletonList(c)).performOneOperandOperation("opposite")
                .<BigInteger>performFlatAlgebraTransfer("initial-objects").collect());
        assertEquals(Collections.singletonList("1"),m.flow(m.integerSets,Collections.singletonList(FiniteSet.of(n(9))))
                .<FiniteCategory>performAlgebraTransfer("FiniteCategory.discrete-on").<BigInteger>performAlgebraTransfer("arrow-count").collect());
        assertEquals(Collections.singletonList(n(9)),FiniteCategory.discrete(FiniteSet.of(n(9))).initialObjects());
        assertEquals(Collections.singletonList(n(9)),FiniteCategory.discrete(FiniteSet.of(n(9))).terminalObjects());
        FiniteCategory empty=FiniteCategory.discrete(FiniteSet.of());
        assertTrue(empty.isGroupoid()); assertTrue(empty.isThin()); assertTrue(empty.initialObjects().isEmpty());
    }
    @Test public void categoryFlowsSerializeAndUndefinedQueriesRemainDistinctFromEmptyResults() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory c=FiniteCategory.fromPreorder(chain(m));
        assertThrows(MathFailure.class,() -> c.compose(n(1),n(0)));
        assertThrows(MathFailure.class,() -> c.identity(n(99)));
        assertThrows(MathFailure.class,() -> c.source(n(99)));
        assertThrows(MathFailure.class,() -> c.hom(n(0),n(99)));
        assertThrows(MathFailure.class,() -> c.inverseOf(n(99)));
        assertTrue(c.hom(n(1),n(0)).isEmpty());
        List<BigInteger> labels=new ArrayList<>(); for(int i=0;i<=FiniteCategory.MAX_ARROWS;i++) labels.add(n(i));
        assertEquals(MathFailure.Kind.IMPLEMENTATION_FAILURE,assertThrows(MathFailure.class,() -> FiniteCategory.discrete(new FiniteSet<>(labels))).kind());
        labels.remove(labels.size()-1); assertEquals(128,FiniteCategory.discrete(new FiniteSet<>(labels)).arrows().size());
        IAlgebraFlow<BigInteger> flow=m.flow(m.integerRelations,Collections.singletonList(chain(m)))
                .<FiniteCategory>performAlgebraTransfer("FiniteCategory.from-preorder")
                .performOneOperandOperation("opposite")
                .<BigInteger,Pair<BigInteger,BigInteger>>performFlatAlgebraUnsafe("hom",pair(1,0));
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("1"),restored.collect()); assertEquals(Collections.singletonList("1"),restored.collect());
    }
}
