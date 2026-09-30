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

public class NativeAdjunctionTest {
    private static IAlgebraItem<FiniteAdjunction> item(ConcreteMathematics m,FiniteAdjunction a) { return m.adjunctions.algebra().buildAlgebraItem(a); }
    private static FiniteFunctor chainMap(FiniteCategory source,FiniteCategory target,int... images) {
        Map<BigInteger,BigInteger> objects=new TreeMap<>(),arrows=new TreeMap<>();
        for(int i=0;i<images.length;i++) objects.put(n(i),n(images[i]));
        for(BigInteger arrow : source.arrows().keySet()) arrows.put(arrow,target.hom(
                objects.get(source.source(arrow)),objects.get(source.target(arrow))).get(0));
        return new FiniteFunctor(source,target,objects,arrows);
    }
    private static FiniteCategory chain(ConcreteMathematics m,int size) {
        int[] labels=new int[size]; for(int i=0;i<size;i++) labels[i]=i;
        return relationCategory(m,false,labels);
    }
    private static void undefined(Runnable action) {
        assertEquals(MathFailure.Kind.OPERATION_UNDEFINED,assertThrows(MathFailure.class,action::run).kind());
    }
    @Test public void initialAndTerminalInclusionsGiveNoninvertibleAdjunctions() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory point=chain(m,1),interval=chain(m,2);
        FiniteFunctor bottom=chainMap(point,interval,0),collapse=chainMap(interval,point,0,0);
        IAlgebraItem<FiniteAdjunction> wrapped=m.functors.algebra().buildAlgebraItem(bottom).performAlgebraTransfer("FiniteAdjunction.from-left");
        assertSame(m.adjunctions.algebra(),wrapped.getAlgebra()); FiniteAdjunction a=wrapped.getResult();
        assertEquals(collapse,a.right); assertEquals(map(0,0),a.unit.componentMap()); assertEquals(map(0,0,1,1),a.counit.componentMap());
        assertFalse(a.isEquivalence()); assertFalse(a.counit.isIsomorphism()); assertTrue(a.unit.isIsomorphism());
        assertEquals(a,FiniteAdjunction.fromRight(collapse)); undefined(a::toEquivalence);
        FiniteAdjunction other=FiniteAdjunction.fromLeft(collapse);
        assertEquals(chainMap(point,interval,1),other.right); assertEquals(map(0,1,1,2),other.unit.componentMap());
        assertFalse(other.unit.isIsomorphism()); assertTrue(other.counit.isIsomorphism());
        undefined(() -> FiniteAdjunction.fromRight(bottom));
        IAlgebraItem<BigInteger> transposed=item(m,a).performUnsafeOperation("transpose",new Pair<>(n(0),n(1)));
        assertSame(m.integers.algebra(),transposed.getAlgebra()); assertEquals(n(0),transposed.getResult());
        assertEquals(n(1),item(m,a).performUnsafeOperation("untranspose",new Pair<>(n(1),n(0))).getResult());
        assertEquals(a,a.opposite().opposite());
        assertEquals(a.right.opposite(),a.opposite().left); assertEquals(a.counit.opposite(),a.opposite().unit);
    }
    @Test public void everySmallMonotoneMapMatchesIndependentOrderAdjointFormulas() {
        ConcreteMathematics m=new ConcreteMathematics();
        for(int n=0;n<=4;n++) for(int p=0;p<=4;p++) {
            FiniteCategory source=chain(m,n),target=chain(m,p); int count=1;
            for(int i=0;i<n;i++) count*=p;
            for(int code=0;code<count;code++) {
                int[] images=new int[n]; int digits=code; boolean monotone=true;
                for(int i=0;i<n;i++) { images[i]=digits%p; digits/=p; if(i>0 && images[i-1]>images[i]) monotone=false; }
                if(!monotone) continue;
                FiniteFunctor f=chainMap(source,target,images); int[] right=new int[p],left=new int[p];
                boolean hasRight=true,hasLeft=true;
                for(int j=0;j<p;j++) {
                    right[j]=-1; left[j]=-1;
                    for(int i=0;i<n;i++) {
                        if(images[i]<=j) right[j]=i;
                        if(j<=images[i] && left[j]<0) left[j]=i;
                    }
                    if(right[j]<0) hasRight=false; if(left[j]<0) hasLeft=false;
                }
                if(hasRight) {
                    FiniteAdjunction a=m.functors.algebra().buildAlgebraItem(f).<FiniteAdjunction>performAlgebraTransfer("FiniteAdjunction.from-left").getResult();
                    assertEquals(f,a.left); assertEquals(chainMap(target,source,right),a.right);
                    for(int i=0;i<n;i++) for(int j=0;j<p;j++) {
                        Map<BigInteger,BigInteger> expected=new TreeMap<>();
                        if(images[i]<=j) expected.put(target.hom(n(images[i]),n(j)).get(0),source.hom(n(i),n(right[j])).get(0));
                        assertEquals(expected,a.homMap(n(i),n(j)));
                    }
                } else undefined(() -> FiniteAdjunction.fromLeft(f));
                if(hasLeft) {
                    FiniteAdjunction a=m.functors.algebra().buildAlgebraItem(f).<FiniteAdjunction>performAlgebraTransfer("FiniteAdjunction.from-right").getResult();
                    assertEquals(f,a.right); assertEquals(chainMap(target,source,left),a.left);
                } else undefined(() -> FiniteAdjunction.fromRight(f));
            }
        }
    }
    /** Product of a two-object chain with the noncommutative full transformation monoid on two points. */
    private static FiniteCategory transformationProduct() {
        int[][] maps={{0,1},{1,0},{0,0},{1,1}}; int[][] pairs={{0,0},{0,1},{1,1}};
        Map<BigInteger,Pair<BigInteger,BigInteger>> arrows=new TreeMap<>();
        Map<Pair<BigInteger,BigInteger>,BigInteger> table=new LinkedHashMap<>();
        for(int a=0;a<12;a++) arrows.put(n(a),new Pair<>(n(pairs[a/4][0]),n(pairs[a/4][1])));
        for(int a=0;a<12;a++) for(int b=0;b<12;b++) if(pairs[a/4][1]==pairs[b/4][0]) {
            int type=pairs[a/4][0]==1?2:(pairs[b/4][1]==0?0:1);
            for(int k=0;k<4;k++) if(maps[k][0]==maps[b%4][maps[a%4][0]] && maps[k][1]==maps[b%4][maps[a%4][1]])
                table.put(new Pair<>(n(a),n(b)),n(4*type+k));
        }
        return new FiniteCategory(set(0,1),arrows,map(0,0,1,8),table);
    }
    @Test public void universalSearchAndHomBijectionsHandleParallelNoncommutingArrows() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory product=transformationProduct();
        int[][] multiplication=new int[4][4];
        for(int a=0;a<4;a++) for(int b=0;b<4;b++) multiplication[a][b]=product.compose(n(a),n(b)).intValue();
        FiniteCategory monoid=monoid(multiplication,0); Map<BigInteger,BigInteger> arrows=new TreeMap<>();
        for(int a=0;a<12;a++) arrows.put(n(a),n(a%4));
        FiniteFunctor projection=new FiniteFunctor(product,monoid,map(0,0,1,0),arrows);
        FiniteAdjunction adj=FiniteAdjunction.fromLeft(projection);
        assertEquals(map(0,1),adj.right.objectMap()); assertEquals(map(0,8,1,9,2,10,3,11),adj.right.arrowMap());
        assertEquals(map(0,4,1,8),adj.unit.componentMap()); assertFalse(adj.isEquivalence());
        assertNotEquals(monoid.compose(n(1),n(2)),monoid.compose(n(2),n(1)));
        for(int c=0;c<2;c++) {
            Map<BigInteger,BigInteger> expected=new TreeMap<>();
            for(int h=0;h<4;h++) {
                expected.put(n(h),n((c+1)*4+h));
                assertEquals(n((c+1)*4+h),adj.transpose(n(c),n(h)));
                assertEquals(n(h),adj.untranspose(n(0),n((c+1)*4+h)));
            }
            IAlgebraItem<FiniteFunction<BigInteger,BigInteger>> hom=item(m,adj).performUnsafeOperation("hom-map",new Pair<>(n(c),n(0)));
            assertSame(m.integerFunctions.algebra(),hom.getAlgebra()); assertEquals(expected,hom.getResult().mapping());
            assertTrue(hom.getResult().isBijective()); assertSame(m.integers.algebra(),hom.getResult().source);
        }
        // Equal hom cardinalities alone do not establish the required bijection.
        FiniteFunctor collapse=new FiniteFunctor(monoid,monoid,map(0,0),map(0,0,1,0,2,0,3,0));
        undefined(() -> FiniteAdjunction.fromLeft(collapse)); undefined(() -> FiniteAdjunction.fromRight(collapse));
        assertEquals(map(0,0),FiniteAdjunction.fromRight(projection).left.objectMap());
        undefined(() -> adj.transpose(n(2),n(0))); undefined(() -> adj.transpose(n(0),n(99)));
        undefined(() -> adj.untranspose(n(0),n(0))); undefined(() -> adj.untranspose(n(2),n(4)));
        undefined(() -> adj.homMap(n(0),n(99)));
        assertThrows(UnsupportedOperationException.class,() -> adj.homMap(n(0),n(0)).clear());
        // A noncentral invertible component makes the order in eta;R(h) observable.
        Map<BigInteger,BigInteger> conjugation=new TreeMap<>();
        for(int h=0;h<4;h++) conjugation.put(n(h),n(multiplication[multiplication[1][h]][1]));
        FiniteFunctor twist=new FiniteFunctor(monoid,monoid,map(0,0),conjugation),id=FiniteFunctor.identity(monoid);
        FiniteAdjunction twisted=new FiniteAdjunction(twist,id,
                new FiniteNaturalTransformation(id,twist,map(0,1)),new FiniteNaturalTransformation(twist,id,map(0,1)));
        for(int h=0;h<4;h++) {
            assertEquals(n(multiplication[1][h]),twisted.transpose(n(0),n(h)));
            assertEquals(n(h),twisted.untranspose(n(0),twisted.transpose(n(0),n(h))));
        }
    }
    @Test public void constructorRejectsMistypedWitnessesAndEachTriangleFailure() {
        FiniteFunctor id=FiniteFunctor.identity(cyclic(3)); FiniteNaturalTransformation one=new FiniteNaturalTransformation(id,id,map(0,1));
        assertEquals(MathFailure.Kind.INVALID_MEMBER,assertThrows(MathFailure.class,() -> new FiniteAdjunction(id,id,one,one)).kind());
        FiniteFunctor point=FiniteFunctor.identity(discrete(0));
        assertThrows(MathFailure.class,() -> new FiniteAdjunction(id,point,one,one));
        assertThrows(MathFailure.class,() -> new FiniteAdjunction(id,id,FiniteNaturalTransformation.identity(point),one));
        FiniteCategory idem=monoid(new int[][]{{0,1},{1,1}},0);
        FiniteFunctor l=new FiniteFunctor(idem,discrete(0),map(0,0),map(0,0,1,0));
        FiniteFunctor r=new FiniteFunctor(discrete(0),idem,map(0,0),map(0,0));
        FiniteNaturalTransformation unit=new FiniteNaturalTransformation(FiniteFunctor.identity(idem),r.compose(l),map(0,1));
        FiniteNaturalTransformation counit=FiniteNaturalTransformation.identity(point);
        // The left triangle holds after l collapses the nonidentity idempotent; the right one fails.
        assertTrue(assertThrows(MathFailure.class,() -> new FiniteAdjunction(l,r,unit,counit)).getMessage().contains("right triangle"));
        assertTrue(assertThrows(MathFailure.class,() -> new FiniteAdjunction(r.opposite(),l.opposite(),counit.opposite(),unit.opposite())).getMessage().contains("left triangle"));
    }
    @Test public void compositionPreservesNoninvertibleWitnessesAndAdjointOrder() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteCategory c=chain(m,3); List<FiniteAdjunction> values=new ArrayList<>();
        for(int i=0;i<3;i++) for(int j=i;j<3;j++) values.add(FiniteAdjunction.fromLeft(chainMap(c,c,0,i,j)));
        FiniteAdjunction identity=FiniteAdjunction.identity(c);
        for(FiniteAdjunction a : values) {
            assertEquals(a,a.compose(identity)); assertEquals(a,identity.compose(a));
            for(FiniteAdjunction b : values) {
                FiniteAdjunction composed=item(m,a).performOperation("compose",b).perform().getResult();
                int[] expected=new int[3]; for(int i=0;i<3;i++) expected[i]=a.left.mapObject(b.left.mapObject(n(i))).intValue();
                assertEquals(FiniteAdjunction.fromLeft(chainMap(c,c,expected)),composed);
                for(int i=0;i<3;i++) assertEquals(b.right.mapObject(a.right.mapObject(n(i))),composed.right.mapObject(n(i)));
                assertEquals(b.opposite().compose(a.opposite()),composed.opposite());
                for(FiniteAdjunction d : values) assertEquals(a.compose(b).compose(d),a.compose(b.compose(d)));
            }
        }
        undefined(() -> identity.compose(FiniteAdjunction.identity(discrete(10))));
    }
    @Test public void conversionRetainsChosenWitnessesAndCorrespondenceDependsOnThem() {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteFunctor id=FiniteFunctor.identity(cyclic(3));
        for(int r=0;r<3;r++) {
            FiniteEquivalence e=new FiniteEquivalence(id,id,new FiniteNaturalTransformation(id,id,map(0,r)),
                    new FiniteNaturalTransformation(id,id,map(0,(3-r)%3)));
            IAlgebraItem<FiniteAdjunction> wrapped=m.equivalences.algebra().buildAlgebraItem(e).performAlgebraTransfer("FiniteAdjunction.from-equivalence");
            FiniteAdjunction a=wrapped.getResult(); assertTrue(a.isEquivalence());
            IAlgebraItem<FiniteEquivalence> back=wrapped.performAlgebraTransfer("to-equivalence");
            assertSame(m.equivalences.algebra(),back.getAlgebra()); assertEquals(e,back.getResult());
            for(int h=0;h<3;h++) {
                assertEquals(n((h+r)%3),a.transpose(n(0),n(h)));
                assertEquals(n((h+3-r)%3),a.untranspose(n(0),n(h)));
            }
            assertEquals(a,a.opposite().opposite()); assertEquals(a.hashCode(),FiniteAdjunction.fromEquivalence(e).hashCode());
        }
        FiniteAdjunction empty=FiniteAdjunction.identity(discrete());
        assertEquals(empty,FiniteAdjunction.fromLeft(FiniteFunctor.identity(discrete())));
        assertEquals(empty,FiniteAdjunction.fromRight(FiniteFunctor.identity(discrete())));
        undefined(() -> empty.homMap(n(0),n(0)));
        FiniteAdjunction disconnected=FiniteAdjunction.identity(discrete(0,1));
        FiniteFunction<BigInteger,BigInteger> noArrows=item(m,disconnected)
                .<FiniteFunction<BigInteger,BigInteger>,Pair<BigInteger,BigInteger>>performUnsafeOperation("hom-map",new Pair<>(n(0),n(1))).getResult();
        assertEquals(set(),noArrows.domain); assertEquals(set(),noArrows.codomain); assertTrue(noArrows.isBijective());
        undefined(() -> disconnected.transpose(n(0),n(1))); undefined(() -> disconnected.untranspose(n(0),n(1)));
    }
    @Test public void constructedAdjointsRunThroughSerializedNativeFlows() throws Exception {
        ConcreteMathematics m=new ConcreteMathematics(); FiniteFunctor f=chainMap(chain(m,1),chain(m,2),0);
        IAlgebraFlow<BigInteger> flow=m.flow(m.functors,Collections.singletonList(f))
                .<FiniteAdjunction>performAlgebraTransfer("FiniteAdjunction.from-left")
                .<FiniteFunction<BigInteger,BigInteger>,Pair<BigInteger,BigInteger>>performAlgebraUnsafe("hom-map",new Pair<>(n(0),n(1)))
                .<BigInteger>performFlatAlgebraTransfer("values").performOneOperandOperation("negate");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Collections.singletonList("0"),restored.collect()); assertEquals(Collections.singletonList("0"),restored.collect());
        assertEquals(Collections.singletonList("1"),m.flow(m.functors,Collections.singletonList(f))
                .<FiniteAdjunction>performAlgebraTransfer("FiniteAdjunction.from-left")
                .<BigInteger,Pair<BigInteger,BigInteger>>performAlgebraUnsafe("untranspose",new Pair<>(n(1),n(0))).collect());
    }
}
