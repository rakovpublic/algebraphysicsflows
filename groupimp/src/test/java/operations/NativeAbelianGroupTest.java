package operations;

import algebra.IAlgebraItem;
import algebra.concrete.ConcreteMathematics;
import algebraflow.IAlgebraFlow;
import mathematics.core.MathFailure;
import mathematics.structures.AbelianGroupType;
import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class NativeAbelianGroupTest {
    private static BigInteger z(long value) { return BigInteger.valueOf(value); }
    private static AbelianGroupType group(long rank,long... orders) {
        List<BigInteger> factors=new ArrayList<>(); for(long order : orders) factors.add(z(order));
        return new AbelianGroupType(z(rank),factors);
    }
    private static void failure(MathFailure.Kind kind,Runnable action) { assertEquals(kind,assertThrows(MathFailure.class,action::run).kind()); }
    private static long gcd(long a,long b) { while(b!=0) { long remainder=a%b; a=b; b=remainder; } return a; }
    private static Map<Long,Integer> orderCounts(long a,long b) {
        Map<Long,Integer> counts=new TreeMap<>();
        for(long x=0;x<a;x++) for(long y=0;y<b;y++) {
            long first=a/gcd(a,x),second=b/gcd(b,y),order=first/gcd(first,second)*second;
            counts.merge(order,1,Integer::sum);
        }
        return counts;
    }
    @Test public void invariantFactorsMatchIndependentFiniteElementOrderCounts() {
        for(int a=1;a<=12;a++) for(int b=1;b<=12;b++) {
            AbelianGroupType type=group(0,a,b); List<BigInteger> factors=type.invariantFactors();
            assertEquals(z((long)a*b),type.order());
            assertEquals(orderCounts(a,b),orderCounts(factors.isEmpty()?1:factors.get(0).longValueExact(),factors.size()<2?1:factors.get(1).longValueExact()));
            for(int i=1;i<factors.size();i++) assertEquals(BigInteger.ZERO,factors.get(i).mod(factors.get(i-1)));
            assertEquals(type,group(0,b,a));
        }
        assertEquals(group(0,30,30),group(0,6,10,15));
        assertEquals(group(2,2,60),group(2,1,4,6,5));
        assertEquals(AbelianGroupType.ZERO,group(0,1,1));
    }
    @Test public void tensorHomTorAndExtRespectSourceTargetAndFreeSummands() {
        AbelianGroupType a=group(1,6),b=group(2,4,12);
        assertEquals(group(3,2,12,12),a.directSum(b));
        assertEquals(group(2,2,2,6,6,12,12),a.tensorProduct(b));
        assertEquals(group(2,2,2,12,12),a.homGroup(b));
        assertEquals(group(0,2,6),a.tor1(b));
        assertEquals(group(0,2,6,6,6),a.ext1(b));
        assertEquals(a,AbelianGroupType.Z.tensorProduct(a)); assertEquals(a,AbelianGroupType.Z.homGroup(a));
        assertEquals(group(1),a.homGroup(AbelianGroupType.Z));
        assertEquals(group(0,6),a.ext1(AbelianGroupType.Z)); assertEquals(AbelianGroupType.ZERO,AbelianGroupType.Z.ext1(a));
        assertEquals(AbelianGroupType.ZERO,a.tor1(AbelianGroupType.Z));
        for(int source=1;source<=12;source++) for(int target=1;target<=12;target++) {
            int maps=0;
            for(int image=0;image<target;image++) if((source*image)%target==0) maps++;
            assertEquals(z(maps),group(0,source).homGroup(group(0,target)).order());
        }
    }
    @Test public void directSumAndTensorLawsHoldAcrossMixedFiniteAndFreeTypes() {
        List<AbelianGroupType> types=Arrays.asList(group(0),group(1),group(0,2),group(0,3),group(1,4),group(2,2,6));
        for(AbelianGroupType a : types) for(AbelianGroupType b : types) for(AbelianGroupType c : types) {
            assertEquals(a.directSum(b).directSum(c),a.directSum(b.directSum(c)));
            assertEquals(a.tensorProduct(b).tensorProduct(c),a.tensorProduct(b.tensorProduct(c)));
            assertEquals(a.tensorProduct(b.directSum(c)),a.tensorProduct(b).directSum(a.tensorProduct(c)));
            assertEquals(a.homGroup(b.directSum(c)),a.homGroup(b).directSum(a.homGroup(c)));
            assertEquals(a.directSum(b).ext1(c),a.ext1(c).directSum(b.ext1(c)));
            assertEquals(a.tor1(b),b.tor1(a)); assertEquals(a.tensorProduct(b),b.tensorProduct(a));
        }
    }
    @Test public void finiteAndInfiniteMetadataHaveExplicitConventions() {
        AbelianGroupType type=group(3,4,12);
        assertEquals(z(3),type.freeRank()); assertEquals(z(5),type.minimalGenerators());
        assertEquals(group(0,4,12),type.torsionPart()); assertEquals(group(3),type.freePart());
        assertFalse(type.isFinite()); assertFalse(type.isCyclic()); assertFalse(type.isTorsionFree()); assertFalse(type.isTrivial());
        assertEquals(z(48),type.torsionPart().order()); assertEquals(z(12),type.torsionPart().exponent());
        assertEquals(BigInteger.ONE,AbelianGroupType.ZERO.order()); assertEquals(BigInteger.ONE,AbelianGroupType.ZERO.exponent());
        assertTrue(AbelianGroupType.ZERO.isCyclic()); assertTrue(AbelianGroupType.Z.isCyclic()); assertFalse(group(1,2).isCyclic());
        assertEquals(AbelianGroupType.Z,AbelianGroupType.cyclic(BigInteger.ZERO));
        assertEquals(AbelianGroupType.ZERO,AbelianGroupType.cyclic(BigInteger.ONE));
        assertEquals(group(6,4,4,12,12),type.repeat(z(2))); assertEquals(AbelianGroupType.ZERO,type.repeat(z(0)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,type::order); failure(MathFailure.Kind.OPERATION_UNDEFINED,type::exponent);
    }
    @Test public void normalizationAndResourceLimitsDoNotConfuseIsomorphismWithNonexistence() {
        List<BigInteger> input=new ArrayList<>(Arrays.asList(z(2),z(3))); AbelianGroupType type=new AbelianGroupType(z(0),input); input.clear();
        assertEquals(group(0,6),type); assertEquals(group(0,6).hashCode(),type.hashCode());
        assertThrows(UnsupportedOperationException.class,() -> type.invariantFactors().clear());
        failure(MathFailure.Kind.INVALID_MEMBER,() -> group(-1)); failure(MathFailure.Kind.INVALID_MEMBER,() -> group(0,0));
        failure(MathFailure.Kind.INVALID_MEMBER,() -> group(0,-2)); failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> group(1).repeat(z(-1)));
        failure(MathFailure.Kind.OPERATION_UNDEFINED,() -> AbelianGroupType.cyclic(z(-1)));
        BigInteger huge=BigInteger.TEN.pow(100);
        assertEquals(AbelianGroupType.free(huge.multiply(huge)),AbelianGroupType.free(huge).tensorProduct(AbelianGroupType.free(huge)));
        assertEquals(AbelianGroupType.free(huge),AbelianGroupType.Z.repeat(huge));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> group(0,2).repeat(huge));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new AbelianGroupType(z(0),Collections.nCopies(257,z(2))));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> new AbelianGroupType(z(0),Collections.nCopies(1025,z(1))));
        AbelianGroupType twos=new AbelianGroupType(z(0),Collections.nCopies(256,z(2)));
        AbelianGroupType threes=new AbelianGroupType(z(0),Collections.nCopies(256,z(3)));
        assertEquals(new AbelianGroupType(z(0),Collections.nCopies(256,z(6))),twos.directSum(threes));
        assertEquals(AbelianGroupType.ZERO,twos.tensorProduct(threes));
        AbelianGroupType many=new AbelianGroupType(z(0),Collections.nCopies(33,z(2)));
        failure(MathFailure.Kind.IMPLEMENTATION_FAILURE,() -> many.tensorProduct(many));
    }
    @Test public void nativeTypeOperationsReturnOriginalCarrierWrappersAndSerialize() throws Exception {
        ConcreteMathematics math=new ConcreteMathematics();
        IAlgebraItem<AbelianGroupType> cyclic=math.naturals.algebra().buildAlgebraItem(z(6)).performAlgebraTransfer("AbelianGroupType.cyclic");
        assertSame(math.abelianGroups.algebra(),cyclic.getAlgebra()); assertSame(math.mathTool.getAlgebra("AbelianGroupType"),cyclic.getAlgebra());
        assertEquals(group(0,2,12),cyclic.performOperation("direct-sum",group(0,4)).perform().getResult());
        assertSame(math.naturals.algebra(),cyclic.performAlgebraTransfer("order").getAlgebra());
        IAlgebraFlow<BigInteger> flow=math.flow(math.abelianGroups,Collections.singletonList(group(1,6)))
                .performOperation("ext1",group(2,4,12)).<BigInteger>performFlatAlgebraTransfer("invariant-factors");
        ByteArrayOutputStream bytes=new ByteArrayOutputStream(); try(ObjectOutputStream out=new ObjectOutputStream(bytes)) { out.writeObject(flow); }
        IAlgebraFlow<?> restored;
        try(ObjectInputStream in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) { restored=(IAlgebraFlow<?>)in.readObject(); }
        assertEquals(Arrays.asList("2","6","6","6"),restored.collect()); assertEquals(restored.collect(),restored.collect());
    }
}
